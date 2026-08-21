package com.example

import com.example.ui.dialogs.oneHourAfter
import com.example.ui.dialogs.buildPeriodName
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionTimeTest {

    @Test
    fun endTimeDefaultsToOneHourAfterStart() {
        assertEquals("09:15", oneHourAfter("08:15"))
        assertEquals("18:45", oneHourAfter("17:45"))
    }

    @Test
    fun endTimeStaysWithinTheSameDay() {
        assertEquals("23:59", oneHourAfter("23:30"))
    }

    @Test
    fun periodNameAlwaysIncludesBothYears() {
        assertEquals(
            "Ene 2026 – Mar 2026",
            buildPeriodName("2026-01-10", "2026-03-31")
        )
        assertEquals(
            "Nov 2026 – Mar 2027",
            buildPeriodName("2026-11-01", "2027-03-31")
        )
    }
}
