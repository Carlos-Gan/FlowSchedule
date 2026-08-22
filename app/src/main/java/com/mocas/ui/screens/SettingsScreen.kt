package com.mocas.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mocas.BuildConfig
import com.mocas.data.local.AcademicPeriodEntity
import com.mocas.data.local.SchoolEventType
import com.mocas.data.repository.CalendarSyncHelper
import com.mocas.ui.components.parseColorFromHex
import com.mocas.ui.dialogs.AcademicPeriodDialog
import com.mocas.ui.theme.IndigoPrimary
import com.mocas.ui.util.capitalizeFirstLetter
import com.mocas.ui.viewmodel.ScheduleViewModel
import com.mocas.util.DateTimeUtils
import java.time.temporal.ChronoUnit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: ScheduleViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    val subjectsWithSlots by viewModel.subjectsWithSlots.collectAsStateWithLifecycle()
    val academicPeriods by viewModel.academicPeriods.collectAsStateWithLifecycle()
    val deletedSubjects by viewModel.deletedSubjects.collectAsStateWithLifecycle()
    val deletedEvents by viewModel.deletedEvents.collectAsStateWithLifecycle()
    val automaticBackups by viewModel.automaticBackups.collectAsStateWithLifecycle()

    var showNameDialog by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf(settings.userName) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showPeriodDialog by remember { mutableStateOf(false) }
    var editingPeriod by remember { mutableStateOf<AcademicPeriodEntity?>(null) }
    var copyTargetPeriod by remember { mutableStateOf<AcademicPeriodEntity?>(null) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var showTrashDialog by remember { mutableStateOf(false) }
    var showBackupsDialog by remember { mutableStateOf(false) }
    var showEmptyTrashConfirm by remember { mutableStateOf(false) }
    var pendingAutomaticRestore by remember { mutableStateOf<String?>(null) }

    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let(viewModel::exportScheduleBackup)
    }
    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        pendingImportUri = uri
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("settings_screen_list"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Bento User Profile Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        tempName = settings.userName
                        showNameDialog = true
                    }
                    .testTag("settings_profile_card"),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(IndigoPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = settings.userName.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = settings.userName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Estudiante Universitario • Semestre Actual",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = IndigoPrimary.copy(alpha = 0.1f),
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Editar perfil",
                            tint = IndigoPrimary,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(20.dp)
                        )
                    }
                }
            }
        }

        // Section: Preferencias de Visualización y Horario
        item {
            Text(
                text = "Preferencias",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = IndigoPrimary
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val nextMode = when (settings.themeMode) {
                                    "AUTO" -> "LIGHT"
                                    "LIGHT" -> "DARK"
                                    else -> "AUTO"
                                }
                                viewModel.updateSettings(settings.copy(themeMode = nextMode))
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = IndigoPrimary.copy(alpha = 0.1f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.DarkMode,
                                    contentDescription = null,
                                    tint = IndigoPrimary,
                                    modifier = Modifier.size(21.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 12.dp)
                        ) {
                            Text("Tema visual", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                "Toca para cambiar la apariencia",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        SettingsValueChip(
                            text = when (settings.themeMode) {
                                "LIGHT" -> "Claro"
                                "DARK" -> "Oscuro"
                                else -> "Sistema"
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 12.dp)
                        ) {
                            Text(
                                text = "Funciones con IA",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Mostrar el escáner y la importación inteligente",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = settings.aiFeaturesEnabled,
                            onCheckedChange = {
                                viewModel.updateSettings(
                                    settings.copy(aiFeaturesEnabled = it)
                                )
                            },
                            colors = settingsSwitchColors()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Google Calendar Sync Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sincronización con Google Calendar",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Permitir exportación directa al calendario del sistema",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = settings.calendarSyncEnabled,
                            onCheckedChange = {
                                viewModel.updateSettings(settings.copy(calendarSyncEnabled = it))
                            },
                            colors = settingsSwitchColors()
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Notificaciones",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = IndigoPrimary
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    NotificationSettingRow(
                        title = "Permitir notificaciones",
                        subtitle = "Interruptor general para todos los recordatorios",
                        checked = settings.notificationsEnabled,
                        onCheckedChange = {
                            viewModel.updateSettings(settings.copy(notificationsEnabled = it))
                        }
                    )

                    if (settings.notificationsEnabled) {
                        NotificationDivider()
                        NotificationSettingRow(
                            title = "Clases y cambios",
                            subtitle = "Próxima clase y cambios de hora o aula",
                            checked = settings.classNotificationsEnabled,
                            onCheckedChange = {
                                viewModel.updateSettings(settings.copy(classNotificationsEnabled = it))
                            },
                            leadText = formatReminderLead(settings.defaultReminderMinutes),
                            onLeadClick = {
                                viewModel.updateSettings(
                                    settings.copy(
                                        defaultReminderMinutes = nextReminderValue(
                                            settings.defaultReminderMinutes,
                                            listOf(5, 15, 30, 60)
                                        )
                                    )
                                )
                            }
                        )
                        NotificationDivider()
                        NotificationSettingRow(
                            title = "Tareas",
                            subtitle = "Entregas pendientes",
                            checked = settings.taskNotificationsEnabled,
                            onCheckedChange = {
                                viewModel.updateSettings(settings.copy(taskNotificationsEnabled = it))
                            },
                            leadText = formatReminderLead(settings.taskReminderMinutes),
                            onLeadClick = {
                                viewModel.updateSettings(
                                    settings.copy(
                                        taskReminderMinutes = nextReminderValue(
                                            settings.taskReminderMinutes,
                                            listOf(60, 12 * 60, 24 * 60, 2 * 24 * 60, 3 * 24 * 60, 7 * 24 * 60)
                                        )
                                    )
                                )
                            }
                        )
                        NotificationDivider()
                        NotificationSettingRow(
                            title = "Exámenes",
                            subtitle = "Avisos independientes de las tareas",
                            checked = settings.examNotificationsEnabled,
                            onCheckedChange = {
                                viewModel.updateSettings(settings.copy(examNotificationsEnabled = it))
                            },
                            leadText = formatReminderLead(settings.examReminderMinutes),
                            onLeadClick = {
                                viewModel.updateSettings(
                                    settings.copy(
                                        examReminderMinutes = nextReminderValue(
                                            settings.examReminderMinutes,
                                            listOf(12 * 60, 24 * 60, 2 * 24 * 60, 3 * 24 * 60, 7 * 24 * 60)
                                        )
                                    )
                                )
                            }
                        )
                        NotificationDivider()
                        NotificationSettingRow(
                            title = "Otros eventos",
                            subtitle = "Exposiciones, reuniones y eventos escolares",
                            checked = settings.eventNotificationsEnabled,
                            onCheckedChange = {
                                viewModel.updateSettings(settings.copy(eventNotificationsEnabled = it))
                            },
                            leadText = formatReminderLead(settings.eventReminderMinutes),
                            onLeadClick = {
                                viewModel.updateSettings(
                                    settings.copy(
                                        eventReminderMinutes = nextReminderValue(
                                            settings.eventReminderMinutes,
                                            listOf(60, 12 * 60, 24 * 60, 2 * 24 * 60, 3 * 24 * 60)
                                        )
                                    )
                                )
                            }
                        )
                        NotificationDivider()
                        NotificationSettingRow(
                            title = "Actividades vencidas",
                            subtitle = "Avisar si una actividad sigue pendiente",
                            checked = settings.overdueNotificationsEnabled,
                            onCheckedChange = {
                                viewModel.updateSettings(settings.copy(overdueNotificationsEnabled = it))
                            }
                        )
                        NotificationDivider()
                        NotificationSettingRow(
                            title = "Resumen de mañana",
                            subtitle = "Clases y actividades del día siguiente",
                            checked = settings.tomorrowSummaryEnabled,
                            onCheckedChange = {
                                viewModel.updateSettings(settings.copy(tomorrowSummaryEnabled = it))
                            },
                            leadText = "20:00"
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Periodos académicos",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = IndigoPrimary
                )
                OutlinedButton(
                    onClick = {
                        editingPeriod = null
                        showPeriodDialog = true
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("Nuevo", fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = when (academicPeriods.size) {
                            0 -> "No hay periodos guardados"
                            1 -> "1 periodo guardado"
                            else -> "${academicPeriods.size} periodos guardados"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Puedes crear uno al agregar o editar una materia.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 12.dp)
                        ) {
                            Text(
                                text = "¿Días fuera del periodo son vacaciones?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (academicPeriods.isEmpty()) {
                                    "Se aplicará cuando tengas al menos un periodo guardado"
                                } else {
                                    "Marcarlos con un punto verde en el calendario"
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.outsidePeriodsAreVacations,
                            onCheckedChange = {
                                viewModel.updateSettings(
                                    settings.copy(outsidePeriodsAreVacations = it)
                                )
                            },
                            colors = settingsSwitchColors()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 12.dp)
                        ) {
                            Text(
                                text = "Mostrar vacaciones en el horario",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Pintar de verde los días de vacaciones en las vistas semanal y diaria",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.showVacationsInTimetable,
                            onCheckedChange = {
                                viewModel.updateSettings(
                                    settings.copy(showVacationsInTimetable = it)
                                )
                            },
                            colors = settingsSwitchColors()
                        )
                    }

                    academicPeriods.forEachIndexed { index, period ->
                        Spacer(modifier = Modifier.height(if (index == 0) 12.dp else 8.dp))
                        Surface(
                            modifier = Modifier.clickable {
                                editingPeriod = period
                                showPeriodDialog = true
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = 12.dp,
                                        top = 10.dp,
                                        bottom = 10.dp,
                                        end = 4.dp
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(end = 10.dp)
                                        .size(12.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(parseColorFromHex(period.colorHex))
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = period.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = periodSummary(period),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        viewModel.openAddEvent(
                                            defaultDate = period.startDate,
                                            defaultType = SchoolEventType.VACACIONES,
                                            defaultTitle = "Vacaciones"
                                        )
                                    },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BeachAccess,
                                        contentDescription = "Agregar vacaciones en ${period.name}",
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                if (academicPeriods.size > 1) {
                                    IconButton(
                                        onClick = { copyTargetPeriod = period },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copiar materias a ${period.name}",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { viewModel.deleteAcademicPeriod(period.id) },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Eliminar ${period.name}",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Acciones y Datos
        item {
            Text(
                text = "Datos y Respaldo",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = IndigoPrimary
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Portable SnapMySchedule backup
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                exportBackupLauncher.launch(
                                    "SnapMySchedule-${DateTimeUtils.todayString()}.json"
                                )
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = IndigoPrimary.copy(alpha = 0.1f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = null,
                                    tint = IndigoPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Exportar respaldo", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                "Guardar materias, sesiones, periodos y actividades",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                importBackupLauncher.launch(
                                    arrayOf("application/json", "text/plain", "application/octet-stream")
                                )
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0EA5E9).copy(alpha = 0.12f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.FileUpload,
                                    contentDescription = null,
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Importar respaldo", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                "Restaurar un archivo exportado por SnapMySchedule",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTrashDialog = true }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF59E0B).copy(alpha = 0.12f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.RestoreFromTrash, null, tint = Color(0xFFD97706))
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Papelera", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            val trashCount = deletedSubjects.size + deletedEvents.size
                            Text(
                                "$trashCount elementos · se eliminan después de 30 días",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showBackupsDialog = true }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.12f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Backup, null, tint = Color(0xFF059669))
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Respaldos automáticos", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                "${automaticBackups.size} copias disponibles · máximo 5",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Export ICS
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (subjectsWithSlots.isEmpty()) {
                                    Toast.makeText(
                                        context,
                                        "Agrega una materia antes de exportar.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    try {
                                        val sendIntent = CalendarSyncHelper.createIcsShareIntent(
                                            context,
                                            subjectsWithSlots
                                        )
                                        val chooser = Intent.createChooser(
                                            sendIntent,
                                            "Exportar horario (.ics)"
                                        )
                                        if (chooser.resolveActivity(context.packageManager) != null) {
                                            context.startActivity(chooser)
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "No hay una app compatible para compartir el archivo.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    } catch (error: Exception) {
                                        Toast.makeText(
                                            context,
                                            error.message ?: "No se pudo crear el archivo ICS.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = IndigoPrimary.copy(alpha = 0.1f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = null,
                                    tint = IndigoPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Compartir horario (.ics)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Compatible con Google Calendar, Apple y Outlook",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Clear All Data
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showClearConfirmDialog = true }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFEF4444).copy(alpha = 0.12f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Borrar todos los datos",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFFEF4444)
                            )
                            Text(
                                text = "Vaciar todas las materias, horarios y eventos",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // About App Card Bento style
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.5f
                    )
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = IndigoPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally)
                    {
                        Text(
                            text = "SnapMySchedule",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )

                        Text(
                            text = "v${BuildConfig.VERSION_NAME}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "Diseñada para estudiantes • Bento Grid UI & Jetpack Compose",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }

    if (showPeriodDialog) {
        AcademicPeriodDialog(
            editingPeriod = editingPeriod,
            onDismiss = { showPeriodDialog = false },
            onSave = { period ->
                viewModel.saveAcademicPeriod(period)
                showPeriodDialog = false
            }
        )
    }

    copyTargetPeriod?.let { target ->
        val sourcePeriods = academicPeriods.filter { it.id != target.id }
        AlertDialog(
            onDismissRequest = { copyTargetPeriod = null },
            title = { Text("Copiar materias", fontWeight = FontWeight.ExtraBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Elige el periodo de origen. Las materias se copiarán a ${target.name} con sus sesiones, aula y profesor.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    sourcePeriods.forEach { source ->
                        OutlinedButton(
                            onClick = {
                                viewModel.copySubjectsBetweenPeriods(
                                    sourcePeriodId = source.id,
                                    targetPeriodId = target.id
                                )
                                copyTargetPeriod = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(parseColorFromHex(source.colorHex))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(source.name, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                OutlinedButton(onClick = { copyTargetPeriod = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Name change dialog
    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text("Nombre del Estudiante", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = capitalizeFirstLetter(it) },
                    label = { Text("Tu nombre") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempName.isNotBlank()) {
                            viewModel.updateSettings(settings.copy(userName = tempName.trim()))
                            showNameDialog = false
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) {
                    Text("Guardar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showNameDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Clear Confirm Dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("¿Borrar todos los datos?", fontWeight = FontWeight.Bold) },
            text = { Text("Esta acción eliminará todas las asignaturas, horarios y eventos registrados en la aplicación.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showClearConfirmDialog = false
                        Toast.makeText(
                            context,
                            "Todos los datos han sido borrados",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Borrar Todo", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showClearConfirmDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    pendingImportUri?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingImportUri = null },
            title = { Text("¿Restaurar este respaldo?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Se reemplazarán las materias, sesiones, periodos, excepciones y actividades actuales. " +
                        "Tu nombre, tema y preferencias permanecerán sin cambios."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.importScheduleBackup(uri)
                        pendingImportUri = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) {
                    Text("Restaurar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { pendingImportUri = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showTrashDialog) {
        AlertDialog(
            onDismissRequest = { showTrashDialog = false },
            title = { Text("Papelera", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (deletedSubjects.isEmpty() && deletedEvents.isEmpty()) {
                        item { Text("La papelera está vacía.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                    deletedSubjects.forEach { subject ->
                        item(key = "trash-subject-${subject.id}") {
                            TrashRow(
                                title = subject.name,
                                subtitle = "Materia",
                                onRestore = { viewModel.restoreDeletedSubject(subject.id) },
                                onDelete = { viewModel.permanentlyDeleteSubject(subject.id) }
                            )
                        }
                    }
                    deletedEvents.forEach { event ->
                        item(key = "trash-event-${event.id}") {
                            TrashRow(
                                title = event.title,
                                subtitle = "Actividad · ${event.startDate}",
                                onRestore = { viewModel.restoreDeletedEvent(event.id) },
                                onDelete = { viewModel.permanentlyDeleteEvent(event.id) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                if (deletedSubjects.isNotEmpty() || deletedEvents.isNotEmpty()) {
                    Button(
                        onClick = {
                            showTrashDialog = false
                            showEmptyTrashConfirm = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) { Text("Vaciar") }
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showTrashDialog = false }) { Text("Cerrar") }
            }
        )
    }

    if (showBackupsDialog) {
        AlertDialog(
            onDismissRequest = { showBackupsDialog = false },
            title = { Text("Respaldos automáticos", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (automaticBackups.isEmpty()) {
                        item {
                            Text(
                                "Se creará una copia antes de importar, restaurar o borrar todos los datos.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    automaticBackups.forEach { backup ->
                        item(key = backup.fileName) {
                            TrashRow(
                                title = backup.reason,
                                subtitle = formatBackupDate(backup.createdAtMillis),
                                onRestore = {
                                    showBackupsDialog = false
                                    pendingAutomaticRestore = backup.fileName
                                },
                                onDelete = { viewModel.deleteAutomaticBackup(backup.fileName) },
                                restoreDescription = "Restaurar respaldo"
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                OutlinedButton(onClick = { showBackupsDialog = false }) { Text("Cerrar") }
            }
        )
    }

    if (showEmptyTrashConfirm) {
        AlertDialog(
            onDismissRequest = { showEmptyTrashConfirm = false },
            title = { Text("¿Vaciar la papelera?", fontWeight = FontWeight.Bold) },
            text = { Text("Los elementos se eliminarán definitivamente y no podrán recuperarse.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.emptyTrash()
                        showEmptyTrashConfirm = false
                        showTrashDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) { Text("Eliminar definitivamente") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEmptyTrashConfirm = false }) { Text("Cancelar") }
            }
        )
    }

    pendingAutomaticRestore?.let { fileName ->
        AlertDialog(
            onDismissRequest = { pendingAutomaticRestore = null },
            title = { Text("¿Restaurar esta copia?", fontWeight = FontWeight.Bold) },
            text = { Text("El estado actual se guardará primero en otra copia automática.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.restoreAutomaticBackup(fileName)
                        pendingAutomaticRestore = null
                        showBackupsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) { Text("Restaurar") }
            },
            dismissButton = {
                OutlinedButton(onClick = { pendingAutomaticRestore = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun TrashRow(
    title: String,
    subtitle: String,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
    restoreDescription: String = "Restaurar"
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onRestore) {
                Icon(Icons.Default.Restore, contentDescription = restoreDescription, tint = IndigoPrimary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar definitivamente", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private fun formatBackupDate(timestamp: Long): String =
    SimpleDateFormat("dd MMM yyyy · HH:mm", Locale.getDefault()).format(Date(timestamp))

@Composable
private fun NotificationSettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    leadText: String? = null,
    onLeadClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        ) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(
                subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (leadText != null) {
            SettingsValueChip(
                text = leadText,
                modifier = Modifier.then(
                    if (onLeadClick == null) Modifier else Modifier.clickable(onClick = onLeadClick)
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = settingsSwitchColors()
        )
    }
}

@Composable
private fun NotificationDivider() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
    )
}

private fun nextReminderValue(current: Int, options: List<Int>): Int {
    val index = options.indexOf(current)
    return options[(index + 1) % options.size]
}

private fun formatReminderLead(minutes: Int): String = when {
    minutes < 60 -> "$minutes min"
    minutes % (24 * 60) == 0 -> {
        val days = minutes / (24 * 60)
        if (days == 1) "1 día" else "$days días"
    }
    minutes % 60 == 0 -> {
        val hours = minutes / 60
        if (hours == 1) "1 h" else "$hours h"
    }
    else -> "$minutes min"
}

@Composable
private fun SettingsValueChip(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .widthIn(min = 64.dp)
            .defaultMinSize(minHeight = 36.dp),
        shape = RoundedCornerShape(18.dp),
        color = IndigoPrimary.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, IndigoPrimary.copy(alpha = 0.28f))
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = IndigoPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun settingsSwitchColors() = SwitchDefaults.colors(
    checkedThumbColor = Color.White,
    checkedTrackColor = IndigoPrimary,
    checkedBorderColor = IndigoPrimary,
    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
    uncheckedBorderColor = MaterialTheme.colorScheme.outline
)

private fun periodSummary(period: AcademicPeriodEntity): String {
    val start = DateTimeUtils.parseDate(period.startDate)
    val end = DateTimeUtils.parseDate(period.endDate)
    val durationDays = if (start != null && end != null) {
        ChronoUnit.DAYS.between(start, end) + 1
    } else {
        null
    }
    val dates = "${DateTimeUtils.formatDate(period.startDate)} – " +
            DateTimeUtils.formatDate(period.endDate)
    return if (durationDays == null) dates else "$dates · $durationDays días"
}
