package com.noor.app.ui.reader

import com.noor.core.media.QuranPlaybackState

internal enum class ReaderListenAction {
    PLAY_CURRENT,
    TOGGLE_PAUSE,
}

/**
 * Resolves the reader's single listen control without resuming a different hidden ayah.
 *
 * Buffering and paused states only retain the existing queue when that queue currently points to
 * the visible ayah. This avoids the previous behavior where pressing "listen" on another verse
 * resumed unrelated audio.
 */
internal fun resolveReaderListenAction(
    playbackState: QuranPlaybackState,
    currentMediaId: String?,
    currentAyahId: Int,
): ReaderListenAction {
    val visibleMediaId = "ayah:$currentAyahId"
    return when (playbackState) {
        QuranPlaybackState.Idle,
        QuranPlaybackState.Error,
        -> ReaderListenAction.PLAY_CURRENT

        QuranPlaybackState.Buffering,
        QuranPlaybackState.Paused,
        -> if (currentMediaId == visibleMediaId) {
            ReaderListenAction.TOGGLE_PAUSE
        } else {
            ReaderListenAction.PLAY_CURRENT
        }

        is QuranPlaybackState.Playing -> {
            if (playbackState.mediaId == visibleMediaId) {
                ReaderListenAction.TOGGLE_PAUSE
            } else {
                ReaderListenAction.PLAY_CURRENT
            }
        }
    }
}
