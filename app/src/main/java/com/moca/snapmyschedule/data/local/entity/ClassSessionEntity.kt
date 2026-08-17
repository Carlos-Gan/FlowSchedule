package com.moca.snapmyschedule.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "class_sessions",
    indices = [
        Index(value = ["courseId"]),
        Index(value = ["dayOrder"])
    ]
)
data class ClassSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /*
     * Todas las sesiones de una misma materia
     * compartirán este identificador.
     */
    val courseId: String,

    val subjectName: String,
    val subjectCode: String,
    val teacher: String,

    /*
    * El salón pertenece a la sesión porque puede
    * cambiar dependiendo del bloque de horario.
    */
    val room: String,

    /*
     * Guardamos el nombre del enum:
     * MONDAY, TUESDAY, WEDNESDAY...
     */
    val day: String,

    /*
     * Permite ordenar los días sin depender
     * de una consulta SQL compleja.
     */
    val dayOrder: Int,

    /*
     * Formato de 24 horas:
     * 08:00, 09:30, 14:00...
     */
    val startTime: String,
    val endTime: String
)