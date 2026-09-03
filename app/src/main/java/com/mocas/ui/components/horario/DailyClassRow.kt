package com.mocas.ui.components.horario

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mocas.ui.components.parseColorFromHex

@Composable
fun DailyClassRow(
    classItem: com.mocas.ui.model.DayClassItem,
    onClick: () -> Unit
) {
    val subjectColor = parseColorFromHex(classItem.subject.colorHex)
    val isNow = classItem.isLiveNow

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Time Column
        Column(
            modifier = Modifier
                .width(52.dp)
                .padding(top = 10.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = classItem.slot.startTime,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                ),
                color = if (isNow) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = classItem.slot.endTime,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Card Block
        Card(
            modifier = Modifier
                .weight(1f)
                .clickable { onClick() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isNow) subjectColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceContainerLowest
            ),
            border = BorderStroke(
                width = if (isNow) 1.5.dp else 1.dp,
                color = if (isNow) subjectColor.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isNow) 3.dp else 0.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                // Colored Side Strip
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(5.dp)
                        .background(subjectColor)
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = classItem.subject.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp,
                                letterSpacing = (-0.3).sp,
                                lineHeight = 20.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(end = 12.dp),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        val room = classItem.slot.room.ifBlank { classItem.subject.defaultRoom }
                        val professor = classItem.subject.professor
                        val metadata = listOfNotNull(
                            if (room.isNotBlank()) room else null,
                            if (professor.isNotBlank()) professor else null
                        ).joinToString("  •  ")

                        if (metadata.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = metadata,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Red Dot Indicator
                    if (isNow) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .size(8.dp)
                                .background(Color.Red, CircleShape)
                        )
                    }
                }
            }
        }
    }
}
