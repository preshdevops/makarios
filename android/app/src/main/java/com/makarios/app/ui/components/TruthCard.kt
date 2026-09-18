package com.makarios.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(8.dp),
                spotColor = InkLampblack.copy(alpha = 0.08f)
            )
            .clip(RoundedCornerShape(8.dp))
            .background(PaperSurface)
            .border(1.dp, BorderBroadsheet, RoundedCornerShape(8.dp))
            .clickable(onClick = onCardClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Folio Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rubric Category & Folio Number
                Text(
                    text = "FOLIO № 07 · ${affirmation.category.uppercase()}",
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    letterSpacing = 2.2.sp,
                    color = RubricVermilion
                )

                // Refined Bookmark Action
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clickable(onClick = onToggleSave),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Save declaration",
                        tint = if (isSaved) GoldLeaf else InkMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // The Central Declaration in Majestic Newsreader Serif
            Text(
                text = "“${affirmation.declaration}”",
                fontFamily = DisplayFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 25.sp,
                lineHeight = 35.sp,
                letterSpacing = (-0.3).sp,
                textAlign = TextAlign.Center,
                color = InkLampblack,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sacred Asterism Divider
            Text(
                text = "— ✤ —",
                color = GoldLeaf,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Supporting Scripture Passage in Poetic Italic
            Text(
                text = "\"${affirmation.scriptureText}\"",
                fontFamily = DisplayFontFamily,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                lineHeight = 23.sp,
                textAlign = TextAlign.Center,
                color = InkIronGall,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Biblical Reference in Tracked Small-Caps
            Text(
                text = affirmation.reference.uppercase(),
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.5.sp,
                letterSpacing = 2.5.sp,
                textAlign = TextAlign.Center,
                color = RubricVermilion
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Matted Fine-Art Plate Window
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(PaperMuted)
                    .border(0.75.dp, HairlineRule, RoundedCornerShape(4.dp))
            ) {
                AsyncImage(
                    model = affirmation.imageUrl,
                    contentDescription = "Contemplative architectural plate",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
