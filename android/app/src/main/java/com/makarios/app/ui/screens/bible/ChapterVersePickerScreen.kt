package com.makarios.app.ui.screens.bible

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.data.bible.*
import com.makarios.app.ui.theme.Ink
import com.makarios.app.ui.theme.MakariosTypography

@Composable
fun ChapterVersePickerScreen(
    bookName: String,
    initialChapter: Int = 1,
    onNavigateBack: () -> Unit,
    onVerseSelected: (VerseSelection) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedChapter by remember { mutableStateOf(initialChapter) }

    val bookInfo = remember(bookName) {
        BibleRepository.books.firstOrNull { it.name.equals(bookName, ignoreCase = true) }
            ?: BibleBook(bookName, Testament.NT, 28)
    }

    var chapterVerses by remember { mutableStateOf<List<BibleVerse>>(emptyList()) }

    LaunchedEffect(bookName, selectedChapter) {
        chapterVerses = BibleRepository.getChapter(context, bookName, selectedChapter)
    }

    var startVerse by remember { mutableStateOf<Int?>(null) }
    var endVerse by remember { mutableStateOf<Int?>(null) }
    var rangeMessage by remember { mutableStateOf<String?>(null) }

    // Resolve selected verse range (capped at 8 verses)
    val selectedRange: IntRange? = remember(startVerse, endVerse) {
        val s = startVerse ?: return@remember null
        val e = endVerse ?: s
        val minV = minOf(s, e)
        val maxV = maxOf(s, e)
        if (maxV - minV + 1 > 8) {
            rangeMessage = "Eight verses is the most that fits on one image"
            minV..(minV + 7)
        } else {
            rangeMessage = null
            minV..maxV
        }
    }

    val selectedVersesText = remember(selectedRange, chapterVerses) {
        if (selectedRange == null) ""
        else {
            chapterVerses.filter { it.verse in selectedRange }
                .joinToString(" ") { it.text }
        }
    }

    val formattedReference = remember(bookName, selectedChapter, selectedRange) {
        if (selectedRange == null) "$bookName $selectedChapter"
        else if (selectedRange.first == selectedRange.last) "$bookName $selectedChapter:${selectedRange.first}"
        else "$bookName $selectedChapter:${selectedRange.first}-${selectedRange.last}"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFBF9F5))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
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
                    .background(Ink.copy(alpha = 0.08f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Ink
                )
            }

            Text(
                text = bookName,
                style = MakariosTypography.displaySmall.copy(fontSize = 24.sp),
                color = Ink
            )

            Spacer(modifier = Modifier.size(40.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // Chapter Grid (5 columns)
            Text(
                text = "Chapter",
                style = MakariosTypography.labelMedium,
                color = Ink.copy(alpha = 0.6f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            val chapters = (1..bookInfo.chapterCount).toList()
            val chapterRows = chapters.chunked(5)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                chapterRows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { ch ->
                            val isSelected = ch == selectedChapter
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Ink else Color.White)
                                    .clickable {
                                        selectedChapter = ch
                                        startVerse = null
                                        endVerse = null
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ch.toString(),
                                    style = MakariosTypography.titleMedium,
                                    color = if (isSelected) Color(0xFFFFF4E4) else Ink
                                )
                            }
                        }
                        // Fill extra spaces if last row has < 5
                        for (i in 0 until (5 - row.size)) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Verse Grid (7 columns)
            Text(
                text = "Verse in chapter $selectedChapter",
                style = MakariosTypography.labelMedium,
                color = Ink.copy(alpha = 0.6f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            val versesCount = chapterVerses.size.coerceAtLeast(1)
            val verseNumbers = (1..versesCount).toList()
            val verseRows = verseNumbers.chunked(7)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                verseRows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        row.forEach { vNum ->
                            val isSelected = selectedRange != null && vNum in selectedRange
                            val isBound = vNum == selectedRange?.first || vNum == selectedRange?.last
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        when {
                                            isBound -> Ink
                                            isSelected -> Color(0x33B5532B)
                                            else -> Color.White
                                        }
                                    )
                                    .clickable {
                                        if (startVerse == null) {
                                            startVerse = vNum
                                            endVerse = vNum
                                        } else if (endVerse == startVerse) {
                                            endVerse = vNum
                                        } else {
                                            // Reset to new start
                                            startVerse = vNum
                                            endVerse = vNum
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = vNum.toString(),
                                    style = MakariosTypography.labelMedium,
                                    color = if (isBound) Color(0xFFFFF4E4) else Ink
                                )
                            }
                        }
                        for (i in 0 until (7 - row.size)) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            // Gentle range cap message
            rangeMessage?.let { msg ->
                Spacer(Modifier.height(12.dp))
                Text(
                    text = msg,
                    style = MakariosTypography.labelMedium,
                    color = Color(0xFF8A5A14),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Live Preview Card
            if (selectedVersesText.isNotBlank()) {
                Spacer(Modifier.height(20.dp))
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(
                            text = formattedReference,
                            style = MakariosTypography.labelLarge,
                            color = Color(0xFF8A5A14)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "“$selectedVersesText”",
                            style = MakariosTypography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                            color = Ink.copy(alpha = 0.88f)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "World English Bible (WEB)",
                            style = MakariosTypography.labelSmall,
                            color = Ink.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))
        }

        // Bottom Action Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Button(
                onClick = {
                    val range = selectedRange ?: return@Button
                    val sel = VerseSelection(
                        book = bookName,
                        chapter = selectedChapter,
                        fromVerse = range.first,
                        toVerse = range.last,
                        text = selectedVersesText,
                        reference = formattedReference
                    )
                    RecentVersesStore.addRecent(context, formattedReference)
                    onVerseSelected(sel)
                },
                enabled = selectedRange != null,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Color(0xFFFFF4E4)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = if (selectedRange != null) "Use $formattedReference" else "Select verse",
                    style = MakariosTypography.labelLarge
                )
            }
        }
    }
}
