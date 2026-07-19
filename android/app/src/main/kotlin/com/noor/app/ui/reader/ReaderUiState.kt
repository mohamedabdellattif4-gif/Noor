package com.noor.app.ui.reader

import androidx.compose.runtime.Immutable
import com.noor.core.media.QuranPlaybackInfo
import com.noor.core.media.QuranPlaybackState
import com.noor.domain.model.Ayah
import com.noor.domain.model.Reciter
import com.noor.domain.model.Surah

@Immutable
data class ReaderUiState(
    val surah: Surah? = null,
    val ayahs: List<Ayah> = emptyList(),
    val bookmarkedAyahIds: Set<Int> = emptySet(),
    val pendingBookmarkAyahIds: Set<Int> = emptySet(),
    val textScale: Float = 1f,
    val reciter: Reciter = Reciter.ALAFASY,
    val targetAyahId: Int? = null,
    val playbackState: QuranPlaybackState = QuranPlaybackState.Idle,
    val playbackInfo: QuranPlaybackInfo = QuranPlaybackInfo(),
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
)
