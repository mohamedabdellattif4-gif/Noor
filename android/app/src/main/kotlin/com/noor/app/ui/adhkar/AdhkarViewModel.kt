package com.noor.app.ui.adhkar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noor.core.common.text.ArabicNormalizer
import com.noor.domain.model.AdhkarCategory
import com.noor.domain.model.Dhikr
import com.noor.domain.repository.AdhkarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AdhkarViewModel @Inject constructor(
    private val repository: AdhkarRepository,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow<AdhkarCategory?>(null)
    private val favoritesOnly = MutableStateFlow(false)
    private val completedRepeats = MutableStateFlow<Map<String, Int>>(emptyMap())
    private val eventChannel = Channel<AdhkarEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    private val catalog = repository.adhkar
        .map { CatalogSnapshot(items = it, hasError = false) }
        .catch { emit(CatalogSnapshot(items = emptyList(), hasError = true)) }

    val uiState: StateFlow<AdhkarUiState> = combine(
        catalog,
        query,
        selectedCategory,
        favoritesOnly,
        completedRepeats,
    ) { snapshot, queryValue, category, favorites, completed ->
        val items = snapshot.items
        val normalizedQuery = ArabicNormalizer.normalize(queryValue).trim()
        val visible = items.filter { item ->
            val categoryMatches = category == null || item.category == category
            val favoriteMatches = !favorites || item.isFavorite
            val queryMatches = normalizedQuery.isBlank() || searchableText(item).contains(normalizedQuery)
            categoryMatches && favoriteMatches && queryMatches
        }
        AdhkarUiState(
            items = visible,
            query = queryValue,
            selectedCategory = category,
            favoritesOnly = favorites,
            completedRepeats = completed,
            isLoading = false,
            hasError = snapshot.hasError,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AdhkarUiState(),
    )

    fun setQuery(value: String) {
        query.value = value.take(MAX_QUERY_LENGTH)
    }

    fun selectCategory(value: AdhkarCategory?) {
        selectedCategory.value = value
    }

    fun toggleFavoritesOnly() {
        favoritesOnly.value = !favoritesOnly.value
    }

    fun toggleFavorite(item: Dhikr) {
        viewModelScope.launch {
            try {
                repository.setFavorite(item.id, !item.isFavorite)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                eventChannel.send(AdhkarEvent.UpdateFailed)
            }
        }
    }

    fun increment(item: Dhikr) {
        completedRepeats.value = completedRepeats.value.toMutableMap().apply {
            val next = (get(item.id) ?: 0) + 1
            put(item.id, next.coerceAtMost(item.repeatCount))
        }
    }

    fun reset(item: Dhikr) {
        completedRepeats.value = completedRepeats.value - item.id
    }

    private fun searchableText(item: Dhikr): String = ArabicNormalizer.normalize(
        listOf(item.title, item.textArabic, item.reference).joinToString(" "),
    )

    private data class CatalogSnapshot(
        val items: List<Dhikr>,
        val hasError: Boolean,
    )

    private companion object {
        const val MAX_QUERY_LENGTH = 100
    }
}
