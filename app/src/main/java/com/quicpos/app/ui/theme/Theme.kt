package com.quicpos.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Available app themes.
 */
enum class AppTheme(val displayName: String, val isDark: Boolean) {
    NIGHT_BLUE("Night Blue", true),
    DARK_GREEN("Dark Green", true),
    LIGHT("Light", false),
    AMOLED_BLACK("AMOLED Black", true);

    companion object {
        fun fromString(value: String): AppTheme =
            entries.find { it.name == value } ?: NIGHT_BLUE
    }
}

// ─── Night Blue (Default) ───
private val NightBlueColorScheme = darkColorScheme(
    primary = Green500,
    onPrimary = TextOnGreen,
    primaryContainer = NightBlue700,
    onPrimaryContainer = Green100,
    secondary = NightBlueAccent,
    onSecondary = TextOnGreen,
    secondaryContainer = NightBlue800,
    onSecondaryContainer = NightBlue200,
    tertiary = InfoBlue,
    onTertiary = TextOnGreen,
    background = NightBlue950,
    onBackground = NightBlueTextPrimary,
    surface = NightBlue900,
    onSurface = NightBlueTextPrimary,
    surfaceVariant = NightBlue800,
    onSurfaceVariant = NightBlueTextSecondary,
    surfaceContainerHigh = NightBlue700,
    surfaceContainerLow = NightBlue800,
    outline = NightBlueDivider,
    outlineVariant = NightBlue600,
    error = ErrorRed,
    onError = TextOnGreen,
    errorContainer = ErrorRedDark,
    onErrorContainer = Color(0xFFFFDAD6),
    inverseSurface = LightSurface,
    inverseOnSurface = TextDark,
    inversePrimary = Green800
)

// ─── Dark Green (Original) ───
private val DarkGreenColorScheme = darkColorScheme(
    primary = Green500,
    onPrimary = TextOnGreen,
    primaryContainer = Green800,
    onPrimaryContainer = Green100,
    secondary = Green400,
    onSecondary = TextDark,
    secondaryContainer = Green900,
    onSecondaryContainer = Green200,
    tertiary = InfoBlue,
    onTertiary = TextOnGreen,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    surfaceContainerHigh = DarkSurfaceElevated,
    surfaceContainerLow = DarkSurfaceCard,
    outline = DarkDivider,
    outlineVariant = Color(0xFF505050),
    error = ErrorRed,
    onError = TextOnGreen,
    errorContainer = ErrorRedDark,
    onErrorContainer = Color(0xFFFFDAD6),
    inverseSurface = LightSurface,
    inverseOnSurface = TextDark,
    inversePrimary = Green800
)

// ─── Light ───
private val LightColorScheme = lightColorScheme(
    primary = Green800,
    onPrimary = TextOnGreen,
    primaryContainer = Green100,
    onPrimaryContainer = Green900,
    secondary = Green600,
    onSecondary = TextOnGreen,
    secondaryContainer = Green50,
    onSecondaryContainer = Green900,
    tertiary = InfoBlue,
    onTertiary = TextOnGreen,
    background = LightBackground,
    onBackground = TextDark,
    surface = LightSurface,
    onSurface = TextDark,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextDarkSecondary,
    surfaceContainerHigh = LightSurfaceElevated,
    surfaceContainerLow = LightSurfaceCard,
    outline = Color(0xFFBDBDBD),
    outlineVariant = Color(0xFFE0E0E0),
    error = ErrorRedDark,
    onError = TextOnGreen,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    inverseSurface = DarkSurface,
    inverseOnSurface = TextPrimary,
    inversePrimary = Green400
)

// ─── AMOLED Black ───
private val AmoledBlackColorScheme = darkColorScheme(
    primary = Green500,
    onPrimary = TextOnGreen,
    primaryContainer = Green900,
    onPrimaryContainer = Green100,
    secondary = Green400,
    onSecondary = TextDark,
    secondaryContainer = Color(0xFF1A3A1A),
    onSecondaryContainer = Green200,
    tertiary = InfoBlue,
    onTertiary = TextOnGreen,
    background = AmoledBlack,
    onBackground = TextPrimary,
    surface = AmoledSurface,
    onSurface = TextPrimary,
    surfaceVariant = AmoledSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    surfaceContainerHigh = AmoledSurfaceElevated,
    surfaceContainerLow = AmoledSurfaceCard,
    outline = AmoledDivider,
    outlineVariant = Color(0xFF333333),
    error = ErrorRed,
    onError = TextOnGreen,
    errorContainer = ErrorRedDark,
    onErrorContainer = Color(0xFFFFDAD6),
    inverseSurface = LightSurface,
    inverseOnSurface = TextDark,
    inversePrimary = Green800
)

@Composable
fun QuicPOSTheme(
    appTheme: AppTheme = AppTheme.NIGHT_BLUE,
    content: @Composable () -> Unit
) {
    val colorScheme = when (appTheme) {
        AppTheme.NIGHT_BLUE -> NightBlueColorScheme
        AppTheme.DARK_GREEN -> DarkGreenColorScheme
        AppTheme.LIGHT -> LightColorScheme
        AppTheme.AMOLED_BLACK -> AmoledBlackColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !appTheme.isDark
                isAppearanceLightNavigationBars = !appTheme.isDark
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = QuicPOSTypography,
        shapes = QuicPOSShapes,
        content = content
    )
}
