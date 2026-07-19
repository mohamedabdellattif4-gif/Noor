package com.noor.app.ui.home

import androidx.compose.runtime.Immutable
import com.noor.domain.model.Ayah
import com.noor.domain.model.Reciter
import com.noor.domain.model.Surah

@Immutable
data class HomeUiState(
    val surahs: List<Surah> = emptyList(),
    val continueSurah: Surah? = null,
    val continueAyah: Ayah? = null,
    val selectedReciter: Reciter = Reciter.ALAFASY,
    val dailyPagesRead: Int = 0,
    val readingStreakDays: Int = 0,
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
) {
    val primarySurah: Surah?
        get() = continueSurah ?: surahs.firstOrNull()

    val primaryAyahId: Int?
        get() = continueAyah?.id

    val dailyGoalPages: Int
        get() = DAILY_GOAL_PAGES

    companion object {
        const val DAILY_GOAL_PAGES: Int = 8
    }
}
