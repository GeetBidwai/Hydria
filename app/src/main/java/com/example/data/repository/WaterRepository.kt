package com.example.data.repository

import android.content.Context
import com.example.data.local.DateTotal
import com.example.data.local.WaterEntry
import com.example.data.local.WaterEntryDao
import com.example.data.preferences.UserPreferencesDataStore
import com.example.domain.model.UserProfile
import com.example.widgets.WidgetRefreshHelper
import kotlinx.coroutines.flow.Flow

class WaterRepository(
    private val waterEntryDao: WaterEntryDao,
    private val preferencesDataStore: UserPreferencesDataStore,
    private val context: Context? = null
) {
    fun getEntriesForDate(dateKey: String): Flow<List<WaterEntry>> =
        waterEntryDao.getEntriesByDate(dateKey)

    fun getDailyIntakeMl(dateKey: String): Flow<Int> =
        waterEntryDao.getDailyIntakeMl(dateKey)

    fun getAllEntries(): Flow<List<WaterEntry>> =
        waterEntryDao.getAllEntries()

    fun getDailyTotals(): Flow<List<DateTotal>> =
        waterEntryDao.getDailyTotals()

    val dailyTargetMl: Flow<Int> = preferencesDataStore.dailyTargetMl

    val isOnboardingCompleted: Flow<Boolean> = preferencesDataStore.isOnboardingCompleted

    val userProfile: Flow<UserProfile> = preferencesDataStore.userProfile

    val preferredUnit: Flow<String> = preferencesDataStore.preferredUnit

    val remindersEnabled: Flow<Boolean> = preferencesDataStore.remindersEnabled

    val reminderIntervalMinutes: Flow<Int> = preferencesDataStore.reminderIntervalMinutes

    val reminderWeekendsEnabled: Flow<Boolean> = preferencesDataStore.reminderWeekendsEnabled

    val wakeTime: Flow<String> = preferencesDataStore.wakeTime

    val sleepTime: Flow<String> = preferencesDataStore.sleepTime

    val widgetQuickAddMl: Flow<Int> = preferencesDataStore.widgetQuickAddMl

    private suspend fun triggerWidgetRefresh() {
        context?.let { WidgetRefreshHelper.refreshAllWidgets(it) }
    }

    suspend fun logWater(amountMl: Int, dateKey: String): Long {
        val entry = WaterEntry(
            amountMl = amountMl,
            timestamp = System.currentTimeMillis(),
            dateKey = dateKey
        )
        val id = waterEntryDao.insertEntry(entry)
        triggerWidgetRefresh()
        return id
    }

    suspend fun insertEntry(entry: WaterEntry): Long {
        val id = waterEntryDao.insertEntry(entry)
        triggerWidgetRefresh()
        return id
    }

    suspend fun deleteEntry(id: Long) {
        waterEntryDao.deleteEntryById(id)
        triggerWidgetRefresh()
    }

    suspend fun saveProfile(profile: UserProfile, calculatedTargetMl: Int? = null) {
        preferencesDataStore.saveProfile(profile, calculatedTargetMl)
        triggerWidgetRefresh()
    }

    suspend fun setDailyTargetMl(targetMl: Int) {
        preferencesDataStore.setDailyTargetMl(targetMl)
        triggerWidgetRefresh()
    }

    suspend fun setPreferredUnit(unit: String) {
        preferencesDataStore.setPreferredUnit(unit)
        triggerWidgetRefresh()
    }

    suspend fun setRemindersEnabled(enabled: Boolean) {
        preferencesDataStore.setRemindersEnabled(enabled)
    }

    suspend fun setReminderIntervalMinutes(minutes: Int) {
        preferencesDataStore.setReminderIntervalMinutes(minutes)
    }

    suspend fun setReminderWeekendsEnabled(enabled: Boolean) {
        preferencesDataStore.setReminderWeekendsEnabled(enabled)
    }

    suspend fun setWakingHours(wakeTime: String, sleepTime: String) {
        preferencesDataStore.setWakingHours(wakeTime, sleepTime)
    }

    suspend fun setWidgetQuickAddMl(amountMl: Int) {
        preferencesDataStore.setWidgetQuickAddMl(amountMl)
        triggerWidgetRefresh()
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        preferencesDataStore.setOnboardingCompleted(completed)
    }

    suspend fun resetProfileData() {
        // Clear profile preferences without clearing historical water logs
        preferencesDataStore.clearProfileOnly()
        triggerWidgetRefresh()
    }

    suspend fun clearHistory() {
        waterEntryDao.clearAll()
        triggerWidgetRefresh()
    }

    suspend fun clearAllData() {
        waterEntryDao.clearAll()
        preferencesDataStore.clearAll()
        triggerWidgetRefresh()
    }
}
