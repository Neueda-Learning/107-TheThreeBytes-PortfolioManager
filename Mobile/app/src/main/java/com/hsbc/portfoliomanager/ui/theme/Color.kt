package com.hsbc.portfoliomanager.ui.theme

import androidx.compose.ui.graphics.Color

// ── Groww Primary ─────────────────────────────────────────────────────────────
val GrowwGreen       = Color(0xFF00C853)  // Groww signature green
val GrowwGreenDark   = Color(0xFF009624)
val GrowwGreenLight  = Color(0xFF69F97C)
val GrowwGreenAlpha  = Color(0x2600C853)  // 15% green for backgrounds

// ── Backgrounds ──────────────────────────────────────────────────────────────
val GrowwBg          = Color(0xFF0E0E0E)  // near-black app background
val GrowwSurface     = Color(0xFF1A1A1A)  // card/surface
val GrowwSurface2    = Color(0xFF252525)  // elevated surface
val GrowwSurface3    = Color(0xFF2E2E2E)  // dividers / chips

// ── Text ────────────────────────────────────────────────────────────────────
val TextPrimary      = Color(0xFFFFFFFF)
val TextSecondary    = Color(0xFFAAAAAA)
val TextHint         = Color(0xFF666666)
val TextOnPrimary    = Color(0xFFFFFFFF)

// ── Semantic Colors ──────────────────────────────────────────────────────────
val GrowwRed         = Color(0xFFFF3B30)  // loss / negative
val GrowwRedAlpha    = Color(0x26FF3B30)
val AccentGreen      = GrowwGreen
val AccentOrange     = Color(0xFFFF9500)
val AccentBlue       = Color(0xFF0A84FF)
val AccentPurple     = Color(0xFFBF5AF2)
val AccentTeal       = Color(0xFF32D74B)

val SuccessGreen     = GrowwGreen
val ErrorRed         = GrowwRed
val WarningOrange    = AccentOrange
val InfoBlue         = AccentBlue

// ── Bottom Nav ───────────────────────────────────────────────────────────────
val BottomNavBg      = Color(0xFF141414)
val BottomNavBorder  = Color(0xFF2A2A2A)

// ── Chart Colors ─────────────────────────────────────────────────────────────
val ChartColors = listOf(
    GrowwGreen,
    Color(0xFF0A84FF),  // Blue
    Color(0xFFFF9500),  // Orange
    Color(0xFFBF5AF2),  // Purple
    Color(0xFFFF375F),  // Pink
    Color(0xFF32D74B),  // Teal-green
    Color(0xFF64D2FF),  // Cyan
    Color(0xFFFFD60A),  // Yellow
)
