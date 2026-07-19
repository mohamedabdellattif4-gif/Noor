package com.noor.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.noor.data.adhkar.BundledAdhkarCatalog
import com.noor.data.settings.noorDataStore
import com.noor.domain.model.AdhkarReminderSettings
import com.noor.domain.model.Dhikr
import com.noor.domain.repository.AdhkarRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

@Singleton
internal class DefaultAdhkarRepository @Inject constructor(
    catalog: BundledAdhkarCatalog,
    @ApplicationContext private val context: Context,
) : AdhkarRepository {
    private val bundledItems: List<Dhikr> = catalog.load()
    private val preferences = context.noorDataStore.data.catch { error ->
        if (error is IOException) emit(emptyPreferences()) else throw error
    }

    override val adhkar: Flow<List<Dhikr>> = preferences.map { values ->
        val favoriteIds = values[Keys.FAVORITES].orEmpty()
        bundledItems.map { item -> item.copy(isFavorite = item.id in favoriteIds) }
    }

    override val reminderSettings: Flow<AdhkarReminderSettings> = preferences.map { values ->
        AdhkarReminderSettings(
            morningEnabled = values[Keys.MORNING_REMINDER] ?: false,
            eveningEnabled = values[Keys.EVENING_REMINDER] ?: false,
        )
    }

    override suspend fun setFavorite(dhikrId: String, favorite: Boolean) {
        require(bundledItems.any { it.id == dhikrId }) { "Unknown dhikr id" }
        context.noorDataStore.edit { values ->
            val updated = values[Keys.FAVORITES].orEmpty().toMutableSet()
            if (favorite) updated += dhikrId else updated -= dhikrId
            values[Keys.FAVORITES] = updated
        }
    }

    override suspend fun setMorningReminderEnabled(enabled: Boolean) {
        context.noorDataStore.edit { it[Keys.MORNING_REMINDER] = enabled }
    }

    override suspend fun setEveningReminderEnabled(enabled: Boolean) {
        context.noorDataStore.edit { it[Keys.EVENING_REMINDER] = enabled }
    }

    private object Keys {
        val FAVORITES = stringSetPreferencesKey("adhkar_favorites")
        val MORNING_REMINDER = booleanPreferencesKey("adhkar_morning_reminder")
        val EVENING_REMINDER = booleanPreferencesKey("adhkar_evening_reminder")
    }
}
