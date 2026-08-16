package com.moca.snapmyschedule.data.model

import java.time.LocalTime
import java.time.MonthDay

enum class WeekDay(
    val displayName: String,
    val shortName: String,
    val order:Int
){
    MONDAY("Lunes", "Lun", 1),
    TUESDAY("Martes", "Mar", 2),
    WEDNESDAY("Miercoles", "Mie", 3),
    THURSDAY("Jueves", "Jue", 4),
    FRIDAY("Viernes", "Vie", 5),
    SATURDAY("Sabado", "Sab", 6),
    SUNDAY("Domingo", "Dom",7)
}

data class ClassSession(
    val id: Long,
    val subjectName: String,
    val subjectCode: String = "",
    val teacher: String = "",
    val room: String = "",
    val day: WeekDay,
    val startTime: String,
    val endTime: String
)