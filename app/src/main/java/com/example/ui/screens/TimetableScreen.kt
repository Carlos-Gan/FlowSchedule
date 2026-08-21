package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ScheduleSlotEntity
import com.example.data.local.SubjectEntity
import com.example.data.local.SubjectWithSlots
import com.example.data.repository.CalendarSyncHelper
import com.example.ui.components.ClassScheduleCard
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.parseColorFromHex
import com.example.ui.model.TimetableDisplayMode
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.TurquoiseSecondary
import com.example.ui.util.showCalendarResult
import com.example.ui.viewmodel.ScheduleViewModel
import com.example.util.DateTimeUtils
import java.util.Calendar

@Composable
fun TimetableScreen(
    viewModel: ScheduleViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentMode by viewModel.timetableMode.collectAsStateWithLifecycle()
    val subjectsWithSlots by viewModel.subjectsWithSlots.collectAsStateWithLifecycle()
    val selectedDay by viewModel.selectedDayOfWeek.collectAsStateWithLifecycle()
    val activeSubjectsWithSlots = remember(subjectsWithSlots) {
        val today = DateTimeUtils.today()
        subjectsWithSlots.filter { item ->
            val start = DateTimeUtils.parseDate(item.subject.semesterStart)
            val end = DateTimeUtils.parseDate(item.subject.semesterEnd)
            start != null && end != null && today in start..end
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Bento Segmented Bar: Vista Semanal | Vista Diaria | Lista de Materias
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimetableDisplayMode.entries.forEach { mode ->
                    val isSelected = currentMode == mode
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.setTimetableMode(mode) }
                            .testTag("timetable_mode_${mode.name.lowercase()}"),
                        tonalElevation = if (isSelected) 3.dp else 0.dp
                    ) {
                        Text(
                            text = mode.displayName,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }

        when (currentMode) {
            TimetableDisplayMode.SEMANAL -> {
                WeeklyGridView(
                    subjectsWithSlots = activeSubjectsWithSlots,
                    onClassClick = { subjectId -> viewModel.openSubjectDetail(subjectId) },
                    onAddClassClick = { viewModel.openAddSubject() }
                )
            }
            TimetableDisplayMode.DIARIA -> {
                DailyScheduleView(
                    viewModel = viewModel,
                    selectedDay = selectedDay,
                    onDaySelected = { viewModel.setSelectedDayOfWeek(it) },
                    onClassClick = { subjectId -> viewModel.openSubjectDetail(subjectId) },
                    onSyncClass = { sub, slot ->
                        showCalendarResult(
                            context,
                            CalendarSyncHelper.addClassToPhoneCalendar(context, sub, slot)
                        )
                    }
                )
            }
            TimetableDisplayMode.MATERIAS -> {
                SubjectListView(
                    subjectsWithSlots = subjectsWithSlots,
                    onSubjectClick = { subjectId -> viewModel.openSubjectDetail(subjectId) },
                    onAddNewSubject = { viewModel.openAddSubject() }
                )
            }
        }
    }
}

@Composable
fun WeeklyGridView(
    subjectsWithSlots: List<SubjectWithSlots>,
    onClassClick: (Long) -> Unit,
    onAddClassClick: () -> Unit
) {
    if (subjectsWithSlots.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            EmptyStateCard(
                title = "Tu horario semanal está vacío",
                message = "Agrega tus materias o importa una foto de tu horario escolar.",
                icon = Icons.Default.School,
                actionButtonText = "+ Agregar Materia",
                onActionClick = onAddClassClick
            )
        }
        return
    }

    val days = listOf(
        1 to "LUN",
        2 to "MAR",
        3 to "MIÉ",
        4 to "JUE",
        5 to "VIE",
        6 to "SÁB",
        7 to "DOM"
    )

    val allSlots = subjectsWithSlots.flatMap { it.slots }
    val earliestStart = allSlots
        .minOfOrNull { ScheduleViewModel.parseTimeToMinutes(it.startTime) }
        ?: 7 * 60
    val latestEnd = allSlots
        .maxOfOrNull { ScheduleViewModel.parseTimeToMinutes(it.endTime) }
        ?: 21 * 60
    val gridStartHour = minOf(7, earliestStart / 60).coerceIn(0, 23)
    val gridEndHour = maxOf(21, (latestEnd + 59) / 60)
        .coerceIn(gridStartHour + 1, 24)
    val hours = (gridStartHour until gridEndHour).toList()
    val hourHeight = 78.dp
    val timeColWidth = 52.dp
    val dayColWidth = 128.dp

    val hScrollState = rememberScrollState()
    val vScrollState = rememberScrollState()

    val currentDayOfWeek = ScheduleViewModel.getCurrentDayOfWeekNumber()
    val cal = Calendar.getInstance()
    val nowMins = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Sticky Header: Days row Bento Style
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(start = timeColWidth)
                .horizontalScroll(hScrollState)
        ) {
            days.forEach { (dayNum, label) ->
                val isToday = dayNum == currentDayOfWeek
                Box(
                    modifier = Modifier
                        .width(dayColWidth)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isToday) IndigoPrimary else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(
                            1.dp,
                            if (isToday) IndigoPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isToday) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }

        // Scrollable Grid (Horizontal & Vertical synced)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(vScrollState)
        ) {
            // Time Labels Column
            Column(
                modifier = Modifier
                    .width(timeColWidth)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            ) {
                hours.forEach { hour ->
                    Box(
                        modifier = Modifier
                            .height(hourHeight)
                            .fillMaxWidth()
                            .padding(end = 6.dp, top = 4.dp),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Text(
                            text = String.format(java.util.Locale.ROOT, "%02d:00", hour),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Grid Days columns & Class Blocks
            Row(
                modifier = Modifier
                    .horizontalScroll(hScrollState)
            ) {
                days.forEach { (dayNum, _) ->
                    val isToday = dayNum == currentDayOfWeek

                    Box(
                        modifier = Modifier
                            .width(dayColWidth)
                            .height(hourHeight * hours.size)
                            .background(
                                if (isToday) IndigoPrimary.copy(alpha = 0.03f) else Color.Transparent
                            )
                            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        // Hour horizontal divider lines
                        hours.forEachIndexed { index, _ ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .offset(y = hourHeight * index)
                                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            )
                        }

                        // Render Slots for this day
                        for (subWithSlots in subjectsWithSlots) {
                            for (slot in subWithSlots.slots) {
                                if (slot.dayOfWeek == dayNum) {
                                    val startM = ScheduleViewModel.parseTimeToMinutes(slot.startTime)
                                    val endM = ScheduleViewModel.parseTimeToMinutes(slot.endTime)
                                    val duration = (endM - startM).coerceAtLeast(30)

                                    val topOffsetMins = (startM - gridStartHour * 60)
                                        .coerceAtLeast(0)
                                    val topOffsetDp = (topOffsetMins.toFloat() / 60f) * hourHeight.value
                                    val heightDp = (duration.toFloat() / 60f) * hourHeight.value
                                    val compactBlock = duration < 60
                                    val showRoom = duration >= 50

                                    val isNow = isToday && nowMins in startM..endM
                                    val subColor = parseColorFromHex(subWithSlots.subject.colorHex)

                                    Card(
                                        modifier = Modifier
                                            .offset(y = topOffsetDp.dp)
                                            .fillMaxWidth()
                                            .height(heightDp.dp)
                                            .padding(horizontal = 4.dp, vertical = 3.dp)
                                            .clickable { onClassClick(subWithSlots.subject.id) }
                                            .testTag("grid_block_${subWithSlots.subject.id}_${slot.id}"),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isNow) Color.White else subColor.copy(alpha = 0.6f)
                                        ),
                                        colors = CardDefaults.cardColors(containerColor = subColor),
                                        elevation = CardDefaults.cardElevation(defaultElevation = if (isNow) 4.dp else 2.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(
                                                    horizontal = 7.dp,
                                                    vertical = if (compactBlock) 4.dp else 7.dp
                                                ),
                                             verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                if (isNow) {
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = Color(0xFFEF4444)
                                                    ) {
                                                        Text(
                                                            text = "AHORA",
                                                            color = Color.White,
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                }

                                                Text(
                                                    text = subWithSlots.subject.name,
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    lineHeight = 13.sp,
                                                    maxLines = if (compactBlock) 1 else 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            Column {
                                                Text(
                                                    text = "${slot.startTime}-${slot.endTime}",
                                                    color = Color.White.copy(alpha = 0.9f),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Clip
                                                )
                                                val room = slot.room.ifBlank { subWithSlots.subject.defaultRoom }
                                                if (showRoom && room.isNotBlank()) {
                                                    Text(
                                                        text = room,
                                                        color = Color.White.copy(alpha = 0.95f),
                                                        fontSize = 9.sp,
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyScheduleView(
    viewModel: ScheduleViewModel,
    selectedDay: Int,
    onDaySelected: (Int) -> Unit,
    onClassClick: (Long) -> Unit,
    onSyncClass: (SubjectEntity, ScheduleSlotEntity) -> Unit
) {
    val todayClasses by viewModel.todayClasses.collectAsStateWithLifecycle()
    val daysList = listOf(
        1 to "Lunes",
        2 to "Martes",
        3 to "Miércoles",
        4 to "Jueves",
        5 to "Viernes",
        6 to "Sábado"
    )

    val currentDayName = daysList.find { it.first == selectedDay }?.second ?: "Lunes"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Bento Day selector banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val prev = if (selectedDay > 1) selectedDay - 1 else 6
                        onDaySelected(prev)
                    }
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Día anterior", tint = IndigoPrimary)
                }

                Text(
                    text = currentDayName,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = {
                        val next = if (selectedDay < 6) selectedDay + 1 else 1
                        onDaySelected(next)
                    }
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Día siguiente", tint = IndigoPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (todayClasses.isEmpty()) {
            EmptyStateCard(
                title = "Tu día está libre",
                message = "No tienes clases asignadas para el $currentDayName.",
                icon = Icons.Default.School,
                actionButtonText = "+ Agregar Clase",
                onActionClick = { viewModel.openAddSubject() }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 70.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(todayClasses, key = { it.slot.id }) { classItem ->
                    ClassScheduleCard(
                        dayClassItem = classItem,
                        onClick = { onClassClick(classItem.subject.id) },
                        onSyncCalendarClick = {
                            onSyncClass(classItem.subject, classItem.slot)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SubjectListView(
    subjectsWithSlots: List<SubjectWithSlots>,
    onSubjectClick: (Long) -> Unit,
    onAddNewSubject: () -> Unit
) {
    if (subjectsWithSlots.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            EmptyStateCard(
                title = "No hay materias registradas",
                message = "Agrega tu primera materia para armar tu semestre.",
                icon = Icons.Default.School,
                actionButtonText = "+ Agregar Materia",
                onActionClick = onAddNewSubject
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .padding(bottom = 70.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(subjectsWithSlots, key = { it.subject.id }) { subWithSlots ->
            val sub = subWithSlots.subject
            val color = parseColorFromHex(sub.colorHex)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSubjectClick(sub.id) }
                    .testTag("subject_list_card_${sub.id}"),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(color),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = sub.name.take(2).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = sub.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (sub.code.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = sub.code,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        if (sub.professor.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = sub.professor,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Schedule badges summary
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            subWithSlots.slots.forEach { slot ->
                                val dayAbbr = when (slot.dayOfWeek) {
                                    1 -> "Lun"
                                    2 -> "Mar"
                                    3 -> "Mié"
                                    4 -> "Jue"
                                    5 -> "Vie"
                                    6 -> "Sáb"
                                    else -> "Dom"
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = color.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "$dayAbbr ${slot.startTime}",
                                        color = color,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
