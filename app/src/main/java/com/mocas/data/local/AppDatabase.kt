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
        ClassExceptionEntity::class,
        SubtaskEntity::class,
        GradeCategoryEntity::class,
        GradeUnitEntity::class,
        GradeUnitCategoryWeightEntity::class,
        GradeItemEntity::class
    ],
    version = 11,
    exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun subjectDao(): SubjectDao

    abstract fun scheduleSlotDao(): ScheduleSlotDao

    abstract fun schoolEventDao(): SchoolEventDao

    abstract fun academicPeriodDao(): AcademicPeriodDao
    abstract fun classExceptionDao(): ClassExceptionDao
    abstract fun subtaskDao(): SubtaskDao
    abstract fun gradeDao(): GradeDao

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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11)
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

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `subjects` ADD COLUMN `isDeleted` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `subjects` ADD COLUMN `deletedAtMillis` INTEGER")
                db.execSQL("ALTER TABLE `school_events` ADD COLUMN `isDeleted` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `school_events` ADD COLUMN `deletedAtMillis` INTEGER")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `school_events` ADD COLUMN `priority` TEXT NOT NULL DEFAULT 'MEDIUM'")
                db.execSQL("ALTER TABLE `school_events` ADD COLUMN `recurrenceType` TEXT NOT NULL DEFAULT 'NONE'")
                db.execSQL("ALTER TABLE `school_events` ADD COLUMN `recurrenceInterval` INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE `school_events` ADD COLUMN `recurrenceEndDate` TEXT")
                db.execSQL("ALTER TABLE `school_events` ADD COLUMN `recurrenceGroupId` TEXT")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `event_subtasks` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `eventId` INTEGER NOT NULL,
                        `title` TEXT NOT NULL,
                        `isCompleted` INTEGER NOT NULL,
                        `sortOrder` INTEGER NOT NULL,
                        `createdAtMillis` INTEGER NOT NULL,
                        `updatedAtMillis` INTEGER NOT NULL,
                        FOREIGN KEY(`eventId`) REFERENCES `school_events`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_event_subtasks_eventId` ON `event_subtasks` (`eventId`)")
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `grade_categories` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `subjectId` INTEGER NOT NULL,
                        `name` TEXT NOT NULL,
                        `weightPercent` REAL NOT NULL,
                        `sortOrder` INTEGER NOT NULL,
                        `createdAtMillis` INTEGER NOT NULL,
                        FOREIGN KEY(`subjectId`) REFERENCES `subjects`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_grade_categories_subjectId` ON `grade_categories` (`subjectId`)")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `grade_items` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `categoryId` INTEGER NOT NULL,
                        `name` TEXT NOT NULL,
                        `score` REAL NOT NULL,
                        `createdAtMillis` INTEGER NOT NULL,
                        FOREIGN KEY(`categoryId`) REFERENCES `grade_categories`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_grade_items_categoryId` ON `grade_items` (`categoryId`)")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `grade_items` ADD COLUMN `unitName` TEXT NOT NULL DEFAULT 'Unidad 1'")
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `grade_units` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `subjectId` INTEGER NOT NULL,
                        `name` TEXT NOT NULL,
                        `sortOrder` INTEGER NOT NULL,
                        `createdAtMillis` INTEGER NOT NULL,
                        FOREIGN KEY(`subjectId`) REFERENCES `subjects`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_grade_units_subjectId` ON `grade_units` (`subjectId`)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_grade_units_subjectId_name` ON `grade_units` (`subjectId`, `name`)")
                db.execSQL("""
                    INSERT OR IGNORE INTO grade_units(subjectId, name, sortOrder, createdAtMillis)
                    SELECT gc.subjectId, gi.unitName, 0, gi.createdAtMillis
                    FROM grade_items gi JOIN grade_categories gc ON gc.id = gi.categoryId
                """.trimIndent())
                db.execSQL("ALTER TABLE `grade_items` ADD COLUMN `unitId` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("""
                    UPDATE grade_items SET unitId = COALESCE((
                        SELECT gu.id FROM grade_units gu
                        JOIN grade_categories gc ON gc.subjectId = gu.subjectId
                        WHERE gc.id = grade_items.categoryId AND gu.name = grade_items.unitName
                        LIMIT 1
                    ), 0)
                """.trimIndent())
            }
        }

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `grade_unit_category_weights` (
                        `unitId` INTEGER NOT NULL,
                        `categoryId` INTEGER NOT NULL,
                        `weightPercent` REAL NOT NULL,
                        PRIMARY KEY(`unitId`, `categoryId`),
                        FOREIGN KEY(`unitId`) REFERENCES `grade_units`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`categoryId`) REFERENCES `grade_categories`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_grade_unit_category_weights_categoryId` ON `grade_unit_category_weights` (`categoryId`)")
            }
        }
    }
}
