package com.mocas.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mocas.data.local.OrganizationTag
import com.mocas.data.local.SchoolEventWithSubject
import com.mocas.data.local.SubjectWithSlots
import com.mocas.ui.util.capitalizeFirstLetter

private const val FILTER_ALL = "TODOS"
private const val FILTER_IMPORTANT = "IMPORTANTES"

@Composable
fun GlobalSearchDialog(
    subjects: List<SubjectWithSlots>,
    events: List<SchoolEventWithSubject>,
    onDismiss: () -> Unit,
    onSubjectClick: (Long) -> Unit,
    onEventClick: (SchoolEventWithSubject) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(FILTER_ALL) }
    val normalizedQuery = query.trim()
    val filteredSubjects = remember(subjects, normalizedQuery, filter) {
        subjects.filter { item ->
            matchesOrganization(item.subject.organizationTag, item.subject.isImportant, filter) &&
                listOf(item.subject.name, item.subject.code, item.subject.professor, item.subject.defaultRoom)
                    .any { it.contains(normalizedQuery, ignoreCase = true) }
        }
    }
    val filteredEvents = remember(events, normalizedQuery, filter) {
        events.filter { item ->
            matchesOrganization(item.event.organizationTag, item.event.isImportant, filter) &&
                listOf(
                    item.event.title,
                    item.event.description,
                    item.event.location,
                    item.subject?.name.orEmpty()
                ).any { it.contains(normalizedQuery, ignoreCase = true) }
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Buscar", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Cerrar búsqueda") }
                }
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = capitalizeFirstLetter(it) },
                    label = { Text("Materias o actividades") },
                    placeholder = { Text("Nombre, profesor, salón...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth()
                )
                LazyRow(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { SearchFilterChip("Todos", FILTER_ALL, filter) { filter = it } }
                    items(OrganizationTag.entries) { tag ->
                        SearchFilterChip(tag.displayName, tag.name, filter) { filter = it }
                    }
                    item { SearchFilterChip("Importante", FILTER_IMPORTANT, filter) { filter = it } }
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (filteredSubjects.isNotEmpty()) {
                        item { SearchSectionTitle("Materias", filteredSubjects.size) }
                        items(filteredSubjects, key = { "subject-${it.subject.id}" }) { item ->
                            SearchResultRow(
                                title = item.subject.name,
                                subtitle = listOf(
                                    OrganizationTag.fromStored(item.subject.organizationTag).displayName,
                                    item.subject.code,
                                    item.subject.professor
                                ).filter { it.isNotBlank() }.joinToString(" · "),
                                isImportant = item.subject.isImportant,
                                isSubject = true,
                                onClick = { onSubjectClick(item.subject.id) }
                            )
                        }
                    }
                    if (filteredEvents.isNotEmpty()) {
                        item { SearchSectionTitle("Actividades", filteredEvents.size) }
                        items(filteredEvents, key = { "event-${it.event.id}" }) { item ->
                            SearchResultRow(
                                title = item.event.title,
                                subtitle = listOf(
                                    OrganizationTag.fromStored(item.event.organizationTag).displayName,
                                    item.event.startDate,
                                    item.subject?.name.orEmpty()
                                ).filter { it.isNotBlank() }.joinToString(" · "),
                                isImportant = item.event.isImportant,
                                isSubject = false,
                                onClick = { onEventClick(item) }
                            )
                        }
                    }
                    if (filteredSubjects.isEmpty() && filteredEvents.isEmpty()) {
                        item {
                            Text(
                                text = "No encontramos resultados con esos filtros.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 36.dp)
                            )
                        }
                    }
                    item { Spacer(Modifier.height(20.dp)) }
                }
            }
        }
    }
}

private fun matchesOrganization(tag: String, important: Boolean, filter: String): Boolean = when (filter) {
    FILTER_ALL -> true
    FILTER_IMPORTANT -> important
    else -> tag == filter
}

@Composable
private fun SearchFilterChip(label: String, value: String, selected: String, onSelected: (String) -> Unit) {
    FilterChip(selected = selected == value, onClick = { onSelected(value) }, label = { Text(label) })
}

@Composable
private fun SearchSectionTitle(title: String, count: Int) {
    Text("$title ($count)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
}

@Composable
private fun SearchResultRow(
    title: String,
    subtitle: String,
    isImportant: Boolean,
    isSubject: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isSubject) Icons.Default.School else Icons.Default.Event,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (isImportant) Icon(Icons.Default.Star, contentDescription = "Importante", tint = MaterialTheme.colorScheme.tertiary)
        }
    }
}
