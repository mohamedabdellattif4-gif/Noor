package com.noor.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noor.core.common.text.ArabicNormalizer
import com.noor.domain.repository.QuranRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val quranRepository: QuranRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var requestGeneration: Long = 0L

    fun onQueryChanged(query: String) {
        val safeQuery = query.take(ArabicNormalizer.MAX_QUERY_LENGTH)
        requestGeneration += 1L
        searchJob?.cancel()

        val normalized = ArabicNormalizer.normalize(safeQuery)
        if (normalized.length < MIN_SEARCH_LENGTH) {
            _uiState.value = SearchUiState(query = safeQuery)
            return
        }

        _uiState.value = SearchUiState(
            query = safeQuery,
            isSearching = true,
            hasSearched = false,
        )
        launchSearch(
            visibleQuery = safeQuery,
            normalizedQuery = normalized,
            generation = requestGeneration,
            debounceMillis = SEARCH_DEBOUNCE_MILLIS,
        )
    }

    fun clearQuery() {
        requestGeneration += 1L
        searchJob?.cancel()
        _uiState.value = SearchUiState()
    }

    fun retry() {
        val visibleQuery = _uiState.value.query
        val normalized = ArabicNormalizer.normalize(visibleQuery)
        if (normalized.length < MIN_SEARCH_LENGTH) return

        requestGeneration += 1L
        searchJob?.cancel()
        _uiState.value = SearchUiState(
            query = visibleQuery,
            isSearching = true,
            hasSearched = false,
        )
        launchSearch(
            visibleQuery = visibleQuery,
            normalizedQuery = normalized,
            generation = requestGeneration,
            debounceMillis = 0L,
        )
    }

    private fun launchSearch(
        visibleQuery: String,
        normalizedQuery: String,
        generation: Long,
        debounceMillis: Long,
    ) {
        searchJob = viewModelScope.launch {
            if (debounceMillis > 0L) delay(debounceMillis)
            try {
                val results = quranRepository.searchAyahs(
                    normalizedQuery = normalizedQuery,
                    limit = SEARCH_RESULT_LIMIT,
                )
                if (generation != requestGeneration || _uiState.value.query != visibleQuery) return@launch
                _uiState.value = SearchUiState(
                    query = visibleQuery,
                    results = results,
                    hasSearched = true,
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (generation != requestGeneration || _uiState.value.query != visibleQuery) return@launch
                _uiState.value = SearchUiState(
                    query = visibleQuery,
                    hasSearched = true,
                    hasError = true,
                )
            }
        }
    }

    private companion object {
        const val MIN_SEARCH_LENGTH: Int = 2
        const val SEARCH_DEBOUNCE_MILLIS: Long = 300L
        const val SEARCH_RESULT_LIMIT: Int = 100
    }
}
