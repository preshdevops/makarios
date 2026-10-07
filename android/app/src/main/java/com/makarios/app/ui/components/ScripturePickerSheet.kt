package com.makarios.app.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.makarios.app.data.BibleVerseEntry
import com.makarios.app.data.BibleVersion
import com.makarios.app.data.BibleVersionRepository
import com.makarios.app.data.ScriptureDatabase
import com.makarios.app.data.bible.BibleReaderPrefs
import com.makarios.app.data.bible.ReaderTheme
import com.makarios.app.data.bible.ScriptureText
import com.makarios.app.ui.theme.*
import com.makarios.app.util.ShareHelper
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
 * Full-Bible browser and reader bottom sheet.
 *
 * Capabilities:
 *  - Flowing chapter reader with Cormorant serif and 1.6 line height
 *  - Small superscript verse numbers and indented poetry
 *  - Verse multi-selection with floating action bar (Use as match, Copy, Share image, Highlight)
 *  - Reader settings (font size, light/sepia/dark themes, verse numbers toggle)
 *  - Bible translation switching (WEB offline, KJV, BBE, ASV online)
 *  - Global keyword and citation search
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

    // ── Reader Preferences ───────────────────────────────────────────────────
    var readerFontSize by remember { mutableFloatStateOf(BibleReaderPrefs.getFontSize(context)) }
    var readerTheme by remember { mutableStateOf(BibleReaderPrefs.getTheme(context)) }
    var showVerseNumbers by remember { mutableStateOf(BibleReaderPrefs.getShowVerseNumbers(context)) }
    var highlights by remember { mutableStateOf(BibleReaderPrefs.getHighlights(context)) }

    var showSettingsDialog by remember { mutableStateOf(false) }

    // ── Corpus Data ──────────────────────────────────────────────────────────
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

    // Selected verse numbers within currently viewed chapter
    var selectedVerseNumbers by remember(selectedBook, selectedChapter) { mutableStateOf<Set<Int>>(emptySet()) }

    // Hand-curated promise set for search results badge
    val promiseReferences = remember {
        ScriptureDatabase.verses.map { it.reference }.toSet()
    }

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
                                text = ScriptureText.clean(obj.getString("t")),
                                isDevotional = obj.optInt("d", 0) == 1
                            )
                        )
                    }
                    allVerses = list
                }
            } catch (_: Exception) {
                allVerses = ScriptureDatabase.verses.map {
                    BibleVerseEntry(it.reference, ScriptureText.clean(it.text), true)
                }
            } finally {
                isLoading = false
            }
        }
    }

    val canonBooks = if (testament == "OT") OLD_TESTAMENT_BOOKS else NEW_TESTAMENT_BOOKS

    // Search results across entire corpus
    val searchResults = remember(searchQuery, allVerses) {
        if (searchQuery.isBlank()) emptyList()
        else {
            val q = searchQuery.trim().lowercase()
            allVerses.filter {
                it.reference.lowercase().contains(q) || it.text.lowercase().contains(q)
            }.take(80)
        }
    }

    // Chapters available in selected book
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

    // Verses in selected chapter
    val versesInChapter = remember(selectedBook, selectedChapter, allVerses) {
        if (selectedBook.isBlank() || selectedChapter == 0) emptyList()
        else allVerses.filter { isVerseInChapter(it.reference, selectedBook, selectedChapter) }
    }

    // ── Handlers ─────────────────────────────────────────────────────────────
    fun goBack() {
        when (browseLevel) {
            BrowseLevel.VERSES -> {
                browseLevel = BrowseLevel.CHAPTERS
                selectedVerseNumbers = emptySet()
            }
            BrowseLevel.CHAPTERS -> {
                browseLevel = BrowseLevel.BOOKS
                selectedBook = ""
            }
            BrowseLevel.BOOKS -> onDismiss()
        }
    }

    fun handleSingleVerseSelect(verse: BibleVerseEntry) {
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

    // Formats reference range for multiple selected verses (e.g. "John 3:16–17")
    fun formatSelectedReference(): String {
        if (selectedVerseNumbers.isEmpty()) return ""
        val sorted = selectedVerseNumbers.sorted()
        val isContiguous = sorted.last() - sorted.first() == sorted.size - 1
        return if (sorted.size == 1) {
            "$selectedBook $selectedChapter:${sorted.first()}"
        } else if (isContiguous) {
            "$selectedBook $selectedChapter:${sorted.first()}–${sorted.last()}"
        } else {
            "$selectedBook $selectedChapter:" + sorted.joinToString(", ")
        }
    }

    fun getSelectedVersesText(): String {
        val sorted = selectedVerseNumbers.sorted()
        return versesInChapter
            .filter { parseVerseNumber(it.reference, selectedBook, selectedChapter) in sorted }
            .joinToString(" ") { it.text }
    }

    // Sheet container color follows reader theme when on verses level
    val containerBg = if (browseLevel == BrowseLevel.VERSES && searchQuery.isBlank()) {
        readerTheme.background
    } else {
        Porcelain
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = containerBg,
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
        modifier = modifier.fillMaxHeight(0.94f)
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
                    if (browseLevel != BrowseLevel.BOOKS || searchQuery.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
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
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = when {
                                    searchQuery.isNotBlank() -> "Search results"
                                    browseLevel == BrowseLevel.VERSES -> "$selectedBook $selectedChapter"
                                    browseLevel == BrowseLevel.CHAPTERS -> selectedBook
                                    else -> "Holy Bible"
                                },
                                fontFamily = DisplayFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 22.sp,
                                color = if (browseLevel == BrowseLevel.VERSES && searchQuery.isBlank()) readerTheme.text else Espresso
                            )

                            // Translation badge shown once in header
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(BorderSubtle)
                                    .clickable { showVersionDialog = true }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = selectedVersion.displayName,
                                        fontFamily = BodyFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = Olive
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Change translation",
                                        tint = Olive,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = when {
                                searchQuery.isNotBlank() -> "${searchResults.size} verse${if (searchResults.size != 1) "s" else ""} found"
                                browseLevel == BrowseLevel.VERSES -> "${versesInChapter.size} verses · ${selectedVersion.fullTitle}"
                                browseLevel == BrowseLevel.CHAPTERS -> "${chaptersInBook.size} chapters"
                                else -> if (testament == "OT") "Old Testament · ${OLD_TESTAMENT_BOOKS.size} books"
                                else "New Testament · ${NEW_TESTAMENT_BOOKS.size} books"
                            },
                            fontFamily = BodyFontFamily,
                            fontSize = 12.sp,
                            color = if (browseLevel == BrowseLevel.VERSES && searchQuery.isBlank()) readerTheme.verseNumber else Stone
                        )
                    }

                    // Reader Settings Button (shown when viewing a chapter)
                    if (browseLevel == BrowseLevel.VERSES && searchQuery.isBlank()) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Surface)
                                .border(1.dp, Border, CircleShape)
                                .clickable { showSettingsDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatSize,
                                contentDescription = "Reader settings",
                                tint = Espresso,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // Close button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Surface)
                            .border(1.dp, Border, CircleShape)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Espresso, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ── Search bar ────────────────────────────────────────────────
                if (browseLevel != BrowseLevel.VERSES || searchQuery.isNotBlank()) {
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
                                cursorBrush = SolidColor(Olive),
                                modifier = Modifier.weight(1f),
                                decorationBox = { innerTextField ->
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Search verse, citation, or topic…",
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
                }

                // ── Testament toggle ──────────────────────────────────────────
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
                        CircularProgressIndicator(color = Olive, strokeWidth = 2.5.dp)
                    }
                } else {
                    // Search results
                    if (searchQuery.isNotBlank()) {
                        if (searchResults.isEmpty()) {
                            EmptySearchState(query = searchQuery)
                        } else {
                            SearchResultList(
                                verses = searchResults,
                                promiseReferences = promiseReferences,
                                onSelect = { handleSingleVerseSelect(it) }
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

                    // Verses level: Flowing Paragraph Chapter Reader
                    if (browseLevel == BrowseLevel.VERSES) {
                        ChapterReader(
                            book = selectedBook,
                            chapter = selectedChapter,
                            verses = versesInChapter,
                            selectedVerseNumbers = selectedVerseNumbers,
                            highlights = highlights,
                            readerTheme = readerTheme,
                            fontSize = readerFontSize,
                            showVerseNumbers = showVerseNumbers,
                            onToggleVerseSelect = { verseNum ->
                                selectedVerseNumbers = if (selectedVerseNumbers.contains(verseNum)) {
                                    selectedVerseNumbers - verseNum
                                } else {
                                    selectedVerseNumbers + verseNum
                                }
                            }
                        )
                    }
                }
            }

            // ── Floating Action Bar (when >= 1 verses selected) ───────────────
            AnimatedVisibility(
                visible = selectedVerseNumbers.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 18.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Espresso,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Use as match (Primary)
                        Button(
                            onClick = {
                                val ref = formatSelectedReference()
                                val text = getSelectedVersesText()
                                onVerseSelected(ref, text)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Olive,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "Use as match",
                                fontFamily = BodyFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }

                        // Copy
                        IconButton(
                            onClick = {
                                val ref = formatSelectedReference()
                                val text = getSelectedVersesText()
                                val formatted = ScriptureText.formatForCopy(ref, text, selectedVersion.displayName)
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("Scripture", formatted))
                                Toast.makeText(context, "Copied $ref", Toast.LENGTH_SHORT).show()
                                selectedVerseNumbers = emptySet()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = Color.White,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // Share image
                        IconButton(
                            onClick = {
                                val ref = formatSelectedReference()
                                val text = getSelectedVersesText()
                                val aff = com.makarios.app.data.Affirmation(
                                    id = "shared_${System.currentTimeMillis()}",
                                    declaration = text,
                                    scriptureText = text,
                                    reference = ref,
                                    category = selectedBook
                                )
                                ShareHelper.shareAffirmationGraphic(context, aff)
                                selectedVerseNumbers = emptySet()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share image",
                                tint = Color.White,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // Highlight
                        IconButton(
                            onClick = {
                                val current = highlights.toMutableSet()
                                val chapterVerses = selectedVerseNumbers.map { "$selectedBook $selectedChapter:$it" }
                                val anyHighlighted = chapterVerses.any { current.contains(it) }

                                chapterVerses.forEach { ref ->
                                    BibleReaderPrefs.toggleHighlight(context, ref)
                                }
                                highlights = BibleReaderPrefs.getHighlights(context)
                                Toast.makeText(
                                    context,
                                    if (anyHighlighted) "Removed highlight" else "Highlighted",
                                    Toast.LENGTH_SHORT
                                ).show()
                                selectedVerseNumbers = emptySet()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Highlight,
                                contentDescription = "Highlight",
                                tint = Color.White,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // Clear selection
                        IconButton(onClick = { selectedVerseNumbers = emptySet() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel selection",
                                tint = StoneMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Translating overlay
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
                            CircularProgressIndicator(color = Olive, strokeWidth = 2.5.dp, modifier = Modifier.size(24.dp))
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
                        text = "Bible translation",
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
                                .background(if (isSelected) OliveLight else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (isSelected) Olive else Color.Transparent,
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
                                            color = if (isSelected) Olive else Espresso
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
                                        tint = Olive,
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

    // ── Reader Settings Dialog ─────────────────────────────────────────────────
    if (showSettingsDialog) {
        Dialog(onDismissRequest = { showSettingsDialog = false }) {
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
                        text = "Reader settings",
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        color = Espresso
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    // Font size selector
                    Text(
                        text = "Text size",
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Stone
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(15f to "Small", 18f to "Default", 21f to "Large", 24f to "Extra").forEach { (size, label) ->
                            val isSelected = readerFontSize == size
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Olive else Surface)
                                    .border(1.dp, if (isSelected) Olive else Border, RoundedCornerShape(10.dp))
                                    .clickable {
                                        readerFontSize = size
                                        BibleReaderPrefs.setFontSize(context, size)
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontFamily = BodyFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else Espresso
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Reading Theme
                    Text(
                        text = "Reading theme",
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Stone
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReaderTheme.values().forEach { theme ->
                            val isSelected = readerTheme == theme
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(theme.background)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) Olive else Border,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        readerTheme = theme
                                        BibleReaderPrefs.setTheme(context, theme)
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = theme.title,
                                    fontFamily = BodyFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = theme.text
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Verse numbers toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Verse numbers",
                                fontFamily = BodyFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = Espresso
                            )
                            Text(
                                text = "Show superscript numbers in text",
                                fontFamily = BodyFontFamily,
                                fontSize = 11.sp,
                                color = StoneMuted
                            )
                        }
                        Switch(
                            checked = showVerseNumbers,
                            onCheckedChange = {
                                showVerseNumbers = it
                                BibleReaderPrefs.setShowVerseNumbers(context, it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Olive
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { showSettingsDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Espresso),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Done",
                            fontFamily = BodyFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// ─── Flowing Chapter Reader ──────────────────────────────────────────────────

@Composable
private fun ChapterReader(
    book: String,
    chapter: Int,
    verses: List<BibleVerseEntry>,
    selectedVerseNumbers: Set<Int>,
    highlights: Set<String>,
    readerTheme: ReaderTheme,
    fontSize: Float,
    showVerseNumbers: Boolean,
    onToggleVerseSelect: (Int) -> Unit
) {
    // Check if poetic book
    val isPoetry = book in setOf("Psalms", "Proverbs", "Song of Solomon", "Lamentations") ||
            (book == "Job" && chapter in 3..41)

    // For prose, group verses into paragraphs of ~4 verses
    val paragraphs = remember(verses, isPoetry) {
        if (isPoetry) verses.map { listOf(it) }
        else verses.chunked(4)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        items(paragraphs) { paragraphVerses ->
            // Paragraph layout state for tap detection
            var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

            val annotatedText = buildAnnotatedString {
                paragraphVerses.forEachIndexed { idx, verse ->
                    val verseNum = parseVerseNumber(verse.reference, book, chapter) ?: (idx + 1)
                    val fullRef = "$book $chapter:$verseNum"
                    val isSelected = selectedVerseNumbers.contains(verseNum)
                    val isHighlighted = highlights.contains(fullRef)

                    val startOffset = length

                    // Add superscript verse number
                    if (showVerseNumbers) {
                        pushStyle(
                            SpanStyle(
                                color = readerTheme.verseNumber,
                                fontSize = (fontSize * 0.65f).sp,
                                baselineShift = BaselineShift(0.35f),
                                fontWeight = FontWeight.Normal
                            )
                        )
                        append("$verseNum ")
                        pop()
                    }

                    // Apply selection / highlight styles to verse text
                    val verseStart = length
                    append(verse.text)
                    val verseEnd = length

                    if (isSelected) {
                        addStyle(
                            SpanStyle(
                                textDecoration = TextDecoration.Underline,
                                background = OliveLight
                            ),
                            startOffset,
                            verseEnd
                        )
                    } else if (isHighlighted) {
                        addStyle(
                            SpanStyle(background = readerTheme.highlightColor),
                            startOffset,
                            verseEnd
                        )
                    }

                    // Attach verse number tag for tap detection
                    addStringAnnotation(
                        tag = "VERSE",
                        annotation = "$verseNum",
                        start = startOffset,
                        end = verseEnd
                    )

                    // Space between verses in prose
                    if (!isPoetry && idx < paragraphVerses.size - 1) {
                        append(" ")
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = if (isPoetry) 3.dp else 8.dp,
                        horizontal = if (isPoetry) 14.dp else 0.dp
                    )
            ) {
                Text(
                    text = annotatedText,
                    fontFamily = DisplayFontFamily,
                    fontSize = fontSize.sp,
                    lineHeight = (fontSize * 1.6f).sp,
                    color = readerTheme.text,
                    style = TextStyle(
                        textIndent = if (isPoetry) TextIndent(firstLine = 0.sp, restLine = 16.sp)
                        else TextIndent.None
                    ),
                    onTextLayout = { layoutResult = it },
                    modifier = Modifier.pointerInput(paragraphVerses) {
                        detectTapGestures { offset ->
                            val layout = layoutResult ?: return@detectTapGestures
                            val position = layout.getOffsetForPosition(offset)
                            annotatedText.getStringAnnotations("VERSE", position, position)
                                .firstOrNull()?.let { annotation ->
                                    annotation.item.toIntOrNull()?.let { verseNum ->
                                        onToggleVerseSelect(verseNum)
                                    }
                                }
                        }
                    }
                )
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
    LazyVerticalGrid(
        columns = GridCells.Adaptive(52.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(chapters) { chapter ->
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
    }
}

@Composable
private fun SearchResultList(
    verses: List<BibleVerseEntry>,
    promiseReferences: Set<String>,
    onSelect: (BibleVerseEntry) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        items(verses, key = { it.reference }) { verse ->
            val isPromise = verse.reference in promiseReferences

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
                        Text(
                            text = verse.reference,
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Olive
                        )

                        if (isPromise) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(OliveLight)
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Promise",
                                    fontFamily = BodyFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 10.sp,
                                    color = Olive
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = verse.text,
                        fontFamily = DisplayFontFamily,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = Espresso,
                        maxLines = 4,
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
            Text(text = "📖", fontSize = 36.sp)
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
                text = "Try a book name (e.g. Psalms), citation (e.g. John 3:16), or a topic like peace or strength.",
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

private fun parseVerseNumber(reference: String, book: String, chapter: Int): Int? {
    val remainder = reference.removePrefix(book).trim()
    if (!remainder.startsWith("$chapter:")) return null
    val verseStr = remainder.substringAfter(":").trim()
    return verseStr.takeWhile { it.isDigit() }.toIntOrNull()
}

private fun isVerseInChapter(reference: String, book: String, chapter: Int): Boolean {
    if (!reference.startsWith(book)) return false
    val chapterParsed = parseChapter(reference, book) ?: return false
    return chapterParsed == chapter
}
