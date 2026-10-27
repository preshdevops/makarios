package com.makarios.app.ui.screens.bible
import com.makarios.app.ui.components.MakChip
import com.makarios.app.ui.components.MakSurface

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.makarios.app.data.BibleVersion
import com.makarios.app.data.BibleVersionRepository
import com.makarios.app.ui.theme.ProvideActiveLight
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.makarios.app.ui.components.ShareSheet
import com.makarios.app.ui.components.ShareContent
import androidx.compose.ui.unit.sp
import com.makarios.app.data.Affirmation
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.data.AffirmationTone
import com.makarios.app.data.bible.*
import com.makarios.app.ui.theme.Ink
import com.makarios.app.ui.theme.Light
import com.makarios.app.ui.theme.MakariosTypography
import com.makarios.app.ui.theme.SystemBarsController
import com.makarios.app.ui.theme.LocalLight
import java.util.Calendar

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BibleReaderScreen(
    bookName: String? = null,
    chapterNumber: Int? = null,
    initialVerse: Int? = null,
    onNavigateBack: () -> Unit,
    onOpenPicker: () -> Unit = {},
    pickMode: Boolean = false,
    onDeclareThis: (VerseSelection) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val activeLight = LocalLight.current
    SystemBarsController(activeLight)
    ProvideActiveLight(activeLight, "Bible reader") {}

    var currentBook by rememberSaveable { mutableStateOf(bookName ?: BibleReaderPrefs.getLastBook(context)) }
    var currentChapter by rememberSaveable { mutableIntStateOf(chapterNumber ?: BibleReaderPrefs.getLastChapter(context)) }
    var shareContentToOpen by remember { mutableStateOf<ShareContent?>(null) }

    // Text size steps: 0 = regular (18sp), 1 = large (21sp), 2 = extra large (24sp)
    var textSizeStep by rememberSaveable { mutableStateOf(1) }
    val baseFontSize = when (textSizeStep) {
        0 -> 18.sp
        1 -> 21.sp
        else -> 24.sp
    }
    val baseLineHeight = when (textSizeStep) {
        0 -> 30.sp
        1 -> 34.sp
        else -> 38.sp
    }

    var selectedVersion by rememberSaveable { mutableStateOf(BibleReaderPrefs.getVersion(context)) }
    var showVersionMenu by remember { mutableStateOf(false) }

    var chapterVerses by remember { mutableStateOf<List<BibleVerse>>(emptyList()) }
    val listState = rememberLazyListState()

    val coroutineScope = rememberCoroutineScope()
    val bibleState by BibleRepository.state.collectAsState()

    // Load one chapter from the selected on-device translation.
    LaunchedEffect(currentBook, currentChapter, selectedVersion) {
        BibleReaderPrefs.setLastPlace(context, currentBook, currentChapter)
        val rows = withContext(Dispatchers.IO) {
            BibleRepository.initialize(context)
            val web = BibleRepository.getChapter(currentBook, currentChapter).getOrElse { emptyList() }
            val version = BibleVersionRepository.availableVersions.firstOrNull { it.displayName == selectedVersion }
                ?: BibleVersionRepository.availableVersions.firstOrNull() ?: BibleVersion("web", "WEB", "World English Bible", true, "")
            BibleVersionRepository.getChapter(context, currentBook, currentChapter, version, web)
        }
        chapterVerses = rows
        if (rows.isNotEmpty()) {
            val savedIndex = BibleReaderPrefs.getScrollPosition(context, currentBook, currentChapter).coerceIn(0, rows.lastIndex)
            listState.scrollToItem(savedIndex)
        }
    }

    LaunchedEffect(currentBook, currentChapter) {
        var initialIndex = true
        snapshotFlow { listState.firstVisibleItemIndex }.collect { index ->
            if (initialIndex) initialIndex = false
            else BibleReaderPrefs.setScrollPosition(context, currentBook, currentChapter, index)
        }
    }
    // Selected verse range
    var selectedVerseStart by rememberSaveable { mutableStateOf(initialVerse) }
    var selectedVerseEnd by rememberSaveable { mutableStateOf(initialVerse) }

    val selectedRange: IntRange? = remember(selectedVerseStart, selectedVerseEnd) {
        val s = selectedVerseStart ?: return@remember null
        val e = selectedVerseEnd ?: s
        val minV = minOf(s, e)
        val maxV = maxOf(s, e)
        minV..minOf(maxV, minV + 7) // Cap at 8 verses
    }

    val selectedReference = remember(currentBook, currentChapter, selectedRange) {
        if (selectedRange == null) ""
        else if (selectedRange.first == selectedRange.last) "$currentBook $currentChapter:${selectedRange.first}"
        else "$currentBook $currentChapter:${selectedRange.first}-${selectedRange.last}"
    }

    val selectedText = remember(selectedRange, chapterVerses) {
        if (selectedRange == null) ""
        else {
            chapterVerses.filter { it.verse in selectedRange }
                .joinToString(" ") { it.text }
        }
    }

    // Scroll to initial verse if provided
    LaunchedEffect(chapterVerses, initialVerse) {
        if (initialVerse != null && chapterVerses.isNotEmpty()) {
            val idx = chapterVerses.indexOfFirst { it.verse == initialVerse }
            if (idx >= 0) {
                listState.animateScrollToItem(idx)
            }
        }
    }

    // Context menu / long press sheet
    var longPressedVerse by remember { mutableStateOf<BibleVerse?>(null) }

    com.makarios.app.ui.components.LightScreen(activeLight, "Bible reader", modifier) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            // Header
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
                        .background(activeLight.text.copy(alpha = 0.08f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = activeLight.text
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onOpenPicker() }) {
                    Text(
                        text = "$currentBook Chapter $currentChapter",
                        style = MakariosTypography.displaySmall.copy(fontSize = 22.sp),
                        color = activeLight.text
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Translation Chip
                    Box {
                        TextButton(onClick = { showVersionMenu = true }) {
                            Text(
                                text = selectedVersion,
                                style = MakariosTypography.labelMedium,
                                color = activeLight.text
                            )
                        }
                        DropdownMenu(
                            expanded = showVersionMenu,
                            onDismissRequest = { showVersionMenu = false }
                        ) {
                            BibleVersionRepository.availableVersions.forEach { version ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(version.displayName)
                                            Spacer(Modifier.width(8.dp))
                                            Text("On this phone", fontSize = 11.sp, color = activeLight.text.copy(alpha = .65f))
                                        }
                                    },
                                    onClick = {
                                        selectedVersion = version.displayName
                                        BibleReaderPrefs.setVersion(context, version.displayName)
                                        showVersionMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Aa text size button
                    IconButton(onClick = { textSizeStep = (textSizeStep + 1) % 3 }) {
                        Icon(
                            imageVector = Icons.Outlined.FormatSize,
                            contentDescription = "Text size",
                            tint = activeLight.text
                        )
                    }
                }
            }

            // Chapter Navigation Strip (Prev / Next)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentChapter > 1) {
                    TextButton(onClick = {
                        currentChapter -= 1
                        selectedVerseStart = null
                        selectedVerseEnd = null
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp), tint = activeLight.text.copy(alpha = 0.7f))
                        Spacer(Modifier.width(4.dp))
                        Text("Ch ${currentChapter - 1}", color = activeLight.text.copy(alpha = 0.7f), style = MakariosTypography.labelMedium)
                    }
                } else {
                    Spacer(Modifier.width(8.dp))
                }

                val bookMaxChapters = BibleRepository.books.firstOrNull { it.name.equals(currentBook, ignoreCase = true) }?.chapterCount ?: 50
                if (currentChapter < bookMaxChapters) {
                    TextButton(onClick = {
                        currentChapter += 1
                        selectedVerseStart = null
                        selectedVerseEnd = null
                    }) {
                        Text("Ch ${currentChapter + 1}", color = activeLight.text.copy(alpha = 0.7f), style = MakariosTypography.labelMedium)
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp), tint = activeLight.text.copy(alpha = 0.7f))
                    }
                }
            }

            if (bibleState is BibleState.Loading && chapterVerses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = activeLight.text)
                }
            } else if (bibleState is BibleState.Failed && chapterVerses.isEmpty()) {
                val err = (bibleState as BibleState.Failed).error
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Could not load Bible", style = MakariosTypography.headlineSmall, color = activeLight.text)
                    Spacer(Modifier.height(8.dp))
                    Text(err.localizedMessage ?: "Unknown error", style = MakariosTypography.bodyMedium, color = activeLight.text.copy(alpha = 0.7f))
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { coroutineScope.launch { BibleRepository.initialize(context) } }) {
                        Text("Retry")
                    }
                }
            } else if (chapterVerses.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Needs internet once to download, then it works offline",
                        style = MakariosTypography.titleMedium,
                        color = activeLight.text,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val web = BibleRepository.getChapter(currentBook, currentChapter).getOrElse { emptyList() }
                                val version = BibleVersionRepository.availableVersions.firstOrNull { it.displayName == selectedVersion }
                                    ?: BibleVersionRepository.availableVersions.firstOrNull() ?: BibleVersion("web", "WEB", "World English Bible", true, "")
                                chapterVerses = BibleVersionRepository.getChapter(context, currentBook, currentChapter, version, web)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = activeLight.buttonFill, contentColor = activeLight.buttonText)
                    ) {
                        Text("Retry")
                    }
                }
            } else {
                // Full Chapter LazyColumn
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                items(chapterVerses.distinctBy { it.verse }, key = { "reader-verse-${it.book}-${it.chapter}-${it.verse}" }) { verse ->
                    val isSelected = selectedRange != null && verse.verse in selectedRange

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) activeLight.highlight else Color.Transparent)
                            .combinedClickable(
                                onClick = {
                                    if (selectedVerseStart == null) {
                                        selectedVerseStart = verse.verse
                                        selectedVerseEnd = verse.verse
                                    } else if (selectedVerseEnd == selectedVerseStart) {
                                        selectedVerseEnd = verse.verse
                                    } else {
                                        selectedVerseStart = verse.verse
                                        selectedVerseEnd = verse.verse
                                    }
                                },
                                onLongClick = {
                                    longPressedVerse = verse
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                            .semantics {
                                contentDescription = "Verse ${verse.verse}, ${if (isSelected) "selected" else "not selected"}"
                            }
                    ) {
                        val annotatedText = buildAnnotatedString {
                            // Superscript verse number in anchor colour #8A5A14
                            withStyle(
                                SpanStyle(
                                    fontSize = 12.sp,
                                    baselineShift = BaselineShift.Superscript,
                                    color = activeLight.accent,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append("${verse.verse} ")
                            }

                            // Verse text in Newsreader body
                            withStyle(
                                SpanStyle(
                                    fontSize = baseFontSize,
                                    color = activeLight.text,
                                    fontStyle = FontStyle.Normal
                                )
                            ) {
                                append(ScriptureText.clean(verse.text))
                            }
                        }

                        Text(
                            text = annotatedText,
                            style = com.makarios.app.ui.theme.VerseTextStyle.copy(fontStyle = FontStyle.Normal, fontSize = baseFontSize, lineHeight = baseLineHeight)
                        )
                    }
                }
            }
            }
        }

        // Floating Ink Pill at Bottom for Active Selection
        AnimatedVisibility(
            visible = selectedRange != null && selectedText.isNotBlank(),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                // Above the bottom nav (56dp bar + hairline + gesture inset), never behind it
                .padding(bottom = 84.dp, start = 20.dp, end = 20.dp)
        ) {
            MakSurface(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Ink),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth().height(62.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedReference,
                            style = MakariosTypography.titleMedium.copy(fontSize = 15.sp),
                            color = activeLight.paper
                        )
                        val count = selectedRange?.let { it.last - it.first + 1 } ?: 0
                        if (count > 0) {
                            Text(
                                text = if (count == 1) "1 verse chosen" else "$count verses chosen",
                                style = MakariosTypography.labelSmall,
                                color = activeLight.paper.copy(alpha = 0.65f)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Copy Button
                        IconButton(
                            onClick = {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("Verse", ScriptureText.formatForCopy(selectedReference, selectedText)))
                                Toast.makeText(context, "Copied $selectedReference", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy", tint = activeLight.paper)
                        }

                        Spacer(Modifier.width(8.dp))

                        // Primary "Declare this" Button
                        Button(
                            onClick = {
                                val range = selectedRange ?: return@Button
                                val sel = VerseSelection(
                                    book = currentBook,
                                    chapter = currentChapter,
                                    fromVerse = range.first,
                                    toVerse = range.last,
                                    text = selectedText,
                                    reference = selectedReference,
                                    translation = selectedVersion
                                )
                                RecentVersesStore.addRecent(context, selectedReference)
                                onDeclareThis(sel)
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = activeLight.paper,
                                contentColor = Ink
                            )
                        ) {
                            Text(if (pickMode) "Use this verse" else "Declare this", style = MakariosTypography.labelLarge)
                        }
                    }
                }
            }
        }

        // Long Press Bottom Sheet
        longPressedVerse?.let { v ->
            val vRef = "${v.book} ${v.chapter}:${v.verse}"
            AlertDialog(
                onDismissRequest = { longPressedVerse = null },
                title = { Text(vRef, style = MakariosTypography.titleLarge) },
                text = { Text("“${v.text}”", style = MakariosTypography.bodyMedium.copy(fontStyle = FontStyle.Italic)) },
                confirmButton = {
                    TextButton(onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("Verse", ScriptureText.formatForCopy(vRef, v.text)))
                        Toast.makeText(context, "Copied $vRef", Toast.LENGTH_SHORT).show()
                        longPressedVerse = null
                    }) {
                        Text("Copy")
                    }
                },
                dismissButton = {
                    Row {
                        TextButton(onClick = {
                            shareContentToOpen = ShareContent(
                                declaration = v.text,
                                verse = v.text,
                                reference = vRef,
                                initialStyleId = "pairing",
                                initialLightName = activeLight.name,
                                source = "bible", contentType = "verse"
                            )
                            longPressedVerse = null
                        }) {
                            Text("Share")
                        }
                        TextButton(onClick = {
                            coroutineScope.launch {
                                val entity = com.makarios.app.data.local.KeptEntity(
                                    id = "kept-${System.currentTimeMillis()}",
                                    declaration = v.text,
                                    verseText = v.text,
                                    reference = vRef,
                                    lightName = "Midday",
                                    styleId = "pairing",
                                    createdAt = System.currentTimeMillis(),
                                    isFavorite = true,
                                    sourceType = "bible"
                                )
                                com.makarios.app.data.local.KeptRepository.save(entity)
                                Toast.makeText(context, "Saved $vRef", Toast.LENGTH_SHORT).show()
                            }
                            longPressedVerse = null
                        }) {
                            Text("Save verse")
                        }
                    }
                }
            )
        }
    }

    shareContentToOpen?.let { content ->
        ShareSheet(
            content = content,
            onDismiss = { shareContentToOpen = null }
        )
    }
}




