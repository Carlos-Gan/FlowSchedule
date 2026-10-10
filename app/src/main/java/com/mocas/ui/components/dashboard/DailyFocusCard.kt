package com.mocas.ui.components.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mocas.R
import com.mocas.ui.model.DailyClassStats
import java.util.Locale

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
        stringResource(R.string.total_hoy_formato, String.format(Locale.US, "%.1f", stats.totalHours))
    } else {
        stringResource(R.string.sin_clases_hoy)
    }

    val backgroundColor =
        if (isFinishedOrFree) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceDim
    val contentColor =
        if (isFinishedOrFree) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    val labelColor =
        if (isFinishedOrFree) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = stringResource(R.string.progreso_del_dia),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = labelColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = remainingLabel,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = contentColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = footerText,
                    style = MaterialTheme.typography.bodySmall,
                    color = labelColor
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(72.dp)
            ) {
                CircularWavyProgressIndicator(
                    progress = { stats.progress },
                    modifier = Modifier.fillMaxSize()
                )
                Text(
                    text = "${(stats.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black
                    ),
                    color = contentColor
                )
            }
        }
    }
}
