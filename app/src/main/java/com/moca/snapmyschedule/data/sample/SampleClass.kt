package com.moca.snapmyschedule.data.sample

import com.moca.snapmyschedule.data.model.ClassSession
import com.moca.snapmyschedule.data.model.WeekDay

val sampleClasses = listOf(
    ClassSession(
        id = 1,
        subjectName = "Estructura de Datos",
        subjectCode = "IF1909",
        teacher = "Felipe Alanís González",
        room = "SC4",
        day = WeekDay.MONDAY,
        startTime = "09:00",
        endTime = "10:00"
    ),
    ClassSession(
        id = 2,
        subjectName = "Física General",
        subjectCode = "SI1810",
        teacher = "José Luis García Rodríguez",
        room = "SC4",
        day = WeekDay.MONDAY,
        startTime = "10:00",
        endTime = "11:00"
    ),
    ClassSession(
        id = 3,
        subjectName = "Cálculo Vectorial",
        subjectCode = "CO1004",
        teacher = "Docente pendiente",
        room = "SC4",
        day = WeekDay.MONDAY,
        startTime = "11:00",
        endTime = "12:00"
    ),
    ClassSession(
        id = 4,
        subjectName = "Estructura de Datos",
        subjectCode = "IF1909",
        teacher = "Felipe Alanís González",
        room = "SC4",
        day = WeekDay.TUESDAY,
        startTime = "09:00",
        endTime = "10:00"
    ),
    ClassSession(
        id = 5,
        subjectName = "Cultura Empresarial",
        subjectCode = "SI1808",
        teacher = "Brenda Avitia Rocha",
        room = "SC4",
        day = WeekDay.TUESDAY,
        startTime = "12:00",
        endTime = "13:00"
    ),
    ClassSession(
        id = 6,
        subjectName = "Desarrollo Sustentable",
        subjectCode = "IT8833",
        teacher = "María Luisa Ortiz Parga",
        room = "SC4",
        day = WeekDay.WEDNESDAY,
        startTime = "08:00",
        endTime = "09:00"
    ),
    ClassSession(
        id = 7,
        subjectName = "Investigación de Operaciones",
        subjectCode = "SCC1013",
        teacher = "Laura Guadalupe Butzmann",
        room = "SC4",
        day = WeekDay.THURSDAY,
        startTime = "07:00",
        endTime = "08:00"
    ),
    ClassSession(
        id = 8,
        subjectName = "Física General",
        subjectCode = "SI1810",
        teacher = "José Luis García Rodríguez",
        room = "SC4",
        day = WeekDay.FRIDAY,
        startTime = "10:00",
        endTime = "11:00"
    )
)