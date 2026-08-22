package com.mocas

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.mocas.data.ai.DetectedSubjectItem
import com.mocas.data.local.AppDatabase
import com.mocas.data.local.AcademicPeriodEntity
import com.mocas.data.local.ClassExceptionEntity
import com.mocas.data.local.ClassExceptionType
import com.mocas.data.local.ScheduleSlotEntity
import com.mocas.data.local.SchoolEventEntity
import com.mocas.data.local.SubjectEntity
import com.mocas.data.repository.ScheduleRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ScheduleRepositoryTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: ScheduleRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repository = ScheduleRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun importGroupsNormalizedSubjectAndDoesNotDuplicateOnSecondImport() = runTest {
        val items = listOf(
            DetectedSubjectItem("  Programación Móvil ", "Dra. Ruiz", 1, "08:00", "09:00", "A1"),
            DetectedSubjectItem("programación   móvil", " dra. ruiz ", 3, "10:00", "11:00", "A2")
        )

        repository.importDetectedSubjects(items, "2026-08-20", "2026-12-10")
        repository.importDetectedSubjects(items, "2026-08-20", "2026-12-10")

        val subjects = repository.allSubjectsWithSlots.first()
        assertEquals(1, subjects.size)
        assertEquals(2, subjects.single().slots.size)
    }

    @Test
    fun rejectsOverlapWithAnotherSubject() = runTest {
        repository.insertSubjectWithSlots(
            SubjectEntity(name = "A", semesterStart = "2026-08-20", semesterEnd = "2026-12-10"),
            listOf(ScheduleSlotEntity(subjectId = 0, dayOfWeek = 1, startTime = "08:00", endTime = "10:00"))
        )

        var rejected = false
        try {
            repository.insertSubjectWithSlots(
                SubjectEntity(name = "B", semesterStart = "2026-08-20", semesterEnd = "2026-12-10"),
                listOf(ScheduleSlotEntity(subjectId = 0, dayOfWeek = 1, startTime = "09:00", endTime = "11:00"))
            )
        } catch (_: IllegalArgumentException) {
            rejected = true
        }
        assertTrue(rejected)
    }

    @Test
    fun deletingSubjectCascadesSlotsAndKeepsEventWithNullSubject() = runTest {
        val subjectId = repository.insertSubjectWithSlots(
            SubjectEntity(name = "Bases", semesterStart = "2026-08-20", semesterEnd = "2026-12-10"),
            listOf(ScheduleSlotEntity(subjectId = 0, dayOfWeek = 2, startTime = "08:00", endTime = "09:00"))
        )
        val slotId = database.scheduleSlotDao().getSlotsForSubjectOnce(subjectId).single().id
        val eventId = repository.insertEvent(
            SchoolEventEntity(
                title = "Examen",
                subjectId = subjectId,
                startDate = "2026-09-10",
                startTime = "10:00",
                endTime = "11:00"
            )
        )

        repository.deleteSubject(subjectId)

        assertEquals(null, database.scheduleSlotDao().getSlotById(slotId))
        assertEquals(null, database.schoolEventDao().getEventById(eventId)?.subjectId)
    }

    @Test
    fun academicPeriodsWithSameMonthsInDifferentYearsStaySeparate() = runTest {
        repository.insertAcademicPeriod(
            AcademicPeriodEntity(
                name = "Ene 2026 – Jun 2026",
                startDate = "2026-01-08",
                endDate = "2026-06-30"
            )
        )
        repository.insertAcademicPeriod(
            AcademicPeriodEntity(
                name = "Ene 2027 – Jun 2027",
                startDate = "2027-01-11",
                endDate = "2027-06-30"
            )
        )

        val periods = repository.allAcademicPeriods.first()
        assertEquals(2, periods.size)
        assertTrue(periods.any { it.startDate == "2026-01-08" })
        assertTrue(periods.any { it.startDate == "2027-01-11" })
    }

    @Test
    fun editingPeriodUpdatesSubjectsThatUsedItsPreviousDates() = runTest {
        val periodId = repository.insertAcademicPeriod(
            AcademicPeriodEntity(
                name = "Agosto – diciembre 2026",
                startDate = "2026-08-17",
                endDate = "2026-12-11"
            )
        )
        repository.insertSubjectWithSlots(
            SubjectEntity(
                name = "Redes",
                semesterStart = "2026-08-17",
                semesterEnd = "2026-12-11"
            ),
            emptyList()
        )

        repository.saveAcademicPeriod(
            AcademicPeriodEntity(
                id = periodId,
                name = "Otoño 2026",
                startDate = "2026-08-24",
                endDate = "2026-12-18",
                colorHex = "#3B82F6"
            )
        )

        val subject = repository.allSubjectsWithSlots.first().single().subject
        val period = repository.allAcademicPeriods.first().single()
        assertEquals("2026-08-24", subject.semesterStart)
        assertEquals("2026-12-18", subject.semesterEnd)
        assertEquals("#3B82F6", period.colorHex)
    }

    @Test
    fun overlappingAcademicPeriodsAreRejected() = runTest {
        repository.insertAcademicPeriod(
            AcademicPeriodEntity(
                name = "Primero",
                startDate = "2026-01-01",
                endDate = "2026-06-30"
            )
        )

        var rejected = false
        try {
            repository.insertAcademicPeriod(
                AcademicPeriodEntity(
                    name = "Segundo",
                    startDate = "2026-06-01",
                    endDate = "2026-12-01"
                )
            )
        } catch (_: IllegalArgumentException) {
            rejected = true
        }

        assertTrue(rejected)
    }

    @Test
    fun copyingPeriodSubjectsKeepsSessionsAndDoesNotDuplicateThem() = runTest {
        val sourceId = repository.insertAcademicPeriod(
            AcademicPeriodEntity(
                name = "Primavera 2026",
                startDate = "2026-01-12",
                endDate = "2026-06-19"
            )
        )
        val targetId = repository.insertAcademicPeriod(
            AcademicPeriodEntity(
                name = "Primavera 2027",
                startDate = "2027-01-11",
                endDate = "2027-06-18"
            )
        )
        repository.insertSubjectWithSlots(
            SubjectEntity(
                name = "Redes",
                professor = "Ana Ruiz",
                semesterStart = "2026-01-12",
                semesterEnd = "2026-06-19"
            ),
            listOf(
                ScheduleSlotEntity(
                    subjectId = 0,
                    dayOfWeek = 2,
                    startTime = "09:00",
                    endTime = "11:00"
                )
            )
        )

        assertEquals(1, repository.copySubjectsBetweenPeriods(sourceId, targetId))
        assertEquals(0, repository.copySubjectsBetweenPeriods(sourceId, targetId))

        val copied = repository.allSubjectsWithSlots.first()
            .single { it.subject.semesterStart == "2027-01-11" }
        assertEquals("Redes", copied.subject.name)
        assertEquals("Ana Ruiz", copied.subject.professor)
        assertEquals(1, copied.slots.size)
        assertEquals(2, copied.slots.single().dayOfWeek)
    }

    @Test
    fun classExceptionOnlyChangesTheSelectedOccurrence() = runTest {
        val subjectId = repository.insertSubjectWithSlots(
            SubjectEntity(
                name = "Redes",
                semesterStart = "2026-08-01",
                semesterEnd = "2026-12-20"
            ),
            listOf(
                ScheduleSlotEntity(
                    subjectId = 0,
                    dayOfWeek = 1,
                    startTime = "10:00",
                    endTime = "11:00"
                )
            )
        )
        val slot = database.scheduleSlotDao().getSlotsForSubjectOnce(subjectId).single()

        repository.saveClassException(
            ClassExceptionEntity(
                subjectId = subjectId,
                slotId = slot.id,
                date = "2026-08-24",
                type = ClassExceptionType.MODIFIED,
                newStartTime = "12:00",
                newEndTime = "13:00",
                newRoom = "B4"
            )
        )

        val exception = repository.allClassExceptions.first().single()
        assertEquals("2026-08-24", exception.date)
        assertEquals("12:00", exception.newStartTime)
        assertEquals("10:00", database.scheduleSlotDao().getSlotById(slot.id)?.startTime)
    }

    @Test
    fun sameWeeklyTimeIsAllowedWhenAcademicPeriodsDoNotOverlap() = runTest {
        repository.insertSubjectWithSlots(
            SubjectEntity(
                name = "Materia 2026",
                semesterStart = "2026-01-01",
                semesterEnd = "2026-06-30"
            ),
            listOf(
                ScheduleSlotEntity(
                    subjectId = 0,
                    dayOfWeek = 1,
                    startTime = "08:00",
                    endTime = "09:00"
                )
            )
        )
        repository.insertSubjectWithSlots(
            SubjectEntity(
                name = "Materia 2027",
                semesterStart = "2027-01-01",
                semesterEnd = "2027-06-30"
            ),
            listOf(
                ScheduleSlotEntity(
                    subjectId = 0,
                    dayOfWeek = 1,
                    startTime = "08:00",
                    endTime = "09:00"
                )
            )
        )

        assertEquals(2, repository.allSubjectsWithSlots.first().size)
    }

    @Test
    fun importKeepsSameSubjectNameSeparateAcrossYears() = runTest {
        val items = listOf(
            DetectedSubjectItem(
                "Redes", "Dra. Ruiz", 2, "10:00", "11:00", "A1"
            )
        )

        repository.importDetectedSubjects(items, "2026-01-10", "2026-06-30")
        repository.importDetectedSubjects(items, "2027-01-10", "2027-06-30")

        val subjects = repository.allSubjectsWithSlots.first()
        assertEquals(2, subjects.size)
        assertTrue(subjects.any { it.subject.semesterStart.startsWith("2026") })
        assertTrue(subjects.any { it.subject.semesterStart.startsWith("2027") })
    }

    @Test
    fun scheduleBackupRestoresRelationshipsWithNewIds() = runTest {
        repository.saveAcademicPeriod(
            AcademicPeriodEntity(
                name = "Ago–Dic 2026",
                startDate = "2026-08-01",
                endDate = "2026-12-20"
            )
        )
        val subjectId = repository.insertSubjectWithSlots(
            SubjectEntity(
                name = "Redes",
                semesterStart = "2026-08-01",
                semesterEnd = "2026-12-20"
            ),
            listOf(
                ScheduleSlotEntity(
                    subjectId = 0,
                    dayOfWeek = 1,
                    startTime = "08:00",
                    endTime = "09:00",
                    room = "SC9"
                )
            )
        )
        val slotId = database.scheduleSlotDao().getSlotsForSubjectOnce(subjectId).single().id
        repository.insertEvent(
            SchoolEventEntity(
                title = "Tarea de redes",
                subjectId = subjectId,
                startDate = "2026-09-01",
                startTime = "10:00",
                endTime = "11:00"
            )
        )
        repository.saveClassException(
            ClassExceptionEntity(
                subjectId = subjectId,
                slotId = slotId,
                date = "2026-08-24",
                type = ClassExceptionType.CANCELED
            )
        )

        val backup = repository.exportScheduleBackup()
        repository.clearAll()
        val summary = repository.importScheduleBackup(backup)

        val restoredSubject = repository.allSubjectsWithSlots.first().single()
        val restoredEvent = repository.allEventsWithSubject.first().single()
        val restoredException = repository.allClassExceptions.first().single()
        assertEquals(1, summary.subjects)
        assertEquals(restoredSubject.subject.id, restoredEvent.event.subjectId)
        assertEquals(restoredSubject.subject.id, restoredException.subjectId)
        assertEquals(restoredSubject.slots.single().id, restoredException.slotId)
        assertEquals(1, repository.allAcademicPeriods.first().size)
    }
}
