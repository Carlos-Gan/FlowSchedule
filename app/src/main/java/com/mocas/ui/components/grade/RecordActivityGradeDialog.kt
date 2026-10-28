package com.mocas.ui.components.grade

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mocas.R
import com.mocas.data.local.GradeCategoryEntity
import com.mocas.data.local.GradeUnitEntity
import com.mocas.data.local.SchoolEventWithSubject

@Composable
fun RecordActivityGradeDialog(
    event: SchoolEventWithSubject,
    unit: GradeUnitEntity?,
    categories: List<GradeCategoryEntity>,
    onDismiss: () -> Unit,
    onMarkGradePending: () -> Unit,
    onSave: (categoryId: Long, score: Double) -> Unit
) {
    var selectedCategoryId by remember(categories) { mutableStateOf(categories.firstOrNull()?.id) }
    var scoreText by remember { mutableStateOf("") }
    var categoriesExpanded by remember { mutableStateOf(false) }
    val score = scoreText.replace(',', '.').toDoubleOrNull()
    val canSave = selectedCategoryId != null && score != null && score in 0.0..100.0
    val selectedCategory = categories.firstOrNull { it.id == selectedCategoryId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.registrar_calificacion)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(event.event.title, fontWeight = FontWeight.Bold)
                Text(
                    "${event.subject?.name.orEmpty()} · ${unit?.name ?: stringResource(R.string.unidad_sin_definir)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )
                if (categories.isEmpty()) {
                    Text(stringResource(R.string.crear_categoria_calificacion_instruccion))
                } else {
                    ExposedDropdownMenuBox(
                        expanded = categoriesExpanded,
                        onExpandedChange = { categoriesExpanded = !categoriesExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            onClick = { categoriesExpanded = true },
                            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                selectedCategory?.name ?: stringResource(R.string.selecciona_categoria),
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        ExposedDropdownMenu(
                            expanded = categoriesExpanded,
                            onDismissRequest = { categoriesExpanded = false }
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        selectedCategoryId = category.id
                                        categoriesExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = scoreText,
                        onValueChange = { scoreText = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        label = { Text(stringResource(R.string.calificacion)) },
                        supportingText = { Text(stringResource(R.string.ingresa_valor_0_100)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = scoreText.isNotBlank() && (score == null || score !in 0.0..100.0)
                    )
                }
                TextButton(
                    onClick = onMarkGradePending,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(stringResource(R.string.grade_pending_action))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(requireNotNull(selectedCategoryId), requireNotNull(score)) },
                enabled = canSave
            ) { Text(stringResource(R.string.guardar_y_completar)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancelar)) } }
    )
}
