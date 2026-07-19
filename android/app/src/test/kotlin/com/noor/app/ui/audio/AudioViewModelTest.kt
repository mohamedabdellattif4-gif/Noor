package com.noor.app.ui.audio

import com.noor.app.testing.MainDispatcherRule
import com.noor.core.media.QuranAudioPlayer
import com.noor.core.media.QuranPlaybackInfo
import com.noor.core.media.QuranPlaybackState
import com.noor.domain.model.Ayah
import com.noor.domain.model.AyahSearchResult
import com.noor.domain.model.QuranCorpus
import com.noor.domain.model.Reciter
import com.noor.domain.model.RevelationType
import com.noor.domain.model.Surah
import com.noor.domain.model.ThemeMode
import com.noor.domain.model.UserSettings
import com.noor.domain.repository.QuranRepository
import com.noor.domain.repository.SettingsRepository
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
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
class AudioViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun lastReadSurahBecomesTheInitialListeningSelection() = runTest {
        val repository = FakeQuranRepository()
        val viewModel = AudioViewModel(
            quranRepository = repository,
            settingsRepository = FakeSettingsRepository(
                UserSettings(lastReadSurah = 55, lastReadAyah = 4901),
            ),
            audioPlayer = FakeQuranAudioPlayer(),
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(55, viewModel.uiState.value.selectedSurah?.number)
        assertEquals(listOf(4901, 4902), viewModel.uiState.value.selectedAyahs.map(Ayah::id))
    }

    @Test
    fun playStartsTheSelectedSurahWithThePersistedReciter() = runTest {
        val player = FakeQuranAudioPlayer()
        val viewModel = AudioViewModel(
            quranRepository = FakeQuranRepository(),
            settingsRepository = FakeSettingsRepository(
                UserSettings(reciter = Reciter.HUSARY, lastReadSurah = 55, lastReadAyah = 4901),
            ),
            audioPlayer = player,
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()

        viewModel.playOrPauseSelectedSurah()

        assertEquals(Reciter.HUSARY, player.lastReciter)
        assertEquals("الرحمن", player.lastSurahName)
        assertEquals(listOf(4901, 4902), player.lastQueue.map(Ayah::id))
    }

    @Test
    fun pausedSelectedQueueResumesButDifferentQueueIsReplaced() = runTest {
        val player = FakeQuranAudioPlayer()
        val viewModel = AudioViewModel(
            quranRepository = FakeQuranRepository(),
            settingsRepository = FakeSettingsRepository(
                UserSettings(lastReadSurah = 55, lastReadAyah = 4901),
            ),
            audioPlayer = player,
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()

        player.mutableState.value = QuranPlaybackState.Paused
        player.mutableInfo.value = QuranPlaybackInfo(mediaId = "ayah:4902")
        advanceUntilIdle()
        viewModel.playOrPauseSelectedSurah()
        assertEquals(1, player.toggleCalls)
        assertTrue(player.lastQueue.isEmpty())

        player.mutableInfo.value = QuranPlaybackInfo(mediaId = "ayah:1")
        advanceUntilIdle()
        viewModel.playOrPauseSelectedSurah()
        assertEquals(listOf(4901, 4902), player.lastQueue.map(Ayah::id))
    }

    @Test
    fun transportAndSeekCommandsDelegateToTheMediaBoundary() = runTest {
        val player = FakeQuranAudioPlayer()
        val viewModel = AudioViewModel(
            quranRepository = FakeQuranRepository(),
            settingsRepository = FakeSettingsRepository(UserSettings(lastReadSurah = 55, lastReadAyah = 4901)),
            audioPlayer = player,
        )

        viewModel.skipToPrevious()
        viewModel.skipToNext()
        viewModel.seekTo(12_500L)
        viewModel.seekTo(-1L)
        viewModel.stop()

        assertEquals(1, player.previousCalls)
        assertEquals(1, player.nextCalls)
        assertEquals(listOf(12_500L), player.seekPositions)
        assertEquals(1, player.stopCalls)
    }

    @Test
    fun reciterFailureEmitsFeedbackAndRestoresTheControlState() = runTest {
        val settings = FakeSettingsRepository(UserSettings(), failReciterUpdate = true)
        val viewModel = AudioViewModel(
            quranRepository = FakeQuranRepository(),
            settingsRepository = settings,
            audioPlayer = FakeQuranAudioPlayer(),
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        val event = backgroundScope.async(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.first()
        }
        advanceUntilIdle()

        viewModel.setReciter(Reciter.MINSHAWI)
        advanceUntilIdle()

        assertEquals(AudioEvent.ReciterUpdateFailed, event.await())
        assertFalse(viewModel.uiState.value.isReciterUpdating)
    }

    @Test
    fun retryReopensRepositoryFlowsAfterARealObservationFailure() = runTest {
        val repository = FakeQuranRepository(failuresBeforeSuccess = 1)
        val viewModel = AudioViewModel(
            quranRepository = repository,
            settingsRepository = FakeSettingsRepository(UserSettings()),
            audioPlayer = FakeQuranAudioPlayer(),
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.hasError)

        viewModel.retry()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasError)
        assertEquals(2, repository.observeSurahsCalls)
    }

    private class FakeQuranAudioPlayer : QuranAudioPlayer {
        val mutableState = MutableStateFlow<QuranPlaybackState>(QuranPlaybackState.Idle)
        val mutableInfo = MutableStateFlow(QuranPlaybackInfo())
        override val playbackState: StateFlow<QuranPlaybackState> = mutableState
        override val playbackInfo: StateFlow<QuranPlaybackInfo> = mutableInfo
        var lastQueue: List<Ayah> = emptyList()
        var lastReciter: Reciter? = null
        var lastSurahName: String? = null
        var toggleCalls = 0
        var previousCalls = 0
        var nextCalls = 0
        var stopCalls = 0
        val seekPositions = mutableListOf<Long>()

        override fun playAyah(ayah: Ayah, reciter: Reciter, surahName: String) = Unit

        override fun playSurah(
            ayahs: List<Ayah>,
            reciter: Reciter,
            surahName: String,
            startAyahId: Int?,
        ) {
            lastQueue = ayahs
            lastReciter = reciter
            lastSurahName = surahName
        }

        override fun togglePause() {
            toggleCalls += 1
        }

        override fun skipToPrevious() {
            previousCalls += 1
        }

        override fun skipToNext() {
            nextCalls += 1
        }

        override fun seekTo(positionMs: Long) {
            seekPositions += positionMs
        }

        override fun stop() {
            stopCalls += 1
        }
    }

    private class FakeQuranRepository(
        private var failuresBeforeSuccess: Int = 0,
    ) : QuranRepository {
        var observeSurahsCalls = 0
            private set
        private val surahs = listOf(
            Surah(1, "الفاتحة", "Al-Fatihah", "The Opening", RevelationType.MECCAN, 7, 1),
            Surah(55, "الرحمن", "Ar-Rahman", "The Most Merciful", RevelationType.MEDINAN, 78, 531),
        )
        private val ayahs = mapOf(
            1 to listOf(sampleAyah(id = 1, surah = 1, number = 1, page = 1)),
            55 to listOf(
                sampleAyah(id = 4901, surah = 55, number = 1, page = 531),
                sampleAyah(id = 4902, surah = 55, number = 2, page = 531),
            ),
        )

        override fun observeSurahs(): Flow<List<Surah>> {
            observeSurahsCalls += 1
            return if (failuresBeforeSuccess > 0) {
                failuresBeforeSuccess -= 1
                flow { throw IOException("surah observation failure") }
            } else {
                flowOf(surahs)
            }
        }

        override fun observeSurah(number: Int): Flow<Surah?> = flowOf(surahs.firstOrNull { it.number == number })
        override fun observeAyahsForSurah(surahNumber: Int): Flow<List<Ayah>> = flowOf(ayahs[surahNumber].orEmpty())
        override fun observeAyahsForPage(pageNumber: Int): Flow<List<Ayah>> = flowOf(emptyList())
        override fun observeAyahsForJuz(juzNumber: Int): Flow<List<Ayah>> = flowOf(emptyList())
        override suspend fun getAyah(id: Int): Ayah? = ayahs.values.flatten().firstOrNull { it.id == id }
        override suspend fun searchAyahs(normalizedQuery: String, limit: Int): List<AyahSearchResult> = emptyList()
        override suspend fun isPopulated(): Boolean = true
        override suspend fun replaceCorpus(corpus: QuranCorpus) = Unit
    }

    private class FakeSettingsRepository(
        initial: UserSettings,
        private val failReciterUpdate: Boolean = false,
    ) : SettingsRepository {
        private val mutableSettings = MutableStateFlow(initial)
        override val settings: Flow<UserSettings> = mutableSettings

        override suspend fun setThemeMode(value: ThemeMode) {
            mutableSettings.value = mutableSettings.value.copy(themeMode = value)
        }

        override suspend fun setDynamicColorEnabled(value: Boolean) {
            mutableSettings.value = mutableSettings.value.copy(dynamicColorEnabled = value)
        }

        override suspend fun setQuranTextScale(value: Float) {
            mutableSettings.value = mutableSettings.value.copy(quranTextScale = value)
        }

        override suspend fun setReciter(value: Reciter) {
            if (failReciterUpdate) throw IOException("settings failure")
            mutableSettings.value = mutableSettings.value.copy(reciter = value)
        }

        override suspend fun setLastRead(surahNumber: Int, ayahId: Int) {
            mutableSettings.value = mutableSettings.value.copy(
                lastReadSurah = surahNumber,
                lastReadAyah = ayahId,
            )
        }
    }

    private companion object {
        fun sampleAyah(id: Int, surah: Int, number: Int, page: Int) = Ayah(
            id = id,
            surahNumber = surah,
            numberInSurah = number,
            textUthmani = "آية $number",
            juzNumber = if (surah == 55) 27 else 1,
            hizbQuarter = if (surah == 55) 213 else 1,
            pageNumber = page,
        )
    }
}
