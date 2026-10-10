package com.mocas.ui.components.grade

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mocas.R
import java.util.Locale

@Composable
fun neededText(needed: Double?, passing: Double, gradedWeight: Double): String =
    when (needed) {
        null -> stringResource(R.string.imposible_alcanzar_meta, format(passing))
        0.0 -> stringResource(R.string.meta_ya_alcanzada, format(passing))
        else -> stringResource(R.string.necesitas_puntos_restante, format(needed), format(100.0 - gradedWeight))
    }

fun format(value: Double): String = String.format(Locale.US, "%.1f", value)