package com.mocas.ui.components.grade

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mocas.R
import com.mocas.data.local.GradeCategoryEntity
import kotlin.collections.forEach

@Composable
fun CategoryManagerDialog(
    subjectId: Long, categories: List<GradeCategoryEntity>, onAdd: (GradeCategoryEntity) -> Unit,
    onDelete: (GradeCategoryEntity) -> Unit, onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    val number = weight.toDoubleOrNull()
    val assigned = categories.sumOf { it.weightPercent }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.porcentajes_generales_materia)) },
        text = {
            Column {
                Text(
                    stringResource(R.string.asignado_porcentaje_formato, format(assigned)),
                    style = MaterialTheme.typography.bodySmall
                )
                categories.forEach { category ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            category.name,
                            Modifier.weight(1f)
                        ); Text("${format(category.weightPercent)}%")
                        IconButton(onClick = { onDelete(category) }) {
                            Icon(
                                Icons.Default.Delete,
                                stringResource(R.string.eliminar_categoria_desc)
                            )
                        }
                    }
                }
                OutlinedTextField(
                    name,
                    { name = it },
                    label = { Text(stringResource(R.string.nueva_categoria)) },
                    singleLine = true
                )
                OutlinedTextField(
                    weight,
                    { weight = it },
                    label = { Text(stringResource(R.string.porcentaje_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val value = number ?: return@Button
                    onAdd(
                        GradeCategoryEntity(
                            subjectId = subjectId,
                            name = name.trim(),
                            weightPercent = value,
                            sortOrder = categories.size
                        )
                    )
                    name = ""; weight = ""
                },
                enabled = name.isNotBlank() && number != null && number > 0 && assigned + number <= 100
            ) { Text(stringResource(R.string.agregar)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.listo)) } })
}