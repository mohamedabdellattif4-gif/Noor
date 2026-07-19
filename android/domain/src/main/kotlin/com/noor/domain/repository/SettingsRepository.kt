package com.noor.domain.repository

import com.noor.domain.model.Reciter
import com.noor.domain.model.ThemeMode
import com.noor.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<UserSettings>
    suspend fun setThemeMode(value: ThemeMode)
    suspend fun setDynamicColorEnabled(value: Boolean)
    suspend fun setQuranTextScale(value: Float)
    suspend fun setReciter(value: Reciter)
    suspend fun setLastRead(surahNumber: Int, ayahId: Int)

    /**
     * Atomically records the last reading position and today's distinct page progress.
     *
     * The default implementation preserves source compatibility for alternative repositories while
     * the DataStore implementation persists the full reading-progress model.
     */
    suspend fun recordReading(surahNumber: Int, ayahId: Int, pageNumber: Int) {
        require(pageNumber > 0)
        setLastRead(surahNumber, ayahId)
    }
}
