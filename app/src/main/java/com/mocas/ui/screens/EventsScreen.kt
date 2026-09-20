package com.mocas.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CoPresent
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mocas.R
import com.mocas.data.local.SchoolEventType
import com.mocas.data.local.SchoolEventWithSubject
import com.mocas.ui.components.EmptyStateCard
import com.mocas.ui.components.events.CustomEventCard
import com.mocas.ui.viewmodel.ScheduleViewModel
import com.mocas.util.DateTimeUtils
import java.time.LocalDate
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    viewModel: ScheduleViewModel,
    modifier: Modifier = Modifier
) {
    val allEvents by viewModel.allEventsWithSubject.collectAsStateWithLifecycle()
    val activeFilter by viewModel.eventFilter.collectAsStateWithLifecycle()

    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val refreshState = rememberPullToRefreshState()

    val onRefresh: () -> Unit = {
        scope.launch {
            isRefreshing = true
            delay(1500.milliseconds) // Simulación de carga
            isRefreshing = false
        }
    }

    val filterOptions = listOf(
        "TODOS" to (null to stringResource(R.string.filtro_todos)),
        SchoolEventType.TAREA.name to (Icons.Default.Assignment to stringResource(R.string.filtro_tareas)),
        SchoolEventType.EXAMEN.name to (Icons.Default.Quiz to stringResource(R.string.filtro_examenes)),
        SchoolEventType.EXPOSICION.name to (Icons.Default.CoPresent to stringResource(R.string.filtro_expos)),
        SchoolEventType.EVENTO_ESCOLAR.name to (Icons.Default.School to stringResource(R.string.filtro_eventos))
    )

    val filteredList = remember(allEvents, activeFilter) {
        if (activeFilter == "TODOS") allEvents
        else allEvents.filter { it.event.type.name == activeFilter }
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

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        state = refreshState,
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tarjeta de Progreso con WavyProgressIndicator (Novedad M3 1.5 alpha)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
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

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                            ) {
                                Text(
                                    text = "${(progressFraction * 100).toInt()}%",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Usamos LinearWavyProgressIndicator (Novedad M3 1.5 alpha)
                        LinearWavyProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }

            // Carrusel de Exámenes Urgentes (Novedad M3 1.4/1.5 alpha)
            if (urgentExams.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.examenes_proximos),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    HorizontalMultiBrowseCarousel(
                        state = rememberCarouselState { urgentExams.size },
                        preferredItemWidth = 140.dp,
                        itemSpacing = 8.dp,
                        modifier = Modifier.fillMaxWidth().height(95.dp)
                    ) { index ->
                        val exam = urgentExams[index]
                        Card(
                            onClick = { viewModel.openAddEvent(exam) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = exam.event.title,
                                    style = MaterialTheme.typography.labelLargeEmphasized,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (exam.subject != null) {
                                    Text(
                                        text = exam.subject.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = DateTimeUtils.formatRelativeDate(exam.event.startDate),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }

            // Filtros usando SegmentedButton con Iconos
            item {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    filterOptions.forEachIndexed { index, (key, option) ->
                        val (icon, label) = option
                        val isSelected = activeFilter == key

                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = filterOptions.size),
                            onClick = { viewModel.setEventFilter(key) },
                            selected = isSelected,
                            label = {
                                if (icon == null) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                } else {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = MaterialTheme.colorScheme.primary,
                                activeContentColor = MaterialTheme.colorScheme.onPrimary,
                                inactiveContainerColor = MaterialTheme.colorScheme.surface,
                                inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                activeBorderColor = MaterialTheme.colorScheme.primary,
                                inactiveBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                    }
                }
            }

            if (filteredList.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = stringResource(R.string.no_hay_actividades),
                        message = stringResource(R.string.mensaje_sin_actividades),
                        icon = Icons.Default.Assignment,
                        actionButtonText = stringResource(R.string.nueva_actividad_boton),
                        onActionClick = { viewModel.openAddEvent() }
                    )
                }
            } else {
                if (pendingEvents.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.pendientes),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
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
}
