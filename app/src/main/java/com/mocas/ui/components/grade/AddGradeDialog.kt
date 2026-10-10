package com.mocas.ui.components.grade

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mocas.R
import com.mocas.data.local.GradeCategoryEntity
import com.mocas.data.local.GradeUnitEntity
import com.mocas.ui.components.SelectableChip
import kotlin.collections.forEach

@Composable
fun AddGradeDialog(
    categories: List<GradeCategoryEntity>, unit: GradeUnitEntity, onDismiss: () -> Unit,
    onConfirm: (GradeCategoryEntity, GradeUnitEntity, String, Double) -> Unit
) {
    var selectedCategory by remember(categories) { mutableStateOf(categories.firstOrNull()) }
    var name by remember { mutableStateOf("") }
    var score by remember { mutableStateOf("") }
    val number = score.toDoubleOrNull()
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.registrar_calificacion)) }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.categoria), fontWeight = FontWeight.SemiBold)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { category ->
                    SelectableChip(
                        "${category.name} (${format(category.weightPercent)}%)",
                        selectedCategory?.id == category.id
                    ) { selectedCategory = category }
                }
            }
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.se_guardara_en_formato, unit.name), modifier = Modifier.padding(10.dp), fontWeight = FontWeight.SemiBold)
            }
            OutlinedTextField(
                name,
                { name = it },
                label = { Text(stringResource(R.string.actividad_evaluacion_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )
            OutlinedTextField(
                score,
                { score = it },
                label = { Text(stringResource(R.string.calificacion_rango_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }
    }, confirmButton = {
        Button(
            onClick = {
                val category = selectedCategory ?: return@Button
                val value = number ?: return@Button
                onConfirm(category, unit, name.trim(), value)
            },
            enabled = selectedCategory != null && name.isNotBlank() && number != null && number in 0.0..100.0
        ) { Text(stringResource(R.string.guardar)) }
    }, dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancelar)) } })
}