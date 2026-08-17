package com.moca.snapmyschedule.data.ocr.model

data class GridParseResult(
    val classes: List<ImportedClass>,
    val warnings: List<String> = emptyList()
)