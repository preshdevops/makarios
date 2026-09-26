package com.makarios.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.makarios.app.R

// ── Cormorant Garamond — Classical, sacred display serif ─────
val CormorantGaramondFontFamily = FontFamily(
    Font(R.font.cormorant_garamond_regular, FontWeight.Normal),
    Font(R.font.cormorant_garamond_regular, FontWeight.Medium),
    Font(R.font.cormorant_garamond_regular, FontWeight.SemiBold),
    Font(R.font.cormorant_garamond_regular, FontWeight.Bold),
    Font(R.font.cormorant_garamond_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.cormorant_garamond_italic, FontWeight.Medium, FontStyle.Italic)
)

// Legacy alias to prevent any build breakage
val FrauncesFontFamily = CormorantGaramondFontFamily

// ── Work Sans — UI humanist sans ─────────────────────────────
val WorkSansFontFamily = FontFamily(
    Font(R.font.worksans_regular, FontWeight.Normal),
    Font(R.font.worksans_regular, FontWeight.Medium),
    Font(R.font.worksans_regular, FontWeight.SemiBold),
    Font(R.font.worksans_regular, FontWeight.Bold)
)

// Semantic aliases
val DisplayFontFamily = CormorantGaramondFontFamily
val BodyFontFamily = WorkSansFontFamily

val MakariosTypography = Typography(
    // Hero question: "What are you carrying today?"
    displayLarge = TextStyle(
        fontFamily = CormorantGaramondFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 27.sp,
        lineHeight = 35.sp,
        letterSpacing = (-0.3).sp
    ),
    // Declaration text on truth card
    headlineMedium = TextStyle(
        fontFamily = CormorantGaramondFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 23.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.2).sp
    ),
    // Makarios wordmark / section titles
    titleLarge = TextStyle(
        fontFamily = CormorantGaramondFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.1).sp
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
        fontFamily = CormorantGaramondFontFamily,
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.Normal,
        fontSize = 15.5.sp,
        lineHeight = 23.sp,
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
