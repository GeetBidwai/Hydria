package com.example.core.ui.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlin.math.abs

/**
 * Data class representing filtered, smoothed physical device tilt.
 * @param roll Lateral tilt angle (-1f to 1f; positive when tilted left, negative when tilted right)
 * @param pitch Forward/backward tilt angle (-1f to 1f)
 */
data class DeviceTilt(
    val roll: Float = 0f,
    val pitch: Float = 0f
) {
    companion object {
        val FLAT = DeviceTilt(roll = 0f, pitch = 0f)

        /**
         * Computes the surface tilt slope for a given roll angle, clamped within stable bounds.
         */
        fun computeSurfaceSlope(roll: Float, maxSlope: Float = 0.55f): Float {
            return (roll * maxSlope).coerceIn(-maxSlope, maxSlope)
        }

        /**
         * Calculates normalized water level height fraction based on current intake and target.
         */
        fun calculateWaterHeightFraction(consumedMl: Int, targetMl: Int): Float {
            if (targetMl <= 0) return 0f
            return (consumedMl.toFloat() / targetMl.toFloat()).coerceIn(0f, 1f)
        }
    }
}

/**
 * Remembers and updates smoothed device tilt using Android's accelerometer.
 * Registers sensors only while the Composable is actively resumed, and cleanly
 * disposes of sensor listeners on pause or disposal to conserve battery.
 */
@Composable
fun rememberDeviceTilt(
    enabled: Boolean = true,
    smoothingFactor: Float = 0.16f
): DeviceTilt {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var rawRoll by remember { mutableFloatStateOf(0f) }
    var rawPitch by remember { mutableFloatStateOf(0f) }

    DisposableEffect(enabled, lifecycleOwner) {
        if (!enabled) return@DisposableEffect onDispose {}

        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (sensorManager == null || accelerometer == null) {
            return@DisposableEffect onDispose {}
        }

        var smoothedRoll = 0f
        var smoothedPitch = 0f

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                val x = event.values[0] // Lateral acceleration (-9.81 to +9.81)
                val y = event.values[1] // Longitudinal acceleration
                val z = event.values[2] // Perpendicular acceleration

                // Normalize roll: positive when phone tilted left, negative when tilted right
                val targetRoll = (x / 9.81f).coerceIn(-1f, 1f)
                val targetPitch = (y / 9.81f).coerceIn(-1f, 1f)

                // Low-pass filter to eliminate accelerometer jitter and sudden noise
                smoothedRoll += (targetRoll - smoothedRoll) * smoothingFactor
                smoothedPitch += (targetPitch - smoothedPitch) * smoothingFactor

                // Deadzone around flat level (within ±0.03) to let liquid settle smoothly
                val settledRoll = if (abs(smoothedRoll) < 0.03f) 0f else smoothedRoll
                val settledPitch = if (abs(smoothedPitch) < 0.03f) 0f else smoothedPitch

                rawRoll = settledRoll
                rawPitch = settledPitch
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    sensorManager.registerListener(
                        listener,
                        accelerometer,
                        SensorManager.SENSOR_DELAY_UI
                    )
                }
                Lifecycle.Event.ON_PAUSE -> {
                    sensorManager.unregisterListener(listener)
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        sensorManager.registerListener(
            listener,
            accelerometer,
            SensorManager.SENSOR_DELAY_UI
        )

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            sensorManager.unregisterListener(listener)
        }
    }

    // Spring interpolation for fluid liquid inertia and settling effect
    val animatedRoll by animateFloatAsState(
        targetValue = rawRoll,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "liquid_roll_tilt"
    )

    val animatedPitch by animateFloatAsState(
        targetValue = rawPitch,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "liquid_pitch_tilt"
    )

    return DeviceTilt(roll = animatedRoll, pitch = animatedPitch)
}
