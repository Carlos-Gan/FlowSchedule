package com.mocas.ui.components

import com.mocas.ui.model.BottomNavTab

data class TopBarConfig(
    val title: String,
    val subtitle: String?,
    val showScanAction: Boolean,
    val showAddAction: Boolean
)

fun getTopBarConfig(
    currentTab: BottomNavTab,
    subjectCount: Int,
    pendingEventCount: Int
): TopBarConfig {
    return when (currentTab) {
        BottomNavTab.INICIO -> TopBarConfig(
            title = "SnapMySchedule",
            subtitle = "Tu día de un vistazo",
            showScanAction = true,
            showAddAction = false
        )

        BottomNavTab.HORARIO -> TopBarConfig(
            title = "Horario",
            subtitle = when (subjectCount) {
                0 -> "Todavía no tienes materias"
                1 -> "1 materia registrada"
                else -> "$subjectCount materias registradas"
            },
            showScanAction = true,
            showAddAction = true
        )

        BottomNavTab.CALENDARIO -> TopBarConfig(
            title = "Calendario",
            subtitle = "Fechas importantes del semestre",
            showScanAction = false,
            showAddAction = true
        )

        BottomNavTab.EVENTOS -> TopBarConfig(
            title = "Actividades",
            subtitle = when (pendingEventCount) {
                0 -> "No tienes actividades pendientes"
                1 -> "1 actividad pendiente"
                else -> "$pendingEventCount actividades pendientes"
            },
            showScanAction = false,
            showAddAction = true
        )

        BottomNavTab.CONFIGURACION -> TopBarConfig(
            title = "Configuración",
            subtitle = "Personaliza tu experiencia",
            showScanAction = false,
            showAddAction = false
        )
    }
}
