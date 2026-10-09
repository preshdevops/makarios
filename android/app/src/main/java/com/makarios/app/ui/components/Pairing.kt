package com.makarios.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.makarios.app.ui.theme.Light
import com.makarios.app.ui.theme.MakariosTypography

@Composable
fun Pairing(
    declaration: String,
    verseText: String,
    verseReference: String,
    light: Light,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false // e.g. for Kept card vs Today
) {
    Column(modifier = modifier) {
        Text(
            text = declaration,
            color = light.text,
            style = if (isCompact) MakariosTypography.headlineLarge else MakariosTypography.displayMedium
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Anchor Rule
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(2.dp)
                .background(light.anchorRule)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = verseText,
            color = light.text,
            style = MakariosTypography.bodyLarge // This is Newsreader Italic 17sp
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = verseReference,
            color = light.text.copy(alpha = light.secondaryAlpha),
            style = MakariosTypography.labelMedium
        )
    }
}
