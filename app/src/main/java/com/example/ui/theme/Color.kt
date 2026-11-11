package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// =========================================================================
// Hydria Minimalist Base Palette (Preserved for backward compatibility)
// =========================================================================
val WaterPrimary = Color(0xFF1E88A8)
val WaterPrimaryDark = Color(0xFF0F4C64)
val WaterPrimaryContainer = Color(0xFFE2F4F8)
val WaterOnPrimaryContainer = Color(0xFF093344)

val WaterBackground = Color(0xFFF4F9FB)
val WaterSurface = Color(0xFFFFFFFF)
val WaterSurfaceVariant = Color(0xFFEAF3F6)

val WaterTextPrimary = Color(0xFF17282F)
val WaterTextSecondary = Color(0xFF6F8791)

val WaterAccentLight = Color(0xFF4AC4DE)
val WaterAccentSuccess = Color(0xFF32B57B)
val WaterOutline = Color(0xFFD6E7ED)

// =========================================================================
// Hydria Ocean Experience Design System Tokens
// =========================================================================
val OceanDeep = Color(0xFF0B3B4E)
val OceanMid = Color(0xFF15657E)
val OceanPrimary = Color(0xFF1E88A8)
val OceanAqua = Color(0xFF26C6DA)
val OceanCyan = Color(0xFF4DD0E1)
val OceanFoam = Color(0xFFE0F7FA)

val OceanBackgroundLight = Color(0xFFF2F8FA)
val OceanCardSurface = Color(0xFFFFFFFF)
val OceanCardBorder = Color(0xFFD8EBF1)

val OceanGold = Color(0xFFFFB300)
val OceanGoldLight = Color(0xFFFFF8E1)
val OceanCoral = Color(0xFFFF7043)
val OceanCoralLight = Color(0xFFFFEBE5)

// Mascot Colors
val MascotDropletBody = Color(0xFF29B6F6)
val MascotDropletShade = Color(0xFF0288D1)
val MascotDropletHighlight = Color(0xFFE1F5FE)
val MascotCheekBlush = Color(0xFFFF8A80)
val MascotEyeColor = Color(0xFF082633)

// Ocean Gradients
val OceanGradientHero = listOf(Color(0xFF0D4E68), Color(0xFF1E88A8), Color(0xFF26C6DA))
val OceanGradientCard = listOf(Color(0xFFEBF7FA), Color(0xFFF7FCFD))
val OceanGradientWater = listOf(Color(0xFF0288D1), Color(0xFF26C6DA))
val OceanGradientCelebration = listOf(Color(0xFF15657E), Color(0xFF00B0FF), Color(0xFF69F0AE))
val OceanGradientGold = listOf(Color(0xFFFFB300), Color(0xFFFFCA28))

object OceanBrushes {
    fun heroBrush(): Brush = Brush.verticalGradient(OceanGradientHero)
    fun cardBrush(): Brush = Brush.verticalGradient(OceanGradientCard)
    fun waterBrush(): Brush = Brush.verticalGradient(OceanGradientWater)
    fun celebrationBrush(): Brush = Brush.linearGradient(OceanGradientCelebration)
}
