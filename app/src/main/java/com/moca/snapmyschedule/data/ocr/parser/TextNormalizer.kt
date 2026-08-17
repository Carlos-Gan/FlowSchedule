package com.moca.snapmyschedule.data.ocr.parser

import com.moca.snapmyschedule.data.model.WeekDay
import java.text.Normalizer
import java.util.Locale

internal fun String.normalizeForOcrMatch(): String {
    val withoutAccents = Normalizer
        .normalize(
            this,
            Normalizer.Form.NFD
        )
        .replace(
            Regex("\\p{Mn}+"),
            ""
        )

    return withoutAccents
        .lowercase(Locale.ROOT)
        .replace(
            Regex("[^a-z]"),
            ""
        )
}

internal fun String.toWeekDayOrNull(): WeekDay? {
    return when (normalizeForOcrMatch()) {
        "lunes",
        "lun",
        "monday",
        "mon" -> WeekDay.MONDAY

        "martes",
        "mar",
        "tuesday",
        "tue",
        "tues" -> WeekDay.TUESDAY

        "miercoles",
        "mier",
        "mie",
        "wednesday",
        "wed" -> WeekDay.WEDNESDAY

        "jueves",
        "jue",
        "thursday",
        "thu",
        "thur",
        "thurs" -> WeekDay.THURSDAY

        "viernes",
        "vie",
        "friday",
        "fri" -> WeekDay.FRIDAY

        "sabado",
        "sab",
        "saturday",
        "sat" -> WeekDay.SATURDAY

        "domingo",
        "dom",
        "sunday",
        "sun" -> WeekDay.SUNDAY

        else -> null
    }
}