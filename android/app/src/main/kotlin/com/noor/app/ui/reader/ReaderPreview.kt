package com.noor.app.ui.reader

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.noor.core.designsystem.theme.NoorTheme
import com.noor.domain.model.Ayah
import com.noor.domain.model.RevelationType
import com.noor.domain.model.Surah

@Preview(
    name = "Premium Reader — Arabic",
    locale = "ar",
    showBackground = true,
    widthDp = 412,
    heightDp = 915,
)
@Composable
private fun PremiumReaderArabicPreview() {
    NoorTheme(darkTheme = false) {
        ReaderScreen(
            uiState = previewReaderState(),
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {},
            onRetry = {},
            onToggleBookmark = {},
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

@Preview(
    name = "Premium Reader — English UI",
    locale = "en",
    showBackground = true,
    widthDp = 412,
    heightDp = 915,
)
@Composable
private fun PremiumReaderEnglishPreview() {
    NoorTheme(darkTheme = false) {
        ReaderScreen(
            uiState = previewReaderState(),
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {},
            onRetry = {},
            onToggleBookmark = {},
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

private fun previewReaderState(): ReaderUiState {
    val ayahs = listOf(
        previewAyah(1, 4901, "ٱلرَّحْمَٰنُ"),
        previewAyah(2, 4902, "عَلَّمَ ٱلْقُرْءَانَ"),
        previewAyah(3, 4903, "خَلَقَ ٱلْإِنسَٰنَ"),
        previewAyah(4, 4904, "عَلَّمَهُ ٱلْبَيَانَ"),
    )
    return ReaderUiState(
        surah = Surah(
            number = 55,
            nameArabic = "الرحمن",
            nameTransliterated = "Ar-Rahman",
            nameTranslated = "The Most Merciful",
            revelationType = RevelationType.MEDINAN,
            ayahCount = 78,
            startPage = 531,
        ),
        ayahs = ayahs,
        bookmarkedAyahIds = setOf(4902),
        textScale = 1f,
        targetAyahId = 4901,
        isLoading = false,
    )
}

private fun previewAyah(number: Int, id: Int, text: String) = Ayah(
    id = id,
    surahNumber = 55,
    numberInSurah = number,
    textUthmani = text,
    juzNumber = 27,
    hizbQuarter = 215,
    pageNumber = 531,
)
