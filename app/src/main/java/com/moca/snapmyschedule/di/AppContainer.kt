package com.moca.snapmyschedule.di

import android.content.Context
import com.moca.snapmyschedule.data.local.AppDatabase
import com.moca.snapmyschedule.data.repository.OfflineScheduleRepository
import com.moca.snapmyschedule.data.repository.ScheduleRepository

/**
 * Contiene las dependencias compartidas por toda la aplicación.
 */
interface AppContainer {
    val scheduleRepository: ScheduleRepository
}

class DefaultAppContainer(
    context: Context
) : AppContainer {
    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    override val scheduleRepository: ScheduleRepository by lazy {
        OfflineScheduleRepository(
            classSessionDao = database.classSessionDao()
        )
    }
}