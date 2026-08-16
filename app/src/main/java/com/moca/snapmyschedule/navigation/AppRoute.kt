package com.moca.snapmyschedule.navigation

sealed class AppRoute(
    val route: String
) {
    object Schedule : AppRoute(
        route = "schedule"
    )

    object AddClass : AppRoute(
        route = "add_class"
    )
}