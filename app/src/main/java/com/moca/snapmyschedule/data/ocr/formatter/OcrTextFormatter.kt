package com.moca.snapmyschedule.data.ocr.formatter

import java.util.Locale

fun String.toReadableTitleCase(): String {
    val locale = Locale("es", "MX")

    val lowercaseWords = setOf(
        "a",
        "al",
        "con",
        "de",
        "del",
        "el",
        "en",
        "la",
        "las",
        "los",
        "o",
        "para",
        "por",
        "u",
        "y"
    )

    val cleanedText = trim()
        .replace(
            Regex("\\s+"),
            " "
        )

    if (cleanedText.isBlank()) {
        return ""
    }

    return cleanedText
        .split(" ")
        .mapIndexed { index, originalWord ->

            val lowercaseWord =
                originalWord.lowercase(locale)

            val wordWithoutSymbols =
                lowercaseWord
                    .replace(
                        Regex("[^a-záéíóúüñ]"),
                        ""
                    )

            when {
                /*
                 * Conserva conectores en minúsculas:
                 *
                 * Ingeniería de Software
                 * Lenguajes y Autómatas
                 */
                index > 0 &&
                        wordWithoutSymbols in lowercaseWords -> {
                    lowercaseWord
                }

                /*
                 * Conserva números romanos:
                 *
                 * Autómatas I
                 * Autómatas II
                 */
                originalWord.matches(
                    Regex(
                        "^[IVXLCDM]+[.,]?$",
                        RegexOption.IGNORE_CASE
                    )
                ) -> {
                    originalWord.uppercase(locale)
                }

                /*
                 * Conserva siglas cortas:
                 *
                 * IA
                 * TI
                 * SQL
                 */
                originalWord
                    .filter { character ->
                        character.isLetter()
                    }
                    .let { letters ->
                        letters.length in 2..3 &&
                                letters.all { character ->
                                    character.isUpperCase()
                                }
                    } -> {
                    originalWord.uppercase(locale)
                }

                else -> {
                    lowercaseWord.replaceFirstChar { character ->

                        character.uppercase(locale)
                    }
                }
            }
        }
        .joinToString(" ")
}