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
}
