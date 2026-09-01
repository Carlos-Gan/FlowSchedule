package com.mocas.ui.components.horario

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mocas.data.local.AcademicPeriodEntity
import com.mocas.data.local.ClassExceptionEntity
import com.mocas.data.local.ScheduleSlotEntity
import com.mocas.data.local.SchoolEventWithSubject
import com.mocas.data.local.SubjectEntity
import com.mocas.data.local.SubjectWithSlots
import com.mocas.ui.components.EmptyStateCard
import com.mocas.ui.components.parseColorFromHex
import com.mocas.ui.util.compactDayDate
import com.mocas.ui.util.forDate
import com.mocas.ui.util.isActiveOn
import com.mocas.ui.util.isVacationDate
import com.mocas.ui.viewmodel.ScheduleViewModel
import java.time.LocalDate

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
    val earliestStart = allSlots.minOfOrNull { ScheduleViewModel.parseTimeToMinutes(it.startTime) }
    val latestEnd = allSlots.maxOfOrNull { ScheduleViewModel.parseTimeToMinutes(it.endTime) }

    // Rango dinámico ajustado a las clases con un pequeño margen
    val gridStartHour = if (earliestStart != null) (earliestStart / 60).coerceAtLeast(0) else 8
    val gridEndHour = if (latestEnd != null) ((latestEnd + 59) / 60).coerceAtMost(24) else 16

    val finalStartHour = (gridStartHour - 1).coerceIn(0, 23)
    val finalEndHour = (gridEndHour + 1).coerceIn(finalStartHour + 1, 24)

    val hours = (finalStartHour until finalEndHour).toList()
    val hourHeight = 84.dp
    val timeColWidth = 56.dp
    val dayColWidth = 140.dp

    val hScrollState = rememberScrollState()
    val vScrollState = rememberScrollState()

    val cal = java.time.LocalDateTime.now()
    val nowMins = cal.hour * 60 + cal.minute
    val today = LocalDate.now()
    val isTodayInWeek = today >= weekStart && today < weekStart.plusDays(7)

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
                            isVacation -> MaterialTheme.colorScheme.secondary
                            isToday -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        border = BorderStroke(
                            1.dp,
                            when {
                                isVacation -> MaterialTheme.colorScheme.secondary
                                isToday -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
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
                                color = if (isToday || isVacation) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = compactDayDate(date),
                                fontSize = 9.sp,
                                color = if (isToday || isVacation) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
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
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                hours.forEach { hour ->
                    Box(
                        modifier = Modifier
                            .height(hourHeight)
                            .fillMaxWidth()
                            .padding(end = 8.dp, top = 8.dp),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Text(
                            text = String.format(java.util.Locale.ROOT, "%02d:00", hour),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Grid Days columns & Class Blocks
            Row(
                modifier = Modifier.horizontalScroll(hScrollState)
            ) {
                Box {
                    Row {
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
                                            isVacation -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)
                                            isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.02f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .border(
                                        0.5.dp,
                                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)
                                    )
                            ) {
                                // Hour horizontal divider lines
                                hours.forEachIndexed { index, _ ->
                                    HorizontalDivider(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .offset(y = hourHeight * index),
                                        thickness = 1.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.08f)
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

                                            val topOffsetMins = (startM - finalStartHour * 60)
                                                .coerceAtLeast(0)
                                            val topOffsetDp =
                                                (topOffsetMins.toFloat() / 60f) * hourHeight.value
                                            val heightDp =
                                                (duration.toFloat() / 60f) * hourHeight.value
                                            val compactBlock = duration < 60
                                            val showRoom = duration >= 50

                                            val isNow = isToday && nowMins in startM..endM
                                            val subColor =
                                                parseColorFromHex(subWithSlots.subject.colorHex)

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
                                                    if (isNow) MaterialTheme.colorScheme.onPrimary else subColor.copy(alpha = 0.6f)
                                                ),
                                                colors = CardDefaults.cardColors(containerColor = subColor),
                                                elevation = CardDefaults.cardElevation(
                                                    defaultElevation = if (isNow) 4.dp else 2.dp
                                                )
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
                                                                color = MaterialTheme.colorScheme.error
                                                            ) {
                                                                Text(
                                                                    text = "AHORA",
                                                                    color = MaterialTheme.colorScheme.onError,
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
                                                            color = MaterialTheme.colorScheme.onPrimary, // Subject color contrast
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
                                                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
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
                                                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.95f),
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

                    // Línea de tiempo (Time Indicator) - Posicionada al final para estar encima de todo
                    if (isTodayInWeek && nowMins in (finalStartHour * 60)..(finalEndHour * 60)) {
                        val yOffsetMins = (nowMins - finalStartHour * 60)
                        val yOffsetDp = (yOffsetMins.toFloat() / 60f) * hourHeight.value

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = yOffsetDp.dp)
                                .height(2.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            // La línea horizontal larga que cruza todas las columnas
                            Box(
                                modifier = Modifier
                                    .width(dayColWidth * days.size)
                                    .height(1.5.dp)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            // El punto indicador al inicio (pegado a la columna de horas)
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .offset(x = (-4).dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            )
                        }
                    }
                }
            }
        }
    }
}
