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
    val isToday: Boolean = false,
    val completionPercentage: Int = 0
)

data class WeeklyStats(
    val last7Days: List<DayHistoryItem> = emptyList(),
    val averageDailyIntakeMl: Int = 0,
    val daysGoalMetCount: Int = 0,
    val totalIntakeMl: Int = 0,
    val completionPercentage: Int = 0
)

data class MonthlyStats(
    val year: Int = 2026,
    val monthIndex: Int = 0,
    val monthLabel: String = "",
    val firstDayOffset: Int = 0, // 0 for Sun, 1 for Mon, etc.
    val daysInMonth: List<DayHistoryItem> = emptyList(),
    val totalMonthIntakeMl: Int = 0,
    val averageDailyIntakeMl: Int = 0,
    val daysGoalMetCount: Int = 0,
    val completionPercentage: Int = 0,
    val activeDaysLoggedCount: Int = 0,
    val totalLogsCount: Int = 0,
    val averageLoggingFrequency: Float = 0f,
    val canGoForward: Boolean = false
)

data class MonthSummaryItem(
    val monthIndex: Int,
    val monthName: String,
    val shortMonthName: String,
    val totalIntakeMl: Int,
    val dailyAverageMl: Int,
    val daysGoalMetCount: Int,
    val totalDaysInMonth: Int,
    val isFuture: Boolean
)

data class YearlyStats(
    val year: Int = 2026,
    val months: List<MonthSummaryItem> = emptyList(),
    val totalYearIntakeMl: Long = 0L,
    val averageDailyIntakeMl: Int = 0,
    val totalDaysGoalMetCount: Int = 0,
    val goalCompletionPercentage: Int = 0,
    val canGoForward: Boolean = false
)

data class HistoryUiState(
    val selectedTab: Int = 0, // 0 = Weekly, 1 = Monthly, 2 = Yearly
    val todayConsumedMl: Int = 0,
    val dailyTargetMl: Int = 2500,
    val preferredUnit: String = "ml",
    val weeklyStats: WeeklyStats = WeeklyStats(),
    val monthlyStats: MonthlyStats = MonthlyStats(),
    val yearlyStats: YearlyStats = YearlyStats(),
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

    private val _selectedYear = MutableStateFlow(currentYear)
    private val _selectedTab = MutableStateFlow(0)
    private val _selectedDateKey = MutableStateFlow(todayKey)

    private data class HistoryNavState(
        val tab: Int,
        val selectedDate: String,
        val monthYear: Int,
        val monthIndex: Int,
        val yearViewYear: Int
    )

    private val navState = combine(
        _selectedTab,
        _selectedDateKey,
        _monthSelection,
        _selectedYear
    ) { tab, dateKey, (mYear, mIndex), yYear ->
        HistoryNavState(tab, dateKey, mYear, mIndex, yYear)
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

            val completion = if (safeTarget > 0) {
                ((total.toFloat() / safeTarget.toFloat()) * 100).toInt()
            } else 0

            DayHistoryItem(
                dateKey = key,
                dayLabel = DateTimeUtils.getDayOfWeekLabel(key),
                shortDateStr = DateTimeUtils.formatShortDisplayDate(key),
                totalMl = total,
                targetMl = safeTarget,
                isGoalMet = isMet,
                isToday = isToday,
                completionPercentage = completion
            )
        }
        val weeklyAverage = weeklyTotalSum / 7
        val weeklyCompletionPercent = ((weeklyGoalsMet.toFloat() / 7f) * 100f).toInt()
        val weeklyStats = WeeklyStats(
            last7Days = weeklyItems,
            averageDailyIntakeMl = weeklyAverage,
            daysGoalMetCount = weeklyGoalsMet,
            totalIntakeMl = weeklyTotalSum,
            completionPercentage = weeklyCompletionPercent
        )

        // 2. Monthly calculations (for nav.monthYear & nav.monthIndex)
        val monthDateKeys = DateTimeUtils.getMonthDateKeys(nav.monthYear, nav.monthIndex)
        val isCurrentCalendarMonth = (nav.monthYear == currentYear && nav.monthIndex == currentMonthIndex)
        val todayDayOfMonth = currentCal.get(Calendar.DAY_OF_MONTH)

        var monthTotalSum = 0
        var monthGoalsMet = 0
        var pastOrPresentDaysCount = 0
        var activeDaysLoggedCount = 0

        val monthEntries = allEntries.filter { entry ->
            entry.dateKey in monthDateKeys
        }
        val totalLogsInMonth = monthEntries.size

        val monthItems = monthDateKeys.mapIndexed { index, key ->
            val dayNumber = index + 1
            val isFuture = (nav.monthYear > currentYear) ||
                    (nav.monthYear == currentYear && nav.monthIndex > currentMonthIndex) ||
                    (isCurrentCalendarMonth && dayNumber > todayDayOfMonth)
            val total = if (isFuture) 0 else (totalsMap[key] ?: 0)
            val isMet = !isFuture && total >= safeTarget
            val isToday = key == todayKey

            if (!isFuture) {
                monthTotalSum += total
                pastOrPresentDaysCount++
                if (isMet) monthGoalsMet++
                if (total > 0) activeDaysLoggedCount++
            }

            val completion = if (!isFuture && safeTarget > 0) {
                ((total.toFloat() / safeTarget.toFloat()) * 100).toInt()
            } else 0

            DayHistoryItem(
                dateKey = key,
                dayLabel = dayNumber.toString(),
                shortDateStr = DateTimeUtils.formatShortDisplayDate(key),
                totalMl = total,
                targetMl = safeTarget,
                isGoalMet = isMet,
                isFuture = isFuture,
                isToday = isToday,
                completionPercentage = completion
            )
        }

        val monthAverage = if (pastOrPresentDaysCount > 0) monthTotalSum / pastOrPresentDaysCount else 0
        val monthCompletionPercent = if (pastOrPresentDaysCount > 0) {
            ((monthGoalsMet.toFloat() / pastOrPresentDaysCount.toFloat()) * 100).toInt()
        } else 0
        val avgLoggingFrequency = if (pastOrPresentDaysCount > 0) {
            totalLogsInMonth.toFloat() / pastOrPresentDaysCount.toFloat()
        } else 0f

        val canGoForwardMonth = (nav.monthYear < currentYear) || (nav.monthYear == currentYear && nav.monthIndex < currentMonthIndex)
        val firstDayCalendarDay = DateTimeUtils.getFirstDayOfWeekForMonth(nav.monthYear, nav.monthIndex)
        // Offset: Calendar.SUNDAY = 1 -> offset 0; MONDAY = 2 -> offset 1, etc.
        val firstDayOffset = firstDayCalendarDay - 1

        val monthlyStats = MonthlyStats(
            year = nav.monthYear,
            monthIndex = nav.monthIndex,
            monthLabel = DateTimeUtils.formatMonthYear(nav.monthYear, nav.monthIndex),
            firstDayOffset = firstDayOffset,
            daysInMonth = monthItems,
            totalMonthIntakeMl = monthTotalSum,
            averageDailyIntakeMl = monthAverage,
            daysGoalMetCount = monthGoalsMet,
            completionPercentage = monthCompletionPercent,
            activeDaysLoggedCount = activeDaysLoggedCount,
            totalLogsCount = totalLogsInMonth,
            averageLoggingFrequency = avgLoggingFrequency,
            canGoForward = canGoForwardMonth
        )

        // 3. Yearly calculations (for nav.yearViewYear: 12 months)
        val isCurrentCalendarYear = (nav.yearViewYear == currentYear)
        var totalYearIntake = 0L
        var totalYearGoalsMet = 0
        var totalYearActiveDays = 0

        val yearMonthSummaries = (0..11).map { mIdx ->
            val isFutureMonth = (nav.yearViewYear > currentYear) || (isCurrentCalendarYear && mIdx > currentMonthIndex)
            val keys = DateTimeUtils.getMonthDateKeys(nav.yearViewYear, mIdx)
            val monthDaysCount = keys.size

            var mIntake = 0
            var mGoals = 0
            var mActiveDays = 0

            if (!isFutureMonth) {
                keys.forEach { k ->
                    val dayNum = keys.indexOf(k) + 1
                    val isDayFuture = isCurrentCalendarYear && (mIdx == currentMonthIndex) && (dayNum > todayDayOfMonth)
                    if (!isDayFuture) {
                        val dTotal = totalsMap[k] ?: 0
                        mIntake += dTotal
                        if (dTotal >= safeTarget) mGoals++
                        mActiveDays++
                    }
                }
                totalYearIntake += mIntake
                totalYearGoalsMet += mGoals
                totalYearActiveDays += mActiveDays
            }

            val dAverage = if (mActiveDays > 0) mIntake / mActiveDays else 0
            MonthSummaryItem(
                monthIndex = mIdx,
                monthName = DateTimeUtils.getMonthName(mIdx),
                shortMonthName = DateTimeUtils.getShortMonthName(mIdx),
                totalIntakeMl = mIntake,
                dailyAverageMl = dAverage,
                daysGoalMetCount = mGoals,
                totalDaysInMonth = monthDaysCount,
                isFuture = isFutureMonth
            )
        }

        val yearDailyAverage = if (totalYearActiveDays > 0) (totalYearIntake / totalYearActiveDays).toInt() else 0
        val yearGoalPercent = if (totalYearActiveDays > 0) {
            ((totalYearGoalsMet.toFloat() / totalYearActiveDays.toFloat()) * 100).toInt()
        } else 0
        val canGoForwardYear = (nav.yearViewYear < currentYear)

        val yearlyStats = YearlyStats(
            year = nav.yearViewYear,
            months = yearMonthSummaries,
            totalYearIntakeMl = totalYearIntake,
            averageDailyIntakeMl = yearDailyAverage,
            totalDaysGoalMetCount = totalYearGoalsMet,
            goalCompletionPercentage = yearGoalPercent,
            canGoForward = canGoForwardYear
        )

        // 4. Selected Day Entries
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
            yearlyStats = yearlyStats,
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
        _selectedTab.update { tabIndex.coerceIn(0, 2) }
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

    fun previousYear() {
        _selectedYear.update { it - 1 }
    }

    fun nextYear() {
        if (_selectedYear.value < currentYear) {
            _selectedYear.update { it + 1 }
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

    fun formatVolumeLong(amountMl: Long, unit: String): String {
        return if (unit == "litres") {
            String.format(Locale.getDefault(), "%.2f L", amountMl / 1000.0)
        } else {
            String.format(Locale.getDefault(), "%,d ml", amountMl)
        }
    }

    class Factory(private val repository: WaterRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HistoryViewModel(repository) as T
        }
    }
}
