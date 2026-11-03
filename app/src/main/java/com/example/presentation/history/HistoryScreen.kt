package com.example.presentation.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.ui.theme.WaterAccentSuccess
import com.example.ui.theme.WaterBackground
import com.example.ui.theme.WaterOutline
import com.example.ui.theme.WaterPrimary
import com.example.ui.theme.WaterPrimaryContainer
import com.example.ui.theme.WaterSurface
import com.example.ui.theme.WaterTextPrimary
import com.example.ui.theme.WaterTextSecondary

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
                text = "Hydration History",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = WaterTextPrimary,
                    fontSize = 24.sp
                ),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Summary Card: Today + 7-Day Performance
        item {
            WaterCard(testTag = "history_summary_card") {
                Text(
                    text = "7-DAY OVERVIEW",
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
                            text = "Daily average (last 7 days)",
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
                        color = WaterPrimary
                    )
                    OverviewStat(
                        label = "Daily Target",
                        value = viewModel.formatVolume(uiState.dailyTargetMl, uiState.preferredUnit),
                        color = WaterTextPrimary
                    )
                    OverviewStat(
                        label = "7-Day Total",
                        value = viewModel.formatVolume(uiState.weeklyStats.totalIntakeMl, uiState.preferredUnit),
                        color = WaterPrimary
                    )
                }
            }
        }

        // Tab Selector: Weekly vs Monthly
        item {
            TabRow(
                selectedTabIndex = uiState.selectedTab,
                containerColor = WaterSurface,
                contentColor = WaterPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab]),
                        color = WaterPrimary
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("history_tab_row")
            ) {
                listOf("Weekly", "Monthly").forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.selectedTab == index,
                        onClick = { viewModel.selectTab(index) },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = if (uiState.selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (uiState.selectedTab == index) WaterPrimary else WaterTextSecondary
                                )
                            )
                        }
                    )
                }
            }
        }

        // TAB CONTENT
        if (uiState.selectedTab == 0) {
            // WEEKLY VIEW
            item {
                WaterCard(testTag = "weekly_chart_card") {
                    Text(
                        text = "LAST 7 DAYS",
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
        } else {
            // MONTHLY VIEW
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
                                tint = WaterPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = uiState.monthlyStats.monthLabel,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = WaterTextPrimary
                            )
                        )

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
                                tint = if (uiState.monthlyStats.canGoForward) WaterPrimary else WaterOutline,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Monthly Overview Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OverviewStat(
                            label = "Month Total",
                            value = viewModel.formatVolume(uiState.monthlyStats.totalMonthIntakeMl, uiState.preferredUnit),
                            color = WaterPrimary
                        )
                        OverviewStat(
                            label = "Daily Average",
                            value = viewModel.formatVolume(uiState.monthlyStats.averageDailyIntakeMl, uiState.preferredUnit),
                            color = WaterTextPrimary
                        )
                        OverviewStat(
                            label = "Goals Met",
                            value = "${uiState.monthlyStats.daysGoalMetCount} Days",
                            color = WaterAccentSuccess
                        )
                    }

                    HorizontalDivider(
                        color = WaterOutline.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    // Month Days Strip / Grid
                    Text(
                        text = "TAP A DAY TO VIEW LOGS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = WaterTextSecondary,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    MonthlyDaysGrid(
                        days = uiState.monthlyStats.daysInMonth,
                        selectedDateKey = uiState.selectedDateKey,
                        onSelectDay = { viewModel.selectDate(it.dateKey) }
                    )
                }
            }
        }

        // DAILY DETAILS CARD (Selected Day Breakdown)
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
                                color = if (uiState.selectedDayTotalMl >= uiState.dailyTargetMl) WaterAccentSuccess else WaterPrimary
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

                // List of Entries for Selected Date
                if (uiState.selectedDayEntries.isEmpty()) {
                    // Empty State
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(WaterOutline.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.EventNote,
                                contentDescription = null,
                                tint = WaterTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No water logged on this day",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = WaterTextPrimary
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
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(WaterPrimaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.WaterDrop,
                                            contentDescription = null,
                                            tint = WaterPrimary,
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
                    day.totalMl > 0 -> WaterPrimary
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
                        .background(if (isSelected) WaterPrimaryContainer else Color.Transparent)
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.dayLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) WaterPrimary else WaterTextSecondary,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthlyDaysGrid(
    days: List<DayHistoryItem>,
    selectedDateKey: String,
    onSelectDay: (DayHistoryItem) -> Unit
) {
    // 7 columns grid for days in month
    val rows = days.chunked(7)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEach { rowDays ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                rowDays.forEach { day ->
                    val isSelected = day.dateKey == selectedDateKey
                    val cellColor = when {
                        day.isFuture -> Color.Transparent
                        isSelected -> WaterPrimaryContainer
                        day.isGoalMet -> WaterAccentSuccess.copy(alpha = 0.15f)
                        day.totalMl > 0 -> WaterPrimary.copy(alpha = 0.08f)
                        else -> WaterSurface
                    }

                    val textColor = when {
                        day.isFuture -> WaterOutline
                        isSelected -> WaterPrimary
                        day.isGoalMet -> WaterAccentSuccess
                        day.totalMl > 0 -> WaterPrimary
                        else -> WaterTextSecondary
                    }

                    Surface(
                        onClick = { if (!day.isFuture) onSelectDay(day) },
                        enabled = !day.isFuture,
                        shape = RoundedCornerShape(8.dp),
                        color = cellColor,
                        border = if (isSelected) BorderStroke(1.5.dp, WaterPrimary) else null,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("month_day_${day.dateKey}")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = day.dayLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected || day.isGoalMet) FontWeight.Bold else FontWeight.Normal,
                                    color = textColor,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }

                // Fill empty slots in the last row to maintain grid alignment
                for (i in 0 until (7 - rowDays.size)) {
                    Spacer(modifier = Modifier.size(36.dp))
                }
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
                fontSize = 15.sp
            )
        )
    }
}
