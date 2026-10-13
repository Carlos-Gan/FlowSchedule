package com.mocas.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CoPresent
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mocas.R
import com.mocas.data.local.SchoolEventType
import com.mocas.ui.components.EmptyStateCard
import com.mocas.ui.components.events.CustomEventCard
import com.mocas.ui.viewmodel.ScheduleViewModel
import com.mocas.util.DateTimeUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
private fun ProgressBadge(modifier: Modifier, progressFraction: Float) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    ) {
        Text(
            text = "${(progressFraction * 100).toInt()}%",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun ProgressSummary(modifier: Modifier, completedCount: Int, totalCount: Int) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.progreso_del_semestre),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.actividades_completadas_formato, completedCount, totalCount),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    viewModel: ScheduleViewModel,
    modifier: Modifier = Modifier
) {
    val fontScale = LocalDensity.current.fontScale
    val largeFontScale = fontScale >= 1.3f
    val allEvents by viewModel.allEventsWithSubject.collectAsStateWithLifecycle()
    val activeFilter by viewModel.eventFilter.collectAsStateWithLifecycle()

    var isRefreshing by remember { mutableStateOf(false) }
    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val statusFilter by viewModel.eventStatusFilter.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val refreshState = rememberPullToRefreshState()

    BackHandler(fabMenuExpanded) { fabMenuExpanded = false }

    val onRefresh: () -> Unit = {
        scope.launch {
            isRefreshing = true
            delay(1500.milliseconds) // Simulación de carga
            isRefreshing = false
        }
    }

    val filteredList = remember(allEvents, activeFilter, statusFilter) {
        var list = if (activeFilter == "TODOS") allEvents
        else allEvents.filter { it.event.type.name == activeFilter }

        list = when (statusFilter) {
            "PENDIENTES" -> list.filter { !it.event.isCompleted }
            "COMPLETADAS" -> list.filter { it.event.isCompleted }
            else -> list
        }
        list
    }

    val pendingEvents = filteredList.filter { !it.event.isCompleted }
        .sortedBy { it.event.startDate }
    val completedEvents = filteredList.filter { it.event.isCompleted }
        .sortedByDescending { it.event.startDate }

    val urgentExams = remember(allEvents) {
        allEvents.filter { it.event.type == SchoolEventType.EXAMEN && !it.event.isCompleted }
            .sortedBy { it.event.startDate }
            .take(3)
    }

    val totalCount = allEvents.size
    val completedCount = allEvents.count { it.event.isCompleted }
    val progressFraction = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            state = refreshState,
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tarjeta de Progreso
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ProgressSummary(Modifier.weight(1f), completedCount, totalCount)
                                ProgressBadge(Modifier, progressFraction)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            LinearWavyProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.outlineVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    text = stringResource(R.string.inicio_ciclo_escolar),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.weight(1f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = stringResource(R.string.meta_100),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.End,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Button Groups for Event Types (below Semester Progress)
                item {
                    val filterOptions = listOf(
                        "TODOS" to (Icons.Default.FilterList to stringResource(R.string.filtro_todos)),
                        SchoolEventType.TAREA.name to (Icons.AutoMirrored.Filled.Assignment to stringResource(R.string.filtro_tareas)),
                        SchoolEventType.EXAMEN.name to (Icons.Default.Quiz to stringResource(R.string.filtro_examenes)),
                        SchoolEventType.EXPOSICION.name to (Icons.Default.CoPresent to stringResource(R.string.filtro_expos)),
                        SchoolEventType.EVENTO_ESCOLAR.name to (Icons.Default.School to stringResource(R.string.filtro_eventos))
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        Row(modifier = Modifier.padding(4.dp)) {
                            filterOptions.forEach { (key, option) ->
                                val (icon, label) = option
                                val isSelected = activeFilter == key
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else androidx.compose.ui.graphics.Color.Transparent
                                        )
                                        .clickable { viewModel.setEventFilter(key) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Carrusel de Exámenes Urgentes
                if (urgentExams.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.examenes_proximos).uppercase(),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 1.2.sp),
                                color = MaterialTheme.colorScheme.error,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = stringResource(R.string.agendado_badge_formato, urgentExams.size),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.error,
                                    maxLines = 1
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))

                        HorizontalMultiBrowseCarousel(
                            state = rememberCarouselState { urgentExams.size },
                            preferredItemWidth = 140.dp * fontScale.coerceAtLeast(1f),
                            itemSpacing = 8.dp,
                            modifier = Modifier.fillMaxWidth().height(105.dp * fontScale.coerceAtLeast(1f))
                        ) { index ->
                            val exam = urgentExams[index]
                            Card(
                                onClick = { viewModel.openAddEvent(exam) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                modifier = Modifier.fillMaxSize(),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    if (exam.subject != null) {
                                        Text(
                                            text = exam.subject.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = exam.event.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = DateTimeUtils.formatRelativeDate(exam.event.startDate),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }

                if (filteredList.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = stringResource(R.string.no_hay_actividades),
                            message = stringResource(R.string.mensaje_sin_actividades),
                            icon = Icons.AutoMirrored.Filled.Assignment,
                            actionButtonText = stringResource(R.string.nueva_actividad_boton),
                            onActionClick = { viewModel.openAddEvent() }
                        )
                    }
                } else {
                    if (pendingEvents.isNotEmpty()) {
                        item {
                            if (largeFontScale) Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(stringResource(R.string.pendientes), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.onSurface)
                                Text(stringResource(R.string.ordenado_por_fecha), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                            } else Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.pendientes),
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(R.string.ordenado_por_fecha),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                        items(pendingEvents, key = { it.event.id }) { eventWithSubject ->
                            CustomEventCard(
                                eventWithSubject = eventWithSubject,
                                onToggleCompleted = { viewModel.toggleEventCompleted(eventWithSubject.event.id, it) },
                                onEditClick = { viewModel.openAddEvent(eventWithSubject) }
                            )
                        }
                    }

                    if (pendingEvents.isNotEmpty() && completedEvents.isNotEmpty()) {
                        item {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 12.dp))
                        }
                    }

                    if (completedEvents.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.completadas),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                        items(completedEvents, key = { it.event.id }) { eventWithSubject ->
                            CustomEventCard(
                                eventWithSubject = eventWithSubject,
                                onToggleCompleted = { viewModel.toggleEventCompleted(eventWithSubject.event.id, it) },
                                onEditClick = { viewModel.openAddEvent(eventWithSubject) }
                            )
                        }
                    }
                }
            }
        }

        // --- M3 FAB MENU FOR ACTIVITY STATUS FILTERING ---
        FloatingActionButtonMenu(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 20.dp),
            expanded = fabMenuExpanded,
            button = {
                ToggleFloatingActionButton(
                    checked = fabMenuExpanded,
                    onCheckedChange = { fabMenuExpanded = !fabMenuExpanded }
                ) {
                    val imageVector = if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Default.FilterList
                    Icon(
                        painter = rememberVectorPainter(imageVector),
                        contentDescription = "Estado",
                        modifier = Modifier.animateIcon({ checkedProgress })
                    )
                }
            }
        ) {
            val statusItems = listOf(
                Triple("TODOS", stringResource(R.string.filtro_todas),
                    Icons.AutoMirrored.Filled.Assignment
                ),
                Triple("PENDIENTES", stringResource(R.string.filtro_incompletas), Icons.Default.Schedule),
                Triple("COMPLETADAS", stringResource(R.string.filtro_completadas), Icons.Default.Check)
            )

            statusItems.forEach { (key, label, icon) ->
                FloatingActionButtonMenuItem(
                    onClick = {
                        viewModel.setEventStatusFilter(key)
                        fabMenuExpanded = false
                    },
                    icon = { Icon(icon, contentDescription = null) },
                    text = {
                        Text(
                            text = label,
                            fontWeight = if (statusFilter == key) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }
    }
}
