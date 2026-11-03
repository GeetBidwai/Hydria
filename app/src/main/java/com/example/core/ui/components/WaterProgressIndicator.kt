package com.example.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WaterAccentSuccess
import com.example.ui.theme.WaterOutline
import com.example.ui.theme.WaterPrimary
import com.example.ui.theme.WaterTextPrimary
import com.example.ui.theme.WaterTextSecondary

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

    val progressColor by animateColorAsState(
        targetValue = if (isTargetReached) WaterAccentSuccess else WaterPrimary,
        animationSpec = tween(durationMillis = 500),
        label = "water_color_anim"
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

            // Background Track
            drawCircle(
                color = WaterOutline.copy(alpha = 0.35f),
                radius = radius,
                center = center,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // Progress Arc
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }

        // Center Text
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
