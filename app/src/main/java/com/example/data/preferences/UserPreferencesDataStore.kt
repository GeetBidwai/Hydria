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
import com.example.domain.model.ActivityLevel
import com.example.domain.model.Climate
import com.example.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
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

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_COMPLETED] = completed
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
