package com.mocas.data.repository

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.mocas.R
import com.mocas.data.local.AppDatabase

class CalendarSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object {
        const val OUTPUT_EVENT_COUNT = "calendar_sync_event_count"
        const val OUTPUT_ERROR = "calendar_sync_error"
        private const val MAX_RETRY_ATTEMPTS = 5
    }

    override suspend fun doWork(): Result {
        return try {
            syncCalendar()
        } catch (e: Exception) {
            retryOrFail(e)
        }
    }

    private suspend fun syncCalendar(): Result {
        if (!CalendarSyncManager.hasCalendarPermissions(applicationContext)) {
            return retryOrFail(
                SecurityException(applicationContext.getString(R.string.calendar_sync_permission_error))
            )
        }
        val database = AppDatabase.getDatabase(applicationContext)
        val subjectDao = database.subjectDao()
        val eventDao = database.schoolEventDao()
        val exceptionDao = database.classExceptionDao()

        val subjects = subjectDao.getAllSubjectsWithSlotsOnce()
        val events = eventDao.getAllEventsWithSubjectOnce().map { it.event }
        val exceptions = exceptionDao.getAllOnce()

        val syncResult = CalendarSyncManager.syncLocalScheduleToCalendar(
            applicationContext,
            subjects,
            events,
            exceptions
        )

        return syncResult.fold(
            onSuccess = { count -> Result.success(workDataOf(OUTPUT_EVENT_COUNT to count)) },
            onFailure = ::retryOrFail
        )
    }

    private fun retryOrFail(error: Throwable): Result {
        val output = Data.Builder()
            .putString(OUTPUT_ERROR, error.localizedMessage ?: error.javaClass.simpleName)
            .build()
        return if (
            error is SecurityException || error is IllegalArgumentException || error is IllegalStateException ||
            runAttemptCount >= MAX_RETRY_ATTEMPTS
        ) {
            Result.failure(output)
        } else {
            Result.retry()
        }
    }

}
