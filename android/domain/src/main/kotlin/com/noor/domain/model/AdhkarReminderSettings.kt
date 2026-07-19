package com.noor.domain.model

data class AdhkarReminderSettings(
    val morningEnabled: Boolean = false,
    val eveningEnabled: Boolean = false,
    val morningHour: Int = DEFAULT_MORNING_HOUR,
    val eveningHour: Int = DEFAULT_EVENING_HOUR,
) {
    init {
        require(morningHour in 0..23)
        require(eveningHour in 0..23)
    }

    companion object {
        const val DEFAULT_MORNING_HOUR: Int = 8
        const val DEFAULT_EVENING_HOUR: Int = 18
    }
}
