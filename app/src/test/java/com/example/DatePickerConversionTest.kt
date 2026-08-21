package com.example

import com.example.ui.components.dateToPickerMillis
import com.example.ui.components.pickerMillisToDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DatePickerConversionTest {

    @Test
    fun calendarSelectionRoundTripsWithoutChangingDayOrYear() {
        listOf("2026-01-01", "2026-12-31", "2027-01-01", "2032-02-29")
            .forEach { date ->
                assertEquals(date, pickerMillisToDate(dateToPickerMillis(date)!!))
            }
    }

    @Test
    fun invalidDateCannotInitializeCalendar() {
        assertNull(dateToPickerMillis("2026-02-30"))
    }
}
