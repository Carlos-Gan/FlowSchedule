package com.moca.snapmyschedule.ui.widgets.add_class

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.moca.snapmyschedule.data.model.WeekDay
import com.moca.snapmyschedule.ui.screens.ScheduleBlockInput
import com.moca.snapmyschedule.ui.screens.isValidTime

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScheduleCard(
    number: Int,
    block: ScheduleBlockInput,
    attemptedSave: Boolean,
    canDelete: Boolean,
    onDelete: () -> Unit,
    onDayClick: (WeekDay) -> Unit,
    onRoomChange: (String) -> Unit,
    onStartTimeChange: (String) -> Unit,
    onEndTimeChange: (String) -> Unit
) {
    val daysHaveError =
        attemptedSave &&
                block.days.isEmpty()

    val timesHaveError =
        attemptedSave &&
                (
                        !isValidTime(block.startTime) ||
                                !isValidTime(block.endTime) ||
                                block.startTime >= block.endTime
                        )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement =
                Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(12.dp),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color =
                        MaterialTheme.colorScheme
                            .primaryContainer
                ) {
                    Box(
                        modifier = Modifier.size(40.dp),
                        contentAlignment =
                            Alignment.Center
                    ) {
                        Text(
                            text = number.toString(),
                            style =
                                MaterialTheme.typography
                                    .titleMedium,
                            fontWeight = FontWeight.Bold,
                            color =
                                MaterialTheme.colorScheme
                                    .onPrimaryContainer
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Horario $number",
                        style =
                            MaterialTheme.typography
                                .titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = scheduleSummary(block),
                        style =
                            MaterialTheme.typography
                                .bodySmall,
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant,
                        maxLines = 1,
                        overflow =
                            TextOverflow.Ellipsis
                    )
                }

                if (canDelete) {
                    IconButton(
                        onClick = onDelete
                    ) {
                        Icon(
                            imageVector =
                                Icons.Default.Delete,
                            contentDescription =
                                "Eliminar horario",
                            tint =
                                MaterialTheme.colorScheme
                                    .error
                        )
                    }
                }
            }

            HorizontalDivider(
                color =
                    MaterialTheme.colorScheme
                        .outlineVariant
            )

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Días de clase",
                    style =
                        MaterialTheme.typography
                            .labelLarge,
                    color = if (daysHaveError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme
                            .onSurface
                    }
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    WeekDay.entries.forEach { day ->
                        val isSelected =
                            day in block.days

                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                onDayClick(day)
                            },
                            label = {
                                Text(day.shortName)
                            },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector =
                                            Icons.Default.Check,
                                        contentDescription = null,
                                        modifier =
                                            Modifier.size(18.dp)
                                    )
                                }
                            } else {
                                null
                            }
                        )
                    }
                }

                AnimatedVisibility(
                    visible = daysHaveError
                ) {
                    ValidationMessage(
                        message =
                            "Selecciona al menos un día."
                    )
                }
            }

            OutlinedTextField(
                value = block.room,
                onValueChange = onRoomChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Salón")
                },
                placeholder = {
                    Text("Ej. SC4, LC3 o LCRBD")
                },
                supportingText = {
                    Text("Opcional")
                },
                singleLine = true,
                shape =
                    MaterialTheme.shapes.large,
                keyboardOptions = KeyboardOptions(
                    capitalization =
                        KeyboardCapitalization.Characters
                )
            )

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Hora de clase",
                    style =
                        MaterialTheme.typography
                            .labelLarge,
                    color = if (timesHaveError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme
                            .onSurface
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {
                    TimePickerField(
                        label = "Inicio",
                        value = block.startTime,
                        onTimeSelected =
                            onStartTimeChange,
                        modifier = Modifier.weight(1f),
                        isError = timesHaveError
                    )

                    TimePickerField(
                        label = "Fin",
                        value = block.endTime,
                        onTimeSelected =
                            onEndTimeChange,
                        modifier = Modifier.weight(1f),
                        isError = timesHaveError
                    )
                }

                AnimatedVisibility(
                    visible = timesHaveError
                ) {
                    ValidationMessage(
                        message =
                            "La hora final debe ser posterior a la inicial."
                    )
                }
            }
        }
    }
}

@Composable
private fun ValidationMessage(
    message: String
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color =
            MaterialTheme.colorScheme
                .errorContainer.copy(alpha = 0.55f)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 8.dp
            ),
            style =
                MaterialTheme.typography.bodySmall,
            color =
                MaterialTheme.colorScheme
                    .onErrorContainer
        )
    }
}

private fun scheduleSummary(
    block: ScheduleBlockInput
): String {
    val daysText =
        if (block.days.isEmpty()) {
            "Sin días"
        } else {
            WeekDay.entries
                .filter { it in block.days }
                .joinToString(", ") {
                    it.shortName
                }
        }

    val roomText =
        block.room
            .takeIf { it.isNotBlank() }
            ?.let { " · $it" }
            .orEmpty()

    return "$daysText · ${block.startTime}–${block.endTime}$roomText"
}