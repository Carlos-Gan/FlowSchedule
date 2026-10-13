package com.mocas.ui.components.horario

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mocas.R
import com.mocas.data.local.ScheduleSlotEntity
import com.mocas.data.local.SubjectEntity
import com.mocas.ui.components.EmptyStateCard
import com.mocas.ui.viewmodel.ScheduleViewModel
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.format.TextStyle
import java.util.Locale

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun DailyScheduleView(
    viewModel: ScheduleViewModel,
    selectedDate: LocalDate,
    onClassClick: (SubjectEntity, ScheduleSlotEntity) -> Unit,
    onAddSubjectClick: () -> Unit
) {
    val todayClasses by viewModel.todayClasses.collectAsStateWithLifecycle()
    val daysList = listOf(
        1 to stringResource(R.string.dia_l),
        2 to stringResource(R.string.dia_m),
        3 to stringResource(R.string.dia_mi),
        4 to stringResource(R.string.dia_j),
        5 to stringResource(R.string.dia_v),
        6 to stringResource(R.string.dia_s),
        7 to stringResource(R.string.dia_d)
    )

    val locale = Locale.getDefault()
    val monthName = remember(selectedDate, locale) {
        val format = if (selectedDate.year != LocalDate.now().year) "MMMM yyyy" else "MMMM"
        selectedDate.format(java.time.format.DateTimeFormatter.ofPattern(format, locale))
            .replaceFirstChar { it.titlecase(locale) }
    }
    val firstDate = remember { LocalDate.now().minusYears(5) }
    val dateCount = remember(firstDate) {
        ChronoUnit.DAYS.between(firstDate, LocalDate.now().plusYears(5)).toInt() + 1
    }
    val selectedIndex = ChronoUnit.DAYS.between(firstDate, selectedDate)
        .toInt()
        .coerceIn(0, dateCount - 1)
    val firstVisibleIndex = (selectedIndex - 2).coerceAtLeast(0)
    val dateListState = rememberLazyListState(initialFirstVisibleItemIndex = firstVisibleIndex)

    LaunchedEffect(selectedDate) {
        dateListState.animateScrollToItem((selectedIndex - 2).coerceAtLeast(0))
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

            Surface(
                onClick = { viewModel.selectTimetableDate(LocalDate.now()) },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = stringResource(R.string.hoy_boton),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- Compact Horizontal Date Strip ---
        LazyRow(
            state = dateListState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                count = dateCount,
                key = { index -> firstDate.plusDays(index.toLong()).toString() }
            ) { index ->
                val date = firstDate.plusDays(index.toLong())
                val label = daysList[date.dayOfWeek.value - 1].second
                val isSelected = date == selectedDate

                Surface(
                    onClick = { viewModel.selectTimetableDate(date) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent),
                    modifier = Modifier.width(58.dp).height(72.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = date.dayOfMonth.toString(),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val context = LocalContext.current
        val emptyMessage = remember(selectedDate) {
            val dayName = selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
            context.getString(R.string.no_clases_dia_formato, dayName)
        }

        if (todayClasses.isEmpty()) {
            EmptyStateCard(
                title = stringResource(R.string.tu_dia_libre),
                message = emptyMessage,
                icon = Icons.Default.School,
                actionButtonText = stringResource(R.string.agregar_clase_boton),
                onActionClick = onAddSubjectClick,
                modifier = Modifier.padding(15.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
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
