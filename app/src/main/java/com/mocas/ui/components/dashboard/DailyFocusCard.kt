package com.mocas.ui.components.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mocas.ui.model.DailyClassStats

@ExperimentalMaterial3Api
@Composable
fun DailyFocusCard(
    stats: DailyClassStats
) {
    val isFinishedOrFree = stats.totalHours == 0.0 || stats.progress >= 1f

    val remainingLabel = if (stats.remainingHours > 0) {
        val wholeHours = stats.remainingHours.toInt()
        val minutes = ((stats.remainingHours - wholeHours) * 60).toInt()
        if (wholeHours > 0) "${wholeHours}h ${minutes}m restan" else "${minutes}min restan"
    } else if (stats.totalHours > 0) {
        "Día terminado"
    } else {
        "Día libre"
    }

    val footerText = if (stats.totalHours > 0) {
        "Total hoy: ${String.format(java.util.Locale.US, "%.1f", stats.totalHours)}h"
    } else {
        "Sin clases hoy"
    }

    val backgroundColor =
        if (isFinishedOrFree) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceContainerLowest
    val contentColor =
        if (isFinishedOrFree) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
    val labelColor =
        if (isFinishedOrFree) Color(0xFF2E7D32).copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
    val secondaryColor =
        if (isFinishedOrFree) Color(0xFF2E7D32) else MaterialTheme.colorScheme.secondary

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        shadowElevation = if (isFinishedOrFree) 0.dp else 2.dp,
        border = if (isFinishedOrFree) androidx.compose.foundation.BorderStroke(
            1.dp,
            Color(0xFFA5D6A7)
        ) else null
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Progreso del día",
                style = MaterialTheme.typography.labelMedium,
                color = labelColor,
                modifier = Modifier.align(Alignment.Start)
            )

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularWavyProgressIndicator(
                    progress = { stats.progress },
                    modifier = Modifier.size(80.dp)
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${(stats.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = contentColor
                    )

                    Text(
                        text = remainingLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp
                        ),
                        color = secondaryColor
                    )
                }
            }

            Text(
                text = footerText,
                style = MaterialTheme.typography.labelSmall,
                color = labelColor
            )
        }
    }
}