package com.mocas.ui.util

import android.content.Context
import android.widget.Toast
import com.mocas.data.repository.CalendarActionResult

fun showCalendarResult(context: Context, result: CalendarActionResult) {
    val message = when (result) {
        CalendarActionResult.Launched -> return
        CalendarActionResult.NoCompatibleApp -> "No hay una app de calendario compatible."
        is CalendarActionResult.InvalidData -> result.reason
        is CalendarActionResult.Failed -> result.reason
    }
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}
