package com.mocas.ui.add.subject

import android.content.ContextWrapper
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mocas.R
import com.mocas.data.local.SchoolEventWithSubject
import com.mocas.data.local.SubjectWithSlots
import com.mocas.data.local.GradeCategoryEntity
import com.mocas.data.local.GradeItemEntity
import com.mocas.data.local.GradeUnitCategoryWeightEntity
import com.mocas.data.local.GradeUnitEntity
import com.mocas.data.repository.CalendarSyncHelper
import com.mocas.ui.components.events.EventItemCard
import com.mocas.ui.components.GradesSection
import com.mocas.ui.components.parseColorFromHex
import com.mocas.ui.util.showCalendarResult

@Composable
fun SubjectDetailScreen(
    subjectWithSlots: SubjectWithSlots,
    linkedEvents: List<SchoolEventWithSubject>,
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onAddEventClick: () -> Unit,
    onToggleEventCompleted: (Long, Boolean) -> Unit,
    onToggleSubtask: (Long, Long, Boolean) -> Unit,
    onEditEvent: (SchoolEventWithSubject) -> Unit,
    gradeCategories: List<GradeCategoryEntity>,
    gradeItems: List<GradeItemEntity>,
    gradeUnits: List<GradeUnitEntity>,
    gradeUnitCategoryWeights: List<GradeUnitCategoryWeightEntity> = emptyList(),
    onAddGradeCategory: (GradeCategoryEntity) -> Unit,
    onAddGradeItem: (GradeItemEntity) -> Unit,
    onDeleteGradeCategory: (GradeCategoryEntity) -> Unit,
    onDeleteItem: (GradeItemEntity) -> Unit,
    onAddGradeUnit: (GradeUnitEntity) -> Unit,
    onDeleteGradeUnit: (GradeUnitEntity) -> Unit,
    onSaveUnitCategoryWeights: (Long, List<GradeUnitCategoryWeightEntity>) -> Unit = { _, _ -> },
    onResetUnitCategoryWeights: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val activity = remember(view) {
        var current = view.context
        while (current is ContextWrapper && current !is ComponentActivity) current = current.baseContext
        current as? ComponentActivity
    }
    val window = activity?.window
    val insetsController = remember(window, view) {
        window?.let { WindowCompat.getInsetsController(it, view) }
    }
    val previousLightStatusBars = remember(insetsController) { insetsController?.isAppearanceLightStatusBars }
    val previousStatusContrast = remember(window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) window?.isStatusBarContrastEnforced else null
    }
    val previousNavigationContrast = remember(window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) window?.isNavigationBarContrastEnforced else null
    }
    val subject = subjectWithSlots.subject
    val slots = subjectWithSlots.slots
    val subjectColor = parseColorFromHex(subject.colorHex)
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    SideEffect {
        insetsController?.isAppearanceLightStatusBars = subjectColor.luminance() > 0.5f
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window?.isStatusBarContrastEnforced = false
            window?.isNavigationBarContrastEnforced = false
        }
    }
    DisposableEffect(window, insetsController) {
        onDispose {
            previousLightStatusBars?.let { insetsController?.isAppearanceLightStatusBars = it }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                previousStatusContrast?.let { window?.isStatusBarContrastEnforced = it }
                previousNavigationContrast?.let { window?.isNavigationBarContrastEnforced = it }
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("subject_detail_screen"),
        color = MaterialTheme.colorScheme.surface
    ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with Subject Color Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(subjectColor, subjectColor.copy(alpha = 0.85f))
                            )
                        )
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = onDismiss, modifier = Modifier.size(44.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.academic_back), tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = subject.name,
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            if (subject.professor.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = subject.professor,
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                        if (subject.code.isNotBlank()) {
                            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)) {
                                Text(subject.code, color = MaterialTheme.colorScheme.onPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                    }
                }

                // Content Body
                Column(modifier = Modifier.padding(20.dp)) {
                    // Solo mostramos el resumen útil; el salón ya aparece en el horario.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(stringResource(R.string.salones_label), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = slots.map { it.room.ifBlank { subject.defaultRoom } }
                                        .filter { it.isNotBlank() }
                                        .distinct()
                                        .joinToString(" · ")
                                        .ifBlank { stringResource(R.string.no_especificado) },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(stringResource(R.string.recordatorio_label), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = stringResource(R.string.min_antes_formato, subject.reminderMinutesBefore),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Agrupa sesiones repetidas: una sola tarjeta por horario y salón.
                    val groupedSlots = slots
                        .groupBy { slot ->
                            Triple(slot.startTime, slot.endTime, slot.room.ifBlank { subject.defaultRoom })
                        }
                        .toList()
                        .sortedBy { (_, group) -> group.minOf { it.dayOfWeek } }
                    Text(
                        text = stringResource(R.string.horario_sesiones_formato, slots.size),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    groupedSlots.forEach { (schedule, group) ->
                        val (startTime, endTime, room) = schedule
                        
                        // Obtenemos los nombres de los días de forma dinámica
                        val dayNames = group.sortedBy { it.dayOfWeek }.map { slot ->
                            when(slot.dayOfWeek) {
                                1 -> stringResource(R.string.lunes)
                                2 -> stringResource(R.string.martes)
                                3 -> stringResource(R.string.miercoles)
                                4 -> stringResource(R.string.jueves)
                                5 -> stringResource(R.string.viernes)
                                6 -> stringResource(R.string.sabado)
                                else -> stringResource(R.string.domingo)
                            }
                        }.joinToString(", ")

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(subjectColor)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = dayNames,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "$startTime - $endTime",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = room.ifBlank { stringResource(R.string.sin_salon) },
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    GradesSection(
                        subjectId = subject.id,
                        categories = gradeCategories,
                        gradeItems = gradeItems,
                        units = gradeUnits,
                        unitCategoryWeights = gradeUnitCategoryWeights,
                        onAddCategory = onAddGradeCategory,
                        onAddItem = onAddGradeItem,
                        onDeleteCategory = onDeleteGradeCategory,
                        onDeleteItem = onDeleteItem,
                        onAddUnit = onAddGradeUnit,
                        onDeleteUnit = onDeleteGradeUnit,
                        onSaveUnitCategoryWeights = onSaveUnitCategoryWeights,
                        onResetUnitCategoryWeights = onResetUnitCategoryWeights
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Linked tasks & exams
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.tareas_examenes_conteo_formato, linkedEvents.size),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = stringResource(R.string.agregar),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.clickable { onAddEventClick() }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (linkedEvents.isEmpty()) {
                        Text(
                            text = stringResource(R.string.sin_tareas_materia),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        linkedEvents.forEach { eventWithSubject ->
                            EventItemCard(
                                eventWithSubject = eventWithSubject,
                                onToggleCompleted = {
                                    onToggleEventCompleted(eventWithSubject.event.id, it)
                                },
                                onClick = { onEditEvent(eventWithSubject) },
                                onCalendarSyncClick = {
                                    showCalendarResult(
                                        context,
                                        CalendarSyncHelper.addEventToPhoneCalendar(
                                            context,
                                            eventWithSubject.event,
                                            subject.name
                                        )
                                    )
                                },
                                onToggleSubtask = { subtaskId, completed ->
                                    onToggleSubtask(eventWithSubject.event.id, subtaskId, completed)
                                }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Actions: Edit and Delete
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showDeleteConfirmation = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.eliminar))
                        }

                        Button(
                            onClick = onEditClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.editar))
                        }
                    }
                }
            }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(R.string.confirmar_eliminar_materia)) },
            text = { Text(stringResource(R.string.mensaje_eliminar_materia)) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        onDeleteClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.eliminar)) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmation = false }) { Text(stringResource(R.string.cancelar)) }
            }
        )
    }
}
