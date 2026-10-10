package com.makarios.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.data.CuratedTopicPair
import com.makarios.app.data.CuratedTopicRepository
import com.makarios.app.data.TopicMeta
import com.makarios.app.data.bible.BibleReferenceParser
import com.makarios.app.data.bible.BibleRepository
import com.makarios.app.data.bible.BibleVerse
import com.makarios.app.data.bible.ParsedReference
import com.makarios.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun ExploreScreen(
    onNavigateToTopic: (String, Light) -> Unit = { _, _ -> },
    onNavigateToBiblePicker: () -> Unit = {},
    onNavigateToReader: (book: String, chapter: Int, verse: Int?) -> Unit = { _, _, _ -> },
    onNavigateToDeclare: (declaration: String, verse: String?, reference: String?) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var selectedGroupChip by remember { mutableStateOf("All") }

    SystemBarsController(Light.Dawn)

    LaunchedEffect(Unit) {
        BibleRepository.initialize(context)
    }

    // 3-in-1 Debounced Search States
    var parsedRef by remember { mutableStateOf<ParsedReference?>(null) }
    var verseMatches by remember { mutableStateOf<List<BibleVerse>>(emptyList()) }
    var declarationMatches by remember { mutableStateOf<List<CuratedTopicPair>>(emptyList()) }
    var topicMatches by remember { mutableStateOf<List<TopicMeta>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    LaunchedEffect(query) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            parsedRef = null
            verseMatches = emptyList()
            declarationMatches = emptyList()
            topicMatches = emptyList()
            isSearching = false
            return@LaunchedEffect
        }
        delay(200) // 200 ms debounce off main thread
        isSearching = true

        parsedRef = BibleReferenceParser.parse(trimmed)
        topicMatches = CuratedTopicRepository.TOPIC_METAS.filter {
            it.name.contains(trimmed, ignoreCase = true) || it.tagline.contains(trimmed, ignoreCase = true)
        }
        declarationMatches = CuratedTopicRepository.searchDeclarations(trimmed)
        verseMatches = BibleRepository.searchVerses(context, trimmed, limit = 25)

        isSearching = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFBF9F5))
            .statusBarsPadding()
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Explore", style = MakariosTypography.displayLarge, color = Ink)
            IconButton(onClick = onNavigateToBiblePicker) {
                Icon(Icons.Outlined.MenuBook, "Open Bible reader", tint = Ink)
            }
        }

        // Search Field
        Row(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Ink.copy(alpha = 0.07f))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, "Search library", tint = Ink.copy(alpha = 0.6f))
            Spacer(Modifier.width(12.dp))
            BasicTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = MakariosTypography.bodyMedium.copy(color = Ink),
                decorationBox = { field ->
                    if (query.isEmpty()) {
                        Text("Search promises and scriptures", color = Ink.copy(alpha = 0.5f))
                    }
                    field()
                }
            )
            if (query.isNotEmpty()) {
                IconButton(onClick = { query = "" }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, "Clear", tint = Ink.copy(alpha = 0.5f))
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (query.isBlank()) {
            // EXPLORE HOME VIEW
            LazyColumn(
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Verse of the Day Strip
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4E4)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onNavigateToReader("Romans", 8, 28)
                            }
                    ) {
                        Column(Modifier.padding(18.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFFB5532B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "VERSE OF THE DAY",
                                        style = MakariosTypography.labelSmall.copy(letterSpacing = 1.2.sp),
                                        color = Color(0xFF8A5A14)
                                    )
                                }
                                Text(
                                    text = "Read chapter",
                                    style = MakariosTypography.labelSmall,
                                    color = Color(0xFF8A5A14)
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "“And we know that in all things God works for the good of those who love Him, who have been called according to His purpose.”",
                                style = MakariosTypography.bodyMedium.copy(fontStyle = FontStyle.Italic, lineHeight = 22.sp),
                                color = Ink
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Romans 8:28",
                                style = MakariosTypography.labelMedium,
                                color = Color(0xFFB5532B)
                            )
                        }
                    }
                }

                // 2. 7 Topic Bands (Light scenes)
                items(CuratedTopicRepository.TOPIC_METAS, key = { it.name }) { topic ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(112.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .drawBehind {
                                drawLight(topic.light, size, horizon = HorizonSpec(0.72f, 0.80f, 0.88f))
                            }
                            .clickable { onNavigateToTopic(topic.name, topic.light) }
                            .padding(16.dp)
                    ) {
                        Text(topic.name, color = topic.light.text, style = MakariosTypography.displaySmall)
                        Row(Modifier.align(Alignment.TopEnd), verticalAlignment = Alignment.CenterVertically) {
                            Text("${topic.count} promises", color = topic.light.text.copy(alpha = 0.8f), style = MakariosTypography.labelSmall)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Outlined.ChevronRight, "Open ${topic.name}", tint = topic.light.text)
                        }
                        Text(
                            topic.tagline,
                            color = topic.light.text.copy(alpha = 0.9f),
                            style = MakariosTypography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                            modifier = Modifier.align(Alignment.BottomStart)
                        )
                    }
                }

                // 3. "Read the Bible" Band
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Ink),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(88.dp)
                            .clickable { onNavigateToBiblePicker() }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFF4E4).copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.MenuBook,
                                        contentDescription = null,
                                        tint = Color(0xFFFFF4E4)
                                    )
                                }
                                Spacer(Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "Read the Bible",
                                        style = MakariosTypography.titleLarge,
                                        color = Color(0xFFFFF4E4)
                                    )
                                    Text(
                                        text = "All 66 books, chapters & verses",
                                        style = MakariosTypography.bodySmall,
                                        color = Color(0xFFFFF4E4).copy(alpha = 0.7f)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Outlined.ChevronRight,
                                contentDescription = "Open Bible",
                                tint = Color(0xFFFFF4E4)
                            )
                        }
                    }
                }
            }
        } else {
            // SEARCH RESULTS VIEW
            Column(modifier = Modifier.fillMaxSize()) {
                // Group chips: All, Verses, Declarations, Topics
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val totalMatches = verseMatches.size + declarationMatches.size + topicMatches.size
                    listOf(
                        "All" to totalMatches,
                        "Verses" to verseMatches.size,
                        "Declarations" to declarationMatches.size,
                        "Topics" to topicMatches.size
                    ).forEach { (group, count) ->
                        val isSelected = selectedGroupChip == group
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (isSelected) Ink else Ink.copy(alpha = 0.08f))
                                .clickable { selectedGroupChip = group }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = "$group ($count)",
                                style = MakariosTypography.labelMedium,
                                color = if (isSelected) Color(0xFFFFF4E4) else Ink.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                val totalCount = verseMatches.size + declarationMatches.size + topicMatches.size

                if (totalCount == 0 && parsedRef == null && !isSearching) {
                    // Empty state
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Nothing matches '$query'. Try a single word like peace, or a reference like John 3:16.",
                                style = MakariosTypography.bodyLarge,
                                color = Ink.copy(alpha = 0.65f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = onNavigateToBiblePicker,
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Color(0xFFFFF4E4))
                            ) {
                                Text("Open Bible picker", style = MakariosTypography.labelMedium)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Direct "Open [Reference]" Card if parsed
                        parsedRef?.let { ref ->
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4E4)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onNavigateToReader(ref.book, ref.chapter, ref.fromVerse)
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Open ${ref.formatDisplay()}",
                                                style = MakariosTypography.titleMedium,
                                                color = Color(0xFF8A5A14)
                                            )
                                            Text(
                                                text = "Read chapter ${ref.chapter} in ${ref.book}",
                                                style = MakariosTypography.bodySmall,
                                                color = Ink.copy(alpha = 0.7f)
                                            )
                                        }
                                        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Color(0xFF8A5A14))
                                    }
                                }
                            }
                        }

                        // Matching Topics
                        if (selectedGroupChip == "All" || selectedGroupChip == "Topics") {
                            items(topicMatches, key = { "topic-${it.name}" }) { topic ->
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateToTopic(topic.name, topic.light) }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(topic.name, style = MakariosTypography.titleLarge, color = Ink)
                                            Text(topic.tagline, style = MakariosTypography.bodySmall.copy(fontStyle = FontStyle.Italic), color = Ink.copy(alpha = 0.7f))
                                        }
                                        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Ink.copy(alpha = 0.4f))
                                    }
                                }
                            }
                        }

                        // Matching Declarations
                        if (selectedGroupChip == "All" || selectedGroupChip == "Declarations") {
                            items(declarationMatches, key = { "decl-${it.id}" }) { decl ->
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onNavigateToDeclare(decl.declaration, null, decl.reference)
                                        }
                                ) {
                                    Column(Modifier.padding(16.dp)) {
                                        Text(
                                            text = decl.declaration,
                                            style = MakariosTypography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                            color = Ink
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(decl.reference, style = MakariosTypography.labelSmall, color = Color(0xFF8A5A14))
                                            Text("Declare this →", style = MakariosTypography.labelSmall, color = Ink)
                                        }
                                    }
                                }
                            }
                        }

                        // Matching Verses
                        if (selectedGroupChip == "All" || selectedGroupChip == "Verses") {
                            items(verseMatches, key = { "verse-${it.reference}" }) { verse ->
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onNavigateToReader(verse.book, verse.chapter, verse.verse)
                                        }
                                ) {
                                    Column(Modifier.padding(16.dp)) {
                                        Text(verse.reference, style = MakariosTypography.labelLarge, color = Color(0xFF8A5A14))
                                        Spacer(Modifier.height(4.dp))
                                        HighlightedSearchText(verse.text, query.trim())
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HighlightedSearchText(text: String, query: String) {
    val annotated = remember(text, query) {
        buildAnnotatedString {
            val q = query.lowercase()
            val lower = text.lowercase()
            var startIdx = 0
            while (startIdx < text.length) {
                val matchIdx = lower.indexOf(q, startIdx)
                if (matchIdx == -1) {
                    append(text.substring(startIdx))
                    break
                }
                if (matchIdx > startIdx) {
                    append(text.substring(startIdx, matchIdx))
                }
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, background = Color(0x33B5532B))) {
                    append(text.substring(matchIdx, matchIdx + q.length))
                }
                startIdx = matchIdx + q.length
            }
        }
    }
    Text(
        text = annotated,
        style = MakariosTypography.bodyMedium.copy(fontStyle = FontStyle.Italic),
        color = Ink.copy(alpha = 0.85f)
    )
}
