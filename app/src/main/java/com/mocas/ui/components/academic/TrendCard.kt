package com.mocas.ui.components.academic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mocas.R
import com.mocas.util.AcademicGradePoint

@Composable
fun TrendCard(points: List<AcademicGradePoint>, gradeLabel: String, useGpa: Boolean) {
    val maxValue = if (useGpa) 4.0 else if (points.maxOfOrNull { it.value }?.let { it > 10 } == true) 100.0 else 10.0
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.academic_chart_title, gradeLabel), fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            GradeEvolutionChart(points, maxValue, Modifier.fillMaxWidth().height(190.dp))
            Text(if (points.size == 1) stringResource(R.string.academic_one_month_note) else stringResource(R.string.academic_chart_note), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}