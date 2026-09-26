package com.makarios.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.makarios.app.data.Affirmation
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.data.SacredBackgrounds
import com.makarios.app.ui.components.AffirmationCard
import com.makarios.app.ui.components.WallpaperActionDialog
import com.makarios.app.ui.theme.*
import com.makarios.app.util.ShareHelper
import com.makarios.app.util.WallpaperRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class SavedTab(val label: String) {
    DECLARATIONS("Declarations"),
    WALLPAPERS("Wallpapers"),
    PERSONAL("Personal")
}

data class WallpaperItem(
    val id: String,
    val title: String,
    val reference: String,
    val scriptureText: String,
    val category: String = "DECLARATION",
    val background: Brush,
    val textColor: Color,
    val accentColor: Color,
    val affirmationId: String,
    val styleIndex: Int = 0,
    val photoUrl: String? = null
)

val curatedWallpapers = listOf(
    WallpaperItem(
        id = "wp-1",
        title = "I am fearfully and wonderfully made.",
        reference = "PSALM 139:14",
        scriptureText = "I praise you because I am fearfully and wonderfully made; your works are wonderful, I know that full well.",
        category = "Identity",
        background = AlabasterDawnGradient,
        textColor = Espresso,
        accentColor = Terracotta,
        affirmationId = "ident-1",
        styleIndex = 0,
        photoUrl = "https://images.unsplash.com/photo-1507652313519-d4e9174996dd?auto=format&fit=crop&w=1600&q=85"
    ),
    WallpaperItem(
        id = "wp-2",
        title = "The Lord is my shepherd; I lack nothing.",
        reference = "PSALM 23:1",
        scriptureText = "The LORD is my shepherd, I lack nothing. He makes me lie down in green pastures, he leads me beside quiet waters.",
        category = "Provision",
        background = SunlitGoldGradient,
        textColor = Espresso,
        accentColor = SunlitGold,
        affirmationId = "aotd-1",
        styleIndex = 1,
        photoUrl = "https://images.unsplash.com/photo-1505765050516-f72dcac9c60e?auto=format&fit=crop&w=1600&q=85"
    ),
    WallpaperItem(
        id = "wp-3",
        title = "In quietness and trust is my strength.",
        reference = "ISAIAH 30:15",
        scriptureText = "In repentance and rest is your salvation, in quietness and trust is your strength.",
        category = "Peace",
        background = MorningSageGradient,
        textColor = Color(0xFF243329),
        accentColor = Sage,
        affirmationId = "peace-still",
        styleIndex = 2,
        photoUrl = "https://images.unsplash.com/photo-1448375240586-882707db888b?auto=format&fit=crop&w=1600&q=85"
    ),
    WallpaperItem(
        id = "wp-4",
        title = "The peace of God guards my heart.",
        reference = "PHILIPPIANS 4:7",
        scriptureText = "And the peace of God, which transcends all understanding, will guard your hearts and your minds in Christ Jesus.",
        category = "Confidence",
        background = LuminousDawnGradient,
        textColor = Color.White,
        accentColor = Color(0xFFFFF0EC),
        affirmationId = "conf-1",
        styleIndex = 3,
        photoUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=1600&q=85"
    ),
    WallpaperItem(
        id = "wp-5",
        title = "Be strong and courageous. Do not be afraid.",
        reference = "JOSHUA 1:9",
        scriptureText = "Have I not commanded you? Be strong and courageous. Do not be afraid; do not be discouraged, for the LORD your God will be with you wherever you go.",
        category = "Courage",
        background = SunlitAmberGradient,
        textColor = Espresso,
        accentColor = AmberGold,
        affirmationId = "str-1",
        styleIndex = 1,
        photoUrl = "https://images.unsplash.com/photo-1486870591958-9b9d0d1dda99?auto=format&fit=crop&w=1600&q=85"
    ),
    WallpaperItem(
        id = "wp-6",
        title = "I can do all things through Christ who gives me strength.",
        reference = "PHILIPPIANS 4:13",
        scriptureText = "I can do all this through him who gives me strength.",
        category = "Strength",
        background = AtmosphericGradient,
        textColor = Color.White,
        accentColor = Color(0xFFDEAC46),
        affirmationId = "str-2",
        styleIndex = 4,
        photoUrl = "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?auto=format&fit=crop&w=1600&q=85"
    )
)

@Composable
fun SavedScreen(
    onNavigateToDetail: (Affirmation) -> Unit,
    onNavigateToCreate: (String) -> Unit,
    onNavigateToLibrary: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(SavedTab.DECLARATIONS) }
    var activeWallpaperItem by remember { mutableStateOf<WallpaperItem?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val handleDownload: (WallpaperItem) -> Unit = { item ->
        coroutineScope.launch {
            val uri = withContext(Dispatchers.IO) {
                val photoBmp = item.photoUrl?.let { WallpaperRenderer.fetchBitmapFromUrl(context, it) }
                val bitmap = WallpaperRenderer.renderBitmap(
                    context = context,
                    declaration = item.title,
                    scripture = item.scriptureText,
                    reference = item.reference,
                    category = item.category,
                    style = WallpaperRenderer.getStyle(item.styleIndex),
                    format = WallpaperRenderer.OutputFormat.WALLPAPER,
                    photoBitmap = photoBmp
                )
                WallpaperRenderer.saveToGallery(context, bitmap, "Makarios Wallpaper")
            }
            if (uri != null) {
                Toast.makeText(context, "Saved wallpaper to Photos in Makarios album", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to save wallpaper", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val handleShare: (WallpaperItem) -> Unit = { item ->
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                val photoBmp = item.photoUrl?.let { WallpaperRenderer.fetchBitmapFromUrl(context, it) }
                val bitmap = WallpaperRenderer.renderBitmap(
                    context = context,
                    declaration = item.title,
                    scripture = item.scriptureText,
                    reference = item.reference,
                    category = item.category,
                    style = WallpaperRenderer.getStyle(item.styleIndex),
                    format = WallpaperRenderer.OutputFormat.WALLPAPER,
                    photoBitmap = photoBmp
                )
                val caption = "“${item.title}”\n— ${item.reference}\n\nShared via Makarios"
                ShareHelper.shareAffirmationImage(context, bitmap, "Makarios Wallpaper", caption)
            }
        }
    }

    val savedList = AffirmationRepository.getSaved()
    val personalList = AffirmationRepository.personalAffirmations

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Porcelain
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 36.dp)
        ) {
            // ── Top Header ──────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp, bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Saved & Created",
                            fontFamily = DisplayFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 24.sp,
                            color = Espresso
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Declarations you've kept close and truths you've authored.",
                            fontFamily = BodyFontFamily,
                            fontSize = 13.sp,
                            color = Stone
                        )
                    }

                    // Floating add button to create
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Surface)
                            .border(1.dp, Border, CircleShape)
                            .clickable { onNavigateToCreate("") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create declaration",
                            tint = Espresso,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // ── Segmented Tabs ──────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(PorcelainWarm.copy(alpha = 0.55f))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                SavedTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    val count = when (tab) {
                        SavedTab.DECLARATIONS -> savedList.size
                        SavedTab.WALLPAPERS -> curatedWallpapers.size
                        SavedTab.PERSONAL -> personalList.size
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .background(Surface)
                                        .shadow(1.dp, RoundedCornerShape(20.dp), spotColor = Espresso.copy(alpha = 0.05f))
                                } else {
                                    Modifier.background(Color.Transparent)
                                }
                            )
                            .clickable { selectedTab = tab }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = tab.label,
                                fontFamily = BodyFontFamily,
                                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                fontSize = 12.5.sp,
                                color = if (isSelected) Espresso else Stone
                            )
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) PorcelainWarm else Color.Transparent
                                    )
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "$count",
                                    fontFamily = BodyFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 10.sp,
                                    color = if (isSelected) Espresso else StoneMuted
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── TAB CONTENT ─────────────────────────────────────────
            when (selectedTab) {

                // ── TAB 1: SAVED DECLARATIONS ───────────────────────
                SavedTab.DECLARATIONS -> {
                    if (savedList.isEmpty()) {
                        EmptySavedState(
                            title = "No saved declarations yet",
                            description = "When a declaration speaks into your season, bookmark it to return to it anytime.",
                            buttonText = "Browse Library",
                            onAction = onNavigateToLibrary
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            savedList.forEach { affirmation ->
                                val isSaved = AffirmationRepository.isSaved(affirmation.id)
                                AffirmationCard(
                                    affirmation = affirmation,
                                    isSaved = isSaved,
                                    onToggleSave = {
                                        AffirmationRepository.toggleSave(affirmation.id)
                                    },
                                    onShare = {
                                        ShareHelper.shareAffirmationGraphic(context, affirmation)
                                    },
                                    onCardClick = { onNavigateToDetail(affirmation) }
                                )
                            }
                        }
                    }
                }

                // ── TAB 2: WALLPAPERS ───────────────────────────────
                SavedTab.WALLPAPERS -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Phone Wallpapers",
                                fontFamily = DisplayFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = Espresso
                            )
                            Text(
                                text = "Lock & Home Screen",
                                fontFamily = BodyFontFamily,
                                fontSize = 12.sp,
                                color = StoneMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 2-column grid of wallpapers
                        for (i in curatedWallpapers.indices step 2) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                WallpaperCard(
                                    wallpaper = curatedWallpapers[i],
                                    onOpenStudio = { onNavigateToCreate(curatedWallpapers[i].affirmationId) },
                                    onSetWallpaper = { activeWallpaperItem = curatedWallpapers[i] },
                                    onDownload = { handleDownload(curatedWallpapers[i]) },
                                    onShare = { handleShare(curatedWallpapers[i]) },
                                    modifier = Modifier.weight(1f)
                                )

                                if (i + 1 < curatedWallpapers.size) {
                                    WallpaperCard(
                                        wallpaper = curatedWallpapers[i + 1],
                                        onOpenStudio = { onNavigateToCreate(curatedWallpapers[i + 1].affirmationId) },
                                        onSetWallpaper = { activeWallpaperItem = curatedWallpapers[i + 1] },
                                        onDownload = { handleDownload(curatedWallpapers[i + 1]) },
                                        onShare = { handleShare(curatedWallpapers[i + 1]) },
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // ── TAB 3: PERSONAL DECLARATIONS ────────────────────
                SavedTab.PERSONAL -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        // Author banner card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(18.dp), spotColor = Espresso.copy(alpha = 0.06f))
                                .clip(RoundedCornerShape(18.dp))
                                .background(Surface)
                                .border(1.dp, Border, RoundedCornerShape(18.dp))
                                .padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Author a Declaration",
                                        fontFamily = DisplayFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp,
                                        color = Espresso
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "Declare God's promises in your own words, matched with scripture.",
                                        fontFamily = BodyFontFamily,
                                        fontSize = 12.5.sp,
                                        lineHeight = 17.sp,
                                        color = Stone
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Button(
                                    onClick = { onNavigateToCreate("") },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Terracotta,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Write",
                                        fontFamily = BodyFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.5.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (personalList.isEmpty()) {
                            EmptySavedState(
                                title = "No personal declarations yet",
                                description = "Write declarations rooted in scripture to speak over your life and season.",
                                buttonText = "Write Declaration",
                                onAction = { onNavigateToCreate("") }
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                personalList.forEach { affirmation ->
                                    val isSaved = AffirmationRepository.isSaved(affirmation.id)
                                    AffirmationCard(
                                        affirmation = affirmation,
                                        isSaved = isSaved,
                                        onToggleSave = {
                                            AffirmationRepository.toggleSave(affirmation.id)
                                        },
                                        onShare = {
                                            ShareHelper.shareAffirmationGraphic(context, affirmation)
                                        },
                                        onCardClick = { onNavigateToDetail(affirmation) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (activeWallpaperItem != null) {
            val item = activeWallpaperItem!!
            WallpaperActionDialog(
                declaration = item.title,
                scripture = item.scriptureText,
                reference = item.reference,
                category = item.category,
                styleIndex = item.styleIndex,
                photoUrl = item.photoUrl,
                onDismiss = { activeWallpaperItem = null }
            )
        }
    }
}

// ── Wallpaper Item Card ──────────────────────────────────────────
@Composable
private fun WallpaperCard(
    wallpaper: WallpaperItem,
    onOpenStudio: () -> Unit,
    onSetWallpaper: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val actionBg = if (wallpaper.photoUrl != null) Color.White.copy(alpha = 0.22f) else wallpaper.textColor.copy(alpha = 0.14f)
    val actionTint = if (wallpaper.photoUrl != null) Color.White else wallpaper.textColor

    Box(
        modifier = modifier
            .height(260.dp)
            .shadow(6.dp, RoundedCornerShape(18.dp), spotColor = Espresso.copy(alpha = 0.2f))
            .clip(RoundedCornerShape(18.dp))
            .then(
                if (wallpaper.photoUrl != null) {
                    Modifier.background(Color.Black)
                } else {
                    Modifier.background(wallpaper.background)
                }
            )
            .clickable(onClick = onSetWallpaper)
    ) {
        if (wallpaper.photoUrl != null) {
            AsyncImage(
                model = wallpaper.photoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0x66000000),
                                Color(0x99000000),
                                Color(0xEB0A0806)
                            )
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top tag
            Text(
                text = "MAKARIOS",
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 8.sp,
                letterSpacing = 2.sp,
                color = if (wallpaper.photoUrl != null) Color(0xFFFFDF7A) else wallpaper.accentColor.copy(alpha = 0.7f)
            )

            // Center quote
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "“${wallpaper.title}”",
                    fontFamily = DisplayFontFamily,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                    color = if (wallpaper.photoUrl != null) Color.White else wallpaper.textColor,
                    maxLines = 4
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(1.dp)
                        .background(
                            if (wallpaper.photoUrl != null) Color(0x66D4AF37) else wallpaper.accentColor.copy(alpha = 0.4f)
                        )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = wallpaper.reference,
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 9.sp,
                    letterSpacing = 1.2.sp,
                    color = if (wallpaper.photoUrl != null) Color(0xFFFFE8A3) else wallpaper.accentColor
                )
            }

            // Bottom action icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Edit in Studio
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(actionBg)
                        .clickable(onClick = onOpenStudio),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit in Studio",
                        tint = actionTint,
                        modifier = Modifier.size(13.dp)
                    )
                }

                // Action group: Share, Set Wallpaper, Download
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(actionBg)
                            .clickable(onClick = onShare),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Wallpaper",
                            tint = actionTint,
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(actionBg)
                            .clickable(onClick = onSetWallpaper),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wallpaper,
                            contentDescription = "Set Wallpaper",
                            tint = actionTint,
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(actionBg)
                            .clickable(onClick = onDownload),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download to Gallery",
                            tint = actionTint,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Empty State ──────────────────────────────────────────────────
@Composable
private fun EmptySavedState(
    title: String,
    description: String,
    buttonText: String,
    onAction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(PorcelainWarm),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.BookmarkBorder,
                contentDescription = null,
                tint = Espresso,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = title,
            fontFamily = DisplayFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp,
            color = Espresso
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = description,
            fontFamily = BodyFontFamily,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            textAlign = TextAlign.Center,
            color = StoneMuted,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onAction,
            colors = ButtonDefaults.buttonColors(
                containerColor = Espresso,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = buttonText,
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
        }
    }
}
