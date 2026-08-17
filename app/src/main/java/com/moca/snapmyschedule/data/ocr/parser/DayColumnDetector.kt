package com.moca.snapmyschedule.data.ocr.parser

import com.moca.snapmyschedule.data.model.WeekDay
import com.moca.snapmyschedule.data.ocr.ScheduleOcrResult
import com.moca.snapmyschedule.data.ocr.model.DetectedDayColumn
import kotlin.math.abs

class DayColumnDetector(
    private val rowTolerance: Float = 0.07f
) {

    fun detect(
        result: ScheduleOcrResult
    ): List<DetectedDayColumn> {

        if (
            result.imageWidth <= 0 ||
            result.imageHeight <= 0
        ) {
            return emptyList()
        }

        /*
         * Buscamos encabezados tanto entre elementos
         * individuales como entre líneas completas.
         */
        val candidates = buildList {

            result.elements.forEach { element ->
                val day =
                    element.text.toWeekDayOrNull()
                        ?: return@forEach

                add(
                    DayHeaderCandidate(
                        day = day,
                        text = element.text,
                        centerX =
                            element.centerX.toFloat() /
                                    result.imageWidth,
                        centerY =
                            element.centerY.toFloat() /
                                    result.imageHeight,
                        bottomY =
                            element.bottom.toFloat() /
                                    result.imageHeight,
                        width =
                            element.width.toFloat() /
                                    result.imageWidth
                    )
                )
            }

            result.lines.forEach { line ->
                val day =
                    line.text.toWeekDayOrNull()
                        ?: return@forEach

                add(
                    DayHeaderCandidate(
                        day = day,
                        text = line.text,
                        centerX =
                            line.centerX.toFloat() /
                                    result.imageWidth,
                        centerY =
                            line.centerY.toFloat() /
                                    result.imageHeight,
                        bottomY =
                            line.bottom.toFloat() /
                                    result.imageHeight,
                        width =
                            line.width.toFloat() /
                                    result.imageWidth
                    )
                )
            }
        }

        if (candidates.isEmpty()) {
            return emptyList()
        }

        /*
         * Agrupamos candidatos que están aproximadamente
         * a la misma altura.
         *
         * Esto permite encontrar la fila:
         *
         * Lunes | Martes | Miércoles | Jueves | Viernes
         */
        val candidateRows =
            mutableListOf<
                    MutableList<DayHeaderCandidate>
                    >()

        candidates
            .sortedBy { candidate ->
                candidate.centerY
            }
            .forEach { candidate ->

                val nearestRow =
                    candidateRows.minByOrNull { row ->
                        abs(
                            row.averageCenterY() -
                                    candidate.centerY
                        )
                    }

                if (
                    nearestRow != null &&
                    abs(
                        nearestRow.averageCenterY() -
                                candidate.centerY
                    ) <= rowTolerance
                ) {
                    nearestRow.add(candidate)
                } else {
                    candidateRows.add(
                        mutableListOf(candidate)
                    )
                }
            }

        /*
         * Si un día apareció como línea y también como
         * elemento, nos quedamos con el candidato
         * de menor anchura.
         */
        val uniqueRows =
            candidateRows.map { row ->
                row
                    .groupBy { candidate ->
                        candidate.day
                    }
                    .map { (_, repeatedCandidates) ->
                        repeatedCandidates.minBy {
                                candidate ->
                            candidate.width
                        }
                    }
            }

        /*
         * Seleccionamos la fila que contenga la mayor
         * cantidad de días distintos.
         */
        val detectedHeaderRow =
            uniqueRows.maxByOrNull { row ->
                val averageY =
                    row.map { candidate ->
                        candidate.centerY
                    }.average()

                /*
                 * La cantidad de días tiene mucho más
                 * peso que la posición vertical.
                 */
                row.size * 1000.0 - averageY
            } ?: return emptyList()

        val sortedHeaders =
            detectedHeaderRow.sortedBy { header ->
                header.centerX
            }

        if (sortedHeaders.isEmpty()) {
            return emptyList()
        }

        val contentTop =
            (
                    sortedHeaders.maxOf { header ->
                        header.bottomY
                    } + 0.005f
                    ).coerceIn(
                    minimumValue = 0f,
                    maximumValue = 1f
                )

        return sortedHeaders.mapIndexed {
                index,
                currentHeader ->

            val leftBoundary =
                calculateLeftBoundary(
                    headers = sortedHeaders,
                    index = index
                )

            val rightBoundary =
                calculateRightBoundary(
                    headers = sortedHeaders,
                    index = index
                )

            DetectedDayColumn(
                day = currentHeader.day,
                headerText =
                    currentHeader.text,
                leftBoundary =
                    leftBoundary,
                rightBoundary =
                    rightBoundary,
                contentTop =
                    contentTop
            )
        }
    }

    private fun calculateLeftBoundary(
        headers: List<DayHeaderCandidate>,
        index: Int
    ): Float {
        val current =
            headers[index]

        if (index > 0) {
            val previous =
                headers[index - 1]

            return (
                    previous.centerX +
                            current.centerX
                    ) / 2f
        }

        if (headers.size == 1) {
            return (
                    current.centerX - 0.25f
                    ).coerceAtLeast(0f)
        }

        val next =
            headers[index + 1]

        val estimatedSpacing =
            next.centerX -
                    current.centerX

        return (
                current.centerX -
                        estimatedSpacing / 2f
                ).coerceAtLeast(0f)
    }

    private fun calculateRightBoundary(
        headers: List<DayHeaderCandidate>,
        index: Int
    ): Float {
        val current =
            headers[index]

        if (index < headers.lastIndex) {
            val next =
                headers[index + 1]

            return (
                    current.centerX +
                            next.centerX
                    ) / 2f
        }

        if (headers.size == 1) {
            return (
                    current.centerX + 0.25f
                    ).coerceAtMost(1f)
        }

        val previous =
            headers[index - 1]

        val estimatedSpacing =
            current.centerX -
                    previous.centerX

        return (
                current.centerX +
                        estimatedSpacing / 2f
                ).coerceAtMost(1f)
    }

    private fun List<DayHeaderCandidate>
            .averageCenterY(): Float {

        if (isEmpty()) {
            return 0f
        }

        return map { candidate ->
            candidate.centerY
        }.average().toFloat()
    }

    private data class DayHeaderCandidate(
        val day: WeekDay,
        val text: String,
        val centerX: Float,
        val centerY: Float,
        val bottomY: Float,
        val width: Float
    )
}