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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
    stats: DailyClassStats,
    isVacation: Boolean = false,
    modifier: Modifier = Modifier,
    showTitle: Boolean = true
) {
    val dayHash = remember { java.time.LocalDate.now().toEpochDay().toInt() }
    val phraseIndex = kotlin.math.abs(dayHash) % 4

    val vacationPhrases = listOf(
        stringResource(R.string.motivacion_vacaciones_1),
        stringResource(R.string.motivacion_vacaciones_2),
        stringResource(R.string.motivacion_vacaciones_3),
        stringResource(R.string.motivacion_vacaciones_4)
    )
    val freeDayPhrases = listOf(
        stringResource(R.string.motivacion_dia_libre_1),
        stringResource(R.string.motivacion_dia_libre_2),
        stringResource(R.string.motivacion_dia_libre_3),
        stringResource(R.string.motivacion_dia_libre_4)
    )
    val finishedPhrases = listOf(
        stringResource(R.string.motivacion_terminado_1),
        stringResource(R.string.motivacion_terminado_2),
        stringResource(R.string.motivacion_terminado_3),
        stringResource(R.string.motivacion_terminado_4)
    )

    val todayDate = remember { java.time.LocalDate.now() }
    val month = todayDate.monthValue
    val day = todayDate.dayOfMonth

    val holidayPhrase = when {
        month == 1 && day == 1 -> stringResource(R.string.festivo_ano_nuevo)
        month == 2 && day == 14 -> stringResource(R.string.festivo_san_valentin)
        month == 10 && day == 31 -> stringResource(R.string.festivo_halloween)
        month == 11 && (day == 1 || day == 2) -> stringResource(R.string.festivo_dia_muertos)
        month == 12 && day == 25 -> stringResource(R.string.festivo_navidad)
        month == 12 && day == 31 -> stringResource(R.string.festivo_fin_ano)
        else -> null
    }

    val isHoliday = holidayPhrase != null
    val isFreeDay = stats.totalHours == 0.0 && !isVacation && !isHoliday
    val isFinished = stats.totalHours > 0.0 && stats.progress >= 1f
    val isFinishedOrFree = isHoliday || isVacation || isFreeDay || isFinished
    val displayedProgress = if (isFinishedOrFree) 1f else stats.progress

    val remainingLabel = when {
        holidayPhrase != null -> holidayPhrase
        isVacation -> vacationPhrases[phraseIndex % vacationPhrases.size]
        isFreeDay -> freeDayPhrases[phraseIndex % freeDayPhrases.size]
        isFinished -> finishedPhrases[phraseIndex % finishedPhrases.size]
        stats.remainingHours > 0 -> {
            val wholeHours = stats.remainingHours.toInt()
            val minutes = ((stats.remainingHours - wholeHours) * 60).toInt()
            if (wholeHours > 0) stringResource(R.string.horas_restan_formato, wholeHours, minutes) 
            else stringResource(R.string.minutos_restan_formato, minutes)
        }
        else -> stringResource(R.string.dia_terminado)
    }

    val footerText = when {
        holidayPhrase != null -> stringResource(R.string.dia_festivo_etiqueta)
        isVacation -> stringResource(R.string.dia_vacaciones)
        isFreeDay -> stringResource(R.string.sin_clases_hoy)
        else -> stringResource(R.string.total_hoy_formato, String.format(Locale.US, "%.1f", stats.totalHours))
    }

    val backgroundColor =
        if (isFinishedOrFree) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceDim
    val contentColor =
        if (isFinishedOrFree) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    val labelColor =
        if (isFinishedOrFree) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth()
                .then(if (!showTitle) Modifier.height(114.dp) else Modifier),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                if (showTitle) {
                    Text(
                        text = stringResource(R.string.progreso_del_dia),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = labelColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

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
                    progress = { displayedProgress },
                    modifier = Modifier.fillMaxSize()
                )
                if (isFreeDay) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.dia_libre),
                            tint = contentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "100%",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                            color = contentColor
                        )
                    }
                } else {
                    Text(
                        text = "${(displayedProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black
                        ),
                        color = contentColor
                    )
                }
            }
        }
    }
}
