package com.example.presentation.dashboard.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.components.WaterCard
import com.example.domain.gamification.DesignationCategory
import com.example.domain.gamification.GamificationState
import com.example.ui.theme.OceanAqua
import com.example.ui.theme.OceanBrushes
import com.example.ui.theme.OceanCyan
import com.example.ui.theme.OceanDeep
import com.example.ui.theme.OceanGold
import com.example.ui.theme.WaterAccentSuccess
import com.example.ui.theme.WaterOutline
import com.example.ui.theme.WaterPrimary
import com.example.ui.theme.WaterPrimaryContainer
import com.example.ui.theme.WaterSurface
import com.example.ui.theme.WaterTextPrimary
import com.example.ui.theme.WaterTextSecondary

@Composable
fun DesignationCard(
    state: GamificationState,
    isTodayGoalAchieved: Boolean,
    modifier: Modifier = Modifier
) {
    val isDisaster = state.isDisasterPath

    val badgeColor = when {
        isDisaster -> Color(0xFFC04E26)
        state.currentDesignationCategory == DesignationCategory.MONTHLY_DESIGNATION -> OceanGold
        state.currentDesignationCategory == DesignationCategory.OCEAN_LEGEND -> OceanAqua
        else -> WaterTextSecondary
    }

    val badgeBg = when {
        isDisaster -> Color(0xFFFBE9E7)
        state.currentDesignationCategory == DesignationCategory.MONTHLY_DESIGNATION -> OceanGold.copy(alpha = 0.15f)
        state.currentDesignationCategory == DesignationCategory.OCEAN_LEGEND -> OceanAqua.copy(alpha = 0.15f)
        else -> Color(0xFFEAEFF2)
    }

    val badgeText = when {
        isDisaster -> "OCEAN DISASTER"
        state.currentDesignationCategory == DesignationCategory.MONTHLY_DESIGNATION -> "MONTHLY DESIGNATION"
        state.currentDesignationCategory == DesignationCategory.OCEAN_LEGEND -> "OCEAN LEGEND"
        else -> "HYDRATION JOURNEY"
    }

    WaterCard(
        testTag = "dashboard_designation_card",
        contentPadding = 18.dp,
        modifier = modifier
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. Header: Category Badge & Streak Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeBg,
                    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isDisaster) Icons.Default.WarningAmber else if (state.currentDesignationCategory == DesignationCategory.MONTHLY_DESIGNATION) Icons.Default.EmojiEvents else Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = badgeColor,
                                letterSpacing = 0.6.sp,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Current streak label
                val streakLabel = when {
                    isDisaster -> "${state.currentMissedStreak} missed ${if (state.currentMissedStreak == 1) "day" else "days"}"
                    state.overallStreak > 0 -> "${state.overallStreak} day streak"
                    else -> "Day 1 in progress"
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("designation_streak_text")
                ) {
                    if (!isDisaster && state.overallStreak > 0) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = OceanGold,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                    Text(
                        text = streakLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDisaster) Color(0xFFC04E26) else OceanAqua,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Current Designation Title
            Text(
                text = state.currentDesignationTitle,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = WaterTextPrimary,
                    fontSize = 20.sp
                ),
                modifier = Modifier.testTag("designation_title_text")
            )

            Spacer(modifier = Modifier.height(3.dp))

            // Short Description
            Text(
                text = state.currentDesignationDescription,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = WaterTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                ),
                modifier = Modifier.testTag("designation_description_text")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Monthly Badge Collection Status (e.g. 6/10 badges)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Monthly Collection",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = WaterTextPrimary,
                        fontSize = 13.sp
                    )
                )

                Text(
                    text = "${state.currentMonthBadgeCount}/10 badges",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (state.isCurrentMonthCompleted) WaterAccentSuccess else OceanAqua,
                        fontSize = 12.sp
                    ),
                    modifier = Modifier.testTag("monthly_badge_count_text")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Progress towards Next Badge or Milestone
            if (state.nextMilestoneTitle != null) {
                val nextLabel = if (isDisaster) {
                    "Next missed tier: ${state.nextMilestoneTitle} (${state.nextMilestoneDays} days)"
                } else if (state.isCurrentMonthCompleted) {
                    "All 10 monthly badges collected!"
                } else {
                    "Next: ${state.nextMilestoneTitle} (${state.nextMilestoneDays} days)"
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = nextLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = WaterTextSecondary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    )

                    val percentStr = "${(state.progressFraction * 100).toInt()}%"
                    Text(
                        text = percentStr,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = WaterTextSecondary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { state.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .testTag("designation_progress_bar"),
                    color = if (isDisaster) Color(0xFFC04E26) else if (state.isCurrentMonthCompleted) WaterAccentSuccess else OceanAqua,
                    trackColor = WaterOutline.copy(alpha = 0.4f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5. Monthly Designation Status Note
            val monthlyStatusNote = if (state.isCurrentMonthCompleted) {
                "🎉 Monthly collection conquered! Designation awarded."
            } else {
                val needed = 10 - state.currentMonthBadgeCount
                "$needed more ${if (needed == 1) "badge" else "badges"} to unlock your next Monthly Designation."
            }

            Text(
                text = monthlyStatusNote,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = WaterTextSecondary,
                    fontSize = 12.sp
                )
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = WaterOutline.copy(alpha = 0.35f))
            Spacer(modifier = Modifier.height(10.dp))

            // 6. Footer: Lifetime Badges, Monthly Collections, and Total Goals Met
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lifetime Badges: ${state.lifetimeUnlockedBadgeCount}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = WaterTextSecondary,
                        fontSize = 11.sp
                    )
                )

                Text(
                    text = "Total Goals Met: ${state.totalSuccessfulDays}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = WaterTextSecondary,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}
