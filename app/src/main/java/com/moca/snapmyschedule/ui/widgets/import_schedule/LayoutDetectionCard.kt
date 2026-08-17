package com.moca.snapmyschedule.ui.widgets.import_schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moca.snapmyschedule.data.ocr.parser.LayoutDetectionResult
import com.moca.snapmyschedule.data.ocr.parser.ScheduleLayoutType
import kotlin.math.roundToInt

@Composable
fun LayoutDetectionCard(
    detection: LayoutDetectionResult,
    modifier: Modifier = Modifier
) {
    val layoutName = when (detection.type) {
        ScheduleLayoutType.GRID ->
            "Cuadrícula visual"

        ScheduleLayoutType.TABLE ->
            "Tabla por materias"

        ScheduleLayoutType.UNKNOWN ->
            "Estructura desconocida"
    }

    val description = when (detection.type) {
        ScheduleLayoutType.GRID ->
            "Los días forman columnas y las horas " +
                    "forman filas."

        ScheduleLayoutType.TABLE ->
            "Cada fila representa una materia y " +
                    "cada día es una columna."

        ScheduleLayoutType.UNKNOWN ->
            "La estructura no pudo determinarse " +
                    "automáticamente."
    }

    val confidencePercent =
        (detection.confidence * 100)
            .roundToInt()

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme
                    .surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Tipo de horario",
                style =
                    MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = layoutName,
                style =
                    MaterialTheme.typography.titleMedium,
                color =
                    MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = description,
                style =
                    MaterialTheme.typography.bodyMedium,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )

            Text(
                text = "Confianza: $confidencePercent%",
                style =
                    MaterialTheme.typography.bodyMedium
            )

            Text(
                text =
                    "Días detectados: " +
                            detection.detectedDays,
                style =
                    MaterialTheme.typography.bodyMedium
            )

            detection.evidence.forEach { evidence ->
                Text(
                    text = "• $evidence",
                    style =
                        MaterialTheme.typography.bodySmall,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )
            }
        }
    }
}
