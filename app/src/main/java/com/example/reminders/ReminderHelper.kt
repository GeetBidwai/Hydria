package com.example.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import java.util.Calendar

object ReminderHelper {
    /**
     * Updated channel ID to migrate existing devices from the silent/default channel
     * to an explicitly configured audible channel with sound and vibration.
     */
    const val CHANNEL_ID = "water_reminders_channel_v2"
    const val CHANNEL_NAME = "Hydration Reminders"
    const val CHANNEL_DESCRIPTION = "Audible reminders to drink water during waking hours"
    const val NOTIFICATION_ID = 1001

    /**
     * Creates or updates the notification channel on Android 8.0 (API 26) and higher
     * with system-default notification sound, appropriate audio attributes, and vibration.
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
                setSound(defaultSoundUri, audioAttributes)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Checks if notifications or the channel sound is muted at the system level.
     */
    fun isChannelSoundSilenced(context: Context): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            return true
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return false
            val channel = notificationManager.getNotificationChannel(CHANNEL_ID)
            if (channel != null) {
                return channel.importance < NotificationManager.IMPORTANCE_DEFAULT || channel.sound == null
            }
        }
        return false
    }

    /**
     * Opens the Android system notification channel settings directly.
     */
    fun openNotificationSettings(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    putExtra(Settings.EXTRA_CHANNEL_ID, CHANNEL_ID)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } else {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(fallbackIntent)
        }
    }

    /**
     * Posts an audible hydration reminder notification if permissions allow.
     */
    fun showHydrationNotification(
        context: Context,
        consumedMl: Int = 0,
        targetMl: Int = 2500,
        preferredUnit: String = "ml"
    ): Boolean {
        createNotificationChannel(context)

        // Android 13+ runtime notification permission check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionStatus = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val bodyText = if (targetMl > 0) {
            if (preferredUnit == "litres") {
                val consumedL = String.format(java.util.Locale.US, "%.2f", consumedMl / 1000f)
                val targetL = String.format(java.util.Locale.US, "%.2f", targetMl / 1000f)
                "You've logged $consumedL L of $targetL L today. Take a moment to drink a glass of water!"
            } else {
                "You've logged $consumedMl ml of $targetMl ml today. Take a moment to drink a glass of water!"
            }
        } else {
            "Stay refreshed! Remember to drink a glass of water."
        }

        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Time to hydrate! 💧")
            .setContentText(bodyText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(defaultSoundUri)
            .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        return try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
            true
        } catch (e: SecurityException) {
            false
        }
    }

    /**
     * Parses a "HH:mm" formatted time string into minutes since midnight (0..1439).
     */
    fun parseTimeToMinutes(timeString: String, defaultMinutes: Int): Int {
        val parts = timeString.trim().split(":")
        if (parts.size != 2) return defaultMinutes
        val hours = parts[0].toIntOrNull() ?: return defaultMinutes
        val minutes = parts[1].toIntOrNull() ?: return defaultMinutes
        if (hours !in 0..23 || minutes !in 0..59) return defaultMinutes
        return hours * 60 + minutes
    }

    /**
     * Checks if a given time (in minutes from midnight) is within the waking hours range.
     * Accurately supports both regular hours (e.g. 08:00 to 22:00) and overnight hours (e.g. 22:00 to 06:00).
     */
    fun isWithinWakingHours(
        currentMinutes: Int,
        wakeTimeString: String,
        sleepTimeString: String
    ): Boolean {
        val wakeMinutes = parseTimeToMinutes(wakeTimeString, defaultMinutes = 8 * 60)
        val sleepMinutes = parseTimeToMinutes(sleepTimeString, defaultMinutes = 22 * 60)

        return if (wakeMinutes <= sleepMinutes) {
            currentMinutes in wakeMinutes..sleepMinutes
        } else {
            // Overnight schedule: wake up at 22:00, sleep at 06:00
            currentMinutes >= wakeMinutes || currentMinutes <= sleepMinutes
        }
    }

    fun isWithinWakingHours(
        calendar: Calendar,
        wakeTimeString: String,
        sleepTimeString: String
    ): Boolean {
        val currentMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        return isWithinWakingHours(currentMinutes, wakeTimeString, sleepTimeString)
    }

    /**
     * Checks if the calendar day is a weekend (Saturday or Sunday).
     */
    fun isWeekend(calendar: Calendar = Calendar.getInstance()): Boolean {
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
    }

    /**
     * Checks if daily water goal has already been achieved.
     */
    fun isDailyGoalReached(consumedMl: Int, targetMl: Int): Boolean {
        return targetMl > 0 && consumedMl >= targetMl
    }
}
