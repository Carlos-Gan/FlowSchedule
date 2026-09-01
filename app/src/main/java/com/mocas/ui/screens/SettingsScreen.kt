package com.mocas.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mocas.data.local.SchoolEventEntity
import com.mocas.data.local.SubjectEntity
import com.mocas.ui.viewmodel.ScheduleViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SettingsScreen(
    viewModel: ScheduleViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    val allEvents by viewModel.allEventsWithSubject.collectAsStateWithLifecycle()
    val subjects by viewModel.subjectsWithSlots.collectAsStateWithLifecycle()
    val categories by viewModel.gradeCategories.collectAsStateWithLifecycle()
    val gradeItems by viewModel.gradeItems.collectAsStateWithLifecycle()
    val units by viewModel.gradeUnits.collectAsStateWithLifecycle()
    val unitWeights by viewModel.gradeUnitCategoryWeights.collectAsStateWithLifecycle()

    var showProfileDialog by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf(settings.userName) }
    var tempEducation by remember { mutableStateOf(settings.educationLevel) }
    var tempInstitution by remember { mutableStateOf(settings.educationInstitution) }

    var showTrashDialog by remember { mutableStateOf(false) }
    var showBackupsDialog by remember { mutableStateOf(false) }

    val completedTasksCount = remember(allEvents) {
        allEvents.count { it.event.isCompleted }
    }

    val realAverage = remember(subjects, categories, gradeItems, units, unitWeights) {
        val subjectIds = subjects.map { it.subject.id }
        com.mocas.data.repository.GradeCalculator.periodAverage(
            subjectIds = subjectIds,
            categories = categories,
            items = gradeItems,
            units = units,
            unitCategoryWeights = unitWeights
        ) ?: 0.0
    }

    val displayAverage = remember(realAverage, settings.useGpaScale) {
        if (settings.useGpaScale) {
            // Conversión a escala 4.0
            val gpa = if (realAverage > 10) (realAverage / 100.0) * 4.0 else (realAverage / 10.0) * 4.0
            "%.2f".format(gpa)
        } else {
            if (realAverage > 10) "%.0f".format(realAverage) else "%.1f".format(realAverage)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- PROFILE HEADER ---
        Box(
            modifier = Modifier
                .size(100.dp)
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                .padding(4.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(50.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = settings.userName,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = settings.educationLevel,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Text(
            text = settings.educationInstitution,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                tempName = settings.userName
                tempEducation = settings.educationLevel
                tempInstitution = settings.educationInstitution
                showProfileDialog = true
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Editar Perfil", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- GPA CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (settings.useGpaScale) "GPA ACTUAL" else "PROMEDIO ACTUAL",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = displayAverage,
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black, fontSize = 42.sp),
                    color = MaterialTheme.colorScheme.primary
                )
                
                if (realAverage > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { (realAverage / (if (realAverage > 10) 100.0 else 10.0)).toFloat() },
                            modifier = Modifier.size(80.dp),
                            color = MaterialTheme.colorScheme.secondary,
                            strokeWidth = 8.dp,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                        Text(
                            text = if (realAverage > 90 || realAverage > 9.0) "Excelente" else "En progreso",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- STATS GRID ---
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                icon = Icons.Outlined.CheckCircle,
                value = completedTasksCount.toString(),
                label = "Actividades completadas",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Outlined.Whatshot,
                value = "0", // Could be implemented with real streak logic
                label = "Días de racha",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- PREFERENCES SECTION ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "Preferencias y Ajustes",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                SettingRow(
                    icon = Icons.Outlined.Notifications,
                    title = "Notificaciones Push",
                    subtitle = "Recordatorios de tareas y clases",
                    action = {
                        Switch(
                            checked = settings.notificationsEnabled,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(notificationsEnabled = it)) },
                            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                )

                SettingRow(
                    icon = Icons.Outlined.DarkMode,
                    title = "Tema Oscuro",
                    subtitle = "Reduce el cansancio visual",
                    action = {
                        Switch(
                            checked = settings.themeMode == "DARK",
                            onCheckedChange = { 
                                viewModel.updateSettings(settings.copy(themeMode = if (it) "DARK" else "LIGHT"))
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                )

                SettingRow(
                    icon = Icons.Default.TrendingUp,
                    title = "Usar Escala GPA",
                    subtitle = "Cambiar promedio de 0-10 a 0-4.0",
                    action = {
                        Switch(
                            checked = settings.useGpaScale,
                            onCheckedChange = { 
                                viewModel.updateSettings(settings.copy(useGpaScale = it))
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                )

                SettingRow(
                    icon = Icons.Outlined.RestoreFromTrash,
                    title = "Papelera de Reciclaje",
                    subtitle = "Recupera materias o eventos borrados",
                    onClick = { showTrashDialog = true }
                )

                SettingRow(
                    icon = Icons.Outlined.Backup,
                    title = "Respaldos y Datos",
                    subtitle = "Copia de seguridad e importación",
                    onClick = { showBackupsDialog = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }

    // --- DIALOGS ---
    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = { Text("Editar Perfil", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text("Tu nombre") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = tempEducation,
                        onValueChange = { tempEducation = it },
                        label = { Text("Carrera / Grado") },
                        placeholder = { Text("Ej. 4to Semestre") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = tempInstitution,
                        onValueChange = { tempInstitution = it },
                        label = { Text("Institución") },
                        placeholder = { Text("Ej. Preparatoria #1") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateSettings(settings.copy(
                            userName = tempName,
                            educationLevel = tempEducation,
                            educationInstitution = tempInstitution
                        ))
                        showProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { showProfileDialog = false }) { Text("Cancelar") }
            }
        )
    }

    if (showTrashDialog) {
        val deletedSubjects by viewModel.deletedSubjects.collectAsStateWithLifecycle()
        val deletedEvents by viewModel.deletedEvents.collectAsStateWithLifecycle()
        var showEmptyConfirm by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showTrashDialog = false },
            title = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Papelera", fontWeight = FontWeight.Bold)
                    if (deletedSubjects.isNotEmpty() || deletedEvents.isNotEmpty()) {
                        IconButton(onClick = { showEmptyConfirm = true }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Vaciar papelera", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (deletedSubjects.isEmpty() && deletedEvents.isEmpty()) {
                        item { Text("La papelera está vacía.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                    items(deletedSubjects) { sub ->
                        TrashRow(
                            title = sub.name,
                            subtitle = "Materia",
                            onRestore = { viewModel.restoreDeletedSubject(sub.id) },
                            onDelete = { viewModel.permanentlyDeleteSubject(sub.id) }
                        )
                    }
                    items(deletedEvents) { ev ->
                        TrashRow(
                            title = ev.title,
                            subtitle = "Actividad",
                            onRestore = { viewModel.restoreDeletedEvent(ev.id) },
                            onDelete = { viewModel.permanentlyDeleteEvent(ev.id) }
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = { OutlinedButton(onClick = { showTrashDialog = false }) { Text("Cerrar") } }
        )

        if (showEmptyConfirm) {
            AlertDialog(
                onDismissRequest = { showEmptyConfirm = false },
                title = { Text("¿Vaciar papelera?", fontWeight = FontWeight.Bold) },
                text = { Text("Esta acción eliminará todos los elementos permanentemente.") },
                confirmButton = {
                    Button(onClick = { viewModel.emptyTrash(); showEmptyConfirm = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                        Text("Eliminar todo")
                    }
                },
                dismissButton = { TextButton(onClick = { showEmptyConfirm = false }) { Text("Cancelar") } }
            )
        }
    }

    if (showBackupsDialog) {
        val automaticBackups by viewModel.automaticBackups.collectAsStateWithLifecycle()
        var pendingRestore by remember { mutableStateOf<String?>(null) }

        val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            uri?.let(viewModel::exportScheduleBackup)
        }
        val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { viewModel.importScheduleBackup(it) }
        }

        AlertDialog(
            onDismissRequest = { showBackupsDialog = false },
            title = { Text("Respaldos y Datos", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { exportLauncher.launch("SnapBackup_${System.currentTimeMillis()}.json") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Exportar", fontSize = 12.sp)
                        }
                        Button(onClick = { importLauncher.launch(arrayOf("application/json", "application/octet-stream")) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Importar", fontSize = 12.sp)
                        }
                    }
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Text("CÓPIAS AUTOMÁTICAS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black))
                    
                    val dateFormatter = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
                    
                    LazyColumn(modifier = Modifier.heightIn(max = 250.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (automaticBackups.isEmpty()) {
                            item { Text("No hay respaldos automáticos.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                        items(automaticBackups) { backup ->
                            TrashRow(
                                title = backup.reason,
                                subtitle = dateFormatter.format(Date(backup.createdAtMillis)),
                                onRestore = { pendingRestore = backup.fileName },
                                onDelete = { viewModel.deleteAutomaticBackup(backup.fileName) }
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { OutlinedButton(onClick = { showBackupsDialog = false }) { Text("Cerrar") } }
        )

        pendingRestore?.let { fileName ->
            AlertDialog(
                onDismissRequest = { pendingRestore = null },
                title = { Text("¿Restaurar respaldo?", fontWeight = FontWeight.Bold) },
                text = { Text("Se reemplazará toda la información actual con la del respaldo.") },
                confirmButton = {
                    Button(onClick = { viewModel.restoreAutomaticBackup(fileName); pendingRestore = null; showBackupsDialog = false }) {
                        Text("Restaurar ahora")
                    }
                },
                dismissButton = { TextButton(onClick = { pendingRestore = null }) { Text("Cancelar") } }
            )
        }
    }
}

@Composable
private fun TrashRow(
    title: String,
    subtitle: String,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onRestore, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Restore, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun BadgeChip(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = color
        )
    }
}

@Composable
private fun StatCard(icon: ImageVector, value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        
        if (action != null) {
            action()
        } else if (onClick != null) {
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.size(14.dp))
        }
    }
}
