package com.example.core.ui.components

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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.sensors.DeviceTilt
import com.example.core.ui.sensors.rememberDeviceTilt
import com.example.ui.theme.OceanAqua
import com.example.ui.theme.OceanCardBorder
import com.example.ui.theme.OceanCyan
import com.example.ui.theme.OceanDeep
import com.example.ui.theme.OceanGold
import com.example.ui.theme.WaterAccentSuccess
import com.example.ui.theme.WaterOutline
import com.example.ui.theme.WaterPrimary
import com.example.ui.theme.WaterTextPrimary
import com.example.ui.theme.WaterTextSecondary
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Hydria Circular Liquid Progress Indicator.
 *
 * Replaces the old cylindrical graphic with a native, circular liquid fill.
 * The liquid fills the circular container from bottom upward according to hydration progress:
 * - 0%: container is empty.
 * - 25%: occupies bottom quarter.
 * - 50%: reaches exact midpoint.
 * - 75%: occupies three quarters.
 * - 100%: completely filled.
 *
 * Realistically responds to physical device tilt:
 * - Tilting left/right tilts the liquid surface naturally.
 * - Level position allows the liquid to settle horizontally.
 * - Liquid shape is strictly clipped to the circular boundary.
 */
@Composable
fun WaterCircularProgress(
    currentMl: Int,
    targetMl: Int,
    modifier: Modifier = Modifier,
    size: Dp = 210.dp,
    strokeWidth: Dp = 12.dp,
    subtitleOverride: String? = null,
    testTag: String = "water_circular_progress",
    tilt: DeviceTilt = rememberDeviceTilt()
) {
    val progress = if (targetMl > 0) {
        (currentMl.toFloat() / targetMl.toFloat()).coerceAtLeast(0f)
    } else 0f

    val clampedProgress = progress.coerceIn(0f, 1f)
    val percentage = (progress * 100).toInt()
    val isTargetReached = currentMl >= targetMl && targetMl > 0

    // Smoothly animated progress transition when logging/editing water
    val animatedProgress by animateFloatAsState(
        targetValue = clampedProgress,
        animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
        label = "water_progress_anim"
    )

    // Gentle ambient harmonic wave motion
    val infiniteTransition = rememberInfiniteTransition(label = "wave_motion")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
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
            val outerRadius = (this.size.minDimension - stroke) / 2f
            val innerRadius = outerRadius - stroke / 2f
            val center = this.center

            // 1. Circular Container Base (Transparent ocean-tinted glass interior)
            drawCircle(
                color = OceanAqua.copy(alpha = 0.05f),
                radius = innerRadius,
                center = center
            )
            drawCircle(
                color = OceanCardBorder.copy(alpha = 0.40f),
                radius = innerRadius,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // 2. Circular Liquid Fill (Clipped strictly to the circular boundary)
            if (animatedProgress > 0.005f) {
                val circleClip = Path().apply {
                    addOval(Rect(center, innerRadius))
                }

                clipPath(circleClip) {
                    val baseWaterLevelY = center.y + innerRadius - (2f * innerRadius * animatedProgress)

                    // Physics tilt slope based on device roll
                    val tiltSlope = DeviceTilt.computeSurfaceSlope(tilt.roll, maxSlope = 0.50f)

                    // Wave amplitude dampening near the very bottom (0%) and top (100%)
                    val fullnessDamping = (1f - abs(animatedProgress - 0.5f) * 1.8f).coerceIn(0.12f, 1f)
                    val amp1 = 5f * fullnessDamping
                    val amp2 = 2.5f * fullnessDamping

                    val startX = center.x - innerRadius - 20f
                    val endX = center.x + innerRadius + 20f
                    val bottomY = center.y + innerRadius + 25f

                    // Layer A: Secondary background wave for oceanic volume depth
                    val backWavePath = Path().apply {
                        moveTo(startX, bottomY)
                        var x = startX
                        val step = 4f
                        while (x <= endX) {
                            val relativeX = (x - startX) / (endX - startX)
                            val waveOffset = sin(relativeX * 2.2f * Math.PI + wavePhase + Math.PI.toFloat()).toFloat() * (amp1 * 0.8f)
                            val tiltOffset = -(x - center.x) * tiltSlope
                            val y = baseWaterLevelY + tiltOffset + waveOffset + 3f
                            lineTo(x, y)
                            x += step
                        }
                        lineTo(endX, bottomY)
                        close()
                    }

                    drawPath(
                        path = backWavePath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                OceanCyan.copy(alpha = 0.30f),
                                OceanAqua.copy(alpha = 0.45f),
                                OceanDeep.copy(alpha = 0.55f)
                            ),
                            startY = center.y - innerRadius,
                            endY = center.y + innerRadius
                        )
                    )

                    // Layer B: Primary foreground liquid wave
                    val frontWavePath = Path().apply {
                        moveTo(startX, bottomY)
                        var x = startX
                        val step = 3f
                        while (x <= endX) {
                            val relativeX = (x - startX) / (endX - startX)
                            val waveOffset = (sin(relativeX * 2.2f * Math.PI + wavePhase).toFloat() * amp1) +
                                    (cos(relativeX * 4.4f * Math.PI - wavePhase * 0.7f).toFloat() * amp2)
                            val tiltOffset = -(x - center.x) * tiltSlope
                            val y = baseWaterLevelY + tiltOffset + waveOffset
                            lineTo(x, y)
                            x += step
                        }
                        lineTo(endX, bottomY)
                        close()
                    }

                    drawPath(
                        path = frontWavePath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                OceanCyan.copy(alpha = 0.65f),
                                OceanAqua.copy(alpha = 0.78f),
                                OceanDeep.copy(alpha = 0.88f)
                            ),
                            startY = center.y - innerRadius,
                            endY = center.y + innerRadius
                        )
                    )

                    // Layer C: Gentle white foam crest at liquid surface
                    val crestPath = Path().apply {
                        var x = startX
                        val step = 3f
                        var isFirst = true
                        while (x <= endX) {
                            val relativeX = (x - startX) / (endX - startX)
                            val waveOffset = (sin(relativeX * 2.2f * Math.PI + wavePhase).toFloat() * amp1) +
                                    (cos(relativeX * 4.4f * Math.PI - wavePhase * 0.7f).toFloat() * amp2)
                            val tiltOffset = -(x - center.x) * tiltSlope
                            val y = baseWaterLevelY + tiltOffset + waveOffset
                            if (isFirst) {
                                moveTo(x, y)
                                isFirst = false
                            } else {
                                lineTo(x, y)
                            }
                            x += step
                        }
                    }

                    drawPath(
                        path = crestPath,
                        color = Color.White.copy(alpha = 0.55f),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Layer D: Ambient liquid bubble gleams
                    val bubbleOffset1 = Offset(center.x - innerRadius * 0.40f, center.y + innerRadius * 0.45f)
                    val bubbleOffset2 = Offset(center.x + innerRadius * 0.35f, center.y + innerRadius * 0.25f)
                    if (baseWaterLevelY < bubbleOffset1.y) {
                        drawCircle(color = Color.White.copy(alpha = 0.30f), radius = 3.dp.toPx(), center = bubbleOffset1)
                    }
                    if (baseWaterLevelY < bubbleOffset2.y) {
                        drawCircle(color = Color.White.copy(alpha = 0.25f), radius = 2.dp.toPx(), center = bubbleOffset2)
                    }
                }
            }

            // 3. Circular Progress Ring Track
            drawCircle(
                color = WaterOutline.copy(alpha = 0.35f),
                radius = outerRadius,
                center = center,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // 4. Progress Arc with Vibrant Ocean Sweep Gradient
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

        // 5. Center Typography (Consistently readable, layered cleanly above liquid)
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
