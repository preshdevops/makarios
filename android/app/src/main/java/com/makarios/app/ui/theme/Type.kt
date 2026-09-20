package com.makarios.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.makarios.app.R

// ── Fraunces — Display serif with explicit variable weight axis mapping ──────
val FrauncesFontFamily = FontFamily(
    Font(
        resId = R.font.fraunces_regular,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(400)
        )
    ),
    Font(
        resId = R.font.fraunces_regular,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(500)
        )
    ),
    Font(
        resId = R.font.fraunces_regular,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(600)
        )
    ),
    Font(
        resId = R.font.fraunces_regular,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(700)
        )
    ),
    Font(
        resId = R.font.fraunces_italic,
        weight = FontWeight.Normal,
        style = FontStyle.Italic,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(400)
        )
    ),
    Font(
        resId = R.font.fraunces_italic,
        weight = FontWeight.Medium,
        style = FontStyle.Italic,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(500)
        )
    )
)

// ── Work Sans — UI humanist sans with explicit variable weight axis mapping ───
val WorkSansFontFamily = FontFamily(
    Font(
        resId = R.font.worksans_regular,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(400)
        )
    ),
    Font(
        resId = R.font.worksans_regular,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(500)
        )
    ),
    Font(
        resId = R.font.worksans_regular,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(600)
        )
    ),
    Font(
        resId = R.font.worksans_regular,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(700)
        )
    )
)

// Semantic aliases
val DisplayFontFamily = FrauncesFontFamily
val BodyFontFamily = WorkSansFontFamily

val MakariosTypography = Typography(
    // Hero question: "What are you carrying today?"
    displayLarge = TextStyle(
        fontFamily = FrauncesFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 25.sp,
        lineHeight = 33.sp,
        letterSpacing = (-0.4).sp
    ),
    // Declaration text on truth card
    headlineMedium = TextStyle(
        fontFamily = FrauncesFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 31.sp,
        letterSpacing = (-0.2).sp
    ),
    // Makarios wordmark / section titles
    titleLarge = TextStyle(
        fontFamily = FrauncesFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 19.sp,
        lineHeight = 25.sp,
        letterSpacing = (-0.2).sp
    ),
    // Section headers, tab labels
    titleMedium = TextStyle(
        fontFamily = WorkSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    ),
    // Body text, descriptions
    bodyLarge = TextStyle(
        fontFamily = WorkSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.5.sp,
        lineHeight = 23.sp,
        letterSpacing = 0.1.sp
    ),
    // Scripture text in italic serif
    bodyMedium = TextStyle(
        fontFamily = FrauncesFontFamily,
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.Normal,
        fontSize = 14.5.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    ),
    // Button labels, category pills
    labelLarge = TextStyle(
        fontFamily = WorkSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp
    ),
    // Scripture reference, meta text
    labelSmall = TextStyle(
        fontFamily = WorkSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.5.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.6.sp
    )
)
