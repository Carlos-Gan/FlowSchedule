package com.mocas.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.mocas.R
import com.mocas.data.backup.BackupImportSummary
import com.mocas.data.backup.ScheduleBackupCodec
import com.mocas.data.backup.ScheduleBackupData
import com.mocas.data.local.AcademicPeriodEntity
import com.mocas.data.local.ClassExceptionEntity
import com.mocas.data.local.ClassExceptionType
import com.mocas.data.local.AppDatabase
import com.mocas.data.local.ScheduleSlotEntity
import com.mocas.data.local.SchoolEventEntity
import com.mocas.data.local.SchoolEventWithSubject
import com.mocas.data.local.SubtaskEntity
import com.mocas.data.local.SubjectEntity
import com.mocas.data.local.SubjectWithSlots
import com.mocas.data.local.GradeCategoryEntity
import com.mocas.data.local.GradeItemEntity
import com.mocas.data.local.GradeUnitEntity
import com.mocas.data.local.GradeUnitCategoryWeightEntity
import com.mocas.util.DateTimeUtils
import kotlinx.coroutines.flow.Flow
import java.time.temporal.ChronoUnit
import java.util.Locale

class ScheduleRepository(private val context: Context, private val database: AppDatabase) {
    private val subjectDao = database.subjectDao()
    private val slotDao = database.scheduleSlotDao()
    private val eventDao = database.schoolEventDao()
    private val periodDao = database.academicPeriodDao()
    private val exceptionDao = database.classExceptionDao()
    private val subtaskDao = database.subtaskDao()
    private val gradeDao = database.gradeDao()

    val allSubjectsWithSlots: Flow<List<SubjectWithSlots>> = subjectDao.getAllSubjectsWithSlots()
    val allEventsWithSubject: Flow<List<SchoolEventWithSubject>> = eventDao.getAllEventsWithSubject()
    val allAcademicPeriods: Flow<List<AcademicPeriodEntity>> = periodDao.getAllPeriods()
    val allClassExceptions: Flow<List<ClassExceptionEntity>> = exceptionDao.getAll()
    val deletedSubjects: Flow<List<SubjectEntity>> = subjectDao.getDeletedSubjects()
    val deletedEvents: Flow<List<SchoolEventEntity>> = eventDao.getDeletedEvents()
    val gradeCategories: Flow<List<GradeCategoryEntity>> = gradeDao.observeCategories()
    val gradeItems: Flow<List<GradeItemEntity>> = gradeDao.observeItems()
    val gradeUnits: Flow<List<GradeUnitEntity>> = gradeDao.observeUnits()
    val gradeUnitCategoryWeights: Flow<List<GradeUnitCategoryWeightEntity>> = gradeDao.observeUnitCategoryWeights()

    suspend fun addGradeUnit(item: GradeUnitEntity): Long {
        require(item.name.isNotBlank()) { context.getString(R.string.error_nombre_unidad_obligatorio) }
        return gradeDao.insertUnit(item.copy(id = 0, name = item.name.trim()))
    }

    suspend fun deleteGradeUnit(item: GradeUnitEntity): Boolean = database.withTransaction {
        gradeDao.deleteItemsForUnit(item.id)
        gradeDao.deleteUnit(item) > 0
    }

    suspend fun saveUnitCategoryWeights(unitId: Long, weights: List<GradeUnitCategoryWeightEntity>) = database.withTransaction {
        require(weights.isNotEmpty()) { context.getString(R.string.agregar_corto) } // Reuse or add new
        require(weights.all { it.unitId == unitId && it.weightPercent >= 0.0 }) { context.getString(R.string.error_porcentajes_invalidos) }
        require(weights.sumOf { it.weightPercent } <= 100.0) { context.getString(R.string.error_porcentajes_superan_100) }
        gradeDao.deleteUnitCategoryWeights(unitId)
        gradeDao.insertUnitCategoryWeights(weights)
    }

    suspend fun resetUnitCategoryWeights(unitId: Long): Boolean =
        gradeDao.deleteUnitCategoryWeights(unitId) >= 0

    suspend fun addGradeCategory(item: GradeCategoryEntity): Long {
        require(item.name.isNotBlank()) { context.getString(R.string.error_categoria_obligatoria) }
        require(item.weightPercent > 0.0 && item.weightPercent <= 100.0) { context.getString(R.string.error_rango_porcentaje) }
        val assigned = gradeDao.getCategoriesForSubjectOnce(item.subjectId).sumOf { it.weightPercent }
        require(assigned + item.weightPercent <= 100.0) {
            context.getString(R.string.error_max_porcentaje_asignado, assigned.toInt())
        }
        return gradeDao.insertCategory(item.copy(id = 0, name = item.name.trim()))
    }

    suspend fun addGradeItem(item: GradeItemEntity): Long {
        require(item.name.isNotBlank()) { context.getString(R.string.error_evaluacion_obligatoria) }
        require(item.score in 0.0..100.0) { context.getString(R.string.error_calificacion_rango) }
        require(item.unitName.isNotBlank()) { context.getString(R.string.error_unidad_obligatoria) }
        require(item.unitId > 0) { context.getString(R.string.error_unidad_valida) }
        return gradeDao.insertItem(item.copy(id = 0, name = item.name.trim(), unitName = item.unitName.trim()))
    }

    suspend fun deleteGradeCategory(item: GradeCategoryEntity): Boolean = gradeDao.deleteCategory(item) > 0
    suspend fun deleteGradeItem(item: GradeItemEntity): Boolean = gradeDao.deleteItem(item) > 0

    suspend fun insertAcademicPeriod(period: AcademicPeriodEntity): Long =
        saveAcademicPeriod(period.copy(id = 0))

    suspend fun saveAcademicPeriod(period: AcademicPeriodEntity): Long = database.withTransaction {
        require(period.name.isNotBlank()) { context.getString(R.string.error_nombre_periodo_obligatorio) }
        val start = requireDate(period.startDate, context.getString(R.string.fecha_inicio_label))
        val end = requireDate(period.endDate, context.getString(R.string.fecha_fin_label))
        require(!end.isBefore(start)) { context.getString(R.string.error_hora_fin_posterior) }
        require(Regex("^#[0-9A-Fa-f]{6}$").matches(period.colorHex)) {
            context.getString(R.string.error_color_invalido)
        }
        val normalized = period.copy(
            name = period.name.trim(),
            startDate = start.toString(),
            endDate = end.toString(),
            colorHex = period.colorHex.uppercase(Locale.ROOT)
        )
        val overlaps = periodDao.getAllPeriodsOnce().any { saved ->
            if (saved.id == period.id) return@any false
            val savedStart = DateTimeUtils.parseDate(saved.startDate) ?: return@any false
            val savedEnd = DateTimeUtils.parseDate(saved.endDate) ?: return@any false
            !savedEnd.isBefore(start) && !end.isBefore(savedStart)
        }
        require(!overlaps) { context.getString(R.string.error_periodo_traslape) }

        if (period.id == 0L) {
            periodDao.insertPeriod(normalized.copy(id = 0))
        } else {
            val previous = requireNotNull(periodDao.getPeriodById(period.id)) {
                context.getString(R.string.error_periodo_no_existe)
            }
            check(periodDao.updatePeriod(normalized) > 0) { context.getString(R.string.msg_error_inesperado) }
            if (previous.startDate != normalized.startDate || previous.endDate != normalized.endDate) {
                subjectDao.updatePeriodDates(
                    oldStart = previous.startDate,
                    oldEnd = previous.endDate,
                    newStart = normalized.startDate,
                    newEnd = normalized.endDate,
                    updatedAtMillis = System.currentTimeMillis()
                )
            }
            period.id
        }
    }

    suspend fun deleteAcademicPeriod(periodId: Long): Boolean =
        periodDao.deletePeriod(periodId) > 0

    suspend fun copySubjectsBetweenPeriods(sourcePeriodId: Long, targetPeriodId: Long): Int =
        database.withTransaction {
            require(sourcePeriodId != targetPeriodId) { context.getString(R.string.error_unidades_diferentes) }
            val source = requireNotNull(periodDao.getPeriodById(sourcePeriodId)) {
                context.getString(R.string.error_periodo_no_existe)
            }
            val target = requireNotNull(periodDao.getPeriodById(targetPeriodId)) {
                context.getString(R.string.error_periodo_no_existe)
            }
            val allSubjects = subjectDao.getAllSubjectsWithSlotsOnce()
            val sourceSubjects = allSubjects.filter { item ->
                item.subject.semesterStart == source.startDate &&
                    item.subject.semesterEnd == source.endDate
            }
            val targetKeys = allSubjects.asSequence()
                .filter { item ->
                    item.subject.semesterStart == target.startDate &&
                        item.subject.semesterEnd == target.endDate
                }
                .map { item -> buildSubjectKey(item.subject.name, item.subject.professor) }
                .toMutableSet()

            var copied = 0
            sourceSubjects.forEach { item ->
                val key = buildSubjectKey(item.subject.name, item.subject.professor)
                if (key in targetKeys) return@forEach
                val subject = item.subject.copy(
                    id = 0,
                    semesterStart = target.startDate,
                    semesterEnd = target.endDate,
                    createdAtMillis = System.currentTimeMillis(),
                    updatedAtMillis = System.currentTimeMillis()
                )
                validateSubject(subject)
                val slots = validateAndPrepareSlots(
                    subjectId = 0,
                    slots = item.slots.map { it.copy(id = 0, subjectId = 0) }
                )
                ensureNoExternalConflicts(
                    slots = slots,
                    excludedSubjectId = -1,
                    semesterStart = target.startDate,
                    semesterEnd = target.endDate
                )
                val newSubjectId = subjectDao.insertSubject(subject)
                if (slots.isNotEmpty()) {
                    slotDao.insertSlots(slots.map { it.copy(subjectId = newSubjectId) })
                }
                targetKeys += key
                copied++
            }
            copied
        }

    suspend fun getSubjectWithSlots(subjectId: Long): SubjectWithSlots? =
        subjectDao.getSubjectWithSlotsById(subjectId)

    fun observeSubjectWithSlots(subjectId: Long): Flow<SubjectWithSlots?> =
        subjectDao.observeSubjectWithSlotsById(subjectId)

    suspend fun insertSubjectWithSlots(
        subject: SubjectEntity,
        slots: List<ScheduleSlotEntity>
    ): Long = database.withTransaction {
        validateSubject(subject)
        val preparedSlots = validateAndPrepareSlots(subjectId = 0, slots = slots)
        ensureNoExternalConflicts(
            preparedSlots,
            excludedSubjectId = -1,
            semesterStart = subject.semesterStart,
            semesterEnd = subject.semesterEnd
        )
        val subjectId = subjectDao.insertSubject(subject.copy(id = 0))
        if (preparedSlots.isNotEmpty()) {
            slotDao.insertSlots(preparedSlots.map { it.copy(subjectId = subjectId, id = 0) })
        }
        subjectId
    }

    suspend fun updateSubjectWithSlots(
        subject: SubjectEntity,
        slots: List<ScheduleSlotEntity>
    ) = database.withTransaction {
        require(subject.id > 0) { context.getString(R.string.error_materia_sin_id) }
        validateSubject(subject)
        val existingSlots = slotDao.getSlotsForSubjectOnce(subject.id)
        val existingIds = existingSlots.mapTo(mutableSetOf()) { it.id }
        val preparedSlots = validateAndPrepareSlots(subject.id, slots)
        require(preparedSlots.filter { it.id > 0 }.all { it.id in existingIds }) {
            context.getString(R.string.error_sesion_otra_materia)
        }
        ensureNoExternalConflicts(
            preparedSlots,
            excludedSubjectId = subject.id,
            semesterStart = subject.semesterStart,
            semesterEnd = subject.semesterEnd
        )
        check(
            subjectDao.updateSubject(subject.copy(updatedAtMillis = System.currentTimeMillis())) > 0
        ) { context.getString(R.string.error_materia_no_existe) }

        val incomingIds = preparedSlots.filter { it.id > 0 }.mapTo(mutableSetOf()) { it.id }
        existingSlots.filter { it.id !in incomingIds }.forEach { slotDao.deleteSlotById(it.id) }
        preparedSlots.forEach { slot ->
            if (slot.id == 0L) {
                slotDao.insertSlot(slot)
            } else {
                check(slotDao.updateSlot(slot) > 0) { "No se encontró el horario ${slot.id}." }
            }
        }
    }

    suspend fun deleteSubject(subjectId: Long): Boolean =
        subjectDao.setSubjectDeleted(subjectId, true, System.currentTimeMillis()) > 0

    suspend fun restoreSubject(subjectId: Long): Boolean =
        subjectDao.setSubjectDeleted(subjectId, false, null) > 0

    suspend fun permanentlyDeleteSubject(subjectId: Long): Boolean =
        subjectDao.deleteSubjectById(subjectId) > 0

    suspend fun insertEvent(
        event: SchoolEventEntity,
        subtasks: List<SubtaskEntity> = emptyList()
    ): Long = database.withTransaction {
        validateEvent(event)
        val now = System.currentTimeMillis()
        val preparedEvent = if (event.isCompleted && event.completedAtMillis == null) {
            event.copy(completedAtMillis = now, updatedAtMillis = now)
        } else event

        val occurrences = EventRecurrenceGenerator.generate(preparedEvent)
        var firstId = 0L
        occurrences.forEachIndexed { index, occurrence ->
            val eventId = eventDao.insertEvent(occurrence.copy(id = 0))
            if (index == 0) firstId = eventId
            insertSubtasksForEvent(eventId, subtasks)
        }
        firstId
    }

    suspend fun updateEvent(
        event: SchoolEventEntity,
        subtasks: List<SubtaskEntity> = emptyList()
    ): Boolean = database.withTransaction {
        require(event.id > 0) { context.getString(R.string.error_evento_sin_id) }
        validateEvent(event)
        val previous = requireNotNull(eventDao.getEventById(event.id)) { context.getString(R.string.error_evento_no_existe) }
        
        val now = System.currentTimeMillis()
        val completedAt = when {
            event.isCompleted && !previous.isCompleted -> now // Recién completado
            event.isCompleted -> previous.completedAtMillis // Ya estaba completado
            else -> null // No completado
        }
        
        val eventToSave = event.copy(
            completedAtMillis = completedAt,
            updatedAtMillis = now
        )

        val turnsIntoSeries = previous.recurrenceType == com.mocas.data.local.RecurrenceType.NONE &&
            event.recurrenceType != com.mocas.data.local.RecurrenceType.NONE
        if (turnsIntoSeries) {
            val occurrences = EventRecurrenceGenerator.generate(eventToSave.copy(id = 0))
            val first = occurrences.first().copy(
                id = event.id,
                createdAtMillis = previous.createdAtMillis,
                updatedAtMillis = now
            )
            check(eventDao.updateEvent(first) > 0)
            subtaskDao.deleteForEvent(event.id)
            insertSubtasksForEvent(event.id, subtasks)
            occurrences.drop(1).forEach { occurrence ->
                val newId = eventDao.insertEvent(occurrence)
                insertSubtasksForEvent(newId, subtasks)
            }
            true
        } else {
            val updated = eventDao.updateEvent(eventToSave) > 0
            if (updated) {
                subtaskDao.deleteForEvent(event.id)
                insertSubtasksForEvent(event.id, subtasks)
            }
            updated
        }
    }

    private suspend fun insertSubtasksForEvent(eventId: Long, items: List<SubtaskEntity>) {
        val prepared = items.mapIndexedNotNull { index, item ->
            item.title.trim().takeIf { it.isNotBlank() }?.let { title ->
                item.copy(id = 0, eventId = eventId, title = title, sortOrder = index)
            }
        }
        if (prepared.isNotEmpty()) subtaskDao.insertSubtasks(prepared)
    }


    suspend fun deleteEvent(eventId: Long): Boolean =
        eventDao.setEventDeleted(eventId, true, System.currentTimeMillis()) > 0

    suspend fun restoreEvent(eventId: Long): Boolean =
        eventDao.setEventDeleted(eventId, false, null) > 0

    suspend fun permanentlyDeleteEvent(eventId: Long): Boolean =
        eventDao.deleteEventById(eventId) > 0

    suspend fun emptyTrash() = database.withTransaction {
        eventDao.getDeletedEventsSnapshot().forEach { eventDao.deleteEventById(it.id) }
        subjectDao.getDeletedSubjectsSnapshot().forEach { subjectDao.deleteSubjectById(it.id) }
    }

    suspend fun purgeExpiredTrash(retentionDays: Long = 30) {
        val threshold = System.currentTimeMillis() - retentionDays * 24 * 60 * 60 * 1000
        database.withTransaction {
            eventDao.purgeDeletedEvents(threshold)
            subjectDao.purgeDeletedSubjects(threshold)
        }
    }

    suspend fun saveClassException(item: ClassExceptionEntity): Long = database.withTransaction {
        val date = requireDate(item.date, context.getString(R.string.fecha_label))
        val slot = requireNotNull(slotDao.getSlotById(item.slotId)) { context.getString(R.string.error_sesion_no_existe) }
        require(slot.subjectId == item.subjectId) { context.getString(R.string.error_sesion_otra_materia) }
        val normalized = if (item.type == ClassExceptionType.CANCELED) {
            item.copy(
                date = date.toString(),
                newStartTime = null,
                newEndTime = null,
                newRoom = null,
                updatedAtMillis = System.currentTimeMillis()
            )
        } else {
            val start = requireNotNull(item.newStartTime) { "Selecciona la nueva hora de inicio." }
            val end = requireNotNull(item.newEndTime) { "Selecciona la nueva hora de fin." }
            require(DateTimeUtils.endIsAfterStart(start, end)) {
                "La hora final debe ser posterior a la inicial."
            }
            ensureNoOccurrenceConflict(item, date, start, end)
            item.copy(date = date.toString(), updatedAtMillis = System.currentTimeMillis())
        }
        val existing = exceptionDao.getForOccurrence(item.slotId, date.toString())
        if (existing == null) exceptionDao.insert(normalized.copy(id = 0))
        else {
            check(exceptionDao.update(normalized.copy(id = existing.id, createdAtMillis = existing.createdAtMillis)) > 0)
            existing.id
        }
    }

    suspend fun deleteClassException(id: Long): Boolean = exceptionDao.delete(id) > 0

    suspend fun setEventCompleted(eventId: Long, completed: Boolean): Boolean = database.withTransaction {
        val now = System.currentTimeMillis()
        val completedAt = if (completed) now else null
        val changed = eventDao.setEventCompleted(eventId, completed, now, completedAt) > 0
        if (changed) subtaskDao.setAllCompletedForEvent(eventId, completed, now)
        changed
    }

    suspend fun postponeEventByDays(eventId: Long, days: Long = 1): Boolean = database.withTransaction {
        require(days > 0) { context.getString(R.string.error_posponer_futuro) }
        val event = requireNotNull(eventDao.getEventById(eventId)) { context.getString(R.string.error_evento_no_existe) }
        val start = requireDate(event.startDate, context.getString(R.string.fecha_inicio_label))
        val end = requireDate(event.endDate, context.getString(R.string.fecha_fin_label))
        val durationDays = ChronoUnit.DAYS.between(start, end)
        val newStart = start.plusDays(days)
        val newEnd = newStart.plusDays(durationDays)
        eventDao.updateEventDates(
            eventId = eventId,
            startDate = newStart.toString(),
            endDate = newEnd.toString(),
            updatedAtMillis = System.currentTimeMillis()
        ) > 0
    }

    suspend fun setSubtaskCompleted(eventId: Long, subtaskId: Long, completed: Boolean): Boolean =
        database.withTransaction {
            val now = System.currentTimeMillis()
            val changed = subtaskDao.setCompleted(subtaskId, completed, now) > 0
            if (!changed) return@withTransaction false
            val subtasks = subtaskDao.getForEvent(eventId)
            if (subtasks.isNotEmpty()) {
                val allCompleted = subtasks.all { it.isCompleted }
                eventDao.setEventCompleted(eventId, allCompleted, now, if (allCompleted) now else null)
            }
            true
        }

    suspend fun exportScheduleBackup(): String = ScheduleBackupCodec.encode(
        ScheduleBackupData(
            periods = periodDao.getAllPeriodsOnce(),
            subjects = subjectDao.getAllSubjectsWithSlotsOnce(),
            events = eventDao.getAllEventsWithSubjectOnce().map { it.event },
            exceptions = exceptionDao.getAllOnce(),
            subtasks = eventDao.getAllEventsWithSubjectOnce().flatMap { it.subtasks },
            gradeCategories = gradeDao.getCategoriesOnce(),
            gradeItems = gradeDao.getItemsOnce(),
            gradeUnits = gradeDao.getUnitsOnce(),
            gradeUnitCategoryWeights = gradeDao.getUnitCategoryWeightsOnce()
        )
    )

    suspend fun importScheduleBackup(json: String): BackupImportSummary = database.withTransaction {
        val backup = ScheduleBackupCodec.decode(context, json)
        validateBackup(backup)

        exceptionDao.clearAll()
        gradeDao.clearUnitCategoryWeights()
        gradeDao.clearItems()
        gradeDao.clearCategories()
        gradeDao.clearUnits()
        eventDao.clearAllEvents()
        subjectDao.clearAllSubjects()
        periodDao.clearAllPeriods()

        backup.periods.forEach { period ->
            periodDao.insertPeriod(period.copy(id = 0))
        }

        val subjectIds = mutableMapOf<Long, Long>()
        val slotIds = mutableMapOf<Long, Long>()
        backup.subjects.forEach { item ->
            val oldSubjectId = item.subject.id
            val newSubjectId = subjectDao.insertSubject(
                item.subject.copy(id = 0, syncCalendar = false)
            )
            subjectIds[oldSubjectId] = newSubjectId
            item.slots.forEach { slot ->
                val newSlotId = slotDao.insertSlot(
                    slot.copy(
                        id = 0,
                        subjectId = newSubjectId,
                        calendarEventId = null,
                        calendarId = null,
                        lastCalendarSyncMillis = null
                    )
                )
                slotIds[slot.id] = newSlotId
            }
        }

        val categoryIds = mutableMapOf<Long, Long>()
        backup.gradeCategories.forEach { category ->
            val newId = gradeDao.insertCategory(
                category.copy(
                    id = 0,
                    subjectId = requireNotNull(subjectIds[category.subjectId]) {
                        "Una categoría de calificación apunta a una materia inexistente."
                    }
                )
            )
            categoryIds[category.id] = newId
        }
        val unitIds = mutableMapOf<Long, Long>()
        val unitsToRestore = backup.gradeUnits.ifEmpty {
            backup.gradeItems.distinctBy { it.categoryId to it.unitName }
                .mapIndexed { index, item ->
                    val oldSubjectId =
                        backup.gradeCategories.first { it.id == item.categoryId }.subjectId
                    GradeUnitEntity(
                        id = -(index + 1L),
                        subjectId = oldSubjectId,
                        name = item.unitName,
                        sortOrder = index
                    )
                }.distinctBy { it.subjectId to it.name }
        }
        unitsToRestore.forEach { unit ->
            val newId = gradeDao.insertUnit(
                unit.copy(id = 0, subjectId = requireNotNull(subjectIds[unit.subjectId]))
            )
            unitIds[unit.id] = newId
        }
        val restoredWeights = backup.gradeUnitCategoryWeights.mapNotNull { weight ->
            val newUnitId = unitIds[weight.unitId] ?: return@mapNotNull null
            val newCategoryId = categoryIds[weight.categoryId] ?: return@mapNotNull null
            GradeUnitCategoryWeightEntity(
                unitId = newUnitId,
                categoryId = newCategoryId,
                weightPercent = weight.weightPercent
            )
        }
        if (restoredWeights.isNotEmpty()) {
            gradeDao.insertUnitCategoryWeights(restoredWeights)
        }
        backup.gradeItems.forEach { item ->
            val oldUnitId = item.unitId.takeIf { it > 0 } ?: unitsToRestore.first {
                it.subjectId == backup.gradeCategories.first { category -> category.id == item.categoryId }.subjectId &&
                    it.name == item.unitName
            }.id
            gradeDao.insertItem(
                item.copy(
                    id = 0,
                    categoryId = requireNotNull(categoryIds[item.categoryId]) {
                        "Una calificación apunta a una categoría inexistente."
                    },
                    unitId = requireNotNull(unitIds[oldUnitId]) { "Una calificación apunta a una unidad inexistente." }
                )
            )
        }

        val eventIds = mutableMapOf<Long, Long>()
        backup.events.forEach { event ->
            val mappedSubjectId = event.subjectId?.let { oldId ->
                requireNotNull(subjectIds[oldId]) { "Una actividad apunta a una materia inexistente." }
            }
            val newEventId = eventDao.insertEvent(
                event.copy(
                    id = 0,
                    subjectId = mappedSubjectId,
                    syncCalendar = false,
                    calendarEventId = null,
                    calendarId = null,
                    lastCalendarSyncMillis = null
                )
            )
            eventIds[event.id] = newEventId
        }

        val restoredSubtasks = backup.subtasks.map { item ->
            item.copy(
                id = 0,
                eventId = requireNotNull(eventIds[item.eventId]) {
                    "Una subtarea apunta a una actividad inexistente."
                }
            )
        }
        if (restoredSubtasks.isNotEmpty()) subtaskDao.insertSubtasks(restoredSubtasks)

        backup.exceptions.forEach { exception ->
            exceptionDao.insert(
                exception.copy(
                    id = 0,
                    subjectId = requireNotNull(subjectIds[exception.subjectId]) {
                        "Una excepción apunta a una materia inexistente."
                    },
                    slotId = requireNotNull(slotIds[exception.slotId]) {
                        "Una excepción apunta a una sesión inexistente."
                    }
                )
            )
        }

        BackupImportSummary(
            subjects = backup.subjects.size,
            sessions = backup.subjects.sumOf { it.slots.size },
            activities = backup.events.size,
            periods = backup.periods.size
        )
    }

    suspend fun clearAll() = database.withTransaction {
        exceptionDao.clearAll()
        gradeDao.clearUnitCategoryWeights()
        gradeDao.clearItems()
        gradeDao.clearCategories()
        gradeDao.clearUnits()
        eventDao.clearAllEvents()
        subjectDao.clearAllSubjects()
        periodDao.clearAllPeriods()
    }

    private fun validateBackup(backup: ScheduleBackupData) {
        require(backup.subjects.map { it.subject.id }.distinct().size == backup.subjects.size) {
            context.getString(R.string.error_materias_duplicadas)
        }
        val allSlots = backup.subjects.flatMap { it.slots }
        require(allSlots.map { it.id }.distinct().size == allSlots.size) {
            context.getString(R.string.error_sesiones_duplicadas)
        }
        backup.periods.forEach { period ->
            require(period.name.isNotBlank()) { context.getString(R.string.error_periodo_sin_nombre) }
            val start = requireDate(period.startDate, context.getString(R.string.inicio_periodo_label))
            val end = requireDate(period.endDate, context.getString(R.string.fin_periodo_label))
            require(!end.isBefore(start)) { context.getString(R.string.error_periodo_fechas_invalidas) }
            require(Regex("^#[0-9A-Fa-f]{6}$").matches(period.colorHex)) {
                context.getString(R.string.error_color_invalido)
            }
        }
        backup.subjects.forEach { item ->
            validateSubject(item.subject)
            validateAndPrepareSlots(item.subject.id, item.slots)
            require(item.slots.all { it.subjectId == item.subject.id }) {
                context.getString(R.string.error_sesion_no_pertenece_materia)
            }
        }
        val subjectIds = backup.subjects.mapTo(mutableSetOf()) { it.subject.id }
        val slotIds = allSlots.mapTo(mutableSetOf()) { it.id }
        val subjectBySlotId = allSlots.associate { it.id to it.subjectId }
        backup.events.forEach { event ->
            validateEvent(event)
            require(event.subjectId == null || event.subjectId in subjectIds) {
                context.getString(R.string.error_materia_no_existe)
            }
        }
        val eventIds = backup.events.mapTo(mutableSetOf()) { it.id }
        require(backup.subtasks.map { it.id }.distinct().size == backup.subtasks.size) {
            context.getString(R.string.error_sesiones_duplicadas) // Reuse sessions duplicated for subtasks too?
        }
        backup.subtasks.forEach { item ->
            require(item.eventId in eventIds) { context.getString(R.string.error_evento_no_existe) }
            require(item.title.isNotBlank()) { context.getString(R.string.error_subtarea_sin_titulo) }
        }
        val categoryIds = backup.gradeCategories.mapTo(mutableSetOf()) { it.id }
        val unitIds = backup.gradeUnits.mapTo(mutableSetOf()) { it.id }
        backup.gradeUnits.forEach { unit ->
            require(unit.subjectId in subjectIds && unit.name.isNotBlank()) { context.getString(R.string.error_unidad_respaldo_invalida) }
        }
        backup.gradeCategories.forEach { category ->
            require(category.subjectId in subjectIds) { context.getString(R.string.error_materia_no_existe) }
            require(category.name.isNotBlank() && category.weightPercent > 0 && category.weightPercent <= 100) {
                context.getString(R.string.error_categoria_respaldo_invalida)
            }
        }
        backup.gradeUnitCategoryWeights.forEach { weight ->
            require(weight.unitId in unitIds && weight.categoryId in categoryIds) {
                context.getString(R.string.error_ponderacion_referencias_invalidas)
            }
            require(weight.weightPercent in 0.0..100.0) {
                context.getString(R.string.error_porcentaje_ponderacion_invalido)
            }
        }
        backup.gradeItems.forEach { item ->
            require(item.categoryId in categoryIds && (item.unitId == 0L || item.unitId in unitIds) &&
                item.name.isNotBlank() && item.unitName.isNotBlank() && item.score in 0.0..100.0) {
                context.getString(R.string.error_calificacion_respaldo_invalida)
            }
        }
        backup.exceptions.forEach { exception ->
            require(exception.subjectId in subjectIds && exception.slotId in slotIds) {
                context.getString(R.string.error_excepcion_referencias_inexistentes)
            }
            require(subjectBySlotId[exception.slotId] == exception.subjectId) {
                context.getString(R.string.error_excepcion_no_pertenece_materia)
            }
            requireDate(exception.date, context.getString(R.string.fecha_label))
            if (exception.type == ClassExceptionType.MODIFIED) {
                require(
                    !exception.newStartTime.isNullOrBlank() &&
                        !exception.newEndTime.isNullOrBlank() &&
                        DateTimeUtils.endIsAfterStart(exception.newStartTime, exception.newEndTime)
                ) { context.getString(R.string.error_excepcion_horario_invalido) }
            }
        }
    }

    private suspend fun ensureNoOccurrenceConflict(
        edited: ClassExceptionEntity,
        date: java.time.LocalDate,
        startTime: String,
        endTime: String
    ) {
        val exceptions = exceptionDao.getAllOnce().associateBy { it.slotId to it.date }
        subjectDao.getAllSubjectsWithSlotsOnce().forEach { subject ->
            val periodStart = DateTimeUtils.parseDate(subject.subject.semesterStart)
            val periodEnd = DateTimeUtils.parseDate(subject.subject.semesterEnd)
            if (periodStart == null || periodEnd == null || date !in periodStart..periodEnd) return@forEach
            subject.slots.filter { it.dayOfWeek == date.dayOfWeek.value && it.id != edited.slotId }
                .forEach { otherSlot ->
                    val otherException = exceptions[otherSlot.id to date.toString()]
                    if (otherException?.type == ClassExceptionType.CANCELED) return@forEach
                    val otherStart = otherException?.newStartTime ?: otherSlot.startTime
                    val otherEnd = otherException?.newEndTime ?: otherSlot.endTime
                    require(startTime >= otherEnd || endTime <= otherStart) {
                        "El cambio se cruza con ${subject.subject.name} ($otherStart-$otherEnd)."
                    }
                }
        }
    }

    private fun validateSubject(subject: SubjectEntity) {
        require(subject.name.isNotBlank()) { context.getString(R.string.error_nombre_materia_obligatorio) }
        val start = requireDate(subject.semesterStart, context.getString(R.string.inicio_periodo_label))
        val end = requireDate(subject.semesterEnd, context.getString(R.string.fin_periodo_label))
        require(!end.isBefore(start)) { context.getString(R.string.error_semestre_fin_antes_inicio) }
        require(subject.reminderMinutesBefore >= 0) { context.getString(R.string.error_recordatorio_negativo) }
    }

    private fun validateEvent(event: SchoolEventEntity) {
        require(event.title.isNotBlank()) { context.getString(R.string.error_titulo_evento_obligatorio) }
        val startDate = requireDate(event.startDate, context.getString(R.string.fecha_inicio_label))
        val endDate = requireDate(event.endDate, context.getString(R.string.fecha_fin_label))
        require(!endDate.isBefore(startDate)) { context.getString(R.string.error_fecha_fin_antes_inicio) }
        require(event.reminderMinutes >= 0) { context.getString(R.string.error_recordatorio_negativo) }
        if (!event.isAllDay) {
            val startTime = event.startTime
            val endTime = event.endTime
            require(!startTime.isNullOrBlank() && DateTimeUtils.isValidTime(startTime)) {
                context.getString(R.string.error_formato_hora_invalido)
            }
            require(!endTime.isNullOrBlank() && DateTimeUtils.isValidTime(endTime)) {
                context.getString(R.string.error_formato_hora_invalido)
            }
            if (startDate == endDate) {
                require(DateTimeUtils.endIsAfterStart(startTime, endTime)) {
                    context.getString(R.string.error_hora_fin_posterior)
                }
            }
        }
    }

    private fun validateAndPrepareSlots(
        subjectId: Long,
        slots: List<ScheduleSlotEntity>
    ): List<ScheduleSlotEntity> {
        val prepared = slots.map { slot ->
            require(slot.dayOfWeek in 1..7) { context.getString(R.string.error_dia_semana_rango) }
            require(DateTimeUtils.endIsAfterStart(slot.startTime, slot.endTime)) {
                context.getString(R.string.error_hora_fin_posterior)
            }
            slot.copy(subjectId = subjectId)
        }
        val exactKeys = prepared.map { Triple(it.dayOfWeek, it.startTime, it.endTime) }
        require(exactKeys.distinct().size == exactKeys.size) { context.getString(R.string.error_horarios_duplicados) }
        prepared.groupBy { it.dayOfWeek }.values.forEach { daySlots ->
            daySlots.sortedBy { it.startTime }.zipWithNext().forEach { (first, second) ->
                require(first.endTime <= second.startTime) { context.getString(R.string.error_horarios_traslapados) }
            }
        }
        return prepared
    }

    private suspend fun ensureNoExternalConflicts(
        slots: List<ScheduleSlotEntity>,
        excludedSubjectId: Long,
        semesterStart: String,
        semesterEnd: String
    ) {
        slots.forEach { slot ->
            require(
                !slotDao.hasScheduleConflictExcludingSubject(
                    slot.dayOfWeek,
                    slot.startTime,
                    slot.endTime,
                    excludedSubjectId,
                    semesterStart,
                    semesterEnd
                )
            ) { context.getString(R.string.error_horario_traslape_externo, slot.startTime, slot.endTime) }
        }
    }

    private fun requireDate(value: String, label: String) =
        requireNotNull(DateTimeUtils.parseDate(value)) { context.getString(R.string.error_formato_fecha_invalido, label) }

    private fun buildSubjectKey(name: String, professor: String): String =
        "${normalize(name)}|${normalize(professor)}"

    private fun buildPeriodSubjectKey(
        name: String,
        professor: String,
        semesterStart: String,
        semesterEnd: String
    ): String = "${buildSubjectKey(name, professor)}|$semesterStart|$semesterEnd"

    private fun normalize(value: String): String = value
        .trim()
        .lowercase(Locale.ROOT)
        .replace(Regex("\\s+"), " ")
}
