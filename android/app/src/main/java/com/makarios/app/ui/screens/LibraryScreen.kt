package com.makarios.app.ui.screens
import com.makarios.app.ui.components.MakChip
import com.makarios.app.ui.components.MakSurface

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.snapshotFlow
import com.makarios.app.data.MatchEngine

@Composable
fun ExploreScreen(
    onNavigateToTopic: (String, Light) -> Unit = { _, _ -> },
    onNavigateToBiblePicker: () -> Unit = {},
    onNavigateToReader: (book: String, chapter: Int, verse: Int?) -> Unit = { _, _, _ -> },
    onNavigateToDeclare: (declaration: String, verse: String?, reference: String?) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val queryState = remember { mutableStateOf("") }
    var activeQuery by remember { mutableStateOf("") }
    var selectedGroupChip by remember { mutableStateOf("All") }

    val appLight = LocalLight.current
    // (LightScreen below publishes this Light to the bottom bar)

    // 3-in-1 Debounced Search States
    var parsedRef by remember { mutableStateOf<ParsedReference?>(null) }
    var verseMatches by remember { mutableStateOf<List<BibleVerse>>(emptyList()) }
    var declarationMatches by remember { mutableStateOf<List<CuratedTopicPair>>(emptyList()) }
    var topicMatches by remember { mutableStateOf<List<TopicMeta>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        launch(Dispatchers.Default) {
            MatchEngine.getInstance(context).initialize()
        }
        snapshotFlow { queryState.value }.collectLatest { rawQuery ->
            delay(200)
            activeQuery = rawQuery
            val trimmed = rawQuery.trim()
            if (trimmed.isBlank()) {
                parsedRef = null
                verseMatches = emptyList()
                declarationMatches = emptyList()
                topicMatches = emptyList()
                isSearching = false
                return@collectLatest
            }
            isSearching = true
            val localMatches = withContext(Dispatchers.Default) {
                Triple(
                    BibleReferenceParser.parse(trimmed),
                    CuratedTopicRepository.TOPIC_METAS.filter {
                        it.name.contains(trimmed, ignoreCase = true) || it.tagline.contains(trimmed, ignoreCase = true)
                    },
                    CuratedTopicRepository.searchDeclarations(trimmed)
                )
            }
            parsedRef = localMatches.first
            topicMatches = localMatches.second
            declarationMatches = localMatches.third
            verseMatches = withContext(Dispatchers.IO) {
                BibleRepository.initialize(context)
                BibleRepository.searchVerses(trimmed, limit = 25).getOrElse { emptyList() }
            }
            isSearching = false
        }
    }
    com.makarios.app.ui.components.LightScreen(appLight, "Explore", modifier) {
    Column(
        modifier = Modifier
            .fillMaxSize()
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
            Text("Explore", style = MakariosTypography.displayLarge, color = appLight.text)
        }

        ExploreSearchField(queryState)
        Spacer(Modifier.height(16.dp))

        if (activeQuery.isBlank()) {
            var votd by remember { mutableStateOf(com.makarios.app.data.bible.VerseOfTheDayStore.fallbackVerse()) }
            LaunchedEffect(Unit) { votd = com.makarios.app.data.bible.VerseOfTheDayStore.loadTodayVerse(context) }
            // EXPLORE HOME VIEW
            LazyColumn(
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 0. Read the Bible Card
                item(key = "explore-read-bible-card") {
                    MakSurface(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Cream),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onNavigateToBiblePicker()
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = "Read the Bible",
                                    style = MakariosTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Ink
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Read any chapter, verse by verse, across translations.",
                                    style = MakariosTypography.bodySmall,
                                    color = Ink.copy(alpha = 0.70f)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Icon(
                                imageVector = Icons.Outlined.MenuBook,
                                contentDescription = "Read the Bible",
                                tint = Ink
                            )
                        }
                    }
                }

                // 1. Verse of the Day Strip
                item(key = "explore-votd") {
                    MakSurface(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Cream),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onNavigateToReader(votd.book, votd.chapter, votd.verse)
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
                                        tint = MakError,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "VERSE OF THE DAY",
                                        style = MakariosTypography.labelSmall.copy(letterSpacing = 1.2.sp),
                                        color = AnchorLight
                                    )
                                }
                                Text(
                                    text = "Read chapter",
                                    style = MakariosTypography.labelSmall,
                                    color = AnchorLight
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "“${votd.text}”",
                                style = MakariosTypography.bodyMedium.copy(fontStyle = FontStyle.Italic, lineHeight = 22.sp),
                                color = Ink
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = votd.reference,
                                style = MakariosTypography.labelMedium,
                                color = MakError
                            )
                        }
                    }
                }

                // 2. 7 Topic Bands (Light scenes)
                items(CuratedTopicRepository.TOPIC_METAS.distinctBy { it.name }, key = { "explore-topic-${it.name}" }) { topic ->
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
                            Icon(Icons.Outlined.ChevronRight, "Open ${topic.name}", tint = topic.light.text)
                        }
                        Text(
                            topic.tagline,
                            color = topic.light.text,
                            style = MakariosTypography.bodyMedium,
                            modifier = Modifier.align(Alignment.BottomStart).background(topic.light.scrim, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                        )
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
                    val groups = remember(verseMatches.size, declarationMatches.size, topicMatches.size) {
                        listOf(
                            "All" to totalMatches,
                            "Verses" to verseMatches.size,
                            "Declarations" to declarationMatches.size,
                            "Topics" to topicMatches.size
                        )
                    }
                    groups.forEach { (group, count) ->
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
                                color = if (isSelected) Cream else Ink.copy(alpha = 0.7f)
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
                                text = "Nothing matches '$activeQuery'. Try a single word like peace, or a reference like John 3:16.",
                                style = MakariosTypography.bodyLarge,
                                color = Ink.copy(alpha = 0.65f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = onNavigateToBiblePicker,
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Cream)
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
                                        item(key = "explore-search-direct-reference") {
                                MakSurface(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Cream),
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
                                                color = AnchorLight
                                            )
                                            Text(
                                                text = "Read chapter ${ref.chapter} in ${ref.book}",
                                                style = MakariosTypography.bodySmall,
                                                color = Ink.copy(alpha = 0.7f)
                                            )
                                        }
                                        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = AnchorLight)
                                    }
                                }
                            }
                        }

                        // Matching Topics
                        if (selectedGroupChip == "All" || selectedGroupChip == "Topics") {
                            items(topicMatches.distinctBy { it.name }, key = { "explore-search-topic-${it.name}" }) { topic ->
                                MakSurface(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Cream),
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
                                            Text(topic.tagline, style = MakariosTypography.bodySmall, color = Ink.copy(alpha = 0.7f))
                                        }
                                        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Ink.copy(alpha = 0.4f))
                                    }
                                }
                            }
                        }

                        // Matching Declarations
                        if (selectedGroupChip == "All" || selectedGroupChip == "Declarations") {
                            items(declarationMatches.distinctBy { it.id }, key = { "explore-search-decl-${it.id}" }) { decl ->
                                MakSurface(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Cream),
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
                                            Text(decl.reference, style = MakariosTypography.labelSmall, color = AnchorLight)
                                            Text("Declare this →", style = MakariosTypography.labelSmall, color = Ink)
                                        }
                                    }
                                }
                            }
                        }

                        // Matching Verses
                        if (selectedGroupChip == "All" || selectedGroupChip == "Verses") {
                            items(verseMatches.distinctBy { "${it.book}-${it.chapter}-${it.verse}" }, key = { "explore-search-verse-${it.book}-${it.chapter}-${it.verse}" }) { verse ->
                                MakSurface(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Cream),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onNavigateToReader(verse.book, verse.chapter, verse.verse)
                                        }
                                ) {
                                    Column(Modifier.padding(16.dp)) {
                                        Text(verse.reference, style = MakariosTypography.labelLarge, color = AnchorLight)
                                        Spacer(Modifier.height(4.dp))
                                        HighlightedSearchText(verse.text, activeQuery.trim())
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
}


@Composable
private fun ExploreSearchField(queryState: androidx.compose.runtime.MutableState<String>) {
    val query = queryState.value
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
            onValueChange = { queryState.value = it },
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = MakariosTypography.bodyMedium.copy(color = Ink),
            decorationBox = { field ->
                if (query.isEmpty()) Text("Search verses and declarations", color = Ink.copy(alpha = 0.5f))
                field()
            }
        )
        if (query.isNotEmpty()) {
            IconButton(onClick = { queryState.value = "" }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, "Clear", tint = Ink.copy(alpha = 0.5f))
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
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, background = Ink.copy(alpha = 0.2f))) {
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
