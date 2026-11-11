package com.mocas.ui.components.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.ModifierLocal
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mocas.R
import com.mocas.data.local.EventPriority
import com.mocas.data.local.SchoolEventType
import com.mocas.data.local.SchoolEventWithSubject

@Composable
fun TaskItemRow(
    eventWithSubject: SchoolEventWithSubject,
    onToggleCompleted: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    val isDone = eventWithSubject.event.isCompleted
    val isHighPriority = eventWithSubject.event.priority == EventPriority.HIGH

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isDone) MaterialTheme.colorScheme.secondaryContainer
                            else Color.Transparent
                        )
                        .border(
                            width = 2.dp,
                            color = if (isDone) MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { onToggleCompleted(!isDone) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        val eventType = eventWithSubject.event.type
                        val typeRes = when (eventType) {
                            SchoolEventType.TAREA -> R.string.tipo_tarea
                            SchoolEventType.EXAMEN -> R.string.tipo_examen
                            SchoolEventType.EXPOSICION -> R.string.tipo_exposicion
                            SchoolEventType.EVENTO_ESCOLAR -> R.string.tipo_evento_escolar
                            SchoolEventType.REUNION -> R.string.tipo_reunion
                            SchoolEventType.VACACIONES -> R.string.legend_vacaciones
                            else -> R.string.tipo_otro
                        }
                        val typeColor = when (eventType) {
                            SchoolEventType.TAREA -> Color(0xFF8B5CF6)
                            SchoolEventType.EXAMEN -> Color(0xFFEF4444)
                            SchoolEventType.EXPOSICION -> Color(0xFF06B6D4)
                            SchoolEventType.EVENTO_ESCOLAR -> Color(0xFFF59E0B)
                            SchoolEventType.REUNION -> Color(0xFF3B82F6)
                            SchoolEventType.VACACIONES -> Color(0xFF10B981)
                            else -> Color(0xFF64748B)
                        }
                        val alpha = if (isDone) 0.45f else 1f

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = typeColor.copy(alpha = 0.14f * alpha)
                        ) {
                            Text(
                                text = stringResource(typeRes).uppercase(),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = typeColor.copy(alpha = alpha)
                            )
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = eventWithSubject.event.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                textDecoration = if (isDone) {
                                    TextDecoration.LineThrough
                                } else {
                                    null
                                }
                            ),
                            color = if (isDone) {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = eventWithSubject.subject?.name ?: stringResource(R.string.sin_materia),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = if (isDone) 0.6f else 1f
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))
                }
            }

            if (isHighPriority && !isDone) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = stringResource(R.string.prioridad_alta),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            } else if (isDone) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Text(
                        text = stringResource(R.string.listo),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
