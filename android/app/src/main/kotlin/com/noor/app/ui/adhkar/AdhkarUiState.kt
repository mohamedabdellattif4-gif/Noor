package com.noor.app.ui.adhkar

import androidx.compose.runtime.Immutable
import com.noor.domain.model.AdhkarCategory
import com.noor.domain.model.Dhikr

@Immutable
data class AdhkarUiState(
    val items: List<Dhikr> = emptyList(),
    val query: String = "",
    val selectedCategory: AdhkarCategory? = null,
    val favoritesOnly: Boolean = false,
    val completedRepeats: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
) {
    val favoriteCount: Int get() = items.count(Dhikr::isFavorite)
}
