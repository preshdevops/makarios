package com.makarios.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.makarios.app.data.Affirmation
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.ui.components.Pairing
import com.makarios.app.ui.theme.*

@Composable
fun SavedScreen(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableStateOf("Declarations") }
    val savedAffirmations = remember { AffirmationRepository.getAll().take(3) } // Mock saved
    val counts = mapOf("Declarations" to savedAffirmations.size, "Wallpapers" to 0, "Mine" to 0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFBF9F5))
            .padding(top = 48.dp)
    ) {
        Text(
            text = "Kept",
            style = MakariosTypography.displayLarge,
            color = Ink,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Tabs
        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            KeptTab("Declarations", counts["Declarations"] ?: 0, selectedTab == "Declarations") { selectedTab = "Declarations" }
            KeptTab("Wallpapers", counts["Wallpapers"] ?: 0, selectedTab == "Wallpapers") { selectedTab = "Wallpapers" }
            KeptTab("Mine", counts["Mine"] ?: 0, selectedTab == "Mine") { selectedTab = "Mine" }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        if (savedAffirmations.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nothing kept yet. Tap the bookmark on Today to keep one.",
                    style = MakariosTypography.bodyMedium,
                    color = Ink.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 48.dp)
                )
            }
        } else {
            when (selectedTab) {
                "Declarations", "Mine" -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 24.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(savedAffirmations) { affirmation ->
                            KeptCard(affirmation)
                        }
                    }
                }
                "Wallpapers" -> {
                    // Wallpaper Grid placeholder
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Wallpapers Grid here", color = Ink)
                    }
                }
            }
        }
    }
}

@Composable
fun KeptTab(title: String, count: Int, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = "$title $count",
            style = MakariosTypography.bodyMedium,
            color = if (selected) Ink else Ink.copy(alpha = 0.4f),
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
        if (selected) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.height(2.dp).width(32.dp).background(Ink))
        }
    }
}

@Composable
fun KeptCard(affirmation: Affirmation) {
    val topicLight = TopicBands.find { it.name == affirmation.category }?.light ?: Light.Grove
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 200.dp)
            .clip(RoundedCornerShape(20.dp))
            .lightBackground(topicLight)
            .padding(24.dp)
    ) {
        Column {
            Pairing(
                declaration = affirmation.declaration,
                verseText = affirmation.scriptureText,
                verseReference = affirmation.reference,
                light = topicLight,
                isCompact = true // uses 22/28
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconRailButton(
                    icon = Icons.Default.Share,
                    contentDescription = "Share",
                    light = topicLight,
                    onClick = { /* Share */ }
                )
                Spacer(modifier = Modifier.width(16.dp))
                IconRailButton(
                    icon = Icons.Default.Favorite,
                    contentDescription = "Unkeep",
                    light = topicLight,
                    onClick = { /* Unkeep */ }
                )
            }
        }
    }
}
