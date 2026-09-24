package com.example.delulu.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.delulu.ui.screens.askdelulu.AskDeluluScreen
import com.example.delulu.ui.screens.focus.FocusScreen
import com.example.delulu.ui.screens.home.HomeScreen
import com.example.delulu.ui.screens.planner.PlannerScreen
import com.example.delulu.ui.screens.progress.ProgressScreen
import com.example.delulu.ui.screens.settings.SettingsScreen
import com.example.delulu.ui.screens.study.StudyScreen
import com.example.delulu.viewmodel.DeluluViewModel

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    object Home : Screen("home", "Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_home")
    object AskDelulu : Screen("ask_delulu", "Ask", Icons.Filled.Psychology, Icons.Outlined.Psychology, "nav_ask")
    object Study : Screen("study", "Study", Icons.Filled.AutoStories, Icons.Outlined.AutoStories, "nav_study")
    object Planner : Screen("planner", "Planner", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth, "nav_planner")
    object Focus : Screen("focus", "Focus", Icons.Filled.Timer, Icons.Outlined.Timer, "nav_focus")
    object Progress : Screen("progress", "Progress", Icons.Filled.Insights, Icons.Outlined.Insights, "nav_progress")
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings")
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.AskDelulu,
    Screen.Study,
    Screen.Planner,
    Screen.Focus,
    Screen.Progress,
    Screen.Settings
)

@Composable
fun DeluluApp(
    viewModel: DeluluViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                bottomNavItems.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.title,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        },
                        modifier = Modifier.testTag(screen.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToAskDelulu = { navController.navigate(Screen.AskDelulu.route) },
                    onNavigateToFocus = { navController.navigate(Screen.Focus.route) },
                    onNavigateToStudy = { navController.navigate(Screen.Study.route) },
                    onNavigateToPlanner = { navController.navigate(Screen.Planner.route) }
                )
            }
            composable(Screen.AskDelulu.route) {
                AskDeluluScreen(viewModel = viewModel)
            }
            composable(Screen.Study.route) {
                StudyScreen(viewModel = viewModel)
            }
            composable(Screen.Planner.route) {
                PlannerScreen(viewModel = viewModel)
            }
            composable(Screen.Focus.route) {
                FocusScreen(viewModel = viewModel)
            }
            composable(Screen.Progress.route) {
                ProgressScreen(viewModel = viewModel)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
