package com.mocas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mocas.R
import android.app.Application
import androidx.compose.ui.platform.LocalContext
import com.mocas.data.local.SchoolEventWithSubject
import com.mocas.ui.components.dashboard.DailyFocusCard
import com.mocas.ui.components.dashboard.NextClassCard
import com.mocas.ui.components.dashboard.QuickActionButton
import com.mocas.ui.components.dashboard.TaskItemRow
import com.mocas.ui.model.BottomNavTab
import com.mocas.ui.model.DailyClassStats
import com.mocas.ui.model.NextClassInfo
import com.mocas.ui.viewmodel.ScheduleViewModel

// Ancho máximo del contenido: en pantallas grandes (tablet/desktop/foldable
// abierto) evita que el texto y las tarjetas se estiren de borde a borde.
private val MAX_CONTENT_WIDTH: Dp = 900.dp

@Composable
fun DashboardScreen(
    viewModel: ScheduleViewModel,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier
) {
    val isCompact = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    val nextClass by viewModel.nextClassInfo.collectAsStateWithLifecycle()
    val allEvents by viewModel.allEventsWithSubject.collectAsStateWithLifecycle()
    val classStats by viewModel.todayClassStats.collectAsStateWithLifecycle()
    val today = ScheduleViewModel.getTodayDateString()

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
        windowSizeClass = windowSizeClass,
        onNextClassClick = { nextClass?.subject?.id?.let(viewModel::openSubjectDetail) },
        onViewAllEvents = { viewModel.setTab(BottomNavTab.EVENTOS) },
        onToggleEventCompleted = { eventWithSubject, completed ->
            viewModel.toggleEventCompleted(eventWithSubject.event.id, completed)
        },
        onEventClick = { viewModel.openAddEvent(it) },
        onAddTaskClick = { viewModel.openAddEvent() },
        onAddSubjectClick = { viewModel.openAddSubject() },
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
    modifier: Modifier = Modifier
) {
    val widthClass = windowSizeClass.widthSizeClass
    val isCompact = widthClass == WindowWidthSizeClass.Compact

    val horizontalPadding =  20.dp

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
            contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
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

            // Quick Actions Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionButton(
                        icon = Icons.Default.AddTask,
                        label = stringResource(R.string.añadir_tarea),
                        containerColor = MaterialTheme.colorScheme.surfaceDim,
                        contentColor = MaterialTheme.colorScheme.primary,
                        onClick = onAddTaskClick,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionButton(
                        icon = Icons.Default.LibraryAdd,
                        label = stringResource(R.string.añadir_materia),
                        containerColor = MaterialTheme.colorScheme.surfaceDim,
                        contentColor = MaterialTheme.colorScheme.secondary,
                        onClick = onAddSubjectClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Next Class & Daily Focus Section (Conditional Layout)
            if (isCompact) {
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
                        stats = classStats,
                        modifier = Modifier.fillMaxWidth()
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
                                    stats = classStats,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                } else {
                    // Sin clase siguiente: DailyFocusCard ocupa
                    // el ancho en vez de dejar medio espacio vacío al lado.
                    item {
                        DailyFocusCard(
                            stats = classStats,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Tasks Due Today Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
        }
    }
}