package com.mocas.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mocas.ui.model.AppSettings
import com.mocas.ui.theme.IndigoLight
import com.mocas.ui.theme.TurquoiseSecondary
import com.mocas.ui.util.capitalizeFirstLetter

@Composable
fun OnboardingScreen(
    initialSettings: AppSettings,
    onComplete: (AppSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember(initialSettings.userName) {
        mutableStateOf(initialSettings.userName.takeUnless { it == "Estudiante" }.orEmpty())
    }
    var themeMode by remember(initialSettings.themeMode) { mutableStateOf(initialSettings.themeMode) }
    var notificationsEnabled by remember { mutableStateOf(initialSettings.notificationsEnabled) }
    var calendarSyncEnabled by remember { mutableStateOf(initialSettings.calendarSyncEnabled) }
    var aiFeaturesEnabled by remember { mutableStateOf(initialSettings.aiFeaturesEnabled) }
    val focusManager = LocalFocusManager.current
    val canContinue = name.isNotBlank()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .testTag("onboarding_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(IndigoLight, MaterialTheme.colorScheme.primary, TurquoiseSecondary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "¡Bienvenido a SnapMySchedule!",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Personaliza la app antes de crear tu primer horario.",
                    modifier = Modifier.padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }

        item {
            SetupCard(
                title = "¿Cómo te llamas?",
                subtitle = "Usaremos tu nombre para personalizar el inicio.",
                icon = Icons.Default.Person
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = capitalizeFirstLetter(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_name_field"),
                    label = { Text("Tu nombre") },
                    placeholder = { Text("Ej. Charles") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    supportingText = {
                        if (name.isBlank()) Text("Escribe tu nombre para continuar")
                    }
                )
            }
        }

        item {
            SetupCard(
                title = "Elige la apariencia",
                subtitle = "Podrás cambiarla después desde Ajustes.",
                icon = Icons.Default.SettingsBrightness
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeOption(
                        label = "Claro",
                        icon = Icons.Default.LightMode,
                        selected = themeMode == "LIGHT",
                        onClick = { themeMode = "LIGHT" },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOption(
                        label = "Sistema",
                        icon = Icons.Default.SettingsBrightness,
                        selected = themeMode == "AUTO",
                        onClick = { themeMode = "AUTO" },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOption(
                        label = "Oscuro",
                        icon = Icons.Default.DarkMode,
                        selected = themeMode == "DARK",
                        onClick = { themeMode = "DARK" },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            SetupCard(
                title = "Preferencias rápidas",
                subtitle = "Activa solamente las funciones que quieras usar.",
                icon = Icons.Default.Notifications
            ) {
                PreferenceToggle(
                    title = "Notificaciones",
                    subtitle = "Recordatorios de clases y actividades",
                    icon = Icons.Default.Notifications,
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
                PreferenceToggle(
                    title = "Calendario",
                    subtitle = "Permitir exportar actividades",
                    icon = Icons.Default.CalendarMonth,
                    checked = calendarSyncEnabled,
                    onCheckedChange = { calendarSyncEnabled = it }
                )
                PreferenceToggle(
                    title = "Funciones con IA",
                    subtitle = "Escanear e importar horarios",
                    icon = Icons.Default.AutoAwesome,
                    checked = aiFeaturesEnabled,
                    onCheckedChange = { aiFeaturesEnabled = it },
                    showDivider = false
                )
            }
        }

        item {
            Button(
                onClick = {
                    focusManager.clearFocus()
                    onComplete(
                        initialSettings.copy(
                            userName = name,
                            themeMode = themeMode,
                            notificationsEnabled = notificationsEnabled,
                            calendarSyncEnabled = calendarSyncEnabled,
                            aiFeaturesEnabled = aiFeaturesEnabled
                        )
                    )
                },
                enabled = canContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .testTag("onboarding_continue_button"),
                shape = RoundedCornerShape(17.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = "Empezar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun SetupCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(9.dp).size(20.dp)
                    )
                }
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun ThemeOption(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        },
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                modifier = Modifier.padding(top = 5.dp),
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun PreferenceToggle(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    showDivider: Boolean = true
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(21.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = onboardingSwitchColors()
            )
        }
        if (showDivider) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
            )
        }
    }
}

@Composable
private fun onboardingSwitchColors() = SwitchDefaults.colors(
    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
    checkedTrackColor = MaterialTheme.colorScheme.primary,
    checkedBorderColor = MaterialTheme.colorScheme.primary,
    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
    uncheckedBorderColor = MaterialTheme.colorScheme.outline
)
