package com.mocas.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassExceptionDao {
    @Query("SELECT * FROM class_exceptions ORDER BY date ASC")
    fun getAll(): Flow<List<ClassExceptionEntity>>

    @Query("SELECT * FROM class_exceptions ORDER BY date ASC")
    suspend fun getAllOnce(): List<ClassExceptionEntity>

    @Query("SELECT * FROM class_exceptions WHERE slotId = :slotId AND date = :date LIMIT 1")
    suspend fun getForOccurrence(slotId: Long, date: String): ClassExceptionEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(item: ClassExceptionEntity): Long

    @Update
    suspend fun update(item: ClassExceptionEntity): Int

    @Query("DELETE FROM class_exceptions WHERE id = :id")
    suspend fun delete(id: Long): Int

    @Query("DELETE FROM class_exceptions")
    suspend fun clearAll(): Int
}
