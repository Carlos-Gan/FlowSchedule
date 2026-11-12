package com.mocas.ui.components.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mocas.R

@Composable
fun PreferencesStep(
    notif: Boolean, onNotifChange: (Boolean) -> Unit,
    cal: Boolean, onCalChange: (Boolean) -> Unit
) {
    StepLayout(
        title = stringResource(R.string.onboarding_paso_preferencias_titulo),
        subtitle = stringResource(R.string.onboarding_paso_preferencias_subtitulo),
        icon = Icons.Default.Settings
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            PreferenceToggleItem(
                title = stringResource(R.string.onboarding_notificaciones_titulo),
                subtitle = stringResource(R.string.onboarding_notificaciones_subtitulo),
                icon = Icons.Default.Notifications,
                checked = notif,
                onCheckedChange = onNotifChange
            )
            PreferenceToggleItem(
                title = stringResource(R.string.onboarding_calendario_titulo),
                subtitle = stringResource(R.string.onboarding_calendario_subtitulo),
                icon = Icons.Default.CalendarMonth,
                checked = cal,
                onCheckedChange = onCalChange
            )
            OnboardingInfoCallout(
                stringResource(R.string.onboarding_preferences_hint),
                success = true
            )
        }
    }
}