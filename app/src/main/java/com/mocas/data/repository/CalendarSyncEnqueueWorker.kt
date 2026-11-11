package com.mocas.data.repository

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/** Periodic work only queues the single serialized sync pipeline. */
class CalendarSyncEnqueueWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        CalendarSyncScheduler.triggerImmediateSync(applicationContext)
        return Result.success()
    }
}
