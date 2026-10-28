package com.mocas.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mocas.R
import com.mocas.data.local.AcademicPeriodEntity
import com.mocas.data.local.GradeItemEntity
import com.mocas.data.local.SubjectEntity
import com.mocas.data.repository.GradeCalculator
import com.mocas.ui.viewmodel.ScheduleViewModel
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

private data class AcademicGradePoint(val label: String, val value: Double)
private enum class ProgressTab { CURRENT, MONTHLY }

@Composable
fun AcademicProgressScreen(viewModel: ScheduleViewModel, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    val subjectsWithSlots by viewModel.subjectsWithSlots.collectAsStateWithLifecycle()
    val periods by viewModel.academicPeriods.collectAsStateWithLifecycle()
    val categories by viewModel.gradeCategories.collectAsStateWithLifecycle()
    val gradeItems by viewModel.gradeItems.collectAsStateWithLifecycle()
    val gradeUnits by viewModel.gradeUnits.collectAsStateWithLifecycle()
    val weights by viewModel.gradeUnitCategoryWeights.collectAsStateWithLifecycle()
    var selectedSubjectId by remember { mutableStateOf<Long?>(null) }
    var selectedTab by remember { mutableStateOf(ProgressTab.CURRENT) }

    val subjects = remember(subjectsWithSlots) { subjectsWithSlots.map { it.subject }.filterNot { it.isDeleted }.distinctBy { it.id } }
    val currentAverage = remember(subjects, categories, gradeItems, gradeUnits, weights, selectedSubjectId) {
        val scopedSubjects = subjects.filter { selectedSubjectId == null || it.id == selectedSubjectId }
        scopedSubjects.mapNotNull { subject ->
            GradeCalculator.subjectAverage(subject.id, categories, gradeItems, gradeUnits, weights)
        }.takeIf { it.isNotEmpty() }?.average()
    }
    val currentValue = currentAverage?.let { toDisplayGrade(it, settings.useGpaScale) }
    val trend = remember(subjects, categories, gradeItems, gradeUnits, weights, selectedSubjectId, settings.useGpaScale) {
        monthlyTrend(subjects, categories, gradeItems, gradeUnits, weights, selectedSubjectId, settings.useGpaScale)
    }
    val change = if (trend.size > 1) trend.last().value - trend[trend.lastIndex - 1].value else null
    val gradeLabel = if (settings.useGpaScale) stringResource(R.string.academic_gpa) else stringResource(R.string.academic_weighted_average)

    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(horizontal = 24.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.academic_back)) }
                Text(stringResource(R.string.academic_progress_eyebrow), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.size(48.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.academic_progress_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.academic_progress_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)).padding(4.dp)) {
                listOf(ProgressTab.CURRENT to stringResource(R.string.academic_tab_current), ProgressTab.MONTHLY to stringResource(R.string.academic_tab_monthly)).forEach { (tab, label) ->
                    Surface(onClick = { selectedTab = tab }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(9.dp), color = if (selectedTab == tab) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant) {
                        Text(label, Modifier.padding(vertical = 12.dp), textAlign = TextAlign.Center, fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium, color = if (selectedTab == tab) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            if (selectedTab == ProgressTab.CURRENT) {
                item {
                    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .55f)) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(stringResource(R.string.academic_current_grade, gradeLabel), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text(currentValue?.let(::formatGrade) ?: stringResource(R.string.academic_no_data_short), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                                Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                            Text(stringResource(R.string.academic_current_description), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                item { SubjectPicker(subjects, selectedSubjectId, onSelect = { selectedSubjectId = it }) }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(stringResource(R.string.academic_period_history), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        val points = remember(subjects, periods, categories, gradeItems, gradeUnits, weights, selectedSubjectId, settings.useGpaScale) {
                            periodPoints(subjects, periods, categories, gradeItems, gradeUnits, weights, selectedSubjectId, settings.useGpaScale)
                        }
                        if (points.isEmpty()) EmptyCard(stringResource(R.string.academic_period_empty))
                        else TrendCard(points, gradeLabel, settings.useGpaScale)
                    }
                }
            } else {
                item { SubjectPicker(subjects, selectedSubjectId, onSelect = { selectedSubjectId = it }) }
                item {
                    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .55f)) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(stringResource(R.string.academic_monthly_change_title), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            Text(change?.let { stringResource(if (it >= 0) R.string.academic_change_up else R.string.academic_change_down, formatGrade(abs(it))) }
                                ?: stringResource(R.string.academic_monthly_change_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.academic_monthly_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(stringResource(if (settings.useGpaScale) R.string.academic_gpa else R.string.academic_average), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (trend.isEmpty()) EmptyCard(stringResource(R.string.academic_monthly_empty)) else TrendCard(trend, gradeLabel, settings.useGpaScale)
                    }
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(stringResource(R.string.academic_monthly_history), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        if (trend.isEmpty()) EmptyCard(stringResource(R.string.academic_monthly_empty)) else {
                            Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                                Column(Modifier.padding(horizontal = 16.dp)) {
                                    trend.asReversed().forEachIndexed { index, point ->
                                        val previous = trend.getOrNull(trend.indexOf(point) - 1)
                                        Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text(point.label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                                            previous?.let { Text(stringResource(if (point.value >= it.value) R.string.academic_change_up_short else R.string.academic_change_down_short, formatGrade(abs(point.value - it.value))), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall); Spacer(Modifier.width(12.dp)) }
                                            Text(formatGrade(point.value), fontWeight = FontWeight.Bold, color = if (index == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                                        }
                                        if (index < trend.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item { Text(stringResource(R.string.academic_data_note, if (settings.useGpaScale) stringResource(R.string.academic_gpa_scale) else stringResource(R.string.academic_original_scale)), Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
private fun SubjectPicker(subjects: List<SubjectEntity>, selectedSubjectId: Long?, onSelect: (Long?) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.academic_subject_filter), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = selectedSubjectId == null, onClick = { onSelect(null) }, label = { Text(stringResource(R.string.academic_all_subjects)) })
            subjects.forEach { subject -> FilterChip(selected = selectedSubjectId == subject.id, onClick = { onSelect(subject.id) }, label = { Text(subject.name, maxLines = 1) }) }
        }
    }
}

@Composable
private fun TrendCard(points: List<AcademicGradePoint>, gradeLabel: String, useGpa: Boolean) {
    val maxValue = if (useGpa) 4.0 else if (points.maxOfOrNull { it.value }?.let { it > 10 } == true) 100.0 else 10.0
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.academic_chart_title, gradeLabel), fontWeight = FontWeight.SemiBold)
                Text(stringResource(R.string.academic_maximum, formatGrade(maxValue)), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
            }
            GradeEvolutionChart(points, maxValue, Modifier.fillMaxWidth().height(230.dp))
            Text(stringResource(R.string.academic_chart_note), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun EmptyCard(message: String) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Text(message, Modifier.padding(18.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun monthlyTrend(
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

private fun periodPoints(
    subjects: List<SubjectEntity>, periods: List<AcademicPeriodEntity>, categories: List<com.mocas.data.local.GradeCategoryEntity>,
    items: List<GradeItemEntity>, units: List<com.mocas.data.local.GradeUnitEntity>, weights: List<com.mocas.data.local.GradeUnitCategoryWeightEntity>,
    subjectId: Long?, useGpa: Boolean
): List<AcademicGradePoint> = periods.sortedBy { it.startDate }.mapNotNull { period ->
    val active = subjects.filter { subject -> (subjectId == null || subject.id == subjectId) && subject.semesterStart <= period.endDate && subject.semesterEnd >= period.startDate }
    active.mapNotNull { GradeCalculator.subjectAverage(it.id, categories, items, units, weights) }.takeIf { it.isNotEmpty() }?.average()
        ?.let { AcademicGradePoint(period.name, toDisplayGrade(it, useGpa)) }
}

private fun toDisplayGrade(value: Double, useGpa: Boolean): Double = if (useGpa) (value / if (value > 10) 100.0 else 10.0) * 4.0 else value

@Composable
private fun GradeEvolutionChart(points: List<AcademicGradePoint>, maxValue: Double, modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val grid = MaterialTheme.colorScheme.outlineVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    Canvas(modifier) {
        val left = 38.dp.toPx(); val right = 8.dp.toPx(); val top = 24.dp.toPx(); val bottom = 38.dp.toPx()
        val chartWidth = size.width - left - right; val chartHeight = size.height - top - bottom
        repeat(5) { i ->
            val y = top + chartHeight * i / 4
            drawLine(grid, Offset(left, y), Offset(size.width - right, y), 1.dp.toPx())
            drawContext.canvas.nativeCanvas.drawText(String.format(Locale.getDefault(), "%.1f", maxValue * (4 - i) / 4), 0f, y + 4.dp.toPx(), android.graphics.Paint().apply { color = labelColor.toArgb(); textSize = 11.sp.toPx() })
        }
        val xs = points.indices.map { if (points.size == 1) left + chartWidth / 2 else left + chartWidth * it / (points.size - 1) }
        val ys = points.map { top + chartHeight * (1f - (it.value / maxValue).toFloat().coerceIn(0f, 1f)) }
        if (points.size > 1) {
            val path = Path().apply { moveTo(xs.first(), ys.first()); for (i in 1 until xs.size) lineTo(xs[i], ys[i]) }
            drawPath(path, primary, style = Stroke(width = 3.dp.toPx()))
        }
        points.indices.forEach { i ->
            drawCircle(surface, 5.dp.toPx(), Offset(xs[i], ys[i])); drawCircle(primary, 3.dp.toPx(), Offset(xs[i], ys[i]))
            val paint = android.graphics.Paint().apply { color = primary.toArgb(); textSize = 12.sp.toPx(); textAlign = android.graphics.Paint.Align.CENTER; typeface = android.graphics.Typeface.DEFAULT_BOLD }
            drawContext.canvas.nativeCanvas.drawText(formatGrade(points[i].value), xs[i], ys[i] - 10.dp.toPx(), paint)
            val labelPaint = android.graphics.Paint().apply { color = labelColor.toArgb(); textSize = 10.sp.toPx(); textAlign = android.graphics.Paint.Align.CENTER }
            drawContext.canvas.nativeCanvas.drawText(points[i].label.take(9), xs[i], size.height - 8.dp.toPx(), labelPaint)
        }
    }
}

private fun formatGrade(value: Double): String = String.format(Locale.getDefault(), "%.2f", value).replace('.', ',')
