package com.mocas.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "subjects",
    indices = [
        Index("name"),
        Index("code")
    ]
)
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,
    val code: String = "",
    val professor: String = "",
    val defaultRoom: String = "",
    val colorHex: String = "#3B82F6",
    val organizationTag: String = OrganizationTag.UNIVERSIDAD.name,
    val isImportant: Boolean = false,

    // Siempre usar formato yyyy-MM-dd
    val semesterStart: String,
    val semesterEnd: String,

    val reminderMinutesBefore: Int = 15,

    // Es mejor que el usuario active esta opción explícitamente.
    val syncCalendar: Boolean = false,

    val isDeleted: Boolean = false,
    val deletedAtMillis: Long? = null,

    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
)
