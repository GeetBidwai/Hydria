package com.example.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.utils.DateTimeUtils
import com.example.data.local.DateTotal
import com.example.data.local.WaterEntry
import com.example.data.repository.WaterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

data class DayHistoryItem(
    val dateKey: String,
    val dayLabel: String,
    val shortDateStr: String,
    val totalMl: Int,
    val targetMl: Int,
    val isGoalMet: Boolean,
    val isFuture: Boolean = false,
    val isToday: Boolean = false
)

data class WeeklyStats(
    val last7Days: List<DayHistoryItem> = emptyList(),
    val averageDailyIntakeMl: Int = 0,
    val daysGoalMetCount: Int = 0,
    val totalIntakeMl: Int = 0
)

data class MonthlyStats(
    val year: Int = 2026,
    val monthIndex: Int = 0,
    val monthLabel: String = "",
    val daysInMonth: List<DayHistoryItem> = emptyList(),
    val totalMonthIntakeMl: Int = 0,
    val averageDailyIntakeMl: Int = 0,
    val daysGoalMetCount: Int = 0,
    val canGoForward: Boolean = false
)

data class HistoryUiState(
    val selectedTab: Int = 0, // 0 = Weekly, 1 = Monthly
    val todayConsumedMl: Int = 0,
    val dailyTargetMl: Int = 2500,
    val preferredUnit: String = "ml",
    val weeklyStats: WeeklyStats = WeeklyStats(),
    val monthlyStats: MonthlyStats = MonthlyStats(),
    val selectedDateKey: String = DateTimeUtils.getTodayDateKey(),
    val selectedDateShortLabel: String = "Today",
    val selectedDayTotalMl: Int = 0,
    val selectedDayEntries: List<WaterEntry> = emptyList(),
    val isLoading: Boolean = false
)

class HistoryViewModel(
    private val repository: WaterRepository
) : ViewModel() {

    private val todayKey = DateTimeUtils.getTodayDateKey()

    private val currentCal = Calendar.getInstance()
    private val currentYear = currentCal.get(Calendar.YEAR)
    private val currentMonthIndex = currentCal.get(Calendar.MONTH)

    private val _monthSelection = MutableStateFlow(
        Pair(currentYear, currentMonthIndex)
    )

    private val _selectedTab = MutableStateFlow(0)
    private val _selectedDateKey = MutableStateFlow(todayKey)

    private data class HistoryNavState(
        val tab: Int,
        val selectedDate: String,
        val year: Int,
        val month: Int
    )

    private val navState = combine(
        _selectedTab,
        _selectedDateKey,
        _monthSelection
    ) { tab, dateKey, (year, month) ->
        HistoryNavState(tab, dateKey, year, month)
    }

    val uiState: StateFlow<HistoryUiState> = combine(
        repository.dailyTargetMl,
        repository.preferredUnit,
        repository.getDailyTotals(),
        repository.getAllEntries(),
        navState
    ) { targetMl, unit, dailyTotals, allEntries, nav ->
        val safeTarget = if (targetMl > 0) targetMl else 2500
        val totalsMap = dailyTotals.associate { it.dateKey to it.totalMl }

        // 1. Weekly calculations (Last 7 calendar days ending today)
        val last7DateKeys = DateTimeUtils.getLastNDaysDateKeys(7)
        var weeklyTotalSum = 0
        var weeklyGoalsMet = 0

        val weeklyItems = last7DateKeys.map { key ->
            val total = totalsMap[key] ?: 0
            val isMet = total >= safeTarget
            val isToday = key == todayKey
            weeklyTotalSum += total
            if (isMet) weeklyGoalsMet++

            DayHistoryItem(
                dateKey = key,
                dayLabel = DateTimeUtils.getDayOfWeekLabel(key),
                shortDateStr = DateTimeUtils.formatShortDisplayDate(key),
                totalMl = total,
                targetMl = safeTarget,
                isGoalMet = isMet,
                isToday = isToday
            )
        }
        val weeklyAverage = weeklyTotalSum / 7
        val weeklyStats = WeeklyStats(
            last7Days = weeklyItems,
            averageDailyIntakeMl = weeklyAverage,
            daysGoalMetCount = weeklyGoalsMet,
            totalIntakeMl = weeklyTotalSum
        )

        // 2. Monthly calculations (for nav.year & nav.month)
        val monthDateKeys = DateTimeUtils.getMonthDateKeys(nav.year, nav.month)
        val isCurrentCalendarMonth = (nav.year == currentYear && nav.month == currentMonthIndex)
        val todayDayOfMonth = currentCal.get(Calendar.DAY_OF_MONTH)

        var monthTotalSum = 0
        var monthGoalsMet = 0
        var pastOrPresentDaysCount = 0

        val monthItems = monthDateKeys.mapIndexed { index, key ->
            val dayNumber = index + 1
            val isFuture = isCurrentCalendarMonth && dayNumber > todayDayOfMonth
            val total = if (isFuture) 0 else (totalsMap[key] ?: 0)
            val isMet = !isFuture && total >= safeTarget
            val isToday = key == todayKey

            if (!isFuture) {
                monthTotalSum += total
                pastOrPresentDaysCount++
                if (isMet) monthGoalsMet++
            }

            DayHistoryItem(
                dateKey = key,
                dayLabel = dayNumber.toString(),
                shortDateStr = DateTimeUtils.formatShortDisplayDate(key),
                totalMl = total,
                targetMl = safeTarget,
                isGoalMet = isMet,
                isFuture = isFuture,
                isToday = isToday
            )
        }

        val monthAverage = if (pastOrPresentDaysCount > 0) monthTotalSum / pastOrPresentDaysCount else 0
        val canGoForward = (nav.year < currentYear) || (nav.year == currentYear && nav.month < currentMonthIndex)

        val monthlyStats = MonthlyStats(
            year = nav.year,
            monthIndex = nav.month,
            monthLabel = DateTimeUtils.formatMonthYear(nav.year, nav.month),
            daysInMonth = monthItems,
            totalMonthIntakeMl = monthTotalSum,
            averageDailyIntakeMl = monthAverage,
            daysGoalMetCount = monthGoalsMet,
            canGoForward = canGoForward
        )

        // 3. Selected Day Entries
        val selectedDayEntries = allEntries
            .filter { it.dateKey == nav.selectedDate }
            .sortedByDescending { it.timestamp }

        val selectedDayTotal = totalsMap[nav.selectedDate] ?: 0
        val todayConsumed = totalsMap[todayKey] ?: 0

        HistoryUiState(
            selectedTab = nav.tab,
            todayConsumedMl = todayConsumed,
            dailyTargetMl = safeTarget,
            preferredUnit = unit,
            weeklyStats = weeklyStats,
            monthlyStats = monthlyStats,
            selectedDateKey = nav.selectedDate,
            selectedDateShortLabel = DateTimeUtils.formatShortDisplayDate(nav.selectedDate),
            selectedDayTotalMl = selectedDayTotal,
            selectedDayEntries = selectedDayEntries,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )

    fun selectTab(tabIndex: Int) {
        _selectedTab.update { tabIndex }
    }

    fun selectDate(dateKey: String) {
        _selectedDateKey.update { dateKey }
    }

    fun previousMonth() {
        _monthSelection.update { (year, month) ->
            if (month == 0) {
                Pair(year - 1, 11)
            } else {
                Pair(year, month - 1)
            }
        }
    }

    fun nextMonth() {
        val (year, month) = _monthSelection.value
        val canGoForward = (year < currentYear) || (year == currentYear && month < currentMonthIndex)
        if (canGoForward) {
            _monthSelection.update {
                if (month == 11) {
                    Pair(year + 1, 0)
                } else {
                    Pair(year, month + 1)
                }
            }
        }
    }

    fun deleteEntry(entry: WaterEntry) {
        viewModelScope.launch {
            repository.deleteEntry(entry.id)
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
            return HistoryViewModel(repository) as T
        }
    }
}
