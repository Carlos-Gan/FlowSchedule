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
interface SchoolEventDao {

    @Query(
        """
        SELECT *
        FROM school_events
        WHERE isDeleted = 0
        ORDER BY
            startDate ASC,
            CASE WHEN startTime IS NULL THEN 0 ELSE 1 END ASC,
            startTime ASC
        """
    )
    fun getAllEvents(): Flow<List<SchoolEventEntity>>

    @Transaction
    @Query(
        """
        SELECT *
        FROM school_events
        WHERE isDeleted = 0
        ORDER BY
            startDate ASC,
            CASE WHEN startTime IS NULL THEN 0 ELSE 1 END ASC,
            startTime ASC
        """
    )
    fun getAllEventsWithSubject(): Flow<List<SchoolEventWithSubject>>

    @Transaction
    @Query("SELECT * FROM school_events WHERE isDeleted = 0 ORDER BY startDate ASC, startTime ASC")
    suspend fun getAllEventsWithSubjectOnce(): List<SchoolEventWithSubject>

    @Query(
        """
        SELECT *
        FROM school_events
        WHERE id = :eventId AND isDeleted = 0
        LIMIT 1
        """
    )
    suspend fun getEventById(
        eventId: Long
    ): SchoolEventEntity?

    @Transaction
    @Query(
        """
        SELECT *
        FROM school_events
        WHERE id = :eventId AND isDeleted = 0
        LIMIT 1
        """
    )
    fun observeEventWithSubjectById(
        eventId: Long
    ): Flow<SchoolEventWithSubject?>

    @Transaction
    @Query(
        """
        SELECT *
        FROM school_events
        WHERE subjectId = :subjectId AND isDeleted = 0
        ORDER BY
            startDate ASC,
            CASE WHEN startTime IS NULL THEN 0 ELSE 1 END ASC,
            startTime ASC
        """
    )
    fun getEventsForSubject(
        subjectId: Long
    ): Flow<List<SchoolEventWithSubject>>

    /**
     * También encuentra eventos que abarcan varios días.
     *
     * Ejemplo:
     * startDate = 2026-12-19
     * endDate = 2027-01-10
     *
     * El evento aparecerá en cualquiera de esas fechas.
     */
    @Transaction
    @Query(
        """
        SELECT *
        FROM school_events
        WHERE isDeleted = 0
          AND startDate <= :date
          AND endDate >= :date
        ORDER BY
            CASE WHEN startTime IS NULL THEN 0 ELSE 1 END ASC,
            startTime ASC
        """
    )
    fun getEventsForDate(
        date: String
    ): Flow<List<SchoolEventWithSubject>>

    @Transaction
    @Query(
        """
        SELECT *
        FROM school_events
        WHERE isDeleted = 0
          AND endDate >= :fromDate
        ORDER BY
            startDate ASC,
            CASE WHEN startTime IS NULL THEN 0 ELSE 1 END ASC,
            startTime ASC
        """
    )
    fun getUpcomingEvents(
        fromDate: String
    ): Flow<List<SchoolEventWithSubject>>

    @Transaction
    @Query(
        """
        SELECT *
        FROM school_events
        WHERE isDeleted = 0
          AND startDate <= :endDate
          AND endDate >= :startDate
        ORDER BY
            startDate ASC,
            CASE WHEN startTime IS NULL THEN 0 ELSE 1 END ASC,
            startTime ASC
        """
    )
    fun getEventsBetweenDates(
        startDate: String,
        endDate: String
    ): Flow<List<SchoolEventWithSubject>>

    @Transaction
    @Query(
        """
        SELECT *
        FROM school_events
        WHERE isDeleted = 0
          AND isCompleted = 0
          AND endDate >= :fromDate
        ORDER BY
            startDate ASC,
            CASE WHEN startTime IS NULL THEN 0 ELSE 1 END ASC,
            startTime ASC
        """
    )
    fun getPendingUpcomingEvents(
        fromDate: String
    ): Flow<List<SchoolEventWithSubject>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEvent(
        event: SchoolEventEntity
    ): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEvents(
        events: List<SchoolEventEntity>
    ): List<Long>

    @Update
    suspend fun updateEvent(
        event: SchoolEventEntity
    ): Int

    @Query("SELECT * FROM school_events WHERE isDeleted = 1 ORDER BY deletedAtMillis DESC")
    fun getDeletedEvents(): Flow<List<SchoolEventEntity>>

    @Query("SELECT * FROM school_events WHERE isDeleted = 1 ORDER BY deletedAtMillis DESC")
    suspend fun getDeletedEventsSnapshot(): List<SchoolEventEntity>

    @Query("UPDATE school_events SET isDeleted = :deleted, deletedAtMillis = :deletedAt WHERE id = :eventId")
    suspend fun setEventDeleted(eventId: Long, deleted: Boolean, deletedAt: Long?): Int

    @Query("DELETE FROM school_events WHERE isDeleted = 1 AND deletedAtMillis < :beforeMillis")
    suspend fun purgeDeletedEvents(beforeMillis: Long): Int

    @Delete
    suspend fun deleteEvent(
        event: SchoolEventEntity
    ): Int

    @Query(
        """
        UPDATE school_events
        SET isCompleted = :completed,
            updatedAtMillis = :updatedAtMillis,
            completedAtMillis = :completedAtMillis
        WHERE id = :eventId AND isDeleted = 0
        """
    )
    suspend fun setEventCompleted(
        eventId: Long,
        completed: Boolean,
        updatedAtMillis: Long,
        completedAtMillis: Long?
    ): Int

    @Query(
        """
        UPDATE school_events
        SET startDate = :startDate,
            endDate = :endDate,
            updatedAtMillis = :updatedAtMillis
        WHERE id = :eventId AND isDeleted = 0
        """
    )
    suspend fun updateEventDates(
        eventId: Long,
        startDate: String,
        endDate: String,
        updatedAtMillis: Long
    ): Int

    @Query(
        """
        UPDATE school_events
        SET calendarEventId = :calendarEventId,
            calendarId = :calendarId,
            lastCalendarSyncMillis = :syncTimeMillis,
            syncCalendar = 1,
            updatedAtMillis = :syncTimeMillis
        WHERE id = :eventId
        """
    )
    suspend fun markEventAsSynced(
        eventId: Long,
        calendarEventId: Long,
        calendarId: Long,
        syncTimeMillis: Long
    ): Int

    @Query(
        """
        UPDATE school_events
        SET calendarEventId = NULL,
            calendarId = NULL,
            lastCalendarSyncMillis = NULL,
            syncCalendar = 0,
            updatedAtMillis = :updatedAtMillis
        WHERE id = :eventId
        """
    )
    suspend fun clearCalendarSync(
        eventId: Long,
        updatedAtMillis: Long
    ): Int

    @Query(
        """
        DELETE FROM school_events
        WHERE id = :eventId
        """
    )
    suspend fun deleteEventById(
        eventId: Long
    ): Int

    @Query("DELETE FROM school_events")
    suspend fun clearAllEvents(): Int
}
