package com.example.data.repository

import com.example.data.local.DateTotal
import com.example.data.local.WaterEntry
import com.example.data.local.WaterEntryDao
import com.example.data.preferences.UserPreferencesDataStore
import com.example.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

class WaterRepository(
    private val waterEntryDao: WaterEntryDao,
    private val preferencesDataStore: UserPreferencesDataStore
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

    suspend fun logWater(amountMl: Int, dateKey: String): Long {
        val entry = WaterEntry(
            amountMl = amountMl,
            timestamp = System.currentTimeMillis(),
            dateKey = dateKey
        )
        return waterEntryDao.insertEntry(entry)
    }

    suspend fun insertEntry(entry: WaterEntry): Long {
        return waterEntryDao.insertEntry(entry)
    }

    suspend fun deleteEntry(id: Long) {
        waterEntryDao.deleteEntryById(id)
    }

    suspend fun saveProfile(profile: UserProfile, calculatedTargetMl: Int? = null) {
        preferencesDataStore.saveProfile(profile, calculatedTargetMl)
    }

    suspend fun setDailyTargetMl(targetMl: Int) {
        preferencesDataStore.setDailyTargetMl(targetMl)
    }

    suspend fun setPreferredUnit(unit: String) {
        preferencesDataStore.setPreferredUnit(unit)
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

    suspend fun setOnboardingCompleted(completed: Boolean) {
        preferencesDataStore.setOnboardingCompleted(completed)
    }

    suspend fun resetProfileData() {
        // Clear profile preferences without clearing historical water logs
        preferencesDataStore.clearProfileOnly()
    }

    suspend fun clearHistory() {
        waterEntryDao.clearAll()
    }

    suspend fun clearAllData() {
        waterEntryDao.clearAll()
        preferencesDataStore.clearAll()
    }
}
