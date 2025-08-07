package com.example.docmat.presentation.navigation

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.docmat.R
import android.util.Base64
import com.example.docmat.presentation.ui.screens.auth.login.LoginScreen
import com.example.docmat.presentation.ui.screens.auth.register.RegisterScreen
import com.example.docmat.presentation.ui.screens.auth.register.RegisterViewModel
import com.example.docmat.presentation.ui.screens.camera.CameraScreen
import com.example.docmat.presentation.ui.screens.history.HistoryScreen
import com.example.docmat.presentation.ui.screens.history.HistoryViewModel
import com.example.docmat.presentation.ui.screens.homescreen.HomeScreen
import com.example.docmat.presentation.ui.screens.homescreen.HomeViewModel
import com.example.docmat.presentation.ui.screens.preview.PreviewScreen
import com.example.docmat.presentation.ui.screens.detail.DetailResultScreen
import com.example.docmat.presentation.ui.screens.settings.SettingsScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun DocmatNavigation(
    navController: NavHostController = rememberNavController(),
    homeViewModel: HomeViewModel = viewModel(),
    historyViewModel: HistoryViewModel = viewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide bottom navigation for specific routes
    val shouldShowBottomBar = when {
        currentRoute == DocmatScreens.Login.route -> false
        currentRoute == DocmatScreens.Register.route -> false
        currentRoute == DocmatScreens.Camera.route -> false
        currentRoute?.startsWith("preview/") == true -> false
        currentRoute?.startsWith("detail_result/") == true -> false
        else -> true
    }

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    painter = painterResource(id = item.icon),
                                    contentDescription = item.title,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = { Text(item.title) },
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = Color.Gray,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->

        // Start destination
        val startDest =
            if (FirebaseAuth.getInstance().currentUser != null)
                DocmatScreens.Home.route
            else
                DocmatScreens.Login.route

        NavHost(
            navController = navController,
            startDestination = startDest,
            modifier = if (shouldShowBottomBar) {
                Modifier.padding(paddingValues)
            } else {
                Modifier
            }
        ) {
            composable(DocmatScreens.Login.route) {
                LoginScreen(
                    onNavigateToRegister = {
                        navController.navigate(DocmatScreens.Register.route)
                    },
                    onLoginSuccess = {
                        navController.navigate(DocmatScreens.Home.route) {
                            popUpTo(DocmatScreens.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(DocmatScreens.Register.route) {
                val registerViewModel: RegisterViewModel = hiltViewModel()
                RegisterScreen(
                    viewModel = registerViewModel,
                    onNavigateToLogin = {
                        navController.popBackStack()
                    },
                    onRegisterSuccess = {
                        navController.navigate(DocmatScreens.Home.route) {
                            popUpTo(DocmatScreens.Register.route) { inclusive = true }
                        }
                    },
                    onEvent = { event ->
                        // Handle register events through viewModel
                        registerViewModel.onEvent(event)
                    }
                )
            }

            composable(DocmatScreens.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToCamera = {
                        navController.navigate(DocmatScreens.Camera.route)
                    },
                    onNavigateToGallery = {
                        val encodedUri = Uri.encode(it.toString())
                        navController.navigate(DocmatScreens.Preview.createRoute(encodedUri))
                    }
                )
            }

            composable(DocmatScreens.News.route) {
                // TODO: Implement news screen
                Text(text = "News", textAlign = TextAlign.Center)
            }

            composable(DocmatScreens.History.route) {
                HistoryScreen(
                    viewModel = historyViewModel,
                    onNavigateToDetail = { historyItem ->
                        // TODO: Navigate to detail screen
                    }
                )
            }

            composable(DocmatScreens.Settings.route) {
                SettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onLogoutClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate(DocmatScreens.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(DocmatScreens.Camera.route) {
                CameraScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onImageCaptured = { uri ->
                        val encodedUri = Uri.encode(uri.toString())
                        navController.navigate(DocmatScreens.Preview.createRoute(encodedUri))
                    }
                )
            }

            composable(
                route = DocmatScreens.Preview.route,
                arguments = listOf(
                    navArgument("imageUri") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val encodedUri = backStackEntry.arguments?.getString("imageUri")
                val imageUri = Uri.parse(Uri.decode(encodedUri))

                PreviewScreen(
                    imageUri = imageUri,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onRetakePhoto = {
                        navController.popBackStack(DocmatScreens.Camera.route, inclusive = false)
                    },
                    onAnalyzePhoto = { predictionResult, originalImageUri ->
                        try {
                            val encodedImageUri = Uri.encode(originalImageUri.toString())
                            
                            // Use Base64 encoding untuk avoid JSON escaping issues
                            val jsonString = com.google.gson.Gson().toJson(predictionResult)
                            val base64Json = Base64.encodeToString(
                                jsonString.toByteArray(Charsets.UTF_8), 
                                Base64.URL_SAFE or Base64.NO_WRAP
                            )
                            
                            navController.navigate(
                                DocmatScreens.DetailResult.createRoute(base64Json, encodedImageUri)
                            )
                        } catch (e: Exception) {
                            android.util.Log.e("Navigation", "Failed to serialize result: ${e.message}")
                            // Fallback: just go back
                            navController.popBackStack()
                        }
                    }
                )
            }

            composable(
                route = DocmatScreens.DetailResult.route,
                arguments = listOf(
                    navArgument("resultJson") { type = NavType.StringType },
                    navArgument("imageUri") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val base64ResultJson = backStackEntry.arguments?.getString("resultJson")
                val encodedImageUri = backStackEntry.arguments?.getString("imageUri")
                val imageUri = Uri.decode(encodedImageUri)
                
                val predictionResult = try {
                    // Decode Base64 back to JSON string
                    val jsonBytes = Base64.decode(base64ResultJson, Base64.URL_SAFE or Base64.NO_WRAP)
                    val jsonString = String(jsonBytes, Charsets.UTF_8)
                    
                    com.google.gson.Gson().fromJson(
                        jsonString,
                        com.example.docmat.domain.model.PredictionResult::class.java
                    )
                } catch (e: Exception) {
                    android.util.Log.e("Navigation", "Failed to deserialize result: ${e.message}")
                    // Return to previous screen if deserialization fails
                    navController.popBackStack()
                    return@composable
                }

                DetailResultScreen(
                    predictionResult = predictionResult,
                    imageUri = imageUri,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onAnalyzeAgain = {
                        navController.popBackStack(DocmatScreens.Preview.route, inclusive = false)
                    },
                    onShare = {
                        // TODO: Implement share functionality
                    }
                )
            }
        }
    }
}

// Bottom navigation items
private val bottomNavItems = listOf(
    BottomNavItem(
        title = "Home",
        icon = R.drawable.ic_home,
        route = DocmatScreens.Home.route
    ),
    BottomNavItem(
        title = "History",
        icon = R.drawable.ic_history,
        route = DocmatScreens.History.route
    ),
    BottomNavItem(
        title = "News",
        icon = R.drawable.ic_news,
        route = DocmatScreens.News.route
    ),
    BottomNavItem(
        title = "Settings",
        icon = R.drawable.ic_settings,
        route = DocmatScreens.Settings.route
    )
)

data class BottomNavItem(
    val title: String,
    val icon: Int,
    val route: String
)

sealed class DocmatScreens(val route: String) {
    data object Login : DocmatScreens("login")
    data object Register : DocmatScreens("register")
    data object Home : DocmatScreens("home")
    data object History : DocmatScreens("history")
    data object News : DocmatScreens("news")
    data object Settings : DocmatScreens("settings")
    data object Camera : DocmatScreens("camera")
    data object Preview : DocmatScreens("preview/{imageUri}") {
        fun createRoute(imageUri: String) = "preview/$imageUri"
    }
    data object DetailResult : DocmatScreens("detail_result/{resultJson}/{imageUri}") {
        fun createRoute(resultJson: String, imageUri: String) = "detail_result/$resultJson/$imageUri"
    }
}
