package com.noor.app.ui.reader

import androidx.lifecycle.SavedStateHandle
import com.noor.app.testing.MainDispatcherRule
import com.noor.core.media.QuranAudioPlayer
import com.noor.core.media.QuranPlaybackState
import com.noor.domain.model.Ayah
import com.noor.domain.model.AyahSearchResult
import com.noor.domain.model.QuranCorpus
import com.noor.domain.model.Reciter
import com.noor.domain.model.RevelationType
import com.noor.domain.model.Surah
import com.noor.domain.model.ThemeMode
import com.noor.domain.model.UserSettings
import com.noor.domain.repository.BookmarkRepository
import com.noor.domain.repository.QuranRepository
import com.noor.domain.repository.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReaderViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun playSurahStartsAtRequestedAyahAndPersistsReadingPosition() = runTest {
        val ayahs = listOf(sampleAyah(id = 1, number = 1), sampleAyah(id = 2, number = 2))
        val settings = FakeSettingsRepository(UserSettings())
        val player = FakeQuranAudioPlayer()
        val viewModel = ReaderViewModel(
            savedStateHandle = SavedStateHandle(mapOf("surahNumber" to 1, "ayahId" to 2)),
            quranRepository = FakeQuranRepository(sampleSurah(), ayahs),
            bookmarkRepository = FakeBookmarkRepository(),
            settingsRepository = settings,
            audioPlayer = player,
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(2, viewModel.uiState.value.targetAyahId)

        viewModel.playSurah()
        advanceUntilIdle()

        assertEquals(2, player.startedAtAyahId)
        assertEquals(1 to 2, settings.lastRead)
        assertEquals(1, settings.lastRecordedPage)
    }

    @Test
    fun toggleBookmarkUpdatesBookmarkAndLastReadTogether() = runTest {
        val bookmarkRepository = FakeBookmarkRepository()
        val settings = FakeSettingsRepository(UserSettings())
        val ayah = sampleAyah(id = 4, number = 4)
        val viewModel = ReaderViewModel(
            savedStateHandle = SavedStateHandle(mapOf("surahNumber" to 1)),
            quranRepository = FakeQuranRepository(sampleSurah(), listOf(ayah)),
            bookmarkRepository = bookmarkRepository,
            settingsRepository = settings,
            audioPlayer = FakeQuranAudioPlayer(),
        )

        viewModel.toggleBookmark(ayah)
        advanceUntilIdle()

        assertEquals(listOf(4 to true), bookmarkRepository.updates)
        assertEquals(1 to 4, settings.lastRead)
        assertEquals(1, settings.lastRecordedPage)
    }


    @Test
    fun textScaleIsClampedBeforePersistence() = runTest {
        val settings = FakeSettingsRepository(UserSettings())
        val viewModel = ReaderViewModel(
            savedStateHandle = SavedStateHandle(mapOf("surahNumber" to 1)),
            quranRepository = FakeQuranRepository(sampleSurah(), listOf(sampleAyah(1, 1))),
            bookmarkRepository = FakeBookmarkRepository(),
            settingsRepository = settings,
            audioPlayer = FakeQuranAudioPlayer(),
        )

        viewModel.setTextScale(9f)
        advanceUntilIdle()

        assertEquals(UserSettings.MAX_TEXT_SCALE, settings.lastTextScale)
    }

    @Test
    fun duplicateBookmarkTapIsIgnoredWhilePersistenceIsRunning() = runTest {
        val gate = kotlinx.coroutines.CompletableDeferred<Unit>()
        val bookmarkRepository = FakeBookmarkRepository(gate = gate)
        val ayah = sampleAyah(id = 5, number = 5)
        val viewModel = ReaderViewModel(
            savedStateHandle = SavedStateHandle(mapOf("surahNumber" to 1)),
            quranRepository = FakeQuranRepository(sampleSurah(), listOf(ayah)),
            bookmarkRepository = bookmarkRepository,
            settingsRepository = FakeSettingsRepository(UserSettings()),
            audioPlayer = FakeQuranAudioPlayer(),
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        viewModel.toggleBookmark(ayah)
        runCurrent()
        viewModel.toggleBookmark(ayah)
        runCurrent()

        assertEquals(1, bookmarkRepository.calls)
        gate.complete(Unit)
        advanceUntilIdle()
    }

    @Test
    fun bookmarkFailureEmitsFeedbackAndDoesNotRecordReading() = runTest {
        val ayah = sampleAyah(id = 6, number = 6)
        val settings = FakeSettingsRepository(UserSettings())
        val viewModel = ReaderViewModel(
            savedStateHandle = SavedStateHandle(mapOf("surahNumber" to 1)),
            quranRepository = FakeQuranRepository(sampleSurah(), listOf(ayah)),
            bookmarkRepository = FakeBookmarkRepository(failWrites = true),
            settingsRepository = settings,
            audioPlayer = FakeQuranAudioPlayer(),
        )
        val event = backgroundScope.async(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.first()
        }

        viewModel.toggleBookmark(ayah)
        advanceUntilIdle()

        assertEquals(ReaderEvent.BookmarkUpdateFailed, event.await())
        assertEquals(null, settings.lastRead)
    }

    @Test
    fun textScaleFailureEmitsFeedbackInsteadOfEscapingTheViewModelScope() = runTest {
        val settings = FakeSettingsRepository(UserSettings(), failTextScale = true)
        val viewModel = ReaderViewModel(
            savedStateHandle = SavedStateHandle(mapOf("surahNumber" to 1)),
            quranRepository = FakeQuranRepository(sampleSurah(), listOf(sampleAyah(1, 1))),
            bookmarkRepository = FakeBookmarkRepository(),
            settingsRepository = settings,
            audioPlayer = FakeQuranAudioPlayer(),
        )
        val event = backgroundScope.async(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.first()
        }

        viewModel.setTextScale(1.2f)
        advanceUntilIdle()

        assertEquals(ReaderEvent.TextScaleUpdateFailed, event.await())
    }

    private class FakeQuranAudioPlayer : QuranAudioPlayer {
        private val mutablePlaybackState = MutableStateFlow<QuranPlaybackState>(QuranPlaybackState.Idle)
        override val playbackState: StateFlow<QuranPlaybackState> = mutablePlaybackState
        var startedAtAyahId: Int? = null
            private set

        override fun playAyah(ayah: Ayah, reciter: Reciter, surahName: String) {
            startedAtAyahId = ayah.id
        }

        override fun playSurah(
            ayahs: List<Ayah>,
            reciter: Reciter,
            surahName: String,
            startAyahId: Int?,
        ) {
            startedAtAyahId = startAyahId
        }

        override fun togglePause() = Unit
        override fun stop() = Unit
    }

    private class FakeQuranRepository(
        private val surah: Surah,
        private val ayahs: List<Ayah>,
    ) : QuranRepository {
        override fun observeSurahs(): Flow<List<Surah>> = flowOf(listOf(surah))
        override fun observeSurah(number: Int): Flow<Surah?> = flowOf(surah)
        override fun observeAyahsForSurah(surahNumber: Int): Flow<List<Ayah>> = flowOf(ayahs)
        override fun observeAyahsForPage(pageNumber: Int): Flow<List<Ayah>> = flowOf(emptyList())
        override fun observeAyahsForJuz(juzNumber: Int): Flow<List<Ayah>> = flowOf(emptyList())
        override suspend fun getAyah(id: Int): Ayah? = ayahs.firstOrNull { it.id == id }
        override suspend fun searchAyahs(normalizedQuery: String, limit: Int): List<AyahSearchResult> = emptyList()
        override suspend fun isPopulated(): Boolean = true
        override suspend fun replaceCorpus(corpus: QuranCorpus) = Unit
    }

    private class FakeBookmarkRepository(
        private val gate: kotlinx.coroutines.CompletableDeferred<Unit>? = null,
        private val failWrites: Boolean = false,
    ) : BookmarkRepository {
        val updates = mutableListOf<Pair<Int, Boolean>>()
        var calls: Int = 0
            private set
        override fun observeBookmarks() = flowOf(emptyList<com.noor.domain.model.Bookmark>())
        override fun observeBookmarkedAyahIds(): Flow<Set<Int>> = flowOf(emptySet())
        override suspend fun setBookmarked(ayahId: Int, bookmarked: Boolean) {
            calls += 1
            gate?.await()
            if (failWrites) throw java.io.IOException("bookmark storage unavailable")
            updates += ayahId to bookmarked
        }
    }

    private class FakeSettingsRepository(
        initial: UserSettings,
        private val failTextScale: Boolean = false,
    ) : SettingsRepository {
        private val mutableSettings = MutableStateFlow(initial)
        override val settings: Flow<UserSettings> = mutableSettings
        var lastRead: Pair<Int, Int>? = null
            private set
        var lastRecordedPage: Int? = null
            private set
        var lastTextScale: Float? = null
            private set

        override suspend fun setThemeMode(value: ThemeMode) {
            mutableSettings.value = mutableSettings.value.copy(themeMode = value)
        }

        override suspend fun setDynamicColorEnabled(value: Boolean) {
            mutableSettings.value = mutableSettings.value.copy(dynamicColorEnabled = value)
        }

        override suspend fun setQuranTextScale(value: Float) {
            if (failTextScale) throw java.io.IOException("settings storage unavailable")
            lastTextScale = value
            mutableSettings.value = mutableSettings.value.copy(quranTextScale = value)
        }

        override suspend fun setReciter(value: Reciter) {
            mutableSettings.value = mutableSettings.value.copy(reciter = value)
        }

        override suspend fun setLastRead(surahNumber: Int, ayahId: Int) {
            lastRead = surahNumber to ayahId
            mutableSettings.value = mutableSettings.value.copy(
                lastReadSurah = surahNumber,
                lastReadAyah = ayahId,
            )
        }

        override suspend fun recordReading(surahNumber: Int, ayahId: Int, pageNumber: Int) {
            lastRecordedPage = pageNumber
            setLastRead(surahNumber, ayahId)
        }
    }

    private companion object {
        fun sampleSurah() = Surah(
            number = 1,
            nameArabic = "الفاتحة",
            nameTransliterated = "Al-Fatihah",
            nameTranslated = "The Opening",
            revelationType = RevelationType.MECCAN,
            ayahCount = 7,
            startPage = 1,
        )

        fun sampleAyah(id: Int, number: Int) = Ayah(
            id = id,
            surahNumber = 1,
            numberInSurah = number,
            textUthmani = "آية $number",
            juzNumber = 1,
            hizbQuarter = 1,
            pageNumber = 1,
        )
    }
}
