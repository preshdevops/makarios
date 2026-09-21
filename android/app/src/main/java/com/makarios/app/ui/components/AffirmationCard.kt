package com.makarios.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.makarios.app.data.Affirmation
import com.makarios.app.ui.theme.BodyFontFamily
import com.makarios.app.ui.theme.DisplayFontFamily
import com.makarios.app.ui.theme.Espresso

@Composable
fun AffirmationCard(
    affirmation: Affirmation,
    isSaved: Boolean,
    onToggleSave: () -> Unit,
    onShare: () -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(26.dp),
                spotColor = Espresso.copy(alpha = 0.16f)
            )
            .clip(RoundedCornerShape(26.dp))
            .border(0.5.dp, Color.White.copy(alpha = 0.20f), RoundedCornerShape(26.dp))
            .clickable(onClick = onCardClick)
    ) {
        // ── 1. Sacred Cinematic Photography ─────────────────────────
        AsyncImage(
            model = affirmation.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        // ── 2. Deep Atmospheric Scrim ───────────────────────────────
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.35f),
                            Color.Black.copy(alpha = 0.55f),
                            Color(0xFA161311)
                        )
                    )
                )
        )

        // ── 3. Typographic Content ──────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Header: Category label & action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = affirmation.category.uppercase(),
                    color = Color.White.copy(alpha = 0.85f),
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    letterSpacing = 2.sp
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Glass Share Button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.18f))
                            .clickable(onClick = onShare),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Glass Bookmark Button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isSaved) Color.White.copy(alpha = 0.30f) else Color.White.copy(alpha = 0.18f))
                            .clickable(onClick = onToggleSave),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Save declaration",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Main Declaration in Fraunces Serif
            Text(
                text = "“${affirmation.declaration}”",
                fontFamily = DisplayFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 21.sp,
                lineHeight = 30.sp,
                letterSpacing = (-0.2).sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Hairline separator for grounded scripture
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(Color.White.copy(alpha = 0.22f))
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Grounding Scripture (Mandatory on every card)
            Text(
                text = "“${affirmation.scriptureText}”",
                fontFamily = DisplayFontFamily,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Normal,
                fontSize = 13.5.sp,
                lineHeight = 20.sp,
                color = Color.White.copy(alpha = 0.88f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = affirmation.reference.uppercase(),
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                letterSpacing = 1.4.sp,
                color = Color.White.copy(alpha = 0.72f)
            )
        }
    }
}
