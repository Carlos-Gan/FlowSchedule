package com.moca.snapmyschedule.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.moca.snapmyschedule.data.local.dao.ClassSessionDao
import com.moca.snapmyschedule.data.local.entity.ClassSessionEntity

@Database(
    entities = [
        ClassSessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun classSessionDao(): ClassSessionDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getDatabase(
            context: Context
        ): AppDatabase {
            return instance ?: synchronized(this) {

                Room.databaseBuilder(
                    context = context.applicationContext,
                    klass = AppDatabase::class.java,
                    name = "snap_my_schedule_database"
                )
                    .build()
                    .also { database ->
                        instance = database
                    }
            }
        }
    }
}