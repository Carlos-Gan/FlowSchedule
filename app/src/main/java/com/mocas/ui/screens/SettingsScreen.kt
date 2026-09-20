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
import com.mocas.ui.model.BadgeStyle
import com.mocas.ui.theme.ThemeConfig
import com.mocas.ui.theme.ThemeOption
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mocas.R
import com.mocas.data.local.SchoolEventEntity
import com.mocas.data.local.SubjectEntity
import com.mocas.ui.components.settings.RemindersConfigDialog
import com.mocas.ui.components.settings.SettingRow
import com.mocas.ui.components.settings.StatCard
import com.mocas.ui.components.settings.TrashRow
import com.mocas.ui.model.AppSettings
import com.mocas.ui.viewmodel.ScheduleViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
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

    val currentStreak by viewModel.currentStreak.collectAsStateWithLifecycle()

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
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(stringResource(R.string.editar_perfil), fontWeight = FontWeight.Bold)
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
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (settings.useGpaScale) stringResource(R.string.gpa_actual) else stringResource(R.string.promedio_actual),
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
                            text = if (realAverage > 90 || realAverage > 9.0) stringResource(R.string.excelente) else stringResource(
                                R.string.en_progreso
                            ),
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
                label = stringResource(R.string.actividades_completadas),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Outlined.Whatshot,
                value = currentStreak.toString(),
                label = stringResource(R.string.dias_de_racha),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- PERSONALIZATION SECTION ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = stringResource(R.string.personalizacion),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                var showThemeModeDialog by remember { mutableStateOf(false) }
                SettingRow(
                    icon = when(settings.themeMode.uppercase()) {
                        "LIGHT" -> Icons.Outlined.LightMode
                        "DARK" -> Icons.Outlined.DarkMode
                        else -> Icons.Outlined.BrightnessAuto
                    },
                    title = stringResource(R.string.modo_apariencia),
                    subtitle = when(settings.themeMode.uppercase()) {
                        "LIGHT" -> stringResource(R.string.tema_claro)
                        "DARK" -> stringResource(R.string.tema_oscuro)
                        else -> stringResource(R.string.tema_sistema)
                    },
                    onClick = { showThemeModeDialog = true }
                )

                if (showThemeModeDialog) {
                    AlertDialog(
                        onDismissRequest = { showThemeModeDialog = false },
                        title = { Text(stringResource(R.string.modo_apariencia), fontWeight = FontWeight.Bold) },
                        text = {
                            Column {
                                listOf("AUTO", "LIGHT", "DARK").forEach { mode ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.updateSettings(settings.copy(themeMode = mode))
                                                showThemeModeDialog = false
                                            }
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = settings.themeMode.uppercase() == mode,
                                            onClick = null
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = when(mode) {
                                                    "LIGHT" -> stringResource(R.string.tema_claro)
                                                    "DARK" -> stringResource(R.string.tema_oscuro)
                                                    else -> stringResource(R.string.tema_sistema)
                                                }
                                            )
                                            Text(
                                                text = when(mode) {
                                                    "LIGHT" -> stringResource(R.string.tema_desc_claro)
                                                    "DARK" -> stringResource(R.string.tema_desc_oscuro)
                                                    else -> stringResource(R.string.tema_desc_sistema)
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showThemeModeDialog = false }) {
                                Text(stringResource(R.string.cerrar))
                            }
                        }
                    )
                }

                SettingRow(
                    icon = Icons.Outlined.Palette,
                    title = stringResource(R.string.tema_visual),
                    subtitle = ThemeConfig.themes.find { it.id == settings.colorTheme }?.let { stringResource(it.nameRes) } ?: stringResource(R.string.tema_estandar),
                    onClick = { viewModel.openAppearance() }
                )

                var showBadgeStyleDialog by remember { mutableStateOf(false) }
                SettingRow(
                    icon = Icons.Outlined.NotificationsActive,
                    title = stringResource(R.string.estilo_indicador),
                    subtitle = when(settings.badgeStyle) {
                        BadgeStyle.NONE -> "Oculto"
                        BadgeStyle.DOT -> "Solo punto"
                        BadgeStyle.NUMBER -> "Con número"
                    },
                    onClick = { showBadgeStyleDialog = true }
                )

                if (showBadgeStyleDialog) {
                    AlertDialog(
                        onDismissRequest = { showBadgeStyleDialog = false },
                        title = { Text(stringResource(R.string.estilo_indicador), fontWeight = FontWeight.Bold) },
                        text = {
                            Column {
                                BadgeStyle.entries.forEach { style ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.updateSettings(settings.copy(badgeStyle = style))
                                                showBadgeStyleDialog = false
                                            }
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = settings.badgeStyle == style,
                                            onClick = null
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = when(style) {
                                                BadgeStyle.NONE -> "Oculto"
                                                BadgeStyle.DOT -> "Solo punto rojo"
                                                BadgeStyle.NUMBER -> "Punto rojo con número"
                                            }
                                        )
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showBadgeStyleDialog = false }) {
                                Text(stringResource(R.string.cerrar))
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- GENERAL & DATA SECTION ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = stringResource(R.string.general_y_datos),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                SettingRow(
                    icon = Icons.Outlined.Notifications,
                    title = stringResource(R.string.notificaciones_push),
                    subtitle = stringResource(R.string.recordatorios_de_tareas_y_clases),
                    action = {
                        Switch(
                            checked = settings.notificationsEnabled,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(notificationsEnabled = it)) },
                            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                )

                var showRemindersDialog by remember { mutableStateOf(false) }
                SettingRow(
                    icon = Icons.Outlined.Alarm,
                    title = stringResource(R.string.configurar_recordatorios),
                    subtitle = stringResource(R.string.configurar_recordatorios_desc),
                    onClick = { showRemindersDialog = true }
                )

                if (showRemindersDialog) {
                    RemindersConfigDialog(
                        settings = settings,
                        onUpdate = viewModel::updateSettings,
                        onDismiss = { showRemindersDialog = false }
                    )
                }

                SettingRow(
                    icon = Icons.Default.TrendingUp,
                    title = stringResource(R.string.usar_escala_gpa),
                    subtitle = stringResource(R.string.cambiar_promedio),
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
                    title = stringResource(R.string.papelera_de_reciclaje),
                    subtitle = stringResource(R.string.recupera_materias),
                    onClick = { showTrashDialog = true }
                )

                SettingRow(
                    icon = Icons.Outlined.Backup,
                    title = stringResource(R.string.respaldos_y_datos),
                    subtitle = stringResource(R.string.copia_de_seguridad),
                    onClick = { showBackupsDialog = true }
                )

                // --- DEBUG ONLY SECTION ---
                if (com.mocas.BuildConfig.DEBUG) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Text(
                        text = stringResource(R.string.ajustes_de_desarrollo_debug),
                        style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )

                    SettingRow(
                        icon = Icons.Default.Language,
                        title = stringResource(R.string.idioma_de_prueba),
                        subtitle = stringResource(R.string.cambiar_a_ingles),
                        action = {
                            Switch(
                                checked = settings.language == "English",
                                onCheckedChange = { 
                                    viewModel.updateSettings(settings.copy(language = if (it) "English" else "Español"))
                                },
                                colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                            )
                        }
                    )

                    SettingRow(
                        icon = Icons.Default.RestartAlt,
                        title = stringResource(R.string.debug_reiniciar_bienvenida),
                        subtitle = stringResource(R.string.debug_reiniciar_bienvenida_desc),
                        onClick = {
                            viewModel.updateSettings(settings.copy(onboardingCompleted = false))
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }

    // --- DIALOGS ---
    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = { Text(stringResource(R.string.editar_perfil), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text(stringResource(R.string.tu_nombre)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = tempEducation,
                        onValueChange = { tempEducation = it },
                        label = { Text(stringResource(R.string.carrera_grado)) },
                        placeholder = { Text(stringResource(R.string.ej_semestre)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = tempInstitution,
                        onValueChange = { tempInstitution = it },
                        label = { Text(stringResource(R.string.institucion)) },
                        placeholder = { Text(stringResource(R.string.ej_preparatoria)) },
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
                ) { Text(stringResource(R.string.guardar)) }
            },
            dismissButton = {
                TextButton(onClick = { showProfileDialog = false }) { Text(stringResource(R.string.cancelar)) }
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
                    Text(stringResource(R.string.papelera), fontWeight = FontWeight.Bold)
                    if (deletedSubjects.isNotEmpty() || deletedEvents.isNotEmpty()) {
                        IconButton(onClick = { showEmptyConfirm = true }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = stringResource(R.string.vaciar_papelera), tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (deletedSubjects.isEmpty() && deletedEvents.isEmpty()) {
                        item { Text(stringResource(R.string.la_papelera_esta_vacia), color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                    items(deletedSubjects) { sub ->
                        TrashRow(
                            title = sub.name,
                            subtitle = stringResource(R.string.materia),
                            onRestore = { viewModel.restoreDeletedSubject(sub.id) },
                            onDelete = { viewModel.permanentlyDeleteSubject(sub.id) }
                        )
                    }
                    items(deletedEvents) { ev ->
                        TrashRow(
                            title = ev.title,
                            subtitle = stringResource(R.string.actividad),
                            onRestore = { viewModel.restoreDeletedEvent(ev.id) },
                            onDelete = { viewModel.permanentlyDeleteEvent(ev.id) }
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = { OutlinedButton(onClick = { showTrashDialog = false }) { Text(stringResource(R.string.cerrar)) } }
        )

        if (showEmptyConfirm) {
            AlertDialog(
                onDismissRequest = { showEmptyConfirm = false },
                title = { Text(stringResource(R.string.confirmar_vaciar_papelera), fontWeight = FontWeight.Bold) },
                text = { Text(stringResource(R.string.accion_eliminar_permanente)) },
                confirmButton = {
                    Button(onClick = { viewModel.emptyTrash(); showEmptyConfirm = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                        Text(stringResource(R.string.eliminar_todo))
                    }
                },
                dismissButton = { TextButton(onClick = { showEmptyConfirm = false }) { Text(stringResource(R.string.cancelar)) } }
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
            title = { Text(stringResource(R.string.respaldos_y_datos), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { exportLauncher.launch("SnapBackup_${System.currentTimeMillis()}.json") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.exportar), fontSize = 12.sp)
                        }
                        Button(onClick = { importLauncher.launch(arrayOf("application/json", "application/octet-stream")) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.importar), fontSize = 12.sp)
                        }
                    }
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Text(stringResource(R.string.copias_automaticas), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black))
                    
                    val dateFormatter = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
                    
                    LazyColumn(modifier = Modifier.heightIn(max = 250.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (automaticBackups.isEmpty()) {
                            item { Text(stringResource(R.string.no_hay_respaldos_automaticos), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
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
            dismissButton = { OutlinedButton(onClick = { showBackupsDialog = false }) { Text(stringResource(R.string.cerrar)) } }
        )

        pendingRestore?.let { fileName ->
            AlertDialog(
                onDismissRequest = { pendingRestore = null },
                title = { Text(stringResource(R.string.confirmar_restaurar_respaldo), fontWeight = FontWeight.Bold) },
                text = { Text(stringResource(R.string.reemplazar_informacion_respaldo)) },
                confirmButton = {
                    Button(onClick = { viewModel.restoreAutomaticBackup(fileName); pendingRestore = null; showBackupsDialog = false }) {
                        Text(stringResource(R.string.restaurar_ahora))
                    }
                },
                dismissButton = { TextButton(onClick = { pendingRestore = null }) { Text(stringResource(R.string.cancelar)) } }
            )
        }
    }
}
