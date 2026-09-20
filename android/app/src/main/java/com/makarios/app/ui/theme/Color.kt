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

// ── Borders & Hairlines ───────────────────────────────────────
val Border = Color(0xFFECE7DF)             // Ultra-delicate warm hairline border
val BorderSubtle = Color(0x0A2C2622)       // Delicate translucent border (~4% alpha)

// ── Atmospheric Gradients (Serene Twilight Sanctuary) ─────────
val AtmosphericGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF342E2B), // Deep candlelit umber top
        Color(0xFF24201D), // Rich serene twilight core
        Color(0xFF171513)  // Deepest obsidian bottom
    )
)

val AtmosphericCardGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF322C29),
        Color(0xFF221E1C)
    )
)

val AmberAtmosphericGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF63382B), // Muted warm terracotta glow
        Color(0xFF3A241E),
        Color(0xFF1F1614)
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
