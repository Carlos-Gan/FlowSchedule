package com.mocas.ui.components.onboarding

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mocas.ui.theme.IndigoLight
import com.mocas.ui.theme.TurquoiseSecondary

@Composable
fun OnboardingHeroIcon(
    icon: ImageVector,
    large: Boolean = false
) {
    val motion = rememberInfiniteTransition(label = "onboarding_hero_motion")
    val pulse by motion.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.12f,
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
    val containerSize = if (large) 164.dp else 94.dp
    val iconContainerSize = if (large) 108.dp else 58.dp

    Box(
        modifier = Modifier.size(containerSize),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    scaleX = pulse
                    scaleY = pulse
                }
                .background(
                    Brush.radialGradient(
                        colorStops = arrayOf(
                            0f to MaterialTheme.colorScheme.primary.copy(alpha = 0.52f),
                            0.52f to MaterialTheme.colorScheme.secondary.copy(alpha = 0.36f),
                            0.78f to MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
                            1f to Color.Transparent
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(iconContainerSize)
                .graphicsLayer { translationY = floatOffset }
                .clip(CircleShape)
                .background(
                    if (large) {
                        Brush.linearGradient(
                            colors = listOf(
                                IndigoLight,
                                MaterialTheme.colorScheme.primary,
                                TurquoiseSecondary
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
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