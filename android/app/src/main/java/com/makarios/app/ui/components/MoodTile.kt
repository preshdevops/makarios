package com.makarios.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.makarios.app.data.MoodCategory
import com.makarios.app.ui.theme.*

@Composable
fun MoodTile(
    mood: MoodCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(135.dp)
            .height(145.dp)
            .shadow(
                elevation = if (isSelected) 6.dp else 2.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = Espresso.copy(alpha = if (isSelected) 0.18f else 0.08f)
            )
            .clip(RoundedCornerShape(22.dp))
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, AmberGold, RoundedCornerShape(22.dp))
                } else {
                    Modifier.border(0.5.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
                }
            )
            .clickable(onClick = onClick)
    ) {
        // Background Photo
        AsyncImage(
            model = mood.imageUrl,
            contentDescription = mood.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Luminous Breathable Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.15f),
                            Color(0xB81A1512)
                        )
                    )
                )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            // Selected Pill Indicator
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.25f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "ACTIVE",
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 8.5.sp,
                        letterSpacing = 1.2.sp,
                        color = Color.White
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Typography
            Column {
                Text(
                    text = mood.name,
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 17.sp,
                    lineHeight = 22.sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = mood.subtitle,
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.5.sp,
                    lineHeight = 14.sp,
                    color = Color.White.copy(alpha = 0.78f),
                    maxLines = 2
                )
            }
        }
    }
}
