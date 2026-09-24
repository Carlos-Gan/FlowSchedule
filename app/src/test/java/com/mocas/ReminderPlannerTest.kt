package com.mocas

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.mocas.data.local.ClassExceptionEntity
import com.mocas.data.local.ClassExceptionType
import com.mocas.data.local.ScheduleSlotEntity
import com.mocas.data.local.SchoolEventEntity
import com.mocas.data.local.SchoolEventType
import com.mocas.data.local.SchoolEventWithSubject
import com.mocas.data.local.SubjectEntity
import com.mocas.data.local.SubjectWithSlots
import com.mocas.data.notifications.planReminders
import com.mocas.ui.model.AppSettings
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ReminderPlannerTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    private val mondayClass = SubjectWithSlots(
        subject = SubjectEntity(
            id = 1,
            name = "Redes",
            defaultRoom = "A1",
            semesterStart = "2026-08-01",
            semesterEnd = "2026-12-20"
        ),
        slots = listOf(
            ScheduleSlotEntity(
                id = 10,
                subjectId = 1,
                dayOfWeek = 1,
                startTime = "10:00",
                endTime = "11:00"
            )
        )
    )

    @Test
    fun `modified class creates change reminder with new room`() {
        val reminders = planReminders(
            context = context,
            subjects = listOf(mondayClass),
            events = emptyList(),
            exceptions = listOf(
                ClassExceptionEntity(
                    subjectId = 1,
                    slotId = 10,
                    date = "2026-08-24",
                    type = ClassExceptionType.MODIFIED,
                    newStartTime = "12:00",
                    newEndTime = "13:00",
                    newRoom = "B4"
                )
            ),
            settings = AppSettings(notificationsEnabled = true, defaultReminderMinutes = 15),
            now = LocalDateTime.parse("2026-08-23T18:00"),
            zoneId = ZoneId.of("UTC")
        )

        assertTrue(reminders.any { it.title.contains("Redes") && "B4" in it.message })
    }

    @Test
    fun `canceled occurrence does not create class reminder`() {
        val reminders = planReminders(
            context = context,
            subjects = listOf(mondayClass),
            events = emptyList(),
            exceptions = listOf(
                ClassExceptionEntity(
                    subjectId = 1,
                    slotId = 10,
                    date = "2026-08-24",
                    type = ClassExceptionType.CANCELED
                )
            ),
            settings = AppSettings(notificationsEnabled = true, taskReminderMinutes = 60),
            now = LocalDateTime.parse("2026-08-23T18:00"),
            zoneId = ZoneId.of("UTC")
        )

        assertFalse(reminders.any { it.id == "class_10_2026-08-24" })
    }

    @Test
    fun `pending task creates reminder and overdue alert`() {
        val task = SchoolEventWithSubject(
            event = SchoolEventEntity(
                id = 7,
                title = "Ensayo",
                type = SchoolEventType.TAREA,
                startDate = "2026-08-25",
                endDate = "2026-08-25",
                startTime = "10:00",
                endTime = "11:00"
            )
        )
        val reminders = planReminders(
            context = context,
            subjects = emptyList(),
            events = listOf(task),
            exceptions = emptyList(),
            settings = AppSettings(notificationsEnabled = true, taskReminderMinutes = 60),
            now = LocalDateTime.parse("2026-08-24T08:00"),
            zoneId = ZoneId.of("UTC")
        )

        assertTrue(reminders.any { it.id == "event_7" })
        assertTrue(reminders.any { it.id == "overdue_7" })
    }

    @Test
    fun `classes can be disabled while task reminders stay enabled two days before`() {
        val task = SchoolEventWithSubject(
            event = SchoolEventEntity(
                id = 8,
                title = "Proyecto",
                type = SchoolEventType.TAREA,
                startDate = "2026-08-25",
                endDate = "2026-08-25",
                startTime = "10:00",
                endTime = "11:00"
            )
        )
        val reminders = planReminders(
            context = context,
            subjects = listOf(mondayClass),
            events = listOf(task),
            exceptions = emptyList(),
            settings = AppSettings(
                notificationsEnabled = true,
                classNotificationsEnabled = false,
                taskNotificationsEnabled = true,
                taskReminderMinutes = 2 * 24 * 60,
                tomorrowSummaryEnabled = false
            ),
            now = LocalDateTime.parse("2026-08-22T08:00"),
            zoneId = ZoneId.of("UTC")
        )

        assertFalse(reminders.any { it.id.startsWith("class_") })
        val taskReminder = reminders.single { it.id == "event_8" }
        assertEquals(8L, taskReminder.eventId)
        assertEquals(
            LocalDateTime.parse("2026-08-23T10:00").toInstant(ZoneOffset.UTC).toEpochMilli(),
            taskReminder.triggerAtMillis
        )
    }
}
