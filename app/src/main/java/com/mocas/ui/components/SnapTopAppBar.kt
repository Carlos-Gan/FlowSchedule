package com.mocas.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.mocas.data.local.OrganizationTag
import com.mocas.data.local.SchoolEventWithSubject
import com.mocas.data.local.SubjectWithSlots
import com.mocas.ui.theme.IndigoPrimary

@Composable
fun SnapTopAppBar(
    title: String,
    subtitle: String? = null,
    searchQuery: String = "",
    onQueryChange: ((String) -> Unit)? = null,
    isSearchActive: Boolean = false,
    onSearchActiveChange: ((Boolean) -> Unit)? = null,
    subjects: List<SubjectWithSlots> = emptyList(),
    events: List<SchoolEventWithSubject> = emptyList(),
    onSubjectClick: ((Long) -> Unit)? = null,
    onEventClick: ((SchoolEventWithSubject) -> Unit)? = null,
    onSearchClick: (() -> Unit)? = null,
    onScanClick: (() -> Unit)? = null,
    onAddClick: (() -> Unit)? = null
) {
    val isBrandHeader = title == "SnapMySchedule"
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            focusRequester.requestFocus()
        }
    }

    val normalizedQuery by remember(searchQuery) { derivedStateOf { searchQuery.trim() } }

    val filteredSubjects by remember(subjects, normalizedQuery) {
        derivedStateOf {
            if (normalizedQuery.isEmpty()) emptyList()
            else subjects.filter { item ->
                listOf(
                    item.subject.name,
                    item.subject.code,
                    item.subject.professor,
                    item.subject.defaultRoom
                )
                    .any { it.contains(normalizedQuery, ignoreCase = true) }
            }
        }
    }

    val filteredEvents by remember(events, normalizedQuery) {
        derivedStateOf {
            if (normalizedQuery.isEmpty()) emptyList()
            else events.filter { item ->
                listOf(
                    item.event.title,
                    item.event.description,
                    item.event.location,
                    item.subject?.name.orEmpty()
                ).any { it.contains(normalizedQuery, ignoreCase = true) }
            }
        }
    }

    val showDropdown by remember(normalizedQuery, isSearchActive) {
        derivedStateOf { isSearchActive && normalizedQuery.isNotEmpty() }
    }

    var barHeightPx by remember { mutableStateOf(0) }
    var barWidthPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current



    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned {
                barHeightPx = it.size.height
                barWidthPx = it.size.width
            },
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp

    ) {
        AnimatedContent(
            targetState = isSearchActive,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "TopAppBarSearchAnimation",
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) { searchActive ->
            if (searchActive) {
                BackHandler {
                    onSearchActiveChange?.invoke(false)
                    focusManager.clearFocus()
                }
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { newValue ->
                                onQueryChange?.invoke(newValue)
                            },
                            placeholder = {
                                Text(
                                    "Buscar materias o actividades...",
                                    fontSize = 14.sp
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                            shape = RoundedCornerShape(16.dp),
                            leadingIcon = {
                                IconButton(onClick = {
                                    onSearchActiveChange?.invoke(false)
                                    focusManager.clearFocus()
                                }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Cerrar búsqueda",
                                        tint = IndigoPrimary
                                    )
                                }
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = {
                                        onQueryChange?.invoke("")
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Limpiar texto",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IndigoPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .focusRequester(focusRequester)
                        )
                    }

                    // Menú Flotante de Resultados sobre la pantalla
                    if (showDropdown) {
                        Popup(
                            alignment = Alignment.TopStart,
                            offset = IntOffset(0, barHeightPx),
                            properties = PopupProperties(focusable = false)
                        ) {
                            Card(
                                modifier = Modifier
                                    .width(with(density) { barWidthPx.toDp() })
                                    .padding(horizontal = 20.dp, vertical = 5.dp)
                                    .heightIn(max = 350.dp),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                LazyColumn(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (filteredSubjects.isNotEmpty()) {
                                        item {
                                            Text(
                                                text = "Materias (${filteredSubjects.size})",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(
                                                    horizontal = 4.dp,
                                                    vertical = 4.dp
                                                )
                                            )
                                        }
                                        items(
                                            filteredSubjects,
                                            key = { "subject-${it.subject.id}" }) { item ->
                                            val subtitle = remember(item) {
                                                listOf(
                                                    OrganizationTag.fromStored(item.subject.organizationTag).displayName,
                                                    item.subject.code,
                                                    item.subject.professor
                                                ).filter { it.isNotBlank() }.joinToString(" · ")
                                            }
                                            SearchResultRow(
                                                title = item.subject.name,
                                                subtitle = subtitle,
                                                isImportant = item.subject.isImportant,
                                                isSubject = true,
                                                onClick = {
                                                    onSubjectClick?.invoke(item.subject.id)
                                                    onSearchActiveChange?.invoke(false)
                                                    onQueryChange?.invoke("")
                                                    focusManager.clearFocus()
                                                }
                                            )
                                        }
                                    }

                                    if (filteredEvents.isNotEmpty()) {
                                        item {
                                            Text(
                                                text = "Actividades (${filteredEvents.size})",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(
                                                    horizontal = 4.dp,
                                                    vertical = 4.dp
                                                )
                                            )
                                        }
                                        items(
                                            filteredEvents,
                                            key = { "event-${it.event.id}" }) { item ->
                                            val subtitle = remember(item) {
                                                listOf(
                                                    OrganizationTag.fromStored(item.event.organizationTag).displayName,
                                                    item.event.startDate,
                                                    item.subject?.name.orEmpty()
                                                ).filter { it.isNotBlank() }.joinToString(" · ")
                                            }
                                            SearchResultRow(
                                                title = item.event.title,
                                                subtitle = subtitle,
                                                isImportant = item.event.isImportant,
                                                isSubject = false,
                                                onClick = {
                                                    onEventClick?.invoke(item)
                                                    onSearchActiveChange?.invoke(false)
                                                    onQueryChange?.invoke("")
                                                    focusManager.clearFocus()
                                                }
                                            )
                                        }
                                    }

                                    if (filteredSubjects.isEmpty() && filteredEvents.isEmpty()) {
                                        item {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(24.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Icon(
                                                        imageVector = Icons.Default.SearchOff,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.outline,
                                                        modifier = Modifier.size(36.dp)
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        text = "No se encontraron resultados",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = if (isBrandHeader) 23.sp else 20.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = if (isBrandHeader) IndigoPrimary else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (subtitle != null) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    IconButton(onClick = {
                        onSearchActiveChange?.invoke(true)
                        onSearchClick?.invoke()
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Buscar materias y actividades",
                            tint = IndigoPrimary
                        )
                    }

                    if (onScanClick != null) {
                        OutlinedButton(
                            onClick = onScanClick,
                            modifier = Modifier.testTag("top_bar_scan_button"),
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, IndigoPrimary.copy(alpha = 0.28f)),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CameraAlt,
                                contentDescription = "Escanear horario con foto",
                                tint = IndigoPrimary,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Escanear",
                                color = IndigoPrimary,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (onAddClick != null) {
                        if (onScanClick != null) Spacer(modifier = Modifier.width(6.dp))

                        FilledIconButton(
                            onClick = onAddClick,
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("top_bar_add_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Agregar",
                                modifier = Modifier.size(21.dp)
                            )

                        }
                    }
                }
            }
        }
    }
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
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isSubject) Icons.Default.School else Icons.Default.Event,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (isImportant) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Importante",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}