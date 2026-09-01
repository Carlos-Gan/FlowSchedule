package com.mocas.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mocas.ui.components.AppTabContent
import com.mocas.ui.components.SnapBottomNavBar
import com.mocas.ui.components.SnapTopAppBar
import com.mocas.ui.components.UserMessageEffect
import com.mocas.ui.components.getTopBarConfig
import com.mocas.ui.dialogs.MainDialogHost
import com.mocas.ui.model.BottomNavTab
import com.mocas.ui.viewmodel.ScheduleViewModel

@Composable
fun MainAppScreen(
    viewModel: ScheduleViewModel
) {
    val focusManager = LocalFocusManager.current
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

    val isSearchActive by
    viewModel.isGlobalSearchOpen.collectAsStateWithLifecycle()

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

    var searchQuery by remember { mutableStateOf("") }

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

    val resolvedTitle = topBarConfig.title ?: topBarConfig.titleRes?.let { stringResource(it) } ?: ""
    val resolvedSubtitle = topBarConfig.subtitle ?: topBarConfig.subtitleRes?.let { resId ->
        topBarConfig.subtitleArgs?.let { args ->
            stringResource(resId, *args)
        } ?: stringResource(resId)
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
                title = resolvedTitle,
                subtitle = resolvedSubtitle,
                searchQuery = searchQuery,
                onQueryChange = { text ->
                    searchQuery = text
                },
                isSearchActive = isSearchActive,
                onSearchActiveChange = { active ->
                    if (active) {
                        viewModel.openGlobalSearch()
                    } else {
                        viewModel.closeGlobalSearch()
                        searchQuery = ""
                    }
                },
                subjects = subjectsWithSlots,
                events = allEventsWithSubject,
                // Acción al hacer clic en una materia
                onSubjectClick = { subjectId ->
                    searchQuery = "" // Limpia la búsqueda y cierra el popup visualmente
                    viewModel.closeGlobalSearch()
                    viewModel.openSubjectDetail(subjectId)
                },
                // Acción al hacer clic en un evento
                onEventClick = { eventItem ->
                    searchQuery = "" // Limpia la búsqueda y cierra el popup visualmente
                    viewModel.closeGlobalSearch()
                    viewModel.openAddEvent(eventToEdit = eventItem)
                },
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
                onTabSelected = {tab->
                    // Cierra la búsqueda activa al cambiar de pestaña
                    searchQuery=""
                    viewModel.closeGlobalSearch()
                    viewModel.setTab(tab)
                    focusManager.clearFocus()
                }
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
