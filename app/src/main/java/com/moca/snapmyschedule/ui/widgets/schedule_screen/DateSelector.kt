package com.moca.snapmyschedule.ui.widgets.schedule_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun DateSelector(
    selectedDate: Calendar,
    onDateSelected: (dayOffset: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedTime = selectedDate.timeInMillis

    val visibleDates = remember(selectedTime) {
        (-3..3).map { offset ->
            (selectedDate.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, offset)
            }
        }
    }

    val today = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 8.dp,
                vertical = 8.dp
            )
    ) {
        Text(
            text = formatMonthAndYear(selectedDate),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 8.dp
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            visibleDates.forEachIndexed { index, date ->
                val offset = index - 3
                val isSelected = offset == 0
                val isToday = isSameDate(date, today)

                DateItem(
                    date = date,
                    selected = isSelected,
                    today = isToday,
                    onClick = {
                        onDateSelected(offset)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DateItem(
    date: Calendar,
    selected: Boolean,
    today: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dayName = remember(date.timeInMillis) {
        SimpleDateFormat(
            "EEE",
            Locale.getDefault()
        ).format(date.time)
            .replace(".", "")
            .replaceFirstChar { character ->
                character.titlecase(Locale.getDefault())
            }
    }

    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = dayName,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date
                    .get(Calendar.DAY_OF_MONTH)
                    .toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (selected) {
                    FontWeight.Bold
                } else {
                    FontWeight.Normal
                },
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        if (today) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            )
        } else {
            Spacer(
                modifier = Modifier.height(5.dp)
            )
        }
    }
}

private fun formatMonthAndYear(
    date: Calendar
): String {
    return SimpleDateFormat(
        "MMMM yyyy",
        Locale.getDefault()
    ).format(date.time)
        .replaceFirstChar { character ->
            character.titlecase(Locale.getDefault())
        }
}

fun isSameDate(
    first: Calendar,
    second: Calendar
): Boolean {
    return first.get(Calendar.YEAR) ==
            second.get(Calendar.YEAR) &&
            first.get(Calendar.DAY_OF_YEAR) ==
            second.get(Calendar.DAY_OF_YEAR)
}