package com.mocas.data.repository

import android.content.ContentUris
import android.content.ContentProviderOperation
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.mocas.data.local.ClassExceptionEntity
import com.mocas.data.local.ClassExceptionType
import com.mocas.data.local.SchoolEventEntity
import com.mocas.data.local.SubjectWithSlots
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import androidx.core.graphics.toColorInt

object CalendarSyncManager {

    private const val CALENDAR_NAME = "FlowSchedule"
    private const val CALENDAR_DISPLAY_NAME = "FlowSchedule Academic"
    private const val ACCOUNT_NAME = "flowschedule@local"
    private const val ACCOUNT_TYPE = CalendarContract.ACCOUNT_TYPE_LOCAL

    private val utcFormatter: DateTimeFormatter =
        DateTimeFormatter
            .ofPattern("yyyyMMdd'T'HHmmss'Z'")
            .withZone(ZoneId.of("UTC"))

    fun hasCalendarPermissions(context: Context): Boolean {
        val readGranted = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED

        val writeGranted = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.WRITE_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED

        return readGranted && writeGranted
    }

    fun getOrCreateFlowScheduleCalendar(context: Context): Long? {
        if (!hasCalendarPermissions(context)) return null

        val projection = arrayOf(CalendarContract.Calendars._ID)

        val selection = "${CalendarContract.Calendars.ACCOUNT_NAME} = ? AND ${CalendarContract.Calendars.ACCOUNT_TYPE} = ? AND ${CalendarContract.Calendars.NAME} = ?"
        val selectionArgs = arrayOf(ACCOUNT_NAME, ACCOUNT_TYPE, CALENDAR_NAME)

        val calendarIds = mutableListOf<Long>()
        context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            val idIdx = cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID)
            while (cursor.moveToNext()) {
                calendarIds.add(cursor.getLong(idIdx))
            }
        }

        if (calendarIds.isNotEmpty()) {
            val mainCalendarId = calendarIds.first()
            // Eliminar calendarios duplicados si existían previamente
            for (i in 1 until calendarIds.size) {
                val deleteUri = ContentUris.withAppendedId(CalendarContract.Calendars.CONTENT_URI, calendarIds[i])
                    .buildUpon()
                    .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                    .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
                    .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, ACCOUNT_TYPE)
                    .build()
                context.contentResolver.delete(deleteUri, null, null)
            }
            return mainCalendarId
        }

        val values = ContentValues().apply {
            put(CalendarContract.Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
            put(CalendarContract.Calendars.ACCOUNT_TYPE, ACCOUNT_TYPE)
            put(CalendarContract.Calendars.NAME, CALENDAR_NAME)
            put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, CALENDAR_DISPLAY_NAME)
            put(CalendarContract.Calendars.CALENDAR_COLOR, "#4F46E5".toColorInt())
            put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER)
            put(CalendarContract.Calendars.OWNER_ACCOUNT, ACCOUNT_NAME)
            put(CalendarContract.Calendars.SYNC_EVENTS, 1)
            put(CalendarContract.Calendars.VISIBLE, 1)
        }

        val uri = CalendarContract.Calendars.CONTENT_URI.buildUpon()
            .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, ACCOUNT_TYPE)
            .build()

        return try {
            val resultUri = context.contentResolver.insert(uri, values)
            resultUri?.lastPathSegment?.toLongOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @Synchronized
    fun syncLocalScheduleToCalendar(
        context: Context,
        subjects: List<SubjectWithSlots>,
        events: List<SchoolEventEntity>,
        exceptions: List<ClassExceptionEntity> = emptyList()
    ): Result<Int> {
        if (!hasCalendarPermissions(context)) {
            return Result.failure(SecurityException("Permisos de calendario no concedidos"))
        }

        val calendarId = try {
            getOrCreateFlowScheduleCalendar(context)
        } catch (e: Exception) {
            return Result.failure(e)
        } ?: return Result.failure(IllegalStateException("No se pudo crear o acceder al calendario de FlowSchedule"))

        val zoneId = ZoneId.systemDefault()
        return try {
            val desiredEvents = linkedMapOf<String, ContentValues>()

            for ((subject, slots) in subjects) {
                val semesterStart = runCatching { LocalDate.parse(subject.semesterStart) }.getOrNull() ?: continue
                val semesterEnd = runCatching { LocalDate.parse(subject.semesterEnd) }.getOrNull() ?: continue
                if (semesterEnd.isBefore(semesterStart)) continue

                val uniqueSlots = slots.distinctBy { "${it.dayOfWeek}_${it.startTime}_${it.endTime}" }
                for ((id, _, dayOfWeek, startTime1, endTime1, room1) in uniqueSlots) {
                    if (dayOfWeek !in 1..7) continue
                    val startTime = runCatching { LocalTime.parse(startTime1) }.getOrNull() ?: continue
                    val endTime = runCatching { LocalTime.parse(endTime1) }.getOrNull() ?: continue
                    val classDay = DayOfWeek.of(dayOfWeek)
                    val firstClassDate = semesterStart.with(TemporalAdjusters.nextOrSame(classDay))
                    if (firstClassDate.isAfter(semesterEnd)) continue

                    val startDateTime = firstClassDate.atTime(startTime).atZone(zoneId)
                    var endDateTime = firstClassDate.atTime(endTime).atZone(zoneId)
                    if (!endDateTime.isAfter(startDateTime)) endDateTime = endDateTime.plusDays(1)
                    val durationMinutes = java.time.Duration.between(startTime, endTime)
                        .toMinutes()
                        .let { if (it <= 0) it + 24 * 60 else it }
                    val durationHoursPart = durationMinutes / 60
                    val durationMinutesPart = durationMinutes % 60
                    val recurrenceDuration = buildString {
                        append("PT")
                        if (durationHoursPart > 0) append("${durationHoursPart}H")
                        if (durationMinutesPart > 0) append("${durationMinutesPart}M")
                    }

                    val recurrenceEnd = semesterEnd.plusDays(1).atStartOfDay(zoneId).minusSeconds(1).toInstant()
                    val recurrenceRule = "FREQ=WEEKLY;BYDAY=${dayToRRule(dayOfWeek)};UNTIL=${utcFormatter.format(recurrenceEnd)}"
                    val canceledDates = exceptions.filter {
                        it.slotId == id && it.type == ClassExceptionType.CANCELED
                    }.mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }
                    val exdateString = canceledDates.takeIf { it.isNotEmpty() }?.joinToString(",") { date ->
                        utcFormatter.format(date.atTime(startTime).atZone(zoneId).toInstant())
                    }

                    val room = room1.ifBlank { subject.defaultRoom }
                    val description = buildString {
                        if (subject.professor.isNotBlank()) appendLine("Profesor: ${subject.professor}")
                        if (subject.code.isNotBlank()) appendLine("Código: ${subject.code}")
                        appendLine("Aula: $room")
                    }
                    val stableKey = "class:${subject.id}:$dayOfWeek:$startTime:$endTime"
                    desiredEvents.putIfAbsent(stableKey, ContentValues().apply {
                        put(CalendarContract.Events.CALENDAR_ID, calendarId)
                        put(CalendarContract.Events.SYNC_DATA1, stableKey)
                        put(CalendarContract.Events.TITLE, "📚 ${subject.name}")
                        put(CalendarContract.Events.DESCRIPTION, description)
                        put(CalendarContract.Events.EVENT_LOCATION, room)
                        put(CalendarContract.Events.DTSTART, startDateTime.toInstant().toEpochMilli())
                        put(CalendarContract.Events.DTEND, endDateTime.toInstant().toEpochMilli())
                        put(CalendarContract.Events.DURATION, recurrenceDuration)
                        put(CalendarContract.Events.EVENT_TIMEZONE, zoneId.id)
                        put(CalendarContract.Events.RRULE, recurrenceRule)
                        if (exdateString.isNullOrBlank()) putNull(CalendarContract.Events.EXDATE)
                        else put(CalendarContract.Events.EXDATE, exdateString)
                        put(CalendarContract.Events.ALL_DAY, 0)
                        put(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
                    })
                }
            }

            for ((id, title, type, _, _, _, startDate1, endDate1, startTime, endTime, isAllDay, location, description) in events.filter { !it.isDeleted && !it.isCompleted }) {
                val startDate = runCatching { LocalDate.parse(startDate1) }.getOrNull() ?: continue
                val endDate = runCatching { LocalDate.parse(endDate1) }.getOrNull() ?: startDate
                val startMillis: Long
                val endMillis: Long
                if (isAllDay) {
                    startMillis = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
                    endMillis = endDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
                } else {
                    val pStart = startTime?.let { runCatching { LocalTime.parse(it) }.getOrNull() } ?: LocalTime.of(8, 0)
                    val pEnd = endTime?.let { runCatching { LocalTime.parse(it) }.getOrNull() } ?: pStart.plusHours(1)
                    startMillis = startDate.atTime(pStart).atZone(zoneId).toInstant().toEpochMilli()
                    endMillis = endDate.atTime(pEnd).atZone(zoneId).toInstant().toEpochMilli()
                }
                val stableKey = "activity:$id"
                desiredEvents[stableKey] = ContentValues().apply {
                    put(CalendarContract.Events.CALENDAR_ID, calendarId)
                    put(CalendarContract.Events.SYNC_DATA1, stableKey)
                    put(CalendarContract.Events.TITLE, "[${type.displayName}] $title")
                    put(CalendarContract.Events.DESCRIPTION, description)
                    put(CalendarContract.Events.EVENT_LOCATION, location)
                    put(CalendarContract.Events.DTSTART, startMillis)
                    put(CalendarContract.Events.DTEND, endMillis)
                    put(CalendarContract.Events.ALL_DAY, isAllDay)
                    put(CalendarContract.Events.EVENT_TIMEZONE, zoneId.id)
                    put(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
                }
            }

            reconcileCalendarEvents(context, calendarId, desiredEvents)
            Result.success(desiredEvents.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private data class ExistingCalendarEvent(
        val id: Long,
        val key: String?,
        val values: Map<String, String?>
    )

    private fun reconcileCalendarEvents(
        context: Context,
        calendarId: Long,
        desiredEvents: Map<String, ContentValues>
    ) {
        val resolver = context.contentResolver
        val syncAdapterEventsUri = CalendarContract.Events.CONTENT_URI.buildUpon()
            .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, ACCOUNT_TYPE)
            .build()
        val comparedColumns = listOf(
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.EVENT_LOCATION,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.DURATION,
            CalendarContract.Events.EVENT_TIMEZONE,
            CalendarContract.Events.RRULE,
            CalendarContract.Events.EXDATE,
            CalendarContract.Events.ALL_DAY,
            CalendarContract.Events.AVAILABILITY
        )
        val projection = (listOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.SYNC_DATA1
        ) + comparedColumns).toTypedArray()
        val existing = mutableListOf<ExistingCalendarEvent>()
        resolver.query(
            syncAdapterEventsUri,
            projection,
            "${CalendarContract.Events.CALENDAR_ID} = ?",
            arrayOf(calendarId.toString()),
            null
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(CalendarContract.Events._ID)
            val keyIndex = cursor.getColumnIndexOrThrow(CalendarContract.Events.SYNC_DATA1)
            while (cursor.moveToNext()) {
                val values = comparedColumns.associateWith { column ->
                    val index = cursor.getColumnIndexOrThrow(column)
                    if (cursor.isNull(index)) null else cursor.getString(index)
                }
                existing += ExistingCalendarEvent(
                    id = cursor.getLong(idIndex),
                    key = cursor.getString(keyIndex),
                    values = values
                )
            }
        }

        val operations = mutableListOf<ContentProviderOperation>()
        val retainedKeys = mutableSetOf<String>()
        for ((id, storedKey, values) in existing) {
            val desired = storedKey?.let(desiredEvents::get)
            if (desired == null || !retainedKeys.add(storedKey)) {
                operations += ContentProviderOperation.newDelete(
                    ContentUris.withAppendedId(syncAdapterEventsUri, id)
                ).build()
                continue
            }

            val changed = comparedColumns.any { column ->
                val wanted = desired.get(column)?.toString()
                values[column] != wanted
            }
            if (changed) {
                operations += ContentProviderOperation.newUpdate(
                    ContentUris.withAppendedId(syncAdapterEventsUri, id)
                ).withValues(desired).build()
            }
        }

        desiredEvents.forEach { (key, values) ->
            if (key !in retainedKeys) {
                operations += ContentProviderOperation.newInsert(syncAdapterEventsUri)
                    .withValues(values)
                    .build()
            }
        }

        operations.chunked(100).forEach { batch ->
            resolver.applyBatch(CalendarContract.AUTHORITY, ArrayList(batch))
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
            else -> "MO"
        }
    }
}
