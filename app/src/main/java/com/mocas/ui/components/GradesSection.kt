package com.mocas.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mocas.R
import com.mocas.data.local.GradeCategoryEntity
import com.mocas.data.local.GradeItemEntity
import com.mocas.data.local.GradeUnitCategoryWeightEntity
import com.mocas.data.local.GradeUnitEntity
import com.mocas.data.repository.GradeCalculator
import com.mocas.ui.components.grade.GradesWorkspaceDialog
import com.mocas.ui.components.grade.format

@Composable
fun GradesSection(
    subjectId: Long,
    categories: List<GradeCategoryEntity>,
    gradeItems: List<GradeItemEntity>,
    units: List<GradeUnitEntity>,
    unitCategoryWeights: List<GradeUnitCategoryWeightEntity> = emptyList(),
    onAddCategory: (GradeCategoryEntity) -> Unit,
    onAddItem: (GradeItemEntity) -> Unit,
    onDeleteCategory: (GradeCategoryEntity) -> Unit,
    onDeleteItem: (GradeItemEntity) -> Unit,
    onAddUnit: (GradeUnitEntity) -> Unit,
    onDeleteUnit: (GradeUnitEntity) -> Unit,
    onSaveUnitCategoryWeights: (Long, List<GradeUnitCategoryWeightEntity>) -> Unit = { _, _ -> },
    onResetUnitCategoryWeights: (Long) -> Unit = {}
) {
    var open by remember { mutableStateOf(false) }
    val currentAverage = remember(categories, gradeItems, units, unitCategoryWeights) {
        GradeCalculator.subjectAverage(subjectId, categories, gradeItems, units, unitCategoryWeights)
    }
    Card(
        onClick = { open = true },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.BarChart,
                        null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Column(Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)) {
                Text(
                    stringResource(R.string.calificaciones),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    currentAverage?.let { stringResource(R.string.promedio_evaluaciones_formato, format(it), gradeItems.size) }
                        ?: stringResource(R.string.configura_porcentajes_instruccion),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, stringResource(R.string.abrir_calificaciones_desc), tint = MaterialTheme.colorScheme.primary)
        }
    }
    if (open) {
        GradesWorkspaceDialog(
            subjectId = subjectId,
            categories = categories,
            gradeItems = gradeItems,
            units = units,
            unitCategoryWeights = unitCategoryWeights,
            onDismiss = { open = false },
            onAddCategory = onAddCategory,
            onAddItem = onAddItem,
            onDeleteCategory = onDeleteCategory,
            onDeleteItem = onDeleteItem,
            onAddUnit = onAddUnit,
            onDeleteUnit = onDeleteUnit,
            onSaveUnitCategoryWeights = onSaveUnitCategoryWeights,
            onResetUnitCategoryWeights = onResetUnitCategoryWeights
        )
    }
}

@Composable
fun SelectableChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun UnitAverageCard(
    name: String,
    average: Double?,
    selected: Boolean,
    isCustomized: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(142.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        border = if (selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(name, maxLines = 1, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                if (isCustomized) {
                    Icon(
                        Icons.Default.Tune,
                        contentDescription = stringResource(R.string.ponderacion_personalizada_desc),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            Text(
                average?.let(::format) ?: "—",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

