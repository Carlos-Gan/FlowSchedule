package com.mocas.data.repository

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import androidx.core.content.FileProvider
import com.mocas.R
import com.mocas.data.local.ScheduleSlotEntity
import com.mocas.data.local.SchoolEventEntity
import com.mocas.data.local.SubjectEntity
import com.mocas.data.local.SubjectWithSlots
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.io.File

sealed interface CalendarActionResult {
    data object Launched : CalendarActionResult
    data object NoCompatibleApp : CalendarActionResult
    data class InvalidData(val reason: String) : CalendarActionResult
    data class Failed(val reason: String) : CalendarActionResult
}

object CalendarSyncHelper {

    private val utcFormatter: DateTimeFormatter =
        DateTimeFormatter
            .ofPattern("yyyyMMdd'T'HHmmss'Z'")
            .withZone(ZoneId.of("UTC"))

    /**
     * Abre el calendario del dispositivo con los datos del evento.
     *
     * Devuelve true cuando existe una aplicación capaz de manejar
     * el Intent y false cuando no se encontró ninguna.
     */
    fun addEventToPhoneCalendar(
        context: Context,
        event: SchoolEventEntity,
        subjectName: String? = null
    ): CalendarActionResult {
        return try {
            val zoneId = ZoneId.systemDefault()

            val startDate = LocalDate.parse(event.startDate)
            val endDate = LocalDate.parse(event.endDate)

            val startMillis: Long
            val endMillis: Long

            if (event.isAllDay) {
                startMillis = startDate
                    .atStartOfDay(zoneId)
                    .toInstant()
                    .toEpochMilli()

                /*
                 * En Calendar los eventos de día completo utilizan una
                 * fecha final exclusiva.
                 *
                 * Un evento del 20 al 20 termina al iniciar el día 21.
                 */
                endMillis = endDate
                    .plusDays(1)
                    .atStartOfDay(zoneId)
                    .toInstant()
                    .toEpochMilli()
            } else {
                val parsedStartTime = event.startTime
                    ?.let(LocalTime::parse)
                    ?: LocalTime.of(8, 0)

                val parsedEndTime = event.endTime
                    ?.let(LocalTime::parse)
                    ?: parsedStartTime.plusHours(1)

                val startDateTime = startDate.atTime(parsedStartTime)
                var endDateTime = endDate.atTime(parsedEndTime)

                if (!endDateTime.isAfter(startDateTime)) {
                    endDateTime = startDateTime.plusHours(1)
                }

                startMillis = startDateTime
                    .atZone(zoneId)
                    .toInstant()
                    .toEpochMilli()

                endMillis = endDateTime
                    .atZone(zoneId)
                    .toInstant()
                    .toEpochMilli()
            }

            val title = buildString {
                append("[")
                append(event.type.displayName)
                append("] ")

                if (!subjectName.isNullOrBlank()) {
                    append(subjectName)
                    append(": ")
                }

                append(event.title)
            }

            val intent = Intent(
                Intent.ACTION_INSERT,
                CalendarContract.Events.CONTENT_URI
            ).apply {
                putExtra(
                    CalendarContract.Events.TITLE,
                    title
                )

                putExtra(
                    CalendarContract.Events.DESCRIPTION,
                    event.description
                )

                putExtra(
                    CalendarContract.Events.EVENT_LOCATION,
                    event.location
                )

                putExtra(
                    CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                    startMillis
                )

                putExtra(
                    CalendarContract.EXTRA_EVENT_END_TIME,
                    endMillis
                )

                putExtra(
                    CalendarContract.Events.ALL_DAY,
                    event.isAllDay
                )

                putExtra(
                    CalendarContract.Events.EVENT_TIMEZONE,
                    zoneId.id
                )

                putExtra(
                    CalendarContract.Events.AVAILABILITY,
                    CalendarContract.Events.AVAILABILITY_BUSY
                )

                putExtra(CalendarContract.Events.HAS_ALARM, event.reminderMinutes >= 0)
                putExtra(CalendarContract.Reminders.MINUTES, event.reminderMinutes)
                putExtra(
                    CalendarContract.Reminders.METHOD,
                    CalendarContract.Reminders.METHOD_ALERT
                )
            }

            if (intent.resolveActivity(context.packageManager) == null) {
                return CalendarActionResult.NoCompatibleApp
            }

            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
            CalendarActionResult.Launched
        } catch (error: ActivityNotFoundException) {
            CalendarActionResult.NoCompatibleApp
        } catch (error: IllegalArgumentException) {
            CalendarActionResult.InvalidData(error.message ?: context.getString(R.string.error_fecha_hora_invalida))
        } catch (error: Exception) {
            CalendarActionResult.Failed(error.message ?: context.getString(R.string.error_abrir_calendario))
        }
    }

    /**
     * Abre el calendario con una clase recurrente semanal.
     */
    fun addClassToPhoneCalendar(
        context: Context,
        subject: SubjectEntity,
        slot: ScheduleSlotEntity
    ): CalendarActionResult {
        return try {
            require(slot.dayOfWeek in 1..7) {
                context.getString(R.string.error_dia_semana_rango)
            }

            val zoneId = ZoneId.systemDefault()
            val semesterStart = LocalDate.parse(subject.semesterStart)
            val semesterEnd = LocalDate.parse(subject.semesterEnd)

            val classDay = DayOfWeek.of(slot.dayOfWeek)

            val firstClassDate = semesterStart.with(
                TemporalAdjusters.nextOrSame(classDay)
            )

            if (firstClassDate.isAfter(semesterEnd)) {
                return CalendarActionResult.InvalidData(context.getString(R.string.error_sin_clases_semestre))
            }

            val startTime = LocalTime.parse(slot.startTime)
            val endTime = LocalTime.parse(slot.endTime)

            val firstStart = firstClassDate
                .atTime(startTime)
                .atZone(zoneId)

            var firstEnd = firstClassDate
                .atTime(endTime)
                .atZone(zoneId)

            if (!firstEnd.isAfter(firstStart)) {
                firstEnd = firstEnd.plusDays(1)
            }

            val recurrenceEnd = semesterEnd
                .plusDays(1)
                .atStartOfDay(zoneId)
                .minusSeconds(1)
                .toInstant()

            val dayCode = dayToRRule(slot.dayOfWeek)

            val recurrenceRule = buildString {
                append("FREQ=WEEKLY")
                append(";BYDAY=")
                append(dayCode)
                append(";UNTIL=")
                append(utcFormatter.format(recurrenceEnd))
            }

            val selectedRoom = slot.room.ifBlank {
                subject.defaultRoom
            }

            val description = buildString {
                if (subject.professor.isNotBlank()) {
                    appendLine(context.getString(R.string.calendar_event_professor_label, subject.professor))
                }

                if (subject.code.isNotBlank()) {
                    appendLine(context.getString(R.string.calendar_event_code_label, subject.code))
                }

                if (selectedRoom.isNotBlank()) {
                    appendLine(context.getString(R.string.calendar_event_room_label, selectedRoom))
                }

                append(context.getString(R.string.calendar_description_footer))
            }

            val intent = Intent(
                Intent.ACTION_INSERT,
                CalendarContract.Events.CONTENT_URI
            ).apply {
                putExtra(
                    CalendarContract.Events.TITLE,
                    context.getString(R.string.calendar_event_class_title, subject.name)
                )

                putExtra(
                    CalendarContract.Events.DESCRIPTION,
                    description
                )

                putExtra(
                    CalendarContract.Events.EVENT_LOCATION,
                    selectedRoom
                )

                putExtra(
                    CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                    firstStart.toInstant().toEpochMilli()
                )

                putExtra(
                    CalendarContract.EXTRA_EVENT_END_TIME,
                    firstEnd.toInstant().toEpochMilli()
                )

                putExtra(
                    CalendarContract.Events.EVENT_TIMEZONE,
                    zoneId.id
                )

                putExtra(
                    CalendarContract.Events.RRULE,
                    recurrenceRule
                )

                putExtra(
                    CalendarContract.Events.AVAILABILITY,
                    CalendarContract.Events.AVAILABILITY_BUSY
                )

                putExtra(CalendarContract.Events.HAS_ALARM, subject.reminderMinutesBefore >= 0)
                putExtra(CalendarContract.Reminders.MINUTES, subject.reminderMinutesBefore)
                putExtra(
                    CalendarContract.Reminders.METHOD,
                    CalendarContract.Reminders.METHOD_ALERT
                )
            }

            if (intent.resolveActivity(context.packageManager) == null) {
                return CalendarActionResult.NoCompatibleApp
            }

            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
            CalendarActionResult.Launched
        } catch (error: ActivityNotFoundException) {
            CalendarActionResult.NoCompatibleApp
        } catch (error: IllegalArgumentException) {
            CalendarActionResult.InvalidData(error.message ?: context.getString(R.string.error_fecha_hora_invalida))
        } catch (error: Exception) {
            CalendarActionResult.Failed(error.message ?: context.getString(R.string.error_abrir_calendario))
        }
    }

    /**
     * Genera el contenido de un archivo .ics.
     */
    fun exportScheduleAsIcsText(
        context: Context,
        subjectsWithSlots: List<SubjectWithSlots>,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): String {
        val lines = mutableListOf<String>()

        lines += "BEGIN:VCALENDAR"
        lines += "VERSION:2.0"
        lines += "PRODID:-//FlowSchedule//Android//ES"
        lines += "CALSCALE:GREGORIAN"
        lines += "METHOD:PUBLISH"
        lines += "X-WR-CALNAME:FlowSchedule"
        lines += "X-WR-TIMEZONE:${escapeIcs(zoneId.id)}"

        val generatedAt = utcFormatter.format(Instant.now())

        subjectsWithSlots.forEach { item ->
            val subject = item.subject

            val semesterStart = runCatching {
                LocalDate.parse(subject.semesterStart)
            }.getOrNull() ?: return@forEach

            val semesterEnd = runCatching {
                LocalDate.parse(subject.semesterEnd)
            }.getOrNull() ?: return@forEach

            if (semesterEnd.isBefore(semesterStart)) return@forEach

            item.slots.forEach slotLoop@{ slot ->
                if (slot.dayOfWeek !in 1..7) {
                    return@slotLoop
                }

                val startTime = runCatching {
                    LocalTime.parse(slot.startTime)
                }.getOrNull() ?: return@slotLoop

                val endTime = runCatching {
                    LocalTime.parse(slot.endTime)
                }.getOrNull() ?: return@slotLoop

                val firstDate = semesterStart.with(
                    TemporalAdjusters.nextOrSame(
                        DayOfWeek.of(slot.dayOfWeek)
                    )
                )

                if (firstDate.isAfter(semesterEnd)) {
                    return@slotLoop
                }

                val startDateTime = firstDate
                    .atTime(startTime)
                    .atZone(zoneId)

                var endDateTime = firstDate
                    .atTime(endTime)
                    .atZone(zoneId)

                if (!endDateTime.isAfter(startDateTime)) {
                    endDateTime = endDateTime.plusDays(1)
                }

                val recurrenceEnd = semesterEnd
                    .plusDays(1)
                    .atStartOfDay(zoneId)
                    .minusSeconds(1)
                    .toInstant()

                val room = slot.room.ifBlank {
                    subject.defaultRoom
                }

                val uid = buildString {
                    append("subject-")
                    append(subject.id)
                    append("-slot-")
                    append(slot.id)
                    append("@flowschedule")
                }

                val description = buildString {
                    if (subject.professor.isNotBlank()) {
                        append(context.getString(R.string.calendar_event_professor_label, subject.professor))
                    }

                    if (subject.code.isNotBlank()) {
                        if (isNotEmpty()) append('\n')
                        append(context.getString(R.string.calendar_event_code_label, subject.code))
                    }
                }

                lines += "BEGIN:VEVENT"
                lines += "UID:${escapeIcs(uid)}"
                lines += "DTSTAMP:$generatedAt"
                lines += "SUMMARY:${escapeIcs(subject.name)}"
                lines += "DESCRIPTION:${escapeIcs(description)}"
                lines += "LOCATION:${escapeIcs(room)}"

                lines += "DTSTART:${
                    utcFormatter.format(
                        startDateTime.toInstant()
                    )
                }"

                lines += "DTEND:${
                    utcFormatter.format(
                        endDateTime.toInstant()
                    )
                }"

                lines += buildString {
                    append("RRULE:FREQ=WEEKLY")
                    append(";BYDAY=")
                    append(dayToRRule(slot.dayOfWeek))
                    append(";UNTIL=")
                    append(utcFormatter.format(recurrenceEnd))
                }

                lines += "END:VEVENT"
            }
        }

        lines += "END:VCALENDAR"

        // Los archivos ICS deben usar CRLF.
        return lines.joinToString(separator = "\r\n") + "\r\n"
    }

    fun createIcsShareIntent(
        context: Context,
        subjectsWithSlots: List<SubjectWithSlots>,
        zoneId: ZoneId = ZoneId.systemDefault(),
        showProfessor: Boolean = true,
        showRoom: Boolean = true
    ): Intent {
        val exportDirectory = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDirectory, "horario-flowschedule.ics")
        var content = exportScheduleAsIcsText(context, subjectsWithSlots, zoneId)
        if (!showProfessor) content = content.replace(Regex("DESCRIPTION:Profesor:[^\\r\\n]*"), "DESCRIPTION:")
        if (!showRoom) content = content.replace(Regex("LOCATION:[^\\r\\n]*"), "LOCATION:")
        file.writeText(content, Charsets.UTF_8)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/calendar"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun dayToRRule(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            1 -> "MO"
            2 -> "TU"
            3 -> "WE"
            4 -> "TH"
            5 -> "FR"
            6 -> "SA"
            7 -> "SU"
            else -> throw IllegalArgumentException(
                "Día inválido: $dayOfWeek"
            )
        }
    }

    private fun escapeIcs(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace("\r\n", "\\n")
            .replace("\n", "\\n")
            .replace(";", "\\;")
            .replace(",", "\\,")
    }
}
