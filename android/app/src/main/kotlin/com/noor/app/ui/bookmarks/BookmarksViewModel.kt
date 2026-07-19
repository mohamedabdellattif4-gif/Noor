package com.noor.app.ui.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noor.domain.repository.BookmarkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val repository: BookmarkRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BookmarksUiState())
    val uiState: StateFlow<BookmarksUiState> = _uiState.asStateFlow()

    private val eventChannel = Channel<BookmarksEvent>(capacity = Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    private var observationJob: Job? = null

    init {
        observeBookmarks()
    }

    fun retry() {
        observeBookmarks()
    }

    fun remove(ayahId: Int) {
        if (ayahId <= 0 || ayahId in _uiState.value.pendingRemovalIds) return

        _uiState.update { state ->
            state.copy(pendingRemovalIds = state.pendingRemovalIds + ayahId)
        }
        viewModelScope.launch {
            try {
                repository.setBookmarked(ayahId, false)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                eventChannel.send(BookmarksEvent.RemovalFailed)
            } finally {
                _uiState.update { state ->
                    state.copy(pendingRemovalIds = state.pendingRemovalIds - ayahId)
                }
            }
        }
    }

    private fun observeBookmarks() {
        observationJob?.cancel()
        observationJob = viewModelScope.launch {
            _uiState.update { state -> state.copy(isLoading = true, hasError = false) }
            try {
                repository.observeBookmarks().collect { bookmarks ->
                    _uiState.update { state ->
                        state.copy(
                            bookmarks = bookmarks,
                            isLoading = false,
                            hasError = false,
                        )
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _uiState.update { state -> state.copy(isLoading = false, hasError = true) }
            }
        }
    }
}
