package com.moca.snapmyschedule.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moca.snapmyschedule.data.model.ClassFormData
import com.moca.snapmyschedule.data.model.ClassSession
import com.moca.snapmyschedule.data.repository.ScheduleRepository
import com.moca.snapmyschedule.util.findScheduleConflict
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ScheduleViewModel(
    private val repository: ScheduleRepository
) : ViewModel() {
    /*
     * Convierte el Flow de Room en un StateFlow
     * que puede consumir la interfaz.
     */
    val sessions: StateFlow<List<ClassSession>> =
        repository.sessions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(
                stopTimeoutMillis = 5_000
            ),
            initialValue = emptyList()
        )

    fun addClass(
        formData: ClassFormData
    ) {
        viewModelScope.launch {
            repository.addClass(formData)
        }
    }

    fun deleteCourse(
        courseId: String
    ) {
        if (courseId.isBlank()) {
            return
        }

        viewModelScope.launch {
            repository.deleteCourse(courseId)
        }
    }

    fun deleteSession(
        sessionId: Long
    ) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
        }
    }

    fun deleteAllSessions() {
        viewModelScope.launch {
            repository.deleteAllSessions()
        }
    }

    fun updateClass(
        courseId: String,
        formData: ClassFormData
    ) {
        if (courseId.isBlank()) {
            return
        }

        viewModelScope.launch {
            repository.updateClass(
                courseId = courseId,
                formData = formData
            )
        }
    }

    fun validateSchedule(
        formData: ClassFormData,
        excludedCourseId: String? = null
    ): String? {
        return findScheduleConflict(
            formData = formData,
            existingSessions = sessions.value,
            excludedCourseId = excludedCourseId
        )
    }

    fun addClasses(
        classes: List<ClassFormData>
    ) {
        if (classes.isEmpty()) {
            return
        }

        viewModelScope.launch {
            repository.addClasses(classes)
        }
    }

}