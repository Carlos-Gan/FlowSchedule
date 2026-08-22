package com.mocas.data.notifications

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

data class PlannedReminder(
    val id: String,
    val triggerAtMillis: Long,
    val title: String,
    val message: String,
    val channel: String,
    val eventId: Long? = null
)

internal fun planReminders(
    subjects: List<SubjectWithSlots>,
    events: List<SchoolEventWithSubject>,
    exceptions: List<ClassExceptionEntity>,
    settings: AppSettings,
    now: LocalDateTime = LocalDateTime.now(),
    zoneId: ZoneId = ZoneId.systemDefault()
): List<PlannedReminder> {
    if (!settings.notificationsEnabled) return emptyList()
    val reminders = mutableListOf<PlannedReminder>()
    val horizon = now.toLocalDate().plusDays(30)

    var date = now.toLocalDate()
    while (!date.isAfter(horizon)) {
        if (settings.classNotificationsEnabled) subjects.filter { it.isActiveOn(date) }.forEach { item ->
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
                    reminders += PlannedReminder(
                        id = "class_${baseSlot.id}_$date",
                        triggerAtMillis = trigger.atZone(zoneId).toInstant().toEpochMilli(),
                        title = if (changed) "Cambio de clase: ${item.subject.name}" else "Próxima clase: ${item.subject.name}",
                        message = "${slot.startTime}-${slot.endTime}" +
                            room.takeIf { it.isNotBlank() }?.let { " · Aula $it" }.orEmpty(),
                        channel = NotificationScheduler.CHANNEL_CLASSES
                    )
                }
            }
        }

        val tomorrow = date.plusDays(1)
        val summaryTime = LocalDateTime.of(date, LocalTime.of(20, 0))
        if (settings.tomorrowSummaryEnabled && summaryTime.isAfter(now)) {
            val tomorrowClasses = subjects.filter { it.isActiveOn(tomorrow) }.sumOf { item ->
                item.slots.count { slot ->
                    slot.dayOfWeek == tomorrow.dayOfWeek.value && slot.forDate(tomorrow, exceptions) != null
                }
            }
            val tomorrowActivities = events.count {
                !it.event.isCompleted && it.event.startDate <= tomorrow.toString() &&
                    it.event.endDate >= tomorrow.toString()
            }
            if (tomorrowClasses + tomorrowActivities > 0) {
                reminders += PlannedReminder(
                    id = "summary_$date",
                    triggerAtMillis = summaryTime.atZone(zoneId).toInstant().toEpochMilli(),
                    title = "Resumen de mañana",
                    message = "$tomorrowClasses clases · $tomorrowActivities actividades",
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
        if (categoryEnabled && trigger.isAfter(now) && eventDate <= horizon) {
            val label = when (event.type) {
                SchoolEventType.TAREA -> "Tarea"
                SchoolEventType.EXAMEN -> "Examen"
                else -> "Actividad"
            }
            reminders += PlannedReminder(
                id = "event_${event.id}",
                triggerAtMillis = trigger.atZone(zoneId).toInstant().toEpochMilli(),
                title = "$label: ${event.title}",
                message = DateTimeUtils.formatDate(event.startDate, true),
                channel = NotificationScheduler.CHANNEL_ACTIVITIES,
                eventId = event.id
            )
        }
        val dueDate = DateTimeUtils.parseDate(event.endDate) ?: return@forEach
        val dueTime = if (event.isAllDay) LocalTime.of(20, 0)
        else DateTimeUtils.parseTime(event.endTime.orEmpty()) ?: LocalTime.of(20, 0)
        val overdue = LocalDateTime.of(dueDate, dueTime).plusMinutes(1)
        if (settings.overdueNotificationsEnabled && categoryEnabled &&
            overdue.isAfter(now) && dueDate <= horizon
        ) {
            reminders += PlannedReminder(
                id = "overdue_${event.id}",
                triggerAtMillis = overdue.atZone(zoneId).toInstant().toEpochMilli(),
                title = "Actividad vencida",
                message = "${event.title} sigue pendiente.",
                channel = NotificationScheduler.CHANNEL_ACTIVITIES,
                eventId = event.id
            )
        }
    }
    return reminders.distinctBy { it.id }
}
