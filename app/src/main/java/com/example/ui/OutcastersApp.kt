package com.example.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.ui.screens.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutcastersApp() {
    val navController = rememberNavController()
    
    val items = listOf(
        "home" to "Home",
        "models" to "Models",
        "settings" to "Settings"
    )
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in items.map { it.first }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    items.forEach { (route, label) ->
                        NavigationBarItem(
                            icon = {
                                when (route) {
                                    "home" -> Icon(Icons.Filled.Home, contentDescription = label)
                                    "models" -> Icon(Icons.Filled.Storage, contentDescription = label)
                                    "settings" -> Icon(Icons.Filled.Settings, contentDescription = label)
                                }
                            },
                            label = { Text(label) },
                            selected = currentRoute == route,
                            onClick = {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            composable("home") { HomeScreen(navController) }
            composable("models") { com.example.ui.screens.ModelHubScreen(navController) }
            composable("settings") { SettingsScreen(navController) }
            composable("settings/behavior") { BehaviorSettingsScreen(navController) }
            composable("settings/appearance") { AppearanceSettingsScreen(navController) }
            composable("settings/performance") { PerformanceSettingsScreen(navController) }
            composable("settings/privacy") { PrivacySettingsScreen(navController) }
            composable("settings/storage") { StorageSettingsScreen(navController) }
            composable("settings/ocr") { OCRSettingsScreen(navController) }
            composable("settings/about") { AboutSettingsScreen(navController) }
            composable("update_feed") { UpdateFeedScreen(navController) }
            
            composable("scan") { ScanScreen(navController) }
            
            composable(
                "chat?mode={mode}&lang={lang}",
                arguments = listOf(
                    navArgument("mode") { defaultValue = "chat" },
                    navArgument("lang") { defaultValue = "English" }
                )
            ) { backStackEntry ->
                val mode = backStackEntry.arguments?.getString("mode") ?: "chat"
                val lang = backStackEntry.arguments?.getString("lang") ?: "English"
                ChatScreen(navController, null, mode, lang)
            }
            
            composable("library") { LibraryScreen(navController) }
            
            composable(
                "chat/{sessionId}?mode={mode}&lang={lang}",
                arguments = listOf(
                    navArgument("sessionId") { type = NavType.StringType },
                    navArgument("mode") { defaultValue = "chat" },
                    navArgument("lang") { defaultValue = "English" }
                )
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getString("sessionId")?.toLongOrNull()
                val mode = backStackEntry.arguments?.getString("mode") ?: "chat"
                val lang = backStackEntry.arguments?.getString("lang") ?: "French"
                ChatScreen(navController, sessionId, mode, lang)
            }
        }
    }
}
