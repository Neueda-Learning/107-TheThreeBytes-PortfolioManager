package com.hsbc.portfoliomanager.ui.theme

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

// Groww is a dark-first app — use the dark scheme as default
private val GrowwDarkColorScheme = darkColorScheme(
    primary              = GrowwGreen,
    onPrimary            = Color.Black,
    primaryContainer     = GrowwGreenDark,
    onPrimaryContainer   = Color.White,
    secondary            = AccentBlue,
    onSecondary          = Color.White,
    secondaryContainer   = Color(0xFF1C3A5C),
    onSecondaryContainer = Color.White,
    tertiary             = AccentPurple,
    onTertiary           = Color.White,
    background           = GrowwBg,
    onBackground         = TextPrimary,
    surface              = GrowwSurface,
    onSurface            = TextPrimary,
    surfaceVariant       = GrowwSurface2,
    onSurfaceVariant     = TextSecondary,
    outline              = GrowwSurface3,
    error                = GrowwRed,
    onError              = Color.White,
    errorContainer       = GrowwRedAlpha,
    onErrorContainer     = GrowwRed,
    inverseSurface       = Color.White,
    inverseOnSurface     = GrowwBg,
    inversePrimary       = GrowwGreenDark
)

// Provide a light fallback (still Groww-branded but lighter)
private val GrowwLightColorScheme = lightColorScheme(
    primary              = GrowwGreenDark,
    onPrimary            = Color.White,
    primaryContainer     = GrowwGreenAlpha,
    onPrimaryContainer   = GrowwGreenDark,
    secondary            = AccentBlue,
    onSecondary          = Color.White,
    secondaryContainer   = Color(0xFFD6E8FF),
    onSecondaryContainer = Color(0xFF003A75),
    tertiary             = AccentPurple,
    onTertiary           = Color.White,
    background           = Color(0xFFF5F5F5),
    onBackground         = Color(0xFF1A1A1A),
    surface              = Color.White,
    onSurface            = Color(0xFF1A1A1A),
    surfaceVariant       = Color(0xFFEEEEEE),
    onSurfaceVariant     = Color(0xFF555555),
    outline              = Color(0xFFCCCCCC),
    error                = GrowwRed,
    onError              = Color.White
)

@Composable
fun PortfolioManagerTheme(
    darkTheme: Boolean = true,   // Groww defaults to dark
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) GrowwDarkColorScheme else GrowwLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = GrowwBg.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
