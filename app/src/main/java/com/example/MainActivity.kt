package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.data.local.WaterDatabase
import com.example.data.preferences.UserPreferencesDataStore
import com.example.data.repository.WaterRepository
import com.example.presentation.navigation.Screen
import com.example.presentation.navigation.WaterIntakeApp
import com.example.reminders.ReminderHelper
import com.example.reminders.ReminderScheduler
import com.example.ui.theme.WaterBackground
import com.example.ui.theme.WaterIntakeTheme
import com.example.ui.theme.WaterPrimary
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize notification channel for hydration reminders
        ReminderHelper.createNotificationChannel(applicationContext)

        val database = WaterDatabase.getDatabase(applicationContext)
        val preferencesDataStore = UserPreferencesDataStore(applicationContext)
        val repository = WaterRepository(
            waterEntryDao = database.waterEntryDao(),
            preferencesDataStore = preferencesDataStore,
            context = applicationContext
        )

        // Ensure reminders are registered in WorkManager and widgets refreshed
        lifecycleScope.launch {
            preferencesDataStore.ensureReminderIntervalMigrated()
            val enabled = preferencesDataStore.remindersEnabled.first()
            if (enabled) {
                val interval = preferencesDataStore.reminderIntervalMinutes.first()
                ReminderScheduler.scheduleReminders(applicationContext, interval)
            }
            com.example.widgets.WidgetRefreshHelper.refreshAllWidgets(applicationContext)
        }

        setContent {
            WaterIntakeTheme {
                val isOnboardingCompleted by repository.isOnboardingCompleted
                    .collectAsStateWithLifecycle(initialValue = null)

                when (val completed = isOnboardingCompleted) {
                    null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(WaterBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = WaterPrimary)
                        }
                    }
                    else -> {
                        val startDestination = if (completed) {
                            Screen.Dashboard.route
                        } else {
                            Screen.Onboarding.route
                        }

                        WaterIntakeApp(
                            repository = repository,
                            startDestination = startDestination
                        )
                    }
                }
            }
        }
    }
}
