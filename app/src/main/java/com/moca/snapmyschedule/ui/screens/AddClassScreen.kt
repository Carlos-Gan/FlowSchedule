package com.moca.snapmyschedule.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.moca.snapmyschedule.data.model.ClassFormData
import com.moca.snapmyschedule.data.model.ScheduleBlock
import com.moca.snapmyschedule.data.model.WeekDay
import com.moca.snapmyschedule.ui.widgets.add_class.ConflictMessageCard
import com.moca.snapmyschedule.ui.widgets.add_class.SaveClassBottomBar
import com.moca.snapmyschedule.ui.widgets.add_class.ScheduleCard
import com.moca.snapmyschedule.ui.widgets.add_class.SectionHeader
import com.moca.snapmyschedule.ui.widgets.add_class.SubjectInformationCard
import java.util.Locale

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class
)
@Composable
fun AddClassScreen(
    onBack: () -> Unit,
    onSave: (ClassFormData) -> Unit,
    modifier: Modifier = Modifier,
    initialData: ClassFormData? = null,
    title: String = "Agregar materia",
    validateSchedule: (ClassFormData) -> String? = {
        null
    }
) {
    var subjectName by rememberSaveable(
        initialData?.subjectName
    ) {
        mutableStateOf(
            initialData?.subjectName.orEmpty()
        )
    }

    var subjectCode by rememberSaveable(
        initialData?.subjectCode
    ) {
        mutableStateOf(
            initialData?.subjectCode.orEmpty()
        )
    }

    var teacher by rememberSaveable(
        initialData?.teacher
    ) {
        mutableStateOf(
            initialData?.teacher.orEmpty()
        )
    }

    var attemptedSave by rememberSaveable {
        mutableStateOf(false)
    }

    var scheduleConflictMessage by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    val scheduleBlocks = remember(initialData) {
        mutableStateListOf<ScheduleBlockInput>().apply {
            val initialBlocks =
                initialData?.scheduleBlocks.orEmpty()

            if (initialBlocks.isEmpty()) {
                add(
                    ScheduleBlockInput(
                        id = 1L,
                        days = emptySet(),
                        startTime = "08:00",
                        endTime = "09:00",
                        room = ""
                    )
                )
            } else {
                addAll(
                    initialBlocks.mapIndexed { index, block ->
                        ScheduleBlockInput(
                            id = index.toLong() + 1L,
                            days = block.days,
                            startTime = block.startTime,
                            endTime = block.endTime,
                            room = block.room
                        )
                    }
                )
            }
        }
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

    fun clearConflict() {
        scheduleConflictMessage = null
    }

    fun updateScheduleBlock(
        index: Int,
        transform: (ScheduleBlockInput) -> ScheduleBlockInput
    ) {
        val currentBlock =
            scheduleBlocks.getOrNull(index) ?: return

        scheduleBlocks[index] =
            transform(currentBlock)

        clearConflict()
    }

    fun saveClass() {
        attemptedSave = true

        if (!canSave) {
            return
        }

        val formData = ClassFormData(
            subjectName = subjectName.trim(),
            subjectCode = subjectCode.trim(),
            teacher = teacher.trim(),
            scheduleBlocks = scheduleBlocks.map { block ->
                ScheduleBlock(
                    days = block.days,
                    startTime = block.startTime,
                    endTime = block.endTime,
                    room = block.room.trim()
                )
            }
        )

        val conflictMessage =
            validateSchedule(formData)

        if (conflictMessage != null) {
            scheduleConflictMessage =
                conflictMessage
        } else {
            scheduleConflictMessage = null
            onSave(formData)
        }
    }

    BackHandler {
        onBack()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor =
            MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack
                    ) {
                        Icon(
                            imageVector =
                                Icons.Default.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                }
            )
        },
        bottomBar = {
            SaveClassBottomBar(
                buttonText = if (initialData == null) {
                    "Guardar materia"
                } else {
                    "Guardar cambios"
                },
                onSave = ::saveClass
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(
                    rememberScrollState()
                )
                .imePadding()
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(20.dp)
        ) {
            SubjectInformationCard(
                subjectName = subjectName,
                subjectCode = subjectCode,
                teacher = teacher,
                subjectNameHasError =
                    attemptedSave &&
                            subjectName.isBlank(),
                onSubjectNameChange = {
                    subjectName = it
                    clearConflict()
                },
                onSubjectCodeChange = {
                    subjectCode = it
                        .uppercase(Locale.ROOT)
                    clearConflict()
                },
                onTeacherChange = {
                    teacher = it
                    clearConflict()
                }
            )

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {
                SectionHeader(
                    title = "Horarios",
                    description =
                        "Selecciona los días, la hora y el salón de cada clase."
                )

                scheduleBlocks.forEachIndexed { index, block ->
                    ScheduleCard(
                        number = index + 1,
                        block = block,
                        attemptedSave = attemptedSave,
                        canDelete =
                            scheduleBlocks.size > 1,
                        onDelete = {
                            scheduleBlocks.removeAt(index)
                            clearConflict()
                        },
                        onDayClick = { day ->
                            updateScheduleBlock(index) {
                                val updatedDays =
                                    if (day in it.days) {
                                        it.days - day
                                    } else {
                                        it.days + day
                                    }

                                it.copy(
                                    days = updatedDays
                                )
                            }
                        },
                        onRoomChange = { room ->
                            updateScheduleBlock(index) {
                                it.copy(
                                    room = room.uppercase(
                                        Locale.ROOT
                                    )
                                )
                            }
                        },
                        onStartTimeChange = { time ->
                            updateScheduleBlock(index) {
                                it.copy(
                                    startTime = time
                                )
                            }
                        },
                        onEndTimeChange = { time ->
                            updateScheduleBlock(index) {
                                it.copy(
                                    endTime = time
                                )
                            }
                        }
                    )
                }

                FilledTonalButton(
                    onClick = {
                        val nextId =
                            (
                                    scheduleBlocks.maxOfOrNull {
                                        it.id
                                    } ?: 0L
                                    ) + 1L

                        scheduleBlocks.add(
                            ScheduleBlockInput(
                                id = nextId,
                                days = emptySet(),
                                startTime = "08:00",
                                endTime = "09:00",
                                room = ""
                            )
                        )

                        clearConflict()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
                    shape =
                        MaterialTheme.shapes.large
                ) {
                    Icon(
                        imageVector =
                            Icons.Default.Add,
                        contentDescription = null
                    )

                    Text(
                        text = "Agregar otro horario",
                        modifier =
                            Modifier.padding(start = 8.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible =
                    scheduleConflictMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                scheduleConflictMessage?.let { message ->
                    ConflictMessageCard(
                        message = message
                    )
                }
            }
        }
    }
}


fun isValidTime(
    value: String
): Boolean {
    val timePattern =
        Regex("^([01]\\d|2[0-3]):[0-5]\\d$")

    return timePattern.matches(value)
}

data class ScheduleBlockInput(
    val id: Long,
    val days: Set<WeekDay>,
    val startTime: String,
    val endTime: String,
    val room: String
)