package com.makarios.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.data.*
import com.makarios.app.data.bible.BibleReferenceParser
import com.makarios.app.data.bible.BibleRepository
import com.makarios.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun TopicDetailScreen(
    topicName: String,
    onNavigateBack: () -> Unit,
    onDeclareThis: (declaration: String, verseText: String, reference: String) -> Unit,
    onReadChapter: (book: String, chapter: Int, verse: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val topicMeta = remember(topicName) {
        CuratedTopicRepository.TOPIC_METAS.firstOrNull { it.name.equals(topicName, ignoreCase = true) }
            ?: TopicMeta(topicName, "Standing on God's truth", Light.Dawn, 26)
    }

    val light = topicMeta.light
    SystemBarsController(light)

    val pairs = remember(topicName) {
        CuratedTopicRepository.getPairsForTopic(topicName)
    }

    var selectedFilter by rememberSaveable { mutableStateOf("Declarations") } // Declarations, Verses, Saved
    val savedIds = remember { mutableStateListOf<String>() }

    // Preload verse texts for pairing rows
    val verseTexts = remember { mutableStateMapOf<String, String>() }
    LaunchedEffect(pairs) {
        BibleRepository.initialize(context)
        pairs.forEach { p ->
            val v = BibleRepository.getVerse(context, p.reference)
            if (v != null) {
                verseTexts[p.id] = v.text
            }
        }
    }

    val displayPairs = remember(pairs, selectedFilter, savedIds.toList()) {
        when (selectedFilter) {
            "Saved" -> pairs.filter { savedIds.contains(it.id) }
            else -> pairs
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFBF9F5))
            .navigationBarsPadding()
    ) {
        // Horizon Scene Header (about 300dp tall in topic's Light)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(290.dp)
                .drawBehind {
                    drawLight(light, size, horizon = HorizonSpec(0.68f, 0.78f, 0.88f))
                }
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            // Back button
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(light.text.copy(alpha = 0.12f))
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Explore",
                    tint = light.text
                )
            }

            // Topic name & tagline at the bottom of header
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = topicMeta.name,
                    style = MakariosTypography.displayLarge.copy(fontSize = 38.sp),
                    color = light.text
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = topicMeta.tagline,
                    style = MakariosTypography.bodyLarge.copy(fontStyle = FontStyle.Italic, fontSize = 17.sp),
                    color = light.text.copy(alpha = 0.88f)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${pairs.size} promises",
                    style = MakariosTypography.labelSmall,
                    color = light.text.copy(alpha = 0.70f)
                )
            }
        }

        // Filter chips: Declarations, Verses, Saved
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("Declarations", "Verses", "Saved").forEach { filter ->
                val isSelected = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) Ink else Ink.copy(alpha = 0.07f))
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = filter,
                        style = MakariosTypography.labelMedium,
                        color = if (isSelected) Color(0xFFFFF4E4) else Ink.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Pairing Rows List with divider hairlines instead of cards
        if (displayPairs.isEmpty() && selectedFilter == "Saved") {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No saved declarations yet in $topicName. Tap the bookmark on any declaration to save it.",
                    style = MakariosTypography.bodyMedium,
                    color = Ink.copy(alpha = 0.6f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 90.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(displayPairs, key = { it.id }) { pair ->
                    val vText = verseTexts[pair.id] ?: "The Word of the Lord stands forever."
                    val isSaved = savedIds.contains(pair.id)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        // Declaration (Newsreader 24)
                        Text(
                            text = pair.declaration,
                            style = MakariosTypography.displaySmall.copy(fontSize = 24.sp, lineHeight = 32.sp),
                            color = Ink
                        )

                        Spacer(Modifier.height(8.dp))

                        // Verse in italic
                        Text(
                            text = "“$vText”",
                            style = MakariosTypography.bodyMedium.copy(fontStyle = FontStyle.Italic, lineHeight = 22.sp),
                            color = Ink.copy(alpha = 0.80f)
                        )

                        Spacer(Modifier.height(4.dp))

                        // Reference
                        Text(
                            text = pair.reference,
                            style = MakariosTypography.labelSmall.copy(letterSpacing = 1.sp),
                            color = Color(0xFF8A5A14)
                        )

                        Spacer(Modifier.height(14.dp))

                        // Action Buttons: Declare this (primary, ink), Read chapter, Bookmark toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // "Declare this" (primary, ink)
                                Button(
                                    onClick = { onDeclareThis(pair.declaration, vText, pair.reference) },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Ink,
                                        contentColor = Color(0xFFFFF4E4)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text("Declare this", style = MakariosTypography.labelMedium)
                                }

                                Spacer(Modifier.width(10.dp))

                                // "Read chapter"
                                OutlinedButton(
                                    onClick = {
                                        val parsed = BibleReferenceParser.parse(pair.reference)
                                        if (parsed != null) {
                                            onReadChapter(parsed.book, parsed.chapter, parsed.fromVerse ?: 1)
                                        }
                                    },
                                    shape = RoundedCornerShape(20.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Ink.copy(alpha = 0.25f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Outlined.MenuBook, contentDescription = null, modifier = Modifier.size(15.dp), tint = Ink)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Read chapter", style = MakariosTypography.labelMedium)
                                }
                            }

                            // Bookmark toggle (saves to Kept)
                            IconButton(
                                onClick = {
                                    if (isSaved) {
                                        savedIds.remove(pair.id)
                                        Toast.makeText(context, "Removed from Kept", Toast.LENGTH_SHORT).show()
                                    } else {
                                        savedIds.add(pair.id)
                                        val aff = Affirmation(
                                            id = "topic-kept-${pair.id}",
                                            declaration = pair.declaration,
                                            scriptureText = vText,
                                            reference = pair.reference,
                                            context = "Saved from Explore: $topicName",
                                            category = topicName,
                                            tone = AffirmationTone.RESOLUTE,
                                            isFavorite = true
                                        )
                                        AffirmationRepository.addPersonalAffirmation(aff)
                                        Toast.makeText(context, "Saved to Kept", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                    contentDescription = if (isSaved) "Remove bookmark" else "Bookmark",
                                    tint = if (isSaved) Color(0xFFB5532B) else Ink.copy(alpha = 0.45f)
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Hairline divider
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = Ink.copy(alpha = 0.12f)
                        )
                    }
                }
            }
        }
    }
}
