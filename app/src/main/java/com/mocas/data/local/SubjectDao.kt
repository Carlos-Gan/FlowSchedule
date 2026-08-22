package com.mocas.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {

    @Query(
        """
        SELECT *
        FROM subjects
        WHERE isDeleted = 0
        ORDER BY name COLLATE NOCASE ASC
        """
    )
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Query(
        """
        SELECT *
        FROM subjects
        WHERE isDeleted = 0
        ORDER BY name COLLATE NOCASE ASC
        """
    )
    suspend fun getAllSubjectsOnce(): List<SubjectEntity>

    @Transaction
    @Query(
        """
        SELECT *
        FROM subjects
        WHERE isDeleted = 0
        ORDER BY name COLLATE NOCASE ASC
        """
    )
    fun getAllSubjectsWithSlots(): Flow<List<SubjectWithSlots>>

    @Transaction
    @Query(
        """
        SELECT *
        FROM subjects
        WHERE isDeleted = 0
        ORDER BY name COLLATE NOCASE ASC
        """
    )
    suspend fun getAllSubjectsWithSlotsOnce(): List<SubjectWithSlots>

    /**
     * Observa una materia y actualiza automáticamente la interfaz
     * cuando cambie la materia o alguno de sus horarios.
     */
    @Transaction
    @Query(
        """
        SELECT *
        FROM subjects
        WHERE id = :subjectId AND isDeleted = 0
        LIMIT 1
        """
    )
    fun observeSubjectWithSlotsById(
        subjectId: Long
    ): Flow<SubjectWithSlots?>

    /**
     * Obtiene la materia una sola vez.
     */
    @Transaction
    @Query(
        """
        SELECT *
        FROM subjects
        WHERE id = :subjectId AND isDeleted = 0
        LIMIT 1
        """
    )
    suspend fun getSubjectWithSlotsById(
        subjectId: Long
    ): SubjectWithSlots?

    @Query(
        """
        SELECT *
        FROM subjects
        WHERE id = :subjectId AND isDeleted = 0
        LIMIT 1
        """
    )
    suspend fun getSubjectById(
        subjectId: Long
    ): SubjectEntity?

    @Query(
        """
        SELECT *
        FROM subjects
        WHERE id = :subjectId AND isDeleted = 0
        LIMIT 1
        """
    )
    fun observeSubjectById(
        subjectId: Long
    ): Flow<SubjectEntity?>

    @Query(
        """
        SELECT *
        FROM subjects
        WHERE isDeleted = 0
          AND (name LIKE '%' || :query || '%'
           OR code LIKE '%' || :query || '%'
           OR professor LIKE '%' || :query || '%')
        ORDER BY name COLLATE NOCASE ASC
        """
    )
    fun searchSubjects(
        query: String
    ): Flow<List<SubjectEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSubject(
        subject: SubjectEntity
    ): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSubjects(
        subjects: List<SubjectEntity>
    ): List<Long>

    @Update
    suspend fun updateSubject(
        subject: SubjectEntity
    ): Int

    @Query("SELECT * FROM subjects WHERE isDeleted = 1 ORDER BY deletedAtMillis DESC")
    fun getDeletedSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE isDeleted = 1 ORDER BY deletedAtMillis DESC")
    suspend fun getDeletedSubjectsSnapshot(): List<SubjectEntity>

    @Query("UPDATE subjects SET isDeleted = :deleted, deletedAtMillis = :deletedAt WHERE id = :subjectId")
    suspend fun setSubjectDeleted(subjectId: Long, deleted: Boolean, deletedAt: Long?): Int

    @Query("DELETE FROM subjects WHERE isDeleted = 1 AND deletedAtMillis < :beforeMillis")
    suspend fun purgeDeletedSubjects(beforeMillis: Long): Int

    @Query(
        """
        UPDATE subjects
        SET semesterStart = :newStart,
            semesterEnd = :newEnd,
            updatedAtMillis = :updatedAtMillis
        WHERE semesterStart = :oldStart AND semesterEnd = :oldEnd
        """
    )
    suspend fun updatePeriodDates(
        oldStart: String,
        oldEnd: String,
        newStart: String,
        newEnd: String,
        updatedAtMillis: Long
    ): Int

    @Delete
    suspend fun deleteSubject(
        subject: SubjectEntity
    ): Int

    @Query(
        """
        DELETE FROM subjects
        WHERE id = :subjectId
        """
    )
    suspend fun deleteSubjectById(
        subjectId: Long
    ): Int

    @Query("DELETE FROM subjects")
    suspend fun clearAllSubjects(): Int
}
