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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mocas.R
import com.mocas.data.local.SchoolEventType
import com.mocas.data.local.SchoolEventWithSubject
import com.mocas.util.DateTimeUtils
import java.time.LocalDate

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

    val sideStripColor = when {
        isCompleted -> colors.tertiary
        isOverdue || daysLeft <= 0L -> colors.error
        daysLeft <= 3L -> colors.secondary
        else -> colors.primary
    }
    val urgencyColor = if (daysLeft <= 1) colors.error else colors.secondary

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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        // Box en vez de Row+IntrinsicSize.Min: el Box se dimensiona por el contenido
        // (el Row de abajo) y la franja lateral copia ese tamaño con matchParentSize(),
        // así la altura de la card siempre se ajusta al contenido real.
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(vertical = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(6.dp)
                        .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                        .background(sideStripColor)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 22.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Checkbox (Rounded Square)
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isCompleted) sideStripColor else Color.Transparent)
                        .border(
                            width = 2.dp,
                            color = if (isCompleted) sideStripColor else colors.outline,
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

                    // ── Fila 1: SOLO dos elementos → nunca se aplasta ──
                    // tag de tipo a la izquierda, botón editar a la derecha.
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = colors.errorContainer
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = colors.onErrorContainer,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = typeLabel,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.onErrorContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Surface(
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { onEditClick() },
                            shape = RoundedCornerShape(8.dp),
                            color = colors.surfaceContainerHigh
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Outlined.Edit,
                                    contentDescription = null,
                                    tint = colors.onSurface,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ── Fila 2: solo el tag de materia, con todo el ancho. ──
                    if (eventWithSubject.subject != null) {
                        FlowRow(modifier = Modifier.fillMaxWidth()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = colors.surfaceContainerHigh,
                                border = BorderStroke(1.dp, colors.outlineVariant)
                            ) {
                                Text(
                                    text = eventWithSubject.subject.name,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Título Principal
                    Text(
                        text = eventWithSubject.event.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )

                    // Subtítulo (Nombre de la materia)
                    if (eventWithSubject.subject != null) {
                        Text(
                            text = eventWithSubject.subject.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                        )
                    }

                    // ── Fila inferior: Fecha, Hora y — junto al reloj — cuánto falta.
                    // FlowRow por si en pantallas angostas no cabe todo en una línea. ──
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = colors.outline,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = DateTimeUtils.formatDate(eventWithSubject.event.startDate),
                                fontSize = 12.sp,
                                color = colors.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Cuánto falta, justo después de la hora (junto al reloj)
                            if (!isCompleted && daysLeft >= 0) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = colors.outline,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (daysLeft == 0L) "hoy" else "en $daysLeft días",
                                    color = urgencyColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}