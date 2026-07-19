package com.noor.app.ui.tafsir

import androidx.lifecycle.SavedStateHandle
import com.noor.app.testing.MainDispatcherRule
import com.noor.domain.model.Ayah
import com.noor.domain.model.AyahSearchResult
import com.noor.domain.model.QuranCorpus
import com.noor.domain.model.Surah
import com.noor.domain.model.Tafsir
import com.noor.domain.model.TafsirOrigin
import com.noor.domain.repository.QuranRepository
import com.noor.domain.repository.TafsirRepository
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TafsirViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun canonicalAyahIsLoadedByIdAndPassedToTafsirRepository() = runTest {
        val ayah = sampleAyah()
        val quran = FakeQuranRepository(ayah)
        val tafsir = FakeTafsirRepository()

        val viewModel = createViewModel(quran, tafsir)
        advanceUntilIdle()

        assertSame(ayah, tafsir.calls.single().ayah)
        assertFalse(tafsir.calls.single().forceRefresh)
        assertEquals(ayah, viewModel.uiState.value.ayah)
        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.hasError)
    }

    @Test
    fun missingCanonicalAyahShowsErrorWithoutCallingTafsirService() = runTest {
        val tafsir = FakeTafsirRepository()
        val viewModel = createViewModel(FakeQuranRepository(null), tafsir)

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasError)
        assertTrue(tafsir.calls.isEmpty())
    }

    @Test
    fun invalidNavigationArgumentFailsSafelyInsteadOfCrashing() = runTest {
        val quran = FakeQuranRepository(sampleAyah())
        val tafsir = FakeTafsirRepository()
        val viewModel = TafsirViewModel(
            savedStateHandle = SavedStateHandle(mapOf("ayahId" to 0)),
            quranRepository = quran,
            tafsirRepository = tafsir,
        )

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasError)
        assertEquals(0, quran.getAyahCalls)
        assertTrue(tafsir.calls.isEmpty())
    }

    @Test
    fun retryRecoversAfterInitialTafsirFailure() = runTest {
        val tafsir = FakeTafsirRepository(failuresBeforeSuccess = 1)
        val viewModel = createViewModel(FakeQuranRepository(sampleAyah()), tafsir)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.hasError)

        viewModel.retry()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasError)
        assertEquals("تفسير", viewModel.uiState.value.tafsir?.text)
        assertEquals(2, tafsir.calls.size)
    }

    @Test
    fun refreshFailureKeepsContentVisibleAndEmitsFeedback() = runTest {
        val tafsir = FakeTafsirRepository(failForcedRefresh = true)
        val viewModel = createViewModel(FakeQuranRepository(sampleAyah()), tafsir)
        advanceUntilIdle()
        val previous = viewModel.uiState.value.tafsir
        val event = backgroundScope.async(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.first()
        }

        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(TafsirEvent.RefreshFailed, event.await())
        assertSame(previous, viewModel.uiState.value.tafsir)
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertFalse(viewModel.uiState.value.hasError)
    }

    @Test
    fun duplicateRefreshIsIgnoredWhileTheFirstRefreshIsRunning() = runTest {
        val gate = CompletableDeferred<Unit>()
        val tafsir = FakeTafsirRepository(refreshGate = gate)
        val viewModel = createViewModel(FakeQuranRepository(sampleAyah()), tafsir)
        advanceUntilIdle()

        viewModel.refresh()
        runCurrent()
        assertTrue(viewModel.uiState.value.isRefreshing)
        viewModel.refresh()
        runCurrent()

        assertEquals(1, tafsir.calls.count { it.forceRefresh })
        gate.complete(Unit)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isRefreshing)
    }

    @Test
    fun successfulRefreshReplacesTheDisplayedTafsir() = runTest {
        val tafsir = FakeTafsirRepository()
        val viewModel = createViewModel(FakeQuranRepository(sampleAyah()), tafsir)
        advanceUntilIdle()

        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(TafsirOrigin.NETWORK, viewModel.uiState.value.tafsir?.origin)
        assertEquals(1, tafsir.calls.count { it.forceRefresh })
    }

    private fun createViewModel(
        quranRepository: QuranRepository,
        tafsirRepository: TafsirRepository,
    ) = TafsirViewModel(
        savedStateHandle = SavedStateHandle(mapOf("ayahId" to 1)),
        quranRepository = quranRepository,
        tafsirRepository = tafsirRepository,
    )

    private class FakeQuranRepository(private val ayah: Ayah?) : QuranRepository {
        var getAyahCalls = 0
            private set

        override fun observeSurahs(): Flow<List<Surah>> = emptyFlow()
        override fun observeSurah(number: Int): Flow<Surah?> = emptyFlow()
        override fun observeAyahsForSurah(surahNumber: Int): Flow<List<Ayah>> = emptyFlow()
        override fun observeAyahsForPage(pageNumber: Int): Flow<List<Ayah>> = emptyFlow()
        override fun observeAyahsForJuz(juzNumber: Int): Flow<List<Ayah>> = emptyFlow()
        override suspend fun getAyah(id: Int): Ayah? {
            getAyahCalls += 1
            return ayah?.takeIf { it.id == id }
        }
        override suspend fun searchAyahs(normalizedQuery: String, limit: Int): List<AyahSearchResult> =
            emptyList()
        override suspend fun isPopulated(): Boolean = ayah != null
        override suspend fun replaceCorpus(corpus: QuranCorpus) = Unit
    }

    private class FakeTafsirRepository(
        private var failuresBeforeSuccess: Int = 0,
        private val failForcedRefresh: Boolean = false,
        private val refreshGate: CompletableDeferred<Unit>? = null,
    ) : TafsirRepository {
        data class Call(val ayah: Ayah, val forceRefresh: Boolean)
        val calls = mutableListOf<Call>()

        override suspend fun getTafsir(ayah: Ayah, forceRefresh: Boolean): Tafsir {
            calls += Call(ayah, forceRefresh)
            if (forceRefresh) refreshGate?.await()
            if (failuresBeforeSuccess > 0) {
                failuresBeforeSuccess -= 1
                throw IOException("temporary failure")
            }
            if (forceRefresh && failForcedRefresh) throw IOException("refresh failure")
            return sampleTafsir(
                origin = if (forceRefresh) TafsirOrigin.NETWORK else TafsirOrigin.CACHE,
            )
        }
    }

    private companion object {
        fun sampleAyah() = Ayah(
            id = 1,
            surahNumber = 1,
            numberInSurah = 1,
            textUthmani = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
            juzNumber = 1,
            hizbQuarter = 1,
            pageNumber = 1,
        )

        fun sampleTafsir(origin: TafsirOrigin = TafsirOrigin.CACHE) = Tafsir(
            ayahId = 1,
            text = "تفسير",
            footnotes = null,
            sourceKey = "arabic_moyassar",
            sourceName = "التفسير الميسر — QuranEnc.com",
            fetchedAtEpochMillis = 1L,
            origin = origin,
        )
    }
}
