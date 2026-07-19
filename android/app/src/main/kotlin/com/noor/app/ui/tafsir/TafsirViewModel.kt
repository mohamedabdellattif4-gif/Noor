package com.noor.app.ui.tafsir

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noor.domain.repository.QuranRepository
import com.noor.domain.repository.TafsirRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class TafsirViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val quranRepository: QuranRepository,
    private val tafsirRepository: TafsirRepository,
) : ViewModel() {
    private val ayahId: Int? = savedStateHandle.get<Int>("ayahId")?.takeIf { it > 0 }

    private val _uiState = MutableStateFlow(TafsirUiState())
    val uiState: StateFlow<TafsirUiState> = _uiState.asStateFlow()

    private val eventChannel = Channel<TafsirEvent>(capacity = Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    private var loadJob: Job? = null

    init {
        load(forceRefresh = false, preserveContent = false)
    }

    fun retry() {
        load(forceRefresh = false, preserveContent = false)
    }

    fun refresh() {
        val state = _uiState.value
        if (state.isLoading || state.isRefreshing || state.ayah == null || state.tafsir == null) return
        load(forceRefresh = true, preserveContent = true)
    }

    private fun load(forceRefresh: Boolean, preserveContent: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val previous = _uiState.value
            if (preserveContent && previous.ayah != null && previous.tafsir != null) {
                _uiState.update { it.copy(isRefreshing = true, hasError = false) }
            } else {
                _uiState.value = TafsirUiState(isLoading = true)
            }

            try {
                val canonicalAyah = previous.ayah
                    ?: ayahId?.let { quranRepository.getAyah(it) }
                if (canonicalAyah == null) {
                    _uiState.value = TafsirUiState(isLoading = false, hasError = true)
                    return@launch
                }

                val tafsir = tafsirRepository.getTafsir(
                    ayah = canonicalAyah,
                    forceRefresh = forceRefresh,
                )
                _uiState.value = TafsirUiState(
                    ayah = canonicalAyah,
                    tafsir = tafsir,
                    isLoading = false,
                    isRefreshing = false,
                    hasError = false,
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (preserveContent && previous.ayah != null && previous.tafsir != null) {
                    _uiState.value = previous.copy(isRefreshing = false, hasError = false)
                    eventChannel.send(TafsirEvent.RefreshFailed)
                } else {
                    _uiState.value = TafsirUiState(isLoading = false, hasError = true)
                }
            }
        }
    }
}
