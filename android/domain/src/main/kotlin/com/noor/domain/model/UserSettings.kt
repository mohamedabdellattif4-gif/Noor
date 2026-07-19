package com.noor.domain.model

data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColorEnabled: Boolean = false,
    val quranTextScale: Float = DEFAULT_TEXT_SCALE,
    val reciter: Reciter = Reciter.ALAFASY,
    val lastReadSurah: Int? = null,
    val lastReadAyah: Int? = null,
    val dailyPagesRead: Int = 0,
    val readingStreakDays: Int = 0,
) {
    init {
        require(quranTextScale in MIN_TEXT_SCALE..MAX_TEXT_SCALE)
        require(lastReadSurah == null || lastReadSurah in 1..114)
        require(lastReadAyah == null || lastReadAyah > 0)
        require(dailyPagesRead >= 0)
        require(readingStreakDays >= 0)
    }

    companion object {
        const val MIN_TEXT_SCALE = 0.8f
        const val MAX_TEXT_SCALE = 1.6f
        const val DEFAULT_TEXT_SCALE = 1.0f
    }
}
