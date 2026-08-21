package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.ai.DetectedSubjectItem
import com.example.data.local.AcademicPeriodEntity
import com.example.data.local.AppDatabase
import com.example.data.local.ScheduleSlotEntity
import com.example.data.local.SchoolEventEntity
import com.example.data.local.SchoolEventWithSubject
import com.example.data.local.SubjectEntity
import com.example.data.local.SubjectWithSlots
import com.example.util.DateTimeUtils
import kotlinx.coroutines.flow.Flow
import java.util.Locale

class ScheduleRepository(private val database: AppDatabase) {
    private val subjectDao = database.subjectDao()
    private val slotDao = database.scheduleSlotDao()
    private val eventDao = database.schoolEventDao()
    private val periodDao = database.academicPeriodDao()

    val allSubjectsWithSlots: Flow<List<SubjectWithSlots>> = subjectDao.getAllSubjectsWithSlots()
    val allEventsWithSubject: Flow<List<SchoolEventWithSubject>> = eventDao.getAllEventsWithSubject()
    val allAcademicPeriods: Flow<List<AcademicPeriodEntity>> = periodDao.getAllPeriods()

    suspend fun insertAcademicPeriod(period: AcademicPeriodEntity): Long {
        require(period.name.isNotBlank()) { "El nombre del periodo es obligatorio." }
        val start = requireDate(period.startDate, "inicio del periodo")
        val end = requireDate(period.endDate, "fin del periodo")
        require(!end.isBefore(start)) { "El fin del periodo no puede ser anterior al inicio." }
        return periodDao.insertPeriod(
            period.copy(
                id = 0,
                name = period.name.trim(),
                startDate = start.toString(),
                endDate = end.toString()
            )
        )
    }

    suspend fun deleteAcademicPeriod(periodId: Long): Boolean =
        periodDao.deletePeriod(periodId) > 0

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
        eventDao.clearAllEvents()
        subjectDao.clearAllSubjects()
        periodDao.clearAllPeriods()
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
