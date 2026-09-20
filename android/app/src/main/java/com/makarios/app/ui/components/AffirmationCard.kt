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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.data.Affirmation
import com.makarios.app.ui.theme.*

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
                elevation = 1.5.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Espresso.copy(alpha = 0.04f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Surface)
            .border(0.5.dp, BorderSubtle, RoundedCornerShape(20.dp))
            .clickable(onClick = onCardClick)
            .padding(22.dp)
    ) {
        Column {
            // Header: Category label & action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = affirmation.category.uppercase(),
                    color = StoneMuted,
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    letterSpacing = 1.8.sp
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(PorcelainWarm.copy(alpha = 0.55f))
                            .clickable(onClick = onShare),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Stone,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isSaved) TerracottaLight else PorcelainWarm.copy(alpha = 0.55f))
                            .clickable(onClick = onToggleSave),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Save declaration",
                            tint = if (isSaved) Terracotta else Stone,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Declaration in Fraunces Serif
            Text(
                text = "“${affirmation.declaration}”",
                fontFamily = DisplayFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                lineHeight = 26.sp,
                letterSpacing = (-0.2).sp,
                color = Espresso
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Delicate hairline separator for grounded scripture
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(BorderSubtle)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Grounding Scripture (mandatory on every card)
            Text(
                text = "“${affirmation.scriptureText}”",
                fontFamily = DisplayFontFamily,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = Stone
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = affirmation.reference.uppercase(),
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 9.5.sp,
                letterSpacing = 1.4.sp,
                color = Terracotta
            )
        }
    }
}
