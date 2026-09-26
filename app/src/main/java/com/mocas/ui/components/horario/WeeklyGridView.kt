package com.mocas.ui.components.horario

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mocas.R
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
import java.util.Locale

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
        1 to stringResource(R.string.dia_l),
        2 to stringResource(R.string.dia_m),
        3 to stringResource(R.string.dia_mi),
        4 to stringResource(R.string.dia_j),
        5 to stringResource(R.string.dia_v),
        6 to stringResource(R.string.dia_s),
        7 to stringResource(R.string.dia_d)
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
                title = stringResource(R.string.no_hay_clases_semana),
                message = if (subjectsWithSlots.isEmpty()) {
                    stringResource(R.string.mensaje_sin_clases_semana)
                } else {
                    stringResource(R.string.mensaje_revisar_periodos)
                },
                icon = Icons.Default.School,
                actionButtonText = stringResource(R.string.agregar_materia_boton),
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
    val hourHeight = 90.dp
    val timeColWidth = 48.dp
    val dayColWidth = 130.dp

    val hScrollState = rememberScrollState()
    val vScrollState = rememberScrollState()

    val cal = java.time.LocalDateTime.now()
    val nowMins = cal.hour * 60 + cal.minute
    val today = LocalDate.now()
    val isTodayInWeek = today >= weekStart && today < weekStart.plusDays(7)

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Sticky Header: Days row (Pill Style)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = timeColWidth, bottom = 16.dp)
                .horizontalScroll(hScrollState)
        ) {
            days.forEach { (dayNum, label) ->
                val date = weekStart.plusDays((dayNum - 1).toLong())
                val isToday = date == LocalDate.now()
                
                Box(
                    modifier = Modifier
                        .width(dayColWidth)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceDim,
                        modifier = Modifier.size(width = 60.dp, height = 40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = label.take(3), // "Lun", "Mar"...
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
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
                            .padding(end = 12.dp, top = 0.dp),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Text(
                            text = String.format(Locale.ROOT, "%02d", hour),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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
                                            isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.03f)
                                            else -> Color.Transparent
                                        }
                                    )
                            ) {
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

                                            val subColor =
                                                parseColorFromHex(subWithSlots.subject.colorHex)

                                            Card(
                                                modifier = Modifier
                                                    .offset(y = topOffsetDp.dp)
                                                    .fillMaxWidth()
                                                    .height(heightDp.dp)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    .clickable {
                                                        onClassClick(
                                                            subWithSlots.subject,
                                                            effectiveSlot,
                                                            date,
                                                            exception
                                                        )
                                                    }
                                                    .testTag("grid_block_${subWithSlots.subject.id}_${slot.id}"),
                                                shape = RoundedCornerShape(16.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = subColor.copy(alpha = 0.15f)
                                                ),
                                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = effectiveSlot.startTime,
                                                            color = subColor,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.ExtraBold
                                                        )
                                                        
                                                        val room = effectiveSlot.room.ifBlank { subWithSlots.subject.defaultRoom }
                                                        if (room.isNotBlank()) {
                                                            Text(
                                                                text = " • $room",
                                                                color = subColor.copy(alpha = 0.7f),
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        }
                                                    }
                                                    
                                                    Text(
                                                        text = subWithSlots.subject.name,
                                                        color = subColor,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        lineHeight = 15.sp,
                                                        maxLines = 3,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
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
