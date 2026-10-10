package com.makarios.app.ui.screens

import android.app.WallpaperManager
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.makarios.app.data.Affirmation
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.data.PhotoEntry
import com.makarios.app.data.PhotoLibrary
import com.makarios.app.data.PhotoRole
import com.makarios.app.ui.components.AffirmationDetailDialog
import com.makarios.app.ui.components.Pairing
import com.makarios.app.ui.theme.*
import com.makarios.app.util.ShareHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

@Composable
fun SavedScreen(
    onNavigateToCreate: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf("Declarations") }
    var selectedDetailAffirmation by remember { mutableStateOf<Affirmation?>(null) }
    var selectedWallpaperPreview by remember { mutableStateOf<PhotoEntry?>(null) }

    // Live reactive lists
    val savedAffirmations = remember(AffirmationRepository.savedAffirmationIds.size) {
        AffirmationRepository.getSaved()
    }
    val personalAffirmations = remember(AffirmationRepository.personalAffirmations.size) {
        AffirmationRepository.personalAffirmations.toList()
    }

    // Curated Wallpapers from PhotoLibrary
    val wallpapers = remember {
        PhotoLibrary.entries.filter {
            it.role in listOf(
                PhotoRole.WALLPAPER_1,
                PhotoRole.WALLPAPER_2,
                PhotoRole.WALLPAPER_3,
                PhotoRole.WALLPAPER_4,
                PhotoRole.WALLPAPER_5,
                PhotoRole.WALLPAPER_6
            )
        }.ifEmpty {
            PhotoLibrary.entries.take(6)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFBF9F5))
            .padding(top = 48.dp)
    ) {
        Text(
            text = "Kept",
            style = MakariosTypography.displayLarge,
            color = Ink,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Tabs
        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            KeptTab("Declarations", savedAffirmations.size, selectedTab == "Declarations") {
                selectedTab = "Declarations"
            }
            KeptTab("Wallpapers", wallpapers.size, selectedTab == "Wallpapers") {
                selectedTab = "Wallpapers"
            }
            KeptTab("Mine", personalAffirmations.size, selectedTab == "Mine") {
                selectedTab = "Mine"
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        when (selectedTab) {
            "Declarations" -> {
                if (savedAffirmations.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Nothing kept yet",
                                style = MakariosTypography.displaySmall,
                                color = Ink
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap the heart icon on any declaration in Today or Explore to keep it here for contemplation.",
                                style = MakariosTypography.bodyMedium,
                                color = Ink.copy(alpha = 0.6f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(savedAffirmations, key = { it.id }) { affirmation ->
                            val topicLight = TopicBands.find { it.name.equals(affirmation.category, ignoreCase = true) }?.light ?: Light.Grove
                            KeptCard(
                                affirmation = affirmation,
                                light = topicLight,
                                onClick = { selectedDetailAffirmation = affirmation },
                                onShare = {
                                    ShareHelper.shareGeneric(
                                        context = context,
                                        affirmation = affirmation,
                                        light = topicLight
                                    )
                                },
                                onUnkeep = {
                                    AffirmationRepository.unsave(affirmation.id)
                                    Toast.makeText(context, "Removed from Kept", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }

            "Wallpapers" -> {
                // 2-Column Gallery Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(wallpapers) { entry ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { selectedWallpaperPreview = entry }
                        ) {
                            AsyncImage(
                                model = entry.url(width = 600),
                                contentDescription = entry.description,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                colorFilter = WarmPhotoGrade
                            )

                            // Scrim overlay with title
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            0.5f to Color.Transparent,
                                            1.0f to Color.Black.copy(alpha = 0.7f)
                                        )
                                    )
                                    .padding(12.dp),
                                contentAlignment = Alignment.BottomStart
                            ) {
                                Text(
                                    text = entry.description,
                                    style = MakariosTypography.labelSmall.copy(fontSize = 11.sp),
                                    color = Cream,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }

            "Mine" -> {
                if (personalAffirmations.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No personal declarations yet",
                                style = MakariosTypography.displaySmall,
                                color = Ink
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Author your own biblical declarations standing on God's Word in the Declare studio.",
                                style = MakariosTypography.bodyMedium,
                                color = Ink.copy(alpha = 0.6f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = onNavigateToCreate,
                                colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Cream),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Text("Author a Declaration", style = MakariosTypography.labelLarge)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(personalAffirmations, key = { it.id }) { affirmation ->
                            KeptCard(
                                affirmation = affirmation,
                                light = Light.Ember,
                                onClick = { selectedDetailAffirmation = affirmation },
                                onShare = {
                                    ShareHelper.shareGeneric(
                                        context = context,
                                        affirmation = affirmation,
                                        light = Light.Ember
                                    )
                                },
                                onUnkeep = {
                                    AffirmationRepository.personalAffirmations.remove(affirmation)
                                    AffirmationRepository.unsave(affirmation.id)
                                    Toast.makeText(context, "Deleted personal declaration", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Affirmation Detail Dialog
    if (selectedDetailAffirmation != null) {
        val aff = selectedDetailAffirmation!!
        val light = TopicBands.find { it.name.equals(aff.category, ignoreCase = true) }?.light ?: Light.Dawn
        AffirmationDetailDialog(
            affirmation = aff,
            light = light,
            onDismiss = { selectedDetailAffirmation = null }
        )
    }

    // Wallpaper Preview Dialog
    if (selectedWallpaperPreview != null) {
        val wallpaperEntry = selectedWallpaperPreview!!
        val coroutineScope = rememberCoroutineScope()
        var isSettingWallpaper by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { selectedWallpaperPreview = null }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Ink)
            ) {
                AsyncImage(
                    model = wallpaperEntry.url(width = 1200),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    colorFilter = WarmPhotoGrade
                )

                // Top Close button
                IconButton(
                    onClick = { selectedWallpaperPreview = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Cream)
                }

                // Bottom actions
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                0.0f to Color.Transparent,
                                0.5f to Color.Black.copy(alpha = 0.7f),
                                1.0f to Color.Black.copy(alpha = 0.9f)
                            )
                        )
                        .padding(24.dp)
                ) {
                    Text(
                        text = wallpaperEntry.description,
                        style = MakariosTypography.bodyMedium,
                        color = Cream
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            isSettingWallpaper = true
                            coroutineScope.launch {
                                try {
                                    val bitmap = withContext(Dispatchers.IO) {
                                        val url = URL(wallpaperEntry.url(width = 1600))
                                        BitmapFactory.decodeStream(url.openStream())
                                    }
                                    val wm = WallpaperManager.getInstance(context)
                                    wm.setBitmap(bitmap)
                                    withContext(Dispatchers.Main) {
                                        isSettingWallpaper = false
                                        selectedWallpaperPreview = null
                                        Toast.makeText(context, "Wallpaper set successfully!", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        isSettingWallpaper = false
                                        Toast.makeText(context, "Could not set wallpaper", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Cream, contentColor = Ink),
                        enabled = !isSettingWallpaper
                    ) {
                        Icon(Icons.Outlined.Wallpaper, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSettingWallpaper) "Setting Wallpaper..." else "Set as Phone Wallpaper",
                            style = MakariosTypography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KeptTab(title: String, count: Int, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = "$title $count",
            style = MakariosTypography.bodyMedium,
            color = if (selected) Ink else Ink.copy(alpha = 0.4f),
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
        if (selected) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.height(2.dp).width(36.dp).background(Ink))
        }
    }
}

@Composable
fun KeptCard(
    affirmation: Affirmation,
    light: Light,
    onClick: () -> Unit,
    onShare: () -> Unit,
    onUnkeep: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 200.dp)
            .clip(RoundedCornerShape(20.dp))
            .lightBackground(light)
            .clickable { onClick() }
    ) {
        if (affirmation.imageUrl.isNotBlank()) {
            AsyncImage(
                model = affirmation.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                colorFilter = WarmPhotoGrade
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to light.top.copy(alpha = 0.88f),
                            1.0f to light.bottom.copy(alpha = 0.95f)
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Pairing(
                declaration = affirmation.declaration,
                verseText = affirmation.scriptureText,
                verseReference = affirmation.reference,
                light = light,
                isCompact = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onShare,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.12f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = light.text,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                IconButton(
                    onClick = onUnkeep,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.12f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Unkeep",
                        tint = light.text,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
