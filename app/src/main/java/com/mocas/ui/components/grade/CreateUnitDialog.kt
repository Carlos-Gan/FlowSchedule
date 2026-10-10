package com.mocas.ui.components.grade

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.mocas.R
import com.mocas.data.local.GradeUnitEntity

@Composable
fun CreateUnitDialog(
    subjectId: Long,
    unitCount: Int,
    onDismiss: () -> Unit,
    onConfirm: (GradeUnitEntity) -> Unit
) {
    var name by remember { mutableStateOf("Unidad ${unitCount + 1}") }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.CreateNewFolder, null) },
        title = { Text(stringResource(R.string.nueva_unidad)) },
        text = {
            Column {
                Text(stringResource(R.string.mensaje_crear_vacia), style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.nombre_unidad_label)) },
                    placeholder = { Text(stringResource(R.string.unidad_ejemplo)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(GradeUnitEntity(subjectId = subjectId, name = name.trim(), sortOrder = unitCount)) },
                enabled = name.isNotBlank()
            ) { Text(stringResource(R.string.crear_unidad_boton)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancelar)) } }
    )
}
