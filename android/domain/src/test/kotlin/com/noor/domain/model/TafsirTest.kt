package com.noor.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class TafsirTest {
    @Test
    fun validTafsirPreservesOptionalFootnotesAndOrigin() {
        val tafsir = Tafsir(
            ayahId = 1,
            text = "تفسير",
            footnotes = "هامش",
            sourceKey = "arabic_moyassar",
            sourceName = "QuranEnc.com",
            fetchedAtEpochMillis = 1L,
            origin = TafsirOrigin.NETWORK,
        )

        assertEquals("هامش", tafsir.footnotes)
        assertEquals(TafsirOrigin.NETWORK, tafsir.origin)
    }

    @Test
    fun invalidIdentityOrBlankContentIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            Tafsir(0, "تفسير", null, "key", "source", 1L, TafsirOrigin.CACHE)
        }
        assertThrows(IllegalArgumentException::class.java) {
            Tafsir(1, " ", null, "key", "source", 1L, TafsirOrigin.CACHE)
        }
        assertThrows(IllegalArgumentException::class.java) {
            Tafsir(1, "تفسير", null, " ", "source", 1L, TafsirOrigin.CACHE)
        }
    }
}
