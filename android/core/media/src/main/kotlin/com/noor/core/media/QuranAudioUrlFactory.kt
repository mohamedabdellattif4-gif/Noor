package com.noor.core.media

import com.noor.domain.model.Ayah
import com.noor.domain.model.Reciter
import java.util.Locale

/** Builds HTTPS verse-audio URLs. The provider can be replaced without touching presentation code. */
object QuranAudioUrlFactory {
    private const val BASE_URL = "https://everyayah.com/data"

    fun create(reciter: Reciter, ayah: Ayah): String {
        require(ayah.surahNumber in 1..114 && ayah.numberInSurah > 0)
        val fileName = String.format(
            Locale.ROOT,
            "%03d%03d.mp3",
            ayah.surahNumber,
            ayah.numberInSurah,
        )
        return "$BASE_URL/${reciter.directory}/$fileName"
    }

    private val Reciter.directory: String
        get() = when (this) {
            Reciter.ALAFASY -> "Alafasy_128kbps"
            Reciter.HUSARY -> "Husary_128kbps"
            Reciter.MINSHAWI -> "Minshawy_Murattal_128kbps"
        }
}
