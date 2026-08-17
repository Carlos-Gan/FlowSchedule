package com.moca.snapmyschedule.data.ocr.model

data class TableParseResult(
    val classes: List<ImportedClass>,
    val warnings: List<String> = emptyList()
)