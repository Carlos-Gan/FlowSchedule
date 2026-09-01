package com.mocas.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mocas.data.local.AcademicPeriodEntity
import com.mocas.data.local.ScheduleSlotEntity
import com.mocas.data.local.SubjectEntity
import com.mocas.data.local.SubjectWithSlots
import com.mocas.ui.add.subject.GeneralDetailsCard
import com.mocas.ui.add.subject.PersonalizationCard
import com.mocas.ui.add.subject.SchedulesCard
import com.mocas.ui.add.subject.SessionTimePickerDialog
import androidx.compose.material3.MaterialTheme
import com.mocas.util.DateTimeUtils
import com.mocas.util.Variants
import java.util.Locale

// --- Clases de Datos ---
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSubjectScreen(
    editingSubject: SubjectWithSlots?,
    academicPeriods: List<AcademicPeriodEntity>,
    existingSubjects: List<SubjectWithSlots>,
    onBack: () -> Unit,
    onSavePeriod: (AcademicPeriodEntity) -> Unit,
    onSave: (SubjectEntity, List<ScheduleSlotEntity>) -> Unit
) {
    val initialSub = editingSubject?.subject

    var name by remember { mutableStateOf(initialSub?.name ?: "") }
    var code by remember { mutableStateOf(initialSub?.code ?: "") }
    var professor by remember { mutableStateOf(initialSub?.professor ?: "") }
    var defaultRoom by remember { mutableStateOf(initialSub?.defaultRoom ?: "") }
    var colorHex by remember { mutableStateOf(initialSub?.colorHex ?: "#3B82F6") }
    var organizationTag by remember { mutableStateOf(initialSub?.organizationTag ?: "UNIVERSIDAD") }
    var isImportant by remember { mutableStateOf(initialSub?.isImportant ?: false) }

    val suggestedPeriod = remember(academicPeriods) {
        val today = DateTimeUtils.today()
        academicPeriods.firstOrNull { period ->
            val start = DateTimeUtils.parseDate(period.startDate)
            val end = DateTimeUtils.parseDate(period.endDate)
            start != null && end != null && today in start..end
        } ?: academicPeriods.firstOrNull()
    }

    var semesterStart by remember { mutableStateOf(initialSub?.semesterStart ?: suggestedPeriod?.startDate ?: DateTimeUtils.todayString()) }
    var semesterEnd by remember { mutableStateOf(initialSub?.semesterEnd ?: suggestedPeriod?.endDate ?: DateTimeUtils.today().plusMonths(4).toString()) }

    var timePickerTarget by remember { mutableStateOf<TimePickerTarget?>(null) }

    val slotsList = remember {
        mutableStateListOf<SlotDraft>().apply {
            if (editingSubject?.slots.isNullOrEmpty()) {
                add(SlotDraft())
            } else {
                editingSubject.slots.groupBy { Triple(it.startTime, it.endTime, it.room) }.values.forEach { groupedSlots ->
                    val firstSlot = groupedSlots.first()
                    add(SlotDraft(
                        idsByDay = groupedSlots.associate { it.dayOfWeek to it.id },
                        selectedDays = groupedSlots.mapTo(sortedSetOf()) { it.dayOfWeek },
                        startTime = firstSlot.startTime,
                        endTime = firstSlot.endTime,
                        room = firstSlot.room
                    ))
                }
            }
        }
    }

    val slotConflicts = detectScheduleConflicts(slotsList, existingSubjects, initialSub?.id, semesterStart, semesterEnd)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (initialSub == null) "Nueva Materia" else "Editar Materia",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    val isValid = name.isNotBlank() && slotsList.isNotEmpty() &&
                            DateTimeUtils.isValidDate(semesterStart) && DateTimeUtils.isValidDate(semesterEnd) &&
                            slotsList.all { it.selectedDays.isNotEmpty() } &&
                            slotsList.all { DateTimeUtils.endIsAfterStart(it.startTime, it.endTime) } &&
                            slotConflicts.isEmpty()

                    Button(
                        onClick = {
                            if (isValid) {
                                val subject = SubjectEntity(
                                    id = initialSub?.id ?: 0L,
                                    name = name.trim(),
                                    code = code.trim(),
                                    professor = professor.trim(),
                                    defaultRoom = defaultRoom.trim(),
                                    colorHex = colorHex,
                                    organizationTag = organizationTag,
                                    isImportant = isImportant,
                                    semesterStart = semesterStart.trim(),
                                    semesterEnd = semesterEnd.trim(),
                                    reminderMinutesBefore = initialSub?.reminderMinutesBefore ?: 15,
                                    syncCalendar = initialSub?.syncCalendar ?: false,
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
                        enabled = isValid,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Text("Guardar", fontWeight = FontWeight.Medium)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            GeneralDetailsCard(
                name = name, onNameChange = { name = it },
                code = code, onCodeChange = { code = it },
                professor = professor, onProfessorChange = { professor = it },
                defaultRoom = defaultRoom, onRoomChange = { defaultRoom = it },
                semesterStart = semesterStart, onStartChange = { semesterStart = it },
                semesterEnd = semesterEnd, onEndChange = { semesterEnd = it },
                academicPeriods = academicPeriods,
                onSavePeriod = onSavePeriod
            )

            PersonalizationCard(
                colorHex = colorHex, onColorChange = { colorHex = it },
                organizationTag = organizationTag, onTagChange = { organizationTag = it },
                isImportant = isImportant, onImportantChange = { isImportant = it }
            )

            SchedulesCard(
                slotsList = slotsList,
                slotConflicts = slotConflicts,
                defaultRoom = defaultRoom,
                onTimeClick = { index, isStart -> timePickerTarget = TimePickerTarget(index, isStart) },
                onAddSlot = { slotsList.add(SlotDraft(selectedDays = setOf(1), startTime = "10:00", endTime = "11:00")) },
                onRemoveSlot = { index -> slotsList.removeAt(index) },
                onUpdateSlot = { index, newDraft -> slotsList[index] = newDraft }
            )
        }
    }

    timePickerTarget?.let { target ->
        val draft = slotsList.getOrNull(target.slotIndex)
        if (draft != null) {
            SessionTimePickerDialog(
                title = if (target.isStartTime) "Hora de inicio" else "Hora de fin",
                initialTime = if (target.isStartTime) draft.startTime else draft.endTime,
                onDismiss = { timePickerTarget = null },
                onTimeSelected = { selectedTime ->
                    val currentDraft = slotsList.getOrNull(target.slotIndex)
                    if (currentDraft != null) {
                        slotsList[target.slotIndex] = if (target.isStartTime) {
                            currentDraft.copy(startTime = selectedTime, endTime = oneHourAfter(selectedTime))
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


// --- Funciones Auxiliares ---

fun parseTime(value: String): Pair<Int, Int> {
    val parts = value.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 8
    val minute = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
    return hour to minute
}

fun formatTime(hour: Int, minute: Int): String =
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