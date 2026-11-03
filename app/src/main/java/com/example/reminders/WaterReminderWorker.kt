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
        val weekendsEnabled = preferences.reminderWeekendsEnabled.first()
        if (!weekendsEnabled && ReminderHelper.isWeekend(Calendar.getInstance())) {
            return Result.success()
        }

        // 3. Check waking hours
        val wakeTime = preferences.wakeTime.first()
        val sleepTime = preferences.sleepTime.first()
        if (!ReminderHelper.isWithinWakingHours(Calendar.getInstance(), wakeTime, sleepTime)) {
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

        // 5. Post notification
        val preferredUnit = preferences.preferredUnit.first()
        ReminderHelper.showHydrationNotification(
            context = context,
            consumedMl = todayIntake,
            targetMl = dailyTarget,
            preferredUnit = preferredUnit
        )

        return Result.success()
    }
}
