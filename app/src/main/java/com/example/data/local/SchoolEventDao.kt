package com.example.data.local

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
        ORDER BY
            startDate ASC,
            CASE WHEN startTime IS NULL THEN 0 ELSE 1 END ASC,
            startTime ASC
        """
    )
    fun getAllEventsWithSubject(): Flow<List<SchoolEventWithSubject>>

    @Query(
        """
        SELECT *
        FROM school_events
        WHERE id = :eventId
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
        WHERE id = :eventId
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
        WHERE subjectId = :subjectId
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
        WHERE startDate <= :date
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
        WHERE endDate >= :fromDate
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
        WHERE startDate <= :endDate
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
        WHERE isCompleted = 0
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

    @Delete
    suspend fun deleteEvent(
        event: SchoolEventEntity
    ): Int

    @Query(
        """
        UPDATE school_events
        SET isCompleted = :completed,
            updatedAtMillis = :updatedAtMillis
        WHERE id = :eventId
        """
    )
    suspend fun setEventCompleted(
        eventId: Long,
        completed: Boolean,
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
