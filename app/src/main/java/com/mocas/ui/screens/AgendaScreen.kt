package com.mocas.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mocas.data.local.ClassExceptionEntity
import com.mocas.data.local.ClassExceptionType
import com.mocas.data.local.SchoolEventWithSubject
import com.mocas.data.local.SubjectWithSlots
import com.mocas.data.repository.CalendarSyncHelper
import com.mocas.ui.components.ClassScheduleCard
import com.mocas.ui.components.EmptyStateCard
import com.mocas.ui.components.EventItemCard
import com.mocas.ui.components.NextClassHeroCard
import com.mocas.ui.components.PeriodGradesCard
import com.mocas.ui.components.agenda.AgendaSectionHeader
import com.mocas.ui.components.agenda.DaySelector
import com.mocas.ui.model.BottomNavTab
import com.mocas.ui.theme.AccentEmerald
import com.mocas.ui.theme.TurquoiseSecondary
import com.mocas.ui.util.lazyItemKey
import com.mocas.ui.util.showCalendarResult
import com.mocas.ui.viewmodel.ScheduleViewModel
import com.mocas.util.DateTimeUtils

private val AgendaDays = listOf(
    1 to "Lun", 2 to "Mar", 3 to "Mié", 4 to "Jue",
    5 to "Vie", 6 to "Sáb", 7 to "Dom"
)

@Composable
fun AgendaScreen(viewModel: ScheduleViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    val dayClasses by viewModel.todayClasses.collectAsStateWithLifecycle()
    val nextClass by viewModel.nextClassInfo.collectAsStateWithLifecycle()
    val allEvents by viewModel.allEventsWithSubject.collectAsStateWithLifecycle()
    val selectedDay by viewModel.selectedDayOfWeek.collectAsStateWithLifecycle()
    val subjects by viewModel.subjectsWithSlots.collectAsStateWithLifecycle()
    val periods by viewModel.academicPeriods.collectAsStateWithLifecycle()
    val gradeCategories by viewModel.gradeCategories.collectAsStateWithLifecycle()
    val gradeItems by viewModel.gradeItems.collectAsStateWithLifecycle()
    val gradeUnits by viewModel.gradeUnits.collectAsStateWithLifecycle()
    val gradeUnitCategoryWeights by viewModel.gradeUnitCategoryWeights.collectAsStateWithLifecycle()
    val classExceptions by viewModel.classExceptions.collectAsStateWithLifecycle()
    val today = ScheduleViewModel.getTodayDateString()

    val pendingEvents = remember(allEvents) {
        allEvents.filterNot { it.event.isCompleted }.take(3)
    }
    val dueTodayEvents = remember(allEvents, today) {
        allEvents
            .filter { !it.event.isCompleted && it.event.endDate == today }
            .sortedWith(compareBy<SchoolEventWithSubject> { it.event.startTime == null }.thenBy { it.event.startTime })
    }
    val todayExceptions = remember(classExceptions, today) {
        classExceptions.filter { it.date == today }
    }
    val nextEvent = pendingEvents.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("agenda_scroll_column"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        if (nextClass != null) {
            item {
                NextClassHeroCard(
                    nextClassInfo = nextClass,
                    onCardClick = {
                        nextClass?.subject?.id?.let(viewModel::openSubjectDetail)
                    }
                )
            }
        } else {
            item {
                HomeSummaryCard(
                    name = settings.userName,
                    classCount = dayClasses.size,
                    pendingCount = pendingEvents.size
                )
            }
            item { FreeDayBanner() }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        eyebrow = if (nextEvent == null) "ACTIVIDADES" else "PRÓXIMA ACTIVIDAD",
                        title = nextEvent?.event?.title ?: "Agregar actividad",
                        supportingText = nextEvent?.let(::formatEventPreview)
                            ?: "Tarea, examen o evento",
                        icon = Icons.AutoMirrored.Filled.EventNote,
                        accent = Color(0xFFFF6B00),
                        onClick = {
                            if (nextEvent == null) viewModel.openAddEvent()
                            else viewModel.openAddEvent(nextEvent)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    if (settings.aiFeaturesEnabled) {
                        QuickActionCard(
                            eyebrow = "IMPORTAR",
                            title = "Escanear horario",
                            supportingText = "Foto o PDF con IA",
                            icon = Icons.Default.CameraAlt,
                            accent = TurquoiseSecondary,
                            onClick = viewModel::openImportSchedule,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        item {
            TodayFocusCard(
                dueTodayEvents = dueTodayEvents,
                todayExceptions = todayExceptions,
                subjects = subjects,
                nextClassSubjectId = nextClass?.subject?.id,
                onCompleteEvent = { eventId -> viewModel.toggleEventCompleted(eventId, true) },
                onPostponeEvent = viewModel::postponeEventOneDay,
                onOpenEvent = { eventWithSubject -> viewModel.openAddEvent(eventWithSubject) },
                onOpenSubject = viewModel::openSubjectDetail,
                onOpenEvents = { viewModel.setTab(BottomNavTab.EVENTOS) }
            )
        }

        item {
            PeriodGradesCard(
                periods = periods,
                subjects = subjects,
                categories = gradeCategories,
                items = gradeItems,
                units = gradeUnits,
                unitCategoryWeights = gradeUnitCategoryWeights,
                onSubjectClick = viewModel::openSubjectDetail
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AgendaSectionHeader(
                    title = "Clases del día",
                    supportingText = when (dayClasses.size) {
                        0 -> "Sin clases"
                        1 -> "1 clase"
                        else -> "${dayClasses.size} clases"
                    }
                )
                DaySelector(
                    days = AgendaDays,
                    selectedDay = selectedDay,
                    actualToday = ScheduleViewModel.getCurrentDayOfWeekNumber(),
                    onDaySelected = viewModel::setSelectedDayOfWeek
                )
            }
        }

        if (dayClasses.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No tienes clases este día",
                    message = "Puedes agregar una materia o elegir otro día.",
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    actionButtonText = "Agregar materia",
                    onActionClick = { viewModel.openAddSubject() }
                )
            }
        } else {
            items(
                dayClasses,
                key = { lazyItemKey("agenda-class", it.slot.id) }
            ) { classItem ->
                ClassScheduleCard(
                    dayClassItem = classItem,
                    onClick = { viewModel.openSubjectDetail(classItem.subject.id) },
                    onSyncCalendarClick = {
                        showCalendarResult(
                            context,
                            CalendarSyncHelper.addClassToPhoneCalendar(
                                context, classItem.subject, classItem.slot
                            )
                        )
                    }
                )
            }
        }

        item {
            AgendaSectionHeader(
                title = "Próximas actividades",
                supportingText = if (pendingEvents.isEmpty()) "Nada pendiente"
                else "${pendingEvents.size} por completar",
                actionText = "Ver todas",
                onActionClick = { viewModel.setTab(BottomNavTab.EVENTOS) }
            )
        }

        if (pendingEvents.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "Todo está al día",
                    message = "Tus próximas tareas y eventos aparecerán aquí.",
                    icon = Icons.AutoMirrored.Filled.EventNote,
                    actionButtonText = "Agregar actividad",
                    onActionClick = { viewModel.openAddEvent() }
                )
            }
        } else {
            items(
                pendingEvents,
                key = { lazyItemKey("agenda-event", it.event.id) }
            ) { eventWithSubject ->
                EventItemCard(
                    eventWithSubject = eventWithSubject,
                    onToggleCompleted = { completed ->
                        viewModel.toggleEventCompleted(eventWithSubject.event.id, completed)
                    },
                    onClick = { viewModel.openAddEvent(eventWithSubject) },
                    onCalendarSyncClick = {
                        showCalendarResult(
                            context,
                            CalendarSyncHelper.addEventToPhoneCalendar(
                                context,
                                eventWithSubject.event,
                                eventWithSubject.subject?.name
                            )
                        )
                    },
                    onToggleSubtask = { subtaskId, completed ->
                        viewModel.toggleSubtaskCompleted(eventWithSubject.event.id, subtaskId, completed)
                    }
                )
            }
        }
    }
}

@Composable
private fun TodayFocusCard(
    dueTodayEvents: List<SchoolEventWithSubject>,
    todayExceptions: List<ClassExceptionEntity>,
    subjects: List<SubjectWithSlots>,
    nextClassSubjectId: Long?,
    onCompleteEvent: (Long) -> Unit,
    onPostponeEvent: (Long) -> Unit,
    onOpenEvent: (SchoolEventWithSubject) -> Unit,
    onOpenSubject: (Long) -> Unit,
    onOpenEvents: () -> Unit
) {
    val firstDue = dueTodayEvents.firstOrNull()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Centro de Hoy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    Text(
                        text = buildTodaySubtitle(dueTodayEvents.size, todayExceptions.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (firstDue != null) {
                TodayEventRow(
                    item = firstDue,
                    onComplete = { onCompleteEvent(firstDue.event.id) },
                    onPostpone = { onPostponeEvent(firstDue.event.id) },
                    onOpen = { onOpenEvent(firstDue) }
                )
            } else {
                TodayEmptyLine(
                    icon = Icons.Default.CheckCircle,
                    title = "Sin actividades que venzan hoy",
                    detail = "Lo urgente aparecerá aquí para resolverlo rápido."
                )
            }

            if (todayExceptions.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                todayExceptions.take(2).forEach { exception ->
                    TodayExceptionRow(
                        exception = exception,
                        subjects = subjects,
                        onOpenSubject = onOpenSubject
                    )
                }
                if (todayExceptions.size > 2) {
                    Text(
                        text = "+${todayExceptions.size - 2} cambios más hoy",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenEvents,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.AutoMirrored.Filled.EventNote, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pendientes")
                }
                Button(
                    onClick = { nextClassSubjectId?.let(onOpenSubject) ?: onOpenEvents() },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (nextClassSubjectId == null) "Abrir" else "Materia")
                }
            }
        }
    }
}

@Composable
private fun TodayEventRow(
    item: SchoolEventWithSubject,
    onComplete: () -> Unit,
    onPostpone: () -> Unit,
    onOpen: () -> Unit
) {
    val accent = Color(0xFFFF6B00)
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = accent.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.18f))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Vence hoy",
                        style = MaterialTheme.typography.labelSmall,
                        color = accent,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = item.event.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.subject?.name ?: formatEventPreview(item),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onComplete,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = accent)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Completar")
                }
                OutlinedButton(onClick = onPostpone, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.MoreTime, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Mañana")
                }
                OutlinedButton(onClick = onOpen, shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Abrir actividad", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun TodayExceptionRow(
    exception: ClassExceptionEntity,
    subjects: List<SubjectWithSlots>,
    onOpenSubject: (Long) -> Unit
) {
    val subject = subjects.firstOrNull { it.subject.id == exception.subjectId }?.subject
    val accent = if (exception.type == ClassExceptionType.CANCELED) Color(0xFFEF4444) else Color(0xFF0EA5E9)
    Surface(
        onClick = { subject?.id?.let(onOpenSubject) },
        shape = RoundedCornerShape(14.dp),
        color = accent.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.18f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (exception.type == ClassExceptionType.CANCELED) Icons.Default.Warning else Icons.Default.EditCalendar,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (exception.type == ClassExceptionType.CANCELED) "Clase cancelada" else "Clase modificada",
                    style = MaterialTheme.typography.labelSmall,
                    color = accent,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = subject?.name ?: "Materia",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val detail = exceptionDetail(exception)
                if (detail.isNotBlank()) {
                    Text(
                        text = detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayEmptyLine(icon: ImageVector, title: String, detail: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(19.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun HomeSummaryCard(name: String, classCount: Int, pendingCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "👋", fontSize = 17.sp)
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ScheduleViewModel.getGreetingText(name),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = ScheduleViewModel.getFormattedTodayHeading(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryMetric(
                    icon = Icons.Default.School,
                    value = "$classCount ${if (classCount == 1) "clase" else "clases"}",
                    label = "programadas hoy",
                    accent = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                SummaryMetric(
                    icon = Icons.AutoMirrored.Filled.EventNote,
                    value = "$pendingCount ${if (pendingCount == 1) "pendiente" else "pendientes"}",
                    label = "actividad próxima",
                    accent = Color(0xFFFF6B00),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SummaryMetric(
    icon: ImageVector,
    value: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = accent.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.16f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun FreeDayBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = AccentEmerald.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, AccentEmerald.copy(alpha = 0.22f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "🎉", fontSize = 18.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Sin clases pendientes hoy — ¡Tu horario está al día! Disfruta tu tiempo libre.",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = AccentEmerald
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    eyebrow: String,
    title: String,
    supportingText: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(142.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.07f)),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(shape = CircleShape, color = accent.copy(alpha = 0.12f)) {
                    Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(19.dp))
                    }
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = eyebrow,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = accent
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun formatEventPreview(event: SchoolEventWithSubject): String {
    val date = DateTimeUtils.formatDate(event.event.startDate)
    val time = event.event.startTime?.let {
        DateTimeUtils.formatTime(it, use24Hour = false)
    }
    return if (time == null) date else "$date · $time"
}

private fun buildTodaySubtitle(dueCount: Int, exceptionCount: Int): String {
    val dueText = when (dueCount) {
        0 -> "sin vencimientos"
        1 -> "1 vencimiento"
        else -> "$dueCount vencimientos"
    }
    val exceptionText = when (exceptionCount) {
        0 -> "sin cambios de clase"
        1 -> "1 cambio de clase"
        else -> "$exceptionCount cambios de clase"
    }
    return "$dueText · $exceptionText"
}

private fun exceptionDetail(exception: ClassExceptionEntity): String = when (exception.type) {
    ClassExceptionType.CANCELED -> exception.note.ifBlank { "No se impartirá esta sesión." }
    ClassExceptionType.MODIFIED -> buildList {
        if (!exception.newStartTime.isNullOrBlank() && !exception.newEndTime.isNullOrBlank()) {
            add("${exception.newStartTime} - ${exception.newEndTime}")
        }
        if (!exception.newRoom.isNullOrBlank()) add("Salón ${exception.newRoom}")
        if (exception.note.isNotBlank()) add(exception.note)
    }.joinToString(" · ")
}
