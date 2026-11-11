package com.example.presentation.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.core.ui.components.WaterCard
import com.example.data.repository.WaterRepository
import com.example.domain.calculator.HydrationCalculator
import com.example.domain.model.ActivityLevel
import com.example.domain.model.Climate
import com.example.domain.model.UserProfile
import com.example.presentation.components.HydriaMascot
import com.example.presentation.components.HydriaMascotMood
import com.example.reminders.ReminderHelper
import com.example.reminders.ReminderScheduler
import com.example.ui.theme.OceanAqua
import com.example.ui.theme.OceanCyan
import com.example.ui.theme.OceanDeep
import com.example.ui.theme.WaterAccentSuccess
import com.example.ui.theme.WaterBackground
import com.example.ui.theme.WaterOutline
import com.example.ui.theme.WaterPrimary
import com.example.ui.theme.WaterPrimaryContainer
import com.example.ui.theme.WaterSurface
import com.example.ui.theme.WaterTextPrimary
import com.example.ui.theme.WaterTextSecondary
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun SettingsScreen(
    repository: WaterRepository,
    onNavigateToCalculator: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val userProfile by repository.userProfile.collectAsState(initial = UserProfile())
    val dailyTargetMl by repository.dailyTargetMl.collectAsState(initial = 2500)
    val preferredUnit by repository.preferredUnit.collectAsState(initial = "ml")

    val remindersEnabled by repository.remindersEnabled.collectAsState(initial = true)
    val reminderIntervalMinutes by repository.reminderIntervalMinutes.collectAsState(initial = 120)
    val reminderWeekendsEnabled by repository.reminderWeekendsEnabled.collectAsState(initial = true)
    val wakeTime by repository.wakeTime.collectAsState(initial = "08:00")
    val sleepTime by repository.sleepTime.collectAsState(initial = "22:00")
    val widgetQuickAddMl by repository.widgetQuickAddMl.collectAsState(initial = 300)

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showUnitDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showIntervalDialog by remember { mutableStateOf(false) }
    var showWakingHoursDialog by remember { mutableStateOf(false) }
    var showPermissionRationaleDialog by remember { mutableStateOf(false) }
    var showWidgetQuickAddDialog by remember { mutableStateOf(false) }
    var hasPromptedNotificationPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            coroutineScope.launch {
                repository.setRemindersEnabled(true)
                ReminderScheduler.scheduleReminders(context, reminderIntervalMinutes)
                Toast.makeText(context, "Hydration reminders enabled", Toast.LENGTH_SHORT).show()
            }
        } else {
            hasPromptedNotificationPermission = true
            Toast.makeText(
                context,
                "Notification permission denied. Enable in Settings to receive alerts.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(WaterBackground)
            .testTag("settings_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = WaterTextPrimary,
                    fontSize = 24.sp
                ),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // ==========================================
        // SECTION A: HYDRATION PROFILE
        // ==========================================
        item {
            WaterCard(testTag = "settings_profile_card") {
                SectionHeader("HYDRATION PROFILE")

                val targetDisplay = if (preferredUnit == "litres") {
                    String.format(Locale.US, "%.2f L / day", dailyTargetMl / 1000f)
                } else {
                    "$dailyTargetMl ml / day"
                }

                SettingsActionRow(
                    icon = Icons.Default.Calculate,
                    title = "Daily Target",
                    subtitle = targetDisplay,
                    onClick = onNavigateToCalculator,
                    testTag = "settings_target_row"
                )

                HorizontalDivider(color = WaterOutline.copy(alpha = 0.4f))

                SettingsActionRow(
                    icon = Icons.Default.Straighten,
                    title = "Unit of Measurement",
                    subtitle = if (preferredUnit == "litres") "Litres (L)" else "Milliliters (ml)",
                    onClick = { showUnitDialog = true },
                    testTag = "settings_units_row"
                )

                HorizontalDivider(color = WaterOutline.copy(alpha = 0.4f))

                val personalSubtitle = buildString {
                    if (userProfile.name.isNotBlank()) append("${userProfile.name} • ")
                    append("${userProfile.weightKg.toInt()} kg • Age ${userProfile.age} • ${userProfile.activityLevel.label}")
                }

                SettingsActionRow(
                    icon = Icons.Default.Person,
                    title = "Personal Information & Activity",
                    subtitle = personalSubtitle,
                    onClick = { showEditProfileDialog = true },
                    testTag = "settings_info_row"
                )
            }
        }

        // ==========================================
        // SECTION B: REMINDER SCHEDULE
        // ==========================================
        item {
            WaterCard(testTag = "settings_reminders_card") {
                SectionHeader("REMINDER SCHEDULE")

                // Main Master Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        SettingsIcon(Icons.Default.Notifications)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Hydration Reminders",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = WaterTextPrimary
                                )
                            )
                            Text(
                                text = if (remindersEnabled) {
                                    "Active from $wakeTime to $sleepTime"
                                } else {
                                    "Disabled"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = WaterTextSecondary,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    Switch(
                        checked = remindersEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (!hasPermission) {
                                        if (hasPromptedNotificationPermission) {
                                            showPermissionRationaleDialog = true
                                        } else {
                                            hasPromptedNotificationPermission = true
                                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        }
                                        return@Switch
                                    }
                                }
                                coroutineScope.launch {
                                    repository.setRemindersEnabled(true)
                                    ReminderScheduler.scheduleReminders(context, reminderIntervalMinutes)
                                }
                            } else {
                                coroutineScope.launch {
                                    repository.setRemindersEnabled(false)
                                    ReminderScheduler.cancelReminders(context)
                                }
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = OceanAqua,
                            checkedTrackColor = OceanAqua.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier.testTag("settings_reminder_switch")
                    )
                }

                if (remindersEnabled) {
                    HorizontalDivider(color = WaterOutline.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 4.dp))

                    SettingsActionRow(
                        icon = Icons.Default.Alarm,
                        title = "Reminder Interval",
                        subtitle = "Every $reminderIntervalMinutes minutes (30, 60, 90, 120)",
                        onClick = { showIntervalDialog = true },
                        testTag = "settings_interval_row"
                    )

                    HorizontalDivider(color = WaterOutline.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 4.dp))

                    SettingsActionRow(
                        icon = Icons.Default.Bedtime,
                        title = "Waking Hours",
                        subtitle = "$wakeTime to $sleepTime",
                        onClick = { showWakingHoursDialog = true },
                        testTag = "settings_waking_hours_row"
                    )

                    HorizontalDivider(color = WaterOutline.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 4.dp))

                    // Weekend Reminders Toggle Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            SettingsIcon(Icons.Default.Today)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Weekend Reminders",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = WaterTextPrimary
                                    )
                                )
                                Text(
                                    text = if (reminderWeekendsEnabled) "Notify on Sat & Sun" else "Silenced on weekends",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = WaterTextSecondary,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }

                        Switch(
                            checked = reminderWeekendsEnabled,
                            onCheckedChange = { enabled ->
                                coroutineScope.launch {
                                    repository.setReminderWeekendsEnabled(enabled)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = OceanAqua,
                                checkedTrackColor = OceanAqua.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier.testTag("settings_weekend_switch")
                        )
                    }
                }
            }
        }

        // ==========================================
        // SECTION C: NOTIFICATIONS & BATTERY GUIDANCE
        // ==========================================
        item {
            WaterCard(testTag = "settings_notifications_card") {
                SectionHeader("NOTIFICATIONS & RELIABILITY")

                val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                } else true

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        SettingsIcon(Icons.Default.Notifications)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "System Notification Permission",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = WaterTextPrimary
                                )
                            )
                            Text(
                                text = if (hasNotificationPermission) "Granted & active" else "Not granted - alerts may not show",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (hasNotificationPermission) WaterAccentSuccess else MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    if (!hasNotificationPermission) {
                        TextButton(onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }) {
                            Text("Grant", color = OceanAqua, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                HorizontalDivider(color = WaterOutline.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 4.dp))

                // Battery Optimization Guidance Card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = OceanDeep.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, OceanAqua.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.BatteryAlert,
                            contentDescription = null,
                            tint = OceanAqua,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Background Reminder Guidance",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = WaterTextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Hydria uses Android WorkManager to deliver offline reminders. Modern Android devices may optimize battery by batching background jobs when idle. If alerts are delayed, consider setting battery usage to 'Unrestricted' in your device settings for Hydria.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = WaterTextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            )
                        }
                    }
                }

                HorizontalDivider(color = WaterOutline.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 4.dp))

                SettingsActionRow(
                    icon = Icons.Default.VolumeUp,
                    title = "Sound & System Alert Settings",
                    subtitle = "Adjust system banner, sound chime, and vibration",
                    onClick = { ReminderHelper.openNotificationSettings(context) },
                    testTag = "settings_sound_alerts_row"
                )

                HorizontalDivider(color = WaterOutline.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 4.dp))

                SettingsActionRow(
                    icon = Icons.Default.NotificationsActive,
                    title = "Send Test Reminder",
                    subtitle = "Trigger an ocean-themed hydration alert now",
                    onClick = {
                        val dispatched = ReminderHelper.showHydrationNotification(
                            context = context,
                            consumedMl = 600,
                            targetMl = dailyTargetMl,
                            preferredUnit = preferredUnit
                        )
                        if (dispatched) {
                            Toast.makeText(context, "Test reminder sent!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Notification permission required", Toast.LENGTH_SHORT).show()
                        }
                    },
                    testTag = "settings_test_reminder_row"
                )
            }
        }

        // ==========================================
        // SECTION D: HOME SCREEN WIDGETS
        // ==========================================
        item {
            WaterCard(testTag = "settings_widget_card") {
                SectionHeader("HOME SCREEN WIDGETS")

                SettingsActionRow(
                    icon = Icons.Default.Widgets,
                    title = "Default Quick Add Preset",
                    subtitle = "$widgetQuickAddMl ml per tap (2×1 Compact & 4×2 Expanded)",
                    onClick = { showWidgetQuickAddDialog = true },
                    testTag = "settings_widget_quick_add_row"
                )

                HorizontalDivider(color = WaterOutline.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(OceanAqua.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = OceanAqua,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "2 Native Widget Options Available",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = WaterTextPrimary
                            )
                        )
                        Text(
                            text = "Add Hydria widgets from your home screen launcher for instant one-tap tracking.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = WaterTextSecondary,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }

        // ==========================================
        // SECTION E: APP INFORMATION & DATA
        // ==========================================
        item {
            WaterCard(testTag = "settings_about_card") {
                SectionHeader("APP INFORMATION & DATA")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HydriaMascot(
                        mood = HydriaMascotMood.CHEERFUL_START,
                        size = 46.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Hydria v1.2",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = WaterTextPrimary
                            )
                        )
                        Text(
                            text = "Ocean-Themed Companion • 100% Offline • Local Room Storage",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = WaterTextSecondary,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                HorizontalDivider(color = WaterOutline.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 6.dp))

                SettingsActionRow(
                    icon = Icons.Default.Refresh,
                    title = "Revisit Onboarding Guide",
                    subtitle = "Walk through the 5-step guided setup again",
                    onClick = onNavigateToOnboarding,
                    testTag = "settings_onboarding_row"
                )

                HorizontalDivider(color = WaterOutline.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 4.dp))

                SettingsActionRow(
                    icon = Icons.Default.DeleteOutline,
                    title = "Clear Intake History",
                    subtitle = "Clear all logged entries (preserves profile and targets)",
                    onClick = { showClearHistoryDialog = true },
                    testTag = "settings_clear_history_row"
                )
            }
        }
    }

    // Interval Dialog
    if (showIntervalDialog) {
        AlertDialog(
            onDismissRequest = { showIntervalDialog = false },
            title = {
                Text(
                    text = "Reminder Interval",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(30, 60, 90, 120).forEach { interval ->
                        val isSelected = reminderIntervalMinutes == interval
                        val label = when (interval) {
                            30 -> "Every 30 minutes"
                            60 -> "Every 60 minutes"
                            90 -> "Every 90 minutes"
                            120 -> "Every 120 minutes (2 hours, Recommended)"
                            else -> "Every $interval minutes"
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    coroutineScope.launch {
                                        repository.setReminderIntervalMinutes(interval)
                                        if (remindersEnabled) {
                                            ReminderScheduler.scheduleReminders(context, interval)
                                        }
                                        showIntervalDialog = false
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    coroutineScope.launch {
                                        repository.setReminderIntervalMinutes(interval)
                                        if (remindersEnabled) {
                                            ReminderScheduler.scheduleReminders(context, interval)
                                        }
                                        showIntervalDialog = false
                                    }
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = OceanAqua)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) OceanAqua else WaterTextPrimary
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showIntervalDialog = false }) {
                    Text("Close", color = OceanAqua)
                }
            },
            containerColor = WaterSurface
        )
    }

    // Waking Hours Dialog
    if (showWakingHoursDialog) {
        var startInput by remember { mutableStateOf(wakeTime) }
        var endInput by remember { mutableStateOf(sleepTime) }
        var errorText by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showWakingHoursDialog = false },
            title = {
                Text(
                    text = "Set Waking Hours",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Reminders will only fire between these hours (24-hour HH:mm format).",
                        style = MaterialTheme.typography.bodySmall.copy(color = WaterTextSecondary)
                    )

                    OutlinedTextField(
                        value = startInput,
                        onValueChange = {
                            startInput = it.take(5)
                            errorText = null
                        },
                        label = { Text("Wake-up Time (e.g. 08:00)") },
                        placeholder = { Text("08:00") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = endInput,
                        onValueChange = {
                            endInput = it.take(5)
                            errorText = null
                        },
                        label = { Text("Bedtime (e.g. 22:00)") },
                        placeholder = { Text("22:00") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (errorText != null) {
                        Text(
                            text = errorText ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val timeRegex = Regex("^([01]?[0-9]|2[0-3]):[0-5][0-9]\$")
                        val cleanStart = startInput.trim()
                        val cleanEnd = endInput.trim()

                        if (!cleanStart.matches(timeRegex) || !cleanEnd.matches(timeRegex)) {
                            errorText = "Please enter valid 24h times (HH:mm, e.g. 08:00 and 22:00)"
                        } else {
                            coroutineScope.launch {
                                repository.setWakingHours(cleanStart, cleanEnd)
                                showWakingHoursDialog = false
                            }
                        }
                    }
                ) {
                    Text("Save", color = OceanAqua, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWakingHoursDialog = false }) {
                    Text("Cancel", color = WaterTextSecondary)
                }
            },
            containerColor = WaterSurface
        )
    }

    // Unit Dialog
    if (showUnitDialog) {
        AlertDialog(
            onDismissRequest = { showUnitDialog = false },
            title = {
                Text(
                    text = "Measurement Unit",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                coroutineScope.launch {
                                    repository.setPreferredUnit("ml")
                                    showUnitDialog = false
                                }
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Milliliters (ml)",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (preferredUnit == "ml") FontWeight.Bold else FontWeight.Normal,
                                color = if (preferredUnit == "ml") OceanAqua else WaterTextPrimary
                            )
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                coroutineScope.launch {
                                    repository.setPreferredUnit("litres")
                                    showUnitDialog = false
                                }
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Litres (L)",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (preferredUnit == "litres") FontWeight.Bold else FontWeight.Normal,
                                color = if (preferredUnit == "litres") OceanAqua else WaterTextPrimary
                            )
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showUnitDialog = false }) {
                    Text("Close", color = OceanAqua)
                }
            },
            containerColor = WaterSurface
        )
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(userProfile.name) }
        var editAge by remember { mutableStateOf(userProfile.age.toString()) }
        var editWeight by remember { mutableStateOf(userProfile.weightKg.toInt().toString()) }
        var editActivity by remember { mutableStateOf(userProfile.activityLevel) }
        var editExercise by remember { mutableStateOf(userProfile.exerciseMinutes.toString()) }
        var dialogError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = {
                Text(
                    text = "Edit Profile & Activity",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editAge,
                        onValueChange = { editAge = it.filter { ch -> ch.isDigit() }.take(3) },
                        label = { Text("Age (Years)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editWeight,
                        onValueChange = { editWeight = it.filter { ch -> ch.isDigit() || ch == '.' }.take(5) },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editExercise,
                        onValueChange = { editExercise = it.filter { ch -> ch.isDigit() }.take(3) },
                        label = { Text("Daily Exercise (Minutes)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Activity Level:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    ActivityLevel.entries.forEach { level ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { editActivity = level }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = editActivity == level,
                                onClick = { editActivity = level },
                                colors = RadioButtonDefaults.colors(selectedColor = OceanAqua)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = level.label,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (editActivity == level) FontWeight.Bold else FontWeight.Normal,
                                    color = if (editActivity == level) OceanAqua else WaterTextPrimary
                                )
                            )
                        }
                    }

                    if (dialogError != null) {
                        Text(
                            text = dialogError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val ageInt = editAge.toIntOrNull()
                        val weightFloat = editWeight.toFloatOrNull()
                        val exerciseInt = editExercise.toIntOrNull() ?: 0
                        when {
                            ageInt == null || ageInt !in 10..120 -> {
                                dialogError = "Please enter age between 10 and 120"
                            }
                            weightFloat == null || weightFloat !in 20f..300f -> {
                                dialogError = "Please enter weight between 20 and 300 kg"
                            }
                            else -> {
                                coroutineScope.launch {
                                    val updated = userProfile.copy(
                                        name = editName.trim(),
                                        age = ageInt,
                                        weightKg = weightFloat,
                                        activityLevel = editActivity,
                                        exerciseMinutes = exerciseInt
                                    )
                                    val newTarget = HydrationCalculator.calculateDailyTargetMl(updated)
                                    repository.saveProfile(updated, calculatedTargetMl = newTarget)
                                    showEditProfileDialog = false
                                }
                            }
                        }
                    }
                ) {
                    Text("Save", color = OceanAqua, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel", color = WaterTextSecondary)
                }
            },
            containerColor = WaterSurface
        )
    }

    // Clear History Dialog
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = {
                Text(
                    text = "Clear Intake History?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "This will erase all recorded daily water logs from Room. Your profile, settings, and hydration targets will remain intact.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = WaterTextSecondary)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            repository.clearHistory()
                            showClearHistoryDialog = false
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear Logs", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel", color = WaterTextSecondary)
                }
            },
            containerColor = WaterSurface
        )
    }

    // Permission Rationale Dialog
    if (showPermissionRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionRationaleDialog = false },
            title = {
                Text(
                    text = "Notification Permission Needed",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "Hydration reminders require notification permission. Please open system settings and enable notifications for Hydria.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = WaterTextSecondary)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionRationaleDialog = false
                        ReminderHelper.openNotificationSettings(context)
                    }
                ) {
                    Text("Open Settings", color = OceanAqua, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionRationaleDialog = false }) {
                    Text("Not Now", color = WaterTextSecondary)
                }
            },
            containerColor = WaterSurface
        )
    }

    // Widget Quick Add Amount Configuration Dialog
    if (showWidgetQuickAddDialog) {
        val presets = listOf(150, 200, 250, 300, 350, 400, 500)
        AlertDialog(
            onDismissRequest = { showWidgetQuickAddDialog = false },
            title = {
                Text(
                    text = "Default Quick Add Amount",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Choose the primary volume logged with one tap on home screen widgets:",
                        style = MaterialTheme.typography.bodySmall.copy(color = WaterTextSecondary),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    presets.forEach { preset ->
                        val isSelected = widgetQuickAddMl == preset
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    coroutineScope.launch {
                                        repository.setWidgetQuickAddMl(preset)
                                        showWidgetQuickAddDialog = false
                                    }
                                }
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    coroutineScope.launch {
                                        repository.setWidgetQuickAddMl(preset)
                                        showWidgetQuickAddDialog = false
                                    }
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = OceanAqua)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$preset ml" + if (preset == 300) " (Default)" else "",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) OceanAqua else WaterTextPrimary
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showWidgetQuickAddDialog = false }) {
                    Text("Close", color = OceanAqua)
                }
            },
            containerColor = WaterSurface
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            color = WaterTextSecondary,
            letterSpacing = 1.sp
        ),
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            SettingsIcon(icon)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = WaterTextPrimary,
                        fontSize = 15.sp
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = WaterTextSecondary,
                        fontSize = 12.sp
                    )
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = WaterTextSecondary,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun SettingsIcon(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(OceanAqua.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = OceanAqua,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = OceanAqua,
    unfocusedBorderColor = WaterOutline,
    focusedContainerColor = WaterSurface,
    unfocusedContainerColor = WaterSurface,
    focusedTextColor = WaterTextPrimary,
    unfocusedTextColor = WaterTextPrimary
)
