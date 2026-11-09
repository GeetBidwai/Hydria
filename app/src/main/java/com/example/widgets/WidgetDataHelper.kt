package com.example.widgets

import android.content.Context
import com.example.core.constants.AppConstants
import com.example.core.utils.DateTimeUtils
import com.example.data.local.WaterDatabase
import com.example.data.preferences.UserPreferencesDataStore
import kotlinx.coroutines.flow.first
import java.util.Locale

data class WidgetHydrationData(
    val consumedMl: Int,
    val targetMl: Int,
    val preferredUnit: String,
    val widgetQuickAddMl: Int,
    val progressFraction: Float,
    val progressPercent: Int,
    val remainingMl: Int,
    val formattedConsumed: String,
    val formattedTarget: String,
    val formattedRemaining: String,
    val isGoalAchieved: Boolean
)

object WidgetDataHelper {

    suspend fun loadWidgetData(context: Context): WidgetHydrationData {
        val database = WaterDatabase.getDatabase(context)
        val preferences = UserPreferencesDataStore(context)
        val todayKey = DateTimeUtils.getTodayDateKey()

        val consumedMl = database.waterEntryDao().getDailyIntakeMl(todayKey).first()
        val targetMl = preferences.dailyTargetMl.first()
        val preferredUnit = preferences.preferredUnit.first()
        val widgetQuickAddMl = preferences.widgetQuickAddMl.first()

        val safeTarget = targetMl.coerceAtLeast(1)
        val progressFraction = (consumedMl.toFloat() / safeTarget).coerceIn(0f, 1f)
        val progressPercent = ((consumedMl.toFloat() / safeTarget) * 100).toInt()
        val remainingMl = (targetMl - consumedMl).coerceAtLeast(0)
        val isGoalAchieved = consumedMl >= targetMl && targetMl > 0

        return WidgetHydrationData(
            consumedMl = consumedMl,
            targetMl = targetMl,
            preferredUnit = preferredUnit,
            widgetQuickAddMl = widgetQuickAddMl,
            progressFraction = progressFraction,
            progressPercent = progressPercent,
            remainingMl = remainingMl,
            formattedConsumed = formatVolume(consumedMl, preferredUnit),
            formattedTarget = formatVolume(targetMl, preferredUnit),
            formattedRemaining = formatVolume(remainingMl, preferredUnit),
            isGoalAchieved = isGoalAchieved
        )
    }

    fun formatVolume(amountMl: Int, unit: String): String {
        return if (unit == "litres") {
            String.format(Locale.US, "%.2f L", amountMl / 1000f)
        } else {
            String.format(Locale.US, "%,d ml", amountMl)
        }
    }
}
