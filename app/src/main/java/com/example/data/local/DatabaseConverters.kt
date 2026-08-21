package com.example.data.local

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
}