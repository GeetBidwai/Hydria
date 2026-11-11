package com.example.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OceanAqua
import com.example.ui.theme.OceanCyan
import com.example.ui.theme.OceanDeep
import com.example.ui.theme.OceanFoam
import com.example.ui.theme.OceanGold
import com.example.ui.theme.WaterAccentSuccess
import com.example.ui.theme.WaterOutline
import com.example.ui.theme.WaterPrimary
import com.example.ui.theme.WaterTextPrimary
import com.example.ui.theme.WaterTextSecondary
import kotlin.math.sin

@Composable
fun WaterCircularProgress(
    currentMl: Int,
    targetMl: Int,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    strokeWidth: Dp = 14.dp,
    subtitleOverride: String? = null,
    testTag: String = "water_circular_progress"
) {
    val progress = if (targetMl > 0) {
        (currentMl.toFloat() / targetMl.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val percentage = if (targetMl > 0) {
        ((currentMl.toFloat() / targetMl.toFloat()) * 100).toInt()
    } else 0

    val isTargetReached = currentMl >= targetMl && targetMl > 0

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "water_progress_anim"
    )

    // Gentle wave animation inside progress circle
    val infiniteTransition = rememberInfiniteTransition(label = "wave_motion")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    Box(
        modifier = modifier
            .size(size)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = strokeWidth.toPx()
            val radius = (this.size.minDimension - stroke) / 2
            val center = this.center

            // 1. Background Track
            drawCircle(
                color = WaterOutline.copy(alpha = 0.35f),
                radius = radius,
                center = center,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // 2. Inner Water Fill Wave (Subtle ambient water level inside circle)
            if (animatedProgress > 0.02f) {
                val innerRadius = radius - stroke / 2f
                val waterLevelY = center.y + innerRadius - (2f * innerRadius * animatedProgress)

                val wavePath = Path().apply {
                    val startX = center.x - innerRadius
                    val endX = center.x + innerRadius
                    moveTo(startX, center.y + innerRadius)

                    var x = startX
                    while (x <= endX) {
                        val relativeX = (x - startX) / (innerRadius * 2f)
                        val waveOffset = sin(relativeX * 2.5f * Math.PI + wavePhase).toFloat() * 6f
                        val y = (waterLevelY + waveOffset).coerceIn(center.y - innerRadius, center.y + innerRadius)
                        lineTo(x, y)
                        x += 4f
                    }
                    lineTo(endX, center.y + innerRadius)
                    close()
                }

                // Clip wave within circular bounds
                val circleClip = Path().apply {
                    addOval(androidx.compose.ui.geometry.Rect(center, innerRadius))
                }
                drawPath(
                    path = wavePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            OceanCyan.copy(alpha = 0.20f),
                            OceanAqua.copy(alpha = 0.32f),
                            OceanDeep.copy(alpha = 0.40f)
                        ),
                        startY = center.y - innerRadius,
                        endY = center.y + innerRadius
                    )
                )
            }

            // 3. Progress Arc with Vibrant Ocean Gradient
            val progressBrush = if (isTargetReached) {
                Brush.sweepGradient(
                    colors = listOf(OceanAqua, OceanCyan, WaterAccentSuccess, OceanGold, OceanAqua),
                    center = center
                )
            } else {
                Brush.sweepGradient(
                    colors = listOf(OceanCyan, OceanAqua, WaterPrimary, OceanDeep, OceanCyan),
                    center = center
                )
            }

            drawArc(
                brush = progressBrush,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }

        // Center Typography
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 34.sp,
                    color = WaterTextPrimary
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitleOverride ?: "$currentMl / $targetMl ml",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = WaterTextSecondary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            )
            if (isTargetReached) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Goal Achieved! 🎉",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = WaterAccentSuccess,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

