package com.moca.snapmyschedule.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moca.snapmyschedule.data.model.ClassSession
import com.moca.snapmyschedule.data.model.WeekDay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassDetailsScreen(
    sessions: List<ClassSession>,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by rememberSaveable {
        mutableStateOf(false)
    }

    val firstSession = sessions.firstOrNull()

    val scheduleBlocks = remember(sessions) {
        sessions
            .groupBy { session ->
                DetailBlockKey(
                    startTime = session.startTime,
                    endTime = session.endTime,
                    room = session.room
                )
            }
            .map { (key, groupedSessions) ->
                DetailBlock(
                    days = groupedSessions
                        .map { it.day }
                        .distinct()
                        .sortedBy { it.ordinal },
                    startTime = key.startTime,
                    endTime = key.endTime,
                    room = key.room
                )
            }
            .sortedWith(
                compareBy<DetailBlock> {
                    it.days.minOfOrNull { day ->
                        day.ordinal
                    } ?: Int.MAX_VALUE
                }.thenBy {
                    it.startTime
                }
            )
    }

    BackHandler {
        onBack()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text("Detalles de la materia")
                },
                navigationIcon = {
                    TextButton(
                        onClick = onBack
                    ) {
                        Text("Volver")
                    }
                }
            )
        }
    ) { innerPadding ->

        if (firstSession == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No se encontró la materia.",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = androidx.compose.foundation.layout
                    .PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Column(
                        verticalArrangement =
                            Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = firstSession.subjectName,
                            style =
                                MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        if (
                            firstSession.subjectCode.isNotBlank()
                        ) {
                            Text(
                                text = firstSession.subjectCode,
                                style =
                                    MaterialTheme.typography.titleMedium,
                                color =
                                    MaterialTheme.colorScheme.primary
                            )
                        }

                        if (firstSession.teacher.isNotBlank()) {
                            Text(
                                text = firstSession.teacher,
                                style =
                                    MaterialTheme.typography.bodyLarge,
                                color =
                                    MaterialTheme.colorScheme
                                        .onSurfaceVariant
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "Horarios",
                        style =
                            MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(
                    items = scheduleBlocks,
                    key = { block ->
                        "${block.days}-${block.startTime}-${block.endTime}-${block.room}"
                    }
                ) { block ->
                    ScheduleBlockCard(
                        block = block
                    )
                }

                item {
                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Editar materia")
                    }
                }

                item {
                    Button(
                        onClick = {
                            showDeleteDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor =
                                MaterialTheme.colorScheme.error,
                            contentColor =
                                MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Eliminar materia")
                    }
                }
            }
        }
    }

    if (showDeleteDialog && firstSession != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            title = {
                Text("Eliminar materia")
            },
            text = {
                Text(
                    "Se eliminarán todos los días y horarios de " +
                            "\"${firstSession.subjectName}\"."
                )
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text("Cancelar")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text(
                        text = "Eliminar",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )
    }
}

@Composable
private fun ScheduleBlockCard(
    block: DetailBlock,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = block.days.joinToString(
                    separator = ", "
                ) { day ->
                    day.displayName
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${block.startTime}–${block.endTime}",
                style = MaterialTheme.typography.bodyLarge
            )

            if (block.room.isNotBlank()) {
                Text(
                    text = "Salón: ${block.room}",
                    style = MaterialTheme.typography.bodyMedium,
                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private data class DetailBlockKey(
    val startTime: String,
    val endTime: String,
    val room: String
)

private data class DetailBlock(
    val days: List<WeekDay>,
    val startTime: String,
    val endTime: String,
    val room: String
)