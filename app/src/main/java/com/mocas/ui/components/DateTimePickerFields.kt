package com.mocas.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mocas.util.DateTimeUtils
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarDateField(
    value: String,
    label: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null,
    isError: Boolean = false
) {
    var showPicker by remember { mutableStateOf(false) }
    PickerField(
        label = label,
        value = DateTimeUtils.formatDate(value),
        icon = Icons.Default.CalendarMonth,
        isError = isError,
        onClick = { showPicker = true },
        modifier = modifier.then(
            if (testTag == null) Modifier else Modifier.testTag(testTag)
        )
    )

    if (showPicker) {
        val selectedMillis = dateToPickerMillis(value)
        val state = rememberDatePickerState(initialSelectedDateMillis = selectedMillis)
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { millis ->
                            onDateSelected(pickerMillisToDate(millis))
                        }
                        showPicker = false
                    }
                ) {
                    Text("Aceptar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(
                state = state,
                title = { Text("Selecciona una fecha", modifier = Modifier.padding(16.dp)) },
                showModeToggle = false
            )
        }
    }
}

internal fun dateToPickerMillis(value: String): Long? = runCatching {
    LocalDate.parse(value)
        .atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()
}.getOrNull()

internal fun pickerMillisToDate(millis: Long): String =
    Instant.ofEpochMilli(millis)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
        .toString()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockTimeField(
    value: String,
    label: String,
    onTimeSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null,
    isError: Boolean = false
) {
    var showPicker by remember { mutableStateOf(false) }
    PickerField(
        label = label,
        value = DateTimeUtils.formatTime(value, use24Hour = false) ?: value,
        icon = Icons.Default.AccessTime,
        isError = isError,
        onClick = { showPicker = true },
        modifier = modifier.then(
            if (testTag == null) Modifier else Modifier.testTag(testTag)
        )
    )

    if (showPicker) {
        val parts = value.split(":")
        val initialHour = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 8
        val initialMinute = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
        val state = rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text("Selecciona $label", fontWeight = FontWeight.Bold) },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = state)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onTimeSelected(
                            String.format(Locale.ROOT, "%02d:%02d", state.hour, state.minute)
                        )
                        showPicker = false
                    }
                ) {
                    Text("Aceptar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun PickerField(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isError: Boolean,
    onClick: () -> Unit,
    modifier: Modifier
) {
    val accent = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (isError) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isError) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = icon,
                contentDescription = "Elegir $label",
                tint = accent,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
