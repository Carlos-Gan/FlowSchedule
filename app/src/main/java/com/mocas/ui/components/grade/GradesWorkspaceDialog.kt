package com.mocas.ui.components.grade

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mocas.R
import com.mocas.data.local.GradeCategoryEntity
import com.mocas.data.local.GradeItemEntity
import com.mocas.data.local.GradeUnitCategoryWeightEntity
import com.mocas.data.local.GradeUnitEntity
import com.mocas.data.repository.GradeCalculator
import com.mocas.ui.components.SelectableChip
import com.mocas.ui.components.UnitAverageCard
import kotlin.collections.forEach

@Composable
fun GradesWorkspaceDialog(
    subjectId: Long,
    categories: List<GradeCategoryEntity>,
    gradeItems: List<GradeItemEntity>,
    units: List<GradeUnitEntity>,
    unitCategoryWeights: List<GradeUnitCategoryWeightEntity>,
    onDismiss: () -> Unit,
    onAddCategory: (GradeCategoryEntity) -> Unit,
    onAddItem: (GradeItemEntity) -> Unit,
    onDeleteCategory: (GradeCategoryEntity) -> Unit,
    onDeleteItem: (GradeItemEntity) -> Unit,
    onAddUnit: (GradeUnitEntity) -> Unit,
    onDeleteUnit: (GradeUnitEntity) -> Unit,
    onSaveUnitCategoryWeights: (Long, List<GradeUnitCategoryWeightEntity>) -> Unit,
    onResetUnitCategoryWeights: (Long) -> Unit
) {
    var selectedUnitId by remember { mutableStateOf<Long?>(null) }
    LaunchedEffect(units) {
        if (units.none { it.id == selectedUnitId }) selectedUnitId = units.firstOrNull()?.id
    }
    val selectedUnit = units.firstOrNull { it.id == selectedUnitId }
    var showAddGrade by remember { mutableStateOf(false) }
    var showAddUnit by remember { mutableStateOf(false) }
    var showCategories by remember { mutableStateOf(false) }
    var showUnitWeightsDialog by remember { mutableStateOf(false) }
    var showSimulator by remember { mutableStateOf(false) }
    var unitPendingDelete by remember { mutableStateOf<GradeUnitEntity?>(null) }
    var passingText by remember { mutableStateOf("70") }
    val passing = passingText.toDoubleOrNull()?.takeIf { it in 0.0..100.0 } ?: 70.0

    val effectiveCategoriesForSelectedUnit = remember(categories, selectedUnitId, unitCategoryWeights) {
        selectedUnitId?.let { id ->
            GradeCalculator.resolveCategoriesForUnit(categories, id, unitCategoryWeights)
        } ?: categories
    }
    val isSelectedUnitCustomized = remember(selectedUnitId, unitCategoryWeights) {
        selectedUnitId != null && GradeCalculator.hasCustomUnitWeights(selectedUnitId!!, unitCategoryWeights)
    }

    val overallSubjectAverage = remember(categories, gradeItems, units, unitCategoryWeights) {
        GradeCalculator.subjectAverage(subjectId, categories, gradeItems, units, unitCategoryWeights)
    }

    val unitItems = gradeItems.filter { it.unitId == selectedUnitId }
    val unitAverage = remember(effectiveCategoriesForSelectedUnit, unitItems) {
        GradeCalculator.summarize(effectiveCategoriesForSelectedUnit, unitItems).currentAverage
    }

    val generalSummary = remember(categories, gradeItems, passing) {
        GradeCalculator.summarize(categories, gradeItems, passing)
    }

    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val screenHeightDp = with(density) { windowInfo.containerSize.height.toDp() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = screenHeightDp * 0.9f)
                .imePadding(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(Modifier
                .verticalScroll(rememberScrollState())
                .padding(18.dp)) {

                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.calificaciones),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(stringResource(R.string.organizadas_por_unidades), style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = { showCategories = true }) {
                        Icon(
                            Icons.Default.Settings,
                            stringResource(R.string.administrar_porcentajes_desc)
                        )
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, stringResource(R.string.cerrar)) }
                }

                Spacer(Modifier.height(14.dp))

                // Promedio general (hero card)
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(Modifier
                        .fillMaxWidth()
                        .padding(16.dp)) {
                        Text(stringResource(R.string.promedio_materia_label), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .8f))
                        Text(
                            overallSubjectAverage?.let { "${format(it)} / 100" }
                                ?: stringResource(R.string.sin_calificaciones),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        LinearProgressIndicator(
                            progress = { ((overallSubjectAverage ?: 0.0) / 100.0).toFloat() },
                            color = MaterialTheme.colorScheme.onPrimary, trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = .25f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        )
                        Text(
                            neededText(generalSummary.neededToPass, passing, generalSummary.gradedWeight),
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .9f),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                Text(stringResource(R.string.promedio_por_unidad), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                if (units.isEmpty()) {
                    Text(stringResource(R.string.crea_unidad_instruccion), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        units.forEach { unit ->
                            val average = GradeCalculator.unitAverage(categories, gradeItems, unit.id, unitCategoryWeights)
                            val isCustom = GradeCalculator.hasCustomUnitWeights(unit.id, unitCategoryWeights)
                            UnitAverageCard(
                                name = unit.name,
                                average = average,
                                selected = selectedUnitId == unit.id,
                                isCustomized = isCustom,
                                onClick = { selectedUnitId = unit.id },
                                onLongClick = {
                                    selectedUnitId = unit.id
                                    showUnitWeightsDialog = true
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Selector de unidades
                Text(
                    stringResource(R.string.unidades),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    units.forEach { unit ->
                        SelectableChip(
                            unit.name,
                            selectedUnitId == unit.id
                        ) { selectedUnitId = unit.id }
                    }
                }

                Spacer(Modifier.height(18.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(Modifier.height(14.dp))

                // Card de contenido de la unidad seleccionada
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                            alpha = 0.3f
                        )
                    )
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    selectedUnit?.name ?: stringResource(R.string.crea_primera_unidad),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "${stringResource(R.string.promedio_por_unidad)}: ${unitAverage?.let(::format) ?: "—"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = { showAddGrade = true },
                                enabled = categories.isNotEmpty() && selectedUnit != null,
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.nota))
                            }
                            if (selectedUnit != null) {
                                IconButton(onClick = { unitPendingDelete = selectedUnit }) {
                                    Icon(Icons.Default.Delete, stringResource(R.string.eliminar_unidad_desc), tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        // Badge / Button to configure unit-specific weights
                        if (selectedUnit != null && categories.isNotEmpty()) {
                            Surface(
                                onClick = { showUnitWeightsDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelectedUnitCustomized) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isSelectedUnitCustomized) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Tune,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = if (isSelectedUnitCustomized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = if (isSelectedUnitCustomized) stringResource(R.string.porcentajes_personalizados) else stringResource(R.string.porcentajes_generales_default),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelectedUnitCustomized) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelectedUnitCustomized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = stringResource(R.string.cambiar_porcentajes_unidad_desc),
                                        modifier = Modifier.size(12.dp),
                                        tint = if (isSelectedUnitCustomized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        if (categories.isEmpty()) {
                            Text(
                                stringResource(R.string.configurar_categorias_primero),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 18.dp, bottom = 6.dp)
                            )
                        } else if (unitItems.isEmpty()) {
                            Text(
                                stringResource(R.string.sin_calificaciones_unidad),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 18.dp, bottom = 6.dp)
                            )
                        } else {
                            effectiveCategoriesForSelectedUnit.forEachIndexed { index, category ->
                                val items = unitItems.filter { it.categoryId == category.id }
                                if (items.isNotEmpty()) {
                                    if (index > 0) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 10.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(
                                                alpha = 0.4f
                                            )
                                        )
                                    }
                                    Text(
                                        "${category.name} · ${format(category.weightPercent)}%",
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                                    )
                                    items.forEach { item -> GradeRow(item, onDeleteItem) }
                                }
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = { showAddUnit = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.nueva_unidad))
                }

                Spacer(Modifier.height(18.dp))

                // Simulador como panel expandible (bento style)
                Surface(
                    onClick = { showSimulator = true },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Insights,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            stringResource(R.string.simulador_calificacion),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = stringResource(R.string.abrir_simulador_desc),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
    if (showAddGrade && selectedUnit != null) {
        AddGradeDialog(
            effectiveCategoriesForSelectedUnit,
            selectedUnit,
            { showAddGrade = false }) { category, unit, name, score ->
            onAddItem(
                GradeItemEntity(
                    categoryId = category.id,
                    unitId = unit.id,
                    unitName = unit.name,
                    name = name,
                    score = score
                )
            )
            showAddGrade = false
        }
    }
    if (showAddUnit) {
        CreateUnitDialog(subjectId, units.size, { showAddUnit = false }) { unit ->
            onAddUnit(unit)
            showAddUnit = false
        }
    }
    if (showCategories) {
        CategoryManagerDialog(
            subjectId,
            categories,
            onAddCategory,
            onDeleteCategory
        ) { showCategories = false }
    }
    if (showUnitWeightsDialog && selectedUnit != null) {
        UnitCategoryWeightsDialog(
            unit = selectedUnit,
            generalCategories = categories,
            currentUnitWeights = unitCategoryWeights,
            onDismiss = { showUnitWeightsDialog = false },
            onSave = { weights -> onSaveUnitCategoryWeights(selectedUnit.id, weights) },
            onResetToGeneral = { onResetUnitCategoryWeights(selectedUnit.id) }
        )
    }
    if (showSimulator) {
        GradeSimulatorDialog(
            categories = categories,
            units = units,
            items = gradeItems,
            passingText = passingText,
            onPassingChanged = { passingText = it },
            onDismiss = { showSimulator = false }
        )
    }
    unitPendingDelete?.let { unit ->
        AlertDialog(
            onDismissRequest = { unitPendingDelete = null },
            title = { Text(stringResource(R.string.confirmar_eliminar_unidad, unit.name)) },
            text = { Text(stringResource(R.string.mensaje_eliminar_unidad)) },
            confirmButton = {
                Button(
                    onClick = { onDeleteUnit(unit); unitPendingDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.eliminar)) }
            },
            dismissButton = { TextButton(onClick = { unitPendingDelete = null }) { Text(stringResource(R.string.cancelar)) } }
        )
    }
}