package com.mocas.ui.util

import com.mocas.data.local.AcademicPeriodEntity
import com.mocas.data.local.SchoolEventType
import com.mocas.data.local.SchoolEventWithSubject
import com.mocas.util.DateTimeUtils
import java.time.LocalDate

internal fun isVacationDate(
    date: LocalDate,
    academicPeriods: List<AcademicPeriodEntity>,
    events: List<SchoolEventWithSubject>,
    outsidePeriodsAreVacations: Boolean
): Boolean {
    val dateString = date.toString()
    val isExplicitVacation = events.any { item ->
        item.event.type == SchoolEventType.VACACIONES &&
            dateString >= item.event.startDate &&
            dateString <= item.event.endDate
    }
    if (isExplicitVacation) return true
    if (!outsidePeriodsAreVacations) return false

    val validPeriods = academicPeriods.mapNotNull { period ->
        val start = DateTimeUtils.parseDate(period.startDate)
        val end = DateTimeUtils.parseDate(period.endDate)
        if (start != null && end != null && start <= end) start..end else null
    }

    return validPeriods.isNotEmpty() && validPeriods.none { date in it }
}
