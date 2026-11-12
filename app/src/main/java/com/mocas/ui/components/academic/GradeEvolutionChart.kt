package com.mocas.ui.components.academic

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mocas.util.AcademicGradePoint
import com.mocas.util.formatGrade
import java.util.Locale

@Composable
fun GradeEvolutionChart(points: List<AcademicGradePoint>, maxValue: Double, modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val grid = MaterialTheme.colorScheme.outlineVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    Canvas(modifier) {
        val left = 38.dp.toPx(); val right = 8.dp.toPx(); val top = 24.dp.toPx(); val bottom = 38.dp.toPx()
        val chartWidth = size.width - left - right; val chartHeight = size.height - top - bottom
        repeat(5) { i ->
            val y = top + chartHeight * i / 4
            drawLine(grid, Offset(left, y), Offset(size.width - right, y), 1.dp.toPx())
            drawContext.canvas.nativeCanvas.drawText(String.format(Locale.getDefault(), "%.1f", maxValue * (4 - i) / 4), 0f, y + 4.dp.toPx(), android.graphics.Paint().apply { color = labelColor.toArgb(); textSize = 11.sp.toPx() })
        }
        val xs = points.indices.map { if (points.size == 1) left + chartWidth / 2 else left + chartWidth * it / (points.size - 1) }
        val ys = points.map { top + chartHeight * (1f - (it.value / maxValue).toFloat().coerceIn(0f, 1f)) }
        if (points.size > 1) {
            val path = Path().apply { moveTo(xs.first(), ys.first()); for (i in 1 until xs.size) lineTo(xs[i], ys[i]) }
            drawPath(path, primary, style = Stroke(width = 3.dp.toPx()))
        }
        points.indices.forEach { i ->
            drawCircle(surface, 5.dp.toPx(), Offset(xs[i], ys[i])); drawCircle(primary, 3.dp.toPx(), Offset(xs[i], ys[i]))
            val paint = android.graphics.Paint().apply { color = primary.toArgb(); textSize = 12.sp.toPx(); textAlign = android.graphics.Paint.Align.CENTER; typeface = android.graphics.Typeface.DEFAULT_BOLD }
            drawContext.canvas.nativeCanvas.drawText(formatGrade(points[i].value), xs[i], ys[i] - 10.dp.toPx(), paint)
            val labelPaint = android.graphics.Paint().apply { color = labelColor.toArgb(); textSize = 10.sp.toPx(); textAlign = android.graphics.Paint.Align.CENTER }
            drawContext.canvas.nativeCanvas.drawText(points[i].label.take(9), xs[i], size.height - 8.dp.toPx(), labelPaint)
        }
    }
}