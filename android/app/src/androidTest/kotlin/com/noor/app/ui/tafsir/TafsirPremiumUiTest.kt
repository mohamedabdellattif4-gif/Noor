package com.noor.app.ui.tafsir

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.noor.core.designsystem.theme.NoorTheme
import com.noor.domain.model.Ayah
import com.noor.domain.model.Tafsir
import com.noor.domain.model.TafsirOrigin
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TafsirPremiumUiTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun loadedStateShowsCanonicalAyahExplanationFootnotesAndSource() {
        composeRule.setContent {
            NoorTheme {
                TafsirScreen(
                    uiState = TafsirUiState(
                        ayah = sampleAyah(),
                        tafsir = sampleTafsir(footnotes = "هامش موثق"),
                        isLoading = false,
                    ),
                    snackbarHostState = remember { SnackbarHostState() },
                    onBack = {},
                    onRetry = {},
                    onRefresh = {},
                )
            }
        }

        composeRule.onNodeWithTag(TafsirTestTags.AYAH_CARD).assertIsDisplayed()
        composeRule.onNodeWithTag(TafsirTestTags.TAFSIR_TEXT).assertIsDisplayed()
        composeRule.onNodeWithTag(TafsirTestTags.FOOTNOTES_CARD)
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithTag(TafsirTestTags.SOURCE_CARD)
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun refreshActionCallsTheRefreshHandler() {
        var refreshed = false
        composeRule.setContent {
            NoorTheme {
                TafsirScreen(
                    uiState = TafsirUiState(
                        ayah = sampleAyah(),
                        tafsir = sampleTafsir(),
                        isLoading = false,
                    ),
                    snackbarHostState = remember { SnackbarHostState() },
                    onBack = {},
                    onRetry = {},
                    onRefresh = { refreshed = true },
                )
            }
        }

        composeRule.onNodeWithTag(TafsirTestTags.REFRESH)
            .assertIsDisplayed()
            .performClick()

        assertTrue(refreshed)
    }

    @Test
    fun refreshingStateDisablesDuplicateRefresh() {
        composeRule.setContent {
            NoorTheme {
                TafsirScreen(
                    uiState = TafsirUiState(
                        ayah = sampleAyah(),
                        tafsir = sampleTafsir(),
                        isLoading = false,
                        isRefreshing = true,
                    ),
                    snackbarHostState = remember { SnackbarHostState() },
                    onBack = {},
                    onRetry = {},
                    onRefresh = {},
                )
            }
        }

        composeRule.onNodeWithTag(TafsirTestTags.REFRESH)
            .assertIsDisplayed()
            .assertIsNotEnabled()
    }

    @Test
    fun errorStateOffersRetry() {
        var retried = false
        composeRule.setContent {
            NoorTheme {
                TafsirScreen(
                    uiState = TafsirUiState(isLoading = false, hasError = true),
                    snackbarHostState = remember { SnackbarHostState() },
                    onBack = {},
                    onRetry = { retried = true },
                    onRefresh = {},
                )
            }
        }

        composeRule.onNodeWithTag(TafsirTestTags.RETRY)
            .assertIsDisplayed()
            .performClick()

        assertTrue(retried)
    }

    private fun sampleAyah() = Ayah(
        id = 1,
        surahNumber = 1,
        numberInSurah = 1,
        textUthmani = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
        juzNumber = 1,
        hizbQuarter = 1,
        pageNumber = 1,
    )

    private fun sampleTafsir(footnotes: String? = null) = Tafsir(
        ayahId = 1,
        text = "تفسير الآية",
        footnotes = footnotes,
        sourceKey = "arabic_moyassar",
        sourceName = "التفسير الميسر — QuranEnc.com",
        fetchedAtEpochMillis = 1L,
        origin = TafsirOrigin.CACHE,
    )
}
