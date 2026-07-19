package com.noor.data.settings

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.noor.domain.model.Reciter
import com.noor.domain.model.ThemeMode
import com.noor.domain.model.UserSettings
import com.noor.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map


internal class PreferencesSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : SettingsRepository {
    override val settings: Flow<UserSettings> = context.noorDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences ->
            val dayKeys = readingDayKeys()
            val storedReadingDay = preferences[Keys.READING_DAY]
            val pagesToday = if (storedReadingDay == dayKeys.current) {
                preferences[Keys.READING_PAGES]
                    .orEmpty()
                    .mapNotNullTo(mutableSetOf(), String::toIntOrNull)
                    .count { it > 0 }
            } else {
                0
            }
            UserSettings(
                themeMode = enumOrDefault(preferences[Keys.THEME_MODE], ThemeMode.SYSTEM),
                dynamicColorEnabled = preferences[Keys.DYNAMIC_COLOR] ?: false,
                quranTextScale = (preferences[Keys.TEXT_SCALE] ?: UserSettings.DEFAULT_TEXT_SCALE)
                    .coerceIn(UserSettings.MIN_TEXT_SCALE, UserSettings.MAX_TEXT_SCALE),
                reciter = enumOrDefault(preferences[Keys.RECITER], Reciter.ALAFASY),
                lastReadSurah = preferences[Keys.LAST_SURAH]?.takeIf { it in 1..114 },
                lastReadAyah = preferences[Keys.LAST_AYAH]?.takeIf { it > 0 },
                dailyPagesRead = pagesToday,
                readingStreakDays = if (
                    storedReadingDay == dayKeys.current || storedReadingDay == dayKeys.previous
                ) {
                    (preferences[Keys.READING_STREAK] ?: 0).coerceAtLeast(0)
                } else {
                    0
                },
            )
        }

    override suspend fun setThemeMode(value: ThemeMode) = edit { it[Keys.THEME_MODE] = value.name }

    override suspend fun setDynamicColorEnabled(value: Boolean) = edit {
        it[Keys.DYNAMIC_COLOR] = value
    }

    override suspend fun setQuranTextScale(value: Float) = edit {
        it[Keys.TEXT_SCALE] = value.coerceIn(UserSettings.MIN_TEXT_SCALE, UserSettings.MAX_TEXT_SCALE)
    }

    override suspend fun setReciter(value: Reciter) = edit { it[Keys.RECITER] = value.name }

    override suspend fun setLastRead(surahNumber: Int, ayahId: Int) {
        require(surahNumber in 1..114 && ayahId > 0)
        edit {
            it[Keys.LAST_SURAH] = surahNumber
            it[Keys.LAST_AYAH] = ayahId
        }
    }

    override suspend fun recordReading(surahNumber: Int, ayahId: Int, pageNumber: Int) {
        require(surahNumber in 1..114 && ayahId > 0 && pageNumber > 0)
        val dayKeys = readingDayKeys()
        edit { preferences ->
            val storedPages = preferences[Keys.READING_PAGES]
                .orEmpty()
                .mapNotNullTo(mutableSetOf(), String::toIntOrNull)
            val update = ReadingProgressCalculator.update(
                currentDayKey = dayKeys.current,
                previousDayKey = dayKeys.previous,
                storedDayKey = preferences[Keys.READING_DAY],
                storedPages = storedPages,
                storedStreakDays = preferences[Keys.READING_STREAK] ?: 0,
                pageNumber = pageNumber,
            )
            preferences[Keys.LAST_SURAH] = surahNumber
            preferences[Keys.LAST_AYAH] = ayahId
            preferences[Keys.READING_DAY] = update.dayKey
            preferences[Keys.READING_PAGES] = update.pages.mapTo(mutableSetOf()) { it.toString() }
            preferences[Keys.READING_STREAK] = update.streakDays
        }
    }

    private suspend inline fun edit(crossinline block: (MutablePreferences) -> Unit) {
        context.noorDataStore.edit { block(it) }
    }

    private inline fun <reified T : Enum<T>> enumOrDefault(raw: String?, default: T): T =
        raw?.let { value -> enumValues<T>().firstOrNull { it.name == value } } ?: default

    private data class ReadingDayKeys(val current: String, val previous: String)

    private fun readingDayKeys(
        nowMillis: Long = System.currentTimeMillis(),
        timeZone: TimeZone = TimeZone.getDefault(),
    ): ReadingDayKeys {
        val calendar = Calendar.getInstance(timeZone).apply { timeInMillis = nowMillis }
        val formatter = SimpleDateFormat(DAY_KEY_PATTERN, Locale.ROOT).apply {
            this.timeZone = timeZone
        }
        val current = formatter.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return ReadingDayKeys(current = current, previous = formatter.format(calendar.time))
    }

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val TEXT_SCALE = floatPreferencesKey("quran_text_scale")
        val RECITER = stringPreferencesKey("reciter")
        val LAST_SURAH = intPreferencesKey("last_read_surah")
        val LAST_AYAH = intPreferencesKey("last_read_ayah")
        val READING_DAY = stringPreferencesKey("reading_day")
        val READING_PAGES = stringSetPreferencesKey("reading_pages")
        val READING_STREAK = intPreferencesKey("reading_streak")
    }

    private companion object {
        const val DAY_KEY_PATTERN = "yyyy-MM-dd"
    }
}
