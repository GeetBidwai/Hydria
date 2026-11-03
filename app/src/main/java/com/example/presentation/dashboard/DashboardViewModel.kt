package com.example.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.utils.DateTimeUtils
import com.example.data.local.WaterEntry
import com.example.data.repository.WaterRepository
import com.example.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

data class DashboardUiState(
    val userName: String = "Friend",
    val currentDateStr: String = "",
    val dailyTargetMl: Int = 2500,
    val consumedMl: Int = 0,
    val remainingMl: Int = 2500,
    val percentage: Int = 0,
    val isGoalAchieved: Boolean = false,
    val preferredUnit: String = "ml",
    val todayEntries: List<WaterEntry> = emptyList(),
    val isLoading: Boolean = false,
    val isCustomAmountDialogVisible: Boolean = false,
    val customAmountInput: String = "",
    val customAmountError: String? = null,
    val lastDeletedEntry: WaterEntry? = null,
    val showUndoSnackbar: Boolean = false,
    val snackbarMessage: String? = null
)

class DashboardViewModel(
    private val repository: WaterRepository
) : ViewModel() {

    private val todayDateKey = DateTimeUtils.getTodayDateKey()

    // Dialog & UI interaction state
    private val _dialogState = MutableStateFlow(
        DialogAndSnackbarState(
            currentDateStr = DateTimeUtils.formatDisplayDate()
        )
    )

    private data class DialogAndSnackbarState(
        val currentDateStr: String,
        val isCustomAmountDialogVisible: Boolean = false,
        val customAmountInput: String = "",
        val customAmountError: String? = null,
        val lastDeletedEntry: WaterEntry? = null,
        val showUndoSnackbar: Boolean = false,
        val snackbarMessage: String? = null
    )

    // Last log timestamp to prevent accidental double-clicks / rapid duplicate entries
    private var lastLogMillis = 0L

    private data class PreferencesInfo(
        val profile: UserProfile,
        val targetMl: Int,
        val unit: String
    )

    private data class TodayIntakeInfo(
        val consumedMl: Int,
        val entries: List<WaterEntry>
    )

    private val preferencesFlow = combine(
        repository.userProfile,
        repository.dailyTargetMl,
        repository.preferredUnit
    ) { profile, targetMl, unit ->
        PreferencesInfo(profile, targetMl, unit)
    }

    private val todayIntakeFlow = combine(
        repository.getDailyIntakeMl(todayDateKey),
        repository.getEntriesForDate(todayDateKey)
    ) { consumedMl, entries ->
        TodayIntakeInfo(consumedMl, entries)
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        preferencesFlow,
        todayIntakeFlow,
        _dialogState
    ) { prefs, intake, dialogState ->
        val safeTarget = if (prefs.targetMl > 0) prefs.targetMl else 2500
        val remaining = (safeTarget - intake.consumedMl).coerceAtLeast(0)
        val percentage = ((intake.consumedMl.toFloat() / safeTarget.toFloat()) * 100).toInt()
        val isGoalAchieved = intake.consumedMl >= safeTarget && safeTarget > 0

        DashboardUiState(
            userName = if (prefs.profile.name.isNotBlank()) prefs.profile.name else "Friend",
            currentDateStr = dialogState.currentDateStr,
            dailyTargetMl = safeTarget,
            consumedMl = intake.consumedMl,
            remainingMl = remaining,
            percentage = percentage,
            isGoalAchieved = isGoalAchieved,
            preferredUnit = prefs.unit,
            todayEntries = intake.entries,
            isLoading = false,
            isCustomAmountDialogVisible = dialogState.isCustomAmountDialogVisible,
            customAmountInput = dialogState.customAmountInput,
            customAmountError = dialogState.customAmountError,
            lastDeletedEntry = dialogState.lastDeletedEntry,
            showUndoSnackbar = dialogState.showUndoSnackbar,
            snackbarMessage = dialogState.snackbarMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(
            currentDateStr = DateTimeUtils.formatDisplayDate()
        )
    )

    fun logQuickAdd(amountMl: Int) {
        val now = System.currentTimeMillis()
        if (now - lastLogMillis < 300) return // Debounce rapid multi-taps
        lastLogMillis = now

        if (amountMl <= 0 || amountMl > 3000) return

        viewModelScope.launch {
            repository.logWater(amountMl, todayDateKey)
        }
    }

    fun showCustomAmountDialog() {
        _dialogState.update {
            it.copy(
                isCustomAmountDialogVisible = true,
                customAmountInput = "",
                customAmountError = null
            )
        }
    }

    fun dismissCustomAmountDialog() {
        _dialogState.update {
            it.copy(
                isCustomAmountDialogVisible = false,
                customAmountInput = "",
                customAmountError = null
            )
        }
    }

    fun updateCustomAmountInput(input: String) {
        val filtered = input.filter { it.isDigit() }.take(4)
        _dialogState.update {
            it.copy(
                customAmountInput = filtered,
                customAmountError = null
            )
        }
    }

    fun submitCustomAmount() {
        val input = _dialogState.value.customAmountInput.trim()
        val amount = input.toIntOrNull()

        when {
            input.isBlank() -> {
                _dialogState.update { it.copy(customAmountError = "Please enter an amount in ml") }
                return
            }
            amount == null || amount <= 0 -> {
                _dialogState.update { it.copy(customAmountError = "Please enter a valid positive amount") }
                return
            }
            amount > 3000 -> {
                _dialogState.update { it.copy(customAmountError = "Maximum single entry is 3,000 ml") }
                return
            }
        }

        viewModelScope.launch {
            repository.logWater(amount, todayDateKey)
            _dialogState.update {
                it.copy(
                    isCustomAmountDialogVisible = false,
                    customAmountInput = "",
                    customAmountError = null
                )
            }
        }
    }

    fun deleteEntry(entry: WaterEntry) {
        viewModelScope.launch {
            repository.deleteEntry(entry.id)
            val formatted = formatVolume(entry.amountMl, uiState.value.preferredUnit)
            _dialogState.update {
                it.copy(
                    lastDeletedEntry = entry,
                    showUndoSnackbar = true,
                    snackbarMessage = "Removed $formatted"
                )
            }
        }
    }

    fun undoDelete() {
        val entryToRestore = _dialogState.value.lastDeletedEntry ?: return
        viewModelScope.launch {
            repository.insertEntry(entryToRestore)
            _dialogState.update {
                it.copy(
                    lastDeletedEntry = null,
                    showUndoSnackbar = false,
                    snackbarMessage = null
                )
            }
        }
    }

    fun dismissUndoSnackbar() {
        _dialogState.update {
            it.copy(
                showUndoSnackbar = false,
                snackbarMessage = null
            )
        }
    }

    fun formatVolume(amountMl: Int, unit: String): String {
        return if (unit == "litres") {
            String.format(Locale.getDefault(), "%.2f L", amountMl / 1000f)
        } else {
            "$amountMl ml"
        }
    }

    class Factory(private val repository: WaterRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(repository) as T
        }
    }
}
