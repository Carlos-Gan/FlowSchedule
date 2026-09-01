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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mocas.data.local.AcademicPeriodEntity
import com.mocas.ui.add.CustomInputField
import com.mocas.ui.components.CalendarDateField
import com.mocas.ui.screens.buildPeriodName
import androidx.compose.material3.MaterialTheme
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
    BaseCard("Detalles Generales") {

        // --- RESTAURADO: Sección de Periodos Guardados ---
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Periodo académico",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant // on-surface-variant
                    )
                    Text(
                        text = "Selecciona uno guardado o define fechas",
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
                                name = buildPeriodName(semesterStart, semesterEnd),
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
                    Text("Guardar periodo", fontSize = 11.sp)
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
                    label = "Inicio periodo", isError = !DateTimeUtils.isValidDate(semesterStart),
                    modifier = Modifier.weight(1f)
                )
                CalendarDateField(
                    value = semesterEnd, onDateSelected = onEndChange,
                    label = "Fin periodo", isError = !DateTimeUtils.isValidDate(semesterEnd),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // --- CAMPOS DE TEXTO CON DISEÑO CORREGIDO (Etiqueta arriba, fondo gris) ---
        CustomInputField(
            label = "Nombre de la Materia *",
            placeholder = "Ej. Cálculo Integral",
            value = name,
            onValueChange = { onNameChange(capitalizeFirstLetter(it)) }
        )

        CustomInputField(
            label = "Código / Abreviatura",
            placeholder = "Ej. MAT-102",
            value = code,
            onValueChange = { onCodeChange(it.uppercase(Locale.ROOT)) }
        )

        CustomInputField(
            label = "Profesor/a",
            placeholder = "Nombre del docente",
            value = professor,
            onValueChange = { onProfessorChange(capitalizeFirstLetter(it)) },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) }
        )

        CustomInputField(
            label = "Aula Principal",
            placeholder = "Ej. Edificio B - Aula 402",
            value = defaultRoom,
            onValueChange = { onRoomChange(capitalizeFirstLetter(it)) },
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) }
        )
    }
}