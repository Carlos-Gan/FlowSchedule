package com.mocas.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.mocas.data.local.AcademicPeriodEntity
import com.mocas.ui.components.CalendarDateField
import com.mocas.ui.components.SubjectColorPicker
import com.mocas.ui.theme.IndigoPrimary
import com.mocas.ui.util.capitalizeFirstLetter
import com.mocas.util.DateTimeUtils

@Composable
fun AcademicPeriodDialog(
    editingPeriod: AcademicPeriodEntity?,
    onDismiss: () -> Unit,
    onSave: (AcademicPeriodEntity) -> Unit
) {
    var name by remember(editingPeriod) {
        mutableStateOf(editingPeriod?.name.orEmpty())
    }
    var startDate by remember(editingPeriod) {
        mutableStateOf(editingPeriod?.startDate ?: DateTimeUtils.todayString())
    }
    var endDate by remember(editingPeriod) {
        mutableStateOf(editingPeriod?.endDate ?: DateTimeUtils.today().plusMonths(4).toString())
    }
    var colorHex by remember(editingPeriod) {
        mutableStateOf(editingPeriod?.colorHex ?: "#10B981")
    }
    val start = DateTimeUtils.parseDate(startDate)
    val end = DateTimeUtils.parseDate(endDate)
    val validDates = start != null && end != null && !end.isBefore(start)

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("academic_period_dialog"),
        title = {
            Text(
                text = if (editingPeriod == null) "Nuevo periodo" else "Editar periodo",
                fontWeight = FontWeight.ExtraBold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = capitalizeFirstLetter(it) },
                    label = { Text("Nombre del periodo") },
                    placeholder = { Text("Ej. Agosto – diciembre 2026") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("period_name_field")
                )
                CalendarDateField(
                    value = startDate,
                    label = "Fecha de inicio",
                    onDateSelected = { startDate = it },
                    isError = start == null,
                    modifier = Modifier.fillMaxWidth()
                )
                CalendarDateField(
                    value = endDate,
                    label = "Fecha de fin",
                    onDateSelected = { endDate = it },
                    isError = !validDates,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(2.dp))
                SubjectColorPicker(
                    selectedHex = colorHex,
                    onColorSelected = { colorHex = it },
                    label = "Color del periodo"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        AcademicPeriodEntity(
                            id = editingPeriod?.id ?: 0,
                            name = name.trim(),
                            startDate = startDate,
                            endDate = endDate,
                            colorHex = colorHex,
                            createdAtMillis = editingPeriod?.createdAtMillis
                                ?: System.currentTimeMillis()
                        )
                    )
                },
                enabled = name.isNotBlank() && validDates,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
            ) {
                Text("Guardar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("Cancelar")
            }
        }
    )
}
