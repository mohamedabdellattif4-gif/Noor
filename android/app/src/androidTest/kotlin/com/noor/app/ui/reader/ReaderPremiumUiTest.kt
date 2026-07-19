package com.noor.app.ui.reader

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.noor.core.designsystem.theme.NoorTheme
import com.noor.domain.model.Ayah
import com.noor.domain.model.RevelationType
import com.noor.domain.model.Surah
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ReaderPremiumUiTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun premiumReaderRendersExactQuranTextAndWiresCurrentAyahActions() {
        val ayah = Ayah(
            id = 4901,
            surahNumber = 55,
            numberInSurah = 1,
            textUthmani = "ٱلرَّحْمَٰنُ",
            juzNumber = 27,
            hizbQuarter = 215,
            pageNumber = 531,
        )
        var bookmarkedAyahId: Int? = null
        composeRule.setContent {
            NoorTheme(darkTheme = false) {
                ReaderScreen(
                    uiState = ReaderUiState(
                        surah = Surah(
                            number = 55,
                            nameArabic = "الرحمن",
                            nameTransliterated = "Ar-Rahman",
                            nameTranslated = "The Most Merciful",
                            revelationType = RevelationType.MEDINAN,
                            ayahCount = 78,
                            startPage = 531,
                        ),
                        ayahs = listOf(ayah),
                        isLoading = false,
                    ),
                    snackbarHostState = remember { SnackbarHostState() },
                    onBack = {},
                    onRetry = {},
                    onToggleBookmark = { selectedAyah -> bookmarkedAyahId = selectedAyah.id },
                    onPlayAyah = {},
                    onPlaySurah = {},
                    onTogglePause = {},
                    onStopAudio = {},
                    onSetTextScale = {},
                    onAyahVisible = {},
                    onOpenTafsir = {},
                )
            }
        }

        composeRule.onNodeWithTag(ReaderTestTags.SURAH_TITLE).assertExists()
        composeRule.onNodeWithText(ayah.textUthmani).assertExists()
        composeRule.onNodeWithTag(ReaderTestTags.BOOKMARK_ACTION).performClick()
        assertEquals(ayah.id, bookmarkedAyahId)

        composeRule.onNodeWithTag(ReaderTestTags.TEXT_SIZE_ACTION).performClick()
        composeRule.onNodeWithTag(ReaderTestTags.TEXT_SIZE_DIALOG).assertExists()
    }
}
