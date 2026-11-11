package com.mocas.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
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
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mocas.ui.add.subject.SubjectDetailScreen
import com.mocas.ui.components.AppTabContent
import com.mocas.ui.components.bar.SnapBottomNavBar
import com.mocas.ui.components.bar.SnapNavigationRail
import com.mocas.ui.components.bar.SnapTopAppBar
import com.mocas.ui.components.UserMessageEffect
import com.mocas.ui.components.bar.getTopBarConfig
import com.mocas.ui.dialogs.MainDialogHost
import com.mocas.ui.model.BottomNavTab
import com.mocas.ui.viewmodel.ScheduleViewModel

@Composable
fun MainAppScreen(
    viewModel: ScheduleViewModel,
    windowSizeClass: WindowSizeClass
) {
    val isCompact = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact
    var showAcademicProgress by remember { mutableStateOf(false) }
    var showSettingsSubScreen by remember { mutableStateOf(false) }
    BackHandler(showAcademicProgress) { showAcademicProgress = false }
    val selectedDetailId by viewModel.selectedSubjectDetailId.collectAsStateWithLifecycle()
    BackHandler(selectedDetailId != null && !showAcademicProgress) { viewModel.closeSubjectDetail() }
    val focusManager = LocalFocusManager.current
    val currentTab by
    viewModel.currentTab.collectAsStateWithLifecycle()
    val isScheduleComparisonOpen by viewModel.isScheduleComparisonOpen.collectAsStateWithLifecycle()
    BackHandler(isScheduleComparisonOpen) { viewModel.closeScheduleComparison() }

    val subjectsWithSlots by
    viewModel.subjectsWithSlots.collectAsStateWithLifecycle()

    val gradeCategories by viewModel.gradeCategories.collectAsStateWithLifecycle()
    val gradeItems by viewModel.gradeItems.collectAsStateWithLifecycle()
    val gradeUnits by viewModel.gradeUnits.collectAsStateWithLifecycle()
    val gradeUnitCategoryWeights by viewModel.gradeUnitCategoryWeights.collectAsStateWithLifecycle()

    val allEventsWithSubject by
    viewModel.allEventsWithSubject.collectAsStateWithLifecycle()

    val selectedSubject = remember(selectedDetailId, subjectsWithSlots) {
        subjectsWithSlots.firstOrNull { it.subject.id == selectedDetailId }
    }
    val linkedSubjectEvents = remember(selectedDetailId, allEventsWithSubject) {
        allEventsWithSubject.filter { it.event.subjectId == selectedDetailId }
    }

    val userMessage by
    viewModel.userMessage.collectAsStateWithLifecycle()

    val isSearchActive by
    viewModel.isGlobalSearchOpen.collectAsStateWithLifecycle()

    val appSettings by
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

    Row(modifier = Modifier.fillMaxSize()) {
        if (!isCompact && selectedDetailId == null && !showAcademicProgress && !isScheduleComparisonOpen) {
            SnapNavigationRail(
                selectedTab = currentTab,
                pendingEventCount = pendingEventCount,
                badgeStyle = appSettings.badgeStyle,
                onTabSelected = { tab ->
                    searchQuery = ""
                    viewModel.closeGlobalSearch()
                    viewModel.setTab(tab)
                    focusManager.clearFocus()
                }
            )
        }

        Scaffold(
            modifier = Modifier.weight(1f),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = if (selectedSubject != null || isScheduleComparisonOpen) WindowInsets(0, 0, 0, 0) else ScaffoldDefaults.contentWindowInsets,
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            topBar = {
                if (!showAcademicProgress && selectedDetailId == null && !showSettingsSubScreen && !isScheduleComparisonOpen) {
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
                    onSubjectClick = { subjectId ->
                        searchQuery = ""
                        viewModel.closeGlobalSearch()
                        viewModel.openSubjectDetail(subjectId)
                    },
                    onEventClick = { eventItem ->
                        searchQuery = ""
                        viewModel.closeGlobalSearch()
                        viewModel.openAddEvent(eventToEdit = eventItem)
                    },
                    onAddClick = if (topBarConfig.showAddAction) {
                        {
                            when (currentTab) {
                                BottomNavTab.HORARIO -> viewModel.openAddSubject()
                                BottomNavTab.CALENDARIO, BottomNavTab.EVENTOS -> viewModel.openAddEvent()
                                else -> {}
                            }
                        }
                    } else null
                )
                }
            },
            bottomBar = {
                if (isCompact && !showAcademicProgress && selectedDetailId == null && !showSettingsSubScreen && !isScheduleComparisonOpen) {
                    SnapBottomNavBar(
                        selectedTab = currentTab,
                        pendingEventCount = pendingEventCount,
                        badgeStyle = appSettings.badgeStyle,
                        onTabSelected = { tab ->
                            searchQuery = ""
                            viewModel.closeGlobalSearch()
                            viewModel.setTab(tab)
                            focusManager.clearFocus()
                        }
                    )
                }
            }
        ) { innerPadding ->
            if (selectedDetailId != null && selectedSubject != null) {
                SubjectDetailScreen(
                    subjectWithSlots = selectedSubject,
                    linkedEvents = linkedSubjectEvents,
                    onDismiss = viewModel::closeSubjectDetail,
                    onEditClick = {
                        viewModel.closeSubjectDetail()
                        viewModel.openAddSubject(selectedSubject)
                    },
                    onDeleteClick = {
                        val subjectId = selectedSubject.subject.id
                        viewModel.closeSubjectDetail()
                        viewModel.deleteSubject(subjectId)
                    },
                    onAddEventClick = {
                        val subjectId = selectedSubject.subject.id
                        viewModel.closeSubjectDetail()
                        viewModel.openAddEvent(subjectId = subjectId)
                    },
                    onToggleEventCompleted = { eventId, completed -> viewModel.toggleEventCompleted(eventId, completed) },
                    onToggleSubtask = viewModel::toggleSubtaskCompleted,
                    onEditEvent = { event ->
                        if (event.event.countsTowardGrade) {
                            viewModel.openGradeEntry(event.event.id)
                        } else {
                            viewModel.closeSubjectDetail()
                            viewModel.openAddEvent(event)
                        }
                    },
                    gradeCategories = gradeCategories.filter { it.subjectId == selectedSubject.subject.id },
                    gradeItems = gradeItems,
                    gradeUnits = gradeUnits.filter { it.subjectId == selectedSubject.subject.id },
                    gradeUnitCategoryWeights = gradeUnitCategoryWeights,
                    onAddGradeCategory = viewModel::addGradeCategory,
                    onAddGradeItem = viewModel::addGradeItem,
                    onDeleteGradeCategory = viewModel::deleteGradeCategory,
                    onDeleteItem = viewModel::deleteGradeItem,
                    onAddGradeUnit = viewModel::addGradeUnit,
                    onDeleteGradeUnit = viewModel::deleteGradeUnit,
                    onSaveUnitCategoryWeights = viewModel::saveUnitCategoryWeights,
                    onResetUnitCategoryWeights = viewModel::resetUnitCategoryWeights,
                    modifier = Modifier.fillMaxSize().padding(innerPadding)
                )
            } else if (showAcademicProgress) {
                AcademicProgressScreen(
                    viewModel = viewModel,
                    onBack = { showAcademicProgress = false },
                    modifier = Modifier.fillMaxSize().padding(innerPadding)
                )
            } else AppTabContent(
                currentTab = currentTab,
                viewModel = viewModel,
                windowSizeClass = windowSizeClass,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                onOpenAcademicProgress = { showAcademicProgress = true },
                onSettingsSubScreenChanged = { showSettingsSubScreen = it }
            )
        }
    }

    MainDialogHost(
        viewModel = viewModel
    )
}
