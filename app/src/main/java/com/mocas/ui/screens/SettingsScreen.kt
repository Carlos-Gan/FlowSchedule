package com.mocas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mocas.data.repository.GradeCalculator
import com.mocas.ui.components.settings.CalendarSyncSection
import com.mocas.ui.components.settings.GeneralDataSection
import com.mocas.ui.components.settings.MetricsCard
import com.mocas.ui.components.settings.PersonalizationSection
import com.mocas.ui.components.settings.ProfileHeaderCard
import com.mocas.ui.dialogs.BackupsDialog
import com.mocas.ui.dialogs.ProfileDialog
import com.mocas.ui.dialogs.TrashDialog
import com.mocas.ui.screens.settings.RemindersScreen
import com.mocas.ui.viewmodel.ScheduleViewModel

@Composable
fun SettingsScreen(
    viewModel: ScheduleViewModel,
    modifier: Modifier = Modifier,
    onRemindersScreenChanged: (Boolean) -> Unit = {}
) {
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    val allEvents by viewModel.allEventsWithSubject.collectAsStateWithLifecycle()
    val subjects by viewModel.subjectsWithSlots.collectAsStateWithLifecycle()
    val categories by viewModel.gradeCategories.collectAsStateWithLifecycle()
    val gradeItems by viewModel.gradeItems.collectAsStateWithLifecycle()
    val units by viewModel.gradeUnits.collectAsStateWithLifecycle()
    val unitWeights by viewModel.gradeUnitCategoryWeights.collectAsStateWithLifecycle()
    val currentStreak by viewModel.currentStreak.collectAsStateWithLifecycle()
    val syncMessage by viewModel.syncMessage.collectAsStateWithLifecycle()

    var showProfileDialog by remember { mutableStateOf(false) }
    var showTrashDialog by remember { mutableStateOf(false) }
    var showBackupsDialog by remember { mutableStateOf(false) }
    var showRemindersScreen by rememberSaveable { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose { onRemindersScreenChanged(false) }
    }

    val completedTasksCount = remember(allEvents) {
        allEvents.count { it.event.isCompleted }
    }

    val realAverage = remember(subjects, categories, gradeItems, units, unitWeights) {
        GradeCalculator.periodAverage(
            subjectIds = subjects.map { it.subject.id },
            categories = categories,
            items = gradeItems,
            units = units,
            unitCategoryWeights = unitWeights
        ) ?: 0.0
    }

    val displayAverage = remember(realAverage, settings.useGpaScale) {
        if (settings.useGpaScale) {
            val gpa =
                if (realAverage > 10) (realAverage / 100.0) * 4.0 else (realAverage / 10.0) * 4.0
            "%.2f".format(gpa)
        } else {
            if (realAverage > 10) "%.0f".format(realAverage) else "%.1f".format(realAverage)
        }
    }

    val context = LocalContext.current
    val versionName = context.packageManager
        .getPackageInfo(context.packageName, 0)
        .versionName.orEmpty()

    if (showRemindersScreen) {
        RemindersScreen(
            settings = settings,
            onUpdate = viewModel::updateSettings,
            onBack = {
                showRemindersScreen = false
                onRemindersScreenChanged(false)
            },
            modifier = modifier
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        ProfileHeaderCard(
            userName = settings.userName,
            educationLevel = settings.educationLevel,
            educationInstitution = settings.educationInstitution,
            onEditClick = { showProfileDialog = true }
        )

        Spacer(modifier = Modifier.height(16.dp))

        MetricsCard(
            displayAverage = displayAverage,
            useGpaScale = settings.useGpaScale,
            completedTasksCount = completedTasksCount,
            currentStreak = currentStreak
        )

        CalendarSyncSection(
            settings = settings,
            syncMessage = syncMessage,
            onSettingsChange = viewModel::updateSettings,
            onSyncNow = viewModel::syncWithDeviceCalendar
        )

        PersonalizationSection(
            settings = settings,
            onSettingsChange = viewModel::updateSettings,
            onOpenAppearance = viewModel::openAppearance
        )

        GeneralDataSection(
            settings = settings,
            onSettingsChange = viewModel::updateSettings,
            onRemindersClick = {
                showRemindersScreen = true
                onRemindersScreenChanged(true)
            },
            onTrashClick = { showTrashDialog = true },
            onBackupsClick = { showBackupsDialog = true }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "FlowSchedule $versionName",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(80.dp))
    }

    if (showProfileDialog) {
        ProfileDialog(
            initialName = settings.userName,
            initialEducation = settings.educationLevel,
            initialInstitution = settings.educationInstitution,
            onSave = { name, education, institution ->
                viewModel.updateSettings(
                    settings.copy(
                        userName = name,
                        educationLevel = education,
                        educationInstitution = institution
                    )
                )
                showProfileDialog = false
            },
            onDismiss = { showProfileDialog = false }
        )
    }

    if (showTrashDialog) {
        TrashDialog(viewModel = viewModel, onDismiss = { showTrashDialog = false })
    }

    if (showBackupsDialog) {
        BackupsDialog(viewModel = viewModel, onDismiss = { showBackupsDialog = false })
    }
}