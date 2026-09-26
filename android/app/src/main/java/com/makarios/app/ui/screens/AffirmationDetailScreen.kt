package com.makarios.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.makarios.app.data.Affirmation
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.ui.theme.*
import com.makarios.app.util.ShareHelper
import com.makarios.app.util.WidgetHelper

@Composable
fun AffirmationDetailScreen(
    affirmation: Affirmation,
    onClose: () -> Unit,
    onNavigateToCreate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isSaved by remember { mutableStateOf(AffirmationRepository.isSaved(affirmation.id)) }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // ── 1. Full-Bleed Sacred Photography ────────────────────────
        AsyncImage(
            model = affirmation.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // ── 2. Cinematic Atmospheric Scrim ──────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.45f),
                            Color.Black.copy(alpha = 0.60f),
                            Color(0xFA141110)
                        )
                    )
                )
        )

        // ── 3. Content Scroll ───────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ── Top Controls ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Close button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f))
                        .clickable(onClick = onClose),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Share & Heart actions
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.18f))
                            .clickable {
                                ShareHelper.shareAffirmation(context, affirmation)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isSaved) Color.White.copy(alpha = 0.32f) else Color.White.copy(alpha = 0.18f))
                            .clickable {
                                AffirmationRepository.toggleSave(affirmation.id)
                                isSaved = !isSaved
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Center Content: The Word & Declaration ──
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Category Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.20f))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = affirmation.category.uppercase(),
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.5.sp,
                        letterSpacing = 2.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                // The Main Declaration in Cormorant Garamond Display
                Text(
                    text = "“${affirmation.declaration}”",
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 26.sp,
                    lineHeight = 36.sp,
                    letterSpacing = (-0.3).sp,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(26.dp))

                // Grounding Scripture Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .border(0.5.dp, Color.White.copy(alpha = 0.20f), RoundedCornerShape(22.dp))
                        .padding(22.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Living Scripture",
                            fontFamily = DisplayFontFamily,
                            fontStyle = FontStyle.Italic,
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.70f)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Full scripture verse text in italic Cormorant Garamond
                        Text(
                            text = "“${affirmation.scriptureText}”",
                            fontFamily = DisplayFontFamily,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Normal,
                            fontSize = 15.sp,
                            lineHeight = 23.sp,
                            textAlign = TextAlign.Center,
                            color = Color.White.copy(alpha = 0.95f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = affirmation.reference.uppercase(),
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.5.sp,
                            letterSpacing = 1.6.sp,
                            color = Color.White.copy(alpha = 0.82f)
                        )
                    }
                }

                // Personal Confession card if present
                if (affirmation.personalDeclaration != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(0.5.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(18.dp))
                            .padding(18.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Personal Confession",
                                fontFamily = DisplayFontFamily,
                                fontStyle = FontStyle.Italic,
                                fontSize = 11.5.sp,
                                color = Color.White.copy(alpha = 0.65f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "“${affirmation.personalDeclaration}”",
                                fontFamily = DisplayFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 13.5.sp,
                                lineHeight = 20.sp,
                                textAlign = TextAlign.Center,
                                color = Color.White.copy(alpha = 0.90f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Bottom Actions ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Primary: Design Studio & Wallpapers
                Button(
                    onClick = { onNavigateToCreate(affirmation.id) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Espresso
                    ),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = Espresso,
                            modifier = Modifier.size(17.dp)
                        )
                        Text(
                            text = "Design Studio & Wallpapers",
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = Espresso
                        )
                    }
                }

                // Secondary row: Add to Widget + Set Reminder
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.14f))
                            .border(0.5.dp, Color.White.copy(alpha = 0.20f), RoundedCornerShape(16.dp))
                            .clickable {
                                WidgetHelper.setWidgetAffirmation(context, affirmation)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Widgets,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Add to Widget",
                                fontFamily = BodyFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.14f))
                            .border(0.5.dp, Color.White.copy(alpha = 0.20f), RoundedCornerShape(16.dp))
                            .clickable {
                                Toast.makeText(context, "Daily reminder scheduled", Toast.LENGTH_SHORT).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Set Reminder",
                                fontFamily = BodyFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
