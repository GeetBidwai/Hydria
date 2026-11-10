package com.example.domain.gamification

import com.example.core.utils.DateTimeUtils
import java.util.Calendar
import java.util.Locale

object GamificationEngine {

    /**
     * Evaluates monthly badge collection, streaks, monthly designations, and disaster states.
     */
    fun evaluate(
        dailyTotals: Map<String, Int>,
        dailyTargetMl: Int,
        todayDateKey: String = DateTimeUtils.getTodayDateKey(),
        todayConsumedMl: Int = 0,
        persistedCollections: List<MonthlyCollectionRecord> = emptyList(),
        persistedHighestDesignation: String? = null
    ): GamificationState {
        val safeTarget = if (dailyTargetMl > 0) dailyTargetMl else 2500
        val isTodayGoalMet = todayConsumedMl >= safeTarget

        // 1. Total successful days across all recorded history
        var totalSuccessfulDays = dailyTotals.count { (dateKey, total) ->
            dateKey != todayDateKey && total >= safeTarget
        }
        if (isTodayGoalMet) {
            totalSuccessfulDays += 1
        }

        // Parse today's calendar details
        val todayCal = Calendar.getInstance()
        val parsedToday = DateTimeUtils.parseDateKey(todayDateKey)
        if (parsedToday != null) {
            todayCal.time = parsedToday
        }
        val currentYear = todayCal.get(Calendar.YEAR)
        val currentMonth = todayCal.get(Calendar.MONTH) // 0-indexed
        val currentMonthKey = String.format(Locale.US, "%04d-%02d", currentYear, currentMonth + 1)
        val daysInCurrentMonth = todayCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val todayDayOfMonth = todayCal.get(Calendar.DAY_OF_MONTH)

        // 2. Chronological simulation of past history from earliest recorded date to yesterday
        val allPastDates = dailyTotals.keys.filter { it < todayDateKey }.sorted()

        var overallStreak = 0
        var currentMissedStreak = 0
        var monthlyStreak = 0
        var monthlyHighestStreak = 0

        val historicalCollectionsMap = mutableMapOf<String, MonthlyCollectionRecord>()
        // Seed with any persisted historical records
        persistedCollections.forEach { historicalCollectionsMap[it.monthKey] = it }

        var activeEvalMonthKey = if (allPastDates.isNotEmpty()) allPastDates.first().substring(0, 7) else currentMonthKey
        val currentMonthBadges = mutableListOf<String>()

        if (allPastDates.isNotEmpty()) {
            val startCal = Calendar.getInstance()
            val parsedStart = DateTimeUtils.parseDateKey(allPastDates.first())
            if (parsedStart != null) {
                startCal.time = parsedStart
            }

            val yesterdayCal = todayCal.clone() as Calendar
            yesterdayCal.add(Calendar.DAY_OF_YEAR, -1)

            val walkCal = startCal.clone() as Calendar

            while (!walkCal.after(yesterdayCal)) {
                val dayKey = DateTimeUtils.formatDateKey(walkCal.timeInMillis)
                val walkYear = walkCal.get(Calendar.YEAR)
                val walkMonth = walkCal.get(Calendar.MONTH)
                val walkDayOfMonth = walkCal.get(Calendar.DAY_OF_MONTH)
                val daysInWalkMonth = walkCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                val monthKey = String.format(Locale.US, "%04d-%02d", walkYear, walkMonth + 1)

                // Detect month transition
                if (monthKey != activeEvalMonthKey) {
                    // Archive previous month collection if it was active
                    if (activeEvalMonthKey != currentMonthKey && currentMonthBadges.isNotEmpty()) {
                        val isComplete = currentMonthBadges.size >= 10
                        val priorCompletedCount = historicalCollectionsMap.values.count {
                            it.monthKey < activeEvalMonthKey && it.isCompleted
                        }
                        historicalCollectionsMap[activeEvalMonthKey] = MonthlyCollectionRecord(
                            monthKey = activeEvalMonthKey,
                            year = activeEvalMonthKey.substring(0, 4).toIntOrNull() ?: walkYear,
                            month = activeEvalMonthKey.substring(5, 7).toIntOrNull() ?: (walkMonth + 1),
                            unlockedBadgeIds = currentMonthBadges.toList(),
                            highestStreak = monthlyHighestStreak,
                            isCompleted = isComplete,
                            designationAwarded = if (isComplete) Designation.getMonthlyDesignation(priorCompletedCount + 1) else null
                        )
                    }
                    activeEvalMonthKey = monthKey
                    monthlyStreak = 0
                    monthlyHighestStreak = 0
                    currentMonthBadges.clear()
                }

                val intake = dailyTotals[dayKey] ?: 0
                val goalMet = intake >= safeTarget

                if (goalMet) {
                    overallStreak++
                    monthlyStreak++
                    currentMissedStreak = 0
                    if (monthlyStreak > monthlyHighestStreak) {
                        monthlyHighestStreak = monthlyStreak
                    }

                    // Check badge unlocks in this month
                    for (badgeIndex in 0 until 10) {
                        val threshold = Designation.getBadgeThreshold(badgeIndex, daysInWalkMonth)
                        val badge = Designation.OCEAN_LEGENDS[badgeIndex]
                        if (!currentMonthBadges.contains(badge.id)) {
                            if (badgeIndex == 9) {
                                // 10th badge unlocks on final calendar day of month if streak is maintained
                                if (walkDayOfMonth == daysInWalkMonth && monthlyStreak >= daysInWalkMonth) {
                                    currentMonthBadges.add(badge.id)
                                }
                            } else {
                                if (monthlyStreak >= threshold) {
                                    currentMonthBadges.add(badge.id)
                                }
                            }
                        }
                    }
                } else {
                    overallStreak = 0
                    monthlyStreak = 0 // Missed day resets active monthly streak run
                    currentMissedStreak++
                }

                walkCal.add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        // If activeEvalMonthKey was from a previous month, ensure clean currentMonth state
        if (activeEvalMonthKey != currentMonthKey) {
            if (currentMonthBadges.isNotEmpty()) {
                val isComplete = currentMonthBadges.size >= 10
                val priorCompletedCount = historicalCollectionsMap.values.count {
                    it.monthKey < activeEvalMonthKey && it.isCompleted
                }
                historicalCollectionsMap[activeEvalMonthKey] = MonthlyCollectionRecord(
                    monthKey = activeEvalMonthKey,
                    year = activeEvalMonthKey.substring(0, 4).toIntOrNull() ?: currentYear,
                    month = activeEvalMonthKey.substring(5, 7).toIntOrNull() ?: (currentMonth + 1),
                    unlockedBadgeIds = currentMonthBadges.toList(),
                    highestStreak = monthlyHighestStreak,
                    isCompleted = isComplete,
                    designationAwarded = if (isComplete) Designation.getMonthlyDesignation(priorCompletedCount + 1) else null
                )
            }
            monthlyStreak = 0
            monthlyHighestStreak = 0
            currentMonthBadges.clear()
        }

        // 3. Dynamic evaluation of TODAY
        if (isTodayGoalMet) {
            overallStreak++
            monthlyStreak++
            currentMissedStreak = 0
            if (monthlyStreak > monthlyHighestStreak) {
                monthlyHighestStreak = monthlyStreak
            }

            // Check current month badge unlocks with today's intake
            for (badgeIndex in 0 until 10) {
                val threshold = Designation.getBadgeThreshold(badgeIndex, daysInCurrentMonth)
                val badge = Designation.OCEAN_LEGENDS[badgeIndex]
                if (!currentMonthBadges.contains(badge.id)) {
                    if (badgeIndex == 9) {
                        if (todayDayOfMonth == daysInCurrentMonth && monthlyStreak >= daysInCurrentMonth) {
                            currentMonthBadges.add(badge.id)
                        }
                    } else {
                        if (monthlyStreak >= threshold) {
                            currentMonthBadges.add(badge.id)
                        }
                    }
                }
            }
        } else {
            // Today is still ongoing; do not count as a missed day
        }

        // 4. Completed collections & monthly designations
        val isCurrentMonthCompleted = currentMonthBadges.size >= 10
        val completedPastCount = historicalCollectionsMap.values.count {
            it.monthKey < currentMonthKey && it.isCompleted
        }
        val totalCompletedCollectionsCount = completedPastCount + (if (isCurrentMonthCompleted) 1 else 0)
        val activeMonthlyDesignation = Designation.getMonthlyDesignation(totalCompletedCollectionsCount)

        // 5. Resolve active designation, titles & descriptions
        val currentDesignationTitle: String
        val currentDesignationCategory: DesignationCategory
        val currentDesignationDescription: String
        val isDisasterPath: Boolean
        val nextMilestoneTitle: String?
        val nextMilestoneDays: Int
        val progressFraction: Float

        if (currentMissedStreak >= 1) {
            isDisasterPath = true
            val disaster = Designation.getDisasterForMissedDays(currentMissedStreak)
                ?: Designation.OCEAN_DISASTERS.first()
            currentDesignationTitle = disaster.title
            currentDesignationCategory = DesignationCategory.OCEAN_DISASTER
            currentDesignationDescription = disaster.description

            val nextDisaster = Designation.getNextDisaster(currentMissedStreak)
            nextMilestoneTitle = nextDisaster?.title
            nextMilestoneDays = nextDisaster?.thresholdDays ?: currentMissedStreak
            progressFraction = if (nextDisaster != null) {
                val prevThreshold = disaster.thresholdDays
                val span = (nextDisaster.thresholdDays - prevThreshold).coerceAtLeast(1)
                ((currentMissedStreak - prevThreshold).toFloat() / span).coerceIn(0f, 1f)
            } else {
                1.0f
            }
        } else {
            isDisasterPath = false

            // Active title preference:
            // 1) Active Monthly Designation if earned
            // 2) Latest unlocked Ocean Legend badge in current month
            // 3) Default starter
            if (activeMonthlyDesignation != null) {
                currentDesignationTitle = activeMonthlyDesignation
                currentDesignationCategory = DesignationCategory.MONTHLY_DESIGNATION
                currentDesignationDescription = "Master of monthly collections with $totalCompletedCollectionsCount full ocean conquests."
            } else if (currentMonthBadges.isNotEmpty()) {
                val lastBadgeId = currentMonthBadges.last()
                val badge = Designation.OCEAN_LEGENDS.find { it.id == lastBadgeId } ?: Designation.DEFAULT_STARTER
                currentDesignationTitle = badge.title
                currentDesignationCategory = DesignationCategory.OCEAN_LEGEND
                currentDesignationDescription = badge.description
            } else {
                currentDesignationTitle = Designation.DEFAULT_STARTER.title
                currentDesignationCategory = DesignationCategory.NOVICE
                currentDesignationDescription = Designation.DEFAULT_STARTER.description
            }

            // Next milestone in current month
            val nextBadgeIndex = currentMonthBadges.size
            if (nextBadgeIndex < 10) {
                val nextBadge = Designation.OCEAN_LEGENDS[nextBadgeIndex]
                val targetThreshold = Designation.getBadgeThreshold(nextBadgeIndex, daysInCurrentMonth)
                val prevThreshold = if (nextBadgeIndex > 0) {
                    Designation.getBadgeThreshold(nextBadgeIndex - 1, daysInCurrentMonth)
                } else 0

                val span = (targetThreshold - prevThreshold).coerceAtLeast(1)
                progressFraction = ((monthlyStreak - prevThreshold).toFloat() / span).coerceIn(0f, 1f)
                nextMilestoneTitle = nextBadge.title
                nextMilestoneDays = targetThreshold
            } else {
                nextMilestoneTitle = "Monthly Collection Complete!"
                nextMilestoneDays = daysInCurrentMonth
                progressFraction = 1.0f
            }
        }

        // Lifetime total badges unlocked across all months
        var lifetimeBadgeCount = 0
        historicalCollectionsMap.values.forEach {
            lifetimeBadgeCount += it.unlockedBadgeIds.size
        }
        lifetimeBadgeCount += currentMonthBadges.size

        // Highest designation achieved preservation
        val highestTitle = resolveLifetimeHighest(
            currentTitle = currentDesignationTitle,
            currentCategory = currentDesignationCategory,
            persistedHighest = persistedHighestDesignation
        )

        return GamificationState(
            overallStreak = overallStreak,
            currentMissedStreak = currentMissedStreak,
            monthlyStreak = monthlyStreak,
            monthlyHighestStreak = monthlyHighestStreak,
            currentMonthKey = currentMonthKey,
            daysInCurrentMonth = daysInCurrentMonth,
            currentMonthUnlockedBadgeIds = currentMonthBadges.toList(),
            currentMonthBadgeCount = currentMonthBadges.size,
            isCurrentMonthCompleted = isCurrentMonthCompleted,
            currentDesignationTitle = currentDesignationTitle,
            currentDesignationCategory = currentDesignationCategory,
            currentDesignationDescription = currentDesignationDescription,
            activeMonthlyDesignation = activeMonthlyDesignation,
            lifetimeHighestDesignation = highestTitle,
            lifetimeUnlockedBadgeCount = lifetimeBadgeCount,
            completedMonthlyCollectionsCount = totalCompletedCollectionsCount,
            completedMonthlyCollections = historicalCollectionsMap.values.toList().sortedBy { it.monthKey },
            nextMilestoneTitle = nextMilestoneTitle,
            nextMilestoneDays = nextMilestoneDays,
            progressFraction = progressFraction,
            isDisasterPath = isDisasterPath,
            totalSuccessfulDays = totalSuccessfulDays,
            lastEvaluatedDateKey = todayDateKey
        )
    }

    private fun resolveLifetimeHighest(
        currentTitle: String,
        currentCategory: DesignationCategory,
        persistedHighest: String?
    ): String {
        if (persistedHighest.isNullOrBlank() || persistedHighest == Designation.DEFAULT_STARTER.title) {
            return if (currentCategory != DesignationCategory.OCEAN_DISASTER) currentTitle else Designation.DEFAULT_STARTER.title
        }

        // Monthly designations outrank Ocean Legends
        val isCurrentMonthly = currentCategory == DesignationCategory.MONTHLY_DESIGNATION
        val isPersistedMonthly = Designation.MONTHLY_DESIGNATION_TITLES.contains(persistedHighest) ||
                persistedHighest.startsWith("Keeper of the Seven Seas")

        if (isCurrentMonthly && !isPersistedMonthly) return currentTitle
        if (!isCurrentMonthly && isPersistedMonthly) return persistedHighest

        if (isCurrentMonthly && isPersistedMonthly) {
            val currIdx = Designation.MONTHLY_DESIGNATION_TITLES.indexOf(currentTitle)
            val persIdx = Designation.MONTHLY_DESIGNATION_TITLES.indexOf(persistedHighest)
            return if (currIdx >= persIdx) currentTitle else persistedHighest
        }

        val currLegend = Designation.OCEAN_LEGENDS.find { it.title == currentTitle }
        val persLegend = Designation.OCEAN_LEGENDS.find { it.title == persistedHighest }

        return when {
            currLegend != null && persLegend != null -> {
                if (currLegend.thresholdDays >= persLegend.thresholdDays) currLegend.title else persLegend.title
            }
            currLegend != null -> currLegend.title
            else -> persistedHighest
        }
    }
}
