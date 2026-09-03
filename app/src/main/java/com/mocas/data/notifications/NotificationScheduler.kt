package com.mocas.data.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.mocas.MainActivity
import com.mocas.R
import com.mocas.data.local.AppDatabase
import com.mocas.data.widget.DailyScheduleWidgetProvider
import com.mocas.data.widget.ScheduleWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object NotificationScheduler {
    const val ACTION_COMPLETE = "com.mocas.action.COMPLETE_EVENT"
    const val ACTION_SNOOZE = "com.mocas.action.SNOOZE_EVENT"
    const val CHANNEL_CLASSES = "class_reminders"
    const val CHANNEL_ACTIVITIES = "activity_reminders"
    const val CHANNEL_SUMMARY = "daily_summary"
    private const val PREFS = "scheduled_reminders"
    private const val KEY_IDS = "ids"

    fun replaceAll(context: Context, reminders: List<PlannedReminder>) {
        createChannels(context)
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        preferences.getStringSet(KEY_IDS, emptySet()).orEmpty().forEach { id ->
            alarmManager.cancel(pendingIntent(context, id, null))
        }
        reminders.forEach { reminder ->
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminder.triggerAtMillis,
                pendingIntent(context, reminder.id, reminder)
            )
        }
        preferences.edit().putStringSet(KEY_IDS, reminders.mapTo(mutableSetOf()) { it.id }).apply()
    }

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        listOf(
            NotificationChannel(CHANNEL_CLASSES, "Clases", NotificationManager.IMPORTANCE_HIGH),
            NotificationChannel(CHANNEL_ACTIVITIES, "Tareas y exámenes", NotificationManager.IMPORTANCE_HIGH),
            NotificationChannel(CHANNEL_SUMMARY, "Resumen diario", NotificationManager.IMPORTANCE_DEFAULT)
        ).forEach(manager::createNotificationChannel)
    }

    fun scheduleOne(context: Context, reminder: PlannedReminder) {
        createChannels(context)
        context.getSystemService(AlarmManager::class.java).setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            reminder.triggerAtMillis,
            pendingIntent(context, reminder.id, reminder)
        )
        val preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val ids = preferences.getStringSet(KEY_IDS, emptySet()).orEmpty().toMutableSet()
        ids += reminder.id
        preferences.edit().putStringSet(KEY_IDS, ids).apply()
    }

    private fun pendingIntent(
        context: Context,
        id: String,
        reminder: PlannedReminder?
    ): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("id", id)
            reminder?.let {
                putExtra("title", it.title)
                putExtra("message", it.message)
                putExtra("channel", it.channel)
                it.eventId?.let { eventId -> putExtra("eventId", eventId) }
            }
        }
        return PendingIntent.getBroadcast(
            context,
            id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return
        val id = intent.getStringExtra("id") ?: return
        val channel = intent.getStringExtra("channel") ?: NotificationScheduler.CHANNEL_ACTIVITIES
        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(intent.getStringExtra("title") ?: "SnapMySchedule")
            .setContentText(intent.getStringExtra("message").orEmpty())
            .setStyle(NotificationCompat.BigTextStyle().bigText(intent.getStringExtra("message").orEmpty()))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .apply {
                val eventId = intent.getLongExtra("eventId", -1L)
                if (eventId > 0) {
                    addAction(
                        R.drawable.ic_notification,
                        "Completar",
                        notificationAction(context, NotificationScheduler.ACTION_COMPLETE, id, eventId, intent)
                    )
                    addAction(
                        R.drawable.ic_notification,
                        "Posponer 1 h",
                        notificationAction(context, NotificationScheduler.ACTION_SNOOZE, id, eventId, intent)
                    )
                }
            }
            .build()
        NotificationManagerCompat.from(context).notify(id.hashCode(), notification)
    }

    private fun notificationAction(
        context: Context,
        action: String,
        notificationId: String,
        eventId: Long,
        source: Intent
    ): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            this.action = action
            putExtra("notificationId", notificationId)
            putExtra("eventId", eventId)
            putExtra("title", source.getStringExtra("title"))
            putExtra("message", source.getStringExtra("message"))
            putExtra("channel", source.getStringExtra("channel"))
        }
        return PendingIntent.getBroadcast(
            context,
            "$action-$notificationId".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

}

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val eventId = intent.getLongExtra("eventId", -1L)
        val notificationId = intent.getStringExtra("notificationId") ?: return
        if (eventId <= 0) return
        NotificationManagerCompat.from(context).cancel(notificationId.hashCode())
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    NotificationScheduler.ACTION_COMPLETE -> {
                        val now = System.currentTimeMillis()
                        AppDatabase.getDatabase(context).schoolEventDao().setEventCompleted(
                            eventId,
                            true,
                            now,
                            now
                        )
                        ReminderRescheduler.reschedule(context)
                        ScheduleWidgetProvider.requestUpdate(context)
                        DailyScheduleWidgetProvider.requestUpdate(context)
                    }
                    NotificationScheduler.ACTION_SNOOZE -> {
                        NotificationScheduler.scheduleOne(
                            context,
                            PlannedReminder(
                                id = "snooze_${eventId}_${System.currentTimeMillis()}",
                                triggerAtMillis = System.currentTimeMillis() + 60 * 60 * 1000,
                                title = intent.getStringExtra("title") ?: "Actividad pendiente",
                                message = intent.getStringExtra("message").orEmpty(),
                                channel = intent.getStringExtra("channel")
                                    ?: NotificationScheduler.CHANNEL_ACTIVITIES,
                                eventId = eventId
                            )
                        )
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
