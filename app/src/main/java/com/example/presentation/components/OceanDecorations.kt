package com.example.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.OceanAqua
import com.example.ui.theme.OceanCardBorder
import com.example.ui.theme.OceanCyan
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.WaterSurface

/**
 * Reusable Ocean Card featuring a soft oceanic background gradient,
 * rounded corners, and subtle wave crest drawing.
 */
@Composable
fun OceanWaveCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 22.dp,
    contentPadding: Dp = 16.dp,
    testTag: String = "ocean_wave_card",
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        WaterSurface,
                        Color(0xFFF3FAFC),
                        Color(0xFFEAF5F8)
                    )
                )
            )
            .border(1.dp, OceanCardBorder, shape)
            .testTag(testTag)
    ) {
        // Subtle ambient wave crest at the bottom
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val wavePath = Path().apply {
                moveTo(0f, h * 0.82f)
                cubicTo(
                    w * 0.25f, h * 0.76f,
                    w * 0.70f, h * 0.90f,
                    w, h * 0.80f
                )
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = wavePath,
                color = OceanAqua.copy(alpha = 0.05f)
            )
        }

        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content
        )
    }
}

/**
 * Subtle floating bubble background particles for hero cards and dashboard headers.
 */
@Composable
fun OceanBubbleParticles(
    modifier: Modifier = Modifier,
    particleColor: Color = OceanCyan.copy(alpha = 0.15f)
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Predefined relative bubble coordinates for deterministic rendering
        val bubbles = listOf(
            Triple(0.12f, 0.25f, 0.035f),
            Triple(0.85f, 0.20f, 0.045f),
            Triple(0.28f, 0.70f, 0.025f),
            Triple(0.72f, 0.65f, 0.030f),
            Triple(0.48f, 0.35f, 0.020f),
            Triple(0.92f, 0.80f, 0.040f),
            Triple(0.08f, 0.85f, 0.028f)
        )

        bubbles.forEach { (relX, relY, relRadius) ->
            val center = Offset(w * relX, h * relY)
            val radius = w * relRadius
            drawCircle(
                color = particleColor,
                radius = radius,
                center = center
            )
            // Tiny bubble gleam
            drawCircle(
                color = Color.White.copy(alpha = 0.40f),
                radius = radius * 0.35f,
                center = center + Offset(-radius * 0.35f, -radius * 0.35f)
            )
        }
    }
}
