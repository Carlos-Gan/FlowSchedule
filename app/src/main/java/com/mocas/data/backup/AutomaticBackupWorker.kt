package com.mocas.data.backup

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mocas.data.local.AppDatabase
import com.mocas.data.repository.ScheduleRepository

class AutomaticBackupWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = try {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ScheduleRepository(applicationContext, database)
        AutomaticBackupManager(applicationContext).create(
            repository.exportScheduleBackup(),
            REASON_SCHEDULED
        )
        Result.success()
    } catch (_: Exception) {
        Result.retry()
    }

    private companion object {
        const val REASON_SCHEDULED = "scheduled"
    }
}
