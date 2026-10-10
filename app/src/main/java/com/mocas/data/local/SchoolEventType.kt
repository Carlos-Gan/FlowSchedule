package com.mocas.data.local

enum class SchoolEventType(
    val displayName: String
) {
    TAREA(
        displayName = "Tarea"
    ),

    EXAMEN(
        displayName = "Examen"
    ),

    EXPOSICION(
        displayName = "Exposición"
    ),

    EVENTO_ESCOLAR(
        displayName = "Evento escolar"
    ),

    REUNION(
        displayName = "Reunión"
    ),

    VACACIONES(
        displayName = "Vacaciones"
    ),

    OTRO(
        displayName = "Otro"
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