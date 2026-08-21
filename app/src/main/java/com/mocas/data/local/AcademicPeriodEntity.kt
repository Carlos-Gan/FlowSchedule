package com.mocas.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "academic_periods",
    indices = [Index(value = ["startDate", "endDate"], unique = true)]
)
data class AcademicPeriodEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val startDate: String,
    val endDate: String,
    val colorHex: String = "#10B981",
    val createdAtMillis: Long = System.currentTimeMillis()
)
