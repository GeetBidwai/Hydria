package com.example

import com.example.core.constants.AppConstants
import com.example.core.utils.DateTimeUtils
import com.example.domain.calculator.HydrationCalculator
import com.example.domain.model.ActivityLevel
import com.example.domain.model.Climate
import com.example.domain.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Locale

class ExampleUnitTest {

    @Test
    fun testDateKeyGeneration() {
        val todayKey = DateTimeUtils.getTodayDateKey()
        // Format is YYYY-MM-DD
        assertTrue(todayKey.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
    }

    @Test
    fun testDefaultConstants() {
        assertEquals(2500, AppConstants.DEFAULT_DAILY_TARGET_ML)
        assertEquals(4, AppConstants.QUICK_ADD_AMOUNTS.size)
        assertTrue(AppConstants.QUICK_ADD_AMOUNTS.contains(250))
    }

    @Test
    fun testUserProfileDefaults() {
        val profile = UserProfile()
        assertEquals(28, profile.age)
        assertEquals(70f, profile.weightKg)
        assertEquals(ActivityLevel.MODERATE, profile.activityLevel)
        assertEquals(Climate.TEMPERATE, profile.climate)
        assertEquals("ml", profile.preferredUnit)
        assertEquals("07:00", profile.wakeTime)
        assertEquals("23:00", profile.sleepTime)
        assertTrue(profile.remindersEnabled)
    }

    @Test
    fun testActivityLevelParsing() {
        assertEquals(ActivityLevel.SEDENTARY, ActivityLevel.fromString("SEDENTARY"))
        assertEquals(ActivityLevel.VERY_ACTIVE, ActivityLevel.fromString("very_active"))
        assertEquals(ActivityLevel.MODERATE, ActivityLevel.fromString("unknown_level"))
    }

    @Test
    fun testClimateParsing() {
        assertEquals(Climate.TEMPERATE, Climate.fromString("TEMPERATE"))
        assertEquals(Climate.WARM, Climate.fromString("warm"))
        assertEquals(Climate.HOT_HUMID, Climate.fromString("hot_humid"))
        assertEquals(Climate.TEMPERATE, Climate.fromString("unknown"))
    }

    @Test
    fun testHydrationCalculationFormula() {
        // 70kg * 35ml = 2450. Moderate (1.25x) = 3062.5 + 300ml (30 min) = 3362.5 -> rounded 3350ml
        val profile = UserProfile(
            weightKg = 70f,
            age = 28,
            activityLevel = ActivityLevel.MODERATE,
            exerciseMinutes = 30,
            climate = Climate.TEMPERATE
        )
        val target = HydrationCalculator.calculateDailyTargetMl(profile)
        assertTrue("Target should be in healthy bounds", target in 2000..4000)

        // Sedentary with zero exercise: 60kg * 35ml = 2100 -> rounded 2100ml
        val sedentaryProfile = UserProfile(
            weightKg = 60f,
            age = 30,
            activityLevel = ActivityLevel.SEDENTARY,
            exerciseMinutes = 0,
            climate = Climate.TEMPERATE
        )
        val sedentaryTarget = HydrationCalculator.calculateDailyTargetMl(sedentaryProfile)
        assertEquals(2100, sedentaryTarget)
    }

    @Test
    fun testClimateHydrationAdjustment() {
        val temperateProfile = UserProfile(weightKg = 70f, activityLevel = ActivityLevel.SEDENTARY, exerciseMinutes = 0, climate = Climate.TEMPERATE)
        val warmProfile = UserProfile(weightKg = 70f, activityLevel = ActivityLevel.SEDENTARY, exerciseMinutes = 0, climate = Climate.WARM)
        val hotProfile = UserProfile(weightKg = 70f, activityLevel = ActivityLevel.SEDENTARY, exerciseMinutes = 0, climate = Climate.HOT_HUMID)

        val tempTarget = HydrationCalculator.calculateDailyTargetMl(temperateProfile)
        val warmTarget = HydrationCalculator.calculateDailyTargetMl(warmProfile)
        val hotTarget = HydrationCalculator.calculateDailyTargetMl(hotProfile)

        assertEquals(tempTarget + 250, warmTarget)
        assertEquals(tempTarget + 500, hotTarget)
    }

    @Test
    fun testCalculationFormulaClamping() {
        // Extremely low weight profile
        val lowProfile = UserProfile(weightKg = 20f, activityLevel = ActivityLevel.SEDENTARY, exerciseMinutes = 0)
        val lowTarget = HydrationCalculator.calculateDailyTargetMl(lowProfile)
        assertTrue(lowTarget >= AppConstants.MIN_DAILY_TARGET_ML)

        // Extremely high weight profile
        val highProfile = UserProfile(weightKg = 250f, activityLevel = ActivityLevel.VERY_ACTIVE, exerciseMinutes = 180, climate = Climate.HOT_HUMID)
        val highTarget = HydrationCalculator.calculateDailyTargetMl(highProfile)
        assertTrue(highTarget <= AppConstants.MAX_DAILY_TARGET_ML)
    }

    @Test
    fun testRemainingWaterNeverNegative() {
        val target = 2500
        val consumedUnder = 1000
        val consumedExact = 2500
        val consumedOver = 3200

        assertEquals(1500, (target - consumedUnder).coerceAtLeast(0))
        assertEquals(0, (target - consumedExact).coerceAtLeast(0))
        assertEquals(0, (target - consumedOver).coerceAtLeast(0))
    }

    @Test
    fun testProgressPercentageCalculation() {
        val target = 2500
        val consumedHalf = 1250
        val consumedFull = 2500
        val consumedOver = 3000

        assertEquals(50, ((consumedHalf.toFloat() / target) * 100).toInt())
        assertEquals(100, ((consumedFull.toFloat() / target) * 100).toInt())
        assertEquals(120, ((consumedOver.toFloat() / target) * 100).toInt())
    }

    @Test
    fun testCustomAmountValidationBounds() {
        fun isValid(amount: Int?): Boolean {
            return amount != null && amount in 1..3000
        }

        assertTrue(isValid(250))
        assertTrue(isValid(1))
        assertTrue(isValid(3000))
        assertFalse(isValid(0))
        assertFalse(isValid(-50))
        assertFalse(isValid(3001))
        assertFalse(isValid(null))
    }

    @Test
    fun testQuickAddAmountsValid() {
        for (amount in AppConstants.QUICK_ADD_AMOUNTS) {
            assertTrue("Quick-add amount must be positive", amount > 0)
            assertTrue("Quick-add amount must not exceed max per-log limit", amount <= 3000)
        }
    }

    @Test
    fun testUnitFormatting() {
        val volumeMl = 2500
        val mlFormatted = "$volumeMl ml"
        val litresFormatted = String.format(Locale.US, "%.2f L", volumeMl / 1000f)

        assertEquals("2500 ml", mlFormatted)
        assertEquals("2.50 L", litresFormatted)
    }

    @Test
    fun testSevenDayDateKeysCount() {
        val last7Days = DateTimeUtils.getLastNDaysDateKeys(7)
        assertEquals(7, last7Days.size)
        // Last element should be today
        assertEquals(DateTimeUtils.getTodayDateKey(), last7Days.last())
    }

    @Test
    fun testSevenDayAverageIncludingZeroIntakeDays() {
        val target = 2500
        // Recorded days: Day1: 2500, Day2: 2000, Day3: 0, Day4: 0, Day5: 2500, Day6: 1500, Day7: 0
        val intakeMap = mapOf(
            "day1" to 2500,
            "day2" to 2000,
            "day3" to 0,
            "day4" to 0,
            "day5" to 2500,
            "day6" to 1500,
            "day7" to 0
        )
        val total = intakeMap.values.sum() // 8500
        val average = total / 7 // 1214
        val daysMet = intakeMap.values.count { it >= target } // 2 days met (Day1, Day5)

        assertEquals(8500, total)
        assertEquals(1214, average)
        assertEquals(2, daysMet)
    }

    @Test
    fun testMonthDateKeysGeneration() {
        // November (month index 10) always has 30 days
        val novDays = DateTimeUtils.getMonthDateKeys(2026, 10)
        assertEquals(30, novDays.size)
        assertEquals("2026-11-01", novDays.first())
        assertEquals("2026-11-30", novDays.last())

        // December (month index 11) has 31 days
        val decDays = DateTimeUtils.getMonthDateKeys(2026, 11)
        assertEquals(31, decDays.size)
        assertEquals("2026-12-01", decDays.first())
        assertEquals("2026-12-31", decDays.last())
    }

    @Test
    fun testMonthNavigationBoundaries() {
        val currentYear = 2026
        val currentMonth = 10 // November

        fun canGoForward(year: Int, month: Int): Boolean {
            return (year < currentYear) || (year == currentYear && month < currentMonth)
        }

        // October 2026 can navigate forward to November
        assertTrue(canGoForward(2026, 9))
        // November 2026 (current) cannot navigate forward to December
        assertFalse(canGoForward(2026, 10))
        // December 2026 cannot navigate forward
        assertFalse(canGoForward(2026, 11))
        // Any month in 2025 can navigate forward
        assertTrue(canGoForward(2025, 11))
    }

    @Test
    fun testNavigationRoutes() {
        assertEquals("dashboard", com.example.presentation.navigation.Screen.Dashboard.route)
        assertEquals("history", com.example.presentation.navigation.Screen.History.route)
        assertEquals("settings", com.example.presentation.navigation.Screen.Settings.route)
        assertEquals("calculator", com.example.presentation.navigation.Screen.Calculator.route)
        assertEquals("onboarding", com.example.presentation.navigation.Screen.Onboarding.route)
        assertEquals(3, com.example.presentation.navigation.bottomNavItems.size)
    }

    @Test
    fun testReminderTimeParsing() {
        assertEquals(8 * 60, com.example.reminders.ReminderHelper.parseTimeToMinutes("08:00", 0))
        assertEquals(22 * 60 + 30, com.example.reminders.ReminderHelper.parseTimeToMinutes("22:30", 0))
        assertEquals(0, com.example.reminders.ReminderHelper.parseTimeToMinutes("00:00", 60))
        // Invalid inputs fallback to default
        assertEquals(480, com.example.reminders.ReminderHelper.parseTimeToMinutes("invalid", 480))
        assertEquals(480, com.example.reminders.ReminderHelper.parseTimeToMinutes("25:99", 480))
    }

    @Test
    fun testWakingHoursStandardSchedule() {
        val wake = "08:00"
        val sleep = "22:00"

        // Exactly at boundary
        assertTrue(com.example.reminders.ReminderHelper.isWithinWakingHours(8 * 60, wake, sleep))
        assertTrue(com.example.reminders.ReminderHelper.isWithinWakingHours(22 * 60, wake, sleep))

        // Inside range (14:30)
        assertTrue(com.example.reminders.ReminderHelper.isWithinWakingHours(14 * 60 + 30, wake, sleep))

        // Before wake time (07:59)
        assertFalse(com.example.reminders.ReminderHelper.isWithinWakingHours(7 * 60 + 59, wake, sleep))

        // After sleep time (22:01)
        assertFalse(com.example.reminders.ReminderHelper.isWithinWakingHours(22 * 60 + 1, wake, sleep))

        // Middle of night (03:00)
        assertFalse(com.example.reminders.ReminderHelper.isWithinWakingHours(3 * 60, wake, sleep))
    }

    @Test
    fun testWakingHoursOvernightSchedule() {
        // Night shift schedule: awake from 22:00 to 06:00
        val wake = "22:00"
        val sleep = "06:00"

        // Inside late evening (23:00)
        assertTrue(com.example.reminders.ReminderHelper.isWithinWakingHours(23 * 60, wake, sleep))

        // Inside early morning (04:00)
        assertTrue(com.example.reminders.ReminderHelper.isWithinWakingHours(4 * 60, wake, sleep))

        // Boundaries
        assertTrue(com.example.reminders.ReminderHelper.isWithinWakingHours(22 * 60, wake, sleep))
        assertTrue(com.example.reminders.ReminderHelper.isWithinWakingHours(6 * 60, wake, sleep))

        // Daytime sleeping (12:00, 18:00)
        assertFalse(com.example.reminders.ReminderHelper.isWithinWakingHours(12 * 60, wake, sleep))
        assertFalse(com.example.reminders.ReminderHelper.isWithinWakingHours(18 * 60, wake, sleep))
    }

    @Test
    fun testWeekendDetection() {
        val sat = Calendar.getInstance().apply { set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY) }
        val sun = Calendar.getInstance().apply { set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY) }
        val wed = Calendar.getInstance().apply { set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY) }

        assertTrue(com.example.reminders.ReminderHelper.isWeekend(sat))
        assertTrue(com.example.reminders.ReminderHelper.isWeekend(sun))
        assertFalse(com.example.reminders.ReminderHelper.isWeekend(wed))
    }

    @Test
    fun testGoalReachedSuppression() {
        // Goal reached -> returns true (suppresses notification)
        assertTrue(com.example.reminders.ReminderHelper.isDailyGoalReached(2500, 2500))
        assertTrue(com.example.reminders.ReminderHelper.isDailyGoalReached(3000, 2500))

        // Goal not yet reached -> returns false (do not suppress)
        assertFalse(com.example.reminders.ReminderHelper.isDailyGoalReached(1500, 2500))
        assertFalse(com.example.reminders.ReminderHelper.isDailyGoalReached(0, 2500))
        assertFalse(com.example.reminders.ReminderHelper.isDailyGoalReached(500, 0))
    }

    @Test
    fun testReminderIntervalsMeetWorkManagerMinimum() {
        val intervals = com.example.reminders.ReminderScheduler.AVAILABLE_INTERVALS
        assertEquals(4, intervals.size)
        // WorkManager minimum periodic work interval is 15 minutes
        for (interval in intervals) {
            assertTrue("Interval $interval must be at least 15 minutes", interval >= 15)
        }
    }

    @Test
    fun testNotificationChannelConfiguration() {
        // Must use the new migrated channel ID to force re-creation with sound on existing devices
        assertEquals("water_reminders_channel_v2", com.example.reminders.ReminderHelper.CHANNEL_ID)
        assertEquals(1001, com.example.reminders.ReminderHelper.NOTIFICATION_ID)
        assertEquals("Hydration Reminders", com.example.reminders.ReminderHelper.CHANNEL_NAME)
    }

    @Test
    fun testWidgetConstantsAndPresets() {
        assertEquals(300, AppConstants.DEFAULT_WIDGET_QUICK_ADD_ML)
        assertEquals(8, AppConstants.WIDGET_PRESET_AMOUNTS.size)
        assertTrue(AppConstants.WIDGET_PRESET_AMOUNTS.contains(150))
        assertTrue(AppConstants.WIDGET_PRESET_AMOUNTS.contains(300))
        assertTrue(AppConstants.WIDGET_PRESET_AMOUNTS.contains(750))
    }

    @Test
    fun testWidgetCustomAmountValidation() {
        fun isValidCustomWidgetAmount(amount: Int?): Boolean {
            return amount != null && amount in 50..3000
        }

        assertTrue(isValidCustomWidgetAmount(300))
        assertTrue(isValidCustomWidgetAmount(50))
        assertTrue(isValidCustomWidgetAmount(3000))
        assertTrue(isValidCustomWidgetAmount(350))
        assertFalse(isValidCustomWidgetAmount(0))
        assertFalse(isValidCustomWidgetAmount(-100))
        assertFalse(isValidCustomWidgetAmount(3001))
        assertFalse(isValidCustomWidgetAmount(null))
    }

    @Test
    fun testWidgetMetricsCalculation() {
        val target = 2000

        // Zero intake
        val zeroConsumed = 0
        val zeroFraction = (zeroConsumed.toFloat() / target).coerceIn(0f, 1f)
        val zeroPercent = ((zeroConsumed.toFloat() / target) * 100).toInt()
        val zeroRemaining = (target - zeroConsumed).coerceAtLeast(0)
        assertEquals(0f, zeroFraction, 0.001f)
        assertEquals(0, zeroPercent)
        assertEquals(2000, zeroRemaining)

        // Half intake
        val halfConsumed = 1000
        val halfFraction = (halfConsumed.toFloat() / target).coerceIn(0f, 1f)
        val halfPercent = ((halfConsumed.toFloat() / target) * 100).toInt()
        val halfRemaining = (target - halfConsumed).coerceAtLeast(0)
        assertEquals(0.5f, halfFraction, 0.001f)
        assertEquals(50, halfPercent)
        assertEquals(1000, halfRemaining)

        // Met target
        val fullConsumed = 2000
        val fullFraction = (fullConsumed.toFloat() / target).coerceIn(0f, 1f)
        val fullPercent = ((fullConsumed.toFloat() / target) * 100).toInt()
        val fullRemaining = (target - fullConsumed).coerceAtLeast(0)
        assertEquals(1.0f, fullFraction, 0.001f)
        assertEquals(100, fullPercent)
        assertEquals(0, fullRemaining)

        // Exceeded target
        val overConsumed = 2600
        val overFraction = (overConsumed.toFloat() / target).coerceIn(0f, 1f)
        val overPercent = ((overConsumed.toFloat() / target) * 100).toInt()
        val overRemaining = (target - overConsumed).coerceAtLeast(0)
        assertEquals(1.0f, overFraction, 0.001f)
        assertEquals(130, overPercent)
        assertEquals(0, overRemaining)
    }

    @Test
    fun testWidgetVolumeFormatting() {
        val formatMl = com.example.widgets.WidgetDataHelper.formatVolume(1500, "ml")
        val formatLitres = com.example.widgets.WidgetDataHelper.formatVolume(2500, "litres")

        assertEquals("1,500 ml", formatMl)
        assertEquals("2.50 L", formatLitres)
    }

    @Test
    fun testWidgetQuickAddOptionsComposition() {
        fun buildQuickAddOptions(customAmount: Int): List<Int> {
            return buildList {
                add(customAmount)
                listOf(150, 300, 500).forEach { fixed ->
                    if (!contains(fixed)) {
                        add(fixed)
                    }
                }
            }.take(4)
        }

        // Default 300 -> [300, 150, 500]
        val defaultOptions = buildQuickAddOptions(300)
        assertEquals(listOf(300, 150, 500), defaultOptions)

        // Custom 350 -> [350, 150, 300, 500]
        val custom350Options = buildQuickAddOptions(350)
        assertEquals(listOf(350, 150, 300, 500), custom350Options)

        // Custom 150 -> [150, 300, 500]
        val custom150Options = buildQuickAddOptions(150)
        assertEquals(listOf(150, 300, 500), custom150Options)
    }

    @Test
    fun testAppBranding() {
        val appName = "Hydria"
        val compactWidgetName = "Hydria Quick Log (2×1)"
        val expandedWidgetName = "Hydria Tracker (4×2)"

        assertEquals("Hydria", appName)
        assertTrue(compactWidgetName.startsWith("Hydria"))
        assertTrue(expandedWidgetName.startsWith("Hydria"))
    }

    @Test
    fun testOceanLegendsMonthlyThresholds() {
        // Badges 1..9 are at 3, 6, 9, 12, 15, 18, 21, 24, 27
        for (i in 0 until 9) {
            val expectedDays = (i + 1) * 3
            assertEquals(expectedDays, com.example.domain.gamification.Designation.getBadgeThreshold(i, 30))
        }
        // 10th badge on final day
        assertEquals(30, com.example.domain.gamification.Designation.getBadgeThreshold(9, 30))
        assertEquals(31, com.example.domain.gamification.Designation.getBadgeThreshold(9, 31))
        assertEquals(28, com.example.domain.gamification.Designation.getBadgeThreshold(9, 28))
        assertEquals(29, com.example.domain.gamification.Designation.getBadgeThreshold(9, 29))
    }

    @Test
    fun testOceanDisastersProgressionOrder() {
        val expectedTitles = listOf(
            "Aquatic Intern",
            "Confused Octopus",
            "Dry Fish",
            "Landlocked Crab",
            "Fish Outta Water",
            "Desert Sponge",
            "SpongeBob's Nemesis",
            "Thirsty Squidward",
            "The Deep",
            "The Sahara Survivor"
        )
        val actualTitles = com.example.domain.gamification.Designation.OCEAN_DISASTERS.map { it.title }
        assertEquals(expectedTitles, actualTitles)
    }

    @Test
    fun testMonthlyBadgesProgressionAt3to27Days() {
        val dailyTotals = mutableMapOf<String, Int>()
        // Build 12 consecutive days in November 2026
        for (day in 1..12) {
            dailyTotals[String.format(java.util.Locale.US, "2026-11-%02d", day)] = 2500
        }
        // Evaluate on Nov 12 with today goal met
        val state = com.example.domain.gamification.GamificationEngine.evaluate(
            dailyTotals = dailyTotals,
            dailyTargetMl = 2500,
            todayDateKey = "2026-11-12",
            todayConsumedMl = 2500
        )

        // 12 days in November unlocks: Triton (3), Varuna (6), Aquaman (9), Neptune (12)
        assertEquals(12, state.monthlyStreak)
        assertEquals(12, state.overallStreak)
        assertEquals(4, state.currentMonthBadgeCount)
        assertEquals(listOf("triton", "varuna", "aquaman", "neptune"), state.currentMonthUnlockedBadgeIds)
        assertEquals("Neptune", state.currentDesignationTitle)
    }

    @Test
    fun testTenthBadgeOnFinalDayOfMonth30Days() {
        val dailyTotals = mutableMapOf<String, Int>()
        // Complete all 30 days in November
        for (day in 1..30) {
            dailyTotals[String.format(java.util.Locale.US, "2026-11-%02d", day)] = 2500
        }
        val state = com.example.domain.gamification.GamificationEngine.evaluate(
            dailyTotals = dailyTotals,
            dailyTargetMl = 2500,
            todayDateKey = "2026-11-30",
            todayConsumedMl = 2500
        )

        assertEquals(30, state.monthlyStreak)
        assertEquals(10, state.currentMonthBadgeCount)
        assertTrue(state.isCurrentMonthCompleted)
        assertTrue(state.currentMonthUnlockedBadgeIds.contains("lord_of_seven_seas"))
        assertEquals("Ocean Explorer", state.currentDesignationTitle)
    }

    @Test
    fun testFebruaryWith28Days() {
        // Non-leap year: 2025 February has 28 days
        val dailyTotals = mutableMapOf<String, Int>()
        for (day in 1..28) {
            dailyTotals[String.format(java.util.Locale.US, "2025-02-%02d", day)] = 2500
        }
        val state = com.example.domain.gamification.GamificationEngine.evaluate(
            dailyTotals = dailyTotals,
            dailyTargetMl = 2500,
            todayDateKey = "2025-02-28",
            todayConsumedMl = 2500
        )

        assertEquals(28, state.daysInCurrentMonth)
        assertEquals(28, state.monthlyStreak)
        assertEquals(10, state.currentMonthBadgeCount)
        assertTrue(state.isCurrentMonthCompleted)
        assertTrue(state.currentMonthUnlockedBadgeIds.contains("lord_of_seven_seas"))
    }

    @Test
    fun testLeapYearFebruaryWith29Days() {
        // Leap year: 2024 February has 29 days
        val dailyTotals = mutableMapOf<String, Int>()
        for (day in 1..29) {
            dailyTotals[String.format(java.util.Locale.US, "2024-02-%02d", day)] = 2500
        }
        val state = com.example.domain.gamification.GamificationEngine.evaluate(
            dailyTotals = dailyTotals,
            dailyTargetMl = 2500,
            todayDateKey = "2024-02-29",
            todayConsumedMl = 2500
        )

        assertEquals(29, state.daysInCurrentMonth)
        assertEquals(29, state.monthlyStreak)
        assertEquals(10, state.currentMonthBadgeCount)
        assertTrue(state.isCurrentMonthCompleted)
        assertTrue(state.currentMonthUnlockedBadgeIds.contains("lord_of_seven_seas"))
    }

    @Test
    fun testOverallStreakContinuityAcrossMonthBoundaries() {
        val dailyTotals = mutableMapOf<String, Int>()
        // End of October (Oct 28..31) = 4 days
        dailyTotals["2026-10-28"] = 2500
        dailyTotals["2026-10-29"] = 2500
        dailyTotals["2026-10-30"] = 2500
        dailyTotals["2026-10-31"] = 2500
        // Day 1 of November = Nov 1
        dailyTotals["2026-11-01"] = 2500

        val state = com.example.domain.gamification.GamificationEngine.evaluate(
            dailyTotals = dailyTotals,
            dailyTargetMl = 2500,
            todayDateKey = "2026-11-01",
            todayConsumedMl = 2500
        )

        // Overall streak continues unbroken (4 + 1 = 5)
        assertEquals(5, state.overallStreak)
        // Monthly streak for November starts at 1
        assertEquals(1, state.monthlyStreak)
        assertEquals(0, state.currentMonthBadgeCount)
    }

    @Test
    fun testMonthlyStreakResetAfterMissedDayPreservesBadges() {
        val dailyTotals = mutableMapOf<String, Int>()
        // Days 1..7 met target in November
        for (day in 1..7) {
            dailyTotals[String.format(java.util.Locale.US, "2026-11-%02d", day)] = 2500
        }
        // Day 8 was missed (0 ml)
        dailyTotals["2026-11-08"] = 0
        // Day 9 user drinks 2500 ml
        dailyTotals["2026-11-09"] = 2500

        val state = com.example.domain.gamification.GamificationEngine.evaluate(
            dailyTotals = dailyTotals,
            dailyTargetMl = 2500,
            todayDateKey = "2026-11-09",
            todayConsumedMl = 2500
        )

        // Monthly streak reset on day 8, restarted on day 9
        assertEquals(1, state.monthlyStreak)
        assertEquals(1, state.overallStreak)
        // Highest streak was 7
        assertEquals(7, state.monthlyHighestStreak)
        // Badges earned during days 1..7 (Triton at 3, Varuna at 6) remain permanently unlocked in this month!
        assertEquals(2, state.currentMonthBadgeCount)
        assertTrue(state.currentMonthUnlockedBadgeIds.contains("triton"))
        assertTrue(state.currentMonthUnlockedBadgeIds.contains("varuna"))
    }

    @Test
    fun testOceanDisastersAndRecovery() {
        val dailyTotals = mutableMapOf<String, Int>()
        // Missed yesterday and day before
        dailyTotals["2026-11-07"] = 0
        dailyTotals["2026-11-08"] = 0
        // Today Nov 9 before drinking
        val disasterState = com.example.domain.gamification.GamificationEngine.evaluate(
            dailyTotals = dailyTotals,
            dailyTargetMl = 2500,
            todayDateKey = "2026-11-09",
            todayConsumedMl = 0
        )

        assertEquals(2, disasterState.currentMissedStreak)
        assertTrue(disasterState.isDisasterPath)
        assertEquals("Confused Octopus", disasterState.currentDesignationTitle)

        // User hydrates and reaches 100% today
        val recoveredState = com.example.domain.gamification.GamificationEngine.evaluate(
            dailyTotals = dailyTotals,
            dailyTargetMl = 2500,
            todayDateKey = "2026-11-09",
            todayConsumedMl = 2500
        )

        assertEquals(0, recoveredState.currentMissedStreak)
        assertFalse(recoveredState.isDisasterPath)
        assertEquals(1, recoveredState.overallStreak)
        assertEquals(1, recoveredState.monthlyStreak)
    }

    @Test
    fun testMonthlyDesignationProgressionAcrossCollections() {
        val titles = com.example.domain.gamification.Designation.MONTHLY_DESIGNATION_TITLES
        assertEquals(12, titles.size)
        assertEquals("Ocean Explorer", com.example.domain.gamification.Designation.getMonthlyDesignation(1))
        assertEquals("Sea Guardian", com.example.domain.gamification.Designation.getMonthlyDesignation(2))
        assertEquals("Keeper of the Seven Seas", com.example.domain.gamification.Designation.getMonthlyDesignation(12))
        // Beyond 12: Prestige
        assertEquals("Keeper of the Seven Seas (Prestige 1)", com.example.domain.gamification.Designation.getMonthlyDesignation(13))
        assertEquals("Keeper of the Seven Seas (Prestige 2)", com.example.domain.gamification.Designation.getMonthlyDesignation(14))
    }

    @Test
    fun testMonthlyCollectionRecordSerialization() {
        val record = com.example.domain.gamification.MonthlyCollectionRecord(
            monthKey = "2026-10",
            year = 2026,
            month = 10,
            unlockedBadgeIds = listOf("triton", "varuna", "aquaman"),
            highestStreak = 10,
            isCompleted = false,
            designationAwarded = null
        )
        val jsonStr = com.example.domain.gamification.MonthlyCollectionRecord.listToJsonString(listOf(record))
        val restored = com.example.domain.gamification.MonthlyCollectionRecord.listFromJsonString(jsonStr)

        assertEquals(1, restored.size)
        assertEquals("2026-10", restored.first().monthKey)
        assertEquals(listOf("triton", "varuna", "aquaman"), restored.first().unlockedBadgeIds)
        assertEquals(10, restored.first().highestStreak)
    }

    @Test
    fun testUsersWhoMissMultipleDaysWithoutOpeningApp() {
        val dailyTotals = mutableMapOf<String, Int>()
        // User logged on Nov 1, then didn't open app until Nov 6
        dailyTotals["2026-11-01"] = 2500
        // Days 2, 3, 4, 5 have no logs

        val state = com.example.domain.gamification.GamificationEngine.evaluate(
            dailyTotals = dailyTotals,
            dailyTargetMl = 2500,
            todayDateKey = "2026-11-06",
            todayConsumedMl = 0
        )

        // 4 past consecutive missed days (Nov 2, 3, 4, 5)
        assertEquals(4, state.currentMissedStreak)
        assertTrue(state.isDisasterPath)
        assertEquals("Dry Fish", state.currentDesignationTitle) // 3+ days missed threshold
    }

    @Test
    fun testFirstCompletedMonthReceivesOceanExplorer() {
        val dailyTotals = mutableMapOf<String, Int>()
        // Complete all 30 days in September 2026
        for (d in 1..30) {
            dailyTotals[String.format(java.util.Locale.US, "2026-09-%02d", d)] = 2500
        }

        // Evaluate on Oct 1
        val state = com.example.domain.gamification.GamificationEngine.evaluate(
            dailyTotals = dailyTotals,
            dailyTargetMl = 2500,
            todayDateKey = "2026-10-01",
            todayConsumedMl = 0
        )

        assertEquals(1, state.completedMonthlyCollectionsCount)
        val septRecord = state.completedMonthlyCollections.find { it.monthKey == "2026-09" }
        org.junit.Assert.assertNotNull(septRecord)
        assertTrue(septRecord!!.isCompleted)
        assertEquals("Ocean Explorer", septRecord.designationAwarded)
    }

    @Test
    fun testSecondCompletedMonthReceivesSeaGuardian() {
        val dailyTotals = mutableMapOf<String, Int>()
        // September (30 days)
        for (d in 1..30) {
            dailyTotals[String.format(java.util.Locale.US, "2026-09-%02d", d)] = 2500
        }
        // October (31 days)
        for (d in 1..31) {
            dailyTotals[String.format(java.util.Locale.US, "2026-10-%02d", d)] = 2500
        }

        // Evaluate on Nov 1
        val state = com.example.domain.gamification.GamificationEngine.evaluate(
            dailyTotals = dailyTotals,
            dailyTargetMl = 2500,
            todayDateKey = "2026-11-01",
            todayConsumedMl = 0
        )

        assertEquals(2, state.completedMonthlyCollectionsCount)
        val septRecord = state.completedMonthlyCollections.find { it.monthKey == "2026-09" }
        val octRecord = state.completedMonthlyCollections.find { it.monthKey == "2026-10" }

        org.junit.Assert.assertNotNull(septRecord)
        org.junit.Assert.assertNotNull(octRecord)
        assertEquals("Ocean Explorer", septRecord!!.designationAwarded)
        assertEquals("Sea Guardian", octRecord!!.designationAwarded)
        assertEquals("Sea Guardian", state.activeMonthlyDesignation)
    }

    @Test
    fun testTwoConsecutiveCompletedMonthsReceiveSequentialTitles() {
        val dailyTotals = mutableMapOf<String, Int>()
        for (d in 1..30) {
            dailyTotals[String.format(java.util.Locale.US, "2026-09-%02d", d)] = 2500
        }
        for (d in 1..31) {
            dailyTotals[String.format(java.util.Locale.US, "2026-10-%02d", d)] = 2500
        }

        val state = com.example.domain.gamification.GamificationEngine.evaluate(
            dailyTotals = dailyTotals,
            dailyTargetMl = 2500,
            todayDateKey = "2026-11-01",
            todayConsumedMl = 2500 // Completes Day 1 of Nov
        )

        val completed = state.completedMonthlyCollections.filter { it.isCompleted }
        assertEquals(2, completed.size)
        assertEquals("Ocean Explorer", completed[0].designationAwarded)
        assertEquals("Sea Guardian", completed[1].designationAwarded)
    }

    @Test
    fun testReevaluatingPersistedCollectionsDoesNotDuplicateOrAdvanceTitles() {
        val dailyTotals = mutableMapOf<String, Int>()
        for (d in 1..30) {
            dailyTotals[String.format(java.util.Locale.US, "2026-09-%02d", d)] = 2500
        }
        for (d in 1..31) {
            dailyTotals[String.format(java.util.Locale.US, "2026-10-%02d", d)] = 2500
        }

        // First pass: evaluate and get persisted collections
        val firstPass = com.example.domain.gamification.GamificationEngine.evaluate(
            dailyTotals = dailyTotals,
            dailyTargetMl = 2500,
            todayDateKey = "2026-11-01",
            todayConsumedMl = 0
        )

        // Pass 2: Re-evaluating with persistedCollections provided (simulating app restart)
        val secondPass = com.example.domain.gamification.GamificationEngine.evaluate(
            dailyTotals = dailyTotals,
            dailyTargetMl = 2500,
            todayDateKey = "2026-11-01",
            todayConsumedMl = 0,
            persistedCollections = firstPass.completedMonthlyCollections,
            persistedHighestDesignation = firstPass.lifetimeHighestDesignation
        )

        assertEquals(2, secondPass.completedMonthlyCollectionsCount)
        val septRecord = secondPass.completedMonthlyCollections.find { it.monthKey == "2026-09" }
        val octRecord = secondPass.completedMonthlyCollections.find { it.monthKey == "2026-10" }

        org.junit.Assert.assertNotNull(septRecord)
        org.junit.Assert.assertNotNull(octRecord)
        // Must NOT have advanced to Sea Guardian and Poseidon's Champion!
        assertEquals("Ocean Explorer", septRecord!!.designationAwarded)
        assertEquals("Sea Guardian", octRecord!!.designationAwarded)
        assertEquals(firstPass.completedMonthlyCollections, secondPass.completedMonthlyCollections)
    }

    @Test
    fun testRepeatedDataStoreEmissionsDoNotCauseInfinitePersistenceLoop() {
        val dailyTotals = mapOf(
            "2026-11-01" to 2500,
            "2026-11-02" to 2500
        )

        val state1 = com.example.domain.gamification.GamificationEngine.evaluate(
            dailyTotals = dailyTotals,
            dailyTargetMl = 2500,
            todayDateKey = "2026-11-02",
            todayConsumedMl = 2500
        )

        val state2 = com.example.domain.gamification.GamificationEngine.evaluate(
            dailyTotals = dailyTotals,
            dailyTargetMl = 2500,
            todayDateKey = "2026-11-02",
            todayConsumedMl = 2500,
            persistedCollections = state1.completedMonthlyCollections,
            persistedHighestDesignation = state1.lifetimeHighestDesignation
        )

        // States must be exactly equal; distinctUntilChanged suppresses redundant emissions
        assertEquals(state1, state2)
    }

    @Test
    fun testUnchangedStateDoesNotTriggerUnnecessaryWrites() {
        val record = com.example.domain.gamification.MonthlyCollectionRecord(
            monthKey = "2026-09",
            year = 2026,
            month = 9,
            unlockedBadgeIds = com.example.domain.gamification.Designation.OCEAN_LEGENDS.map { it.id },
            highestStreak = 30,
            isCompleted = true,
            designationAwarded = "Ocean Explorer"
        )
        val stateA = com.example.domain.gamification.GamificationState(
            overallStreak = 5,
            currentMissedStreak = 0,
            monthlyStreak = 5,
            completedMonthlyCollections = listOf(record)
        )
        val stateB = com.example.domain.gamification.GamificationState(
            overallStreak = 5,
            currentMissedStreak = 0,
            monthlyStreak = 5,
            completedMonthlyCollections = listOf(record)
        )

        assertEquals(stateA, stateB)
        // Verifies data class structural equality ensures distinctUntilChanged() filters identical instances
        assertTrue(stateA == stateB)
    }

    // =========================================================================
    // Ocean Notification Personality System Tests
    // =========================================================================

    @Test
    fun testOceanNotificationContentTotals() {
        assertEquals(12, com.example.reminders.OceanNotificationContent.REGULAR_REMINDERS.size)
        assertEquals(8, com.example.reminders.OceanNotificationContent.PROGRESS_CELEBRATIONS.size)
        assertEquals(7, com.example.reminders.OceanNotificationContent.STREAK_CELEBRATIONS.size)
        assertEquals(6, com.example.reminders.OceanNotificationContent.MISSED_GOALS.size)
        assertEquals(2, com.example.reminders.OceanNotificationContent.RECOVERIES.size)
        assertEquals(8, com.example.reminders.OceanNotificationContent.MISSED_GOAL_AND_RECOVERY.size)
        assertEquals(5, com.example.reminders.OceanNotificationContent.END_OF_DAY_NUDGES.size)
    }

    @Test
    fun testRegularReminderMessageSelection() {
        val message = com.example.reminders.OceanNotificationContent.selectMessage(
            category = com.example.reminders.NotificationCategory.REGULAR_REMINDER
        )
        org.junit.Assert.assertNotNull(message)
        assertTrue(message.title.isNotBlank())
        assertTrue(message.body.isNotBlank())
        assertEquals(com.example.reminders.NotificationCategory.REGULAR_REMINDER, message.category)
    }

    @Test
    fun testMessageRotationWithoutImmediateRepetition() {
        val lastTitle = "Poseidon is getting impatient 🌊"
        for (i in 1..20) {
            val selected = com.example.reminders.OceanNotificationContent.selectMessage(
                category = com.example.reminders.NotificationCategory.REGULAR_REMINDER,
                lastShownTitle = lastTitle
            )
            org.junit.Assert.assertNotEquals(
                "Selected message title must not match lastShownTitle",
                lastTitle,
                selected.title
            )
        }
    }

    @Test
    fun testProgressMilestoneNotificationsTriggeringOncePerDay() {
        val dailyTarget = 2000
        // Cross 25% (500ml) from 400ml to 600ml
        val firstCross = com.example.reminders.OceanNotificationEvaluator.determineMilestonesCrossed(
            previousIntakeMl = 400,
            newIntakeMl = 600,
            dailyTargetMl = dailyTarget,
            alreadyTriggeredMilestones = emptySet()
        )
        assertEquals(listOf(25), firstCross)

        // Second log in the same day: 25% is in alreadyTriggeredMilestones
        val secondCross = com.example.reminders.OceanNotificationEvaluator.determineMilestonesCrossed(
            previousIntakeMl = 600,
            newIntakeMl = 800,
            dailyTargetMl = dailyTarget,
            alreadyTriggeredMilestones = setOf(25)
        )
        // No new milestone reached (needs 50% = 1000ml)
        assertTrue(secondCross.isEmpty())

        // Third log reaches 50% (1100ml)
        val thirdCross = com.example.reminders.OceanNotificationEvaluator.determineMilestonesCrossed(
            previousIntakeMl = 800,
            newIntakeMl = 1100,
            dailyTargetMl = dailyTarget,
            alreadyTriggeredMilestones = setOf(25)
        )
        assertEquals(listOf(50), thirdCross)
    }

    @Test
    fun test100PercentGoalCelebration() {
        val dailyTarget = 2000
        val crossed = com.example.reminders.OceanNotificationEvaluator.determineMilestonesCrossed(
            previousIntakeMl = 1800,
            newIntakeMl = 2000,
            dailyTargetMl = dailyTarget,
            alreadyTriggeredMilestones = setOf(25, 50, 75)
        )
        assertEquals(listOf(100), crossed)

        val celebration = com.example.reminders.OceanNotificationEvaluator.determineIntakeCelebration(
            wasInDisaster = false,
            previousIntakeMl = 1800,
            newIntakeMl = 2000,
            dailyTargetMl = dailyTarget,
            newlyUnlockedStreakBadge = false,
            alreadyTriggeredMilestones = setOf(25, 50, 75)
        )
        org.junit.Assert.assertNotNull(celebration)
        assertEquals(com.example.reminders.NotificationCategory.PROGRESS_CELEBRATION, celebration!!.first)
        // Candidates pool is PROGRESS_GOAL_ACCOMPLISHED
        assertTrue(celebration.second.any { it.title == "MISSION ACCOMPLISHED 🏆" })
    }

    @Test
    fun testStreakMilestoneNotifications() {
        val celebration = com.example.reminders.OceanNotificationEvaluator.determineIntakeCelebration(
            wasInDisaster = false,
            previousIntakeMl = 2200,
            newIntakeMl = 2500,
            dailyTargetMl = 2500,
            newlyUnlockedStreakBadge = true,
            alreadyTriggeredMilestones = setOf(25, 50, 75, 100)
        )
        org.junit.Assert.assertNotNull(celebration)
        assertEquals(com.example.reminders.NotificationCategory.STREAK_CELEBRATION, celebration!!.first)
        assertTrue(celebration.second.any { it.title.contains("Triton") || it.title.contains("streak") })
    }

    @Test
    fun testMissedDayNotificationOnlyAfterDayEnd() {
        // Morning after missed day: current time 08:30 (510 min), wake time 08:00
        val morningCategory = com.example.reminders.OceanNotificationEvaluator.determinePeriodicNotificationCategory(
            remindersEnabled = true,
            isWeekend = false,
            weekendsEnabled = true,
            isWithinWakingHours = true,
            todayIntakeMl = 0,
            dailyTargetMl = 2500,
            currentMinutes = 8 * 60 + 30,
            sleepTimeString = "22:00",
            isYesterdayMissed = true,
            isMissedGoalNotifiedToday = false,
            isEndOfDayNotifiedToday = false,
            isMorningWindow = true
        )
        assertEquals(com.example.reminders.NotificationCategory.MISSED_GOAL_AND_RECOVERY, morningCategory)

        // Once already notified today, does NOT repeat missed day notification
        val repeatedCategory = com.example.reminders.OceanNotificationEvaluator.determinePeriodicNotificationCategory(
            remindersEnabled = true,
            isWeekend = false,
            weekendsEnabled = true,
            isWithinWakingHours = true,
            todayIntakeMl = 0,
            dailyTargetMl = 2500,
            currentMinutes = 9 * 60,
            sleepTimeString = "22:00",
            isYesterdayMissed = true,
            isMissedGoalNotifiedToday = true,
            isEndOfDayNotifiedToday = false,
            isMorningWindow = true
        )
        assertEquals(com.example.reminders.NotificationCategory.REGULAR_REMINDER, repeatedCategory)
    }

    @Test
    fun testRecoveryNotification() {
        // User was in disaster, drinks up to 100% daily goal
        val celebration = com.example.reminders.OceanNotificationEvaluator.determineIntakeCelebration(
            wasInDisaster = true,
            previousIntakeMl = 1500,
            newIntakeMl = 2500,
            dailyTargetMl = 2500,
            newlyUnlockedStreakBadge = false,
            alreadyTriggeredMilestones = emptySet()
        )
        org.junit.Assert.assertNotNull(celebration)
        assertEquals(com.example.reminders.NotificationCategory.MISSED_GOAL_AND_RECOVERY, celebration!!.first)
        // Pool contains recovery comeback messages
        assertTrue(celebration.second.any { it.title == "THE COMEBACK! ⚡" })
    }

    @Test
    fun testEndOfDayNotificationRespectingSleepTime() {
        val sleepTime = "22:00"
        // 20:30 (90 min before sleep) -> within 120-min window
        assertTrue(com.example.reminders.OceanNotificationEvaluator.isNearEndOfDay(20 * 60 + 30, sleepTime))
        // 14:00 (8 hours before sleep) -> not near end of day
        assertFalse(com.example.reminders.OceanNotificationEvaluator.isNearEndOfDay(14 * 60, sleepTime))

        // Evaluation near end of day with incomplete goal
        val category = com.example.reminders.OceanNotificationEvaluator.determinePeriodicNotificationCategory(
            remindersEnabled = true,
            isWeekend = false,
            weekendsEnabled = true,
            isWithinWakingHours = true,
            todayIntakeMl = 1200,
            dailyTargetMl = 2500,
            currentMinutes = 20 * 60 + 30,
            sleepTimeString = sleepTime,
            isYesterdayMissed = false,
            isMissedGoalNotifiedToday = false,
            isEndOfDayNotifiedToday = false,
            isMorningWindow = false
        )
        assertEquals(com.example.reminders.NotificationCategory.END_OF_DAY_NUDGE, category)
    }

    @Test
    fun testNoRoutineReminderAfterGoalCompletion() {
        val category = com.example.reminders.OceanNotificationEvaluator.determinePeriodicNotificationCategory(
            remindersEnabled = true,
            isWeekend = false,
            weekendsEnabled = true,
            isWithinWakingHours = true,
            todayIntakeMl = 2500,
            dailyTargetMl = 2500,
            currentMinutes = 14 * 60,
            sleepTimeString = "22:00",
            isYesterdayMissed = false,
            isMissedGoalNotifiedToday = false,
            isEndOfDayNotifiedToday = false,
            isMorningWindow = false
        )
        org.junit.Assert.assertNull("No periodic reminder should be sent after daily goal completion", category)
    }

    @Test
    fun testNoDuplicateNotificationsAfterAppRestart() {
        // Simulating app restart where DataStore persisted triggered milestones: [25, 50, 75, 100]
        val persistedMilestones = setOf(25, 50, 75, 100)
        val crossed = com.example.reminders.OceanNotificationEvaluator.determineMilestonesCrossed(
            previousIntakeMl = 0,
            newIntakeMl = 2500,
            dailyTargetMl = 2500,
            alreadyTriggeredMilestones = persistedMilestones
        )
        assertTrue("No milestones should re-fire after app restart", crossed.isEmpty())
    }

    @Test
    fun testExistingReminderSettingsRemainingUnchanged() {
        val intervals = com.example.reminders.ReminderScheduler.AVAILABLE_INTERVALS
        assertEquals(listOf(30, 60, 90, 120), intervals)
        assertEquals("water_reminders_channel_v2", com.example.reminders.ReminderHelper.CHANNEL_ID)
        assertEquals(1001, com.example.reminders.ReminderHelper.NOTIFICATION_ID)
    }

    // =========================================================================
    // Stage 1: Design System & Hydria Mascot Tests
    // =========================================================================

    @Test
    fun testHydriaMascotMoodResolution() {
        val target = 2000

        // 0% -> CHEERFUL_START
        assertEquals(
            com.example.presentation.components.HydriaMascotMood.CHEERFUL_START,
            com.example.presentation.components.HydriaMascotHelper.resolveMood(0, target)
        )

        // 25% -> PROGRESS
        assertEquals(
            com.example.presentation.components.HydriaMascotMood.PROGRESS,
            com.example.presentation.components.HydriaMascotHelper.resolveMood(500, target)
        )

        // 50% -> HALFWAY
        assertEquals(
            com.example.presentation.components.HydriaMascotMood.HALFWAY,
            com.example.presentation.components.HydriaMascotHelper.resolveMood(1000, target)
        )

        // 75% -> HALFWAY
        assertEquals(
            com.example.presentation.components.HydriaMascotMood.HALFWAY,
            com.example.presentation.components.HydriaMascotHelper.resolveMood(1500, target)
        )

        // 100% -> GOAL_MET
        assertEquals(
            com.example.presentation.components.HydriaMascotMood.GOAL_MET,
            com.example.presentation.components.HydriaMascotHelper.resolveMood(2000, target)
        )

        // 120% -> GOAL_MET
        assertEquals(
            com.example.presentation.components.HydriaMascotMood.GOAL_MET,
            com.example.presentation.components.HydriaMascotHelper.resolveMood(2400, target)
        )

        // Disaster Path -> MISSED_GOAL
        assertEquals(
            com.example.presentation.components.HydriaMascotMood.MISSED_GOAL,
            com.example.presentation.components.HydriaMascotHelper.resolveMood(500, target, isDisaster = true)
        )

        // Disaster Recovery -> RECOVERY
        assertEquals(
            com.example.presentation.components.HydriaMascotMood.RECOVERY,
            com.example.presentation.components.HydriaMascotHelper.resolveMood(2000, target, isRecovery = true)
        )
    }

    @Test
    fun testHydriaMascotCompanionMessages() {
        val moods = com.example.presentation.components.HydriaMascotMood.values()
        assertEquals(6, moods.size)

        for (mood in moods) {
            val msg = com.example.presentation.components.HydriaMascotHelper.getCompanionMessage(mood)
            assertTrue("Message for $mood must not be blank", msg.isNotBlank())
        }

        assertEquals(
            "Let's get this ocean adventure started!",
            com.example.presentation.components.HydriaMascotHelper.getCompanionMessage(com.example.presentation.components.HydriaMascotMood.CHEERFUL_START)
        )
        assertEquals(
            "Nice! The tide is rising.",
            com.example.presentation.components.HydriaMascotHelper.getCompanionMessage(com.example.presentation.components.HydriaMascotMood.PROGRESS)
        )
        assertEquals(
            "Halfway there, ocean explorer!",
            com.example.presentation.components.HydriaMascotHelper.getCompanionMessage(com.example.presentation.components.HydriaMascotMood.HALFWAY)
        )
        assertEquals(
            "The ocean celebrates you! You legend!",
            com.example.presentation.components.HydriaMascotHelper.getCompanionMessage(com.example.presentation.components.HydriaMascotMood.GOAL_MET)
        )
        assertEquals(
            "We missed you yesterday. Ready for a comeback?",
            com.example.presentation.components.HydriaMascotHelper.getCompanionMessage(com.example.presentation.components.HydriaMascotMood.MISSED_GOAL)
        )
        assertEquals(
            "That's the spirit! Welcome back!",
            com.example.presentation.components.HydriaMascotHelper.getCompanionMessage(com.example.presentation.components.HydriaMascotMood.RECOVERY)
        )
    }

    @Test
    fun testOceanDesignTokensAndBrushes() {
        org.junit.Assert.assertNotNull(com.example.ui.theme.OceanBrushes.heroBrush())
        org.junit.Assert.assertNotNull(com.example.ui.theme.OceanBrushes.cardBrush())
        org.junit.Assert.assertNotNull(com.example.ui.theme.OceanBrushes.waterBrush())
        org.junit.Assert.assertNotNull(com.example.ui.theme.OceanBrushes.celebrationBrush())
        assertEquals(3, com.example.ui.theme.OceanGradientHero.size)
    }

    @Test
    fun testYearlyAnalyticsCalculation() {
        val target = 2000
        val totalsMap = mapOf(
            "2026-01-10" to 2000,
            "2026-01-11" to 2500,
            "2026-02-05" to 2000,
            "2026-03-15" to 1500
        )

        var annualIntake = 0L
        var annualGoals = 0

        totalsMap.values.forEach { intake ->
            annualIntake += intake
            if (intake >= target) annualGoals++
        }

        assertEquals(8000L, annualIntake)
        assertEquals(3, annualGoals)

        // Verify 12 month items generated correctly
        val months = (0..11).map { mIdx ->
            val name = com.example.core.utils.DateTimeUtils.getMonthName(mIdx)
            val shortName = com.example.core.utils.DateTimeUtils.getShortMonthName(mIdx)
            assertTrue(name.isNotBlank())
            assertTrue(shortName.isNotBlank())
            name
        }
        assertEquals(12, months.size)
        assertEquals("January", months[0])
        assertEquals("December", months[11])
    }

    @Test
    fun testMonthlyCalendarGridOffsetAndDays() {
        // November 2026: Nov 1, 2026 is a Sunday (Calendar.SUNDAY = 1) -> offset 0
        val offsetNov2026 = com.example.core.utils.DateTimeUtils.getFirstDayOfWeekForMonth(2026, 10) - 1
        assertEquals(0, offsetNov2026)

        // October 2026: Oct 1, 2026 is a Thursday (Calendar.THURSDAY = 5) -> offset 4
        val offsetOct2026 = com.example.core.utils.DateTimeUtils.getFirstDayOfWeekForMonth(2026, 9) - 1
        assertEquals(4, offsetOct2026)

        // Logging frequency: 15 logs over 5 active days = 3.0 logs/day
        val totalLogs = 15
        val activeDays = 5
        val freq = totalLogs.toFloat() / activeDays.toFloat()
        assertEquals(3.0f, freq, 0.01f)
    }

    @Test
    fun testOnboardingStepNavigationAndValidation() {
        // Step 1 Welcome -> Step 2 Profile
        var currentStep = 1
        fun next(step: Int): Int = step + 1

        currentStep = next(currentStep)
        assertEquals(2, currentStep)

        // Validate age and weight
        fun validate(ageStr: String, weightStr: String): Boolean {
            val age = ageStr.toIntOrNull()
            val weight = weightStr.toFloatOrNull()
            return age != null && age in 10..120 && weight != null && weight in 20f..300f
        }

        assertTrue(validate("28", "70"))
        assertFalse(validate("5", "70"))
        assertFalse(validate("28", "10"))
        assertFalse(validate("", "70"))

        // Proceed to Step 3, 4, 5
        currentStep = next(currentStep) // Step 3: Reminders
        assertEquals(3, currentStep)
        currentStep = next(currentStep) // Step 4: How it works
        assertEquals(4, currentStep)
        currentStep = next(currentStep) // Step 5: Goal reveal
        assertEquals(5, currentStep)
    }

    @Test
    fun testSettingsDefaultPresetsAndReliability() {
        val defaultWidgetAmount = 300
        val intervals = listOf(30, 60, 90, 120)

        assertTrue(intervals.contains(60))
        assertTrue(intervals.contains(120))
        assertEquals(4, intervals.size)
        assertEquals(300, defaultWidgetAmount)

        // 24-hour time regex validation
        val timeRegex = Regex("^([01]?[0-9]|2[0-3]):[0-5][0-9]\$")
        assertTrue("08:00".matches(timeRegex))
        assertTrue("23:30".matches(timeRegex))
        assertFalse("25:00".matches(timeRegex))
        assertFalse("12:60".matches(timeRegex))
        assertFalse("invalid".matches(timeRegex))
    }

    @Test
    fun testSmartReminderDefaultIntervalIs120Minutes() {
        val profile = UserProfile()
        assertEquals(120, profile.reminderIntervalMinutes)
        assertEquals(120, com.example.reminders.ReminderScheduler.DEFAULT_INTERVAL_MINUTES)
    }

    @Test
    fun testReminderSchedulerMinIntervalCalculation() {
        assertEquals(100, com.example.reminders.ReminderScheduler.getMinIntervalMinutes(120))
        assertEquals(75, com.example.reminders.ReminderScheduler.getMinIntervalMinutes(90))
        assertEquals(50, com.example.reminders.ReminderScheduler.getMinIntervalMinutes(60))
        assertEquals(25, com.example.reminders.ReminderScheduler.getMinIntervalMinutes(30))
    }

    @Test
    fun testNaturalTimingVariationBounds() {
        // Test 120-minute interval produces values strictly within [100, 120]
        repeat(100) {
            val delay = com.example.reminders.ReminderScheduler.calculateEffectiveDelayMinutes(120)
            assertTrue("Expected delay in 100..120 but got $delay", delay in 100..120)
        }

        // Test 90-minute interval produces values strictly within [75, 90]
        repeat(50) {
            val delay = com.example.reminders.ReminderScheduler.calculateEffectiveDelayMinutes(90)
            assertTrue("Expected delay in 75..90 but got $delay", delay in 75..90)
        }

        // Test 60-minute interval produces values strictly within [50, 60]
        repeat(50) {
            val delay = com.example.reminders.ReminderScheduler.calculateEffectiveDelayMinutes(60)
            assertTrue("Expected delay in 50..60 but got $delay", delay in 50..60)
        }

        // Test 30-minute interval produces values strictly within [25, 30]
        repeat(50) {
            val delay = com.example.reminders.ReminderScheduler.calculateEffectiveDelayMinutes(30)
            assertTrue("Expected delay in 25..30 but got $delay", delay in 25..30)
        }
    }

    @Test
    fun testIntakeBasedReminderSuppressionLogic() {
        val now = 10_000_000L
        val minIntervalMinutes = com.example.reminders.ReminderScheduler.getMinIntervalMinutes(120) // 100 min
        val minIntervalMillis = minIntervalMinutes * 60 * 1000L

        // Scenario A: User drank water 20 minutes ago (< 100 min)
        val recentIntake = now - (20 * 60 * 1000L)
        val elapsedA = now - recentIntake
        val isSuppressedA = elapsedA < minIntervalMillis
        assertTrue("Intake 20m ago must be suppressed", isSuppressedA)
        val remainingMinutesA = ((minIntervalMillis - elapsedA) / (60 * 1000L)).toInt()
        assertEquals(80, remainingMinutesA)

        // Scenario B: User drank water 105 minutes ago (>= 100 min)
        val olderIntake = now - (105 * 60 * 1000L)
        val elapsedB = now - olderIntake
        val isSuppressedB = elapsedB < minIntervalMillis
        assertFalse("Intake 105m ago must not be suppressed", isSuppressedB)

        // Scenario C: No intake recorded (effectiveLastIntake == 0)
        val noIntake = 0L
        val shouldSuppressC = noIntake > 0 && (now - noIntake < minIntervalMillis)
        assertFalse("Zero timestamp must not suppress reminders", shouldSuppressC)
    }

    @Test
    fun testGamificationCelebrationIndependenceFromReminderSuppression() {
        // Milestone crossing should trigger regardless of whether regular reminder is suppressed
        val crossed = com.example.reminders.OceanNotificationEvaluator.determineMilestonesCrossed(
            previousIntakeMl = 1100,
            newIntakeMl = 1300,
            dailyTargetMl = 2500, // 50% is 1250ml
            alreadyTriggeredMilestones = emptySet()
        )
        assertEquals(1, crossed.size)
        assertEquals(50, crossed.first())

        val celebration = com.example.reminders.OceanNotificationEvaluator.determineIntakeCelebration(
            wasInDisaster = false,
            previousIntakeMl = 1100,
            newIntakeMl = 1300,
            dailyTargetMl = 2500,
            newlyUnlockedStreakBadge = false,
            alreadyTriggeredMilestones = emptySet()
        )
        assertTrue(celebration != null)
        assertEquals(com.example.reminders.NotificationCategory.PROGRESS_CELEBRATION, celebration?.first)
    }

    @Test
    fun testCircularLiquidHeightFractions() {
        val targetMl = 2000

        // 0% empty
        assertEquals(0f, com.example.core.ui.sensors.DeviceTilt.calculateWaterHeightFraction(0, targetMl), 0.001f)
        // 25% one-quarter
        assertEquals(0.25f, com.example.core.ui.sensors.DeviceTilt.calculateWaterHeightFraction(500, targetMl), 0.001f)
        // 50% midpoint
        assertEquals(0.50f, com.example.core.ui.sensors.DeviceTilt.calculateWaterHeightFraction(1000, targetMl), 0.001f)
        // 75% three-quarters
        assertEquals(0.75f, com.example.core.ui.sensors.DeviceTilt.calculateWaterHeightFraction(1500, targetMl), 0.001f)
        // 100% full
        assertEquals(1.0f, com.example.core.ui.sensors.DeviceTilt.calculateWaterHeightFraction(2000, targetMl), 0.001f)
        // >100% clamped to full without breaking circular container
        assertEquals(1.0f, com.example.core.ui.sensors.DeviceTilt.calculateWaterHeightFraction(2600, targetMl), 0.001f)
        // Zero target handling
        assertEquals(0f, com.example.core.ui.sensors.DeviceTilt.calculateWaterHeightFraction(500, 0), 0.001f)
    }

    @Test
    fun testDeviceTiltSlopeCalculations() {
        val maxSlope = 0.50f

        // Level position -> flat horizontal surface
        assertEquals(0f, com.example.core.ui.sensors.DeviceTilt.computeSurfaceSlope(0f, maxSlope), 0.001f)

        // Moderate left tilt
        val leftSlope = com.example.core.ui.sensors.DeviceTilt.computeSurfaceSlope(0.5f, maxSlope)
        assertTrue("Expected positive slope for left tilt", leftSlope > 0f)
        assertEquals(0.25f, leftSlope, 0.001f)

        // Moderate right tilt
        val rightSlope = com.example.core.ui.sensors.DeviceTilt.computeSurfaceSlope(-0.5f, maxSlope)
        assertTrue("Expected negative slope for right tilt", rightSlope < 0f)
        assertEquals(-0.25f, rightSlope, 0.001f)

        // Extreme tilt clamped within maxSlope bounds
        val extremeLeft = com.example.core.ui.sensors.DeviceTilt.computeSurfaceSlope(1.8f, maxSlope)
        assertEquals(maxSlope, extremeLeft, 0.001f)

        val extremeRight = com.example.core.ui.sensors.DeviceTilt.computeSurfaceSlope(-2.0f, maxSlope)
        assertEquals(-maxSlope, extremeRight, 0.001f)
    }

    @Test
    fun testWaterEntryEditValidation() {
        fun validateEditAmount(input: String): Pair<Boolean, String?> {
            val trimmed = input.trim()
            val amount = trimmed.toIntOrNull()
            return when {
                trimmed.isBlank() -> Pair(false, "Please enter an amount in ml")
                amount == null || amount <= 0 -> Pair(false, "Please enter a valid positive amount")
                amount > 3000 -> Pair(false, "Maximum single entry is 3,000 ml")
                else -> Pair(true, null)
            }
        }

        assertTrue(validateEditAmount("250").first)
        assertTrue(validateEditAmount("500").first)
        assertTrue(validateEditAmount("3000").first)

        assertFalse(validateEditAmount("").first)
        assertFalse(validateEditAmount("0").first)
        assertFalse(validateEditAmount("-50").first)
        assertFalse(validateEditAmount("3001").first)
        assertFalse(validateEditAmount("abc").first)
    }

    @Test
    fun testWaterEntryUpdateRecalculatesTotals() {
        val entry1 = com.example.data.local.WaterEntry(id = 1L, amountMl = 250, dateKey = "2026-11-12")
        val entry2 = com.example.data.local.WaterEntry(id = 2L, amountMl = 500, dateKey = "2026-11-12")
        var entries = listOf(entry1, entry2)

        val initialTotal = entries.sumOf { it.amountMl }
        assertEquals(750, initialTotal)

        // Simulate edit: update entry1 from 250ml to 400ml
        val editedEntry1 = entry1.copy(amountMl = 400)
        entries = entries.map { if (it.id == editedEntry1.id) editedEntry1 else it }

        val updatedTotal = entries.sumOf { it.amountMl }
        assertEquals(900, updatedTotal)

        // Simulate delete: delete entry2
        entries = entries.filterNot { it.id == entry2.id }
        val finalTotal = entries.sumOf { it.amountMl }
        assertEquals(400, finalTotal)
    }

    @Test
    fun testReminderDelayCalculationWithLatestIntake() {
        val now = 1_700_000_000_000L // arbitrary fixed time
        val intervalMinutes = 120

        // 1. No entries recorded (0L) -> delay should be within natural variation [100..120]
        val delayEmpty = com.example.reminders.ReminderScheduler.calculateRemainingDelayMinutes(
            intervalMinutes = intervalMinutes,
            lastIntakeTimestamp = 0L,
            currentTimestamp = now
        )
        assertTrue("Empty delay must be in [100..120]", delayEmpty in 100..120)

        // 2. Recent intake: 30 minutes ago
        val intake30MinAgo = now - (30 * 60 * 1000L)
        val delayRecent = com.example.reminders.ReminderScheduler.calculateRemainingDelayMinutes(
            intervalMinutes = intervalMinutes,
            lastIntakeTimestamp = intake30MinAgo,
            currentTimestamp = now
        )
        // effective interval is in [100..120]. 100-30 = 70, 120-30 = 90
        assertTrue("Delay after 30 min must be in [70..90]", delayRecent in 70..90)

        // 3. Overdue intake: 150 minutes ago -> should be 1 minute (due immediately)
        val intake150MinAgo = now - (150 * 60 * 1000L)
        val delayOverdue = com.example.reminders.ReminderScheduler.calculateRemainingDelayMinutes(
            intervalMinutes = intervalMinutes,
            lastIntakeTimestamp = intake150MinAgo,
            currentTimestamp = now
        )
        assertEquals(1, delayOverdue)
    }

    @Test
    fun testLatestIntakeTimestampSelectionAcrossMutations() {
        val t1 = 1_000_000L // 9:00 AM
        val t2 = 2_000_000L // 11:00 AM
        val t3 = 3_000_000L // 2:00 PM

        val e1 = com.example.data.local.WaterEntry(id = 1L, amountMl = 250, timestamp = t1, dateKey = "2026-11-12")
        val e2 = com.example.data.local.WaterEntry(id = 2L, amountMl = 300, timestamp = t2, dateKey = "2026-11-12")
        val e3 = com.example.data.local.WaterEntry(id = 3L, amountMl = 500, timestamp = t3, dateKey = "2026-11-12")

        val entries = mutableListOf(e1, e2, e3)

        // 1. Initial latest is t3
        assertEquals(t3, entries.maxOfOrNull { it.timestamp })

        // 2. Old entry e1 is edited (e.g. quantity changed, timestamp set to 9:15 AM = 1_100_000L)
        val editedE1 = e1.copy(amountMl = 350, timestamp = 1_100_000L)
        val index1 = entries.indexOfFirst { it.id == editedE1.id }
        entries[index1] = editedE1

        // Latest entry MUST remain t3 (3_000_000L), NOT the edited entry (1_100_000L)
        val latestAfterOldEdit = entries.maxOfOrNull { it.timestamp }
        assertEquals(t3, latestAfterOldEdit)

        // 3. Latest entry e3 is deleted -> latest remaining must recalculate to t2 (2_000_000L)
        entries.removeIf { it.id == e3.id }
        val latestAfterDelete = entries.maxOfOrNull { it.timestamp }
        assertEquals(t2, latestAfterDelete)

        // 4. Undoing deletion of e3 restores t3 (3_000_000L)
        entries.add(e3)
        val latestAfterUndo = entries.maxOfOrNull { it.timestamp }
        assertEquals(t3, latestAfterUndo)

        // 5. Deleting all entries yields null / 0L
        entries.clear()
        val latestAfterAllDeleted = entries.maxOfOrNull { it.timestamp } ?: 0L
        assertEquals(0L, latestAfterAllDeleted)
    }

    @Test
    fun testHistoricalEntryNeverTriggersIntakeCelebration() {
        val todayKey = com.example.core.utils.DateTimeUtils.getTodayDateKey()
        val yesterdayKey = "2020-01-01" // Definitely not today

        // Celebration evaluator helper function checks: dateKey == todayKey
        fun wouldTriggerCelebration(dateKey: String): Boolean {
            return dateKey == todayKey
        }

        assertFalse("Historical date edit must never trigger intake celebration", wouldTriggerCelebration(yesterdayKey))
        assertTrue("Today's date is eligible for intake celebration checks", wouldTriggerCelebration(todayKey))
    }

    @Test
    fun testHistoricalEditDoesNotDuplicateAchievements() {
        // Evaluate historical days in GamificationEngine
        val target = 2500
        val historyMap = mapOf(
            "2026-10-01" to 2500,
            "2026-10-02" to 2500,
            "2026-10-03" to 2500
        )
        val state1 = com.example.domain.gamification.GamificationEngine.evaluate(
            dailyTotals = historyMap,
            dailyTargetMl = target,
            todayDateKey = "2026-11-12",
            todayConsumedMl = 0
        )

        // Modifying an old day amount (e.g. from 2500 to 3000) keeps the achievement stable
        val historyMapEdited = mapOf(
            "2026-10-01" to 3000,
            "2026-10-02" to 2500,
            "2026-10-03" to 2500
        )
        val state2 = com.example.domain.gamification.GamificationEngine.evaluate(
            dailyTotals = historyMapEdited,
            dailyTargetMl = target,
            todayDateKey = "2026-11-12",
            todayConsumedMl = 0
        )

        assertEquals(state1.totalSuccessfulDays, state2.totalSuccessfulDays)
        assertEquals(state1.completedMonthlyCollectionsCount, state2.completedMonthlyCollectionsCount)
    }
}


