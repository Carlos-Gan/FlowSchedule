package com.mocas.ui.components.schedule

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mocas.R

@Composable
fun AvailabilityGrid(common: BooleanArray, dayNames: List<String>) {
    val colors = MaterialTheme.colorScheme
    val available = colors.primary
    val partly = colors.tertiaryContainer
    val occupied = colors.surfaceVariant
    Card(colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow), shape = RoundedCornerShape(22.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(Modifier.width(42.dp))
                dayNames.forEach { day ->
                    Text(day, modifier = Modifier.weight(1f), color = colors.onSurfaceVariant, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.width(42.dp)) {
                    (7..22).forEach { hour ->
                        Text("%02d:00".format(hour), modifier = Modifier.height(19.dp), color = colors.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    }
                    Text("23:00", modifier = Modifier.padding(top = 2.dp), color = colors.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                }
                Canvas(modifier = Modifier.weight(1f).height(304.dp)) {
                    val cellWidth = size.width / 7f
                    val cellHeight = size.height / 16f
                    val cellInset = 2.dp.toPx()
                    for (day in 0..6) for (row in 0..15) {
                        val start = day * 48 + 14 + row * 2
                        val freeCount = (if (common[start]) 1 else 0) + (if (common[start + 1]) 1 else 0)
                        val color = when (freeCount) { 2 -> available; 1 -> partly; else -> occupied }
                        drawRoundRect(
                            color,
                            topLeft = androidx.compose.ui.geometry.Offset(day * cellWidth + cellInset, row * cellHeight + cellInset),
                            size = androidx.compose.ui.geometry.Size(cellWidth - cellInset * 2, cellHeight - cellInset * 2),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GridLegend(available, stringResource(R.string.compare_legend_free))
                GridLegend(partly, stringResource(R.string.compare_legend_partial))
                GridLegend(occupied, stringResource(R.string.compare_legend_busy))
            }
            Text(stringResource(R.string.compare_day_bounds), color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}