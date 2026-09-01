package com.mocas.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class EventWithSubject(
    @Embedded
    val event: SchoolEventEntity,

    @Relation(
        parentColumn = "subjectId",
        entityColumn = "id"
    )
    val subject: SubjectEntity?
)
