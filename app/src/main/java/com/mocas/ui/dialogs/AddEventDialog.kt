package com.mocas.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mocas.data.local.SchoolEventEntity
import com.mocas.data.local.SchoolEventType
import com.mocas.data.local.SchoolEventWithSubject
import com.mocas.data.local.SubjectWithSlots
import com.mocas.data.local.SubtaskEntity
import com.mocas.data.local.EventPriority
import com.mocas.data.local.RecurrenceType
import com.mocas.ui.components.CalendarDateField
import com.mocas.ui.components.ClockTimeField
import com.mocas.ui.components.getEventTypeIcon
import com.mocas.ui.components.parseColorFromHex
import com.mocas.ui.util.capitalizeFirstLetter
import com.mocas.util.DateTimeUtils
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(
    editingEvent: SchoolEventWithSubject?,
    defaultSubjectId: Long? = null,
    defaultDate: String? = null,
    defaultType: SchoolEventType? = null,
    defaultTitle: String? = null,
    subjects: List<SubjectWithSlots>,
    onDismiss: () -> Unit,
    onSave: (SchoolEventEntity, List<SubtaskEntity>) -> Unit
) {
    val initialEvent = editingEvent?.event
    var title by remember { mutableStateOf(initialEvent?.title ?: defaultTitle.orEmpty()) }
    var selectedType by remember {
        mutableStateOf(initialEvent?.type ?: defaultType ?: SchoolEventType.TAREA)
    }
    var selectedSubjectId by remember { mutableStateOf(initialEvent?.subjectId ?: defaultSubjectId) }
    var eventDate by remember {
        mutableStateOf(initialEvent?.startDate ?: defaultDate ?: DateTimeUtils.todayString())
    }
    var isAllDay by remember {
        mutableStateOf(initialEvent?.isAllDay ?: (defaultType == SchoolEventType.VACACIONES))
    }
    var eventTime by remember { mutableStateOf(initialEvent?.startTime ?: "09:00") }
    var location by remember { mutableStateOf(initialEvent?.location ?: "") }
    var description by remember { mutableStateOf(initialEvent?.description ?: "") }

    var isSubjectDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(28.dp))
                .testTag("add_event_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (initialEvent == null) "Nueva Actividad" else "Editar Actividad",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 26.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Organiza tu semestre de forma inteligente.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        onClick = onDismiss,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Close, contentDescription = "Cerrar", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Activity Title - Estilo Moderno
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = capitalizeFirstLetter(it) },
                    placeholder = { Text("¿Qué tienes que hacer?", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.02f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Activity Type - Usando SegmentedButton Row
                Text(
                    text = "CATEGORÍA",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    val types = listOf(
                        SchoolEventType.TAREA to "Tarea",
                        SchoolEventType.EXAMEN to "Examen",
                        SchoolEventType.EXPOSICION to "Expo",
                        SchoolEventType.EVENTO_ESCOLAR to "Evento"
                    )
                    types.forEachIndexed { index, (type, label) ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = types.size),
                            onClick = { selectedType = type },
                            selected = selectedType == type,
                            label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = MaterialTheme.colorScheme.primary,
                                activeContentColor = MaterialTheme.colorScheme.onPrimary,
                                inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(24.dp))

                // Subject Selection
                Text(
                    text = "ASIGNATURA",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                ExposedDropdownMenuBox(
                    expanded = isSubjectDropdownExpanded,
                    onExpandedChange = { isSubjectDropdownExpanded = !isSubjectDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val currentSubjectName = subjects.find { it.subject.id == selectedSubjectId }?.subject?.name ?: "Seleccionar materia..."
                    Surface(
                        modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        onClick = { isSubjectDropdownExpanded = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = currentSubjectName,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (selectedSubjectId == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (isSubjectDropdownExpanded) Icons.Outlined.Schedule else Icons.Outlined.School, // Reemplazo visual
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

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
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(parseColorFromHex(item.subject.colorHex)))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(item.subject.name, fontWeight = FontWeight.Medium)
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

                Spacer(modifier = Modifier.height(28.dp))

                // DateTime Block - Estilo Card Elevado sutil
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Outlined.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Todo el día", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp)
                                    Text("Sin hora específica", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Switch(
                                checked = isAllDay,
                                onCheckedChange = { isAllDay = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                                    uncheckedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1.5f),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                CalendarDateField(
                                    value = eventDate,
                                    onDateSelected = { eventDate = it },
                                    label = "Fecha",
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                            
                            if (!isAllDay) {
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    ClockTimeField(
                                        value = eventTime,
                                        onTimeSelected = { eventTime = it },
                                        label = "Hora",
                                        modifier = Modifier.padding(4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Location
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = capitalizeFirstLetter(it) },
                    placeholder = { Text("Ubicación o salón (Opcional)", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                    leadingIcon = { Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Notes
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = capitalizeFirstLetter(it) },
                    placeholder = { Text("Notas adicionales...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                    leadingIcon = { Icon(Icons.Outlined.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)) },
                    minLines = 3,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Action Buttons
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onSave(
                                SchoolEventEntity(
                                    id = initialEvent?.id ?: 0L,
                                    title = title.trim(),
                                    type = selectedType,
                                    subjectId = selectedSubjectId,
                                    startDate = eventDate,
                                    endDate = eventDate,
                                    startTime = if (isAllDay) null else eventTime,
                                    endTime = if (isAllDay) null else eventTime,
                                    isAllDay = isAllDay,
                                    location = location.trim(),
                                    description = description.trim(),
                                    organizationTag = initialEvent?.organizationTag ?: "UNIVERSIDAD",
                                    isImportant = initialEvent?.isImportant ?: false,
                                    priority = initialEvent?.priority ?: EventPriority.MEDIUM,
                                    recurrenceType = initialEvent?.recurrenceType ?: RecurrenceType.NONE,
                                    isCompleted = initialEvent?.isCompleted ?: false,
                                    syncCalendar = initialEvent?.syncCalendar ?: false,
                                    createdAtMillis = initialEvent?.createdAtMillis ?: System.currentTimeMillis(),
                                    updatedAtMillis = initialEvent?.updatedAtMillis ?: System.currentTimeMillis()
                                ),
                                emptyList()
                            )
                        }
                    },
                    enabled = title.isNotBlank(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("GUARDAR ACTIVIDAD", fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 1.sp)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
