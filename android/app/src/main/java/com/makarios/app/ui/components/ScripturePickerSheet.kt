package com.makarios.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.makarios.app.data.BibleVerseEntry
import com.makarios.app.data.BibleVersion
import com.makarios.app.data.BibleVersionRepository
import com.makarios.app.data.ScriptureDatabase
import com.makarios.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.InputStreamReader

// ─── Bible canon metadata ────────────────────────────────────────────────────

private val OLD_TESTAMENT_BOOKS = listOf(
    "Genesis", "Exodus", "Leviticus", "Numbers", "Deuteronomy",
    "Joshua", "Judges", "Ruth", "1 Samuel", "2 Samuel",
    "1 Kings", "2 Kings", "1 Chronicles", "2 Chronicles",
    "Ezra", "Nehemiah", "Esther", "Job", "Psalms", "Proverbs",
    "Ecclesiastes", "Song of Solomon", "Isaiah", "Jeremiah",
    "Lamentations", "Ezekiel", "Daniel", "Hosea", "Joel",
    "Amos", "Obadiah", "Jonah", "Micah", "Nahum", "Habakkuk",
    "Zephaniah", "Haggai", "Zechariah", "Malachi"
)

private val NEW_TESTAMENT_BOOKS = listOf(
    "Matthew", "Mark", "Luke", "John", "Acts",
    "Romans", "1 Corinthians", "2 Corinthians", "Galatians",
    "Ephesians", "Philippians", "Colossians",
    "1 Thessalonians", "2 Thessalonians",
    "1 Timothy", "2 Timothy", "Titus", "Philemon",
    "Hebrews", "James", "1 Peter", "2 Peter",
    "1 John", "2 John", "3 John", "Jude", "Revelation"
)

/** Navigation levels inside the Bible browser */
private enum class BrowseLevel { BOOKS, CHAPTERS, VERSES }

// ─── Main composable ──────────────────────────────────────────────────────────

/**
 * Full-Bible browser bottom sheet.
 *
 * Capabilities:
 *  - Verse, Chapter, Book, and Testament drill-down navigation
 *  - Multiple Bible versions (WEB offline, KJV, BBE, ASV online)
 *  - Instant keyword & reference search across 31,000+ verses
 *  - Retains backward-compatible onVerseSelected(reference, text)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScripturePickerSheet(
    onDismiss: () -> Unit,
    onVerseSelected: (reference: String, text: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // ── State ────────────────────────────────────────────────────────────────
    var allVerses by remember { mutableStateOf<List<BibleVerseEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isTranslating by remember { mutableStateOf(false) }

    var selectedVersion by remember { mutableStateOf(BibleVersionRepository.availableVersions[0]) }
    var showVersionDialog by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var testament by remember { mutableStateOf("OT") } // "OT" | "NT"
    var browseLevel by remember { mutableStateOf(BrowseLevel.BOOKS) }
    var selectedBook by remember { mutableStateOf("") }
    var selectedChapter by remember { mutableIntStateOf(0) }

    // ── Load corpus once ─────────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                context.assets.open("verses.json").use { inputStream ->
                    val jsonString = InputStreamReader(inputStream, Charsets.UTF_8).readText()
                    val jsonArray = JSONArray(jsonString)
                    val list = ArrayList<BibleVerseEntry>(jsonArray.length())
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        list.add(
                            BibleVerseEntry(
                                reference = obj.getString("r"),
                                text = obj.getString("t"),
                                isDevotional = obj.optInt("d", 0) == 1
                            )
                        )
                    }
                    allVerses = list
                }
            } catch (_: Exception) {
                allVerses = ScriptureDatabase.verses.map {
                    BibleVerseEntry(it.reference, it.text, true)
                }
            } finally {
                isLoading = false
            }
        }
    }

    // ── Derived data ─────────────────────────────────────────────────────────

    val canonBooks = if (testament == "OT") OLD_TESTAMENT_BOOKS else NEW_TESTAMENT_BOOKS

    // Keyword/reference search — searches ALL verses (both testaments)
    val searchResults = remember(searchQuery, allVerses) {
        if (searchQuery.isBlank()) emptyList()
        else {
            val q = searchQuery.trim().lowercase()
            allVerses.filter {
                it.reference.lowercase().contains(q) || it.text.lowercase().contains(q)
            }.take(80)
        }
    }

    // Chapters available in the selected book
    val chaptersInBook = remember(selectedBook, allVerses) {
        if (selectedBook.isBlank()) emptyList()
        else {
            allVerses
                .filter { it.reference.startsWith(selectedBook) }
                .mapNotNull { parseChapter(it.reference, selectedBook) }
                .distinct()
                .sorted()
        }
    }

    // Verses in the selected book + chapter
    val versesInChapter = remember(selectedBook, selectedChapter, allVerses) {
        if (selectedBook.isBlank() || selectedChapter == 0) emptyList()
        else allVerses.filter { isVerseInChapter(it.reference, selectedBook, selectedChapter) }
    }

    // ── Handlers ─────────────────────────────────────────────────────────────

    fun goBack() {
        when (browseLevel) {
            BrowseLevel.VERSES -> browseLevel = BrowseLevel.CHAPTERS
            BrowseLevel.CHAPTERS -> {
                browseLevel = BrowseLevel.BOOKS
                selectedBook = ""
            }
            BrowseLevel.BOOKS -> onDismiss()
        }
    }

    fun handleVerseClick(verse: BibleVerseEntry) {
        if (selectedVersion.isOffline || selectedVersion.code == "web") {
            onVerseSelected(verse.reference, verse.text)
            onDismiss()
        } else {
            isTranslating = true
            coroutineScope.launch {
                val translated = BibleVersionRepository.getVerseText(
                    reference = verse.reference,
                    fallbackText = verse.text,
                    version = selectedVersion
                )
                isTranslating = false
                onVerseSelected(verse.reference, translated)
                onDismiss()
            }
        }
    }

    // ── Sheet ─────────────────────────────────────────────────────────────────
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Porcelain,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Border)
            )
        },
        modifier = modifier.fillMaxHeight(0.92f)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {

                // ── Header ────────────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button (when drilling down or in search)
                    if (browseLevel != BrowseLevel.BOOKS || searchQuery.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Surface)
                                .border(1.dp, Border, CircleShape)
                                .clickable {
                                    if (searchQuery.isNotBlank()) searchQuery = ""
                                    else goBack()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Espresso,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when {
                                searchQuery.isNotBlank() -> "Search Results"
                                browseLevel == BrowseLevel.VERSES -> "$selectedBook $selectedChapter"
                                browseLevel == BrowseLevel.CHAPTERS -> selectedBook
                                else -> "Holy Bible"
                            },
                            fontFamily = DisplayFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 22.sp,
                            color = Espresso
                        )
                        Text(
                            text = when {
                                searchQuery.isNotBlank() -> "${searchResults.size} verse${if (searchResults.size != 1) "s" else ""} found"
                                browseLevel == BrowseLevel.VERSES -> "${versesInChapter.size} verses"
                                browseLevel == BrowseLevel.CHAPTERS -> "${chaptersInBook.size} chapters"
                                else -> if (testament == "OT") "Old Testament · ${OLD_TESTAMENT_BOOKS.size} books"
                                else "New Testament · ${NEW_TESTAMENT_BOOKS.size} books"
                            },
                            fontFamily = BodyFontFamily,
                            fontSize = 12.sp,
                            color = Stone
                        )
                    }

                    // Version Selector Chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Surface)
                            .border(1.dp, Border, RoundedCornerShape(12.dp))
                            .clickable { showVersionDialog = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = selectedVersion.displayName,
                                fontFamily = BodyFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = Terracotta
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select Bible Version",
                                tint = Terracotta,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Close button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Surface)
                            .border(1.dp, Border, CircleShape)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Espresso, modifier = Modifier.size(15.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ── Search bar ────────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Surface)
                        .border(1.dp, Border, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 11.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = StoneMuted, modifier = Modifier.size(17.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                if (it.isNotBlank()) {
                                    browseLevel = BrowseLevel.BOOKS
                                    selectedBook = ""
                                    selectedChapter = 0
                                }
                            },
                            textStyle = TextStyle(
                                fontFamily = BodyFontFamily,
                                fontSize = 13.5.sp,
                                color = Espresso
                            ),
                            cursorBrush = SolidColor(Terracotta),
                            modifier = Modifier.weight(1f),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search verse, book, chapter, or keyword…",
                                        fontFamily = BodyFontFamily,
                                        fontSize = 13.sp,
                                        color = StoneMuted
                                    )
                                }
                                innerTextField()
                            }
                        )
                        if (searchQuery.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Border)
                                    .clickable { searchQuery = "" },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Espresso, modifier = Modifier.size(11.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ── Testament toggle (visible only on books level with no search) ──
                AnimatedVisibility(
                    visible = browseLevel == BrowseLevel.BOOKS && searchQuery.isBlank(),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Surface)
                                .border(1.dp, Border, RoundedCornerShape(12.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            TestamentToggleButton(
                                label = "Old Testament",
                                count = "39 books",
                                selected = testament == "OT",
                                modifier = Modifier.weight(1f)
                            ) { testament = "OT" }
                            TestamentToggleButton(
                                label = "New Testament",
                                count = "27 books",
                                selected = testament == "NT",
                                modifier = Modifier.weight(1f)
                            ) { testament = "NT" }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // ── Content ───────────────────────────────────────────────────
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Terracotta, strokeWidth = 2.5.dp)
                    }
                } else {
                    // Search results
                    if (searchQuery.isNotBlank()) {
                        if (searchResults.isEmpty()) {
                            EmptySearchState(query = searchQuery)
                        } else {
                            VerseList(
                                verses = searchResults,
                                versionName = selectedVersion.displayName,
                                onSelect = { handleVerseClick(it) }
                            )
                        }
                        return@Column
                    }

                    // Books level
                    if (browseLevel == BrowseLevel.BOOKS) {
                        BookList(
                            books = canonBooks,
                            onSelect = { book ->
                                selectedBook = book
                                selectedChapter = 0
                                browseLevel = BrowseLevel.CHAPTERS
                            }
                        )
                    }

                    // Chapters level
                    if (browseLevel == BrowseLevel.CHAPTERS) {
                        ChapterGrid(
                            book = selectedBook,
                            chapters = chaptersInBook,
                            onSelect = { chapter ->
                                selectedChapter = chapter
                                browseLevel = BrowseLevel.VERSES
                            }
                        )
                    }

                    // Verses level
                    if (browseLevel == BrowseLevel.VERSES) {
                        VerseList(
                            verses = versesInChapter,
                            versionName = selectedVersion.displayName,
                            onSelect = { handleVerseClick(it) }
                        )
                    }
                }
            }

            // Translation loading overlay
            if (isTranslating) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = Terracotta,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Loading ${selectedVersion.displayName} text…",
                                fontFamily = BodyFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = Espresso
                            )
                        }
                    }
                }
            }
        }
    }

    // ── Version Selection Dialog ───────────────────────────────────────────────
    if (showVersionDialog) {
        Dialog(onDismissRequest = { showVersionDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Porcelain,
                border = androidx.compose.foundation.BorderStroke(1.dp, Border),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Text(
                        text = "Bible Translation",
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        color = Espresso
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Choose your preferred Scripture translation",
                        fontFamily = BodyFontFamily,
                        fontSize = 12.sp,
                        color = Stone
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    BibleVersionRepository.availableVersions.forEach { version ->
                        val isSelected = selectedVersion.code == version.code
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Surface else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (isSelected) Terracotta.copy(alpha = 0.5f) else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    selectedVersion = version
                                    showVersionDialog = false
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = version.displayName,
                                            fontFamily = BodyFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (isSelected) Terracotta else Espresso
                                        )
                                        Text(
                                            text = "· ${version.fullTitle}",
                                            fontFamily = BodyFontFamily,
                                            fontSize = 12.5.sp,
                                            color = Espresso
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = version.note,
                                        fontFamily = BodyFontFamily,
                                        fontSize = 11.sp,
                                        color = StoneMuted
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Terracotta,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

// ─── Sub-composables ──────────────────────────────────────────────────────────

@Composable
private fun TestamentToggleButton(
    label: String,
    count: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(if (selected) Espresso else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontFamily = BodyFontFamily,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 12.5.sp,
                color = if (selected) Color.White else Espresso
            )
            Text(
                text = count,
                fontFamily = BodyFontFamily,
                fontSize = 10.sp,
                color = if (selected) Color.White.copy(alpha = 0.7f) else StoneMuted
            )
        }
    }
}

@Composable
private fun BookList(
    books: List<String>,
    onSelect: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        items(books) { book ->
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(book) }
                        .padding(vertical = 14.dp, horizontal = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = book,
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 17.sp,
                        color = Espresso
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowForwardIos,
                        contentDescription = null,
                        tint = Border,
                        modifier = Modifier.size(13.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(BorderSubtle)
                )
            }
        }
    }
}

@Composable
private fun ChapterGrid(
    book: String,
    chapters: List<Int>,
    onSelect: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Text(
                text = "${book.uppercase()} · CHAPTERS",
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                letterSpacing = 1.4.sp,
                color = StoneMuted,
                modifier = Modifier.padding(bottom = 14.dp)
            )
        }
        val rows = chapters.chunked(5)
        items(rows) { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { chapter ->
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Surface)
                            .border(1.dp, Border, RoundedCornerShape(12.dp))
                            .clickable { onSelect(chapter) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$chapter",
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = Espresso
                        )
                    }
                }
                repeat(5 - row.size) {
                    Spacer(modifier = Modifier.size(52.dp))
                }
            }
        }
    }
}

@Composable
private fun VerseList(
    verses: List<BibleVerseEntry>,
    versionName: String,
    onSelect: (BibleVerseEntry) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        items(verses, key = { it.reference }) { verse ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Surface)
                    .border(1.dp, Border, RoundedCornerShape(14.dp))
                    .clickable { onSelect(verse) }
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = verse.reference.uppercase(),
                                fontFamily = BodyFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.5.sp,
                                letterSpacing = 1.2.sp,
                                color = Terracotta
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(BorderSubtle)
                                    .padding(horizontal = 5.dp, vertical = 1.5.dp)
                            ) {
                                Text(
                                    text = versionName,
                                    fontFamily = BodyFontFamily,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Stone
                                )
                            }
                        }

                        if (verse.isDevotional) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Terracotta.copy(alpha = 0.10f))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "PROMISE",
                                    fontFamily = BodyFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.8.sp,
                                    color = Terracotta
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "\u201C${verse.text}\u201D",
                        fontFamily = DisplayFontFamily,
                        fontStyle = FontStyle.Italic,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = Espresso,
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptySearchState(query: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "📖",
                fontSize = 36.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No verses found for \"$query\"",
                fontFamily = DisplayFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                color = Espresso
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Try a book name (e.g. Psalms), citation (e.g. John 3:16), or a topic like \"peace\" or \"strength\".",
                fontFamily = BodyFontFamily,
                fontSize = 13.sp,
                color = Stone,
                modifier = Modifier.padding(horizontal = 24.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 19.sp
            )
        }
    }
}

// ─── Reference parsing helpers ────────────────────────────────────────────────

private fun parseChapter(reference: String, book: String): Int? {
    val remainder = reference.removePrefix(book).trim()
    return remainder.substringBefore(":").trim().toIntOrNull()
}

private fun isVerseInChapter(reference: String, book: String, chapter: Int): Boolean {
    if (!reference.startsWith(book)) return false
    val chapterParsed = parseChapter(reference, book) ?: return false
    return chapterParsed == chapter
}
