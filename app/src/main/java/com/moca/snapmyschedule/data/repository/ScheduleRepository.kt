package com.moca.snapmyschedule.data.repository

import com.moca.snapmyschedule.data.model.ClassFormData
import com.moca.snapmyschedule.data.model.ClassSession
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {
    val sessions: Flow<List<ClassSession>>

    suspend fun addClass(
        formData: ClassFormData
    )

    suspend fun addClasses(
        classes: List<ClassFormData>
    )

    suspend fun deleteCourse(
        courseId: String
    )

    suspend fun deleteSession(
        sessionId:Long
    )

    suspend fun updateClass(
        courseId: String,
        formData: ClassFormData
    )

    suspend fun deleteAllSessions()
}