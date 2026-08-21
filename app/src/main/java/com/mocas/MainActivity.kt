package com.mocas

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mocas.ui.screens.MainAppScreen
import com.mocas.ui.screens.OnboardingScreen
import com.mocas.ui.theme.SnapMyScheduleTheme
import com.mocas.ui.viewmodel.ScheduleViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ScheduleViewModel by viewModels()

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val settings by
            viewModel.appSettings.collectAsStateWithLifecycle()

            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                if (!granted && settings.notificationsEnabled) {
                    viewModel.updateSettings(settings.copy(notificationsEnabled = false))
                }
            }

            LaunchedEffect(settings.notificationsEnabled, settings.onboardingCompleted) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    settings.notificationsEnabled &&
                    settings.onboardingCompleted &&
                    ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            val darkTheme = resolveDarkTheme(
                themeMode = settings.themeMode,
                systemDarkTheme = isSystemInDarkTheme()
            )

            SnapMyScheduleTheme(
                darkTheme = darkTheme
            ) {
                if (settings.onboardingCompleted) {
                    MainAppScreen(viewModel = viewModel)
                } else {
                    OnboardingScreen(
                        initialSettings = settings,
                        onComplete = viewModel::completeOnboarding
                    )
                }
            }
        }
    }
}

private fun resolveDarkTheme(
    themeMode: String,
    systemDarkTheme: Boolean
): Boolean {
    return when (themeMode.uppercase()) {
        "LIGHT" -> false
        "DARK" -> true
        else -> systemDarkTheme
    }
}
