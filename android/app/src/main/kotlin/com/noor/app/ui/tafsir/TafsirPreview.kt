package com.noor.app.ui.tafsir

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.noor.core.designsystem.theme.NoorTheme
import com.noor.domain.model.Ayah
import com.noor.domain.model.Tafsir
import com.noor.domain.model.TafsirOrigin

@Preview(locale = "ar", showBackground = true)
@Composable
private fun TafsirArabicPreview() {
    NoorTheme {
        TafsirScreen(
            uiState = sampleState(),
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {},
            onRetry = {},
            onRefresh = {},
        )
    }
}

@Preview(locale = "ar", uiMode = 0x20, showBackground = true)
@Composable
private fun TafsirDarkPreview() {
    NoorTheme(darkTheme = true) {
        TafsirScreen(
            uiState = sampleState().copy(isRefreshing = true),
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {},
            onRetry = {},
            onRefresh = {},
        )
    }
}

private fun sampleState() = TafsirUiState(
    ayah = Ayah(
        id = 1,
        surahNumber = 1,
        numberInSurah = 1,
        textUthmani = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
        juzNumber = 1,
        hizbQuarter = 1,
        pageNumber = 1,
    ),
    tafsir = Tafsir(
        ayahId = 1,
        text = "أبتدئ قراءة القرآن باسم الله مستعينًا به، والله هو المعبود بحق، واسع الرحمة بخلقه.",
        footnotes = "التفسير المعروض من المصدر دون تعديل.",
        sourceKey = "arabic_moyassar",
        sourceName = "التفسير الميسر — QuranEnc.com",
        fetchedAtEpochMillis = 1L,
        origin = TafsirOrigin.CACHE,
    ),
    isLoading = false,
)
