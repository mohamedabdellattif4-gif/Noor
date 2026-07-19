package com.noor.app.ui.adhkar

import com.noor.app.testing.MainDispatcherRule
import com.noor.domain.model.AdhkarCategory
import com.noor.domain.model.AdhkarReminderSettings
import com.noor.domain.model.Dhikr
import com.noor.domain.repository.AdhkarRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AdhkarViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun searchNormalizesArabicAndCategoryFilterComposesWithIt() = runTest {
        val repository = FakeAdhkarRepository()
        val viewModel = AdhkarViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        viewModel.setQuery("أستغفر")
        advanceUntilIdle()
        assertEquals(listOf("general_istighfar"), viewModel.uiState.value.items.map { it.id })

        viewModel.selectCategory(AdhkarCategory.MORNING)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.items.isEmpty())
    }

    @Test
    fun favoriteUpdateIsDelegatedAndFavoritesOnlyHidesOtherItems() = runTest {
        val repository = FakeAdhkarRepository()
        val viewModel = AdhkarViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        val first = viewModel.uiState.value.items.first()
        viewModel.toggleFavorite(first)
        advanceUntilIdle()
        assertTrue(repository.items.value.first { it.id == first.id }.isFavorite)

        viewModel.toggleFavoritesOnly()
        advanceUntilIdle()
        assertEquals(listOf(first.id), viewModel.uiState.value.items.map { it.id })
    }

    @Test
    fun perItemCounterIsCappedAndResetWithoutChangingContent() = runTest {
        val repository = FakeAdhkarRepository()
        val viewModel = AdhkarViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()
        val item = viewModel.uiState.value.items.first()

        repeat(10) { viewModel.increment(item) }
        advanceUntilIdle()
        assertEquals(item.repeatCount, viewModel.uiState.value.completedRepeats[item.id])

        viewModel.reset(item)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.completedRepeats.containsKey(item.id))
    }

    private class FakeAdhkarRepository : AdhkarRepository {
        val items = MutableStateFlow(
            listOf(
                Dhikr(
                    id = "morning_bismillah",
                    category = AdhkarCategory.MORNING,
                    title = "بسم الله",
                    textArabic = "بسم الله الذي لا يضر",
                    reference = "سنن أبي داود",
                    repeatCount = 3,
                ),
                Dhikr(
                    id = "general_istighfar",
                    category = AdhkarCategory.GENERAL,
                    title = "الاستغفار",
                    textArabic = "أستغفر الله",
                    reference = "صحيح مسلم",
                    repeatCount = 1,
                ),
            ),
        )
        override val adhkar: Flow<List<Dhikr>> = items
        override val reminderSettings: Flow<AdhkarReminderSettings> =
            MutableStateFlow(AdhkarReminderSettings())

        override suspend fun setFavorite(dhikrId: String, favorite: Boolean) {
            items.value = items.value.map { item ->
                if (item.id == dhikrId) item.copy(isFavorite = favorite) else item
            }
        }

        override suspend fun setMorningReminderEnabled(enabled: Boolean) = Unit
        override suspend fun setEveningReminderEnabled(enabled: Boolean) = Unit
    }
}
