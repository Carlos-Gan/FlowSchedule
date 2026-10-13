package com.mocas.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mocas.R
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
    var currentStep by remember { mutableIntStateOf(0) }
    
    // State
    var name by remember { mutableStateOf("") }
    var educationLevel by remember { mutableStateOf("") }
    var educationInstitution by remember { mutableStateOf("") }
    var themeMode by remember { mutableStateOf("AUTO") }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var calendarSyncEnabled by remember { mutableStateOf(true) }

    val focusManager = LocalFocusManager.current
    val carreraDef = stringResource(R.string.carrera_default)
    val instDef = stringResource(R.string.institucion_default)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            // Animamos la aparición/desaparición de la barra inferior
            AnimatedVisibility(
                visible = currentStep > 0,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                OnboardingBottomBar(
                    currentStep = currentStep,
                    totalSteps = 4,
                    canContinue = when(currentStep) {
                        1 -> name.isNotBlank()
                        else -> true
                    },
                    onNext = { 
                        if (currentStep < 4) currentStep++ 
                        else {
                            onComplete(
                                initialSettings.copy(
                                    userName = name,
                                    educationLevel = educationLevel.ifBlank { carreraDef },
                                    educationInstitution = educationInstitution.ifBlank { instDef },
                                    themeMode = themeMode,
                                    notificationsEnabled = notificationsEnabled,
                                    calendarSyncEnabled = calendarSyncEnabled,
                                    onboardingCompleted = true
                                )
                            )
                        }
                    },
                    onBack = { if (currentStep > 0) currentStep-- }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) { it / 3 } + fadeIn(tween(240)))
                            .togetherWith(slideOutHorizontally(animationSpec = tween(180)) { -it / 4 } + fadeOut(tween(150)))
                    } else {
                        (slideInHorizontally(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) { -it / 3 } + fadeIn(tween(240)))
                            .togetherWith(slideOutHorizontally(animationSpec = tween(180)) { it / 4 } + fadeOut(tween(150)))
                    }
                },
                label = "onboarding_step_transition",
                modifier = Modifier.fillMaxSize()
            ) { step ->
                when (step) {
                    0 -> WelcomeStep(onStart = { currentStep = 1 })
                    1 -> IdentityStep(name, onNameChange = { name = it }, onDone = { focusManager.clearFocus() })
                    2 -> StudiesStep(
                        educationLevel, onLevelChange = { educationLevel = it },
                        educationInstitution, onInstChange = { educationInstitution = it },
                        onDone = { focusManager.clearFocus() }
                    )
                    3 -> AppearanceStep(themeMode, onThemeChange = { themeMode = it })
                    4 -> PreferencesStep(
                        notificationsEnabled, onNotifChange = { notificationsEnabled = it },
                        calendarSyncEnabled, onCalChange = { calendarSyncEnabled = it }
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingHeroIcon(
    icon: ImageVector,
    large: Boolean = false
) {
    val motion = rememberInfiniteTransition(label = "onboarding_hero_motion")
    val pulse by motion.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1700, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "onboarding_hero_pulse"
    )
    val floatOffset by motion.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "onboarding_hero_float"
    )
    val containerSize = if (large) 148.dp else 82.dp
    val haloSize = if (large) 136.dp else 74.dp
    val iconContainerSize = if (large) 108.dp else 58.dp

    Box(
        modifier = Modifier.size(containerSize),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(haloSize)
                .graphicsLayer {
                    scaleX = pulse
                    scaleY = pulse
                }
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.38f))
        )
        Box(
            modifier = Modifier
                .size(iconContainerSize)
                .graphicsLayer { translationY = floatOffset }
                .clip(RoundedCornerShape(if (large) 32.dp else 20.dp))
                .background(
                    if (large) {
                        Brush.linearGradient(
                            colors = listOf(IndigoLight, MaterialTheme.colorScheme.primary, TurquoiseSecondary)
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (large) Color.White else MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(if (large) 56.dp else 30.dp)
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = if (large) 16.dp else 8.dp, end = if (large) 14.dp else 8.dp)
                .size(if (large) 14.dp else 10.dp)
                .graphicsLayer { translationY = -floatOffset }
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.tertiary)
        )
    }
}

@Composable
private fun WelcomeStep(onStart: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OnboardingHeroIcon(icon = Icons.Default.School, large = true)

        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = stringResource(R.string.onboarding_bienvenida_titulo),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                letterSpacing = (-0.5).sp
            )
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = stringResource(R.string.onboarding_bienvenida_subtitulo),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                stringResource(R.string.onboarding_comenzar_boton),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.PlayArrow, null)
        }
    }
}

@Composable
private fun IdentityStep(name: String, onNameChange: (String) -> Unit, onDone: () -> Unit) {
    StepLayout(
        title = stringResource(R.string.onboarding_paso_identidad_titulo),
        subtitle = stringResource(R.string.onboarding_paso_identidad_subtitulo),
        icon = Icons.Default.Person
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { onNameChange(capitalizeFirstLetter(it)) },
            modifier = Modifier.fillMaxWidth().testTag("onboarding_name_field"),
            label = { Text(stringResource(R.string.onboarding_nombre_label)) },
            placeholder = { Text(stringResource(R.string.onboarding_nombre_placeholder)) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            supportingText = {
                if (name.isBlank()) Text(stringResource(R.string.onboarding_error_nombre))
            }
        )
    }
}

@Composable
private fun StudiesStep(
    level: String, onLevelChange: (String) -> Unit,
    inst: String, onInstChange: (String) -> Unit,
    onDone: () -> Unit
) {
    StepLayout(
        title = stringResource(R.string.onboarding_paso_estudios_titulo),
        subtitle = stringResource(R.string.onboarding_paso_estudios_subtitulo),
        icon = Icons.Default.EditCalendar
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = level,
                onValueChange = { onLevelChange(capitalizeFirstLetter(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.onboarding_carrera_label)) },
                placeholder = { Text(stringResource(R.string.onboarding_carrera_placeholder)) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )
            OutlinedTextField(
                value = inst,
                onValueChange = { onInstChange(capitalizeFirstLetter(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.onboarding_institucion_label)) },
                placeholder = { Text(stringResource(R.string.onboarding_institucion_placeholder)) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onDone() })
            )
        }
    }
}

@Composable
private fun AppearanceStep(current: String, onThemeChange: (String) -> Unit) {
    StepLayout(
        title = stringResource(R.string.onboarding_apariencia_titulo),
        subtitle = stringResource(R.string.onboarding_paso_preferencias_subtitulo),
        icon = Icons.Default.SettingsBrightness
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ThemeOptionItem(
                label = stringResource(R.string.onboarding_tema_claro),
                icon = Icons.Default.LightMode,
                selected = current == "LIGHT",
                onClick = { onThemeChange("LIGHT") }
            )
            ThemeOptionItem(
                label = stringResource(R.string.onboarding_tema_sistema),
                icon = Icons.Default.SettingsBrightness,
                selected = current == "AUTO",
                onClick = { onThemeChange("AUTO") }
            )
            ThemeOptionItem(
                label = stringResource(R.string.onboarding_tema_oscuro),
                icon = Icons.Default.DarkMode,
                selected = current == "DARK",
                onClick = { onThemeChange("DARK") }
            )
        }
    }
}

@Composable
private fun PreferencesStep(
    notif: Boolean, onNotifChange: (Boolean) -> Unit,
    cal: Boolean, onCalChange: (Boolean) -> Unit
) {
    StepLayout(
        title = stringResource(R.string.onboarding_paso_preferencias_titulo),
        subtitle = stringResource(R.string.onboarding_paso_preferencias_subtitulo),
        icon = Icons.Default.Settings
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                onCheckedChange = onCalChange,
                showDivider = false
            )
        }
    }
}

@Composable
private fun StepLayout(
    title: String,
    subtitle: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
        OnboardingHeroIcon(icon = icon)
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black)
        )
        
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        content()
    }
}

@Composable
private fun OnboardingBottomBar(
    currentStep: Int,
    totalSteps: Int,
    canContinue: Boolean,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onBack,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.onboarding_atras_boton), fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(totalSteps) { i ->
                    val isCurrent = (i + 1) == currentStep
                    val color by animateColorAsState(
                        if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        animationSpec = tween(260),
                        label = "step_color"
                    )
                    val width by animateDpAsState(
                        targetValue = if (isCurrent) 24.dp else 8.dp,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                        label = "step_width"
                    )
                    Box(
                        modifier = Modifier
                            .width(width)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }

            Button(
                onClick = onNext,
                enabled = canContinue,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = if (currentStep == totalSteps) stringResource(R.string.onboarding_finalizar_boton) else stringResource(R.string.onboarding_siguiente_boton),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
                if (currentStep < totalSteps) {
                    Icon(Icons.Default.ChevronRight, null, modifier = Modifier.padding(start = 4.dp).size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun ThemeOptionItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            RadioButton(selected = selected, onClick = onClick)
        }
    }
}

@Composable
private fun PreferenceToggleItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    showDivider: Boolean = true
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }
        if (showDivider) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        }
    }
}
