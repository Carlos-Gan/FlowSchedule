package com.mocas.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.mocas.ui.model.AppSettings

class AppSettingsStore(context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    fun load(): AppSettings = AppSettings(
        userName = preferences.getString(KEY_USER_NAME, "Estudiante") ?: "Estudiante",
        educationLevel = preferences.getString(KEY_EDUCATION_LEVEL, "Grado o Carrera") ?: "Grado o Carrera",
        educationInstitution = preferences.getString(KEY_EDUCATION_INSTITUTION, "Escuela o Institución") ?: "Escuela o Institución",
        useGpaScale = preferences.getBoolean(KEY_USE_GPA_SCALE, false),
        themeMode = preferences.getString(KEY_THEME_MODE, "LIGHT") ?: "LIGHT",
        defaultReminderMinutes = preferences.getInt(KEY_REMINDER_MINUTES, 15),
        firstDayOfWeek = preferences.getInt(KEY_FIRST_DAY_OF_WEEK, 1),
        notificationsEnabled = preferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true),
        classNotificationsEnabled = preferences.getBoolean(KEY_CLASS_NOTIFICATIONS_ENABLED, true),
        taskNotificationsEnabled = preferences.getBoolean(KEY_TASK_NOTIFICATIONS_ENABLED, true),
        examNotificationsEnabled = preferences.getBoolean(KEY_EXAM_NOTIFICATIONS_ENABLED, true),
        eventNotificationsEnabled = preferences.getBoolean(KEY_EVENT_NOTIFICATIONS_ENABLED, true),
        overdueNotificationsEnabled = preferences.getBoolean(KEY_OVERDUE_NOTIFICATIONS_ENABLED, true),
        tomorrowSummaryEnabled = preferences.getBoolean(KEY_TOMORROW_SUMMARY_ENABLED, true),
        taskReminderMinutes = preferences.getInt(KEY_TASK_REMINDER_MINUTES, 2 * 24 * 60),
        examReminderMinutes = preferences.getInt(KEY_EXAM_REMINDER_MINUTES, 24 * 60),
        eventReminderMinutes = preferences.getInt(KEY_EVENT_REMINDER_MINUTES, 24 * 60),
        calendarSyncEnabled = preferences.getBoolean(KEY_CALENDAR_SYNC_ENABLED, true),
        aiFeaturesEnabled = preferences.getBoolean(KEY_AI_FEATURES_ENABLED, true),
        outsidePeriodsAreVacations = preferences.getBoolean(
            KEY_OUTSIDE_PERIODS_ARE_VACATIONS,
            false
        ),
        showVacationsInTimetable = preferences.getBoolean(
            KEY_SHOW_VACATIONS_IN_TIMETABLE,
            true
        ),
        targetCalendarName = preferences.getString(
            KEY_TARGET_CALENDAR_NAME,
            "Google Calendar (Escolar)"
        ) ?: "Google Calendar (Escolar)",
        language = preferences.getString(KEY_LANGUAGE, "Español") ?: "Español",
        onboardingCompleted = preferences.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    )

    fun save(settings: AppSettings) {
        preferences.edit()
            .putString(KEY_USER_NAME, settings.userName)
            .putString(KEY_EDUCATION_LEVEL, settings.educationLevel)
            .putString(KEY_EDUCATION_INSTITUTION, settings.educationInstitution)
            .putBoolean(KEY_USE_GPA_SCALE, settings.useGpaScale)
            .putString(KEY_THEME_MODE, settings.themeMode)
            .putInt(KEY_REMINDER_MINUTES, settings.defaultReminderMinutes)
            .putInt(KEY_FIRST_DAY_OF_WEEK, settings.firstDayOfWeek)
            .putBoolean(KEY_NOTIFICATIONS_ENABLED, settings.notificationsEnabled)
            .putBoolean(KEY_CLASS_NOTIFICATIONS_ENABLED, settings.classNotificationsEnabled)
            .putBoolean(KEY_TASK_NOTIFICATIONS_ENABLED, settings.taskNotificationsEnabled)
            .putBoolean(KEY_EXAM_NOTIFICATIONS_ENABLED, settings.examNotificationsEnabled)
            .putBoolean(KEY_EVENT_NOTIFICATIONS_ENABLED, settings.eventNotificationsEnabled)
            .putBoolean(KEY_OVERDUE_NOTIFICATIONS_ENABLED, settings.overdueNotificationsEnabled)
            .putBoolean(KEY_TOMORROW_SUMMARY_ENABLED, settings.tomorrowSummaryEnabled)
            .putInt(KEY_TASK_REMINDER_MINUTES, settings.taskReminderMinutes)
            .putInt(KEY_EXAM_REMINDER_MINUTES, settings.examReminderMinutes)
            .putInt(KEY_EVENT_REMINDER_MINUTES, settings.eventReminderMinutes)
            .putBoolean(KEY_CALENDAR_SYNC_ENABLED, settings.calendarSyncEnabled)
            .putBoolean(KEY_AI_FEATURES_ENABLED, settings.aiFeaturesEnabled)
            .putBoolean(KEY_OUTSIDE_PERIODS_ARE_VACATIONS, settings.outsidePeriodsAreVacations)
            .putBoolean(KEY_SHOW_VACATIONS_IN_TIMETABLE, settings.showVacationsInTimetable)
            .putString(KEY_TARGET_CALENDAR_NAME, settings.targetCalendarName)
            .putString(KEY_LANGUAGE, settings.language)
            .putBoolean(KEY_ONBOARDING_COMPLETED, settings.onboardingCompleted)
            .apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "snap_my_schedule_settings"
        const val KEY_USER_NAME = "user_name"
        const val KEY_EDUCATION_LEVEL = "education_level"
        const val KEY_EDUCATION_INSTITUTION = "education_institution"
        const val KEY_USE_GPA_SCALE = "use_gpa_scale"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_REMINDER_MINUTES = "reminder_minutes"
        const val KEY_FIRST_DAY_OF_WEEK = "first_day_of_week"
        const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        const val KEY_CLASS_NOTIFICATIONS_ENABLED = "class_notifications_enabled"
        const val KEY_TASK_NOTIFICATIONS_ENABLED = "task_notifications_enabled"
        const val KEY_EXAM_NOTIFICATIONS_ENABLED = "exam_notifications_enabled"
        const val KEY_EVENT_NOTIFICATIONS_ENABLED = "event_notifications_enabled"
        const val KEY_OVERDUE_NOTIFICATIONS_ENABLED = "overdue_notifications_enabled"
        const val KEY_TOMORROW_SUMMARY_ENABLED = "tomorrow_summary_enabled"
        const val KEY_TASK_REMINDER_MINUTES = "task_reminder_minutes"
        const val KEY_EXAM_REMINDER_MINUTES = "exam_reminder_minutes"
        const val KEY_EVENT_REMINDER_MINUTES = "event_reminder_minutes"
        const val KEY_CALENDAR_SYNC_ENABLED = "calendar_sync_enabled"
        const val KEY_AI_FEATURES_ENABLED = "ai_features_enabled"
        const val KEY_OUTSIDE_PERIODS_ARE_VACATIONS = "outside_periods_are_vacations"
        const val KEY_SHOW_VACATIONS_IN_TIMETABLE = "show_vacations_in_timetable"
        const val KEY_TARGET_CALENDAR_NAME = "target_calendar_name"
        const val KEY_LANGUAGE = "language"
        const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }
}
