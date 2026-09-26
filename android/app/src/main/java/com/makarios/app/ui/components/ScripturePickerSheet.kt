package com.makarios.app.ui.components

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.data.BibleVerseEntry
import com.makarios.app.data.ScriptureDatabase
import com.makarios.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.InputStreamReader

/**
 * Editorial modal bottom sheet for browsing and selecting grounding scriptures
 * from either the curated promise canon or the complete 31,000+ Bible corpus.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScripturePickerSheet(
    onDismiss: () -> Unit,
    onVerseSelected: (reference: String, text: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Promises") }
    var allVerses by remember { mutableStateOf<List<BibleVerseEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val categories = listOf("Promises", "Psalms", "Gospels", "Romans", "Peace", "Strength", "Identity")

    // Load verses asynchronously
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
            } catch (e: Exception) {
                // Fallback to static database if verses.json is not present
                allVerses = ScriptureDatabase.verses.map {
                    BibleVerseEntry(it.reference, it.text, true)
                }
            } finally {
                isLoading = false
            }
        }
    }

    // Filtered results
    val displayedVerses = remember(searchQuery, selectedCategory, allVerses) {
        val q = searchQuery.trim().lowercase()
        val baseList = when (selectedCategory) {
            "Promises" -> allVerses.filter { it.isDevotional }
            "Psalms" -> allVerses.filter { it.reference.startsWith("Psalms") || it.reference.startsWith("Psalm") }
            "Gospels" -> allVerses.filter {
                it.reference.startsWith("Matthew") || it.reference.startsWith("Mark") ||
                        it.reference.startsWith("Luke") || it.reference.startsWith("John")
            }
            "Romans" -> allVerses.filter { it.reference.startsWith("Romans") }
            else -> allVerses
        }

        if (q.isBlank()) {
            baseList.take(60)
        } else {
            allVerses.filter {
                it.reference.lowercase().contains(q) || it.text.lowercase().contains(q)
            }.take(60)
        }
    }

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
        modifier = modifier.fillMaxHeight(0.85f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Scripture Library",
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp,
                        color = Espresso
                    )
                    Text(
                        text = "Browse or search God's living word",
                        fontFamily = BodyFontFamily,
                        fontSize = 12.sp,
                        color = Stone
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
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

            // Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Surface)
                    .border(1.dp, Border, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = StoneMuted, modifier = Modifier.size(18.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = TextStyle(
                            fontFamily = BodyFontFamily,
                            fontSize = 13.5.sp,
                            color = Espresso
                        ),
                        cursorBrush = SolidColor(Terracotta),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search by book, verse, or keyword (e.g. Psalm 23)…",
                                    fontFamily = BodyFontFamily,
                                    fontSize = 13.sp,
                                    color = StoneMuted
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category && searchQuery.isEmpty()
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (isSelected) Espresso else Surface)
                            .border(1.dp, if (isSelected) Color.Transparent else Border, RoundedCornerShape(18.dp))
                            .clickable {
                                selectedCategory = category
                                searchQuery = ""
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = category,
                            fontFamily = BodyFontFamily,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (isSelected) Color.White else Espresso
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Verse List
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Terracotta, strokeWidth = 2.5.dp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(displayedVerses) { verse ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Surface)
                                .border(1.dp, Border, RoundedCornerShape(14.dp))
                                .clickable {
                                    onVerseSelected(verse.reference, verse.text)
                                    onDismiss()
                                }
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = verse.reference.uppercase(),
                                        fontFamily = BodyFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        letterSpacing = 1.2.sp,
                                        color = Terracotta
                                    )
                                    if (verse.isDevotional) {
                                        Text(
                                            text = "PROMISE",
                                            fontFamily = BodyFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 9.sp,
                                            letterSpacing = 0.8.sp,
                                            color = Sage
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "“${verse.text}”",
                                    fontFamily = DisplayFontFamily,
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 14.5.sp,
                                    lineHeight = 21.sp,
                                    color = Espresso
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
