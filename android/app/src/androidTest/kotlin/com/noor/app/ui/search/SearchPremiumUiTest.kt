package com.noor.app.ui.search

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.noor.core.designsystem.theme.NoorTheme
import com.noor.domain.model.Ayah
import com.noor.domain.model.AyahSearchResult
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SearchPremiumUiTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun clearControlIsAvailableForANonEmptyQuery() {
        var cleared = false
        composeRule.setContent {
            NoorTheme {
                SearchScreen(
                    uiState = SearchUiState(query = "الرحمن"),
                    onQueryChanged = {},
                    onClear = { cleared = true },
                    onRetry = {},
                    onBack = {},
                    onOpenResult = {},
                )
            }
        }

        composeRule.onNodeWithTag(SearchTestTags.CLEAR_QUERY)
            .assertIsDisplayed()
            .performClick()

        assertTrue(cleared)
    }

    @Test
    fun tappingAResultOpensTheSelectedAyah() {
        val result = previewResult()
        var opened = false
        composeRule.setContent {
            NoorTheme {
                SearchScreen(
                    uiState = SearchUiState(
                        query = "الرحمن",
                        results = listOf(result),
                        hasSearched = true,
                    ),
                    onQueryChanged = {},
                    onClear = {},
                    onRetry = {},
                    onBack = {},
                    onOpenResult = { opened = it.ayah.id == result.ayah.id },
                )
            }
        }

        composeRule.onNodeWithTag(SearchTestTags.result(result.ayah.id))
            .assertIsDisplayed()
            .performClick()

        assertTrue(opened)
    }

    @Test
    fun errorStateProvidesRetryAction() {
        var retried = false
        composeRule.setContent {
            NoorTheme {
                SearchScreen(
                    uiState = SearchUiState(
                        query = "الرحمن",
                        hasSearched = true,
                        hasError = true,
                    ),
                    onQueryChanged = {},
                    onClear = {},
                    onRetry = { retried = true },
                    onBack = {},
                    onOpenResult = {},
                )
            }
        }

        composeRule.onNodeWithTag(SearchTestTags.RETRY)
            .assertIsDisplayed()
            .performClick()

        assertTrue(retried)
    }

    private fun previewResult(): AyahSearchResult = AyahSearchResult(
        ayah = Ayah(
            id = 4901,
            surahNumber = 55,
            numberInSurah = 1,
            textUthmani = "ٱلرَّحْمَٰنُ",
            juzNumber = 27,
            hizbQuarter = 213,
            pageNumber = 531,
        ),
        surahNameArabic = "الرحمن",
    )
}
