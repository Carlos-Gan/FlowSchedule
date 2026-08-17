package com.moca.snapmyschedule.data.repository

import com.moca.snapmyschedule.data.local.dao.ClassSessionDao
import com.moca.snapmyschedule.data.local.entity.ClassSessionEntity
import com.moca.snapmyschedule.data.mapper.toModel
import com.moca.snapmyschedule.data.model.ClassFormData
import com.moca.snapmyschedule.data.model.ClassSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class OfflineScheduleRepository(
    private val classSessionDao: ClassSessionDao
) : ScheduleRepository {

    override val sessions: Flow<List<ClassSession>> =
        classSessionDao
            .observeAllSessions()
            .map { entities ->
                entities.map { entity ->
                    entity.toModel()
                }
            }

    override suspend fun addClass(
        formData: ClassFormData
    ) {
        val courseId = UUID
            .randomUUID()
            .toString()

        val entities = formData.toEntities(
            courseId = courseId
        )

        if (entities.isNotEmpty()) {
            classSessionDao.insertSessions(entities)
        }
    }

    override suspend fun updateClass(
        courseId: String,
        formData: ClassFormData
    ) {
        if (courseId.isBlank()) {
            return
        }

        val entities = formData.toEntities(
            courseId = courseId
        )

        if (entities.isNotEmpty()) {
            classSessionDao.replaceCourse(
                courseId = courseId,
                sessions = entities
            )
        }
    }

    override suspend fun deleteCourse(
        courseId: String
    ) {
        classSessionDao.deleteCourse(courseId)
    }

    override suspend fun deleteSession(
        sessionId: Long
    ) {
        classSessionDao.deleteSession(sessionId)
    }

    override suspend fun deleteAllSessions() {
        classSessionDao.deleteAllSessions()
    }

    private fun ClassFormData.toEntities(
        courseId: String
    ): List<ClassSessionEntity> {
        return scheduleBlocks.flatMap { block ->
            block.days
                .sortedBy { day ->
                    day.ordinal
                }
                .map { day ->
                    ClassSessionEntity(
                        id = 0,
                        courseId = courseId,
                        subjectName = subjectName.trim(),
                        subjectCode = subjectCode.trim(),
                        teacher = teacher.trim(),
                        room = block.room.trim(),
                        day = day.name,
                        dayOrder = day.ordinal,
                        startTime = block.startTime,
                        endTime = block.endTime
                    )
                }
        }
    }

    override suspend fun addClasses(
        classes: List<ClassFormData>
    ) {
        classes.forEach { formData ->
            addClass(formData)
        }
    }

}