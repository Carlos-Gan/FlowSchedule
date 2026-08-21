package com.mocas

import com.mocas.data.local.SubjectEntity
import com.mocas.data.local.SubjectWithSlots
import com.mocas.ui.util.isActiveOn
import com.mocas.ui.util.weekRangeLabel
import com.mocas.ui.viewmodel.ScheduleViewModel
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WeekScheduleUtilsTest {
    @Test
    fun `week starts on monday even across month and year boundaries`() {
        assertEquals(
            LocalDate.parse("2026-12-28"),
            ScheduleViewModel.startOfWeek(LocalDate.parse("2027-01-01"))
        )
        assertEquals(
            "28 dic 2026 – 3 ene 2027",
            weekRangeLabel(LocalDate.parse("2026-12-28"))
        )
    }

    @Test
    fun `subject is only active on dates inside its period`() {
        val subject = SubjectWithSlots(
            subject = SubjectEntity(
                name = "Redes",
                semesterStart = "2026-08-17",
                semesterEnd = "2026-12-11"
            )
        )

        assertFalse(subject.isActiveOn(LocalDate.parse("2026-08-16")))
        assertTrue(subject.isActiveOn(LocalDate.parse("2026-08-17")))
        assertTrue(subject.isActiveOn(LocalDate.parse("2026-12-11")))
        assertFalse(subject.isActiveOn(LocalDate.parse("2026-12-12")))
    }
}
