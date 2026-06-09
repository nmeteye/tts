package com.nicolas.ttsapp.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.nicolas.ttsapp.ui.screens.favorites.FavoritesScreen
import com.nicolas.ttsapp.ui.screens.history.HistoryScreen
import com.nicolas.ttsapp.ui.screens.home.HomeScreen
import com.nicolas.ttsapp.ui.screens.settings.SettingsScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Home      : Screen("home",      "Lecteur",   Icons.Default.Mic)
    data object History   : Screen("history",   "Historique",Icons.Default.History)
    data object Favorites : Screen("favorites", "Favoris",   Icons.Default.Favorite)
}

@Composable
fun TTSNavGraph(sharedText: String?) {
    val navController = rememberNavController()
    val tabs = listOf(Screen.Home, Screen.History, Screen.Favorites)

    // Si l'app est ouverte via intent SEND
    val startText = remember { sharedText ?: "" }

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStack by navController.currentBackStackEntryAsState()
                val currentDest = navBackStack?.destination
                tabs.forEach { screen ->
                    NavigationBarItem(
                        selected = currentDest?.hierarchy?.any { it.route == screen.route } == true,
                        onClick  = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState    = true
                            }
                        },
                        icon     = { Icon(screen.icon, screen.label) },
                        label    = { Text(screen.label) }
                    )
                }
            }
        }
    ) { _ ->
        NavHost(navController, startDestination = Screen.Home.route) {

            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    onSelectEntry = { text ->
                        navController.navigate(Screen.Home.route) {
                            launchSingleTop = true
                            restoreState    = true
                        }
                        // Transmis via SavedStateHandle dans HomeViewModel si besoin
                    }
                )
            }

            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    onSelectEntry = { _ ->
                        navController.navigate(Screen.Home.route) {
                            launchSingleTop = true
                            restoreState    = true
                        }
                    }
                )
            }

            composable("settings") {
                SettingsScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}
