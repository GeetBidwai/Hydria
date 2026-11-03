package com.example.presentation.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.components.WaterCard
import com.example.data.repository.WaterRepository
import com.example.domain.model.UserProfile
import com.example.reminders.ReminderHelper
import com.example.reminders.ReminderScheduler
import com.example.ui.theme.WaterBackground
import com.example.ui.theme.WaterOutline
import com.example.ui.theme.WaterPrimary
import com.example.ui.theme.WaterPrimaryContainer
import com.example.ui.theme.WaterSurface
import com.example.ui.theme.WaterTextPrimary
import com.example.ui.theme.WaterTextSecondary
import kotlinx.coroutines.launch

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
    val reminderIntervalMinutes by repository.reminderIntervalMinutes.collectAsState(initial = 60)
    val reminderWeekendsEnabled by repository.reminderWeekendsEnabled.collectAsState(initial = true)
    val wakeTime by repository.wakeTime.collectAsState(initial = "08:00")
    val sleepTime by repository.sleepTime.collectAsState(initial = "22:00")

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showUnitDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showIntervalDialog by remember { mutableStateOf(false) }
    var showWakingHoursDialog by remember { mutableStateOf(false) }

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
            Toast.makeText(
                context,
                "Notification permission is needed for hydration reminders",
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
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Hydration Profile Section
        item {
            WaterCard(testTag = "settings_profile_card") {
                SectionHeader("HYDRATION PROFILE")

                val targetDisplay = if (preferredUnit == "litres") {
                    String.format(java.util.Locale.US, "%.2f L / day", dailyTargetMl / 1000f)
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

                HorizontalDivider(color = WaterOutline.copy(alpha = 0.5f))

                SettingsActionRow(
                    icon = Icons.Default.Straighten,
                    title = "Unit of Measurement",
                    subtitle = if (preferredUnit == "litres") "Litres (L)" else "Milliliters (ml)",
                    onClick = { showUnitDialog = true },
                    testTag = "settings_units_row"
                )

                HorizontalDivider(color = WaterOutline.copy(alpha = 0.5f))

                val personalSubtitle = buildString {
                    if (userProfile.name.isNotBlank()) append("${userProfile.name} • ")
                    append("${userProfile.weightKg.toInt()} kg • Age ${userProfile.age}")
                }

                SettingsActionRow(
                    icon = Icons.Default.Person,
                    title = "Personal Information",
                    subtitle = personalSubtitle,
                    onClick = { showEditProfileDialog = true },
                    testTag = "settings_info_row"
                )
            }
        }

        // Reminders Section
        item {
            WaterCard(testTag = "settings_reminders_card") {
                SectionHeader("HYDRATION REMINDERS")

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
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
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
                            checkedThumbColor = WaterPrimary,
                            checkedTrackColor = WaterPrimaryContainer
                        ),
                        modifier = Modifier.testTag("settings_reminder_switch")
                    )
                }

                // If reminders are enabled, display interval, waking hours, weekend toggle, and test notification
                if (remindersEnabled) {
                    HorizontalDivider(color = WaterOutline.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 4.dp))

                    SettingsActionRow(
                        icon = Icons.Default.Alarm,
                        title = "Reminder Interval",
                        subtitle = "Every $reminderIntervalMinutes minutes",
                        onClick = { showIntervalDialog = true },
                        testTag = "settings_interval_row"
                    )

                    HorizontalDivider(color = WaterOutline.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 4.dp))

                    SettingsActionRow(
                        icon = Icons.Default.Bedtime,
                        title = "Waking Hours",
                        subtitle = "$wakeTime to $sleepTime",
                        onClick = { showWakingHoursDialog = true },
                        testTag = "settings_waking_hours_row"
                    )

                    HorizontalDivider(color = WaterOutline.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 4.dp))

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
                                checkedThumbColor = WaterPrimary,
                                checkedTrackColor = WaterPrimaryContainer
                            ),
                            modifier = Modifier.testTag("settings_weekend_switch")
                        )
                    }

                    HorizontalDivider(color = WaterOutline.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 4.dp))

                    // Send Test Reminder Row
                    SettingsActionRow(
                        icon = Icons.Default.NotificationsActive,
                        title = "Send Test Reminder",
                        subtitle = "Trigger a preview notification now",
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                                if (!hasPermission) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    return@SettingsActionRow
                                }
                            }
                            val dispatched = ReminderHelper.showHydrationNotification(
                                context = context,
                                consumedMl = 500,
                                targetMl = dailyTargetMl,
                                preferredUnit = preferredUnit
                            )
                            if (dispatched) {
                                Toast.makeText(context, "Test notification dispatched!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Notification permission required", Toast.LENGTH_SHORT).show()
                            }
                        },
                        testTag = "settings_test_reminder_row"
                    )
                }
            }
        }

        // App & Data Management Section
        item {
            WaterCard(testTag = "settings_data_card") {
                SectionHeader("DATA & ONBOARDING")

                SettingsActionRow(
                    icon = Icons.Default.Refresh,
                    title = "Revisit Onboarding",
                    subtitle = "Restart multi-step setup guide",
                    onClick = onNavigateToOnboarding,
                    testTag = "settings_onboarding_row"
                )

                HorizontalDivider(color = WaterOutline.copy(alpha = 0.5f))

                SettingsActionRow(
                    icon = Icons.Default.DeleteOutline,
                    title = "Clear History",
                    subtitle = "Clear recorded intake entries (preserves profile)",
                    onClick = { showClearHistoryDialog = true },
                    testTag = "settings_clear_history_row"
                )
            }
        }

        // About Section
        item {
            WaterCard(testTag = "settings_about_card") {
                SectionHeader("ABOUT")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SettingsIcon(Icons.Default.Info)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "WaterIntake v1.0",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = WaterTextPrimary
                            )
                        )
                        Text(
                            text = "100% Offline • Private • Built with Jetpack Compose & Room",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = WaterTextSecondary,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }
    }

    // Interval Picker Dialog
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
                    ReminderScheduler.AVAILABLE_INTERVALS.forEach { interval ->
                        val isSelected = reminderIntervalMinutes == interval
                        val label = when (interval) {
                            30 -> "Every 30 minutes"
                            60 -> "Every 60 minutes (Recommended)"
                            90 -> "Every 90 minutes"
                            120 -> "Every 120 minutes (2 hours)"
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
                                colors = RadioButtonDefaults.colors(selectedColor = WaterPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) WaterPrimary else WaterTextPrimary
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showIntervalDialog = false }) {
                    Text("Close", color = WaterPrimary)
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
                    Text("Save", color = WaterPrimary, fontWeight = FontWeight.Bold)
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

    // Quick Unit Picker Dialog
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
                                color = if (preferredUnit == "ml") WaterPrimary else WaterTextPrimary
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
                                color = if (preferredUnit == "litres") WaterPrimary else WaterTextPrimary
                            )
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showUnitDialog = false }) {
                    Text("Close", color = WaterPrimary)
                }
            },
            containerColor = WaterSurface
        )
    }

    // Quick Edit Personal Info Dialog
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(userProfile.name) }
        var editAge by remember { mutableStateOf(userProfile.age.toString()) }
        var editWeight by remember { mutableStateOf(userProfile.weightKg.toInt().toString()) }
        var dialogError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = {
                Text(
                    text = "Edit Personal Info",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                        weightKg = weightFloat
                                    )
                                    repository.saveProfile(updated)
                                    showEditProfileDialog = false
                                }
                            }
                        }
                    }
                ) {
                    Text("Save", color = WaterPrimary, fontWeight = FontWeight.Bold)
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
        modifier = Modifier.padding(bottom = 10.dp)
    )
}

@Composable
private fun SettingsIcon(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(WaterPrimaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = WaterPrimary,
            modifier = Modifier.size(18.dp)
        )
    }
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
                        color = WaterTextPrimary
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
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = WaterPrimary,
    unfocusedBorderColor = WaterOutline,
    focusedContainerColor = WaterSurface,
    unfocusedContainerColor = WaterSurface,
    focusedTextColor = WaterTextPrimary,
    unfocusedTextColor = WaterTextPrimary
)
