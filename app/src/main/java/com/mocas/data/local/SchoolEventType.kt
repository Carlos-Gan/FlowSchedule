package com.mocas.data.local

enum class SchoolEventType(
    val displayName: String,
    val iconName: String,
    val defaultColorHex: String
) {
    TAREA(
        displayName = "Tarea",
        iconName = "Assignment",
        defaultColorHex = "#8B5CF6"
    ),

    EXAMEN(
        displayName = "Examen",
        iconName = "Quiz",
        defaultColorHex = "#EF4444"
    ),

    EXPOSICION(
        displayName = "Exposición",
        iconName = "CoPresent",
        defaultColorHex = "#06B6D4"
    ),

    EVENTO_ESCOLAR(
        displayName = "Evento escolar",
        iconName = "School",
        defaultColorHex = "#F59E0B"
    ),

    REUNION(
        displayName = "Reunión",
        iconName = "Groups",
        defaultColorHex = "#3B82F6"
    ),

    VACACIONES(
        displayName = "Vacaciones",
        iconName = "BeachAccess",
        defaultColorHex = "#10B981"
    ),

    OTRO(
        displayName = "Otro",
        iconName = "Event",
        defaultColorHex = "#64748B"
    );

    companion object {
        fun fromString(value: String?): SchoolEventType {
            if (value.isNullOrBlank()) {
                return OTRO
            }

            val normalizedValue = value.trim()

            return entries.firstOrNull { type ->
                type.name.equals(normalizedValue, ignoreCase = true) ||
                        type.displayName.equals(normalizedValue, ignoreCase = true)
            } ?: OTRO
        }
    }
}