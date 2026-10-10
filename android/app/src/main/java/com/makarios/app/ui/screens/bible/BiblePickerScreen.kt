package com.makarios.app.ui.screens.bible

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.data.bible.*
import com.makarios.app.ui.theme.Ink
import com.makarios.app.ui.theme.MakariosTypography
import kotlinx.coroutines.delay

@Composable
fun BiblePickerScreen(
    onNavigateBack: () -> Unit,
    onSelectBookChapter: (book: String, chapter: Int) -> Unit,
    onVerseSelected: (VerseSelection) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var selectedTestament by remember { mutableStateOf(Testament.OT) }
    val recents = remember { RecentVersesStore.getRecents(context) }

    // Initialize BibleRepository
    LaunchedEffect(Unit) {
        BibleRepository.initialize(context)
    }

    // Debounced search state
    var parsedReference by remember { mutableStateOf<ParsedReference?>(null) }
    var parsedVerseResult by remember { mutableStateOf<BibleVerse?>(null) }
    var searchResults by remember { mutableStateOf<List<BibleVerse>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    LaunchedEffect(query) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            parsedReference = null
            parsedVerseResult = null
            searchResults = emptyList()
            isSearching = false
            return@LaunchedEffect
        }
        delay(200) // 200ms debounce
        isSearching = true
        val parsed = BibleReferenceParser.parse(trimmed)
        parsedReference = parsed
        if (parsed != null) {
            val v = if (parsed.fromVerse != null) {
                BibleRepository.getVerse(context, "${parsed.book} ${parsed.chapter}:${parsed.fromVerse}")
            } else {
                BibleRepository.getChapter(context, parsed.book, parsed.chapter).firstOrNull()
            }
            parsedVerseResult = v
            searchResults = emptyList()
        } else {
            parsedVerseResult = null
            searchResults = BibleRepository.searchVerses(context, trimmed, limit = 20)
        }
        isSearching = false
    }

    val otListState = rememberLazyListState()
    val ntListState = rememberLazyListState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFBF9F5))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Ink.copy(alpha = 0.08f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Ink
                )
            }

            Text(
                text = "Bible",
                style = MakariosTypography.displaySmall.copy(fontSize = 24.sp),
                color = Ink
            )

            Spacer(modifier = Modifier.size(40.dp))
        }

        // Smart Search Field
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Ink.copy(alpha = 0.06f))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, contentDescription = "Search", tint = Ink.copy(alpha = 0.6f))
            Spacer(Modifier.width(10.dp))
            BasicTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = MakariosTypography.bodyLarge.copy(color = Ink),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = "Try John 3:16, or a word like fear",
                            style = MakariosTypography.bodyLarge,
                            color = Ink.copy(alpha = 0.45f)
                        )
                    }
                    innerTextField()
                }
            )
            if (query.isNotBlank()) {
                IconButton(onClick = { query = "" }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = Ink.copy(alpha = 0.5f))
                }
            }
        }

        // Search Results or Standard Browser
        if (query.isNotBlank()) {
            if (parsedReference != null) {
                // Parsed Reference Direct Result Card
                val pref = parsedReference!!
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Scripture reference",
                        style = MakariosTypography.labelMedium,
                        color = Ink.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(8.dp))
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Text(
                                text = pref.formatDisplay(),
                                style = MakariosTypography.displaySmall.copy(fontSize = 22.sp),
                                color = Ink
                            )
                            parsedVerseResult?.let { v ->
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "“${v.text}”",
                                    style = MakariosTypography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                                    color = Ink.copy(alpha = 0.85f)
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    val sel = VerseSelection(
                                        book = pref.book,
                                        chapter = pref.chapter,
                                        fromVerse = pref.fromVerse ?: 1,
                                        toVerse = pref.toVerse ?: pref.fromVerse ?: 1,
                                        text = parsedVerseResult?.text ?: "Scripture verse",
                                        reference = pref.formatDisplay()
                                    )
                                    RecentVersesStore.addRecent(context, pref.formatDisplay())
                                    onVerseSelected(sel)
                                },
                                shape = RoundedCornerShape(28.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Color(0xFFFFF4E4)),
                                modifier = Modifier.fillMaxWidth().height(50.dp)
                            ) {
                                Text("Use this verse", style = MakariosTypography.labelLarge)
                            }
                        }
                    }
                }
            } else if (searchResults.isNotEmpty()) {
                // Keyword Search Results
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(searchResults, key = { it.reference }) { verse ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val sel = VerseSelection(
                                        book = verse.book,
                                        chapter = verse.chapter,
                                        fromVerse = verse.verse,
                                        toVerse = verse.verse,
                                        text = verse.text,
                                        reference = verse.reference
                                    )
                                    RecentVersesStore.addRecent(context, verse.reference)
                                    onVerseSelected(sel)
                                }
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    text = verse.reference,
                                    style = MakariosTypography.labelLarge,
                                    color = Color(0xFF8A5A14)
                                )
                                Spacer(Modifier.height(4.dp))
                                HighlightedText(text = verse.text, query = query.trim())
                            }
                        }
                    }
                }
            } else if (!isSearching) {
                // Empty search result copy
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No verse has those words. Try one word, or a reference like Psalm 23.",
                        style = MakariosTypography.bodyLarge,
                        color = Ink.copy(alpha = 0.6f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            // Standard Book Browser
            Column(modifier = Modifier.fillMaxSize()) {
                // Recent Chips
                if (recents.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent",
                            style = MakariosTypography.labelSmall,
                            color = Ink.copy(alpha = 0.6f)
                        )
                        recents.forEach { recentRef ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Ink.copy(alpha = 0.08f))
                                    .clickable {
                                        query = recentRef
                                    }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = recentRef,
                                    style = MakariosTypography.labelMedium,
                                    color = Ink
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Segmented switch: Old Testament / New Testament
                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Ink.copy(alpha = 0.08f))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selectedTestament == Testament.OT) Ink else Color.Transparent)
                            .clickable { selectedTestament = Testament.OT },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Old Testament",
                            style = MakariosTypography.labelLarge,
                            color = if (selectedTestament == Testament.OT) Color(0xFFFFF4E4) else Ink.copy(alpha = 0.7f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selectedTestament == Testament.NT) Ink else Color.Transparent)
                            .clickable { selectedTestament = Testament.NT },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "New Testament",
                            style = MakariosTypography.labelLarge,
                            color = if (selectedTestament == Testament.NT) Color(0xFFFFF4E4) else Ink.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Book List with chapter counts
                val activeBooks = BibleRepository.books.filter { it.testament == selectedTestament }
                val activeListState = if (selectedTestament == Testament.OT) otListState else ntListState

                LazyColumn(
                    state = activeListState,
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(activeBooks, key = { it.name }) { book ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White)
                                .clickable {
                                    onSelectBookChapter(book.name, 1)
                                }
                                .padding(horizontal = 18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = book.name,
                                style = MakariosTypography.titleMedium,
                                color = Ink
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${book.chapterCount} chapters",
                                    style = MakariosTypography.bodyMedium,
                                    color = Ink.copy(alpha = 0.55f)
                                )
                                Spacer(Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Outlined.ChevronRight,
                                    contentDescription = "Open ${book.name}",
                                    tint = Ink.copy(alpha = 0.4f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HighlightedText(text: String, query: String) {
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
