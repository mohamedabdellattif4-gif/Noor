package com.noor.app.ui.adhkar

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.noor.core.designsystem.theme.NoorTheme
import org.junit.Rule
import org.junit.Test

class TasbihPremiumUiTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun counterIsAnAccessibleActionWhileIncomplete() {
        composeRule.setContent {
            NoorTheme {
                TasbihScreen(
                    uiState = TasbihUiState(count = 2, target = 33),
                    onBack = {},
                    onIncrement = {},
                    onReset = {},
                    onPhraseSelected = {},
                    onTargetSelected = {},
                )
            }
        }

        composeRule.onNodeWithTag(TasbihTestTags.COUNTER).assertHasClickAction()
    }

    @Test
    fun completedCounterIsDisabledForAccessibility() {
        composeRule.setContent {
            NoorTheme {
                TasbihScreen(
                    uiState = TasbihUiState(count = 33, target = 33),
                    onBack = {},
                    onIncrement = {},
                    onReset = {},
                    onPhraseSelected = {},
                    onTargetSelected = {},
                )
            }
        }

        composeRule.onNodeWithTag(TasbihTestTags.COUNTER).assertIsNotEnabled()
    }
}
