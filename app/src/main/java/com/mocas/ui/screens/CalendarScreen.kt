package com.mocas.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mocas.data.local.SchoolEventType
import com.mocas.data.local.ClassExceptionEntity
import com.mocas.data.local.SchoolEventWithSubject
import com.mocas.data.local.SubjectWithSlots
import com.mocas.data.repository.CalendarSyncHelper
import com.mocas.ui.components.ClassScheduleCard
import com.mocas.ui.components.EmptyStateCard
import com.mocas.ui.components.EventItemCard
import com.mocas.ui.model.DayClassItem
import com.mocas.ui.theme.TurquoiseSecondary
import com.mocas.ui.util.showCalendarResult
import com.mocas.ui.util.lazyItemKey
import com.mocas.ui.util.isVacationDate
import com.mocas.ui.util.forDate
import com.mocas.ui.viewmodel.ScheduleViewModel
import com.mocas.util.DateTimeUtils
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CalendarScreen(
    viewModel: ScheduleViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedDateStr by viewModel.selectedCalendarDate.collectAsStateWithLifecycle()
    val allEvents by viewModel.allEventsWithSubject.collectAsStateWithLifecycle()
    val subjectsWithSlots by viewModel.subjectsWithSlots.collectAsStateWithLifecycle()
    val academicPeriods by viewModel.academicPeriods.collectAsStateWithLifecycle()
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    val classExceptions by viewModel.classExceptions.collectAsStateWithLifecycle()

    var calendarMonth by remember { mutableStateOf(YearMonth.now()) }

    val currentMonthHeading = remember(calendarMonth) {
        val str = calendarMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "MX")))
        str.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "ES")) else it.toString() }
    }

    // Parse day of week for selected date
    val selectedDayOfWeekNum = remember(selectedDateStr) {
        DateTimeUtils.parseDate(selectedDateStr)?.dayOfWeek?.value
            ?: DateTimeUtils.currentDayOfWeek()
    }

    // Classes for selected date
    val classesForSelectedDay = remember(
        subjectsWithSlots,
        selectedDayOfWeekNum,
        selectedDateStr,
        classExceptions
    ) {
        val list = mutableListOf<DayClassItem>()
        for (subWithSlots in subjectsWithSlots) {
            for (slot in subWithSlots.slots) {
                val selectedDate = DateTimeUtils.parseDate(selectedDateStr)
                val semesterStart = DateTimeUtils.parseDate(subWithSlots.subject.semesterStart)
                val semesterEnd = DateTimeUtils.parseDate(subWithSlots.subject.semesterEnd)
                if (slot.dayOfWeek == selectedDayOfWeekNum && selectedDate != null &&
                    semesterStart != null && semesterEnd != null && selectedDate in semesterStart..semesterEnd) {
                    slot.forDate(selectedDate, classExceptions)?.let { effective ->
                        list.add(DayClassItem(subject = subWithSlots.subject, slot = effective))
                    }
                }
            }
        }
        list.sortedBy { ScheduleViewModel.parseTimeToMinutes(it.slot.startTime) }
    }

    // Events for selected date
    val eventsForSelectedDay = remember(allEvents, selectedDateStr) {
        allEvents.filter {
            it.event.startDate <= selectedDateStr && it.event.endDate >= selectedDateStr
        }
    }
    val selectedDateIsVacation = remember(
        selectedDateStr,
        academicPeriods,
        allEvents,
        settings.outsidePeriodsAreVacations
    ) {
        DateTimeUtils.parseDate(selectedDateStr)?.let { date ->
            isVacationDate(
                date = date,
                academicPeriods = academicPeriods,
                events = allEvents,
                outsidePeriodsAreVacations = settings.outsidePeriodsAreVacations
            )
        } ?: false
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("calendar_screen_column"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Bento Month Navigation Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                calendarMonth = calendarMonth.minusMonths(1)
                            }
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Mes anterior", tint = MaterialTheme.colorScheme.primary)
                        }

                        Text(
                            text = currentMonthHeading,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        IconButton(
                            onClick = {
                                calendarMonth = calendarMonth.plusMonths(1)
                            }
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Mes siguiente", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Day of week labels (L M X J V S D)
                    val daysHeader = listOf("L", "M", "M", "J", "V", "S", "D")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        daysHeader.forEach { label ->
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(36.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Monthly Grid Computation
                    val monthGrid = remember(
                        calendarMonth,
                        selectedDateStr,
                        allEvents,
                        subjectsWithSlots,
                        academicPeriods,
                        settings.outsidePeriodsAreVacations,
                        classExceptions
                    ) {
                        generateMonthDays(
                            calendarMonth = calendarMonth,
                            selectedDateStr = selectedDateStr,
                            allEvents = allEvents,
                            subjectsWithSlots = subjectsWithSlots,
                            academicPeriods = academicPeriods,
                            outsidePeriodsAreVacations = settings.outsidePeriodsAreVacations,
                            classExceptions = classExceptions
                        )
                    }

                    // Render weeks
                    monthGrid.chunked(7).forEach { week ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            week.forEach { dayItem ->
                                MonthDayCell(
                                    dayItem = dayItem,
                                    onClick = {
                                        if (dayItem.isCurrentMonth) {
                                            viewModel.setSelectedCalendarDate(dayItem.dateString)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Legend of colored indicator dots Bento style
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LegendItem(color = Color(0xFF3B82F6), label = "Clases")
                            LegendItem(color = Color(0xFF8B5CF6), label = "Tareas")
                            LegendItem(color = Color(0xFFEF4444), label = "Exámenes")
                            LegendItem(color = Color(0xFFF59E0B), label = "Eventos")
                            LegendItem(color = Color(0xFF10B981), label = "Vacaciones")
                        }
                    }
                }
            }
        }

        // Selected Date Heading & Sync Action Bento Row
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Actividades para $selectedDateStr",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${classesForSelectedDay.size} clases • ${eventsForSelectedDay.size} actividades",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = {
                            if (eventsForSelectedDay.isNotEmpty()) {
                                val first = eventsForSelectedDay.first()
                                showCalendarResult(
                                    context,
                                    CalendarSyncHelper.addEventToPhoneCalendar(context, first.event, first.subject?.name)
                                )
                            } else if (classesForSelectedDay.isNotEmpty()) {
                                val first = classesForSelectedDay.first()
                                showCalendarResult(
                                    context,
                                    CalendarSyncHelper.addClassToPhoneCalendar(context, first.subject, first.slot)
                                )
                            } else {
                                viewModel.openAddEvent(defaultDate = selectedDateStr)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("calendar_sync_day_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Sincronizar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Day's Events List
        if (eventsForSelectedDay.isNotEmpty()) {
            item {
                Text(
                    text = "Tareas y eventos del día",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            items(
                eventsForSelectedDay,
                key = { lazyItemKey("calendar-event", it.event.id) }
            ) { eventWithSubject ->
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
                    onToggleSubtask = { subtaskId, completed ->
                        viewModel.toggleSubtaskCompleted(eventWithSubject.event.id, subtaskId, completed)
                    }
                )
            }
        }

        // Day's Classes List
        if (classesForSelectedDay.isNotEmpty()) {
            item {
                Text(
                    text = "Clases del día",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            items(
                classesForSelectedDay,
                key = { lazyItemKey("calendar-class", it.slot.id) }
            ) { classItem ->
                ClassScheduleCard(
                    dayClassItem = classItem,
                    onClick = {
                        DateTimeUtils.parseDate(selectedDateStr)?.let { date ->
                            viewModel.openClassOccurrence(
                                subject = classItem.subject,
                                slot = classItem.slot,
                                date = date,
                                exception = classExceptions.firstOrNull {
                                    it.slotId == classItem.slot.id && it.date == selectedDateStr
                                }
                            )
                        }
                    },
                    onSyncCalendarClick = {
                        showCalendarResult(
                            context,
                            CalendarSyncHelper.addClassToPhoneCalendar(
                                context,
                                classItem.subject,
                                classItem.slot
                            )
                        )
                    }
                )
            }
        }

        if (classesForSelectedDay.isEmpty() && eventsForSelectedDay.isEmpty()) {
            item {
                EmptyStateCard(
                    title = if (selectedDateIsVacation) "Día de vacaciones" else "Sin actividades para este día",
                    message = if (selectedDateIsVacation) {
                        "Este día está fuera de tus periodos académicos."
                    } else {
                        "¿Quieres programar una tarea o examen?"
                    },
                    icon = Icons.Default.EventNote,
                    actionButtonText = "+ Agregar Actividad",
                    onActionClick = { viewModel.openAddEvent(defaultDate = selectedDateStr) }
                )
            }
        }
    }
}

@Composable
fun MonthDayCell(
    dayItem: com.mocas.ui.model.CalendarDayItem,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                when {
                    dayItem.isSelected -> MaterialTheme.colorScheme.primary
                    dayItem.isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else -> Color.Transparent
                }
            )
            .clickable(enabled = dayItem.isCurrentMonth) { onClick() }
            .testTag("calendar_day_${dayItem.dateString}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (dayItem.dayNumber > 0) "${dayItem.dayNumber}" else "",
                fontSize = 13.sp,
                fontWeight = if (dayItem.isSelected || dayItem.isToday) FontWeight.ExtraBold else FontWeight.Medium,
                color = when {
                    !dayItem.isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    dayItem.isSelected -> MaterialTheme.colorScheme.onPrimary
                    dayItem.isToday -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )

            // Activity Indicator Dots
            if (dayItem.isCurrentMonth) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(5.dp)
                ) {
                    if (dayItem.hasExams) {
                        Box(modifier = Modifier.size(3.5.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                    }
                    if (dayItem.hasTasks) {
                        Box(modifier = Modifier.size(3.5.dp).clip(CircleShape).background(Color(0xFF8B5CF6)))
                    }
                    if (dayItem.hasClasses) {
                        Box(modifier = Modifier.size(3.5.dp).clip(CircleShape).background(if (dayItem.isSelected) MaterialTheme.colorScheme.onPrimary else Color(0xFF3B82F6)))
                    }
                    if (dayItem.hasEvents) {
                        Box(modifier = Modifier.size(3.5.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                    }
                    if (dayItem.isHoliday) {
                        Box(
                            modifier = Modifier
                                .size(3.5.dp)
                                .clip(CircleShape)
                                .background(
                                    if (dayItem.isSelected) MaterialTheme.colorScheme.onPrimary else Color(0xFF10B981)
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun generateMonthDays(
    calendarMonth: YearMonth,
    selectedDateStr: String,
    allEvents: List<SchoolEventWithSubject>,
    subjectsWithSlots: List<SubjectWithSlots>,
    academicPeriods: List<com.mocas.data.local.AcademicPeriodEntity>,
    outsidePeriodsAreVacations: Boolean,
    classExceptions: List<ClassExceptionEntity>
): List<com.mocas.ui.model.CalendarDayItem> {
    val list = mutableListOf<com.mocas.ui.model.CalendarDayItem>()
    val year = calendarMonth.year
    val month = calendarMonth.monthValue
    val maxDays = calendarMonth.lengthOfMonth()
    val leadingOffset = calendarMonth.atDay(1).dayOfWeek.value - 1

    // Today string
    val todayStr = ScheduleViewModel.getTodayDateString()

    // Leading days from previous month
    for (i in 0 until leadingOffset) {
        list.add(
            com.mocas.ui.model.CalendarDayItem(
                dateString = "",
                dayNumber = 0,
                isCurrentMonth = false,
                isToday = false,
                isSelected = false
            )
        )
    }

    // Days of current month
    for (day in 1..maxDays) {
        val date = calendarMonth.atDay(day)
        val dateStr = date.toString()
        val dayOfWeekNum = date.dayOfWeek.value

        val hasClasses = subjectsWithSlots.any { sub ->
            val start = DateTimeUtils.parseDate(sub.subject.semesterStart)
            val end = DateTimeUtils.parseDate(sub.subject.semesterEnd)
            start != null && end != null && date in start..end &&
                sub.slots.any {
                    it.dayOfWeek == dayOfWeekNum && it.forDate(date, classExceptions) != null
                }
        }
        val dayEvents = allEvents.filter { it.event.startDate <= dateStr && it.event.endDate >= dateStr }
        val hasTasks = dayEvents.any { it.event.type == SchoolEventType.TAREA }
        val hasExams = dayEvents.any { it.event.type == SchoolEventType.EXAMEN }
        val hasEvents = dayEvents.any {
            it.event.type == SchoolEventType.EVENTO_ESCOLAR || it.event.type == SchoolEventType.EXPOSICION
        }
        val isHoliday = isVacationDate(
            date = date,
            academicPeriods = academicPeriods,
            events = allEvents,
            outsidePeriodsAreVacations = outsidePeriodsAreVacations
        )

        list.add(
            com.mocas.ui.model.CalendarDayItem(
                dateString = dateStr,
                dayNumber = day,
                isCurrentMonth = true,
                isToday = dateStr == todayStr,
                isSelected = dateStr == selectedDateStr,
                hasClasses = hasClasses,
                hasTasks = hasTasks,
                hasExams = hasExams,
                hasEvents = hasEvents,
                isHoliday = isHoliday
            )
        )
    }

    // Trailing to complete multiple of 7
    while (list.size % 7 != 0) {
        list.add(
            com.mocas.ui.model.CalendarDayItem(
                dateString = "",
                dayNumber = 0,
                isCurrentMonth = false,
                isToday = false,
                isSelected = false
            )
        )
    }

    return list
}
