package com.makarios.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.ui.theme.*

@Composable
fun LibraryScreen(
    onNavigateToCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Porcelain
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Header: < Library with filter sliders icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Library",
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 22.sp,
                        color = Espresso
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Surface)
                        .border(1.dp, Border, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Filter",
                        tint = Espresso,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Search Bar: "Search affirmations or categories..."
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(24.dp), spotColor = Espresso.copy(alpha = 0.05f))
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
                        text = if (searchQuery.isEmpty()) "Search affirmations or categories..." else searchQuery,
                        fontFamily = BodyFontFamily,
                        fontSize = 14.sp,
                        color = if (searchQuery.isEmpty()) StoneMuted else Espresso
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Section 1: Identity & Purpose
            LibrarySection(title = "Identity & Purpose") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LibraryCategoryCard(
                        title = "Royal Identity",
                        count = "42 Affirmations",
                        icon = "👑",
                        onClick = { onNavigateToCategory("Identity") },
                        modifier = Modifier.weight(1f)
                    )
                    LibraryCategoryCard(
                        title = "Purpose Path",
                        count = "35 Affirmations",
                        icon = "🧭",
                        onClick = { onNavigateToCategory("Purpose") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 2: Emotional Well-being
            LibrarySection(title = "Emotional Well-being") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LibraryCategoryCard(
                        title = "Inner Peace",
                        count = "50 Affirmations",
                        icon = "🌿",
                        onClick = { onNavigateToCategory("Peace") },
                        modifier = Modifier.weight(1f)
                    )
                    LibraryCategoryCard(
                        title = "Abounding Joy",
                        count = "28 Affirmations",
                        icon = "☀️",
                        onClick = { onNavigateToCategory("Joy") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 3: WEEKLY SPECIAL Banner Card (Strength in Silence)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .shadow(4.dp, RoundedCornerShape(18.dp), spotColor = Espresso.copy(alpha = 0.15f))
                    .clip(RoundedCornerShape(18.dp))
                    .background(AtmosphericGradient) // Warm espresso-plum gradient
                    .clickable { onNavigateToCategory("Strength") }
                    .padding(22.dp)
            ) {
                Column {
                    Text(
                        text = "WEEKLY SPECIAL",
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        letterSpacing = 1.8.sp,
                        color = TerracottaLight
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Strength in\nSilence",
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 22.sp,
                        lineHeight = 28.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "12 new declarations →",
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.5.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 4: Life Pillars
            LibrarySection(title = "Life Pillars") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    LifePillarRow(
                        title = "Work & Discipline",
                        subtitle = "Faith in the workplace",
                        icon = Icons.Default.Work,
                        onClick = { onNavigateToCategory("Work") }
                    )
                    LifePillarRow(
                        title = "Relationships",
                        subtitle = "Loving as Christ loved",
                        icon = Icons.Default.Favorite,
                        onClick = { onNavigateToCategory("Relationships") }
                    )
                }
            }
        }
    }
}

@Composable
private fun LibrarySection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = title,
            fontFamily = BodyFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = Espresso
        )
        Spacer(modifier = Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun LibraryCategoryCard(
    title: String,
    count: String,
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp), spotColor = Espresso.copy(alpha = 0.06f))
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(TerracottaLight),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Espresso
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = count,
                fontFamily = BodyFontFamily,
                fontSize = 12.sp,
                color = StoneMuted
            )
        }
    }
}

@Composable
private fun LifePillarRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(14.dp), spotColor = Espresso.copy(alpha = 0.05f))
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(TerracottaLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Terracotta,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = title,
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Espresso
                    )
                    Text(
                        text = subtitle,
                        fontFamily = BodyFontFamily,
                        fontSize = 12.sp,
                        color = StoneMuted
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = StoneMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
