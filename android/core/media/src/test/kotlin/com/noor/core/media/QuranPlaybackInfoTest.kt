package com.noor.core.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QuranPlaybackInfoTest {
    @Test
    fun currentAyahIdAcceptsOnlyPositiveNoorMediaIds() {
        assertEquals(42, QuranPlaybackInfo(mediaId = "ayah:42").currentAyahId)
        assertNull(QuranPlaybackInfo(mediaId = "audio:42").currentAyahId)
        assertNull(QuranPlaybackInfo(mediaId = "ayah:0").currentAyahId)
        assertNull(QuranPlaybackInfo(mediaId = "ayah:not-a-number").currentAyahId)
    }

    @Test
    fun progressFractionIsBoundedAndUnavailableWithoutDuration() {
        assertNull(QuranPlaybackInfo(positionMs = 10L, durationMs = 0L).progressFraction)
        assertEquals(0.5f, QuranPlaybackInfo(positionMs = 500L, durationMs = 1_000L).progressFraction)
        assertEquals(1f, QuranPlaybackInfo(positionMs = 2_000L, durationMs = 1_000L).progressFraction)
    }
}
