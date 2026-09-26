package com.makarios.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ── Canvas & Surface ──────────────────────────────────────────
val Porcelain = Color(0xFFFBF9F5)           // Primary background — warm alabaster linen
val PorcelainWarm = Color(0xFFF3EFE8)       // Soft almond/oat neutral accent
val Surface = Color(0xFFFFFFFF)             // Pure crisp white card surface
val SurfaceMuted = Color(0xFFF6F3EE)        // Muted surface for inputs/search

// ── Text & Dark Accents ───────────────────────────────────────
val Espresso = Color(0xFF2C2622)            // Primary ink & dark buttons — deep warm charcoal-umber
val EspressoLight = Color(0xFF453E38)       // Secondary display text
val Stone = Color(0xFF787069)              // Body text, scripture — warm driftwood
val StoneMuted = Color(0xFFA39B93)          // Captions, metadata, search placeholder

// ── Brand Accents (Quiet Luxury / Contemplative) ───────────────
val Terracotta = Color(0xFFA85842)          // Primary accent — quiet earthy terracotta clay
val TerracottaLight = Color(0xFFF7EBE7)     // Soft terracotta blush wash
val Sage = Color(0xFF607768)               // Secondary accent — muted eucalyptus sage
val SageLight = Color(0xFFF0F4F1)           // Soft sage mist wash
val AmberGold = Color(0xFFC49B45)           // Warm sacred amber/ochre

// ── Luminous Accents & Sunlit Tokens ──────────────────────────
val SunlitGold = Color(0xFFD4A038)          // Radiant sunlit sacred gold
val SunlitGoldLight = Color(0xFFFDF8EE)     // Soft morning sunlight wash
val DawnBlush = Color(0xFFFAF1ED)           // Warm dawn terracotta blush
val LuminousAlabaster = Color(0xFFFCFAF7)   // Bright ethereal linen

// ── Borders & Hairlines ───────────────────────────────────────
val Border = Color(0xFFECE7DF)             // Ultra-delicate warm hairline border
val BorderSubtle = Color(0x0A2C2622)       // Delicate translucent border (~4% alpha)

// ── Radiant & Atmospheric Gradients ───────────────────────────
val SunlitAmberGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFDEAC46), // Glowing sunlit amber dawn top
        Color(0xFFC7922E), // Rich sacred gold core
        Color(0xFFA6761E)  // Warm honey ochre base
    )
)

val LuminousDawnGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFE88A6E), // Warm morning blush
        Color(0xFFD47355),
        Color(0xFFB85A3E)
    )
)

val AlabasterDawnGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFAF7F2), // Crisp warm ivory
        Color(0xFFF3EDE4)  // Soft sunlit linen
    )
)

val SunlitGoldGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFDF7EA), // Radiant morning gold mist
        Color(0xFFF6E8CA)  // Soft honey glow
    )
)

val MorningSageGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFF2F6F3), // Fresh morning dew
        Color(0xFFE2EBE5)  // Soft eucalyptus mist
    )
)

val AtmosphericGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF382F2A), // Candlelit warm umber top
        Color(0xFF28211D), // Serene evening core
        Color(0xFF1B1613)  // Deep espresso base
    )
)

val AtmosphericCardGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF382F2A),
        Color(0xFF241D1A)
    )
)

val AmberAtmosphericGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF6E3E30), // Muted warm terracotta glow
        Color(0xFF452A22),
        Color(0xFF241916)
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
