package com.mocas.ui.components.horario

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mocas.data.local.ScheduleSlotEntity
import com.mocas.data.local.SubjectEntity
import com.mocas.ui.components.EmptyStateCard
import com.mocas.ui.viewmodel.ScheduleViewModel
import java.time.LocalDate

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
    val weekStart by viewModel.selectedWeekStart.collectAsStateWithLifecycle()

    val daysList = remember {
        listOf(
            1 to "LUN",
            2 to "MAR",
            3 to "MIÉ",
            4 to "JUE",
            5 to "VIE",
            6 to "SÁB",
            7 to "DOM"
        )
    }

    val monthName = remember(selectedDate) {
        selectedDate.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault())
            .replaceFirstChar { it.uppercase() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- Compact Header & Date Carousel ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = monthName,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.onSurface
            )

            IconButton(
                onClick = { viewModel.showCurrentWeek() },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = "Hoy",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // --- Compact Horizontal Date Strip ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            daysList.forEach { (dayNum, label) ->
                val date = weekStart.plusDays((dayNum - 1).toLong())
                val isSelected = dayNum == selectedDay

                Surface(
                    onClick = { viewModel.setSelectedDayOfWeek(dayNum) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent),
                    modifier = Modifier.width(54.dp).height(68.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = date.dayOfMonth.toString(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val emptyMessage = remember(selectedDate) {
            "No tienes clases asignadas para el ${selectedDate.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault())}."
        }

        if (todayClasses.isEmpty()) {
            EmptyStateCard(
                title = "Tu día está libre",
                message = emptyMessage,
                icon = Icons.Default.School,
                actionButtonText = "+ Agregar Clase",
                onActionClick = onAddSubjectClick
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 4.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(todayClasses, key = { it.slot.id }) { classItem ->
                    DailyClassRow(
                        classItem = classItem,
                        onClick = { onClassClick(classItem.subject, classItem.slot) }
                    )
                }
            }
        }
    }
}
