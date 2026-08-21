package com.mocas.data.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mocas.data.local.AppDatabase
import com.mocas.data.preferences.AppSettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ReminderRescheduler {
    suspend fun reschedule(context: Context) {
        val database = AppDatabase.getDatabase(context)
        val reminders = planReminders(
            subjects = database.subjectDao().getAllSubjectsWithSlotsOnce(),
            events = database.schoolEventDao().getAllEventsWithSubjectOnce(),
            exceptions = database.classExceptionDao().getAllOnce(),
            settings = AppSettingsStore(context).load()
        )
        NotificationScheduler.replaceAll(context, reminders)
    }
}

class ReminderBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ReminderRescheduler.reschedule(context.applicationContext)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
