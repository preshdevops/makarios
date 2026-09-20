package com.makarios.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.data.Affirmation
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.ui.theme.*

@Composable
fun SavedScreen(
    onNavigateToCreate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("All") }
    val savedList = remember { AffirmationRepository.savedAffirmationIds }
    val allSaved = AffirmationRepository.getSaved()

    val filteredSaved = remember(selectedCategory, savedList.size) {
        if (selectedCategory == "All") {
            AffirmationRepository.getSaved()
        } else {
            AffirmationRepository.getSaved().filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Porcelain
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp, bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Makarios",
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp,
                        color = Espresso
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(PorcelainWarm),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "M",
                            fontFamily = DisplayFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Espresso
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Saved Declarations",
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp,
                    lineHeight = 32.sp,
                    color = Espresso
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your personal sanctuary of bookmarked sovereign truths.",
                    color = Stone,
                    fontFamily = BodyFontFamily,
                    fontSize = 15.sp
                )
            }

            // Collection / Category Filter Pills
            val categories = listOf("All") + AffirmationRepository.categories
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                categories.forEach { cat ->
                    val isSelected = cat.equals(selectedCategory, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .then(
                                if (isSelected) {
                                    Modifier.background(Terracotta)
                                } else {
                                    Modifier
                                        .background(Surface)
                                        .border(1.dp, Border, RoundedCornerShape(20.dp))
                                }
                            )
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) Surface else Espresso,
                            fontFamily = BodyFontFamily,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Saved Declarations List or Empty State
            if (filteredSaved.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(PorcelainWarm),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookmarkBorder,
                                contentDescription = null,
                                tint = StoneMuted,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Text(
                            text = "No saved declarations here",
                            fontFamily = DisplayFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp,
                            color = Espresso
                        )
                        Text(
                            text = "Tap the bookmark icon on any truth card on the Home screen to keep it anchored in your collection.",
                            fontFamily = BodyFontFamily,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Center,
                            color = StoneMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredSaved, key = { it.id }) { affirmation ->
                        SavedCardItem(
                            affirmation = affirmation,
                            onRemove = { AffirmationRepository.toggleSave(affirmation.id) },
                            onCreateWallpaper = { onNavigateToCreate(affirmation.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedCardItem(
    affirmation: Affirmation,
    onRemove: () -> Unit,
    onCreateWallpaper: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Espresso.copy(alpha = 0.06f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Column {
            // Category & Reference header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${affirmation.category.uppercase()} · ${affirmation.reference}",
                    color = Terracotta,
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    letterSpacing = 1.4.sp
                )

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove from saved",
                        tint = StoneMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Declaration
            Text(
                text = "“${affirmation.declaration}”",
                fontFamily = DisplayFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                lineHeight = 25.sp,
                color = Espresso
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Scripture quote
            Text(
                text = "“${affirmation.scriptureText}”",
                fontFamily = DisplayFontFamily,
                fontStyle = FontStyle.Italic,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Stone
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Footer action: Create wallpaper shortcut
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onCreateWallpaper)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Terracotta,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Create Wallpaper",
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Terracotta
                    )
                }
            }
        }
    }
}
