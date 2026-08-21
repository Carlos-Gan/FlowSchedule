package com.mocas.ui.util

import com.mocas.data.local.SubjectWithSlots
import com.mocas.data.local.ScheduleSlotEntity
import com.mocas.data.local.ClassExceptionEntity
import com.mocas.data.local.ClassExceptionType
import com.mocas.util.DateTimeUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val weekLocale = Locale.forLanguageTag("es-MX")

internal fun weekRangeLabel(weekStart: LocalDate): String {
    val weekEnd = weekStart.plusDays(6)
    val dayMonth = DateTimeFormatter.ofPattern("d MMM", weekLocale)
    return if (weekStart.year == weekEnd.year) {
        "${weekStart.format(dayMonth)} – ${weekEnd.format(dayMonth)} ${weekEnd.year}"
    } else {
        "${weekStart.format(dayMonth)} ${weekStart.year} – ${weekEnd.format(dayMonth)} ${weekEnd.year}"
    }
}

internal fun compactDayDate(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("d MMM", weekLocale)).uppercase(weekLocale)

internal fun fullDayDate(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", weekLocale))
        .replaceFirstChar { it.titlecase(weekLocale) }

internal fun SubjectWithSlots.isActiveOn(date: LocalDate): Boolean {
    val start = DateTimeUtils.parseDate(subject.semesterStart)
    val end = DateTimeUtils.parseDate(subject.semesterEnd)
    return start != null && end != null && date in start..end
}

internal fun ScheduleSlotEntity.forDate(
    date: LocalDate,
    exceptions: List<ClassExceptionEntity>
): ScheduleSlotEntity? {
    val exception = exceptions.firstOrNull { it.slotId == id && it.date == date.toString() }
    if (exception?.type == ClassExceptionType.CANCELED) return null
    return if (exception?.type == ClassExceptionType.MODIFIED) {
        copy(
            startTime = exception.newStartTime ?: startTime,
            endTime = exception.newEndTime ?: endTime,
            room = exception.newRoom ?: room
        )
    } else this
}
