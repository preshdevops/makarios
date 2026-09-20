package com.makarios.app.ui.components

import androidx.compose.foundation.background
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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Espresso.copy(alpha = 0.08f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Surface)
            .clickable(onClick = onCardClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Photo top plate
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(185.dp)
                .background(PorcelainWarm)
        ) {
            AsyncImage(
                model = affirmation.imageUrl,
                contentDescription = "Contemplative imagery",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Circular white bookmark button in top-right overlay matching mockup
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Surface.copy(alpha = 0.90f))
                    .clickable(onClick = onToggleSave),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Save declaration",
                    tint = if (isSaved) Terracotta else Espresso,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Center Declaration in high-contrast Fraunces Serif
        Text(
            text = "“${affirmation.declaration}”",
            fontFamily = DisplayFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 22.sp,
            lineHeight = 31.sp,
            letterSpacing = (-0.2).sp,
            textAlign = TextAlign.Center,
            color = Espresso,
            modifier = Modifier.padding(horizontal = 22.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Scripture quote passage
        Text(
            text = "“${affirmation.scriptureText}”",
            fontFamily = DisplayFontFamily,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Normal,
            fontSize = 14.5.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
            color = Stone,
            modifier = Modifier.padding(horizontal = 26.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Scripture reference in tracked small caps
        Text(
            text = affirmation.reference.uppercase(),
            fontFamily = BodyFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.5.sp,
            letterSpacing = 1.6.sp,
            textAlign = TextAlign.Center,
            color = StoneMuted
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
