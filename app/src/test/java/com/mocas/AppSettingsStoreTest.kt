package com.mocas

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.mocas.data.preferences.AppSettingsStore
import com.mocas.ui.model.AppSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AppSettingsStoreTest {
    private lateinit var context: Context

    @Before
    fun clearPreferences() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("snap_my_schedule_settings", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun `onboarding is pending on first launch`() {
        val settings = AppSettingsStore(context).load()

        assertFalse(settings.onboardingCompleted)
        assertEquals("Estudiante", settings.userName)
    }

    @Test
    fun `completed onboarding and preferences survive a new store instance`() {
        AppSettingsStore(context).save(
            AppSettings(
                userName = "Charles",
                themeMode = "DARK",
                notificationsEnabled = false,
                classNotificationsEnabled = false,
                taskNotificationsEnabled = true,
                eventNotificationsEnabled = false,
                taskReminderMinutes = 3 * 24 * 60,
                eventReminderMinutes = 12 * 60,
                calendarSyncEnabled = false,
                aiFeaturesEnabled = false,
                outsidePeriodsAreVacations = true,
                showVacationsInTimetable = false,
                onboardingCompleted = true
            )
        )

        val restored = AppSettingsStore(context).load()

        assertEquals("Charles", restored.userName)
        assertEquals("DARK", restored.themeMode)
        assertFalse(restored.notificationsEnabled)
        assertFalse(restored.classNotificationsEnabled)
        assertTrue(restored.taskNotificationsEnabled)
        assertFalse(restored.eventNotificationsEnabled)
        assertEquals(3 * 24 * 60, restored.taskReminderMinutes)
        assertEquals(12 * 60, restored.eventReminderMinutes)
        assertFalse(restored.calendarSyncEnabled)
        assertFalse(restored.aiFeaturesEnabled)
        assertTrue(restored.outsidePeriodsAreVacations)
        assertFalse(restored.showVacationsInTimetable)
        assertTrue(restored.onboardingCompleted)
    }
}
