package com.example.london_live_bus_journey_tracker.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = BusYellow,
    onPrimary = TextOnYellow,
    primaryContainer = BusYellowLight,
    onPrimaryContainer = TextPrimary,
    secondary = StartGreen,
    onSecondary = White,
    secondaryContainer = StartGreen,
    onSecondaryContainer = White,
    tertiary = EndRed,
    onTertiary = White,
    tertiaryContainer = EndRed,
    onTertiaryContainer = White,
    error = ErrorRed,
    onError = White,
    errorContainer = ErrorRed,
    onErrorContainer = White,
    background = White,
    onBackground = TextPrimary,
    surface = White,
    onSurface = TextPrimary,
    surfaceVariant = LightGray,
    onSurfaceVariant = TextSecondary,
    outline = MediumGray,
    outlineVariant = LightGray
)

private val DarkColorScheme = darkColorScheme(
    primary = BusYellow,
    onPrimary = TextOnYellow,
    primaryContainer = BusYellowDark,
    onPrimaryContainer = White,
    secondary = StartGreen,
    onSecondary = White,
    tertiary = EndRed,
    onTertiary = White,
    error = ErrorRed,
    onError = White,
    background = NearBlack,
    onBackground = White,
    surface = CharcoalGray,
    onSurface = White,
    surfaceVariant = CharcoalGray,
    onSurfaceVariant = MediumGray,
    outline = DarkGray,
    outlineVariant = CharcoalGray
)

@Composable
fun LondonBusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}