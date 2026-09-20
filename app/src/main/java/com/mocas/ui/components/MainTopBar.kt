package com.mocas.ui.components

import com.mocas.R
import com.mocas.ui.model.BottomNavTab

data class TopBarConfig(
    val title: String? = null,
    val titleRes: Int? = null,
    val subtitle: String? = null,
    val subtitleRes: Int? = null,
    val subtitleArgs: Array<Any>? = null,
    val showScanAction: Boolean,
    val showAddAction: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TopBarConfig

        if (titleRes != other.titleRes) return false
        if (subtitleRes != other.subtitleRes) return false
        if (showScanAction != other.showScanAction) return false
        if (showAddAction != other.showAddAction) return false
        if (title != other.title) return false
        if (subtitle != other.subtitle) return false
        if (!subtitleArgs.contentEquals(other.subtitleArgs)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = titleRes ?: 0
        result = 31 * result + (subtitleRes ?: 0)
        result = 31 * result + showScanAction.hashCode()
        result = 31 * result + showAddAction.hashCode()
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (subtitle?.hashCode() ?: 0)
        result = 31 * result + (subtitleArgs?.contentHashCode() ?: 0)
        return result
    }
}

fun getTopBarConfig(
    currentTab: BottomNavTab,
    subjectCount: Int,
    pendingEventCount: Int
): TopBarConfig {
    return when (currentTab) {
        BottomNavTab.INICIO -> TopBarConfig(
            title = "FlowSchedule",
            subtitleRes = R.string.sub_inicio,
            showScanAction = true,
            showAddAction = false
        )

        BottomNavTab.HORARIO -> TopBarConfig(
            titleRes = R.string.nav_horario,
            subtitleRes = when (subjectCount) {
                0 -> R.string.sub_horario_vacio
                1 -> R.string.materia_singular
                else -> R.string.sub_horario_materias_formato
            },
            subtitleArgs = if (subjectCount > 1) arrayOf(subjectCount) else null,
            showScanAction = true,
            showAddAction = true
        )

        BottomNavTab.CALENDARIO -> TopBarConfig(
            titleRes = R.string.nav_calendario,
            subtitleRes = R.string.sub_calendario,
            showScanAction = true,
            showAddAction = true
        )

        BottomNavTab.EVENTOS -> TopBarConfig(
            titleRes = R.string.nav_eventos,
            subtitleRes = when (pendingEventCount) {
                0 -> R.string.sub_actividades_vacio
                1 -> R.string.actividad_singular
                else -> R.string.sub_actividades_pendientes_formato
            },
            subtitleArgs = if (pendingEventCount > 1) arrayOf(pendingEventCount) else null,
            showScanAction = true,
            showAddAction = true
        )

        BottomNavTab.CONFIGURACION -> TopBarConfig(
            titleRes = R.string.titulo_configuracion,
            subtitleRes = R.string.sub_configuracion,
            showScanAction = false,
            showAddAction = false
        )
    }
}
