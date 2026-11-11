package com.example.presentation.dashboard

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.core.ui.components.WaterCircularProgress
import com.example.core.utils.DateTimeUtils
import com.example.data.local.WaterEntry
import com.example.presentation.components.HydriaMascot
import com.example.presentation.components.HydriaMascotHelper
import com.example.presentation.components.HydriaMascotMood
import com.example.presentation.components.HydriaMascotSpeechCard
import com.example.presentation.components.OceanWaveCard
import com.example.presentation.dashboard.components.DesignationCard
import com.example.ui.theme.OceanAqua
import com.example.ui.theme.OceanBrushes
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
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToCalculator: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Dynamic Mascot Mood Resolution based on intake and gamification state
    val isDisaster = uiState.gamificationState.isDisasterPath
    val isRecovery = uiState.isGoalAchieved && isDisaster
    val mascotMood = HydriaMascotHelper.resolveMood(
        consumedMl = uiState.consumedMl,
        targetMl = uiState.dailyTargetMl,
        isDisaster = isDisaster,
        isRecovery = isRecovery
    )
    val companionMessage = HydriaMascotHelper.getCompanionMessage(mascotMood)

    // Display Undo Snackbar when an entry is deleted
    LaunchedEffect(uiState.showUndoSnackbar, uiState.lastDeletedEntry) {
        if (uiState.showUndoSnackbar && uiState.snackbarMessage != null) {
            val result = snackbarHostState.showSnackbar(
                message = uiState.snackbarMessage ?: "Entry deleted",
                actionLabel = "UNDO",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            } else {
                viewModel.dismissUndoSnackbar()
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = OceanDeep,
                    contentColor = WaterSurface,
                    actionColor = OceanCyan
                )
            }
        },
        containerColor = WaterBackground,
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Header: Greeting, Date, and Calculator Quick Action
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hello, ${uiState.userName}",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = WaterTextPrimary,
                                fontSize = 24.sp
                            ),
                            modifier = Modifier.testTag("user_greeting_text")
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = uiState.currentDateStr,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = WaterTextSecondary,
                                fontSize = 13.sp
                            )
                        )
                    }

                    IconButton(
                        onClick = onNavigateToCalculator,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(OceanAqua.copy(alpha = 0.12f))
                            .testTag("dashboard_calculator_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = "Open Hydration Calculator",
                            tint = OceanAqua
                        )
                    }
                }
            }

            // 2. Mascot Companion Speech Bubble Section
            item {
                HydriaMascotSpeechCard(
                    mood = mascotMood,
                    customMessage = companionMessage,
                    modifier = Modifier.testTag("mascot_companion_card")
                )
            }

            // 3. Hero Hydration Progress Card (Circular Indicator with animated wave fill)
            item {
                OceanWaveCard(
                    testTag = "dashboard_progress_card",
                    contentPadding = 22.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Consumed / Target display
                        val consumedLiters = String.format(Locale.getDefault(), "%.2f L", uiState.consumedMl / 1000f)
                        val targetLiters = String.format(Locale.getDefault(), "%.2f L", uiState.dailyTargetMl / 1000f)
                        val subtitleFormatted = "${uiState.consumedMl} ml / ${uiState.dailyTargetMl} ml ($consumedLiters / $targetLiters)"

                        WaterCircularProgress(
                            currentMl = uiState.consumedMl,
                            targetMl = uiState.dailyTargetMl,
                            subtitleOverride = subtitleFormatted,
                            size = 200.dp,
                            strokeWidth = 14.dp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Stats Row: Consumed & Remaining with Litres
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem(
                                label = "Consumed",
                                value = "${uiState.consumedMl} ml ($consumedLiters)",
                                color = OceanAqua
                            )
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(38.dp)
                                    .background(WaterOutline.copy(alpha = 0.5f))
                            )
                            val remainingLiters = String.format(Locale.getDefault(), "%.2f L", uiState.remainingMl / 1000f)
                            StatItem(
                                label = "Remaining",
                                value = if (uiState.remainingMl == 0) "Goal Reached! 🎉" else "${uiState.remainingMl} ml ($remainingLiters)",
                                color = if (uiState.remainingMl == 0) WaterAccentSuccess else WaterTextSecondary
                            )
                        }
                    }
                }
            }

            // 4. Hydration Designation & Gamification Card
            item {
                DesignationCard(
                    state = uiState.gamificationState,
                    isTodayGoalAchieved = uiState.isGoalAchieved
                )
            }

            // 5. Quick Add Hydration Controls (+150, +250, +300, +500, Custom)
            item {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "QUICK ADD",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = WaterTextSecondary,
                                letterSpacing = 1.sp
                            )
                        )

                        Text(
                            text = "TAP TO LOG",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = OceanAqua,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Exactly 4 presets + custom
                        val presets = listOf(150, 250, 300, 500)
                        presets.forEach { amount ->
                            QuickAddChip(
                                amountMl = amount,
                                onClick = { viewModel.logQuickAdd(amount) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Custom Amount Chip
                        Surface(
                            onClick = { viewModel.showCustomAmountDialog() },
                            shape = RoundedCornerShape(12.dp),
                            color = OceanAqua.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, OceanAqua.copy(alpha = 0.45f)),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("quick_add_custom")
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Custom amount",
                                    tint = OceanAqua,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Custom",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = OceanAqua,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 6. Recent Intake Activity
            item {
                WaterCard(
                    testTag = "dashboard_recent_card",
                    contentPadding = 18.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Activity",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = WaterTextPrimary
                            )
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.showCustomAmountDialog() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("dashboard_custom_add_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = OceanAqua,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Log Custom",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = OceanAqua,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (uiState.todayEntries.isEmpty()) {
                        // Empty state with friendly mascot guidance
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            HydriaMascot(
                                mood = HydriaMascotMood.CHEERFUL_START,
                                size = 56.dp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No water logged yet today",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = WaterTextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Tap any quick-add button above to start your tide!",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = WaterTextSecondary,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    } else {
                        // List entries with delete action
                        Column {
                            uiState.todayEntries.forEachIndexed { index, entry ->
                                if (index > 0) {
                                    HorizontalDivider(
                                        color = WaterOutline.copy(alpha = 0.35f),
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                                IntakeEntryRow(
                                    entry = entry,
                                    formattedAmount = viewModel.formatVolume(entry.amountMl, uiState.preferredUnit),
                                    onDelete = { viewModel.deleteEntry(entry) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Custom Quantity Entry Dialog
    if (uiState.isCustomAmountDialogVisible) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCustomAmountDialog() },
            title = {
                Text(
                    text = "Log Custom Amount",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter water volume consumed in milliliters (ml):",
                        style = MaterialTheme.typography.bodyMedium.copy(color = WaterTextSecondary)
                    )

                    OutlinedTextField(
                        value = uiState.customAmountInput,
                        onValueChange = viewModel::updateCustomAmountInput,
                        placeholder = { Text("e.g. 350") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = uiState.customAmountError != null,
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_amount_input")
                    )

                    if (uiState.customAmountError != null) {
                        Text(
                            text = uiState.customAmountError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                        )
                    }

                    // Quick suggestion chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(200, 350, 450).forEach { preset ->
                            Surface(
                                onClick = { viewModel.updateCustomAmountInput(preset.toString()) },
                                shape = RoundedCornerShape(8.dp),
                                color = OceanAqua.copy(alpha = 0.12f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "$preset ml",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = OceanAqua,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.submitCustomAmount() },
                    modifier = Modifier.testTag("submit_custom_amount_btn")
                ) {
                    Text("Add", color = OceanAqua, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissCustomAmountDialog() }) {
                    Text("Cancel", color = WaterTextSecondary)
                }
            },
            containerColor = WaterSurface
        )
    }
}

@Composable
private fun IntakeEntryRow(
    entry: WaterEntry,
    formattedAmount: String,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag("intake_entry_${entry.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(OceanAqua.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = OceanAqua,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "+$formattedAmount",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = WaterTextPrimary,
                        fontSize = 15.sp
                    )
                )
                Text(
                    text = DateTimeUtils.formatTime(entry.timestamp),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = WaterTextSecondary,
                        fontSize = 12.sp
                    )
                )
            }
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .size(48.dp)
                .testTag("delete_entry_${entry.id}")
        ) {
            Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = "Delete entry",
                tint = WaterTextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = WaterTextSecondary,
                fontSize = 12.sp
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 15.sp
            )
        )
    }
}

@Composable
private fun QuickAddChip(
    amountMl: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = WaterSurface,
        border = BorderStroke(1.dp, OceanAqua.copy(alpha = 0.40f)),
        modifier = modifier
            .height(52.dp)
            .testTag("quick_add_$amountMl")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "+$amountMl",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = OceanAqua,
                    fontSize = 14.sp
                )
            )
            Text(
                text = "ml",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = WaterTextSecondary,
                    fontSize = 10.sp
                )
            )
        }
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
