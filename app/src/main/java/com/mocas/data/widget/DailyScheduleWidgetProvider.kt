package com.mocas.data.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.mocas.MainActivity
import com.mocas.R
import com.mocas.data.local.AppDatabase
import com.mocas.data.local.ClassExceptionEntity
import com.mocas.data.local.ClassExceptionType
import com.mocas.data.local.SchoolEventWithSubject
import com.mocas.data.local.SubjectWithSlots
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class DailyWidgetClassItem(
    val id: Long,
    val subjectName: String,
    val time: String,
    val room: String,
    val pendingCount: Int,
    val isHappeningNow: Boolean
)

class DailyScheduleWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { appWidgetId ->
            val serviceIntent = Intent(context, DailyScheduleWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            val openApp = PendingIntent.getActivity(
                context,
                appWidgetId,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val openFromClass = PendingIntent.getActivity(
                context,
                appWidgetId + 100_000,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            val dateLabel = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale.forLanguageTag("es-MX")))
                .replaceFirstChar { it.uppercase() }
            val views = RemoteViews(context.packageName, R.layout.widget_daily_schedule).apply {
                setTextViewText(R.id.daily_widget_date, dateLabel)
                setRemoteAdapter(R.id.daily_widget_list, serviceIntent)
                setEmptyView(R.id.daily_widget_list, R.id.daily_widget_empty)
                setOnClickPendingIntent(R.id.daily_widget_root, openApp)
                setPendingIntentTemplate(R.id.daily_widget_list, openFromClass)
            }
            manager.updateAppWidget(appWidgetId, views)
            manager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.daily_widget_list)
        }
    }

    companion object {
        fun requestUpdate(context: Context) {
            val component = ComponentName(context, DailyScheduleWidgetProvider::class.java)
            val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(component)
            if (ids.isEmpty()) return
            context.sendBroadcast(
                Intent(context, DailyScheduleWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
            )
        }
    }
}

class DailyScheduleWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory =
        DailyScheduleWidgetFactory(applicationContext)
}

private class DailyScheduleWidgetFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory {
    private var items: List<DailyWidgetClassItem> = emptyList()

    override fun onCreate() = Unit

    override fun onDataSetChanged() {
        items = runBlocking(Dispatchers.IO) {
            val database = AppDatabase.getDatabase(context)
            buildDailyWidgetItems(
                subjects = database.subjectDao().getAllSubjectsWithSlotsOnce(),
                exceptions = database.classExceptionDao().getAllOnce(),
                events = database.schoolEventDao().getAllEventsWithSubjectOnce(),
                date = LocalDate.now(),
                now = LocalTime.now()
            )
        }
    }

    override fun onDestroy() { items = emptyList() }
    override fun getCount(): Int = items.size

    override fun getViewAt(position: Int): RemoteViews? {
        val item = items.getOrNull(position) ?: return null
        return RemoteViews(context.packageName, R.layout.widget_daily_schedule_item).apply {
            setTextViewText(R.id.daily_item_time, item.time)
            setTextViewText(R.id.daily_item_subject, item.subjectName)
            setTextViewText(R.id.daily_item_room, item.room.ifBlank { "Sin salón" })
            setTextViewText(
                R.id.daily_item_pending,
                when (item.pendingCount) {
                    0 -> "Al día"
                    1 -> "1 pendiente"
                    else -> "${item.pendingCount} pendientes"
                }
            )
            setTextColor(
                R.id.daily_item_pending,
                Color.parseColor(if (item.pendingCount > 0) "#FBBF24" else "#A7F3D0")
            )
            setViewVisibility(R.id.daily_item_now, if (item.isHappeningNow) View.VISIBLE else View.GONE)
            setOnClickFillInIntent(R.id.daily_item_root, Intent())
        }
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = items.getOrNull(position)?.id ?: position.toLong()
    override fun hasStableIds(): Boolean = true
}

fun buildDailyWidgetItems(
    subjects: List<SubjectWithSlots>,
    exceptions: List<ClassExceptionEntity>,
    events: List<SchoolEventWithSubject>,
    date: LocalDate,
    now: LocalTime
): List<DailyWidgetClassItem> {
    val pendingBySubject = events
        .asSequence()
        .filter { !it.event.isCompleted && it.event.subjectId != null }
        .groupingBy { it.event.subjectId!! }
        .eachCount()

    return subjects.flatMap { item ->
        val periodStart = runCatching { LocalDate.parse(item.subject.semesterStart) }.getOrNull()
        val periodEnd = runCatching { LocalDate.parse(item.subject.semesterEnd) }.getOrNull()
        if (periodStart == null || periodEnd == null || date !in periodStart..periodEnd) {
            emptyList()
        } else {
            item.slots.filter { it.dayOfWeek == date.dayOfWeek.value }.mapNotNull { slot ->
                val exception = exceptions.firstOrNull { it.slotId == slot.id && it.date == date.toString() }
                if (exception?.type == ClassExceptionType.CANCELED) return@mapNotNull null
                val startText = exception?.newStartTime ?: slot.startTime
                val endText = exception?.newEndTime ?: slot.endTime
                val start = runCatching { LocalTime.parse(startText) }.getOrNull() ?: return@mapNotNull null
                val end = runCatching { LocalTime.parse(endText) }.getOrNull() ?: return@mapNotNull null
                DailyWidgetClassItem(
                    id = slot.id,
                    subjectName = item.subject.name,
                    time = "$startText–$endText",
                    room = (exception?.newRoom ?: slot.room).ifBlank { item.subject.defaultRoom },
                    pendingCount = pendingBySubject[item.subject.id] ?: 0,
                    isHappeningNow = !now.isBefore(start) && now.isBefore(end)
                )
            }
        }
    }.sortedBy { it.time }
}
