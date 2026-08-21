package com.mocas.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ClassExceptionType { CANCELED, MODIFIED }

@Entity(
    tableName = "class_exceptions",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ScheduleSlotEntity::class,
            parentColumns = ["id"],
            childColumns = ["slotId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("subjectId"),
        Index(value = ["slotId", "date"], unique = true)
    ]
)
data class ClassExceptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val slotId: Long,
    val date: String,
    val type: ClassExceptionType,
    val newStartTime: String? = null,
    val newEndTime: String? = null,
    val newRoom: String? = null,
    val note: String = "",
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
)
