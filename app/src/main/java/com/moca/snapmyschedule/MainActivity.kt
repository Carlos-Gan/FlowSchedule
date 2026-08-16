package com.moca.snapmyschedule

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.moca.snapmyschedule.navigation.AppNavHost
import com.moca.snapmyschedule.ui.theme.SnapMyScheduleTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SnapMyScheduleTheme {
                AppNavHost()
            }
        }
    }
}