package com.mocas.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AcademicPeriodDao {
    @Query("SELECT * FROM academic_periods ORDER BY startDate DESC, endDate DESC")
    fun getAllPeriods(): Flow<List<AcademicPeriodEntity>>

    @Query("SELECT * FROM academic_periods ORDER BY startDate DESC, endDate DESC")
    suspend fun getAllPeriodsOnce(): List<AcademicPeriodEntity>

    @Query("SELECT * FROM academic_periods WHERE id = :periodId LIMIT 1")
    suspend fun getPeriodById(periodId: Long): AcademicPeriodEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPeriod(period: AcademicPeriodEntity): Long

    @Update
    suspend fun updatePeriod(period: AcademicPeriodEntity): Int

    @Query("DELETE FROM academic_periods WHERE id = :periodId")
    suspend fun deletePeriod(periodId: Long): Int

    @Query("DELETE FROM academic_periods")
    suspend fun clearAllPeriods(): Int
}
