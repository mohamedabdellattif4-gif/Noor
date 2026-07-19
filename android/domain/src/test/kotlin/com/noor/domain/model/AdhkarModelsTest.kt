package com.noor.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AdhkarModelsTest {
    @Test
    fun categoryKeysRoundTripAndUnknownValuesFailSafely() {
        AdhkarCategory.entries.forEach { category ->
            assertEquals(category, AdhkarCategory.fromKey(category.key))
        }
        assertNull(AdhkarCategory.fromKey("unknown"))
        assertNull(AdhkarCategory.fromKey(null))
    }

    @Test(expected = IllegalArgumentException::class)
    fun dhikrRejectsNonPositiveRepeatCount() {
        Dhikr(
            id = "sample",
            category = AdhkarCategory.GENERAL,
            title = "ذكر",
            textArabic = "سبحان الله",
            reference = "مرجع",
            repeatCount = 0,
        )
    }
}
