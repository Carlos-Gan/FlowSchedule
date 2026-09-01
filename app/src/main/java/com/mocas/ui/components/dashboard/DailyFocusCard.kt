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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mocas.R
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
        if (wholeHours > 0) stringResource(R.string.horas_restan_formato, wholeHours, minutes) 
        else stringResource(R.string.minutos_restan_formato, minutes)
    } else if (stats.totalHours > 0) {
        stringResource(R.string.dia_terminado)
    } else {
        stringResource(R.string.dia_libre)
    }

    val footerText = if (stats.totalHours > 0) {
        stringResource(R.string.total_hoy_formato, String.format(java.util.Locale.US, "%.1f", stats.totalHours))
    } else {
        stringResource(R.string.sin_clases_hoy)
    }

    val backgroundColor =
        if (isFinishedOrFree) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceContainerLowest
    val contentColor =
        if (isFinishedOrFree) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface
    val labelColor =
        if (isFinishedOrFree) MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
    val secondaryColor =
        if (isFinishedOrFree) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.secondary

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        shadowElevation = if (isFinishedOrFree) 0.dp else 2.dp,
        border = if (isFinishedOrFree) androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
        ) else null
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.progreso_del_dia),
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
