package com.moca.snapmyschedule.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.moca.snapmyschedule.data.local.entity.ClassSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassSessionDao {

    /*
     * Room emitirá una lista nueva cada vez que
     * se agregue, actualice o elimine una sesión.
     */
    @Query(
        """
        SELECT * FROM class_sessions
        ORDER BY dayOrder ASC, startTime ASC
        """
    )
    fun observeAllSessions(): Flow<List<ClassSessionEntity>>

    @Query(
        """
        SELECT * FROM class_sessions
        WHERE id = :sessionId
        LIMIT 1
        """
    )
    suspend fun getSessionById(
        sessionId: Long
    ): ClassSessionEntity?

    /*
     * Obtiene todos los bloques que pertenecen
     * a una misma materia.
     */
    @Query(
        """
        SELECT * FROM class_sessions
        WHERE courseId = :courseId
        ORDER BY dayOrder ASC, startTime ASC
        """
    )
    suspend fun getSessionsByCourseId(
        courseId: String
    ): List<ClassSessionEntity>

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun insertSession(
        session: ClassSessionEntity
    ): Long

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun insertSessions(
        sessions: List<ClassSessionEntity>
    )

    @Update
    suspend fun updateSession(
        session: ClassSessionEntity
    )

    /*
     * Elimina toda una materia, incluyendo
     * todos sus días, horas y salones.
     */
    @Query(
        """
        DELETE FROM class_sessions
        WHERE courseId = :courseId
        """
    )
    suspend fun deleteCourse(
        courseId: String
    )

    @Query(
        """
        DELETE FROM class_sessions
        WHERE id = :sessionId
        """
    )
    suspend fun deleteSession(
        sessionId: Long
    )

    @Query("DELETE FROM class_sessions")
    suspend fun deleteAllSessions()

    @Transaction
    suspend fun replaceCourse(
        courseId: String,
        sessions: List<ClassSessionEntity>
    ) {
        deleteCourse(courseId)
        insertSessions(sessions)
    }
}