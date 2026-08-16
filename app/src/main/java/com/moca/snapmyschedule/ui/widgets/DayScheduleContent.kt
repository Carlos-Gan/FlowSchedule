package com.moca.snapmyschedule.ui.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moca.snapmyschedule.data.model.ClassSession
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun DayScheduleContent(
    date: Calendar,
    classes: List<ClassSession>,
    modifier: Modifier = Modifier
) {
    val isToday = remember(date.timeInMillis) {
        isSameDate(
            first = date,
            second = Calendar.getInstance()
        )
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 12.dp
            )
        ) {
            Text(
                text = formatFullDate(date),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (isToday) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )

            if (isToday) {
                Text(
                    text = "Hoy",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (classes.isEmpty()) {
            EmptySchedule(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = classes,
                    key = { classSession ->
                        classSession.id
                    }
                ) { classSession ->

                    ClassCard(
                        classSession = classSession,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }

                item {
                    Spacer(
                        modifier = Modifier.height(88.dp)
                    )
                }
            }
        }
    }
}

private fun formatFullDate(
    date: Calendar
): String {
    val locale = Locale.getDefault()

    val formatter = SimpleDateFormat(
        "EEEE, d 'de' MMMM",
        locale
    )

    return formatter
        .format(date.time)
        .replaceFirstChar { character ->
            if (character.isLowerCase()) {
                character.titlecase(locale)
            } else {
                character.toString()
            }
        }
}
