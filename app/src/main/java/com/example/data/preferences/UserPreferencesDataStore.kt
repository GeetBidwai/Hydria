package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.core.constants.AppConstants
import com.example.domain.gamification.GamificationState
import com.example.domain.gamification.MonthlyCollectionRecord
import com.example.domain.model.ActivityLevel
import com.example.domain.model.Climate
import com.example.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = AppConstants.PREFERENCES_NAME)

class UserPreferencesDataStore(private val context: Context) {

    companion object {
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_USER_AGE = intPreferencesKey("user_age")
        val KEY_USER_WEIGHT_KG = floatPreferencesKey("user_weight_kg")
        val KEY_ACTIVITY_LEVEL = stringPreferencesKey("activity_level")
        val KEY_EXERCISE_MINUTES = intPreferencesKey("exercise_minutes")
        val KEY_MEASUREMENT_UNIT = stringPreferencesKey("measurement_unit") // "ml" or "litres"
        val KEY_DAILY_TARGET_ML = intPreferencesKey("daily_target_ml")
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")

        // Reminder preferences
        val KEY_REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val KEY_WAKE_TIME = stringPreferencesKey("wake_time") // e.g. "08:00"
        val KEY_SLEEP_TIME = stringPreferencesKey("sleep_time") // e.g. "22:00"
        val KEY_REMINDER_START_HOUR = intPreferencesKey("reminder_start_hour")
        val KEY_REMINDER_END_HOUR = intPreferencesKey("reminder_end_hour")
        val KEY_REMINDER_INTERVAL_HOURS = intPreferencesKey("reminder_interval_hours")
        val KEY_REMINDER_INTERVAL_MINUTES = intPreferencesKey("reminder_interval_minutes")
        val KEY_REMINDER_WEEKENDS_ENABLED = booleanPreferencesKey("reminder_weekends_enabled")
        val KEY_WIDGET_QUICK_ADD_ML = intPreferencesKey("widget_quick_add_ml")

        // Gamification preferences
        val KEY_GAMIFICATION_CURRENT_STREAK = intPreferencesKey("gamification_current_streak")
        val KEY_GAMIFICATION_OVERALL_STREAK = intPreferencesKey("gamification_overall_streak")
        val KEY_GAMIFICATION_MISSED_STREAK = intPreferencesKey("gamification_missed_streak")
        val KEY_GAMIFICATION_MONTHLY_STREAK = intPreferencesKey("gamification_monthly_streak")
        val KEY_GAMIFICATION_MONTHLY_HIGHEST_STREAK = intPreferencesKey("gamification_monthly_highest_streak")
        val KEY_GAMIFICATION_TOTAL_SUCCESS_DAYS = intPreferencesKey("gamification_total_success_days")
        val KEY_GAMIFICATION_CURRENT_DESIGNATION = stringPreferencesKey("gamification_current_designation")
        val KEY_GAMIFICATION_HIGHEST_DESIGNATION = stringPreferencesKey("gamification_highest_designation")
        val KEY_GAMIFICATION_LAST_EVAL_DATE = stringPreferencesKey("gamification_last_eval_date")
        val KEY_GAMIFICATION_UNLOCKED_MILESTONES = stringPreferencesKey("gamification_unlocked_milestones")
        val KEY_GAMIFICATION_CURRENT_MONTH_KEY = stringPreferencesKey("gamification_current_month_key")
        val KEY_GAMIFICATION_CURRENT_MONTH_BADGES_JSON = stringPreferencesKey("gamification_current_month_badges_json")
        val KEY_GAMIFICATION_MONTHLY_COLLECTIONS_JSON = stringPreferencesKey("gamification_monthly_collections_json")
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_COMPLETED] ?: false
    }

    val dailyTargetMl: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_DAILY_TARGET_ML] ?: AppConstants.DEFAULT_DAILY_TARGET_ML
    }

    val preferredUnit: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_MEASUREMENT_UNIT] ?: "ml"
    }

    val remindersEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_REMINDERS_ENABLED] ?: true
    }

    val reminderIntervalMinutes: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_REMINDER_INTERVAL_MINUTES] ?: 60
    }

    val reminderWeekendsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_REMINDER_WEEKENDS_ENABLED] ?: true
    }

    val wakeTime: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_WAKE_TIME] ?: "08:00"
    }

    val sleepTime: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_SLEEP_TIME] ?: "22:00"
    }

    val widgetQuickAddMl: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_WIDGET_QUICK_ADD_ML] ?: AppConstants.DEFAULT_WIDGET_QUICK_ADD_ML
    }

    val userProfile: Flow<UserProfile> = context.dataStore.data.map { prefs ->
        UserProfile(
            name = prefs[KEY_USER_NAME] ?: "",
            age = prefs[KEY_USER_AGE] ?: 28,
            weightKg = prefs[KEY_USER_WEIGHT_KG] ?: 70f,
            activityLevel = ActivityLevel.fromString(prefs[KEY_ACTIVITY_LEVEL]),
            exerciseMinutes = prefs[KEY_EXERCISE_MINUTES] ?: 30,
            preferredUnit = prefs[KEY_MEASUREMENT_UNIT] ?: "ml",
            remindersEnabled = prefs[KEY_REMINDERS_ENABLED] ?: true,
            wakeTime = prefs[KEY_WAKE_TIME] ?: "07:00",
            sleepTime = prefs[KEY_SLEEP_TIME] ?: "23:00",
            climate = Climate.TEMPERATE,
            reminderIntervalMinutes = prefs[KEY_REMINDER_INTERVAL_MINUTES] ?: 60,
            reminderWeekendsEnabled = prefs[KEY_REMINDER_WEEKENDS_ENABLED] ?: true
        )
    }

    suspend fun saveProfile(profile: UserProfile, calculatedTargetMl: Int? = null) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_NAME] = profile.name
            prefs[KEY_USER_AGE] = profile.age
            prefs[KEY_USER_WEIGHT_KG] = profile.weightKg
            prefs[KEY_ACTIVITY_LEVEL] = profile.activityLevel.name
            prefs[KEY_EXERCISE_MINUTES] = profile.exerciseMinutes
            prefs[KEY_MEASUREMENT_UNIT] = profile.preferredUnit
            prefs[KEY_REMINDERS_ENABLED] = profile.remindersEnabled
            prefs[KEY_WAKE_TIME] = profile.wakeTime
            prefs[KEY_SLEEP_TIME] = profile.sleepTime
            prefs[KEY_REMINDER_INTERVAL_MINUTES] = profile.reminderIntervalMinutes
            prefs[KEY_REMINDER_WEEKENDS_ENABLED] = profile.reminderWeekendsEnabled
            if (calculatedTargetMl != null && calculatedTargetMl > 0) {
                prefs[KEY_DAILY_TARGET_ML] = calculatedTargetMl
            }
        }
    }

    suspend fun setDailyTargetMl(targetMl: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DAILY_TARGET_ML] = targetMl
        }
    }

    suspend fun setPreferredUnit(unit: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_MEASUREMENT_UNIT] = unit
        }
    }

    suspend fun setRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_REMINDERS_ENABLED] = enabled
        }
    }

    suspend fun setReminderIntervalMinutes(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_REMINDER_INTERVAL_MINUTES] = minutes
        }
    }

    suspend fun setReminderWeekendsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_REMINDER_WEEKENDS_ENABLED] = enabled
        }
    }

    suspend fun setWakingHours(wakeTime: String, sleepTime: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WAKE_TIME] = wakeTime
            prefs[KEY_SLEEP_TIME] = sleepTime
        }
    }

    suspend fun setWidgetQuickAddMl(amountMl: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WIDGET_QUICK_ADD_ML] = amountMl
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    val gamificationHighestDesignation: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_GAMIFICATION_HIGHEST_DESIGNATION] ?: "Wave Seeker"
    }.distinctUntilChanged()

    val gamificationUnlockedMilestoneIds: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_GAMIFICATION_UNLOCKED_MILESTONES] ?: ""
        if (raw.isBlank()) emptySet() else raw.split(",").toSet()
    }.distinctUntilChanged()

    val gamificationMonthlyCollections: Flow<List<MonthlyCollectionRecord>> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_GAMIFICATION_MONTHLY_COLLECTIONS_JSON]
        MonthlyCollectionRecord.listFromJsonString(raw)
    }.distinctUntilChanged()

    suspend fun saveGamificationV2Snapshot(state: GamificationState) {
        context.dataStore.edit { prefs ->
            val currentJson = prefs[KEY_GAMIFICATION_MONTHLY_COLLECTIONS_JSON] ?: ""
            val newJson = MonthlyCollectionRecord.listToJsonString(state.completedMonthlyCollections)
            val currentHighest = prefs[KEY_GAMIFICATION_HIGHEST_DESIGNATION] ?: ""
            val currentStreak = prefs[KEY_GAMIFICATION_OVERALL_STREAK] ?: 0
            val currentMissed = prefs[KEY_GAMIFICATION_MISSED_STREAK] ?: 0
            val currentMonthStreak = prefs[KEY_GAMIFICATION_MONTHLY_STREAK] ?: 0
            val currentMonthHighest = prefs[KEY_GAMIFICATION_MONTHLY_HIGHEST_STREAK] ?: 0
            val currentTotalSuccess = prefs[KEY_GAMIFICATION_TOTAL_SUCCESS_DAYS] ?: 0
            val currentDesig = prefs[KEY_GAMIFICATION_CURRENT_DESIGNATION] ?: ""
            val currentLastEval = prefs[KEY_GAMIFICATION_LAST_EVAL_DATE] ?: ""
            val currentMonthKey = prefs[KEY_GAMIFICATION_CURRENT_MONTH_KEY] ?: ""
            val currentBadges = prefs[KEY_GAMIFICATION_CURRENT_MONTH_BADGES_JSON] ?: ""
            val newBadges = state.currentMonthUnlockedBadgeIds.joinToString(",")

            // Skip writing if nothing changed to prevent disk writes and redundant DataStore emissions
            if (currentStreak == state.overallStreak &&
                currentMissed == state.currentMissedStreak &&
                currentMonthStreak == state.monthlyStreak &&
                currentMonthHighest == state.monthlyHighestStreak &&
                currentTotalSuccess == state.totalSuccessfulDays &&
                currentDesig == state.currentDesignationTitle &&
                currentHighest == state.lifetimeHighestDesignation &&
                currentLastEval == state.lastEvaluatedDateKey &&
                currentMonthKey == state.currentMonthKey &&
                currentBadges == newBadges &&
                currentJson == newJson
            ) {
                return@edit
            }

            prefs[KEY_GAMIFICATION_OVERALL_STREAK] = state.overallStreak
            prefs[KEY_GAMIFICATION_CURRENT_STREAK] = state.overallStreak
            prefs[KEY_GAMIFICATION_MISSED_STREAK] = state.currentMissedStreak
            prefs[KEY_GAMIFICATION_MONTHLY_STREAK] = state.monthlyStreak
            prefs[KEY_GAMIFICATION_MONTHLY_HIGHEST_STREAK] = state.monthlyHighestStreak
            prefs[KEY_GAMIFICATION_TOTAL_SUCCESS_DAYS] = state.totalSuccessfulDays
            prefs[KEY_GAMIFICATION_CURRENT_DESIGNATION] = state.currentDesignationTitle
            prefs[KEY_GAMIFICATION_HIGHEST_DESIGNATION] = state.lifetimeHighestDesignation
            prefs[KEY_GAMIFICATION_LAST_EVAL_DATE] = state.lastEvaluatedDateKey
            prefs[KEY_GAMIFICATION_CURRENT_MONTH_KEY] = state.currentMonthKey
            prefs[KEY_GAMIFICATION_CURRENT_MONTH_BADGES_JSON] = newBadges
            prefs[KEY_GAMIFICATION_MONTHLY_COLLECTIONS_JSON] = newJson
        }
    }

    suspend fun clearProfileOnly() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_USER_NAME)
            prefs.remove(KEY_USER_AGE)
            prefs.remove(KEY_USER_WEIGHT_KG)
            prefs.remove(KEY_ACTIVITY_LEVEL)
            prefs.remove(KEY_EXERCISE_MINUTES)
            prefs.remove(KEY_ONBOARDING_COMPLETED)
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
