package com.mocas.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import com.mocas.ui.model.BottomNavTab
import com.mocas.ui.screens.DashboardScreen
import com.mocas.ui.screens.CalendarScreen
import com.mocas.ui.screens.EventsScreen
import com.mocas.ui.screens.SettingsScreen
import com.mocas.ui.screens.TimetableScreen
import com.mocas.ui.viewmodel.ScheduleViewModel


@Composable
fun AppTabContent(
    currentTab: BottomNavTab,
    viewModel: ScheduleViewModel,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier
) {
    val stateHolder = rememberSaveableStateHolder()

    Crossfade(
        targetState = currentTab,
        modifier = modifier,
        label = "main_tab_transition"
    ) { tab ->
        stateHolder.SaveableStateProvider(
            key = tab.name
        ) {
            when (tab) {
                BottomNavTab.INICIO -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        windowSizeClass = windowSizeClass
                    )
                }

                BottomNavTab.HORARIO -> {
                    TimetableScreen(viewModel = viewModel)
                }

                BottomNavTab.CALENDARIO -> {
                    CalendarScreen(viewModel = viewModel, windowSizeClass = windowSizeClass)
                }

                BottomNavTab.EVENTOS -> {
                    EventsScreen(viewModel = viewModel)
                }

                BottomNavTab.CONFIGURACION -> {
                    SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}