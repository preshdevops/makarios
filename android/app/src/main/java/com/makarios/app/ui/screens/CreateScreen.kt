package com.makarios.app.ui.screens
import com.makarios.app.ui.components.MakChip
import com.makarios.app.ui.components.MakSurface

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
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import com.makarios.app.data.bible.BibleReferenceParser
import com.makarios.app.data.bible.BibleRepository
import com.makarios.app.data.bible.ScriptureText
import com.makarios.app.ui.wallpaper.StyleTypefaces
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
import androidx.compose.ui.graphics.FilterQuality
import kotlin.math.roundToInt
import kotlin.math.max
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.makarios.app.ui.components.ShareSheet
import com.makarios.app.ui.components.ShareContent
import androidx.compose.ui.unit.sp
import com.makarios.app.data.*
import com.makarios.app.ui.components.BibleReaderSheet
import com.makarios.app.ui.components.Pairing
import com.makarios.app.ui.theme.*
import com.makarios.app.ui.wallpaper.AutoStyleSelector
import com.makarios.app.ui.wallpaper.Style
import com.makarios.app.ui.wallpaper.StyleRegistry
import com.makarios.app.ui.wallpaper.StyleFavoritesStore
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import com.makarios.app.ui.wallpaper.StyleSpec
import com.makarios.app.util.ExportFormat
import com.makarios.app.util.LightCanvas
import com.makarios.app.util.PerformanceTier
import com.makarios.app.util.BitmapMemoryCache
import com.makarios.app.data.ShareRepository
import com.makarios.app.data.ShareRequest
import com.makarios.app.data.ShareResult
import com.makarios.app.data.ShareTarget
import com.makarios.app.data.bible.VerseSelection
import com.makarios.app.ui.screens.bible.BiblePickerScreen
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
    onSearchBible: ((currentDeclaration: String) -> Unit)? = null,
    onOpenWidgetSettings: (() -> Unit)? = null,
    onOpenReminderSettings: (() -> Unit)? = null,
    onStepChange: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var step by remember(initialStep) { mutableStateOf(initialStep) }
    LaunchedEffect(step) {
        onStepChange?.invoke(step)
    }

    val declarationTextState = remember(initialDeclaration) { mutableStateOf(initialDeclaration ?: "") }
    val appLight = LocalLight.current
    var selectedLight by remember { mutableStateOf(appLight) }
    ProvideActiveLight(selectedLight, "Declare: Step $step")
    var selectedStyle by remember { mutableStateOf<Style>(StyleRegistry.PAIRING) }
    var formatName by remember { mutableStateOf("Story") }
    var shareContentToOpen by remember { mutableStateOf<ShareContent?>(null) }
    var selectedPhotoUrl by remember { mutableStateOf<String?>(null) }
    var showNotificationAsk by remember { mutableStateOf(false) }
    var isSharingInProgress by remember { mutableStateOf(false) }
    val matchEngine = remember { MatchEngine.getInstance(context) }
    var isMatchingInProgress by remember { mutableStateOf(false) }
    val isEngineInitializing by produceState(initialValue = !matchEngine.ready && matchEngine.isInitializing) {
        while (!matchEngine.ready && matchEngine.isInitializing) {
            value = true
            kotlinx.coroutines.delay(100)
        }
        value = false
    }
    val isEngineLoading = isMatchingInProgress || isEngineInitializing

    // Verses matched for the declaration
    var matchedVerses by remember(initialVerseText, initialReference) {
        mutableStateOf(
            if (initialVerseText != null && !initialReference.isNullOrBlank()) {
                listOf(ScriptureVerse(text = initialVerseText, reference = initialReference))
            } else emptyList()
        )
    }
    var currentVerseIndex by remember { mutableStateOf(0) }
    var isEngineConfident by remember { mutableStateOf(true) }
    var engineReason by remember { mutableStateOf("") }
    var savedAffirmation by remember { mutableStateOf<Affirmation?>(null) }

    val currentVerse: ScriptureVerse? = remember(matchedVerses, currentVerseIndex) {
        if (matchedVerses.isNotEmpty() && currentVerseIndex < matchedVerses.size) {
            matchedVerses[currentVerseIndex]
        } else null
    }

    fun getOrCreateSavedAffirmation(): Affirmation {
        val existing = savedAffirmation
        if (existing != null) return existing
        val v = currentVerse ?: com.makarios.app.data.ScriptureVerse("The Lord is my shepherd; I shall not want.", "Psalm 23:1")
        val safeRef = if (v.reference.isNotBlank()) v.reference else "Scripture"
        val aff = Affirmation(
            id = "custom-${System.currentTimeMillis()}",
            declaration = declarationTextState.value.trim(),
            scriptureText = v.text,
            reference = safeRef,
            context = "Authored in Makarios Declare Studio.",
            category = "Personal",
            tone = AffirmationTone.STILL,
            imageUrl = selectedPhotoUrl ?: "",
            isFavorite = true
        )
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
                    declarationTextState.value = ""
                    savedAffirmation = null
                    matchedVerses = emptyList()
                    currentVerseIndex = 0
                    step = 1
                },
                onOpenWidgetSettings = onOpenWidgetSettings,
                onOpenReminderSettings = onOpenReminderSettings
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

                Crossfade(targetState = step, animationSpec = tween(if (PerformanceTier.animationsEnabled) 220 else 0), label = "CreateSteps") { currentStep ->
                    when (currentStep) {
                        1 -> WriteStep(
                            textState = declarationTextState,
                            isEngineLoading = isEngineLoading,
                            light = activeStepLight,
                            onNext = {
                                coroutineScope.launch {
                                    isMatchingInProgress = true
                                    try {
                                        val result = matchEngine.match(declarationTextState.value, limit = 3)
                                        isEngineConfident = result.confident
                                        engineReason = result.reason
                                        matchedVerses = result.top.map {
                                            ScriptureVerse(text = it.text, reference = it.reference)
                                        }
                                        currentVerseIndex = 0
                                        step = 2
                                    } finally {
                                        isMatchingInProgress = false
                                    }
                                }
                            }
                        )
                        2 -> VerseStep(
                            declarationText = declarationTextState.value,
                            light = activeStepLight,
                            matchedVerses = matchedVerses,
                            selectedVerseIndex = currentVerseIndex,
                            engineReason = engineReason,
                            isConfident = isEngineConfident,
                            onSelectVerseIndex = { currentVerseIndex = it },
                            onSearchBible = { onSearchBible?.invoke(declarationTextState.value) },
                            onAddCustomVerse = { customVerse ->
                                matchedVerses = listOf(customVerse) + matchedVerses
                                currentVerseIndex = 0
                                engineReason = "Selected from reference"
                                isEngineConfident = true
                            },
                            onEditDeclaration = { step = 1 },
                            onUseThisVerse = {
                                val cv = currentVerse
                                if (cv != null && cv.reference.isNotBlank()) {
                                    selectedStyle = AutoStyleSelector.pickStyle(
                                        declaration = declarationTextState.value,
                                        verse = cv.text,
                                        reference = cv.reference,
                                        light = selectedLight
                                    )
                                    step = 3
                                }
                            }
                        )
                        3 -> LookStep(
                            declarationText = declarationTextState.value,
                            verseText = currentVerse?.text.orEmpty(),
                            verseReference = currentVerse?.reference.orEmpty(),
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
                                shareContentToOpen = ShareContent(
                                    declaration = aff.declaration,
                                    verse = aff.scriptureText,
                                    reference = aff.reference,
                                    initialStyleId = selectedStyle.id,
                                    initialLightName = selectedLight.name,
                                    initialFormat = formatName,
                                    declarationId = aff.id,
                                    source = "declare",
                                    contentType = "declaration"
                                )
                            },
                            onDone = {
                                coroutineScope.launch {
                                    try {
                                        val aff = getOrCreateSavedAffirmation()
                                        val entity = com.makarios.app.data.local.KeptEntity(
                                            id = aff.id,
                                            declaration = aff.declaration,
                                            verseText = aff.scriptureText,
                                            reference = aff.reference,
                                            lightName = selectedLight.name,
                                            styleId = selectedStyle.id,
                                            createdAt = System.currentTimeMillis(),
                                            isFavorite = true,
                                            sourceType = "authored"
                                        )
                                        com.makarios.app.data.local.KeptRepository.save(entity)
                                        showNotificationAsk = true
                                        step = 4
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Could not save: ${e.message}. Please tap Save again.", Toast.LENGTH_LONG).show()
                                    }
                                }
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
                                        Toast.makeText(context, "Wallpaper updated & saved!", Toast.LENGTH_SHORT).show()
                                    }
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

    shareContentToOpen?.let { content ->
        ShareSheet(
            content = content,
            onDismiss = { shareContentToOpen = null }
        )
    }
}

@Composable
fun WriteStep(textState: MutableState<String>, isEngineLoading: Boolean, light: Light, onNext: () -> Unit) {
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

        DeclarationInputBox(textState = textState, light = light)

        Spacer(modifier = Modifier.height(28.dp))

        StartersSection(light = light, onSelectStarter = { textState.value = it })

        Spacer(modifier = Modifier.height(36.dp))

        FindVerseButton(
            textState = textState,
            isEngineLoading = isEngineLoading,
            light = light,
            onNext = onNext
        )
    }
}

@Composable
private fun DeclarationInputBox(
    textState: MutableState<String>,
    light: Light
) {
    val text = textState.value
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
            onValueChange = { if (it.length <= 280) textState.value = it },
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
}

@Composable
private fun StartersSection(
    light: Light,
    onSelectStarter: (String) -> Unit
) {
    var starterIndex by remember { mutableStateOf(0) }
    val starters = remember(starterIndex) {
        val all = SeedDeclarations.entries.map { it.declaration }
        val start = (starterIndex * 3) % all.size
        (0 until 3).map { all[(start + it) % all.size] }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Start from yours", style = MakariosTypography.labelMedium, color = light.text.copy(alpha = 0.8f))
        IconButton(
            onClick = { starterIndex++ },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = "Refresh declarations",
                tint = light.text.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
    Spacer(modifier = Modifier.height(10.dp))

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        starters.forEach { starter ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(light.text.copy(alpha = 0.05f))
                    .clickable { onSelectStarter(starter) }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "\u201C$starter\u201D",
                    style = MakariosTypography.bodyMedium,
                    color = light.text
                )
            }
        }
    }
}

@Composable
private fun FindVerseButton(
    textState: State<String>,
    isEngineLoading: Boolean,
    light: Light,
    onNext: () -> Unit
) {
    val isEnabled by remember(textState, isEngineLoading) {
        derivedStateOf { textState.value.isNotBlank() && !isEngineLoading }
    }
    Button(
        onClick = onNext,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = light.text, contentColor = light.top),
        shape = RoundedCornerShape(28.dp),
        enabled = isEnabled
    ) {
        Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(if (isEngineLoading) "Getting ready…" else "Find my verse", style = MakariosTypography.labelLarge)
    }
}

@Composable
fun VerseStep(
    declarationText: String,
    light: Light,
    matchedVerses: List<ScriptureVerse>,
    selectedVerseIndex: Int,
    engineReason: String = "",
    isConfident: Boolean = true,
    onSelectVerseIndex: (Int) -> Unit,
    onSearchBible: () -> Unit,
    onAddCustomVerse: (ScriptureVerse) -> Unit,
    onEditDeclaration: () -> Unit,
    onUseThisVerse: () -> Unit
) {
    val context = LocalContext.current
    var showTypeRefDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 1. Top: "YOUR WORDS", declaration, short rule, then "STANDING ON"
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "YOUR WORDS",
                style = MakariosTypography.labelSmall.copy(
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = light.text.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "\"${declarationText.ifBlank { "I walk in God's peace and strength." }}\"",
                style = TextStyle(
                    fontFamily = NewsreaderFontFamily,
                    fontSize = 22.sp,
                    lineHeight = 28.sp
                ),
                color = light.text
            )
            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(
                modifier = Modifier.width(48.dp),
                thickness = 1.dp,
                color = light.text.copy(alpha = 0.25f)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "STANDING ON",
                style = MakariosTypography.labelSmall.copy(
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = light.text.copy(alpha = 0.6f)
            )
        }

        if (!isConfident || matchedVerses.isEmpty()) {
            // 6. No confident match calm prompt without picking a random fallback
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
                        imageVector = Icons.Outlined.Search,
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
                        text = "We couldn't find a close match for that phrasing. Pick from the Bible or try rephrasing.",
                        style = MakariosTypography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = light.text.copy(alpha = 0.75f)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onSearchBible,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.dp, light.text.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp), tint = light.text)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Search the Bible", style = MakariosTypography.labelMedium, color = light.text)
                        }
                        OutlinedButton(
                            onClick = { showTypeRefDialog = true },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.dp, light.text.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = light.text)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Type reference", style = MakariosTypography.labelMedium, color = light.text)
                        }
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

            // 2. Best fit large selected card
            val selectedVerse = matchedVerses.getOrNull(selectedVerseIndex) ?: matchedVerses[0]
            AnimatedContent(
                targetState = selectedVerse,
                transitionSpec = {
                    fadeIn(animationSpec = tween(if (PerformanceTier.animationsEnabled) 150 else 0)) togetherWith fadeOut(animationSpec = tween(if (PerformanceTier.animationsEnabled) 150 else 0))
                },
                label = "SelectedVerseSwap"
            ) { verse ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (light.isDark) Color(0x2EFFFFFF) else Color(0xE6FFFFFF))
                        .border(
                            width = 2.dp,
                            color = light.text,
                            shape = RoundedCornerShape(20.dp)
                        )
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
                                    text = "BEST FIT",
                                    style = MakariosTypography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        letterSpacing = 1.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = light.top
                                )
                            }
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

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "\"${verse.text}\"",
                            style = TextStyle(
                                fontFamily = NewsreaderFontFamily,
                                fontStyle = FontStyle.Italic,
                                fontSize = 20.sp,
                                lineHeight = 28.sp
                            ),
                            color = light.text
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = verse.reference,
                            style = MakariosTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = light.text
                        )

                        if (engineReason.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = engineReason,
                                style = MakariosTypography.bodySmall,
                                color = light.text.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // 3. Two alternative cards: smaller, reference + first line + reason, selectable on tap (swaps into top slot)
            val alternatives = remember(matchedVerses.size, selectedVerseIndex) { matchedVerses.indices.filter { it != selectedVerseIndex } }
            if (alternatives.isNotEmpty()) {
                Spacer(modifier = Modifier.height(18.dp))
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

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    alternatives.forEach { altIdx ->
                        val altVerse = matchedVerses[altIdx]
                        val firstLine = altVerse.text.lineSequence().firstOrNull()?.take(90) ?: altVerse.text.take(90)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (light.isDark) Color(0x1EFFFFFF) else Color(0xD0FFFFFF))
                                .border(
                                    width = 1.dp,
                                    color = light.text.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { onSelectVerseIndex(altIdx) }
                                .padding(14.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = altVerse.reference,
                                    style = MakariosTypography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = light.text
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "\"$firstLine...\"",
                                    style = MakariosTypography.bodySmall.copy(
                                        fontStyle = FontStyle.Italic,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    ),
                                    color = light.text.copy(alpha = 0.85f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Two outline action buttons below cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onSearchBible,
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(23.dp),
                    border = BorderStroke(1.dp, light.text.copy(alpha = 0.4f))
                ) {
                    Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp), tint = light.text)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Search the Bible", style = MakariosTypography.labelMedium, color = light.text)
                }

                OutlinedButton(
                    onClick = { showTypeRefDialog = true },
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(23.dp),
                    border = BorderStroke(1.dp, light.text.copy(alpha = 0.4f))
                ) {
                    Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = light.text)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Type a reference", style = MakariosTypography.labelMedium, color = light.text)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Primary button: "Use <reference>"
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
                Text(
                    text = "Use ${selectedVerse.reference}",
                    style = MakariosTypography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showTypeRefDialog) {
        var refInput by remember { mutableStateOf("") }
        var refError by remember { mutableStateOf<String?>(null) }
        var isLookingUp by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        AlertDialog(
            onDismissRequest = { showTypeRefDialog = false },
            title = {
                Text(
                    "Type a reference",
                    style = MakariosTypography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        "Enter any Bible reference (e.g. Phil 4:13, Romans 8:28, Psalm 23:1):",
                        style = MakariosTypography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = refInput,
                        onValueChange = {
                            refInput = it
                            refError = null
                        },
                        singleLine = true,
                        placeholder = { Text("e.g. Phil 4:13") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (refError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = refError ?: "",
                            style = MakariosTypography.bodySmall,
                            color = MakError
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = BibleReferenceParser.parse(refInput)
                        if (parsed == null) {
                            refError = "Could not parse reference. Try 'Book Chapter:Verse' (e.g. Phil 4:13)."
                            return@Button
                        }
                        scope.launch {
                            isLookingUp = true
                            val res = withContext(Dispatchers.IO) {
                                BibleRepository.initialize(context)
                                BibleRepository.getVerse(refInput)
                            }
                            val verse = res.getOrNull()
                            if (verse != null) {
                                val dispRef = ScriptureText.formatDisplayReference(verse.reference)
                                onAddCustomVerse(ScriptureVerse(reference = dispRef, text = verse.text))
                                showTypeRefDialog = false
                            } else {
                                refError = "Verse not found in this translation. Check the chapter and verse numbers."
                            }
                            isLookingUp = false
                        }
                    },
                    enabled = refInput.isNotBlank() && !isLookingUp
                ) {
                    Text("Find verse")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTypeRefDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/** Cache for offscreen preview bitmaps keyed by (styleId, lightName, formatName, size, textHash) */
private val previewBitmapCache = com.makarios.app.util.BitmapMemoryCache
private val thumbnailBitmapCache = android.util.LruCache<String, android.graphics.Bitmap>(24)

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
    onShareToInstagram: () -> Unit = {},
    onShareToWhatsApp: () -> Unit = {}
) {
    val context = LocalContext.current
    val exportFormat = remember(format) { ExportFormat.from(format) }

    var activeCategory by remember { mutableStateOf("For you") }
    var recipientName by remember { mutableStateOf("") }
    var replyText by remember { mutableStateOf("Needed this today") }
    var rewrittenText by remember(declarationText) { mutableStateOf(PronounRewriter.rewrite(declarationText)) }
    var selectedMilestone by remember { mutableStateOf(30) }
    var favoriteIds by remember { mutableStateOf(StyleFavoritesStore.getFavorites(context)) }
    val recentIds = remember { StyleFavoritesStore.getRecent(context) }

    val spec = remember(
        declarationText, verseText, verseReference,
        selectedStyle.id, recipientName, rewrittenText, selectedMilestone, replyText
    ) {
        StyleSpec(
            declaration = if (selectedStyle.id == StyleRegistry.FOR_YOU.id) rewrittenText else declarationText,
            verse = verseText,
            reference = verseReference,
            recipientName = recipientName.takeIf { it.isNotBlank() },
            isPronounRewritten = selectedStyle.id == StyleRegistry.FOR_YOU.id,
            milestoneDays = selectedMilestone,
            replyText = replyText
        )
    }

    val isIgInstalled = remember { ShareRepository.isInstagramInstalled(context) }
    val isWaInstalled = remember { ShareRepository.isWhatsAppInstalled(context) }

    // Auto-fallback if text exceeds maxChars
    LaunchedEffect(declarationText, selectedStyle.id) {
        if (declarationText.length > selectedStyle.maxChars) {
            val fallback = StyleRegistry.all.firstOrNull { it.maxChars >= declarationText.length } ?: StyleRegistry.PAIRING
            onStyleChanged(fallback)
            StyleFavoritesStore.setLastUsed(context, fallback.id)
        }
    }

    val density = androidx.compose.ui.platform.LocalDensity.current.density
    val formatAspect = when (exportFormat) {
        ExportFormat.Square -> 1.0f
        ExportFormat.Portrait -> 1.25f
        ExportFormat.X -> 9f / 16f
        ExportFormat.Story -> 16f / 9f
        ExportFormat.Wallpaper -> 844f / 390f
    }
    val previewWidthDp = when (exportFormat) {
        ExportFormat.Square -> 240.dp
        ExportFormat.Portrait -> 220.dp
        ExportFormat.X -> 280.dp
        ExportFormat.Story -> 200.dp
        ExportFormat.Wallpaper -> 200.dp
    }
    val previewHeightDp = (previewWidthDp.value * formatAspect).dp
    val rawWidthPx = (previewWidthDp.value * density).roundToInt()
    val rawHeightPx = (rawWidthPx * formatAspect).roundToInt()
    val maxDim = max(rawWidthPx, rawHeightPx)
    val previewScale = if (PerformanceTier.isLowRamDevice && maxDim > 1080) 1080f / maxDim else 1f
    val widthPx = (rawWidthPx * previewScale).roundToInt().coerceAtLeast(1)
    val heightPx = (rawHeightPx * previewScale).roundToInt().coerceAtLeast(1)

    // Keep the last preview visible until the debounced new render is ready.
    var activePreviewBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    DisposableEffect(activePreviewBitmap) {
        BitmapMemoryCache.pin(activePreviewBitmap)
        onDispose { BitmapMemoryCache.unpin(activePreviewBitmap) }
    }

    LaunchedEffect(
        selectedStyle.id, selectedLight.name, format, spec.declaration, verseText, spec.recipientName, spec.milestoneDays, widthPx, heightPx
    ) {
        kotlinx.coroutines.delay(150)
        val rendered = withContext(Dispatchers.Default) {
            val key = "${selectedStyle.id}:${selectedLight.name}:${exportFormat.name}:${widthPx}x${heightPx}:${spec.declaration.hashCode()}:${verseText.hashCode()}:${spec.recipientName.hashCode()}:${spec.milestoneDays}"
            previewBitmapCache.getOrPut(context, key) {
                selectedStyle.render(spec, selectedLight, IntSize(widthPx, heightPx))
            }
        }
        activePreviewBitmap = rendered
    }
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(12.dp))

        // 1. Live preview rendered at real pixel resolution
        Box(
            Modifier
                .width(previewWidthDp)
                .height(previewHeightDp)
                .clip(RoundedCornerShape(22.dp))
                .background(selectedLight.bottom)
                .border(1.dp, selectedLight.text.copy(alpha = 0.12f), RoundedCornerShape(22.dp)),
            contentAlignment = Alignment.Center
        ) {
            val current = activePreviewBitmap
            if (current != null && !current.isRecycled) {
                androidx.compose.foundation.Image(
                    bitmap = current.asImageBitmap(),
                    contentDescription = "Live style preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    filterQuality = FilterQuality.High
                )
            } else {
                CircularProgressIndicator(color = selectedLight.text, modifier = Modifier.size(28.dp))
            }
        }

        Spacer(Modifier.height(24.dp))

        // 1. Contextual Option: "For" name field (Birthday, Letter, Praying for you, For you)
        val isNameStyle = selectedStyle.id in listOf(
            StyleRegistry.FOR_YOU.id,
            StyleRegistry.BIRTHDAY.id,
            StyleRegistry.LETTER.id,
            StyleRegistry.PRAYING_FOR_YOU.id
        )
        if (isNameStyle) {
            MakSurface(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = selectedLight.text.copy(alpha = 0.06f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("For (Recipient)", style = MakariosTypography.labelMedium, color = selectedLight.text)
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
                    if (selectedStyle.id == StyleRegistry.FOR_YOU.id) {
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
            }
            Spacer(Modifier.height(16.dp))
        }

        // 2. Contextual Option: Reply text (Chat bubble)
        if (selectedStyle.id == StyleRegistry.CHAT_BUBBLE.id) {
            MakSurface(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = selectedLight.text.copy(alpha = 0.06f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Reply message", style = MakariosTypography.labelMedium, color = selectedLight.text)
                    Spacer(Modifier.height(8.dp))
                    BasicTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        singleLine = true,
                        textStyle = MakariosTypography.bodyMedium.copy(color = selectedLight.text),
                        decorationBox = { inner ->
                            if (replyText.isEmpty()) Text("e.g. Needed this today", color = selectedLight.text.copy(alpha = 0.45f), style = MakariosTypography.bodyMedium)
                            inner()
                        },
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

        // 3. Contextual Option: Milestone days (Streak, Week)
        if (selectedStyle.id in listOf(StyleRegistry.STREAK.id, StyleRegistry.WEEK.id)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Days:", style = MakariosTypography.labelSmall, color = selectedLight.text.copy(alpha = 0.7f))
                listOf(3, 5, 7, 14, 21, 30, 100).forEach { ms ->
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

        // 4. Two-Level Style Picker
        // Header with "Surprise me"
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Style", style = MakariosTypography.labelMedium, color = selectedLight.text.copy(alpha = 0.8f))
            TextButton(
                onClick = {
                    val eligible = StyleRegistry.all.filter { it.maxChars >= spec.declaration.length }
                    val pick = eligible.randomOrNull() ?: StyleRegistry.PAIRING
                    onStyleChanged(pick)
                    StyleFavoritesStore.setLastUsed(context, pick.id)
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = selectedLight.text, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Surprise me", style = MakariosTypography.labelSmall, color = selectedLight.text)
            }
        }
        Spacer(Modifier.height(4.dp))

        // Level 1: Category Chips Row
        val allCategories = remember(favoriteIds.isNotEmpty()) { buildList {
            add("For you")
            if (favoriteIds.isNotEmpty()) {
                add("Favourites")
            }
            add("Type")
            add("Light")
            add("Paper")
            add("Occasion")
            add("Conversation")
        } }

        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            allCategories.forEach { cat ->
                val isCatActive = activeCategory == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isCatActive) selectedLight.text else selectedLight.text.copy(alpha = 0.08f))
                        .clickable { activeCategory = cat }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = cat,
                        style = MakariosTypography.labelSmall,
                        color = if (isCatActive) selectedLight.top else selectedLight.text
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        // Level 2: Style Thumbnails Row for active category
        val stylesToDisplay = remember(activeCategory, spec, selectedLight, recentIds, favoriteIds) { when (activeCategory) {
            "For you" -> StyleRegistry.forYouStyles(spec, selectedLight, recentIds, favoriteIds)
            "Favourites" -> StyleRegistry.all.filter { favoriteIds.contains(it.id.lowercase()) }
            else -> StyleRegistry.byCategory(activeCategory)
        } }

        @OptIn(ExperimentalFoundationApi::class)
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            stylesToDisplay.forEach { style ->
                val isEligible = spec.declaration.length <= style.maxChars
                val isSelected = style.id == selectedStyle.id
                val isFav = favoriteIds.contains(style.id.lowercase())
                val thumbW = (56f * density).roundToInt()
                val thumbH = (121f * density).roundToInt()
                val thumbBitmap by produceState<android.graphics.Bitmap?>(null, style.id, selectedLight.name, thumbW, thumbH) {
                    val thumbKey = "${style.id}:${selectedLight.name}:thumb:${thumbW}x${thumbH}:${spec.shortText.hashCode()}"
                    val cached = thumbnailBitmapCache.get(thumbKey)
                    if (cached != null && !cached.isRecycled) {
                        value = cached
                    } else {
                        value = withContext(Dispatchers.Default) {
                            val rendered = style.render(spec, selectedLight, IntSize(thumbW, thumbH))
                            thumbnailBitmapCache.put(thumbKey, rendered)
                            rendered
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(62.dp)
                ) {
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
                            .alpha(if (isEligible) 1f else 0.35f)
                            .then(
                                if (isEligible) {
                                    Modifier.combinedClickable(
                                        onClick = {
                                            onStyleChanged(style)
                                            StyleFavoritesStore.setLastUsed(context, style.id)
                                            if (!style.supports(exportFormat)) {
                                                onFormatChanged("Story")
                                            }
                                        },
                                        onLongClick = {
                                            StyleFavoritesStore.toggleFavorite(context, style.id)
                                            favoriteIds = StyleFavoritesStore.getFavorites(context)
                                        }
                                    )
                                } else Modifier
                            ),
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

                        // Favorite indicator dot top-right
                        if (isFav) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (selectedLight.dark) SunlitGold else MakError)
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = style.displayName,
                        style = MakariosTypography.labelSmall.copy(fontSize = 11.sp),
                        color = if (isSelected) selectedLight.text else if (isEligible) selectedLight.text.copy(alpha = 0.65f) else selectedLight.text.copy(alpha = 0.30f),
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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


        }

        Spacer(Modifier.height(36.dp))
    }
}

/**
 * Item H: Step 4 ("Declare, Done" board)
 * Displays check mark, "Saved.", preview of the created artwork,
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
    onDeclareAnother: () -> Unit,
    onOpenWidgetSettings: (() -> Unit)? = null,
    onOpenReminderSettings: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()

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
            previewBitmapCache.getOrPut(context, key) {
                style.render(spec, light, IntSize(140, 245))
            }
        }
    }

    DisposableEffect(previewBitmap) {
        BitmapMemoryCache.pin(previewBitmap)
        onDispose { BitmapMemoryCache.unpin(previewBitmap) }
    }

    // Widget & reminders toggle state
    var inWidget by remember { mutableStateOf(false) }
    var inReminders by remember { mutableStateOf(false) }
    var showPermissionDenied by remember { mutableStateOf(false) }

    // Permission launcher for POST_NOTIFICATIONS (Android 13+)
    val permissionLauncher = if (Build.VERSION.SDK_INT >= 33) {
        androidx.activity.compose.rememberLauncherForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                inReminders = true
                coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    com.makarios.app.data.local.KeptRepository.setInReminders(affirmation.id, true)
                }
            } else {
                inReminders = false
                showPermissionDenied = true
            }
        }
    } else null

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
                contentDescription = "Saved",
                tint = light.text,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // "Saved." headline
        Text(
            text = "Saved.",
            style = MakariosTypography.displayMedium,
            color = light.text,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = "Saved to My declarations. It's waiting for you.",
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

        Spacer(modifier = Modifier.height(28.dp))

        // ---- Widget & Reminders toggles card ----
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (light.isDark) light.text.copy(alpha = 0.10f)
                    else Cream
                )
                .padding(horizontal = 20.dp, vertical = 4.dp)
        ) {
            // "Show on my widget" row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Show on my widget",
                        style = MakariosTypography.bodyMedium,
                        color = light.text
                    )
                    Text(
                        "On your home screen",
                        style = MakariosTypography.labelSmall,
                        color = light.text.copy(alpha = 0.65f)
                    )
                }
                Switch(
                    checked = inWidget,
                    onCheckedChange = { enabled ->
                        inWidget = enabled
                        coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                                        com.makarios.app.data.local.KeptRepository.setInWidget(affirmation.id, enabled)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = light.top,
                        checkedTrackColor = light.text,
                        uncheckedThumbColor = light.text,
                        uncheckedTrackColor = light.text.copy(alpha = 0.22f)
                    )
                )
            }

            HorizontalDivider(color = light.text.copy(alpha = 0.10f), thickness = 0.5.dp)

            // "Use in my reminders" row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Use in my reminders",
                        style = MakariosTypography.bodyMedium,
                        color = light.text
                    )
                    Text(
                        "A gentle nudge each day",
                        style = MakariosTypography.labelSmall,
                        color = light.text.copy(alpha = 0.65f)
                    )
                }
                Switch(
                    checked = inReminders,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            // Check notification permission first
                            if (Build.VERSION.SDK_INT >= 33) {
                                val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                                    context, android.Manifest.permission.POST_NOTIFICATIONS
                                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                if (hasPermission) {
                                    inReminders = true
                                    showPermissionDenied = false
                                    coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                                                                com.makarios.app.data.local.KeptRepository.setInReminders(affirmation.id, true)
                                    }
                                } else {
                                    permissionLauncher?.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                }
                            } else {
                                inReminders = true
                                showPermissionDenied = false
                                coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                                                        com.makarios.app.data.local.KeptRepository.setInReminders(affirmation.id, true)
                                }
                            }
                        } else {
                            inReminders = false
                            showPermissionDenied = false
                            coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                                                com.makarios.app.data.local.KeptRepository.setInReminders(affirmation.id, false)
                            }
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = light.top,
                        checkedTrackColor = light.text,
                        uncheckedThumbColor = light.text,
                        uncheckedTrackColor = light.text.copy(alpha = 0.22f)
                    )
                )
            }

            // Notification denied message
            if (showPermissionDenied) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Notifications are off.",
                        style = MakariosTypography.labelSmall,
                        color = light.text.copy(alpha = 0.70f),
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = {
                            context.startActivity(
                                android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                    .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                            )
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text(
                            "Allow in Settings",
                            style = MakariosTypography.labelSmall,
                            color = light.text
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Text links: "Widget settings" and "Reminder settings"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = { onOpenWidgetSettings?.invoke() },
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
            ) {
                Text(
                    "Widget settings >",
                    style = MakariosTypography.labelMedium,
                    color = light.text,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
            }
            Text(
                "•",
                style = MakariosTypography.labelMedium,
                color = light.text.copy(alpha = 0.40f),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            TextButton(
                onClick = { onOpenReminderSettings?.invoke() },
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
            ) {
                Text(
                    "Reminder settings >",
                    style = MakariosTypography.labelMedium,
                    color = light.text,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

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






