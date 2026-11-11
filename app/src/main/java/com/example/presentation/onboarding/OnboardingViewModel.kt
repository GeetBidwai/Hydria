package com.example.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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

data class OnboardingUiState(
    val currentStep: Int = 1, // 1 to 5
    val name: String = "",
    val age: String = "28",
    val weightKg: String = "70",
    val activityLevel: ActivityLevel = ActivityLevel.MODERATE,
    val exerciseMinutes: String = "30",
    val preferredUnit: String = "ml", // "ml" or "litres"
    val remindersEnabled: Boolean = true,
    val reminderIntervalMinutes: Int = 120,
    val reminderWeekendsEnabled: Boolean = true,
    val wakeTime: String = "07:00",
    val sleepTime: String = "23:00",
    val ageError: String? = null,
    val weightError: String? = null,
    val exerciseError: String? = null,
    val calculatedTargetMl: Int = 2500,
    val isSaving: Boolean = false,
    val isEditMode: Boolean = false
)

class OnboardingViewModel(
    private val repository: WaterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        // Load initial profile if exists
        viewModelScope.launch {
            repository.userProfile.collect { profile ->
                val target = HydrationCalculator.calculateDailyTargetMl(profile)
                val interval = try { repository.reminderIntervalMinutes.first() } catch (_: Exception) { 120 }
                val weekends = try { repository.reminderWeekendsEnabled.first() } catch (_: Exception) { true }

                _uiState.update { current ->
                    if (current.currentStep == 1 && !current.isSaving) {
                        current.copy(
                            name = profile.name,
                            age = profile.age.toString(),
                            weightKg = if (profile.weightKg % 1f == 0f) profile.weightKg.toInt().toString() else profile.weightKg.toString(),
                            activityLevel = profile.activityLevel,
                            exerciseMinutes = profile.exerciseMinutes.toString(),
                            preferredUnit = profile.preferredUnit,
                            remindersEnabled = profile.remindersEnabled,
                            reminderIntervalMinutes = interval,
                            reminderWeekendsEnabled = weekends,
                            wakeTime = profile.wakeTime,
                            sleepTime = profile.sleepTime,
                            calculatedTargetMl = target
                        )
                    } else {
                        current
                    }
                }
            }
        }
    }

    fun setEditMode(isEdit: Boolean) {
        _uiState.update { it.copy(isEditMode = isEdit, currentStep = if (isEdit) 2 else 1) }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun updateAge(ageStr: String) {
        val filtered = ageStr.filter { it.isDigit() }.take(3)
        _uiState.update {
            it.copy(
                age = filtered,
                ageError = null
            )
        }
        recalculateTarget()
    }

    fun updateWeight(weightStr: String) {
        val sanitized = weightStr.filter { it.isDigit() || it == '.' }
        _uiState.update {
            it.copy(
                weightKg = sanitized,
                weightError = null
            )
        }
        recalculateTarget()
    }

    fun updateActivityLevel(level: ActivityLevel) {
        _uiState.update { it.copy(activityLevel = level) }
        recalculateTarget()
    }

    fun updateExerciseMinutes(minutesStr: String) {
        val filtered = minutesStr.filter { it.isDigit() }.take(3)
        _uiState.update {
            it.copy(
                exerciseMinutes = filtered,
                exerciseError = null
            )
        }
        recalculateTarget()
    }

    fun updatePreferredUnit(unit: String) {
        _uiState.update { it.copy(preferredUnit = unit) }
    }

    fun updateRemindersEnabled(enabled: Boolean) {
        _uiState.update { it.copy(remindersEnabled = enabled) }
    }

    fun updateReminderIntervalMinutes(minutes: Int) {
        _uiState.update { it.copy(reminderIntervalMinutes = minutes) }
    }

    fun updateReminderWeekendsEnabled(enabled: Boolean) {
        _uiState.update { it.copy(reminderWeekendsEnabled = enabled) }
    }

    fun updateWakeTime(time: String) {
        _uiState.update { it.copy(wakeTime = time) }
    }

    fun updateSleepTime(time: String) {
        _uiState.update { it.copy(sleepTime = time) }
    }

    private fun recalculateTarget() {
        val weight = _uiState.value.weightKg.toFloatOrNull() ?: 70f
        val age = _uiState.value.age.toIntOrNull() ?: 28
        val exercise = _uiState.value.exerciseMinutes.toIntOrNull() ?: 0
        val target = HydrationCalculator.calculateDailyTargetMl(
            weightKg = weight,
            age = age,
            activityLevel = _uiState.value.activityLevel,
            exerciseMinutes = exercise,
            climate = Climate.TEMPERATE
        )
        _uiState.update { it.copy(calculatedTargetMl = target) }
    }

    fun validatePersonalInfo(): Boolean {
        var isValid = true
        val state = _uiState.value

        val ageInt = state.age.toIntOrNull()
        val ageErr = when {
            state.age.isBlank() -> "Age is required"
            ageInt == null || ageInt < 10 || ageInt > 120 -> "Please enter a valid age between 10 and 120"
            else -> null
        }
        if (ageErr != null) isValid = false

        val weightFloat = state.weightKg.toFloatOrNull()
        val weightErr = when {
            state.weightKg.isBlank() -> "Weight is required"
            weightFloat == null || weightFloat < 20f || weightFloat > 300f -> "Please enter a valid weight between 20 and 300 kg"
            else -> null
        }
        if (weightErr != null) isValid = false

        _uiState.update { it.copy(ageError = ageErr, weightError = weightErr) }
        return isValid
    }

    fun validateActivityInfo(): Boolean {
        val state = _uiState.value
        val exerciseInt = state.exerciseMinutes.toIntOrNull()
        val exerciseErr = when {
            state.exerciseMinutes.isNotBlank() && (exerciseInt == null || exerciseInt < 0 || exerciseInt > 360) ->
                "Daily exercise must be between 0 and 360 minutes"
            else -> null
        }
        _uiState.update { it.copy(exerciseError = exerciseErr) }
        return exerciseErr == null
    }

    fun goToNextStep(): Boolean {
        val current = _uiState.value.currentStep
        when (current) {
            1 -> {
                _uiState.update { it.copy(currentStep = 2) }
                return true
            }
            2 -> {
                if (validatePersonalInfo() && validateActivityInfo()) {
                    _uiState.update { it.copy(currentStep = 3) }
                    return true
                }
                return false
            }
            3 -> {
                _uiState.update { it.copy(currentStep = 4) }
                return true
            }
            4 -> {
                _uiState.update { it.copy(currentStep = 5) }
                return true
            }
        }
        return false
    }

    fun goToPreviousStep(): Boolean {
        val current = _uiState.value.currentStep
        if (current > 1) {
            _uiState.update { it.copy(currentStep = current - 1) }
            return true
        }
        return false
    }

    fun jumpToStep(step: Int) {
        if (step in 1..5) {
            _uiState.update { it.copy(currentStep = step) }
        }
    }

    fun saveProfileAndComplete(onSuccess: () -> Unit) {
        if (!validatePersonalInfo() || !validateActivityInfo()) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val state = _uiState.value

            val profile = UserProfile(
                name = state.name.trim(),
                age = state.age.toIntOrNull() ?: 28,
                weightKg = state.weightKg.toFloatOrNull() ?: 70f,
                activityLevel = state.activityLevel,
                exerciseMinutes = state.exerciseMinutes.toIntOrNull() ?: 0,
                preferredUnit = state.preferredUnit,
                remindersEnabled = state.remindersEnabled,
                wakeTime = state.wakeTime,
                sleepTime = state.sleepTime,
                climate = Climate.TEMPERATE
            )

            // Save profile and calculated target without wiping water intake history
            repository.saveProfile(profile, calculatedTargetMl = state.calculatedTargetMl)
            repository.setRemindersEnabled(state.remindersEnabled)
            repository.setReminderIntervalMinutes(state.reminderIntervalMinutes)
            repository.setReminderWeekendsEnabled(state.reminderWeekendsEnabled)
            repository.setWakingHours(state.wakeTime, state.sleepTime)
            repository.setPreferredUnit(state.preferredUnit)
            repository.setOnboardingCompleted(true)

            _uiState.update { it.copy(isSaving = false) }
            onSuccess()
        }
    }

    class Factory(private val repository: WaterRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OnboardingViewModel(repository) as T
        }
    }
}
