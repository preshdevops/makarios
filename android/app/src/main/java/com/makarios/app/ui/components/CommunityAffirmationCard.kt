package com.makarios.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import com.makarios.app.data.PhotoLibrary
import com.makarios.app.data.PhotoRole
import com.makarios.app.data.PublicAffirmation
import com.makarios.app.ui.theme.*

@Composable
fun CommunityAffirmationCard(
    affirmation: PublicAffirmation,
    isSaved: Boolean,
    isAmened: Boolean,
    onToggleAmen: () -> Unit,
    onToggleSave: () -> Unit,
    onShare: () -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 220.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(26.dp),
                spotColor = Espresso.copy(alpha = 0.16f)
            )
            .clip(RoundedCornerShape(26.dp))
            .border(0.5.dp, Color.White.copy(alpha = 0.20f), RoundedCornerShape(26.dp))
            .clickable(onClick = onCardClick)
    ) {
        // ── 1. Photography ──
        AsyncImage(
            model = affirmation.imageUrl ?: PhotoLibrary.getForRole(PhotoRole.COMMUNITY_FALLBACK).url(width = 1200),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            colorFilter = WarmPhotoGrade,
            modifier = Modifier.matchParentSize()
        )

        // ── 2. Atmospheric Scrim ──
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(PhotoTextScrim)
        )

        // ── 3. Card Content ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            // Header Row: Author details and Save/Share actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Author & Category
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = affirmation.authorName,
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                        if (affirmation.authorUsername.isNotBlank()) {
                            Text(
                                text = "@${affirmation.authorUsername}",
                                fontFamily = BodyFontFamily,
                                fontSize = 11.5.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = affirmation.category.uppercase(),
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 9.sp,
                        letterSpacing = 1.4.sp,
                        color = SunlitGold
                    )
                }

                // Actions: Save & Share
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(ScrimPillColor)
                            .clickable(onClick = onShare),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isSaved) Olive else ScrimPillColor)
                            .clickable(onClick = onToggleSave),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Save declaration",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // The Declaration
            Text(
                text = "“${affirmation.declaration}”",
                color = Color.White,
                fontFamily = DisplayFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp,
                lineHeight = 27.sp,
                letterSpacing = (-0.3).sp,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Grounding Scripture & Reference
            if (affirmation.scriptureText.isNotBlank()) {
                Text(
                    text = "“${affirmation.scriptureText}”",
                    color = Color.White.copy(alpha = 0.85f),
                    fontFamily = DisplayFontFamily,
                    fontStyle = FontStyle.Italic,
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = "— ${affirmation.reference.uppercase()}",
                color = SunlitGold,
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom: Amen button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isAmened) Olive else ScrimPillColor)
                        .clickable(onClick = onToggleAmen)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isAmened) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Amen",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (affirmation.amenCount > 0) "Amen · ${affirmation.amenCount}" else "Amen",
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
