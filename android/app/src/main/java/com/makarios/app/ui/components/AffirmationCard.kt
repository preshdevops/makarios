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
                elevation = 2.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = Espresso.copy(alpha = 0.05f)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(18.dp))
            .clickable(onClick = onCardClick)
            .padding(20.dp)
    ) {
        Column {
            // Category header tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = affirmation.category.uppercase(),
                    color = Terracotta,
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.5.sp,
                    letterSpacing = 1.5.sp
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Porcelain)
                            .clickable(onClick = onShare),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Stone,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Porcelain)
                            .clickable(onClick = onToggleSave),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Save declaration",
                            tint = if (isSaved) Terracotta else Stone,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Declaration in Fraunces
            Text(
                text = "“${affirmation.declaration}”",
                fontFamily = DisplayFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 17.5.sp,
                lineHeight = 25.sp,
                letterSpacing = (-0.2).sp,
                color = Espresso
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Scripture quote & reference (Rule: Every affirmation always displays its grounding Bible verse reference)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Porcelain)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "“${affirmation.scriptureText}”",
                    fontFamily = DisplayFontFamily,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = Stone
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = affirmation.reference.uppercase(),
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    letterSpacing = 1.2.sp,
                    color = Terracotta
                )
            }
        }
    }
}
