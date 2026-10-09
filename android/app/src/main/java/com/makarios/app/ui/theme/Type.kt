package com.makarios.app.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.makarios.app.R

// Using the variable fonts we downloaded. We instantiate the specific weights we need.
val NewsreaderFontFamily = FontFamily(
    Font(R.font.newsreader, FontWeight.Medium, FontStyle.Normal),
    Font(R.font.newsreader_italic, FontWeight.Normal, FontStyle.Italic)
)

val HankenGroteskFontFamily = FontFamily(
    Font(R.font.hanken_grotesk, FontWeight.Normal),
    Font(R.font.hanken_grotesk, FontWeight.Medium),
    Font(R.font.hanken_grotesk, FontWeight.SemiBold)
)

val MakariosTypography = androidx.compose.material3.Typography(
    // Screen titles (Newsreader 34)
    displayLarge = TextStyle(
        fontFamily = NewsreaderFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 34.sp,
        lineHeight = 42.sp,
        fontFeatureSettings = "lnum, tnum"
    ),
    // Today declaration (Newsreader 34/42 sp)
    displayMedium = TextStyle(
        fontFamily = NewsreaderFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 34.sp,
        lineHeight = 42.sp,
        fontFeatureSettings = "lnum, tnum"
    ),
    // Explore band title (Newsreader 28/32)
    displaySmall = TextStyle(
        fontFamily = NewsreaderFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        fontFeatureSettings = "lnum, tnum"
    ),
    // Kept card (Newsreader 22/28)
    headlineLarge = TextStyle(
        fontFamily = NewsreaderFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontFeatureSettings = "lnum, tnum"
    ),
    // Verse italic (Newsreader Italic 17/26)
    bodyLarge = TextStyle(
        fontFamily = NewsreaderFontFamily,
        fontWeight = FontWeight.Normal,
        fontStyle = FontStyle.Italic,
        fontSize = 17.sp,
        lineHeight = 26.sp,
        fontFeatureSettings = "lnum, tnum"
    ),
    // Body (Hanken 16/24)
    bodyMedium = TextStyle(
        fontFamily = HankenGroteskFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontFeatureSettings = "lnum, tnum"
    ),
    // Label (Hanken 14/20)
    labelLarge = TextStyle(
        fontFamily = HankenGroteskFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontFeatureSettings = "lnum, tnum"
    ),
    // Reference text (13sp, weight 500)
    labelMedium = TextStyle(
        fontFamily = HankenGroteskFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontFeatureSettings = "lnum, tnum"
    ),
    // Caption (12/16 minimum)
    labelSmall = TextStyle(
        fontFamily = HankenGroteskFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontFeatureSettings = "lnum, tnum"
    )
)
