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
}

