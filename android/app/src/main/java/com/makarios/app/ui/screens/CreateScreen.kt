package com.makarios.app.ui.screens

import android.app.WallpaperManager
import android.graphics.BitmapFactory
import android.os.Build
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.makarios.app.data.ShareRepository
import com.makarios.app.data.ShareRequest
import com.makarios.app.data.ShareResult
import com.makarios.app.data.ShareTarget
import com.makarios.app.data.bible.VerseSelection
import com.makarios.app.ui.screens.bible.BiblePickerScreen
import com.makarios.app.ui.screens.bible.ChapterVersePickerScreen
import com.makarios.app.util.PronounRewriter
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun CreateScreen(
    onNavigateBack: () -> Unit,
    initialDeclaration: String? = null,
    initialVerseText: String? = null,
    initialReference: String? = null,
    initialStep: Int = 1,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var step by remember(initialStep) { mutableStateOf(initialStep) }
    var declarationText by remember(initialDeclaration) { mutableStateOf(initialDeclaration ?: "") }
    var tone by remember { mutableStateOf("Still") }
    var selectedLight by remember { mutableStateOf(Light.forNow()) }
    var selectedStyle by remember { mutableStateOf<Style>(StyleRegistry.PAIRING) }
    var formatName by remember { mutableStateOf("Story") }
    var selectedPhotoUrl by remember { mutableStateOf<String?>(null) }
    var showBiblePicker by remember { mutableStateOf(false) }
    var showNotificationAsk by remember { mutableStateOf(false) }
    var isSharingInProgress by remember { mutableStateOf(false) }

    // Verses matched for the declaration
    var matchedVerses by remember(initialVerseText, initialReference) {
        mutableStateOf(
            if (initialVerseText != null) {
                listOf(ScriptureVerse(text = initialVerseText, reference = initialReference ?: "Romans 8:28"))
            } else emptyList()
        )
    }
    var currentVerseIndex by remember { mutableStateOf(0) }
    var savedAffirmation by remember { mutableStateOf<Affirmation?>(null) }

    val currentVerse: ScriptureVerse? = remember(matchedVerses, currentVerseIndex) {
        if (matchedVerses.isNotEmpty() && currentVerseIndex < matchedVerses.size) {
            matchedVerses[currentVerseIndex]
        } else null
    }

    fun getOrCreateSavedAffirmation(): Affirmation {
        val existing = savedAffirmation
        if (existing != null) return existing
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
        savedAffirmation = aff
        return aff
    }

    // Step 4 (Declare Done) back press navigates to Today, not back into flow
    BackHandler {
        when (step) {
            1 -> onNavigateBack()
            2 -> step = 1
            3 -> step = 2
            4 -> onNavigateBack()
        }
    }

    val activeStepLight = selectedLight
    SystemBarsController(activeStepLight)

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind { drawLight(activeStepLight) }
    ) {
        if (step == 4) {
            val aff = getOrCreateSavedAffirmation()
            DeclareDoneStep(
                affirmation = aff,
                light = selectedLight,
                style = selectedStyle,
                onBackToToday = onNavigateBack,
                onDeclareAnother = {
                    declarationText = ""
                    savedAffirmation = null
                    matchedVerses = emptyList()
                    currentVerseIndex = 0
                    step = 1
                }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
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
                            .background(activeStepLight.text.copy(alpha = 0.08f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = activeStepLight.text
                        )
                    }

                    Text(
                        text = when (step) {
                            1 -> "1. Write Truth"
                            2 -> "2. Anchor in Scripture"
                            else -> "3. Look (styles)"
                        },
                        style = MakariosTypography.labelLarge,
                        color = activeStepLight.text
                    )

                    Spacer(modifier = Modifier.size(40.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Thin 3-segment progress bar following active Light text
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
                            .clip(CircleShape)
                            .background(activeStepLight.text)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(if (step >= 2) activeStepLight.text else activeStepLight.text.copy(alpha = 0.22f))
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(if (step >= 3) activeStepLight.text else activeStepLight.text.copy(alpha = 0.22f))
                    )
                }

                Crossfade(targetState = step, label = "CreateSteps") { currentStep ->
                    when (currentStep) {
                        1 -> WriteStep(
                            text = declarationText,
                            onTextChanged = { declarationText = it },
                            tone = tone,
                            onToneChanged = { tone = it },
                            light = activeStepLight,
                            onNext = {
                                val selectedTone = when (tone) {
                                    "Bold" -> AffirmationTone.RESOLUTE
                                    "Gentle" -> AffirmationTone.GENTLE
                                    else -> AffirmationTone.STILL
                                }
                                val results = ScriptureMatcher.match(declarationText, selectedTone, limit = 15)
                                matchedVerses = results.map { it.verse }
                                currentVerseIndex = 0
                                step = 2
                            }
                        )
                        2 -> VerseStep(
                            declarationText = declarationText,
                            light = activeStepLight,
                            matchedVerses = matchedVerses,
                            selectedVerseIndex = currentVerseIndex,
                            onSelectVerseIndex = { currentVerseIndex = it },
                            onSearchBible = { showBiblePicker = true },
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
                            isSharingInProgress = isSharingInProgress,
                            onShare = {
                                val aff = getOrCreateSavedAffirmation()
                                showNotificationAsk = true
                                val exportFmt = ExportFormat.from(formatName)
                                val spec = StyleSpec(
                                    declaration = aff.declaration,
                                    verse = aff.scriptureText,
                                    reference = aff.reference
                                )
                                coroutineScope.launch {
                                    isSharingInProgress = true
                                    ShareRepository.share(context, ShareRequest(spec, selectedLight, exportFmt, selectedStyle, ShareTarget.Chooser))
                                    isSharingInProgress = false
                                }
                            },
                            onDone = {
                                getOrCreateSavedAffirmation()
                                showNotificationAsk = true
                                step = 4
                            },
                            onSavePhotos = {
                                val aff = getOrCreateSavedAffirmation()
                                showNotificationAsk = true
                                val exportFmt = ExportFormat.from(formatName)
                                val spec = StyleSpec(declaration = aff.declaration, verse = aff.scriptureText, reference = aff.reference)
                                coroutineScope.launch {
                                    val res = ShareRepository.share(context, ShareRequest(spec, selectedLight, exportFmt, selectedStyle, ShareTarget.SaveToPhotos))
                                    if (res is ShareResult.Success) {
                                        Toast.makeText(context, "Saved to Pictures/Makarios", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onSetWallpaper = {
                                val aff = getOrCreateSavedAffirmation()
                                showNotificationAsk = true
                                val spec = StyleSpec(declaration = aff.declaration, verse = aff.scriptureText, reference = aff.reference)
                                coroutineScope.launch {
                                    val res = ShareRepository.share(context, ShareRequest(spec, selectedLight, ExportFormat.Wallpaper, selectedStyle, ShareTarget.SetWallpaper))
                                    if (res is ShareResult.Success) {
                                        Toast.makeText(context, "Wallpaper updated & saved to Kept!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onShareAsFile = {
                                val aff = getOrCreateSavedAffirmation()
                                showNotificationAsk = true
                                val exportFmt = ExportFormat.from(formatName)
                                val spec = StyleSpec(declaration = aff.declaration, verse = aff.scriptureText, reference = aff.reference)
                                coroutineScope.launch {
                                    ShareRepository.share(context, ShareRequest(spec, selectedLight, exportFmt, selectedStyle, ShareTarget.ShareAsFile))
                                }
                            },
                            onShareToInstagram = {
                                val aff = getOrCreateSavedAffirmation()
                                showNotificationAsk = true
                                val exportFmt = ExportFormat.from(formatName)
                                val spec = StyleSpec(declaration = aff.declaration, verse = aff.scriptureText, reference = aff.reference)
                                coroutineScope.launch {
                                    ShareRepository.share(context, ShareRequest(spec, selectedLight, exportFmt, selectedStyle, ShareTarget.InstagramStories))
                                }
                            },
                            onShareToWhatsApp = {
                                val aff = getOrCreateSavedAffirmation()
                                showNotificationAsk = true
                                val exportFmt = ExportFormat.from(formatName)
                                val spec = StyleSpec(declaration = aff.declaration, verse = aff.scriptureText, reference = aff.reference)
                                coroutineScope.launch {
                                    ShareRepository.share(context, ShareRequest(spec, selectedLight, exportFmt, selectedStyle, ShareTarget.WhatsAppStatus))
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showNotificationAsk && !com.makarios.app.util.NotificationStore.askShown(context)) {
        NotificationSoftAsk { showNotificationAsk = false }
    }

    if (showBiblePicker) {
        var pickerBook by remember { mutableStateOf<String?>(null) }
        var pickerChapter by remember { mutableStateOf(1) }

        if (pickerBook != null) {
            ChapterVersePickerScreen(
                bookName = pickerBook!!,
                initialChapter = pickerChapter,
                onNavigateBack = { pickerBook = null },
                onVerseSelected = { selection ->
                    matchedVerses = listOf(ScriptureVerse(text = selection.text, reference = selection.reference)) + matchedVerses
                    currentVerseIndex = 0
                    pickerBook = null
                    showBiblePicker = false
                }
            )
        } else {
            BiblePickerScreen(
                onNavigateBack = { showBiblePicker = false },
                onSelectBookChapter = { b, ch ->
                    pickerBook = b
                    pickerChapter = ch
                },
                onVerseSelected = { selection ->
                    matchedVerses = listOf(ScriptureVerse(text = selection.text, reference = selection.reference)) + matchedVerses
                    currentVerseIndex = 0
                    showBiblePicker = false
                }
            )
        }
    }
}

@Composable
fun WriteStep(
    text: String,
    onTextChanged: (String) -> Unit,
    tone: String,
    onToneChanged: (String) -> Unit,
    light: Light,
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
            color = light.text
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 160.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, light.text.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                .background(if (light.isDark) Color(0x28FFFFFF) else Color(0xE6FFFFFF))
                .padding(16.dp)
        ) {
            BasicTextField(
                value = text,
                onValueChange = { if (it.length <= 280) onTextChanged(it) },
                textStyle = MakariosTypography.displaySmall.copy(color = light.text, fontSize = 20.sp),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (text.isEmpty()) {
                        Text(
                            text = "I am...",
                            style = MakariosTypography.displaySmall.copy(
                                color = light.text.copy(alpha = 0.35f),
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
            Text("Say it in first person.", style = MakariosTypography.labelSmall, color = light.text.copy(alpha = 0.6f))
            Text("${text.length} / 280", style = MakariosTypography.labelSmall, color = light.text.copy(alpha = 0.6f))
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Tone Selector
        Text("Tone", style = MakariosTypography.labelMedium, color = light.text.copy(alpha = 0.8f))
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(22.dp))
                .border(1.dp, light.text.copy(alpha = 0.15f), RoundedCornerShape(22.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("Still", "Bold", "Gentle").forEach { t ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (tone == t) light.text else Color.Transparent)
                        .clickable { onToneChanged(t) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = t,
                        style = MakariosTypography.labelLarge,
                        color = if (tone == t) light.top else light.text
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Starters
        Text("Or begin with one of these", style = MakariosTypography.labelMedium, color = light.text.copy(alpha = 0.8f))
        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            starters.forEach { starter ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(light.text.copy(alpha = 0.05f))
                        .clickable { onTextChanged(starter) }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "“$starter”",
                        style = MakariosTypography.bodyMedium,
                        color = light.text
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
            colors = ButtonDefaults.buttonColors(containerColor = light.text, contentColor = light.top),
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
    light: Light,
    matchedVerses: List<ScriptureVerse>,
    selectedVerseIndex: Int,
    onSelectVerseIndex: (Int) -> Unit,
    onSearchBible: () -> Unit,
    onEditDeclaration: () -> Unit,
    onUseThisVerse: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Your declaration banner
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(if (light.isDark) Color(0x22FFFFFF) else Color(0x55FFFFFF))
                .border(1.dp, light.text.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "YOUR DECLARATION",
                style = MakariosTypography.labelSmall.copy(fontSize = 11.sp, letterSpacing = 1.sp),
                color = light.text.copy(alpha = 0.65f),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "\"${declarationText.ifBlank { "I walk in God's peace and strength." }}\"",
                style = MakariosTypography.bodyLarge.copy(
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontSize = 17.sp,
                    lineHeight = 24.sp
                ),
                color = light.text
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Anchor in Scripture",
            style = MakariosTypography.displaySmall.copy(fontSize = 24.sp),
            color = light.text
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Choose the verse that anchors what you are declaring.",
            style = MakariosTypography.bodyMedium,
            color = light.text.copy(alpha = 0.75f)
        )

        if (matchedVerses.isEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (light.isDark) Color(0x2EFFFFFF) else Color(0xE6FFFFFF))
                    .border(
                        width = 1.dp,
                        color = light.text.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MenuBook,
                        contentDescription = null,
                        tint = light.text.copy(alpha = 0.7f),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No confident match",
                        style = MakariosTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = light.text
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "We could not find a clear scripture match for this declaration. Search the Bible to find the exact verse you want to anchor with.",
                        style = MakariosTypography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = light.text.copy(alpha = 0.75f)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onSearchBible,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = light.text,
                            contentColor = light.top
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Icon(Icons.Outlined.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Search the Bible", style = MakariosTypography.labelLarge)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(onClick = onEditDeclaration) {
                    Text("Edit declaration", style = MakariosTypography.labelLarge, color = light.text.copy(alpha = 0.75f))
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        } else {
            Spacer(modifier = Modifier.height(16.dp))

            // Best Match Card
            val bestMatch = matchedVerses.getOrNull(0) ?: ScriptureVerse(
                reference = "Romans 8:38-39",
                text = "For I am convinced that neither death nor life, neither angels nor demons, neither the present nor the future, nor any powers... will be able to separate us from the love of God that is in Christ Jesus our Lord.",
                themes = setOf("love", "peace"),
                toneAffinity = AffirmationTone.STILL,
                keywords = setOf("love", "convinced", "separate")
            )
            val isBestSelected = selectedVerseIndex == 0

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (light.isDark) Color(0x2EFFFFFF) else Color(0xE6FFFFFF))
                    .border(
                        width = if (isBestSelected) 2.dp else 1.dp,
                        color = if (isBestSelected) light.text else light.text.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onSelectVerseIndex(0) }
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(light.text)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "BEST MATCH",
                                style = MakariosTypography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = light.top
                            )
                        }

                        if (isBestSelected) {
                            Box(
                                modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(light.text),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selected",
                                    tint = light.top,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = bestMatch.reference,
                        style = MakariosTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = light.text
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "\"${bestMatch.text}\"",
                        style = MakariosTypography.bodyLarge.copy(
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            fontSize = 16.sp,
                            lineHeight = 24.sp
                        ),
                        color = light.text.copy(alpha = 0.9f)
                    )
                }
            }

            // Alternatives
            val altIndices = listOf(1, 2).filter { it < matchedVerses.size }
            if (altIndices.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "ALTERNATIVES",
                    style = MakariosTypography.labelSmall.copy(
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = light.text.copy(alpha = 0.65f)
                )
                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    altIndices.forEach { altIdx ->
                        val altVerse = matchedVerses[altIdx]
                        val isAltSelected = selectedVerseIndex == altIdx

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (light.isDark) Color(0x1EFFFFFF) else Color(0xD0FFFFFF))
                                .border(
                                    width = if (isAltSelected) 2.dp else 1.dp,
                                    color = if (isAltSelected) light.text else light.text.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { onSelectVerseIndex(altIdx) }
                                .padding(16.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = altVerse.reference,
                                        style = MakariosTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
                                        color = light.text
                                    )
                                    if (isAltSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(light.text),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = "Selected",
                                                tint = light.top,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "\"${altVerse.text}\"",
                                    style = MakariosTypography.bodyMedium.copy(
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp
                                    ),
                                    color = light.text.copy(alpha = 0.85f),
                                    maxLines = 4,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search the Bible and Edit declaration row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onSearchBible) {
                    Icon(Icons.Outlined.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp), tint = light.text)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Search the Bible", style = MakariosTypography.labelLarge, color = light.text)
                }

                TextButton(onClick = onEditDeclaration) {
                    Text("Edit declaration", style = MakariosTypography.labelLarge, color = light.text.copy(alpha = 0.75f))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Primary: ink pill "Use this verse" (56dp)
            Button(
                onClick = onUseThisVerse,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = light.text,
                    contentColor = light.top
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Use this verse", style = MakariosTypography.labelLarge)
            }

            Spacer(modifier = Modifier.height(32.dp))
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
    isSharingInProgress: Boolean = false,
    onShare: () -> Unit,
    onDone: () -> Unit,
    onSavePhotos: () -> Unit,
    onSetWallpaper: () -> Unit,
    onShareAsFile: () -> Unit,
    onShareToInstagram: () -> Unit = {},
    onShareToWhatsApp: () -> Unit = {}
) {
    val context = LocalContext.current
    val exportFormat = remember(format) { ExportFormat.from(format) }

    var activeCategory by remember { mutableStateOf("Essentials") }
    var recipientName by remember { mutableStateOf("") }
    var rewrittenText by remember(declarationText) { mutableStateOf(PronounRewriter.rewrite(declarationText)) }
    var selectedMilestone by remember { mutableStateOf(30) }

    val spec = remember(
        declarationText, verseText, verseReference,
        selectedStyle.id, recipientName, rewrittenText, selectedMilestone
    ) {
        StyleSpec(
            declaration = if (selectedStyle.id == StyleRegistry.FOR_YOU.id) rewrittenText else declarationText,
            verse = verseText,
            reference = verseReference,
            recipientName = recipientName.takeIf { it.isNotBlank() },
            isPronounRewritten = selectedStyle.id == StyleRegistry.FOR_YOU.id,
            milestoneDays = selectedMilestone
        )
    }

    val isIgInstalled = remember { ShareRepository.isInstagramInstalled(context) }
    val isWaInstalled = remember { ShareRepository.isWhatsAppInstalled(context) }

    // Auto-fallback if Numerals is currently selected but chapter is 1-digit and ineligible
    LaunchedEffect(spec.isNumeralsEligible) {
        if (selectedStyle.id == StyleRegistry.NUMERALS.id && !spec.isNumeralsEligible) {
            onStyleChanged(AutoStyleSelector.pickStyle(declarationText, verseText, verseReference, null, selectedLight))
        }
    }

    // Performance: render 200x433 preview in background coroutine at 1/4 scale with cache
    val previewBitmap by produceState<android.graphics.Bitmap?>(
        initialValue = null,
        selectedStyle.id,
        selectedLight.name,
        format,
        spec.declaration,
        verseText,
        spec.recipientName,
        spec.milestoneDays
    ) {
        value = withContext(Dispatchers.Default) {
            val key = "${selectedStyle.id}:${selectedLight.name}:${exportFormat.name}:200x433:${spec.declaration.hashCode()}:${verseText.hashCode()}:${spec.recipientName.hashCode()}"
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
                .border(1.dp, selectedLight.text.copy(alpha = 0.12f), RoundedCornerShape(22.dp)),
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

        // Extra controls for For you (gift) style
        if (selectedStyle.id == StyleRegistry.FOR_YOU.id) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = selectedLight.text.copy(alpha = 0.06f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("For you (Gift)", style = MakariosTypography.labelMedium, color = selectedLight.text)
                    Spacer(Modifier.height(8.dp))
                    BasicTextField(
                        value = recipientName,
                        onValueChange = { recipientName = it },
                        singleLine = true,
                        textStyle = MakariosTypography.bodyMedium.copy(color = selectedLight.text),
                        decorationBox = { inner ->
                            if (recipientName.isEmpty()) Text("Recipient's name (e.g. Ada)", color = selectedLight.text.copy(alpha = 0.45f), style = MakariosTypography.bodyMedium)
                            inner()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(selectedLight.text.copy(alpha = 0.08f))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text("Declaration for them (editable)", style = MakariosTypography.labelSmall, color = selectedLight.text.copy(alpha = 0.7f))
                    Spacer(Modifier.height(4.dp))
                    BasicTextField(
                        value = rewrittenText,
                        onValueChange = { rewrittenText = it },
                        textStyle = MakariosTypography.bodyMedium.copy(color = selectedLight.text),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(selectedLight.text.copy(alpha = 0.08f))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // Extra controls for Streak style
        if (selectedStyle.id == StyleRegistry.STREAK.id) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Milestone:", style = MakariosTypography.labelSmall, color = selectedLight.text.copy(alpha = 0.7f))
                listOf(7, 14, 21, 30, 50, 100, 365).forEach { ms ->
                    val isMsSel = selectedMilestone == ms
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isMsSel) selectedLight.text else selectedLight.text.copy(alpha = 0.08f))
                            .clickable { selectedMilestone = ms }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("$ms days", style = MakariosTypography.labelSmall, color = if (isMsSel) selectedLight.top else selectedLight.text)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // 2. Style row: Grouped into Essentials (12) and More (9)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Style", style = MakariosTypography.labelMedium, color = selectedLight.text.copy(alpha = 0.8f))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Essentials", "More").forEach { cat ->
                    val isCatActive = activeCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isCatActive) selectedLight.text else selectedLight.text.copy(alpha = 0.08f))
                            .clickable { activeCategory = cat }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = cat,
                            style = MakariosTypography.labelSmall,
                            color = if (isCatActive) selectedLight.top else selectedLight.text
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))

        val stylesToDisplay = if (activeCategory == "Essentials") StyleRegistry.essentials else StyleRegistry.more
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            stylesToDisplay.forEach { style ->
                val isNumerals = style.id == StyleRegistry.NUMERALS.id
                val isEligible = !isNumerals || spec.isNumeralsEligible
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
                    modifier = Modifier
                        .then(
                            if (isEligible) {
                                Modifier.clickable {
                                    onStyleChanged(style)
                                    if (!style.supports(exportFormat)) {
                                        onFormatChanged("Story")
                                    }
                                }
                            } else Modifier
                        )
                ) {
                    // Outer box providing 2dp outline with 3dp offset when selected
                    Box(
                        Modifier
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .border(2.dp, selectedLight.text, RoundedCornerShape(14.dp))
                                        .padding(3.dp)
                                } else {
                                    Modifier.padding(5.dp)
                                }
                            )
                            .size(width = 56.dp, height = 121.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(selectedLight.top)
                            .alpha(if (isEligible) 1f else 0.35f),
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
                        color = if (isSelected) selectedLight.text else if (isEligible) selectedLight.text.copy(alpha = 0.65f) else selectedLight.text.copy(alpha = 0.30f),
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // 3. Light row: Auto + 8 swatches (40dp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            Text("Light", style = MakariosTypography.labelMedium, color = selectedLight.text.copy(alpha = 0.8f))
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
                    .background(selectedLight.text.copy(alpha = 0.08f))
                    .border(if (isAuto) 2.dp else 1.dp, if (isAuto) selectedLight.text else selectedLight.text.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                    .clickable { onLightChanged(Light.forNow()) },
                contentAlignment = Alignment.Center
            ) {
                Text("Auto", style = MakariosTypography.labelSmall.copy(fontSize = 11.sp), color = selectedLight.text)
            }

            // 8 Lights swatches
            Light.values().forEach { lightOption ->
                val isSelected = lightOption == selectedLight
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(lightOption.top)
                        .border(if (isSelected) 2.dp else 0.dp, if (isSelected) selectedLight.text else Color.Transparent, RoundedCornerShape(10.dp))
                        .clickable { onLightChanged(lightOption) }
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // 4. Format row: Story, Square, Portrait, X, Wallpaper
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            Text("Format", style = MakariosTypography.labelMedium, color = selectedLight.text.copy(alpha = 0.8f))
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
                        .background(if (isSelected) selectedLight.text else selectedLight.text.copy(alpha = if (isSupported) 0.08f else 0.03f))
                        .clickable(enabled = isSupported) { onFormatChanged(fmt) }
                        .padding(horizontal = 16.dp, vertical = 9.dp)
                ) {
                    Text(
                        text = fmt,
                        style = MakariosTypography.labelMedium,
                        color = if (isSelected) selectedLight.top else if (isSupported) selectedLight.text else selectedLight.text.copy(alpha = 0.3f)
                    )
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        // 5. Two equal pills side by side: Share (outline) and Done (ink)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onShare,
                enabled = !isSharingInProgress,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, selectedLight.text),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = selectedLight.text
                )
            ) {
                if (isSharingInProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = selectedLight.text
                    )
                } else {
                    Icon(Icons.Outlined.Share, contentDescription = null, tint = selectedLight.text, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Share", style = MakariosTypography.labelLarge, color = selectedLight.text)
                }
            }

            Button(
                onClick = onDone,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = selectedLight.text,
                    contentColor = selectedLight.top
                )
            ) {
                Text("Done", style = MakariosTypography.labelLarge)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Instagram Stories & WhatsApp Status direct buttons (if apps are installed)
        if (isIgInstalled || isWaInstalled) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isIgInstalled) {
                    OutlinedButton(
                        onClick = onShareToInstagram,
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(23.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, selectedLight.text.copy(alpha = 0.4f))
                    ) {
                        Text("Instagram Story", color = selectedLight.text, style = MakariosTypography.labelMedium)
                    }
                }
                if (isWaInstalled) {
                    OutlinedButton(
                        onClick = onShareToWhatsApp,
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(23.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, selectedLight.text.copy(alpha = 0.4f))
                    ) {
                        Text("WhatsApp Status", color = selectedLight.text, style = MakariosTypography.labelMedium)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        // 6. Text buttons: "Save to photos", "Set as wallpaper", "Share as file"
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onSavePhotos) {
                Icon(Icons.Outlined.Image, contentDescription = null, tint = selectedLight.text, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Save to photos", color = selectedLight.text, style = MakariosTypography.labelMedium)
            }

            TextButton(onClick = onSetWallpaper) {
                Icon(Icons.Outlined.Wallpaper, contentDescription = null, tint = selectedLight.text, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Set as wallpaper", color = selectedLight.text, style = MakariosTypography.labelMedium)
            }

            TextButton(onClick = onShareAsFile) {
                Icon(Icons.Outlined.InsertDriveFile, contentDescription = null, tint = selectedLight.text, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Share as file", color = selectedLight.text, style = MakariosTypography.labelMedium)
            }
        }

        Spacer(Modifier.height(36.dp))
    }
}

/**
 * Item H: Step 4 ("Declare, Done" board)
 * Displays check mark, "Kept.", preview of the created artwork,
 * primary "Back to Today" button, and "Declare another" text button.
 * Back press on Done navigates directly to Today.
 * Plays success haptic CONFIRM on API 30+.
 */
@Composable
fun DeclareDoneStep(
    affirmation: Affirmation,
    light: Light,
    style: Style,
    onBackToToday: () -> Unit,
    onDeclareAnother: () -> Unit
) {
    val view = LocalView.current
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    val spec = remember(affirmation.declaration, affirmation.scriptureText, affirmation.reference) {
        StyleSpec(
            declaration = affirmation.declaration,
            verse = affirmation.scriptureText,
            reference = affirmation.reference
        )
    }

    val previewBitmap by produceState<android.graphics.Bitmap?>(
        initialValue = null,
        style.id,
        light.name,
        affirmation.declaration,
        affirmation.scriptureText
    ) {
        value = withContext(Dispatchers.Default) {
            val key = "${style.id}:${light.name}:done:140x245:${affirmation.declaration.hashCode()}"
            previewBitmapCache.getOrPut(key) {
                style.render(spec, light, IntSize(140, 245))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Check mark icon
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(light.text.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Kept",
                tint = light.text,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // "Kept." headline
        Text(
            text = "Kept.",
            style = MakariosTypography.displayMedium,
            color = light.text,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle: "Saved to Mine. It will be waiting for you."
        Text(
            text = "Saved to Mine. It will be waiting for you.",
            style = MakariosTypography.bodyLarge,
            color = light.text.copy(alpha = 0.78f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Small preview of the image (140x245dp)
        Box(
            modifier = Modifier
                .width(140.dp)
                .height(245.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(light.bottom)
                .border(1.dp, light.text.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            previewBitmap?.let { bmp ->
                androidx.compose.foundation.Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Artwork preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } ?: CircularProgressIndicator(color = light.text, modifier = Modifier.size(24.dp))
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Primary: ink pill "Back to Today" (56dp)
        Button(
            onClick = onBackToToday,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = light.text,
                contentColor = light.top
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("Back to Today", style = MakariosTypography.labelLarge)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Text button "Declare another"
        TextButton(
            onClick = onDeclareAnother,
            modifier = Modifier.height(48.dp)
        ) {
            Text(
                "Declare another",
                style = MakariosTypography.labelLarge,
                color = light.text.copy(alpha = 0.85f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
