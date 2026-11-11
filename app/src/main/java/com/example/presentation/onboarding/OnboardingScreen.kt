package com.example.presentation.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.components.WaterCard
import com.example.core.ui.components.WaterPrimaryButton
import com.example.core.ui.components.WaterSecondaryButton
import com.example.domain.model.ActivityLevel
import com.example.presentation.components.HydriaMascot
import com.example.presentation.components.HydriaMascotMood
import com.example.presentation.components.OceanWaveCard
import com.example.ui.theme.OceanAqua
import com.example.ui.theme.OceanCyan
import com.example.ui.theme.OceanDeep
import com.example.ui.theme.OceanGold
import com.example.ui.theme.WaterAccentSuccess
import com.example.ui.theme.WaterBackground
import com.example.ui.theme.WaterOutline
import com.example.ui.theme.WaterPrimary
import com.example.ui.theme.WaterPrimaryContainer
import com.example.ui.theme.WaterSurface
import com.example.ui.theme.WaterTextPrimary
import com.example.ui.theme.WaterTextSecondary
import java.util.Locale

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit,
    onBackToSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle back button to navigate backwards through onboarding steps
    BackHandler(enabled = true) {
        if (uiState.currentStep > 1) {
            viewModel.goToPreviousStep()
        } else {
            onBackToSettings?.invoke()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WaterBackground)
            .testTag("onboarding_screen")
    ) {
        // Top Header / Step Progress Indicator
        if (uiState.currentStep > 1) {
            OnboardingHeader(
                currentStep = uiState.currentStep,
                totalSteps = 5,
                onBackClick = {
                    if (uiState.currentStep > 1) {
                        viewModel.goToPreviousStep()
                    } else {
                        onBackToSettings?.invoke()
                    }
                }
            )
        }

        // Animated step content
        AnimatedContent(
            targetState = uiState.currentStep,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally { width -> width } togetherWith slideOutHorizontally { width -> -width }
                } else {
                    slideInHorizontally { width -> -width } togetherWith slideOutHorizontally { width -> width }
                }
            },
            label = "onboarding_step_transition",
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { step ->
            when (step) {
                1 -> Step1Welcome(
                    onGetStarted = { viewModel.goToNextStep() }
                )
                2 -> Step2HydrationProfile(
                    name = uiState.name,
                    onNameChange = viewModel::updateName,
                    age = uiState.age,
                    onAgeChange = viewModel::updateAge,
                    ageError = uiState.ageError,
                    weightKg = uiState.weightKg,
                    onWeightChange = viewModel::updateWeight,
                    weightError = uiState.weightError,
                    activityLevel = uiState.activityLevel,
                    onSelectLevel = viewModel::updateActivityLevel,
                    exerciseMinutes = uiState.exerciseMinutes,
                    onExerciseChange = viewModel::updateExerciseMinutes,
                    exerciseError = uiState.exerciseError,
                    preferredUnit = uiState.preferredUnit,
                    onUnitChange = viewModel::updatePreferredUnit,
                    onNext = { viewModel.goToNextStep() }
                )
                3 -> Step3ReminderSchedule(
                    remindersEnabled = uiState.remindersEnabled,
                    onRemindersChange = viewModel::updateRemindersEnabled,
                    reminderIntervalMinutes = uiState.reminderIntervalMinutes,
                    onIntervalChange = viewModel::updateReminderIntervalMinutes,
                    reminderWeekendsEnabled = uiState.reminderWeekendsEnabled,
                    onWeekendsChange = viewModel::updateReminderWeekendsEnabled,
                    wakeTime = uiState.wakeTime,
                    onWakeTimeChange = viewModel::updateWakeTime,
                    sleepTime = uiState.sleepTime,
                    onSleepTimeChange = viewModel::updateSleepTime,
                    onNext = { viewModel.goToNextStep() },
                    onBack = { viewModel.goToPreviousStep() }
                )
                4 -> Step4HowHydriaWorks(
                    onNext = { viewModel.goToNextStep() },
                    onBack = { viewModel.goToPreviousStep() }
                )
                5 -> Step5DailyGoalReveal(
                    calculatedTargetMl = uiState.calculatedTargetMl,
                    preferredUnit = uiState.preferredUnit,
                    isSaving = uiState.isSaving,
                    onComplete = { viewModel.saveProfileAndComplete(onComplete) },
                    onBack = { viewModel.goToPreviousStep() }
                )
            }
        }
    }
}

@Composable
private fun OnboardingHeader(
    currentStep: Int,
    totalSteps: Int,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("onboarding_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Go back",
                    tint = WaterTextPrimary
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "Step $currentStep of $totalSteps",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = WaterTextPrimary
                )
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { currentStep.toFloat() / totalSteps.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = OceanAqua,
            trackColor = WaterOutline.copy(alpha = 0.4f)
        )
    }
}

// -------------------------------------------------------------
// STEP 1: WELCOME SCREEN (With Hydri Mascot)
// -------------------------------------------------------------
@Composable
private fun Step1Welcome(
    onGetStarted: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Hero Mascot Animation
        HydriaMascot(
            mood = HydriaMascotMood.CHEERFUL_START,
            size = 110.dp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Welcome to Hydria",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = WaterTextPrimary
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your personal ocean-themed hydration companion. Build a steady daily drinking habit with smart goals and playful milestones.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = WaterTextSecondary,
                fontSize = 15.sp,
                lineHeight = 22.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        OceanWaveCard(testTag = "welcome_features_card") {
            Text(
                text = "WHY HYDRIA?",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = WaterTextSecondary,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(14.dp))
            WelcomeFeatureItem("Personalized Targets", "Tailored scientifically to your weight, age, and activity level.")
            Spacer(modifier = Modifier.height(12.dp))
            WelcomeFeatureItem("Playful Gamification", "Collect 10 Ocean Legends badges every month and avoid Ocean Disasters.")
            Spacer(modifier = Modifier.height(12.dp))
            WelcomeFeatureItem("100% Offline & Private", "No accounts, no cloud sync. All your data stays safely on your device.")
        }

        Spacer(modifier = Modifier.height(36.dp))

        WaterPrimaryButton(
            text = "Get Started",
            onClick = onGetStarted,
            testTag = "onboarding_start_button"
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun WelcomeFeatureItem(title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(OceanAqua.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = OceanAqua,
                modifier = Modifier.size(13.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = WaterTextPrimary,
                    fontSize = 14.sp
                )
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = WaterTextSecondary,
                    fontSize = 13.sp
                )
            )
        }
    }
}

// -------------------------------------------------------------
// STEP 2: HYDRATION PROFILE (Inputs & Calculations)
// -------------------------------------------------------------
@Composable
private fun Step2HydrationProfile(
    name: String,
    onNameChange: (String) -> Unit,
    age: String,
    onAgeChange: (String) -> Unit,
    ageError: String?,
    weightKg: String,
    onWeightChange: (String) -> Unit,
    weightError: String?,
    activityLevel: ActivityLevel,
    onSelectLevel: (ActivityLevel) -> Unit,
    exerciseMinutes: String,
    onExerciseChange: (String) -> Unit,
    exerciseError: String?,
    preferredUnit: String,
    onUnitChange: (String) -> Unit,
    onNext: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Your Hydration Profile",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = WaterTextPrimary,
                fontSize = 24.sp
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Tell us about yourself so Hydria can calibrate your optimal daily water intake.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = WaterTextSecondary,
                fontSize = 14.sp
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Personal Information Card
        WaterCard(testTag = "personal_info_card") {
            // Name field (Optional)
            Text(
                text = "Your Name (Optional)",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = WaterTextPrimary
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                placeholder = { Text("e.g. Alex", color = WaterTextSecondary) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_user_name")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Age & Weight Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Age (Years)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = WaterTextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = age,
                        onValueChange = onAgeChange,
                        placeholder = { Text("e.g. 28") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = ageError != null,
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_user_age")
                    )
                    if (ageError != null) {
                        Text(
                            text = ageError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            modifier = Modifier.padding(start = 2.dp, top = 2.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Weight (kg)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = WaterTextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = weightKg,
                        onValueChange = onWeightChange,
                        placeholder = { Text("e.g. 70") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        isError = weightError != null,
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_user_weight")
                    )
                    if (weightError != null) {
                        Text(
                            text = weightError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            modifier = Modifier.padding(start = 2.dp, top = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Preferred Unit
            Text(
                text = "Measurement Unit",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = WaterTextPrimary
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    onClick = { onUnitChange("ml") },
                    shape = RoundedCornerShape(10.dp),
                    color = if (preferredUnit == "ml") OceanAqua.copy(alpha = 0.15f) else WaterSurface,
                    border = BorderStroke(1.dp, if (preferredUnit == "ml") OceanAqua else WaterOutline.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("unit_chip_ml")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Milliliters (ml)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = if (preferredUnit == "ml") FontWeight.Bold else FontWeight.Normal,
                                color = if (preferredUnit == "ml") OceanAqua else WaterTextPrimary,
                                fontSize = 13.sp
                            )
                        )
                    }
                }

                Surface(
                    onClick = { onUnitChange("litres") },
                    shape = RoundedCornerShape(10.dp),
                    color = if (preferredUnit == "litres") OceanAqua.copy(alpha = 0.15f) else WaterSurface,
                    border = BorderStroke(1.dp, if (preferredUnit == "litres") OceanAqua else WaterOutline.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("unit_chip_litres")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Litres (L)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = if (preferredUnit == "litres") FontWeight.Bold else FontWeight.Normal,
                                color = if (preferredUnit == "litres") OceanAqua else WaterTextPrimary,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Activity Level Cards
        Text(
            text = "Activity Level",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = WaterTextPrimary
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ActivityLevel.entries.forEach { level ->
                val isSelected = level == activityLevel
                Surface(
                    onClick = { onSelectLevel(level) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) OceanAqua.copy(alpha = 0.12f) else WaterSurface,
                    border = BorderStroke(1.dp, if (isSelected) OceanAqua else WaterOutline.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("activity_option_${level.name}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = level.label,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) OceanAqua else WaterTextPrimary,
                                    fontSize = 14.sp
                                )
                            )
                            Text(
                                text = level.description,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = WaterTextSecondary,
                                    fontSize = 12.sp
                                )
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = OceanAqua,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Daily Exercise
        WaterCard(testTag = "exercise_duration_card") {
            Text(
                text = "Daily Exercise (Minutes)",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = WaterTextPrimary
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = exerciseMinutes,
                onValueChange = onExerciseChange,
                placeholder = { Text("e.g. 30") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = exerciseError != null,
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_exercise_minutes")
            )
            if (exerciseError != null) {
                Text(
                    text = exerciseError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    modifier = Modifier.padding(start = 2.dp, top = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        WaterPrimaryButton(
            text = "Continue",
            onClick = onNext,
            testTag = "personal_info_next_btn"
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// -------------------------------------------------------------
// STEP 3: REMINDER SCHEDULE (Intervals & Active Hours)
// -------------------------------------------------------------
@Composable
private fun Step3ReminderSchedule(
    remindersEnabled: Boolean,
    onRemindersChange: (Boolean) -> Unit,
    reminderIntervalMinutes: Int,
    onIntervalChange: (Int) -> Unit,
    reminderWeekendsEnabled: Boolean,
    onWeekendsChange: (Boolean) -> Unit,
    wakeTime: String,
    onWakeTimeChange: (String) -> Unit,
    sleepTime: String,
    onSleepTimeChange: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Reminder Schedule",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = WaterTextPrimary,
                fontSize = 24.sp
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Gentle, non-intrusive alerts keep you on track throughout your active day.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = WaterTextSecondary,
                fontSize = 14.sp
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        WaterCard(testTag = "reminder_preferences_card") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hydration Reminders",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = WaterTextPrimary
                        )
                    )
                    Text(
                        text = "Scheduled during your waking hours only",
                        style = MaterialTheme.typography.bodySmall.copy(color = WaterTextSecondary)
                    )
                }
                Switch(
                    checked = remindersEnabled,
                    onCheckedChange = onRemindersChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = OceanAqua,
                        checkedTrackColor = OceanAqua.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier.testTag("onboarding_reminder_switch")
                )
            }

            if (remindersEnabled) {
                HorizontalDivider(
                    color = WaterOutline.copy(alpha = 0.4f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // Interval Selection
                Text(
                    text = "REMINDER INTERVAL",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = WaterTextSecondary,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(30, 60, 90, 120).forEach { interval ->
                        val isSelected = reminderIntervalMinutes == interval
                        Surface(
                            onClick = { onIntervalChange(interval) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) OceanAqua.copy(alpha = 0.15f) else WaterSurface,
                            border = BorderStroke(1.dp, if (isSelected) OceanAqua else WaterOutline.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("interval_chip_$interval")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${interval}m",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) OceanAqua else WaterTextPrimary,
                                        fontSize = 13.sp
                                    )
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(
                    color = WaterOutline.copy(alpha = 0.4f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // Active Hours
                Text(
                    text = "WAKING HOURS (24-HOUR FORMAT)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = WaterTextSecondary,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Wake-up Time",
                            style = MaterialTheme.typography.labelSmall.copy(color = WaterTextSecondary)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = wakeTime,
                            onValueChange = onWakeTimeChange,
                            placeholder = { Text("07:00") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = textFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_wake_time")
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Bedtime",
                            style = MaterialTheme.typography.labelSmall.copy(color = WaterTextSecondary)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = sleepTime,
                            onValueChange = onSleepTimeChange,
                            placeholder = { Text("23:00") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = textFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_sleep_time")
                        )
                    }
                }

                HorizontalDivider(
                    color = WaterOutline.copy(alpha = 0.4f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // Weekend Reminders Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Weekend Reminders",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = WaterTextPrimary
                            )
                        )
                        Text(
                            text = if (reminderWeekendsEnabled) "Active on Saturday & Sunday" else "Silenced on weekends",
                            style = MaterialTheme.typography.bodySmall.copy(color = WaterTextSecondary)
                        )
                    }
                    Switch(
                        checked = reminderWeekendsEnabled,
                        onCheckedChange = onWeekendsChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = OceanAqua,
                            checkedTrackColor = OceanAqua.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier.testTag("onboarding_weekend_switch")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        WaterPrimaryButton(
            text = "Continue",
            onClick = onNext,
            testTag = "preferences_next_btn"
        )

        Spacer(modifier = Modifier.height(10.dp))

        WaterSecondaryButton(
            text = "Back",
            onClick = onBack,
            testTag = "preferences_back_btn"
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// -------------------------------------------------------------
// STEP 4: HOW HYDRIA WORKS (Concise Visual Tour)
// -------------------------------------------------------------
@Composable
private fun Step4HowHydriaWorks(
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = "How Hydria Works",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = WaterTextPrimary,
                fontSize = 24.sp
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "A quick guide to building a consistent hydration rhythm.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = WaterTextSecondary,
                fontSize = 14.sp
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            HowItWorksCard(
                icon = Icons.Default.LocalDrink,
                iconColor = OceanAqua,
                title = "1. Easy One-Tap Logging",
                desc = "Log standard amounts (+150, +250, +300, +500 ml) or custom volumes in seconds from the app or home-screen widgets."
            )

            HowItWorksCard(
                icon = Icons.Default.WaterDrop,
                iconColor = OceanCyan,
                title = "2. Real-Time Daily Progress",
                desc = "Watch your circular ocean wave rise throughout the day as you approach and conquer your customized hydration goal."
            )

            HowItWorksCard(
                icon = Icons.Default.Assessment,
                iconColor = OceanDeep,
                title = "3. Detailed History & Analytics",
                desc = "Review 7-day bar charts, interactive monthly calendar grids, and annual trends to understand your personal drinking patterns."
            )

            HowItWorksCard(
                icon = Icons.Default.EmojiEvents,
                iconColor = OceanGold,
                title = "4. Monthly Ocean Legends",
                desc = "Unlock 10 collectible ocean badges each calendar month (from Triton to Neptune to Lord of the Seven Seas)."
            )

            HowItWorksCard(
                icon = Icons.Default.WarningAmber,
                iconColor = Color(0xFFC04E26),
                title = "5. Streaks & Ocean Disasters",
                desc = "Keep your streak going strong! Missed days humorously label you as Beached Whale or Dried Coral until you recover."
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        WaterPrimaryButton(
            text = "Reveal My Daily Target",
            onClick = onNext,
            testTag = "how_it_works_next_btn"
        )

        Spacer(modifier = Modifier.height(10.dp))

        WaterSecondaryButton(
            text = "Back",
            onClick = onBack,
            testTag = "how_it_works_back_btn"
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun HowItWorksCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    desc: String
) {
    WaterCard {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = WaterTextPrimary,
                        fontSize = 15.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = WaterTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                )
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 5: DAILY GOAL REVEAL & COMPLETE
// -------------------------------------------------------------
@Composable
private fun Step5DailyGoalReveal(
    calculatedTargetMl: Int,
    preferredUnit: String,
    isSaving: Boolean,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Celebratory Mascot with Royal Golden Crown
        HydriaMascot(
            mood = HydriaMascotMood.GOAL_MET,
            size = 116.dp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Your Hydration Blueprint",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = WaterTextPrimary
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Based on your personal biometrics, activity level, and lifestyle:",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = WaterTextSecondary,
                fontSize = 14.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Hero Target Card
        OceanWaveCard(
            testTag = "review_target_card",
            contentPadding = 24.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "RECOMMENDED DAILY TARGET",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = OceanAqua,
                        letterSpacing = 1.2.sp
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                val formattedTarget = if (preferredUnit == "litres") {
                    String.format(Locale.getDefault(), "%.2f L / day", calculatedTargetMl / 1000f)
                } else {
                    "$calculatedTargetMl ml / day"
                }

                Text(
                    text = formattedTarget,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = WaterTextPrimary,
                        fontSize = 32.sp
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "≈ ${String.format(Locale.getDefault(), "%.1f", calculatedTargetMl / 250f)} glasses (250 ml each)",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = OceanAqua,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                HorizontalDivider(color = WaterOutline.copy(alpha = 0.4f))

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Calculated with 35 ml/kg baseline formula, exercise multiplier, and temperate climate baseline.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = WaterTextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        WaterPrimaryButton(
            text = if (isSaving) "Entering Dashboard..." else "Enter Dashboard",
            onClick = onComplete,
            enabled = !isSaving,
            testTag = "save_profile_btn"
        )

        Spacer(modifier = Modifier.height(10.dp))

        WaterSecondaryButton(
            text = "Back",
            onClick = onBack,
            enabled = !isSaving,
            testTag = "review_back_btn"
        )

        Spacer(modifier = Modifier.height(24.dp))
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
