package com.moca.snapmyschedule.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.moca.snapmyschedule.data.model.ClassFormData
import com.moca.snapmyschedule.data.model.ClassSession
import com.moca.snapmyschedule.data.model.ScheduleBlock
import com.moca.snapmyschedule.ui.screens.AddClassScreen
import com.moca.snapmyschedule.ui.screens.ClassDetailsScreen
import com.moca.snapmyschedule.ui.screens.ImportScheduleScreen
import com.moca.snapmyschedule.ui.screens.ScheduleScreen
import com.moca.snapmyschedule.ui.viewmodel.ScheduleViewModel
import kotlinx.coroutines.launch

@Composable
fun AppNavHost(
    scheduleViewModel: ScheduleViewModel,
    navController: NavHostController =
        rememberNavController()
) {
    val classes by scheduleViewModel
        .sessions
        .collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )

    val coroutineScope = rememberCoroutineScope()

    val currentBackStackEntry by
    navController.currentBackStackEntryAsState()

    val currentRoute =
        currentBackStackEntry?.destination?.route

    val drawerGesturesEnabled =
        currentRoute == AppRoute.Schedule.route ||
                currentRoute ==
                AppRoute.ImportSchedule.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerGesturesEnabled,
        drawerContent = {
            AppDrawerContent(
                currentRoute = currentRoute,
                onScheduleClick = {
                    coroutineScope.launch {
                        drawerState.close()

                        if (
                            currentRoute !=
                            AppRoute.Schedule.route
                        ) {
                            navController.navigate(
                                AppRoute.Schedule.route
                            ) {
                                popUpTo(
                                    AppRoute.Schedule.route
                                )

                                launchSingleTop = true
                            }
                        }
                    }
                },
                onImportClick = {
                    coroutineScope.launch {
                        drawerState.close()

                        if (
                            currentRoute !=
                            AppRoute.ImportSchedule.route
                        ) {
                            navController.navigate(
                                AppRoute.ImportSchedule.route
                            ) {
                                popUpTo(
                                    AppRoute.Schedule.route
                                )

                                launchSingleTop = true
                            }
                        }
                    }
                }
            )
        }
    )
    {
        NavHost(
            navController = navController,
            startDestination = AppRoute.Schedule.route
        ) {
            composable(
                route = AppRoute.Schedule.route
            ) {
                ScheduleScreen(
                    classes = classes,
                    onOpenDrawer = {
                        coroutineScope.launch {
                            drawerState.open()
                        }
                    },
                    onAddClass = {
                        navController.navigate(
                            AppRoute.AddClass.route
                        ) {
                            launchSingleTop = true
                        }
                    },
                    onClassClick = { classSession ->
                        if (classSession.courseId.isNotBlank()) {
                            navController.navigate(
                                AppRoute.ClassDetails.createRoute(
                                    classSession.courseId
                                )
                            )
                        }
                    }
                )
            }

            composable(
                route = AppRoute.ImportSchedule.route
            ) {
                ImportScheduleScreen(
                    onOpenDrawer = {
                        coroutineScope.launch {
                            drawerState.open()
                        }
                    },
                    onImportClasses = { classes ->
                        scheduleViewModel.addClasses(
                            classes = classes
                        )

                        navController.navigate(
                            AppRoute.Schedule.route
                        ) {
                            popUpTo(
                                AppRoute.Schedule.route
                            ) {
                                inclusive = true
                            }

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
                    validateSchedule = { formData ->
                        scheduleViewModel.validateSchedule(
                            formData = formData
                        )
                    },
                    onSave = { formData ->
                        scheduleViewModel.addClass(
                            formData = formData
                        )
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = AppRoute.ClassDetails.route,
                arguments = listOf(
                    navArgument(
                        AppRoute.ClassDetails
                            .COURSE_ID_ARGUMENT
                    ) {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->

                val courseId = backStackEntry.arguments
                    ?.getString(
                        AppRoute.ClassDetails
                            .COURSE_ID_ARGUMENT
                    )
                    .orEmpty()

                val courseSessions = classes.filter {
                    it.courseId == courseId
                }

                ClassDetailsScreen(
                    sessions = courseSessions,
                    onBack = {
                        navController.popBackStack()
                    },
                    onEdit = {
                        navController.navigate(
                            AppRoute.EditClass.createRoute(
                                courseId
                            )
                        )
                    },
                    onDelete = {
                        scheduleViewModel.deleteCourse(
                            courseId
                        )

                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = AppRoute.EditClass.route,
                arguments = listOf(
                    navArgument(
                        AppRoute.EditClass.COURSE_ID_ARGUMENT
                    ) {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->

                val courseId = backStackEntry.arguments
                    ?.getString(
                        AppRoute.EditClass.COURSE_ID_ARGUMENT
                    )
                    .orEmpty()

                val courseSessions = classes.filter {
                    it.courseId == courseId
                }

                val initialData = remember(courseSessions) {
                    courseSessions.toClassFormData()
                }

                if (initialData != null) {
                    AddClassScreen(
                        initialData = initialData,
                        title = "Editar materia",
                        onBack = {
                            navController.popBackStack()
                        },
                        validateSchedule = { formData ->
                            scheduleViewModel.validateSchedule(
                                formData = formData,
                                excludedCourseId = courseId
                            )
                        },
                        onSave = { formData ->
                            scheduleViewModel.updateClass(
                                courseId = courseId,
                                formData = formData
                            )

                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}

private data class ScheduleBlockKey(
    val startTime: String,
    val endTime: String,
    val room: String
)

private fun List<ClassSession>.toClassFormData():
        ClassFormData? {

    val firstSession = firstOrNull()
        ?: return null

    val blocks = groupBy { session ->
        ScheduleBlockKey(
            startTime = session.startTime,
            endTime = session.endTime,
            room = session.room
        )
    }.map { (key, sessions) ->
        ScheduleBlock(
            days = sessions
                .map { it.day }
                .toSet(),
            startTime = key.startTime,
            endTime = key.endTime,
            room = key.room
        )
    }.sortedWith(
        compareBy<ScheduleBlock> { block ->
            block.days.minOfOrNull { day ->
                day.ordinal
            } ?: Int.MAX_VALUE
        }.thenBy { block ->
            block.startTime
        }
    )

    return ClassFormData(
        subjectName = firstSession.subjectName,
        subjectCode = firstSession.subjectCode,
        teacher = firstSession.teacher,
        scheduleBlocks = blocks
    )
}