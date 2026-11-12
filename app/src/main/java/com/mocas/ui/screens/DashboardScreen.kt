package com.mocas.ui.screens

import android.app.Application
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mocas.R
import com.mocas.data.local.SchoolEventWithSubject
import com.mocas.data.repository.GradeCalculator
import com.mocas.ui.components.dashboard.DailyFocusCard
import com.mocas.ui.components.dashboard.DashboardAcademicSummaryCard
import com.mocas.ui.components.dashboard.DashboardContent
import com.mocas.ui.components.dashboard.DashboardSummaryMetric
import com.mocas.ui.components.dashboard.NextClassCard
import com.mocas.ui.components.dashboard.TaskItemRow
import com.mocas.ui.components.dashboard.TasksDueTodaySection
import com.mocas.ui.model.BottomNavTab
import com.mocas.ui.model.DailyClassStats
import com.mocas.ui.model.NextClassInfo
import com.mocas.ui.viewmodel.ScheduleViewModel
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: ScheduleViewModel,
    windowSizeClass: WindowSizeClass,
    onOpenAcademicProgress: () -> Unit = {},
    onOpenAcademicPeriods: () -> Unit = {},
) {
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    val nextClass by viewModel.nextClassInfo.collectAsStateWithLifecycle()
    val allEvents by viewModel.allEventsWithSubject.collectAsStateWithLifecycle()
    val classStats by viewModel.todayClassStats.collectAsStateWithLifecycle()
    val subjects by viewModel.subjectsWithSlots.collectAsStateWithLifecycle()
    val periods by viewModel.academicPeriods.collectAsStateWithLifecycle()
    val gradeCategories by viewModel.gradeCategories.collectAsStateWithLifecycle()
    val gradeItems by viewModel.gradeItems.collectAsStateWithLifecycle()
    val gradeUnits by viewModel.gradeUnits.collectAsStateWithLifecycle()
    val gradeWeights by viewModel.gradeUnitCategoryWeights.collectAsStateWithLifecycle()
    val today = ScheduleViewModel.getTodayDateString()

    val average = remember(subjects, gradeCategories, gradeItems, gradeUnits, gradeWeights, settings.useGpaScale) {
        GradeCalculator.periodAverage(
            subjectIds = subjects.filterNot { it.subject.isDeleted }.map { it.subject.id }.distinct(),
            categories = gradeCategories,
            items = gradeItems,
            units = gradeUnits,
            unitCategoryWeights = gradeWeights
        )?.let { value ->
            val displayValue = if (settings.useGpaScale) {
                if (value > 10) (value / 100.0) * 4.0 else (value / 10.0) * 4.0
            } else value
            val pattern = if (settings.useGpaScale) "%.2f" else if (displayValue > 10) "%.0f" else "%.1f"
            String.format(Locale.getDefault(), pattern, displayValue)
        }
    }
    val activePeriod = remember(periods, today) {
        val now = runCatching { LocalDate.parse(today) }.getOrElse { LocalDate.now() }
        periods.firstOrNull { period ->
            val start = runCatching { LocalDate.parse(period.startDate) }.getOrNull()
            val end = runCatching { LocalDate.parse(period.endDate) }.getOrNull()
            start != null && end != null && now in start..end
        }
    }
    val remainingDays = activePeriod?.let { period ->
        runCatching {
            val now = runCatching { LocalDate.parse(today) }.getOrElse { LocalDate.now() }
            ChronoUnit.DAYS.between(now, LocalDate.parse(period.endDate)).toInt().coerceAtLeast(0)
        }.getOrNull()
    }

    val dueTodayEvents = remember(allEvents, today) {
        allEvents
            .filter { !it.event.isCompleted && it.event.endDate == today }
            .sortedBy { it.event.startTime ?: "23:59" }
    }

    val todayDate = remember(today) {
        runCatching { LocalDate.parse(today) }.getOrElse { LocalDate.now() }
    }
    val isVacation = remember(todayDate, periods, allEvents, settings.outsidePeriodsAreVacations) {
        com.mocas.ui.util.isVacationDate(todayDate, periods, allEvents, settings.outsidePeriodsAreVacations)
    }

    DashboardContent(
        greeting = ScheduleViewModel.getGreetingText(
            LocalContext.current.applicationContext as Application,
            settings.userName
        ),
        nextClass = nextClass,
        dueTodayEvents = dueTodayEvents,
        classStats = classStats,
        isVacation = isVacation,
        average = average,
        remainingDays = remainingDays,
        academicPeriodName = activePeriod?.name,
        windowSizeClass = windowSizeClass,
        onNextClassClick = { nextClass?.subject?.id?.let(viewModel::openSubjectDetail) },
        onViewAllEvents = { viewModel.setTab(BottomNavTab.EVENTOS) },
        onToggleEventCompleted = { eventWithSubject, completed ->
            viewModel.toggleEventCompleted(eventWithSubject.event.id, completed)
        },
        onEventClick = { viewModel.openAddEvent(it) },
        onAddTaskClick = { viewModel.openAddEvent() },
        onAddSubjectClick = { viewModel.openAddSubject() },
        onOpenAcademicProgress = onOpenAcademicProgress,
        onOpenAcademicPeriods = onOpenAcademicPeriods,
        modifier = modifier
    )
}