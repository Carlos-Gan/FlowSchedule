package com.mocas.ui.components.calendar

import com.mocas.data.local.ClassExceptionEntity
import com.mocas.data.local.SchoolEventType
import com.mocas.data.local.SchoolEventWithSubject
import com.mocas.data.local.SubjectWithSlots
import com.mocas.ui.util.forDate
import com.mocas.ui.util.isVacationDate
import com.mocas.ui.viewmodel.ScheduleViewModel
import com.mocas.util.DateTimeUtils
import java.time.YearMonth

fun generateMonthDays(
    calendarMonth: YearMonth,
    selectedDateStr: String,
    allEvents: List<SchoolEventWithSubject>,
    subjectsWithSlots: List<SubjectWithSlots>,
    academicPeriods: List<com.mocas.data.local.AcademicPeriodEntity>,
    classExceptions: List<ClassExceptionEntity>
): List<com.mocas.ui.model.CalendarDayItem> {
    val list = mutableListOf<com.mocas.ui.model.CalendarDayItem>()
    val year = calendarMonth.year
    val month = calendarMonth.monthValue
    val maxDays = calendarMonth.lengthOfMonth()
    val leadingOffset = calendarMonth.atDay(1).dayOfWeek.value - 1

    // Today string
    val todayStr = ScheduleViewModel.getTodayDateString()

    // Leading days from previous month
    for (i in 0 until leadingOffset) {
        list.add(
            com.mocas.ui.model.CalendarDayItem(
                dateString = "",
                dayNumber = 0,
                isCurrentMonth = false,
                isToday = false,
                isSelected = false
            )
        )
    }

    // Days of current month
    for (day in 1..maxDays) {
        val date = calendarMonth.atDay(day)
        val dateStr = date.toString()
        val dayOfWeekNum = date.dayOfWeek.value

        val hasClasses = subjectsWithSlots.any { sub ->
            val start = DateTimeUtils.parseDate(sub.subject.semesterStart)
            val end = DateTimeUtils.parseDate(sub.subject.semesterEnd)
            start != null && end != null && date in start..end &&
                    sub.slots.any {
                        it.dayOfWeek == dayOfWeekNum && it.forDate(date, classExceptions) != null
                    }
        }
        val dayEvents = allEvents.filter { it.event.startDate <= dateStr && it.event.endDate >= dateStr }
        val hasTasks = dayEvents.any { it.event.type == SchoolEventType.TAREA }
        val hasExams = dayEvents.any { it.event.type == SchoolEventType.EXAMEN }
        val hasEvents = dayEvents.any {
            it.event.type == SchoolEventType.EVENTO_ESCOLAR || it.event.type == SchoolEventType.EXPOSICION
        }
        val isHoliday = isVacationDate(
            date = date,
            academicPeriods = academicPeriods,
            events = allEvents,
            outsidePeriodsAreVacations = true // Forzado a true para el calendario
        )

        list.add(
            com.mocas.ui.model.CalendarDayItem(
                dateString = dateStr,
                dayNumber = day,
                isCurrentMonth = true,
                isToday = dateStr == todayStr,
                isSelected = dateStr == selectedDateStr,
                hasClasses = hasClasses,
                hasTasks = hasTasks,
                hasExams = hasExams,
                hasEvents = hasEvents,
                isHoliday = isHoliday
            )
        )
    }

    // Trailing to complete multiple of 7
    while (list.size % 7 != 0) {
        list.add(
            com.mocas.ui.model.CalendarDayItem(
                dateString = "",
                dayNumber = 0,
                isCurrentMonth = false,
                isToday = false,
                isSelected = false
            )
        )
    }

    return list
}
