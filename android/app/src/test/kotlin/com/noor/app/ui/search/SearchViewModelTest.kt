package com.noor.app.ui.search

import com.noor.app.testing.MainDispatcherRule
import com.noor.core.common.text.ArabicNormalizer
import com.noor.domain.model.Ayah
import com.noor.domain.model.AyahSearchResult
import com.noor.domain.model.QuranCorpus
import com.noor.domain.model.Surah
import com.noor.domain.repository.QuranRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun newerQueryCancelsThePendingSearch() = runTest {
        val repository = FakeQuranRepository()
        val viewModel = SearchViewModel(repository)

        viewModel.onQueryChanged("الرحمن")
        advanceTimeBy(100)
        viewModel.onQueryChanged("الرحيم")
        advanceUntilIdle()

        assertEquals(listOf("الرحيم"), repository.queries)
        assertFalse(viewModel.uiState.value.isSearching)
    }

    @Test
    fun inputLengthIsBoundedBeforeStateAndRepositoryUse() = runTest {
        val repository = FakeQuranRepository()
        val viewModel = SearchViewModel(repository)
        val longQuery = "ا".repeat(ArabicNormalizer.MAX_QUERY_LENGTH + 50)

        viewModel.onQueryChanged(longQuery)
        advanceUntilIdle()

        assertEquals(ArabicNormalizer.MAX_QUERY_LENGTH, viewModel.uiState.value.query.length)
        assertEquals(ArabicNormalizer.MAX_QUERY_LENGTH, repository.queries.single().length)
    }

    @Test
    fun shortNormalizedQueryDoesNotReportAnEmptySearch() = runTest {
        val repository = FakeQuranRepository()
        val viewModel = SearchViewModel(repository)

        viewModel.onQueryChanged("َُ")
        advanceUntilIdle()

        assertTrue(repository.queries.isEmpty())
        assertFalse(viewModel.uiState.value.hasSearched)
        assertFalse(viewModel.uiState.value.isSearching)
    }

    @Test
    fun changingACompletedQueryClearsStaleResultsImmediately() = runTest {
        val repository = FakeQuranRepository { query ->
            if (query == "الرحمن") listOf(result(id = 4901, text = "ٱلرَّحْمَٰنُ"))
            else {
                delay(1_000)
                emptyList()
            }
        }
        val viewModel = SearchViewModel(repository)

        viewModel.onQueryChanged("الرحمن")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.results.isNotEmpty())

        viewModel.onQueryChanged("الرحيم")

        assertTrue(viewModel.uiState.value.isSearching)
        assertTrue(viewModel.uiState.value.results.isEmpty())
        assertFalse(viewModel.uiState.value.hasSearched)
    }

    @Test
    fun staleRepositoryResultCannotOverwriteTheNewerQuery() = runTest {
        val repository = FakeQuranRepository { query ->
            if (query == "الرحمن") {
                try {
                    delay(1_000)
                } catch (_: CancellationException) {
                    // Simulate a data source that completes even after cancellation.
                }
                listOf(result(id = 4901, text = "نتيجة قديمة"))
            } else {
                listOf(result(id = 4902, text = "نتيجة حديثة"))
            }
        }
        val viewModel = SearchViewModel(repository)

        viewModel.onQueryChanged("الرحمن")
        advanceTimeBy(300)
        viewModel.onQueryChanged("الرحيم")
        advanceUntilIdle()

        assertEquals("الرحيم", viewModel.uiState.value.query)
        assertEquals(listOf(4902), viewModel.uiState.value.results.map { it.ayah.id })
    }

    @Test
    fun retryRepeatsTheCurrentQueryAndRecoversFromFailure() = runTest {
        var attempts = 0
        val repository = FakeQuranRepository {
            attempts += 1
            if (attempts == 1) error("temporary failure")
            listOf(result(id = 4901, text = "ٱلرَّحْمَٰنُ"))
        }
        val viewModel = SearchViewModel(repository)

        viewModel.onQueryChanged("الرحمن")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.hasError)

        viewModel.retry()
        assertTrue(viewModel.uiState.value.isSearching)
        advanceUntilIdle()

        assertEquals(2, attempts)
        assertFalse(viewModel.uiState.value.hasError)
        assertEquals(1, viewModel.uiState.value.results.size)
    }

    @Test
    fun clearQueryCancelsSearchAndRestoresInitialState() = runTest {
        val repository = FakeQuranRepository {
            delay(1_000)
            listOf(result(id = 4901, text = "ٱلرَّحْمَٰنُ"))
        }
        val viewModel = SearchViewModel(repository)

        viewModel.onQueryChanged("الرحمن")
        advanceTimeBy(300)
        viewModel.clearQuery()
        advanceUntilIdle()

        assertEquals(SearchUiState(), viewModel.uiState.value)
    }

    private class FakeQuranRepository(
        private val searchHandler: suspend (String) -> List<AyahSearchResult> = { emptyList() },
    ) : QuranRepository {
        val queries = mutableListOf<String>()

        override fun observeSurahs(): Flow<List<Surah>> = flowOf(emptyList())
        override fun observeSurah(number: Int): Flow<Surah?> = flowOf(null)
        override fun observeAyahsForSurah(surahNumber: Int): Flow<List<Ayah>> = flowOf(emptyList())
        override fun observeAyahsForPage(pageNumber: Int): Flow<List<Ayah>> = flowOf(emptyList())
        override fun observeAyahsForJuz(juzNumber: Int): Flow<List<Ayah>> = flowOf(emptyList())
        override suspend fun getAyah(id: Int): Ayah? = null

        override suspend fun searchAyahs(
            normalizedQuery: String,
            limit: Int,
        ): List<AyahSearchResult> {
            queries += normalizedQuery
            return searchHandler(normalizedQuery)
        }

        override suspend fun isPopulated(): Boolean = true
        override suspend fun replaceCorpus(corpus: QuranCorpus) = Unit
    }

    private companion object {
        fun result(id: Int, text: String): AyahSearchResult = AyahSearchResult(
            ayah = Ayah(
                id = id,
                surahNumber = 55,
                numberInSurah = id - 4900,
                textUthmani = text,
                juzNumber = 27,
                hizbQuarter = 213,
                pageNumber = 531,
            ),
            surahNameArabic = "الرحمن",
        )
    }
}
