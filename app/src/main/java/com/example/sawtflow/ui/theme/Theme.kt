package com.example.sawtflow.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = Color.White,
    primaryContainer = NavyLight,
    onPrimaryContainer = Color.White,
    secondary = AmberAccent,
    onSecondary = Color.White,
    background = NavyPrimary,
    onBackground = Color.White,
    surface = NavySurface,
    onSurface = Color.White,
    surfaceVariant = NavyCard,
    onSurfaceVariant = Color.White,
    outline = CyanAccent.copy(alpha = 0.3f)
)

private val LightColorScheme = lightColorScheme(
    primary = BlueBrand,
    onPrimary = CreamBg,
    primaryContainer = Color(0xFFDCE9FF),
    onPrimaryContainer = BlueBrand,
    secondary = AmberDark,
    onSecondary = CreamBg,
    background = CreamBg,
    onBackground = TextDark,
    surface = CreamSurface,
    onSurface = TextDark,
    surfaceVariant = Color(0xFFE8EFF8),
    onSurfaceVariant = TextDark,
    outline = BorderLight
)

@Composable
fun SawtFlowTheme(
    darkTheme: Boolean = true, // SawtFlow looks stunning in dark mode brand style
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SawtFlowTypography
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides TextStyle(
                fontFamily = TajawalFontFamily,
                color = Color.White
            ),
            content = content
        )
    }
}
