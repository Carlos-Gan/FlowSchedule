package com.moca.snapmyschedule.data.ocr

data class ScheduleOcrResult(
    val fullText: String,
    val imageWidth: Int,
    val imageHeight: Int,
    val lines: List<RecognizedTextLine>,
    val elements: List<RecognizedElement>
)