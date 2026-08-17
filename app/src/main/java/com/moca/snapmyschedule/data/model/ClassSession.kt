package com.moca.snapmyschedule.data.model

data class ClassSession(
    val id: Long=0,
    val courseId: String = "",
    val subjectName: String,
    val subjectCode: String = "",
    val teacher: String = "",
    val room: String = "",
    val day: WeekDay,
    val startTime: String,
    val endTime: String
)