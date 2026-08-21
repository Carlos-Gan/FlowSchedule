package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SchoolEventType
import com.example.data.repository.CalendarSyncHelper
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.EventItemCard
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.TurquoiseSecondary
import com.example.ui.util.showCalendarResult
import com.example.ui.viewmodel.ScheduleViewModel

@Composable
fun EventsScreen(
    viewModel: ScheduleViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allEvents by viewModel.allEventsWithSubject.collectAsStateWithLifecycle()
    val activeFilter by viewModel.eventFilter.collectAsStateWithLifecycle()

    val filterOptions = listOf(
        "TODOS" to "Todos",
        SchoolEventType.TAREA.name to "Tareas",
        SchoolEventType.EXAMEN.name to "Exámenes",
        SchoolEventType.EXPOSICION.name to "Exposiciones",
        SchoolEventType.EVENTO_ESCOLAR.name to "Eventos",
        "COMPLETADOS" to "Completados"
    )

    // Filter logic
    val filteredList = remember(allEvents, activeFilter) {
        when (activeFilter) {
            "TODOS" -> allEvents
            "COMPLETADOS" -> allEvents.filter { it.event.isCompleted }
            else -> allEvents.filter { it.event.type.name == activeFilter }
        }
    }

    val totalCount = allEvents.size
    val completedCount = allEvents.count { it.event.isCompleted }
    val progressFraction = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .testTag("events_screen_list"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Bento Summary Progress Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Progreso del Semestre",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$completedCount de $totalCount actividades completadas",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = IndigoPrimary.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, IndigoPrimary.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "${(progressFraction * 100).toInt()}%",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp
                                    ),
                                    color = IndigoPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = IndigoPrimary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }

            // Bento Category Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filterOptions) { (key, label) ->
                        val isSelected = activeFilter == key
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .clickable { viewModel.setEventFilter(key) }
                                .testTag("event_filter_$key"),
                            tonalElevation = if (isSelected) 3.dp else 0.dp
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Events List or Empty State
            if (filteredList.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "No hay actividades en esta categoría",
                        message = "Puedes registrar tareas, exposiciones o recordatorios escolares.",
                        icon = Icons.Default.Assignment,
                        actionButtonText = "+ Nueva Tarea o Evento",
                        onActionClick = { viewModel.openAddEvent() }
                    )
                }
            } else {
                items(filteredList, key = { it.event.id }) { eventWithSubject ->
                    EventItemCard(
                        eventWithSubject = eventWithSubject,
                        onToggleCompleted = {
                            viewModel.toggleEventCompleted(eventWithSubject.event.id, it)
                        },
                        onClick = { viewModel.openAddEvent(eventWithSubject) },
                        onCalendarSyncClick = {
                            showCalendarResult(
                                context,
                                CalendarSyncHelper.addEventToPhoneCalendar(
                                    context,
                                    eventWithSubject.event,
                                    eventWithSubject.subject?.name
                                )
                            )
                        },
                        onDeleteClick = {
                            viewModel.deleteEvent(eventWithSubject.event.id)
                        }
                    )
                }
            }
        }

        // Floating Action Button with Bento rounded shape
        FloatingActionButton(
            onClick = { viewModel.openAddEvent() },
            shape = RoundedCornerShape(16.dp),
            containerColor = IndigoPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 20.dp)
                .testTag("events_fab_add")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Nueva Actividad"
            )
        }
    }
}
