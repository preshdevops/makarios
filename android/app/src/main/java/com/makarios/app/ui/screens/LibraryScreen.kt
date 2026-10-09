package com.makarios.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.data.Affirmation
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.ui.theme.*

data class TopicBand(val name: String, val promise: String, val light: Light)

val TopicBands = listOf(
    TopicBand("Identity", "Who you are in Christ", Light.Dawn),
    TopicBand("Peace", "Rest for a worried mind", Light.Mist),
    TopicBand("Strength", "When you have nothing left", Light.Ember),
    TopicBand("Purpose", "Why you are here", Light.Rain),
    TopicBand("Courage", "For the next brave step", Light.Dusk),
    TopicBand("Joy", "Gladness that lasts", Light.Midday),
    TopicBand("Healing", "Wholeness for body and soul", Light.Grove),
    TopicBand("Rest", "Stillness in His presence", Light.Night)
)

@Composable
fun ExploreScreen(
    onNavigateToTopic: (String, Light) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("Curated") }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFBF9F5)) // Or default background if needed
            .padding(top = 48.dp)
    ) {
        Text(
            text = "Explore",
            style = MakariosTypography.displayLarge,
            color = Ink,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Search field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(56.dp)
                .border(1.dp, Ink.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Search...",
                style = MakariosTypography.bodyMedium,
                color = Ink.copy(alpha = 0.4f),
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                contentDescription = "Bible",
                tint = Ink
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Tabs
        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ExploreTab("Curated", selectedTab == "Curated") { selectedTab = "Curated" }
            ExploreTab("Community", selectedTab == "Community") { selectedTab = "Community" }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (selectedTab == "Curated") {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 24.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(TopicBands) { topic ->
                    TopicBandItem(topic = topic, count = AffirmationRepository.getAll().size, onClick = {
                        onNavigateToTopic(topic.name, topic.light)
                    })
                }
            }
        } else {
            // Community empty state on Mist
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .lightBackground(Light.Mist)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No declarations here yet.",
                        style = MakariosTypography.bodyLarge,
                        color = Light.Mist.text
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Be the first to share one.",
                        style = MakariosTypography.bodyMedium,
                        color = Light.Mist.text.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { /* Share a declaration */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Light.Mist.buttonFill,
                            contentColor = Light.Mist.buttonText
                        ),
                        shape = RoundedCornerShape(28.dp), // 56dp height pill
                        modifier = Modifier.height(56.dp).fillMaxWidth()
                    ) {
                        Text(text = "Share a declaration", style = MakariosTypography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
fun ExploreTab(title: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = title,
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
fun TopicBandItem(topic: TopicBand, count: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .clip(RoundedCornerShape(20.dp))
            .lightBackground(topic.light)
            .clickable { onClick() }
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = topic.name,
                style = MakariosTypography.displaySmall,
                color = topic.light.text
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = topic.promise,
                style = MakariosTypography.labelLarge.copy(fontSize = 14.sp),
                color = topic.light.text.copy(alpha = 0.70f)
            )
        }
        
        Text(
            text = count.toString(),
            style = MakariosTypography.bodyMedium,
            color = topic.light.text
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = topic.light.text
        )
    }
}
