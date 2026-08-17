package com.moca.snapmyschedule.data.ocr.model

import com.moca.snapmyschedule.data.model.ScheduleBlock

data class ImportedClass(
    val subjectName: String,
    val subjectCode: String = "",
    val teacher: String = "",
    val group: String = "",
    val credits: String = "",
    val scheduleBlocks: List<ScheduleBlock>,
    val warnings: List<String> = emptyList()
)