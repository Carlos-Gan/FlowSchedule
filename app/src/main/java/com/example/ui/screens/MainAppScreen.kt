package com.example.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AppTabContent
import com.example.ui.components.SnapBottomNavBar
import com.example.ui.components.SnapTopAppBar
import com.example.ui.components.UserMessageEffect
import com.example.ui.components.getTopBarConfig
import com.example.ui.dialogs.MainDialogHost
import com.example.ui.model.BottomNavTab
import com.example.ui.viewmodel.ScheduleViewModel

@Composable
fun MainAppScreen(
    viewModel: ScheduleViewModel
) {
    val currentTab by
    viewModel.currentTab.collectAsStateWithLifecycle()

    val subjectsWithSlots by
    viewModel.subjectsWithSlots.collectAsStateWithLifecycle()

    val allEventsWithSubject by
    viewModel.allEventsWithSubject.collectAsStateWithLifecycle()

    val userMessage by
    viewModel.userMessage.collectAsStateWithLifecycle()

    val settings by
    viewModel.appSettings.collectAsStateWithLifecycle()

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    val pendingEventCount = remember(
        allEventsWithSubject
    ) {
        allEventsWithSubject.count {
            !it.event.isCompleted
        }
    }

    val topBarConfig = remember(
        currentTab,
        subjectsWithSlots.size,
        pendingEventCount
    ) {
        getTopBarConfig(
            currentTab = currentTab,
            subjectCount = subjectsWithSlots.size,
            pendingEventCount = pendingEventCount
        )
    }

    UserMessageEffect(
        message = userMessage,
        snackbarHostState = snackbarHostState,
        onMessageConsumed = viewModel::clearUserMessage
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor =
            MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            )
        },
        topBar = {
            SnapTopAppBar(
                title = topBarConfig.title,
                subtitle = topBarConfig.subtitle,
                onScanClick = if (
                    topBarConfig.showScanAction &&
                    settings.aiFeaturesEnabled
                ) {
                    viewModel::openImportSchedule
                } else {
                    null
                },
                onAddClick = if (
                    topBarConfig.showAddAction
                ) {
                    {
                        when (currentTab) {
                            BottomNavTab.HORARIO -> {
                                viewModel.openAddSubject()
                            }

                            BottomNavTab.CALENDARIO,
                            BottomNavTab.EVENTOS -> {
                                viewModel.openAddEvent()
                            }

                            BottomNavTab.INICIO,
                            BottomNavTab.CONFIGURACION -> {
                            }
                        }
                    }
                } else {
                    null
                }
            )
        },
        bottomBar = {
            SnapBottomNavBar(
                selectedTab = currentTab,
                onTabSelected = viewModel::setTab
            )
        }
    ) { innerPadding ->
        AppTabContent(
            currentTab = currentTab,
            viewModel = viewModel,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }

    MainDialogHost(
        viewModel = viewModel
    )
}
