package com.makarios.app.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.*
import com.makarios.app.R
import com.makarios.app.ui.components.ScripturePickerSheet
import com.makarios.app.ui.components.SocialShareSheet
import com.makarios.app.ui.components.WallpaperActionDialog
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.makarios.app.data.Affirmation
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.data.AffirmationTone
import com.makarios.app.data.CommunityRepository
import com.makarios.app.data.SacredBackgrounds
import com.makarios.app.data.SacredPhotoBackground
import com.makarios.app.data.ScriptureDatabase
import com.makarios.app.data.ScriptureMatcher
import com.makarios.app.data.VectorSearchEngine
import com.makarios.app.data.VerseMatch
import com.makarios.app.ui.theme.*
import com.makarios.app.util.ShareHelper
import com.makarios.app.util.WallpaperRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ── Create Screen Stages ─────────────────────────────────────────
enum class CreateStage {
    WRITE,   // 1. User writes their declaration
    MATCH,   // 2. Matched scripture is shown
    DESIGN   // 3. Design & Share
}

// ── Output Formats ───────────────────────────────────────────────
data class ShareFormat(val label: String, val ratio: Float, val size: String)

val shareFormats = listOf(
    ShareFormat("Story", 9f / 16f, "9:16"),
    ShareFormat("Square", 1f, "1:1"),
    ShareFormat("Snapchat", 9f / 16f, "Snap"),
    ShareFormat("X Card", 16f / 9f, "16:9"),
    ShareFormat("Status", 4f / 5f, "4:5"),
    ShareFormat("Wallpaper", 9f / 20f, "Phone")
)

// ── Design Styles ────────────────────────────────────────────────
data class DesignStyle(
    val name: String,
    val background: Brush,
    val textColor: Color,
    val accentColor: Color
)

val designStyles = listOf(
    DesignStyle(
        "Alabaster",
        AlabasterDawnGradient,
        Espresso,
        Olive
    ),
    DesignStyle(
        "Sunlit Gold",
        SunlitGoldGradient,
        Espresso,
        SunlitGold
    ),
    DesignStyle(
        "Morning Sage",
        MorningSageGradient,
        Color(0xFF243329),
        Sage
    ),
    DesignStyle(
        "Rose Dawn",
        LuminousDawnGradient,
        Color.White,
        Color(0xFFFFF0EC)
    ),
    DesignStyle(
        "Twilight",
        AtmosphericGradient,
        Color.White,
        Color(0xFFF7EBE7)
    )
)

// Scripture matching is now handled by ScriptureMatcher.kt

@Composable
fun CreateScreen(
    affirmationId: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Pre-fill from existing affirmation if opened from detail/library
    val seedAffirmation = remember(affirmationId) {
        if (!affirmationId.isNullOrBlank()) AffirmationRepository.getById(affirmationId) else null
    }

    var stage by remember { mutableStateOf(CreateStage.WRITE) }
    var declarationText by remember { mutableStateOf(seedAffirmation?.declaration ?: "") }
    var matchedReference by remember { mutableStateOf("") }
    var matchedScripture by remember { mutableStateOf("") }
    var selectedTone by remember { mutableStateOf<AffirmationTone?>(null) }
    var matchResults by remember { mutableStateOf<List<ScriptureMatcher.MatchResult>>(emptyList()) }
    var vectorMatches by remember { mutableStateOf<List<VerseMatch>>(emptyList()) }
    var matchIndex by remember { mutableIntStateOf(0) }
    var isMatching by remember { mutableStateOf(false) }
    var showPickerSheet by remember { mutableStateOf(false) }
    var showWallpaperDialog by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }
    var selectedFormatIndex by remember { mutableIntStateOf(0) }
    var selectedStyleIndex by remember { mutableIntStateOf(0) }
    var selectedPhotoId by remember { mutableStateOf<String?>("dawn_01") }
    val coroutineScope = rememberCoroutineScope()

    val currentPhotoUrl = selectedPhotoId?.let { SacredBackgrounds.getById(it)?.photoUrl }

    var savedPersonalAffirmationId by remember { mutableStateOf<String?>(null) }
    var isShareToCommunity by remember { mutableStateOf(false) }

    fun getOrCreateAffirmation(): Affirmation {
        val existingId = savedPersonalAffirmationId
        if (existingId != null) {
            val existing = AffirmationRepository.getById(existingId)
            if (existing != null) return existing
        }
        val targetId = seedAffirmation?.id ?: "personal-${System.currentTimeMillis()}"
        val affirmation = Affirmation(
            id = targetId,
            declaration = declarationText,
            scriptureText = matchedScripture.ifBlank { "I praise you because I am fearfully and wonderfully made; your works are wonderful, I know that full well." },
            reference = matchedReference.ifBlank { "PSALM 139:14" },
            context = "Personal declaration created in Makarios.",
            category = "Personal",
            tone = selectedTone ?: AffirmationTone.RESOLUTE,
            imageUrl = currentPhotoUrl ?: SacredBackgrounds.PHOTOS.first().photoUrl,
            isFavorite = true,
            personalDeclaration = declarationText
        )
        if (seedAffirmation == null || !AffirmationRepository.personalAffirmations.any { it.id == targetId }) {
            AffirmationRepository.addPersonalAffirmation(affirmation)
            if (isShareToCommunity) {
                CommunityRepository.publishDeclaration(
                    declaration = affirmation.declaration,
                    scriptureText = affirmation.scriptureText,
                    reference = affirmation.reference,
                    category = seedAffirmation?.category ?: "Peace",
                    imageUrl = affirmation.imageUrl,
                    onSuccess = {
                        Toast.makeText(context, "Shared with Community ✓", Toast.LENGTH_SHORT).show()
                    },
                    onError = { err ->
                        Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
        savedPersonalAffirmationId = targetId
        return affirmation
    }

    val saveCurrentDesignToGallery: () -> Unit = {
        coroutineScope.launch {
            val newAffirmation = getOrCreateAffirmation()
            val uri = withContext(Dispatchers.IO) {
                val photoBmp = currentPhotoUrl?.let { WallpaperRenderer.fetchBitmapFromUrl(context, it) }
                val bitmap = WallpaperRenderer.renderBitmap(
                    context = context,
                    declaration = declarationText,
                    scripture = newAffirmation.scriptureText,
                    reference = newAffirmation.reference,
                    category = "Personal",
                    style = WallpaperRenderer.getStyle(selectedStyleIndex),
                    format = WallpaperRenderer.OutputFormat.fromIndex(selectedFormatIndex),
                    photoBitmap = photoBmp
                )
                WallpaperRenderer.saveToGallery(context, bitmap, "Makarios")
            }
            if (uri != null) {
                Toast.makeText(context, "Saved to your Photos ✓", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Couldn't save — try again", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val shareCurrentDesign: () -> Unit = {
        getOrCreateAffirmation()
        showShareSheet = true
    }

    val openWallpaperDialog: () -> Unit = {
        getOrCreateAffirmation()
        showWallpaperDialog = true
    }

    val runMatch: () -> Unit = {
        val query = declarationText.trim()
        if (query.isNotBlank() && !isMatching) {
            isMatching = true
            coroutineScope.launch {
                try {
                    // 1. Semantic vector search across 31k verses (hybrid scored)
                    val vectorResults = VectorSearchEngine.getInstance(context).search(query, topK = 20)

                    if (vectorResults.isNotEmpty()) {
                        // 2. Tone post-filter: build a lookup of references that have a known tone
                        //    from the curated ScriptureDatabase (120 devotional verses with toneAffinity)
                        val toneDb: Map<String, AffirmationTone> = ScriptureDatabase.verses
                            .associateBy({ it.reference }, { it.toneAffinity })

                        val ordered = if (selectedTone != null) {
                            val toneMatched = vectorResults.filter { toneDb[it.reference] == selectedTone }
                            val unmatched  = vectorResults.filter { toneDb[it.reference] != selectedTone }
                            // Prefer tone-matched but don't drop the rest — user can cycle through all
                            (toneMatched + unmatched)
                        } else {
                            vectorResults
                        }

                        vectorMatches = ordered
                        matchResults = emptyList()
                        matchIndex = 0
                        matchedReference = ordered[0].reference
                        matchedScripture = ordered[0].text
                        stage = CreateStage.MATCH
                    } else {
                        // 3. Fallback: keyword + theme match on curated 120-verse corpus
                        val fallback = ScriptureMatcher.match(query, selectedTone)
                        matchResults = fallback
                        vectorMatches = emptyList()
                        matchIndex = 0
                        if (fallback.isNotEmpty()) {
                            matchedReference = fallback[0].verse.reference
                            matchedScripture = fallback[0].verse.text
                        }
                        stage = CreateStage.MATCH
                    }
                } catch (e: Exception) {
                    val fallback = ScriptureMatcher.match(query, selectedTone)
                    matchResults = fallback
                    vectorMatches = emptyList()
                    matchIndex = 0
                    if (fallback.isNotEmpty()) {
                        matchedReference = fallback[0].verse.reference
                        matchedScripture = fallback[0].verse.text
                    }
                    stage = CreateStage.MATCH
                } finally {
                    isMatching = false
                }
            }
        } else if (query.isBlank()) {
            Toast.makeText(context, "Write your declaration first", Toast.LENGTH_SHORT).show()
        }
    }

    val handleBack: () -> Unit = {
        when (stage) {
            CreateStage.DESIGN -> stage = CreateStage.MATCH
            CreateStage.MATCH -> stage = CreateStage.WRITE
            CreateStage.WRITE -> onBack()
        }
    }

    BackHandler(onBack = handleBack)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Porcelain
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── TOP BAR ───────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Surface)
                        .border(1.dp, Border, CircleShape)
                        .clickable(onClick = handleBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Espresso,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Stage progress indicator
                // Stage progress indicator (progress only, non-clickable)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("Write", "Match", "Design").forEachIndexed { i, label ->
                        val stageEnum = CreateStage.values()[i]
                        val isActive = stage == stageEnum
                        val isPast = stage.ordinal > i
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when {
                                        isActive -> Olive
                                        isPast -> OliveLight
                                        else -> Surface
                                    }
                                )
                                .border(
                                    width = 1.dp,
                                    color = when {
                                        isActive -> Olive
                                        isPast -> Olive.copy(alpha = 0.25f)
                                        else -> Border
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (isPast) "✓ $label" else label,
                                fontFamily = BodyFontFamily,
                                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 11.sp,
                                color = when {
                                    isActive -> Color.White
                                    isPast -> Olive
                                    else -> StoneMuted
                                }
                            )
                        }
                    }
                }

                // Right action — the ONLY primary action, distinct labels per step
                when (stage) {
                    CreateStage.WRITE -> {
                        Button(
                            onClick = runMatch,
                            enabled = !isMatching,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Olive, contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            if (isMatching) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(13.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = if (isMatching) "Finding…" else "Find a verse",
                                fontFamily = BodyFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                    CreateStage.MATCH -> {
                        Button(
                            onClick = { stage = CreateStage.DESIGN },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Olive, contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Use this verse", fontFamily = BodyFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                    CreateStage.DESIGN -> {
                        Button(
                            onClick = saveCurrentDesignToGallery,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Olive, contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Save", fontFamily = BodyFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // ── BODY — per stage ──────────────────────────────────
            when (stage) {

                // ── STAGE 1: WRITE ────────────────────────────────
                CreateStage.WRITE -> WriteStage(
                    declarationText = declarationText,
                    onDeclarationChange = { declarationText = it },
                    seedAffirmation = seedAffirmation,
                    selectedTone = selectedTone,
                    onToneSelect = { selectedTone = it },
                    isMatching = isMatching,
                    onMatch = runMatch
                )

                // ── STAGE 2: MATCH ────────────────────────────────
                CreateStage.MATCH -> MatchStage(
                    declaration = declarationText,
                    reference = matchedReference,
                    scripture = matchedScripture,
                    matchIndex = matchIndex,
                    matchCount = if (vectorMatches.isNotEmpty()) vectorMatches.size else matchResults.size,
                    onBack = { stage = CreateStage.WRITE },
                    onAccept = { stage = CreateStage.DESIGN },
                    onTryDifferent = {
                        if (vectorMatches.isNotEmpty()) {
                            matchIndex = (matchIndex + 1) % vectorMatches.size
                            val next = vectorMatches[matchIndex]
                            matchedReference = next.reference
                            matchedScripture = next.text
                        } else if (matchResults.size > 1) {
                            matchIndex = (matchIndex + 1) % matchResults.size
                            val next = matchResults[matchIndex]
                            matchedReference = next.verse.reference
                            matchedScripture = next.verse.text
                        }
                    },
                    onBrowseLibrary = { showPickerSheet = true }
                )

                // ── STAGE 3: DESIGN ───────────────────────────────
                CreateStage.DESIGN -> DesignStage(
                    declaration = declarationText,
                    reference = matchedReference,
                    scripture = matchedScripture,
                    selectedFormatIndex = selectedFormatIndex,
                    onFormatSelect = { selectedFormatIndex = it },
                    selectedStyleIndex = selectedStyleIndex,
                    onStyleSelect = { selectedStyleIndex = it },
                    selectedPhotoId = selectedPhotoId,
                    onSelectPhoto = { selectedPhotoId = it },
                    onSaveToGallery = saveCurrentDesignToGallery,
                    onSaveAsWallpaper = openWallpaperDialog,
                    onShare = shareCurrentDesign,
                    isShareToCommunity = isShareToCommunity,
                    onToggleShareToCommunity = { isShareToCommunity = it }
                )
            }
        }

        if (showPickerSheet) {
            ScripturePickerSheet(
                onDismiss = { showPickerSheet = false },
                onVerseSelected = { ref, text ->
                    matchedReference = ref
                    matchedScripture = text
                    showPickerSheet = false
                }
            )
        }

        if (showWallpaperDialog) {
            WallpaperActionDialog(
                declaration = declarationText,
                scripture = matchedScripture.ifBlank { "I praise you because I am fearfully and wonderfully made; your works are wonderful, I know that full well." },
                reference = matchedReference.ifBlank { "PSALM 139:14" },
                category = "Personal",
                styleIndex = selectedStyleIndex,
                photoUrl = currentPhotoUrl,
                onDismiss = { showWallpaperDialog = false }
            )
        }

        if (showShareSheet) {
            val aff = getOrCreateAffirmation()
            SocialShareSheet(
                affirmation = aff,
                initialStyleIndex = selectedStyleIndex,
                photoUrl = currentPhotoUrl,
                onDismiss = { showShareSheet = false }
            )
        }
    }
}

// ── STAGE 1 COMPOSABLE: WRITE ─────────────────────────────────────
@Composable
private fun WriteStage(
    declarationText: String,
    onDeclarationChange: (String) -> Unit,
    seedAffirmation: Affirmation?,
    selectedTone: AffirmationTone?,
    onToneSelect: (AffirmationTone?) -> Unit,
    isMatching: Boolean = false,
    onMatch: () -> Unit
) {
    val scrollState = rememberScrollState()
    val charCount = declarationText.length

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(bottom = 48.dp)
    ) {
        // Heading
        Text(
            text = "Write your declaration",
            fontFamily = DisplayFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            letterSpacing = (-0.3).sp,
            color = Espresso
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Declare it in first person. A matching scripture will follow.",
            fontFamily = BodyFontFamily,
            fontSize = 13.sp,
            color = Stone
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Main Text Input
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 3.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = Espresso.copy(alpha = 0.08f)
                )
                .clip(RoundedCornerShape(20.dp))
                .background(Surface)
                .border(1.dp, Border, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column {
                // Prompt
                Text(
                    text = "I AM / I HAVE / I WALK IN…",
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                    color = Olive
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp)) {
                    if (declarationText.isEmpty()) {
                        Text(
                            text = "e.g. I am not moved by fear. I walk in perfect peace because my trust is in Him…",
                            fontFamily = DisplayFontFamily,
                            fontStyle = FontStyle.Italic,
                            fontSize = 17.sp,
                            lineHeight = 25.sp,
                            color = StoneMuted,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    BasicTextField(
                        value = declarationText,
                        onValueChange = onDeclarationChange,
                        textStyle = TextStyle(
                            color = Espresso,
                            fontFamily = DisplayFontFamily,
                            fontSize = 17.sp,
                            lineHeight = 25.sp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Character count
                Text(
                    text = "$charCount / 280",
                    fontFamily = BodyFontFamily,
                    fontSize = 11.sp,
                    color = if (charCount > 250) Olive else StoneMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Tone pickers
        Text(
            text = "Tone",
            fontFamily = BodyFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = StoneMuted
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "Still & Restful" to AffirmationTone.STILL,
                "Bold & Resolute" to AffirmationTone.RESOLUTE,
                "Gentle & Tender" to AffirmationTone.GENTLE
            ).forEach { (label, tone) ->
                val isSelected = selectedTone == tone
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) Olive else Surface)
                        .border(
                            width = if (isSelected) 0.dp else 1.dp,
                            color = if (isSelected) Color.Transparent else Border,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { onToneSelect(if (isSelected) null else tone) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontFamily = BodyFontFamily,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 11.5.sp,
                        maxLines = 1,
                        softWrap = false,
                        color = if (isSelected) Color.White else Espresso,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Inspiration seeds — pick from curated
        if (seedAffirmation == null) {
            Text(
                text = "Start from a curated declaration",
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = StoneMuted
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val seeds = listOf(
                    AffirmationRepository.affirmationOfTheDay,
                    AffirmationRepository.courageAffirmation,
                    AffirmationRepository.widgetAffirmation
                )
                seeds.forEach { seed ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(14.dp), spotColor = Espresso.copy(alpha = 0.05f))
                            .clip(RoundedCornerShape(14.dp))
                            .background(Surface)
                            .border(1.dp, Border, RoundedCornerShape(14.dp))
                            .clickable { onDeclarationChange(seed.declaration) }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = seed.category.uppercase(),
                                    fontFamily = BodyFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 9.5.sp,
                                    letterSpacing = 1.2.sp,
                                    color = Olive
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "“${seed.declaration}”",
                                    fontFamily = DisplayFontFamily,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    color = Espresso,
                                    maxLines = 2
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Use this",
                                tint = StoneMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── STAGE 2 COMPOSABLE: MATCH ────────────────────────────────────
@Composable
private fun MatchStage(
    declaration: String,
    reference: String,
    scripture: String,
    matchIndex: Int = 0,
    matchCount: Int = 1,
    onBack: () -> Unit,
    onAccept: () -> Unit,
    onTryDifferent: () -> Unit,
    onBrowseLibrary: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 48.dp)
    ) {
        Text(
            text = "Your scripture match",
            fontFamily = DisplayFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            letterSpacing = (-0.3).sp,
            color = Espresso
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Review the grounding verse beneath your declaration.",
            fontFamily = BodyFontFamily,
            fontSize = 13.sp,
            color = Stone
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Preview card — how the complete affirmation will look
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(22.dp),
                    spotColor = Espresso.copy(alpha = 0.06f)
                )
                .clip(RoundedCornerShape(22.dp))
                .background(Surface)
                .border(1.dp, Border, RoundedCornerShape(22.dp))
                .padding(24.dp)
        ) {
            Column {
                // Declaration
                Text(
                    text = "“$declaration”",
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 22.sp,
                    lineHeight = 31.sp,
                    letterSpacing = (-0.2).sp,
                    color = Espresso
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Grounding Scripture Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(PorcelainWarm.copy(alpha = 0.65f))
                        .border(0.5.dp, BorderSubtle, RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "“$scripture”",
                            fontFamily = DisplayFontFamily,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.5.sp,
                            lineHeight = 22.sp,
                            color = EspressoLight
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = reference.uppercase(),
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            letterSpacing = 1.2.sp,
                            color = Olive
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Match explanation note
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(OliveLight)
                .border(1.dp, Olive.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Olive,
                    modifier = Modifier.size(18.dp).padding(top = 2.dp)
                )
                Column {
                    Text(
                        text = "Your words, grounded in this verse.",
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Espresso
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Rooted in God's Word. Not the verse itself — a declaration built on its truth.",
                        fontFamily = BodyFontFamily,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = Stone
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (matchCount > 1) {
            Text(
                text = "Match ${matchIndex + 1} of $matchCount",
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = StoneMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )
        }

        // Secondary actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onTryDifferent,
                border = BorderStroke(1.dp, Border),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text(
                    text = "Try another verse",
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = Espresso
                )
            }

            OutlinedButton(
                onClick = onBack,
                border = BorderStroke(1.dp, Border),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text(
                    text = "Edit declaration",
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = Stone
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Browse / Search Scripture Library
        OutlinedButton(
            onClick = onBrowseLibrary,
            border = BorderStroke(1.dp, Border),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = Olive,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Browse or search full Scripture Library",
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.5.sp,
                    color = Espresso
                )
            }
        }
    }
}

// ── STAGE 3 COMPOSABLE: DESIGN ───────────────────────────────────
@Composable
private fun DesignStage(
    declaration: String,
    reference: String,
    scripture: String,
    selectedFormatIndex: Int,
    onFormatSelect: (Int) -> Unit,
    selectedStyleIndex: Int,
    onStyleSelect: (Int) -> Unit,
    selectedPhotoId: String?,
    onSelectPhoto: (String?) -> Unit,
    onSaveToGallery: () -> Unit,
    onSaveAsWallpaper: () -> Unit,
    onShare: () -> Unit,
    isShareToCommunity: Boolean,
    onToggleShareToCommunity: (Boolean) -> Unit
) {
    val currentFormat = shareFormats[selectedFormatIndex]
    val currentStyle = designStyles[selectedStyleIndex]
    val selectedPhoto = selectedPhotoId?.let { SacredBackgrounds.getById(it) }

    var selectedBackgroundTab by remember { mutableIntStateOf(0) } // 0 = Sacred Photos (28), 1 = Sacred Minimal (6)
    var selectedCategory by remember { mutableStateOf("All") }

    val photosForCategory = remember(selectedCategory) {
        SacredBackgrounds.getByCategory(selectedCategory)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 48.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "Make it beautiful",
                fontFamily = DisplayFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                letterSpacing = (-0.3).sp,
                color = Espresso
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pick a background, choose your layout, then share it.",
                fontFamily = BodyFontFamily,
                fontSize = 13.sp,
                color = Stone
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ── Format selector (horizontal scroll) ──────────────────
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "FORMAT",
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                letterSpacing = 1.4.sp,
                color = StoneMuted
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            shareFormats.forEachIndexed { index, format ->
                val isSelected = selectedFormatIndex == index
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .then(
                            if (isSelected) {
                                Modifier
                                    .background(Espresso)
                                    .shadow(2.dp, RoundedCornerShape(14.dp), spotColor = Espresso.copy(0.2f))
                            } else {
                                Modifier
                                    .background(Surface)
                                    .border(1.dp, Border, RoundedCornerShape(14.dp))
                            }
                        )
                        .clickable { onFormatSelect(index) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = format.label,
                            fontFamily = BodyFontFamily,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 13.sp,
                            color = if (isSelected) Color.White else Espresso
                        )
                        Text(
                            text = format.size,
                            fontFamily = BodyFontFamily,
                            fontSize = 10.sp,
                            color = if (isSelected) Color.White.copy(alpha = 0.7f) else StoneMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Live Canvas Preview (YouVersion Photographic / Sacred Minimal) ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            val previewHeight = (280f / currentFormat.ratio).coerceIn(180f, 380f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(previewHeight.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(22.dp),
                        spotColor = Espresso.copy(alpha = 0.28f)
                    )
                    .clip(RoundedCornerShape(22.dp))
                    .then(
                        if (selectedPhoto != null) {
                            Modifier.background(Color.Black)
                        } else {
                            Modifier.background(currentStyle.background)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selectedPhoto != null) {
                    // Photographic background with protective scrim
                    AsyncImage(
                        model = selectedPhoto.photoUrl,
                        contentDescription = selectedPhoto.title,
                        colorFilter = WarmPhotoGrade,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Multi-stop protective gradient scrim for maximum readability
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(PhotoTextScrim)
                    )
                }

                // Typography layer
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 22.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    // Category pill
                    if (selectedPhoto != null) {
                        ScrimPill {
                            Text(
                                text = "DECLARATION",
                                fontFamily = BodyFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 9.sp,
                                letterSpacing = 1.6.sp,
                                color = Color.White
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(currentStyle.accentColor.copy(alpha = 0.18f))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "DECLARATION",
                                fontFamily = BodyFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 9.sp,
                                letterSpacing = 1.6.sp,
                                color = currentStyle.accentColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Declaration text
                    Text(
                        text = "“$declaration”",
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 17.sp,
                        lineHeight = 24.sp,
                        letterSpacing = (-0.2).sp,
                        textAlign = TextAlign.Center,
                        color = if (selectedPhoto != null) Color.White else currentStyle.textColor,
                        maxLines = 5
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.45f)
                            .height(1.dp)
                            .background(
                                if (selectedPhoto != null) Color.White.copy(alpha = 0.35f)
                                else currentStyle.textColor.copy(alpha = 0.18f)
                            )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Scripture reference
                    Text(
                        text = reference.uppercase(),
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        letterSpacing = 1.2.sp,
                        color = if (selectedPhoto != null) Color.White else currentStyle.accentColor
                    )
                }

                // Makarios watermark
                Text(
                    text = "makarios",
                    fontFamily = DisplayFontFamily,
                    fontStyle = FontStyle.Italic,
                    fontSize = 9.sp,
                    color = if (selectedPhoto != null) Color.White.copy(alpha = 0.45f) else currentStyle.textColor.copy(alpha = 0.3f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Background Style Selector (Tabs: Photos vs Minimal) ────────
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Surface)
                    .border(1.dp, Border, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Tab 0: Photos
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .then(
                            if (selectedBackgroundTab == 0) {
                                Modifier
                                    .background(Olive)
                                    .shadow(2.dp, RoundedCornerShape(9.dp), spotColor = Olive.copy(0.2f))
                            } else Modifier
                        )
                        .clickable { selectedBackgroundTab = 0 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            tint = if (selectedBackgroundTab == 0) Color.White else Stone,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Photo backgrounds",
                            fontFamily = BodyFontFamily,
                            fontWeight = if (selectedBackgroundTab == 0) FontWeight.SemiBold else FontWeight.Medium,
                            fontSize = 12.sp,
                            color = if (selectedBackgroundTab == 0) Color.White else Stone
                        )
                    }
                }

                // Tab 1: Minimal Presets
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .then(
                            if (selectedBackgroundTab == 1) {
                                Modifier
                                    .background(Olive)
                                    .shadow(2.dp, RoundedCornerShape(9.dp), spotColor = Olive.copy(0.2f))
                            } else Modifier
                        )
                        .clickable { selectedBackgroundTab = 1 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Palette,
                            contentDescription = null,
                            tint = if (selectedBackgroundTab == 1) Color.White else Stone,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Color themes",
                            fontFamily = BodyFontFamily,
                            fontWeight = if (selectedBackgroundTab == 1) FontWeight.SemiBold else FontWeight.Medium,
                            fontSize = 12.sp,
                            color = if (selectedBackgroundTab == 1) Color.White else Stone
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedBackgroundTab == 0) {
            // ── Photographic Library ─────────────────────────────────
            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SacredBackgrounds.CATEGORIES.forEach { category ->
                    val isCatSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .then(
                                if (isCatSelected) {
                                    Modifier.background(Olive)
                                } else {
                                    Modifier
                                        .background(Surface)
                                        .border(1.dp, Border, RoundedCornerShape(20.dp))
                                }
                            )
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = category,
                            fontFamily = BodyFontFamily,
                            fontWeight = if (isCatSelected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 11.sp,
                            color = if (isCatSelected) Color.White else Espresso
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Horizontal Photo Thumbnails
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                photosForCategory.forEach { photo ->
                    val isSelected = selectedPhotoId == photo.id
                    Box(
                        modifier = Modifier
                            .width(82.dp)
                            .height(118.dp)
                            .shadow(
                                elevation = if (isSelected) 6.dp else 2.dp,
                                shape = RoundedCornerShape(14.dp),
                                spotColor = Espresso.copy(alpha = 0.2f)
                            )
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) Olive else Border,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { onSelectPhoto(photo.id) }
                    ) {
                        AsyncImage(
                            model = photo.thumbnailUrl,
                            contentDescription = photo.title,
                            colorFilter = WarmPhotoGrade,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Subtle bottom vignette with title
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color(0xD90A0806))
                                    )
                                )
                                .padding(horizontal = 4.dp, vertical = 3.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Text(
                                text = photo.title,
                                fontFamily = BodyFontFamily,
                                fontSize = 8.sp,
                                maxLines = 1,
                                color = Color.White.copy(alpha = 0.9f),
                                textAlign = TextAlign.Center
                            )
                        }

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(20.dp)
                                    .align(Alignment.TopEnd)
                                    .background(Olive, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // ── Minimalist Solid Presets ─────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                designStyles.forEachIndexed { index, style ->
                    val isSelected = selectedStyleIndex == index && selectedPhotoId == null
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .shadow(
                                if (isSelected) 4.dp else 1.dp,
                                RoundedCornerShape(14.dp),
                                spotColor = Espresso.copy(alpha = 0.15f)
                            )
                            .clip(RoundedCornerShape(14.dp))
                            .background(style.background)
                            .border(
                                if (isSelected) 2.5.dp else 1.dp,
                                if (isSelected) Olive else Border,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                onSelectPhoto(null)
                                onStyleSelect(index)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected style",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))
                Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        text = designStyles[selectedStyleIndex].name,
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Espresso
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Community Visibility Toggle ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                Text(
                    text = "Share to Community",
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Espresso
                )
                Text(
                    text = "Allow others in the Makarios community to discover this declaration",
                    fontFamily = BodyFontFamily,
                    fontSize = 11.5.sp,
                    color = Stone
                )
            }
            Switch(
                checked = isShareToCommunity,
                onCheckedChange = onToggleShareToCommunity,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Olive,
                    uncheckedThumbColor = StoneMuted,
                    uncheckedTrackColor = Border
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Secondary Export actions ────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onShare,
                    border = BorderStroke(1.dp, Border),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(46.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = Espresso,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Share image",
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = Espresso
                        )
                    }
                }

                OutlinedButton(
                    onClick = onSaveAsWallpaper,
                    border = BorderStroke(1.dp, Border),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(46.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wallpaper,
                            contentDescription = null,
                            tint = Espresso,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Set wallpaper",
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = Espresso
                        )
                    }
                }
            }
        }
    }
}
