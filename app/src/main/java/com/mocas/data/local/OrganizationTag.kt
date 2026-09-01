package com.mocas.data.local

import com.mocas.R

enum class OrganizationTag(val titleRes: Int) {
    UNIVERSIDAD(R.string.tag_universidad),
    TRABAJO(R.string.tag_trabajo),
    PERSONAL(R.string.tag_personal);

    companion object {
        fun fromStored(value: String): OrganizationTag =
            entries.firstOrNull { it.name == value } ?: UNIVERSIDAD
    }
}
