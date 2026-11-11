package com.example.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MascotCheekBlush
import com.example.ui.theme.MascotDropletBody
import com.example.ui.theme.MascotDropletHighlight
import com.example.ui.theme.MascotDropletShade
import com.example.ui.theme.MascotEyeColor
import com.example.ui.theme.OceanAqua
import com.example.ui.theme.OceanCyan
import com.example.ui.theme.OceanGold
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.WaterSurface
import com.example.ui.theme.WaterTextPrimary

/**
 * The six contextual moods for the Hydria water-drop mascot ("Hydri").
 */
enum class HydriaMascotMood {
    CHEERFUL_START, // 0% intake (morning, eager to begin)
    PROGRESS,       // 1% to 49% intake (steady progress)
    HALFWAY,        // 50% to 99% intake (celebratory milestone)
    GOAL_MET,       // 100%+ (victory crown, stars, joyful celebration)
    MISSED_GOAL,    // Disaster path (droopy, empathetic comeback encourage)
    RECOVERY        // Comeback after missed streak (heroic aura, trident emblem)
}

object HydriaMascotHelper {
    /**
     * Resolves the current mascot mood based on today's intake and gamification status.
     */
    fun resolveMood(
        consumedMl: Int,
        targetMl: Int,
        isDisaster: Boolean = false,
        isRecovery: Boolean = false
    ): HydriaMascotMood {
        return when {
            isRecovery -> HydriaMascotMood.RECOVERY
            isDisaster -> HydriaMascotMood.MISSED_GOAL
            targetMl > 0 && consumedMl >= targetMl -> HydriaMascotMood.GOAL_MET
            targetMl > 0 && consumedMl >= (targetMl / 2) -> HydriaMascotMood.HALFWAY
            consumedMl > 0 -> HydriaMascotMood.PROGRESS
            else -> HydriaMascotMood.CHEERFUL_START
        }
    }

    /**
     * Provides the default contextual speech text for each mascot mood.
     */
    fun getCompanionMessage(mood: HydriaMascotMood): String {
        return when (mood) {
            HydriaMascotMood.CHEERFUL_START -> "Let's get this ocean adventure started!"
            HydriaMascotMood.PROGRESS -> "Nice! The tide is rising."
            HydriaMascotMood.HALFWAY -> "Halfway there, ocean explorer!"
            HydriaMascotMood.GOAL_MET -> "The ocean celebrates you! You legend!"
            HydriaMascotMood.MISSED_GOAL -> "We missed you yesterday. Ready for a comeback?"
            HydriaMascotMood.RECOVERY -> "That's the spirit! Welcome back!"
        }
    }
}

/**
 * Native Jetpack Compose Canvas rendering of Hydria's Water-Drop mascot.
 * Fully scalable, lightweight, and zero external binary dependencies.
 */
@Composable
fun HydriaMascot(
    mood: HydriaMascotMood,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    animated: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mascot_anim")

    val floatOffset by if (animated) {
        infiniteTransition.animateFloat(
            initialValue = -3f,
            targetValue = 3f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "float_offset"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    }

    val auraPulse by if (animated && (mood == HydriaMascotMood.RECOVERY || mood == HydriaMascotMood.GOAL_MET)) {
        infiniteTransition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "aura_pulse"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(1f) }
    }

    Box(
        modifier = modifier
            .size(size)
            .testTag("hydria_mascot_${mood.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(size)
                .offset(y = floatOffset.dp)
        ) {
            val w = this.size.width
            val h = this.size.height

            // 1. Aura glow for Recovery or Goal Met
            if (mood == HydriaMascotMood.RECOVERY || mood == HydriaMascotMood.GOAL_MET) {
                val auraColor = if (mood == HydriaMascotMood.GOAL_MET) OceanGold.copy(alpha = 0.25f) else OceanCyan.copy(alpha = 0.35f)
                drawCircle(
                    color = auraColor,
                    radius = (w * 0.44f) * auraPulse,
                    center = Offset(w * 0.5f, h * 0.55f)
                )
            }

            // 2. The Droplet Body Geometry
            val bodyGradient = when (mood) {
                HydriaMascotMood.GOAL_MET -> Brush.verticalGradient(
                    listOf(Color(0xFF4DD0E1), Color(0xFF00B0FF), Color(0xFF0077C2))
                )
                HydriaMascotMood.MISSED_GOAL -> Brush.verticalGradient(
                    listOf(Color(0xFF81D4FA), Color(0xFF4FC3F7), Color(0xFF0288D1).copy(alpha = 0.85f))
                )
                HydriaMascotMood.RECOVERY -> Brush.verticalGradient(
                    listOf(Color(0xFF00E5FF), Color(0xFF0091EA), Color(0xFF01579B))
                )
                else -> Brush.verticalGradient(
                    listOf(MascotDropletBody, OceanPrimary, MascotDropletShade)
                )
            }

            val dropletPath = Path().apply {
                val tipX = w * 0.5f
                val tipY = h * 0.12f

                moveTo(tipX, tipY)
                // Right flank curve down to base
                cubicTo(
                    w * 0.54f, h * 0.18f,
                    w * 0.90f, h * 0.45f,
                    w * 0.90f, h * 0.65f
                )
                // Bottom rounded base
                cubicTo(
                    w * 0.90f, h * 0.90f,
                    w * 0.10f, h * 0.90f,
                    w * 0.10f, h * 0.65f
                )
                // Left flank curve up to tip
                cubicTo(
                    w * 0.10f, h * 0.45f,
                    w * 0.46f, h * 0.18f,
                    tipX, tipY
                )
                close()
            }

            drawPath(path = dropletPath, brush = bodyGradient)

            // Outline stroke for clean graphic definition
            drawPath(
                path = dropletPath,
                color = MascotDropletShade.copy(alpha = 0.3f),
                style = Stroke(width = w * 0.025f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // 3. Glossy 3D Sheen Highlight (upper-left shoulder)
            val sheenPath = Path().apply {
                moveTo(w * 0.32f, h * 0.28f)
                cubicTo(w * 0.22f, h * 0.40f, w * 0.20f, h * 0.55f, w * 0.26f, h * 0.68f)
                cubicTo(w * 0.22f, h * 0.55f, w * 0.25f, h * 0.38f, w * 0.34f, h * 0.28f)
                close()
            }
            drawPath(
                path = sheenPath,
                color = Color.White.copy(alpha = 0.55f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.65f),
                radius = w * 0.04f,
                center = Offset(w * 0.36f, h * 0.25f)
            )

            // 4. Rosy Cheeks
            val cheekAlpha = if (mood == HydriaMascotMood.MISSED_GOAL) 0.3f else 0.75f
            drawCircle(
                color = MascotCheekBlush.copy(alpha = cheekAlpha),
                radius = w * 0.065f,
                center = Offset(w * 0.28f, h * 0.63f)
            )
            drawCircle(
                color = MascotCheekBlush.copy(alpha = cheekAlpha),
                radius = w * 0.065f,
                center = Offset(w * 0.72f, h * 0.63f)
            )

            // 5. Facial Expressions (Eyes & Mouth)
            drawFacialFeatures(mood, w, h)

            // 6. Mood-Specific Props (Crown, Headband, Sweat drop, Stars)
            drawMoodProps(mood, w, h)
        }
    }
}

/**
 * Draws the eyes and mouth according to the mascot mood.
 */
private fun DrawScope.drawFacialFeatures(mood: HydriaMascotMood, w: Float, h: Float) {
    when (mood) {
        HydriaMascotMood.GOAL_MET -> {
            // Joyful closed curved eyes: ^ ^
            val strokeW = w * 0.035f
            val eyePathLeft = Path().apply {
                moveTo(w * 0.32f, h * 0.53f)
                quadraticBezierTo(w * 0.38f, h * 0.47f, w * 0.44f, h * 0.53f)
            }
            val eyePathRight = Path().apply {
                moveTo(w * 0.56f, h * 0.53f)
                quadraticBezierTo(w * 0.62f, h * 0.47f, w * 0.68f, h * 0.53f)
            }
            drawPath(eyePathLeft, MascotEyeColor, style = Stroke(strokeW, cap = StrokeCap.Round))
            drawPath(eyePathRight, MascotEyeColor, style = Stroke(strokeW, cap = StrokeCap.Round))

            // Big open smile with tongue
            val mouthPath = Path().apply {
                moveTo(w * 0.42f, h * 0.66f)
                quadraticBezierTo(w * 0.5f, h * 0.77f, w * 0.58f, h * 0.66f)
                close()
            }
            drawPath(mouthPath, MascotEyeColor, style = Fill)
            drawCircle(
                color = Color(0xFFFF5252),
                radius = w * 0.035f,
                center = Offset(w * 0.5f, h * 0.72f)
            )
        }

        HydriaMascotMood.HALFWAY -> {
            // Left eye: playful wink ^
            val strokeW = w * 0.035f
            val winkPath = Path().apply {
                moveTo(w * 0.32f, h * 0.53f)
                quadraticBezierTo(w * 0.38f, h * 0.47f, w * 0.44f, h * 0.53f)
            }
            drawPath(winkPath, MascotEyeColor, style = Stroke(strokeW, cap = StrokeCap.Round))

            // Right eye: wide open with star sparkle
            val eyeCenterRight = Offset(w * 0.62f, h * 0.51f)
            drawCircle(MascotEyeColor, radius = w * 0.055f, center = eyeCenterRight)
            drawCircle(Color.White, radius = w * 0.022f, center = eyeCenterRight + Offset(-w * 0.015f, -h * 0.015f))

            // Cute happy smile
            val smilePath = Path().apply {
                moveTo(w * 0.44f, h * 0.68f)
                quadraticBezierTo(w * 0.5f, h * 0.74f, w * 0.56f, h * 0.68f)
            }
            drawPath(smilePath, MascotEyeColor, style = Stroke(strokeW, cap = StrokeCap.Round))
        }

        HydriaMascotMood.MISSED_GOAL -> {
            // Droopy sad eyes
            val strokeW = w * 0.032f
            val sadEyeLeft = Path().apply {
                moveTo(w * 0.33f, h * 0.49f)
                quadraticBezierTo(w * 0.39f, h * 0.53f, w * 0.44f, h * 0.50f)
            }
            val sadEyeRight = Path().apply {
                moveTo(w * 0.56f, h * 0.50f)
                quadraticBezierTo(w * 0.61f, h * 0.53f, w * 0.67f, h * 0.49f)
            }
            drawPath(sadEyeLeft, MascotEyeColor, style = Stroke(strokeW, cap = StrokeCap.Round))
            drawPath(sadEyeRight, MascotEyeColor, style = Stroke(strokeW, cap = StrokeCap.Round))

            // Slightly wavy mouth: ~
            val wavyMouth = Path().apply {
                moveTo(w * 0.43f, h * 0.69f)
                quadraticBezierTo(w * 0.47f, h * 0.66f, w * 0.50f, h * 0.69f)
                quadraticBezierTo(w * 0.53f, h * 0.72f, w * 0.57f, h * 0.69f)
            }
            drawPath(wavyMouth, MascotEyeColor, style = Stroke(strokeW, cap = StrokeCap.Round))
        }

        HydriaMascotMood.RECOVERY -> {
            // Determined, energetic eyes with bright glint
            val eyeCenterLeft = Offset(w * 0.38f, h * 0.51f)
            val eyeCenterRight = Offset(w * 0.62f, h * 0.51f)
            val eyeRadius = w * 0.055f

            drawCircle(MascotEyeColor, radius = eyeRadius, center = eyeCenterLeft)
            drawCircle(MascotEyeColor, radius = eyeRadius, center = eyeCenterRight)
            drawCircle(Color.White, radius = w * 0.022f, center = eyeCenterLeft + Offset(-w * 0.015f, -h * 0.015f))
            drawCircle(Color.White, radius = w * 0.022f, center = eyeCenterRight + Offset(-w * 0.015f, -h * 0.015f))

            // Determined heroic smirk
            val strokeW = w * 0.035f
            val smirk = Path().apply {
                moveTo(w * 0.42f, h * 0.67f)
                quadraticBezierTo(w * 0.52f, h * 0.72f, w * 0.60f, h * 0.65f)
            }
            drawPath(smirk, MascotEyeColor, style = Stroke(strokeW, cap = StrokeCap.Round))
        }

        else -> {
            // CHEERFUL_START and PROGRESS: Wide round friendly eyes
            val eyeCenterLeft = Offset(w * 0.38f, h * 0.51f)
            val eyeCenterRight = Offset(w * 0.62f, h * 0.51f)
            val eyeRadius = w * 0.055f

            drawCircle(MascotEyeColor, radius = eyeRadius, center = eyeCenterLeft)
            drawCircle(MascotEyeColor, radius = eyeRadius, center = eyeCenterRight)

            // Two glints per eye for anime-style cuteness
            drawCircle(Color.White, radius = w * 0.024f, center = eyeCenterLeft + Offset(-w * 0.015f, -h * 0.015f))
            drawCircle(Color.White, radius = w * 0.010f, center = eyeCenterLeft + Offset(w * 0.015f, h * 0.015f))
            drawCircle(Color.White, radius = w * 0.024f, center = eyeCenterRight + Offset(-w * 0.015f, -h * 0.015f))
            drawCircle(Color.White, radius = w * 0.010f, center = eyeCenterRight + Offset(w * 0.015f, h * 0.015f))

            // Cheerful smile arc
            val strokeW = w * 0.035f
            val smilePath = Path().apply {
                moveTo(w * 0.43f, h * 0.67f)
                quadraticBezierTo(w * 0.5f, h * 0.74f, w * 0.57f, h * 0.67f)
            }
            drawPath(smilePath, MascotEyeColor, style = Stroke(strokeW, cap = StrokeCap.Round))
        }
    }
}

/**
 * Draws accessories and props (Crown, Trident, Sweat drop, Stars).
 */
private fun DrawScope.drawMoodProps(mood: HydriaMascotMood, w: Float, h: Float) {
    when (mood) {
        HydriaMascotMood.GOAL_MET -> {
            // Golden 3-pointed royal crown resting on droplet tip
            val crownPath = Path().apply {
                moveTo(w * 0.32f, h * 0.14f)
                lineTo(w * 0.28f, h * 0.04f) // Left point
                lineTo(w * 0.42f, h * 0.10f)
                lineTo(w * 0.50f, h * 0.01f) // Center tall point
                lineTo(w * 0.58f, h * 0.10f)
                lineTo(w * 0.72f, h * 0.04f) // Right point
                lineTo(w * 0.68f, h * 0.14f)
                close()
            }
            drawPath(crownPath, OceanGold, style = Fill)
            drawPath(crownPath, Color(0xFFC78B00), style = Stroke(width = w * 0.02f, cap = StrokeCap.Round))

            // Jewel dots on crown peaks
            drawCircle(Color(0xFF00E5FF), radius = w * 0.022f, center = Offset(w * 0.50f, h * 0.01f))
            drawCircle(Color(0xFFFF5252), radius = w * 0.018f, center = Offset(w * 0.28f, h * 0.04f))
            drawCircle(Color(0xFFFF5252), radius = w * 0.018f, center = Offset(w * 0.72f, h * 0.04f))
        }

        HydriaMascotMood.MISSED_GOAL -> {
            // Small sweat/anxiety droplet on the side of head
            val sweatPath = Path().apply {
                moveTo(w * 0.80f, h * 0.32f)
                lineTo(w * 0.85f, h * 0.38f)
                quadraticBezierTo(w * 0.80f, h * 0.44f, w * 0.75f, h * 0.38f)
                close()
            }
            drawPath(sweatPath, Color(0xFF81D4FA), style = Fill)
            drawPath(sweatPath, Color(0xFF0288D1), style = Stroke(width = w * 0.015f))
        }

        HydriaMascotMood.RECOVERY -> {
            // Heroic golden trident emblem on forehead
            val tridentColor = OceanGold
            val tX = w * 0.5f
            val tY = h * 0.32f
            val s = w * 0.08f

            // Central prong
            drawLine(tridentColor, Offset(tX, tY + s), Offset(tX, tY - s * 0.7f), strokeWidth = w * 0.025f, cap = StrokeCap.Round)
            // Left prong
            drawLine(tridentColor, Offset(tX - s * 0.6f, tY + s * 0.4f), Offset(tX - s * 0.6f, tY - s * 0.4f), strokeWidth = w * 0.02f, cap = StrokeCap.Round)
            // Right prong
            drawLine(tridentColor, Offset(tX + s * 0.6f, tY + s * 0.4f), Offset(tX + s * 0.6f, tY - s * 0.4f), strokeWidth = w * 0.02f, cap = StrokeCap.Round)
            // Crossbar
            drawLine(tridentColor, Offset(tX - s * 0.6f, tY + s * 0.4f), Offset(tX + s * 0.6f, tY + s * 0.4f), strokeWidth = w * 0.02f, cap = StrokeCap.Round)
        }

        else -> {}
    }
}

/**
 * Mascot companion speech bubble container.
 */
@Composable
fun HydriaMascotSpeechCard(
    mood: HydriaMascotMood,
    modifier: Modifier = Modifier,
    customMessage: String? = null,
    mascotSize: Dp = 72.dp
) {
    val message = customMessage ?: HydriaMascotHelper.getCompanionMessage(mood)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(WaterSurface)
            .border(1.dp, Color(0xFFD6EBF1), RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("mascot_speech_card"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HydriaMascot(
            mood = mood,
            size = mascotSize
        )
        Spacer(modifier = Modifier.width(14.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFF1F9FB))
                .border(1.dp, Color(0xFFE2F0F4), RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = WaterTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            )
        }
    }
}
