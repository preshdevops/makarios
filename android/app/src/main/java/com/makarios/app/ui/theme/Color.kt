package com.makarios.app.ui.theme

import androidx.compose.ui.graphics.Color

// Paper & Parchment Canvas (Warm, tactile vellum)
val ParchmentBackground = Color(0xFFFBF9F5)  // Tactile warm broadsheet paper
val ParchmentWarm = Color(0xFFF4EFE6)        // Aged parchment accent / tabs
val PaperSurface = Color(0xFFFFFFFF)         // Pure matte rag paper
val PaperMuted = Color(0xFFF7F4EE)           // Editorial plate matting

// Ink Tones (Literary & Typographic)
val InkLampblack = Color(0xFF161311)         // Primary text - deep bistre / lampblack
val InkCharcoal = Color(0xFF2C2622)          // Sub-headers & secondary display
val InkIronGall = Color(0xFF5A524C)          // Scripture commentary & reflections
val InkMuted = Color(0xFF938980)             // Folio pagination, chapter markers, meta

// Sacred Rubrication & Illuminated Accents
val RubricVermilion = Color(0xFFB34335)      // Classic illuminated rubric red (warm cinnabar)
val RubricVermilionLight = Color(0xFFFAECE8) // Soft tint for rubric highlights
val GoldLeaf = Color(0xFFC49B58)             // Illuminated metallic gold leaf
val GoldLeafMuted = Color(0xFFDFB28A)        // Soft amber gold
val LaurelSage = Color(0xFF435948)           // Restful olive green for peace/healing

// Editorial Hairlines & Rules
val HairlineRule = Color(0xFFE8E2D8)         // 0.75dp delicate separator rules
val BorderBroadsheet = Color(0xFFE2DDD3)     // Plate framing
val BorderSubtle = Color(0x12161311)         // ~7% ink alpha
val BorderCard = Color(0x0F161311)           // Subtle card stroke

// Core & Compatibility mappings
val BrandPlum = InkLampblack
val BrandPlumLight = InkCharcoal
val BrandPlumDark = Color(0xFF0F0D0C)
val PorcelainBackground = ParchmentBackground
val PorcelainWarm = ParchmentWarm
val SurfaceWhite = PaperSurface
val ToneResoluteSand = ParchmentWarm
val SalmonTerracotta = RubricVermilion
val SalmonLight = RubricVermilionLight
val LiveFeedGreen = LaurelSage
val LiveFeedGreenBg = Color(0xFFEEF3EE)
val AmberGold = GoldLeaf
val TextPrimary = InkLampblack
val TextSecondary = InkIronGall
val TextMuted = InkMuted
val BorderPill = HairlineRule
