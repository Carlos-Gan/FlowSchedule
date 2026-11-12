package com.mocas.ui.components.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mocas.R

@Composable
fun AppearanceStep(current: String, onThemeChange: (String) -> Unit) {
    StepLayout(
        title = stringResource(R.string.onboarding_apariencia_titulo),
        subtitle = stringResource(R.string.onboarding_theme_subtitle),
        icon = Icons.Default.SettingsBrightness
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ThemeOptionItem(
                label = stringResource(R.string.onboarding_tema_claro),
                description = stringResource(R.string.onboarding_theme_light_description),
                icon = Icons.Default.LightMode,
                selected = current == "LIGHT",
                onClick = { onThemeChange("LIGHT") }
            )
            ThemeOptionItem(
                label = stringResource(R.string.onboarding_tema_sistema),
                description = stringResource(R.string.onboarding_theme_auto_description),
                icon = Icons.Default.SettingsBrightness,
                selected = current == "AUTO",
                onClick = { onThemeChange("AUTO") }
            )
            ThemeOptionItem(
                label = stringResource(R.string.onboarding_tema_oscuro),
                description = stringResource(R.string.onboarding_theme_dark_description),
                icon = Icons.Default.DarkMode,
                selected = current == "DARK",
                onClick = { onThemeChange("DARK") }
            )
            OnboardingInfoCallout(stringResource(R.string.onboarding_theme_hint))
        }
    }
}