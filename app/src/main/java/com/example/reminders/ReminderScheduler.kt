package com.example.reminders

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.core.constants.AppConstants
import java.util.concurrent.TimeUnit
import kotlin.random.Random

object ReminderScheduler {
    val AVAILABLE_INTERVALS = listOf(30, 60, 90, 120)
    const val DEFAULT_INTERVAL_MINUTES = 120

    /**
     * Calculates the minimum required elapsed minutes before a reminder can fire.
     * - For 120 min (default): 100 minutes.
     * - For 90 min: 75 minutes.
     * - For 60 min: 50 minutes.
     * - For 30 min: 25 minutes.
     */
    fun getMinIntervalMinutes(intervalMinutes: Int): Int {
        return when (intervalMinutes) {
            120 -> 100
            90 -> 75
            60 -> 50
            30 -> 25
            else -> (intervalMinutes * 0.83f).toInt().coerceAtLeast(15)
        }
    }

    /**
     * Calculates an effective randomized delay within the natural timing variation window:
     * - For 120 min: randomized between 100 and 120 minutes (inclusive).
     * - For 90 min: randomized between 75 and 90 minutes.
     * - For 60 min: randomized between 50 and 60 minutes.
     * - For 30 min: randomized between 25 and 30 minutes.
     */
    fun calculateEffectiveDelayMinutes(intervalMinutes: Int): Int {
        val minMinutes = getMinIntervalMinutes(intervalMinutes)
        val maxMinutes = intervalMinutes.coerceAtLeast(minMinutes)
        return if (minMinutes < maxMinutes) {
            Random.nextInt(minMinutes, maxMinutes + 1)
        } else {
            maxMinutes
        }
    }

    /**
     * Enqueues or updates periodic hydration reminder background work.
     * WorkManager guarantees periodic execution with a minimum interval of 15 minutes.
     * If [delayMinutes] is provided, sets it as initial delay.
     * Otherwise, calculates a natural variation initial delay.
     */
    fun scheduleReminders(
        context: Context,
        intervalMinutes: Int = DEFAULT_INTERVAL_MINUTES,
        delayMinutes: Int? = null
    ) {
        val safeIntervalMinutes = intervalMinutes.coerceAtLeast(15)
        val effectiveDelay = (delayMinutes ?: calculateEffectiveDelayMinutes(safeIntervalMinutes)).coerceAtLeast(1)

        val workRequest = PeriodicWorkRequestBuilder<WaterReminderWorker>(
            safeIntervalMinutes.toLong(),
            TimeUnit.MINUTES
        )
            .setInitialDelay(effectiveDelay.toLong(), TimeUnit.MINUTES)
            .addTag(AppConstants.REMINDER_WORK_TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            AppConstants.REMINDER_WORK_TAG,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            workRequest
        )
    }

    /**
     * Reschedules the reminder schedule after the user logs water.
     * The next reminder will fire after a natural variation delay (e.g. 100–120 minutes for 120m).
     */
    fun rescheduleAfterIntake(context: Context, intervalMinutes: Int = DEFAULT_INTERVAL_MINUTES) {
        val nextDelay = calculateEffectiveDelayMinutes(intervalMinutes)
        scheduleReminders(context, intervalMinutes, nextDelay)
    }

    /**
     * Cancels any scheduled hydration reminder periodic work.
     */
    fun cancelReminders(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(AppConstants.REMINDER_WORK_TAG)
    }
}
