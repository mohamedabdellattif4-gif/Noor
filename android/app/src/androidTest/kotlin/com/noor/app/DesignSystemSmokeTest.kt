package com.noor.app

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.noor.core.designsystem.theme.NoorTheme
import org.junit.Rule
import org.junit.Test

class DesignSystemSmokeTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun themeRendersContent() {
        composeRule.setContent { NoorTheme { Text("Noor") } }
        composeRule.onNodeWithText("Noor").assertExists()
    }
}
