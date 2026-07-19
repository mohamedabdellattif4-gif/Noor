package com.noor.app.adhkar

import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class AdhkarReminderDelayCalculatorTest {
    private val utc = TimeZone.getTimeZone("UTC")

    @Test
    fun futureHourOnSameDayUsesSameDay() {
        val now = instant(hour = 7, minute = 30)
        val delay = AdhkarReminderDelayCalculator.delayUntilHour(8, now, utc)
        assertEquals(TimeUnit.MINUTES.toMillis(30), delay)
    }

    @Test
    fun elapsedHourRollsToTheNextDay() {
        val now = instant(hour = 20, minute = 0)
        val delay = AdhkarReminderDelayCalculator.delayUntilHour(18, now, utc)
        assertEquals(TimeUnit.HOURS.toMillis(22), delay)
    }

    private fun instant(hour: Int, minute: Int): Long = Calendar.getInstance(utc).apply {
        clear()
        set(2026, Calendar.JULY, 19, hour, minute, 0)
    }.timeInMillis
}
