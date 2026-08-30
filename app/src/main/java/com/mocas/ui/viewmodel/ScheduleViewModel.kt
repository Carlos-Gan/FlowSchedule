package com.mocas.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mocas.data.ai.DetectedSubjectItem
import com.mocas.data.ai.ScheduleScannerService
import com.mocas.data.backup.AutomaticBackupInfo
import com.mocas.data.backup.AutomaticBackupManager
import com.mocas.data.local.AcademicPeriodEntity
import com.mocas.data.local.AppDatabase
import com.mocas.data.local.ClassExceptionEntity
import com.mocas.data.local.ClassExceptionType
import com.mocas.data.local.ScheduleSlotEntity
import com.mocas.data.local.SchoolEventEntity
import com.mocas.data.local.SchoolEventType
import com.mocas.data.local.SchoolEventWithSubject
import com.mocas.data.local.SubjectEntity
import com.mocas.data.local.SubjectWithSlots
import com.mocas.data.local.GradeCategoryEntity
import com.mocas.data.local.GradeItemEntity
import com.mocas.data.local.GradeUnitCategoryWeightEntity
import com.mocas.data.local.GradeUnitEntity
import com.mocas.data.preferences.AppSettingsStore
import com.mocas.data.notifications.ReminderRescheduler
import com.mocas.data.widget.ScheduleWidgetProvider
import com.mocas.data.widget.DailyScheduleWidgetProvider
import com.mocas.data.repository.ScheduleRepository
import com.mocas.ui.model.AppSettings
import com.mocas.ui.model.ClassOccurrenceInfo
import com.mocas.ui.model.BottomNavTab
import com.mocas.ui.model.DayClassItem
import com.mocas.ui.model.NextClassInfo
import com.mocas.ui.model.TimetableDisplayMode
import com.mocas.ui.model.DailyClassStats
import com.mocas.util.DateTimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters
import java.util.Locale

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ScheduleRepository(AppDatabase.getDatabase(application))
    private val settingsStore = AppSettingsStore(application)
    private val automaticBackupManager = AutomaticBackupManager(application)

    private val _currentTab = MutableStateFlow(BottomNavTab.INICIO)
    val currentTab = _currentTab.asStateFlow()
    private val _timetableMode = MutableStateFlow(TimetableDisplayMode.SEMANAL)
    val timetableMode = _timetableMode.asStateFlow()
    private val _selectedDayOfWeek = MutableStateFlow(getCurrentDayOfWeekNumber())
    val selectedDayOfWeek = _selectedDayOfWeek.asStateFlow()
    private val _selectedWeekStart = MutableStateFlow(startOfWeek(LocalDate.now()))
    val selectedWeekStart = _selectedWeekStart.asStateFlow()
    private val _selectedCalendarDate = MutableStateFlow(getTodayDateString())
    val selectedCalendarDate = _selectedCalendarDate.asStateFlow()
    private val _eventFilter = MutableStateFlow("TODOS")
    val eventFilter = _eventFilter.asStateFlow()
    private val _appSettings = MutableStateFlow(settingsStore.load())
    val appSettings = _appSettings.asStateFlow()

    private val _isAddSubjectOpen = MutableStateFlow(false)
    val isAddSubjectOpen = _isAddSubjectOpen.asStateFlow()
    private val _isAddEventOpen = MutableStateFlow(false)
    val isAddEventOpen = _isAddEventOpen.asStateFlow()
    private val _isImportScheduleOpen = MutableStateFlow(false)
    val isImportScheduleOpen = _isImportScheduleOpen.asStateFlow()
    private val _isGlobalSearchOpen = MutableStateFlow(false)
    val isGlobalSearchOpen = _isGlobalSearchOpen.asStateFlow()
    private val _selectedSubjectDetailId = MutableStateFlow<Long?>(null)
    val selectedSubjectDetailId = _selectedSubjectDetailId.asStateFlow()
    private val _editingSubject = MutableStateFlow<SubjectWithSlots?>(null)
    val editingSubject = _editingSubject.asStateFlow()
    private val _editingEvent = MutableStateFlow<SchoolEventWithSubject?>(null)
    val editingEvent = _editingEvent.asStateFlow()
    private val _newEventSubjectId = MutableStateFlow<Long?>(null)
    val newEventSubjectId = _newEventSubjectId.asStateFlow()
    private val _newEventDate = MutableStateFlow<String?>(null)
    val newEventDate = _newEventDate.asStateFlow()
    private val _newEventType = MutableStateFlow<SchoolEventType?>(null)
    val newEventType = _newEventType.asStateFlow()
    private val _newEventTitle = MutableStateFlow<String?>(null)
    val newEventTitle = _newEventTitle.asStateFlow()
    private val _selectedClassOccurrence = MutableStateFlow<ClassOccurrenceInfo?>(null)
    val selectedClassOccurrence = _selectedClassOccurrence.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()
    private val _detectedSubjects = MutableStateFlow<List<DetectedSubjectItem>>(emptyList())
    val detectedSubjects = _detectedSubjects.asStateFlow()
    private val _capturedPhotoBitmap = MutableStateFlow<Bitmap?>(null)
    val capturedPhotoBitmap = _capturedPhotoBitmap.asStateFlow()
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage = _userMessage.asStateFlow()

    val subjectsWithSlots: StateFlow<List<SubjectWithSlots>> = repository.allSubjectsWithSlots
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val allEventsWithSubject: StateFlow<List<SchoolEventWithSubject>> = repository.allEventsWithSubject
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val academicPeriods: StateFlow<List<AcademicPeriodEntity>> = repository.allAcademicPeriods
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val classExceptions: StateFlow<List<ClassExceptionEntity>> = repository.allClassExceptions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val deletedSubjects: StateFlow<List<SubjectEntity>> = repository.deletedSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val deletedEvents: StateFlow<List<SchoolEventEntity>> = repository.deletedEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val gradeCategories: StateFlow<List<GradeCategoryEntity>> = repository.gradeCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val gradeItems: StateFlow<List<GradeItemEntity>> = repository.gradeItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val gradeUnits: StateFlow<List<GradeUnitEntity>> = repository.gradeUnits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val gradeUnitCategoryWeights: StateFlow<List<GradeUnitCategoryWeightEntity>> = repository.gradeUnitCategoryWeights
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    private val _automaticBackups = MutableStateFlow<List<AutomaticBackupInfo>>(emptyList())
    val automaticBackups = _automaticBackups.asStateFlow()
    val todayClasses: StateFlow<List<DayClassItem>> = combine(
        subjectsWithSlots,
        _selectedDayOfWeek,
        _selectedWeekStart,
        classExceptions
    ) { subjects, day, weekStart, exceptions ->
        computeClassesForDate(
            subjects,
            weekStart.plusDays((day - 1).toLong()),
            exceptions
        )
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val todayClassStats: StateFlow<DailyClassStats> = combine(
        subjectsWithSlots,
        classExceptions
    ) { subjects, exceptions ->
        computeDailyClassStats(subjects, LocalDate.now(), exceptions)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DailyClassStats())
    val nextClassInfo: StateFlow<NextClassInfo?> = subjectsWithSlots
        .combine(_selectedDayOfWeek) { subjects, _ -> computeNextClass(subjects) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch {
            repository.purgeExpiredTrash()
            refreshAutomaticBackups()
        }
        viewModelScope.launch {
            combine(
                repository.allSubjectsWithSlots,
                repository.allEventsWithSubject,
                repository.allClassExceptions,
                _appSettings
            ) { _, _, _, _ -> Unit }
                .collectLatest {
                    ReminderRescheduler.reschedule(application)
                    ScheduleWidgetProvider.requestUpdate(application)
                    DailyScheduleWidgetProvider.requestUpdate(application)
                }
        }
    }

    fun setTab(tab: BottomNavTab) { _currentTab.value = tab }
    fun setTimetableMode(mode: TimetableDisplayMode) { _timetableMode.value = mode }
    fun setSelectedDayOfWeek(day: Int) { if (day in 1..7) _selectedDayOfWeek.value = day }
    fun showPreviousWeek() { _selectedWeekStart.value = _selectedWeekStart.value.minusWeeks(1) }
    fun showNextWeek() { _selectedWeekStart.value = _selectedWeekStart.value.plusWeeks(1) }
    fun showPreviousDay() {
        if (_selectedDayOfWeek.value == 1) {
            _selectedWeekStart.value = _selectedWeekStart.value.minusWeeks(1)
            _selectedDayOfWeek.value = 7
        } else {
            _selectedDayOfWeek.value -= 1
        }
    }
    fun showNextDay() {
        if (_selectedDayOfWeek.value == 7) {
            _selectedWeekStart.value = _selectedWeekStart.value.plusWeeks(1)
            _selectedDayOfWeek.value = 1
        } else {
            _selectedDayOfWeek.value += 1
        }
    }
    fun showCurrentWeek() {
        _selectedWeekStart.value = startOfWeek(LocalDate.now())
        _selectedDayOfWeek.value = getCurrentDayOfWeekNumber()
    }
    fun setSelectedCalendarDate(date: String) {
        if (DateTimeUtils.isValidDate(date)) _selectedCalendarDate.value = date
    }
    fun setEventFilter(filter: String) { _eventFilter.value = filter }
    fun updateSettings(newSettings: AppSettings) {
        _appSettings.value = newSettings
        settingsStore.save(newSettings)
        if (!newSettings.aiFeaturesEnabled) {
            _isImportScheduleOpen.value = false
            _isScanning.value = false
            _detectedSubjects.value = emptyList()
            _capturedPhotoBitmap.value = null
        }
    }
    fun completeOnboarding(settings: AppSettings) {
        updateSettings(
            settings.copy(
                userName = settings.userName.trim(),
                onboardingCompleted = true
            )
        )
    }
    fun clearUserMessage() { _userMessage.value = null }

    fun openAddSubject(subjectToEdit: SubjectWithSlots? = null) {
        _editingSubject.value = subjectToEdit
        _isAddSubjectOpen.value = true
    }
    fun closeAddSubject() {
        _isAddSubjectOpen.value = false
        _editingSubject.value = null
    }
    fun openAddEvent(
        eventToEdit: SchoolEventWithSubject? = null,
        subjectId: Long? = null,
        defaultDate: String? = null,
        defaultType: SchoolEventType? = null,
        defaultTitle: String? = null
    ) {
        _editingEvent.value = eventToEdit
        _newEventSubjectId.value = eventToEdit?.event?.subjectId ?: subjectId
        _newEventDate.value = defaultDate?.takeIf(DateTimeUtils::isValidDate)
        _newEventType.value = defaultType
        _newEventTitle.value = defaultTitle
        _isAddEventOpen.value = true
    }
    fun closeAddEvent() {
        _isAddEventOpen.value = false
        _editingEvent.value = null
        _newEventSubjectId.value = null
        _newEventDate.value = null
        _newEventType.value = null
        _newEventTitle.value = null
    }
    fun openImportSchedule() {
        if (!_appSettings.value.aiFeaturesEnabled) return
        _detectedSubjects.value = emptyList()
        _capturedPhotoBitmap.value = null
        _isScanning.value = false
        _isImportScheduleOpen.value = true
    }
    fun closeImportSchedule() { _isImportScheduleOpen.value = false }
    fun openGlobalSearch() { _isGlobalSearchOpen.value = true }
    fun closeGlobalSearch() { _isGlobalSearchOpen.value = false }
    fun openSubjectDetail(subjectId: Long) { _selectedSubjectDetailId.value = subjectId }
    fun closeSubjectDetail() { _selectedSubjectDetailId.value = null }
    fun openClassOccurrence(
        subject: SubjectEntity,
        slot: ScheduleSlotEntity,
        date: LocalDate,
        exception: ClassExceptionEntity? = null
    ) {
        _selectedClassOccurrence.value = ClassOccurrenceInfo(subject, slot, date.toString(), exception)
    }
    fun closeClassOccurrence() { _selectedClassOccurrence.value = null }

    fun saveClassException(item: ClassExceptionEntity) {
        viewModelScope.launch {
            runOperation {
                repository.saveClassException(item)
                closeClassOccurrence()
                _userMessage.value = if (item.type == ClassExceptionType.CANCELED) {
                    "Clase cancelada solamente para esta fecha."
                } else {
                    "Cambio aplicado solamente a esta fecha."
                }
            }
        }
    }

    fun deleteClassException(id: Long) {
        viewModelScope.launch {
            runOperation {
                check(repository.deleteClassException(id)) { "La excepción ya no existe." }
                closeClassOccurrence()
                _userMessage.value = "La clase volvió a su horario habitual."
            }
        }
    }

    fun saveSubject(subject: SubjectEntity, slots: List<ScheduleSlotEntity>) {
        viewModelScope.launch {
            runOperation {
                if (subject.id == 0L) repository.insertSubjectWithSlots(subject, slots)
                else repository.updateSubjectWithSlots(subject, slots)
                closeAddSubject()
            }
        }
    }

    fun deleteSubject(subjectId: Long) {
        viewModelScope.launch {
            runOperation {
                check(repository.deleteSubject(subjectId)) { "La materia ya no existe." }
                closeSubjectDetail()
                _userMessage.value = "Materia movida a la papelera."
            }
        }
    }

    fun saveEvent(event: SchoolEventEntity, subtasks: List<com.mocas.data.local.SubtaskEntity>) {
        viewModelScope.launch {
            runOperation {
                if (event.id == 0L) repository.insertEvent(event, subtasks)
                else repository.updateEvent(event, subtasks)
                closeAddEvent()
            }
        }
    }

    fun deleteEvent(eventId: Long) {
        viewModelScope.launch {
            runOperation {
                check(repository.deleteEvent(eventId)) { "El evento ya no existe." }
                _userMessage.value = "Actividad movida a la papelera."
            }
        }
    }

    fun restoreDeletedSubject(subjectId: Long) {
        viewModelScope.launch {
            runOperation {
                check(repository.restoreSubject(subjectId)) { "La materia ya no está en la papelera." }
                _userMessage.value = "Materia restaurada."
            }
        }
    }

    fun restoreDeletedEvent(eventId: Long) {
        viewModelScope.launch {
            runOperation {
                check(repository.restoreEvent(eventId)) { "La actividad ya no está en la papelera." }
                _userMessage.value = "Actividad restaurada."
            }
        }
    }

    fun permanentlyDeleteSubject(subjectId: Long) {
        viewModelScope.launch {
            runOperation { repository.permanentlyDeleteSubject(subjectId) }
        }
    }

    fun permanentlyDeleteEvent(eventId: Long) {
        viewModelScope.launch {
            runOperation { repository.permanentlyDeleteEvent(eventId) }
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            runOperation {
                repository.emptyTrash()
                _userMessage.value = "Papelera vaciada."
            }
        }
    }

    fun saveAcademicPeriod(period: AcademicPeriodEntity) {
        viewModelScope.launch {
            runOperation {
                repository.saveAcademicPeriod(period)
                _userMessage.value = if (period.id == 0L) "Periodo guardado." else "Periodo actualizado."
            }
        }
    }

    fun deleteAcademicPeriod(periodId: Long) {
        viewModelScope.launch {
            runOperation {
                check(repository.deleteAcademicPeriod(periodId)) {
                    "El periodo ya no existe."
                }
            }
        }
    }

    fun copySubjectsBetweenPeriods(sourcePeriodId: Long, targetPeriodId: Long) {
        viewModelScope.launch {
            runOperation {
                val copied = repository.copySubjectsBetweenPeriods(sourcePeriodId, targetPeriodId)
                _userMessage.value = when (copied) {
                    0 -> "No había materias nuevas para copiar."
                    1 -> "Se copió 1 materia al periodo."
                    else -> "Se copiaron $copied materias al periodo."
                }
            }
        }
    }

    fun toggleEventCompleted(eventId: Long, completed: Boolean) {
        viewModelScope.launch {
            runOperation {
                check(repository.setEventCompleted(eventId, completed)) { "El evento ya no existe." }
            }
        }
    }

    fun postponeEventOneDay(eventId: Long) {
        viewModelScope.launch {
            runOperation {
                check(repository.postponeEventByDays(eventId, 1)) { "No se pudo posponer la actividad." }
                _userMessage.value = "Actividad pospuesta para mañana."
            }
        }
    }

    fun toggleSubtaskCompleted(eventId: Long, subtaskId: Long, completed: Boolean) {
        viewModelScope.launch {
            runOperation {
                check(repository.setSubtaskCompleted(eventId, subtaskId, completed)) {
                    "La subtarea ya no existe."
                }
            }
        }
    }

    fun addGradeCategory(item: GradeCategoryEntity) {
        viewModelScope.launch { runOperation { repository.addGradeCategory(item) } }
    }

    fun addGradeItem(item: GradeItemEntity) {
        viewModelScope.launch { runOperation { repository.addGradeItem(item) } }
    }

    fun addGradeUnit(item: GradeUnitEntity) {
        viewModelScope.launch { runOperation { repository.addGradeUnit(item) } }
    }

    fun deleteGradeUnit(item: GradeUnitEntity) {
        viewModelScope.launch { runOperation { repository.deleteGradeUnit(item) } }
    }

    fun saveUnitCategoryWeights(unitId: Long, weights: List<GradeUnitCategoryWeightEntity>) {
        viewModelScope.launch { runOperation { repository.saveUnitCategoryWeights(unitId, weights) } }
    }

    fun resetUnitCategoryWeights(unitId: Long) {
        viewModelScope.launch { runOperation { repository.resetUnitCategoryWeights(unitId) } }
    }

    fun deleteGradeCategory(item: GradeCategoryEntity) {
        viewModelScope.launch { runOperation { repository.deleteGradeCategory(item) } }
    }

    fun deleteGradeItem(item: GradeItemEntity) {
        viewModelScope.launch { runOperation { repository.deleteGradeItem(item) } }
    }

    fun scanScheduleImage(bitmap: Bitmap?) {
        if (!_appSettings.value.aiFeaturesEnabled) return
        if (bitmap == null) {
            _userMessage.value = "No se pudo leer la imagen seleccionada."
            return
        }
        _capturedPhotoBitmap.value = bitmap
        _isScanning.value = true
        viewModelScope.launch {
            try {
                _detectedSubjects.value = ScheduleScannerService.analyzeScheduleImage(bitmap)
            } catch (error: Exception) {
                _detectedSubjects.value = emptyList()
                _userMessage.value = error.message ?: "No se pudo analizar el horario."
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun toggleDetectedItemSelection(index: Int) {
        val current = _detectedSubjects.value.toMutableList()
        if (index in current.indices) {
            current[index] = current[index].copy(isSelected = !current[index].isSelected)
            _detectedSubjects.value = current
        }
    }

    fun updateDetectedItem(index: Int, updated: DetectedSubjectItem) {
        val current = _detectedSubjects.value.toMutableList()
        if (index in current.indices) {
            current[index] = updated
            _detectedSubjects.value = current
        }
    }

    fun confirmImportDetectedSchedule(semesterStart: String, semesterEnd: String) {
        viewModelScope.launch {
            runOperation {
                repository.importDetectedSubjects(_detectedSubjects.value, semesterStart, semesterEnd)
                closeImportSchedule()
                _currentTab.value = BottomNavTab.HORARIO
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            runOperation {
                createAutomaticBackup("clear")
                repository.clearAll()
                _userMessage.value = "Datos borrados. Guardamos un respaldo automático."
            }
        }
    }

    fun exportScheduleBackup(uri: Uri) {
        viewModelScope.launch {
            runOperation {
                val json = withContext(Dispatchers.IO) {
                    repository.exportScheduleBackup()
                }
                withContext(Dispatchers.IO) {
                    val resolver = getApplication<Application>().contentResolver
                    requireNotNull(resolver.openOutputStream(uri, "wt")) {
                        "No se pudo abrir el archivo de destino."
                    }.bufferedWriter().use { writer -> writer.write(json) }
                }
                _userMessage.value = "Respaldo exportado correctamente."
            }
        }
    }

    fun importScheduleBackup(uri: Uri) {
        viewModelScope.launch {
            runOperation {
                val json = withContext(Dispatchers.IO) {
                    val resolver = getApplication<Application>().contentResolver
                    requireNotNull(resolver.openInputStream(uri)) {
                        "No se pudo abrir el respaldo seleccionado."
                    }.bufferedReader().use { reader -> reader.readText() }
                }
                createAutomaticBackup("import")
                val summary = repository.importScheduleBackup(json)
                _userMessage.value =
                    "Respaldo restaurado: ${summary.subjects} materias, " +
                    "${summary.sessions} sesiones y ${summary.activities} actividades."
            }
        }
    }

    fun restoreAutomaticBackup(fileName: String) {
        viewModelScope.launch {
            runOperation {
                val json = withContext(Dispatchers.IO) { automaticBackupManager.read(fileName) }
                createAutomaticBackup("restore")
                val summary = repository.importScheduleBackup(json)
                refreshAutomaticBackups()
                _userMessage.value = "Respaldo restaurado: ${summary.subjects} materias y ${summary.activities} actividades."
            }
        }
    }

    fun deleteAutomaticBackup(fileName: String) {
        viewModelScope.launch {
            runOperation {
                withContext(Dispatchers.IO) { automaticBackupManager.delete(fileName) }
                refreshAutomaticBackups()
            }
        }
    }

    private suspend fun createAutomaticBackup(reasonCode: String) {
        withContext(Dispatchers.IO) {
            automaticBackupManager.create(repository.exportScheduleBackup(), reasonCode)
        }
        refreshAutomaticBackups()
    }

    private suspend fun refreshAutomaticBackups() {
        _automaticBackups.value = withContext(Dispatchers.IO) {
            automaticBackupManager.listFiles()
        }
    }

    private suspend fun runOperation(block: suspend () -> Unit) {
        try {
            block()
        } catch (error: Exception) {
            _userMessage.value = error.message ?: "Ocurrió un error inesperado."
        }
    }

    companion object {
        fun getCurrentDayOfWeekNumber(): Int = DateTimeUtils.currentDayOfWeek()
        fun getTodayDateString(): String = DateTimeUtils.todayString()
        fun getFormattedTodayHeading(): String = DateTimeUtils.formatDate(getTodayDateString(), true)
        fun getGreetingText(name: String): String {
            val greeting = when (LocalTime.now().hour) {
                in 6..11 -> "Buenos días"
                in 12..19 -> "Buenas tardes"
                else -> "Buenas noches"
            }
            return if (name.isBlank()) "¡$greeting!" else "¡$greeting, $name!"
        }

        fun startOfWeek(date: LocalDate): LocalDate =
            date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        private fun computeClassesForDate(
            subjects: List<SubjectWithSlots>,
            date: LocalDate,
            exceptions: List<ClassExceptionEntity>
        ): List<DayClassItem> {
            val today = LocalDate.now()
            val nowMinutes = LocalTime.now().let { it.hour * 60 + it.minute }
            return subjects.filter { item ->
                val start = DateTimeUtils.parseDate(item.subject.semesterStart)
                val end = DateTimeUtils.parseDate(item.subject.semesterEnd)
                start != null && end != null && date in start..end
            }.flatMap { item ->
                item.slots.filter { it.dayOfWeek == date.dayOfWeek.value }.mapNotNull { slot ->
                    val exception = exceptions.firstOrNull {
                        it.slotId == slot.id && it.date == date.toString()
                    }
                    if (exception?.type == ClassExceptionType.CANCELED) return@mapNotNull null
                    val effectiveSlot = if (exception?.type == ClassExceptionType.MODIFIED) {
                        slot.copy(
                            startTime = exception.newStartTime ?: slot.startTime,
                            endTime = exception.newEndTime ?: slot.endTime,
                            room = exception.newRoom ?: slot.room
                        )
                    } else slot
                    val start = parseTimeToMinutes(effectiveSlot.startTime)
                    val end = parseTimeToMinutes(effectiveSlot.endTime)
                    val isToday = date == today
                    DayClassItem(
                        subject = item.subject,
                        slot = effectiveSlot,
                        isLiveNow = isToday && nowMinutes >= start && nowMinutes < end,
                        isCompletedToday = isToday && nowMinutes >= end
                    )
                }
            }.sortedBy { parseTimeToMinutes(it.slot.startTime) }
        }

        private fun computeDailyClassStats(
            subjects: List<SubjectWithSlots>,
            date: LocalDate,
            exceptions: List<ClassExceptionEntity>
        ): DailyClassStats {
            val classes = computeClassesForDate(subjects, date, exceptions)
            if (classes.isEmpty()) return DailyClassStats()

            val nowMinutes = LocalTime.now().let { it.hour * 60 + it.minute }
            
            var totalMinutes = 0.0
            var completedMinutes = 0.0
            
            classes.forEach { item ->
                val start = parseTimeToMinutes(item.slot.startTime)
                val end = parseTimeToMinutes(item.slot.endTime)
                val duration = (end - start).toDouble()
                
                totalMinutes += duration
                
                if (nowMinutes >= end) {
                    completedMinutes += duration
                } else if (nowMinutes > start) {
                    completedMinutes += (nowMinutes - start).toDouble()
                }
            }
            
            val remainingMinutes = (totalMinutes - completedMinutes).coerceAtLeast(0.0)
            val progress = if (totalMinutes > 0) (completedMinutes / totalMinutes).toFloat() else 0f
            
            return DailyClassStats(
                totalHours = totalMinutes / 60.0,
                remainingHours = remainingMinutes / 60.0,
                progress = progress
            )
        }

        private fun computeNextClass(subjects: List<SubjectWithSlots>): NextClassInfo? {
            val today = LocalDate.now()
            val nowMinutes = LocalTime.now().let { it.hour * 60 + it.minute }
            
            // Solo buscamos clases para el día de hoy para evitar mostrar clases de días futuros 
            // en el dashboard, según lo solicitado.
            val date = today
            
            val candidates = subjects.flatMap { item ->
                val semesterStart = DateTimeUtils.parseDate(item.subject.semesterStart)
                val semesterEnd = DateTimeUtils.parseDate(item.subject.semesterEnd)
                if (semesterStart == null || semesterEnd == null || date !in semesterStart..semesterEnd) {
                    emptyList()
                } else {
                    item.slots.filter { it.dayOfWeek == date.dayOfWeek.value }
                        .map { item.subject to it }
                }
            }.sortedBy { parseTimeToMinutes(it.second.startTime) }

            candidates.forEach { (subject, slot) ->
                val start = parseTimeToMinutes(slot.startTime)
                val end = parseTimeToMinutes(slot.endTime)
                
                // Si la clase no ha terminado todavía
                if (nowMinutes < end) {
                    val happening = nowMinutes >= start
                    val dayName = "Hoy"
                    
                    return NextClassInfo(
                        subject = subject,
                        slot = slot,
                        dayName = dayName,
                        timeRange = "${slot.startTime} - ${slot.endTime}",
                        room = slot.room.ifBlank { subject.defaultRoom },
                        // minutesUntil será >= 0 ya que happening=true -> 0, y !happening -> start-nowMinutes > 0
                        minutesUntil = if (!happening) start - nowMinutes else 0,
                        isHappeningNow = happening
                    )
                }
            }
            return null
        }

        fun parseTimeToMinutes(timeStr: String): Int = DateTimeUtils.timeToMinutes(timeStr) ?: 0
    }
}
