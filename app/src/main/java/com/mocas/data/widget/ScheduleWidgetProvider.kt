package com.mocas.data.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.mocas.MainActivity
import com.mocas.R
import com.mocas.data.local.AppDatabase
import com.mocas.data.local.ClassExceptionEntity
import com.mocas.data.local.ClassExceptionType
import com.mocas.data.local.SubjectWithSlots
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class WidgetSnapshot(
    val className: String,
    val classDetails: String,
    val countdown: String,
    val pendingCount: Int
)

class ScheduleWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getDatabase(context)
                val snapshot = buildWidgetSnapshot(
                    context = context,
                    subjects = database.subjectDao().getAllSubjectsWithSlotsOnce(),
                    exceptions = database.classExceptionDao().getAllOnce(),
                    pendingCount = database.schoolEventDao().getAllEventsWithSubjectOnce().count { !it.event.isCompleted },
                    now = LocalDateTime.now()
                )
                appWidgetIds.forEach { id -> manager.updateAppWidget(id, createViews(context, snapshot)) }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        fun requestUpdate(context: Context) {
            val component = ComponentName(context, ScheduleWidgetProvider::class.java)
            val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(component)
            if (ids.isEmpty()) return
            context.sendBroadcast(
                Intent(context, ScheduleWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
            )
        }

        private fun createViews(context: Context, snapshot: WidgetSnapshot): RemoteViews {
            val openApp = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            return RemoteViews(context.packageName, R.layout.widget_schedule).apply {
                setTextViewText(R.id.widget_class_name, snapshot.className)
                setTextViewText(R.id.widget_class_details, snapshot.classDetails)
                setTextViewText(R.id.widget_countdown, snapshot.countdown)
                setTextViewText(
                    R.id.widget_pending,
                    if (snapshot.pendingCount == 1) context.getString(R.string.widget_pending_singular)
                    else context.getString(R.string.widget_pending_plural, snapshot.pendingCount)
                )
                setOnClickPendingIntent(R.id.widget_root, openApp)
            }
        }
    }
}

fun buildWidgetSnapshot(
    context: Context,
    subjects: List<SubjectWithSlots>,
    exceptions: List<ClassExceptionEntity>,
    pendingCount: Int,
    now: LocalDateTime
): WidgetSnapshot {
    data class Candidate(val subject: String, val room: String, val start: LocalDateTime, val end: LocalDateTime)
    // Cubre también vacaciones largas y el siguiente periodo académico.
    val candidates = (0..370).flatMap { offset ->
        val date = now.toLocalDate().plusDays(offset.toLong())
        subjects.flatMap { item ->
            val semesterStart = runCatching { LocalDate.parse(item.subject.semesterStart) }.getOrNull()
            val semesterEnd = runCatching { LocalDate.parse(item.subject.semesterEnd) }.getOrNull()
            if (semesterStart == null || semesterEnd == null || date !in semesterStart..semesterEnd) {
                emptyList()
            } else {
                item.slots.filter { it.dayOfWeek == date.dayOfWeek.value }.mapNotNull { slot ->
                    val exception = exceptions.firstOrNull { it.slotId == slot.id && it.date == date.toString() }
                    if (exception?.type == ClassExceptionType.CANCELED) return@mapNotNull null
                    val startTime = parseWidgetTime(exception?.newStartTime ?: slot.startTime) ?: return@mapNotNull null
                    val endTime = parseWidgetTime(exception?.newEndTime ?: slot.endTime) ?: return@mapNotNull null
                    Candidate(
                        subject = item.subject.name,
                        room = exception?.newRoom ?: slot.room.ifBlank { item.subject.defaultRoom },
                        start = LocalDateTime.of(date, startTime),
                        end = LocalDateTime.of(date, endTime)
                    )
                }
            }
        }
    }.filter { it.end.isAfter(now) }.sortedBy { it.start }

    val next = candidates.firstOrNull()
        ?: return WidgetSnapshot(
            context.getString(R.string.widget_no_classes_title),
            context.getString(R.string.widget_no_classes_msg),
            context.getString(R.string.widget_no_classes_action),
            pendingCount
        )
    val locale = context.resources.configuration.locales[0]
    val dayLabel = when (next.start.toLocalDate()) {
        now.toLocalDate() -> context.getString(R.string.widget_today)
        now.toLocalDate().plusDays(1) -> context.getString(R.string.widget_tomorrow)
        else -> next.start.format(DateTimeFormatter.ofPattern(context.getString(R.string.widget_date_pattern), locale)).replaceFirstChar { it.uppercase(locale) }
    }
    val time = next.start.format(DateTimeFormatter.ofPattern("HH:mm"))
    val details = listOf("$dayLabel · $time", next.room).filter { it.isNotBlank() }.joinToString(" · ")
    return WidgetSnapshot(next.subject, details, formatCountdown(context, now, next.start, next.end), pendingCount)
}

private fun parseWidgetTime(value: String): LocalTime? = runCatching { LocalTime.parse(value) }.getOrNull()

private fun formatCountdown(context: Context, now: LocalDateTime, start: LocalDateTime, end: LocalDateTime): String {
    if (!now.isBefore(start) && now.isBefore(end)) return context.getString(R.string.widget_in_class_now)
    val minutes = Duration.between(now, start).toMinutes().coerceAtLeast(0)
    return when {
        minutes < 60 -> context.getString(R.string.widget_countdown_mins, minutes)
        minutes < 24 * 60 -> context.getString(R.string.widget_countdown_hours_mins, minutes / 60, minutes % 60)
        else -> context.getString(R.string.widget_countdown_days, minutes / (24 * 60))
    }
}
