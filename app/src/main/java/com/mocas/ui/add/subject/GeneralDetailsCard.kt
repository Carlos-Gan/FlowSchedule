package com.mocas.ui.add.subject

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mocas.R
import com.mocas.data.local.AcademicPeriodEntity
import com.mocas.ui.add.CustomInputField
import com.mocas.ui.components.CalendarDateField
import com.mocas.ui.screens.buildPeriodName
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalContext
import com.mocas.ui.util.capitalizeFirstLetter
import com.mocas.util.DateTimeUtils
import java.util.Locale

@Composable
fun GeneralDetailsCard(
    name: String, onNameChange: (String) -> Unit,
    code: String, onCodeChange: (String) -> Unit,
    professor: String, onProfessorChange: (String) -> Unit,
    defaultRoom: String, onRoomChange: (String) -> Unit,
    semesterStart: String, onStartChange: (String) -> Unit,
    semesterEnd: String, onEndChange: (String) -> Unit,
    academicPeriods: List<AcademicPeriodEntity>,
    onSavePeriod: (AcademicPeriodEntity) -> Unit
) {
    val context = LocalContext.current
    BaseCard(stringResource(R.string.detalles_generales_titulo)) {

        // --- RESTAURADO: Sección de Periodos Guardados ---
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.periodo_academico_default),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant // on-surface-variant
                    )
                    Text(
                        text = stringResource(R.string.selecciona_periodo_instruccion),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                val canSavePeriod = DateTimeUtils.isValidDate(semesterStart) &&
                        DateTimeUtils.parseDate(semesterEnd)?.let { end ->
                            DateTimeUtils.parseDate(semesterStart)?.let { start ->
                                !end.isBefore(start)
                            } ?: false
                        } ?: false

                OutlinedButton(
                    onClick = {
                        onSavePeriod(
                            AcademicPeriodEntity(
                                name = buildPeriodName(context, semesterStart, semesterEnd),
                                startDate = semesterStart,
                                endDate = semesterEnd
                            )
                        )
                    },
                    enabled = canSavePeriod,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(stringResource(R.string.guardar_periodo_boton), fontSize = 11.sp)
                }
            }

            if (academicPeriods.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(academicPeriods, key = { it.id }) { period ->
                        val selected = period.startDate == semesterStart && period.endDate == semesterEnd
                        Surface(
                            modifier = Modifier.clickable {
                                onStartChange(period.startDate)
                                onEndChange(period.endDate)
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(
                                1.dp,
                                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Text(
                                text = period.name,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fechas de inicio y fin
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CalendarDateField(
                    value = semesterStart, onDateSelected = onStartChange,
                    label = stringResource(R.string.inicio_periodo_label), isError = !DateTimeUtils.isValidDate(semesterStart),
                    modifier = Modifier.weight(1f)
                )
                CalendarDateField(
                    value = semesterEnd, onDateSelected = onEndChange,
                    label = stringResource(R.string.fin_periodo_label), isError = !DateTimeUtils.isValidDate(semesterEnd),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // --- CAMPOS DE TEXTO CON DISEÑO CORREGIDO (Etiqueta arriba, fondo gris) ---
        CustomInputField(
            label = stringResource(R.string.nombre_materia_label),
            placeholder = stringResource(R.string.nombre_materia_placeholder),
            value = name,
            onValueChange = { onNameChange(capitalizeFirstLetter(it)) }
        )

        CustomInputField(
            label = stringResource(R.string.codigo_label),
            placeholder = stringResource(R.string.codigo_placeholder),
            value = code,
            onValueChange = { onCodeChange(it.uppercase(Locale.ROOT)) }
        )

        CustomInputField(
            label = stringResource(R.string.profesor_label),
            placeholder = stringResource(R.string.profesor_placeholder),
            value = professor,
            onValueChange = { onProfessorChange(capitalizeFirstLetter(it)) },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) }
        )

        CustomInputField(
            label = stringResource(R.string.aula_principal_label),
            placeholder = stringResource(R.string.aula_principal_placeholder),
            value = defaultRoom,
            onValueChange = { onRoomChange(capitalizeFirstLetter(it)) },
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) }
        )
    }
}
