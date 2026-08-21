package com.mocas

import com.mocas.util.DateTimeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DateTimeUtilsTest {
    @Test
    fun validatesStrictDatabaseFormats() {
        assertTrue(DateTimeUtils.isValidDate("2026-08-20"))
        assertFalse(DateTimeUtils.isValidDate("20/08/2026"))
        assertTrue(DateTimeUtils.isValidTime("09:05"))
        assertFalse(DateTimeUtils.isValidTime("9:05"))
        assertFalse(DateTimeUtils.endIsAfterStart("10:00", "09:00"))
    }

    @Test
    fun findsFirstRealClassDate() {
        assertEquals(
            "2026-08-24",
            DateTimeUtils.firstDateForDay("2026-08-20", 1).toString()
        )
    }
}
