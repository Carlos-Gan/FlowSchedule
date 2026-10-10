package com.mocas.ui.add.event

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mocas.R
import com.mocas.data.local.EventPriority
import com.mocas.data.local.OrganizationTag
import com.mocas.data.local.RecurrenceType
import com.mocas.data.local.SchoolEventEntity
import com.mocas.data.local.SchoolEventType
import com.mocas.data.local.SchoolEventWithSubject
import com.mocas.data.local.SubjectWithSlots
import com.mocas.data.local.SubtaskEntity
import com.mocas.ui.components.CalendarDateField
import com.mocas.ui.components.ClockTimeField
import com.mocas.ui.components.parseColorFromHex
import com.mocas.ui.util.capitalizeFirstLetter
import com.mocas.util.DateTimeUtils

@Composable
fun AddEventDialog(
    editingEvent: SchoolEventWithSubject?,
    defaultSubjectId: Long? = null,
    defaultDate: String? = null,
    defaultType: SchoolEventType? = null,
    defaultTitle: String? = null,
    subjects: List<SubjectWithSlots>,
    onDismiss: () -> Unit,
    onSave: (SchoolEventEntity, List<SubtaskEntity>) -> Unit
) {
    val initialEvent = editingEvent?.event
    var title by remember { mutableStateOf(initialEvent?.title ?: defaultTitle.orEmpty()) }
    var selectedType by remember {
        mutableStateOf(initialEvent?.type ?: defaultType ?: SchoolEventType.TAREA)
    }
    var selectedSubjectId by remember { mutableStateOf(initialEvent?.subjectId ?: defaultSubjectId) }
    var eventDate by remember {
        mutableStateOf(initialEvent?.startDate ?: defaultDate ?: DateTimeUtils.todayString())
    }
    var isAllDay by remember {
        mutableStateOf(initialEvent?.isAllDay ?: (defaultType == SchoolEventType.VACACIONES))
    }

    val calculatedDefaultTime = remember(selectedSubjectId, eventDate, subjects) {
        if (initialEvent != null) return@remember initialEvent.startTime ?: "09:00"

        val date = DateTimeUtils.parseDate(eventDate)
        val dow = date?.dayOfWeek?.value ?: DateTimeUtils.currentDayOfWeek()
        
        val classStart = subjects.find { it.subject.id == selectedSubjectId }
            ?.slots?.find { it.dayOfWeek == dow }
            ?.startTime

        classStart ?: "09:00"
    }

    var eventTime by remember { mutableStateOf(calculatedDefaultTime) }

    LaunchedEffect(calculatedDefaultTime) {
        if (initialEvent == null) {
            eventTime = calculatedDefaultTime
        }
    }

    var location by remember { mutableStateOf(initialEvent?.location ?: "") }
    var description by remember { mutableStateOf(initialEvent?.description ?: "") }
    var organizationTag by remember { mutableStateOf(initialEvent?.organizationTag ?: OrganizationTag.UNIVERSIDAD.name) }
    var isImportant by remember { mutableStateOf(initialEvent?.isImportant ?: false) }

    var isSubjectDropdownExpanded by remember { mutableStateOf(false) }

    val isValid = title.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (initialEvent == null) stringResource(R.string.nueva_actividad_titulo) else stringResource(R.string.editar_actividad_titulo),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.volver_desc))
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (isValid) {
                                val finalStartTime = if (isAllDay) null else eventTime
                                val finalEndTime = if (isAllDay) null else DateTimeUtils.getEndTime(eventTime)

                                onSave(
                                    SchoolEventEntity(
                                        id = initialEvent?.id ?: 0L,
                                        title = title.trim(),
                                        type = selectedType,
                                        subjectId = selectedSubjectId,
                                        startDate = eventDate,
                                        endDate = eventDate,
                                        startTime = finalStartTime,
                                        endTime = finalEndTime,
                                        isAllDay = isAllDay,
                                        location = location.trim(),
                                        description = description.trim(),
                                        organizationTag = organizationTag,
                                        isImportant = isImportant,
                                        priority = if (isImportant) EventPriority.HIGH else (initialEvent?.priority ?: EventPriority.MEDIUM),
                                        recurrenceType = initialEvent?.recurrenceType ?: RecurrenceType.NONE,
                                        isCompleted = initialEvent?.isCompleted ?: false,
                                        syncCalendar = initialEvent?.syncCalendar ?: false,
                                        createdAtMillis = initialEvent?.createdAtMillis ?: System.currentTimeMillis(),
                                        updatedAtMillis = initialEvent?.updatedAtMillis ?: System.currentTimeMillis()
                                    ),
                                    emptyList()
                                )
                            }
                        },
                        enabled = isValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Text(stringResource(R.string.guardar), fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. DETALLES PRINCIPALES
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Header Row with Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.detalles_principales),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 1.2.sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = stringResource(R.string.requerido),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Title Field
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row {
                            Text(
                                text = stringResource(R.string.que_hacer_asterisco).substringBefore("*"),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "*",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = capitalizeFirstLetter(it) },
                            placeholder = { Text(stringResource(R.string.ej_proyecto_final), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Category
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.categoria_label),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            val types = listOf(
                                SchoolEventType.TAREA to stringResource(R.string.filtro_tareas),
                                SchoolEventType.EXAMEN to stringResource(R.string.filtro_examenes),
                                SchoolEventType.EXPOSICION to stringResource(R.string.filtro_expos),
                                SchoolEventType.EVENTO_ESCOLAR to stringResource(R.string.filtro_eventos)
                            )
                            types.forEachIndexed { index, (type, label) ->
                                SegmentedButton(
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = types.size),
                                    onClick = { selectedType = type },
                                    selected = selectedType == type,
                                    icon = {
                                        if (selectedType == type) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                        }
                                    },
                                    label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                    colors = SegmentedButtonDefaults.colors(
                                        activeContainerColor = MaterialTheme.colorScheme.primary,
                                        activeContentColor = MaterialTheme.colorScheme.onPrimary,
                                        inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        inactiveContainerColor = MaterialTheme.colorScheme.surface
                                    )
                                )
                            }
                        }
                    }

                    // Linked Subject
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.asignatura_label),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        ExposedDropdownMenuBox(
                            expanded = isSubjectDropdownExpanded,
                            onExpandedChange = { isSubjectDropdownExpanded = !isSubjectDropdownExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val currentSubjectName = subjects.find { it.subject.id == selectedSubjectId }?.subject?.name ?: stringResource(R.string.seleccionar_materia_placeholder)
                            Surface(
                                modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                onClick = { isSubjectDropdownExpanded = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Outlined.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = currentSubjectName,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = if (selectedSubjectId == null) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isSubjectDropdownExpanded)
                                }
                            }

                            ExposedDropdownMenu(
                                expanded = isSubjectDropdownExpanded,
                                onDismissRequest = { isSubjectDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.ninguna_materia)) },
                                    onClick = {
                                        selectedSubjectId = null
                                        isSubjectDropdownExpanded = false
                                    }
                                )
                                subjects.forEach { item ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(parseColorFromHex(item.subject.colorHex)))
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(item.subject.name, fontWeight = FontWeight.Medium)
                                            }
                                        },
                                        onClick = {
                                            selectedSubjectId = item.subject.id
                                            isSubjectDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. FECHA Y HORARIO
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.fecha_y_horario),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 1.2.sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = stringResource(R.string.recordatorio_label),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Outlined.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(stringResource(R.string.todo_el_dia), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp)
                                Text(stringResource(R.string.sin_hora_especifica), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = isAllDay,
                            onCheckedChange = { isAllDay = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            CalendarDateField(
                                value = eventDate,
                                onDateSelected = { eventDate = it },
                                label = stringResource(R.string.fecha_label),
                                modifier = Modifier.padding(4.dp)
                            )
                        }

                        if (!isAllDay) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                ClockTimeField(
                                    value = eventTime,
                                    onTimeSelected = { eventTime = it },
                                    label = stringResource(R.string.hora_label),
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. CONTEXTO Y NOTAS
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.contexto_y_notas),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 1.2.sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = stringResource(R.string.opcional),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.lugar_o_aula),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = capitalizeFirstLetter(it) },
                            placeholder = { Text(stringResource(R.string.ej_edificio_b), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                            leadingIcon = { Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.notas_adicionales),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = capitalizeFirstLetter(it) },
                            placeholder = { Text(stringResource(R.string.escribe_instrucciones_placeholder), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                            leadingIcon = { Icon(Icons.Outlined.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)) },
                            minLines = 3,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 4. ORGANIZACIÓN Y PRIORIDAD
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.organizacion_y_prioridad),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 1.2.sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = stringResource(R.string.gemelo_de_materia),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.espacio_o_etiqueta),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val tags = listOf(
                                OrganizationTag.UNIVERSIDAD to stringResource(R.string.tag_universidad),
                                OrganizationTag.TRABAJO to stringResource(R.string.tag_trabajo),
                                OrganizationTag.PERSONAL to stringResource(R.string.tag_personal)
                            )
                            tags.forEach { (tag, label) ->
                                val selected = organizationTag == tag.name
                                FilterChip(
                                    selected = selected,
                                    onClick = { organizationTag = tag.name },
                                    label = { Text(label, fontWeight = FontWeight.Medium) },
                                    leadingIcon = if (selected) {
                                        {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.onPrimary)
                                            )
                                        }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(stringResource(R.string.prioridad_alta_titulo), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp)
                                Text(stringResource(R.string.destacar_lista_dia), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = isImportant,
                            onCheckedChange = { isImportant = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }
    }
}
