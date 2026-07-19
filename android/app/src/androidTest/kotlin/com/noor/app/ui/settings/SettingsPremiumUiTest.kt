package com.noor.app.ui.settings

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.noor.core.designsystem.theme.NoorTheme
import com.noor.domain.model.ThemeMode
import com.noor.domain.model.UserSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsPremiumUiTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun entireDynamicColorRowIsInteractive() {
        var requestedValue = false
        composeRule.setContent {
            NoorTheme {
                SettingsScreen(
                    settings = UserSettings(dynamicColorEnabled = false),
                    snackbarHostState = remember { SnackbarHostState() },
                    onBack = {},
                    onOpenLegal = {},
                    onThemeModeChange = {},
                    onDynamicColorChange = { requestedValue = it },
                    onTextScaleChange = {},
                    onReciterChange = {},
                )
            }
        }

        composeRule.onNodeWithTag(SettingsTestTags.DYNAMIC_COLOR).performClick()

        assertTrue(requestedValue)
    }

    @Test
    fun entireThemeRowIsInteractive() {
        var selectedMode = ThemeMode.SYSTEM
        composeRule.setContent {
            NoorTheme {
                SettingsScreen(
                    settings = UserSettings(),
                    snackbarHostState = remember { SnackbarHostState() },
                    onBack = {},
                    onOpenLegal = {},
                    onThemeModeChange = { selectedMode = it },
                    onDynamicColorChange = {},
                    onTextScaleChange = {},
                    onReciterChange = {},
                )
            }
        }

        composeRule.onNodeWithTag(SettingsTestTags.themeMode(ThemeMode.DARK)).performClick()

        assertEquals(ThemeMode.DARK, selectedMode)
    }
}
