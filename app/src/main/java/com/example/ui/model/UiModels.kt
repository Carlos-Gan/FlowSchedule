package com.example.ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CalendarViewWeek
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.local.ScheduleSlotEntity
import com.example.data.local.SchoolEventEntity
import com.example.data.local.SubjectEntity

enum class BottomNavTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    INICIO("Inicio", Icons.Filled.Home, Icons.Outlined.Home),
    HORARIO("Horario", Icons.Filled.CalendarViewWeek, Icons.Outlined.CalendarViewWeek),
    CALENDARIO("Calendario", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    EVENTOS("Eventos", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle),
    CONFIGURACION("Ajustes", Icons.Filled.Settings, Icons.Outlined.Settings)
}

enum class TimetableDisplayMode(val displayName: String) {
    SEMANAL("Vista Semanal"),
    DIARIA("Vista Diaria"),
    MATERIAS("Lista de Materias")
}

data class NextClassInfo(
    val subject: SubjectEntity,
    val slot: ScheduleSlotEntity,
    val dayName: String,
    val timeRange: String,
    val room: String,
    val minutesUntil: Int,
    val isHappeningNow: Boolean
)

data class DayClassItem(
    val subject: SubjectEntity,
    val slot: ScheduleSlotEntity,
    val isLiveNow: Boolean = false,
    val isCompletedToday: Boolean = false
)

data class CalendarDayItem(
    val dateString: String, // "YYYY-MM-DD"
    val dayNumber: Int,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val isSelected: Boolean,
    val hasClasses: Boolean = false,
    val hasTasks: Boolean = false,
    val hasExams: Boolean = false,
    val hasEvents: Boolean = false,
    val isHoliday: Boolean = false
)

data class AppSettings(
    val userName: String = "Estudiante",
    val themeMode: String = "LIGHT", // LIGHT por defecto; AUTO y DARK siguen disponibles
    val defaultReminderMinutes: Int = 15,
    val firstDayOfWeek: Int = 1, // 1 = Lunes, 7 = Domingo
    val notificationsEnabled: Boolean = true,
    val calendarSyncEnabled: Boolean = true,
    val aiFeaturesEnabled: Boolean = true,
    val targetCalendarName: String = "Google Calendar (Escolar)",
    val language: String = "Español"
)
