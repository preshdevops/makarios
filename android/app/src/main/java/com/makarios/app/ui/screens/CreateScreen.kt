package com.makarios.app.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.*
import com.makarios.app.ui.components.ScripturePickerSheet
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.data.Affirmation
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.data.AffirmationTone
import com.makarios.app.data.ScriptureMatcher
import com.makarios.app.data.VectorSearchEngine
import com.makarios.app.data.VerseMatch
import com.makarios.app.ui.theme.*
import com.makarios.app.util.ShareHelper
import kotlinx.coroutines.launch

// ── Create Screen Stages ─────────────────────────────────────────
enum class CreateStage {
    WRITE,   // 1. User writes their declaration
    MATCH,   // 2. Matched scripture is shown
    DESIGN   // 3. Design Studio — format, style, share
}

// ── Output Formats ───────────────────────────────────────────────
data class ShareFormat(val label: String, val ratio: Float, val size: String)

val shareFormats = listOf(
    ShareFormat("Story", 9f / 16f, "9:16"),
    ShareFormat("Square", 1f, "1:1"),
    ShareFormat("Status", 4f / 5f, "4:5"),
    ShareFormat("X Card", 16f / 9f, "16:9"),
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
        Terracotta
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
    var selectedFormatIndex by remember { mutableIntStateOf(0) }
    var selectedStyleIndex by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    val runMatch: () -> Unit = {
        if (declarationText.trim().length >= 10 && !isMatching) {
            isMatching = true
            coroutineScope.launch {
                try {
                    val vectorResults = VectorSearchEngine.getInstance(context).search(declarationText, topK = 8)
                    if (vectorResults.isNotEmpty()) {
                        vectorMatches = vectorResults
                        matchIndex = 0
                        matchedReference = vectorResults[0].reference
                        matchedScripture = vectorResults[0].text
                        stage = CreateStage.MATCH
                    } else {
                        val fallback = ScriptureMatcher.match(declarationText, selectedTone)
                        matchResults = fallback
                        matchIndex = 0
                        if (fallback.isNotEmpty()) {
                            matchedReference = fallback[0].verse.reference
                            matchedScripture = fallback[0].verse.text
                        }
                        stage = CreateStage.MATCH
                    }
                } catch (e: Exception) {
                    val fallback = ScriptureMatcher.match(declarationText, selectedTone)
                    matchResults = fallback
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
        } else if (declarationText.trim().length < 10) {
            Toast.makeText(context, "Write a few words first", Toast.LENGTH_SHORT).show()
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
                        .clickable(onClick = onBack),
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
                                        isActive -> Espresso
                                        isPast -> Terracotta
                                        else -> Border
                                    }
                                )
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (isPast) "✓ $label" else label,
                                fontFamily = BodyFontFamily,
                                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 11.sp,
                                color = if (isActive || isPast) Color.White else StoneMuted
                            )
                        }
                    }
                }

                // Right action — context-aware
                when (stage) {
                    CreateStage.WRITE -> {
                        Button(
                            onClick = runMatch,
                            enabled = !isMatching,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Terracotta, contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(
                                text = if (isMatching) "Matching…" else "Match",
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
                                containerColor = Espresso, contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Design", fontFamily = BodyFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                    CreateStage.DESIGN -> {
                        Button(
                            onClick = {
                                val newAffirmation = Affirmation(
                                    id = "personal-${System.currentTimeMillis()}",
                                    declaration = declarationText,
                                    scriptureText = matchedScripture.ifBlank { "I praise you because I am fearfully and wonderfully made; your works are wonderful, I know that full well." },
                                    reference = matchedReference.ifBlank { "PSALM 139:14" },
                                    context = "Personal declaration created in Makarios Studio.",
                                    category = "Personal",
                                    tone = selectedTone ?: AffirmationTone.RESOLUTE,
                                    imageUrl = "https://images.unsplash.com/photo-1507652313519-d4e9174996dd?auto=format&fit=crop&w=1000&q=85",
                                    isFavorite = true,
                                    personalDeclaration = declarationText
                                )
                                AffirmationRepository.addPersonalAffirmation(newAffirmation)
                                ShareHelper.shareAffirmation(context, newAffirmation)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Terracotta, contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                Text("Share", fontFamily = BodyFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
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
                    onSaveToGallery = {
                        val newAffirmation = Affirmation(
                            id = "personal-${System.currentTimeMillis()}",
                            declaration = declarationText,
                            scriptureText = matchedScripture.ifBlank { "I praise you because I am fearfully and wonderfully made; your works are wonderful, I know that full well." },
                            reference = matchedReference.ifBlank { "PSALM 139:14" },
                            context = "Personal declaration created in Makarios Studio.",
                            category = "Personal",
                            tone = selectedTone ?: AffirmationTone.RESOLUTE,
                            imageUrl = "https://images.unsplash.com/photo-1507652313519-d4e9174996dd?auto=format&fit=crop&w=1000&q=85",
                            isFavorite = true,
                            personalDeclaration = declarationText
                        )
                        AffirmationRepository.addPersonalAffirmation(newAffirmation)
                        Toast.makeText(context, "Saved image to gallery and Personal", Toast.LENGTH_SHORT).show()
                    },
                    onSaveAsWallpaper = {
                        val newAffirmation = Affirmation(
                            id = "personal-${System.currentTimeMillis()}",
                            declaration = declarationText,
                            scriptureText = matchedScripture.ifBlank { "I praise you because I am fearfully and wonderfully made; your works are wonderful, I know that full well." },
                            reference = matchedReference.ifBlank { "PSALM 139:14" },
                            context = "Personal declaration created in Makarios Studio.",
                            category = "Personal",
                            tone = selectedTone ?: AffirmationTone.RESOLUTE,
                            imageUrl = "https://images.unsplash.com/photo-1507652313519-d4e9174996dd?auto=format&fit=crop&w=1000&q=85",
                            isFavorite = true,
                            personalDeclaration = declarationText
                        )
                        AffirmationRepository.addPersonalAffirmation(newAffirmation)
                        Toast.makeText(context, "Wallpaper saved and added to Personal", Toast.LENGTH_SHORT).show()
                    },
                    onShare = {
                        val newAffirmation = Affirmation(
                            id = "personal-${System.currentTimeMillis()}",
                            declaration = declarationText,
                            scriptureText = matchedScripture.ifBlank { "I praise you because I am fearfully and wonderfully made; your works are wonderful, I know that full well." },
                            reference = matchedReference.ifBlank { "PSALM 139:14" },
                            context = "Personal declaration created in Makarios Studio.",
                            category = "Personal",
                            tone = selectedTone ?: AffirmationTone.RESOLUTE,
                            imageUrl = "https://images.unsplash.com/photo-1507652313519-d4e9174996dd?auto=format&fit=crop&w=1000&q=85",
                            isFavorite = true,
                            personalDeclaration = declarationText
                        )
                        AffirmationRepository.addPersonalAffirmation(newAffirmation)
                        ShareHelper.shareAffirmation(context, newAffirmation)
                    }
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
                    letterSpacing = 1.4.sp,
                    color = Terracotta
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp)) {
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
                    text = "$charCount characters",
                    fontFamily = BodyFontFamily,
                    fontSize = 11.sp,
                    color = if (charCount >= 10) Terracotta else StoneMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Tone pickers
        Text(
            text = "TONE",
            fontFamily = BodyFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
            letterSpacing = 1.4.sp,
            color = StoneMuted
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                "Still & Restful" to AffirmationTone.STILL,
                "Bold & Resolute" to AffirmationTone.RESOLUTE,
                "Gentle & Tender" to AffirmationTone.GENTLE
            ).forEach { (label, tone) ->
                val isSelected = selectedTone == tone
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) Espresso else Surface)
                        .border(
                            width = if (isSelected) 0.dp else 1.dp,
                            color = if (isSelected) Color.Transparent else Border,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { onToneSelect(if (isSelected) null else tone) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        fontFamily = BodyFontFamily,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 12.sp,
                        color = if (isSelected) Color.White else Espresso
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Inspiration seeds — pick from curated
        if (seedAffirmation == null) {
            Text(
                text = "START FROM A CURATED DECLARATION",
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                letterSpacing = 1.4.sp,
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
                                    color = Terracotta
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

        Spacer(modifier = Modifier.height(24.dp))

        // Main CTA
        Button(
            onClick = onMatch,
            enabled = !isMatching,
            colors = ButtonDefaults.buttonColors(
                containerColor = Espresso, contentColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isMatching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(17.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(17.dp))
                }
                Text(
                    text = if (isMatching) "Searching Scriptures…" else "Find Matching Scripture",
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
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
                            letterSpacing = 1.4.sp,
                            color = Terracotta
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
                .background(TerracottaLight)
                .border(1.dp, Terracotta.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Terracotta,
                    modifier = Modifier.size(18.dp).padding(top = 2.dp)
                )
                Column {
                    Text(
                        text = "This verse grounds your declaration",
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Espresso
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Makarios links your affirmation to scripture — not as the verse itself, but as a declaration rooted in its truth.",
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
                text = "MATCH ${matchIndex + 1} OF $matchCount",
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                letterSpacing = 1.4.sp,
                color = StoneMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )
        }

        // Action row
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
                    .height(50.dp)
            ) {
                Text(
                    text = "Try another",
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = Espresso
                )
            }

            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Espresso, contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(15.dp))
                    Text(
                        text = "Use this verse",
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
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
                    tint = Terracotta,
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

        Spacer(modifier = Modifier.height(10.dp))

        // Back to editing
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onBack)
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Edit my declaration",
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = Stone
            )
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
    onSaveToGallery: () -> Unit,
    onSaveAsWallpaper: () -> Unit,
    onShare: () -> Unit
) {
    val currentFormat = shareFormats[selectedFormatIndex]
    val currentStyle = designStyles[selectedStyleIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 48.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "Design Studio",
                fontFamily = DisplayFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                letterSpacing = (-0.3).sp,
                color = Espresso
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Choose a format and style. Share anywhere.",
                fontFamily = BodyFontFamily,
                fontSize = 13.sp,
                color = Stone
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

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

        // ── Live canvas preview ────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            // Dynamic aspect ratio based on format
            val previewHeight = (280f / currentFormat.ratio).coerceIn(160f, 380f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(previewHeight.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(20.dp),
                        spotColor = Espresso.copy(alpha = 0.25f)
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .background(currentStyle.background),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(22.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    // Category label
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
                        color = currentStyle.textColor,
                        maxLines = 5
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(1.dp)
                            .background(currentStyle.textColor.copy(alpha = 0.18f))
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Scripture reference
                    Text(
                        text = reference.uppercase(),
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        letterSpacing = 1.2.sp,
                        color = currentStyle.accentColor
                    )
                }

                // Makarios watermark
                Text(
                    text = "makarios",
                    fontFamily = DisplayFontFamily,
                    fontStyle = FontStyle.Italic,
                    fontSize = 9.sp,
                    color = currentStyle.textColor.copy(alpha = 0.3f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Style selector ────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "STYLE",
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
            designStyles.forEachIndexed { index, style ->
                val isSelected = selectedStyleIndex == index
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .shadow(
                            if (isSelected) 4.dp else 1.dp,
                            RoundedCornerShape(14.dp),
                            spotColor = Espresso.copy(alpha = 0.15f)
                        )
                        .clip(RoundedCornerShape(14.dp))
                        .background(style.background)
                        .border(
                            if (isSelected) 2.dp else 1.dp,
                            if (isSelected) Terracotta else Border,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { onStyleSelect(index) },
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

            // Style labels
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

        Spacer(modifier = Modifier.height(24.dp))

        // ── Share & Export actions ────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Primary: Share
            Button(
                onClick = onShare,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Terracotta, contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(17.dp))
                    Text(
                        text = "Share to Social",
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }

            // Secondary: Save to gallery
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onSaveToGallery,
                    border = BorderStroke(1.dp, Border),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(46.dp)
                ) {
                    Text(
                        text = "Save Image",
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = Espresso
                    )
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
                            Icons.Default.Wallpaper,
                            contentDescription = null,
                            tint = Espresso,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Wallpaper",
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
