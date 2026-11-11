package com.mocas.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.mocas.R
import com.mocas.data.local.ClassExceptionEntity
import com.mocas.data.local.SchoolEventWithSubject
import com.mocas.data.local.SubjectWithSlots
import com.mocas.data.repository.ScheduleAvailability
import com.mocas.data.repository.SharedAvailability
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.core.net.toUri

@Composable
fun ScheduleComparisonScreen(
    subjects: List<SubjectWithSlots>,
    events: List<SchoolEventWithSubject>,
    classExceptions: List<ClassExceptionEntity>,
    initialWeek: LocalDate,
    incoming: SharedAvailability? = null,
    onConsumeIncoming: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales[0]
    val invalidQrText = rememberUpdatedState(stringResource(R.string.compare_invalid_qr))
    val wrongWeekText = rememberUpdatedState(stringResource(R.string.compare_wrong_week))
    val addedText = rememberUpdatedState(stringResource(R.string.compare_added))
    val scanFailedText = rememberUpdatedState(stringResource(R.string.compare_scan_failed))
    val shareLinkTitle = stringResource(R.string.compare_share_link)
    var week by remember { mutableStateOf(initialWeek) }
    val ownAvailability = remember(subjects, events, classExceptions, week) {
        ScheduleAvailability.fromSchedule(subjects, week, events, classExceptions)
    }
    val participants = remember(week) { mutableStateListOf(ownAvailability) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var feedback by remember { mutableStateOf<String?>(null) }
    val common = ScheduleAvailability.commonFree(participants)
    val dayNames = listOf(
        stringResource(R.string.compare_monday), stringResource(R.string.compare_tuesday),
        stringResource(R.string.compare_wednesday), stringResource(R.string.compare_thursday),
        stringResource(R.string.compare_friday), stringResource(R.string.compare_saturday),
        stringResource(R.string.compare_sunday)
    )
    val weekLabel = week.format(DateTimeFormatter.ofPattern("d MMM", locale)) + " – " +
        week.plusDays(6).format(DateTimeFormatter.ofPattern("d MMM yyyy", locale))

    fun acceptScanned(value: String?) {
        val received = value?.let { ScheduleAvailability.fromUri(it.toUri()) }
        when {
            received == null -> feedback = invalidQrText.value
            received.weekStart != week -> feedback = wrongWeekText.value
            else -> {
                participants += received
                feedback = addedText.value
            }
        }
    }

    LaunchedEffect(ownAvailability) {
        if (participants.isNotEmpty()) participants[0] = ownAvailability
    }
    LaunchedEffect(incoming) {
        if (incoming != null) {
            if (incoming.weekStart == week && participants.none { it === incoming }) participants += incoming
            onConsumeIncoming()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cerrar), tint = MaterialTheme.colorScheme.onSurface)
            }
            Text(
                stringResource(R.string.compare_section_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.compare_title), color = MaterialTheme.colorScheme.onSurface, fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.compare_privacy), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
        }

        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { week = week.minusWeeks(1); qrBitmap = null }) { Text("‹", color = MaterialTheme.colorScheme.onSurface, fontSize = 26.sp) }
                    Text(weekLabel, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                    TextButton(onClick = { week = week.plusWeeks(1); qrBitmap = null }) { Text("›", color = MaterialTheme.colorScheme.onSurface, fontSize = 26.sp) }
                }
                TextButton(onClick = { week = LocalDate.now().with(java.time.DayOfWeek.MONDAY); qrBitmap = null }) {
                    Text(stringResource(R.string.compare_this_week), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }

        Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Groups, null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(24.dp))
                Text(stringResource(R.string.compare_people_label), color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f), modifier = Modifier.weight(1f).padding(start = 12.dp))
                Text(participants.size.toString(), color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.compare_open_slots), color = MaterialTheme.colorScheme.onSurface, fontSize = 23.sp, fontWeight = FontWeight.Bold)
            AvailabilityGrid(common = common, dayNames = dayNames)
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.compare_ranges_title), color = MaterialTheme.colorScheme.onSurface, fontSize = 23.sp, fontWeight = FontWeight.Bold)
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), shape = RoundedCornerShape(22.dp)) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    dayNames.forEachIndexed { day, name ->
                        val ranges = freeRanges(common, day)
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.Top) {
                            Text(name, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, modifier = Modifier.width(48.dp))
                            Text(
                                ranges.ifEmpty { stringResource(R.string.compare_no_day_slots) },
                                color = if (ranges.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        if (day < dayNames.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(R.string.compare_connect_title), color = MaterialTheme.colorScheme.onSurface, fontSize = 23.sp, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.compare_connect_description), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            Button(
                onClick = {
                    qrBitmap = runCatching { ScheduleAvailability.qrBitmap(ScheduleAvailability.toUri(ownAvailability)) }.getOrNull()
                    feedback = null
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
            ) {
                Icon(Icons.Default.QrCode2, null)
                Text(stringResource(R.string.compare_show_qr), modifier = Modifier.padding(start = 10.dp), fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = {
                    val uri = ScheduleAvailability.toUri(ownAvailability)
                    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, uri.toString())
                    }, shareLinkTitle))
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow, contentColor = MaterialTheme.colorScheme.onSurface)
            ) {
                Icon(Icons.Default.Share, null)
                Text(stringResource(R.string.compare_share_link), modifier = Modifier.padding(start = 10.dp), fontWeight = FontWeight.SemiBold)
            }
            OutlinedButton(
                onClick = {
                    GmsBarcodeScanning.getClient(context).startScan()
                        .addOnSuccessListener { barcode: Barcode -> acceptScanned(barcode.rawValue) }
                        .addOnFailureListener { feedback = scanFailedText.value }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.QrCodeScanner, null, tint = MaterialTheme.colorScheme.primary)
                Text(stringResource(R.string.compare_scan), color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 10.dp), fontWeight = FontWeight.Bold)
            }
            feedback?.let { Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall) }
            qrBitmap?.let { bitmap ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), shape = RoundedCornerShape(20.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Image(bitmap.asImageBitmap(), contentDescription = stringResource(R.string.compare_qr_description), modifier = Modifier.size(240.dp))
                        Text(stringResource(R.string.compare_qr_hint), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    }
                }
            }
        }

        TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 18.dp)) {
            Text(stringResource(R.string.cerrar), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AvailabilityGrid(common: BooleanArray, dayNames: List<String>) {
    val colors = MaterialTheme.colorScheme
    val available = colors.primary
    val partly = colors.tertiaryContainer
    val occupied = colors.surfaceVariant
    Card(colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow), shape = RoundedCornerShape(22.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(Modifier.width(42.dp))
                dayNames.forEach { day ->
                    Text(day, modifier = Modifier.weight(1f), color = colors.onSurfaceVariant, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.width(42.dp)) {
                    (7..22).forEach { hour ->
                        Text("%02d:00".format(hour), modifier = Modifier.height(19.dp), color = colors.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    }
                    Text("23:00", modifier = Modifier.padding(top = 2.dp), color = colors.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                }
                Canvas(modifier = Modifier.weight(1f).height(304.dp)) {
                    val cellWidth = size.width / 7f
                    val cellHeight = size.height / 16f
                    val cellInset = 2.dp.toPx()
                    for (day in 0..6) for (row in 0..15) {
                        val start = day * 48 + 14 + row * 2
                        val freeCount = (if (common[start]) 1 else 0) + (if (common[start + 1]) 1 else 0)
                        val color = when (freeCount) { 2 -> available; 1 -> partly; else -> occupied }
                        drawRoundRect(
                            color,
                            topLeft = androidx.compose.ui.geometry.Offset(day * cellWidth + cellInset, row * cellHeight + cellInset),
                            size = androidx.compose.ui.geometry.Size(cellWidth - cellInset * 2, cellHeight - cellInset * 2),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GridLegend(available, stringResource(R.string.compare_legend_free))
                GridLegend(partly, stringResource(R.string.compare_legend_partial))
                GridLegend(occupied, stringResource(R.string.compare_legend_busy))
            }
            Text(stringResource(R.string.compare_day_bounds), color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun GridLegend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(Modifier.size(10.dp).background(color, RoundedCornerShape(3.dp)))
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
    }
}

private fun freeRanges(common: BooleanArray, day: Int): String {
    val slots = common.indices.filter { ScheduleAvailability.dayIndex(it) == day && it % 48 in 14..45 && common[it] }
    val ranges = mutableListOf<String>()
    var index = 0
    while (index < slots.size) {
        val start = slots[index]
        var end = start
        while (index + 1 < slots.size && slots[index + 1] == end + 1) {
            index++
            end++
        }
        ranges += "${ScheduleAvailability.slotLabel(start)}–${ScheduleAvailability.slotLabel(end + 1)}"
        index++
    }
    return ranges.joinToString(" · ")
}
