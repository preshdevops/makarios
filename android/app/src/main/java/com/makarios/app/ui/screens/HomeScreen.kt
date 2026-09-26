package com.makarios.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.makarios.app.data.Affirmation
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.ui.components.AffirmationCard
import com.makarios.app.ui.components.CreatorPromptCard
import com.makarios.app.ui.components.MoodTile
import com.makarios.app.ui.components.WidgetPreviewCard
import com.makarios.app.ui.theme.*
import com.makarios.app.util.ShareHelper
import com.makarios.app.util.WidgetHelper

@Composable
fun HomeScreen(
    onNavigateToDetail: (Affirmation) -> Unit,
    onNavigateToCreate: (String) -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToWidgets: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("All") }
    val aotd = AffirmationRepository.affirmationOfTheDay
    var isAotdSaved by remember { mutableStateOf(AffirmationRepository.isSaved(aotd.id)) }

    // Time-aware greeting
    val currentHour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
    val greeting = remember(currentHour) {
        when (currentHour) {
            in 4..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    // Filter curated affirmations
    val curatedAffirmations = remember(selectedCategory) {
        val all = listOf(
            AffirmationRepository.strengthAffirmation,
            AffirmationRepository.provisionAffirmation,
            AffirmationRepository.courageAffirmation,
            AffirmationRepository.favorite1,
            AffirmationRepository.favorite2,
            AffirmationRepository.purposeAffirmation,
            AffirmationRepository.confidenceAffirmation
        )
        if (selectedCategory == "All") all else all.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Porcelain,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToCreate("") },
                containerColor = Espresso,
                contentColor = Surface,
                shape = CircleShape,
                modifier = Modifier
                    .size(52.dp)
                    .shadow(4.dp, CircleShape, spotColor = Espresso.copy(alpha = 0.20f))
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Declaration",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 76.dp)
        ) {
            // ── 1. Top Header Bar ─────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Makarios",
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 24.sp,
                        letterSpacing = (-0.3).sp,
                        color = Espresso
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = greeting,
                        fontFamily = DisplayFontFamily,
                        fontStyle = FontStyle.Italic,
                        fontSize = 13.5.sp,
                        color = Stone
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Notification Bell
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Surface)
                            .border(0.5.dp, BorderSubtle, CircleShape)
                            .clickable {
                                Toast.makeText(context, "Daily declarations active", Toast.LENGTH_SHORT).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Stone,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Profile Avatar
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(PorcelainWarm)
                            .border(0.5.dp, BorderSubtle, CircleShape)
                    ) {
                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=160&q=80",
                            contentDescription = "Profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ── 2. Today's Truth (Sanctuary Hero Moment) ──────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .shadow(6.dp, RoundedCornerShape(26.dp), spotColor = Espresso.copy(alpha = 0.18f))
                    .clip(RoundedCornerShape(26.dp))
                    .clickable { onNavigateToDetail(aotd) }
            ) {
                // Background Photo
                AsyncImage(
                    model = aotd.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )

                // Luminous Breathable Scrim
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.08f),
                                    Color.Black.copy(alpha = 0.30f),
                                    Color(0xD91E1916)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Header row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today’s Truth",
                            fontFamily = DisplayFontFamily,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.20f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = aotd.category.uppercase(),
                                fontFamily = BodyFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 9.sp,
                                letterSpacing = 1.6.sp,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Main Quote in Cormorant Garamond Serif
                    Text(
                        text = "“${aotd.declaration}”",
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 24.sp,
                        lineHeight = 33.sp,
                        letterSpacing = (-0.2).sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Grounding Scripture Quote
                    Text(
                        text = "“${aotd.scriptureText}”",
                        fontFamily = DisplayFontFamily,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Normal,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = Color.White.copy(alpha = 0.88f)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Reference & Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = aotd.reference.uppercase(),
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.5.sp,
                            letterSpacing = 1.6.sp,
                            color = Color.White.copy(alpha = 0.75f)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Native Share Button
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.20f))
                                    .clickable {
                                        ShareHelper.shareAffirmation(context, aotd)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Favorite Button
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isAotdSaved) Color.White.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.20f))
                                    .clickable {
                                        AffirmationRepository.toggleSave(aotd.id)
                                        isAotdSaved = !isAotdSaved
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isAotdSaved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Enter Sanctuary detail arrow
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .clickable { onNavigateToDetail(aotd) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "View detail",
                                    tint = Espresso,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── 3. Creator Prompt Card ────────────────────────────
            CreatorPromptCard(onClick = { onNavigateToCreate("") })

            Spacer(modifier = Modifier.height(28.dp))

            // ── 4. Visual Mood Exploration (Photographic Tiles) ───
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Spiritual Seasons",
                            fontFamily = DisplayFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 19.sp,
                            color = Espresso
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Anchor your soul in divine truth",
                            fontFamily = BodyFontFamily,
                            fontSize = 12.sp,
                            color = Stone
                        )
                    }

                    Text(
                        text = if (selectedCategory == "All") "See Library" else "Show All",
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Terracotta,
                        modifier = Modifier.clickable {
                            if (selectedCategory != "All") {
                                selectedCategory = "All"
                            } else {
                                onNavigateToLibrary()
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Horizontal photographic mood carousel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AffirmationRepository.moodCategories.forEach { mood ->
                        MoodTile(
                            mood = mood,
                            isSelected = selectedCategory.equals(mood.name, ignoreCase = true),
                            onClick = {
                                selectedCategory = if (selectedCategory.equals(mood.name, ignoreCase = true)) "All" else mood.name
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // ── 5. Curated Photo Sanctuary Feed ───────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedCategory == "All") "Curated Declarations" else "$selectedCategory Declarations",
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 19.sp,
                        color = Espresso
                    )

                    Text(
                        text = "${curatedAffirmations.size} available",
                        fontFamily = BodyFontFamily,
                        fontSize = 12.sp,
                        color = StoneMuted
                    )
                }

                curatedAffirmations.forEach { affirmation ->
                    var isSaved by remember(affirmation.id) {
                        mutableStateOf(AffirmationRepository.isSaved(affirmation.id))
                    }

                    AffirmationCard(
                        affirmation = affirmation,
                        isSaved = isSaved,
                        onToggleSave = {
                            AffirmationRepository.toggleSave(affirmation.id)
                            isSaved = !isSaved
                        },
                        onShare = {
                            ShareHelper.shareAffirmation(context, affirmation)
                        },
                        onCardClick = { onNavigateToDetail(affirmation) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // ── 6. Live on Your Phone (Glance Widget Pinning) ─────
            WidgetPreviewCard(
                affirmation = AffirmationRepository.widgetAffirmation,
                onAddToHomeScreen = {
                    WidgetHelper.pinWidgetToHomeScreen(context)
                },
                onSeeAllWidgets = onNavigateToWidgets
            )
        }
    }
}
