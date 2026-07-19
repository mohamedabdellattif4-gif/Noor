package com.noor.app.ui.audio

import android.content.res.Configuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.noor.core.designsystem.theme.NoorTheme
import com.noor.core.media.QuranPlaybackInfo
import com.noor.core.media.QuranPlaybackState
import com.noor.domain.model.Ayah
import com.noor.domain.model.Reciter
import com.noor.domain.model.RevelationType
import com.noor.domain.model.Surah

@Preview(
    name = "Premium Audio Arabic",
    locale = "ar",
    showBackground = true,
    widthDp = 412,
    heightDp = 915,
)
@Composable
private fun AudioArabicPreview() {
    NoorTheme(useDynamicColor = false) {
        AudioPreviewContent()
    }
}

@Preview(
    name = "Premium Audio Dark",
    locale = "en",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    widthDp = 412,
    heightDp = 915,
)
@Composable
private fun AudioDarkPreview() {
    NoorTheme(darkTheme = true, useDynamicColor = false) {
        AudioPreviewContent()
    }
}

@Composable
private fun AudioPreviewContent() {
    val rahman = sampleSurah(number = 55, name = "الرحمن", ayahCount = 78, startPage = 531)
    val mulk = sampleSurah(number = 67, name = "الملك", ayahCount = 30, startPage = 562)
    AudioScreen(
        uiState = AudioUiState(
            surahs = listOf(rahman, mulk),
            selectedSurah = rahman,
            selectedAyahs = listOf(
                sampleAyah(id = 4901, number = 1),
                sampleAyah(id = 4902, number = 2),
                sampleAyah(id = 4903, number = 3),
            ),
            reciter = Reciter.ALAFASY,
            playbackState = QuranPlaybackState.Playing(mediaId = "ayah:4902"),
            playbackInfo = QuranPlaybackInfo(
                mediaId = "ayah:4902",
                title = "سورة الرحمن • الآية 2",
                artist = "مشاري راشد العفاسي",
                currentIndex = 1,
                mediaCount = 78,
                positionMs = 36_000L,
                durationMs = 92_000L,
                canSkipPrevious = true,
                canSkipNext = true,
            ),
            isLoading = false,
        ),
        snackbarHostState = remember { SnackbarHostState() },
        onBack = {},
        onRetry = {},
        onSelectSurah = {},
        onSelectReciter = {},
        onPlayPause = {},
        onPrevious = {},
        onNext = {},
        onStop = {},
        onSeek = {},
        onOpenReader = {},
    )
}

private fun sampleSurah(number: Int, name: String, ayahCount: Int, startPage: Int) = Surah(
    number = number,
    nameArabic = name,
    nameTransliterated = null,
    nameTranslated = null,
    revelationType = RevelationType.MECCAN,
    ayahCount = ayahCount,
    startPage = startPage,
)

private fun sampleAyah(id: Int, number: Int) = Ayah(
    id = id,
    surahNumber = 55,
    numberInSurah = number,
    textUthmani = when (number) {
        1 -> "ٱلرَّحْمَٰنُ"
        2 -> "عَلَّمَ ٱلْقُرْءَانَ"
        else -> "خَلَقَ ٱلْإِنسَٰنَ"
    },
    juzNumber = 27,
    hizbQuarter = 213,
    pageNumber = 531,
)
