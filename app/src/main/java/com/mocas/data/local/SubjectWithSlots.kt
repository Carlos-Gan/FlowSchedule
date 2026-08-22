package com.mocas.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class SubjectWithSlots(
    @Embedded
    val subject: SubjectEntity,

    @Relation(
        entity = ScheduleSlotEntity::class,
        parentColumn = "id",
        entityColumn = "subjectId"
    )
    val slots: List<ScheduleSlotEntity> = emptyList()
)

data class SchoolEventWithSubject(
    @Embedded
    val event: SchoolEventEntity,

    @Relation(
        entity = SubjectEntity::class,
        parentColumn = "subjectId",
        entityColumn = "id"
    )
    val subject: SubjectEntity? = null,

    @Relation(
        entity = SubtaskEntity::class,
        parentColumn = "id",
        entityColumn = "eventId"
    )
    val subtasks: List<SubtaskEntity> = emptyList()
)
