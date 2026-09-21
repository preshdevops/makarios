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
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.*
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
import com.makarios.app.ui.theme.*
import com.makarios.app.util.ShareHelper

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
        "Espresso",
        Brush.verticalGradient(listOf(Color(0xFF342E2B), Color(0xFF24201D), Color(0xFF171513))),
        Color.White,
        Color(0xFFF7EBE7)
    ),
    DesignStyle(
        "Porcelain",
        Brush.verticalGradient(listOf(Color(0xFFFBF9F5), Color(0xFFF3EFE8))),
        Color(0xFF2C2622),
        Color(0xFFA85842)
    ),
    DesignStyle(
        "Terracotta",
        Brush.verticalGradient(listOf(Color(0xFF63382B), Color(0xFF3A241E), Color(0xFF1F1614))),
        Color.White,
        Color(0xFFF7EBE7)
    ),
    DesignStyle(
        "Sage",
        Brush.verticalGradient(listOf(Color(0xFF314238), Color(0xFF222F28), Color(0xFF17201B))),
        Color.White,
        Color(0xFFF0F4F1)
    )
)

// ── Scripture Matching (simulated — will wire to AI in future) ───
fun matchScripture(declaration: String): Pair<String, String> {
    val lower = declaration.lowercase()
    return when {
        lower.contains("fear") || lower.contains("afraid") || lower.contains("anxious") ->
            Pair("Isaiah 41:10", "Do not fear, for I am with you; do not be dismayed, for I am your God. I will strengthen you and help you; I will uphold you with my righteous right hand.")
        lower.contains("strong") || lower.contains("strength") || lower.contains("weak") ->
            Pair("2 Corinthians 12:9", "My grace is sufficient for you, for my power is made perfect in weakness.")
        lower.contains("purpose") || lower.contains("plan") || lower.contains("future") ->
            Pair("Jeremiah 29:11", "For I know the plans I have for you, declares the Lord, plans to prosper you and not to harm you, plans to give you hope and a future.")
        lower.contains("peace") || lower.contains("still") || lower.contains("rest") ->
            Pair("Philippians 4:7", "And the peace of God, which transcends all understanding, will guard your hearts and your minds in Christ Jesus.")
        lower.contains("love") || lower.contains("loved") || lower.contains("worth") ->
            Pair("Romans 8:38–39", "For I am convinced that neither death nor life, neither angels nor demons, neither the present nor the future, nor any powers, neither height nor depth, nor anything else in all creation, will be able to separate us from the love of God.")
        lower.contains("identity") || lower.contains("who i am") || lower.contains("child") ->
            Pair("1 John 3:1", "See what great love the Father has lavished on us, that we should be called children of God! And that is what we are!")
        lower.contains("heal") || lower.contains("broken") || lower.contains("restore") ->
            Pair("Psalm 147:3", "He heals the brokenhearted and binds up their wounds.")
        lower.contains("provision") || lower.contains("need") || lower.contains("provide") ->
            Pair("Philippians 4:19", "And my God will meet all your needs according to the riches of his glory in Christ Jesus.")
        lower.contains("joy") || lower.contains("happy") || lower.contains("delight") ->
            Pair("Nehemiah 8:10", "Do not grieve, for the joy of the Lord is your strength.")
        lower.contains("courage") || lower.contains("bold") || lower.contains("brave") ->
            Pair("Joshua 1:9", "Have I not commanded you? Be strong and courageous. Do not be afraid; do not be discouraged, for the Lord your God will be with you wherever you go.")
        else ->
            Pair("Psalm 139:14", "I praise you because I am fearfully and wonderfully made; your works are wonderful, I know that full well.")
    }
}

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
    var selectedFormatIndex by remember { mutableIntStateOf(0) }
    var selectedStyleIndex by remember { mutableIntStateOf(0) }

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
                            onClick = {
                                if (declarationText.trim().length >= 10) {
                                    val (ref, scr) = matchScripture(declarationText)
                                    matchedReference = ref
                                    matchedScripture = scr
                                    stage = CreateStage.MATCH
                                } else {
                                    Toast.makeText(context, "Write a few words first", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Terracotta, contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Match", fontFamily = BodyFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
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
                                    tone = AffirmationTone.RESOLUTE,
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
                    onMatch = {
                        if (declarationText.trim().length >= 10) {
                            val (ref, scr) = matchScripture(declarationText)
                            matchedReference = ref
                            matchedScripture = scr
                            stage = CreateStage.MATCH
                        } else {
                            Toast.makeText(context, "Write a few words first", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                // ── STAGE 2: MATCH ────────────────────────────────
                CreateStage.MATCH -> MatchStage(
                    declaration = declarationText,
                    reference = matchedReference,
                    scripture = matchedScripture,
                    onBack = { stage = CreateStage.WRITE },
                    onAccept = { stage = CreateStage.DESIGN },
                    onTryDifferent = {
                        // Rotate through alternatives — simplified for now
                        val alts = listOf(
                            Pair("Psalm 23:1", "The Lord is my shepherd; I shall not want."),
                            Pair("Romans 8:28", "And we know that in all things God works for the good of those who love him, who have been called according to his purpose."),
                            Pair("Isaiah 40:31", "But those who hope in the Lord will renew their strength. They will soar on wings like eagles; they will run and not grow weary, they will walk and not be faint.")
                        )
                        val current = alts.indexOfFirst { it.first == matchedReference }
                        val next = alts[(current + 1) % alts.size]
                        matchedReference = next.first
                        matchedScripture = next.second
                    }
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
                            tone = AffirmationTone.RESOLUTE,
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
                            tone = AffirmationTone.RESOLUTE,
                            imageUrl = "https://images.unsplash.com/photo-1507652313519-d4e9174996dd?auto=format&fit=crop&w=1000&q=85",
                            isFavorite = true,
                            personalDeclaration = declarationText
                        )
                        AffirmationRepository.addPersonalAffirmation(newAffirmation)
                        Toast.makeText(context, "Wallpaper saved and added to Personal", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

// ── STAGE 1 COMPOSABLE: WRITE ─────────────────────────────────────
@Composable
private fun WriteStage(
    declarationText: String,
    onDeclarationChange: (String) -> Unit,
    seedAffirmation: Affirmation?,
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
            ).forEach { (label, _) ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Surface)
                        .border(1.dp, Border, RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        fontFamily = BodyFontFamily,
                        fontSize = 12.sp,
                        color = Espresso
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
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(17.dp))
                Text(
                    text = "Find Matching Scripture",
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
    onBack: () -> Unit,
    onAccept: () -> Unit,
    onTryDifferent: () -> Unit
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
                    elevation = 8.dp,
                    shape = RoundedCornerShape(22.dp),
                    spotColor = Espresso.copy(alpha = 0.20f)
                )
                .clip(RoundedCornerShape(22.dp))
                .background(AtmosphericGradient)
                .padding(24.dp)
        ) {
            Column {
                // Declaration
                Text(
                    text = "“$declaration”",
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 22.sp,
                    lineHeight = 31.sp,
                    letterSpacing = (-0.2).sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Hairline divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.18f))
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Scripture
                Text(
                    text = "“$scripture”",
                    fontFamily = DisplayFontFamily,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = reference.uppercase(),
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    letterSpacing = 1.4.sp,
                    color = Color(0xFFFAEDE8)
                )
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
    onSaveAsWallpaper: () -> Unit
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
                onClick = { /* Share intent */ },
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
