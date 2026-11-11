package com.mocas.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mocas.R
import com.mocas.data.local.AppDatabase
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class MotivationalNotificationWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = try {
        val context = applicationContext
        val database = AppDatabase.getDatabase(context)
        val periods = database.academicPeriodDao().getAllPeriodsOnce()

        val today = LocalDate.now()
        val month = today.monthValue
        val day = today.dayOfMonth

        // 1. Check holidays
        val holidayMessage = when {
            month == 1 && day == 1 -> context.getString(R.string.festivo_ano_nuevo)
            month == 2 && day == 14 -> context.getString(R.string.festivo_san_valentin)
            month == 10 && day == 31 -> context.getString(R.string.festivo_halloween)
            month == 11 && (day == 1 || day == 2) -> context.getString(R.string.festivo_dia_muertos)
            month == 12 && day == 25 -> context.getString(R.string.festivo_navidad)
            month == 12 && day == 31 -> context.getString(R.string.festivo_fin_ano)
            else -> null
        }

        if (holidayMessage != null) {
            showNotification(
                context,
                context.getString(R.string.notif_holiday_title),
                holidayMessage,
                1001
            )
            return Result.success()
        }

        // 2. Check semester ending countdown (10, 5, 3, 2, 1 days remaining)
        val activePeriod = periods.firstOrNull { period ->
            val start = runCatching { LocalDate.parse(period.startDate) }.getOrNull()
            val end = runCatching { LocalDate.parse(period.endDate) }.getOrNull()
            start != null && end != null && today in start..end
        }

        if (activePeriod != null) {
            val endDate = runCatching { LocalDate.parse(activePeriod.endDate) }.getOrNull()
            if (endDate != null) {
                val remainingDays = ChronoUnit.DAYS.between(today, endDate).toInt()
                if (remainingDays in listOf(1, 2, 3, 5, 10)) {
                    val message = context.getString(R.string.notif_semester_ending_message, remainingDays)
                    showNotification(
                        context,
                        context.getString(R.string.notif_semester_ending_title),
                        message,
                        1002
                    )
                }
            }
        }

        Result.success()
    } catch (_: Exception) {
        Result.retry()
    }

    private fun showNotification(context: Context, title: String, message: String, notificationId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "motivation_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                context.getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}
