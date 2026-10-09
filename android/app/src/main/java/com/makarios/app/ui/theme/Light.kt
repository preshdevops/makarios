package com.makarios.app.ui.theme

import androidx.compose.ui.graphics.Color

enum class Light(
    val top: Color,
    val bottom: Color,
    val text: Color,
    val secondaryAlpha: Float
) {
    Dawn(Color(0xFFFBE3C4), Color(0xFFEBB195), Ink, 0.78f),
    Midday(Color(0xFFFFF4D6), Color(0xFFEFD58A), Ink, 0.78f),
    Mist(Color(0xFFE3EADB), Color(0xFFBCCBB2), Ink, 0.78f),
    Rain(Color(0xFFD5E0E2), Color(0xFFA3B9BE), Ink, 0.78f),
    Ember(Color(0xFF9C4A26), Color(0xFF6B2E1E), Cream, 0.88f),
    Dusk(Color(0xFF8F4B3A), Color(0xFF43292B), Cream, 0.88f),
    Grove(Color(0xFF3F5A37), Color(0xFF223019), Cream, 0.88f),
    Night(Color(0xFF1F2B2A), Color(0xFF0F1716), Cream, 0.88f);

    val isLight: Boolean
        get() = this == Dawn || this == Midday || this == Mist || this == Rain

    val anchorRule: Color
        get() = if (isLight) AnchorLight else AnchorDark
        
    val buttonFill: Color
        get() = if (isLight) Ink else Cream
        
    val buttonText: Color
        get() = if (isLight) Cream else Ink
}
