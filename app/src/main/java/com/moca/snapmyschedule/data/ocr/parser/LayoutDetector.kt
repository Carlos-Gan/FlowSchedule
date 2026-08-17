package com.moca.snapmyschedule.data.ocr.parser

import com.moca.snapmyschedule.data.ocr.ScheduleOcrResult
import kotlin.math.abs

enum class ScheduleLayoutType {
    GRID,
    TABLE,
    UNKNOWN
}

data class LayoutDetectionResult(
    val type: ScheduleLayoutType,
    val confidence: Float,
    val detectedDays: Int,
    val evidence: List<String>
)

class LayoutDetector(
    private val rowTolerance: Float = 0.04f
) {

    fun detect(
        result: ScheduleOcrResult
    ): LayoutDetectionResult {

        if (
            result.imageWidth <= 0 ||
            result.imageHeight <= 0
        ) {
            return unknownResult(
                reason = "La imagen no tiene dimensiones válidas."
            )
        }

        /*
         * Usamos elementos individuales cuando existen.
         * Como respaldo, usamos líneas completas.
         */
        val tokens = if (result.elements.isNotEmpty()) {
            result.elements.map { element ->
                LayoutToken(
                    text = element.text,
                    centerX = element.centerX.toFloat() /
                            result.imageWidth.toFloat(),
                    centerY = element.centerY.toFloat() /
                            result.imageHeight.toFloat()
                )
            }
        } else {
            result.lines.map { line ->
                LayoutToken(
                    text = line.text,
                    centerX = line.centerX.toFloat() /
                            result.imageWidth.toFloat(),
                    centerY = line.centerY.toFloat() /
                            result.imageHeight.toFloat()
                )
            }
        }

        if (tokens.isEmpty()) {
            return unknownResult(
                reason = "El OCR no devolvió elementos con posición."
            )
        }

        val rows = groupIntoRows(tokens)

        /*
         * Buscamos una fila que contenga varios días.
         *
         * Ejemplos:
         *
         * Hora | Lunes | Martes | Miércoles
         *
         * Materia | Gpo | Cr | Lunes | Martes
         */
        val headerCandidates = rows
            .map { row ->
                buildHeaderCandidate(row)
            }
            .filter { candidate ->
                candidate.daysCount >= 2
            }

        val header = headerCandidates
            .maxWithOrNull(
                compareBy<HeaderCandidate> {
                    it.daysCount
                }.thenBy {
                    it.supportingHeaderWords
                }.thenBy {
                    -it.centerY
                }
            )
            ?: return unknownResult(
                reason = "No se encontró una fila con varios días."
            )

        val firstDayX = header.tokens
            .filter { token ->
                token.text.toWeekDayOrNull() != null
            }
            .minOfOrNull { token ->
                token.centerX
            } ?: 1f

        val standaloneHours = tokens.count { token ->
            token.centerY > header.centerY &&
                    token.centerX < firstDayX &&
                    token.text.isStandaloneTime()
        }

        val tableIndicators = listOf(
            header.hasSubjectHeader,
            header.hasGroupHeader,
            header.hasCreditsHeader
        ).count { it }

        val evidence = mutableListOf<String>()

        evidence.add(
            "Se detectaron ${header.daysCount} días en la fila de encabezados."
        )

        if (header.hasHourHeader) {
            evidence.add(
                "Se encontró una columna llamada Hora."
            )
        }

        if (header.hasSubjectHeader) {
            evidence.add(
                "Se encontró la columna Materia."
            )
        }

        if (header.hasGroupHeader) {
            evidence.add(
                "Se encontró una columna de grupo."
            )
        }

        if (header.hasCreditsHeader) {
            evidence.add(
                "Se encontró una columna de créditos."
            )
        }

        if (standaloneHours > 0) {
            evidence.add(
                "Se detectaron $standaloneHours horas independientes " +
                        "a la izquierda de los días."
            )
        }

        /*
         * Tabla académica:
         *
         * Materia | Gpo | Cr | Lunes | Martes...
         */
        if (
            header.daysCount >= 2 &&
            tableIndicators >= 2
        ) {
            val confidence = (
                    0.70f +
                            header.daysCount.coerceAtMost(6) * 0.03f +
                            tableIndicators * 0.03f
                    ).coerceAtMost(0.98f)

            return LayoutDetectionResult(
                type = ScheduleLayoutType.TABLE,
                confidence = confidence,
                detectedDays = header.daysCount,
                evidence = evidence
            )
        }

        /*
         * Cuadrícula visual:
         *
         * Hora | Lunes | Martes...
         *
         * 08:00 | tarjetas de clases
         * 09:00 | tarjetas de clases
         */
        if (
            header.daysCount >= 2 &&
            (
                    header.hasHourHeader ||
                            standaloneHours >= 3
                    )
        ) {
            val confidence = (
                    0.70f +
                            header.daysCount.coerceAtMost(6) * 0.03f +
                            standaloneHours.coerceAtMost(8) * 0.015f
                    ).coerceAtMost(0.98f)

            return LayoutDetectionResult(
                type = ScheduleLayoutType.GRID,
                confidence = confidence,
                detectedDays = header.daysCount,
                evidence = evidence
            )
        }

        /*
         * Algunas tablas pueden perder Gpo o Cr durante OCR,
         * pero conservar Materia y muchos días.
         */
        if (
            header.daysCount >= 4 &&
            tableIndicators >= 1
        ) {
            evidence.add(
                "La tabla parece incompleta, pero conserva " +
                        "una columna académica y varios días."
            )

            return LayoutDetectionResult(
                type = ScheduleLayoutType.TABLE,
                confidence = 0.66f,
                detectedDays = header.daysCount,
                evidence = evidence
            )
        }

        evidence.add(
            "No hubo suficientes señales para determinar la estructura."
        )

        return LayoutDetectionResult(
            type = ScheduleLayoutType.UNKNOWN,
            confidence = 0.30f,
            detectedDays = header.daysCount,
            evidence = evidence
        )
    }

    private fun buildHeaderCandidate(
        tokens: List<LayoutToken>
    ): HeaderCandidate {

        val normalizedTexts = tokens.map { token ->
            token.text.normalizeForOcrMatch()
        }

        val daysCount = tokens
            .mapNotNull { token ->
                token.text.toWeekDayOrNull()
            }
            .distinct()
            .size

        val hasSubjectHeader =
            normalizedTexts.any { normalized ->
                normalized in setOf(
                    "materia",
                    "asignatura",
                    "subject",
                    "curso",
                    "course"
                )
            }

        val hasGroupHeader =
            normalizedTexts.any { normalized ->
                normalized in setOf(
                    "gpo",
                    "grupo",
                    "group"
                )
            }

        val hasCreditsHeader =
            normalizedTexts.any { normalized ->
                normalized in setOf(
                    "cr",
                    "creditos",
                    "credito",
                    "credits",
                    "credit"
                )
            }

        val hasHourHeader =
            normalizedTexts.any { normalized ->
                normalized in setOf(
                    "hora",
                    "horario",
                    "time"
                )
            }

        val supportingHeaderWords = listOf(
            hasSubjectHeader,
            hasGroupHeader,
            hasCreditsHeader,
            hasHourHeader
        ).count { it }

        return HeaderCandidate(
            tokens = tokens,
            daysCount = daysCount,
            hasSubjectHeader = hasSubjectHeader,
            hasGroupHeader = hasGroupHeader,
            hasCreditsHeader = hasCreditsHeader,
            hasHourHeader = hasHourHeader,
            supportingHeaderWords = supportingHeaderWords,
            centerY = tokens
                .map { token -> token.centerY }
                .average()
                .toFloat()
        )
    }

    private fun groupIntoRows(
        tokens: List<LayoutToken>
    ): List<List<LayoutToken>> {

        val rows =
            mutableListOf<MutableList<LayoutToken>>()

        tokens
            .sortedBy { token ->
                token.centerY
            }
            .forEach { token ->

                val nearestRow = rows.minByOrNull { row ->
                    abs(
                        row.averageCenterY() -
                                token.centerY
                    )
                }

                if (
                    nearestRow != null &&
                    abs(
                        nearestRow.averageCenterY() -
                                token.centerY
                    ) <= rowTolerance
                ) {
                    nearestRow.add(token)
                } else {
                    rows.add(
                        mutableListOf(token)
                    )
                }
            }

        return rows.map { row ->
            row.sortedBy { token ->
                token.centerX
            }
        }
    }

    private fun List<LayoutToken>.averageCenterY(): Float {
        if (isEmpty()) {
            return 0f
        }

        return map { token ->
            token.centerY
        }.average().toFloat()
    }

    private fun String.isStandaloneTime(): Boolean {
        return Regex(
            pattern = """^\s*\d{1,2}\s*:\s*\d{2}\s*$"""
        ).matches(this)
    }

    private fun unknownResult(
        reason: String
    ): LayoutDetectionResult {
        return LayoutDetectionResult(
            type = ScheduleLayoutType.UNKNOWN,
            confidence = 0f,
            detectedDays = 0,
            evidence = listOf(reason)
        )
    }

    private data class LayoutToken(
        val text: String,
        val centerX: Float,
        val centerY: Float
    )

    private data class HeaderCandidate(
        val tokens: List<LayoutToken>,
        val daysCount: Int,
        val hasSubjectHeader: Boolean,
        val hasGroupHeader: Boolean,
        val hasCreditsHeader: Boolean,
        val hasHourHeader: Boolean,
        val supportingHeaderWords: Int,
        val centerY: Float
    )
}