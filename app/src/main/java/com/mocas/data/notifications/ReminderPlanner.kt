package com.mocas.data.notifications

import android.content.Context
import com.mocas.R
import com.mocas.data.local.ClassExceptionEntity
import com.mocas.data.local.ClassExceptionType
import com.mocas.data.local.SchoolEventType
import com.mocas.data.local.SchoolEventWithSubject
import com.mocas.data.local.SubjectWithSlots
import com.mocas.ui.model.AppSettings
import com.mocas.ui.util.forDate
import com.mocas.ui.util.isActiveOn
import com.mocas.util.DateTimeUtils
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

private const val REMINDER_HORIZON_DAYS = 30L
private val DAILY_SUMMARY_TIME: LocalTime = LocalTime.of(20, 0)

data class PlannedReminder(
    val id: String,
    val triggerAtMillis: Long,
    val title: String,
    val message: String,
    val channel: String,
    val eventId: Long? = null
)

private fun LocalDate.isWithinHorizon(horizon: LocalDate): Boolean = !isAfter(horizon)

internal fun planReminders(
    context: Context,
    subjects: List<SubjectWithSlots>,
    events: List<SchoolEventWithSubject>,
    exceptions: List<ClassExceptionEntity>,
    settings: AppSettings,
    now: LocalDateTime = LocalDateTime.now(),
    zoneId: ZoneId = ZoneId.systemDefault()
): List<PlannedReminder> {
    if (!settings.notificationsEnabled) return emptyList()
    val reminders = mutableListOf<PlannedReminder>()
    val horizon = now.toLocalDate().plusDays(REMINDER_HORIZON_DAYS)

    var date = now.toLocalDate()
    while (!date.isAfter(horizon)) {
        if (settings.classNotificationsEnabled) subjects.filter { it.isActiveOn(date) }
            .forEach { item ->
                item.slots.filter { it.dayOfWeek == date.dayOfWeek.value }.forEach { baseSlot ->
                    val slot = baseSlot.forDate(date, exceptions) ?: return@forEach
                    val start = DateTimeUtils.parseTime(slot.startTime) ?: return@forEach
                    val trigger = LocalDateTime.of(date, start)
                        .minusMinutes(settings.defaultReminderMinutes.toLong())
                    if (trigger.isAfter(now)) {
                        val changed = exceptions.any {
                            it.slotId == baseSlot.id && it.date == date.toString() &&
                                    it.type == ClassExceptionType.MODIFIED
                        }
                        val room = slot.room.ifBlank { item.subject.defaultRoom }

                        val title = if (changed) {
                            context.getString(R.string.notif_class_change_title, item.subject.name)
                        } else {
                            context.getString(R.string.notif_next_class_title, item.subject.name)
                        }

                        val roomLabel =
                            if (room.isNotBlank()) " · ${context.getString(R.string.salon)} $room" else ""
                        val message = "${slot.startTime}-${slot.endTime}$roomLabel"

                        reminders += PlannedReminder(
                            id = "class_${baseSlot.id}_$date",
                            triggerAtMillis = trigger.atZone(zoneId).toInstant().toEpochMilli(),
                            title = title,
                            message = message,
                            channel = NotificationScheduler.CHANNEL_CLASSES
                        )
                    }
                }
            }

        val tomorrow = date.plusDays(1)
        val summaryTime = LocalDateTime.of(date, DAILY_SUMMARY_TIME)
        if (settings.tomorrowSummaryEnabled && summaryTime.isAfter(now)) {
            val tomorrowClasses = subjects.filter { it.isActiveOn(tomorrow) }.sumOf { item ->
                item.slots.count { slot ->
                    slot.dayOfWeek == tomorrow.dayOfWeek.value && slot.forDate(
                        tomorrow,
                        exceptions
                    ) != null
                }
            }
            val tomorrowActivities = events.count { eventWithSubject ->
                val start = DateTimeUtils.parseDate(eventWithSubject.event.startDate)
                val end = DateTimeUtils.parseDate(eventWithSubject.event.endDate)
                !eventWithSubject.event.isCompleted && start != null && end != null &&
                        !start.isAfter(tomorrow) && !end.isBefore(tomorrow)
            }
            if (tomorrowClasses + tomorrowActivities > 0) {
                reminders += PlannedReminder(
                    id = "summary_$date",
                    triggerAtMillis = summaryTime.atZone(zoneId).toInstant().toEpochMilli(),
                    title = context.getString(R.string.notif_summary_title),
                    message = context.getString(
                        R.string.notif_summary_message,
                        tomorrowClasses,
                        tomorrowActivities
                    ),
                    channel = NotificationScheduler.CHANNEL_SUMMARY
                )
            }
        }
        date = date.plusDays(1)
    }

    events.filter { !it.event.isCompleted }.forEach { item ->
        val event = item.event
        val eventDate = DateTimeUtils.parseDate(event.startDate) ?: return@forEach
        val eventTime = if (event.isAllDay) LocalTime.of(9, 0)
        else DateTimeUtils.parseTime(event.startTime.orEmpty()) ?: return@forEach
        val categoryEnabled: Boolean
        val reminderMinutes: Int
        when (event.type) {
            SchoolEventType.TAREA -> {
                categoryEnabled = settings.taskNotificationsEnabled
                reminderMinutes = settings.taskReminderMinutes
            }

            SchoolEventType.EXAMEN -> {
                categoryEnabled = settings.examNotificationsEnabled
                reminderMinutes = settings.examReminderMinutes
            }

            else -> {
                categoryEnabled = settings.eventNotificationsEnabled
                reminderMinutes = settings.eventReminderMinutes
            }
        }
        val trigger = LocalDateTime.of(eventDate, eventTime)
            .minusMinutes(reminderMinutes.toLong())
        if (categoryEnabled && trigger.isAfter(now) && eventDate.isWithinHorizon(horizon)) {
            // Mismas etiquetas por tipo que ya usa CustomEventCard, para que la
            // notificación diga "Exposición: ..." en vez de agrupar todo bajo "Actividad".
            val label = when (event.type) {
                SchoolEventType.TAREA -> context.getString(R.string.tipo_tarea)
                SchoolEventType.EXAMEN -> context.getString(R.string.tipo_examen)
                SchoolEventType.EXPOSICION -> context.getString(R.string.tipo_exposicion)
                else -> context.getString(R.string.tipo_otro)
            }
            reminders += PlannedReminder(
                id = "event_${event.id}",
                triggerAtMillis = trigger.atZone(zoneId).toInstant().toEpochMilli(),
                title = context.getString(R.string.notif_event_reminder_title, label, event.title),
                message = DateTimeUtils.formatDate(event.startDate, true),
                channel = NotificationScheduler.CHANNEL_ACTIVITIES,
                eventId = event.id
            )
        }

        // Recordatorio de preparación (especialmente para exámenes)
        if (event.type == SchoolEventType.EXAMEN && settings.examPrepReminderEnabled) {
            val prepTrigger = LocalDateTime.of(eventDate, eventTime)
                .minusMinutes(settings.examPrepReminderMinutes.toLong())
            if (categoryEnabled && prepTrigger.isAfter(now) && eventDate.isWithinHorizon(horizon)) {
                reminders += PlannedReminder(
                    id = "event_prep_${event.id}",
                    triggerAtMillis = prepTrigger.atZone(zoneId).toInstant().toEpochMilli(),
                    title = context.getString(R.string.notif_prep_title, event.title),
                    message = context.getString(R.string.notif_prep_message),
                    channel = NotificationScheduler.CHANNEL_ACTIVITIES,
                    eventId = event.id
                )
            }
        }

        val dueDate = DateTimeUtils.parseDate(event.endDate) ?: return@forEach
        val dueTime = if (event.isAllDay) LocalTime.of(20, 0)
        else DateTimeUtils.parseTime(event.endTime.orEmpty()) ?: LocalTime.of(20, 0)
        val overdue = LocalDateTime.of(dueDate, dueTime).plusMinutes(1)
        if (settings.overdueNotificationsEnabled && categoryEnabled &&
            overdue.isAfter(now) && dueDate.isWithinHorizon(horizon)
        ) {
            reminders += PlannedReminder(
                id = "overdue_${event.id}",
                triggerAtMillis = overdue.atZone(zoneId).toInstant().toEpochMilli(),
                title = context.getString(R.string.notif_overdue_title),
                message = context.getString(R.string.notif_overdue_message, event.title),
                channel = NotificationScheduler.CHANNEL_ACTIVITIES,
                eventId = event.id
            )
        }
    }
    return reminders.distinctBy { it.id }
}