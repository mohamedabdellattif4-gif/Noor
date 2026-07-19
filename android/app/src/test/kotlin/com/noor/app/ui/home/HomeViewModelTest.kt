package com.noor.app.ui.home

import com.noor.app.testing.MainDispatcherRule
import com.noor.domain.model.Ayah
import com.noor.domain.model.AyahSearchResult
import com.noor.domain.model.QuranCorpus
import com.noor.domain.model.RevelationType
import com.noor.domain.model.Surah
import com.noor.domain.model.ThemeMode
import com.noor.domain.model.UserSettings
import com.noor.domain.model.Reciter
import com.noor.domain.repository.QuranRepository
import com.noor.domain.repository.SettingsRepository
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun contentCombinesSurahsAndLastReadSettings() = runTest {
        val surahs = listOf(sampleSurah())
        val ayah = sampleAyah()
        val settings = FakeSettingsRepository(
            UserSettings(
                lastReadSurah = 1,
                lastReadAyah = ayah.id,
                dailyPagesRead = 3,
                readingStreakDays = 12,
            ),
        )
        val viewModel = HomeViewModel(FakeQuranRepository(surahs, ayah = ayah), settings)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(surahs, state.surahs)
        assertEquals(surahs.first(), state.continueSurah)
        assertEquals(ayah, state.continueAyah)
        assertEquals(3, state.dailyPagesRead)
        assertEquals(12, state.readingStreakDays)
        assertFalse(state.isLoading)
        assertFalse(state.hasError)
    }

    @Test
    fun retryRecoversAfterObservationFailure() = runTest {
        val repository = FakeQuranRepository(
            surahs = listOf(sampleSurah()),
            failuresBeforeSuccess = 1,
        )
        val viewModel = HomeViewModel(repository, FakeSettingsRepository(UserSettings()))

        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.hasError)

        viewModel.retry()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasError)
        assertEquals(1, viewModel.uiState.value.surahs.size)
        assertEquals(2, repository.observeCalls)
    }

    private class FakeQuranRepository(
        private val surahs: List<Surah>,
        private var failuresBeforeSuccess: Int = 0,
        private val ayah: Ayah? = null,
    ) : QuranRepository {
        var observeCalls: Int = 0
            private set

        override fun observeSurahs(): Flow<List<Surah>> {
            observeCalls += 1
            return if (failuresBeforeSuccess > 0) {
                failuresBeforeSuccess -= 1
                flow { throw IOException("test failure") }
            } else {
                flowOf(surahs)
            }
        }

        override fun observeSurah(number: Int): Flow<Surah?> = flowOf(null)
        override fun observeAyahsForSurah(surahNumber: Int): Flow<List<Ayah>> = flowOf(emptyList())
        override fun observeAyahsForPage(pageNumber: Int): Flow<List<Ayah>> = flowOf(emptyList())
        override fun observeAyahsForJuz(juzNumber: Int): Flow<List<Ayah>> = flowOf(emptyList())
        override suspend fun getAyah(id: Int): Ayah? = ayah?.takeIf { it.id == id }
        override suspend fun searchAyahs(normalizedQuery: String, limit: Int): List<AyahSearchResult> = emptyList()
        override suspend fun isPopulated(): Boolean = true
        override suspend fun replaceCorpus(corpus: QuranCorpus) = Unit
    }

    private class FakeSettingsRepository(initial: UserSettings) : SettingsRepository {
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
        fun sampleAyah() = Ayah(
            id = 3,
            surahNumber = 1,
            numberInSurah = 3,
            textUthmani = "الرَّحْمَٰنِ الرَّحِيمِ",
            juzNumber = 1,
            hizbQuarter = 1,
            pageNumber = 1,
        )

        fun sampleSurah() = Surah(
            number = 1,
            nameArabic = "الفاتحة",
            nameTransliterated = "Al-Fatihah",
            nameTranslated = "The Opening",
            revelationType = RevelationType.MECCAN,
            ayahCount = 7,
            startPage = 1,
        )
    }
}
