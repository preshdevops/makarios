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
import androidx.compose.ui.draw.shadow
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
        // Tone selector segmented capsule + "Another" action row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Segmented pill container matching mockup
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(PorcelainWarm.copy(alpha = 0.65f))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AffirmationTone.values().forEach { tone ->
                    val isSelected = tone == selectedTone
                    val label = when (tone) {
                        AffirmationTone.STILL -> "Still"
                        AffirmationTone.RESOLUTE -> "Resolute"
                        AffirmationTone.GENTLE -> "Gentle"
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .shadow(elevation = 1.dp, shape = RoundedCornerShape(20.dp))
                                        .background(Surface)
                                } else {
                                    Modifier.background(Color.Transparent)
                                }
                            )
                            .clickable { onToneSelected(tone) }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Espresso else Stone,
                            fontFamily = BodyFontFamily,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 13.5.sp
                        )
                    }
                }
            }

            // "Another" action on the right
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onAnotherClicked)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Another declaration",
                    tint = Stone,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Another",
                    color = Stone,
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Big Primary CTA: "Create Wallpaper" in deep espresso-plum matching mockup
        Button(
            onClick = onCreateWallpaperClicked,
            colors = ButtonDefaults.buttonColors(
                containerColor = Espresso,
                contentColor = Surface
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = Espresso.copy(alpha = 0.20f)
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFFE5B869), // Warm amber gold spark from mockup
                    modifier = Modifier.size(17.dp)
                )
                Text(
                    text = "Create Wallpaper",
                    color = Surface,
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
