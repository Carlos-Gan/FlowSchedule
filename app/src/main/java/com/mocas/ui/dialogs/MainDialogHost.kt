package com.mocas.ui.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mocas.ui.add.event.AddEditSubjectScreen
import com.mocas.ui.add.event.AddEventDialog
import com.mocas.ui.viewmodel.ScheduleViewModel
import com.mocas.ui.screens.AppearanceScreen
import com.mocas.ui.components.grade.RecordActivityGradeDialog
import com.mocas.ui.components.grade.GradesWorkspaceDialog

@Composable
fun MainDialogHost(
    viewModel: ScheduleViewModel
) {
    var gradeWorkspaceSubjectId by remember { mutableStateOf<Long?>(null) }
    val isAddSubjectOpen by viewModel.isAddSubjectOpen.collectAsStateWithLifecycle()
    val isAddEventOpen by viewModel.isAddEventOpen.collectAsStateWithLifecycle()
    val isAppearanceOpen by viewModel.isAppearanceOpen.collectAsStateWithLifecycle()
    val subjectsWithSlots by viewModel.subjectsWithSlots.collectAsStateWithLifecycle()
    val academicPeriods by viewModel.academicPeriods.collectAsStateWithLifecycle()
    val editingSubject by viewModel.editingSubject.collectAsStateWithLifecycle()
    val editingEvent by viewModel.editingEvent.collectAsStateWithLifecycle()
    val newEventSubjectId by viewModel.newEventSubjectId.collectAsStateWithLifecycle()
    val newEventDate by viewModel.newEventDate.collectAsStateWithLifecycle()
    val newEventType by viewModel.newEventType.collectAsStateWithLifecycle()
    val newEventTitle by viewModel.newEventTitle.collectAsStateWithLifecycle()
    val selectedClassOccurrence by viewModel.selectedClassOccurrence.collectAsStateWithLifecycle()

    val gradeCategories by viewModel.gradeCategories.collectAsStateWithLifecycle()
    val gradeItems by viewModel.gradeItems.collectAsStateWithLifecycle()
    val gradeUnits by viewModel.gradeUnits.collectAsStateWithLifecycle()
    val gradeUnitCategoryWeights by viewModel.gradeUnitCategoryWeights.collectAsStateWithLifecycle()
    val showWhatsNew by viewModel.showWhatsNew.collectAsStateWithLifecycle()
    val eventAwaitingGrade by viewModel.eventAwaitingGrade.collectAsStateWithLifecycle()

    if (showWhatsNew) {
        WhatsNewDialog(
            onDismiss = viewModel::dismissWhatsNew
        )
    }

    if (isAddSubjectOpen) {
        AddEditSubjectScreen(
            editingSubject = editingSubject,
            academicPeriods = academicPeriods,
            existingSubjects = subjectsWithSlots,
            onBack = viewModel::closeAddSubject,
            onSavePeriod = viewModel::saveAcademicPeriod,
            onSave = viewModel::saveSubject
        )
    }

    if (isAddEventOpen) {
        AddEventDialog(
            editingEvent = editingEvent,
            defaultSubjectId = newEventSubjectId,
            defaultDate = newEventDate,
            defaultType = newEventType,
            defaultTitle = newEventTitle,
            subjects = subjectsWithSlots,
            gradeUnits = gradeUnits,
            onManageGradeUnits = { gradeWorkspaceSubjectId = it },
            onDismiss = viewModel::closeAddEvent,
            onSave = viewModel::saveEvent
        )
    }

    if (isAppearanceOpen) {
        AppearanceScreen(
            viewModel = viewModel,
            onBack = viewModel::closeAppearance
        )
    }

    eventAwaitingGrade?.let { event ->
        val unit = gradeUnits.firstOrNull { it.id == event.event.gradeUnitId }
        RecordActivityGradeDialog(
            event = event,
            unit = unit,
            categories = gradeCategories.filter { it.subjectId == event.event.subjectId },
            onDismiss = viewModel::dismissGradeEntry,
            onMarkGradePending = { viewModel.completeEventWithoutGrade(event.event.id) },
            onSave = { categoryId, score ->
                viewModel.completeEventWithGrade(event.event.id, categoryId, score)
            }
        )
    }

    selectedClassOccurrence?.let { occurrence ->
        ClassExceptionDialog(
            occurrence = occurrence,
            onDismiss = viewModel::closeClassOccurrence,
            onSave = viewModel::saveClassException,
            onRestore = occurrence.exception?.let { { id -> viewModel.deleteClassException(id) } }
        )
    }

    gradeWorkspaceSubjectId?.let { subjectId ->
        GradesWorkspaceDialog(
            subjectId = subjectId,
            categories = gradeCategories.filter { it.subjectId == subjectId },
            gradeItems = gradeItems,
            units = gradeUnits.filter { it.subjectId == subjectId },
            unitCategoryWeights = gradeUnitCategoryWeights,
            onDismiss = { gradeWorkspaceSubjectId = null },
            onAddCategory = viewModel::addGradeCategory,
            onAddItem = viewModel::addGradeItem,
            onDeleteCategory = viewModel::deleteGradeCategory,
            onDeleteItem = viewModel::deleteGradeItem,
            onAddUnit = viewModel::addGradeUnit,
            onDeleteUnit = viewModel::deleteGradeUnit,
            onSaveUnitCategoryWeights = viewModel::saveUnitCategoryWeights,
            onResetUnitCategoryWeights = viewModel::resetUnitCategoryWeights
        )
    }
}
