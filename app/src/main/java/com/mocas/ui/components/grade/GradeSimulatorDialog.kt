package com.mocas.ui.components.grade

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mocas.R
import com.mocas.data.local.GradeCategoryEntity
import com.mocas.data.local.GradeItemEntity
import com.mocas.data.local.GradeUnitEntity
import com.mocas.data.repository.GradeCalculator
import com.mocas.ui.components.SelectableChip
import kotlin.collections.forEach

@Composable
fun GradeSimulatorDialog(
    categories: List<GradeCategoryEntity>,
    units: List<GradeUnitEntity>,
    items: List<GradeItemEntity>,
    passingText: String,
    onPassingChanged: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedUnit by remember(units) { mutableStateOf(units.firstOrNull()) }
    var selectedCategory by remember(categories) { mutableStateOf(categories.firstOrNull()) }
    var hypothetical by remember { mutableFloatStateOf(85f) }
    val sliderState = remember {
        SliderState(
            value = hypothetical,
            steps = 19,
            trackRange = 0f..100f
        )
    }
    val current = GradeCalculator.summarize(categories, items).currentAverage
    val projected = selectedCategory?.let {
        GradeCalculator.simulate(categories, items, it.id, hypothetical.toDouble())
    }
    val passing = passingText.toDoubleOrNull()?.takeIf { it in 0.0..100.0 } ?: 70.0
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Insights, null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text(stringResource(R.string.simulador_titulo)) },
        text = {
            Column {
                Text(stringResource(R.string.simulador_paso_1), fontWeight = FontWeight.SemiBold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 6.dp)) {
                    units.forEach { unit ->
                        SelectableChip(unit.name, selectedUnit?.id == unit.id) { selectedUnit = unit }
                    }
                }
                Text(stringResource(R.string.simulador_paso_2), fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 14.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 6.dp)) {
                    categories.forEach { category ->
                        SelectableChip(category.name, selectedCategory?.id == category.id) { selectedCategory = category }
                    }
                }
                Text(stringResource(R.string.simulador_paso_3), fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 14.dp))
                Text(format(hypothetical.toDouble()), style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Slider(
                    state = sliderState,
                    onValueChange = { hypothetical = it }
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SimulatorResultCard(stringResource(R.string.actual_label), current, Modifier.weight(1f))
                    SimulatorResultCard(stringResource(R.string.proyectado_label), projected, Modifier.weight(1f), highlighted = true)
                }
                projected?.let {
                    Text(
                        if (it >= passing) stringResource(R.string.meta_alcanzada)
                        else stringResource(R.string.puntos_faltantes_formato, format(passing - it)),
                        color = if (it >= passing) Color(0xFF059669) else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                OutlinedTextField(
                    passingText, onPassingChanged,
                    label = { Text(stringResource(R.string.meta_aprobar_label)) },
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Text(stringResource(R.string.simulacion_no_guarda), style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text(stringResource(R.string.listo)) } }
    )
}