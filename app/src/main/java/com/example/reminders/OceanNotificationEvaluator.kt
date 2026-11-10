package com.example.reminders

object OceanNotificationEvaluator {

    val MILESTONES = listOf(25, 50, 75, 100)

    /**
     * Identifies which progress percentage milestones (25%, 50%, 75%, 100%) were crossed
     * by comparing previous intake with new intake against the daily target.
     * Milestones already triggered earlier today are excluded to prevent duplicate notifications.
     */
    fun determineMilestonesCrossed(
        previousIntakeMl: Int,
        newIntakeMl: Int,
        dailyTargetMl: Int,
        alreadyTriggeredMilestones: Set<Int>
    ): List<Int> {
        if (dailyTargetMl <= 0) return emptyList()

        return MILESTONES.filter { milestone ->
            val requiredMl = (dailyTargetMl * milestone) / 100
            val previouslyReached = previousIntakeMl >= requiredMl
            val newlyReached = newIntakeMl >= requiredMl
            newlyReached && !previouslyReached && !alreadyTriggeredMilestones.contains(milestone)
        }
    }

    /**
     * Determines whether the current time is within [windowMinutes] before bedtime.
     * Accurately supports both daytime schedules (e.g. 08:00 to 22:00) and overnight schedules.
     */
    fun isNearEndOfDay(
        currentMinutes: Int,
        sleepTimeString: String,
        windowMinutes: Int = 120
    ): Boolean {
        val sleepMinutes = ReminderHelper.parseTimeToMinutes(sleepTimeString, 22 * 60)
        val window = windowMinutes.coerceIn(15, 240)

        val diff = if (currentMinutes <= sleepMinutes) {
            sleepMinutes - currentMinutes
        } else {
            // Clock wrapped around midnight: e.g. current 23:30 (1410 min), sleep 01:00 (60 min)
            (1440 - currentMinutes) + sleepMinutes
        }

        return diff in 1..window
    }

    /**
     * Decides which periodic notification category to dispatch during WorkManager execution.
     * Returns null if no notification should be sent.
     */
    fun determinePeriodicNotificationCategory(
        remindersEnabled: Boolean,
        isWeekend: Boolean,
        weekendsEnabled: Boolean,
        isWithinWakingHours: Boolean,
        todayIntakeMl: Int,
        dailyTargetMl: Int,
        currentMinutes: Int,
        sleepTimeString: String,
        isYesterdayMissed: Boolean,
        isMissedGoalNotifiedToday: Boolean,
        isEndOfDayNotifiedToday: Boolean,
        isMorningWindow: Boolean = false
    ): NotificationCategory? {
        if (!remindersEnabled) return null
        if (isWeekend && !weekendsEnabled) return null
        if (!isWithinWakingHours) return null

        // Rule: Do not send reminder if today's target is already achieved
        if (dailyTargetMl > 0 && todayIntakeMl >= dailyTargetMl) {
            return null
        }

        // Priority 1: Morning notification for yesterday's missed goal
        if (isYesterdayMissed && !isMissedGoalNotifiedToday && isMorningWindow) {
            return NotificationCategory.MISSED_GOAL_AND_RECOVERY
        }

        // Priority 2: Near end-of-day nudge before sleeping
        if (isNearEndOfDay(currentMinutes, sleepTimeString) && !isEndOfDayNotifiedToday) {
            return NotificationCategory.END_OF_DAY_NUDGE
        }

        // Priority 3: Regular routine reminder
        return NotificationCategory.REGULAR_REMINDER
    }

    /**
     * Determines the appropriate celebration notification to show upon logging water intake.
     * Enforces strict priority: Recovery > Streak Milestone > Goal / Progress Milestone.
     */
    fun determineIntakeCelebration(
        wasInDisaster: Boolean,
        previousIntakeMl: Int,
        newIntakeMl: Int,
        dailyTargetMl: Int,
        newlyUnlockedStreakBadge: Boolean,
        alreadyTriggeredMilestones: Set<Int>
    ): Pair<NotificationCategory, List<NotificationMessage>>? {
        val isTargetReached = dailyTargetMl > 0 && newIntakeMl >= dailyTargetMl

        // Priority 1: Recovery from missed streak
        if (wasInDisaster && isTargetReached) {
            return Pair(
                NotificationCategory.MISSED_GOAL_AND_RECOVERY,
                OceanNotificationContent.RECOVERIES
            )
        }

        // Priority 2: New Streak milestone / Ocean Legend badge unlocked
        if (newlyUnlockedStreakBadge) {
            return Pair(
                NotificationCategory.STREAK_CELEBRATION,
                OceanNotificationContent.STREAK_CELEBRATIONS
            )
        }

        // Priority 3: Progress celebrations (25%, 50%, 75%, 100%)
        val crossed = determineMilestonesCrossed(
            previousIntakeMl = previousIntakeMl,
            newIntakeMl = newIntakeMl,
            dailyTargetMl = dailyTargetMl,
            alreadyTriggeredMilestones = alreadyTriggeredMilestones
        )

        if (crossed.isNotEmpty()) {
            return if (crossed.contains(100)) {
                Pair(
                    NotificationCategory.PROGRESS_CELEBRATION,
                    OceanNotificationContent.PROGRESS_GOAL_ACCOMPLISHED
                )
            } else {
                Pair(
                    NotificationCategory.PROGRESS_CELEBRATION,
                    OceanNotificationContent.PROGRESS_MID_MILESTONES
                )
            }
        }

        return null
    }
}
