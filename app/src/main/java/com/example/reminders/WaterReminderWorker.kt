package com.example.reminders

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.core.utils.DateTimeUtils
import com.example.data.local.WaterDatabase
import com.example.data.preferences.UserPreferencesDataStore
import kotlinx.coroutines.flow.first
import java.util.Calendar

class WaterReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val preferences = UserPreferencesDataStore(context)

        // 1. Check if reminders are enabled
        val remindersEnabled = preferences.remindersEnabled.first()
        if (!remindersEnabled) {
            ReminderScheduler.cancelReminders(context)
            return Result.success()
        }

        // 2. Check weekend preference
        val now = Calendar.getInstance()
        val isWeekend = ReminderHelper.isWeekend(now)
        val weekendsEnabled = preferences.reminderWeekendsEnabled.first()
        if (!weekendsEnabled && isWeekend) {
            return Result.success()
        }

        // 3. Check waking hours
        val wakeTime = preferences.wakeTime.first()
        val sleepTime = preferences.sleepTime.first()
        val isWithinWaking = ReminderHelper.isWithinWakingHours(now, wakeTime, sleepTime)
        if (!isWithinWaking) {
            return Result.success()
        }

        // 4. Check if daily target is already reached
        val database = WaterDatabase.getDatabase(context)
        val todayKey = DateTimeUtils.getTodayDateKey()
        val todayIntake = database.waterEntryDao().getDailyIntakeMl(todayKey).first()
        val dailyTarget = preferences.dailyTargetMl.first()

        if (ReminderHelper.isDailyGoalReached(todayIntake, dailyTarget)) {
            return Result.success()
        }

        // 4b. Check recent water intake (Intake-based suppression)
        val intervalMinutes = preferences.reminderIntervalMinutes.first()
        val minIntervalMinutes = ReminderScheduler.getMinIntervalMinutes(intervalMinutes)
        val lastIntakePref = preferences.lastIntakeTimestamp.first()
        val latestDbIntake = try { database.waterEntryDao().getLatestEntryTimestamp() ?: 0L } catch (_: Exception) { 0L }
        val effectiveLastIntake = maxOf(lastIntakePref, latestDbIntake)

        if (effectiveLastIntake > 0) {
            val elapsedMillis = System.currentTimeMillis() - effectiveLastIntake
            val minIntervalMillis = minIntervalMinutes * 60 * 1000L
            if (elapsedMillis < minIntervalMillis) {
                // Water was consumed recently; suppress notification and reschedule for remaining delay
                val remainingMillis = minIntervalMillis - elapsedMillis
                val remainingMinutes = ((remainingMillis / (60 * 1000L)).toInt()).coerceAtLeast(15)
                ReminderScheduler.scheduleReminders(context, intervalMinutes, remainingMinutes)
                return Result.success()
            }
        }

        // 5. Evaluate historical context for yesterday's missed goal
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val wakeMinutes = ReminderHelper.parseTimeToMinutes(wakeTime, 8 * 60)
        val isMorningWindow = (currentMinutes - wakeMinutes) in 0..150

        val yesterdayCal = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
        val yesterdayKey = DateTimeUtils.formatDateKey(yesterdayCal.timeInMillis)
        val yesterdayIntake = database.waterEntryDao().getDailyIntakeMl(yesterdayKey).first()

        val allEntries = database.waterEntryDao().getAllEntries().first()
        val hasRecordedHistory = allEntries.any { it.dateKey <= yesterdayKey }
        val isYesterdayMissed = hasRecordedHistory && yesterdayIntake < dailyTarget

        val isMissedGoalNotified = preferences.isMissedGoalNotifiedToday(todayKey)
        val isEndOfDayNotified = preferences.isEndOfDayNotifiedToday(todayKey)

        // 6. Determine context-aware periodic notification category
        val category = OceanNotificationEvaluator.determinePeriodicNotificationCategory(
            remindersEnabled = remindersEnabled,
            isWeekend = isWeekend,
            weekendsEnabled = weekendsEnabled,
            isWithinWakingHours = isWithinWaking,
            todayIntakeMl = todayIntake,
            dailyTargetMl = dailyTarget,
            currentMinutes = currentMinutes,
            sleepTimeString = sleepTime,
            isYesterdayMissed = isYesterdayMissed,
            isMissedGoalNotifiedToday = isMissedGoalNotified,
            isEndOfDayNotifiedToday = isEndOfDayNotified,
            isMorningWindow = isMorningWindow
        ) ?: return Result.success()

        // 7. Select message with rotation to avoid immediate repetition
        val lastShownTitle = preferences.getLastShownNotificationTitle(category.name)
        val specificPool = if (category == NotificationCategory.MISSED_GOAL_AND_RECOVERY) {
            OceanNotificationContent.MISSED_GOALS
        } else null

        val message = OceanNotificationContent.selectMessage(
            category = category,
            lastShownTitle = lastShownTitle,
            specificPool = specificPool
        )

        // 8. Dispatch notification via ReminderHelper
        val posted = ReminderHelper.showOceanNotification(context, message)
        if (posted) {
            preferences.setLastShownNotificationTitle(category.name, message.title)
            if (category == NotificationCategory.MISSED_GOAL_AND_RECOVERY) {
                preferences.setMissedGoalNotifiedToday(todayKey)
            } else if (category == NotificationCategory.END_OF_DAY_NUDGE) {
                preferences.setEndOfDayNotifiedToday(todayKey)
            }
        }

        // Reschedule next reminder with natural timing variation
        val nextDelay = ReminderScheduler.calculateEffectiveDelayMinutes(intervalMinutes)
        ReminderScheduler.scheduleReminders(context, intervalMinutes, nextDelay)

        return Result.success()
    }
}
