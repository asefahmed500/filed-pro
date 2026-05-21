package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

// Declare color keys at the top so they are fully initialized when building the scheme properties
val DarkForegroundKey = Color(0xFFF4F4F5)
val LightForegroundKey = Color(0xFF1E293B)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkBg,
    secondary = DarkSecondary,
    onSecondary = DarkForegroundKey,
    tertiary = SuccessGreen,
    background = DarkBg,
    onBackground = DarkForegroundKey,
    surface = DarkCardBg,
    onSurface = DarkForegroundKey,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
    error = AlertRose,
    primaryContainer = DarkPrimary.copy(alpha = 0.15f),
    onPrimaryContainer = DarkPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightBg,
    secondary = LightSecondary,
    onSecondary = LightPrimary,
    tertiary = SuccessGreen,
    background = LightBg,
    onBackground = LightForegroundKey,
    surface = LightCardBg,
    onSurface = LightForegroundKey,
    outline = LightBorder,
    outlineVariant = LightBorder,
    error = AlertRose,
    primaryContainer = LightPrimary.copy(alpha = 0.1f),
    onPrimaryContainer = LightPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Force false to enforce crisp white background theme across all screens
    dynamicColor: Boolean = false, // Keep false to respect custom gold branding design pattern
    content: @Composable () -> Unit,
) {
    // Force LightColorScheme (pristine pure-white backgrounds) as requested
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
