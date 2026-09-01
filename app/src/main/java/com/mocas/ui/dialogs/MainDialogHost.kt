package com.mocas.ui.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mocas.ui.viewmodel.ScheduleViewModel
import com.mocas.ui.screens.AddEditSubjectScreen // Importante: Nueva ruta de la pantalla

@Composable
fun MainDialogHost(
    viewModel: ScheduleViewModel
) {
    val isAddSubjectOpen by viewModel.isAddSubjectOpen.collectAsStateWithLifecycle()
    val isAddEventOpen by viewModel.isAddEventOpen.collectAsStateWithLifecycle()
    val isImportScheduleOpen by viewModel.isImportScheduleOpen.collectAsStateWithLifecycle()
    val isGlobalSearchOpen by viewModel.isGlobalSearchOpen.collectAsStateWithLifecycle()
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    val selectedDetailId by viewModel.selectedSubjectDetailId.collectAsStateWithLifecycle()
    val subjectsWithSlots by viewModel.subjectsWithSlots.collectAsStateWithLifecycle()
    val allEventsWithSubject by viewModel.allEventsWithSubject.collectAsStateWithLifecycle()
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

    val selectedSubject = remember(
        selectedDetailId,
        subjectsWithSlots
    ) {
        subjectsWithSlots.firstOrNull {
            it.subject.id == selectedDetailId
        }
    }

    val linkedEvents = remember(
        selectedDetailId,
        allEventsWithSubject
    ) {
        if (selectedDetailId == null) {
            emptyList()
        } else {
            allEventsWithSubject.filter {
                it.event.subjectId == selectedDetailId
            }
        }
    }

    // Llamada actualizada a la nueva pantalla de Materia
    if (isAddSubjectOpen) {
        AddEditSubjectScreen(
            editingSubject = editingSubject,
            academicPeriods = academicPeriods,
            existingSubjects = subjectsWithSlots,
            onBack = viewModel::closeAddSubject, // Actualizado de onDismiss a onBack
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
            onDismiss = viewModel::closeAddEvent,
            onSave = viewModel::saveEvent
        )
    }

    if (isImportScheduleOpen && settings.aiFeaturesEnabled) {
        ImportScheduleDialog(
            viewModel = viewModel,
            onDismiss = viewModel::closeImportSchedule,
            onConfirmImport = { semesterStart, semesterEnd ->
                viewModel.confirmImportDetectedSchedule(
                    semesterStart = semesterStart,
                    semesterEnd = semesterEnd
                )
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

    if (
        selectedDetailId != null &&
        selectedSubject != null
    ) {
        SubjectDetailDialog(
            subjectWithSlots = selectedSubject,
            linkedEvents = linkedEvents,
            onDismiss = viewModel::closeSubjectDetail,
            onEditClick = {
                viewModel.closeSubjectDetail()
                viewModel.openAddSubject(selectedSubject)
            },
            onDeleteClick = {
                val subjectId = selectedSubject.subject.id
                viewModel.closeSubjectDetail()
                viewModel.deleteSubject(subjectId)
            },
            onAddEventClick = {
                val subjectId = selectedSubject.subject.id
                viewModel.closeSubjectDetail()
                viewModel.openAddEvent(subjectId = subjectId)
            },
            onToggleEventCompleted = { eventId, isCompleted ->
                viewModel.toggleEventCompleted(
                    eventId = eventId,
                    completed = isCompleted
                )
            },
            onToggleSubtask = viewModel::toggleSubtaskCompleted,
            onEditEvent = { event ->
                viewModel.closeSubjectDetail()
                viewModel.openAddEvent(event)
            },
            gradeCategories = gradeCategories.filter { it.subjectId == selectedSubject.subject.id },
            gradeItems = gradeItems,
            gradeUnits = gradeUnits.filter { it.subjectId == selectedSubject.subject.id },
            gradeUnitCategoryWeights = gradeUnitCategoryWeights,
            onAddGradeCategory = viewModel::addGradeCategory,
            onAddGradeItem = viewModel::addGradeItem,
            onDeleteGradeCategory = viewModel::deleteGradeCategory,
            onDeleteItem = viewModel::deleteGradeItem,
            onAddGradeUnit = viewModel::addGradeUnit,
            onDeleteGradeUnit = viewModel::deleteGradeUnit,
            onSaveUnitCategoryWeights = viewModel::saveUnitCategoryWeights,
            onResetUnitCategoryWeights = viewModel::resetUnitCategoryWeights
        )
    }
}