package com.noor.app.ui.audio

import androidx.compose.runtime.Immutable
import com.noor.core.media.QuranPlaybackInfo
import com.noor.core.media.QuranPlaybackState
import com.noor.domain.model.Ayah
import com.noor.domain.model.Reciter
import com.noor.domain.model.Surah

@Immutable
data class AudioUiState(
    val surahs: List<Surah> = emptyList(),
    val selectedSurah: Surah? = null,
    val selectedAyahs: List<Ayah> = emptyList(),
    val reciter: Reciter = Reciter.ALAFASY,
    val playbackState: QuranPlaybackState = QuranPlaybackState.Idle,
    val playbackInfo: QuranPlaybackInfo = QuranPlaybackInfo(),
    val isReciterUpdating: Boolean = false,
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
) {
    val currentAyah: Ayah?
        get() = playbackInfo.currentAyahId?.let { id -> selectedAyahs.firstOrNull { it.id == id } }

    val isSelectedQueueActive: Boolean
        get() = playbackInfo.currentAyahId?.let { id ->
            selectedAyahs.any { it.id == id }
        } ?: false

    val canPlaySelectedSurah: Boolean
        get() = selectedSurah != null && selectedAyahs.isNotEmpty()
}
