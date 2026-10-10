package com.mocas.ui.components.grade

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mocas.R
import com.mocas.data.local.GradeCategoryEntity
import com.mocas.data.local.GradeUnitCategoryWeightEntity
import com.mocas.data.local.GradeUnitEntity
import kotlin.collections.forEach
import kotlin.collections.set
import kotlin.text.toDoubleOrNull

@Composable
fun UnitCategoryWeightsDialog(
    unit: GradeUnitEntity,
    generalCategories: List<GradeCategoryEntity>,
    currentUnitWeights: List<GradeUnitCategoryWeightEntity>,
    onDismiss: () -> Unit,
    onSave: (List<GradeUnitCategoryWeightEntity>) -> Unit,
    onResetToGeneral: () -> Unit
) {
    val isInitiallyCustomized = currentUnitWeights.any { it.unitId == unit.id }
    var useCustom by remember { mutableStateOf(isInitiallyCustomized) }

    val weightValues = remember(unit.id, generalCategories, currentUnitWeights) {
        val initialMap = mutableStateMapOf<Long, String>()
        generalCategories.forEach { category ->
            val custom = currentUnitWeights.firstOrNull { it.unitId == unit.id && it.categoryId == category.id }
            val weight = custom?.weightPercent ?: category.weightPercent
            initialMap[category.id] = if (weight % 1.0 == 0.0) weight.toInt().toString() else format(weight)
        }
        initialMap
    }

    val totalAssigned = weightValues.values.sumOf { it.toDoubleOrNull() ?: 0.0 }
    val isValid = weightValues.values.all { val d = it.toDoubleOrNull(); d != null && d >= 0.0 } && totalAssigned <= 100.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(stringResource(R.string.ponderacion_de_formato, unit.name), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    stringResource(R.string.ajusta_porcentajes_instruccion),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    onClick = { useCustom = false },
                    shape = RoundedCornerShape(12.dp),
                    color = if (!useCustom) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = if (!useCustom) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = !useCustom, onClick = { useCustom = false })
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(stringResource(R.string.porcentajes_generales_radio), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            Text(stringResource(R.string.porcentajes_generales_default), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Surface(
                    onClick = { useCustom = true },
                    shape = RoundedCornerShape(12.dp),
                    color = if (useCustom) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = if (useCustom) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = useCustom, onClick = { useCustom = true })
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(stringResource(R.string.personalizar_para_formato, unit.name), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            Text(stringResource(R.string.aplica_porcentajes_unidad_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                if (useCustom) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.categoria), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text(
                            stringResource(R.string.total_porcentaje_formato, format(totalAssigned)),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (totalAssigned > 100.0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }

                    if (generalCategories.isEmpty()) {
                        Text(
                            stringResource(R.string.sin_categorias_materia),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        generalCategories.forEach { category ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(category.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                    Text(stringResource(R.string.general_porcentaje_formato, format(category.weightPercent)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                OutlinedTextField(
                                    value = weightValues[category.id] ?: "",
                                    onValueChange = { input -> weightValues[category.id] = input },
                                    label = { Text("%") },
                                    singleLine = true,
                                    modifier = Modifier.width(90.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!useCustom) {
                        onResetToGeneral()
                    } else {
                        val weights = generalCategories.map { category ->
                            val percent = weightValues[category.id]?.toDoubleOrNull() ?: category.weightPercent
                            GradeUnitCategoryWeightEntity(
                                unitId = unit.id,
                                categoryId = category.id,
                                weightPercent = percent
                            )
                        }
                        onSave(weights)
                    }
                    onDismiss()
                },
                enabled = !useCustom || (isValid && generalCategories.isNotEmpty())
            ) {
                Text(stringResource(R.string.guardar))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancelar))
            }
        }
    )
}