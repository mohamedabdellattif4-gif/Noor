package com.noor.domain.repository

import com.noor.domain.model.AdhkarReminderSettings
import com.noor.domain.model.Dhikr
import kotlinx.coroutines.flow.Flow

interface AdhkarRepository {
    val adhkar: Flow<List<Dhikr>>
    val reminderSettings: Flow<AdhkarReminderSettings>

    suspend fun setFavorite(dhikrId: String, favorite: Boolean)
    suspend fun setMorningReminderEnabled(enabled: Boolean)
    suspend fun setEveningReminderEnabled(enabled: Boolean)
}
