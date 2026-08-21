package com.example.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import com.example.ui.model.BottomNavTab
import com.example.ui.screens.AgendaScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.EventsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TimetableScreen
import com.example.ui.viewmodel.ScheduleViewModel


@Composable
fun AppTabContent(
    currentTab: BottomNavTab,
    viewModel: ScheduleViewModel,
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
                    AgendaScreen(viewModel = viewModel)
                }

                BottomNavTab.HORARIO -> {
                    TimetableScreen(viewModel = viewModel)
                }

                BottomNavTab.CALENDARIO -> {
                    CalendarScreen(viewModel = viewModel)
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