package com.makarios.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        // Tone Triad & Cycle Action Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Editorial Tone Triad
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(ParchmentWarm)
                    .border(0.75.dp, HairlineRule, RoundedCornerShape(4.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AffirmationTone.values().forEach { tone ->
                    val isSelected = tone == selectedTone
                    val label = when (tone) {
                        AffirmationTone.STILL -> "STILL"
                        AffirmationTone.RESOLUTE -> "RESOLUTE"
                        AffirmationTone.GENTLE -> "GENTLE"
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isSelected) InkLampblack else Color.Transparent)
                            .clickable { onToneSelected(tone) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) GoldLeaf else InkIronGall,
                            fontFamily = BodyFontFamily,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            fontSize = 10.sp,
                            letterSpacing = 1.4.sp
                        )
                    }
                }
            }

            // "Cycle Folio" Action
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(onClick = onAnotherClicked)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Cycle folio declaration",
                    tint = InkLampblack,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "CYCLE FOLIO",
                    color = InkLampblack,
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    letterSpacing = 1.4.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Luxury Editorial CTA: Illuminate Wallpaper
        Button(
            onClick = onCreateWallpaperClicked,
            colors = ButtonDefaults.buttonColors(
                containerColor = InkLampblack,
                contentColor = GoldLeaf
            ),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .border(0.75.dp, BorderBroadsheet, RoundedCornerShape(4.dp))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = GoldLeaf,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "ILLUMINATE WALLPAPER",
                    color = GoldLeaf,
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}
