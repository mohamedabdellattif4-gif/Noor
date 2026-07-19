package com.noor.app.adhkar

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.noor.app.MainActivity
import com.noor.app.R
import com.noor.domain.model.AdhkarCategory
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
internal class AdhkarReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParameters: WorkerParameters,
) : CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        val category = AdhkarCategory.fromKey(inputData.getString(CATEGORY_KEY)) ?: return Result.failure()
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }
        createChannel()
        val title = context.getString(
            if (category == AdhkarCategory.MORNING) {
                R.string.adhkar_morning_reminder_title
            } else {
                R.string.adhkar_evening_reminder_title
            },
        )
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_ADHKAR, true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            category.ordinal,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_noor)
            .setContentTitle(title)
            .setContentText(context.getString(R.string.adhkar_reminder_body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_BASE + category.ordinal, notification)
        return Result.success()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.adhkar_reminder_channel),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.adhkar_reminder_channel_description)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        const val CATEGORY_KEY = "category_key"
        const val EXTRA_OPEN_ADHKAR = "open_adhkar"
        private const val CHANNEL_ID = "adhkar_reminders"
        private const val NOTIFICATION_ID_BASE = 8100
    }
}
