package com.noor.data.settings

import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore

internal val Context.noorDataStore by preferencesDataStore(
    name = "noor_settings",
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
)
