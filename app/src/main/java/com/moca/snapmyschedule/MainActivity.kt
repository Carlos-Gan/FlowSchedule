package com.moca.snapmyschedule

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moca.snapmyschedule.navigation.AppNavHost
import com.moca.snapmyschedule.ui.theme.SnapMyScheduleTheme
import com.moca.snapmyschedule.ui.viewmodel.ScheduleViewModel
import com.moca.snapmyschedule.ui.viewmodel.ScheduleViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SnapMyScheduleTheme {
                val application = application as SnapMyScheduleApplication

                val scheduleViewModel:
                        ScheduleViewModel = viewModel(
                    factory = ScheduleViewModelFactory(
                        repository = application.container.scheduleRepository
                    )
                )
                AppNavHost(
                    scheduleViewModel = scheduleViewModel
                )

            }
        }
    }
}