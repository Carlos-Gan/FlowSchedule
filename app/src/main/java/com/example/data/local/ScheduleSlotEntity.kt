package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "schedule_slots",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("subjectId"),

        // Evita guardar exactamente el mismo horario dos veces.
        Index(
            value = [
                "subjectId",
                "dayOfWeek",
                "startTime",
                "endTime"
            ],
            unique = true
        ),

        Index("calendarEventId")
    ]
)
data class ScheduleSlotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val subjectId: Long,

    /**
     * Coincide con java.time.DayOfWeek:
     * 1 = lunes
     * 2 = martes
     * 3 = miércoles
     * 4 = jueves
     * 5 = viernes
     * 6 = sábado
     * 7 = domingo
     */
    val dayOfWeek: Int,

    // Formato HH:mm
    val startTime: String,
    val endTime: String,

    // Si está vacío, se usa SubjectEntity.defaultRoom.
    val room: String = "",

    // Cada horario recurrente tendrá su propio evento.
    val calendarEventId: Long? = null,
    val calendarId: Long? = null,
    val lastCalendarSyncMillis: Long? = null
)