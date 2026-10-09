package com.makarios.app.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.makarios.app.data.Affirmation
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.data.AuthManager
import com.makarios.app.data.CommunityRepository
import com.makarios.app.data.PhotoLibrary
import com.makarios.app.data.PublicAffirmation
import com.makarios.app.ui.components.AffirmationCard
import com.makarios.app.ui.components.CommunityAffirmationCard
import com.makarios.app.ui.components.ScripturePickerSheet
import com.makarios.app.ui.theme.*
import com.makarios.app.util.ShareHelper

enum class LibraryTab(val title: String) {
    CURATED("Curated"),
    COMMUNITY("Community")
}

private data class ThemeItem(
    val name: String,
    val description: String,
    val previewQuote: String,
    val imageUrl: String,
    val accentColor: Color,
    val badgeBg: Color
)

private val categoryThemes = listOf(
    ThemeItem(
        name = "Identity",
        description = "Rooted in who God declares you are, redeemed and unconditionally beloved.",
        previewQuote = "I am fully known, deeply loved, and precisely placed.",
        imageUrl = PhotoLibrary.getThemePhoto("Identity").url(width = 800),
        accentColor = Olive,
        badgeBg = OliveLight
    ),
    ThemeItem(
        name = "Peace",
        description = "Stillness that guards your heart and mind when anxiety and worry press in.",
        previewQuote = "The peace of God guards my heart and mind today.",
        imageUrl = PhotoLibrary.getThemePhoto("Peace").url(width = 800),
        accentColor = Sage,
        badgeBg = SageLight
    ),
    ThemeItem(
        name = "Strength",
        description = "Divine endurance and quiet power renewed when human stamina runs out.",
        previewQuote = "My strength is made perfect in weakness.",
        imageUrl = PhotoLibrary.getThemePhoto("Strength").url(width = 800),
        accentColor = SunlitGold,
        badgeBg = SunlitGoldLight
    ),
    ThemeItem(
        name = "Purpose",
        description = "Clarity for your calling and steady confidence for the road ahead.",
        previewQuote = "He who began a good work in you will complete it.",
        imageUrl = PhotoLibrary.getThemePhoto("Purpose").url(width = 800),
        accentColor = Espresso,
        badgeBg = PorcelainWarm
    ),
    ThemeItem(
        name = "Courage",
        description = "Holy boldness to step into the unknown and conquer fear with faith.",
        previewQuote = "The Lord is my light and salvation; whom shall I fear?",
        imageUrl = PhotoLibrary.getThemePhoto("Courage").url(width = 800),
        accentColor = Olive,
        badgeBg = OliveLight
    ),
    ThemeItem(
        name = "Joy",
        description = "Unshakeable gladness flowing from thankfulness and the favor of God.",
        previewQuote = "The joy of the Lord is my unshakeable strength.",
        imageUrl = PhotoLibrary.getThemePhoto("Joy").url(width = 800),
        accentColor = SunlitGold,
        badgeBg = SunlitGoldLight
    ),
    ThemeItem(
        name = "Provision",
        description = "Resting in the inexhaustible abundance and daily care of your Father.",
        previewQuote = "My God meets every need according to His glory.",
        imageUrl = PhotoLibrary.getThemePhoto("Provision").url(width = 800),
        accentColor = Sage,
        badgeBg = SageLight
    ),
    ThemeItem(
        name = "Confidence",
        description = "Quiet assurance and unshakable trust that will not shrink back in doubt.",
        previewQuote = "I can do all things through Christ who empowers me.",
        imageUrl = PhotoLibrary.getThemePhoto("Confidence").url(width = 800),
        accentColor = Espresso,
        badgeBg = PorcelainWarm
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    onNavigateToDetail: (Affirmation) -> Unit,
    onNavigateToCreate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(LibraryTab.CURATED) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showBibleBrowser by remember { mutableStateOf(false) }

    val categories = remember {
        listOf("All", "Identity", "Peace", "Strength", "Purpose", "Courage", "Joy", "Provision", "Confidence", "Relationships", "Discipline")
    }

    val allAffirmations = remember { AffirmationRepository.getAll() }

    // Start/stop listening to public declarations when on Community tab
    DisposableEffect(selectedTab, selectedCategory) {
        if (selectedTab == LibraryTab.COMMUNITY) {
            CommunityRepository.startListeningToPublicDeclarations(selectedCategory)
        }
        onDispose {
            if (selectedTab == LibraryTab.COMMUNITY) {
                CommunityRepository.stopListeningToPublicDeclarations()
            }
        }
    }

    // Filter curated affirmations
    val filteredAffirmations = remember(searchQuery, selectedCategory) {
        allAffirmations.filter { affirmation ->
            val matchesCategory = selectedCategory == "All" || affirmation.category.equals(selectedCategory, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() ||
                    affirmation.declaration.contains(searchQuery, ignoreCase = true) ||
                    affirmation.scriptureText.contains(searchQuery, ignoreCase = true) ||
                    affirmation.reference.contains(searchQuery, ignoreCase = true) ||
                    affirmation.category.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    // Filter community declarations
    val filteredCommunityDeclarations = remember(
        CommunityRepository.publicDeclarations.size,
        CommunityRepository.publicDeclarations.toList(),
        searchQuery,
        selectedCategory
    ) {
        CommunityRepository.publicDeclarations.filter { pub ->
            val matchesCategory = selectedCategory == "All" || pub.category.equals(selectedCategory, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() ||
                    pub.declaration.contains(searchQuery, ignoreCase = true) ||
                    pub.scriptureText.contains(searchQuery, ignoreCase = true) ||
                    pub.reference.contains(searchQuery, ignoreCase = true) ||
                    pub.authorName.contains(searchQuery, ignoreCase = true) ||
                    pub.authorUsername.contains(searchQuery, ignoreCase = true) ||
                    pub.category.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    val showCuratedOverview = selectedTab == LibraryTab.CURATED && selectedCategory == "All" && searchQuery.isBlank()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Porcelain
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 76.dp)
        ) {
            // ── 1. Top Header Bar: Title, subtitle & Curated / Community toggle ──
            item(key = "header_bar") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 16.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = "Library",
                            fontFamily = DisplayFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 24.sp,
                            letterSpacing = (-0.3).sp,
                            color = Espresso
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = if (selectedTab == LibraryTab.CURATED)
                                "Biblical declarations rooted in scripture"
                            else
                                "Declarations shared by believers worldwide",
                            fontFamily = BodyFontFamily,
                            fontSize = 13.sp,
                            color = Stone
                        )
                    }

                    // Segmented Tab Selector
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Surface)
                            .border(0.5.dp, BorderSubtle, RoundedCornerShape(20.dp))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LibraryTab.values().forEach { tab ->
                            val isSelected = selectedTab == tab
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(17.dp))
                                    .background(if (isSelected) Olive else Color.Transparent)
                                    .clickable { selectedTab = tab }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tab.title,
                                    fontFamily = BodyFontFamily,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = if (isSelected) Color.White else Stone
                                )
                            }
                        }
                    }
                }
            }

            // ── 2. Featured Today Card (Top of Library when exploring Curated) ──
            if (showCuratedOverview) {
                item(key = "featured_today_card") {
                    FeaturedTodayCard(
                        affirmation = AffirmationRepository.affirmationOfTheDay,
                        onClick = { onNavigateToDetail(AffirmationRepository.affirmationOfTheDay) }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            // ── 3. Sticky Search Bar and Category Chips ───────────
            stickyHeader(key = "sticky_search_and_chips") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Porcelain)
                        .padding(bottom = 12.dp)
                ) {
                    // Search Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 2.dp)
                            .shadow(elevation = 1.dp, shape = RoundedCornerShape(24.dp), spotColor = Espresso.copy(alpha = 0.04f))
                            .clip(RoundedCornerShape(24.dp))
                            .background(Surface)
                            .border(0.5.dp, BorderSubtle, RoundedCornerShape(24.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = StoneMuted,
                                modifier = Modifier.size(18.dp)
                            )

                            Box(modifier = Modifier.weight(1f)) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = if (selectedTab == LibraryTab.CURATED)
                                            "Search declarations, scriptures, topics..."
                                        else
                                            "Search community declarations, authors...",
                                        fontFamily = BodyFontFamily,
                                        fontSize = 14.sp,
                                        color = StoneMuted
                                    )
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    textStyle = TextStyle(
                                        color = Espresso,
                                        fontFamily = BodyFontFamily,
                                        fontSize = 14.sp
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }

                            if (searchQuery.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = StoneMuted,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable { searchQuery = "" }
                                )
                            }

                            // Quick access to full Bible search & browser
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(OliveLight)
                                    .clickable { showBibleBrowser = true }
                                    .padding(horizontal = 9.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.MenuBook,
                                        contentDescription = "Open Bible Browser",
                                        tint = Olive,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Bible",
                                        fontFamily = BodyFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.5.sp,
                                        color = Olive
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category Filter Pills
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        categories.forEach { category ->
                            val isSelected = category == selectedCategory
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .then(
                                        if (isSelected) {
                                            Modifier
                                                .background(Olive)
                                                .shadow(1.dp, RoundedCornerShape(20.dp), spotColor = Olive.copy(alpha = 0.25f))
                                        } else {
                                            Modifier
                                                .background(Surface)
                                                .border(0.5.dp, BorderSubtle, RoundedCornerShape(20.dp))
                                        }
                                    )
                                    .clickable { selectedCategory = category }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = category,
                                    fontFamily = BodyFontFamily,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = if (isSelected) Color.White else Stone
                                )
                            }
                        }
                    }
                }
            }

            // ── 4. Theme Cards Section (Curated Tab Only, when "All" and no search) ──
            if (showCuratedOverview) {
                item(key = "theme_cards_header") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(top = 4.dp, bottom = 12.dp)
                    ) {
                        Text(
                            text = "Life Seasons & Themes",
                            fontFamily = DisplayFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            letterSpacing = (-0.2).sp,
                            color = Espresso
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Declarations crafted for specific seasons of life and spiritual warfare.",
                            fontFamily = BodyFontFamily,
                            fontSize = 12.5.sp,
                            color = Stone
                        )
                    }
                }

                item(key = "theme_cards_grid") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        categoryThemes.chunked(2).forEach { rowPairs ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowPairs.forEach { item ->
                                    val count = allAffirmations.count { it.category.equals(item.name, ignoreCase = true) }
                                    val countText = if (count == 1) "1 declaration" else "$count declarations"
                                    ThemeCard(
                                        item = item,
                                        countLabel = countText,
                                        onClick = { selectedCategory = item.name },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowPairs.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // ── 5. Declarations Header Row ────────────────────────
            item(key = "declarations_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 6.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedTab == LibraryTab.CURATED) {
                            when {
                                searchQuery.isNotBlank() -> "Search Results (${filteredAffirmations.size})"
                                selectedCategory != "All" -> "$selectedCategory Declarations (${filteredAffirmations.size})"
                                else -> "All Declarations (${filteredAffirmations.size})"
                            }
                        } else {
                            when {
                                searchQuery.isNotBlank() -> "Community Results (${filteredCommunityDeclarations.size})"
                                selectedCategory != "All" -> "$selectedCategory Declarations (${filteredCommunityDeclarations.size})"
                                else -> "Community Stream (${filteredCommunityDeclarations.size})"
                            }
                        },
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Espresso
                    )

                    if (selectedCategory != "All" || searchQuery.isNotBlank()) {
                        Text(
                            text = "Clear filter",
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = Olive,
                            modifier = Modifier.clickable {
                                selectedCategory = "All"
                                searchQuery = ""
                            }
                        )
                    }
                }
            }

            // ── 6. Declarations List Items ────────────────────────
            if (selectedTab == LibraryTab.CURATED) {
                if (filteredAffirmations.isEmpty()) {
                    item(key = "curated_empty_state") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 24.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Surface)
                                .border(0.5.dp, BorderSubtle, RoundedCornerShape(18.dp))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No declarations found",
                                    fontFamily = DisplayFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 17.sp,
                                    color = Espresso
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Write your own declaration and match with scripture.",
                                    fontFamily = BodyFontFamily,
                                    fontSize = 13.sp,
                                    color = Stone,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { onNavigateToCreate("") },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Olive,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.height(44.dp)
                                ) {
                                    Text(
                                        text = "Create Declaration",
                                        fontFamily = BodyFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(
                        items = filteredAffirmations,
                        key = { it.id }
                    ) { affirmation ->
                        var isSaved by remember(affirmation.id) {
                            mutableStateOf(AffirmationRepository.isSaved(affirmation.id))
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 7.dp)
                        ) {
                            AffirmationCard(
                                affirmation = affirmation,
                                isSaved = isSaved,
                                onToggleSave = {
                                    AffirmationRepository.toggleSave(affirmation.id)
                                    isSaved = !isSaved
                                },
                                onShare = {
                                    ShareHelper.shareAffirmationGraphic(context, affirmation)
                                },
                                onCardClick = { onNavigateToDetail(affirmation) }
                            )
                        }
                    }
                }
            } else {
                // ── Community Tab ──
                if (filteredCommunityDeclarations.isEmpty()) {
                    item(key = "community_empty_state") {
                        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                            EmptyCommunityState(
                                searchQuery = searchQuery,
                                onShareClick = { onNavigateToCreate("") }
                            )
                        }
                    }
                } else {
                    items(
                        items = filteredCommunityDeclarations,
                        key = { it.id }
                    ) { pub ->
                        val pubAffirmation = pub.toAffirmation()
                        var isSaved by remember(pub.id) {
                            mutableStateOf(AffirmationRepository.isSaved(pub.id))
                        }
                        val myUid = AuthManager.currentUser?.uid.orEmpty()
                        val isAmened = pub.amenedBy.contains(myUid)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 7.dp)
                        ) {
                            CommunityAffirmationCard(
                                affirmation = pub,
                                isSaved = isSaved,
                                isAmened = isAmened,
                                onToggleAmen = {
                                    if (!AuthManager.isLoggedIn || AuthManager.isAnonymous) {
                                        Toast.makeText(context, "Sign in to say Amen", Toast.LENGTH_SHORT).show()
                                    } else {
                                        CommunityRepository.toggleAmen(pub.id, pub.amenedBy)
                                    }
                                },
                                onToggleSave = {
                                    if (isSaved) {
                                        AffirmationRepository.toggleSave(pub.id)
                                        isSaved = false
                                    } else {
                                        AffirmationRepository.addPersonalAffirmation(pubAffirmation)
                                        isSaved = true
                                    }
                                },
                                onShare = {
                                    ShareHelper.shareAffirmationGraphic(context, pubAffirmation)
                                },
                                onCardClick = { onNavigateToDetail(pubAffirmation) }
                            )
                        }
                    }
                }
            }

            // ── 7. Author Invitation Banner ───────────────────────
            item(key = "author_invitation_banner") {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(18.dp), spotColor = AmberGold.copy(alpha = 0.08f))
                        .clip(RoundedCornerShape(18.dp))
                        .background(SunlitGoldLight)
                        .border(1.dp, AmberGold.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
                        .clickable { onNavigateToCreate("") }
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = "Need a declaration for your season?",
                                fontFamily = DisplayFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.5.sp,
                                color = Espresso
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Write your declaration · Match with Scripture",
                                fontFamily = BodyFontFamily,
                                fontSize = 12.sp,
                                color = Stone
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Surface)
                                .border(1.dp, Border, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Create",
                                tint = Olive,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        if (showBibleBrowser) {
            ScripturePickerSheet(
                onDismiss = { showBibleBrowser = false },
                onVerseSelected = { ref, text ->
                    showBibleBrowser = false
                    onNavigateToCreate("")
                }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Components
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FeaturedTodayCard(
    affirmation: Affirmation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Espresso.copy(alpha = 0.12f)
            )
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .height(200.dp)
    ) {
        // Image background
        AsyncImage(
            model = affirmation.imageUrl,
            contentDescription = "Featured declaration",
            contentScale = ContentScale.Crop,
            colorFilter = WarmPhotoGrade,
            modifier = Modifier.fillMaxSize()
        )

        // Dark gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PhotoTextScrim)
        )

        // Card Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Row: Category tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ScrimPill {
                    Text(
                        text = "FEATURED TODAY · ${affirmation.category.uppercase()}",
                        color = Color.White,
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 9.sp,
                        letterSpacing = 1.4.sp
                    )
                }
            }

            // Bottom Section: Declaration quote + Scripture reference
            Column {
                Text(
                    text = "“${affirmation.declaration}”",
                    fontFamily = DisplayFontFamily,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    lineHeight = 23.sp,
                    color = Color.White,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.55f),
                            offset = Offset(0f, 1.5f),
                            blurRadius = 4f
                        )
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = affirmation.reference.uppercase(),
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    letterSpacing = 1.3.sp,
                    color = SunlitGoldLight,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.45f),
                            offset = Offset(0f, 1f),
                            blurRadius = 3f
                        )
                    )
                )
            }
        }
    }
}

@Composable
private fun ThemeCard(
    item: ThemeItem,
    countLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "theme_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = Espresso.copy(alpha = 0.08f)
            )
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .height(180.dp)
    ) {
        // Background photo
        AsyncImage(
            model = item.imageUrl,
            contentDescription = item.name,
            contentScale = ContentScale.Crop,
            colorFilter = WarmPhotoGrade,
            modifier = Modifier.fillMaxSize()
        )

        // Tinted overlay scrim (theme accent tint + dark bottom)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to item.accentColor.copy(alpha = 0.55f),
                        0.5f to Color(0xFF161412).copy(alpha = 0.75f),
                        1f to Color(0xFF100E0D).copy(alpha = 0.92f)
                    )
                )
        )

        // Content inside theme card
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: Name in serif + Count pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.name,
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = Color.White
                )

                ScrimPill {
                    Text(
                        text = countLabel,
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 9.5.sp,
                        color = Color.White
                    )
                }
            }

            // Middle: Short description clamped to 2 lines
            Text(
                text = item.description,
                fontFamily = BodyFontFamily,
                fontSize = 11.5.sp,
                lineHeight = 15.sp,
                color = Color.White.copy(alpha = 0.85f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Bottom: Preview quote in italic serif with soft text shadow
            Text(
                text = "“${item.previewQuote}”",
                fontFamily = DisplayFontFamily,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = Color.White.copy(alpha = 0.95f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        offset = Offset(0f, 1f),
                        blurRadius = 3f
                    )
                )
            )
        }
    }
}

@Composable
private fun EmptyCommunityState(
    searchQuery: String,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Surface)
            .border(0.5.dp, BorderSubtle, RoundedCornerShape(20.dp))
            .padding(horizontal = 24.dp, vertical = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Sacred Illustration container
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(PorcelainWarm),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(OliveLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Olive,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (searchQuery.isNotBlank())
                    "No matching community declarations"
                else
                    "Be the first to share in this season",
                fontFamily = DisplayFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Espresso,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (searchQuery.isNotBlank())
                    "Try searching for another keyword or scripture reference."
                else
                    "Speak faith and share a grounded scripture declaration with the community.",
                fontFamily = BodyFontFamily,
                fontSize = 12.5.sp,
                lineHeight = 17.sp,
                color = Stone,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onShareClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Olive,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.height(44.dp)
            ) {
                Text(
                    text = "Share a declaration",
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
