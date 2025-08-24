package com.example.docmat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.docmat.presentation.navigation.DocmatNavigation
import com.example.docmat.presentation.theme.DocmatTheme
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen
        val splashScreen = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        
        // Keep the splash screen visible until the app is ready
        splashScreen.setKeepOnScreenCondition {
            // You can add your app initialization logic here
            // Return false when the app is ready
            false
        }
        setContent {
            DocmatTheme {
              Surface(
                  modifier = Modifier.fillMaxSize(),
                  color = MaterialTheme.colorScheme.background
              ) {
                  DocmatNavigation()
              }
            }
        }
    }
}

