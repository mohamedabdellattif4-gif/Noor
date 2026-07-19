package com.noor.app.ui.audio

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.noor.core.designsystem.theme.NoorTheme
import com.noor.core.media.QuranPlaybackInfo
import com.noor.core.media.QuranPlaybackState
import com.noor.domain.model.Ayah
import com.noor.domain.model.Reciter
import com.noor.domain.model.RevelationType
import com.noor.domain.model.Surah
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AudioPremiumUiTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun playerControlsExposeValidEnabledStatesAndActions() {
        var played = false
        var advanced = false
        composeRule.setContent {
            NoorTheme {
                AudioScreen(
                    uiState = sampleState(),
                    snackbarHostState = remember { SnackbarHostState() },
                    onBack = {},
                    onRetry = {},
                    onSelectSurah = {},
                    onSelectReciter = {},
                    onPlayPause = { played = true },
                    onPrevious = {},
                    onNext = { advanced = true },
                    onStop = {},
                    onSeek = {},
                    onOpenReader = {},
                )
            }
        }

        composeRule.onNodeWithTag(AudioTestTags.PLAY_PAUSE)
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()
        composeRule.onNodeWithTag(AudioTestTags.PREVIOUS).assertIsNotEnabled()
        composeRule.onNodeWithTag(AudioTestTags.NEXT)
            .assertIsEnabled()
            .performClick()

        assertTrue(played)
        assertTrue(advanced)
    }

    @Test
    fun selectedReciterIsAccessibleAndAnotherReciterCanBeChosen() {
        var selected: Reciter? = null
        composeRule.setContent {
            NoorTheme {
                AudioScreen(
                    uiState = sampleState(),
                    snackbarHostState = remember { SnackbarHostState() },
                    onBack = {},
                    onRetry = {},
                    onSelectSurah = {},
                    onSelectReciter = { selected = it },
                    onPlayPause = {},
                    onPrevious = {},
                    onNext = {},
                    onStop = {},
                    onSeek = {},
                    onOpenReader = {},
                )
            }
        }

        composeRule.onNodeWithTag(AudioTestTags.reciter(Reciter.ALAFASY)).assertIsSelected()
        composeRule.onNodeWithTag(AudioTestTags.reciter(Reciter.HUSARY))
            .performClick()

        assertEquals(Reciter.HUSARY, selected)
    }

    @Test
    fun selectingASurahTargetsThatExactSurah() {
        var selectedSurah: Int? = null
        composeRule.setContent {
            NoorTheme {
                AudioScreen(
                    uiState = sampleState(),
                    snackbarHostState = remember { SnackbarHostState() },
                    onBack = {},
                    onRetry = {},
                    onSelectSurah = { selectedSurah = it },
                    onSelectReciter = {},
                    onPlayPause = {},
                    onPrevious = {},
                    onNext = {},
                    onStop = {},
                    onSeek = {},
                    onOpenReader = {},
                )
            }
        }

        composeRule.onNodeWithTag(AudioTestTags.surah(67))
            .performScrollTo()
            .performClick()

        assertEquals(67, selectedSurah)
    }

    @Test
    fun playerOffersAReaderTransition() {
        var opened = false
        composeRule.setContent {
            NoorTheme {
                AudioScreen(
                    uiState = sampleState(),
                    snackbarHostState = remember { SnackbarHostState() },
                    onBack = {},
                    onRetry = {},
                    onSelectSurah = {},
                    onSelectReciter = {},
                    onPlayPause = {},
                    onPrevious = {},
                    onNext = {},
                    onStop = {},
                    onSeek = {},
                    onOpenReader = { opened = true },
                )
            }
        }

        composeRule.onNodeWithTag(AudioTestTags.OPEN_READER)
            .assertIsDisplayed()
            .performClick()

        assertTrue(opened)
    }

    private fun sampleState(): AudioUiState {
        val rahman = Surah(
            number = 55,
            nameArabic = "الرحمن",
            nameTransliterated = "Ar-Rahman",
            nameTranslated = "The Most Merciful",
            revelationType = RevelationType.MEDINAN,
            ayahCount = 78,
            startPage = 531,
        )
        val mulk = Surah(
            number = 67,
            nameArabic = "الملك",
            nameTransliterated = "Al-Mulk",
            nameTranslated = "The Sovereignty",
            revelationType = RevelationType.MECCAN,
            ayahCount = 30,
            startPage = 562,
        )
        return AudioUiState(
            surahs = listOf(rahman, mulk),
            selectedSurah = rahman,
            selectedAyahs = listOf(
                Ayah(4901, 55, 1, "ٱلرَّحْمَٰنُ", 27, 213, 531),
                Ayah(4902, 55, 2, "عَلَّمَ ٱلْقُرْءَانَ", 27, 213, 531),
            ),
            reciter = Reciter.ALAFASY,
            playbackState = QuranPlaybackState.Paused,
            playbackInfo = QuranPlaybackInfo(
                mediaId = "ayah:4901",
                currentIndex = 0,
                mediaCount = 2,
                durationMs = 60_000L,
                canSkipPrevious = false,
                canSkipNext = true,
            ),
            isLoading = false,
        )
    }
}
