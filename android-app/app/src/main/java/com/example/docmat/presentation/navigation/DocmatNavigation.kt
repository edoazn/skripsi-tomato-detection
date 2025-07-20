package com.example.docmat.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.docmat.R
import com.example.docmat.R.drawable.ic_news
import com.example.docmat.presentation.ui.screens.auth.login.LoginScreen
import com.example.docmat.presentation.ui.screens.auth.register.RegisterScreen
import com.example.docmat.presentation.ui.screens.camera.CameraScreen
import com.example.docmat.presentation.ui.screens.history.HistoryScreen
import com.example.docmat.presentation.ui.screens.history.HistoryViewModel
import com.example.docmat.presentation.ui.screens.homescreen.HomeScreen
import com.example.docmat.presentation.ui.screens.homescreen.HomeViewModel
import com.example.docmat.presentation.ui.screens.settings.SettingsScreen

@Composable
fun DocmatNavigation(
    navController: NavHostController = rememberNavController(),
    homeViewModel: HomeViewModel = viewModel(),
    historyViewModel: HistoryViewModel = viewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute != DocmatScreens.Login.route && currentRoute != DocmatScreens.Register.route) {
                NavigationBar {
                    val items = listOf(
                        DocmatScreens.Home,
                        DocmatScreens.History,
                        DocmatScreens.News,
                        DocmatScreens.Settings
                    )

                    items.forEach { item ->
                        NavigationBarItem(
                            icon = {
                                if (item.icon != null) {
                                    Icon(
                                        painter = painterResource(id = item.icon),
                                        contentDescription = item.title,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = item.title,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            },
                            label = { Text(item.title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = Color.Transparent
                            ),
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationRoute!!) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = DocmatScreens.Login.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Login Route
            composable(DocmatScreens.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        // Handle login success
                        navController.navigate(DocmatScreens.Home.route)
                    },
                    onNavigateToRegister = {
                        navController.navigate(DocmatScreens.Register.route)
                    }
                )
            }

            // Register Route
            composable(DocmatScreens.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = {
                        // Handle register success
                        navController.navigate(DocmatScreens.Home.route)
                    },
                    onNavigateToLogin = {
                        navController.navigate(DocmatScreens.Login.route)
                    }
                )
            }

            // Home Screen
            composable(DocmatScreens.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToCamera = {
                        navController.navigate(DocmatScreens.Camera.route)
                    },
                    onNavigateToHistory = {
                        navController.navigate(DocmatScreens.History.route)
                    },
                    onNavigateToSettings = {
                        navController.navigate(DocmatScreens.Settings.route)
                    }
                )
            }

            // Camera
            composable(DocmatScreens.Camera.route) {
                CameraScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // History Route
            composable(DocmatScreens.History.route) {
                HistoryScreen(
                    viewModel = historyViewModel,
                    onNavigateToDetail = { historyId ->
                        // Handle navigation to history detail screen if needed (not implemented here)
                        // Example: navController.navigate("history_detail/$historyId")
                        navController.navigate(DocmatScreens.History.route)
                    },
                )
            }

            // News Route
            composable(DocmatScreens.News.route) {
                // Placeholder for News Screen
                // You can implement the NewsScreen composable similarly to others
                Text("News Screen", modifier = Modifier.padding(16.dp))
            }

            // Settings Route
            composable(DocmatScreens.Settings.route) {
                SettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

sealed class DocmatScreens(
    val route: String,
    val title: String = "",
    val icon: Int? = null
) {
    data object Auth : DocmatScreens("auth")

    data object Login : DocmatScreens("login")
    data object Register : DocmatScreens("register")
    data object ForgotPassword : DocmatScreens("forgot_password")
    data object Home : DocmatScreens(
        route = "home",
        title = "Home",
        icon = R.drawable.ic_home
    )

    data object Camera : DocmatScreens("camera")
    data object History : DocmatScreens(
        route = "history",
        title = "History",
        icon = R.drawable.ic_history
    )

    data object News : DocmatScreens(
        route = "news",
        title = "News",
        icon = ic_news
    )

    data object Settings : DocmatScreens(
        route = "settings",
        title = "Settings",
        icon = R.drawable.ic_settings
    )
}