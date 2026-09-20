package com.makarios.app.ui.theme

import androidx.compose.ui.graphics.Color

// ── Canvas & Surface ──────────────────────────────────────────
val Porcelain = Color(0xFFFAF8F5)           // Primary background
val PorcelainWarm = Color(0xFFEAE3D8)       // Warm almond/sand
val Surface = Color(0xFFFFFFFF)             // Card surface
val SurfaceMuted = Color(0xFFF5F2ED)        // Input fields, muted areas

// ── Text ──────────────────────────────────────────────────────
val Espresso = Color(0xFF1F1118)            // Primary text — deep espresso-plum
val EspressoLight = Color(0xFF3D2E35)       // Secondary display text
val Stone = Color(0xFF6B5E66)              // Body text, scripture
val StoneMuted = Color(0xFF9C9297)          // Captions, meta, placeholders

// ── Accent ────────────────────────────────────────────────────
val Terracotta = Color(0xFFC46851)          // Primary accent — warm salmon
val TerracottaLight = Color(0xFFFAEDE8)     // Soft terracotta tint
val Sage = Color(0xFF5A7362)               // Secondary accent — olive green

// ── Borders & Dividers ───────────────────────────────────────
val Border = Color(0xFFE8E2D8)             // Card borders, dividers
val BorderSubtle = Color(0x0F1F1118)       // Very subtle borders (~6% alpha)

// ── Compatibility Aliases (for Theme.kt & existing references) ─
val BrandPlum = Espresso
val BrandPlumLight = EspressoLight
val BrandPlumDark = Color(0xFF150B10)
val PorcelainBackground = Porcelain
val SurfaceWhite = Surface
val ToneResoluteSand = PorcelainWarm
val SalmonTerracotta = Terracotta
val SalmonLight = TerracottaLight
val LiveFeedGreen = Sage
val LiveFeedGreenBg = Color(0xFFEEF3EE)
val TextPrimary = Espresso
val TextSecondary = Stone
val TextMuted = StoneMuted
val BorderPill = Border
