package com.noor.app.ui.reader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noor.core.media.QuranAudioPlayer
import com.noor.domain.model.Ayah
import com.noor.domain.model.UserSettings
import com.noor.domain.repository.BookmarkRepository
import com.noor.domain.repository.QuranRepository
import com.noor.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val quranRepository: QuranRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val settingsRepository: SettingsRepository,
    private val audioPlayer: QuranAudioPlayer,
) : ViewModel() {
    private val surahNumber: Int = requireNotNull(savedStateHandle["surahNumber"])
    private val requestedAyahId: Int? = savedStateHandle.get<Int>("ayahId")?.takeIf { it > 0 }
    private val contentState = MutableStateFlow(ReaderUiState(targetAyahId = requestedAyahId))
    private val pendingBookmarkAyahIds = MutableStateFlow<Set<Int>>(emptySet())
    private val eventChannel = Channel<ReaderEvent>(capacity = Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    private var observationJob: Job? = null
    private var readingFailureReported = false

    val uiState: StateFlow<ReaderUiState> = combine(
        contentState,
        audioPlayer.playbackState,
        audioPlayer.playbackInfo,
        pendingBookmarkAyahIds,
    ) { content, playback, playbackInfo, pendingBookmarks ->
        content.copy(
            playbackState = playback,
            playbackInfo = playbackInfo,
            pendingBookmarkAyahIds = pendingBookmarks,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ReaderUiState(targetAyahId = requestedAyahId),
        )

    init {
        observeContent()
    }

    fun retry() {
        observeContent()
    }

    fun toggleBookmark(ayah: Ayah) {
        val state = uiState.value
        if (ayah.id in state.pendingBookmarkAyahIds) return
        val targetBookmarked = ayah.id !in state.bookmarkedAyahIds
        pendingBookmarkAyahIds.update { it + ayah.id }
        viewModelScope.launch {
            var bookmarkUpdated = false
            try {
                bookmarkRepository.setBookmarked(ayah.id, targetBookmarked)
                bookmarkUpdated = true
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                eventChannel.send(ReaderEvent.BookmarkUpdateFailed)
            } finally {
                pendingBookmarkAyahIds.update { it - ayah.id }
            }
            if (bookmarkUpdated) recordReadingSafely(ayah)
        }
    }

    fun playAyah(ayah: Ayah) {
        val state = uiState.value
        val surahName = state.surah?.nameArabic ?: ayah.surahNumber.toString()
        audioPlayer.playAyah(ayah, state.reciter, surahName)
        markRead(ayah)
    }

    fun playSurah() {
        val state = uiState.value
        val surah = state.surah ?: return
        audioPlayer.playSurah(state.ayahs, state.reciter, surah.nameArabic, state.targetAyahId)
        val startAyah = state.targetAyahId
            ?.let { id -> state.ayahs.firstOrNull { it.id == id } }
            ?: state.ayahs.firstOrNull()
        startAyah?.let(::markRead)
    }

    fun togglePause() = audioPlayer.togglePause()

    fun stopAudio() = audioPlayer.stop()

    fun setTextScale(value: Float) {
        val safeValue = value.coerceIn(
            UserSettings.MIN_TEXT_SCALE,
            UserSettings.MAX_TEXT_SCALE,
        )
        viewModelScope.launch {
            try {
                settingsRepository.setQuranTextScale(safeValue)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                eventChannel.send(ReaderEvent.TextScaleUpdateFailed)
            }
        }
    }

    fun markRead(ayah: Ayah) {
        viewModelScope.launch { recordReadingSafely(ayah) }
    }

    private suspend fun recordReadingSafely(ayah: Ayah) {
        try {
            settingsRepository.recordReading(ayah.surahNumber, ayah.id, ayah.pageNumber)
            readingFailureReported = false
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            if (!readingFailureReported) {
                readingFailureReported = true
                eventChannel.send(ReaderEvent.ReadingProgressUpdateFailed)
            }
        }
    }

    private fun observeContent() {
        observationJob?.cancel()
        observationJob = viewModelScope.launch {
            contentState.value = ReaderUiState(targetAyahId = requestedAyahId, isLoading = true)
            try {
                combine(
                    quranRepository.observeSurah(surahNumber),
                    quranRepository.observeAyahsForSurah(surahNumber),
                    bookmarkRepository.observeBookmarkedAyahIds(),
                    settingsRepository.settings,
                ) { surah, ayahs, bookmarks, settings ->
                    ReaderUiState(
                        surah = surah,
                        ayahs = ayahs,
                        bookmarkedAyahIds = bookmarks,
                        textScale = settings.quranTextScale,
                        reciter = settings.reciter,
                        targetAyahId = requestedAyahId,
                        isLoading = false,
                        hasError = surah == null || ayahs.isEmpty(),
                    )
                }.collect { state -> contentState.value = state }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                contentState.value = ReaderUiState(
                    targetAyahId = requestedAyahId,
                    isLoading = false,
                    hasError = true,
                )
            }
        }
    }
}

sealed interface ReaderEvent {
    data object BookmarkUpdateFailed : ReaderEvent
    data object TextScaleUpdateFailed : ReaderEvent
    data object ReadingProgressUpdateFailed : ReaderEvent
}
