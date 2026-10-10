package com.mocas.ui.components.events

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mocas.R
import com.mocas.data.local.SchoolEventType
import com.mocas.data.local.SchoolEventWithSubject
import com.mocas.ui.components.parseColorFromHex
import com.mocas.util.DateTimeUtils
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun CustomEventCard(
    eventWithSubject: SchoolEventWithSubject,
    onToggleCompleted: (Boolean) -> Unit,
    onEditClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    val isCompleted = eventWithSubject.event.isCompleted
    val eventDate = DateTimeUtils.parseDate(eventWithSubject.event.startDate) ?: LocalDate.now()
    val today = LocalDate.now()
    val isOverdue = !isCompleted && eventDate.isBefore(today)
    val daysLeft = DateTimeUtils.daysRemaining(eventWithSubject.event.startDate) ?: 99L

    val accentColor = when {
        isCompleted -> colors.tertiary
        isOverdue || daysLeft <= 0L -> colors.error
        daysLeft <= 3L -> colors.secondary
        else -> colors.primary
    }

    val createdAtMillis = eventWithSubject.event.createdAtMillis
    val dueDateMillis = runCatching {
        LocalDate.parse(eventWithSubject.event.startDate)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
    }.getOrNull() ?: (createdAtMillis + 86400000L)

    val nowMillis = System.currentTimeMillis()
    val totalDuration = (dueDateMillis - createdAtMillis).toFloat()
    val remainingDuration = (dueDateMillis - nowMillis).toFloat()

    val progressFraction = if (isCompleted || remainingDuration <= 0f) {
        0.0f
    } else if (totalDuration > 0f) {
        (remainingDuration / totalDuration).coerceIn(0f, 1f)
    } else {
        0.0f
    }

    val baseSubjectColor = eventWithSubject.subject?.colorHex?.let { parseColorFromHex(it) } ?: colors.primary
    val factor = 0.4f + 0.6f * progressFraction
    val dynamicBarColor = if (isCompleted) {
        colors.tertiary
    } else if (isOverdue || daysLeft <= 0L) {
        colors.error
    } else {
        Color(
            red = (baseSubjectColor.red * factor).coerceIn(0f, 1f),
            green = (baseSubjectColor.green * factor).coerceIn(0f, 1f),
            blue = (baseSubjectColor.blue * factor).coerceIn(0f, 1f),
            alpha = baseSubjectColor.alpha
        )
    }

    val typeLabel = when (eventWithSubject.event.type) {
        SchoolEventType.TAREA -> stringResource(R.string.tipo_tarea)
        SchoolEventType.EXAMEN -> stringResource(R.string.tipo_examen)
        SchoolEventType.EXPOSICION -> stringResource(R.string.tipo_exposicion)
        else -> stringResource(R.string.tipo_otro)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Checkbox (Rounded Square)
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isCompleted) accentColor else Color.Transparent)
                        .border(
                            width = 2.dp,
                            color = if (isCompleted) accentColor else colors.outlineVariant,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onToggleCompleted(!isCompleted) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = colors.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Tags wrap on narrow layouts or when the system font is enlarged.
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FlowRow(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            itemVerticalAlignment = Alignment.CenterVertically
                        ) {
                            // Type Tag
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = colors.errorContainer
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = colors.onErrorContainer,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = typeLabel,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.onErrorContainer
                                    )
                                }
                            }

                            // Subject Tag
                            if (eventWithSubject.subject != null) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = colors.surfaceContainerHigh,
                                    border = BorderStroke(1.dp, colors.outlineVariant)
                                ) {
                                    Text(
                                        text = eventWithSubject.subject.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.onSurface,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { onEditClick() },
                            shape = RoundedCornerShape(10.dp),
                            color = colors.surfaceContainerHigh
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Outlined.Edit,
                                    contentDescription = null,
                                    tint = colors.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Título Principal
                    Text(
                        text = eventWithSubject.event.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )

                    // Descripción adicional si existe
                    if (eventWithSubject.event.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = eventWithSubject.event.description,
                            fontSize = 13.sp,
                            color = colors.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bottom Row: Date and Urgency badge
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        itemVerticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = colors.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = DateTimeUtils.formatDate(eventWithSubject.event.startDate),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.onSurfaceVariant
                            )
                        }

                        if (!isCompleted && daysLeft >= 0) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = accentColor.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (daysLeft == 0L) stringResource(R.string.hoy_exclamacion) else stringResource(R.string.vence_en_dias_formato, daysLeft),
                                        color = accentColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom countdown progress bar across the card (decreases from 100% to 0%, colored with subject color darkening as it approaches due date)
            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = dynamicBarColor,
                trackColor = colors.surfaceContainerHigh
            )
        }
    }
}
