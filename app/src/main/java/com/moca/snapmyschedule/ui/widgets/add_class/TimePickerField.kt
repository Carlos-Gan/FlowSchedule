package com.moca.snapmyschedule.ui.widgets.add_class

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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

    val borderColor = when {
        isError -> MaterialTheme.colorScheme.error
        value.isNotBlank() -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    val containerColor = when {
        isError -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.22f)
        value.isNotBlank() ->
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)

        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (isError) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )

        Surface(
            onClick = {
                showTimePicker = true
            },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = containerColor,
            border = BorderStroke(
                width = if (isError || value.isNotBlank()) 1.5.dp else 1.dp,
                color = borderColor
            ),
            tonalElevation = if (value.isNotBlank()) 1.dp else 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp)
                    .padding(
                        horizontal = 16.dp,
                        vertical = 10.dp
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = if (value.isBlank()) {
                            "Seleccionar hora"
                        } else {
                            value
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (value.isBlank()) {
                            FontWeight.Normal
                        } else {
                            FontWeight.SemiBold
                        },
                        color = if (value.isBlank()) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )

                    Text(
                        text = if (value.isBlank()) {
                            "Toca para elegir"
                        } else {
                            "Formato de 24 horas"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Abrir selector de hora",
                    tint = if (isError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            }
        }

        AnimatedVisibility(
            visible = isError,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Text(
                text = "Selecciona una hora",
                modifier = Modifier.padding(start = 4.dp),
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
