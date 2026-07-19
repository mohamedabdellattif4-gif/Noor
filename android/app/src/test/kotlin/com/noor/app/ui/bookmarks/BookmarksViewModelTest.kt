package com.noor.app.ui.bookmarks

import com.noor.app.testing.MainDispatcherRule
import com.noor.domain.model.Ayah
import com.noor.domain.model.Bookmark
import com.noor.domain.repository.BookmarkRepository
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookmarksViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun retryRecoversAfterObservationFailure() = runTest {
        val expected = listOf(sampleBookmark())
        val repository = FakeBookmarkRepository(expected, failuresBeforeSuccess = 1)
        val viewModel = BookmarksViewModel(repository)

        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.hasError)

        viewModel.retry()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.hasError)
        assertEquals(expected, state.bookmarks)
        assertEquals(2, repository.observeCalls)
    }

    @Test
    fun removeClearsTheRequestedBookmark() = runTest {
        val repository = FakeBookmarkRepository(emptyList())
        val viewModel = BookmarksViewModel(repository)
        advanceUntilIdle()

        viewModel.remove(ayahId = 12)
        advanceUntilIdle()

        assertEquals(listOf(12 to false), repository.updates)
        assertTrue(viewModel.uiState.value.pendingRemovalIds.isEmpty())
    }

    @Test
    fun duplicateRemoveIsIgnoredWhileTheFirstRequestIsPending() = runTest {
        val removalGate = CompletableDeferred<Unit>()
        val repository = FakeBookmarkRepository(emptyList(), removalGate = removalGate)
        val viewModel = BookmarksViewModel(repository)
        advanceUntilIdle()

        viewModel.remove(ayahId = 12)
        viewModel.remove(ayahId = 12)
        runCurrent()

        assertEquals(listOf(12 to false), repository.updates)
        assertEquals(setOf(12), viewModel.uiState.value.pendingRemovalIds)

        removalGate.complete(Unit)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.pendingRemovalIds.isEmpty())
    }

    @Test
    fun removalFailureEmitsEventAndClearsPendingState() = runTest {
        val repository = FakeBookmarkRepository(emptyList(), failRemoval = true)
        val viewModel = BookmarksViewModel(repository)
        advanceUntilIdle()
        val event = backgroundScope.async(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.first()
        }

        viewModel.remove(ayahId = 12)
        advanceUntilIdle()

        assertEquals(BookmarksEvent.RemovalFailed, event.await())
        assertTrue(viewModel.uiState.value.pendingRemovalIds.isEmpty())
    }

    @Test
    fun invalidAyahIdIsRejectedWithoutCallingTheRepository() = runTest {
        val repository = FakeBookmarkRepository(emptyList())
        val viewModel = BookmarksViewModel(repository)
        advanceUntilIdle()

        viewModel.remove(ayahId = 0)
        advanceUntilIdle()

        assertTrue(repository.updates.isEmpty())
    }

    private class FakeBookmarkRepository(
        private val bookmarks: List<Bookmark>,
        private var failuresBeforeSuccess: Int = 0,
        private val removalGate: CompletableDeferred<Unit>? = null,
        private val failRemoval: Boolean = false,
    ) : BookmarkRepository {
        var observeCalls: Int = 0
            private set
        val updates = mutableListOf<Pair<Int, Boolean>>()

        override fun observeBookmarks(): Flow<List<Bookmark>> {
            observeCalls += 1
            return if (failuresBeforeSuccess > 0) {
                failuresBeforeSuccess -= 1
                flow { throw IOException("test failure") }
            } else {
                flowOf(bookmarks)
            }
        }

        override fun observeBookmarkedAyahIds(): Flow<Set<Int>> = flowOf(emptySet())

        override suspend fun setBookmarked(ayahId: Int, bookmarked: Boolean) {
            updates += ayahId to bookmarked
            removalGate?.await()
            if (failRemoval) throw IOException("remove failure")
        }
    }

    private companion object {
        fun sampleBookmark() = Bookmark(
            ayah = Ayah(
                id = 1,
                surahNumber = 1,
                numberInSurah = 1,
                textUthmani = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                juzNumber = 1,
                hizbQuarter = 1,
                pageNumber = 1,
            ),
            surahNameArabic = "الفاتحة",
            createdAtEpochMillis = 1L,
        )
    }
}
