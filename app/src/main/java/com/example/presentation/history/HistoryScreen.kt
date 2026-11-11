package com.example.presentation.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.components.WaterCard
import com.example.core.utils.DateTimeUtils
import com.example.data.local.WaterEntry
import com.example.presentation.components.HydriaMascot
import com.example.presentation.components.HydriaMascotMood
import com.example.presentation.components.OceanWaveCard
import com.example.ui.theme.OceanAqua
import com.example.ui.theme.OceanCyan
import com.example.ui.theme.OceanDeep
import com.example.ui.theme.OceanGold
import com.example.ui.theme.WaterAccentSuccess
import com.example.ui.theme.WaterBackground
import com.example.ui.theme.WaterOutline
import com.example.ui.theme.WaterPrimary
import com.example.ui.theme.WaterPrimaryContainer
import com.example.ui.theme.WaterSurface
import com.example.ui.theme.WaterTextPrimary
import com.example.ui.theme.WaterTextSecondary
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(WaterBackground)
            .testTag("history_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title Header
        item {
            Text(
                text = "Hydration Analytics",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = WaterTextPrimary,
                    fontSize = 24.sp
                ),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Tab Selector: Weekly vs Monthly vs Yearly
        item {
            TabRow(
                selectedTabIndex = uiState.selectedTab,
                containerColor = WaterSurface,
                contentColor = OceanAqua,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab]),
                        color = OceanAqua
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("history_tab_row")
            ) {
                listOf("Weekly", "Monthly", "Yearly").forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.selectedTab == index,
                        onClick = { viewModel.selectTab(index) },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = if (uiState.selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (uiState.selectedTab == index) OceanAqua else WaterTextSecondary
                                )
                            )
                        }
                    )
                }
            }
        }

        when (uiState.selectedTab) {
            0 -> {
                // ==========================================
                // 1. WEEKLY VIEW
                // ==========================================
                item {
                    OceanWaveCard(testTag = "history_summary_card") {
                        Text(
                            text = "7-DAY SUMMARY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = WaterTextSecondary,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = viewModel.formatVolume(uiState.weeklyStats.averageDailyIntakeMl, uiState.preferredUnit),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = WaterTextPrimary,
                                        fontSize = 28.sp
                                    )
                                )
                                Text(
                                    text = "Daily average intake",
                                    style = MaterialTheme.typography.bodySmall.copy(color = WaterTextSecondary)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(WaterAccentSuccess.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = WaterAccentSuccess,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${uiState.weeklyStats.daysGoalMetCount}/7 Days Met",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = WaterAccentSuccess,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        HorizontalDivider(
                            color = WaterOutline.copy(alpha = 0.5f),
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OverviewStat(
                                label = "Today's Intake",
                                value = viewModel.formatVolume(uiState.todayConsumedMl, uiState.preferredUnit),
                                color = OceanAqua
                            )
                            OverviewStat(
                                label = "Daily Target",
                                value = viewModel.formatVolume(uiState.dailyTargetMl, uiState.preferredUnit),
                                color = WaterTextPrimary
                            )
                            OverviewStat(
                                label = "Goal Rate",
                                value = "${uiState.weeklyStats.completionPercentage}%",
                                color = if (uiState.weeklyStats.completionPercentage >= 70) WaterAccentSuccess else OceanAqua
                            )
                        }
                    }
                }

                item {
                    WaterCard(testTag = "weekly_chart_card") {
                        Text(
                            text = "7-DAY HYDRATION BREAKDOWN",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = WaterTextSecondary,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        WeeklyBarChart(
                            days = uiState.weeklyStats.last7Days,
                            targetMl = uiState.dailyTargetMl,
                            selectedDateKey = uiState.selectedDateKey,
                            onSelectDay = { viewModel.selectDate(it.dateKey) },
                            formatVolume = { viewModel.formatVolume(it, uiState.preferredUnit) }
                        )
                    }
                }
            }

            1 -> {
                // ==========================================
                // 2. MONTHLY VIEW
                // ==========================================
                item {
                    WaterCard(testTag = "monthly_calendar_card") {
                        // Month Navigation Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { viewModel.previousMonth() },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("btn_prev_month")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                                    contentDescription = "Previous month",
                                    tint = OceanAqua,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = OceanAqua,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = uiState.monthlyStats.monthLabel,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = WaterTextPrimary
                                    )
                                )
                            }

                            IconButton(
                                onClick = { viewModel.nextMonth() },
                                enabled = uiState.monthlyStats.canGoForward,
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("btn_next_month")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    contentDescription = "Next month",
                                    tint = if (uiState.monthlyStats.canGoForward) OceanAqua else WaterOutline,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Monthly Analytics Summary Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OverviewStat(
                                label = "Total Volume",
                                value = viewModel.formatVolume(uiState.monthlyStats.totalMonthIntakeMl, uiState.preferredUnit),
                                color = OceanAqua
                            )
                            OverviewStat(
                                label = "Daily Avg",
                                value = viewModel.formatVolume(uiState.monthlyStats.averageDailyIntakeMl, uiState.preferredUnit),
                                color = WaterTextPrimary
                            )
                            OverviewStat(
                                label = "Goals Met",
                                value = "${uiState.monthlyStats.daysGoalMetCount} Days",
                                color = WaterAccentSuccess
                            )
                            OverviewStat(
                                label = "Success Rate",
                                value = "${uiState.monthlyStats.completionPercentage}%",
                                color = if (uiState.monthlyStats.completionPercentage >= 70) WaterAccentSuccess else OceanAqua
                            )
                        }

                        HorizontalDivider(
                            color = WaterOutline.copy(alpha = 0.5f),
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        // Average logging frequency indicator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Avg Logging Frequency",
                                style = MaterialTheme.typography.labelSmall.copy(color = WaterTextSecondary)
                            )
                            Text(
                                text = String.format(Locale.getDefault(), "%.1f logs / day", uiState.monthlyStats.averageLoggingFrequency),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OceanAqua
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Calendar Legend
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            CalendarLegendItem(color = WaterAccentSuccess, label = "Goal Met")
                            CalendarLegendItem(color = OceanAqua, label = "Partial")
                            CalendarLegendItem(color = WaterOutline.copy(alpha = 0.5f), label = "No Intake")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Monthly Calendar Grid
                        MonthlyCalendarGrid(
                            firstDayOffset = uiState.monthlyStats.firstDayOffset,
                            days = uiState.monthlyStats.daysInMonth,
                            selectedDateKey = uiState.selectedDateKey,
                            onSelectDay = { viewModel.selectDate(it.dateKey) }
                        )
                    }
                }
            }

            2 -> {
                // ==========================================
                // 3. YEARLY VIEW
                // ==========================================
                item {
                    OceanWaveCard(testTag = "yearly_summary_card") {
                        // Year Navigation Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { viewModel.previousYear() },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("btn_prev_year")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                                    contentDescription = "Previous year",
                                    tint = OceanAqua,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = OceanAqua,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Year ${uiState.yearlyStats.year}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = WaterTextPrimary
                                    )
                                )
                            }

                            IconButton(
                                onClick = { viewModel.nextYear() },
                                enabled = uiState.yearlyStats.canGoForward,
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("btn_next_year")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    contentDescription = "Next year",
                                    tint = if (uiState.yearlyStats.canGoForward) OceanAqua else WaterOutline,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Annual Stats Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OverviewStat(
                                label = "Annual Total",
                                value = viewModel.formatVolumeLong(uiState.yearlyStats.totalYearIntakeMl, uiState.preferredUnit),
                                color = OceanAqua
                            )
                            OverviewStat(
                                label = "Daily Average",
                                value = viewModel.formatVolume(uiState.yearlyStats.averageDailyIntakeMl, uiState.preferredUnit),
                                color = WaterTextPrimary
                            )
                            OverviewStat(
                                label = "Goals Met",
                                value = "${uiState.yearlyStats.totalDaysGoalMetCount} Days",
                                color = WaterAccentSuccess
                            )
                            OverviewStat(
                                label = "Goal Rate",
                                value = "${uiState.yearlyStats.goalCompletionPercentage}%",
                                color = if (uiState.yearlyStats.goalCompletionPercentage >= 70) WaterAccentSuccess else OceanAqua
                            )
                        }
                    }
                }

                // Month-by-Month Comparison Cards
                item {
                    WaterCard(testTag = "yearly_breakdown_card") {
                        Text(
                            text = "MONTH-BY-MONTH HYDRATION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = WaterTextSecondary,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            uiState.yearlyStats.months.forEach { monthItem ->
                                MonthSummaryRow(
                                    item = monthItem,
                                    targetMl = uiState.dailyTargetMl,
                                    formatVolume = { viewModel.formatVolume(it, uiState.preferredUnit) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // DAILY DETAILS CARD (Selected Day Breakdown)
        // ==========================================
        item {
            WaterCard(testTag = "selected_day_card") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = uiState.selectedDateShortLabel,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = WaterTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Target: ${viewModel.formatVolume(uiState.dailyTargetMl, uiState.preferredUnit)}",
                            style = MaterialTheme.typography.bodySmall.copy(color = WaterTextSecondary)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = viewModel.formatVolume(uiState.selectedDayTotalMl, uiState.preferredUnit),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.selectedDayTotalMl >= uiState.dailyTargetMl && uiState.dailyTargetMl > 0) WaterAccentSuccess else OceanAqua
                            )
                        )
                        if (uiState.selectedDayTotalMl >= uiState.dailyTargetMl && uiState.dailyTargetMl > 0) {
                            Text(
                                text = "Target Met 🎉",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = WaterAccentSuccess,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                HorizontalDivider(
                    color = WaterOutline.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // List of Entries for Selected Date or Empty State
                if (uiState.selectedDayEntries.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        HydriaMascot(
                            mood = HydriaMascotMood.CHEERFUL_START,
                            size = 52.dp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No water logged on this date",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = WaterTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Select any past day with recorded intake to inspect timestamps",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = WaterTextSecondary,
                                fontSize = 12.sp
                            )
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.selectedDayEntries.forEach { entry ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(OceanAqua.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.WaterDrop,
                                            contentDescription = null,
                                            tint = OceanAqua,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "+${viewModel.formatVolume(entry.amountMl, uiState.preferredUnit)}",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = WaterTextPrimary
                                            )
                                        )
                                        Text(
                                            text = DateTimeUtils.formatTime(entry.timestamp),
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = WaterTextSecondary,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.deleteEntry(entry) },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete entry",
                                        tint = WaterTextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyBarChart(
    days: List<DayHistoryItem>,
    targetMl: Int,
    selectedDateKey: String,
    onSelectDay: (DayHistoryItem) -> Unit,
    formatVolume: (Int) -> String
) {
    val maxIntake = (days.maxOfOrNull { it.totalMl } ?: targetMl).coerceAtLeast(targetMl)
    val maxChartHeight = 120.dp

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(maxChartHeight),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            days.forEach { day ->
                val isSelected = day.dateKey == selectedDateKey
                val barFraction = if (maxIntake > 0) {
                    (day.totalMl.toFloat() / maxIntake.toFloat()).coerceIn(0f, 1f)
                } else 0f

                val barColor = when {
                    day.isGoalMet -> WaterAccentSuccess
                    day.totalMl > 0 -> OceanAqua
                    else -> WaterOutline.copy(alpha = 0.5f)
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectDay(day) }
                        .padding(horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    if (day.isGoalMet) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Goal met",
                            tint = WaterAccentSuccess,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    // Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(maxChartHeight * barFraction.coerceAtLeast(0.04f))
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(barColor)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Day Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEach { day ->
                val isSelected = day.dateKey == selectedDateKey
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectDay(day) }
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) OceanAqua.copy(alpha = 0.15f) else Color.Transparent)
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.dayLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) OceanAqua else WaterTextSecondary,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthlyCalendarGrid(
    firstDayOffset: Int,
    days: List<DayHistoryItem>,
    selectedDateKey: String,
    onSelectDay: (DayHistoryItem) -> Unit
) {
    val weekDayLabels = listOf("S", "M", "T", "W", "T", "F", "S")

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // Week Day Headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekDayLabels.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.size(38.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = WaterTextSecondary,
                        fontSize = 11.sp
                    )
                )
            }
        }

        // Build list with leading nulls for offset
        val gridCells: List<DayHistoryItem?> = buildList {
            repeat(firstDayOffset.coerceAtLeast(0)) { add(null) }
            addAll(days)
        }

        val rows = gridCells.chunked(7)
        rows.forEach { rowCells ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                rowCells.forEach { day ->
                    if (day == null) {
                        Spacer(modifier = Modifier.size(38.dp))
                    } else {
                        val isSelected = day.dateKey == selectedDateKey
                        val cellBg = when {
                            day.isFuture -> Color.Transparent
                            isSelected -> OceanAqua.copy(alpha = 0.20f)
                            day.isGoalMet -> WaterAccentSuccess.copy(alpha = 0.16f)
                            day.totalMl > 0 -> OceanAqua.copy(alpha = 0.10f)
                            else -> WaterSurface
                        }

                        val cellText = when {
                            day.isFuture -> WaterOutline
                            isSelected -> OceanAqua
                            day.isGoalMet -> WaterAccentSuccess
                            day.totalMl > 0 -> OceanAqua
                            else -> WaterTextSecondary
                        }

                        Surface(
                            onClick = { if (!day.isFuture) onSelectDay(day) },
                            enabled = !day.isFuture,
                            shape = RoundedCornerShape(8.dp),
                            color = cellBg,
                            border = if (isSelected) BorderStroke(1.5.dp, OceanAqua) else null,
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("month_day_${day.dateKey}")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = day.dayLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected || day.isGoalMet) FontWeight.Bold else FontWeight.Normal,
                                        color = cellText,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }
                }

                // Fill trailing empty slots if last row has fewer than 7 cells
                for (i in 0 until (7 - rowCells.size)) {
                    Spacer(modifier = Modifier.size(38.dp))
                }
            }
        }
    }
}

@Composable
private fun CalendarLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = WaterTextSecondary,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
private fun MonthSummaryRow(
    item: MonthSummaryItem,
    targetMl: Int,
    formatVolume: (Int) -> String
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (item.isFuture) WaterSurface.copy(alpha = 0.5f) else WaterSurface,
        border = BorderStroke(1.dp, WaterOutline.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.monthName,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (item.isFuture) WaterTextSecondary else WaterTextPrimary,
                        fontSize = 14.sp
                    )
                )
                Text(
                    text = if (item.isFuture) "Upcoming month" else "Avg ${formatVolume(item.dailyAverageMl)} / day • ${item.daysGoalMetCount} goals met",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = WaterTextSecondary,
                        fontSize = 12.sp
                    )
                )
            }

            if (!item.isFuture) {
                Text(
                    text = formatVolume(item.totalIntakeMl),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = OceanAqua,
                        fontSize = 14.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun OverviewStat(
    label: String,
    value: String,
    color: Color
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = WaterTextSecondary,
                fontSize = 11.sp
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 14.sp
            )
        )
    }
}
