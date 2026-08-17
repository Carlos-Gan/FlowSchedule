package com.moca.snapmyschedule.ui.widgets.import_schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moca.snapmyschedule.data.ocr.model.ImportedClass
import com.moca.snapmyschedule.data.ocr.formatter.toReadableTitleCase

@Composable
fun ImportedClassesCard(
    title: String,
    classes: List<ImportedClass>,
    warnings: List<String>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style =
                    MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text =
                    "Se detectaron ${classes.size} materias.",
                style =
                    MaterialTheme.typography.bodyMedium,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )

            warnings.forEach { warning ->
                Text(
                    text = "• $warning",
                    color =
                        MaterialTheme.colorScheme.error,
                    style =
                        MaterialTheme.typography.bodySmall
                )
            }

            if (classes.isEmpty()) {
                Text(
                    text =
                        "No se encontraron materias en la imagen.",
                    style =
                        MaterialTheme.typography.bodyMedium,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )
            }

            classes.forEachIndexed { index,
                                     importedClass ->

                ImportedClassItem(
                    number = index + 1,
                    importedClass = importedClass
                )
            }
        }
    }
}

@Composable
private fun ImportedClassItem(
    number: Int,
    importedClass: ImportedClass,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement =
                Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text =
                    "$number. " +
                            importedClass.subjectName
                                .toReadableTitleCase(),
                style =
                    MaterialTheme.typography.titleMedium,
                fontWeight =
                    FontWeight.SemiBold
            )

            if (
                importedClass.subjectCode.isNotBlank()
            ) {
                Text(
                    text =
                        "Clave: ${importedClass.subjectCode}"
                )
            }

            if (
                importedClass.teacher.isNotBlank()
            ) {
                Text(
                    text =
                        "Profesor: " +
                                importedClass.teacher
                                    .toReadableTitleCase()
                )
            }

            if (
                importedClass.group.isNotBlank()
            ) {
                Text(
                    text =
                        "Grupo: ${importedClass.group}"
                )
            }

            if (
                importedClass.credits.isNotBlank()
            ) {
                Text(
                    text =
                        "Créditos: ${importedClass.credits}"
                )
            }

            importedClass.scheduleBlocks
                .forEach { block ->

                    val days =
                        block.days
                            .sortedBy { day ->
                                day.ordinal
                            }
                            .joinToString(
                                separator = ", "
                            ) { day ->
                                day.shortName
                            }

                    Text(
                        text = buildString {
                            append(days)
                            append(": ")
                            append(block.startTime)
                            append("–")
                            append(block.endTime)

                            if (block.room.isNotBlank()) {
                                append(" · ")
                                append(block.room)
                            }
                        },
                        style =
                            MaterialTheme.typography
                                .bodyMedium
                    )
                }

            importedClass.warnings
                .forEach { warning ->
                    Text(
                        text = "• $warning",
                        color =
                            MaterialTheme.colorScheme.error,
                        style =
                            MaterialTheme.typography
                                .bodySmall
                    )
                }
        }
    }
}