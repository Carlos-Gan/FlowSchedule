package com.mocas.ui.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mocas.ui.viewmodel.ScheduleViewModel

@Composable
fun MainDialogHost(
    viewModel: ScheduleViewModel
) {
    val isAddSubjectOpen by
    viewModel.isAddSubjectOpen.collectAsStateWithLifecycle()

    val isAddEventOpen by
    viewModel.isAddEventOpen.collectAsStateWithLifecycle()

    val isImportScheduleOpen by
    viewModel.isImportScheduleOpen.collectAsStateWithLifecycle()

    val isGlobalSearchOpen by
    viewModel.isGlobalSearchOpen.collectAsStateWithLifecycle()

    val settings by
    viewModel.appSettings.collectAsStateWithLifecycle()

    val selectedDetailId by
    viewModel.selectedSubjectDetailId.collectAsStateWithLifecycle()

    val subjectsWithSlots by
    viewModel.subjectsWithSlots.collectAsStateWithLifecycle()

    val allEventsWithSubject by
    viewModel.allEventsWithSubject.collectAsStateWithLifecycle()

    val academicPeriods by
    viewModel.academicPeriods.collectAsStateWithLifecycle()

    val editingSubject by
    viewModel.editingSubject.collectAsStateWithLifecycle()

    val editingEvent by
    viewModel.editingEvent.collectAsStateWithLifecycle()

    val newEventSubjectId by
    viewModel.newEventSubjectId.collectAsStateWithLifecycle()

    val newEventDate by
    viewModel.newEventDate.collectAsStateWithLifecycle()

    val newEventType by
    viewModel.newEventType.collectAsStateWithLifecycle()

    val newEventTitle by
    viewModel.newEventTitle.collectAsStateWithLifecycle()

    val selectedClassOccurrence by
    viewModel.selectedClassOccurrence.collectAsStateWithLifecycle()

    val gradeCategories by viewModel.gradeCategories.collectAsStateWithLifecycle()
    val gradeItems by viewModel.gradeItems.collectAsStateWithLifecycle()
    val gradeUnits by viewModel.gradeUnits.collectAsStateWithLifecycle()

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

    if (isAddSubjectOpen) {
        AddEditSubjectDialog(
            editingSubject = editingSubject,
            academicPeriods = academicPeriods,
            existingSubjects = subjectsWithSlots,
            onDismiss = viewModel::closeAddSubject,
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
            onConfirmImport = {
                    semesterStart,
                    semesterEnd ->

                viewModel.confirmImportDetectedSchedule(
                    semesterStart = semesterStart,
                    semesterEnd = semesterEnd
                )
            }
        )
    }

    if (isGlobalSearchOpen) {
        GlobalSearchDialog(
            subjects = subjectsWithSlots,
            events = allEventsWithSubject,
            onDismiss = viewModel::closeGlobalSearch,
            onSubjectClick = { subjectId ->
                viewModel.closeGlobalSearch()
                viewModel.openSubjectDetail(subjectId)
            },
            onEventClick = { event ->
                viewModel.closeGlobalSearch()
                viewModel.openAddEvent(eventToEdit = event)
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
                val subjectId =
                    selectedSubject.subject.id

                viewModel.closeSubjectDetail()
                viewModel.deleteSubject(subjectId)
            },
            onAddEventClick = {
                val subjectId =
                    selectedSubject.subject.id

                // Evita tener dos diálogos superpuestos.
                viewModel.closeSubjectDetail()

                viewModel.openAddEvent(
                    subjectId = subjectId
                )
            },
            onToggleEventCompleted = {
                    eventId,
                    isCompleted ->

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
            onAddGradeCategory = viewModel::addGradeCategory,
            onAddGradeItem = viewModel::addGradeItem,
            onDeleteGradeCategory = viewModel::deleteGradeCategory,
            onDeleteGradeItem = viewModel::deleteGradeItem,
            onAddGradeUnit = viewModel::addGradeUnit,
            onDeleteGradeUnit = viewModel::deleteGradeUnit
        )
    }
}
