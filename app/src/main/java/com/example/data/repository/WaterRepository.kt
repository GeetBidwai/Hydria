package com.example.data.repository

import android.content.Context
import com.example.core.utils.DateTimeUtils
import com.example.data.local.DateTotal
import com.example.data.local.WaterEntry
import com.example.data.local.WaterEntryDao
import com.example.data.preferences.UserPreferencesDataStore
import com.example.domain.gamification.GamificationEngine
import com.example.domain.gamification.GamificationState
import com.example.domain.model.UserProfile
import com.example.reminders.OceanNotificationContent
import com.example.reminders.OceanNotificationEvaluator
import com.example.reminders.ReminderHelper
import com.example.reminders.ReminderScheduler
import com.example.widgets.WidgetRefreshHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach

class WaterRepository(
    private val waterEntryDao: WaterEntryDao,
    private val preferencesDataStore: UserPreferencesDataStore,
    private val context: Context? = null
) {
    private var lastPersistedSnapshot: GamificationState? = null
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

    val lastIntakeTimestamp: Flow<Long> = preferencesDataStore.lastIntakeTimestamp

    suspend fun setLastIntakeTimestamp(timestamp: Long) {
        preferencesDataStore.setLastIntakeTimestamp(timestamp)
    }

    private suspend fun triggerIntakeReminderReset(latestIntakeTimestamp: Long) {
        if (context == null) return
        try {
            val enabled = preferencesDataStore.remindersEnabled.first()
            if (enabled) {
                val interval = preferencesDataStore.reminderIntervalMinutes.first()
                ReminderScheduler.rescheduleWithLatestIntake(context, interval, latestIntakeTimestamp)
            }
        } catch (_: Exception) {}
    }

    val gamificationState: Flow<GamificationState> = combine(
        getDailyTotals(),
        dailyTargetMl,
        preferencesDataStore.gamificationHighestDesignation,
        preferencesDataStore.gamificationMonthlyCollections
    ) { totals, target, highestDesignation, collections ->
        val totalsMap = totals.associate { it.dateKey to it.totalMl }
        val todayKey = DateTimeUtils.getTodayDateKey()
        val todayConsumed = totalsMap[todayKey] ?: 0

        GamificationEngine.evaluate(
            dailyTotals = totalsMap,
            dailyTargetMl = target,
            todayDateKey = todayKey,
            todayConsumedMl = todayConsumed,
            persistedCollections = collections,
            persistedHighestDesignation = highestDesignation
        )
    }.distinctUntilChanged().onEach { evaluated ->
        if (evaluated != lastPersistedSnapshot) {
            lastPersistedSnapshot = evaluated
            try {
                preferencesDataStore.saveGamificationV2Snapshot(evaluated)
            } catch (_: Exception) {}
        }
    }

    private suspend fun triggerWidgetRefresh() {
        context?.let { WidgetRefreshHelper.refreshAllWidgets(it) }
    }

    suspend fun logWater(amountMl: Int, dateKey: String): Long {
        val now = System.currentTimeMillis()
        val prevIntake = try { waterEntryDao.getDailyIntakeMl(dateKey).first() } catch (_: Exception) { 0 }
        val entry = WaterEntry(
            amountMl = amountMl,
            timestamp = now,
            dateKey = dateKey
        )
        val id = waterEntryDao.insertEntry(entry)
        val newIntake = prevIntake + amountMl
        val latestTimestamp = waterEntryDao.getLatestEntryTimestamp() ?: now
        preferencesDataStore.setLastIntakeTimestamp(latestTimestamp)
        triggerWidgetRefresh()
        triggerIntakeReminderReset(latestTimestamp)
        checkAndPostIntakeCelebration(dateKey, prevIntake, newIntake)
        return id
    }

    suspend fun insertEntry(entry: WaterEntry): Long {
        val prevIntake = try { waterEntryDao.getDailyIntakeMl(entry.dateKey).first() } catch (_: Exception) { 0 }
        val id = waterEntryDao.insertEntry(entry)
        val newIntake = prevIntake + entry.amountMl
        val latestTimestamp = waterEntryDao.getLatestEntryTimestamp() ?: entry.timestamp
        preferencesDataStore.setLastIntakeTimestamp(latestTimestamp)
        triggerWidgetRefresh()
        triggerIntakeReminderReset(latestTimestamp)
        checkAndPostIntakeCelebration(entry.dateKey, prevIntake, newIntake)
        return id
    }

    private suspend fun checkAndPostIntakeCelebration(dateKey: String, previousIntake: Int, newIntake: Int) {
        val todayKey = DateTimeUtils.getTodayDateKey()
        if (dateKey != todayKey || context == null) return

        try {
            val remindersEnabled = preferencesDataStore.remindersEnabled.first()
            if (!remindersEnabled) return

            val dailyTarget = preferencesDataStore.dailyTargetMl.first()
            val alreadyTriggered = preferencesDataStore.getTriggeredMilestonesToday(todayKey)
            val previousState = lastPersistedSnapshot

            val wasInDisaster = previousState?.isDisasterPath == true
            val isTargetReached = dailyTarget > 0 && newIntake >= dailyTarget

            val crossedMilestones = OceanNotificationEvaluator.determineMilestonesCrossed(
                previousIntakeMl = previousIntake,
                newIntakeMl = newIntake,
                dailyTargetMl = dailyTarget,
                alreadyTriggeredMilestones = alreadyTriggered
            )

            val lastShownBadge = preferencesDataStore.getLastShownStreakBadgeId()
            val currentBadge = previousState?.currentMonthUnlockedBadgeIds?.lastOrNull()
            val newlyUnlockedStreakBadge = isTargetReached && currentBadge != null && currentBadge != lastShownBadge

            val celebration = OceanNotificationEvaluator.determineIntakeCelebration(
                wasInDisaster = wasInDisaster,
                previousIntakeMl = previousIntake,
                newIntakeMl = newIntake,
                dailyTargetMl = dailyTarget,
                newlyUnlockedStreakBadge = newlyUnlockedStreakBadge,
                alreadyTriggeredMilestones = alreadyTriggered
            )

            if (celebration != null) {
                val category = celebration.first
                val lastTitle = preferencesDataStore.getLastShownNotificationTitle(category.name)
                val message = OceanNotificationContent.selectMessage(
                    category = category,
                    lastShownTitle = lastTitle,
                    specificPool = celebration.second
                )

                val posted = ReminderHelper.showOceanNotification(context, message)
                if (posted) {
                    preferencesDataStore.setLastShownNotificationTitle(category.name, message.title)
                    if (crossedMilestones.isNotEmpty()) {
                        preferencesDataStore.recordMilestonesTriggeredToday(todayKey, crossedMilestones.toSet())
                    }
                    if (newlyUnlockedStreakBadge && currentBadge != null) {
                        preferencesDataStore.setLastShownStreakBadgeId(currentBadge)
                    }
                }
            } else if (crossedMilestones.isNotEmpty()) {
                preferencesDataStore.recordMilestonesTriggeredToday(todayKey, crossedMilestones.toSet())
            }
        } catch (_: Exception) {}
    }

    suspend fun deleteEntry(id: Long) {
        waterEntryDao.deleteEntryById(id)
        val latestTimestamp = waterEntryDao.getLatestEntryTimestamp() ?: 0L
        preferencesDataStore.setLastIntakeTimestamp(latestTimestamp)
        triggerWidgetRefresh()
        triggerIntakeReminderReset(latestTimestamp)
    }

    suspend fun updateEntry(entry: WaterEntry) {
        waterEntryDao.updateEntry(entry)
        val latestTimestamp = waterEntryDao.getLatestEntryTimestamp() ?: 0L
        preferencesDataStore.setLastIntakeTimestamp(latestTimestamp)
        triggerWidgetRefresh()
        triggerIntakeReminderReset(latestTimestamp)
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
        preferencesDataStore.setLastIntakeTimestamp(0L)
        triggerWidgetRefresh()
        triggerIntakeReminderReset(0L)
    }

    suspend fun clearAllData() {
        waterEntryDao.clearAll()
        preferencesDataStore.clearAll()
        triggerWidgetRefresh()
        triggerIntakeReminderReset(0L)
    }
}
