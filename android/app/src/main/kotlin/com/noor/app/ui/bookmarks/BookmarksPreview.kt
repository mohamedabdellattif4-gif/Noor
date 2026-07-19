package com.noor.app.ui.bookmarks

import android.content.res.Configuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.noor.core.designsystem.theme.NoorTheme
import com.noor.domain.model.Ayah
import com.noor.domain.model.Bookmark

@Preview(
    name = "Premium Bookmarks Arabic",
    locale = "ar",
    showBackground = true,
    widthDp = 412,
    heightDp = 892,
)
@Composable
private fun PremiumBookmarksArabicPreview() {
    BookmarksPreviewContent(darkTheme = false, empty = false)
}

@Preview(
    name = "Premium Bookmarks Empty Dark",
    locale = "en",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    widthDp = 412,
    heightDp = 892,
)
@Composable
private fun PremiumBookmarksEmptyDarkPreview() {
    BookmarksPreviewContent(darkTheme = true, empty = true)
}

@Composable
private fun BookmarksPreviewContent(
    darkTheme: Boolean,
    empty: Boolean,
) {
    NoorTheme(darkTheme = darkTheme) {
        BookmarksScreen(
            uiState = BookmarksUiState(
                bookmarks = if (empty) emptyList() else previewBookmarks(),
                isLoading = false,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {},
            onRetry = {},
            onOpenBookmark = {},
            onRemoveBookmark = {},
        )
    }
}

private fun previewBookmarks(): List<Bookmark> = listOf(
    previewBookmark(
        id = 4901,
        ayahNumber = 1,
        text = "ٱلرَّحْمَٰنُ",
        createdAt = 2L,
    ),
    previewBookmark(
        id = 4902,
        ayahNumber = 2,
        text = "عَلَّمَ ٱلْقُرْءَانَ",
        createdAt = 1L,
    ),
)

private fun previewBookmark(
    id: Int,
    ayahNumber: Int,
    text: String,
    createdAt: Long,
): Bookmark = Bookmark(
    ayah = Ayah(
        id = id,
        surahNumber = 55,
        numberInSurah = ayahNumber,
        textUthmani = text,
        juzNumber = 27,
        hizbQuarter = 213,
        pageNumber = 531,
    ),
    surahNameArabic = "الرحمن",
    createdAtEpochMillis = createdAt,
)
