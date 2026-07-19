package com.noor.domain.repository

import com.noor.domain.model.AdhkarReminderSettings

interface AdhkarReminderScheduler {
    fun synchronize(settings: AdhkarReminderSettings)
}
