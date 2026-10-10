package com.mocas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mocas.R
import com.mocas.ui.components.horario.DailyScheduleView
import com.mocas.ui.components.horario.SubjectListView
import com.mocas.ui.components.horario.WeekNavigationBar
import com.mocas.ui.components.horario.WeeklyGridView
import com.mocas.ui.dialogs.ShareScheduleDialog
import com.mocas.ui.model.TimetableDisplayMode
import com.mocas.ui.util.isVacationDate
import com.mocas.ui.viewmodel.ScheduleViewModel

@Composable
fun TimetableScreen(
    viewModel: ScheduleViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentMode by viewModel.timetableMode.collectAsStateWithLifecycle()
    val subjectsWithSlots by viewModel.subjectsWithSlots.collectAsStateWithLifecycle()
    val selectedDay by viewModel.selectedDayOfWeek.collectAsStateWithLifecycle()
    val selectedWeekStart by viewModel.selectedWeekStart.collectAsStateWithLifecycle()
    val academicPeriods by viewModel.academicPeriods.collectAsStateWithLifecycle()
    val allEvents by viewModel.allEventsWithSubject.collectAsStateWithLifecycle()
    val classExceptions by viewModel.classExceptions.collectAsStateWithLifecycle()
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    var showShareDialog by remember { mutableStateOf(false) }
    val selectedDate = selectedWeekStart.plusDays((selectedDay - 1).toLong())
    val selectedDateIsVacation = settings.showVacationsInTimetable && isVacationDate(
        date = selectedDate,
        academicPeriods = academicPeriods,
        events = allEvents,
        outsidePeriodsAreVacations = settings.outsidePeriodsAreVacations
    )
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Bento Segmented Bar: Vista Semanal | Vista Diaria | Lista de Materias
        Surface(
            modifier = Modifier.fillMaxWidth(),
            //color = MaterialTheme.colorScheme.surface,
        ) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                val modes = listOf(
                    TimetableDisplayMode.SEMANAL to stringResource(R.string.vista_semanal),
                    TimetableDisplayMode.DIARIA to stringResource(R.string.vista_diaria),
                    TimetableDisplayMode.MATERIAS to stringResource(R.string.lista_materias)
                )

                modes.forEachIndexed { index, (mode, label) ->
                    val isSelected = currentMode == mode
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
                        onClick = { viewModel.setTimetableMode(mode) },
                        selected = isSelected,
                        label = {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                            )
                        },
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = MaterialTheme.colorScheme.primary,
                            activeContentColor = MaterialTheme.colorScheme.onPrimary,
                            inactiveContainerColor = MaterialTheme.colorScheme.surfaceDim,
                            inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }

        if (currentMode != TimetableDisplayMode.MATERIAS) {
            WeekNavigationBar(
                weekStart = selectedWeekStart,
                onPreviousWeek = viewModel::showPreviousWeek,
                onCurrentWeek = viewModel::showCurrentWeek,
                onNextWeek = viewModel::showNextWeek,
                onShare = { showShareDialog = true }
            )
        }

        when (currentMode) {
            TimetableDisplayMode.SEMANAL -> {
                WeeklyGridView(
                    subjectsWithSlots = subjectsWithSlots,
                    weekStart = selectedWeekStart,
                    academicPeriods = academicPeriods,
                    allEvents = allEvents,
                    outsidePeriodsAreVacations = settings.outsidePeriodsAreVacations,
                    showVacations = settings.showVacationsInTimetable,
                    classExceptions = classExceptions,
                    onClassClick = { subject, slot, date, exception ->
                        viewModel.openClassOccurrence(subject, slot, date, exception)
                    },
                    onAddClassClick = { viewModel.openAddSubject() }
                )
            }

            TimetableDisplayMode.DIARIA -> {
                DailyScheduleView(
                    viewModel = viewModel,
                    selectedDay = selectedDay,
                    selectedDate = selectedDate,
                    onClassClick = { subject, slot ->
                        viewModel.openClassOccurrence(
                            subject = subject,
                            slot = slot,
                            date = selectedDate,
                            exception = classExceptions.firstOrNull {
                                it.slotId == slot.id && it.date == selectedDate.toString()
                            }
                        )
                    },
                    onAddSubjectClick = { viewModel.openAddSubject() }
                )
            }

            TimetableDisplayMode.MATERIAS -> {
                SubjectListView(
                    subjectsWithSlots = subjectsWithSlots,
                    onSubjectClick = { subjectId -> viewModel.openSubjectDetail(subjectId) },
                    onAddNewSubject = { viewModel.openAddSubject() }
                )
            }
        }
    }
    if (showShareDialog) {
        ShareScheduleDialog(
            context = context,
            subjects = subjectsWithSlots,
            events = allEvents,
            weekStart = selectedWeekStart,
            onDismiss = { showShareDialog = false }
        )
    }
}
