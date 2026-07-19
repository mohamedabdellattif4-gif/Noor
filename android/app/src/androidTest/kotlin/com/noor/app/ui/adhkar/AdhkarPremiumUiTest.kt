package com.noor.app.ui.adhkar

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import com.noor.core.designsystem.theme.NoorTheme
import com.noor.domain.model.AdhkarCategory
import com.noor.domain.model.Dhikr
import org.junit.Rule
import org.junit.Test

class AdhkarPremiumUiTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun contentShowsBundledDhikrCard() {
        val item = sampleDhikr()
        composeRule.setContent {
            NoorTheme {
                AdhkarScreen(
                    uiState = AdhkarUiState(items = listOf(item), isLoading = false),
                    snackbarHostState = remember { SnackbarHostState() },
                    onBack = {},
                    onOpenTasbih = {},
                    onOpenReminders = {},
                    onQueryChange = {},
                    onCategorySelected = {},
                    onToggleFavoritesOnly = {},
                    onToggleFavorite = {},
                    onIncrement = {},
                    onReset = {},
                )
            }
        }

        composeRule.onNodeWithTag(AdhkarTestTags.CONTENT).assertIsDisplayed()
        composeRule.onNodeWithTag("${AdhkarTestTags.ITEM_PREFIX}${item.id}")
            .performScrollTo()
            .assertIsDisplayed()
    }


    @Test
    fun favoriteControlExposesToggleState() {
        val item = sampleDhikr()
        composeRule.setContent {
            NoorTheme {
                AdhkarScreen(
                    uiState = AdhkarUiState(items = listOf(item), isLoading = false),
                    snackbarHostState = remember { SnackbarHostState() },
                    onBack = {},
                    onOpenTasbih = {},
                    onOpenReminders = {},
                    onQueryChange = {},
                    onCategorySelected = {},
                    onToggleFavoritesOnly = {},
                    onToggleFavorite = {},
                    onIncrement = {},
                    onReset = {},
                )
            }
        }

        composeRule.onNodeWithTag("${AdhkarTestTags.FAVORITE_PREFIX}${item.id}")
            .assertIsOff()
    }

    @Test
    fun favoriteControlReportsSelectedState() {
        val item = sampleDhikr().copy(isFavorite = true)
        composeRule.setContent {
            NoorTheme {
                AdhkarScreen(
                    uiState = AdhkarUiState(items = listOf(item), isLoading = false),
                    snackbarHostState = remember { SnackbarHostState() },
                    onBack = {},
                    onOpenTasbih = {},
                    onOpenReminders = {},
                    onQueryChange = {},
                    onCategorySelected = {},
                    onToggleFavoritesOnly = {},
                    onToggleFavorite = {},
                    onIncrement = {},
                    onReset = {},
                )
            }
        }

        composeRule.onNodeWithTag("${AdhkarTestTags.FAVORITE_PREFIX}${item.id}")
            .assertIsOn()
    }

    private fun sampleDhikr() = Dhikr(
        id = "sample",
        category = AdhkarCategory.MORNING,
        title = "ذكر الصباح",
        textArabic = "سُبْحَانَ اللَّهِ",
        reference = "صحيح مسلم",
        repeatCount = 3,
    )
}
