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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mocas.R
import com.mocas.data.local.SchoolEventWithSubject
import com.mocas.data.repository.GradeCalculator
import com.mocas.ui.components.dashboard.DailyFocusCard
import com.mocas.ui.components.dashboard.NextClassCard
import com.mocas.ui.components.dashboard.TaskItemRow
import com.mocas.ui.model.BottomNavTab
import com.mocas.ui.model.DailyClassStats
import com.mocas.ui.model.NextClassInfo
import com.mocas.ui.viewmodel.ScheduleViewModel
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale

// Ancho máximo del contenido: en pantallas grandes (tablet/desktop/foldable
// abierto) evita que el texto y las tarjetas se estiren de borde a borde.
private val MAX_CONTENT_WIDTH: Dp = 1200.dp

@Composable
fun DashboardScreen(
    viewModel: ScheduleViewModel,
    windowSizeClass: WindowSizeClass,
    onOpenAcademicProgress: () -> Unit = {},
    onOpenAcademicPeriods: () -> Unit = {},
    modifier: Modifier = Modifier
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

    DashboardContent(
        greeting = ScheduleViewModel.getGreetingText(
            LocalContext.current.applicationContext as Application,
            settings.userName
        ),
        nextClass = nextClass,
        dueTodayEvents = dueTodayEvents,
        classStats = classStats,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    greeting: String,
    nextClass: NextClassInfo?,
    dueTodayEvents: List<SchoolEventWithSubject>,
    classStats: DailyClassStats,
    windowSizeClass: WindowSizeClass,
    onNextClassClick: () -> Unit,
    onViewAllEvents: () -> Unit,
    onToggleEventCompleted: (SchoolEventWithSubject, Boolean) -> Unit,
    onEventClick: (SchoolEventWithSubject) -> Unit,
    onAddTaskClick: () -> Unit,
    onAddSubjectClick: () -> Unit,
    average: String? = null,
    remainingDays: Int? = null,
    academicPeriodName: String? = null,
    onOpenAcademicProgress: () -> Unit = {},
    onOpenAcademicPeriods: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }
    BackHandler(fabMenuExpanded) { fabMenuExpanded = false }

    val widthClass = windowSizeClass.widthSizeClass
    val isCompact = widthClass == WindowWidthSizeClass.Compact
    val isExpanded = widthClass == WindowWidthSizeClass.Expanded

    val horizontalPadding = if (isExpanded) 32.dp else 20.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = MAX_CONTENT_WIDTH)
                .fillMaxWidth(),
            contentPadding = PaddingValues(
                start = horizontalPadding,
                end = horizontalPadding,
                top = 24.dp,
                bottom = 104.dp
            ),
            verticalArrangement = Arrangement.spacedBy(if (isExpanded) 20.dp else 13.dp)
        ) {
            // Greeting Section
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (dueTodayEvents.isEmpty())
                            stringResource(R.string.todo_al_dia_mensaje)
                        else
                            stringResource(R.string.tareas_hoy_conteo_formato, dueTodayEvents.size),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                DashboardAcademicSummaryCard(
                    average = average,
                    remainingDays = remainingDays,
                    periodName = academicPeriodName,
                    onAverageClick = onOpenAcademicProgress,
                    onPeriodClick = onOpenAcademicPeriods
                )
            }

            if (isExpanded) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        if (nextClass != null) {
                            NextClassCard(nextClass = nextClass, onClick = onNextClassClick)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(
                                modifier = Modifier.weight(0.82f),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.progreso_del_dia),
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                DailyFocusCard(
                                    stats = classStats,
                                    modifier = Modifier.height(150.dp),
                                    showTitle = false
                                )
                            }
                            TasksDueTodaySection(
                                dueTodayEvents = dueTodayEvents,
                                onViewAllEvents = onViewAllEvents,
                                onToggleEventCompleted = onToggleEventCompleted,
                                onEventClick = onEventClick,
                                modifier = Modifier.weight(1.18f),
                                emptyStateHeight = 150.dp
                            )
                        }
                    }
                }
            } else if (isCompact) {
                if (nextClass != null) {
                    item {
                        NextClassCard(
                            nextClass = nextClass,
                            onClick = onNextClassClick
                        )
                    }
                }

                item {
                    DailyFocusCard(
                        stats = classStats
                    )
                }
            } else {
                if (nextClass != null) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                NextClassCard(
                                    nextClass = nextClass,
                                    onClick = onNextClassClick
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                DailyFocusCard(
                                    stats = classStats
                                )
                            }
                        }
                    }
                } else {
                    // Sin clase siguiente: DailyFocusCard ocupa
                    // el ancho en vez de dejar medio espacio vacío al lado.
                    item {
                        DailyFocusCard(
                            stats = classStats
                        )
                    }
                }
            }

            if (!isExpanded) {
                item {
                    TasksDueTodaySection(
                        dueTodayEvents = dueTodayEvents,
                        onViewAllEvents = onViewAllEvents,
                        onToggleEventCompleted = onToggleEventCompleted,
                        onEventClick = onEventClick
                    )
                }
            }
        }

        FloatingActionButtonMenu(
            modifier = Modifier.align(Alignment.BottomEnd),
            expanded = fabMenuExpanded,
            button = {
                ToggleFloatingActionButton(
                    checked = fabMenuExpanded,
                    onCheckedChange = { fabMenuExpanded = it }
                ) {
                    val icon = if (checkedProgress > 0.5f) Icons.Default.Close else Icons.Default.Add
                    Icon(
                        painter = rememberVectorPainter(icon),
                        contentDescription = stringResource(R.string.añadir_tarea),
                        modifier = Modifier.animateIcon({ checkedProgress })
                    )
                }
            }
        ) {
            FloatingActionButtonMenuItem(
                onClick = {
                    fabMenuExpanded = false
                    onAddTaskClick()
                },
                icon = { Icon(Icons.Default.AddTask, contentDescription = null) },
                text = { Text(stringResource(R.string.añadir_tarea)) }
            )
            FloatingActionButtonMenuItem(
                onClick = {
                    fabMenuExpanded = false
                    onAddSubjectClick()
                },
                icon = { Icon(Icons.Default.LibraryAdd, contentDescription = null) },
                text = { Text(stringResource(R.string.añadir_materia)) }
            )
        }
    }
}

@Composable
private fun DashboardAcademicSummaryCard(
    average: String?,
    remainingDays: Int?,
    periodName: String?,
    onAverageClick: () -> Unit,
    onPeriodClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                DashboardSummaryMetric(
                    modifier = Modifier.weight(1f).clickable(role = Role.Button, onClick = onAverageClick).padding(end = 12.dp),
                    icon = Icons.Default.School,
                    value = average ?: stringResource(R.string.academic_no_data_short),
                    label = stringResource(R.string.academic_average)
                )
                DashboardSummaryMetric(
                    modifier = Modifier.weight(1f).clickable(role = Role.Button, onClick = onPeriodClick).padding(start = 12.dp),
                    icon = Icons.Default.HourglassBottom,
                    value = remainingDays?.toString() ?: stringResource(R.string.academic_no_data_short),
                    label = stringResource(R.string.dashboard_days_remaining),
                    detail = periodName ?: stringResource(R.string.widget_no_academic_period)
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(1.dp)
                    .height(34.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
        }
    }
}

@Composable
private fun DashboardSummaryMetric(
    modifier: Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    detail: String? = null
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            }
        }
        Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            if (detail != null) {
                Text(detail, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }
    }
}

@Composable
private fun TasksDueTodaySection(
    dueTodayEvents: List<SchoolEventWithSubject>,
    onViewAllEvents: () -> Unit,
    onToggleEventCompleted: (SchoolEventWithSubject, Boolean) -> Unit,
    onEventClick: (SchoolEventWithSubject) -> Unit,
    modifier: Modifier = Modifier,
    emptyStateHeight: Dp? = null
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.tareas_para_hoy),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            TextButton(
                onClick = onViewAllEvents,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    stringResource(R.string.ver_todo),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (dueTodayEvents.isEmpty()) {
            if (emptyStateHeight != null) {
                Box(modifier = Modifier.fillMaxWidth().height(emptyStateHeight)) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceDim,
                        shadowElevation = 2.dp
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                stringResource(R.string.no_hay_tareas_hoy),
                                modifier = Modifier.padding(20.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceDim,
                    shadowElevation = 2.dp
                ) {
                    Text(
                        stringResource(R.string.no_hay_tareas_hoy),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                dueTodayEvents.take(5).forEach { eventWithSubject ->
                    TaskItemRow(
                        eventWithSubject = eventWithSubject,
                        onToggleCompleted = { completed ->
                            onToggleEventCompleted(eventWithSubject, completed)
                        },
                        onClick = { onEventClick(eventWithSubject) }
                    )
                }
            }
        }
    }
}
