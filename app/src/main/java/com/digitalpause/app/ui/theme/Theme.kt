package com.digitalpause.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = Color.White,
    primaryContainer = IndigoContainer,
    onPrimaryContainer = IndigoOnContainer,
    secondary = MindfulTeal,
    onSecondary = Color.White,
    secondaryContainer = MindfulTealContainer,
    onSecondaryContainer = MindfulTealOnContainer,
    tertiary = GentleAmber,
    onTertiary = Color.White,
    tertiaryContainer = GentleAmberContainer,
    onTertiaryContainer = GentleAmberOnContainer,
    error = PauseCrimson,
    onError = Color.White,
    errorContainer = PauseCrimsonContainer,
    onErrorContainer = PauseCrimsonOnContainer,
    background = CanvasLight,
    onBackground = OnCanvasLight,
    surface = CanvasLight,
    onSurface = OnCanvasLight,
    surfaceVariant = CanvasVariantLight,
    onSurfaceVariant = OnCanvasVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight
)

private val DarkColorScheme = darkColorScheme(
    primary = IndigoPrimaryDark,
    onPrimary = Color(0xFF0B1033),
    primaryContainer = IndigoContainer,
    onPrimaryContainer = IndigoOnContainer,
    secondary = MindfulTealDark,
    onSecondary = Color(0xFF003732),
    secondaryContainer = Color(0xFF115E59),
    onSecondaryContainer = Color(0xFF99F6E4),
    tertiary = GentleAmberDark,
    onTertiary = Color(0xFF451A03),
    tertiaryContainer = GentleAmberContainer,
    onTertiaryContainer = GentleAmberOnContainer,
    error = PauseCrimsonDark,
    onError = Color(0xFF4C0519),
    errorContainer = Color(0xFF881337),
    onErrorContainer = Color(0xFFFFD1DC),
    background = CanvasDark,
    onBackground = OnCanvasDark,
    surface = CanvasDark,
    onSurface = OnCanvasDark,
    surfaceVariant = CanvasVariantDark,
    onSurfaceVariant = OnCanvasVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark
)

// Extended Stitch Surface and Semantic Color Tokens
data class ExtendedColors(
    val surfaceContainerLowest: Color,
    val surfaceContainerLow: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val surfaceContainerHighest: Color,
    val primaryFixed: Color,
    val onPrimaryFixed: Color,
    val secondaryFixed: Color,
    val secondaryFixedDim: Color,
    val onSecondaryFixed: Color,
    val tertiaryFixed: Color,
    val tertiaryFixedDim: Color,
    val onTertiaryFixed: Color
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        surfaceContainerLowest = SurfaceContainerLowestLight,
        surfaceContainerLow = SurfaceContainerLowLight,
        surfaceContainer = SurfaceContainerBaseLight,
        surfaceContainerHigh = SurfaceContainerHighLight,
        surfaceContainerHighest = SurfaceContainerHighestLight,
        primaryFixed = IndigoPrimaryFixed,
        onPrimaryFixed = IndigoOnPrimaryFixed,
        secondaryFixed = MindfulTealFixed,
        secondaryFixedDim = MindfulTealFixedDim,
        onSecondaryFixed = MindfulTealOnFixed,
        tertiaryFixed = GentleAmberFixed,
        tertiaryFixedDim = GentleAmberFixedDim,
        onTertiaryFixed = GentleAmberOnFixed
    )
}

val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    get() = LocalExtendedColors.current

@Composable
fun DigitalPauseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) {
        ExtendedColors(
            surfaceContainerLowest = SurfaceContainerLowestDark,
            surfaceContainerLow = SurfaceContainerLowDark,
            surfaceContainer = SurfaceContainerBaseDark,
            surfaceContainerHigh = SurfaceContainerHighDark,
            surfaceContainerHighest = SurfaceContainerHighestDark,
            primaryFixed = Color(0xFF3730A3),
            onPrimaryFixed = Color(0xFFE0E7FF),
            secondaryFixed = Color(0xFF134E4A),
            secondaryFixedDim = Color(0xFF115E59),
            onSecondaryFixed = Color(0xFFCCFBF1),
            tertiaryFixed = Color(0xFF78350F),
            tertiaryFixedDim = Color(0xFF92400E),
            onTertiaryFixed = Color(0xFFFEF3C7)
        )
    } else {
        ExtendedColors(
            surfaceContainerLowest = SurfaceContainerLowestLight,
            surfaceContainerLow = SurfaceContainerLowLight,
            surfaceContainer = SurfaceContainerBaseLight,
            surfaceContainerHigh = SurfaceContainerHighLight,
            surfaceContainerHighest = SurfaceContainerHighestLight,
            primaryFixed = IndigoPrimaryFixed,
            onPrimaryFixed = IndigoOnPrimaryFixed,
            secondaryFixed = MindfulTealFixed,
            secondaryFixedDim = MindfulTealFixedDim,
            onSecondaryFixed = MindfulTealOnFixed,
            tertiaryFixed = GentleAmberFixed,
            tertiaryFixedDim = GentleAmberFixedDim,
            onTertiaryFixed = GentleAmberOnFixed
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}
