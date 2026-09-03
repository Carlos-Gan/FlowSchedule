package com.mocas.data.repository

import androidx.room.withTransaction
import com.mocas.data.backup.BackupImportSummary
import com.mocas.data.backup.ScheduleBackupCodec
import com.mocas.data.backup.ScheduleBackupData
import com.mocas.data.ai.DetectedSubjectItem
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

class ScheduleRepository(private val database: AppDatabase) {
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
        require(item.name.isNotBlank()) { "El nombre de la unidad es obligatorio." }
        return gradeDao.insertUnit(item.copy(id = 0, name = item.name.trim()))
    }

    suspend fun deleteGradeUnit(item: GradeUnitEntity): Boolean = database.withTransaction {
        gradeDao.deleteItemsForUnit(item.id)
        gradeDao.deleteUnit(item) > 0
    }

    suspend fun saveUnitCategoryWeights(unitId: Long, weights: List<GradeUnitCategoryWeightEntity>) = database.withTransaction {
        require(weights.isNotEmpty()) { "Agrega al menos un porcentaje." }
        require(weights.all { it.unitId == unitId && it.weightPercent >= 0.0 }) { "Los porcentajes no son válidos." }
        require(weights.sumOf { it.weightPercent } <= 100.0) { "Los porcentajes no pueden superar el 100%." }
        gradeDao.deleteUnitCategoryWeights(unitId)
        gradeDao.insertUnitCategoryWeights(weights)
    }

    suspend fun resetUnitCategoryWeights(unitId: Long): Boolean =
        gradeDao.deleteUnitCategoryWeights(unitId) >= 0

    suspend fun addGradeCategory(item: GradeCategoryEntity): Long {
        require(item.name.isNotBlank()) { "El nombre de la categoría es obligatorio." }
        require(item.weightPercent > 0.0 && item.weightPercent <= 100.0) { "El porcentaje debe estar entre 0 y 100." }
        val assigned = gradeDao.getCategoriesForSubjectOnce(item.subjectId).sumOf { it.weightPercent }
        require(assigned + item.weightPercent <= 100.0) {
            "Las categorías no pueden superar el 100%. Ya tienes ${assigned.toInt()}% asignado."
        }
        return gradeDao.insertCategory(item.copy(id = 0, name = item.name.trim()))
    }

    suspend fun addGradeItem(item: GradeItemEntity): Long {
        require(item.name.isNotBlank()) { "El nombre de la evaluación es obligatorio." }
        require(item.score in 0.0..100.0) { "La calificación debe estar entre 0 y 100." }
        require(item.unitName.isNotBlank()) { "La unidad es obligatoria." }
        require(item.unitId > 0) { "Selecciona una unidad válida." }
        return gradeDao.insertItem(item.copy(id = 0, name = item.name.trim(), unitName = item.unitName.trim()))
    }

    suspend fun deleteGradeCategory(item: GradeCategoryEntity): Boolean = gradeDao.deleteCategory(item) > 0
    suspend fun deleteGradeItem(item: GradeItemEntity): Boolean = gradeDao.deleteItem(item) > 0

    suspend fun insertAcademicPeriod(period: AcademicPeriodEntity): Long =
        saveAcademicPeriod(period.copy(id = 0))

    suspend fun saveAcademicPeriod(period: AcademicPeriodEntity): Long = database.withTransaction {
        require(period.name.isNotBlank()) { "El nombre del periodo es obligatorio." }
        val start = requireDate(period.startDate, "inicio del periodo")
        val end = requireDate(period.endDate, "fin del periodo")
        require(!end.isBefore(start)) { "El fin del periodo no puede ser anterior al inicio." }
        require(Regex("^#[0-9A-Fa-f]{6}$").matches(period.colorHex)) {
            "El color del periodo no es válido."
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
        require(!overlaps) { "Este periodo se cruza con otro periodo guardado." }

        if (period.id == 0L) {
            periodDao.insertPeriod(normalized.copy(id = 0))
        } else {
            val previous = requireNotNull(periodDao.getPeriodById(period.id)) {
                "El periodo ya no existe."
            }
            check(periodDao.updatePeriod(normalized) > 0) { "No se pudo actualizar el periodo." }
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
            require(sourcePeriodId != targetPeriodId) { "Elige dos periodos diferentes." }
            val source = requireNotNull(periodDao.getPeriodById(sourcePeriodId)) {
                "El periodo de origen ya no existe."
            }
            val target = requireNotNull(periodDao.getPeriodById(targetPeriodId)) {
                "El periodo de destino ya no existe."
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
        require(subject.id > 0) { "No se puede actualizar una materia sin ID." }
        validateSubject(subject)
        val existingSlots = slotDao.getSlotsForSubjectOnce(subject.id)
        val existingIds = existingSlots.mapTo(mutableSetOf()) { it.id }
        val preparedSlots = validateAndPrepareSlots(subject.id, slots)
        require(preparedSlots.filter { it.id > 0 }.all { it.id in existingIds }) {
            "Uno de los horarios ya no pertenece a esta materia."
        }
        ensureNoExternalConflicts(
            preparedSlots,
            excludedSubjectId = subject.id,
            semesterStart = subject.semesterStart,
            semesterEnd = subject.semesterEnd
        )
        check(
            subjectDao.updateSubject(subject.copy(updatedAtMillis = System.currentTimeMillis())) > 0
        ) { "No se encontró la materia con ID ${subject.id}." }

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
        require(event.id > 0) { "No se puede actualizar un evento sin ID." }
        validateEvent(event)
        val previous = requireNotNull(eventDao.getEventById(event.id)) { "La actividad ya no existe." }
        
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
        val date = requireDate(item.date, "fecha de la excepción")
        val slot = requireNotNull(slotDao.getSlotById(item.slotId)) { "La sesión ya no existe." }
        require(slot.subjectId == item.subjectId) { "La sesión no pertenece a esta materia." }
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
        require(days > 0) { "Solo se puede posponer hacia una fecha futura." }
        val event = requireNotNull(eventDao.getEventById(eventId)) { "La actividad ya no existe." }
        val start = requireDate(event.startDate, "fecha inicial")
        val end = requireDate(event.endDate, "fecha final")
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

    suspend fun importDetectedSubjects(
        items: List<DetectedSubjectItem>,
        semesterStart: String,
        semesterEnd: String
    ) = database.withTransaction {
        val start = requireDate(semesterStart, "inicio del semestre")
        val end = requireDate(semesterEnd, "fin del semestre")
        require(!end.isBefore(start)) { "El fin del semestre no puede ser anterior al inicio." }

        val groupedItems = items.asSequence()
            .filter { it.isSelected && it.name.isNotBlank() }
            .onEach { item ->
                require(item.dayOfWeek in 1..7) { "Día inválido para ${item.name}." }
                require(DateTimeUtils.endIsAfterStart(item.startTime, item.endTime)) {
                    "Horario inválido para ${item.name}."
                }
            }
            .groupBy { buildSubjectKey(it.name, it.professor) }

        val subjectsByKey = subjectDao.getAllSubjectsOnce()
            .associateByTo(mutableMapOf()) {
                buildPeriodSubjectKey(
                    it.name,
                    it.professor,
                    it.semesterStart,
                    it.semesterEnd
                )
            }

        groupedItems.forEach { (key, subjectItems) ->
            val first = subjectItems.first()
            val periodKey = "$key|$semesterStart|$semesterEnd"
            val existingSubject = subjectsByKey[periodKey]
            val candidateSlots = subjectItems.map { item ->
                ScheduleSlotEntity(
                    subjectId = existingSubject?.id ?: 0,
                    dayOfWeek = item.dayOfWeek,
                    startTime = item.startTime,
                    endTime = item.endTime,
                    room = item.room.trim()
                )
            }.distinctBy { Triple(it.dayOfWeek, it.startTime, it.endTime) }

            if (existingSubject == null) {
                val prepared = validateAndPrepareSlots(0, candidateSlots)
                ensureNoExternalConflicts(prepared, -1, semesterStart, semesterEnd)
                val subject = SubjectEntity(
                    name = first.name.trim(),
                    professor = first.professor.trim(),
                    defaultRoom = first.room.trim(),
                    colorHex = first.colorHex,
                    semesterStart = semesterStart,
                    semesterEnd = semesterEnd
                )
                val subjectId = subjectDao.insertSubject(subject)
                if (prepared.isNotEmpty()) {
                    slotDao.insertSlots(prepared.map { it.copy(id = 0, subjectId = subjectId) })
                }
                subjectsByKey[periodKey] = subject.copy(id = subjectId)
            } else {
                val currentSlots = slotDao.getSlotsForSubjectOnce(existingSubject.id)
                val exactKeys = currentSlots.mapTo(mutableSetOf()) {
                    Triple(it.dayOfWeek, it.startTime, it.endTime)
                }
                val newSlots = candidateSlots.filter {
                    Triple(it.dayOfWeek, it.startTime, it.endTime) !in exactKeys
                }.map { it.copy(subjectId = existingSubject.id) }
                validateAndPrepareSlots(existingSubject.id, currentSlots + newSlots)
                ensureNoExternalConflicts(
                    newSlots,
                    existingSubject.id,
                    existingSubject.semesterStart,
                    existingSubject.semesterEnd
                )
                if (newSlots.isNotEmpty()) slotDao.insertSlots(newSlots)
            }
        }
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
        val backup = ScheduleBackupCodec.decode(json)
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
        val unitsToRestore = if (backup.gradeUnits.isNotEmpty()) {
            backup.gradeUnits
        } else {
            backup.gradeItems.distinctBy { it.categoryId to it.unitName }.mapIndexed { index, item ->
                val oldSubjectId = backup.gradeCategories.first { it.id == item.categoryId }.subjectId
                GradeUnitEntity(id = -(index + 1L), subjectId = oldSubjectId, name = item.unitName, sortOrder = index)
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
            "El respaldo contiene materias duplicadas."
        }
        val allSlots = backup.subjects.flatMap { it.slots }
        require(allSlots.map { it.id }.distinct().size == allSlots.size) {
            "El respaldo contiene sesiones duplicadas."
        }
        backup.periods.forEach { period ->
            require(period.name.isNotBlank()) { "Hay un periodo sin nombre en el respaldo." }
            val start = requireDate(period.startDate, "inicio del periodo")
            val end = requireDate(period.endDate, "fin del periodo")
            require(!end.isBefore(start)) { "Hay un periodo con fechas inválidas." }
            require(Regex("^#[0-9A-Fa-f]{6}$").matches(period.colorHex)) {
                "Hay un periodo con color inválido."
            }
        }
        backup.subjects.forEach { item ->
            validateSubject(item.subject)
            validateAndPrepareSlots(item.subject.id, item.slots)
            require(item.slots.all { it.subjectId == item.subject.id }) {
                "Una sesión no pertenece a su materia."
            }
        }
        val subjectIds = backup.subjects.mapTo(mutableSetOf()) { it.subject.id }
        val slotIds = allSlots.mapTo(mutableSetOf()) { it.id }
        val subjectBySlotId = allSlots.associate { it.id to it.subjectId }
        backup.events.forEach { event ->
            validateEvent(event)
            require(event.subjectId == null || event.subjectId in subjectIds) {
                "Una actividad apunta a una materia inexistente."
            }
        }
        val eventIds = backup.events.mapTo(mutableSetOf()) { it.id }
        require(backup.subtasks.map { it.id }.distinct().size == backup.subtasks.size) {
            "El respaldo contiene subtareas duplicadas."
        }
        backup.subtasks.forEach { item ->
            require(item.eventId in eventIds) { "Una subtarea apunta a una actividad inexistente." }
            require(item.title.isNotBlank()) { "El respaldo contiene una subtarea sin título." }
        }
        val categoryIds = backup.gradeCategories.mapTo(mutableSetOf()) { it.id }
        val unitIds = backup.gradeUnits.mapTo(mutableSetOf()) { it.id }
        backup.gradeUnits.forEach { unit ->
            require(unit.subjectId in subjectIds && unit.name.isNotBlank()) { "El respaldo contiene una unidad inválida." }
        }
        backup.gradeCategories.forEach { category ->
            require(category.subjectId in subjectIds) { "Una categoría apunta a una materia inexistente." }
            require(category.name.isNotBlank() && category.weightPercent > 0 && category.weightPercent <= 100) {
                "El respaldo contiene una categoría de calificación inválida."
            }
        }
        backup.gradeUnitCategoryWeights.forEach { weight ->
            require(weight.unitId in unitIds && weight.categoryId in categoryIds) {
                "El respaldo contiene una ponderación por unidad con referencias inválidas."
            }
            require(weight.weightPercent in 0.0..100.0) {
                "El respaldo contiene un porcentaje de ponderación inválido."
            }
        }
        backup.gradeItems.forEach { item ->
            require(item.categoryId in categoryIds && (item.unitId == 0L || item.unitId in unitIds) &&
                item.name.isNotBlank() && item.unitName.isNotBlank() && item.score in 0.0..100.0) {
                "El respaldo contiene una calificación inválida."
            }
        }
        backup.exceptions.forEach { exception ->
            require(exception.subjectId in subjectIds && exception.slotId in slotIds) {
                "Una excepción contiene referencias inexistentes."
            }
            require(subjectBySlotId[exception.slotId] == exception.subjectId) {
                "Una excepción no pertenece a la materia indicada."
            }
            requireDate(exception.date, "fecha de la excepción")
            if (exception.type == ClassExceptionType.MODIFIED) {
                require(
                    !exception.newStartTime.isNullOrBlank() &&
                        !exception.newEndTime.isNullOrBlank() &&
                        DateTimeUtils.endIsAfterStart(exception.newStartTime, exception.newEndTime)
                ) { "Una excepción contiene un horario inválido." }
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
        require(subject.name.isNotBlank()) { "El nombre de la materia es obligatorio." }
        val start = requireDate(subject.semesterStart, "inicio del semestre")
        val end = requireDate(subject.semesterEnd, "fin del semestre")
        require(!end.isBefore(start)) { "El fin del semestre no puede ser anterior al inicio." }
        require(subject.reminderMinutesBefore >= 0) { "El recordatorio no puede ser negativo." }
    }

    private fun validateEvent(event: SchoolEventEntity) {
        require(event.title.isNotBlank()) { "El título del evento es obligatorio." }
        val startDate = requireDate(event.startDate, "fecha inicial")
        val endDate = requireDate(event.endDate, "fecha final")
        require(!endDate.isBefore(startDate)) { "La fecha final no puede ser anterior a la inicial." }
        require(event.reminderMinutes >= 0) { "El recordatorio no puede ser negativo." }
        if (!event.isAllDay) {
            val startTime = event.startTime
            val endTime = event.endTime
            require(!startTime.isNullOrBlank() && DateTimeUtils.isValidTime(startTime)) {
                "La hora inicial debe usar HH:mm."
            }
            require(!endTime.isNullOrBlank() && DateTimeUtils.isValidTime(endTime)) {
                "La hora final debe usar HH:mm."
            }
            if (startDate == endDate) {
                require(DateTimeUtils.endIsAfterStart(startTime, endTime)) {
                    "La hora final debe ser posterior a la inicial."
                }
            }
        }
    }

    private fun validateAndPrepareSlots(
        subjectId: Long,
        slots: List<ScheduleSlotEntity>
    ): List<ScheduleSlotEntity> {
        val prepared = slots.map { slot ->
            require(slot.dayOfWeek in 1..7) { "El día del horario debe estar entre 1 y 7." }
            require(DateTimeUtils.endIsAfterStart(slot.startTime, slot.endTime)) {
                "La hora final debe ser posterior a la inicial."
            }
            slot.copy(subjectId = subjectId)
        }
        val exactKeys = prepared.map { Triple(it.dayOfWeek, it.startTime, it.endTime) }
        require(exactKeys.distinct().size == exactKeys.size) { "Hay horarios duplicados." }
        prepared.groupBy { it.dayOfWeek }.values.forEach { daySlots ->
            daySlots.sortedBy { it.startTime }.zipWithNext().forEach { (first, second) ->
                require(first.endTime <= second.startTime) { "Hay horarios de la materia que se traslapan." }
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
            ) { "El horario ${slot.startTime}-${slot.endTime} se traslapa con otra materia." }
        }
    }

    private fun requireDate(value: String, label: String) =
        requireNotNull(DateTimeUtils.parseDate(value)) { "La $label debe usar yyyy-MM-dd." }

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
