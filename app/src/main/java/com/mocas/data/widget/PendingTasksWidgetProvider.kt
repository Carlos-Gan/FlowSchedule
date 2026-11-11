package com.mocas.data.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.mocas.R
import com.mocas.data.local.AppDatabase
import com.mocas.data.repository.ScheduleRepository
import com.mocas.util.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

data class PendingTaskWidgetModel(
    val id: Long,
    val title: String,
    val subjectName: String,
    val dateText: String,
    val typeName: String
)

class PendingTasksWidgetProvider : AppWidgetProvider() {
    @Suppress("DEPRECATION")
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_TOGGLE_TASK) {
            val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
            if (taskId != -1L) {
                runBlocking(Dispatchers.IO) {
                    val db = AppDatabase.getDatabase(context)
                    val repo = ScheduleRepository(context, db)
                    val event = db.schoolEventDao().getEventById(taskId)
                    if (event != null) {
                        repo.setEventCompleted(taskId, !event.isCompleted)
                    }
                }
                requestUpdate(context)
                ScheduleWidgetProvider.requestUpdate(context)
                DailyScheduleWidgetProvider.requestUpdate(context)
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { appWidgetId ->
            val serviceIntent = Intent(context, PendingTasksWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            val toggleIntent = Intent(context, PendingTasksWidgetProvider::class.java).apply {
                action = ACTION_TOGGLE_TASK
            }
            val togglePendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId + 200_000,
                toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )

            val views = RemoteViews(context.packageName, R.layout.widget_pending_tasks).apply {
                setRemoteAdapter(appWidgetId, R.id.pending_tasks_widget_list, serviceIntent)
                setEmptyView(R.id.pending_tasks_widget_list, R.id.pending_tasks_widget_empty)
                setPendingIntentTemplate(R.id.pending_tasks_widget_list, togglePendingIntent)
            }
            manager.updateAppWidget(appWidgetId, views)
        }
        manager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.pending_tasks_widget_list)
    }

    companion object {
        const val ACTION_TOGGLE_TASK = "com.mocas.ACTION_TOGGLE_TASK"
        const val EXTRA_TASK_ID = "EXTRA_TASK_ID"

        fun requestUpdate(context: Context) {
            val component = ComponentName(context, PendingTasksWidgetProvider::class.java)
            val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(component)
            if (ids.isEmpty()) return
            context.sendBroadcast(
                Intent(context, PendingTasksWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
            )
        }
    }
}

class PendingTasksWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory =
        PendingTasksWidgetFactory(applicationContext)
}

private class PendingTasksWidgetFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory {
    private var items: List<PendingTaskWidgetModel> = emptyList()

    override fun onCreate() = Unit

    override fun onDataSetChanged() {
        items = runBlocking(Dispatchers.IO) {
            val database = AppDatabase.getDatabase(context)
            database.schoolEventDao().getAllEventsWithSubjectOnce()
                .filter { !it.event.isCompleted && !it.event.isDeleted }
                .sortedBy { it.event.startDate }
                .map { item ->
                    PendingTaskWidgetModel(
                        id = item.event.id,
                        title = item.event.title,
                        subjectName = item.subject?.name ?: context.getString(R.string.sin_materia),
                        dateText = DateTimeUtils.formatRelativeDate(item.event.startDate),
                        typeName = item.event.type.name
                    )
                }
        }
    }

    override fun onDestroy() { items = emptyList() }
    override fun getCount(): Int = items.size

    override fun getViewAt(position: Int): RemoteViews? {
        val item = items.getOrNull(position) ?: return null
        return RemoteViews(context.packageName, R.layout.widget_pending_tasks_item).apply {
            setTextViewText(R.id.pending_item_title, item.title)
            setTextViewText(R.id.pending_item_subject, item.subjectName)
            setTextViewText(R.id.pending_item_date, item.dateText)

            val fillInIntent = Intent().apply {
                putExtra(PendingTasksWidgetProvider.EXTRA_TASK_ID, item.id)
            }
            setOnClickFillInIntent(R.id.pending_item_checkbox, fillInIntent)
            setOnClickFillInIntent(R.id.pending_item_root, fillInIntent)
        }
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = items.getOrNull(position)?.id ?: position.toLong()
    override fun hasStableIds(): Boolean = true
}
