package com.mocas.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.mocas.ui.components.onboarding.AppearanceStep
import com.mocas.ui.components.onboarding.IdentityStep
import com.mocas.ui.components.onboarding.OnboardingBottomBar
import com.mocas.ui.components.onboarding.OnboardingHeroIcon
import com.mocas.ui.components.onboarding.OnboardingInfoCallout
import com.mocas.ui.components.onboarding.PreferenceToggleItem
import com.mocas.ui.components.onboarding.PreferencesStep
import com.mocas.ui.components.onboarding.StepLayout
import com.mocas.ui.components.onboarding.StudiesStep
import com.mocas.ui.components.onboarding.ThemeOptionItem
import com.mocas.ui.components.onboarding.WelcomeFeature
import com.mocas.ui.components.onboarding.WelcomeStep
import com.mocas.ui.components.onboarding.onboardingControlBorder
import com.mocas.ui.components.onboarding.onboardingControlFill
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
    var educationStage by remember { mutableStateOf(initialSettings.educationStage) }
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
                    canContinue = when (currentStep) {
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
                                    educationStage = educationStage,
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
                            .togetherWith(slideOutHorizontally(animationSpec = tween(180)) { -it / 4 } + fadeOut(
                                tween(150)
                            ))
                    } else {
                        (slideInHorizontally(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) { -it / 3 } + fadeIn(tween(240)))
                            .togetherWith(slideOutHorizontally(animationSpec = tween(180)) { it / 4 } + fadeOut(
                                tween(150)
                            ))
                    }
                },
                label = "onboarding_step_transition",
                modifier = Modifier.fillMaxSize()
            ) { step ->
                when (step) {
                    0 -> WelcomeStep(onStart = { currentStep = 1 })
                    1 -> IdentityStep(
                        name,
                        onNameChange = { name = it },
                        onDone = { focusManager.clearFocus() })

                    2 -> StudiesStep(
                        educationLevel, onLevelChange = { educationLevel = it },
                        educationStage, onStageChange = { educationStage = it },
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