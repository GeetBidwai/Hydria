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
import androidx.glance.layout.fillMaxHeight
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

class CompactWaterWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = WidgetDataHelper.loadWidgetData(context)

        provideContent {
            // Colors from Hydria theme
            val bgSoft = Color(0xFFF7F8F6)
            val cardWhite = Color(0xFFFFFFFF)
            val primaryBlue = Color(0xFF4A90A4)
            val textDark = Color(0xFF202B30)
            val textSubtle = Color(0xFF5F6E75)
            val progressTrack = Color(0xFFE3EDF0)
            val successGreen = Color(0xFF2E7D32)

            // Outer wrapper with rounded background
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(bgSoft)
                    .cornerRadius(18.dp)
                    .padding(6.dp)
            ) {
                // Inner card surface
                Row(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(cardWhite)
                        .cornerRadius(14.dp)
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                        .clickable(actionStartActivity(MainActivity::class.java)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left column: Branding, Intake & Progress
                    Column(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Branding row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = GlanceModifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "💧 Hydria",
                                style = TextStyle(
                                    color = ColorProvider(primaryBlue),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            if (data.isGoalAchieved) {
                                Spacer(modifier = GlanceModifier.width(4.dp))
                                Text(
                                    text = "• Done",
                                    style = TextStyle(
                                        color = ColorProvider(successGreen),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = GlanceModifier.height(2.dp))

                        // Current intake
                        Text(
                            text = data.formattedConsumed,
                            style = TextStyle(
                                color = ColorProvider(textDark),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        // Goal progress text
                        Text(
                            text = "${data.progressPercent}% of ${data.formattedTarget}",
                            style = TextStyle(
                                color = ColorProvider(textSubtle),
                                fontSize = 10.sp
                            )
                        )

                        Spacer(modifier = GlanceModifier.height(4.dp))

                        // Linear progress indicator
                        LinearProgressIndicator(
                            progress = data.progressFraction,
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .cornerRadius(3.dp),
                            color = ColorProvider(if (data.isGoalAchieved) successGreen else primaryBlue),
                            backgroundColor = ColorProvider(progressTrack)
                        )
                    }

                    Spacer(modifier = GlanceModifier.width(10.dp))

                    // Right column: Prominent Quick-Add Button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            text = "+${data.widgetQuickAddMl}",
                            onClick = actionRunCallback<WidgetQuickAddAction>(
                                actionParametersOf(WidgetQuickAddAction.KEY_AMOUNT_ML to data.widgetQuickAddMl)
                            ),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = ColorProvider(primaryBlue),
                                contentColor = ColorProvider(Color.White)
                            ),
                            modifier = GlanceModifier
                                .cornerRadius(12.dp)
                                .height(38.dp)
                        )
                    }
                }
            }
        }
    }
}
