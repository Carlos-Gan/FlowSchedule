package com.mocas.ui.add.subject

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mocas.ui.screens.SlotDraft
import com.mocas.util.DateTimeUtils

@Composable
fun SchedulesCard(
    slotsList: List<SlotDraft>,
    slotConflicts: Map<Int, List<String>>,
    defaultRoom: String,
    onTimeClick: (Int, Boolean) -> Unit,
    onAddSlot: () -> Unit,
    onRemoveSlot: (Int) -> Unit,
    onUpdateSlot: (Int, SlotDraft) -> Unit
) {
    BaseCard("Horarios de Clase") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onAddSlot) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Añadir sesión")
            }
        }

        slotsList.forEachIndexed { index, slotDraft ->
            val conflicts = slotConflicts[index].orEmpty()
            val hasConflict = conflicts.isNotEmpty()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (hasConflict) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = if (hasConflict) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sesión ${index + 1}", fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelLarge)
                        IconButton(onClick = { onRemoveSlot(index) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        }
                    }

                    if (hasConflict) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                conflicts.forEach { conflictMsg ->
                                    Text(conflictMsg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    val dayInitials = listOf(1 to "L", 2 to "M", 3 to "X", 4 to "J", 5 to "V", 6 to "S", 7 to "D")
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        dayInitials.forEach { (dayNum, initial) ->
                            val isSelected = dayNum in slotDraft.selectedDays
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable {
                                        val updatedDays = if (isSelected && slotDraft.selectedDays.size > 1) {
                                            slotDraft.selectedDays - dayNum
                                        } else {
                                            slotDraft.selectedDays + dayNum
                                        }
                                        onUpdateSlot(index, slotDraft.copy(selectedDays = updatedDays))
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initial,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SessionTimeField(
                            label = "Inicio", time = slotDraft.startTime,
                            onClick = { onTimeClick(index, true) }, modifier = Modifier.weight(1f)
                        )
                        Text("-", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        SessionTimeField(
                            label = "Fin", time = slotDraft.endTime,
                            onClick = { onTimeClick(index, false) }, modifier = Modifier.weight(1f),
                            isError = !DateTimeUtils.endIsAfterStart(slotDraft.startTime, slotDraft.endTime)
                        )
                    }
                }
            }
        }
    }
}