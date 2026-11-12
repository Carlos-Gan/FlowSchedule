package com.mocas.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ExpandMore
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
import com.mocas.data.local.GradeItemEntity
import com.mocas.data.local.SubjectEntity
import com.mocas.data.repository.GradeCalculator
import com.mocas.ui.components.academic.EmptyCard
import com.mocas.ui.components.academic.GradeEvolutionChart
import com.mocas.ui.components.academic.SubjectPicker
import com.mocas.ui.components.academic.TrendCard
import com.mocas.ui.viewmodel.ScheduleViewModel
import com.mocas.util.ProgressTab
import com.mocas.util.formatGrade
import com.mocas.util.monthlyTrend
import com.mocas.util.toDisplayGrade
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

@Composable
fun AcademicProgressScreen(viewModel: ScheduleViewModel, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    val subjectsWithSlots by viewModel.subjectsWithSlots.collectAsStateWithLifecycle()
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
    val recordedGradeCount = remember(categories, gradeItems, selectedSubjectId) {
        val categoryIds = categories.filter { selectedSubjectId == null || it.subjectId == selectedSubjectId }.mapTo(mutableSetOf()) { it.id }
        gradeItems.count { it.categoryId in categoryIds }
    }
    val trend = remember(subjects, categories, gradeItems, gradeUnits, weights, selectedSubjectId, settings.useGpaScale) {
        monthlyTrend(subjects, categories, gradeItems, gradeUnits, weights, selectedSubjectId, settings.useGpaScale)
    }
    val change = if (trend.size > 1) trend.last().value - trend[trend.lastIndex - 1].value else null
    val gradeLabel = if (settings.useGpaScale) stringResource(R.string.academic_gpa) else stringResource(R.string.academic_weighted_average)

    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding(horizontal = 24.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.academic_back)) }
                Text(stringResource(R.string.academic_progress_eyebrow), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 8.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.academic_progress_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.academic_progress_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)).padding(4.dp)) {
                listOf(ProgressTab.CURRENT to stringResource(R.string.academic_tab_current), ProgressTab.MONTHLY to stringResource(R.string.academic_tab_monthly)).forEach { (tab, label) ->
                    Surface(onClick = { selectedTab = tab }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(9.dp), color = if (selectedTab == tab) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant) {
                        Text(label, Modifier.padding(vertical = 10.dp), textAlign = TextAlign.Center, fontWeight = if (selectedTab == tab) FontWeight.SemiBold else FontWeight.Medium, color = if (selectedTab == tab) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            if (selectedTab == ProgressTab.CURRENT) {
                item {
                    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(stringResource(R.string.academic_current_grade, gradeLabel), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text(currentValue?.let(::formatGrade) ?: stringResource(R.string.academic_no_data_short), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                                if (currentValue != null) Text(if (settings.useGpaScale) " / 4" else " / 100", color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .75f))
                            }
                            Text(stringResource(R.string.academic_grade_count, recordedGradeCount), color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .8f), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
                item { SubjectPicker(subjects, selectedSubjectId, onSelect = { selectedSubjectId = it }) }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.academic_evolution_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(YearMonth.now().year.toString(), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
                        }
                        if (trend.isEmpty()) EmptyCard(stringResource(R.string.academic_monthly_empty))
                        else TrendCard(trend, gradeLabel, settings.useGpaScale)
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