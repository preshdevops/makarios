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
import com.makarios.app.ui.components.WidgetPreviewCard
import com.makarios.app.ui.theme.*

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

    val categories = remember {
        listOf("All", "Peace", "Strength", "Identity", "Purpose", "Courage", "Joy", "Provision")
    }

    // Filter curated affirmations
    val curatedAffirmations = remember(selectedCategory) {
        val all = listOf(
            AffirmationRepository.strengthAffirmation,
            AffirmationRepository.provisionAffirmation,
            AffirmationRepository.courageAffirmation,
            AffirmationRepository.favorite1,
            AffirmationRepository.favorite2
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
                    .size(50.dp)
                    .shadow(3.dp, CircleShape, spotColor = Espresso.copy(alpha = 0.18f))
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Declaration",
                    modifier = Modifier.size(22.dp)
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
            // ── 1. Top Header Bar ─────────────────────────────────
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
                        fontWeight = FontWeight.Medium,
                        fontSize = 22.sp,
                        letterSpacing = (-0.2).sp,
                        color = Espresso
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = greeting,
                        fontFamily = DisplayFontFamily,
                        fontStyle = FontStyle.Italic,
                        fontSize = 13.sp,
                        color = Stone
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Notification Bell (peaceful)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
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
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Profile Avatar
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PorcelainWarm)
                            .border(0.5.dp, BorderSubtle, CircleShape)
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

            Spacer(modifier = Modifier.height(4.dp))

            // ── 2. Creator Prompt Card ("What do you need to hear today?") ──
            CreatorPromptCard(
                onClick = { onNavigateToCreate("") }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // ── 3. Today's Declaration Hero Card (Candlelit Sanctuary) ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .shadow(4.dp, RoundedCornerShape(24.dp), spotColor = Espresso.copy(alpha = 0.10f))
                    .clip(RoundedCornerShape(24.dp))
                    .background(AtmosphericGradient)
                    .clickable { onNavigateToDetail(aotd) }
                    .padding(24.dp)
            ) {
                Column {
                    // Header line without kicker eyebrow badge
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
                            fontSize = 13.5.sp,
                            color = Color.White.copy(alpha = 0.70f)
                        )

                        Text(
                            text = aotd.category.uppercase(),
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 9.5.sp,
                            letterSpacing = 1.6.sp,
                            color = Color.White.copy(alpha = 0.60f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Main Quote in Fraunces Serif
                    Text(
                        text = "“${aotd.declaration}”",
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 22.sp,
                        lineHeight = 31.sp,
                        letterSpacing = (-0.2).sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Grounding Scripture Quote
                    Text(
                        text = "“${aotd.scriptureText}”",
                        fontFamily = DisplayFontFamily,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

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
                            letterSpacing = 1.4.sp,
                            color = Color.White.copy(alpha = 0.80f)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Share Button
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .clickable {
                                        Toast.makeText(context, "Shared declaration", Toast.LENGTH_SHORT).show()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            // Favorite Button
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (isAotdSaved) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.15f))
                                    .clickable {
                                        AffirmationRepository.toggleSave(aotd.id)
                                        isAotdSaved = !isAotdSaved
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isAotdSaved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (isAotdSaved) TerracottaLight else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Forward Arrow Button -> Fullscreen Detail
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f))
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

            Spacer(modifier = Modifier.height(24.dp))

            // ── 4. Browse Categories (Text-only pills) ────────────
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Browse Categories",
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        color = Espresso
                    )
                    Text(
                        text = "See All",
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Stone,
                        modifier = Modifier.clickable(onClick = onNavigateToLibrary)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    categories.forEach { category ->
                        val isSelected = category == selectedCategory
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .then(
                                    if (isSelected) {
                                        Modifier
                                            .background(Espresso)
                                            .shadow(1.dp, RoundedCornerShape(20.dp), spotColor = Espresso.copy(alpha = 0.15f))
                                    } else {
                                        Modifier
                                            .background(Surface)
                                            .border(0.5.dp, BorderSubtle, RoundedCornerShape(20.dp))
                                    }
                                )
                                .clickable { selectedCategory = category }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = category,
                                fontFamily = BodyFontFamily,
                                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                fontSize = 13.sp,
                                color = if (isSelected) Surface else Stone
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── 5. Curated Declarations ───────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (selectedCategory == "All") "Curated Declarations" else "$selectedCategory Declarations",
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Espresso
                )

                if (curatedAffirmations.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No affirmations found in $selectedCategory",
                            fontFamily = BodyFontFamily,
                            color = StoneMuted,
                            fontSize = 13.5.sp
                        )
                    }
                } else {
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
                                Toast.makeText(context, "Shared declaration", Toast.LENGTH_SHORT).show()
                            },
                            onCardClick = { onNavigateToDetail(affirmation) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── 6. Live on Your Phone (Widget Preview) ────────────
            WidgetPreviewCard(
                affirmation = AffirmationRepository.widgetAffirmation,
                onAddToHomeScreen = {
                    Toast.makeText(context, "Hold your home screen and select Makarios from widgets", Toast.LENGTH_LONG).show()
                },
                onSeeAllWidgets = onNavigateToWidgets
            )
        }
    }
}
