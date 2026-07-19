package com.noor.app.ui.search

import androidx.compose.runtime.Immutable
import com.noor.domain.model.AyahSearchResult

@Immutable
data class SearchUiState(
    val query: String = "",
    val results: List<AyahSearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val hasSearched: Boolean = false,
    val hasError: Boolean = false,
)
