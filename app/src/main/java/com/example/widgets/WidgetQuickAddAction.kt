package com.example.widgets

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.example.core.constants.AppConstants
import com.example.core.utils.DateTimeUtils
import com.example.data.local.WaterDatabase
import com.example.data.local.WaterEntry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class WidgetQuickAddAction : ActionCallback {

    companion object {
        val KEY_AMOUNT_ML = ActionParameters.Key<Int>("quick_add_amount_ml")
        private val mutex = Mutex()
        private var lastLogTimestamp = 0L
    }

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val amountMl = parameters[KEY_AMOUNT_ML] ?: AppConstants.DEFAULT_WIDGET_QUICK_ADD_ML
        if (amountMl <= 0) return

        val now = System.currentTimeMillis()

        // Debounce fast repeated taps (within 350ms) to prevent accidental duplicate entries
        val shouldLog = mutex.withLock {
            if (now - lastLogTimestamp < 350L) {
                false
            } else {
                lastLogTimestamp = now
                true
            }
        }

        if (!shouldLog) return

        val database = WaterDatabase.getDatabase(context)
        val todayKey = DateTimeUtils.getTodayDateKey()
        val entry = WaterEntry(
            amountMl = amountMl,
            timestamp = now,
            dateKey = todayKey
        )
        database.waterEntryDao().insertEntry(entry)

        // Record intake timestamp and reset reminder schedule
        val preferences = com.example.data.preferences.UserPreferencesDataStore(context)
        preferences.setLastIntakeTimestamp(now)
        try {
            val enabled = preferences.remindersEnabled.first()
            if (enabled) {
                val interval = preferences.reminderIntervalMinutes.first()
                com.example.reminders.ReminderScheduler.rescheduleAfterIntake(context, interval)
            }
        } catch (_: Exception) {}

        // Immediately update both widgets with the fresh database records
        WidgetRefreshHelper.refreshAllWidgets(context)
    }
}
