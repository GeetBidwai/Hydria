package com.example.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.repository.WaterRepository
import com.example.presentation.calculator.CalculatorScreen
import com.example.presentation.dashboard.DashboardScreen
import com.example.presentation.history.HistoryScreen
import com.example.presentation.onboarding.OnboardingScreen
import com.example.presentation.onboarding.OnboardingViewModel
import com.example.presentation.settings.SettingsScreen
import com.example.ui.theme.WaterBackground
import com.example.ui.theme.WaterPrimary
import com.example.ui.theme.WaterPrimaryContainer
import com.example.ui.theme.WaterSurface
import com.example.ui.theme.WaterTextSecondary

@Composable
fun WaterIntakeApp(
    repository: WaterRepository,
    startDestination: String = Screen.Dashboard.route,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Show bottom bar only on primary dashboard, history, and settings tabs
    val showBottomBar = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.History.route,
        Screen.Settings.route
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_root_scaffold"),
        containerColor = WaterBackground,
        bottomBar = {
            if (showBottomBar) {
                WaterBottomNavigation(
                    currentRoute = currentRoute,
                    onNavigateToDestination = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.Onboarding.route) {
                val onboardingViewModel: OnboardingViewModel = viewModel(
                    factory = OnboardingViewModel.Factory(repository)
                )
                OnboardingScreen(
                    viewModel = onboardingViewModel,
                    onComplete = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    },
                    onBackToSettings = {
                        if (!navController.popBackStack()) {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable(Screen.Dashboard.route) {
                val dashboardViewModel: com.example.presentation.dashboard.DashboardViewModel = viewModel(
                    factory = com.example.presentation.dashboard.DashboardViewModel.Factory(repository)
                )
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToCalculator = {
                        navController.navigate(Screen.Calculator.route)
                    }
                )
            }

            composable(Screen.Calculator.route) {
                val calculatorViewModel: com.example.presentation.calculator.CalculatorViewModel = viewModel(
                    factory = com.example.presentation.calculator.CalculatorViewModel.Factory(repository)
                )
                CalculatorScreen(
                    viewModel = calculatorViewModel,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.History.route) {
                val historyViewModel: com.example.presentation.history.HistoryViewModel = viewModel(
                    factory = com.example.presentation.history.HistoryViewModel.Factory(repository)
                )
                HistoryScreen(
                    viewModel = historyViewModel
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    repository = repository,
                    onNavigateToCalculator = {
                        navController.navigate(Screen.Calculator.route)
                    },
                    onNavigateToOnboarding = {
                        navController.navigate(Screen.Onboarding.route)
                    }
                )
            }
        }
    }
}

@Composable
fun WaterBottomNavigation(
    currentRoute: String?,
    onNavigateToDestination: (String) -> Unit
) {
    NavigationBar(
        containerColor = WaterSurface,
        tonalElevation = 4.dp,
        modifier = Modifier.testTag("bottom_nav_bar")
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigateToDestination(item.route) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = WaterPrimary,
                    selectedTextColor = WaterPrimary,
                    unselectedIconColor = WaterTextSecondary,
                    unselectedTextColor = WaterTextSecondary,
                    indicatorColor = WaterPrimaryContainer
                ),
                modifier = Modifier.testTag(item.testTag)
            )
        }
    }
}
