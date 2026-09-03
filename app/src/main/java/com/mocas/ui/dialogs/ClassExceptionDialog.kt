package com.mocas.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import com.mocas.R
import com.mocas.data.local.ClassExceptionEntity
import com.mocas.data.local.ClassExceptionType
import com.mocas.ui.components.ClockTimeField
import com.mocas.ui.model.ClassOccurrenceInfo
import com.mocas.ui.util.capitalizeFirstLetter
import com.mocas.util.DateTimeUtils

@Composable
fun ClassExceptionDialog(
    occurrence: ClassOccurrenceInfo,
    onDismiss: () -> Unit,
    onSave: (ClassExceptionEntity) -> Unit,
    onRestore: ((Long) -> Unit)?
) {
    val saved = occurrence.exception
    var type by remember(saved) { mutableStateOf(saved?.type ?: ClassExceptionType.CANCELED) }
    var startTime by remember(saved) { mutableStateOf(saved?.newStartTime ?: occurrence.slot.startTime) }
    var endTime by remember(saved) { mutableStateOf(saved?.newEndTime ?: occurrence.slot.endTime) }
    var room by remember(saved) {
        mutableStateOf(saved?.newRoom ?: occurrence.slot.room.ifBlank { occurrence.subject.defaultRoom })
    }
    var note by remember(saved) { mutableStateOf(saved?.note.orEmpty()) }
    val validTime = type == ClassExceptionType.CANCELED ||
        DateTimeUtils.endIsAfterStart(startTime, endTime)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.cambiar_esta_clase), fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "${occurrence.subject.name} · ${DateTimeUtils.formatDate(occurrence.date, true)}",
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == ClassExceptionType.CANCELED,
                        onClick = { type = ClassExceptionType.CANCELED },
                        label = { Text(stringResource(R.string.cancelar_clase)) }
                    )
                    FilterChip(
                        selected = type == ClassExceptionType.MODIFIED,
                        onClick = { type = ClassExceptionType.MODIFIED },
                        label = { Text(stringResource(R.string.cambiar)) }
                    )
                }
                if (type == ClassExceptionType.MODIFIED) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ClockTimeField(
                            value = startTime,
                            label = stringResource(R.string.inicio_label),
                            onTimeSelected = { selected ->
                                startTime = selected
                                // Siempre sumamos una hora al cambiar el inicio para evitar errores de validación
                                endTime = DateTimeUtils.getEndTime(selected)
                            },
                            modifier = Modifier.weight(1f)
                        )
                        ClockTimeField(
                            value = endTime,
                            label = stringResource(R.string.fin_label),
                            onTimeSelected = { endTime = it },
                            isError = !validTime,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = room,
                        onValueChange = { room = it.uppercase() },
                        label = { Text(stringResource(R.string.aula_fecha_label)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = capitalizeFirstLetter(it) },
                    label = { Text(stringResource(R.string.nota_opcional)) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        ClassExceptionEntity(
                            id = saved?.id ?: 0,
                            subjectId = occurrence.subject.id,
                            slotId = occurrence.slot.id,
                            date = occurrence.date,
                            type = type,
                            newStartTime = startTime.takeIf { type == ClassExceptionType.MODIFIED },
                            newEndTime = endTime.takeIf { type == ClassExceptionType.MODIFIED },
                            newRoom = room.trim().takeIf { type == ClassExceptionType.MODIFIED },
                            note = note.trim(),
                            createdAtMillis = saved?.createdAtMillis ?: System.currentTimeMillis()
                        )
                    )
                },
                enabled = validTime,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (type == ClassExceptionType.CANCELED) {
                        MaterialTheme.colorScheme.error
                    } else MaterialTheme.colorScheme.primary
                )
            ) { Text(stringResource(R.string.guardar), fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (saved != null && onRestore != null) {
                    OutlinedButton(onClick = { onRestore(saved.id) }) { Text(stringResource(R.string.restaurar)) }
                }
                OutlinedButton(onClick = onDismiss) { Text(stringResource(R.string.cerrar)) }
            }
        }
    )
}
