package com.mocas

import com.mocas.data.local.ClassExceptionEntity
import com.mocas.data.local.ClassExceptionType
import com.mocas.data.local.ScheduleSlotEntity
import com.mocas.data.local.SchoolEventEntity
import com.mocas.data.local.SchoolEventWithSubject
import com.mocas.data.local.SubjectEntity
import com.mocas.data.local.SubjectWithSlots
import com.mocas.data.widget.buildWidgetSnapshot
import com.mocas.data.widget.buildDailyWidgetItems
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScheduleWidgetTest {
    private val subject = SubjectEntity(
        id = 1,
        name = "Redes",
        defaultRoom = "SC9",
        semesterStart = "2026-01-01",
        semesterEnd = "2026-12-31"
    )
    private val fridaySlot = ScheduleSlotEntity(
        id = 10,
        subjectId = 1,
        dayOfWeek = 5,
        startTime = "11:00",
        endTime = "12:00"
    )

    @Test
    fun showsNextClassCountdownAndPendingActivities() {
        val snapshot = buildWidgetSnapshot(
            subjects = listOf(SubjectWithSlots(subject, listOf(fridaySlot))),
            exceptions = emptyList(),
            pendingCount = 3,
            now = LocalDateTime.of(2026, 8, 21, 9, 30)
        )

        assertEquals("Redes", snapshot.className)
        assertEquals(3, snapshot.pendingCount)
        assertTrue(snapshot.classDetails.contains("11:00"))
        assertEquals("En 1 h 30 min", snapshot.countdown)
    }

    @Test
    fun ignoresACanceledOccurrence() {
        val snapshot = buildWidgetSnapshot(
            subjects = listOf(SubjectWithSlots(subject, listOf(fridaySlot))),
            exceptions = listOf(
                ClassExceptionEntity(
                    subjectId = 1,
                    slotId = 10,
                    date = "2026-08-21",
                    type = ClassExceptionType.CANCELED
                )
            ),
            pendingCount = 0,
            now = LocalDateTime.of(2026, 8, 21, 9, 30)
        )

        assertEquals("Redes", snapshot.className)
        assertTrue(snapshot.classDetails.contains("28"))
    }

    @Test
    fun dailyWidgetShowsPendingItemsForEachClass() {
        val events = listOf(
            SchoolEventWithSubject(
                event = SchoolEventEntity(
                    id = 20,
                    title = "Entregar práctica",
                    subjectId = 1,
                    startDate = "2026-08-22"
                ),
                subject = subject
            )
        )

        val items = buildDailyWidgetItems(
            subjects = listOf(SubjectWithSlots(subject, listOf(fridaySlot))),
            exceptions = emptyList(),
            events = events,
            date = LocalDate.of(2026, 8, 21),
            now = LocalTime.of(11, 30)
        )

        assertEquals(1, items.size)
        assertEquals(1, items.single().pendingCount)
        assertTrue(items.single().isHappeningNow)
        assertEquals("SC9", items.single().room)
    }
}
