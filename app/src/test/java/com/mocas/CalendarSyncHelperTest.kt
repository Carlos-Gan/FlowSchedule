package com.mocas

import com.mocas.data.local.ScheduleSlotEntity
import com.mocas.data.local.SubjectEntity
import com.mocas.data.local.SubjectWithSlots
import com.mocas.data.repository.CalendarSyncHelper
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId

class CalendarSyncHelperTest {
    @Test
    fun exportsValidRecurringIcsWithEscapedTextAndCrLf() {
        val subject = SubjectEntity(
            id = 7,
            name = "Redes, seguridad; y nube",
            professor = "Ana\\Pérez",
            defaultRoom = "Lab; 2",
            semesterStart = "2026-08-20",
            semesterEnd = "2026-12-10"
        )
        val slot = ScheduleSlotEntity(
            id = 9,
            subjectId = 7,
            dayOfWeek = 1,
            startTime = "08:00",
            endTime = "09:30"
        )

        val ics = CalendarSyncHelper.exportScheduleAsIcsText(
            listOf(SubjectWithSlots(subject, listOf(slot))),
            ZoneId.of("America/Mexico_City")
        )

        assertTrue(ics.startsWith("BEGIN:VCALENDAR\r\nVERSION:2.0\r\n"))
        assertTrue(ics.contains("UID:subject-7-slot-9@snapmyschedule"))
        assertTrue(ics.contains("SUMMARY:Redes\\, seguridad\\; y nube"))
        assertTrue(ics.contains("RRULE:FREQ=WEEKLY;BYDAY=MO;UNTIL="))
        assertTrue(ics.endsWith("END:VCALENDAR\r\n"))
        assertFalse(Regex("(?<!\\r)\\n").containsMatchIn(ics))
    }
}
