package com.moca.snapmyschedule.data.model

data class ClassFormData(
    val subjectName: String,
    val subjectCode: String,
    val teacher: String,
    val scheduleBlock: List<ScheduleBlock>
)