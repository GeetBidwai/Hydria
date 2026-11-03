package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = WaterPrimary,
    onPrimary = Color.White,
    primaryContainer = WaterPrimaryContainer,
    onPrimaryContainer = WaterOnPrimaryContainer,
    secondary = WaterAccentLight,
    onSecondary = Color.White,
    background = WaterBackground,
    onBackground = WaterTextPrimary,
    surface = WaterSurface,
    onSurface = WaterTextPrimary,
    surfaceVariant = WaterSurfaceVariant,
    onSurfaceVariant = WaterTextSecondary,
    outline = WaterOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = WaterAccentLight,
    onPrimary = Color(0xFF0F313A),
    primaryContainer = WaterPrimaryDark,
    onPrimaryContainer = Color(0xFFD4EFF6),
    secondary = WaterPrimary,
    onSecondary = Color.White,
    background = Color(0xFF12181B),
    onBackground = Color(0xFFE5ECEF),
    surface = Color(0xFF1A2226),
    onSurface = Color(0xFFE5ECEF),
    surfaceVariant = Color(0xFF242F34),
    onSurfaceVariant = Color(0xFF98A6AC),
    outline = Color(0xFF37464D)
)

val WaterShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

@Composable
fun WaterIntakeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = WaterShapes,
        content = content
    )
}

// Backward compatibility alias for template references
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    WaterIntakeTheme(darkTheme = darkTheme, content = content)
}
