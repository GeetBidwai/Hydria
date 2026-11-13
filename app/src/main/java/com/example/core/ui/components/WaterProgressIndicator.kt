package com.example.core.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.runtime.remember
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
import androidx.compose.ui.graphics.drawscope.rotate
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
 * Native circular liquid fill that responds dynamically to physical gravity across
 * full 360-degree device orientation:
 * - 0%: container is empty.
 * - 25%: occupies lower quarter in gravity's direction.
 * - 50%: reaches exact midpoint.
 * - 75%: occupies three quarters.
 * - 100%: completely filled.
 *
 * The circular ring and typography remain stationary while only the clipped liquid
 * settles smoothly toward physical gravity at any angle (upright, tilted, landscape,
 * or upside down).
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

    // Continuous seamless harmonic wave motion
    val infiniteTransition = rememberInfiniteTransition(label = "wave_motion")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    // Reusable Path instances to avoid per-frame allocations during 60/120fps wave & tilt draws
    val circleClip = remember { Path() }
    val backWavePath = remember { Path() }
    val frontWavePath = remember { Path() }
    val crestPath = remember { Path() }

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

            // 1. Circular Container Base (Stationary ocean-tinted glass interior)
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

            // 2. Gravity-Responsive Circular Liquid Fill (Clipped strictly to circular boundary)
            if (animatedProgress > 0.005f) {
                circleClip.reset()
                circleClip.addOval(Rect(center, innerRadius))

                clipPath(circleClip) {
                    // Rotate only the liquid coordinate frame toward physical gravity down
                    rotate(degrees = tilt.gravityAngleDegrees, pivot = center) {
                        val baseWaterLevelY = center.y + innerRadius - (2f * innerRadius * animatedProgress)

                        // Dampen wave height near empty (0%) and full (100%); gently boost during rotation
                        val fullnessDamping = (1f - abs(animatedProgress - 0.5f) * 1.8f).coerceIn(0.12f, 1f)
                        val sloshBoost = 1f + (tilt.sloshFactor * 0.85f)
                        val amp1 = 5f * fullnessDamping * sloshBoost
                        val amp2 = 2.5f * fullnessDamping * sloshBoost

                        val startX = center.x - innerRadius - 28f
                        val endX = center.x + innerRadius + 28f
                        val bottomY = center.y + innerRadius + 28f

                        // Layer A: Secondary background wave for oceanic volume depth
                        backWavePath.reset()
                        backWavePath.moveTo(startX, bottomY)
                        var x = startX
                        val backStep = 4f
                        while (x <= endX) {
                            val relativeX = (x - startX) / (endX - startX)
                            val waveOffset = sin(relativeX * 2.2f * Math.PI + wavePhase + Math.PI.toFloat()).toFloat() * (amp1 * 0.8f)
                            val y = baseWaterLevelY + waveOffset + 3f
                            backWavePath.lineTo(x, y)
                            x += backStep
                        }
                        backWavePath.lineTo(endX, bottomY)
                        backWavePath.close()

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

                        // Layer B: Primary foreground liquid wave & Layer C: Foam crest
                        frontWavePath.reset()
                        frontWavePath.moveTo(startX, bottomY)
                        crestPath.reset()

                        x = startX
                        val frontStep = 3f
                        var isFirst = true
                        while (x <= endX) {
                            val relativeX = (x - startX) / (endX - startX)
                            val waveOffset = (sin(relativeX * 2.2f * Math.PI + wavePhase).toFloat() * amp1) +
                                    (cos(relativeX * 4.4f * Math.PI - wavePhase * 0.7f).toFloat() * amp2)
                            val y = baseWaterLevelY + waveOffset
                            frontWavePath.lineTo(x, y)
                            if (isFirst) {
                                crestPath.moveTo(x, y)
                                isFirst = false
                            } else {
                                crestPath.lineTo(x, y)
                            }
                            x += frontStep
                        }
                        frontWavePath.lineTo(endX, bottomY)
                        frontWavePath.close()

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

                        drawPath(
                            path = crestPath,
                            color = Color.White.copy(alpha = 0.55f),
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Layer D: Ambient liquid bubble gleams submerged in the gravity-lowered region
                        val bubbleOffset1 = Offset(center.x - innerRadius * 0.38f, center.y + innerRadius * 0.45f)
                        val bubbleOffset2 = Offset(center.x + innerRadius * 0.34f, center.y + innerRadius * 0.25f)
                        if (baseWaterLevelY < bubbleOffset1.y - 4.dp.toPx()) {
                            drawCircle(color = Color.White.copy(alpha = 0.30f), radius = 3.dp.toPx(), center = bubbleOffset1)
                        }
                        if (baseWaterLevelY < bubbleOffset2.y - 4.dp.toPx()) {
                            drawCircle(color = Color.White.copy(alpha = 0.25f), radius = 2.dp.toPx(), center = bubbleOffset2)
                        }
                    }
                }
            }

            // 3. Circular Progress Ring Track (Stationary)
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
