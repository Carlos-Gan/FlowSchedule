package com.mocas

import com.mocas.data.local.AcademicPeriodEntity
import com.mocas.data.local.SchoolEventEntity
import com.mocas.data.local.SchoolEventType
import com.mocas.data.local.SchoolEventWithSubject
import com.mocas.ui.util.isVacationDate
import java.time.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VacationUtilsTest {
    private val period = AcademicPeriodEntity(
        name = "Enero - junio 2026",
        startDate = "2026-01-12",
        endDate = "2026-06-30"
    )

    @Test
    fun `an explicit vacation event always marks its dates`() {
        val vacation = SchoolEventWithSubject(
            event = SchoolEventEntity(
                title = "Vacaciones de primavera",
                type = SchoolEventType.VACACIONES,
                startDate = "2026-03-30",
                endDate = "2026-04-10",
                isAllDay = true
            )
        )

        assertTrue(
            isVacationDate(
                date = LocalDate.parse("2026-04-03"),
                academicPeriods = listOf(period),
                events = listOf(vacation),
                outsidePeriodsAreVacations = false
            )
        )
    }

    @Test
    fun `a date outside every period is an automatic vacation when enabled`() {
        assertTrue(
            isVacationDate(
                date = LocalDate.parse("2026-07-15"),
                academicPeriods = listOf(period),
                events = emptyList(),
                outsidePeriodsAreVacations = true
            )
        )
        assertFalse(
            isVacationDate(
                date = LocalDate.parse("2026-05-15"),
                academicPeriods = listOf(period),
                events = emptyList(),
                outsidePeriodsAreVacations = true
            )
        )
    }

    @Test
    fun `enabling automatic vacations without periods does not mark every date`() {
        assertFalse(
            isVacationDate(
                date = LocalDate.parse("2026-07-15"),
                academicPeriods = emptyList(),
                events = emptyList(),
                outsidePeriodsAreVacations = true
            )
        )
    }
}
