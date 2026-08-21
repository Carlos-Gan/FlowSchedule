package com.mocas.data.repository

import androidx.room.withTransaction
import com.mocas.data.ai.DetectedSubjectItem
import com.mocas.data.local.AcademicPeriodEntity
import com.mocas.data.local.ClassExceptionEntity
import com.mocas.data.local.ClassExceptionType
import com.mocas.data.local.AppDatabase
import com.mocas.data.local.ScheduleSlotEntity
import com.mocas.data.local.SchoolEventEntity
import com.mocas.data.local.SchoolEventWithSubject
import com.mocas.data.local.SubjectEntity
import com.mocas.data.local.SubjectWithSlots
import com.mocas.util.DateTimeUtils
import kotlinx.coroutines.flow.Flow
import java.util.Locale

class ScheduleRepository(private val database: AppDatabase) {
    private val subjectDao = database.subjectDao()
    private val slotDao = database.scheduleSlotDao()
    private val eventDao = database.schoolEventDao()
    private val periodDao = database.academicPeriodDao()
    private val exceptionDao = database.classExceptionDao()

    val allSubjectsWithSlots: Flow<List<SubjectWithSlots>> = subjectDao.getAllSubjectsWithSlots()
    val allEventsWithSubject: Flow<List<SchoolEventWithSubject>> = eventDao.getAllEventsWithSubject()
    val allAcademicPeriods: Flow<List<AcademicPeriodEntity>> = periodDao.getAllPeriods()
    val allClassExceptions: Flow<List<ClassExceptionEntity>> = exceptionDao.getAll()

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

    suspend fun deleteSubject(subjectId: Long): Boolean = subjectDao.deleteSubjectById(subjectId) > 0

    suspend fun insertEvent(event: SchoolEventEntity): Long {
        validateEvent(event)
        return eventDao.insertEvent(event.copy(id = 0))
    }

    suspend fun updateEvent(event: SchoolEventEntity): Boolean {
        require(event.id > 0) { "No se puede actualizar un evento sin ID." }
        validateEvent(event)
        return eventDao.updateEvent(event.copy(updatedAtMillis = System.currentTimeMillis())) > 0
    }

    suspend fun deleteEvent(eventId: Long): Boolean = eventDao.deleteEventById(eventId) > 0

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

    suspend fun setEventCompleted(eventId: Long, completed: Boolean): Boolean =
        eventDao.setEventCompleted(eventId, completed, System.currentTimeMillis()) > 0

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

    suspend fun clearAll() = database.withTransaction {
        exceptionDao.clearAll()
        eventDao.clearAllEvents()
        subjectDao.clearAllSubjects()
        periodDao.clearAllPeriods()
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
