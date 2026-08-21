package com.example.ui.util

internal fun capitalizeFirstLetter(value: String): String {
    val index = value.indexOfFirst(Char::isLetter)
    if (index < 0 || value[index].isUpperCase()) return value
    return value.replaceRange(index, index + 1, value[index].uppercase())
}
