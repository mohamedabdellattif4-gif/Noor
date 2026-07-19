package com.noor.core.media

import com.noor.domain.model.Ayah
import com.noor.domain.model.Reciter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Testable playback boundary consumed by presentation without exposing Media3 implementation types. */
interface QuranAudioPlayer {
    val playbackState: StateFlow<QuranPlaybackState>

    /**
     * Rich queue and progress information for playback surfaces.
     *
     * A default immutable state preserves source compatibility for lightweight test doubles and
     * alternative implementations that only need the original playback-state contract.
     */
    val playbackInfo: StateFlow<QuranPlaybackInfo>
        get() = EmptyQuranPlaybackInfo

    fun playAyah(ayah: Ayah, reciter: Reciter, surahName: String)

    fun playSurah(
        ayahs: List<Ayah>,
        reciter: Reciter,
        surahName: String,
        startAyahId: Int? = null,
    )

    fun togglePause()

    fun skipToPrevious() = Unit

    fun skipToNext() = Unit

    fun seekTo(positionMs: Long) = Unit

    fun stop()
}

private val EmptyQuranPlaybackInfo: StateFlow<QuranPlaybackInfo> =
    MutableStateFlow(QuranPlaybackInfo())
