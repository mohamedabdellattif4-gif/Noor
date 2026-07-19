package com.noor.core.media

import com.noor.domain.model.Ayah
import com.noor.domain.model.Reciter
import org.junit.Assert.assertEquals
import org.junit.Test

class QuranAudioUrlFactoryTest {
    @Test
    fun buildsZeroPaddedHttpsUrl() {
        val ayah = Ayah(1, 1, 1, "نص", 1, 1, 1)
        assertEquals(
            "https://everyayah.com/data/Alafasy_128kbps/001001.mp3",
            QuranAudioUrlFactory.create(Reciter.ALAFASY, ayah),
        )
    }
}
