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
import com.moca.snapmyschedule.data.ocr.model.DetectedDayColumn
import kotlin.collections.forEach
import kotlin.math.roundToInt

@Composable
fun DetectedColumnsCard(
    columns: List<DetectedDayColumn>,
    modifier: Modifier = Modifier
) {
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
                text = "Columnas detectadas",
                style =
                    MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (columns.isEmpty()) {
                Text(
                    text =
                        "No se encontraron encabezados " +
                                "de días en una misma fila.",
                    style =
                        MaterialTheme.typography.bodyMedium,
                    color =
                        MaterialTheme.colorScheme.error
                )
            } else {
                Text(
                    text =
                        "Se detectaron ${columns.size} columnas.",
                    style =
                        MaterialTheme.typography.bodyMedium,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )

                columns.forEach { column ->

                    val leftPercent =
                        (
                                column.leftBoundary * 100
                                ).roundToInt()

                    val rightPercent =
                        (
                                column.rightBoundary * 100
                                ).roundToInt()

                    Card(
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier =
                                Modifier.padding(12.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text =
                                    column.day.displayName,
                                style =
                                    MaterialTheme.typography
                                        .titleMedium,
                                fontWeight =
                                    FontWeight.SemiBold
                            )

                            Text(
                                text =
                                    "Encabezado OCR: " +
                                            column.headerText,
                                style =
                                    MaterialTheme.typography
                                        .bodySmall
                            )

                            Text(
                                text =
                                    "Área horizontal: " +
                                            "$leftPercent% – " +
                                            "$rightPercent%",
                                style =
                                    MaterialTheme.typography
                                        .bodySmall,
                                color =
                                    MaterialTheme.colorScheme
                                        .onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

