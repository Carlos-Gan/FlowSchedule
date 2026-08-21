package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.DetectedSubjectItem
import com.example.data.ai.ScheduleScannerService
import com.example.data.local.AcademicPeriodEntity
import com.example.data.local.AppDatabase
import com.example.data.local.ScheduleSlotEntity
import com.example.data.local.SchoolEventEntity
import com.example.data.local.SchoolEventWithSubject
import com.example.data.local.SubjectEntity
import com.example.data.local.SubjectWithSlots
import com.example.data.repository.ScheduleRepository
import com.example.ui.model.AppSettings
import com.example.ui.model.BottomNavTab
import com.example.ui.model.DayClassItem
import com.example.ui.model.NextClassInfo
import com.example.ui.model.TimetableDisplayMode
import com.example.util.DateTimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ScheduleRepository(AppDatabase.getDatabase(application))

    private val _currentTab = MutableStateFlow(BottomNavTab.INICIO)
    val currentTab = _currentTab.asStateFlow()
    private val _timetableMode = MutableStateFlow(TimetableDisplayMode.SEMANAL)
    val timetableMode = _timetableMode.asStateFlow()
    private val _selectedDayOfWeek = MutableStateFlow(getCurrentDayOfWeekNumber())
    val selectedDayOfWeek = _selectedDayOfWeek.asStateFlow()
    private val _selectedCalendarDate = MutableStateFlow(getTodayDateString())
    val selectedCalendarDate = _selectedCalendarDate.asStateFlow()
    private val _eventFilter = MutableStateFlow("TODOS")
    val eventFilter = _eventFilter.asStateFlow()
    private val _appSettings = MutableStateFlow(AppSettings())
    val appSettings = _appSettings.asStateFlow()

    private val _isAddSubjectOpen = MutableStateFlow(false)
    val isAddSubjectOpen = _isAddSubjectOpen.asStateFlow()
    private val _isAddEventOpen = MutableStateFlow(false)
    val isAddEventOpen = _isAddEventOpen.asStateFlow()
    private val _isImportScheduleOpen = MutableStateFlow(false)
    val isImportScheduleOpen = _isImportScheduleOpen.asStateFlow()
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
    val todayClasses: StateFlow<List<DayClassItem>> = combine(
        subjectsWithSlots,
        _selectedDayOfWeek
    ) { subjects, day -> computeClassesForDay(subjects, day) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val nextClassInfo: StateFlow<NextClassInfo?> = subjectsWithSlots
        .combine(_selectedDayOfWeek) { subjects, _ -> computeNextClass(subjects) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setTab(tab: BottomNavTab) { _currentTab.value = tab }
    fun setTimetableMode(mode: TimetableDisplayMode) { _timetableMode.value = mode }
    fun setSelectedDayOfWeek(day: Int) { if (day in 1..7) _selectedDayOfWeek.value = day }
    fun setSelectedCalendarDate(date: String) {
        if (DateTimeUtils.isValidDate(date)) _selectedCalendarDate.value = date
    }
    fun setEventFilter(filter: String) { _eventFilter.value = filter }
    fun updateSettings(newSettings: AppSettings) {
        _appSettings.value = newSettings
        if (!newSettings.aiFeaturesEnabled) {
            _isImportScheduleOpen.value = false
            _isScanning.value = false
            _detectedSubjects.value = emptyList()
            _capturedPhotoBitmap.value = null
        }
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
        defaultDate: String? = null
    ) {
        _editingEvent.value = eventToEdit
        _newEventSubjectId.value = eventToEdit?.event?.subjectId ?: subjectId
        _newEventDate.value = defaultDate?.takeIf(DateTimeUtils::isValidDate)
        _isAddEventOpen.value = true
    }
    fun closeAddEvent() {
        _isAddEventOpen.value = false
        _editingEvent.value = null
        _newEventSubjectId.value = null
        _newEventDate.value = null
    }
    fun openImportSchedule() {
        if (!_appSettings.value.aiFeaturesEnabled) return
        _detectedSubjects.value = emptyList()
        _capturedPhotoBitmap.value = null
        _isScanning.value = false
        _isImportScheduleOpen.value = true
    }
    fun closeImportSchedule() { _isImportScheduleOpen.value = false }
    fun openSubjectDetail(subjectId: Long) { _selectedSubjectDetailId.value = subjectId }
    fun closeSubjectDetail() { _selectedSubjectDetailId.value = null }

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
            }
        }
    }

    fun saveEvent(event: SchoolEventEntity) {
        viewModelScope.launch {
            runOperation {
                if (event.id == 0L) repository.insertEvent(event) else repository.updateEvent(event)
                closeAddEvent()
            }
        }
    }

    fun deleteEvent(eventId: Long) {
        viewModelScope.launch {
            runOperation { check(repository.deleteEvent(eventId)) { "El evento ya no existe." } }
        }
    }

    fun saveAcademicPeriod(period: AcademicPeriodEntity) {
        viewModelScope.launch {
            runOperation {
                repository.insertAcademicPeriod(period)
                _userMessage.value = "Periodo guardado."
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

    fun toggleEventCompleted(eventId: Long, completed: Boolean) {
        viewModelScope.launch {
            runOperation {
                check(repository.setEventCompleted(eventId, completed)) { "El evento ya no existe." }
            }
        }
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
        viewModelScope.launch { runOperation { repository.clearAll() } }
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

        private fun computeClassesForDay(
            subjects: List<SubjectWithSlots>,
            dayOfWeek: Int
        ): List<DayClassItem> {
            val today = LocalDate.now()
            val nowMinutes = LocalTime.now().let { it.hour * 60 + it.minute }
            val currentDay = getCurrentDayOfWeekNumber()
            return subjects.filter { item ->
                val start = DateTimeUtils.parseDate(item.subject.semesterStart)
                val end = DateTimeUtils.parseDate(item.subject.semesterEnd)
                start != null && end != null && today in start..end
            }.flatMap { item ->
                item.slots.filter { it.dayOfWeek == dayOfWeek }.map { slot ->
                    val start = parseTimeToMinutes(slot.startTime)
                    val end = parseTimeToMinutes(slot.endTime)
                    val isToday = dayOfWeek == currentDay
                    DayClassItem(
                        subject = item.subject,
                        slot = slot,
                        isLiveNow = isToday && nowMinutes >= start && nowMinutes < end,
                        isCompletedToday = isToday && nowMinutes >= end
                    )
                }
            }.sortedBy { parseTimeToMinutes(it.slot.startTime) }
        }

        private fun computeNextClass(subjects: List<SubjectWithSlots>): NextClassInfo? {
            val today = LocalDate.now()
            val nowMinutes = LocalTime.now().let { it.hour * 60 + it.minute }
            for (offset in 0..7) {
                val date = today.plusDays(offset.toLong())
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
                    if (offset > 0 || nowMinutes < end) {
                        val happening = offset == 0 && nowMinutes >= start && nowMinutes < end
                        val dayName = when {
                            offset == 0 -> "Hoy"
                            offset == 1 -> "Mañana (${dayName(date.dayOfWeek.value)})"
                            else -> "El ${dayName(date.dayOfWeek.value)}"
                        }
                        return NextClassInfo(
                            subject = subject,
                            slot = slot,
                            dayName = dayName,
                            timeRange = "${slot.startTime} - ${slot.endTime}",
                            room = slot.room.ifBlank { subject.defaultRoom },
                            minutesUntil = if (offset == 0 && !happening) start - nowMinutes else if (happening) 0 else -1,
                            isHappeningNow = happening
                        )
                    }
                }
            }
            return null
        }

        fun parseTimeToMinutes(timeStr: String): Int = DateTimeUtils.timeToMinutes(timeStr) ?: 0

        private fun dayName(day: Int): String = when (day) {
            1 -> "Lunes"
            2 -> "Martes"
            3 -> "Miércoles"
            4 -> "Jueves"
            5 -> "Viernes"
            6 -> "Sábado"
            else -> "Domingo"
        }
    }
}
