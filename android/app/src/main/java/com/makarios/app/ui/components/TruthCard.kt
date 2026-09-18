package com.makarios.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.makarios.app.data.Affirmation
import com.makarios.app.ui.theme.*

@Composable
fun TruthCard(
    affirmation: Affirmation,
    isSaved: Boolean,
    onToggleSave: () -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 6.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = BrandPlum
            )
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceWhite)
            .border(1.dp, BorderCard, RoundedCornerShape(24.dp))
            .clickable(onClick = onCardClick)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Photo Header with Floating Bookmark Pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(Color(0xFFEAE4DC))
            ) {
                AsyncImage(
                    model = affirmation.imageUrl,
                    contentDescription = "Architectural reflection image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Floating Bookmark Button
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(38.dp)
                        .shadow(3.dp, CircleShape)
                        .clip(CircleShape)
                        .background(SurfaceWhite)
                        .clickable(onClick = onToggleSave),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Save affirmation",
                        tint = if (isSaved) BrandPlum else TextPrimary,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            // Typography Content Body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Declaration with Typographic Curly Quotes
                Text(
                    text = "“${affirmation.declaration}”",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 23.sp,
                    lineHeight = 33.sp,
                    textAlign = TextAlign.Center,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Supporting Scripture in Italic Serif
                Text(
                    text = "\"${affirmation.scriptureText}\"",
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Scripture Reference in Tracked All-Caps
                Text(
                    text = affirmation.reference.uppercase(),
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    letterSpacing = 2.2.sp,
                    textAlign = TextAlign.Center,
                    color = TextMuted
                )
            }
        }
    }
}
