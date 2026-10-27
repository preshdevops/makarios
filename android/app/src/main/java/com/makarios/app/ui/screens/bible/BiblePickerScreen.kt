package com.makarios.app.ui.screens.bible

import androidx.compose.foundation.background
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.data.bible.*
import com.makarios.app.ui.components.*
import com.makarios.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** BibleNavV2: plain rows, in-place chapter numbers, no cards/tiles. */
@Composable
fun BiblePickerScreen(
    onNavigateBack: () -> Unit,
    onSelectBookChapter: (book: String, chapter: Int) -> Unit,
    onVerseSelected: (VerseSelection) -> Unit,
    initialQuery: String = "",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val light = LocalLight.current
    var query by remember(initialQuery) { mutableStateOf(initialQuery) }
    var testament by remember { mutableStateOf(Testament.OT) }
    var expanded by remember { mutableStateOf<String?>(null) }
    var searchResults by remember { mutableStateOf<List<BibleVerse>>(emptyList()) }
    val lastBook = remember { BibleReaderPrefs.getLastBook(context) }
    val lastChapter = remember { BibleReaderPrefs.getLastChapter(context) }

    LaunchedEffect(Unit) { BibleRepository.initialize(context) }
    LaunchedEffect(query) {
        val q = query.trim()
        if (q.isBlank()) { searchResults = emptyList(); return@LaunchedEffect }
        delay(200)
        searchResults = withContext(Dispatchers.IO) {
            BibleRepository.initialize(context)
            val parsed = BibleReferenceParser.parse(q)
            if (parsed != null) listOfNotNull(BibleRepository.getVerse(context, "${parsed.book} ${parsed.chapter}:${parsed.fromVerse ?: 1}"))
            else BibleRepository.searchVerses(context, q, limit = 20)
        }
    }

    LightScreen(light, "Bible picker", modifier) {
        Column(Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 20.dp)) {
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = light.text) }
                Text("Bible", style = MakariosTypography.displaySmall, color = light.text)
            }
            Row(Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(12.dp)).background(light.surfaceTint).padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, "Search", tint = light.textMuted)
                Spacer(Modifier.width(10.dp))
                BasicTextField(query, { query = it }, Modifier.weight(1f), singleLine = true, textStyle = MakariosTypography.bodyMedium.copy(color = light.text), decorationBox = { inner ->
                    if (query.isEmpty()) Text("Try John 3:16, or a word like fear", style = MakariosTypography.bodyMedium, color = light.textMuted)
                    inner()
                })
                if (query.isNotBlank()) IconButton(onClick = { query = "" }) { Icon(Icons.Default.Close, "Clear", tint = light.textMuted) }
            }
            Spacer(Modifier.height(8.dp))
            if (query.isNotBlank()) {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(searchResults.distinctBy { it.reference }, key = { it.reference }) { verse ->
                        MakRow(onClick = {
                            RecentVersesStore.addRecent(context, verse.reference)
                            onVerseSelected(VerseSelection(verse.book, verse.chapter, verse.verse, verse.verse, verse.text, verse.reference))
                        }) {
                            Column(Modifier.weight(1f)) {
                                Text(verse.reference, style = MakariosTypography.labelLarge, color = light.accent)
                                Text(verse.text, style = VerseTextStyle, color = light.text)
                            }
                        }
                    }
                    if (searchResults.isEmpty()) item { Text("No verse has those words. Try one word, or a reference like Psalm 23.", style = MakariosTypography.bodyMedium, color = light.textMuted, modifier = Modifier.padding(24.dp)) }
                }
            } else {
                MakRow(onClick = { onSelectBookChapter(lastBook, lastChapter) }) {
                    Column(Modifier.weight(1f)) {
                        Text("Continue reading", style = MakariosTypography.labelLarge, color = light.textMuted)
                        Text("$lastBook $lastChapter", style = MakariosTypography.headlineLarge.copy(fontSize = 21.sp), color = light.text)
                    }
                    Icon(Icons.Outlined.ChevronRight, null, tint = light.textMuted)
                }
                MakSegmented(listOf("Old Testament", "New Testament"), if (testament == Testament.OT) 0 else 1, { testament = if (it == 0) Testament.OT else Testament.NT })
                val books = BibleRepository.books.filter { it.testament == testament }
                LazyColumn(Modifier.fillMaxSize()) {
                    items(books.distinctBy { it.name }, key = { it.name }) { book ->
                        Column {
                            MakRow(onClick = { expanded = if (expanded == book.name) null else book.name }) {
                                Text(book.name, style = MakariosTypography.headlineLarge.copy(fontSize = 21.sp), color = light.text, modifier = Modifier.weight(1f))
                                Icon(Icons.Outlined.ChevronRight, "Open ${book.name}", tint = light.textMuted, modifier = Modifier.size(18.dp))
                            }
                            if (expanded == book.name) {
                                Column(Modifier.padding(vertical = 8.dp)) {
                                    (1..book.chapterCount).chunked(6).forEach { rowNums ->
                                        Row(Modifier.fillMaxWidth()) {
                                            rowNums.forEach { num ->
                                                Box(Modifier.size(44.dp).clickable { onSelectBookChapter(book.name, num) }, contentAlignment = Alignment.Center) {
                                                    if (book.name == lastBook && num == lastChapter) Box(Modifier.size(36.dp).clip(CircleShape).background(light.accent), contentAlignment = Alignment.Center) {
                                                        Text("$num", style = MakariosTypography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = light.onAccent)
                                                    } else Text("$num", style = MakariosTypography.bodyMedium.copy(fontSize = 17.sp), color = light.text)
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
    }
}
