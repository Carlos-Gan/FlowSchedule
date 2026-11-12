package com.mocas.ui.components.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mocas.R
import com.mocas.data.repository.CalendarSyncManager
import com.mocas.data.repository.CalendarSyncScheduler
import com.mocas.ui.model.AppSettings

@Composable
fun CalendarSyncSection(
    settings: AppSettings, // tu tipo real
    syncMessage: String?,
    onSettingsChange: (AppSettings) -> Unit,
    onSyncNow: () -> Unit
) {
    val context = LocalContext.current
    val permissions = arrayOf(
        android.Manifest.permission.READ_CALENDAR,
        android.Manifest.permission.WRITE_CALENDAR
    )
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.all { it }) {
            onSettingsChange(settings.copy(calendarSyncEnabled = true))
        }
    }

    SettingsSectionHeader(title = stringResource(R.string.calendar_sync_title))

    SettingsCard {
        SettingRow(
            icon = Icons.Outlined.CalendarMonth,
            title = stringResource(R.string.calendar_sync_auto_title),
            subtitle = stringResource(R.string.calendar_sync_auto_desc),
            action = {
                Switch(
                    checked = settings.calendarSyncEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            if (CalendarSyncManager.hasCalendarPermissions(context)) {
                                onSettingsChange(settings.copy(calendarSyncEnabled = true))
                            } else {
                                permissionLauncher.launch(permissions)
                            }
                        } else {
                            onSettingsChange(settings.copy(calendarSyncEnabled = false))
                            CalendarSyncScheduler.cancelPeriodicSync(context)
                        }
                    },
                    colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                )
            }
        )

        SettingsDivider()

        SettingRow(
            icon = Icons.Outlined.CalendarMonth,
            title = stringResource(R.string.sync_now_btn),
            subtitle = stringResource(R.string.calendar_sync_now_desc),
            onClick = {
                if (CalendarSyncManager.hasCalendarPermissions(context)) {
                    onSyncNow()
                } else {
                    permissionLauncher.launch(permissions)
                }
            }
        )

        if (syncMessage != null) {
            SettingsDivider()
            Text(
                text = syncMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}