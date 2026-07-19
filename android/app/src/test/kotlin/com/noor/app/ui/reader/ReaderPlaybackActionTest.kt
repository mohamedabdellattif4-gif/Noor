package com.noor.app.ui.reader

import com.noor.core.media.QuranPlaybackState
import org.junit.Assert.assertEquals
import org.junit.Test

class ReaderPlaybackActionTest {
    @Test
    fun idleAndErrorPlayTheVisibleAyah() {
        assertEquals(
            ReaderListenAction.PLAY_CURRENT,
            resolveReaderListenAction(
                playbackState = QuranPlaybackState.Idle,
                currentMediaId = null,
                currentAyahId = 42,
            ),
        )
        assertEquals(
            ReaderListenAction.PLAY_CURRENT,
            resolveReaderListenAction(
                playbackState = QuranPlaybackState.Error,
                currentMediaId = "ayah:42",
                currentAyahId = 42,
            ),
        )
    }

    @Test
    fun samePlayingAyahTogglesPauseButDifferentAyahStartsVisibleAyah() {
        assertEquals(
            ReaderListenAction.TOGGLE_PAUSE,
            resolveReaderListenAction(
                playbackState = QuranPlaybackState.Playing(mediaId = "ayah:42"),
                currentMediaId = "ayah:42",
                currentAyahId = 42,
            ),
        )
        assertEquals(
            ReaderListenAction.PLAY_CURRENT,
            resolveReaderListenAction(
                playbackState = QuranPlaybackState.Playing(mediaId = "ayah:7"),
                currentMediaId = "ayah:7",
                currentAyahId = 42,
            ),
        )
    }

    @Test
    fun bufferingAndPausedOnlyResumeTheVisibleAyah() {
        assertEquals(
            ReaderListenAction.TOGGLE_PAUSE,
            resolveReaderListenAction(
                playbackState = QuranPlaybackState.Buffering,
                currentMediaId = "ayah:42",
                currentAyahId = 42,
            ),
        )
        assertEquals(
            ReaderListenAction.PLAY_CURRENT,
            resolveReaderListenAction(
                playbackState = QuranPlaybackState.Buffering,
                currentMediaId = "ayah:7",
                currentAyahId = 42,
            ),
        )
        assertEquals(
            ReaderListenAction.TOGGLE_PAUSE,
            resolveReaderListenAction(
                playbackState = QuranPlaybackState.Paused,
                currentMediaId = "ayah:42",
                currentAyahId = 42,
            ),
        )
        assertEquals(
            ReaderListenAction.PLAY_CURRENT,
            resolveReaderListenAction(
                playbackState = QuranPlaybackState.Paused,
                currentMediaId = "ayah:7",
                currentAyahId = 42,
            ),
        )
    }

    @Test
    fun basmalaPresentationMatchesVerifiedCorpusRules() {
        assertEquals(false, shouldShowReaderBasmala(1))
        assertEquals(false, shouldShowReaderBasmala(9))
        assertEquals(true, shouldShowReaderBasmala(55))
    }
}
