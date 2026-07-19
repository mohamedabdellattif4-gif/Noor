package com.noor.data.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class ReadingProgressCalculatorTest {
    @Test
    fun sameDayAddsOnlyDistinctPagesAndPreservesStreak() {
        val update = ReadingProgressCalculator.update(
            currentDayKey = "2026-07-18",
            previousDayKey = "2026-07-17",
            storedDayKey = "2026-07-18",
            storedPages = setOf(299, 300),
            storedStreakDays = 12,
            pageNumber = 300,
        )

        assertEquals(setOf(299, 300), update.pages)
        assertEquals(12, update.streakDays)
    }

    @Test
    fun consecutiveDayStartsNewDailySetAndIncrementsStreak() {
        val update = ReadingProgressCalculator.update(
            currentDayKey = "2026-07-18",
            previousDayKey = "2026-07-17",
            storedDayKey = "2026-07-17",
            storedPages = setOf(299),
            storedStreakDays = 12,
            pageNumber = 300,
        )

        assertEquals(setOf(300), update.pages)
        assertEquals(13, update.streakDays)
    }

    @Test
    fun brokenStreakRestartsAtOne() {
        val update = ReadingProgressCalculator.update(
            currentDayKey = "2026-07-18",
            previousDayKey = "2026-07-17",
            storedDayKey = "2026-07-15",
            storedPages = setOf(299),
            storedStreakDays = 12,
            pageNumber = 300,
        )

        assertEquals(setOf(300), update.pages)
        assertEquals(1, update.streakDays)
    }
}
