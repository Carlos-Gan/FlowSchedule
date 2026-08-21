package com.example.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale

object DateTimeUtils {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val spanishLocale = Locale("es", "MX")
    private val readableDateFormatter =
        DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", spanishLocale)
    private val shortDateFormatter =
        DateTimeFormatter.ofPattern("d MMM yyyy", spanishLocale)
    private val twelveHourFormatter =
        DateTimeFormatter.ofPattern("h:mm a", spanishLocale)

    fun parseDate(value: String): LocalDate? = try {
        LocalDate.parse(value, dateFormatter)
    } catch (_: DateTimeParseException) {
        null
    }

    fun parseTime(value: String): LocalTime? = try {
        LocalTime.parse(value, timeFormatter)
    } catch (_: DateTimeParseException) {
        null
    }

    fun isValidDate(value: String): Boolean = parseDate(value) != null

    fun isValidTime(value: String): Boolean = parseTime(value) != null

    fun today(): LocalDate = LocalDate.now()

    fun todayString(): String = today().format(dateFormatter)

    fun currentDayOfWeek(): Int = today().dayOfWeek.value

    fun formatDate(value: String, includeWeekday: Boolean = false): String {
        val date = parseDate(value) ?: return value
        return date.format(if (includeWeekday) readableDateFormatter else shortDateFormatter)
            .replaceFirstChar { it.titlecase(spanishLocale) }
    }

    fun formatTime(value: String?, use24Hour: Boolean = true): String? {
        if (value.isNullOrBlank()) return null
        val time = parseTime(value) ?: return value
        return time.format(if (use24Hour) timeFormatter else twelveHourFormatter)
            .lowercase(spanishLocale)
    }

    fun firstDateForDay(semesterStart: String, dayOfWeek: Int): LocalDate? {
        if (dayOfWeek !in 1..7) return null
        return parseDate(semesterStart)?.with(
            TemporalAdjusters.nextOrSame(DayOfWeek.of(dayOfWeek))
        )
    }

    fun toMillis(date: String, time: String, zoneId: ZoneId = ZoneId.systemDefault()): Long? {
        val parsedDate = parseDate(date) ?: return null
        val parsedTime = parseTime(time) ?: return null
        return LocalDateTime.of(parsedDate, parsedTime)
            .atZone(zoneId)
            .toInstant()
            .toEpochMilli()
    }

    fun daysRemaining(date: String): Long? =
        parseDate(date)?.let { ChronoUnit.DAYS.between(today(), it) }

    fun timeToMinutes(value: String): Int? =
        parseTime(value)?.let { it.hour * 60 + it.minute }

    fun endIsAfterStart(startTime: String, endTime: String): Boolean {
        val start = parseTime(startTime) ?: return false
        val end = parseTime(endTime) ?: return false
        return end.isAfter(start)
    }
}
