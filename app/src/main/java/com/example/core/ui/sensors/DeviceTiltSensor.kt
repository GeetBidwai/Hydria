package com.example.core.ui.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.view.Surface
import android.view.WindowManager
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
import kotlin.math.atan2
import kotlin.math.hypot

/**
 * Data class representing filtered, smoothed physical gravity orientation for liquid physics.
 *
 * @param roll Lateral tilt normalized (-1f to 1f)
 * @param pitch Longitudinal tilt normalized (-1f to 1f)
 * @param gravityAngleDegrees Continuous unwrapped rotation angle (in degrees) of the physical
 *   gravity direction relative to Canvas down (+Y). 0° = upright bottom, +90° = landscape left,
 *   -90° = landscape right, ±180° = upside down.
 * @param gravityX Normalized X component of gravity in Canvas coordinates (+X right)
 * @param gravityY Normalized Y component of gravity in Canvas coordinates (+Y down)
 * @param sloshFactor Normalized dynamic motion factor (0f at rest, up to 1f during rapid rotation)
 */
data class DeviceTilt(
    val roll: Float = 0f,
    val pitch: Float = 0f,
    val gravityAngleDegrees: Float = 0f,
    val gravityX: Float = 0f,
    val gravityY: Float = 1f,
    val sloshFactor: Float = 0f
) {
    companion object {
        val FLAT = DeviceTilt(
            roll = 0f,
            pitch = 0f,
            gravityAngleDegrees = 0f,
            gravityX = 0f,
            gravityY = 1f,
            sloshFactor = 0f
        )

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

        /**
         * Maps Android hardware sensor (x, y) readings (which measure upward support force
         * in device-natural coordinates) to the physical downward gravity vector (gx, gy)
         * in Compose Canvas coordinates (+X right, +Y down), accounting for [displayRotation].
         */
        fun mapSensorToCanvasGravity(
            sensorX: Float,
            sensorY: Float,
            displayRotation: Int = Surface.ROTATION_0
        ): Pair<Float, Float> {
            return when (displayRotation) {
                Surface.ROTATION_90 -> Pair(sensorY, sensorX)
                Surface.ROTATION_180 -> Pair(sensorX, -sensorY)
                Surface.ROTATION_270 -> Pair(-sensorY, -sensorX)
                else -> Pair(-sensorX, sensorY) // Surface.ROTATION_0
            }
        }

        /**
         * Converts a Canvas gravity vector (gx, gy) into a rotation angle in degrees
         * for `DrawScope.rotate(degrees, pivot)`, where 0° points straight down (0, +1),
         * +90° points left (-1, 0), -90° points right (+1, 0), and ±180° points up (0, -1).
         */
        fun computeGravityAngleDegrees(gravityX: Float, gravityY: Float): Float {
            if (gravityX == 0f && gravityY == 0f) return 0f
            return Math.toDegrees(atan2(-gravityX.toDouble(), gravityY.toDouble())).toFloat()
        }

        /**
         * Computes the shortest-arc unwrapped target angle relative to [currentContinuousDegrees]
         * so 360-degree rotations never jump backward across the ±180° boundary.
         */
        fun unwrapAngleDegrees(currentContinuousDegrees: Float, newRawDegrees: Float): Float {
            var delta = (newRawDegrees - currentContinuousDegrees) % 360f
            if (delta > 180f) delta -= 360f
            if (delta < -180f) delta += 360f
            return currentContinuousDegrees + delta
        }
    }
}

@Suppress("DEPRECATION")
private fun getSafeDisplayRotation(context: Context): Int {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.display?.rotation ?: Surface.ROTATION_0
        } else {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            wm?.defaultDisplay?.rotation ?: Surface.ROTATION_0
        }
    } catch (_: Exception) {
        Surface.ROTATION_0
    }
}

/**
 * Remembers and updates smoothed 360-degree gravity orientation using Android's
 * Gravity sensor (preferred) or Accelerometer (fallback with low-pass filtering).
 *
 * Registers sensors only while the Composable is actively resumed, and cleanly
 * disposes of sensor listeners on pause or disposal to conserve battery.
 */
@Composable
fun rememberDeviceTilt(
    enabled: Boolean = true,
    smoothingFactor: Float = 0.20f
): DeviceTilt {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var rawRoll by remember { mutableFloatStateOf(0f) }
    var rawPitch by remember { mutableFloatStateOf(0f) }
    var targetGravityX by remember { mutableFloatStateOf(0f) }
    var targetGravityY by remember { mutableFloatStateOf(1f) }
    var continuousAngleTarget by remember { mutableFloatStateOf(0f) }

    DisposableEffect(enabled, lifecycleOwner) {
        if (!enabled) return@DisposableEffect onDispose {}

        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val gravitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val activeSensor = gravitySensor ?: accelerometer

        if (sensorManager == null || activeSensor == null) {
            return@DisposableEffect onDispose {}
        }

        val isGravitySensor = activeSensor.type == Sensor.TYPE_GRAVITY
        // Gravity sensor is already sensor-fused; accelerometer benefits from stronger low-pass filtering
        val effectiveAlpha = if (isGravitySensor) {
            smoothingFactor.coerceIn(0.15f, 0.45f)
        } else {
            (smoothingFactor * 0.75f).coerceIn(0.08f, 0.25f)
        }

        var filteredX = 0f
        var filteredY = 9.81f
        var initialized = false
        var currentContinuousAngle = continuousAngleTarget
        var isRegistered = false

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                val sx = event.values[0]
                val sy = event.values[1]

                if (!initialized) {
                    filteredX = sx
                    filteredY = sy
                    initialized = true
                } else {
                    filteredX += (sx - filteredX) * effectiveAlpha
                    filteredY += (sy - filteredY) * effectiveAlpha
                }

                val displayRotation = getSafeDisplayRotation(context)
                val (gx, gy) = DeviceTilt.mapSensorToCanvasGravity(
                    sensorX = filteredX,
                    sensorY = filteredY,
                    displayRotation = displayRotation
                )

                val inPlaneMag = hypot(gx, gy)
                val rollNorm = (-gx / 9.81f).coerceIn(-1f, 1f)
                val pitchNorm = (gy / 9.81f).coerceIn(-1f, 1f)
                rawRoll = if (abs(rollNorm) < 0.03f) 0f else rollNorm
                rawPitch = if (abs(pitchNorm) < 0.03f) 0f else pitchNorm

                // Flat-device hysteresis:
                // When device is tilted > ~7° (inPlaneMag >= 1.2 m/s^2), track exact 360° gravity angle.
                // When laid nearly flat on a table (inPlaneMag < 0.75 m/s^2), gently settle to bottom (0°).
                if (inPlaneMag >= 1.2f) {
                    val normGx = gx / inPlaneMag
                    val normGy = gy / inPlaneMag
                    targetGravityX = normGx
                    targetGravityY = normGy

                    val rawAngle = DeviceTilt.computeGravityAngleDegrees(normGx, normGy)
                    val unwrapped = DeviceTilt.unwrapAngleDegrees(currentContinuousAngle, rawAngle)
                    if (abs(unwrapped - currentContinuousAngle) >= 0.5f) {
                        currentContinuousAngle = unwrapped
                        continuousAngleTarget = unwrapped
                    }
                } else if (inPlaneMag < 0.75f) {
                    targetGravityX = 0f
                    targetGravityY = 1f
                    val unwrappedFlat = DeviceTilt.unwrapAngleDegrees(currentContinuousAngle, 0f)
                    if (abs(unwrappedFlat - currentContinuousAngle) >= 0.5f) {
                        currentContinuousAngle = unwrappedFlat
                        continuousAngleTarget = unwrappedFlat
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        fun registerIfNeeded() {
            if (!isRegistered) {
                isRegistered = sensorManager.registerListener(
                    listener,
                    activeSensor,
                    SensorManager.SENSOR_DELAY_UI
                )
            }
        }

        fun unregisterIfNeeded() {
            if (isRegistered) {
                sensorManager.unregisterListener(listener)
                isRegistered = false
            }
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> registerIfNeeded()
                Lifecycle.Event.ON_PAUSE -> unregisterIfNeeded()
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            registerIfNeeded()
        }

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            unregisterIfNeeded()
        }
    }

    // Spring interpolation for fluid 360-degree liquid settling and natural inertia
    val animatedGravityAngle by animateFloatAsState(
        targetValue = continuousAngleTarget,
        animationSpec = spring(
            dampingRatio = 0.68f,
            stiffness = 110f
        ),
        label = "liquid_gravity_angle"
    )

    val animatedRoll by animateFloatAsState(
        targetValue = rawRoll,
        animationSpec = spring(
            dampingRatio = 0.70f,
            stiffness = 140f
        ),
        label = "liquid_roll_tilt"
    )

    val animatedPitch by animateFloatAsState(
        targetValue = rawPitch,
        animationSpec = spring(
            dampingRatio = 0.85f,
            stiffness = 140f
        ),
        label = "liquid_pitch_tilt"
    )

    val sloshFactor = (abs(continuousAngleTarget - animatedGravityAngle) / 45f).coerceIn(0f, 1f)

    return DeviceTilt(
        roll = animatedRoll,
        pitch = animatedPitch,
        gravityAngleDegrees = animatedGravityAngle,
        gravityX = targetGravityX,
        gravityY = targetGravityY,
        sloshFactor = sloshFactor
    )
}
