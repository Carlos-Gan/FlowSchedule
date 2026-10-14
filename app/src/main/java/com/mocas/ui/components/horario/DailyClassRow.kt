package com.mocas.ui.components.horario

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
            .fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Time Column
        Column(
            modifier = Modifier
                .widthIn(min = 60.dp)
                .padding(top = 10.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = classItem.slot.startTime,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                ),
                maxLines = 1,
                softWrap = false,
                color = if (isNow) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = classItem.slot.endTime,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                maxLines = 1,
                softWrap = false,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // A slim subject-color marker keeps the schedule row clear without
        // turning the whole class into a pill-shaped card.
        Row(
            modifier = Modifier
                .weight(1f)
                .height(IntrinsicSize.Min)
                .clickable { onClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(subjectColor)
            )

            Box(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier
                        .padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
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
                        room.ifBlank { null },
                        professor.ifBlank { null }
                    ).joinToString("  •  ")

                    if (metadata.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = metadata,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

            }
        }
    }
}
