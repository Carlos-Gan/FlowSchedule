package com.mocas.ui.components.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.RestoreFromTrash
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mocas.R
import com.mocas.ui.model.AppSettings

@Composable
fun GeneralDataSection(
    settings: AppSettings, // pon aquí el tipo real de tu `settings`
    onSettingsChange: (AppSettings) -> Unit,
    onRemindersClick: () -> Unit,
    onTrashClick: () -> Unit,
    onBackupsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsSectionHeader(
        title = stringResource(R.string.general_y_datos)
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceDim)
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            SettingRow(
                icon = Icons.Outlined.Notifications,
                title = stringResource(R.string.notificaciones_push),
                subtitle = stringResource(R.string.recordatorios_de_tareas_y_clases),
                action = {
                    Switch(
                        checked = settings.notificationsEnabled,
                        onCheckedChange = {
                            onSettingsChange(settings.copy(notificationsEnabled = it))
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                    )
                }
            )

            SettingsDivider()

            SettingRow(
                icon = Icons.Outlined.Alarm,
                title = stringResource(R.string.configurar_recordatorios),
                subtitle = stringResource(R.string.configurar_recordatorios_desc),
                onClick = onRemindersClick
            )

            SettingsDivider()

            SettingRow(
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                title = stringResource(R.string.usar_escala_gpa),
                subtitle = stringResource(R.string.cambiar_promedio),
                action = {
                    Switch(
                        checked = settings.useGpaScale,
                        onCheckedChange = {
                            onSettingsChange(settings.copy(useGpaScale = it))
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                    )
                }
            )

            SettingsDivider()

            SettingRow(
                icon = Icons.Outlined.RestoreFromTrash,
                title = stringResource(R.string.papelera_de_reciclaje),
                subtitle = stringResource(R.string.recupera_materias),
                onClick = onTrashClick
            )

            SettingsDivider()

            SettingRow(
                icon = Icons.Outlined.Backup,
                title = stringResource(R.string.respaldos_y_datos),
                subtitle = stringResource(R.string.copia_de_seguridad),
                onClick = onBackupsClick
            )
        }
    }
}
