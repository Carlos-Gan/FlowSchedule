package com.mocas.ui.components.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.mocas.R

@Composable
fun DashboardAcademicSummaryCard(
    average: String?,
    remainingDays: Int?,
    periodName: String?,
    onAverageClick: () -> Unit,
    onPeriodClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                DashboardSummaryMetric(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(role = Role.Button, onClick = onAverageClick)
                        .padding(horizontal = 6.dp),
                    icon = Icons.Default.School,
                    value = average ?: stringResource(R.string.academic_no_data_short),
                    label = stringResource(R.string.academic_average)
                )
                DashboardSummaryMetric(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(role = Role.Button, onClick = onPeriodClick)
                        .padding(horizontal = 6.dp),
                    icon = Icons.Default.HourglassBottom,
                    value = remainingDays?.toString() ?: stringResource(R.string.academic_no_data_short),
                    label = stringResource(R.string.dashboard_days_remaining),
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(1.dp)
                    .height(34.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
        }
    }
}