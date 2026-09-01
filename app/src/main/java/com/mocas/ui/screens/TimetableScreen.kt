package com.mocas.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mocas.R
import com.mocas.data.local.ScheduleSlotEntity
import com.mocas.data.local.AcademicPeriodEntity
import com.mocas.data.local.ClassExceptionEntity
import com.mocas.data.local.SchoolEventWithSubject
import com.mocas.data.local.SubjectEntity
import com.mocas.data.local.SubjectWithSlots
import com.mocas.data.repository.CalendarSyncHelper
import com.mocas.ui.components.ClassScheduleCard
import com.mocas.ui.components.EmptyStateCard
import com.mocas.ui.components.horario.DailyClassRow
import com.mocas.ui.components.horario.DailyScheduleView
import com.mocas.ui.components.horario.SubjectListView
import com.mocas.ui.components.horario.WeekNavigationBar
import com.mocas.ui.components.horario.WeeklyGridView
import com.mocas.ui.components.parseColorFromHex
import com.mocas.ui.model.TimetableDisplayMode
import com.mocas.ui.theme.TurquoiseSecondary
import com.mocas.ui.util.showCalendarResult
import com.mocas.ui.util.isVacationDate
import com.mocas.ui.util.compactDayDate
import com.mocas.ui.util.fullDayDate
import com.mocas.ui.util.isActiveOn
import com.mocas.ui.util.weekRangeLabel
import com.mocas.ui.util.forDate
import com.mocas.ui.viewmodel.ScheduleViewModel
import com.mocas.ui.dialogs.ShareScheduleDialog
import java.util.Calendar
import java.time.LocalDate

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
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
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
                            inactiveContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
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
                    isVacationDay = selectedDateIsVacation,
                    onPreviousDay = viewModel::showPreviousDay,
                    onNextDay = viewModel::showNextDay,
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
                    onSyncClass = { sub, slot ->
                        showCalendarResult(
                            context,
                            CalendarSyncHelper.addClassToPhoneCalendar(context, sub, slot)
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
