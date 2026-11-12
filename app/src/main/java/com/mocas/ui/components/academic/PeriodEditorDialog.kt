package com.mocas.ui.components.academic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mocas.R
import com.mocas.data.local.AcademicPeriodEntity
import com.mocas.ui.add.event.buildPeriodName
import com.mocas.ui.components.CalendarDateField
import com.mocas.util.DateTimeUtils
import java.time.LocalDate

@Composable
fun PeriodEditorDialog(
    period: AcademicPeriodEntity?,
    onDismiss: () -> Unit,
    onSave: (AcademicPeriodEntity) -> Unit
) {
    val context = LocalContext.current
    val today = LocalDate.now()
    var startDate by remember(period?.id) { mutableStateOf(period?.startDate ?: today.toString()) }
    var endDate by remember(period?.id) { mutableStateOf(period?.endDate ?: today.plusMonths(4).toString()) }

    val initialName = remember(period?.id) {
        if (period != null) {
            period.name
        } else {
            buildPeriodName(context, startDate, endDate)
        }
    }

    var name by remember(period?.id) { mutableStateOf(initialName) }
    var isManuallyEdited by remember(period?.id) { mutableStateOf(period != null) }

    val validDates = DateTimeUtils.isValidDate(startDate) && DateTimeUtils.isValidDate(endDate) &&
            !LocalDate.parse(endDate).isBefore(LocalDate.parse(startDate))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (period == null) R.string.nuevo_periodo_titulo else R.string.editar_periodo_titulo)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        isManuallyEdited = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.academic_periods_name)) },
                    singleLine = true
                )
                CalendarDateField(
                    value = startDate,
                    label = stringResource(R.string.inicio_periodo_label),
                    onDateSelected = { newStart ->
                        startDate = newStart
                        if (!isManuallyEdited && DateTimeUtils.isValidDate(newStart) && DateTimeUtils.isValidDate(endDate)) {
                            name = buildPeriodName(context, newStart, endDate)
                        }
                    },
                    isError = !DateTimeUtils.isValidDate(startDate)
                )
                CalendarDateField(
                    value = endDate,
                    label = stringResource(R.string.fin_periodo_label),
                    onDateSelected = { newEnd ->
                        endDate = newEnd
                        if (!isManuallyEdited && DateTimeUtils.isValidDate(startDate) && DateTimeUtils.isValidDate(newEnd)) {
                            name = buildPeriodName(context, startDate, newEnd)
                        }
                    },
                    isError = !DateTimeUtils.isValidDate(endDate) || !validDates
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        AcademicPeriodEntity(
                            id = period?.id ?: 0,
                            name = name.trim(),
                            startDate = startDate,
                            endDate = endDate,
                            colorHex = period?.colorHex ?: "#10B981",
                            createdAtMillis = period?.createdAtMillis ?: System.currentTimeMillis()
                        )
                    )
                },
                enabled = name.isNotBlank() && validDates
            ) { Text(stringResource(R.string.guardar_periodo_boton)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancelar_boton)) }
        }
    )
}