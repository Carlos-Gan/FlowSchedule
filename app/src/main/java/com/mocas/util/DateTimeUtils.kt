package com.mocas.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale

object DateTimeUtils {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    
    private val currentLocale: Locale get() = Locale.getDefault()

    private val readableDateFormatter: DateTimeFormatter get() {
        val pattern = when (currentLocale.language.lowercase(Locale.ROOT)) {
            "es", "pt" -> "EEEE, d 'de' MMMM 'de' yyyy"
            "it" -> "EEEE d MMMM yyyy"
            else -> "EEEE, MMMM d, yyyy"
        }
        return DateTimeFormatter.ofPattern(pattern, currentLocale)
    }
    
    private val shortDateFormatter: DateTimeFormatter get() =
        DateTimeFormatter.ofPattern("d MMM yyyy", currentLocale)
    
    private val twelveHourFormatter: DateTimeFormatter get() =
        DateTimeFormatter.ofPattern("h:mm a", currentLocale)

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
            .replaceFirstChar { it.titlecase(currentLocale) }
    }

    fun formatTime(value: String?, use24Hour: Boolean = true): String? {
        if (value.isNullOrBlank()) return null
        val time = parseTime(value) ?: return value
        return time.format(if (use24Hour) timeFormatter else twelveHourFormatter)
            .lowercase(currentLocale)
    }

    fun firstDateForDay(semesterStart: String, dayOfWeek: Int): LocalDate? {
        if (dayOfWeek !in 1..7) return null
        return parseDate(semesterStart)?.with(
            TemporalAdjusters.nextOrSame(DayOfWeek.of(dayOfWeek))
        )
    }

    fun daysRemaining(date: String): Long? =
        parseDate(date)?.let { ChronoUnit.DAYS.between(today(), it) }

    fun timeToMinutes(value: String): Int? =
        parseTime(value)?.let { it.hour * 60 + it.minute }

    fun getEndTime(startTime: String): String {
        val time = parseTime(startTime) ?: return startTime
        return time.plusHours(1).format(timeFormatter)
    }

    fun endIsAfterStart(startTime: String, endTime: String): Boolean {
        val start = parseTime(startTime) ?: return false
        val end = parseTime(endTime) ?: return false
        return end.isAfter(start)
    }

    fun formatRelativeDate(dateString: String): String {
        val date = parseDate(dateString) ?: return dateString
        val today = LocalDate.now()
        val lang = currentLocale.language.lowercase(Locale.ROOT)
        return when (val daysBetween = ChronoUnit.DAYS.between(today, date)) {
            0L -> when (lang) {
                "es" -> "Hoy"
                "pt" -> "Hoje"
                "it" -> "Oggi"
                else -> "Today"
            }
            1L -> when (lang) {
                "es" -> "Mañana"
                "pt" -> "Amanhã"
                "it" -> "Domani"
                else -> "Tomorrow"
            }
            -1L -> when (lang) {
                "es" -> "Ayer"
                "pt" -> "Ontem"
                "it" -> "Ieri"
                else -> "Yesterday"
            }
            in 2L..7L -> when (lang) {
                "es" -> "En $daysBetween días"
                "pt" -> "Em $daysBetween dias"
                "it" -> "Tra $daysBetween giorni"
                else -> "In $daysBetween days"
            }
            in -7L..-2L -> when (lang) {
                "es" -> "Hace ${-daysBetween} días"
                "pt" -> "Há ${-daysBetween} dias"
                "it" -> "${-daysBetween} giorni fa"
                else -> "${-daysBetween} days ago"
            }
            else -> formatDate(dateString)
        }
    }
}
