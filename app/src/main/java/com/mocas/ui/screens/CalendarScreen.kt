package com.mocas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mocas.R
import com.mocas.data.repository.CalendarSyncHelper
import com.mocas.ui.components.ClassScheduleCard
import com.mocas.ui.components.EmptyStateCard
import com.mocas.ui.components.events.EventItemCard
import com.mocas.ui.components.calendar.LegendItem
import com.mocas.ui.components.calendar.generateMonthDays
import com.mocas.ui.model.DayClassItem
import com.mocas.ui.util.forDate
import com.mocas.ui.util.isVacationDate
import com.mocas.ui.util.showCalendarResult
import com.mocas.ui.viewmodel.ScheduleViewModel
import com.mocas.util.DateTimeUtils
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

// Ancho máximo del contenido cuando hay dos columnas, para que en pantallas
// muy anchas (desktop/tablet grande) no queden demasiado separadas.
private val MAX_CONTENT_WIDTH: Dp = 1200.dp

@Composable
fun CalendarScreen(
    viewModel: ScheduleViewModel,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedDateStr by viewModel.selectedCalendarDate.collectAsStateWithLifecycle()
    val allEvents by viewModel.allEventsWithSubject.collectAsStateWithLifecycle()
    val subjectsWithSlots by viewModel.subjectsWithSlots.collectAsStateWithLifecycle()
    val academicPeriods by viewModel.academicPeriods.collectAsStateWithLifecycle()
    val classExceptions by viewModel.classExceptions.collectAsStateWithLifecycle()

    var calendarMonth by remember { mutableStateOf(YearMonth.now()) }

    val isCompact = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact

    val currentMonthHeading = remember(calendarMonth) {
        val locale = Locale.getDefault()
        val str = calendarMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", locale))
        str.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
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
                    semesterStart != null && semesterEnd != null && selectedDate in semesterStart..semesterEnd
                ) {
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
        allEvents
    ) {
        DateTimeUtils.parseDate(selectedDateStr)?.let { date ->
            isVacationDate(
                date = date,
                academicPeriods = academicPeriods,
                events = allEvents,
                outsidePeriodsAreVacations = true // Forzado a true para marcar días fuera de periodo
            )
        } ?: false
    }

    // ---- Secciones reutilizables (funciones locales: capturan todo lo de arriba) ----

    @Composable
    fun MonthNavigationCard() {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            //border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceDim),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { calendarMonth = calendarMonth.minusMonths(1) }
                    ) {
                        Icon(
                            Icons.Default.ChevronLeft,
                            contentDescription = stringResource(R.string.mes_anterior_desc),
                            tint = MaterialTheme.colorScheme.primary
                        )
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
                        onClick = { calendarMonth = calendarMonth.plusMonths(1) }
                    ) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = stringResource(R.string.mes_siguiente_desc),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Day of week labels (L M X J V S D)
                val daysHeader = listOf(
                    stringResource(R.string.dia_l),
                    stringResource(R.string.dia_m),
                    stringResource(R.string.dia_mi),
                    stringResource(R.string.dia_j),
                    stringResource(R.string.dia_v),
                    stringResource(R.string.dia_s),
                    stringResource(R.string.dia_d)
                )
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
                    classExceptions
                ) {
                    generateMonthDays(
                        calendarMonth = calendarMonth,
                        selectedDateStr = selectedDateStr,
                        allEvents = allEvents,
                        subjectsWithSlots = subjectsWithSlots,
                        academicPeriods = academicPeriods,
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
                        LegendItem(
                            color = Color(0xFF3B82F6),
                            label = stringResource(R.string.legend_clases)
                        )
                        LegendItem(
                            color = Color(0xFF8B5CF6),
                            label = stringResource(R.string.legend_tareas)
                        )
                        LegendItem(
                            color = Color(0xFFEF4444),
                            label = stringResource(R.string.legend_examenes)
                        )
                        LegendItem(
                            color = Color(0xFFF59E0B),
                            label = stringResource(R.string.legend_eventos)
                        )
                        LegendItem(
                            color = Color(0xFF10B981),
                            label = stringResource(R.string.legend_vacaciones)
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun DayHeadingSyncCard() {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceDim)
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
                        text = stringResource(R.string.actividades_para_formato, selectedDateStr),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(
                            R.string.clases_actividades_conteo,
                            classesForSelectedDay.size,
                            eventsForSelectedDay.size
                        ),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = {
                        if (eventsForSelectedDay.isNotEmpty()) {
                            val first = eventsForSelectedDay.first()
                            showCalendarResult(
                                context,
                                CalendarSyncHelper.addEventToPhoneCalendar(
                                    context,
                                    first.event,
                                    first.subject?.name
                                )
                            )
                        } else if (classesForSelectedDay.isNotEmpty()) {
                            val first = classesForSelectedDay.first()
                            showCalendarResult(
                                context,
                                CalendarSyncHelper.addClassToPhoneCalendar(
                                    context,
                                    first.subject,
                                    first.slot
                                )
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
                    Text(
                        text = stringResource(R.string.sincronizar_boton),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    @Composable
    fun EventsListSection() {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.tareas_eventos_dia),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                eventsForSelectedDay.forEach { eventWithSubject ->
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
                            viewModel.toggleSubtaskCompleted(
                                eventWithSubject.event.id,
                                subtaskId,
                                completed
                            )
                        }
                    )
                }
            }
        }
    }

    @Composable
    fun ClassesListSection() {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.clases_dia),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                ),
                color = MaterialTheme.colorScheme.surfaceDim
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                classesForSelectedDay.forEach { classItem ->
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
        }
    }

    // Vacío combinado: se usa en compacto cuando no hay ni actividades ni clases.
    @Composable
    fun CombinedEmptyState() {
        EmptyStateCard(
            title = if (selectedDateIsVacation) stringResource(R.string.dia_vacaciones) else stringResource(
                R.string.sin_actividades_dia
            ),
            message = if (selectedDateIsVacation) {
                stringResource(R.string.mensaje_fuera_periodos)
            } else {
                stringResource(R.string.mensaje_programar_actividad)
            },
            icon = Icons.Default.EventNote,
            actionButtonText = stringResource(R.string.nueva_actividad_boton),
            onActionClick = { viewModel.openAddEvent(defaultDate = selectedDateStr) }
        )
    }



    if (isCompact) {
        // ---- Pantalla angosta: una sola columna, orden original ----
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .testTag("calendar_screen_column"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { MonthNavigationCard() }
            item { DayHeadingSyncCard() }

            if (eventsForSelectedDay.isNotEmpty()) {
                item { EventsListSection() }
            }
            if (classesForSelectedDay.isNotEmpty()) {
                item { ClassesListSection() }
            }
            if (classesForSelectedDay.isEmpty() && eventsForSelectedDay.isEmpty()) {
                item { CombinedEmptyState() }
            }
        }
    } else {
        // ---- Pantalla grande: dos columnas ----
        // Izquierda: Calendario mensual. Derecha: Encabezado, eventos y clases del día seleccionado.
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxHeight()
                    .widthIn(max = MAX_CONTENT_WIDTH)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("calendar_screen_row"),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Columna izquierda: calendario mensual navegable
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    MonthNavigationCard()
                }

                // Columna derecha: encabezado del día, lista de tareas/eventos y clases
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    DayHeadingSyncCard()

                    if (eventsForSelectedDay.isNotEmpty()) {
                        EventsListSection()
                    }
                    if (classesForSelectedDay.isNotEmpty()) {
                        ClassesListSection()
                    }
                    if (eventsForSelectedDay.isEmpty() && classesForSelectedDay.isEmpty()) {
                        CombinedEmptyState()
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
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
                        Box(
                            modifier = Modifier
                                .size(3.5.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444))
                        )
                    }
                    if (dayItem.hasTasks) {
                        Box(
                            modifier = Modifier
                                .size(3.5.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF8B5CF6))
                        )
                    }
                    if (dayItem.hasClasses) {
                        Box(
                            modifier = Modifier
                                .size(3.5.dp)
                                .clip(CircleShape)
                                .background(
                                    if (dayItem.isSelected) MaterialTheme.colorScheme.onPrimary else Color(
                                        0xFF3B82F6
                                    )
                                )
                        )
                    }
                    if (dayItem.hasEvents) {
                        Box(
                            modifier = Modifier
                                .size(3.5.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF59E0B))
                        )
                    }
                    if (dayItem.isHoliday) {
                        Box(
                            modifier = Modifier
                                .size(3.5.dp)
                                .clip(CircleShape)
                                .background(
                                    if (dayItem.isSelected) MaterialTheme.colorScheme.onPrimary else Color(
                                        0xFF10B981
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}