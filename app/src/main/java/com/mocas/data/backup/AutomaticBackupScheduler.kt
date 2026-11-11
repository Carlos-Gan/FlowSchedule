package com.mocas.data.backup

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import androidx.core.content.edit

object AutomaticBackupScheduler {
    const val DAILY = "daily"
    const val WEEKLY = "weekly"
    const val MONTHLY = "monthly"

    private const val WORK_NAME = "FlowSchedulePeriodicAutomaticBackup"
    private const val PREFS_NAME = "flow_schedule_settings"
    private const val KEY_ENABLED = "automatic_backup_enabled"
    private const val KEY_INTERVAL = "automatic_backup_interval"

    fun isEnabled(context: Context): Boolean = preferences(context).getBoolean(KEY_ENABLED, false)

    fun getInterval(context: Context): String = preferences(context)
        .getString(KEY_INTERVAL, WEEKLY)
        ?.takeIf { it == DAILY || it == WEEKLY || it == MONTHLY } ?: WEEKLY

    fun configure(context: Context, enabled: Boolean, interval: String = getInterval(context)) {
        val safeInterval = interval.takeIf { it in setOf(DAILY, WEEKLY, MONTHLY) } ?: WEEKLY
        this.preferences(context).edit {
            putBoolean(KEY_ENABLED, enabled)
                .putString(KEY_INTERVAL, safeInterval)
        }

        val workManager = WorkManager.getInstance(context)
        if (!enabled) {
            workManager.cancelUniqueWork(WORK_NAME)
            return
        }

        workManager.cancelUniqueWork(WORK_NAME)
        val repeat = when (safeInterval) {
            DAILY -> 1L to TimeUnit.DAYS
            WEEKLY -> 7L to TimeUnit.DAYS
            else -> 30L to TimeUnit.DAYS
        }
        val request = PeriodicWorkRequestBuilder<AutomaticBackupWorker>(repeat.first, repeat.second).build()
        workManager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    private fun preferences(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
