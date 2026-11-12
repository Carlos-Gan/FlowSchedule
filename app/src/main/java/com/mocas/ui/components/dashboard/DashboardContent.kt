package com.mocas.ui.components.dashboard

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mocas.R
import com.mocas.data.local.SchoolEventWithSubject
import com.mocas.ui.model.DailyClassStats
import com.mocas.ui.model.NextClassInfo

// Ancho máximo del contenido: en pantallas grandes (tablet/desktop/foldable
// abierto) evita que el texto y las tarjetas se estiren de borde a borde.
private val MAX_CONTENT_WIDTH: Dp = 1200.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    greeting: String,
    nextClass: NextClassInfo?,
    dueTodayEvents: List<SchoolEventWithSubject>,
    classStats: DailyClassStats,
    isVacation: Boolean,
    windowSizeClass: WindowSizeClass,
    onNextClassClick: () -> Unit,
    onViewAllEvents: () -> Unit,
    onToggleEventCompleted: (SchoolEventWithSubject, Boolean) -> Unit,
    onEventClick: (SchoolEventWithSubject) -> Unit,
    onAddTaskClick: () -> Unit,
    onAddSubjectClick: () -> Unit,
    average: String? = null,
    remainingDays: Int? = null,
    academicPeriodName: String? = null,
    onOpenAcademicProgress: () -> Unit = {},
    onOpenAcademicPeriods: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }
    BackHandler(fabMenuExpanded) { fabMenuExpanded = false }

    val widthClass = windowSizeClass.widthSizeClass
    val isCompact = widthClass == WindowWidthSizeClass.Compact
    val isExpanded = widthClass == WindowWidthSizeClass.Expanded

    val horizontalPadding = if (isExpanded) 32.dp else 20.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = MAX_CONTENT_WIDTH)
                .fillMaxWidth(),
            contentPadding = PaddingValues(
                start = horizontalPadding,
                end = horizontalPadding,
                top = 24.dp,
                bottom = 104.dp
            ),
            verticalArrangement = Arrangement.spacedBy(if (isExpanded) 20.dp else 13.dp)
        ) {
            // Greeting Section
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (dueTodayEvents.isEmpty())
                            stringResource(R.string.todo_al_dia_mensaje)
                        else
                            stringResource(R.string.tareas_hoy_conteo_formato, dueTodayEvents.size),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                DashboardAcademicSummaryCard(
                    average = average,
                    remainingDays = remainingDays,
                    periodName = academicPeriodName,
                    onAverageClick = onOpenAcademicProgress,
                    onPeriodClick = onOpenAcademicPeriods
                )
            }

            if (isExpanded) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        if (nextClass != null) {
                            NextClassCard(nextClass = nextClass, onClick = onNextClassClick)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(
                                modifier = Modifier.weight(0.82f),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.progreso_del_dia),
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                DailyFocusCard(
                                    stats = classStats,
                                    isVacation = isVacation,
                                    modifier = Modifier.height(150.dp),
                                    showTitle = false
                                )
                            }
                            TasksDueTodaySection(
                                dueTodayEvents = dueTodayEvents,
                                onViewAllEvents = onViewAllEvents,
                                onToggleEventCompleted = onToggleEventCompleted,
                                onEventClick = onEventClick,
                                modifier = Modifier.weight(1.18f),
                                emptyStateHeight = 150.dp
                            )
                        }
                    }
                }
            } else if (isCompact) {
                if (nextClass != null) {
                    item {
                        NextClassCard(
                            nextClass = nextClass,
                            onClick = onNextClassClick
                        )
                    }
                }

                item {
                    DailyFocusCard(
                        stats = classStats,
                        isVacation = isVacation
                    )
                }
            } else {
                if (nextClass != null) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                NextClassCard(
                                    nextClass = nextClass,
                                    onClick = onNextClassClick
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                DailyFocusCard(
                                    stats = classStats,
                                    isVacation = isVacation
                                )
                            }
                        }
                    }
                } else {
                    // Sin clase siguiente: DailyFocusCard ocupa
                    // el ancho en vez de dejar medio espacio vacío al lado.
                    item {
                        DailyFocusCard(
                            stats = classStats,
                            isVacation = isVacation
                        )
                    }
                }
            }

            if (!isExpanded) {
                item {
                    TasksDueTodaySection(
                        dueTodayEvents = dueTodayEvents,
                        onViewAllEvents = onViewAllEvents,
                        onToggleEventCompleted = onToggleEventCompleted,
                        onEventClick = onEventClick
                    )
                }
            }
        }

        FloatingActionButtonMenu(
            modifier = Modifier.align(Alignment.BottomEnd),
            expanded = fabMenuExpanded,
            button = {
                ToggleFloatingActionButton(
                    checked = fabMenuExpanded,
                    onCheckedChange = { fabMenuExpanded = it }
                ) {
                    val icon = if (checkedProgress > 0.5f) Icons.Default.Close else Icons.Default.Add
                    Icon(
                        painter = rememberVectorPainter(icon),
                        contentDescription = stringResource(R.string.añadir_tarea),
                        modifier = Modifier.animateIcon({ checkedProgress })
                    )
                }
            }
        ) {
            FloatingActionButtonMenuItem(
                onClick = {
                    fabMenuExpanded = false
                    onAddTaskClick()
                },
                icon = { Icon(Icons.Default.AddTask, contentDescription = null) },
                text = { Text(stringResource(R.string.añadir_tarea)) }
            )
            FloatingActionButtonMenuItem(
                onClick = {
                    fabMenuExpanded = false
                    onAddSubjectClick()
                },
                icon = { Icon(Icons.Default.LibraryAdd, contentDescription = null) },
                text = { Text(stringResource(R.string.añadir_materia)) }
            )
        }
    }
}