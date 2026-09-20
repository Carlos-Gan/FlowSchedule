package com.mocas.ui.components.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
fun RemindersConfigDialog(
    settings: AppSettings,
    onUpdate: (AppSettings) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    stringResource(R.string.configurar_recordatorios),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = stringResource(R.string.aviso_antes_de),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Tareas
                ReminderCategoryCard(
                    icon = Icons.AutoMirrored.Outlined.Assignment,
                    title = stringResource(R.string.filtro_tareas),
                    currentMinutes = settings.taskReminderMinutes,
                    onSelect = { onUpdate(settings.copy(taskReminderMinutes = it)) }
                )

                // Exámenes
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ReminderCategoryCard(
                        icon = Icons.Outlined.Quiz,
                        title = stringResource(R.string.filtro_examenes),
                        currentMinutes = settings.examReminderMinutes,
                        onSelect = { onUpdate(settings.copy(examReminderMinutes = it)) }
                    )

                    // Card especial para Preparación de Exámenes
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Outlined.NotificationsActive,
                                        null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        stringResource(R.string.recordatorio_preparacion),
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        stringResource(R.string.recordatorio_preparacion_desc),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 14.sp
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
                                Spacer(modifier = Modifier.height(12.dp))
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
                    }
                }

                // Eventos
                ReminderCategoryCard(
                    icon = Icons.Outlined.Event,
                    title = stringResource(R.string.filtro_eventos),
                    currentMinutes = settings.eventReminderMinutes,
                    onSelect = { onUpdate(settings.copy(eventReminderMinutes = it)) }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.padding(bottom = 8.dp, end = 8.dp)
            ) {
                Text(
                    stringResource(R.string.listo),
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
private fun ReminderCategoryCard(
    icon: ImageVector,
    title: String,
    currentMinutes: Int,
    onSelect: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                title,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        ReminderOptionChips(
            currentMinutes = currentMinutes,
            options = listOf(
                15 to stringResource(R.string.minuto_corto, 15),
                30 to stringResource(R.string.minuto_corto, 30),
                60 to stringResource(R.string.hora_corta, 1),
                120 to stringResource(R.string.hora_corta, 2),
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
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { (minutes, label) ->
            val isSelected = currentMinutes == minutes
            Surface(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onSelect(minutes) },
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                shape = CircleShape
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
