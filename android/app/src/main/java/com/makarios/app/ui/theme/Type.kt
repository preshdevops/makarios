package com.makarios.app.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.makarios.app.R

val NewsreaderFontFamily = FontFamily(
    Font(R.font.newsreader, FontWeight.Medium, FontStyle.Normal),
    Font(R.font.newsreader_italic, FontWeight.Normal, FontStyle.Italic)
)
val HankenGroteskFontFamily = FontFamily(
    Font(R.font.hanken_grotesk, FontWeight.Normal),
    Font(R.font.hanken_grotesk, FontWeight.Medium),
    Font(R.font.hanken_grotesk, FontWeight.SemiBold)
)
val DisplayFontFamily = NewsreaderFontFamily
val BodyFontFamily = HankenGroteskFontFamily

/** Italic Newsreader is ONLY for verses (Prompt 21 Part 5). */
val VerseTextStyle = TextStyle(fontFamily = NewsreaderFontFamily, fontWeight = FontWeight.Normal, fontStyle = FontStyle.Italic, fontSize = 17.sp, lineHeight = 26.sp, fontFeatureSettings = "lnum, tnum")

private fun serif(size: Int, lh: Int) = TextStyle(fontFamily = NewsreaderFontFamily, fontWeight = FontWeight.Medium, fontSize = size.sp, lineHeight = lh.sp, fontFeatureSettings = "lnum, tnum")
private fun sans(size: Int, lh: Int, w: FontWeight = FontWeight.Normal) = TextStyle(fontFamily = HankenGroteskFontFamily, fontWeight = w, fontSize = size.sp, lineHeight = lh.sp, fontFeatureSettings = "lnum, tnum")

val MakariosTypography = androidx.compose.material3.Typography(
    displayLarge = serif(34, 42), displayMedium = serif(34, 42), displaySmall = serif(28, 32),
    headlineLarge = serif(22, 28), headlineMedium = serif(20, 26), headlineSmall = serif(18, 24),
    titleLarge = serif(22, 28), titleMedium = serif(17, 22), titleSmall = serif(15, 20),
    bodyLarge = sans(17, 26), bodyMedium = sans(16, 24), bodySmall = sans(14, 20),
    labelLarge = sans(14, 20), labelMedium = sans(13, 18, FontWeight.Medium), labelSmall = sans(12, 16)
)
