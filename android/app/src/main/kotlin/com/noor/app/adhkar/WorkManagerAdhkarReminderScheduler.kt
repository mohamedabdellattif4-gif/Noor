package com.noor.app.adhkar

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.noor.domain.model.AdhkarCategory
import com.noor.domain.model.AdhkarReminderSettings
import com.noor.domain.repository.AdhkarReminderScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class WorkManagerAdhkarReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : AdhkarReminderScheduler {
    override fun synchronize(settings: AdhkarReminderSettings) {
        scheduleOrCancel(
            uniqueName = MORNING_WORK,
            category = AdhkarCategory.MORNING,
            enabled = settings.morningEnabled,
            hour = settings.morningHour,
        )
        scheduleOrCancel(
            uniqueName = EVENING_WORK,
            category = AdhkarCategory.EVENING,
            enabled = settings.eveningEnabled,
            hour = settings.eveningHour,
        )
    }

    private fun scheduleOrCancel(
        uniqueName: String,
        category: AdhkarCategory,
        enabled: Boolean,
        hour: Int,
    ) {
        val workManager = WorkManager.getInstance(context)
        if (!enabled) {
            workManager.cancelUniqueWork(uniqueName)
            return
        }
        val request = PeriodicWorkRequestBuilder<AdhkarReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(AdhkarReminderDelayCalculator.delayUntilHour(hour), TimeUnit.MILLISECONDS)
            .setInputData(
                Data.Builder()
                    .putString(AdhkarReminderWorker.CATEGORY_KEY, category.key)
                    .build(),
            )
            .build()
        workManager.enqueueUniquePeriodicWork(
            uniqueName,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    private companion object {
        const val MORNING_WORK = "adhkar_morning_reminder"
        const val EVENING_WORK = "adhkar_evening_reminder"
    }
}


internal object AdhkarReminderDelayCalculator {
    fun delayUntilHour(
        hour: Int,
        nowMillis: Long = System.currentTimeMillis(),
        timeZone: TimeZone = TimeZone.getDefault(),
    ): Long {
        require(hour in 0..23)
        val now = Calendar.getInstance(timeZone).apply { timeInMillis = nowMillis }
        val target = Calendar.getInstance(timeZone).apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= nowMillis) add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }
}
