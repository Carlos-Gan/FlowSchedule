package com.mocas

import com.mocas.data.local.ScheduleSlotEntity
import com.mocas.data.local.SubjectEntity
import com.mocas.data.local.SubjectWithSlots
import com.mocas.ui.dialogs.SlotDraft
import com.mocas.ui.dialogs.detectScheduleConflicts
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScheduleConflictValidationTest {

    @Test
    fun overlappingDraftSessionsOnSameDayAreRejected() {
        val conflicts = detectScheduleConflicts(
            drafts = listOf(
                SlotDraft(selectedDays = setOf(1), startTime = "09:00", endTime = "11:00"),
                SlotDraft(selectedDays = setOf(1), startTime = "10:30", endTime = "12:00")
            ),
            existingSubjects = emptyList(),
            excludedSubjectId = null,
            periodStart = "2026-01-01",
            periodEnd = "2026-06-30"
        )

        assertEquals(setOf(0, 1), conflicts.keys)
    }

    @Test
    fun adjacentSessionsAreAllowed() {
        val conflicts = detectScheduleConflicts(
            drafts = listOf(
                SlotDraft(selectedDays = setOf(1), startTime = "09:00", endTime = "10:00"),
                SlotDraft(selectedDays = setOf(1), startTime = "10:00", endTime = "11:00")
            ),
            existingSubjects = emptyList(),
            excludedSubjectId = null,
            periodStart = "2026-01-01",
            periodEnd = "2026-06-30"
        )

        assertTrue(conflicts.isEmpty())
    }

    @Test
    fun sameTimeInDifferentAcademicYearDoesNotConflict() {
        val existing = SubjectWithSlots(
            subject = SubjectEntity(
                id = 8,
                name = "Redes 2026",
                semesterStart = "2026-01-01",
                semesterEnd = "2026-06-30"
            ),
            slots = listOf(
                ScheduleSlotEntity(
                    id = 9,
                    subjectId = 8,
                    dayOfWeek = 1,
                    startTime = "09:00",
                    endTime = "11:00"
                )
            )
        )

        val conflicts = detectScheduleConflicts(
            drafts = listOf(
                SlotDraft(selectedDays = setOf(1), startTime = "09:30", endTime = "10:30")
            ),
            existingSubjects = listOf(existing),
            excludedSubjectId = null,
            periodStart = "2027-01-01",
            periodEnd = "2027-06-30"
        )

        assertTrue(conflicts.isEmpty())
    }

    @Test
    fun overlapWithExistingClassInSamePeriodIsReported() {
        val existing = SubjectWithSlots(
            subject = SubjectEntity(
                id = 12,
                name = "Bases de datos",
                semesterStart = "2026-01-01",
                semesterEnd = "2026-06-30"
            ),
            slots = listOf(
                ScheduleSlotEntity(
                    id = 13,
                    subjectId = 12,
                    dayOfWeek = 3,
                    startTime = "11:00",
                    endTime = "13:00"
                )
            )
        )

        val conflicts = detectScheduleConflicts(
            drafts = listOf(
                SlotDraft(selectedDays = setOf(3), startTime = "12:00", endTime = "14:00")
            ),
            existingSubjects = listOf(existing),
            excludedSubjectId = null,
            periodStart = "2026-01-10",
            periodEnd = "2026-05-30"
        )

        assertTrue(conflicts.getValue(0).single().contains("Bases de datos"))
    }
}
