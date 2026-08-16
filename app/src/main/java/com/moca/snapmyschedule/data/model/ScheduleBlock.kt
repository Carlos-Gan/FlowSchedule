package com.moca.snapmyschedule.data.model

data class ScheduleBlock(
    val days: Set<WeekDay>,
    val startTime: String,
    val endTime: String,
    val room: String
)