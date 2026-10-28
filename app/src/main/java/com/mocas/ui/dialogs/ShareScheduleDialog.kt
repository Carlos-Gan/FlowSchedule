package com.mocas.ui.dialogs

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mocas.R
import com.mocas.data.local.SchoolEventWithSubject
import com.mocas.data.local.SubjectWithSlots
import com.mocas.data.repository.CalendarSyncHelper
import com.mocas.data.repository.ScheduleShareExporter
import com.mocas.data.repository.SharePrivacyOptions
import java.time.LocalDate

@Composable
fun ShareScheduleDialog(
    context: Context,
    subjects: List<SubjectWithSlots>,
    events: List<SchoolEventWithSubject>,
    weekStart: LocalDate,
    onShareFlow: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var showProfessor by remember { mutableStateOf(true) }
    var showRoom by remember { mutableStateOf(true) }
    var includePrivate by remember { mutableStateOf(false) }
    val options = SharePrivacyOptions(showProfessor, showRoom, includePrivate)
    fun share(intent: android.content.Intent, title: String) {
        context.startActivity(android.content.Intent.createChooser(intent, title))
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.compartir_horario_titulo)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    stringResource(R.string.compartir_horario_subtitulo),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(stringResource(R.string.privacidad), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold)
                        PrivacySwitch(stringResource(R.string.profesor), showProfessor) { showProfessor = it }
                        PrivacySwitch(stringResource(R.string.salon), showRoom) { showRoom = it }
                        PrivacySwitch(stringResource(R.string.actividades_personales), includePrivate) { includePrivate = it }
                    }
                }

                ShareOptionButton(
                    icon = Icons.Default.Image,
                    title = stringResource(R.string.opcion_imagen_titulo),
                    detail = stringResource(R.string.opcion_imagen_detalle),
                    accent = Color(0xFF8B5CF6),
                    onClick = {
                        val bitmap = ScheduleShareExporter.createBitmap(subjects, events, weekStart, options)
                        share(ScheduleShareExporter.sharePng(context, bitmap), context.getString(R.string.compartir_imagen_desc))
                    }
                )
                ShareOptionButton(
                    icon = Icons.Default.PictureAsPdf,
                    title = stringResource(R.string.opcion_pdf_titulo),
                    detail = stringResource(R.string.opcion_pdf_detalle),
                    accent = Color(0xFFEF4444),
                    onClick = {
                        val bitmap = ScheduleShareExporter.createBitmap(subjects, events, weekStart, options)
                        share(ScheduleShareExporter.sharePdf(context, bitmap), context.getString(R.string.compartir_pdf_desc))
                    }
                )
                ShareOptionButton(
                    icon = Icons.Default.Share,
                    title = stringResource(R.string.opcion_ics_titulo),
                    detail = stringResource(R.string.opcion_ics_detalle),
                    accent = Color(0xFF0EA5E9),
                    onClick = {
                        share(
                            CalendarSyncHelper.createIcsShareIntent(
                                context = context,
                                subjectsWithSlots = subjects,
                                showProfessor = showProfessor,
                                showRoom = showRoom
                            ),
                            context.getString(R.string.compartir_calendario_desc)
                        )
                    }
                )
                if (onShareFlow != null) {
                    ShareOptionButton(
                        icon = Icons.Default.FileUpload,
                        title = stringResource(R.string.opcion_flow_titulo),
                        detail = stringResource(R.string.opcion_flow_detalle),
                        accent = Color(0xFF10B981),
                        onClick = onShareFlow
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cerrar)) } }
    )
}

@Composable
private fun PrivacySwitch(label: String, checked: Boolean, onChanged: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChanged)
    }
}

@Composable
private fun ShareOptionButton(
    icon: ImageVector,
    title: String,
    detail: String,
    accent: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = accent)
    ) {
        Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f)) {
            Box(modifier = Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
            Text(title, fontWeight = FontWeight.ExtraBold)
            Text(detail, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f))
        }
    }
}
