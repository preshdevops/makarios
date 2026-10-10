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
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.data.*
import com.makarios.app.ui.components.BibleReaderSheet
import com.makarios.app.ui.components.Pairing
import com.makarios.app.ui.theme.*
import com.makarios.app.ui.wallpaper.AutoStyleSelector
import com.makarios.app.ui.wallpaper.Style
import com.makarios.app.ui.wallpaper.StyleRegistry
import com.makarios.app.ui.wallpaper.StyleSpec
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
    var selectedStyle by remember { mutableStateOf<Style>(StyleRegistry.PAIRING) }
    var formatName by remember { mutableStateOf("Story") }
    var selectedPhotoUrl by remember { mutableStateOf<String?>(null) }
    var showBibleReader by remember { mutableStateOf(false) }
    var showNotificationAsk by remember { mutableStateOf(false) }

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
            imageUrl = selectedPhotoUrl ?: "",
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
                    else -> "3. Look (styles)"
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
                    onUseThisVerse = {
                        selectedStyle = AutoStyleSelector.pickStyle(
                            declaration = declarationText,
                            verse = currentVerse?.text,
                            reference = currentVerse?.reference,
                            light = selectedLight
                        )
                        step = 3
                    }
                )
                3 -> LookStep(
                    declarationText = declarationText,
                    verseText = currentVerse?.text ?: "The Lord is my strength and my shield.",
                    verseReference = currentVerse?.reference ?: "Psalm 28:7",
                    selectedLight = selectedLight,
                    onLightChanged = { selectedLight = it },
                    selectedStyle = selectedStyle,
                    onStyleChanged = { selectedStyle = it },
                    format = formatName,
                    onFormatChanged = { formatName = it },
                    onShare = {
                        val aff = saveCreatedAffirmation()
                        showNotificationAsk = true
                        val exportFmt = ExportFormat.from(formatName)
                        ShareHelper.shareGeneric(
                            context = context,
                            affirmation = aff,
                            light = selectedLight,
                            format = exportFmt,
                            style = selectedStyle
                        )
                        Toast.makeText(context, "Saved to your Kept collection!", Toast.LENGTH_SHORT).show()
                    },
                    onSavePhotos = {
                        val aff = saveCreatedAffirmation()
                        showNotificationAsk = true
                        val exportFmt = ExportFormat.from(formatName)
                        val saved = ShareHelper.saveToPhotos(context, aff, selectedLight, exportFmt, style = selectedStyle)
                        Toast.makeText(context, if (saved != null) "Saved to Pictures/Makarios" else "Could not save image", Toast.LENGTH_SHORT).show()
                    },
                    onSetWallpaper = {
                        val aff = saveCreatedAffirmation()
                        showNotificationAsk = true
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
                                style = selectedStyle,
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

    if (showNotificationAsk && !com.makarios.app.util.NotificationStore.askShown(context)) {
        NotificationSoftAsk { showNotificationAsk = false }
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

/** Cache for offscreen preview bitmaps keyed by (styleId, lightName, formatName, size, textHash) */
private val previewBitmapCache = mutableMapOf<String, android.graphics.Bitmap>()

@Composable
fun LookStep(
    declarationText: String,
    verseText: String,
    verseReference: String,
    selectedLight: Light,
    onLightChanged: (Light) -> Unit,
    selectedStyle: Style,
    onStyleChanged: (Style) -> Unit,
    format: String,
    onFormatChanged: (String) -> Unit,
    onShare: () -> Unit,
    onSavePhotos: () -> Unit,
    onSetWallpaper: () -> Unit
) {
    val exportFormat = remember(format) { ExportFormat.from(format) }
    val spec = remember(declarationText, verseText, verseReference) {
        StyleSpec(declaration = declarationText, verse = verseText, reference = verseReference)
    }

    // Performance: render 200x433 preview in background coroutine at 1/4 scale with cache
    val previewBitmap by produceState<android.graphics.Bitmap?>(
        initialValue = null,
        selectedStyle.id,
        selectedLight.name,
        format,
        declarationText,
        verseText
    ) {
        value = withContext(Dispatchers.Default) {
            val key = "${selectedStyle.id}:${selectedLight.name}:${exportFormat.name}:200x433:${declarationText.hashCode()}:${verseText.hashCode()}"
            previewBitmapCache.getOrPut(key) {
                // Render at exact preview aspect ratio (200 x 433)
                val (w, h) = when (exportFormat) {
                    ExportFormat.Square -> 200 to 200
                    ExportFormat.Portrait -> 200 to 250
                    ExportFormat.X -> 320 to 180
                    ExportFormat.Story, ExportFormat.Wallpaper -> 200 to 433
                }
                selectedStyle.render(spec, selectedLight, IntSize(w, h))
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(12.dp))

        // 1. Live preview: 200x433 (the same renderer scaled)
        Box(
            Modifier
                .width(200.dp)
                .height(433.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(selectedLight.bottom)
                .border(1.dp, Ink.copy(alpha = 0.12f), RoundedCornerShape(22.dp)),
            contentAlignment = Alignment.Center
        ) {
            previewBitmap?.let { bmp ->
                androidx.compose.foundation.Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Live style preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } ?: CircularProgressIndicator(color = selectedLight.text, modifier = Modifier.size(28.dp))
        }

        Spacer(Modifier.height(24.dp))

        // 2. Style row: 12 thumbnails (56x121, horizontally scrollable, selected = 2dp ink outline with 3dp offset)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            Text("Style", style = MakariosTypography.labelMedium, color = Ink.copy(alpha = 0.8f))
        }
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StyleRegistry.all.forEach { style ->
                val isSelected = style.id == selectedStyle.id
                val thumbBitmap by produceState<android.graphics.Bitmap?>(null, style.id, selectedLight.name) {
                    value = withContext(Dispatchers.Default) {
                        val thumbKey = "${style.id}:${selectedLight.name}:thumb:56x121:${spec.shortText.hashCode()}"
                        previewBitmapCache.getOrPut(thumbKey) {
                            style.render(spec, selectedLight, IntSize(56, 121))
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        onStyleChanged(style)
                        if (!style.supports(exportFormat)) {
                            onFormatChanged("Story")
                        }
                    }
                ) {
                    // Outer box providing 2dp ink outline with 3dp offset when selected
                    Box(
                        Modifier
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .border(2.dp, Ink, RoundedCornerShape(14.dp))
                                        .padding(3.dp)
                                } else {
                                    Modifier.padding(5.dp)
                                }
                            )
                            .size(width = 56.dp, height = 121.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(selectedLight.top),
                        contentAlignment = Alignment.Center
                    ) {
                        thumbBitmap?.let {
                            androidx.compose.foundation.Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = style.displayName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = style.displayName,
                        style = MakariosTypography.labelSmall.copy(fontSize = 11.sp),
                        color = if (isSelected) Ink else Ink.copy(alpha = 0.65f),
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // 3. Light row: Auto + 8 swatches (40dp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            Text("Light", style = MakariosTypography.labelMedium, color = Ink.copy(alpha = 0.8f))
        }
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Auto swatch
            val isAuto = selectedLight == Light.forNow()
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Ink.copy(alpha = 0.08f))
                    .border(if (isAuto) 2.dp else 1.dp, if (isAuto) Ink else Ink.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                    .clickable { onLightChanged(Light.forNow()) },
                contentAlignment = Alignment.Center
            ) {
                Text("Auto", style = MakariosTypography.labelSmall.copy(fontSize = 11.sp), color = Ink)
            }

            // 8 Lights swatches
            Light.values().forEach { lightOption ->
                val isSelected = lightOption == selectedLight
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(lightOption.top)
                        .border(if (isSelected) 2.dp else 0.dp, if (isSelected) Ink else Color.Transparent, RoundedCornerShape(10.dp))
                        .clickable { onLightChanged(lightOption) }
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // 4. Format row: Story, Square, Portrait, X, Wallpaper
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            Text("Format", style = MakariosTypography.labelMedium, color = Ink.copy(alpha = 0.8f))
        }
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Story", "Square", "Portrait", "X", "Wallpaper").forEach { fmt ->
                val isSelected = format.equals(fmt, ignoreCase = true)
                val isSupported = selectedStyle.supports(fmt)

                Box(
                    Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isSelected) Ink else Ink.copy(alpha = if (isSupported) 0.06f else 0.02f))
                        .clickable(enabled = isSupported) { onFormatChanged(fmt) }
                        .padding(horizontal = 16.dp, vertical = 9.dp)
                ) {
                    Text(
                        text = fmt,
                        style = MakariosTypography.labelMedium,
                        color = if (isSelected) Cream else if (isSupported) Ink else Ink.copy(alpha = 0.3f)
                    )
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        // 5. Ink "Share" pill
        Button(
            onClick = onShare,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Cream),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("Share", style = MakariosTypography.labelLarge)
        }

        Spacer(Modifier.height(10.dp))

        // 6. Text buttons: "Save to photos" and "Set as wallpaper"
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onSavePhotos) {
                Icon(Icons.Outlined.Image, contentDescription = null, tint = Ink, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Save to photos", color = Ink, style = MakariosTypography.labelLarge)
            }

            TextButton(onClick = onSetWallpaper) {
                Icon(Icons.Outlined.Wallpaper, contentDescription = null, tint = Ink, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Set as wallpaper", color = Ink, style = MakariosTypography.labelLarge)
            }
        }

        Spacer(Modifier.height(36.dp))
    }
}
