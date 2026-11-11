package com.mocas.data.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import com.mocas.MainActivity
import com.mocas.R
import com.mocas.data.local.AcademicPeriodEntity
import com.mocas.data.local.AppDatabase
import com.mocas.data.local.ClassExceptionEntity
import com.mocas.data.local.ClassExceptionType
import com.mocas.data.local.SchoolEventWithSubject
import com.mocas.data.local.SubjectWithSlots
import com.mocas.ui.util.isVacationDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class AcademicPeriodWidgetSnapshot(
    val periodName: String,
    val dateRange: String,
    val remainingDays: Int,
    val elapsedDays: Int,
    val totalDays: Int,
    val progressPercent: Int,
    val isFinished: Boolean,
    val hasActivePeriod: Boolean
)

class AcademicPeriodWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val snapshot = buildAcademicPeriodSnapshot(
                    periods = db.academicPeriodDao().getAllPeriodsOnce(),
                    subjects = db.subjectDao().getAllSubjectsWithSlotsOnce(),
                    exceptions = db.classExceptionDao().getAllOnce(),
                    events = db.schoolEventDao().getAllEventsWithSubjectOnce(),
                    now = LocalDate.now(),
                    outsidePeriodsAreVacations = false
                )
                appWidgetIds.forEach { id ->
                    val options = manager.getAppWidgetOptions(id)
                    val views = createViews(context, snapshot, options)
                    manager.updateAppWidget(id, views)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val snapshot = buildAcademicPeriodSnapshot(
                    periods = db.academicPeriodDao().getAllPeriodsOnce(),
                    subjects = db.subjectDao().getAllSubjectsWithSlotsOnce(),
                    exceptions = db.classExceptionDao().getAllOnce(),
                    events = db.schoolEventDao().getAllEventsWithSubjectOnce(),
                    now = LocalDate.now(),
                    outsidePeriodsAreVacations = false
                )
                val views = createViews(context, snapshot, newOptions)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        fun requestUpdate(context: Context) {
            val component = ComponentName(context, AcademicPeriodWidgetProvider::class.java)
            val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(component)
            if (ids.isEmpty()) return
            context.sendBroadcast(
                Intent(context, AcademicPeriodWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
            )
        }

        private fun createViews(context: Context, snapshot: AcademicPeriodWidgetSnapshot, options: Bundle?): RemoteViews {
            val openApp = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val minWidth = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 130) ?: 130
            val isWide = minWidth >= 220 // 4 cells or more width

            return RemoteViews(context.packageName, R.layout.widget_academic_period).apply {
                if (!snapshot.hasActivePeriod) {
                    setTextViewText(R.id.period_widget_title, context.getString(R.string.widget_no_academic_period))
                    setTextViewText(R.id.period_widget_dates, "")
                    setTextViewText(R.id.period_widget_remaining_number, "-")
                    setTextViewText(R.id.period_widget_subtitle, context.getString(R.string.widget_no_academic_period_msg))
                    setTextViewText(R.id.period_widget_total, "")
                    setProgressBar(R.id.period_widget_progress_bar, 100, 0, false)
                    setViewVisibility(R.id.period_widget_elapsed_container, View.GONE)
                } else {
                    setTextViewText(R.id.period_widget_title, snapshot.periodName)
                    setTextViewText(R.id.period_widget_dates, snapshot.dateRange)
                    setProgressBar(R.id.period_widget_progress_bar, 100, snapshot.progressPercent, false)
                    
                    if (snapshot.isFinished) {
                        setTextViewText(R.id.period_widget_remaining_number, "0")
                        setTextViewText(R.id.period_widget_subtitle, context.getString(R.string.widget_period_ended))
                    } else {
                        setTextViewText(R.id.period_widget_remaining_number, snapshot.remainingDays.toString())
                        setTextViewText(R.id.period_widget_subtitle, context.getString(R.string.widget_school_days_remaining))
                    }

                    if (isWide) {
                        setViewVisibility(R.id.period_widget_elapsed_container, View.VISIBLE)
                        setTextViewText(R.id.period_widget_elapsed_number, snapshot.elapsedDays.toString())
                        setTextViewText(
                            R.id.period_widget_total,
                            context.getString(R.string.widget_school_days_total_progress, snapshot.totalDays, snapshot.progressPercent)
                        )
                    } else {
                        setViewVisibility(R.id.period_widget_elapsed_container, View.GONE)
                        setTextViewText(
                            R.id.period_widget_total,
                            context.getString(R.string.widget_school_days_total_simple, snapshot.totalDays)
                        )
                    }
                }
                setOnClickPendingIntent(R.id.period_widget_root, openApp)
            }
        }
    }
}

fun buildAcademicPeriodSnapshot(
    periods: List<AcademicPeriodEntity>,
    subjects: List<SubjectWithSlots>,
    exceptions: List<ClassExceptionEntity>,
    events: List<SchoolEventWithSubject>,
    now: LocalDate,
    outsidePeriodsAreVacations: Boolean = false
): AcademicPeriodWidgetSnapshot {
    val activePeriod = periods.firstOrNull { 
        val start = runCatching { LocalDate.parse(it.startDate) }.getOrNull()
        val end = runCatching { LocalDate.parse(it.endDate) }.getOrNull()
        start != null && end != null && !now.isBefore(start) && !now.isAfter(end)
    } ?: periods.maxByOrNull { it.endDate }

    if (activePeriod == null) {
        return AcademicPeriodWidgetSnapshot(
            periodName = "",
            dateRange = "",
            remainingDays = 0,
            elapsedDays = 0,
            totalDays = 0,
            progressPercent = 0,
            isFinished = false,
            hasActivePeriod = false
        )
    }

    val start = runCatching { LocalDate.parse(activePeriod.startDate) }.getOrNull()
    val end = runCatching { LocalDate.parse(activePeriod.endDate) }.getOrNull()

    if (start == null || end == null || start > end) {
        return AcademicPeriodWidgetSnapshot(
            periodName = activePeriod.name,
            dateRange = "",
            remainingDays = 0,
            elapsedDays = 0,
            totalDays = 0,
            progressPercent = 0,
            isFinished = false,
            hasActivePeriod = true
        )
    }

    fun isSchoolDay(date: LocalDate): Boolean {
        if (date.isBefore(start) || date.isAfter(end)) return false
        if (isVacationDate(date, periods, events, outsidePeriodsAreVacations)) return false

        val hasClasses = subjects.any { item ->
            val semStart = runCatching { LocalDate.parse(item.subject.semesterStart) }.getOrNull()
            val semEnd = runCatching { LocalDate.parse(item.subject.semesterEnd) }.getOrNull()
            !(semStart == null || semEnd == null || date !in semStart..semEnd) && item.slots.any { slot ->
                if (slot.dayOfWeek != date.dayOfWeek.value) return@any false
                val exception = exceptions.firstOrNull { it.slotId == slot.id && it.date == date.toString() }
                exception?.type != ClassExceptionType.CANCELED
            }
        }
        return hasClasses
    }

    var totalDays = 0
    var elapsedDays = 0
    var remainingDays = 0

    var curr: LocalDate = start

    while (!curr.isAfter(end)) {
        if (isSchoolDay(curr)) {
            totalDays++
            if (curr.isBefore(now)) {
                elapsedDays++
            } else {
                remainingDays++
            }
        }
        curr = curr.plusDays(1)
    }

    val isFinished = now.isAfter(end)
    val progressPercent = if (totalDays > 0) ((elapsedDays * 100) / totalDays).coerceIn(0, 100) else 0
    val formatter = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
    val dateRangeStr = "${start.format(formatter)} – ${end.format(formatter)}"

    return AcademicPeriodWidgetSnapshot(
        periodName = activePeriod.name,
        dateRange = dateRangeStr,
        remainingDays = remainingDays,
        elapsedDays = elapsedDays,
        totalDays = totalDays,
        progressPercent = progressPercent,
        isFinished = isFinished,
        hasActivePeriod = true
    )
}
