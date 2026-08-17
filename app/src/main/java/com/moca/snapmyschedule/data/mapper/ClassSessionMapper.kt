package com.moca.snapmyschedule.data.mapper

import com.moca.snapmyschedule.data.local.entity.ClassSessionEntity
import com.moca.snapmyschedule.data.model.ClassSession
import com.moca.snapmyschedule.data.model.WeekDay

fun ClassSessionEntity.toModel(): ClassSession {
    return ClassSession(
        id = id,
        courseId = courseId,
        subjectName = subjectName,
        subjectCode = subjectCode,
        teacher = teacher,
        room = room,
        day = day.toWeekDay(),
        startTime = startTime,
        endTime = endTime
    )
}

fun ClassSession.toEntity(): ClassSessionEntity {
    return ClassSessionEntity(
        id = id,
        courseId = courseId,
        subjectName = subjectName,
        subjectCode = subjectCode,
        teacher = teacher,
        room = room,
        day = day.name,
        dayOrder = day.ordinal,
        startTime = startTime,
        endTime = endTime
    )
}

private fun String.toWeekDay(): WeekDay {
    return runCatching {
        WeekDay.valueOf(this)
    }.getOrDefault(WeekDay.MONDAY)
}