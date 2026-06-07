package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// --- Apple-inspired Premium Dark Theme Palette ---
val PureBlack = Color(0xFF000000)
val AppleSystemBg = Color(0xFF000000)         // Pure Black background
val AppleCardGray = Color(0xFF1C1C1E)         // iOS Grouped Card Background
val AppleSecondaryCard = Color(0xFF2C2C2E)    // Lighter iOS Dark Gray (nested surfaces)
val AppleStrokeGray = Color(0xFF2C2C2E)       // Premium slim dividers
val AppleTextPrimary = Color(0xFFFFFFFF)      // Pure white readable text
val AppleTextSecondary = Color(0xFF8E8E93)    // iOS SF secondary text grey
val AppleTextTertiary = Color(0xFFAEAEB2)     // Tertiary descriptive grey

val AppleGreen = Color(0xFF30D158)            // iOS systemGreen
val AppleGreenTransparent = Color(0x1230D158) // Highly refined translucent tint
val SoftOrange = Color(0xFFFF9F0A)            // iOS systemOrange
val SoftOrangeTransparent = Color(0x12FF9F0A) // Highly refined warning translucent tint
val SoftRed = Color(0xFFFF453A)               // iOS systemRed
val SoftRedTransparent = Color(0x12FF453A)    // Highly refined alarm translucent tint
val AppleBlue = Color(0xFF0A84FF)             // iOS systemBlue
val AppleGold = Color(0xFFFFD60A)             // iOS systemYellow

// We map existing variables to prevent code compilation failures and to immediately propagate changes
val CyanGlow = AppleTextPrimary               // Use pure/silver display color instead of neon cyan
val HyperGreen = AppleGreen
val ElectricBlue = AppleBlue

val CyberObsidian = PureBlack
val SpaceSlate = AppleCardGray
val SteelGray = AppleSecondaryCard
val CardBorder = AppleStrokeGray
val TextPrimary = AppleTextPrimary
val TextSecondary = AppleTextSecondary
val FlareRed = SoftRed
val CoreAmber = SoftOrange
val CalmGray = AppleTextTertiary
