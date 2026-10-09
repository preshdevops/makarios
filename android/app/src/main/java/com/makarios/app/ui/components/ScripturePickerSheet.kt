package com.makarios.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.ui.theme.Ink
import com.makarios.app.ui.theme.Light
import com.makarios.app.ui.theme.MakariosTypography
import com.makarios.app.ui.theme.lightBackground
import java.util.Calendar

data class Verse(val number: Int, val text: String, val isPoetry: Boolean = false)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleReaderSheet(
    onDismiss: () -> Unit,
    verses: List<Verse> = listOf(
        Verse(1, "In the beginning, God created the heavens and the earth."),
        Verse(2, "The earth was without form and void, and darkness was over the face of the deep. And the Spirit of God was hovering over the face of the waters.")
    ),
    bookAndChapter: String = "Genesis 1",
    translation: String = "WEB"
) {
    var selectedLight by remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        mutableStateOf(if (hour in 6..18) Light.Midday else Light.Night)
    }
    
    var selectedVerses by remember { mutableStateOf(setOf<Int>()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = androidx.compose.ui.graphics.Color.Transparent, // Handle in box
        dragHandle = null
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .lightBackground(selectedLight)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$bookAndChapter ?" $translation",
                        style = MakariosTypography.displaySmall,
                        color = selectedLight.text
                    )
                    // Settings could go here
                }
                
                // Content
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 24.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    item {
                        // Flowing paragraphs
                        val annotatedString = buildAnnotatedString {
                            verses.forEach { verse ->
                                val isSelected = selectedVerses.contains(verse.number)
                                
                                // Superscript verse number
                                withStyle(
                                    style = SpanStyle(
                                        fontSize = 10.sp,
                                        baselineShift = BaselineShift.Superscript,
                                        color = selectedLight.text.copy(alpha = 0.5f)
                                    )
                                ) {
                                    append("${verse.number} ")
                                }
                                
                                // Verse text
                                withStyle(
                                    style = SpanStyle(
                                        textDecoration = if (isSelected) TextDecoration.Underline else TextDecoration.None
                                    )
                                ) {
                                    append(verse.text + " ")
                                }
                            }
                        }
                        
                        Text(
                            text = annotatedString,
                            style = MakariosTypography.bodyMedium,
                            color = selectedLight.text,
                            modifier = Modifier.clickable {
                                // Real implementation would determine which verse was tapped
                                // For now, mock selecting verse 1
                                selectedVerses = if (selectedVerses.contains(1)) emptySet() else setOf(1)
                            }
                        )
                    }
                }
                
                // Bottom action bar (if text selected)
                if (selectedVerses.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(selectedLight.text)
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val actionColor = if (selectedLight.isLight) selectedLight.bottom else selectedLight.top
                        Text("Use as match", color = actionColor, style = MakariosTypography.labelLarge)
                        Text("Copy", color = actionColor, style = MakariosTypography.labelLarge)
                        Text("Share image", color = actionColor, style = MakariosTypography.labelLarge)
                        Text("Highlight", color = actionColor, style = MakariosTypography.labelLarge)
                    }
                }
            }
        }
    }
}
