package com.moca.snapmyschedule.ui.widgets.add_class

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun TimePickerField(
    label: String,
    value: String,
    onTimeSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    defaultHour: Int = 8,
    defaultMinute: Int = 0,
    isError: Boolean = false
) {
    var showTimePicker by rememberSaveable {
        mutableStateOf(false)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isError) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )

        OutlinedButton(
            onClick = {
                showTimePicker = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = value.ifBlank {
                    "Seleccionar"
                },
                style = MaterialTheme.typography.titleMedium
            )
        }

        if (isError) {
            Text(
                text = "Selecciona una hora",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            title = label,
            initialTime = value,
            defaultHour = defaultHour,
            defaultMinute = defaultMinute,
            onDismiss = {
                showTimePicker = false
            },
            onConfirm = { selectedTime ->
                onTimeSelected(selectedTime)
                showTimePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    title: String,
    initialTime: String,
    defaultHour: Int,
    defaultMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val initialHour = initialTime
        .substringBefore(":")
        .toIntOrNull()
        ?: defaultHour

    val initialMinute = initialTime
        .substringAfter(
            delimiter = ":",
            missingDelimiterValue = ""
        )
        .toIntOrNull()
        ?: defaultMinute

    val timePickerState = rememberTimePickerState(
        initialHour = initialHour.coerceIn(0, 23),
        initialMinute = initialMinute.coerceIn(0, 59),
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(min = 280.dp)
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(
                    state = timePickerState
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancelar")
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val formattedTime = String.format(
                        Locale.US,
                        "%02d:%02d",
                        timePickerState.hour,
                        timePickerState.minute
                    )

                    onConfirm(formattedTime)
                }
            ) {
                Text("Aceptar")
            }
        }
    )
}