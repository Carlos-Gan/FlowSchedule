package com.mocas.util

import com.mocas.data.local.AcademicPeriodEntity
import com.mocas.data.local.GradeItemEntity
import com.mocas.data.local.SubjectEntity
import com.mocas.data.repository.GradeCalculator
import com.mocas.ui.screens.PeriodSummary
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

enum class PeriodPhase { ACTIVE, UPCOMING, PAST }


fun summarizePeriod(period: AcademicPeriodEntity, today: LocalDate): PeriodSummary? {
    val start = runCatching { LocalDate.parse(period.startDate) }.getOrNull() ?: return null
    val end = runCatching { LocalDate.parse(period.endDate) }.getOrNull() ?: return null
    val phase = when {
        today.isBefore(start) -> PeriodPhase.UPCOMING
        today.isAfter(end) -> PeriodPhase.PAST
        else -> PeriodPhase.ACTIVE
    }
    val days = when (phase) {
        PeriodPhase.ACTIVE -> ChronoUnit.DAYS.between(today, end).toInt().coerceAtLeast(0)
        PeriodPhase.UPCOMING -> ChronoUnit.DAYS.between(today, start).toInt().coerceAtLeast(0)
        PeriodPhase.PAST -> null
    }
    val totalDays = ChronoUnit.DAYS.between(start, end).toInt().coerceAtLeast(1)
    val progress = when (phase) {
        PeriodPhase.ACTIVE -> (ChronoUnit.DAYS.between(start, today).toFloat() / totalDays).coerceIn(0f, 1f)
        PeriodPhase.UPCOMING -> 0f
        PeriodPhase.PAST -> 1f
    }
    return PeriodSummary(period, phase, days, progress)
}

fun formatGrade(value: Double): String = String.format(Locale.getDefault(), "%.2f", value).replace('.', ',')


data class AcademicGradePoint(val label: String, val value: Double)
enum class ProgressTab { CURRENT, MONTHLY }

fun monthlyTrend(
    subjects: List<SubjectEntity>, categories: List<com.mocas.data.local.GradeCategoryEntity>, items: List<GradeItemEntity>,
    units: List<com.mocas.data.local.GradeUnitEntity>, weights: List<com.mocas.data.local.GradeUnitCategoryWeightEntity>,
    subjectId: Long?, useGpa: Boolean
): List<AcademicGradePoint> {
    if (items.isEmpty()) return emptyList()
    val categorySubjects = categories.associate { it.id to it.subjectId }
    val relevant = items.filter { item -> categorySubjects[item.categoryId]?.let { subjectId == null || it == subjectId } == true }
    if (relevant.isEmpty()) return emptyList()
    val zone = ZoneId.systemDefault()
    val months = relevant.map { YearMonth.from(Instant.ofEpochMilli(it.createdAtMillis).atZone(zone)) }.distinct().sorted()
    return months.mapNotNull { month ->
        val cutoff = month.atEndOfMonth().atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()
        val asOf = relevant.filter { it.createdAtMillis <= cutoff }
        val scoped = subjects.filter { subject -> subjectId == null || subject.id == subjectId }
        val avg = scoped.mapNotNull { subject -> GradeCalculator.subjectAverage(subject.id, categories, asOf, units, weights) }
            .takeIf { it.isNotEmpty() }?.average()
        avg?.let { AcademicGradePoint(month.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.getDefault())), toDisplayGrade(it, useGpa)) }
    }
}

fun toDisplayGrade(value: Double, useGpa: Boolean): Double = if (useGpa) (value / if (value > 10) 100.0 else 10.0) * 4.0 else value