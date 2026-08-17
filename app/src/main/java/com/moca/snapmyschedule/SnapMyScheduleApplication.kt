package com.moca.snapmyschedule

import android.app.Application
import com.moca.snapmyschedule.di.AppContainer
import com.moca.snapmyschedule.di.DefaultAppContainer

class SnapMyScheduleApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()

        container = DefaultAppContainer(
            context = applicationContext
        )
    }
}