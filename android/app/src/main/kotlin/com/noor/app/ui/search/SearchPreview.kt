package com.noor.app.ui.search

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.noor.core.designsystem.theme.NoorTheme
import com.noor.domain.model.Ayah
import com.noor.domain.model.AyahSearchResult

@Preview(
    name = "Premium Search Arabic",
    locale = "ar",
    showBackground = true,
    widthDp = 412,
    heightDp = 892,
)
@Composable
private fun PremiumSearchArabicPreview() {
    SearchPreviewContent(darkTheme = false)
}

@Preview(
    name = "Premium Search Dark",
    locale = "en",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    widthDp = 412,
    heightDp = 892,
)
@Composable
private fun PremiumSearchDarkPreview() {
    SearchPreviewContent(darkTheme = true)
}

@Composable
private fun SearchPreviewContent(darkTheme: Boolean) {
    NoorTheme(darkTheme = darkTheme) {
        SearchScreen(
            uiState = SearchUiState(
                query = "الرحمن",
                results = listOf(
                    previewResult(
                        id = 4901,
                        number = 1,
                        text = "ٱلرَّحْمَٰنُ",
                    ),
                    previewResult(
                        id = 4902,
                        number = 2,
                        text = "عَلَّمَ ٱلْقُرْءَانَ",
                    ),
                ),
                hasSearched = true,
            ),
            onQueryChanged = {},
            onClear = {},
            onRetry = {},
            onBack = {},
            onOpenResult = {},
        )
    }
}

private fun previewResult(
    id: Int,
    number: Int,
    text: String,
): AyahSearchResult = AyahSearchResult(
    ayah = Ayah(
        id = id,
        surahNumber = 55,
        numberInSurah = number,
        textUthmani = text,
        juzNumber = 27,
        hizbQuarter = 213,
        pageNumber = 531,
    ),
    surahNameArabic = "الرحمن",
)
