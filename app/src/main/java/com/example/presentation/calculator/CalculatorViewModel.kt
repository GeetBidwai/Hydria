package com.example.presentation.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.constants.AppConstants
import com.example.data.repository.WaterRepository
import com.example.domain.calculator.HydrationCalculator
import com.example.domain.model.ActivityLevel
import com.example.domain.model.Climate
import com.example.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class CalculationBreakdown(
    val baseMl: Int = 2450,
    val activityMultiplier: Float = 1.25f,
    val activityAdjustedMl: Int = 3060,
    val exerciseAllowanceMl: Int = 300,
    val climateAllowanceMl: Int = 0,
    val recommendedTotalMl: Int = 3350
)

data class CalculatorUiState(
    val weightKg: String = "70",
    val age: String = "28",
    val activityLevel: ActivityLevel = ActivityLevel.MODERATE,
    val climate: Climate = Climate.TEMPERATE,
    val exerciseMinutes: String = "30",
    val isManualOverride: Boolean = false,
    val manualTargetMl: String = "2500",
    val calculatedTargetMl: Int = 2500,
    val effectiveTargetMl: Int = 2500,
    val preferredUnit: String = "ml",
    val breakdown: CalculationBreakdown = CalculationBreakdown(),
    val weightError: String? = null,
    val ageError: String? = null,
    val exerciseError: String? = null,
    val manualTargetError: String? = null,
    val isSavedSuccess: Boolean = false
)

class CalculatorViewModel(
    private val repository: WaterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val profile = repository.userProfile.first()
            val savedTarget = repository.dailyTargetMl.first()
            val unit = repository.preferredUnit.first()

            val weightStr = if (profile.weightKg % 1f == 0f) profile.weightKg.toInt().toString() else profile.weightKg.toString()
            val isCustomOverride = savedTarget != HydrationCalculator.calculateDailyTargetMl(profile)

            _uiState.update {
                it.copy(
                    weightKg = weightStr,
                    age = profile.age.toString(),
                    activityLevel = profile.activityLevel,
                    climate = profile.climate,
                    exerciseMinutes = profile.exerciseMinutes.toString(),
                    isManualOverride = isCustomOverride,
                    manualTargetMl = savedTarget.toString(),
                    preferredUnit = unit
                )
            }
            recompute()
        }
    }

    fun updateWeight(weight: String) {
        val sanitized = weight.filter { it.isDigit() || it == '.' }.take(5)
        _uiState.update { it.copy(weightKg = sanitized, weightError = null) }
        recompute()
    }

    fun updateAge(age: String) {
        val filtered = age.filter { it.isDigit() }.take(3)
        _uiState.update { it.copy(age = filtered, ageError = null) }
        recompute()
    }

    fun updateActivityLevel(level: ActivityLevel) {
        _uiState.update { it.copy(activityLevel = level) }
        recompute()
    }

    fun updateClimate(climate: Climate) {
        _uiState.update { it.copy(climate = climate) }
        recompute()
    }

    fun updateExerciseMinutes(minutes: String) {
        val filtered = minutes.filter { it.isDigit() }.take(3)
        _uiState.update { it.copy(exerciseMinutes = filtered, exerciseError = null) }
        recompute()
    }

    fun toggleManualOverride(enabled: Boolean) {
        _uiState.update {
            it.copy(
                isManualOverride = enabled,
                manualTargetMl = if (enabled && it.manualTargetMl.isBlank()) it.calculatedTargetMl.toString() else it.manualTargetMl
            )
        }
        recompute()
    }

    fun updateManualTarget(target: String) {
        val filtered = target.filter { it.isDigit() }.take(5)
        _uiState.update { it.copy(manualTargetMl = filtered, manualTargetError = null) }
        recompute()
    }

    fun resetToCalculated() {
        _uiState.update {
            it.copy(
                isManualOverride = false,
                manualTargetMl = it.calculatedTargetMl.toString(),
                manualTargetError = null
            )
        }
        recompute()
    }

    private fun recompute() {
        val state = _uiState.value
        val weight = state.weightKg.toFloatOrNull() ?: 70f
        val exercise = state.exerciseMinutes.toIntOrNull() ?: 0

        val baseMl = (weight * 35f).roundToInt()
        val activityAdjustedMl = (baseMl * state.activityLevel.multiplier).roundToInt()
        val exerciseAllowanceMl = (exercise * 10f).roundToInt()
        val climateAllowanceMl = state.climate.extraMl

        val rawTotal = activityAdjustedMl + exerciseAllowanceMl + climateAllowanceMl
        val roundedCalculated = ((rawTotal / 50f).roundToInt() * 50)
            .coerceIn(AppConstants.MIN_DAILY_TARGET_ML, AppConstants.MAX_DAILY_TARGET_ML)

        val breakdown = CalculationBreakdown(
            baseMl = baseMl,
            activityMultiplier = state.activityLevel.multiplier,
            activityAdjustedMl = activityAdjustedMl,
            exerciseAllowanceMl = exerciseAllowanceMl,
            climateAllowanceMl = climateAllowanceMl,
            recommendedTotalMl = roundedCalculated
        )

        val effective = if (state.isManualOverride) {
            state.manualTargetMl.toIntOrNull() ?: roundedCalculated
        } else {
            roundedCalculated
        }

        _uiState.update {
            it.copy(
                calculatedTargetMl = roundedCalculated,
                effectiveTargetMl = effective,
                breakdown = breakdown
            )
        }
    }

    fun validate(): Boolean {
        var valid = true
        val state = _uiState.value

        val weightFloat = state.weightKg.toFloatOrNull()
        val weightErr = when {
            state.weightKg.isBlank() -> "Weight is required"
            weightFloat == null || weightFloat < 20f || weightFloat > 300f -> "Weight must be between 20 and 300 kg"
            else -> null
        }
        if (weightErr != null) valid = false

        val ageInt = state.age.toIntOrNull()
        val ageErr = when {
            state.age.isBlank() -> "Age is required"
            ageInt == null || ageInt < 10 || ageInt > 120 -> "Age must be between 10 and 120"
            else -> null
        }
        if (ageErr != null) valid = false

        val exerciseInt = state.exerciseMinutes.toIntOrNull()
        val exerciseErr = when {
            state.exerciseMinutes.isNotBlank() && (exerciseInt == null || exerciseInt < 0 || exerciseInt > 360) ->
                "Exercise must be between 0 and 360 minutes"
            else -> null
        }
        if (exerciseErr != null) valid = false

        var manualErr: String? = null
        if (state.isManualOverride) {
            val manualInt = state.manualTargetMl.toIntOrNull()
            manualErr = when {
                state.manualTargetMl.isBlank() -> "Manual target is required"
                manualInt == null || manualInt < 1000 || manualInt > 6000 -> "Target must be between 1,000 and 6,000 ml"
                else -> null
            }
            if (manualErr != null) valid = false
        }

        _uiState.update {
            it.copy(
                weightError = weightErr,
                ageError = ageErr,
                exerciseError = exerciseErr,
                manualTargetError = manualErr
            )
        }

        return valid
    }

    fun applyAndSave(onComplete: () -> Unit) {
        if (!validate()) return

        viewModelScope.launch {
            val state = _uiState.value
            val currentProfile = repository.userProfile.first()

            val updatedProfile = currentProfile.copy(
                weightKg = state.weightKg.toFloatOrNull() ?: currentProfile.weightKg,
                age = state.age.toIntOrNull() ?: currentProfile.age,
                activityLevel = state.activityLevel,
                climate = state.climate,
                exerciseMinutes = state.exerciseMinutes.toIntOrNull() ?: currentProfile.exerciseMinutes
            )

            // Save updated factors and effective daily target to DataStore
            repository.saveProfile(updatedProfile, calculatedTargetMl = state.effectiveTargetMl)
            repository.setDailyTargetMl(state.effectiveTargetMl)

            _uiState.update { it.copy(isSavedSuccess = true) }
            onComplete()
        }
    }

    class Factory(private val repository: WaterRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CalculatorViewModel(repository) as T
        }
    }
}
