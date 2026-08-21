package com.mocas.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        SubjectEntity::class,
        ScheduleSlotEntity::class,
        SchoolEventEntity::class,
        AcademicPeriodEntity::class,
        ClassExceptionEntity::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun subjectDao(): SubjectDao

    abstract fun scheduleSlotDao(): ScheduleSlotDao

    abstract fun schoolEventDao(): SchoolEventDao

    abstract fun academicPeriodDao(): AcademicPeriodDao
    abstract fun classExceptionDao(): ClassExceptionDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "snap_my_schedule_db_v2"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                    .also { database ->
                        INSTANCE = database
                    }
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `academic_periods` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `startDate` TEXT NOT NULL,
                        `endDate` TEXT NOT NULL,
                        `createdAtMillis` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS
                    `index_academic_periods_startDate_endDate`
                    ON `academic_periods` (`startDate`, `endDate`)
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `academic_periods` " +
                        "ADD COLUMN `colorHex` TEXT NOT NULL DEFAULT '#10B981'"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `class_exceptions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `subjectId` INTEGER NOT NULL,
                        `slotId` INTEGER NOT NULL,
                        `date` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `newStartTime` TEXT,
                        `newEndTime` TEXT,
                        `newRoom` TEXT,
                        `note` TEXT NOT NULL,
                        `createdAtMillis` INTEGER NOT NULL,
                        `updatedAtMillis` INTEGER NOT NULL,
                        FOREIGN KEY(`subjectId`) REFERENCES `subjects`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`slotId`) REFERENCES `schedule_slots`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_class_exceptions_subjectId` ON `class_exceptions` (`subjectId`)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_class_exceptions_slotId_date` ON `class_exceptions` (`slotId`, `date`)")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `subjects` ADD COLUMN `organizationTag` TEXT NOT NULL DEFAULT 'UNIVERSIDAD'")
                db.execSQL("ALTER TABLE `subjects` ADD COLUMN `isImportant` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `school_events` ADD COLUMN `organizationTag` TEXT NOT NULL DEFAULT 'UNIVERSIDAD'")
                db.execSQL("ALTER TABLE `school_events` ADD COLUMN `isImportant` INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
