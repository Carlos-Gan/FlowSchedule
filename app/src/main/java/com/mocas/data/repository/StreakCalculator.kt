package com.mocas.data.repository

import com.mocas.data.local.SchoolEventEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object StreakCalculator {

    /**
     * Calcula la racha actual de días productivos.
     * La racha se pierde si hay alguna actividad vencida (fecha pasada y no completada).
     * Los días sin actividades no rompen la racha.
     */
    fun calculateCurrentStreak(events: List<SchoolEventEntity>): Int {
        val today = LocalDate.now()

        // 1. Condición de pérdida: Si hay alguna actividad vencida, la racha es 0.
        // Se considera vencida si la fecha de fin es anterior a hoy y no está completada.
        val hasOverdue = events.any { event ->
            if (event.isCompleted || event.isDeleted) return@any false
            try {
                val endDate = LocalDate.parse(event.endDate)
                endDate.isBefore(today)
            } catch (e: Exception) {
                false
            }
        }

        if (hasOverdue) return 0

        // 2. La racha es el conteo de días únicos en los que se ha completado al menos una actividad.
        // Como no hay nada vencido, este número representa tu historial de cumplimiento.
        val completedDaysCount = events
            .filter { it.isCompleted && !it.isDeleted && it.completedAtMillis != null }
            .map { 
                Instant.ofEpochMilli(it.completedAtMillis!!)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate() 
            }
            .distinct()
            .size

        return completedDaysCount
    }
}
