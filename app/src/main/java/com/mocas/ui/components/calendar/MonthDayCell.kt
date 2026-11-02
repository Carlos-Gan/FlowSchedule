package com.mocas.ui.components.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MonthDayCell(
    dayItem: com.mocas.ui.model.CalendarDayItem,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                when {
                    dayItem.isSelected -> MaterialTheme.colorScheme.primary
                    dayItem.isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else -> Color.Transparent
                }
            )
            .clickable(enabled = dayItem.isCurrentMonth) { onClick() }
            .testTag("calendar_day_${dayItem.dateString}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (dayItem.dayNumber > 0) "${dayItem.dayNumber}" else "",
                fontSize = 13.sp,
                fontWeight = if (dayItem.isSelected || dayItem.isToday) FontWeight.ExtraBold else FontWeight.Medium,
                color = when {
                    !dayItem.isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    dayItem.isSelected -> MaterialTheme.colorScheme.onPrimary
                    dayItem.isToday -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )

            // Activity Indicator Dots
            if (dayItem.isCurrentMonth) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(5.dp)
                ) {
                    if (dayItem.hasExams) {
                        Box(
                            modifier = Modifier
                                .size(3.5.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444))
                        )
                    }
                    if (dayItem.hasTasks) {
                        Box(
                            modifier = Modifier
                                .size(3.5.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF8B5CF6))
                        )
                    }
                    if (dayItem.hasClasses) {
                        Box(
                            modifier = Modifier
                                .size(3.5.dp)
                                .clip(CircleShape)
                                .background(
                                    if (dayItem.isSelected) MaterialTheme.colorScheme.onPrimary else Color(
                                        0xFF3B82F6
                                    )
                                )
                        )
                    }
                    if (dayItem.hasEvents) {
                        Box(
                            modifier = Modifier
                                .size(3.5.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF59E0B))
                        )
                    }
                    if (dayItem.isHoliday) {
                        Box(
                            modifier = Modifier
                                .size(3.5.dp)
                                .clip(CircleShape)
                                .background(
                                    if (dayItem.isSelected) MaterialTheme.colorScheme.onPrimary else Color(
                                        0xFF10B981
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}