package com.makarios.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.data.AffirmationTone
import com.makarios.app.ui.theme.*

@Composable
fun ToneSelector(
    selectedTone: AffirmationTone,
    onToneSelected: (AffirmationTone) -> Unit,
    onAnotherClicked: () -> Unit,
    onCreateWallpaperClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Tone Pills & Another Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tones on Left
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AffirmationTone.values().forEach { tone ->
                    val isSelected = tone == selectedTone
                    val label = when (tone) {
                        AffirmationTone.STILL -> "Still"
                        AffirmationTone.RESOLUTE -> "Resolute"
                        AffirmationTone.GENTLE -> "Gentle"
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (isSelected) ToneResoluteSand else Color.Transparent)
                            .clickable { onToneSelected(tone) }
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) TextPrimary else TextSecondary,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // "Another" Button on Right
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .clickable(onClick = onAnotherClicked)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Cycle affirmation",
                    tint = TextPrimary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Another",
                    color = TextPrimary,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }

        // Primary Action Button: Create Wallpaper
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 8.dp)
        ) {
            Button(
                onClick = onCreateWallpaperClicked,
                colors = ButtonDefaults.buttonColors(containerColor = BrandPlum),
                shape = RoundedCornerShape(999.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = AmberGold,
                        modifier = Modifier.size(17.dp)
                    )
                    Text(
                        text = "Create Wallpaper",
                        color = Color.White,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.5.sp,
                        letterSpacing = 0.3.sp
                    )
                }
            }
        }
    }
}
