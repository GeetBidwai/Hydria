package com.example.widgets

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.ButtonDefaults
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.MainActivity

class ExpandedWaterWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = WidgetDataHelper.loadWidgetData(context)

        provideContent {
            // Theme colors
            val bgSoft = Color(0xFFF7F8F6)
            val cardWhite = Color(0xFFFFFFFF)
            val primaryBlue = Color(0xFF4A90A4)
            val secondarySurface = Color(0xFFEEF5F7)
            val textDark = Color(0xFF202B30)
            val textSubtle = Color(0xFF5F6E75)
            val progressTrack = Color(0xFFE3EDF0)
            val successGreen = Color(0xFF2E7D32)

            // Determine unique quick-add amounts to show
            // Primary configured amount + fixed presets (150, 300, 500)
            val quickAddOptions = buildList {
                add(data.widgetQuickAddMl)
                listOf(150, 300, 500).forEach { fixed ->
                    if (!contains(fixed)) {
                        add(fixed)
                    }
                }
            }.take(4)

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(bgSoft)
                    .cornerRadius(20.dp)
                    .padding(8.dp)
            ) {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(cardWhite)
                        .cornerRadius(16.dp)
                        .padding(14.dp)
                        .clickable(actionStartActivity(MainActivity::class.java))
                ) {
                    // 1. Header: Branding & Goal status
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💧 Hydria",
                            style = TextStyle(
                                color = ColorProvider(primaryBlue),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Spacer(modifier = GlanceModifier.defaultWeight())

                        Text(
                            text = if (data.isGoalAchieved) "Goal Reached! 🎉" else "${data.progressPercent}% Achieved",
                            style = TextStyle(
                                color = ColorProvider(if (data.isGoalAchieved) successGreen else primaryBlue),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.height(8.dp))

                    // 2. Metrics Row: Today's Consumed, Target, Remaining
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "Today's Intake",
                                style = TextStyle(
                                    color = ColorProvider(textSubtle),
                                    fontSize = 11.sp
                                )
                            )
                            Text(
                                text = data.formattedConsumed,
                                style = TextStyle(
                                    color = ColorProvider(textDark),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(modifier = GlanceModifier.defaultWeight())

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Target: ${data.formattedTarget}",
                                style = TextStyle(
                                    color = ColorProvider(textSubtle),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Spacer(modifier = GlanceModifier.height(2.dp))
                            Text(
                                text = if (data.isGoalAchieved) "Goal met!" else "Remaining: ${data.formattedRemaining}",
                                style = TextStyle(
                                    color = ColorProvider(if (data.isGoalAchieved) successGreen else textSubtle),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = GlanceModifier.height(6.dp))

                    // 3. Progress Bar
                    LinearProgressIndicator(
                        progress = data.progressFraction,
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .cornerRadius(3.dp),
                        color = ColorProvider(if (data.isGoalAchieved) successGreen else primaryBlue),
                        backgroundColor = ColorProvider(progressTrack)
                    )

                    Spacer(modifier = GlanceModifier.height(10.dp))

                    // 4. Quick Add Buttons Row
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Quick Log:",
                            style = TextStyle(
                                color = ColorProvider(textSubtle),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )

                        Spacer(modifier = GlanceModifier.width(6.dp))

                        quickAddOptions.forEachIndexed { index, amount ->
                            if (index > 0) {
                                Spacer(modifier = GlanceModifier.width(6.dp))
                            }
                            val isPrimary = amount == data.widgetQuickAddMl
                            Button(
                                text = "+$amount",
                                onClick = actionRunCallback<WidgetQuickAddAction>(
                                    actionParametersOf(WidgetQuickAddAction.KEY_AMOUNT_ML to amount)
                                ),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = ColorProvider(if (isPrimary) primaryBlue else secondarySurface),
                                    contentColor = ColorProvider(if (isPrimary) Color.White else textDark)
                                ),
                                modifier = GlanceModifier
                                    .cornerRadius(10.dp)
                                    .height(34.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
