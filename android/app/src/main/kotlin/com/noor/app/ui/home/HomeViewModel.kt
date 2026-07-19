package com.noor.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noor.domain.repository.QuranRepository
import com.noor.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val quranRepository: QuranRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private var observationJob: Job? = null

    init {
        observeContent()
    }

    fun retry() {
        observeContent()
    }

    private fun observeContent() {
        observationJob?.cancel()
        observationJob = viewModelScope.launch {
            _uiState.value = HomeUiState(isLoading = true)
            try {
                combine(
                    quranRepository.observeSurahs(),
                    settingsRepository.settings,
                    ::HomeSource,
                ).mapLatest { source ->
                    val continueSurah = source.settings.lastReadSurah
                        ?.let { number -> source.surahs.firstOrNull { it.number == number } }
                    val continueAyah = source.settings.lastReadAyah
                        ?.let { id -> quranRepository.getAyah(id) }
                        ?.takeIf { ayah -> continueSurah == null || ayah.surahNumber == continueSurah.number }
                    HomeUiState(
                        surahs = source.surahs,
                        continueSurah = continueSurah,
                        continueAyah = continueAyah,
                        selectedReciter = source.settings.reciter,
                        dailyPagesRead = source.settings.dailyPagesRead,
                        readingStreakDays = source.settings.readingStreakDays,
                        isLoading = false,
                        hasError = source.surahs.isEmpty(),
                    )
                }.collect { state -> _uiState.value = state }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _uiState.value = HomeUiState(isLoading = false, hasError = true)
            }
        }
    }

    private data class HomeSource(
        val surahs: List<com.noor.domain.model.Surah>,
        val settings: com.noor.domain.model.UserSettings,
    )
}
