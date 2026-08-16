package com.moca.snapmyschedule.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moca.snapmyschedule.data.model.ClassFormData
import com.moca.snapmyschedule.data.model.WeekDay
import com.moca.snapmyschedule.ui.widgets.add_class.TimePickerField
import com.moca.snapmyschedule.data.model.ScheduleBlock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddClassScreen(
    onBack: () -> Unit,
    onSave: (ClassFormData) -> Unit,
    modifier: Modifier = Modifier
) {
    var subjectName by rememberSaveable {
        mutableStateOf("")
    }

    var subjectCode by rememberSaveable {
        mutableStateOf("")
    }

    var teacher by rememberSaveable {
        mutableStateOf("")
    }


    var attemptedSave by rememberSaveable {
        mutableStateOf(false)
    }

    val scheduleBlocks = remember {
        mutableStateListOf(
            ScheduleBlockInput(
                id = 1L,
                days = emptySet(),
                startTime = "08:00",
                endTime = "09:00",
                room = ""
            )
        )
    }

    val validScheduleBlocks =
        scheduleBlocks.isNotEmpty() &&
                scheduleBlocks.all { block ->
                    block.days.isNotEmpty() &&
                            isValidTime(block.startTime) &&
                            isValidTime(block.endTime) &&
                            block.startTime < block.endTime
                }

    val canSave =
        subjectName.isNotBlank() &&
                validScheduleBlocks

    BackHandler {
        onBack()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text("Agregar materia")
                },
                navigationIcon = {
                    TextButton(
                        onClick = onBack
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = subjectName,
                onValueChange = {
                    subjectName = it
                },
                label = {
                    Text("Nombre de la materia")
                },
                placeholder = {
                    Text("Ej. Estructura de Datos")
                },
                isError = attemptedSave &&
                        subjectName.isBlank(),
                supportingText = {
                    if (
                        attemptedSave &&
                        subjectName.isBlank()
                    ) {
                        Text("Escribe el nombre de la materia")
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = subjectCode,
                onValueChange = {
                    subjectCode = it.uppercase()
                },
                label = {
                    Text("Clave")
                },
                placeholder = {
                    Text("Ej. IF1909")
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = teacher,
                onValueChange = {
                    teacher = it
                },
                label = {
                    Text("Profesor")
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )



            Text(
                text = "Horarios",
                style = MaterialTheme.typography.titleLarge
            )

            scheduleBlocks.forEachIndexed { index, block ->

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Horario ${index + 1}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            if (scheduleBlocks.size > 1) {
                                TextButton(
                                    onClick = {
                                        scheduleBlocks.removeAt(index)
                                    }
                                ) {
                                    Text("Eliminar")
                                }
                            }
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = WeekDay.entries,
                                key = { day -> day.name }
                            ) { day ->

                                val isSelected = day in block.days

                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        val updatedDays =
                                            if (isSelected) {
                                                block.days - day
                                            } else {
                                                block.days + day
                                            }

                                        scheduleBlocks[index] = block.copy(
                                            days = updatedDays
                                        )
                                    },
                                    label = {
                                        Text(day.shortName)
                                    }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = block.room,
                            onValueChange = { newRoom ->
                                scheduleBlocks[index] = block.copy(
                                    room = newRoom.uppercase()
                                )
                            },
                            label = {
                                Text("Salón")
                            },
                            placeholder = {
                                Text("Ej. SC4, LC3 o LCRBD")
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TimePickerField(
                                label = "Inicio",
                                value = block.startTime,
                                onTimeSelected = { selectedTime ->
                                    scheduleBlocks[index] = block.copy(
                                        startTime = selectedTime
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )

                            TimePickerField(
                                label = "Fin",
                                value = block.endTime,
                                onTimeSelected = { selectedTime ->
                                    scheduleBlocks[index] = block.copy(
                                        endTime = selectedTime
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (
                            attemptedSave &&
                            block.days.isEmpty()
                        ) {
                            Text(
                                text = "Selecciona al menos un día",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        if (
                            attemptedSave &&
                            block.startTime >= block.endTime
                        ) {
                            Text(
                                text = "La hora final debe ser posterior a la inicial",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    val nextId =
                        (scheduleBlocks.maxOfOrNull { it.id } ?: 0L) + 1L

                    scheduleBlocks.add(
                        ScheduleBlockInput(
                            id = nextId,
                            days = emptySet(),
                            startTime = "08:00",
                            endTime = "09:00",
                            room = ""
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("+ Agregar otro horario")
            }

            Button(
                onClick = {
                    attemptedSave = true

                    if (canSave) {
                        onSave(
                            ClassFormData(
                                subjectName = subjectName.trim(),
                                subjectCode = subjectCode.trim(),
                                teacher = teacher.trim(),
                                scheduleBlock = scheduleBlocks.map { block ->
                                    ScheduleBlock(
                                        days = block.days,
                                        startTime = block.startTime,
                                        endTime = block.endTime,
                                        room = block.room.trim()
                                    )
                                }
                            )
                        )
                    }

                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar materia")
            }
        }
    }
}

private fun isValidTime(
    value: String
): Boolean {
    val timePattern =
        Regex("^([01]\\d|2[0-3]):[0-5]\\d$")

    return timePattern.matches(value)
}

private data class ScheduleBlockInput(
    val id: Long,
    val days: Set<WeekDay>,
    val startTime: String,
    val endTime: String,
    val room: String
)