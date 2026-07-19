package com.noor.domain.model

import org.junit.Assert.assertThrows
import org.junit.Test

class QuranModelsTest {
    @Test
    fun rejectsInvalidAyahMetadata() {
        assertThrows(IllegalArgumentException::class.java) {
            Ayah(0, 1, 1, "نص", 1, 1, 1)
        }
    }

    @Test
    fun userSettingsClampsMustBePerformedByRepository() {
        assertThrows(IllegalArgumentException::class.java) {
            UserSettings(quranTextScale = 3f)
        }
    }
}
