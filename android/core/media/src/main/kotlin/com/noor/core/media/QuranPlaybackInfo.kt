package com.noor.core.media

/**
 * Framework-neutral playback details exposed to presentation code.
 *
 * Position values are sanitized by the Media3 adapter. A zero duration means that the remote
 * stream has not published a seekable duration yet, so callers must render an indeterminate
 * progress state instead of dividing by zero.
 */
data class QuranPlaybackInfo(
    val mediaId: String? = null,
    val title: String? = null,
    val artist: String? = null,
    val currentIndex: Int = 0,
    val mediaCount: Int = 0,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val canSkipPrevious: Boolean = false,
    val canSkipNext: Boolean = false,
) {
    init {
        require(currentIndex >= 0) { "Current media index must not be negative." }
        require(mediaCount >= 0) { "Media count must not be negative." }
        require(positionMs >= 0L) { "Playback position must not be negative." }
        require(durationMs >= 0L) { "Playback duration must not be negative." }
    }

    val currentAyahId: Int?
        get() = mediaId
            ?.takeIf { it.startsWith(AYAH_MEDIA_ID_PREFIX) }
            ?.removePrefix(AYAH_MEDIA_ID_PREFIX)
            ?.toIntOrNull()
            ?.takeIf { it > 0 }

    val progressFraction: Float?
        get() = durationMs
            .takeIf { it > 0L }
            ?.let { duration -> (positionMs.toDouble() / duration).coerceIn(0.0, 1.0).toFloat() }

    companion object {
        const val AYAH_MEDIA_ID_PREFIX: String = "ayah:"
    }
}
