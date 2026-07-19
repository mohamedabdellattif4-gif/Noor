package com.noor.core.media

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.noor.domain.model.Ayah
import com.noor.domain.model.Reciter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class QuranAudioController @Inject constructor(
    @ApplicationContext context: Context,
) : QuranAudioPlayer {
    private val appContext = context.applicationContext
    private val controllerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _playbackState = MutableStateFlow<QuranPlaybackState>(QuranPlaybackState.Idle)
    override val playbackState: StateFlow<QuranPlaybackState> = _playbackState.asStateFlow()
    private val _playbackInfo = MutableStateFlow(QuranPlaybackInfo())
    override val playbackInfo: StateFlow<QuranPlaybackInfo> = _playbackInfo.asStateFlow()
    private var positionPollingJob: Job? = null

    private val controllerFuture = MediaController.Builder(
        appContext,
        SessionToken(appContext, ComponentName(appContext, QuranPlaybackService::class.java)),
    ).buildAsync()

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            updateFromController(player)
        }

        override fun onPlayerError(error: PlaybackException) {
            _playbackState.value = QuranPlaybackState.Error
            stopPositionPolling()
        }
    }

    init {
        controllerFuture.addListener(
            {
                runCatching {
                    controllerFuture.get().also { controller ->
                        controller.addListener(listener)
                        updateFromController(controller)
                    }
                }.onFailure {
                    _playbackState.value = QuranPlaybackState.Error
                    _playbackInfo.value = QuranPlaybackInfo()
                }
            },
            ContextCompat.getMainExecutor(appContext),
        )
        controllerScope.launch {
            _playbackInfo.subscriptionCount.collect { collectorCount ->
                if (collectorCount > 0) {
                    withController(::updatePositionPolling)
                } else {
                    stopPositionPolling()
                }
            }
        }
    }

    override fun playAyah(ayah: Ayah, reciter: Reciter, surahName: String) {
        play(listOf(ayah), reciter, surahName, startAyahId = ayah.id)
    }

    override fun playSurah(
        ayahs: List<Ayah>,
        reciter: Reciter,
        surahName: String,
        startAyahId: Int?,
    ) {
        if (ayahs.isEmpty()) return
        play(ayahs, reciter, surahName, startAyahId)
    }

    override fun togglePause() = withController { controller ->
        // isPlaying is false while buffering even when playback is scheduled to start. Using
        // playWhenReady preserves the expected pause/resume behavior across READY and BUFFERING.
        if (controller.playWhenReady) controller.pause() else controller.play()
    }

    override fun skipToPrevious() = withController { controller ->
        when {
            controller.hasPreviousMediaItem() -> controller.seekToPreviousMediaItem()
            controller.currentMediaItem != null -> controller.seekTo(0L)
        }
    }

    override fun skipToNext() = withController { controller ->
        if (controller.hasNextMediaItem()) controller.seekToNextMediaItem()
    }

    override fun seekTo(positionMs: Long) = withController { controller ->
        if (controller.currentMediaItem == null) return@withController
        val duration = controller.duration.takeIf { it != C.TIME_UNSET && it > 0L }
        val safePosition = if (duration == null) {
            positionMs.coerceAtLeast(0L)
        } else {
            positionMs.coerceIn(0L, duration)
        }
        controller.seekTo(safePosition)
    }

    override fun stop() = withController { controller ->
        controller.stop()
        controller.clearMediaItems()
        _playbackState.value = QuranPlaybackState.Idle
        _playbackInfo.value = QuranPlaybackInfo()
        stopPositionPolling()
    }

    private fun play(
        ayahs: List<Ayah>,
        reciter: Reciter,
        surahName: String,
        startAyahId: Int?,
    ) = withController { controller ->
        val items = ayahs.map { ayah ->
            MediaItem.Builder()
                .setMediaId("${QuranPlaybackInfo.AYAH_MEDIA_ID_PREFIX}${ayah.id}")
                .setUri(QuranAudioUrlFactory.create(reciter, ayah))
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(
                            appContext.getString(
                                R.string.media_ayah_title,
                                surahName,
                                ayah.numberInSurah,
                            ),
                        )
                        .setArtist(appContext.getString(reciter.displayNameResource))
                        .build(),
                )
                .build()
        }
        val startIndex = startAyahId?.let { id -> ayahs.indexOfFirst { it.id == id } }
            ?.takeIf { it >= 0 } ?: 0
        controller.setMediaItems(items, startIndex, 0L)
        controller.prepare()
        controller.play()
        updateFromController(controller)
    }

    private fun withController(block: (MediaController) -> Unit) {
        controllerFuture.addListener(
            {
                runCatching { block(controllerFuture.get()) }
                    .onFailure {
                        _playbackState.value = QuranPlaybackState.Error
                        stopPositionPolling()
                    }
            },
            ContextCompat.getMainExecutor(appContext),
        )
    }

    private fun updateFromController(controller: Player) {
        updatePlaybackState(controller)
        updatePlaybackInfo(controller)
        updatePositionPolling(controller)
    }

    private fun updatePlaybackState(controller: Player) {
        _playbackState.value = when {
            controller.playerError != null -> QuranPlaybackState.Error
            controller.playbackState == Player.STATE_BUFFERING -> QuranPlaybackState.Buffering
            controller.isPlaying -> QuranPlaybackState.Playing(controller.currentMediaItem?.mediaId)
            controller.playbackState == Player.STATE_READY && controller.currentMediaItem != null -> {
                QuranPlaybackState.Paused
            }
            else -> QuranPlaybackState.Idle
        }
    }

    private fun updatePlaybackInfo(controller: Player) {
        val duration = controller.duration
            .takeIf { it != C.TIME_UNSET && it > 0L }
            ?: 0L
        val unboundedPosition = controller.currentPosition.coerceAtLeast(0L)
        val position = if (duration > 0L) unboundedPosition.coerceAtMost(duration) else unboundedPosition
        val mediaCount = controller.mediaItemCount.coerceAtLeast(0)
        val rawIndex = controller.currentMediaItemIndex
        val currentIndex = rawIndex
            .takeIf { it != C.INDEX_UNSET && it >= 0 }
            ?.coerceAtMost((mediaCount - 1).coerceAtLeast(0))
            ?: 0
        val metadata = controller.currentMediaItem?.mediaMetadata

        _playbackInfo.value = QuranPlaybackInfo(
            mediaId = controller.currentMediaItem?.mediaId,
            title = metadata?.title?.toString()?.takeIf { it.isNotBlank() },
            artist = metadata?.artist?.toString()?.takeIf { it.isNotBlank() },
            currentIndex = currentIndex,
            mediaCount = mediaCount,
            positionMs = position,
            durationMs = duration,
            canSkipPrevious = controller.hasPreviousMediaItem() || position > 0L,
            canSkipNext = controller.hasNextMediaItem(),
        )
    }

    private fun updatePositionPolling(controller: Player) {
        val shouldPoll = _playbackInfo.subscriptionCount.value > 0 &&
            controller.playWhenReady &&
            (controller.playbackState == Player.STATE_BUFFERING ||
                controller.playbackState == Player.STATE_READY)
        if (!shouldPoll) {
            stopPositionPolling()
            return
        }
        if (positionPollingJob?.isActive == true) return
        positionPollingJob = controllerScope.launch {
            while (isActive) {
                val connectedController = runCatching { controllerFuture.get() }.getOrNull() ?: break
                updatePlaybackInfo(connectedController)
                delay(POSITION_UPDATE_INTERVAL_MS)
            }
        }
    }

    private fun stopPositionPolling() {
        positionPollingJob?.cancel()
        positionPollingJob = null
    }

    private companion object {
        const val POSITION_UPDATE_INTERVAL_MS = 500L
    }
}

private val Reciter.displayNameResource: Int
    get() = when (this) {
        Reciter.ALAFASY -> R.string.media_reciter_alafasy
        Reciter.HUSARY -> R.string.media_reciter_husary
        Reciter.MINSHAWI -> R.string.media_reciter_minshawi
    }
