package com.makarios.app.ui.screens

import android.app.WallpaperManager
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.animation.Crossfade
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.MenuBook
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
import coil.compose.AsyncImage
import com.makarios.app.data.*
import com.makarios.app.ui.components.BibleReaderSheet
import com.makarios.app.ui.components.Pairing
import com.makarios.app.ui.theme.*
import com.makarios.app.util.ExportFormat
import com.makarios.app.util.LightCanvas
import com.makarios.app.util.ShareHelper
import java.io.File

@Composable
fun CreateScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var step by remember { mutableStateOf(1) }
    var declarationText by remember { mutableStateOf("") }
    var tone by remember { mutableStateOf("Still") }
    var selectedLight by remember { mutableStateOf(Light.Dawn) }
    var formatName by remember { mutableStateOf("Story") }
    var selectedPhotoUrl by remember { mutableStateOf<String?>(null) }
    var showBibleReader by remember { mutableStateOf(false) }

    // Verses matched for the declaration
    var matchedVerses by remember { mutableStateOf<List<ScriptureVerse>>(emptyList()) }
    var currentVerseIndex by remember { mutableStateOf(0) }

    val currentVerse: ScriptureVerse? = remember(matchedVerses, currentVerseIndex) {
        if (matchedVerses.isNotEmpty() && currentVerseIndex < matchedVerses.size) {
            matchedVerses[currentVerseIndex]
        } else null
    }

    fun saveCreatedAffirmation(): Affirmation {
        val aff = Affirmation(
            id = "custom-${System.currentTimeMillis()}",
            declaration = declarationText.trim(),
            scriptureText = currentVerse?.text ?: "The Lord is my strength and my shield.",
            reference = currentVerse?.reference ?: "Psalm 28:7",
            context = "Authored in Makarios Declare Studio.",
            category = "Personal",
            tone = when (tone) {
                "Bold" -> AffirmationTone.RESOLUTE
                "Gentle" -> AffirmationTone.GENTLE
                else -> AffirmationTone.STILL
            },
            imageUrl = selectedPhotoUrl ?: PhotoLibrary.getForRole(PhotoRole.AFFIRMATION_PERSONAL_1).url(width = 1200),
            isFavorite = true
        )
        AffirmationRepository.addPersonalAffirmation(aff)
        return aff
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Porcelain)
            .padding(top = 48.dp)
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = {
                    if (step > 1) step -= 1 else onNavigateBack()
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Ink.copy(alpha = 0.05f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Ink
                )
            }

            Text(
                text = when (step) {
                    1 -> "1. Write Truth"
                    2 -> "2. Anchor in Scripture"
                    else -> "3. Format & Meditate"
                },
                style = MakariosTypography.labelLarge,
                color = Ink
            )

            Spacer(modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Thin 3-segment progress bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Ink)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(if (step >= 2) Ink else Ink.copy(alpha = 0.2f))
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(if (step >= 3) Ink else Ink.copy(alpha = 0.2f))
            )
        }

        Crossfade(targetState = step, label = "CreateSteps") { currentStep ->
            when (currentStep) {
                1 -> WriteStep(
                    text = declarationText,
                    onTextChanged = { declarationText = it },
                    tone = tone,
                    onToneChanged = { tone = it },
                    onNext = {
                        val selectedTone = when (tone) {
                            "Bold" -> AffirmationTone.RESOLUTE
                            "Gentle" -> AffirmationTone.GENTLE
                            else -> AffirmationTone.STILL
                        }
                        val results = ScriptureMatcher.match(declarationText, selectedTone, limit = 15)
                        if (results.isNotEmpty()) {
                            matchedVerses = results.map { it.verse }
                        } else {
                            matchedVerses = ScriptureDatabase.verses.take(10)
                        }
                        currentVerseIndex = 0
                        step = 2
                    }
                )
                2 -> VerseStep(
                    declarationText = declarationText,
                    currentVerse = currentVerse,
                    currentIndex = currentVerseIndex,
                    totalCount = matchedVerses.size,
                    onNextVerse = {
                        if (matchedVerses.isNotEmpty()) {
                            currentVerseIndex = (currentVerseIndex + 1) % matchedVerses.size
                        }
                    },
                    onSearchBible = { showBibleReader = true },
                    onEditDeclaration = { step = 1 },
                    onUseThisVerse = { step = 3 }
                )
                3 -> LookStep(
                    declarationText = declarationText,
                    verseText = currentVerse?.text ?: "The Lord is my strength and my shield.",
                    verseReference = currentVerse?.reference ?: "Psalm 28:7",
                    selectedLight = selectedLight,
                    onLightChanged = { selectedLight = it },
                    format = formatName,
                    onFormatChanged = { formatName = it },
                    selectedPhotoUrl = selectedPhotoUrl,
                    onPhotoSelected = { selectedPhotoUrl = it },
                    onShare = {
                        val aff = saveCreatedAffirmation()
                        val exportFmt = when (formatName) {
                            "Status", "Story" -> ExportFormat.Story
                            "Square" -> ExportFormat.Square
                            "X" -> ExportFormat.X
                            else -> ExportFormat.Portrait
                        }
                        ShareHelper.shareGeneric(
                            context = context,
                            affirmation = aff,
                            light = selectedLight,
                            format = exportFmt
                        )
                        Toast.makeText(context, "Saved to your Kept collection!", Toast.LENGTH_SHORT).show()
                    },
                    onSavePhotos = {
                        val aff = saveCreatedAffirmation()
                        try {
                            val renderer = LightCanvas(context)
                            val cacheFile = File(context.cacheDir, "declaration_${System.currentTimeMillis()}.png")
                            renderer.render(
                                declaration = aff.declaration,
                                verseText = aff.scriptureText,
                                verseReference = aff.reference,
                                light = selectedLight,
                                format = ExportFormat.Story,
                                outputFile = cacheFile
                            )
                            Toast.makeText(context, "Saved to device & Kept library!", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Saved to Kept collection", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onSetWallpaper = {
                        val aff = saveCreatedAffirmation()
                        try {
                            val wm = WallpaperManager.getInstance(context)
                            val renderer = LightCanvas(context)
                            val file = File(context.cacheDir, "custom_wallpaper_${System.currentTimeMillis()}.png")
                            renderer.render(
                                declaration = aff.declaration,
                                verseText = aff.scriptureText,
                                verseReference = aff.reference,
                                light = selectedLight,
                                format = ExportFormat.Wallpaper,
                                outputFile = file
                            )
                            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                            wm.setBitmap(bitmap)
                            Toast.makeText(context, "Wallpaper updated & saved to Kept!", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not set wallpaper", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }

    if (showBibleReader) {
        BibleReaderSheet(
            onDismiss = { showBibleReader = false }
        )
    }
}

@Composable
fun WriteStep(
    text: String,
    onTextChanged: (String) -> Unit,
    tone: String,
    onToneChanged: (String) -> Unit,
    onNext: () -> Unit
) {
    val starters = listOf(
        "I walk in perfect peace because God guards my heart.",
        "I lack nothing, for the Lord is my shepherd.",
        "I am strong in the grace that is in Christ Jesus.",
        "My steps are ordered and my future is secure in God's hands."
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "What truth are you declaring?",
            style = MakariosTypography.displaySmall,
            color = Ink
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 160.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Ink.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(16.dp)
        ) {
            BasicTextField(
                value = text,
                onValueChange = { if (it.length <= 280) onTextChanged(it) },
                textStyle = MakariosTypography.displaySmall.copy(color = Ink, fontSize = 20.sp),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (text.isEmpty()) {
                        Text(
                            text = "I am...",
                            style = MakariosTypography.displaySmall.copy(
                                color = Ink.copy(alpha = 0.35f),
                                fontSize = 20.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        )
                    }
                    innerTextField()
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Say it in first person.", style = MakariosTypography.labelSmall, color = Ink.copy(alpha = 0.6f))
            Text("${text.length} / 280", style = MakariosTypography.labelSmall, color = Ink.copy(alpha = 0.6f))
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Tone Selector
        Text("Tone", style = MakariosTypography.labelMedium, color = Ink.copy(alpha = 0.8f))
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(22.dp))
                .border(1.dp, Ink.copy(alpha = 0.15f), RoundedCornerShape(22.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("Still", "Bold", "Gentle").forEach { t ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (tone == t) Ink else Color.Transparent)
                        .clickable { onToneChanged(t) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = t,
                        style = MakariosTypography.labelLarge,
                        color = if (tone == t) Cream else Ink
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Starters
        Text("Or begin with one of these", style = MakariosTypography.labelMedium, color = Ink.copy(alpha = 0.8f))
        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            starters.forEach { starter ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Ink.copy(alpha = 0.04f))
                        .clickable { onTextChanged(starter) }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "“$starter”",
                        style = MakariosTypography.bodyMedium,
                        color = Ink
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Cream),
            shape = RoundedCornerShape(28.dp),
            enabled = text.isNotBlank()
        ) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Find my verse", style = MakariosTypography.labelLarge)
        }
    }
}

@Composable
fun VerseStep(
    declarationText: String,
    currentVerse: ScriptureVerse?,
    currentIndex: Int,
    totalCount: Int,
    onNextVerse: () -> Unit,
    onSearchBible: () -> Unit,
    onEditDeclaration: () -> Unit,
    onUseThisVerse: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Your words, standing on this verse.",
            style = MakariosTypography.labelMedium,
            color = Ink.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Pairing Preview Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 240.dp)
                .clip(RoundedCornerShape(20.dp))
                .lightBackground(Light.Midday)
                .padding(24.dp)
        ) {
            Pairing(
                declaration = declarationText,
                verseText = currentVerse?.text ?: "For we are his workmanship, created in Christ Jesus for good works...",
                verseReference = currentVerse?.reference ?: "Ephesians 2:10",
                light = Light.Midday,
                isCompact = true
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Verse Cycling Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onNextVerse,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Ink.copy(alpha = 0.2f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink)
            ) {
                Text("Another verse", style = MakariosTypography.labelLarge)
            }

            Text(
                text = "${currentIndex + 1} of ${totalCount.coerceAtLeast(1)}",
                style = MakariosTypography.labelMedium,
                color = Ink.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onSearchBible) {
                Icon(Icons.Outlined.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp), tint = Ink)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Search the Bible", style = MakariosTypography.labelLarge, color = Ink)
            }

            TextButton(onClick = onEditDeclaration) {
                Text("Edit declaration", style = MakariosTypography.labelLarge, color = Ink.copy(alpha = 0.7f))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onUseThisVerse,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Cream),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("Use this verse", style = MakariosTypography.labelLarge)
        }
    }
}

@Composable
fun LookStep(
    declarationText: String,
    verseText: String,
    verseReference: String,
    selectedLight: Light,
    onLightChanged: (Light) -> Unit,
    format: String,
    onFormatChanged: (String) -> Unit,
    selectedPhotoUrl: String?,
    onPhotoSelected: (String?) -> Unit,
    onShare: () -> Unit,
    onSavePhotos: () -> Unit,
    onSetWallpaper: () -> Unit
) {
    val createPhotos = remember { PhotoLibrary.getCreateBackgrounds() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Live Visual Preview Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(24.dp))
                .lightBackground(selectedLight)
        ) {
            if (selectedPhotoUrl != null) {
                AsyncImage(
                    model = selectedPhotoUrl,
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
                                0.0f to selectedLight.top.copy(alpha = 0.82f),
                                1.0f to selectedLight.bottom.copy(alpha = 0.94f)
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Pairing(
                    declaration = declarationText,
                    verseText = verseText,
                    verseReference = verseReference,
                    light = selectedLight,
                    isCompact = true
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Format Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, Ink.copy(alpha = 0.15f), RoundedCornerShape(20.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("Story", "Square", "Status", "X").forEach { f ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (format == f) Ink else Color.Transparent)
                        .clickable { onFormatChanged(f) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = f,
                        style = MakariosTypography.labelSmall,
                        color = if (format == f) Cream else Ink
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 8 Light Swatches
        Text("Light Palette", style = MakariosTypography.labelMedium, color = Ink.copy(alpha = 0.8f))
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Light.values().forEach { l ->
                val isSelected = selectedLight == l
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onLightChanged(l) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .lightBackground(l)
                            .border(
                                width = if (isSelected) 2.5.dp else 0.dp,
                                color = if (isSelected) Ink else Color.Transparent,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = l.text, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(l.name, style = MakariosTypography.labelSmall.copy(fontSize = 11.sp), color = Ink)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Photo backgrounds row
        Text("Photography Layer", style = MakariosTypography.labelMedium, color = Ink.copy(alpha = 0.8f))
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // None / Pure Gradient option
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = if (selectedPhotoUrl == null) 2.dp else 1.dp,
                        color = if (selectedPhotoUrl == null) Ink else Ink.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onPhotoSelected(null) },
                contentAlignment = Alignment.Center
            ) {
                Text("None", style = MakariosTypography.labelSmall, color = Ink)
            }

            // Create background photos
            createPhotos.forEach { entry ->
                val isSel = selectedPhotoUrl == entry.url(width = 800)
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = if (isSel) 2.dp else 0.dp,
                            color = if (isSel) Ink else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onPhotoSelected(entry.url(width = 800)) }
                ) {
                    AsyncImage(
                        model = entry.url(width = 300),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        colorFilter = WarmPhotoGrade
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Main Share Button
        Button(
            onClick = onShare,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Cream),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("Share Declaration", style = MakariosTypography.labelLarge)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Actions: Save to Photos & Set as Wallpaper
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onSavePhotos) {
                Icon(Icons.Outlined.Image, contentDescription = null, modifier = Modifier.size(18.dp), tint = Ink)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save to photos", style = MakariosTypography.labelLarge, color = Ink)
            }

            TextButton(onClick = onSetWallpaper) {
                Icon(Icons.Outlined.Wallpaper, contentDescription = null, modifier = Modifier.size(18.dp), tint = Ink)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Set as wallpaper", style = MakariosTypography.labelLarge, color = Ink)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}
