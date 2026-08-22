package com.mocas.data.local

import androidx.room.TypeConverter

class DatabaseConverters {

    @TypeConverter
    fun schoolEventTypeToString(
        type: SchoolEventType
    ): String {
        return type.name
    }

    @TypeConverter
    fun stringToSchoolEventType(
        value: String?
    ): SchoolEventType {
        return SchoolEventType.fromString(value)
    }

    @TypeConverter
    fun classExceptionTypeToString(type: ClassExceptionType): String = type.name

    @TypeConverter
    fun stringToClassExceptionType(value: String): ClassExceptionType =
        runCatching { ClassExceptionType.valueOf(value) }.getOrDefault(ClassExceptionType.CANCELED)

    @TypeConverter
    fun eventPriorityToString(value: EventPriority): String = value.name

    @TypeConverter
    fun stringToEventPriority(value: String?): EventPriority = EventPriority.fromString(value)

    @TypeConverter
    fun recurrenceTypeToString(value: RecurrenceType): String = value.name

    @TypeConverter
    fun stringToRecurrenceType(value: String?): RecurrenceType = RecurrenceType.fromString(value)
}
