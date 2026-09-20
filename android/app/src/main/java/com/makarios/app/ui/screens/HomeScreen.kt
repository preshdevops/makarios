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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.makarios.app.data.Affirmation
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.ui.theme.*

@Composable
fun HomeScreen(
    onNavigateToDetail: (Affirmation) -> Unit,
    onNavigateToCreate: (String) -> Unit,
    onNavigateToLibrary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFocusArea by remember { mutableStateOf("Strength") }
    val aotd = AffirmationRepository.affirmationOfTheDay
    var isAotdSaved by remember { mutableStateOf(AffirmationRepository.isSaved(aotd.id)) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Porcelain,
        floatingActionButton = {
            // Floating Action Button (+) matching Page 5 of PDF
            FloatingActionButton(
                onClick = { onNavigateToCreate(aotd.id) },
                containerColor = Terracotta,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(54.dp)
                    .shadow(6.dp, CircleShape, spotColor = Terracotta.copy(alpha = 0.35f))
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
                .padding(bottom = 72.dp)
        ) {
            // ── Top Header: Makarios + Daily Spiritual Nourishment + Notification + Avatar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Makarios",
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp,
                        letterSpacing = (-0.2).sp,
                        color = Espresso
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Daily Spiritual Nourishment",
                        fontFamily = BodyFontFamily,
                        fontSize = 12.sp,
                        color = StoneMuted
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Notification Bell with badge dot
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Surface)
                            .border(1.dp, Border, CircleShape)
                            .clickable {
                                Toast.makeText(context, "No unread notifications", Toast.LENGTH_SHORT).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Espresso,
                            modifier = Modifier.size(19.dp)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Terracotta)
                        )
                    }

                    // Profile Avatar
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(PorcelainWarm)
                            .border(1.dp, Border, CircleShape)
                    ) {
                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=160&q=80",
                            contentDescription = "Profile",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // ── Search Bar: "Search affirmations..." ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .shadow(2.dp, RoundedCornerShape(24.dp), spotColor = Espresso.copy(alpha = 0.05f))
                    .clip(RoundedCornerShape(24.dp))
                    .background(Surface)
                    .border(1.dp, Border, RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = StoneMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Search affirmations...",
                        fontFamily = BodyFontFamily,
                        fontSize = 14.sp,
                        color = StoneMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Hero Card: AFFIRMATION OF THE DAY (Page 5 in PDF) ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = Espresso.copy(alpha = 0.18f))
                    .clip(RoundedCornerShape(20.dp))
                    .background(AtmosphericGradient) // Warm espresso-plum gradient
                    .clickable { onNavigateToDetail(aotd) }
                    .padding(22.dp)
            ) {
                Column {
                    // Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.16f))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "AFFIRMATION OF THE DAY",
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 9.5.sp,
                            letterSpacing = 1.6.sp,
                            color = TerracottaLight
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Main Quote in Fraunces Serif
                    Text(
                        text = "“${aotd.declaration}”",
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 21.sp,
                        lineHeight = 30.sp,
                        letterSpacing = (-0.2).sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Reference & Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = aotd.reference,
                            fontFamily = BodyFontFamily,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.75f)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Favorite Button
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .clickable {
                                        AffirmationRepository.toggleSave(aotd.id)
                                        isAotdSaved = !isAotdSaved
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isAotdSaved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (isAotdSaved) Terracotta else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Forward Arrow Button -> Fullscreen Detail
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Terracotta)
                                    .clickable { onNavigateToDetail(aotd) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "View detail",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Focus Areas: Horizontal circular icons + See All ──
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Focus Areas",
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Espresso
                    )
                    Text(
                        text = "See All",
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.5.sp,
                        color = Terracotta,
                        modifier = Modifier.clickable(onClick = onNavigateToLibrary)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val focusList = listOf(
                        "Peace" to "🕊️",
                        "Strength" to "🛡️",
                        "Identity" to "👑",
                        "Purpose" to "🧭",
                        "Joy" to "☀️"
                    )

                    focusList.forEach { (name, emoji) ->
                        val isSelected = name == selectedFocusArea
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { selectedFocusArea = name }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .then(
                                        if (isSelected) {
                                            Modifier
                                                .background(Terracotta)
                                                .shadow(4.dp, CircleShape, spotColor = Terracotta.copy(alpha = 0.3f))
                                        } else {
                                            Modifier
                                                .background(Surface)
                                                .border(1.dp, Border, CircleShape)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = name,
                                fontFamily = BodyFontFamily,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 12.sp,
                                color = if (isSelected) Espresso else StoneMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Discover More Feed (Page 5 in PDF) ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Discover More",
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Espresso
                )

                // Card 1: Standard Affirmation Card (Strength)
                StandardFeedCard(
                    affirmation = AffirmationRepository.strengthAffirmation,
                    onCardClick = { onNavigateToDetail(AffirmationRepository.strengthAffirmation) }
                )

                // Card 2: Banner Card (PREMIUM / Visualise)
                BannerFeedCard(
                    affirmation = AffirmationRepository.provisionAffirmation,
                    onVisualiseClick = { onNavigateToCreate(AffirmationRepository.provisionAffirmation.id) }
                )

                // Card 3: Action / Added to Daily Card (Courage)
                AddedToDailyFeedCard(
                    affirmation = AffirmationRepository.courageAffirmation,
                    onCardClick = { onNavigateToDetail(AffirmationRepository.courageAffirmation) }
                )
            }
        }
    }
}

// ── Feed Card 1: Standard Card with +12k listeners & Bookmark ──
@Composable
private fun StandardFeedCard(
    affirmation: Affirmation,
    onCardClick: () -> Unit
) {
    val context = LocalContext.current
    var isSaved by remember { mutableStateOf(AffirmationRepository.isSaved(affirmation.id)) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Espresso.copy(alpha = 0.05f))
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(16.dp))
            .clickable(onClick = onCardClick)
            .padding(18.dp)
    ) {
        Column {
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
                    letterSpacing = 1.4.sp
                )
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = null,
                    tint = StoneMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "“${affirmation.declaration}”",
                fontFamily = DisplayFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 17.sp,
                lineHeight = 24.sp,
                color = Espresso
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Listener count
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "👥  ${affirmation.listenerCount ?: "+12k"}",
                        fontFamily = BodyFontFamily,
                        fontSize = 11.5.sp,
                        color = StoneMuted
                    )
                }

                // Share & Bookmark
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = StoneMuted,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { Toast.makeText(context, "Shared", Toast.LENGTH_SHORT).show() }
                    )
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (isSaved) Terracotta else StoneMuted,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable {
                                AffirmationRepository.toggleSave(affirmation.id)
                                isSaved = !isSaved
                            }
                    )
                }
            }
        }
    }
}

// ── Feed Card 2: Banner Card with "PREMIUM" and "Visualise" ──
@Composable
private fun BannerFeedCard(
    affirmation: Affirmation,
    onVisualiseClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Espresso.copy(alpha = 0.12f))
            .clip(RoundedCornerShape(16.dp))
            .background(AtmosphericGradient) // Warm espresso atmospheric background
            .padding(18.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.16f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "PREMIUM",
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 9.sp,
                    letterSpacing = 1.4.sp,
                    color = TerracottaLight
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "“${affirmation.declaration}”",
                fontFamily = DisplayFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 17.sp,
                lineHeight = 24.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Category: ${affirmation.category}",
                    fontFamily = BodyFontFamily,
                    fontSize = 11.5.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Button(
                    onClick = onVisualiseClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Surface,
                        contentColor = Espresso
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = "Visualise",
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// ── Feed Card 3: Action Card with "✓ ADDED TO DAILY" ──
@Composable
private fun AddedToDailyFeedCard(
    affirmation: Affirmation,
    onCardClick: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Espresso.copy(alpha = 0.05f))
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(16.dp))
            .clickable(onClick = onCardClick)
            .padding(18.dp)
    ) {
        Column {
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
                    letterSpacing = 1.4.sp
                )
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = null,
                    tint = StoneMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "“${affirmation.declaration}”",
                fontFamily = DisplayFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 17.sp,
                lineHeight = 24.sp,
                color = Espresso
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Sage,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "ADDED TO DAILY",
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.5.sp,
                        letterSpacing = 1.2.sp,
                        color = Sage
                    )
                }

                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = StoneMuted,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { Toast.makeText(context, "Shared", Toast.LENGTH_SHORT).show() }
                )
            }
        }
    }
}
