package com.mocas.data.repository

import com.mocas.data.local.RecurrenceType
import com.mocas.data.local.SchoolEventEntity
import com.mocas.util.DateTimeUtils
import java.time.temporal.ChronoUnit
import java.util.UUID

object EventRecurrenceGenerator {
    fun generate(event: SchoolEventEntity, limit: Int = 200): List<SchoolEventEntity> {
        if (event.recurrenceType == RecurrenceType.NONE) {
            return listOf(event.copy(recurrenceEndDate = null, recurrenceGroupId = null))
        }
        val start = requireNotNull(DateTimeUtils.parseDate(event.startDate)) { "Fecha inicial inválida." }
        val end = requireNotNull(DateTimeUtils.parseDate(event.endDate)) { "Fecha final inválida." }
        val recurrenceEnd = requireNotNull(event.recurrenceEndDate?.let(DateTimeUtils::parseDate)) {
            "Selecciona hasta qué fecha se repetirá la actividad."
        }
        require(!recurrenceEnd.isBefore(start)) { "El fin de la repetición no puede ser anterior al inicio." }
        val durationDays = ChronoUnit.DAYS.between(start, end)
        val groupId = event.recurrenceGroupId ?: UUID.randomUUID().toString()
        val interval = event.recurrenceInterval.coerceAtLeast(1).toLong()
        val dates = generateSequence(start) { current ->
            when (event.recurrenceType) {
                RecurrenceType.WEEKLY -> current.plusWeeks(interval)
                RecurrenceType.MONTHLY -> current.plusMonths(interval)
                RecurrenceType.NONE -> null
            }
        }.takeWhile { !it.isAfter(recurrenceEnd) }.take(limit + 1).toList()
        require(dates.size <= limit) {
            "La serie supera el límite de $limit actividades. Reduce la fecha final."
        }
        return dates.map { date ->
            event.copy(
                id = 0,
                startDate = date.toString(),
                endDate = date.plusDays(durationDays).toString(),
                recurrenceGroupId = groupId
            )
        }
    }
}
