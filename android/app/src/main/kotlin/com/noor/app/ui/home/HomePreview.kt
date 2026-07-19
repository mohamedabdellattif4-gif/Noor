package com.noor.app.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.noor.core.designsystem.theme.NoorTheme
import com.noor.domain.model.Ayah
import com.noor.domain.model.Reciter
import com.noor.domain.model.RevelationType
import com.noor.domain.model.Surah

@Preview(
    name = "Premium Home — Arabic",
    locale = "ar",
    showBackground = true,
    widthDp = 412,
    heightDp = 915,
)
@Composable
private fun PremiumHomeArabicPreview() {
    NoorTheme(darkTheme = false) {
        HomeScreen(
            uiState = previewState(),
            onOpenSurah = { _, _ -> },
            onOpenTafsir = {},
            onSearch = {},
            onBookmarks = {},
            onAudio = {},
            onAdhkar = {},
            onSettings = {},
            onRetry = {},
        )
    }
}

@Preview(
    name = "Premium Home — English",
    locale = "en",
    showBackground = true,
    widthDp = 412,
    heightDp = 915,
)
@Composable
private fun PremiumHomeEnglishPreview() {
    NoorTheme(darkTheme = false) {
        HomeScreen(
            uiState = previewState(),
            onOpenSurah = { _, _ -> },
            onOpenTafsir = {},
            onSearch = {},
            onBookmarks = {},
            onAudio = {},
            onAdhkar = {},
            onSettings = {},
            onRetry = {},
        )
    }
}

private fun previewState(): HomeUiState {
    val surahs = listOf(
        Surah(1, "الفاتحة", "Al-Fatihah", "The Opening", RevelationType.MECCAN, 7, 1),
        Surah(18, "الكهف", "Al-Kahf", "The Cave", RevelationType.MECCAN, 110, 293),
        Surah(36, "يس", "Ya-Sin", "Ya Sin", RevelationType.MECCAN, 83, 440),
    )
    return HomeUiState(
        surahs = surahs,
        continueSurah = surahs[1],
        continueAyah = Ayah(
            id = 2175,
            surahNumber = 18,
            numberInSurah = 35,
            textUthmani = "وَدَخَلَ جَنَّتَهُۥ وَهُوَ ظَالِمٌۭ لِّنَفْسِهِۦ",
            juzNumber = 15,
            hizbQuarter = 119,
            pageNumber = 299,
        ),
        selectedReciter = Reciter.ALAFASY,
        dailyPagesRead = 3,
        readingStreakDays = 12,
        isLoading = false,
    )
}
