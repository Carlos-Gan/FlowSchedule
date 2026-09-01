package com.mocas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mocas.R
import com.mocas.data.local.SchoolEventWithSubject
import com.mocas.ui.components.dashboard.DailyFocusCard
import com.mocas.ui.components.dashboard.NextClassCard
import com.mocas.ui.components.dashboard.QuickActionButton
import com.mocas.ui.components.dashboard.TaskItemRow
import com.mocas.ui.model.BottomNavTab
import com.mocas.ui.model.DailyClassStats
import com.mocas.ui.model.NextClassInfo
import com.mocas.ui.viewmodel.ScheduleViewModel

@Composable
fun DashboardScreen(
    viewModel: ScheduleViewModel,
    modifier: Modifier = Modifier
) {
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
        userName = settings.userName,
        nextClass = nextClass,
        dueTodayEvents = dueTodayEvents,
        classStats = classStats,
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
    userName: String,
    nextClass: NextClassInfo?,
    dueTodayEvents: List<SchoolEventWithSubject>,
    classStats: DailyClassStats,
    onNextClassClick: () -> Unit,
    onViewAllEvents: () -> Unit,
    onToggleEventCompleted: (SchoolEventWithSubject, Boolean) -> Unit,
    onEventClick: (SchoolEventWithSubject) -> Unit,
    onAddTaskClick: () -> Unit,
    onAddSubjectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        // Greeting Section
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.buenos_dias_formato, userName),
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
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    onClick = onAddTaskClick,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    icon = Icons.Default.LibraryAdd,
                    label = stringResource(R.string.añadir_materia),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
                    contentColor = MaterialTheme.colorScheme.secondary,
                    onClick = onAddSubjectClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Next Class Section
        if (nextClass != null) {
            item {
                NextClassCard(
                    nextClass = nextClass,
                    onClick = onNextClassClick
                )
            }
        }

        // Daily Focus Section
        item {
            DailyFocusCard(
                stats = classStats
            )
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
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
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
