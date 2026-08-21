package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.local.ScheduleSlotEntity
import com.example.data.local.AcademicPeriodEntity
import com.example.data.local.SubjectEntity
import com.example.data.local.SubjectWithSlots
import com.example.ui.components.SubjectColorPicker
import com.example.ui.components.CalendarDateField
import com.example.ui.theme.IndigoPrimary
import com.example.ui.util.capitalizeFirstLetter
import com.example.util.DateTimeUtils
import java.util.Locale

data class SlotDraft(
    val idsByDay: Map<Int, Long> = emptyMap(),
    val selectedDays: Set<Int> = setOf(1),
    val startTime: String = "08:00",
    val endTime: String = "09:00",
    val room: String = ""
)

private data class TimePickerTarget(
    val slotIndex: Int,
    val isStartTime: Boolean
)

@Composable
fun AddEditSubjectDialog(
    editingSubject: SubjectWithSlots?,
    academicPeriods: List<AcademicPeriodEntity>,
    existingSubjects: List<SubjectWithSlots>,
    onDismiss: () -> Unit,
    onSavePeriod: (AcademicPeriodEntity) -> Unit,
    onSave: (SubjectEntity, List<ScheduleSlotEntity>) -> Unit
) {
    val initialSub = editingSubject?.subject
    var name by remember { mutableStateOf(initialSub?.name ?: "") }
    var code by remember { mutableStateOf(initialSub?.code ?: "") }
    var professor by remember { mutableStateOf(initialSub?.professor ?: "") }
    var defaultRoom by remember { mutableStateOf(initialSub?.defaultRoom ?: "") }
    var colorHex by remember { mutableStateOf(initialSub?.colorHex ?: "#3B82F6") }
    var reminderMinutes by remember { mutableIntStateOf(initialSub?.reminderMinutesBefore ?: 15) }
    var syncCalendar by remember { mutableStateOf(initialSub?.syncCalendar ?: false) }
    val suggestedPeriod = remember(academicPeriods) {
        val today = DateTimeUtils.today()
        academicPeriods.firstOrNull { period ->
            val start = DateTimeUtils.parseDate(period.startDate)
            val end = DateTimeUtils.parseDate(period.endDate)
            start != null && end != null && today in start..end
        } ?: academicPeriods.firstOrNull()
    }
    var semesterStart by remember {
        mutableStateOf(
            initialSub?.semesterStart ?: suggestedPeriod?.startDate ?: DateTimeUtils.todayString()
        )
    }
    var semesterEnd by remember {
        mutableStateOf(
            initialSub?.semesterEnd ?: suggestedPeriod?.endDate
            ?: DateTimeUtils.today().plusMonths(4).toString()
        )
    }
    var timePickerTarget by remember { mutableStateOf<TimePickerTarget?>(null) }

    val slotsList = remember {
        mutableStateListOf<SlotDraft>().apply {
            if (editingSubject?.slots.isNullOrEmpty()) {
                add(SlotDraft())
            } else {
                editingSubject.slots
                    .groupBy { Triple(it.startTime, it.endTime, it.room) }
                    .values
                    .forEach { groupedSlots ->
                    val firstSlot = groupedSlots.first()
                    add(
                        SlotDraft(
                            idsByDay = groupedSlots.associate { it.dayOfWeek to it.id },
                            selectedDays = groupedSlots.mapTo(sortedSetOf()) { it.dayOfWeek },
                            startTime = firstSlot.startTime,
                            endTime = firstSlot.endTime,
                            room = firstSlot.room
                        )
                    )
                }
            }
        }
    }

    val daysOptions = listOf(
        1 to "Lun",
        2 to "Mar",
        3 to "Mié",
        4 to "Jue",
        5 to "Vie",
        6 to "Sáb",
        7 to "Dom"
    )
    val slotConflicts = detectScheduleConflicts(
        drafts = slotsList,
        existingSubjects = existingSubjects,
        excludedSubjectId = initialSub?.id,
        periodStart = semesterStart,
        periodEnd = semesterEnd
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("add_subject_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialSub == null) "Nueva Materia" else "Editar Materia",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Periodo académico",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Selecciona uno guardado o define fechas exactas",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            onSavePeriod(
                                AcademicPeriodEntity(
                                    name = buildPeriodName(semesterStart, semesterEnd),
                                    startDate = semesterStart,
                                    endDate = semesterEnd
                                )
                            )
                        },
                        enabled = DateTimeUtils.isValidDate(semesterStart) &&
                            DateTimeUtils.parseDate(semesterEnd)?.let { end ->
                                DateTimeUtils.parseDate(semesterStart)?.let { start ->
                                    !end.isBefore(start)
                                } ?: false
                            } ?: false,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Guardar periodo", fontSize = 11.sp)
                    }
                }

                if (academicPeriods.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(academicPeriods, key = { it.id }) { period ->
                            val selected = period.startDate == semesterStart &&
                                period.endDate == semesterEnd
                            Surface(
                                modifier = Modifier.clickable {
                                    semesterStart = period.startDate
                                    semesterEnd = period.endDate
                                },
                                shape = RoundedCornerShape(14.dp),
                                color = if (selected) IndigoPrimary
                                else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(
                                    1.dp,
                                    if (selected) IndigoPrimary
                                    else MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Text(
                                    text = period.name,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    color = if (selected) Color.White
                                    else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CalendarDateField(
                        value = semesterStart,
                        onDateSelected = { semesterStart = it },
                        label = "Inicio del periodo",
                        isError = !DateTimeUtils.isValidDate(semesterStart),
                        modifier = Modifier.weight(1f)
                    )
                    CalendarDateField(
                        value = semesterEnd,
                        onDateSelected = { semesterEnd = it },
                        label = "Fin del periodo",
                        isError = DateTimeUtils.parseDate(semesterEnd)?.let { end ->
                            DateTimeUtils.parseDate(semesterStart)?.let { end.isBefore(it) } ?: true
                        } ?: true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = capitalizeFirstLetter(it) },
                    label = { Text("Nombre de la materia *") },
                    placeholder = { Text("Ej. Desarrollo Móvil, Redes, Álgebra") },
                    leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = IndigoPrimary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subject_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Code and Room in one row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it.uppercase(Locale.ROOT) },
                        label = { Text("Código / Siglas") },
                        placeholder = { Text("DAM-501") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = defaultRoom,
                        onValueChange = { defaultRoom = capitalizeFirstLetter(it) },
                        label = { Text("Salón principal") },
                        placeholder = { Text("Aula B12") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = IndigoPrimary) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Professor
                OutlinedTextField(
                    value = professor,
                    onValueChange = { professor = capitalizeFirstLetter(it) },
                    label = { Text("Profesor / Docente") },
                    placeholder = { Text("Ing. Roberto Ramos") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = IndigoPrimary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Color Picker
                SubjectColorPicker(
                    selectedHex = colorHex,
                    onColorSelected = { colorHex = it }
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Schedule Slots Builder Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Horarios de clase (${slotsList.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Button(
                        onClick = {
                            slotsList.add(
                                SlotDraft(
                                    selectedDays = setOf(1),
                                    startTime = "10:00",
                                    endTime = "11:00"
                                )
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                        modifier = Modifier.testTag("add_slot_draft_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Horario", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Render each slot card
                slotsList.forEachIndexed { index, slotDraft ->
                    val conflicts = slotConflicts[index].orEmpty()
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = if (conflicts.isNotEmpty()) {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                        } else {
                            null
                        },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Sesión ${index + 1}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = IndigoPrimary
                                )
                                if (slotsList.size > 1) {
                                    IconButton(
                                        onClick = { slotsList.removeAt(index) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar horario",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Day selector chips for this slot
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(daysOptions) { (dayNum, dayLabel) ->
                                    val isSelected = dayNum in slotDraft.selectedDays
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.clickable {
                                            val updatedDays = if (isSelected) {
                                                if (slotDraft.selectedDays.size == 1) {
                                                    slotDraft.selectedDays
                                                } else {
                                                    slotDraft.selectedDays - dayNum
                                                }
                                            } else {
                                                slotDraft.selectedDays + dayNum
                                            }
                                            slotsList[index] = slotDraft.copy(selectedDays = updatedDays)
                                        }
                                    ) {
                                        Text(
                                            text = dayLabel,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Start & End Time
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SessionTimeField(
                                    label = "Inicio",
                                    time = slotDraft.startTime,
                                    onClick = {
                                        timePickerTarget = TimePickerTarget(
                                            slotIndex = index,
                                            isStartTime = true
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )

                                SessionTimeField(
                                    label = "Fin",
                                    time = slotDraft.endTime,
                                    isError = !DateTimeUtils.endIsAfterStart(
                                        slotDraft.startTime,
                                        slotDraft.endTime
                                    ),
                                    onClick = {
                                        timePickerTarget = TimePickerTarget(
                                            slotIndex = index,
                                            isStartTime = false
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (!DateTimeUtils.endIsAfterStart(
                                    slotDraft.startTime,
                                    slotDraft.endTime
                                )
                            ) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "La hora de fin debe ser posterior a la de inicio.",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = slotDraft.room,
                                onValueChange = {
                                    slotsList[index] = slotDraft.copy(
                                        room = capitalizeFirstLetter(it)
                                    )
                                },
                                label = { Text("Salón de esta sesión") },
                                placeholder = { Text(defaultRoom.ifBlank { "Usar salón principal" }) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            conflicts.forEach { conflict ->
                                Spacer(modifier = Modifier.height(5.dp))
                                Text(
                                    text = conflict,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Sync to calendar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Recordatorios de clases",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Avisar $reminderMinutes min antes de cada clase",
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
                            if (name.isNotBlank()) {
                                val subject = SubjectEntity(
                                    id = initialSub?.id ?: 0L,
                                    name = name.trim(),
                                    code = code.trim(),
                                    professor = professor.trim(),
                                    defaultRoom = defaultRoom.trim(),
                                    colorHex = colorHex,
                                    semesterStart = semesterStart.trim(),
                                    semesterEnd = semesterEnd.trim(),
                                    reminderMinutesBefore = reminderMinutes,
                                    syncCalendar = syncCalendar,
                                    createdAtMillis = initialSub?.createdAtMillis ?: System.currentTimeMillis(),
                                    updatedAtMillis = initialSub?.updatedAtMillis ?: System.currentTimeMillis()
                                )
                                val slots = slotsList.flatMap { draft ->
                                    draft.selectedDays.sorted().map { dayOfWeek ->
                                        ScheduleSlotEntity(
                                            id = draft.idsByDay[dayOfWeek] ?: 0L,
                                            subjectId = subject.id,
                                            dayOfWeek = dayOfWeek,
                                            startTime = draft.startTime.trim(),
                                            endTime = draft.endTime.trim(),
                                            room = draft.room.trim().ifBlank { defaultRoom.trim() }
                                        )
                                    }
                                }
                                onSave(subject, slots)
                            }
                        },
                        enabled = name.isNotBlank() &&
                            slotsList.isNotEmpty() &&
                            DateTimeUtils.isValidDate(semesterStart) &&
                            DateTimeUtils.isValidDate(semesterEnd) &&
                            slotsList.all { it.selectedDays.isNotEmpty() } &&
                            slotsList.all { DateTimeUtils.endIsAfterStart(it.startTime, it.endTime) } &&
                            slotConflicts.isEmpty(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_subject_button")
                    ) {
                        Text("Guardar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    timePickerTarget?.let { target ->
        val draft = slotsList.getOrNull(target.slotIndex)
        if (draft != null) {
            SessionTimePickerDialog(
                title = if (target.isStartTime) {
                    "Hora de inicio"
                } else {
                    "Hora de fin"
                },
                initialTime = if (target.isStartTime) {
                    draft.startTime
                } else {
                    draft.endTime
                },
                onDismiss = { timePickerTarget = null },
                onTimeSelected = { selectedTime ->
                    val currentDraft = slotsList.getOrNull(target.slotIndex)
                    if (currentDraft != null) {
                        slotsList[target.slotIndex] = if (target.isStartTime) {
                            currentDraft.copy(
                                startTime = selectedTime,
                                endTime = oneHourAfter(selectedTime)
                            )
                        } else {
                            currentDraft.copy(endTime = selectedTime)
                        }
                    }
                    timePickerTarget = null
                }
            )
        }
    }
}

@Composable
private fun SessionTimeField(
    label: String,
    time: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    val borderColor = if (isError) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.outline
    }

    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Text(
                    text = DateTimeUtils.formatTime(time, use24Hour = false) ?: time,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = "Elegir $label",
                tint = if (isError) MaterialTheme.colorScheme.error else IndigoPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionTimePickerDialog(
    title: String,
    initialTime: String,
    onDismiss: () -> Unit,
    onTimeSelected: (String) -> Unit
) {
    val (initialHour, initialMinute) = parseTime(initialTime)
    val pickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title, fontWeight = FontWeight.Bold)
        },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TimePicker(state = pickerState)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeSelected(formatTime(pickerState.hour, pickerState.minute))
                }
            ) {
                Text("Aceptar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

private fun parseTime(value: String): Pair<Int, Int> {
    val parts = value.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 8
    val minute = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
    return hour to minute
}

private fun formatTime(hour: Int, minute: Int): String =
    String.format(Locale.ROOT, "%02d:%02d", hour, minute)

internal fun oneHourAfter(startTime: String): String {
    val (hour, minute) = parseTime(startTime)
    val endMinutes = (hour * 60 + minute + 60).coerceAtMost(23 * 60 + 59)
    return formatTime(endMinutes / 60, endMinutes % 60)
}

internal fun buildPeriodName(startDate: String, endDate: String): String {
    val start = DateTimeUtils.parseDate(startDate) ?: return "Periodo académico"
    val end = DateTimeUtils.parseDate(endDate) ?: return "Periodo académico"
    val months = listOf(
        "Ene", "Feb", "Mar", "Abr", "May", "Jun",
        "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
    )
    return "${months[start.monthValue - 1]} ${start.year} – " +
        "${months[end.monthValue - 1]} ${end.year}"
}

internal fun detectScheduleConflicts(
    drafts: List<SlotDraft>,
    existingSubjects: List<SubjectWithSlots>,
    excludedSubjectId: Long?,
    periodStart: String,
    periodEnd: String
): Map<Int, List<String>> {
    val conflicts = mutableMapOf<Int, MutableList<String>>()

    drafts.forEachIndexed { firstIndex, first ->
        drafts.drop(firstIndex + 1).forEachIndexed { offset, second ->
            val secondIndex = firstIndex + offset + 1
            if (!timeRangesOverlap(first.startTime, first.endTime, second.startTime, second.endTime)) {
                return@forEachIndexed
            }
            val sharedDays = first.selectedDays intersect second.selectedDays
            sharedDays.forEach { day ->
                conflicts.getOrPut(firstIndex) { mutableListOf() }.add(
                    "Se cruza con la sesión ${secondIndex + 1} el ${shortDayName(day)}."
                )
                conflicts.getOrPut(secondIndex) { mutableListOf() }.add(
                    "Se cruza con la sesión ${firstIndex + 1} el ${shortDayName(day)}."
                )
            }
        }
    }

    val newStart = DateTimeUtils.parseDate(periodStart)
    val newEnd = DateTimeUtils.parseDate(periodEnd)
    if (newStart != null && newEnd != null) {
        existingSubjects
            .filter { it.subject.id != excludedSubjectId }
            .filter { existing ->
                val existingStart = DateTimeUtils.parseDate(existing.subject.semesterStart)
                val existingEnd = DateTimeUtils.parseDate(existing.subject.semesterEnd)
                existingStart != null && existingEnd != null &&
                    !existingEnd.isBefore(newStart) && !newEnd.isBefore(existingStart)
            }
            .forEach { existing ->
                drafts.forEachIndexed { draftIndex, draft ->
                    existing.slots.forEach { savedSlot ->
                        if (
                            savedSlot.dayOfWeek in draft.selectedDays &&
                            timeRangesOverlap(
                                draft.startTime,
                                draft.endTime,
                                savedSlot.startTime,
                                savedSlot.endTime
                            )
                        ) {
                            conflicts.getOrPut(draftIndex) { mutableListOf() }.add(
                                "Se cruza con ${existing.subject.name} el " +
                                    "${shortDayName(savedSlot.dayOfWeek)} " +
                                    "(${savedSlot.startTime}–${savedSlot.endTime})."
                            )
                        }
                    }
                }
            }
    }

    return conflicts.mapValues { (_, messages) -> messages.distinct() }
}

private fun timeRangesOverlap(
    firstStart: String,
    firstEnd: String,
    secondStart: String,
    secondEnd: String
): Boolean {
    if (
        !DateTimeUtils.isValidTime(firstStart) ||
        !DateTimeUtils.isValidTime(firstEnd) ||
        !DateTimeUtils.isValidTime(secondStart) ||
        !DateTimeUtils.isValidTime(secondEnd)
    ) {
        return false
    }
    return firstStart < secondEnd && firstEnd > secondStart
}

private fun shortDayName(day: Int): String = when (day) {
    1 -> "lunes"
    2 -> "martes"
    3 -> "miércoles"
    4 -> "jueves"
    5 -> "viernes"
    6 -> "sábado"
    else -> "domingo"
}
