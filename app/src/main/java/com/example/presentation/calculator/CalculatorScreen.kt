package com.example.presentation.calculator

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.components.WaterCard
import com.example.core.ui.components.WaterPrimaryButton
import com.example.core.ui.components.WaterTopBar
import com.example.domain.model.ActivityLevel
import com.example.domain.model.Climate
import com.example.ui.theme.WaterAccentSuccess
import com.example.ui.theme.WaterBackground
import com.example.ui.theme.WaterOutline
import com.example.ui.theme.WaterPrimary
import com.example.ui.theme.WaterPrimaryContainer
import com.example.ui.theme.WaterSurface
import androidx.activity.compose.BackHandler
import com.example.ui.theme.WaterTextPrimary
import com.example.ui.theme.WaterTextSecondary

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    BackHandler {
        onBackClick()
    }

    Scaffold(
        topBar = {
            WaterTopBar(
                title = "Hydration Calculator",
                onBackClick = onBackClick
            )
        },
        containerColor = WaterBackground,
        modifier = modifier.testTag("calculator_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Transparency & Medical Disclaimer
            item {
                WaterCard(
                    backgroundColor = WaterPrimaryContainer.copy(alpha = 0.5f),
                    borderColor = WaterPrimary.copy(alpha = 0.25f),
                    testTag = "calculator_disclaimer_card"
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Information",
                            tint = WaterPrimary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Personalized Estimation",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = WaterTextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "This calculator provides an estimated baseline recommendation. Hydration needs vary based on health, climate, and perspiration. It is not intended as medical advice.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = WaterTextSecondary,
                                    lineHeight = 18.sp
                                )
                            )
                        }
                    }
                }
            }

            // 2. Recommended Target Hero Card
            item {
                WaterCard(testTag = "calculator_target_hero_card") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ESTIMATED DAILY TARGET",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = WaterTextSecondary,
                                letterSpacing = 1.sp
                            )
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (uiState.isManualOverride) WaterPrimaryContainer else WaterAccentSuccess.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (uiState.isManualOverride) "Custom Target" else "Calculated",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.isManualOverride) WaterPrimary else WaterAccentSuccess
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val targetDisplay = if (uiState.preferredUnit == "litres") {
                        String.format("%.2f L / day", uiState.effectiveTargetMl / 1000f)
                    } else {
                        "${uiState.effectiveTargetMl} ml / day"
                    }

                    Text(
                        text = targetDisplay,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = WaterPrimary,
                            fontSize = 32.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (uiState.isManualOverride) {
                            "Manually adjusted (Calculated baseline: ${uiState.calculatedTargetMl} ml)"
                        } else {
                            "Based on ${uiState.weightKg.ifBlank { "70" }} kg body weight & ${uiState.activityLevel.label}"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = WaterTextSecondary
                        )
                    )
                }
            }

            // 3. Transparent Calculation Breakdown
            item {
                WaterCard(testTag = "calculator_breakdown_card") {
                    Text(
                        text = "FORMULA BREAKDOWN",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = WaterTextSecondary,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    BreakdownRow(
                        label = "Base requirement (35 ml / kg)",
                        detail = "${uiState.weightKg.ifBlank { "70" }} kg × 35 ml",
                        value = "${uiState.breakdown.baseMl} ml"
                    )

                    BreakdownRow(
                        label = "Activity factor (${uiState.activityLevel.label})",
                        detail = "${uiState.breakdown.activityMultiplier}x multiplier",
                        value = "+${uiState.breakdown.activityAdjustedMl - uiState.breakdown.baseMl} ml"
                    )

                    if (uiState.breakdown.exerciseAllowanceMl > 0) {
                        BreakdownRow(
                            label = "Exercise allowance",
                            detail = "${uiState.exerciseMinutes} min × 10 ml/min",
                            value = "+${uiState.breakdown.exerciseAllowanceMl} ml"
                        )
                    }

                    if (uiState.breakdown.climateAllowanceMl > 0) {
                        BreakdownRow(
                            label = "Climate factor (${uiState.climate.label})",
                            detail = "Environmental heat allowance",
                            value = "+${uiState.breakdown.climateAllowanceMl} ml"
                        )
                    }

                    HorizontalDivider(
                        color = WaterOutline.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recommended Total",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = WaterTextPrimary
                            )
                        )
                        Text(
                            text = "${uiState.breakdown.recommendedTotalMl} ml",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = WaterPrimary
                            )
                        )
                    }
                }
            }

            // 4. Input Parameters Section
            item {
                WaterCard(testTag = "calculator_inputs_card") {
                    Text(
                        text = "YOUR PARAMETERS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = WaterTextSecondary,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Weight & Age Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Weight (kg)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = WaterTextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = uiState.weightKg,
                                onValueChange = viewModel::updateWeight,
                                placeholder = { Text("70") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                isError = uiState.weightError != null,
                                shape = RoundedCornerShape(12.dp),
                                colors = textFieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("calc_input_weight")
                            )
                            if (uiState.weightError != null) {
                                Text(
                                    text = uiState.weightError ?: "",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Age",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = WaterTextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = uiState.age,
                                onValueChange = viewModel::updateAge,
                                placeholder = { Text("28") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                isError = uiState.ageError != null,
                                shape = RoundedCornerShape(12.dp),
                                colors = textFieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("calc_input_age")
                            )
                            if (uiState.ageError != null) {
                                Text(
                                    text = uiState.ageError ?: "",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Activity Level
                    Text(
                        text = "Activity Level",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = WaterTextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ActivityLevel.entries.forEach { level ->
                            val isSelected = level == uiState.activityLevel
                            Surface(
                                onClick = { viewModel.updateActivityLevel(level) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) WaterPrimaryContainer else WaterSurface,
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelected) WaterPrimary else WaterOutline.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = level.label,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) WaterPrimary else WaterTextPrimary,
                                            fontSize = 13.sp
                                        )
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = WaterPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Climate
                    Text(
                        text = "Climate",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = WaterTextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Climate.entries.forEach { climate ->
                            val isSelected = climate == uiState.climate
                            Surface(
                                onClick = { viewModel.updateClimate(climate) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) WaterPrimaryContainer else WaterSurface,
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelected) WaterPrimary else WaterOutline.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = climate.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) WaterPrimary else WaterTextPrimary,
                                            fontSize = 11.sp
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Exercise Duration
                    Text(
                        text = "Daily Exercise (Minutes)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = WaterTextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = uiState.exerciseMinutes,
                        onValueChange = viewModel::updateExerciseMinutes,
                        placeholder = { Text("0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = uiState.exerciseError != null,
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("calc_input_exercise")
                    )
                    if (uiState.exerciseError != null) {
                        Text(
                            text = uiState.exerciseError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // 5. Manual Target Adjustment (Override)
            item {
                WaterCard(testTag = "calculator_manual_override_card") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Manual Target Override",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = WaterTextPrimary
                                )
                            )
                            Text(
                                text = "Set your own custom intake target",
                                style = MaterialTheme.typography.bodySmall.copy(color = WaterTextSecondary)
                            )
                        }
                        Switch(
                            checked = uiState.isManualOverride,
                            onCheckedChange = viewModel::toggleManualOverride,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = WaterPrimary,
                                checkedTrackColor = WaterPrimaryContainer
                            ),
                            modifier = Modifier.testTag("manual_override_switch")
                        )
                    }

                    if (uiState.isManualOverride) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Custom Target (ml)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = WaterTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = uiState.manualTargetMl,
                            onValueChange = viewModel::updateManualTarget,
                            placeholder = { Text("e.g. 2500") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            isError = uiState.manualTargetError != null,
                            shape = RoundedCornerShape(12.dp),
                            colors = textFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_manual_target")
                        )
                        if (uiState.manualTargetError != null) {
                            Text(
                                text = uiState.manualTargetError ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = viewModel::resetToCalculated) {
                                Text(
                                    text = "Reset to Calculated (${uiState.calculatedTargetMl} ml)",
                                    color = WaterPrimary,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }

            // 6. Action Button: Apply and Save
            item {
                Spacer(modifier = Modifier.height(4.dp))
                WaterPrimaryButton(
                    text = "Apply Daily Target",
                    onClick = {
                        viewModel.applyAndSave(onComplete = onBackClick)
                    },
                    testTag = "calculator_apply_btn"
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun BreakdownRow(
    label: String,
    detail: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = WaterTextPrimary,
                    fontSize = 13.sp
                )
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = WaterTextSecondary,
                    fontSize = 11.sp
                )
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = WaterPrimary,
                fontSize = 13.sp
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
