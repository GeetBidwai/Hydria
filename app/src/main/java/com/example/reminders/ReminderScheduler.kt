package com.example.reminders

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.core.constants.AppConstants
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    val AVAILABLE_INTERVALS = listOf(30, 60, 90, 120)

    /**
     * Enqueues or updates periodic hydration reminder background work.
     * WorkManager guarantees periodic execution with a minimum interval of 15 minutes.
     */
    fun scheduleReminders(context: Context, intervalMinutes: Int) {
        val safeIntervalMinutes = intervalMinutes.coerceAtLeast(15)

        val workRequest = PeriodicWorkRequestBuilder<WaterReminderWorker>(
            safeIntervalMinutes.toLong(),
            TimeUnit.MINUTES
        )
            .addTag(AppConstants.REMINDER_WORK_TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            AppConstants.REMINDER_WORK_TAG,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    /**
     * Cancels any scheduled hydration reminder periodic work.
     */
    fun cancelReminders(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(AppConstants.REMINDER_WORK_TAG)
    }
}
