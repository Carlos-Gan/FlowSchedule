package com.mocas.ui.components.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mocas.R
import com.mocas.ui.model.AppSettings

@Composable
fun RemindersContent(
    settings: AppSettings,
    onUpdate: (AppSettings) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Tareas
        ReminderCategoryCard(
            icon = Icons.AutoMirrored.Outlined.Assignment,
            title = stringResource(R.string.filtro_tareas),
            currentMinutes = settings.taskReminderMinutes,
            onSelect = { onUpdate(settings.copy(taskReminderMinutes = it)) }
        )

        // Exámenes y preparación usan una sola tarjeta.
        ReminderCategoryCard(
            icon = Icons.Outlined.Quiz,
            title = stringResource(R.string.filtro_examenes),
            currentMinutes = settings.examReminderMinutes,
            onSelect = { onUpdate(settings.copy(examReminderMinutes = it)) },
            extraContent = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.recordatorio_preparacion),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                stringResource(R.string.recordatorio_preparacion_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }
                        Switch(
                            checked = settings.examPrepReminderEnabled,
                            onCheckedChange = { onUpdate(settings.copy(examPrepReminderEnabled = it)) },
                            modifier = Modifier.scale(0.8f),
                            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                        )
                    }

                    if (settings.examPrepReminderEnabled) {
                        ReminderOptionChips(
                            currentMinutes = settings.examPrepReminderMinutes,
                            options = listOf(
                                3 * 24 * 60 to "3 ${stringResource(R.string.dias).lowercase()}",
                                5 * 24 * 60 to "5 ${stringResource(R.string.dias).lowercase()}",
                                7 * 24 * 60 to "1 ${stringResource(R.string.semana_label).lowercase()}"
                            ),
                            onSelect = { onUpdate(settings.copy(examPrepReminderMinutes = it)) }
                        )
                    }
                }
            }
        )

        // Eventos
        ReminderCategoryCard(
            icon = Icons.Outlined.Event,
            title = stringResource(R.string.filtro_eventos),
            currentMinutes = settings.eventReminderMinutes,
            onSelect = { onUpdate(settings.copy(eventReminderMinutes = it)) }
        )
    }
}

@Composable
private fun ReminderCategoryCard(
    icon: ImageVector,
    title: String,
    currentMinutes: Int,
    onSelect: (Int) -> Unit,
    extraContent: (@Composable () -> Unit)? = null
) {
    var showCustomDialog by remember { mutableStateOf(false) }
    val options = listOf(
        15 to stringResource(R.string.minuto_corto, 15),
        30 to stringResource(R.string.minuto_corto, 30),
        60 to stringResource(R.string.hora_corta, 1),
        120 to stringResource(R.string.hora_corta, 2),
        24 * 60 to "1 ${stringResource(R.string.dia).lowercase()}",
        2 * 24 * 60 to "2 ${stringResource(R.string.dias).lowercase()}"
    )
    val selectedOption = options.firstOrNull { it.first == currentMinutes }
    val selectedLabel = selectedOption?.second ?: when {
        currentMinutes % (24 * 60) == 0 -> {
            val days = currentMinutes / (24 * 60)
            "$days ${stringResource(if (days == 1) R.string.dia else R.string.dias).lowercase()}"
        }
        currentMinutes % 60 == 0 -> stringResource(R.string.hora_corta, currentMinutes / 60)
        else -> stringResource(R.string.recordatorio_minutos_personalizado, currentMinutes)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        stringResource(R.string.recordatorio_aviso_antes_formato, selectedLabel),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            ReminderOptionChips(
                currentMinutes = currentMinutes,
                options = options,
                onSelect = onSelect,
                onCustomClick = { showCustomDialog = true },
                customLabel = stringResource(R.string.recordatorio_personalizado),
                customSelected = selectedOption == null,
                customSelectedLabel = selectedLabel
            )
            extraContent?.invoke()
        }
    }

    if (showCustomDialog) {
        val initialUnit = when {
            currentMinutes % (24 * 60) == 0 -> 24 * 60
            currentMinutes % 60 == 0 -> 60
            else -> 1
        }
        var unitMinutes by remember(currentMinutes) { mutableStateOf(initialUnit) }
        var amountText by remember(currentMinutes) {
            mutableStateOf((currentMinutes / initialUnit).toString())
        }
        val amount = amountText.toIntOrNull()
        val minutes = amount?.times(unitMinutes)
        val maxAmount = 43_200 / unitMinutes
        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            title = { Text(stringResource(R.string.recordatorio_personalizado)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { value -> amountText = value.filter(Char::isDigit).take(5) },
                        label = { Text(stringResource(R.string.recordatorio_cantidad)) },
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            1 to stringResource(R.string.recordatorio_unidad_minutos),
                            60 to stringResource(R.string.recordatorio_unidad_horas),
                            24 * 60 to stringResource(R.string.recordatorio_unidad_dias)
                        ).forEach { (unit, label) ->
                            FilterChip(
                                selected = unitMinutes == unit,
                                onClick = { unitMinutes = unit },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSelect(minutes!!)
                        showCustomDialog = false
                    },
                    enabled = amount != null && amount > 0 && amount <= maxAmount
                ) { Text(stringResource(R.string.guardar)) }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDialog = false }) {
                    Text(stringResource(R.string.cancelar))
                }
            }
        )
    }
}

@Composable
private fun ReminderOptionChips(
    currentMinutes: Int,
    options: List<Pair<Int, String>>,
    onSelect: (Int) -> Unit,
    onCustomClick: () -> Unit = {},
    customLabel: String? = null,
    customSelected: Boolean = false,
    customSelectedLabel: String? = null
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { (minutes, label) ->
            val isSelected = currentMinutes == minutes
            Surface(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onSelect(minutes) }
                    .heightIn(min = 48.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = CircleShape
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (customLabel != null) {
            Surface(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onCustomClick)
                    .heightIn(min = 48.dp),
                color = if (customSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = CircleShape
            ) {
                Text(
                    text = if (customSelected) customSelectedLabel ?: customLabel else customLabel,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    fontSize = 13.sp,
                    fontWeight = if (customSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (customSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
