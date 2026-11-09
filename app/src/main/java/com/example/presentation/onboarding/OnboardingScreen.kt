package com.example.presentation.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.ui.components.WaterCard
import com.example.core.ui.components.WaterPrimaryButton
import com.example.core.ui.components.WaterSecondaryButton
import com.example.domain.model.ActivityLevel
import com.example.ui.theme.WaterAccentSuccess
import com.example.ui.theme.WaterBackground
import com.example.ui.theme.WaterOutline
import com.example.ui.theme.WaterPrimary
import com.example.ui.theme.WaterPrimaryContainer
import com.example.ui.theme.WaterSurface
import com.example.ui.theme.WaterTextPrimary
import com.example.ui.theme.WaterTextSecondary

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
                2 -> Step2PersonalInfo(
                    name = uiState.name,
                    onNameChange = viewModel::updateName,
                    age = uiState.age,
                    onAgeChange = viewModel::updateAge,
                    ageError = uiState.ageError,
                    weightKg = uiState.weightKg,
                    onWeightChange = viewModel::updateWeight,
                    weightError = uiState.weightError,
                    onNext = { viewModel.goToNextStep() }
                )
                3 -> Step3Activity(
                    selectedLevel = uiState.activityLevel,
                    onSelectLevel = viewModel::updateActivityLevel,
                    exerciseMinutes = uiState.exerciseMinutes,
                    onExerciseChange = viewModel::updateExerciseMinutes,
                    exerciseError = uiState.exerciseError,
                    onNext = { viewModel.goToNextStep() },
                    onBack = { viewModel.goToPreviousStep() }
                )
                4 -> Step4Preferences(
                    preferredUnit = uiState.preferredUnit,
                    onUnitChange = viewModel::updatePreferredUnit,
                    remindersEnabled = uiState.remindersEnabled,
                    onRemindersChange = viewModel::updateRemindersEnabled,
                    wakeTime = uiState.wakeTime,
                    onWakeTimeChange = viewModel::updateWakeTime,
                    sleepTime = uiState.sleepTime,
                    onSleepTimeChange = viewModel::updateSleepTime,
                    onNext = { viewModel.goToNextStep() },
                    onBack = { viewModel.goToPreviousStep() }
                )
                5 -> Step5Review(
                    uiState = uiState,
                    onEditStep = viewModel::jumpToStep,
                    onSave = { viewModel.saveProfileAndComplete(onComplete) },
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
            color = WaterPrimary,
            trackColor = WaterOutline.copy(alpha = 0.4f)
        )
    }
}

// -------------------------------------------------------------
// STEP 1: WELCOME SCREEN
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
        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .size(108.dp)
                .clip(CircleShape)
                .background(WaterPrimary.copy(alpha = 0.10f))
                .testTag("onboarding_hero_icon"),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_hydria_logo),
                contentDescription = "Hydria Logo",
                modifier = Modifier.size(76.dp)
            )
        }

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
            text = "Your minimalist, offline-first hydration companion. Build a balanced daily hydration habit with personalized goals.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = WaterTextSecondary,
                fontSize = 15.sp,
                lineHeight = 22.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        WaterCard(testTag = "welcome_features_card") {
            Text(
                text = "WHAT WE DO",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = WaterTextSecondary,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(14.dp))
            WelcomeFeatureItem("Personalized Target", "Scientifically tailored baseline for your weight & lifestyle.")
            Spacer(modifier = Modifier.height(12.dp))
            WelcomeFeatureItem("Gentle Reminders", "Scheduled during your waking hours with zero spam.")
            Spacer(modifier = Modifier.height(12.dp))
            WelcomeFeatureItem("100% Private & Offline", "No accounts, no cloud sync. All data stays on your device.")
        }

        Spacer(modifier = Modifier.height(40.dp))

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
                .size(20.dp)
                .clip(CircleShape)
                .background(WaterPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = WaterPrimary,
                modifier = Modifier.size(12.dp)
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
// STEP 2: PERSONAL INFORMATION
// -------------------------------------------------------------
@Composable
private fun Step2PersonalInfo(
    name: String,
    onNameChange: (String) -> Unit,
    age: String,
    onAgeChange: (String) -> Unit,
    ageError: String?,
    weightKg: String,
    onWeightChange: (String) -> Unit,
    weightError: String?,
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
            text = "Tell us about yourself",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = WaterTextPrimary,
                fontSize = 24.sp
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Your weight and age help determine your daily baseline hydration requirements.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = WaterTextSecondary,
                fontSize = 14.sp
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

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

            Spacer(modifier = Modifier.height(20.dp))

            // Age field (Required)
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
                placeholder = { Text("e.g. 28", color = WaterTextSecondary) },
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
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Weight field (Required)
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
                placeholder = { Text("e.g. 70", color = WaterTextSecondary) },
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
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        WaterPrimaryButton(
            text = "Continue",
            onClick = onNext,
            testTag = "personal_info_next_btn"
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// -------------------------------------------------------------
// STEP 3: ACTIVITY INFORMATION
// -------------------------------------------------------------
@Composable
private fun Step3Activity(
    selectedLevel: ActivityLevel,
    onSelectLevel: (ActivityLevel) -> Unit,
    exerciseMinutes: String,
    onExerciseChange: (String) -> Unit,
    exerciseError: String?,
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
            text = "Activity & Exercise",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = WaterTextPrimary,
                fontSize = 24.sp
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Select your general physical activity level so we can calibrate your hydration target.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = WaterTextSecondary,
                fontSize = 14.sp
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Activity Level Selection Cards
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ActivityLevel.entries.forEach { level ->
                val isSelected = level == selectedLevel
                ActivityOptionCard(
                    level = level,
                    isSelected = isSelected,
                    onSelect = { onSelectLevel(level) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Optional Daily Exercise Duration
        WaterCard(testTag = "exercise_duration_card") {
            Text(
                text = "Average Daily Exercise (Minutes)",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = WaterTextPrimary
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Optional workout time to factor in extra hydration.",
                style = MaterialTheme.typography.bodySmall.copy(color = WaterTextSecondary)
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = exerciseMinutes,
                onValueChange = onExerciseChange,
                placeholder = { Text("e.g. 30", color = WaterTextSecondary) },
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
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        WaterPrimaryButton(
            text = "Continue",
            onClick = onNext,
            testTag = "activity_next_btn"
        )

        Spacer(modifier = Modifier.height(12.dp))

        WaterSecondaryButton(
            text = "Back",
            onClick = onBack,
            testTag = "activity_back_btn"
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ActivityOptionCard(
    level: ActivityLevel,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) WaterPrimaryContainer else WaterSurface,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) WaterPrimary else WaterOutline.copy(alpha = 0.6f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("activity_option_${level.name}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = level.label,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = WaterTextPrimary,
                        fontSize = 15.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = level.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = WaterTextSecondary,
                        fontSize = 12.sp
                    )
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) WaterPrimary else Color.Transparent)
                    .then(
                        if (!isSelected) Modifier.background(Color.Transparent, CircleShape)
                        else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 4: PREFERENCES (UNITS & REMINDERS)
// -------------------------------------------------------------
@Composable
private fun Step4Preferences(
    preferredUnit: String,
    onUnitChange: (String) -> Unit,
    remindersEnabled: Boolean,
    onRemindersChange: (Boolean) -> Unit,
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
            text = "Preferences",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = WaterTextPrimary,
                fontSize = 24.sp
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Choose your preferred unit and setup your reminder schedule.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = WaterTextSecondary,
                fontSize = 14.sp
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Unit Selection
        WaterCard(testTag = "unit_selection_card") {
            Text(
                text = "PREFERRED MEASUREMENT UNIT",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = WaterTextSecondary,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                UnitOptionChip(
                    label = "Milliliters (ml)",
                    isSelected = preferredUnit == "ml",
                    onSelect = { onUnitChange("ml") },
                    modifier = Modifier.weight(1f)
                )
                UnitOptionChip(
                    label = "Litres (L)",
                    isSelected = preferredUnit == "litres",
                    onSelect = { onUnitChange("litres") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Reminders & Active Hours
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
                            fontWeight = FontWeight.SemiBold,
                            color = WaterTextPrimary
                        )
                    )
                    Text(
                        text = "Receive gentle alerts during your waking hours",
                        style = MaterialTheme.typography.bodySmall.copy(color = WaterTextSecondary)
                    )
                }
                Switch(
                    checked = remindersEnabled,
                    onCheckedChange = onRemindersChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = WaterPrimary,
                        checkedTrackColor = WaterPrimaryContainer
                    ),
                    modifier = Modifier.testTag("onboarding_reminder_switch")
                )
            }

            if (remindersEnabled) {
                HorizontalDivider(
                    color = WaterOutline.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 14.dp)
                )

                Text(
                    text = "ACTIVE HOURS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = WaterTextSecondary,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Wake-up Time",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = WaterTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = wakeTime,
                            onValueChange = onWakeTimeChange,
                            placeholder = { Text("07:00") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = textFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_wake_time")
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Bedtime",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = WaterTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = sleepTime,
                            onValueChange = onSleepTimeChange,
                            placeholder = { Text("23:00") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = textFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_sleep_time")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        WaterPrimaryButton(
            text = "Continue",
            onClick = onNext,
            testTag = "preferences_next_btn"
        )

        Spacer(modifier = Modifier.height(12.dp))

        WaterSecondaryButton(
            text = "Back",
            onClick = onBack,
            testTag = "preferences_back_btn"
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun UnitOptionChip(
    label: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) WaterPrimaryContainer else WaterSurface,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) WaterPrimary else WaterOutline.copy(alpha = 0.6f)
        ),
        modifier = modifier
            .height(48.dp)
            .testTag("unit_chip_$label")
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) WaterPrimary else WaterTextPrimary,
                    fontSize = 13.sp
                )
            )
        }
    }
}

// -------------------------------------------------------------
// STEP 5: REVIEW & SAVE
// -------------------------------------------------------------
@Composable
private fun Step5Review(
    uiState: OnboardingUiState,
    onEditStep: (Int) -> Unit,
    onSave: () -> Unit,
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
            text = "Review your profile",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = WaterTextPrimary,
                fontSize = 24.sp
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Confirm your details. You can adjust any section at any time.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = WaterTextSecondary,
                fontSize = 14.sp
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Calculated Daily Intake Hero Card
        WaterCard(
            backgroundColor = WaterPrimaryContainer.copy(alpha = 0.6f),
            borderColor = WaterPrimary.copy(alpha = 0.3f),
            testTag = "review_target_card"
        ) {
            Text(
                text = "YOUR ESTIMATED DAILY TARGET",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = WaterPrimary,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (uiState.preferredUnit == "litres") {
                    String.format("%.2f L / day", uiState.calculatedTargetMl / 1000f)
                } else {
                    "${uiState.calculatedTargetMl} ml / day"
                },
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = WaterTextPrimary,
                    fontSize = 28.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Calculated using 35ml/kg baseline with activity & exercise multipliers.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = WaterTextSecondary,
                    lineHeight = 18.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Personal Information Summary Card
        WaterCard(testTag = "review_personal_card") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PERSONAL INFORMATION",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = WaterTextSecondary,
                        letterSpacing = 1.sp
                    )
                )
                IconButton(
                    onClick = { onEditStep(2) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit personal info",
                        tint = WaterPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            ReviewRow(label = "Name", value = uiState.name.ifBlank { "Not specified" })
            ReviewRow(label = "Age", value = "${uiState.age} years")
            ReviewRow(label = "Weight", value = "${uiState.weightKg} kg")
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Activity Summary Card
        WaterCard(testTag = "review_activity_card") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVITY & EXERCISE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = WaterTextSecondary,
                        letterSpacing = 1.sp
                    )
                )
                IconButton(
                    onClick = { onEditStep(3) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit activity",
                        tint = WaterPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            ReviewRow(label = "Activity Level", value = uiState.activityLevel.label)
            ReviewRow(label = "Daily Exercise", value = "${uiState.exerciseMinutes.ifBlank { "0" }} min/day")
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Preferences Summary Card
        WaterCard(testTag = "review_preferences_card") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PREFERENCES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = WaterTextSecondary,
                        letterSpacing = 1.sp
                    )
                )
                IconButton(
                    onClick = { onEditStep(4) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit preferences",
                        tint = WaterPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            ReviewRow(label = "Unit", value = if (uiState.preferredUnit == "litres") "Litres (L)" else "Milliliters (ml)")
            ReviewRow(label = "Reminders", value = if (uiState.remindersEnabled) "Enabled (${uiState.wakeTime} - ${uiState.sleepTime})" else "Disabled")
        }

        Spacer(modifier = Modifier.height(32.dp))

        WaterPrimaryButton(
            text = if (uiState.isSaving) "Saving Profile..." else "Save Profile",
            onClick = onSave,
            enabled = !uiState.isSaving,
            testTag = "save_profile_btn"
        )

        Spacer(modifier = Modifier.height(12.dp))

        WaterSecondaryButton(
            text = "Back",
            onClick = onBack,
            enabled = !uiState.isSaving,
            testTag = "review_back_btn"
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ReviewRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = WaterTextSecondary,
                fontSize = 14.sp
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = WaterTextPrimary,
                fontSize = 14.sp
            )
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
