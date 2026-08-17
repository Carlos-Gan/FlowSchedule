package com.moca.snapmyschedule.ui.widgets.add_class

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp

@Composable
fun SubjectInformationCard(
    subjectName: String,
    subjectCode: String,
    teacher: String,
    subjectNameHasError: Boolean,
    onSubjectNameChange: (String) -> Unit,
    onSubjectCodeChange: (String) -> Unit,
    onTeacherChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme
                    .surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement =
                Arrangement.spacedBy(16.dp)
        ) {
            SectionHeader(
                title = "Información de la materia",
                description =
                    "Agrega los datos que aparecerán en tu horario."
            )

            OutlinedTextField(
                value = subjectName,
                onValueChange =
                    onSubjectNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Nombre de la materia")
                },
                placeholder = {
                    Text("Ej. Estructura de Datos")
                },
                isError = subjectNameHasError,
                supportingText = if (subjectNameHasError) {
                    {
                        Text(
                            "Escribe el nombre de la materia"
                        )
                    }
                } else {
                    null
                },
                singleLine = true,
                shape =
                    MaterialTheme.shapes.large,
                keyboardOptions = KeyboardOptions(
                    capitalization =
                        KeyboardCapitalization.Words
                )
            )

            OutlinedTextField(
                value = subjectCode,
                onValueChange =
                    onSubjectCodeChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Clave")
                },
                placeholder = {
                    Text("Ej. IF1909")
                },
                supportingText = {
                    Text("Opcional")
                },
                singleLine = true,
                shape =
                    MaterialTheme.shapes.large,
                keyboardOptions = KeyboardOptions(
                    capitalization =
                        KeyboardCapitalization.Characters
                )
            )

            OutlinedTextField(
                value = teacher,
                onValueChange = onTeacherChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Profesor")
                },
                placeholder = {
                    Text("Ej. María González")
                },
                supportingText = {
                    Text("Opcional")
                },
                singleLine = true,
                shape =
                    MaterialTheme.shapes.large,
                keyboardOptions = KeyboardOptions(
                    capitalization =
                        KeyboardCapitalization.Words
                )
            )
        }
    }
}