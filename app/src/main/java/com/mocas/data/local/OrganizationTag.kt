package com.mocas.data.local

enum class OrganizationTag(val displayName: String) {
    UNIVERSIDAD("Universidad"),
    TRABAJO("Trabajo"),
    PERSONAL("Personal");

    companion object {
        fun fromStored(value: String): OrganizationTag =
            entries.firstOrNull { it.name == value } ?: UNIVERSIDAD
    }
}
