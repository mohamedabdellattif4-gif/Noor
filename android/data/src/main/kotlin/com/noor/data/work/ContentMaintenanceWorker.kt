package com.noor.data.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.noor.data.local.dao.TafsirDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException

@HiltWorker
class ContentMaintenanceWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val tafsirDao: TafsirDao,
) : CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result = try {
        val cutoff = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(CACHE_RETENTION_DAYS)
        tafsirDao.deleteOlderThan(cutoff)
        Result.success()
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
    }

    companion object {
        const val UNIQUE_NAME = "noor_content_maintenance"
        const val CACHE_RETENTION_DAYS = 90L
        const val MAX_RETRIES = 3
    }
}
