package com.makarios.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.makarios.app.data.Affirmation
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.data.PhotoLibrary
import com.makarios.app.ui.components.AffirmationDetailDialog
import com.makarios.app.ui.components.BibleReaderSheet
import com.makarios.app.ui.components.Pairing
import com.makarios.app.ui.theme.*

data class TopicBand(
    val name: String,
    val promise: String,
    val light: Light,
    val searchCategory: String
)

val TopicBands = listOf(
    TopicBand("Identity", "Who you are in Christ", Light.Dawn, "Identity"),
    TopicBand("Peace", "Rest for a worried mind", Light.Mist, "Peace"),
    TopicBand("Strength", "When you have nothing left", Light.Ember, "Strength"),
    TopicBand("Purpose", "Why you are here", Light.Rain, "Purpose"),
    TopicBand("Courage", "For the next brave step", Light.Dusk, "Courage"),
    TopicBand("Joy", "Gladness that lasts", Light.Midday, "Joy"),
    TopicBand("Provision", "Resting in divine abundance", Light.Grove, "Provision"),
    TopicBand("Discipline", "Clarity, endurance and focus", Light.Night, "Discipline")
)

@Composable
fun ExploreScreen(
    onNavigateToTopic: (String, Light) -> Unit = { _, _ -> },
    onNavigateToCreate: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("Curated") }
    var searchQuery by remember { mutableStateOf("") }
    var activeTopic by remember { mutableStateOf<TopicBand?>(null) }
    var selectedAffirmationForDetail by remember { mutableStateOf<Affirmation?>(null) }
    var showBibleReader by remember { mutableStateOf(false) }

    val allAffirmations = remember { AffirmationRepository.getAll() }

    // Live filtered results based on search query
    val searchResults = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            val q = searchQuery.trim().lowercase()
            allAffirmations.filter { aff ->
                aff.declaration.lowercase().contains(q) ||
                aff.scriptureText.lowercase().contains(q) ||
                aff.reference.lowercase().contains(q) ||
                aff.category.lowercase().contains(q) ||
                aff.context.lowercase().contains(q)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFBF9F5))
            .padding(top = 48.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Explore",
                style = MakariosTypography.displayLarge,
                color = Ink
            )

            // Bible Reader Icon button
            IconButton(
                onClick = { showBibleReader = true },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Ink.copy(alpha = 0.06f))
            ) {
                Icon(
                    imageVector = Icons.Outlined.MenuBook,
                    contentDescription = "Open Bible Reader",
                    tint = Ink
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Interactive Search Field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, Ink.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                .background(Color.White)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = Ink.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            BasicTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                textStyle = MakariosTypography.bodyMedium.copy(color = Ink),
                modifier = Modifier.weight(1f),
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search promises, scriptures, themes...",
                            style = MakariosTypography.bodyMedium,
                            color = Ink.copy(alpha = 0.4f)
                        )
                    }
                    innerTextField()
                }
            )

            if (searchQuery.isNotEmpty()) {
                IconButton(
                    onClick = { searchQuery = "" },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Clear search",
                        tint = Ink.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Tabs (Curated / Community)
        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ExploreTab("Curated", selectedTab == "Curated") {
                selectedTab = "Curated"
                activeTopic = null
            }
            ExploreTab("Community", selectedTab == "Community") {
                selectedTab = "Community"
                activeTopic = null
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Content Area
        if (searchQuery.isNotBlank()) {
            // Search Results Mode
            Text(
                text = "${searchResults.size} declarations found",
                style = MakariosTypography.labelMedium,
                color = Ink.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No declarations found for \"$searchQuery\"",
                            style = MakariosTypography.bodyLarge,
                            color = Ink.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Try searching for Peace, Grace, Strength, or browse the Bible reader.",
                            style = MakariosTypography.bodyMedium,
                            color = Ink.copy(alpha = 0.5f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(searchResults) { aff ->
                        val topicLight = TopicBands.find { it.name.equals(aff.category, ignoreCase = true) }?.light ?: Light.Dawn
                        AffirmationCardMini(
                            affirmation = aff,
                            light = topicLight,
                            onClick = { selectedAffirmationForDetail = aff }
                        )
                    }
                }
            }
        } else if (selectedTab == "Curated") {
            if (activeTopic != null) {
                // Topic Affirmations Drill-Down View
                val topic = activeTopic!!
                val topicAffirmations = allAffirmations.filter {
                    it.category.equals(topic.searchCategory, ignoreCase = true)
                }.ifEmpty {
                    allAffirmations.take(3)
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .clickable { activeTopic = null },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Ink,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("All Topics", style = MakariosTypography.labelLarge, color = Ink)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Topic Hero Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .height(130.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .lightBackground(topic.light)
                    ) {
                        val photoUrl = PhotoLibrary.getThemePhoto(topic.name).url(width = 800)
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            colorFilter = WarmPhotoGrade
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        0.0f to topic.light.top.copy(alpha = 0.90f),
                                        1.0f to topic.light.bottom.copy(alpha = 0.65f)
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = topic.name,
                                style = MakariosTypography.displaySmall,
                                color = topic.light.text
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = topic.promise,
                                style = MakariosTypography.labelLarge,
                                color = topic.light.text.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    LazyColumn(
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(topicAffirmations) { aff ->
                            AffirmationCardMini(
                                affirmation = aff,
                                light = topic.light,
                                onClick = { selectedAffirmationForDetail = aff }
                            )
                        }
                    }
                }
            } else {
                // Topic Bands List
                LazyColumn(
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(TopicBands) { topic ->
                        val count = allAffirmations.count {
                            it.category.equals(topic.searchCategory, ignoreCase = true)
                        }.let { if (it == 0) 2 else it }

                        TopicBandItem(
                            topic = topic,
                            count = count,
                            onClick = {
                                activeTopic = topic
                                onNavigateToTopic(topic.name, topic.light)
                            }
                        )
                    }
                }
            }
        } else {
            // Community View
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .lightBackground(Light.Mist),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Global Voices of Truth",
                        style = MakariosTypography.displaySmall,
                        color = Light.Mist.text,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Believers around the world are declaring God's promises today.",
                        style = MakariosTypography.bodyMedium,
                        color = Light.Mist.text.copy(alpha = 0.8f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onNavigateToCreate,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Light.Mist.buttonFill,
                            contentColor = Light.Mist.buttonText
                        ),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(text = "Author a Declaration", style = MakariosTypography.labelLarge)
                    }
                }
            }
        }
    }

    // Detail Dialog
    if (selectedAffirmationForDetail != null) {
        val aff = selectedAffirmationForDetail!!
        val light = TopicBands.find { it.name.equals(aff.category, ignoreCase = true) }?.light ?: Light.Dawn
        AffirmationDetailDialog(
            affirmation = aff,
            light = light,
            onDismiss = { selectedAffirmationForDetail = null }
        )
    }

    // Bible Reader Sheet
    if (showBibleReader) {
        BibleReaderSheet(
            onDismiss = { showBibleReader = false }
        )
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(20.dp))
            .lightBackground(topic.light)
            .clickable { onClick() }
    ) {
        // Subtle theme background photo preview
        val photoUrl = PhotoLibrary.getThemePhoto(topic.name).url(width = 600)
        AsyncImage(
            model = photoUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            colorFilter = WarmPhotoGrade
        )

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        0.0f to topic.light.top.copy(alpha = 0.92f),
                        0.7f to topic.light.top.copy(alpha = 0.85f),
                        1.0f to topic.light.bottom.copy(alpha = 0.70f)
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = topic.name,
                    style = MakariosTypography.displaySmall,
                    color = topic.light.text
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = topic.promise,
                    style = MakariosTypography.labelLarge.copy(fontSize = 13.sp),
                    color = topic.light.text.copy(alpha = 0.75f)
                )
            }

            Text(
                text = count.toString(),
                style = MakariosTypography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
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
}

@Composable
fun AffirmationCardMini(
    affirmation: Affirmation,
    light: Light,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 160.dp)
            .clip(RoundedCornerShape(20.dp))
            .lightBackground(light)
            .clickable { onClick() }
    ) {
        if (affirmation.imageUrl.isNotBlank()) {
            AsyncImage(
                model = affirmation.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                colorFilter = WarmPhotoGrade
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to light.top.copy(alpha = 0.88f),
                            1.0f to light.bottom.copy(alpha = 0.95f)
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Pairing(
                declaration = affirmation.declaration,
                verseText = affirmation.scriptureText,
                verseReference = affirmation.reference,
                light = light,
                isCompact = true
            )
        }
    }
}
