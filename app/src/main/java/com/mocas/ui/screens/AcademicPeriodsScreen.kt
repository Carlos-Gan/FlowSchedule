package com.mocas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mocas.R
import com.mocas.data.local.AcademicPeriodEntity
import com.mocas.ui.components.CalendarDateField
import com.mocas.ui.viewmodel.ScheduleViewModel
import com.mocas.util.DateTimeUtils
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private enum class PeriodPhase { ACTIVE, UPCOMING, PAST }

private data class PeriodSummary(
    val period: AcademicPeriodEntity,
    val phase: PeriodPhase,
    val days: Int?,
    val progress: Float
)

@Composable
fun AcademicPeriodsScreen(
    viewModel: ScheduleViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val periods by viewModel.academicPeriods.collectAsStateWithLifecycle()
    val today = LocalDate.now()
    val summaries = remember(periods, today) {
        periods.mapNotNull { period -> summarizePeriod(period, today) }
            .sortedByDescending { it.period.startDate }
    }
    val featured = summaries.firstOrNull { it.phase == PeriodPhase.ACTIVE }
        ?: summaries.firstOrNull { it.phase == PeriodPhase.UPCOMING }

    var editorPeriod by remember { mutableStateOf<AcademicPeriodEntity?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var periodToDelete by remember { mutableStateOf<AcademicPeriodEntity?>(null) }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.academic_back))
                    }
                    Text(
                        stringResource(R.string.academic_periods_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (featured != null) {
                item { FeaturedPeriodCard(featured) }
            } else if (summaries.isEmpty()) {
                item { EmptyPeriodsCard() }
            } else {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(
                                stringResource(R.string.widget_no_academic_period),
                                modifier = Modifier.padding(start = 12.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (summaries.isNotEmpty()) {
                item {
                    Text(
                        stringResource(R.string.academic_periods_saved),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(summaries, key = { it.period.id }) { summary ->
                    AcademicPeriodRow(
                        summary = summary,
                        onEdit = {
                            editorPeriod = summary.period
                            showEditor = true
                        },
                        onDelete = { periodToDelete = summary.period }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = {
                editorPeriod = null
                showEditor = true
            },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp).navigationBarsPadding(),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.academic_periods_add))
        }
    }

    if (showEditor) {
        PeriodEditorDialog(
            period = editorPeriod,
            onDismiss = { showEditor = false },
            onSave = { viewModel.saveAcademicPeriod(it); showEditor = false }
        )
    }

    periodToDelete?.let { period ->
        AlertDialog(
            onDismissRequest = { periodToDelete = null },
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null) },
            title = { Text(stringResource(R.string.academic_periods_delete_title)) },
            text = { Text(stringResource(R.string.academic_periods_delete_message, period.name)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAcademicPeriod(period.id)
                    periodToDelete = null
                }) { Text(stringResource(R.string.eliminar), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { periodToDelete = null }) { Text(stringResource(R.string.cancelar_boton)) }
            }
        )
    }
}

@Composable
private fun FeaturedPeriodCard(summary: PeriodSummary) {
    val period = summary.period
    val statusText = when (summary.phase) {
        PeriodPhase.ACTIVE -> stringResource(R.string.academic_periods_active)
        PeriodPhase.UPCOMING -> stringResource(R.string.academic_periods_upcoming)
        PeriodPhase.PAST -> stringResource(R.string.academic_periods_past)
    }
    val daysText = when (summary.phase) {
        PeriodPhase.ACTIVE -> summary.days?.let { stringResource(R.string.academic_periods_days_left, it) }
        PeriodPhase.UPCOMING -> summary.days?.let { stringResource(R.string.academic_periods_starts_in, it) }
        PeriodPhase.PAST -> null
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)) {
                    Icon(
                        Icons.Default.EventAvailable,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(10.dp).size(22.dp)
                    )
                }
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(statusText, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(period.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            if (daysText != null) {
                Text(daysText, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Text(
                "${DateTimeUtils.formatDate(period.startDate)} – ${DateTimeUtils.formatDate(period.endDate)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            LinearProgressIndicator(
                progress = { summary.progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f)
            )
        }
    }
}

@Composable
private fun AcademicPeriodRow(
    summary: PeriodSummary,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val period = summary.period
    val status = when (summary.phase) {
        PeriodPhase.ACTIVE -> stringResource(R.string.academic_periods_active)
        PeriodPhase.UPCOMING -> summary.days?.let { stringResource(R.string.academic_periods_starts_in, it) }.orEmpty()
        PeriodPhase.PAST -> stringResource(R.string.academic_periods_past)
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 14.dp, bottom = 14.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(period.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "${DateTimeUtils.formatDate(period.startDate)} – ${DateTimeUtils.formatDate(period.endDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(status, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.editar), tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, contentDescription = stringResource(R.string.eliminar), tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun EmptyPeriodsCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Text(stringResource(R.string.academic_periods_empty_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                stringResource(R.string.academic_periods_empty_message),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun PeriodEditorDialog(
    period: AcademicPeriodEntity?,
    onDismiss: () -> Unit,
    onSave: (AcademicPeriodEntity) -> Unit
) {
    val today = LocalDate.now()
    var name by remember(period?.id) { mutableStateOf(period?.name.orEmpty()) }
    var startDate by remember(period?.id) { mutableStateOf(period?.startDate ?: today.toString()) }
    var endDate by remember(period?.id) { mutableStateOf(period?.endDate ?: today.plusMonths(4).toString()) }
    val validDates = DateTimeUtils.isValidDate(startDate) && DateTimeUtils.isValidDate(endDate) &&
        !LocalDate.parse(endDate).isBefore(LocalDate.parse(startDate))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (period == null) R.string.nuevo_periodo_titulo else R.string.editar_periodo_titulo)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.academic_periods_name)) },
                    singleLine = true
                )
                CalendarDateField(
                    value = startDate,
                    label = stringResource(R.string.inicio_periodo_label),
                    onDateSelected = { startDate = it },
                    isError = !DateTimeUtils.isValidDate(startDate)
                )
                CalendarDateField(
                    value = endDate,
                    label = stringResource(R.string.fin_periodo_label),
                    onDateSelected = { endDate = it },
                    isError = !DateTimeUtils.isValidDate(endDate) || !validDates
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        AcademicPeriodEntity(
                            id = period?.id ?: 0,
                            name = name.trim(),
                            startDate = startDate,
                            endDate = endDate,
                            colorHex = period?.colorHex ?: "#10B981",
                            createdAtMillis = period?.createdAtMillis ?: System.currentTimeMillis()
                        )
                    )
                },
                enabled = name.isNotBlank() && validDates
            ) { Text(stringResource(R.string.guardar_periodo_boton)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancelar_boton)) }
        }
    )
}

private fun summarizePeriod(period: AcademicPeriodEntity, today: LocalDate): PeriodSummary? {
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
