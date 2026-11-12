package com.mocas.ui.components.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mocas.R
import com.mocas.ui.model.AppSettings
import com.mocas.ui.model.BadgeStyle
import com.mocas.ui.theme.ThemeConfig

@Composable
fun PersonalizationSection(
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    onOpenAppearance: () -> Unit
) {
    var showThemeModeDialog by remember { mutableStateOf(false) }
    var showBadgeStyleDialog by remember { mutableStateOf(false) }

    SettingsSectionHeader(title = stringResource(R.string.personalizacion))

    SettingsCard {
        SettingRow(
            icon = when (settings.themeMode.uppercase()) {
                "LIGHT" -> Icons.Outlined.LightMode
                "DARK" -> Icons.Outlined.DarkMode
                else -> Icons.Outlined.BrightnessAuto
            },
            title = stringResource(R.string.modo_apariencia),
            subtitle = themeModeLabel(settings.themeMode.uppercase()),
            onClick = { showThemeModeDialog = true }
        )

        SettingsDivider()

        SettingRow(
            icon = Icons.Outlined.Palette,
            title = stringResource(R.string.tema_visual),
            subtitle = ThemeConfig.themes.find { it.id == settings.colorTheme }
                ?.let { stringResource(it.nameRes) }
                ?: stringResource(R.string.tema_estandar),
            onClick = onOpenAppearance
        )

        SettingsDivider()

        SettingRow(
            icon = Icons.Outlined.NotificationsActive,
            title = stringResource(R.string.estilo_indicador),
            subtitle = when (settings.badgeStyle) {
                BadgeStyle.NONE -> stringResource(R.string.indicador_oculto)
                BadgeStyle.DOT -> stringResource(R.string.indicador_solo_punto)
                BadgeStyle.NUMBER -> stringResource(R.string.indicador_con_numero)
            },
            onClick = { showBadgeStyleDialog = true }
        )
    }

    if (showThemeModeDialog) {
        ThemeModeDialog(
            current = settings.themeMode.uppercase(),
            onSelect = {
                onSettingsChange(settings.copy(themeMode = it))
                showThemeModeDialog = false
            },
            onDismiss = { showThemeModeDialog = false }
        )
    }

    if (showBadgeStyleDialog) {
        BadgeStyleDialog(
            current = settings.badgeStyle,
            onSelect = {
                onSettingsChange(settings.copy(badgeStyle = it))
                showBadgeStyleDialog = false
            },
            onDismiss = { showBadgeStyleDialog = false }
        )
    }
}

@Composable
private fun themeModeLabel(mode: String): String = when (mode) {
    "LIGHT" -> stringResource(R.string.tema_claro)
    "DARK" -> stringResource(R.string.tema_oscuro)
    else -> stringResource(R.string.tema_sistema)
}

@Composable
private fun ThemeModeDialog(
    current: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.modo_apariencia), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                listOf("AUTO", "LIGHT", "DARK").forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(mode) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = current == mode, onClick = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(themeModeLabel(mode))
                            Text(
                                text = when (mode) {
                                    "LIGHT" -> stringResource(R.string.tema_desc_claro)
                                    "DARK" -> stringResource(R.string.tema_desc_oscuro)
                                    else -> stringResource(R.string.tema_desc_sistema)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cerrar)) }
        }
    )
}

@Composable
private fun BadgeStyleDialog(
    current: BadgeStyle,
    onSelect: (BadgeStyle) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.estilo_indicador), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                BadgeStyle.entries.forEach { style ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(style) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = current == style, onClick = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = when (style) {
                                BadgeStyle.NONE -> stringResource(R.string.indicador_oculto)
                                BadgeStyle.DOT -> stringResource(R.string.indicador_solo_punto_rojo)
                                BadgeStyle.NUMBER -> stringResource(R.string.indicador_punto_con_numero)
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cerrar)) }
        }
    )
}