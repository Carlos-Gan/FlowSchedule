package com.mocas.ui.components.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mocas.R
import com.mocas.ui.model.AppSettings

@Composable
fun RemindersConfigDialog(
    settings: AppSettings,
    onUpdate: (AppSettings) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.configurar_recordatorios), fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Config para Tareas
                ReminderSection(
                    title = stringResource(R.string.filtro_tareas),
                    currentMinutes = settings.taskReminderMinutes,
                    onSelect = { onUpdate(settings.copy(taskReminderMinutes = it)) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Config para Exámenes
                ReminderSection(
                    title = stringResource(R.string.filtro_examenes),
                    currentMinutes = settings.examReminderMinutes,
                    onSelect = { onUpdate(settings.copy(examReminderMinutes = it)) }
                )

                // Extra: Recordatorio de preparación para exámenes
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = settings.examPrepReminderEnabled,
                            onCheckedChange = { onUpdate(settings.copy(examPrepReminderEnabled = it)) }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.recordatorio_preparacion), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text(stringResource(R.string.recordatorio_preparacion_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    if (settings.examPrepReminderEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        ReminderOptionChips(
                            currentMinutes = settings.examPrepReminderMinutes,
                            options = listOf(
                                3 * 24 * 60 to "3 ${stringResource(R.string.dias)}",
                                5 * 24 * 60 to "5 ${stringResource(R.string.dias)}",
                                7 * 24 * 60 to "1 ${stringResource(R.string.semana_label).lowercase()}"
                            ),
                            onSelect = { onUpdate(settings.copy(examPrepReminderMinutes = it)) }
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Config para Eventos
                ReminderSection(
                    title = stringResource(R.string.filtro_eventos),
                    currentMinutes = settings.eventReminderMinutes,
                    onSelect = { onUpdate(settings.copy(eventReminderMinutes = it)) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.listo)) }
        }
    )
}

@Composable
private fun ReminderSection(
    title: String,
    currentMinutes: Int,
    onSelect: (Int) -> Unit
) {
    Column {
        Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        ReminderOptionChips(
            currentMinutes = currentMinutes,
            options = listOf(
                15 to "15m",
                30 to "30m",
                60 to "1h",
                120 to "2h",
                24 * 60 to "1 ${stringResource(R.string.dia).lowercase()}",
                2 * 24 * 60 to "2 ${stringResource(R.string.dias)}"
            ),
            onSelect = onSelect
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReminderOptionChips(
    currentMinutes: Int,
    options: List<Pair<Int, String>>,
    onSelect: (Int) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (minutes, label) ->
            FilterChip(
                selected = currentMinutes == minutes,
                onClick = { onSelect(minutes) },
                label = { Text(label, fontSize = 12.sp) }
            )
        }
    }
}