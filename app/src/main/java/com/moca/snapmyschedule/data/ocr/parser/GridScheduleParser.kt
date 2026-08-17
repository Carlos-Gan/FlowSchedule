package com.moca.snapmyschedule.data.ocr.parser

import com.moca.snapmyschedule.data.model.ScheduleBlock
import com.moca.snapmyschedule.data.model.WeekDay
import com.moca.snapmyschedule.data.ocr.ScheduleOcrResult
import com.moca.snapmyschedule.data.ocr.model.DetectedDayColumn
import com.moca.snapmyschedule.data.ocr.model.GridParseResult
import com.moca.snapmyschedule.data.ocr.model.ImportedClass
import kotlin.math.abs

class GridScheduleParser(
    private val dayColumnDetector: DayColumnDetector =
        DayColumnDetector()
) {

    fun parse(
        result: ScheduleOcrResult
    ): GridParseResult {
        if (
            result.imageWidth <= 0 ||
            result.imageHeight <= 0
        ) {
            return GridParseResult(
                classes = emptyList(),
                warnings = listOf(
                    "La imagen no tiene dimensiones válidas."
                )
            )
        }

        val dayColumns =
            dayColumnDetector.detect(result)

        if (dayColumns.isEmpty()) {
            return GridParseResult(
                classes = emptyList(),
                warnings = listOf(
                    "No se detectaron columnas de días."
                )
            )
        }

        val detectedCells =
            dayColumns.flatMap { column ->
                parseDayColumn(
                    result = result,
                    column = column
                )
            }

        val classes =
            mergeCellsIntoClasses(
                cells = detectedCells
            )

        val warnings = buildList {
            if (detectedCells.isEmpty()) {
                add(
                    "Se detectaron los días, pero no se " +
                            "encontraron tarjetas con horarios."
                )
            }

            if (classes.isEmpty()) {
                add(
                    "No fue posible formar materias con " +
                            "los datos reconocidos."
                )
            }

            if (
                classes.any { importedClass ->
                    importedClass.subjectName.contains("...")
                }
            ) {
                add(
                    "Algunos nombres están recortados en la " +
                            "imagen y deberán corregirse."
                )
            }
        }

        return GridParseResult(
            classes = classes,
            warnings = warnings
        )
    }

    private fun parseDayColumn(
        result: ScheduleOcrResult,
        column: DetectedDayColumn
    ): List<DetectedGridCell> {
        val columnLines =
            result.lines
                .mapNotNull { line ->
                    val normalizedX =
                        line.centerX.toFloat() /
                                result.imageWidth.toFloat()

                    val normalizedY =
                        line.centerY.toFloat() /
                                result.imageHeight.toFloat()

                    if (
                        normalizedX >=
                        column.leftBoundary &&
                        normalizedX <=
                        column.rightBoundary &&
                        normalizedY >=
                        column.contentTop
                    ) {
                        SpatialText(
                            text = line.text.trim(),
                            centerX = normalizedX,
                            centerY = normalizedY
                        )
                    } else {
                        null
                    }
                }
                .filter { spatialText ->
                    spatialText.text.isNotBlank()
                }
                .sortedBy { spatialText ->
                    spatialText.centerY
                }

        val columnElements =
            result.elements
                .mapNotNull { element ->
                    val normalizedX =
                        element.centerX.toFloat() /
                                result.imageWidth.toFloat()

                    val normalizedY =
                        element.centerY.toFloat() /
                                result.imageHeight.toFloat()

                    if (
                        normalizedX >=
                        column.leftBoundary &&
                        normalizedX <=
                        column.rightBoundary &&
                        normalizedY >=
                        column.contentTop
                    ) {
                        SpatialText(
                            text = element.text.trim(),
                            centerX = normalizedX,
                            centerY = normalizedY
                        )
                    } else {
                        null
                    }
                }
                .filter { spatialText ->
                    spatialText.text.isNotBlank()
                }
                .sortedBy { spatialText ->
                    spatialText.centerY
                }

        val anchorsFromLines =
            columnLines.mapNotNull { spatialText ->
                parseTimeRange(
                    spatialText.text
                )?.let { timeRange ->
                    TimeAnchor(
                        startTime = timeRange.startTime,
                        endTime = timeRange.endTime,
                        centerX = spatialText.centerX,
                        centerY = spatialText.centerY
                    )
                }
            }

        val anchorsFromElements =
            columnElements.mapNotNull { spatialText ->
                parseTimeRange(
                    spatialText.text
                )?.let { timeRange ->
                    TimeAnchor(
                        startTime = timeRange.startTime,
                        endTime = timeRange.endTime,
                        centerX = spatialText.centerX,
                        centerY = spatialText.centerY
                    )
                }
            }

        val anchors =
            removeDuplicateAnchors(
                anchorsFromLines +
                        anchorsFromElements
            )

        if (anchors.isEmpty()) {
            return emptyList()
        }

        val averageGap =
            anchors
                .sortedBy { anchor ->
                    anchor.centerY
                }
                .zipWithNext { first, second ->
                    second.centerY -
                            first.centerY
                }
                .filter { gap ->
                    gap > 0f
                }
                .takeIf { gaps ->
                    gaps.isNotEmpty()
                }
                ?.average()
                ?.toFloat()
                ?: 0.065f

        return anchors
            .sortedBy { anchor ->
                anchor.centerY
            }
            .mapIndexedNotNull { index, anchor ->
                val previousGap =
                    if (index > 0) {
                        anchor.centerY -
                                anchors[index - 1].centerY
                    } else {
                        averageGap
                    }

                val nextGap =
                    if (index < anchors.lastIndex) {
                        anchors[index + 1].centerY -
                                anchor.centerY
                    } else {
                        averageGap
                    }

                /*
                 * En este tipo de horario, el nombre y el
                 * profesor suelen aparecer encima del rango
                 * de horas de la tarjeta.
                 */
                val cardTop =
                    (
                            anchor.centerY -
                                    previousGap * 0.82f
                            ).coerceAtLeast(
                            column.contentTop
                        )

                val cardBottom =
                    (
                            anchor.centerY +
                                    nextGap * 0.20f
                            ).coerceAtMost(1f)

                parseCard(
                    column = column,
                    anchor = anchor,
                    cardTop = cardTop,
                    cardBottom = cardBottom,
                    lines = columnLines,
                    elements = columnElements
                )
            }
    }

    private fun parseCard(
        column: DetectedDayColumn,
        anchor: TimeAnchor,
        cardTop: Float,
        cardBottom: Float,
        lines: List<SpatialText>,
        elements: List<SpatialText>
    ): DetectedGridCell? {
        val cardLines =
            lines
                .filter { spatialText ->
                    spatialText.centerY >= cardTop &&
                            spatialText.centerY <= cardBottom
                }
                .filterNot { spatialText ->
                    parseTimeRange(
                        spatialText.text
                    ) != null
                }
                .filterNot { spatialText ->
                    spatialText.text
                        .toWeekDayOrNull() != null
                }
                .filterNot { spatialText ->
                    isStandaloneTime(
                        spatialText.text
                    )
                }
                .distinctBy { spatialText ->
                    spatialText.text
                        .normalizeForOcrMatch()
                }
                .sortedBy { spatialText ->
                    spatialText.centerY
                }

        val room =
            detectRoom(
                anchor = anchor,
                elements = elements,
                cardTop = cardTop,
                cardBottom = cardBottom
            )

        val contentLines =
            cardLines
                .map { spatialText ->
                    spatialText.text.trim()
                }
                .filter { text ->
                    text.isNotBlank()
                }
                .filterNot { text ->
                    room.isNotBlank() &&
                            text.replace(" ", "")
                                .equals(
                                    room,
                                    ignoreCase = true
                                )
                }

        val subjectName =
            contentLines.firstOrNull()
                .orEmpty()

        val teacher =
            contentLines
                .drop(1)
                .firstOrNull()
                .orEmpty()

        if (
            subjectName.isBlank() &&
            teacher.isBlank()
        ) {
            return null
        }

        val warnings = buildList {
            if (subjectName.isBlank()) {
                add(
                    "No se detectó el nombre de la materia."
                )
            }

            if (subjectName.contains("...")) {
                add(
                    "El nombre parece estar recortado."
                )
            }

            if (teacher.contains("...")) {
                add(
                    "El nombre del profesor parece estar recortado."
                )
            }

            if (room.isBlank()) {
                add(
                    "No se detectó el salón."
                )
            }
        }

        return DetectedGridCell(
            day = column.day,
            subjectName =
                subjectName.ifBlank {
                    "Materia sin nombre"
                },
            teacher = teacher,
            room = room,
            startTime = anchor.startTime,
            endTime = anchor.endTime,
            warnings = warnings
        )
    }

    private fun detectRoom(
        anchor: TimeAnchor,
        elements: List<SpatialText>,
        cardTop: Float,
        cardBottom: Float
    ): String {
        return elements
            .asSequence()
            .filter { spatialText ->
                spatialText.centerY >= cardTop &&
                        spatialText.centerY <= cardBottom
            }
            .filter { spatialText ->
                abs(
                    spatialText.centerY -
                            anchor.centerY
                ) <= 0.025f
            }
            .map { spatialText ->
                spatialText.text
                    .trim()
                    .replace(" ", "")
                    .uppercase()
            }
            .filter { text ->
                parseTimeRange(text) == null
            }
            .filter { text ->
                looksLikeRoom(text)
            }
            .minByOrNull { text ->
                /*
                 * Normalmente el salón es un código corto:
                 * SC9, SC6, LC23, LCRBD...
                 */
                text.length
            }
            .orEmpty()
    }

    private fun removeDuplicateAnchors(
        anchors: List<TimeAnchor>
    ): List<TimeAnchor> {
        val sortedAnchors =
            anchors.sortedBy { anchor ->
                anchor.centerY
            }

        val uniqueAnchors =
            mutableListOf<TimeAnchor>()

        sortedAnchors.forEach { anchor ->
            val existing =
                uniqueAnchors.firstOrNull { saved ->
                    saved.startTime ==
                            anchor.startTime &&
                            saved.endTime ==
                            anchor.endTime &&
                            abs(
                                saved.centerY -
                                        anchor.centerY
                            ) <= 0.01f
                }

            if (existing == null) {
                uniqueAnchors.add(anchor)
            }
        }

        return uniqueAnchors
    }

    private fun mergeCellsIntoClasses(
        cells: List<DetectedGridCell>
    ): List<ImportedClass> {
        return cells
            .groupBy { cell ->
                SubjectKey(
                    subject =
                        cell.subjectName
                            .normalizeForOcrMatch(),
                    teacher =
                        cell.teacher
                            .normalizeForOcrMatch()
                )
            }
            .map { (_, subjectCells) ->
                val firstCell =
                    subjectCells.first()

                val scheduleBlocks =
                    subjectCells
                        .groupBy { cell ->
                            BlockKey(
                                startTime =
                                    cell.startTime,
                                endTime =
                                    cell.endTime,
                                room =
                                    cell.room
                            )
                        }
                        .map { (key, groupedCells) ->
                            ScheduleBlock(
                                days = groupedCells
                                    .map { cell ->
                                        cell.day
                                    }
                                    .toSet(),
                                startTime =
                                    key.startTime,
                                endTime =
                                    key.endTime,
                                room = key.room
                            )
                        }
                        .sortedWith(
                            compareBy<ScheduleBlock> {
                                    block ->
                                block.startTime
                            }.thenBy { block ->
                                block.endTime
                            }
                        )

                ImportedClass(
                    subjectName =
                        firstCell.subjectName,
                    subjectCode = "",
                    teacher =
                        firstCell.teacher,
                    group = "",
                    credits = "",
                    scheduleBlocks =
                        scheduleBlocks,
                    warnings =
                        subjectCells
                            .flatMap { cell ->
                                cell.warnings
                            }
                            .distinct()
                )
            }
            .sortedBy { importedClass ->
                importedClass.scheduleBlocks
                    .minOfOrNull { block ->
                        block.startTime
                    }
                    .orEmpty()
            }
    }

    private fun parseTimeRange(
        text: String
    ): ParsedTimeRange? {
        val match =
            TIME_RANGE_REGEX.find(text)
                ?: return null

        val startHour =
            match.groupValues[1]
                .toIntOrNull()
                ?: return null

        val startMinute =
            match.groupValues[2]
                .toIntOrNull()
                ?: return null

        val endHour =
            match.groupValues[3]
                .toIntOrNull()
                ?: return null

        val endMinute =
            match.groupValues[4]
                .toIntOrNull()
                ?: return null

        if (
            startHour !in 0..23 ||
            endHour !in 0..23 ||
            startMinute !in 0..59 ||
            endMinute !in 0..59
        ) {
            return null
        }

        return ParsedTimeRange(
            startTime =
                "%02d:%02d".format(
                    startHour,
                    startMinute
                ),
            endTime =
                "%02d:%02d".format(
                    endHour,
                    endMinute
                )
        )
    }

    private fun isStandaloneTime(
        text: String
    ): Boolean {
        return STANDALONE_TIME_REGEX
            .matches(text.trim())
    }

    private fun looksLikeRoom(
        text: String
    ): Boolean {
        val compact =
            text.trim()
                .replace(" ", "")
                .uppercase()

        if (compact.length !in 2..12) {
            return false
        }

        if (
            compact.contains(":") ||
            compact.contains("...")
        ) {
            return false
        }

        /*
         * Acepta:
         * SC9
         * SC10
         * LC23
         * LCRBD
         *
         * Evita tomar palabras largas como profesor
         * o materia.
         */
        val containsDigit =
            compact.any { character ->
                character.isDigit()
            }

        val looksLikeAcronym =
            compact.length in 2..6 &&
                    compact.all { character ->
                        character.isUpperCase()
                    }

        return (
                containsDigit ||
                        looksLikeAcronym
                ) &&
                compact.all { character ->
                    character.isLetterOrDigit() ||
                            character == '-' ||
                            character == '_'
                }
    }

    private data class SpatialText(
        val text: String,
        val centerX: Float,
        val centerY: Float
    )

    private data class TimeAnchor(
        val startTime: String,
        val endTime: String,
        val centerX: Float,
        val centerY: Float
    )

    private data class ParsedTimeRange(
        val startTime: String,
        val endTime: String
    )

    private data class DetectedGridCell(
        val day: WeekDay,
        val subjectName: String,
        val teacher: String,
        val room: String,
        val startTime: String,
        val endTime: String,
        val warnings: List<String>
    )

    private data class SubjectKey(
        val subject: String,
        val teacher: String
    )

    private data class BlockKey(
        val startTime: String,
        val endTime: String,
        val room: String
    )

    companion object {
        private val TIME_RANGE_REGEX =
            Regex(
                """(\d{1,2})\s*:\s*(\d{2})\s*[-–—]+\s*(\d{1,2})\s*:\s*(\d{2})"""
            )

        private val STANDALONE_TIME_REGEX =
            Regex(
                """^\s*\d{1,2}\s*:\s*\d{2}\s*$"""
            )
    }
}