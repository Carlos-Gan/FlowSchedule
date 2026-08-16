package com.moca.snapmyschedule.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.moca.snapmyschedule.data.model.ClassFormData
import com.moca.snapmyschedule.data.model.ClassSession
import com.moca.snapmyschedule.data.sample.sampleClasses
import com.moca.snapmyschedule.ui.screens.AddClassScreen
import com.moca.snapmyschedule.ui.screens.ScheduleScreen

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController()
) {
    val classes = remember {
        mutableStateListOf<ClassSession>().apply {
            addAll(sampleClasses)
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppRoute.Schedule.route
    ) {
        composable(
            route = AppRoute.Schedule.route
        ) {
            ScheduleScreen(
                classes = classes,
                onAddClass = {
                    navController.navigate(
                        AppRoute.AddClass.route
                    ) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = AppRoute.AddClass.route
        ) {
            AddClassScreen(
                onBack = {
                    navController.popBackStack()
                },
                onSave = { formData ->
                    val nextId = (
                            classes.maxOfOrNull { it.id } ?: 0L
                            ) + 1L

                    classes.addAll(
                        formData.toClassSessions(
                            firstId = nextId
                        )
                    )

                    navController.popBackStack()
                }
            )
        }
    }
}

private fun ClassFormData.toClassSessions(
    firstId: Long
): List<ClassSession> {
    var currentId = firstId

    return scheduleBlock.flatMap { block ->
        block.days
            .sortedBy { day -> day.ordinal }
            .map { day ->
                ClassSession(
                    id = currentId++,
                    subjectName = subjectName,
                    subjectCode = subjectCode,
                    teacher = teacher,
                    room = block.room,
                    day = day,
                    startTime = block.startTime,
                    endTime = block.endTime
                )
            }
    }
}