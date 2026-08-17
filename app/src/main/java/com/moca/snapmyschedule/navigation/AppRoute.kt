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

    object ImportSchedule : AppRoute(
        route = "import_schedule"
    )

    object ClassDetails : AppRoute(
        route = "class_details/{courseId}"
    ){
        const val COURSE_ID_ARGUMENT = "courseId"

        fun createRoute(
            courseId : String
        ): String{
            return "class_details/$courseId"
        }
    }

    object EditClass : AppRoute(
        route = "edit_class/{courseId}"
    ) {
        const val COURSE_ID_ARGUMENT = "courseId"

        fun createRoute(
            courseId: String
        ): String {
            return "edit_class/$courseId"
        }
    }
}