package com.mocas.ui.dialogs

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mocas.R
import com.mocas.ui.components.settings.TrashRow
import com.mocas.ui.viewmodel.ScheduleViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BackupsDialog(
    viewModel: ScheduleViewModel,
    onDismiss: () -> Unit
) {
    val automaticBackups by viewModel.automaticBackups.collectAsStateWithLifecycle()
    val automaticBackupEnabled by viewModel.automaticBackupEnabled.collectAsStateWithLifecycle()
    val automaticBackupInterval by viewModel.automaticBackupInterval.collectAsStateWithLifecycle()
    var pendingRestore by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { viewModel.refreshAutomaticBackupsNow() }

    // Se mantiene la extensión .flow; application/json puede hacer que algunos
    // proveedores de documentos agreguen .json al nombre sugerido.
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri -> uri?.let(viewModel::exportScheduleBackup) }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { viewModel.importScheduleBackup(it) } }

    val dateFormatter = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.respaldos_y_datos), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { exportLauncher.launch("FlowSchedule_${System.currentTimeMillis()}.flow") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.FileUpload, null, Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.exportar), fontSize = 12.sp)
                    }
                    Button(
                        onClick = {
                            importLauncher.launch(
                                arrayOf("application/json", "application/octet-stream", "*/*")
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.FileDownload, null, Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.importar), fontSize = 12.sp)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Text(
                    stringResource(R.string.copias_automaticas),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        stringResource(R.string.respaldo_programado_automaticamente),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = automaticBackupEnabled,
                        onCheckedChange = viewModel::setAutomaticBackupEnabled
                    )
                }

                if (automaticBackupEnabled) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            "daily" to R.string.respaldo_cada_dia,
                            "weekly" to R.string.respaldo_cada_semana,
                            "monthly" to R.string.respaldo_cada_mes
                        ).forEach { (interval, labelRes) ->
                            FilterChip(
                                selected = automaticBackupInterval == interval,
                                onClick = { viewModel.setAutomaticBackupInterval(interval) },
                                label = { Text(stringResource(labelRes)) }
                            )
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.heightIn(max = 250.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (automaticBackups.isEmpty()) {
                        item {
                            Text(
                                stringResource(R.string.no_hay_respaldos_automaticos),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    items(automaticBackups) { backup ->
                        TrashRow(
                            title = if (backup.reason == "Respaldo programado") {
                                stringResource(R.string.respaldo_programado)
                            } else backup.reason,
                            subtitle = dateFormatter.format(Date(backup.createdAtMillis)),
                            onRestore = { pendingRestore = backup.fileName },
                            onDelete = { viewModel.deleteAutomaticBackup(backup.fileName) }
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text(stringResource(R.string.cerrar)) }
        }
    )

    pendingRestore?.let { fileName ->
        AlertDialog(
            onDismissRequest = { pendingRestore = null },
            title = {
                Text(
                    stringResource(R.string.confirmar_restaurar_respaldo),
                    fontWeight = FontWeight.Bold
                )
            },
            text = { Text(stringResource(R.string.reemplazar_informacion_respaldo)) },
            confirmButton = {
                Button(onClick = {
                    viewModel.restoreAutomaticBackup(fileName)
                    pendingRestore = null
                    onDismiss()
                }) { Text(stringResource(R.string.restaurar_ahora)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestore = null }) {
                    Text(stringResource(R.string.cancelar))
                }
            }
        )
    }
}