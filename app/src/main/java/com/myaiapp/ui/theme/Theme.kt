package com.myaiapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = AppColors.Blue,
    onPrimary = Color.White,
    primaryContainer = AppColors.Blue.copy(alpha = 0.1f),
    onPrimaryContainer = AppColors.Blue,

    secondary = AppColors.Purple,
    onSecondary = Color.White,
    secondaryContainer = AppColors.Purple.copy(alpha = 0.1f),
    onSecondaryContainer = AppColors.Purple,

    tertiary = AppColors.Teal,
    onTertiary = Color.White,

    background = AppColors.Background,
    onBackground = AppColors.Gray900,

    surface = AppColors.Surface,
    onSurface = AppColors.Gray900,
    surfaceVariant = AppColors.SurfaceVariant,
    onSurfaceVariant = AppColors.Gray600,

    outline = AppColors.Gray200,
    outlineVariant = AppColors.Gray100,

    error = AppColors.Red,
    onError = Color.White,
    errorContainer = AppColors.Red.copy(alpha = 0.1f),
    onErrorContainer = AppColors.Red
)

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.Blue,
    onPrimary = Color.White,
    primaryContainer = AppColors.Blue.copy(alpha = 0.2f),
    onPrimaryContainer = AppColors.Blue,

    secondary = AppColors.Purple,
    onSecondary = Color.White,

    background = Color(0xFF121212),
    onBackground = Color.White,

    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2D2D2D),
    onSurfaceVariant = AppColors.Gray400,

    outline = AppColors.Gray700,
    outlineVariant = AppColors.Gray800,

    error = AppColors.Red,
    onError = Color.White
)

@Composable
fun MyAIAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
