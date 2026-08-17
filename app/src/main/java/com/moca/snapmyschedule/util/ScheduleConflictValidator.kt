package com.moca.snapmyschedule.util

import com.moca.snapmyschedule.data.model.ClassFormData
import com.moca.snapmyschedule.data.model.ClassSession

fun findScheduleConflict(
    formData: ClassFormData,
    existingSessions: List<ClassSession>,
    excludedCourseId: String? = null
): String? {

    /*
     * 1. Comprueba conflictos entre los bloques
     * del mismo formulario.
     */
    val blocks = formData.scheduleBlocks

    for (firstIndex in blocks.indices) {
        for (secondIndex in firstIndex + 1 until blocks.size) {
            val firstBlock = blocks[firstIndex]
            val secondBlock = blocks[secondIndex]

            val sharedDays = firstBlock.days
                .intersect(secondBlock.days)
                .sortedBy { day -> day.ordinal }

            val conflictingDay = sharedDays.firstOrNull { day ->
                timesOverlap(
                    firstStart = firstBlock.startTime,
                    firstEnd = firstBlock.endTime,
                    secondStart = secondBlock.startTime,
                    secondEnd = secondBlock.endTime
                )
            }

            if (conflictingDay != null) {
                return buildString {
                    append("Dos bloques de esta materia se cruzan el ")
                    append(conflictingDay.displayName)
                    append(": ")
                    append(firstBlock.startTime)
                    append("–")
                    append(firstBlock.endTime)
                    append(" y ")
                    append(secondBlock.startTime)
                    append("–")
                    append(secondBlock.endTime)
                    append(".")
                }
            }
        }
    }

    /*
     * 2. Comprueba conflictos contra las materias
     * que ya están guardadas.
     */
    val sessionsToCheck = existingSessions.filter { session ->
        excludedCourseId.isNullOrBlank() ||
                session.courseId != excludedCourseId
    }

    blocks.forEach { block ->
        block.days
            .sortedBy { day -> day.ordinal }
            .forEach { day ->

                val conflictingSession =
                    sessionsToCheck.firstOrNull { existingSession ->

                        existingSession.day == day &&
                                timesOverlap(
                                    firstStart = block.startTime,
                                    firstEnd = block.endTime,
                                    secondStart =
                                        existingSession.startTime,
                                    secondEnd =
                                        existingSession.endTime
                                )
                    }

                if (conflictingSession != null) {
                    return buildString {
                        append("El horario ")
                        append(block.startTime)
                        append("–")
                        append(block.endTime)
                        append(" del ")
                        append(day.displayName)
                        append(" se cruza con ")
                        append(conflictingSession.subjectName)
                        append(" (")
                        append(conflictingSession.startTime)
                        append("–")
                        append(conflictingSession.endTime)
                        append(").")
                    }
                }
            }
    }

    return null
}

private fun timesOverlap(
    firstStart: String,
    firstEnd: String,
    secondStart: String,
    secondEnd: String
): Boolean {
    val firstStartMinutes =
        firstStart.toMinutesOrNull() ?: return false

    val firstEndMinutes =
        firstEnd.toMinutesOrNull() ?: return false

    val secondStartMinutes =
        secondStart.toMinutesOrNull() ?: return false

    val secondEndMinutes =
        secondEnd.toMinutesOrNull() ?: return false

    /*
     * Ejemplo de conflicto:
     *
     * Primera:  08:00 ───── 10:00
     * Segunda:        09:00 ───── 11:00
     *
     * Horarios consecutivos no chocan:
     *
     * 08:00–09:00
     * 09:00–10:00
     */
    return firstStartMinutes < secondEndMinutes &&
            secondStartMinutes < firstEndMinutes
}

private fun String.toMinutesOrNull(): Int? {
    val parts = split(":")

    if (parts.size != 2) {
        return null
    }

    val hour = parts[0].toIntOrNull()
        ?: return null

    val minute = parts[1].toIntOrNull()
        ?: return null

    if (hour !in 0..23 || minute !in 0..59) {
        return null
    }

    return hour * 60 + minute
}