package com.mocas.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "school_events",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("subjectId"),
        Index(value = ["startDate", "startTime"]),
        Index("isCompleted"),
        Index("calendarEventId")
    ]
)
data class SchoolEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String,

    val type: SchoolEventType = SchoolEventType.TAREA,

    val subjectId: Long? = null,

    // Formato yyyy-MM-dd
    val startDate: String,

    // Permite eventos de varios días.
    // Para eventos de un día debe ser igual a startDate.
    val endDate: String = startDate,

    // Deben ser null cuando isAllDay sea true.
    // Formato HH:mm.
    val startTime: String? = null,
    val endTime: String? = null,

    val isAllDay: Boolean = false,

    val location: String = "",
    val description: String = "",
    val organizationTag: String = OrganizationTag.UNIVERSIDAD.name,
    val isImportant: Boolean = false,

    val priority: EventPriority = EventPriority.MEDIUM,
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val recurrenceInterval: Int = 1,
    val recurrenceEndDate: String? = null,
    val recurrenceGroupId: String? = null,

    val reminderMinutes: Int = 30,
    val isCompleted: Boolean = false,

    val syncCalendar: Boolean = false,

    // ID generado por CalendarContract al insertar directamente.
    val calendarEventId: Long? = null,

    // Calendario del teléfono donde fue guardado.
    val calendarId: Long? = null,

    val lastCalendarSyncMillis: Long? = null,

    val isDeleted: Boolean = false,
    val deletedAtMillis: Long? = null,

    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
)
