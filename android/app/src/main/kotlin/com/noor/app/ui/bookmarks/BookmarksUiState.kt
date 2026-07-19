package com.noor.app.ui.bookmarks

import androidx.compose.runtime.Immutable
import com.noor.domain.model.Bookmark

@Immutable
data class BookmarksUiState(
    val bookmarks: List<Bookmark> = emptyList(),
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    val pendingRemovalIds: Set<Int> = emptySet(),
)
