package com.noor.app.ui.audio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noor.core.media.QuranAudioPlayer
import com.noor.core.media.QuranPlaybackState
import com.noor.domain.model.Ayah
import com.noor.domain.model.Reciter
import com.noor.domain.model.Surah
import com.noor.domain.model.UserSettings
import com.noor.domain.repository.QuranRepository
import com.noor.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AudioViewModel @Inject constructor(
    private val quranRepository: QuranRepository,
    private val settingsRepository: SettingsRepository,
    private val audioPlayer: QuranAudioPlayer,
) : ViewModel() {
    private val selectedSurahNumber = MutableStateFlow<Int?>(null)
    private val reciterUpdateInProgress = MutableStateFlow(false)
    private val contentGeneration = MutableStateFlow(0)
    private val eventChannel = Channel<AudioEvent>(capacity = Channel.BUFFERED)
    internal val events: Flow<AudioEvent> = eventChannel.receiveAsFlow()

    private val content: Flow<AudioContent> = contentGeneration.flatMapLatest {
        combine(
            quranRepository.observeSurahs(),
            settingsRepository.settings,
            selectedSurahNumber,
        ) { surahs, settings, explicitSelection ->
            AudioSource(
                surahs = surahs,
                settings = settings,
                selectedSurahNumber = resolveSelectedSurahNumber(
                    surahs = surahs,
                    explicitSelection = explicitSelection,
                    lastReadSurah = settings.lastReadSurah,
                ),
            )
        }.flatMapLatest { source ->
            val selectedNumber = source.selectedSurahNumber
            if (selectedNumber == null) {
                flowOf(AudioContent(source = source, selectedAyahs = emptyList()))
            } else {
                quranRepository.observeAyahsForSurah(selectedNumber).map { ayahs ->
                    AudioContent(source = source, selectedAyahs = ayahs)
                }
            }
        }.catch { error ->
            if (error is CancellationException) throw error
            emit(AudioContent.failed())
        }
    }

    val uiState: StateFlow<AudioUiState> = combine(
        content,
        audioPlayer.playbackState,
        audioPlayer.playbackInfo,
        reciterUpdateInProgress,
    ) { content, playbackState, playbackInfo, isReciterUpdating ->
        val selectedSurah = content.source.selectedSurahNumber?.let { number ->
            content.source.surahs.firstOrNull { it.number == number }
        }
        AudioUiState(
            surahs = content.source.surahs,
            selectedSurah = selectedSurah,
            selectedAyahs = content.selectedAyahs,
            reciter = content.source.settings.reciter,
            playbackState = playbackState,
            playbackInfo = playbackInfo,
            isReciterUpdating = isReciterUpdating,
            isLoading = false,
            hasError = content.failed || selectedSurah == null || content.selectedAyahs.isEmpty(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AudioUiState(),
    )

    fun selectSurah(surahNumber: Int) {
        if (uiState.value.surahs.none { it.number == surahNumber }) return
        selectedSurahNumber.value = surahNumber
    }

    fun retry() {
        contentGeneration.value += 1
    }

    fun setReciter(reciter: Reciter) {
        val state = uiState.value
        if (reciter == state.reciter || reciterUpdateInProgress.value) return
        reciterUpdateInProgress.value = true
        viewModelScope.launch {
            try {
                settingsRepository.setReciter(reciter)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                eventChannel.send(AudioEvent.ReciterUpdateFailed)
            } finally {
                reciterUpdateInProgress.value = false
            }
        }
    }

    fun playOrPauseSelectedSurah() {
        val state = uiState.value
        val surah = state.selectedSurah ?: return
        if (state.selectedAyahs.isEmpty()) return
        val currentBelongsToSelection = state.playbackInfo.currentAyahId?.let { currentId ->
            state.selectedAyahs.any { it.id == currentId }
        } ?: false
        val resumable = state.playbackState is QuranPlaybackState.Playing ||
            state.playbackState is QuranPlaybackState.Buffering ||
            state.playbackState is QuranPlaybackState.Paused
        if (currentBelongsToSelection && resumable) {
            audioPlayer.togglePause()
        } else {
            audioPlayer.playSurah(
                ayahs = state.selectedAyahs,
                reciter = state.reciter,
                surahName = surah.nameArabic,
                startAyahId = null,
            )
        }
    }

    fun skipToPrevious() = audioPlayer.skipToPrevious()

    fun skipToNext() = audioPlayer.skipToNext()

    fun seekTo(positionMs: Long) {
        if (positionMs < 0L) return
        audioPlayer.seekTo(positionMs)
    }

    fun stop() = audioPlayer.stop()

    private data class AudioSource(
        val surahs: List<Surah>,
        val settings: UserSettings,
        val selectedSurahNumber: Int?,
    )

    private data class AudioContent(
        val source: AudioSource,
        val selectedAyahs: List<Ayah>,
        val failed: Boolean = false,
    ) {
        companion object {
            fun failed(): AudioContent = AudioContent(
                source = AudioSource(
                    surahs = emptyList(),
                    settings = UserSettings(),
                    selectedSurahNumber = null,
                ),
                selectedAyahs = emptyList(),
                failed = true,
            )
        }
    }

    private companion object {
        fun resolveSelectedSurahNumber(
            surahs: List<Surah>,
            explicitSelection: Int?,
            lastReadSurah: Int?,
        ): Int? {
            val availableNumbers = surahs.asSequence().map(Surah::number).toSet()
            return explicitSelection?.takeIf(availableNumbers::contains)
                ?: lastReadSurah?.takeIf(availableNumbers::contains)
                ?: surahs.firstOrNull()?.number
        }
    }
}
