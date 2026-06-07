package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CosmicDarkColorScheme = darkColorScheme(
    primary = CyanGlow,
    secondary = HyperGreen,
    tertiary = ElectricBlue,
    background = CyberObsidian,
    surface = SpaceSlate,
    onPrimary = CyberObsidian,
    onSecondary = CyberObsidian,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = SteelGray,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder,
    error = FlareRed
)

// We maintain standard fallback LightColorScheme just in case, but keep it highly polished
private val CosmicLightColorScheme = lightColorScheme(
    primary = ElectricBlue,
    secondary = HyperGreen,
    tertiary = CyanGlow,
    background = androidx.compose.ui.graphics.Color(0xFFF8FAFC),
    surface = androidx.compose.ui.graphics.Color.White,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = androidx.compose.ui.graphics.Color.Black,
    onBackground = androidx.compose.ui.graphics.Color(0xFF0F172A),
    onSurface = androidx.compose.ui.graphics.Color(0xFF0F172A)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force Dark Theme for amazing Cyber aesthetic
    dynamicColor: Boolean = false, // Disable dynamic colors to keep ChargeDoctor's gorgeous identity
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) CosmicDarkColorScheme else CosmicLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
