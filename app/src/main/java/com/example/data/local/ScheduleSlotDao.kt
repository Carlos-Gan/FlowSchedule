package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleSlotDao {

    @Query(
        """
        SELECT *
        FROM schedule_slots
        ORDER BY dayOfWeek ASC, startTime ASC
        """
    )
    fun getAllSlots(): Flow<List<ScheduleSlotEntity>>

    @Query(
        """
        SELECT *
        FROM schedule_slots
        WHERE id = :slotId
        LIMIT 1
        """
    )
    suspend fun getSlotById(
        slotId: Long
    ): ScheduleSlotEntity?

    @Query(
        """
        SELECT *
        FROM schedule_slots
        WHERE id = :slotId
        LIMIT 1
        """
    )
    fun observeSlotById(
        slotId: Long
    ): Flow<ScheduleSlotEntity?>

    @Query(
        """
        SELECT *
        FROM schedule_slots
        WHERE subjectId = :subjectId
        ORDER BY dayOfWeek ASC, startTime ASC
        """
    )
    fun getSlotsForSubject(
        subjectId: Long
    ): Flow<List<ScheduleSlotEntity>>

    @Query(
        """
        SELECT *
        FROM schedule_slots
        WHERE subjectId = :subjectId
        ORDER BY dayOfWeek ASC, startTime ASC
        """
    )
    suspend fun getSlotsForSubjectOnce(
        subjectId: Long
    ): List<ScheduleSlotEntity>

    @Query(
        """
        SELECT *
        FROM schedule_slots
        WHERE dayOfWeek = :dayOfWeek
        ORDER BY startTime ASC
        """
    )
    fun getSlotsForDay(
        dayOfWeek: Int
    ): Flow<List<ScheduleSlotEntity>>

    /**
     * Comprueba si existe otro horario que choque con el intervalo recibido.
     *
     * excludeSlotId permite ignorar el horario que se está editando.
     * Utiliza null cuando se esté creando uno nuevo.
     */
    @Query(
        """
        SELECT EXISTS(
            SELECT 1
            FROM schedule_slots
            WHERE dayOfWeek = :dayOfWeek
              AND startTime < :endTime
              AND endTime > :startTime
              AND (:excludeSlotId IS NULL OR id != :excludeSlotId)
        )
        """
    )
    suspend fun hasScheduleConflict(
        dayOfWeek: Int,
        startTime: String,
        endTime: String,
        excludeSlotId: Long? = null
    ): Boolean

    @Query(
        """
        SELECT EXISTS(
            SELECT 1
            FROM schedule_slots AS slot
            INNER JOIN subjects AS subject ON subject.id = slot.subjectId
            WHERE slot.dayOfWeek = :dayOfWeek
              AND slot.startTime < :endTime
              AND slot.endTime > :startTime
              AND slot.subjectId != :excludedSubjectId
              AND subject.semesterStart <= :semesterEnd
              AND subject.semesterEnd >= :semesterStart
        )
        """
    )
    suspend fun hasScheduleConflictExcludingSubject(
        dayOfWeek: Int,
        startTime: String,
        endTime: String,
        excludedSubjectId: Long,
        semesterStart: String,
        semesterEnd: String
    ): Boolean

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSlot(
        slot: ScheduleSlotEntity
    ): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSlots(
        slots: List<ScheduleSlotEntity>
    ): List<Long>

    @Update
    suspend fun updateSlot(
        slot: ScheduleSlotEntity
    ): Int

    @Delete
    suspend fun deleteSlot(
        slot: ScheduleSlotEntity
    ): Int

    @Query(
        """
        DELETE FROM schedule_slots
        WHERE id = :slotId
        """
    )
    suspend fun deleteSlotById(
        slotId: Long
    ): Int

    @Query(
        """
        DELETE FROM schedule_slots
        WHERE subjectId = :subjectId
        """
    )
    suspend fun deleteSlotsBySubjectId(
        subjectId: Long
    ): Int

    @Query("DELETE FROM schedule_slots")
    suspend fun clearAllSlots(): Int
}
