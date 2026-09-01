package com.mocas.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CoPresent
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mocas.data.local.SchoolEventEntity
import com.mocas.data.local.SchoolEventType
import com.mocas.data.local.SchoolEventWithSubject
import com.mocas.data.local.EventPriority
import com.mocas.data.local.RecurrenceType
import com.mocas.util.DateTimeUtils
import java.util.Locale

fun getEventTypeIcon(
    type: SchoolEventType
): ImageVector {
    return when (type) {
        SchoolEventType.TAREA ->
            Icons.Default.Assignment

        SchoolEventType.EXAMEN ->
            Icons.Default.Quiz

        SchoolEventType.EXPOSICION ->
            Icons.Default.CoPresent

        SchoolEventType.EVENTO_ESCOLAR ->
            Icons.Default.School

        SchoolEventType.REUNION ->
            Icons.Default.Groups

        SchoolEventType.VACACIONES ->
            Icons.Default.BeachAccess

        SchoolEventType.OTRO ->
            Icons.Default.Event
    }
}

fun getEventTypeColor(
    type: SchoolEventType
): Color {
    return when (type) {
        SchoolEventType.TAREA ->
            Color(0xFF8B5CF6)

        SchoolEventType.EXAMEN ->
            Color(0xFFEF4444)

        SchoolEventType.EXPOSICION ->
            Color(0xFF06B6D4)

        SchoolEventType.EVENTO_ESCOLAR ->
            Color(0xFFF59E0B)

        SchoolEventType.REUNION ->
            Color(0xFF3B82F6)

        SchoolEventType.VACACIONES ->
            Color(0xFF10B981)

        SchoolEventType.OTRO ->
            Color(0xFF64748B)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EventItemCard(
    eventWithSubject: SchoolEventWithSubject,
    onToggleCompleted: (Boolean) -> Unit,
    onClick: () -> Unit,
    onCalendarSyncClick: () -> Unit,
    modifier: Modifier = Modifier,
    onDeleteClick: (() -> Unit)? = null,
    onToggleSubtask: (Long, Boolean) -> Unit = { _, _ -> }
) {
    val event = eventWithSubject.event
    val subject = eventWithSubject.subject
    var showDeleteConfirmation by remember(event.id) { mutableStateOf(false) }
    var subtasksExpanded by remember(event.id) { mutableStateOf(false) }

    /*
     * SchoolEventEntity.type debe ser SchoolEventType.
     */
    val eventType = event.type
    val typeColor = getEventTypeColor(eventType)

    val scheduleParts = remember(
        event.startDate,
        event.endDate,
        event.startTime,
        event.endTime,
        event.isAllDay
    ) {
        formatEventSchedule(event)
    }

    val isUrgent = remember(
        event.startDate,
        event.type,
        event.isCompleted
    ) {
        isEventUrgent(event)
    }

    val subjectColor = remember(subject?.colorHex) {
        parseColorFromHex(
            subject?.colorHex ?: "#3B82F6"
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (eventWithSubject.subtasks.isNotEmpty()) {
                        subtasksExpanded = !subtasksExpanded
                    }
                },
                onLongClick = onClick,
                onLongClickLabel = "Editar actividad"
            )
            .testTag("event_card_${event.id}"),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isUrgent) {
                typeColor.copy(alpha = 0.55f)
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        ),
        colors = CardDefaults.cardColors(
            containerColor = when {
                event.isCompleted -> MaterialTheme.colorScheme.surface

                isUrgent -> {
                    typeColor.copy(alpha = 0.07f).compositeOver(MaterialTheme.colorScheme.surface)
                }

                else -> {
                    MaterialTheme.colorScheme.surface
                }
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isUrgent) {
                3.dp
            } else {
                1.dp
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 12.dp,
                    vertical = 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = event.isCompleted,
                onCheckedChange = { newValue ->
                    onToggleCompleted(newValue)
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = typeColor,
                    uncheckedColor =
                        MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag(
                    "event_checkbox_${event.id}"
                )
            )

            Spacer(modifier = Modifier.width(6.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement =
                        Arrangement.spacedBy(6.dp)
                ) {
                    EventTypeChip(
                        type = eventType,
                        color = typeColor
                    )

                    if (subject != null) {
                        SubjectChip(
                            name = subject.name,
                            color = subjectColor,
                            modifier = Modifier.weight(
                                weight = 1f,
                                fill = false
                            )
                        )
                    }
                }

                if (event.priority != EventPriority.MEDIUM || event.recurrenceType != RecurrenceType.NONE) {
                    Spacer(modifier = Modifier.height(5.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (event.priority != EventPriority.MEDIUM) {
                            val priorityColor = if (event.priority == EventPriority.HIGH) {
                                Color(0xFFEF4444)
                            } else {
                                Color(0xFF10B981)
                            }
                            Text(
                                text = "Prioridad ${event.priority.displayName.lowercase()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = priorityColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (event.recurrenceType != RecurrenceType.NONE) {
                            Icon(
                                Icons.Default.Repeat,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = event.recurrenceType.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (event.isCompleted) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    },
                    color = if (event.isCompleted) {
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                            .copy(alpha = 0.85f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (
                    event.description.isNotBlank() &&
                    !event.isCompleted
                ) {
                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodySmall,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(7.dp))

                EventScheduleRow(
                    dateText = scheduleParts.first,
                    timeText = scheduleParts.second,
                    color = if (isUrgent) {
                        typeColor
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                if (event.location.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))

                    EventLocationRow(
                        location = event.location
                    )
                }

                if (eventWithSubject.subtasks.isNotEmpty()) {
                    val completed = eventWithSubject.subtasks.count { it.isCompleted }
                    val total = eventWithSubject.subtasks.size
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Progreso", style = MaterialTheme.typography.labelSmall)
                        Text("$completed/$total", style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    LinearProgressIndicator(
                        progress = { completed.toFloat() / total },
                        modifier = Modifier.fillMaxWidth(),
                        color = typeColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Text(
                        text = if (subtasksExpanded) "Toca para ocultar" else "Toca para ver los pasos",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 5.dp)
                    )
                    if (subtasksExpanded) {
                        Spacer(modifier = Modifier.height(6.dp))
                        eventWithSubject.subtasks.sortedBy { it.sortOrder }.forEach { subtask ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = subtask.isCompleted,
                                    onCheckedChange = { onToggleSubtask(subtask.id, it) },
                                    colors = CheckboxDefaults.colors(checkedColor = typeColor),
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = subtask.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    textDecoration = if (subtask.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                    color = if (subtask.isCompleted) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = onCalendarSyncClick,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag(
                            "sync_event_btn_${event.id}"
                        )
                ) {
                    Icon(
                        imageVector =
                            Icons.Default.CalendarMonth,
                        contentDescription =
                            "Agregar al calendario del teléfono",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(19.dp)
                    )
                }

                if (onDeleteClick != null) {
                    IconButton(
                        onClick = { showDeleteConfirmation = true },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag(
                                "delete_event_btn_${event.id}"
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar evento",
                            tint = MaterialTheme.colorScheme.error
                                .copy(alpha = 0.8f),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirmation && onDeleteClick != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("¿Eliminar evento?") },
            text = { Text("Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        onDeleteClick()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false }
                ) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun EventTypeChip(
    type: SchoolEventType,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.14f)
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 7.dp,
                vertical = 4.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = getEventTypeIcon(type),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = type.displayName.uppercase(
                    Locale("es", "MX")
                ),
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun SubjectChip(
    name: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = name,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(
                horizontal = 7.dp,
                vertical = 4.dp
            )
        )
    }
}

@Composable
private fun EventScheduleRow(
    dateText: String,
    timeText: String,
    color: Color
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Event,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )

            Spacer(modifier = Modifier.width(5.dp))

            Text(
                text = dateText,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = timeText,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EventLocationRow(
    location: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme
                .onSurfaceVariant
                .copy(alpha = 0.75f),
            modifier = Modifier.size(14.dp)
        )

        Spacer(modifier = Modifier.width(5.dp))

        Text(
            text = location,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun formatEventSchedule(
    event: SchoolEventEntity
): Pair<String, String> {
    val startDateText = formatDate(event.startDate)
    val endDateText = formatDate(event.endDate)

    val dateText = if (event.startDate == event.endDate) {
        startDateText
    } else {
        "$startDateText – $endDateText"
    }

    if (event.isAllDay) {
        return dateText to "Todo el día"
    }

    val startTimeText = formatTime(event.startTime)
    val endTimeText = formatTime(event.endTime)

    val timeText = when {
        startTimeText != null && endTimeText != null ->
            "$startTimeText – $endTimeText"

        startTimeText != null ->
            startTimeText

        else ->
            "Sin hora"
    }

    return dateText to timeText
}

private fun formatDate(
    date: String
): String {
    return DateTimeUtils.formatDate(date)
}

private fun formatTime(
    time: String?
): String? {
    if (time.isNullOrBlank()) {
        return null
    }

    return DateTimeUtils.formatTime(time, use24Hour = false)
}

private fun isEventUrgent(
    event: SchoolEventEntity
): Boolean {
    if (event.isCompleted) {
        return false
    }

    val urgentTypes = setOf(
        SchoolEventType.TAREA,
        SchoolEventType.EXAMEN,
        SchoolEventType.EXPOSICION
    )

    if (event.type !in urgentTypes) {
        return false
    }

    val today = DateTimeUtils.todayString()
    if (event.startDate <= today && event.endDate >= today) return true
    return DateTimeUtils.daysRemaining(event.startDate)?.let { it in 0..2 } ?: false
}
