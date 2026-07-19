package com.noor.data.remote

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.noor.domain.model.Ayah
import com.noor.domain.model.TafsirOrigin
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QuranEncTafsirParserTest {
    @Test
    fun validDirectPayloadPreservesTranslationAndFootnotes() {
        val result = QuranEncTafsirParser.parse(
            body = """{"sura":"1","aya":"1","translation":"النص كما هو","footnotes":"حاشية المصدر"}""",
            ayah = sampleAyah(),
            fetchedAtEpochMillis = 42L,
        )

        assertEquals("النص كما هو", result.text)
        assertEquals("حاشية المصدر", result.footnotes)
        assertEquals(42L, result.fetchedAtEpochMillis)
        assertEquals(TafsirOrigin.NETWORK, result.origin)
    }

    @Test
    fun wrappedPayloadAndBlankFootnotesAreAccepted() {
        val result = QuranEncTafsirParser.parse(
            body = """{"result":{"sura":1,"aya":1,"translation":"تفسير","footnotes":""}}""",
            ayah = sampleAyah(),
            fetchedAtEpochMillis = 1L,
        )

        assertEquals("تفسير", result.text)
        assertNull(result.footnotes)
    }

    @Test
    fun mismatchedCoordinatesAreRejected() {
        val failed = runCatching {
            QuranEncTafsirParser.parse(
                body = """{"sura":2,"aya":1,"translation":"تفسير","footnotes":""}""",
                ayah = sampleAyah(),
                fetchedAtEpochMillis = 1L,
            )
        }.exceptionOrNull()

        assertTrue(failed is IOException)
    }

    @Test
    fun missingTranslationIsRejected() {
        val failed = runCatching {
            QuranEncTafsirParser.parse(
                body = """{"sura":1,"aya":1,"translation":""}""",
                ayah = sampleAyah(),
                fetchedAtEpochMillis = 1L,
            )
        }.exceptionOrNull()

        assertTrue(failed is IOException)
    }

    private fun sampleAyah() = Ayah(
        id = 1,
        surahNumber = 1,
        numberInSurah = 1,
        textUthmani = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
        juzNumber = 1,
        hizbQuarter = 1,
        pageNumber = 1,
    )
}
