package com.moca.snapmyschedule.data.ocr.mapper

import com.moca.snapmyschedule.data.model.ClassFormData
import com.moca.snapmyschedule.data.ocr.formatter.toReadableTitleCase
import com.moca.snapmyschedule.data.ocr.model.ImportedClass

fun ImportedClass.toClassFormData(): ClassFormData {
    return ClassFormData(
        subjectName =
            subjectName.toReadableTitleCase(),
        subjectCode =
            subjectCode.trim().uppercase(),
        teacher =
            teacher.toReadableTitleCase(),
        scheduleBlocks =
            scheduleBlocks
    )
}