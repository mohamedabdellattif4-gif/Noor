package com.noor.app.ui.bookmarks

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.noor.core.designsystem.theme.NoorTheme
import com.noor.domain.model.Ayah
import com.noor.domain.model.Bookmark
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class BookmarksPremiumUiTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun tappingABookmarkOpensTheSelectedAyah() {
        val bookmark = sampleBookmark()
        var openedAyahId: Int? = null
        composeRule.setContent {
            NoorTheme {
                BookmarksScreen(
                    uiState = BookmarksUiState(
                        bookmarks = listOf(bookmark),
                        isLoading = false,
                    ),
                    snackbarHostState = remember { SnackbarHostState() },
                    onBack = {},
                    onRetry = {},
                    onOpenBookmark = { openedAyahId = it.ayah.id },
                    onRemoveBookmark = {},
                )
            }
        }

        composeRule.onNodeWithTag(BookmarksTestTags.item(bookmark.ayah.id))
            .assertIsDisplayed()
            .performClick()

        assertEquals(bookmark.ayah.id, openedAyahId)
    }

    @Test
    fun removeActionTargetsTheBookmarkWithoutOpeningIt() {
        val bookmark = sampleBookmark()
        var removedAyahId: Int? = null
        var opened = false
        composeRule.setContent {
            NoorTheme {
                BookmarksScreen(
                    uiState = BookmarksUiState(
                        bookmarks = listOf(bookmark),
                        isLoading = false,
                    ),
                    snackbarHostState = remember { SnackbarHostState() },
                    onBack = {},
                    onRetry = {},
                    onOpenBookmark = { opened = true },
                    onRemoveBookmark = { removedAyahId = it },
                )
            }
        }

        composeRule.onNodeWithTag(BookmarksTestTags.remove(bookmark.ayah.id))
            .assertIsDisplayed()
            .performClick()

        assertEquals(bookmark.ayah.id, removedAyahId)
        assertFalse(opened)
    }

    @Test
    fun pendingRemovalDisablesTheRemoveAction() {
        val bookmark = sampleBookmark()
        composeRule.setContent {
            NoorTheme {
                BookmarksScreen(
                    uiState = BookmarksUiState(
                        bookmarks = listOf(bookmark),
                        isLoading = false,
                        pendingRemovalIds = setOf(bookmark.ayah.id),
                    ),
                    snackbarHostState = remember { SnackbarHostState() },
                    onBack = {},
                    onRetry = {},
                    onOpenBookmark = {},
                    onRemoveBookmark = {},
                )
            }
        }

        composeRule.onNodeWithTag(BookmarksTestTags.remove(bookmark.ayah.id))
            .assertIsDisplayed()
            .assertIsNotEnabled()
    }

    @Test
    fun emptyStateProvidesABrowseAction() {
        var browsed = false
        composeRule.setContent {
            NoorTheme {
                BookmarksScreen(
                    uiState = BookmarksUiState(isLoading = false),
                    snackbarHostState = remember { SnackbarHostState() },
                    onBack = { browsed = true },
                    onRetry = {},
                    onOpenBookmark = {},
                    onRemoveBookmark = {},
                )
            }
        }

        composeRule.onNodeWithTag(BookmarksTestTags.EMPTY_ACTION)
            .assertIsDisplayed()
            .performClick()

        assertTrue(browsed)
    }

    private fun sampleBookmark(): Bookmark = Bookmark(
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
        createdAtEpochMillis = 1L,
    )
}
