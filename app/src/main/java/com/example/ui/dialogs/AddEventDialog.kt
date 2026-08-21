package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.SchoolEventEntity
import com.example.data.local.SchoolEventType
import com.example.data.local.SchoolEventWithSubject
import com.example.data.local.SubjectWithSlots
import com.example.ui.components.getEventTypeColor
import com.example.ui.components.getEventTypeIcon
import com.example.ui.components.parseColorFromHex
import com.example.ui.components.CalendarDateField
import com.example.ui.components.ClockTimeField
import com.example.ui.theme.IndigoPrimary
import com.example.ui.util.capitalizeFirstLetter
import com.example.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(
    editingEvent: SchoolEventWithSubject?,
    defaultSubjectId: Long? = null,
    defaultDate: String? = null,
    subjects: List<SubjectWithSlots>,
    onDismiss: () -> Unit,
    onSave: (SchoolEventEntity) -> Unit
) {
    val initialEvent = editingEvent?.event
    var title by remember { mutableStateOf(initialEvent?.title ?: "") }
    var selectedType by remember { mutableStateOf(initialEvent?.type ?: SchoolEventType.TAREA) }
    var selectedSubjectId by remember { mutableStateOf(initialEvent?.subjectId ?: defaultSubjectId) }
    var startDate by remember {
        mutableStateOf(initialEvent?.startDate ?: defaultDate ?: DateTimeUtils.todayString())
    }
    var endDate by remember { mutableStateOf(initialEvent?.endDate ?: startDate) }
    var isAllDay by remember { mutableStateOf(initialEvent?.isAllDay ?: false) }
    var startTime by remember { mutableStateOf(initialEvent?.startTime ?: "10:00") }
    var endTime by remember { mutableStateOf(initialEvent?.endTime ?: "11:00") }
    var location by remember { mutableStateOf(initialEvent?.location ?: "") }
    var description by remember { mutableStateOf(initialEvent?.description ?: "") }
    var reminderMinutes by remember { mutableIntStateOf(initialEvent?.reminderMinutes ?: 30) }
    var syncCalendar by remember { mutableStateOf(initialEvent?.syncCalendar ?: false) }

    var isSubjectDropdownExpanded by remember { mutableStateOf(false) }

    val quickDateChips = listOf(
        "Hoy" to 0,
        "Mañana" to 1,
        "En 3 días" to 3,
        "En 1 semana" to 7
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("add_event_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Dialog Title Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialEvent == null) "Nueva Actividad" else "Editar Actividad",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = capitalizeFirstLetter(it) },
                    label = { Text("Título de la actividad *") },
                    placeholder = { Text("Ej. Examen Parcial 2, Entrega de Ensayo") },
                    leadingIcon = { Icon(Icons.Default.Title, contentDescription = null, tint = IndigoPrimary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("event_title_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Event Type Selector Chips
                Text(
                    text = "Tipo de actividad",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(SchoolEventType.entries) { type ->
                        val isSelected = selectedType == type
                        val typeColor = getEventTypeColor(type)

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) typeColor else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable { selectedType = type }
                                .testTag("type_chip_${type.name}"),
                            tonalElevation = if (isSelected) 3.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = getEventTypeIcon(type),
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else typeColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = type.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Subject Selection Dropdown
                ExposedDropdownMenuBox(
                    expanded = isSubjectDropdownExpanded,
                    onExpandedChange = { isSubjectDropdownExpanded = !isSubjectDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val currentSubjectName = subjects.find { it.subject.id == selectedSubjectId }?.subject?.name ?: "Ninguna (General)"
                    OutlinedTextField(
                        value = currentSubjectName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Materia vinculada") },
                        leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = IndigoPrimary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isSubjectDropdownExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    )

                    ExposedDropdownMenu(
                        expanded = isSubjectDropdownExpanded,
                        onDismissRequest = { isSubjectDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Ninguna (General / Escolar)") },
                            onClick = {
                                selectedSubjectId = null
                                isSubjectDropdownExpanded = false
                            }
                        )
                        subjects.forEach { item ->
                            val color = parseColorFromHex(item.subject.colorHex)
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(item.subject.name)
                                    }
                                },
                                onClick = {
                                    selectedSubjectId = item.subject.id
                                    isSubjectDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CalendarDateField(
                        value = startDate,
                        onDateSelected = {
                            val previous = startDate
                            startDate = it
                            if (endDate == previous) endDate = it
                        },
                        label = "Fecha inicial",
                        isError = !DateTimeUtils.isValidDate(startDate),
                        modifier = Modifier.weight(1f),
                        testTag = "event_start_date_input"
                    )
                    CalendarDateField(
                        value = endDate,
                        onDateSelected = { endDate = it },
                        label = "Fecha final",
                        isError = DateTimeUtils.parseDate(endDate)?.let { end ->
                            DateTimeUtils.parseDate(startDate)?.let { end.isBefore(it) } ?: true
                        } ?: true,
                        modifier = Modifier.weight(1f),
                        testTag = "event_end_date_input"
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickDateChips) { (label, dayOffset) ->
                        val chipDate = DateTimeUtils.today().plusDays(dayOffset.toLong()).toString()
                        val isSelected = startDate == chipDate && endDate == chipDate

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) IndigoPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable {
                                startDate = chipDate
                                endDate = chipDate
                            }
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Evento de todo el día", fontWeight = FontWeight.SemiBold)
                        Text("No requiere hora de inicio ni fin", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(checked = isAllDay, onCheckedChange = { isAllDay = it })
                }

                if (!isAllDay) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ClockTimeField(
                            value = startTime,
                            onTimeSelected = {
                                startTime = it
                                endTime = oneHourAfter(it)
                            },
                            label = "Hora de inicio",
                            isError = !DateTimeUtils.isValidTime(startTime),
                            modifier = Modifier.weight(1f)
                        )
                        ClockTimeField(
                            value = endTime,
                            onTimeSelected = { endTime = it },
                            label = "Hora de fin",
                            isError = !DateTimeUtils.isValidTime(endTime) ||
                                (startDate == endDate &&
                                    !DateTimeUtils.endIsAfterStart(startTime, endTime)),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Location / Salon
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = capitalizeFirstLetter(it) },
                    label = { Text("Lugar o Aula (opcional)") },
                    placeholder = { Text("Ej. Salón B12, Zoom, Plataforma") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = IndigoPrimary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = capitalizeFirstLetter(it) },
                    label = { Text("Notas o descripción") },
                    placeholder = { Text("Temas de estudio, enlaces, criterios...") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = IndigoPrimary) },
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Reminder Selector
                Text(
                    text = "Recordatorio antes del evento",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                val reminderOptions = listOf(15 to "15 min", 30 to "30 min", 60 to "1 hora", 1440 to "1 día")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    reminderOptions.forEach { (mins, label) ->
                        val isSelected = reminderMinutes == mins
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { reminderMinutes = mins }
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Sync with Phone Calendar Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sincronizar con Google Calendar",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Crear recordatorio en tu calendario del teléfono",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = syncCalendar,
                        onCheckedChange = { syncCalendar = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = IndigoPrimary)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions: Cancel & Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onSave(
                                    SchoolEventEntity(
                                        id = initialEvent?.id ?: 0L,
                                        title = title.trim(),
                                        type = selectedType,
                                        subjectId = selectedSubjectId,
                                        startDate = startDate.trim(),
                                        endDate = endDate.trim(),
                                        startTime = if (isAllDay) null else startTime.trim(),
                                        endTime = if (isAllDay) null else endTime.trim(),
                                        isAllDay = isAllDay,
                                        location = location.trim(),
                                        description = description.trim(),
                                        reminderMinutes = reminderMinutes,
                                        isCompleted = initialEvent?.isCompleted ?: false,
                                        syncCalendar = syncCalendar,
                                        calendarEventId = initialEvent?.calendarEventId,
                                        calendarId = initialEvent?.calendarId,
                                        lastCalendarSyncMillis = initialEvent?.lastCalendarSyncMillis,
                                        createdAtMillis = initialEvent?.createdAtMillis ?: System.currentTimeMillis(),
                                        updatedAtMillis = initialEvent?.updatedAtMillis ?: System.currentTimeMillis()
                                    )
                                )
                            }
                        },
                        enabled = title.isNotBlank() &&
                            DateTimeUtils.parseDate(startDate) != null &&
                            DateTimeUtils.parseDate(endDate)?.let { end ->
                                DateTimeUtils.parseDate(startDate)?.let { !end.isBefore(it) } ?: false
                            } == true &&
                            (isAllDay || (
                                DateTimeUtils.isValidTime(startTime) &&
                                    DateTimeUtils.isValidTime(endTime) &&
                                    (startDate != endDate || DateTimeUtils.endIsAfterStart(startTime, endTime))
                                )),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_event_button")
                    ) {
                        Text("Guardar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
