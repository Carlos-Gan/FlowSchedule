package com.mocas.ui.components.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun onboardingControlFill(selected: Boolean): Color =
    if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surface

@Composable
fun onboardingControlBorder(selected: Boolean): BorderStroke = BorderStroke(
    width = if (selected) 1.dp else 1.dp,
    if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f) else MaterialTheme.colorScheme.outlineVariant
)
