package com.makarios.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ── Canvas & Surface (from media sp 11 5:00 pm) ───────────────
val Porcelain = Color(0xFFFAF8F5)           // Primary background — warm porcelain ground
val PorcelainWarm = Color(0xFFEAE3D8)       // Warm almond/sand accent
val Surface = Color(0xFFFFFFFF)             // Pure crisp white card surface
val SurfaceMuted = Color(0xFFF5F2ED)        // Muted surface for inputs/search

// ── Text & Dark Accents ───────────────────────────────────────
val Espresso = Color(0xFF1F1118)            // Primary text & dark buttons — deep espresso-plum
val EspressoLight = Color(0xFF3D2E35)       // Secondary display text
val Stone = Color(0xFF6B5E66)              // Body text, scripture
val StoneMuted = Color(0xFF9C9297)          // Captions, metadata, search placeholder

// ── Brand Accents ─────────────────────────────────────────────
val Terracotta = Color(0xFFC46851)          // Primary accent — warm salmon / terracotta
val TerracottaLight = Color(0xFFFAEDE8)     // Soft terracotta background wash
val Sage = Color(0xFF5A7362)               // Secondary accent — eucalyptus sage
val SageLight = Color(0xFFEEF3EE)           // Soft sage background wash
val AmberGold = Color(0xFFD4AF37)           // Warm sacred amber/gold

// ── Borders & Hairlines ───────────────────────────────────────
val Border = Color(0xFFE8E2D8)             // Delicate 0.75dp–1dp card/divider borders
val BorderSubtle = Color(0x0F1F1118)       // Subtle borders (~6% alpha)

// ── Atmospheric Gradients (replacing cold PDF purple with warm espresso glow) ─
val AtmosphericGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF331E2A), // Deep warm plum-espresso top
        Color(0xFF1F1118), // Rich espresso core
        Color(0xFF150B10)  // Deepest bottom
    )
)

val AtmosphericCardGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF2E1925),
        Color(0xFF1F1118)
    )
)

val AmberAtmosphericGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF8A4633), // Warm terracotta-amber glow
        Color(0xFF4A1F2C),
        Color(0xFF1F1118)
    )
)

// ── Compatibility Aliases ─────────────────────────────────────
val BrandPlum = Espresso
val BrandPlumLight = EspressoLight
val BrandPlumDark = Color(0xFF150B10)
val PorcelainBackground = Porcelain
val SurfaceWhite = Surface
val ToneResoluteSand = PorcelainWarm
val SalmonTerracotta = Terracotta
val SalmonLight = TerracottaLight
val LiveFeedGreen = Sage
val LiveFeedGreenBg = SageLight
val TextPrimary = Espresso
val TextSecondary = Stone
val TextMuted = StoneMuted
val BorderPill = Border
