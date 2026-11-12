package com.mocas.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mocas.R
import com.mocas.ui.components.settings.TrashRow
import com.mocas.ui.viewmodel.ScheduleViewModel
import com.mocas.util.DateTimeUtils

@Composable
fun TrashDialog(
    viewModel: ScheduleViewModel,
    onDismiss: () -> Unit
) {
    val deletedSubjects by viewModel.deletedSubjects.collectAsStateWithLifecycle()
    val deletedEvents by viewModel.deletedEvents.collectAsStateWithLifecycle()
    val canceledClasses by viewModel.canceledClassExceptions.collectAsStateWithLifecycle()
    var showEmptyConfirm by remember { mutableStateOf(false) }

    val isEmpty = deletedSubjects.isEmpty() && deletedEvents.isEmpty() && canceledClasses.isEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.papelera), fontWeight = FontWeight.Bold)
                if (!isEmpty) {
                    IconButton(onClick = { showEmptyConfirm = true }) {
                        Icon(
                            Icons.Default.DeleteSweep,
                            contentDescription = stringResource(R.string.vaciar_papelera),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isEmpty) {
                    item {
                        Text(
                            stringResource(R.string.la_papelera_esta_vacia),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                items(canceledClasses, key = { "exc_${it.first.id}" }) { (exc, subName) ->
                    val formattedDate = DateTimeUtils.formatDate(exc.date, true)
                    TrashRow(
                        title = subName,
                        subtitle = "${stringResource(R.string.clase_cancelada)} · $formattedDate",
                        onRestore = { viewModel.deleteClassException(exc.id) },
                        onDelete = { viewModel.permanentlyDeleteCanceledExceptionFromTrash(exc.id) }
                    )
                }
                items(deletedSubjects) { sub ->
                    TrashRow(
                        title = sub.name,
                        subtitle = stringResource(R.string.materia),
                        onRestore = { viewModel.restoreDeletedSubject(sub.id) },
                        onDelete = { viewModel.permanentlyDeleteSubject(sub.id) }
                    )
                }
                items(deletedEvents) { ev ->
                    TrashRow(
                        title = ev.title,
                        subtitle = stringResource(R.string.actividad),
                        onRestore = { viewModel.restoreDeletedEvent(ev.id) },
                        onDelete = { viewModel.permanentlyDeleteEvent(ev.id) }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text(stringResource(R.string.cerrar)) }
        }
    )

    if (showEmptyConfirm) {
        AlertDialog(
            onDismissRequest = { showEmptyConfirm = false },
            title = {
                Text(
                    stringResource(R.string.confirmar_vaciar_papelera),
                    fontWeight = FontWeight.Bold
                )
            },
            text = { Text(stringResource(R.string.accion_eliminar_permanente)) },
            confirmButton = {
                Button(
                    onClick = { viewModel.emptyTrash(); showEmptyConfirm = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.eliminar_todo)) }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyConfirm = false }) {
                    Text(stringResource(R.string.cancelar))
                }
            }
        )
    }
}