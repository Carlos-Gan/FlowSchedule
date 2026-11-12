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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.mocas.ui.add.event.buildPeriodName
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mocas.R
import com.mocas.data.local.AcademicPeriodEntity
import com.mocas.ui.components.CalendarDateField
import com.mocas.ui.components.academic.AcademicPeriodRow
import com.mocas.ui.components.academic.EmptyPeriodsCard
import com.mocas.ui.components.academic.FeaturedPeriodCard
import com.mocas.ui.components.academic.PeriodEditorDialog
import com.mocas.ui.viewmodel.ScheduleViewModel
import com.mocas.util.DateTimeUtils
import com.mocas.util.PeriodPhase
import com.mocas.util.summarizePeriod
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class PeriodSummary(
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
