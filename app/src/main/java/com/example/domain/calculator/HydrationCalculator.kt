package com.example.domain.calculator

import com.example.core.constants.AppConstants
import com.example.domain.model.ActivityLevel
import com.example.domain.model.Climate
import com.example.domain.model.UserProfile
import kotlin.math.roundToInt

object HydrationCalculator {

    /**
     * Estimates daily water intake target in milliliters:
     * - Baseline: 35 ml per kg of body weight
     * - Activity Multiplier: Sedentary (1.0x), Light (1.15x), Moderate (1.25x), Very Active (1.4x)
     * - Exercise: ~10 ml per minute of recorded workout
     * - Climate: extra allowance for warm (+250 ml) or hot/humid (+500 ml)
     * - Rounded to nearest 50 ml, clamped between 1,200 ml and 5,000 ml.
     */
    fun calculateDailyTargetMl(profile: UserProfile): Int {
        val baseIntake = profile.weightKg * 35f
        val activityAdjusted = baseIntake * profile.activityLevel.multiplier
        val exerciseAllowance = profile.exerciseMinutes * 10f
        val climateAllowance = profile.climate.extraMl.toFloat()

        val total = activityAdjusted + exerciseAllowance + climateAllowance
        val rounded = ((total / 50f).roundToInt() * 50)

        return rounded.coerceIn(AppConstants.MIN_DAILY_TARGET_ML, AppConstants.MAX_DAILY_TARGET_ML)
    }

    fun calculateDailyTargetMl(
        weightKg: Float,
        age: Int,
        activityLevel: ActivityLevel,
        exerciseMinutes: Int = 0,
        climate: Climate = Climate.TEMPERATE
    ): Int {
        val profile = UserProfile(
            age = age,
            weightKg = weightKg,
            activityLevel = activityLevel,
            exerciseMinutes = exerciseMinutes,
            climate = climate
        )
        return calculateDailyTargetMl(profile)
    }
}
