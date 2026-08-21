package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.MainAppScreen
import com.example.ui.theme.SnapMyScheduleTheme
import com.example.ui.viewmodel.ScheduleViewModel

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

            val darkTheme = resolveDarkTheme(
                themeMode = settings.themeMode,
                systemDarkTheme = isSystemInDarkTheme()
            )

            SnapMyScheduleTheme(
                darkTheme = darkTheme
            ) {
                MainAppScreen(
                    viewModel = viewModel
                )
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