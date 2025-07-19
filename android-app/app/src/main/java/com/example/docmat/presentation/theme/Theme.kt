package com.example.docmat.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = TomatoRed,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = TomatoRedDark,
    onPrimaryContainer = Color(0xFFFFFFFF),

    // Use TomatoBrown as the secondary color instead of TomatoGreen
    secondary = TomatoBrown,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = TomatoBrownDark,
    onSecondaryContainer = Color(0xFFFFFFFF),

    tertiary = TomatoGreen, // Move TomatoGreen to tertiary
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = TomatoGreenDark,
    onTertiaryContainer = Color(0xFFFFFFFF),

    background = BackgroundDark,
    onBackground = Color(0xFFF8E2E2),
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,

    error = TomatoRedLight
)

private val LightColorScheme = lightColorScheme(
    primary = TomatoRed,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = TomatoRedLight,
    onPrimaryContainer = Color(0xFF3F0000),

    // Use TomatoBrown as the secondary color
    secondary = TomatoBrown,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = TomatoBrownLight,
    onSecondaryContainer = Color(0xFF3F2723),

    tertiary = TomatoGreen,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = TomatoGreenDark,
    onTertiaryContainer = Color(0xFFFFFFFF),

    background = BackgroundLight,
    onBackground = Color(0xFF201A1A),
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,

    error = TomatoRed
)

@Composable
fun DocmatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme

        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}