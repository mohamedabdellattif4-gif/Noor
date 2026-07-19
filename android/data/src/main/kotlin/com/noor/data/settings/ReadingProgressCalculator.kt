package com.noor.data.settings

internal data class ReadingProgressUpdate(
    val dayKey: String,
    val pages: Set<Int>,
    val streakDays: Int,
)

/** Pure reading-progress state transition used inside an atomic DataStore edit. */
internal object ReadingProgressCalculator {
    fun update(
        currentDayKey: String,
        previousDayKey: String,
        storedDayKey: String?,
        storedPages: Set<Int>,
        storedStreakDays: Int,
        pageNumber: Int,
    ): ReadingProgressUpdate {
        require(currentDayKey.isNotBlank())
        require(previousDayKey.isNotBlank())
        require(pageNumber > 0)

        return when (storedDayKey) {
            currentDayKey -> ReadingProgressUpdate(
                dayKey = currentDayKey,
                pages = storedPages.filterTo(mutableSetOf()) { it > 0 } + pageNumber,
                streakDays = storedStreakDays.coerceAtLeast(1),
            )

            previousDayKey -> ReadingProgressUpdate(
                dayKey = currentDayKey,
                pages = setOf(pageNumber),
                streakDays = if (storedStreakDays == Int.MAX_VALUE) {
                    Int.MAX_VALUE
                } else {
                    storedStreakDays.coerceAtLeast(0) + 1
                },
            )

            else -> ReadingProgressUpdate(
                dayKey = currentDayKey,
                pages = setOf(pageNumber),
                streakDays = 1,
            )
        }
    }
}
