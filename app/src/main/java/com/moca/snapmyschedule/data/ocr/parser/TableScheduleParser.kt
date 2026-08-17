package com.moca.snapmyschedule.data.ocr.parser

import com.moca.snapmyschedule.data.model.ScheduleBlock
import com.moca.snapmyschedule.data.ocr.ScheduleOcrResult
import com.moca.snapmyschedule.data.ocr.model.DetectedDayColumn
import com.moca.snapmyschedule.data.ocr.model.ImportedClass
import com.moca.snapmyschedule.data.ocr.model.TableParseResult
import kotlin.math.abs

class TableScheduleParser(
    private val dayColumnDetector: DayColumnDetector =
        DayColumnDetector(),
    private val rowTolerance: Float = 0.025f
) {

    fun parse(
        result: ScheduleOcrResult
    ): TableParseResult {
        if (
            result.imageWidth <= 0 ||
            result.imageHeight <= 0
        ) {
            return TableParseResult(
                classes = emptyList(),
                warnings = listOf(
                    "La imagen no tiene dimensiones válidas."
                )
            )
        }

        val dayColumns =
            dayColumnDetector.detect(result)

        if (dayColumns.isEmpty()) {
            return TableParseResult(
                classes = emptyList(),
                warnings = listOf(
                    "No se detectaron columnas de días."
                )
            )
        }

        val contentTop =
            dayColumns.maxOf { column ->
                column.contentTop
            }

        val metadataColumns =
            detectMetadataColumns(
                result = result,
                dayColumns = dayColumns,
                contentTop = contentTop
            )

        val rowBands =
            detectRowBands(
                result = result,
                dayColumns = dayColumns,
                contentTop = contentTop
            )

        if (rowBands.isEmpty()) {
            return TableParseResult(
                classes = emptyList(),
                warnings = listOf(
                    "No se encontraron filas con horarios."
                )
            )
        }

        val importedClasses =
            rowBands.mapNotNull { rowBand ->
                parseRow(
                    result = result,
                    rowBand = rowBand,
                    dayColumns = dayColumns,
                    metadataColumns = metadataColumns
                )
            }

        val warnings = buildList {
            if (metadataColumns.subject == null) {
                add(
                    "No se encontró claramente la columna Materia."
                )
            }

            if (importedClasses.isEmpty()) {
                add(
                    "Se detectó la tabla, pero no fue posible " +
                            "extraer materias."
                )
            }
        }

        return TableParseResult(
            classes = importedClasses,
            warnings = warnings
        )
    }

    private fun detectMetadataColumns(
        result: ScheduleOcrResult,
        dayColumns: List<DetectedDayColumn>,
        contentTop: Float
    ): MetadataColumns {
        val firstDayColumn =
            dayColumns.minBy { column ->
                column.leftBoundary
            }

        val firstDayCenter =
            (
                    firstDayColumn.leftBoundary +
                            firstDayColumn.rightBoundary
                    ) / 2f

        val subjectCenter = findHeaderCenter(
            result = result,
            aliases = setOf(
                "materia",
                "asignatura",
                "subject",
                "curso"
            ),
            contentTop = contentTop
        )

        val groupCenter = findHeaderCenter(
            result = result,
            aliases = setOf(
                "gpo",
                "grupo",
                "group"
            ),
            contentTop = contentTop
        )

        val creditsCenter = findHeaderCenter(
            result = result,
            aliases = setOf(
                "cr",
                "credito",
                "creditos",
                "credit",
                "credits"
            ),
            contentTop = contentTop
        )

        val detectedHeaders = buildList {
            subjectCenter?.let {
                add(
                    MetadataHeader(
                        type = MetadataType.SUBJECT,
                        centerX = it
                    )
                )
            }

            groupCenter?.let {
                add(
                    MetadataHeader(
                        type = MetadataType.GROUP,
                        centerX = it
                    )
                )
            }

            creditsCenter?.let {
                add(
                    MetadataHeader(
                        type = MetadataType.CREDITS,
                        centerX = it
                    )
                )
            }
        }.sortedBy { header ->
            header.centerX
        }

        if (detectedHeaders.isEmpty()) {
            return MetadataColumns(
                subject = ColumnBounds(
                    left = 0f,
                    right =
                        firstDayColumn.leftBoundary
                ),
                group = null,
                credits = null
            )
        }

        fun boundsFor(
            index: Int
        ): ColumnBounds {
            val current =
                detectedHeaders[index]

            val left = if (index == 0) {
                0f
            } else {
                val previous =
                    detectedHeaders[index - 1]

                (
                        previous.centerX +
                                current.centerX
                        ) / 2f
            }

            val right =
                if (index == detectedHeaders.lastIndex) {
                    (
                            current.centerX +
                                    firstDayCenter
                            ) / 2f
                } else {
                    val next =
                        detectedHeaders[index + 1]

                    (
                            current.centerX +
                                    next.centerX
                            ) / 2f
                }

            return ColumnBounds(
                left = left.coerceIn(0f, 1f),
                right = right.coerceIn(0f, 1f)
            )
        }

        var subjectBounds: ColumnBounds? = null
        var groupBounds: ColumnBounds? = null
        var creditsBounds: ColumnBounds? = null

        detectedHeaders.forEachIndexed {
                index,
                header ->

            when (header.type) {
                MetadataType.SUBJECT ->
                    subjectBounds = boundsFor(index)

                MetadataType.GROUP ->
                    groupBounds = boundsFor(index)

                MetadataType.CREDITS ->
                    creditsBounds = boundsFor(index)
            }
        }

        /*
         * La columna Materia es indispensable.
         * Cuando el OCR no detecta su encabezado,
         * usamos todo el espacio antes de Gpo/Cr/días.
         */
        if (subjectBounds == null) {
            val firstOtherColumn =
                listOfNotNull(
                    groupBounds?.left,
                    creditsBounds?.left,
                    firstDayColumn.leftBoundary
                ).minOrNull()
                    ?: firstDayColumn.leftBoundary

            subjectBounds = ColumnBounds(
                left = 0f,
                right = firstOtherColumn
            )
        }

        return MetadataColumns(
            subject = subjectBounds,
            group = groupBounds,
            credits = creditsBounds
        )
    }

    private fun detectRowBands(
        result: ScheduleOcrResult,
        dayColumns: List<DetectedDayColumn>,
        contentTop: Float
    ): List<RowBand> {
        val timeCandidates = buildList {
            result.lines.forEach { line ->
                val normalizedX =
                    line.centerX.toFloat() /
                            result.imageWidth

                val normalizedY =
                    line.centerY.toFloat() /
                            result.imageHeight

                val belongsToDay =
                    dayColumns.any { column ->
                        normalizedX >=
                                column.leftBoundary &&
                                normalizedX <=
                                column.rightBoundary &&
                                normalizedY >=
                                column.contentTop
                    }

                if (
                    belongsToDay &&
                    parseTimeRange(line.text) != null
                ) {
                    add(normalizedY)
                }
            }

            result.elements.forEach { element ->
                val normalizedX =
                    element.centerX.toFloat() /
                            result.imageWidth

                val normalizedY =
                    element.centerY.toFloat() /
                            result.imageHeight

                val belongsToDay =
                    dayColumns.any { column ->
                        normalizedX >=
                                column.leftBoundary &&
                                normalizedX <=
                                column.rightBoundary &&
                                normalizedY >=
                                column.contentTop
                    }

                if (
                    belongsToDay &&
                    parseTimeRange(element.text) != null
                ) {
                    add(normalizedY)
                }
            }
        }

        if (timeCandidates.isEmpty()) {
            return emptyList()
        }

        val clusters =
            mutableListOf<MutableList<Float>>()

        timeCandidates
            .sorted()
            .forEach { candidateY ->

                val nearestCluster =
                    clusters.minByOrNull { cluster ->
                        abs(
                            cluster.averageY() -
                                    candidateY
                        )
                    }

                if (
                    nearestCluster != null &&
                    abs(
                        nearestCluster.averageY() -
                                candidateY
                    ) <= rowTolerance
                ) {
                    nearestCluster.add(candidateY)
                } else {
                    clusters.add(
                        mutableListOf(candidateY)
                    )
                }
            }

        val rowCenters =
            clusters
                .map { cluster ->
                    cluster.averageY()
                }
                .sorted()

        if (rowCenters.isEmpty()) {
            return emptyList()
        }

        val averageDistance =
            rowCenters
                .zipWithNext { first, second ->
                    second - first
                }
                .takeIf { distances ->
                    distances.isNotEmpty()
                }
                ?.average()
                ?.toFloat()
                ?: 0.10f

        return rowCenters.mapIndexed {
                index,
                centerY ->

            val top = if (index == 0) {
                contentTop
            } else {
                (
                        rowCenters[index - 1] +
                                centerY
                        ) / 2f
            }

            val bottom =
                if (index == rowCenters.lastIndex) {
                    (
                            centerY +
                                    averageDistance / 2f
                            ).coerceAtMost(1f)
                } else {
                    (
                            centerY +
                                    rowCenters[index + 1]
                            ) / 2f
                }

            RowBand(
                centerY = centerY,
                top = top,
                bottom = bottom
            )
        }
    }

    private fun parseRow(
        result: ScheduleOcrResult,
        rowBand: RowBand,
        dayColumns: List<DetectedDayColumn>,
        metadataColumns: MetadataColumns
    ): ImportedClass? {
        val subjectLines =
            metadataColumns.subject
                ?.let { bounds ->
                    linesInside(
                        result = result,
                        bounds = bounds,
                        rowBand = rowBand
                    )
                }
                .orEmpty()
                .filterNot { text ->
                    text.normalizeForOcrMatch() in
                            setOf(
                                "totalcreditos",
                                "cerrar"
                            )
                }

        val parsedSubject =
            parseSubjectData(subjectLines)

        val group =
            metadataColumns.group
                ?.let { bounds ->
                    firstTextInside(
                        result = result,
                        bounds = bounds,
                        rowBand = rowBand
                    )
                }
                .orEmpty()

        val credits =
            metadataColumns.credits
                ?.let { bounds ->
                    firstTextInside(
                        result = result,
                        bounds = bounds,
                        rowBand = rowBand
                    )
                }
                .orEmpty()

        val rawBlocks =
            dayColumns.mapNotNull { column ->
                parseDayCell(
                    result = result,
                    rowBand = rowBand,
                    column = column
                )
            }

        val mergedBlocks =
            mergeScheduleBlocks(rawBlocks)

        if (
            parsedSubject.subjectName.isBlank() &&
            mergedBlocks.isEmpty()
        ) {
            return null
        }

        val warnings = buildList {
            if (parsedSubject.subjectName.isBlank()) {
                add(
                    "No se detectó el nombre de la materia."
                )
            }

            if (mergedBlocks.isEmpty()) {
                add(
                    "No se detectaron horarios para esta fila."
                )
            }
        }

        return ImportedClass(
            subjectName =
                parsedSubject.subjectName.ifBlank {
                    "Materia sin nombre"
                },
            subjectCode =
                parsedSubject.subjectCode,
            teacher =
                parsedSubject.teacher,
            group = group,
            credits = credits,
            scheduleBlocks = mergedBlocks,
            warnings = warnings
        )
    }

    private fun parseDayCell(
        result: ScheduleOcrResult,
        rowBand: RowBand,
        column: DetectedDayColumn
    ): ScheduleBlock? {
        val cellTexts =
            spatialTextsInside(
                result = result,
                left = column.leftBoundary,
                right = column.rightBoundary,
                rowBand = rowBand
            )

        val timeText =
            cellTexts.firstOrNull { spatialText ->
                parseTimeRange(
                    spatialText.text
                ) != null
            } ?: return null

        val parsedTime =
            parseTimeRange(timeText.text)
                ?: return null

        val room =
            cellTexts
                .asSequence()
                .filter { spatialText ->
                    spatialText !== timeText
                }
                .filter { spatialText ->
                    parseTimeRange(
                        spatialText.text
                    ) == null
                }
                .map { spatialText ->
                    spatialText.text
                        .trim()
                        .replace(" ", "")
                }
                .filter { text ->
                    looksLikeRoom(text)
                }
                .minByOrNull { text ->
                    text.length
                }
                .orEmpty()

        return ScheduleBlock(
            days = setOf(column.day),
            startTime = parsedTime.startTime,
            endTime = parsedTime.endTime,
            room = room
        )
    }

    private fun mergeScheduleBlocks(
        blocks: List<ScheduleBlock>
    ): List<ScheduleBlock> {
        return blocks
            .groupBy { block ->
                BlockKey(
                    startTime = block.startTime,
                    endTime = block.endTime,
                    room = block.room
                )
            }
            .map { (key, groupedBlocks) ->
                ScheduleBlock(
                    days = groupedBlocks
                        .flatMap { block ->
                            block.days
                        }
                        .toSet(),
                    startTime = key.startTime,
                    endTime = key.endTime,
                    room = key.room
                )
            }
            .sortedWith(
                compareBy<ScheduleBlock> { block ->
                    block.startTime
                }.thenBy { block ->
                    block.endTime
                }
            )
    }

    private fun parseSubjectData(
        lines: List<String>
    ): ParsedSubject {
        if (lines.isEmpty()) {
            return ParsedSubject()
        }

        val cleanedLines =
            lines.map { line ->
                line.trim()
            }.filter { line ->
                line.isNotBlank()
            }

        val codeIndex =
            cleanedLines.indexOfFirst { line ->
                looksLikeSubjectCode(line)
            }

        val subjectCode =
            if (codeIndex >= 0) {
                cleanedLines[codeIndex]
                    .replace(" ", "")
            } else {
                ""
            }

        val contentLines =
            cleanedLines.filterIndexed {
                    index,
                    _ ->
                index != codeIndex
            }

        if (contentLines.isEmpty()) {
            return ParsedSubject(
                subjectCode = subjectCode
            )
        }

        if (contentLines.size == 1) {
            return ParsedSubject(
                subjectCode = subjectCode,
                subjectName = contentLines.first()
            )
        }

        /*
         * En las tablas académicas normalmente:
         *
         * código
         * nombre de materia
         * profesor
         *
         * Tomamos la última línea como profesor y
         * las anteriores como nombre de materia.
         */
        return ParsedSubject(
            subjectCode = subjectCode,
            subjectName = contentLines
                .dropLast(1)
                .joinToString(" "),
            teacher = contentLines.last()
        )
    }

    private fun linesInside(
        result: ScheduleOcrResult,
        bounds: ColumnBounds,
        rowBand: RowBand
    ): List<String> {
        return result.lines
            .filter { line ->
                val normalizedX =
                    line.centerX.toFloat() /
                            result.imageWidth

                val normalizedY =
                    line.centerY.toFloat() /
                            result.imageHeight

                normalizedX >= bounds.left &&
                        normalizedX <= bounds.right &&
                        normalizedY >= rowBand.top &&
                        normalizedY < rowBand.bottom
            }
            .sortedBy { line ->
                line.top
            }
            .map { line ->
                line.text
            }
    }

    private fun firstTextInside(
        result: ScheduleOcrResult,
        bounds: ColumnBounds,
        rowBand: RowBand
    ): String {
        val lineText =
            result.lines
                .filter { line ->
                    val normalizedX =
                        line.centerX.toFloat() /
                                result.imageWidth

                    val normalizedY =
                        line.centerY.toFloat() /
                                result.imageHeight

                    normalizedX >= bounds.left &&
                            normalizedX <= bounds.right &&
                            normalizedY >= rowBand.top &&
                            normalizedY < rowBand.bottom
                }
                .sortedBy { line ->
                    line.top
                }
                .firstOrNull()
                ?.text

        if (!lineText.isNullOrBlank()) {
            return lineText.trim()
        }

        return result.elements
            .filter { element ->
                val normalizedX =
                    element.centerX.toFloat() /
                            result.imageWidth

                val normalizedY =
                    element.centerY.toFloat() /
                            result.imageHeight

                normalizedX >= bounds.left &&
                        normalizedX <= bounds.right &&
                        normalizedY >= rowBand.top &&
                        normalizedY < rowBand.bottom
            }
            .sortedBy { element ->
                element.top
            }
            .firstOrNull()
            ?.text
            ?.trim()
            .orEmpty()
    }

    private fun spatialTextsInside(
        result: ScheduleOcrResult,
        left: Float,
        right: Float,
        rowBand: RowBand
    ): List<SpatialText> {
        val lineTexts =
            result.lines.mapNotNull { line ->
                val normalizedX =
                    line.centerX.toFloat() /
                            result.imageWidth

                val normalizedY =
                    line.centerY.toFloat() /
                            result.imageHeight

                if (
                    normalizedX >= left &&
                    normalizedX <= right &&
                    normalizedY >= rowBand.top &&
                    normalizedY < rowBand.bottom
                ) {
                    SpatialText(
                        text = line.text.trim(),
                        centerY = normalizedY
                    )
                } else {
                    null
                }
            }

        val elementTexts =
            result.elements.mapNotNull { element ->
                val normalizedX =
                    element.centerX.toFloat() /
                            result.imageWidth

                val normalizedY =
                    element.centerY.toFloat() /
                            result.imageHeight

                if (
                    normalizedX >= left &&
                    normalizedX <= right &&
                    normalizedY >= rowBand.top &&
                    normalizedY < rowBand.bottom
                ) {
                    SpatialText(
                        text = element.text.trim(),
                        centerY = normalizedY
                    )
                } else {
                    null
                }
            }

        return (lineTexts + elementTexts)
            .filter { spatialText ->
                spatialText.text.isNotBlank()
            }
            .distinctBy { spatialText ->
                spatialText.text to
                        (spatialText.centerY * 1000)
                            .toInt()
            }
            .sortedBy { spatialText ->
                spatialText.centerY
            }
    }

    private fun findHeaderCenter(
        result: ScheduleOcrResult,
        aliases: Set<String>,
        contentTop: Float
    ): Float? {
        val headerTop =
            (contentTop - 0.15f)
                .coerceAtLeast(0f)

        return result.elements
            .filter { element ->
                val normalizedY =
                    element.centerY.toFloat() /
                            result.imageHeight

                normalizedY >= headerTop &&
                        normalizedY <= contentTop
            }
            .firstOrNull { element ->
                element.text
                    .normalizeForOcrMatch() in aliases
            }
            ?.centerX
            ?.toFloat()
            ?.div(result.imageWidth)
    }

    private fun parseTimeRange(
        text: String
    ): ParsedTimeRange? {
        val match = TIME_RANGE_REGEX
            .find(text)
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

        val startTime =
            "%02d:%02d".format(
                startHour,
                startMinute
            )

        val endTime =
            "%02d:%02d".format(
                endHour,
                endMinute
            )

        return ParsedTimeRange(
            startTime = startTime,
            endTime = endTime
        )
    }

    private fun looksLikeSubjectCode(
        text: String
    ): Boolean {
        val compact =
            text.trim()
                .replace(" ", "")

        return SUBJECT_CODE_REGEX
            .matches(compact)
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

        if (compact.contains(":")) {
            return false
        }

        return compact.any { character ->
            character.isLetter()
        } && compact.all { character ->
            character.isLetterOrDigit() ||
                    character == '-' ||
                    character == '_'
        }
    }

    private fun List<Float>.averageY(): Float {
        if (isEmpty()) {
            return 0f
        }

        return average().toFloat()
    }

    private data class RowBand(
        val centerY: Float,
        val top: Float,
        val bottom: Float
    )

    private data class ColumnBounds(
        val left: Float,
        val right: Float
    )

    private data class MetadataColumns(
        val subject: ColumnBounds?,
        val group: ColumnBounds?,
        val credits: ColumnBounds?
    )

    private enum class MetadataType {
        SUBJECT,
        GROUP,
        CREDITS
    }

    private data class MetadataHeader(
        val type: MetadataType,
        val centerX: Float
    )

    private data class ParsedSubject(
        val subjectCode: String = "",
        val subjectName: String = "",
        val teacher: String = ""
    )

    private data class ParsedTimeRange(
        val startTime: String,
        val endTime: String
    )

    private data class SpatialText(
        val text: String,
        val centerY: Float
    )

    private data class BlockKey(
        val startTime: String,
        val endTime: String,
        val room: String
    )

    companion object {
        private val TIME_RANGE_REGEX = Regex(
            """(\d{1,2})\s*:\s*(\d{2})\s*[-–—]+\s*(\d{1,2})\s*:\s*(\d{2})"""
        )

        private val SUBJECT_CODE_REGEX = Regex(
            """^[A-Z]{2,6}[A-Z0-9-]*\d{3,6}[A-Z0-9-]*$""",
            option = RegexOption.IGNORE_CASE
        )
    }
}