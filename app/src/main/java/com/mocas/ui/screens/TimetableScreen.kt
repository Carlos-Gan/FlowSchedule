package com.mocas.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import com.mocas.data.local.ScheduleSlotEntity
import com.mocas.data.local.AcademicPeriodEntity
import com.mocas.data.local.ClassExceptionEntity
import com.mocas.data.local.SchoolEventWithSubject
import com.mocas.data.local.SubjectEntity
import com.mocas.data.local.SubjectWithSlots
import com.mocas.data.repository.CalendarSyncHelper
import com.mocas.ui.components.ClassScheduleCard
import com.mocas.ui.components.EmptyStateCard
import com.mocas.ui.components.parseColorFromHex
import com.mocas.ui.model.TimetableDisplayMode
import com.mocas.ui.theme.IndigoPrimary
import com.mocas.ui.theme.TurquoiseSecondary
import com.mocas.ui.util.showCalendarResult
import com.mocas.ui.util.isVacationDate
import com.mocas.ui.util.compactDayDate
import com.mocas.ui.util.fullDayDate
import com.mocas.ui.util.isActiveOn
import com.mocas.ui.util.weekRangeLabel
import com.mocas.ui.util.forDate
import com.mocas.ui.viewmodel.ScheduleViewModel
import com.mocas.ui.dialogs.ShareScheduleDialog
import java.util.Calendar
import java.time.LocalDate

@Composable
fun TimetableScreen(
    viewModel: ScheduleViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentMode by viewModel.timetableMode.collectAsStateWithLifecycle()
    val subjectsWithSlots by viewModel.subjectsWithSlots.collectAsStateWithLifecycle()
    val selectedDay by viewModel.selectedDayOfWeek.collectAsStateWithLifecycle()
    val selectedWeekStart by viewModel.selectedWeekStart.collectAsStateWithLifecycle()
    val academicPeriods by viewModel.academicPeriods.collectAsStateWithLifecycle()
    val allEvents by viewModel.allEventsWithSubject.collectAsStateWithLifecycle()
    val classExceptions by viewModel.classExceptions.collectAsStateWithLifecycle()
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    var showShareDialog by remember { mutableStateOf(false) }
    val selectedDate = selectedWeekStart.plusDays((selectedDay - 1).toLong())
    val selectedDateIsVacation = settings.showVacationsInTimetable && isVacationDate(
        date = selectedDate,
        academicPeriods = academicPeriods,
        events = allEvents,
        outsidePeriodsAreVacations = settings.outsidePeriodsAreVacations
    )
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
                        onClick = { viewModel.setTimetableMode(mode) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.outline.copy(
                                alpha = 0.3f
                            )
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("timetable_mode_${mode.name.lowercase()}"),
                        tonalElevation = if (isSelected) 3.dp else 0.dp
                    ) {
                        Text(
                            text = mode.displayName,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }

        if (currentMode != TimetableDisplayMode.MATERIAS) {
            WeekNavigationBar(
                weekStart = selectedWeekStart,
                onPreviousWeek = viewModel::showPreviousWeek,
                onCurrentWeek = viewModel::showCurrentWeek,
                onNextWeek = viewModel::showNextWeek,
                onShare = { showShareDialog = true }
            )
        }

        when (currentMode) {
            TimetableDisplayMode.SEMANAL -> {
                WeeklyGridView(
                    subjectsWithSlots = subjectsWithSlots,
                    weekStart = selectedWeekStart,
                    academicPeriods = academicPeriods,
                    allEvents = allEvents,
                    outsidePeriodsAreVacations = settings.outsidePeriodsAreVacations,
                    showVacations = settings.showVacationsInTimetable,
                    classExceptions = classExceptions,
                    onClassClick = { subject, slot, date, exception ->
                        viewModel.openClassOccurrence(subject, slot, date, exception)
                    },
                    onAddClassClick = { viewModel.openAddSubject() }
                )
            }

            TimetableDisplayMode.DIARIA -> {
                DailyScheduleView(
                    viewModel = viewModel,
                    selectedDay = selectedDay,
                    selectedDate = selectedDate,
                    isVacationDay = selectedDateIsVacation,
                    onPreviousDay = viewModel::showPreviousDay,
                    onNextDay = viewModel::showNextDay,
                    onClassClick = { subject, slot ->
                        viewModel.openClassOccurrence(
                            subject = subject,
                            slot = slot,
                            date = selectedDate,
                            exception = classExceptions.firstOrNull {
                                it.slotId == slot.id && it.date == selectedDate.toString()
                            }
                        )
                    },
                    onSyncClass = { sub, slot ->
                        showCalendarResult(
                            context,
                            CalendarSyncHelper.addClassToPhoneCalendar(context, sub, slot)
                        )
                    },
                    onAddSubjectClick = { viewModel.openAddSubject() }
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
    if (showShareDialog) {
        ShareScheduleDialog(
            context = context,
            subjects = subjectsWithSlots,
            events = allEvents,
            weekStart = selectedWeekStart,
            onDismiss = { showShareDialog = false }
        )
    }
}

@Composable
private fun WeekNavigationBar(
    weekStart: LocalDate,
    onPreviousWeek: () -> Unit,
    onCurrentWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onShare: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        IconButton(onClick = onPreviousWeek) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Semana anterior",
                tint = IndigoPrimary
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Semana",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = weekRangeLabel(weekStart),
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
        Surface(
            onClick = onCurrentWeek,
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = "Hoy",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        IconButton(onClick = onNextWeek) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Semana siguiente",
                tint = IndigoPrimary
            )
        }
        IconButton(onClick = onShare) {
            Icon(Icons.Default.Share, contentDescription = "Compartir horario", tint = IndigoPrimary)
        }
    }
}

@Composable
fun WeeklyGridView(
    subjectsWithSlots: List<SubjectWithSlots>,
    weekStart: LocalDate,
    academicPeriods: List<AcademicPeriodEntity>,
    allEvents: List<SchoolEventWithSubject>,
    outsidePeriodsAreVacations: Boolean,
    showVacations: Boolean,
    classExceptions: List<ClassExceptionEntity>,
    onClassClick: (SubjectEntity, ScheduleSlotEntity, LocalDate, ClassExceptionEntity?) -> Unit,
    onAddClassClick: () -> Unit
) {
    val days = listOf(
        1 to "LUN",
        2 to "MAR",
        3 to "MIÉ",
        4 to "JUE",
        5 to "VIE",
        6 to "SÁB",
        7 to "DOM"
    )

    val allSlots = subjectsWithSlots.flatMap { item ->
        item.slots.mapNotNull { slot ->
            val date = weekStart.plusDays((slot.dayOfWeek - 1).toLong())
            if (item.isActiveOn(date)) slot.forDate(date, classExceptions) else null
        }
    }
    if (allSlots.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            EmptyStateCard(
                title = "No hay clases esta semana",
                message = if (subjectsWithSlots.isEmpty()) {
                    "Agrega tus materias o importa una foto de tu horario escolar."
                } else {
                    "Elige otra semana o revisa las fechas de tus periodos académicos."
                },
                icon = Icons.Default.School,
                actionButtonText = "+ Agregar Materia",
                onActionClick = onAddClassClick
            )
        }
        return
    }
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
                val date = weekStart.plusDays((dayNum - 1).toLong())
                val isToday = date == LocalDate.now()
                val isVacation = showVacations && isVacationDate(
                    date = date,
                    academicPeriods = academicPeriods,
                    events = allEvents,
                    outsidePeriodsAreVacations = outsidePeriodsAreVacations
                )
                Box(
                    modifier = Modifier
                        .width(dayColWidth)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = when {
                            isVacation -> Color(0xFF10B981)
                            isToday -> IndigoPrimary
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        border = BorderStroke(
                            1.dp,
                            when {
                                isVacation -> Color(0xFF059669)
                                isToday -> IndigoPrimary
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isToday || isVacation) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = compactDayDate(date),
                                fontSize = 9.sp,
                                color = if (isToday || isVacation) Color.White.copy(alpha = 0.85f)
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
                    val date = weekStart.plusDays((dayNum - 1).toLong())
                    val isToday = date == LocalDate.now()
                    val isVacation = showVacations && isVacationDate(
                        date = date,
                        academicPeriods = academicPeriods,
                        events = allEvents,
                        outsidePeriodsAreVacations = outsidePeriodsAreVacations
                    )

                    Box(
                        modifier = Modifier
                            .width(dayColWidth)
                            .height(hourHeight * hours.size)
                            .background(
                                when {
                                    isVacation -> Color(0xFF10B981).copy(alpha = 0.08f)
                                    isToday -> IndigoPrimary.copy(alpha = 0.03f)
                                    else -> Color.Transparent
                                }
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
                                if (slot.dayOfWeek == dayNum && subWithSlots.isActiveOn(date)) {
                                    val exception = classExceptions.firstOrNull {
                                        it.slotId == slot.id && it.date == date.toString()
                                    }
                                    val effectiveSlot =
                                        slot.forDate(date, classExceptions) ?: continue
                                    val startM =
                                        ScheduleViewModel.parseTimeToMinutes(effectiveSlot.startTime)
                                    val endM =
                                        ScheduleViewModel.parseTimeToMinutes(effectiveSlot.endTime)
                                    val duration = (endM - startM).coerceAtLeast(30)

                                    val topOffsetMins = (startM - gridStartHour * 60)
                                        .coerceAtLeast(0)
                                    val topOffsetDp =
                                        (topOffsetMins.toFloat() / 60f) * hourHeight.value
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
                                            .clickable {
                                                onClassClick(
                                                    subWithSlots.subject,
                                                    effectiveSlot,
                                                    date,
                                                    exception
                                                )
                                            }
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
                                                            modifier = Modifier.padding(
                                                                horizontal = 4.dp,
                                                                vertical = 1.dp
                                                            )
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

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "${effectiveSlot.startTime}-${effectiveSlot.endTime}",
                                                    color = Color.White.copy(alpha = 0.9f),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Clip
                                                )
                                                val room =
                                                    effectiveSlot.room.ifBlank { subWithSlots.subject.defaultRoom }
                                                if (showRoom && room.isNotBlank()) {
                                                    Text(
                                                        text = " · $room",
                                                        color = Color.White.copy(alpha = 0.95f),
                                                        fontSize = 9.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f)
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
    selectedDate: LocalDate,
    isVacationDay: Boolean,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onClassClick: (SubjectEntity, ScheduleSlotEntity) -> Unit,
    onSyncClass: (SubjectEntity, ScheduleSlotEntity) -> Unit,
    onAddSubjectClick: () -> Unit
) {
    val todayClasses by viewModel.todayClasses.collectAsStateWithLifecycle()
    val daysList = remember {
        listOf(
            1 to "Lunes",
            2 to "Martes",
            3 to "Miércoles",
            4 to "Jueves",
            5 to "Viernes",
            6 to "Sábado",
            7 to "Domingo"
        )
    }

    val currentDayName = daysList.find { it.first == selectedDay }?.second ?: "Lunes"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        // Bento Day selector banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(
                1.dp,
                if (isVacationDay) {
                    Color(0xFF10B981)
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                }
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isVacationDay) {
                    Color(0xFF10B981).copy(alpha = 0.12f)
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onPreviousDay,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("previous_day_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.ChevronLeft,
                            contentDescription = "Día anterior",
                            tint = IndigoPrimary
                        )
                    }
                }

                AnimatedContent(
                    targetState = selectedDay,
                    transitionSpec = {
                        val forward = targetState > initialState ||
                                (initialState == 7 && targetState == 1)
                        if (forward) {
                            (slideInHorizontally { width -> width } + fadeIn())
                                .togetherWith(slideOutHorizontally { width -> -width } + fadeOut())
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn())
                                .togetherWith(slideOutHorizontally { width -> width } + fadeOut())
                        }
                    },
                    label = "day_selector_content"
                ) { animatedDay ->
                    val animatedDayName = daysList.find { it.first == animatedDay }?.second ?: "Lunes"
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (isVacationDay) {
                                Icon(
                                    imageVector = Icons.Default.BeachAccess,
                                    contentDescription = null,
                                    tint = Color(0xFF059669),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = if (isVacationDay) {
                                    "$animatedDayName · Vacaciones"
                                } else {
                                    animatedDayName
                                },
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    letterSpacing = (-0.3).sp
                                ),
                                color = if (isVacationDay) Color(0xFF059669)
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = fullDayDate(selectedDate),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    onClick = onNextDay,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("next_day_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "Día siguiente",
                            tint = IndigoPrimary
                        )
                    }
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
                onActionClick = onAddSubjectClick
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(todayClasses, key = { it.slot.id }) { classItem ->
                    ClassScheduleCard(
                        dayClassItem = classItem,
                        onClick = { onClassClick(classItem.subject, classItem.slot) },
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
                                    fontSize = 16.sp,
                                    lineHeight = 19.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            if (sub.code.isNotBlank()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.width(58.dp)
                                ) {
                                    Text(
                                        text = sub.code,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(
                                            horizontal = 4.dp,
                                            vertical = 4.dp
                                        )
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
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
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
                                        modifier = Modifier.padding(
                                            horizontal = 6.dp,
                                            vertical = 2.dp
                                        )
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
