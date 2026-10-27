package com.makarios.app.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.data.local.KeptRepository
import com.makarios.app.data.ShareRepository
import com.makarios.app.data.ShareRequest
import com.makarios.app.data.ShareResult
import com.makarios.app.data.ShareTarget
import com.makarios.app.ui.theme.Light
import com.makarios.app.ui.theme.MakariosTypography
import com.makarios.app.ui.theme.getCurrentLightForTime
import com.makarios.app.ui.wallpaper.*
import com.makarios.app.util.ExportFormat
import com.makarios.app.util.WallpaperHelper
import com.makarios.app.util.WallpaperScreenTarget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

data class ShareContent(
    val declaration: String = "",
    val verse: String? = null,
    val reference: String? = null,
    val initialStyleId: String? = null,
    val initialLightName: String? = null,
    val initialFormat: String? = null,
    val initialStyleOptionsJson: String? = null,
    val declarationId: String? = null,
    val source: String = "today",
    val contentType: String = "declaration" // "declaration", "verse", "community"
)

enum class WhatToShareOption(val label: String) {
    DECLARATION_AND_VERSE("Declaration & verse"),
    VERSE_ONLY("Verse only"),
    DECLARATION_ONLY("Declaration only"),
    TEXT_ONLY("Text only")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareSheet(
    content: ShareContent,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { android.util.Log.d("MakariosShare", "ShareSheet opened from=${content.source}") }
    val coroutineScope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("makarios_share_prefs", Context.MODE_PRIVATE) }

    // 1. Initial State Resolution
    val type = content.contentType
    val defaultLight = remember { getCurrentLightForTime() }

    val savedLightName = content.initialLightName ?: prefs.getString("last_light_$type", defaultLight.name)
    var selectedLight by remember(savedLightName) {
        mutableStateOf(Light.values().firstOrNull { it.name.equals(savedLightName, ignoreCase = true) } ?: defaultLight)
    }

    val savedStyleId = content.initialStyleId ?: prefs.getString("last_style_$type", StyleRegistry.all.firstOrNull()?.id ?: "sanctuary")
    var selectedStyle by remember(savedStyleId) {
        mutableStateOf(StyleRegistry[savedStyleId ?: ""] ?: StyleRegistry.all.firstOrNull() ?: StyleRegistry.PAIRING)
    }

    val savedFormatName = content.initialFormat ?: prefs.getString("last_format_$type", "Story")
    var selectedFormat by remember(savedFormatName) {
        mutableStateOf(ExportFormat.from(savedFormatName ?: "Story"))
    }

    val hasDecl = content.declaration.isNotBlank()
    val hasVerse = !content.verse.isNullOrBlank()

    val initialWhat = when {
        hasDecl && hasVerse -> WhatToShareOption.DECLARATION_AND_VERSE
        hasVerse -> WhatToShareOption.VERSE_ONLY
        else -> WhatToShareOption.DECLARATION_ONLY
    }
    var whatToShare by remember { mutableStateOf(initialWhat) }

    var styleOptions by remember(content.initialStyleOptionsJson) {
        mutableStateOf(StyleOptions.fromJson(content.initialStyleOptionsJson))
    }

    var showCustomiseDialog by remember { mutableStateOf(false) }
    var showWallpaperDialog by remember { mutableStateOf(false) }
    var isSharing by remember { mutableStateOf(false) }

    // Active Category Filter
    var activeCategory by remember { mutableStateOf("All") }
    val stylesToDisplay = remember(activeCategory) {
        if (activeCategory == "All") StyleRegistry.all else StyleRegistry.byCategory(activeCategory)
    }

    // Persist memory on change
    fun persistChoice(style: Style, light: Light, format: ExportFormat, what: WhatToShareOption, options: StyleOptions) {
        prefs.edit()
            .putString("last_style_$type", style.id)
            .putString("last_light_$type", light.name)
            .putString("last_format_$type", format.name)
            .putString("last_what_$type", what.name)
            .apply()

        // Also update per-declaration memory if declarationId exists
        content.declarationId?.let { declId ->
            coroutineScope.launch(Dispatchers.IO) {
                runCatching {
                    KeptRepository.updateStyleMemory(
                        id = declId,
                        styleId = style.id,
                        lightName = light.name,
                        format = format.name,
                        styleOptionsJson = options.toJson()
                    )
                }
            }
        }
    }

    // Resolved StyleSpec
    val resolvedSpec = remember(content, whatToShare, styleOptions) {
        when (whatToShare) {
            WhatToShareOption.DECLARATION_AND_VERSE -> StyleSpec(
                declaration = content.declaration,
                verse = content.verse,
                reference = content.reference,
                options = styleOptions,
                milestoneDays = styleOptions.daysCount,
                recipientName = styleOptions.recipientName,
                timestampFormatted = styleOptions.date
            )
            WhatToShareOption.VERSE_ONLY -> StyleSpec(
                declaration = content.verse ?: content.declaration,
                verse = null,
                reference = content.reference,
                options = styleOptions
            )
            WhatToShareOption.DECLARATION_ONLY -> StyleSpec(
                declaration = content.declaration,
                verse = null,
                reference = null,
                options = styleOptions,
                milestoneDays = styleOptions.daysCount,
                recipientName = styleOptions.recipientName,
                timestampFormatted = styleOptions.date
            )
            WhatToShareOption.TEXT_ONLY -> StyleSpec(
                declaration = content.declaration,
                verse = content.verse,
                reference = content.reference,
                options = styleOptions
            )
        }
    }

    // Render Preview Bitmap
    val previewSize = remember(selectedFormat) {
        when (selectedFormat) {
            ExportFormat.Square -> IntSize(400, 400)
            ExportFormat.Portrait -> IntSize(360, 450)
            ExportFormat.X -> IntSize(480, 270)
            ExportFormat.Story -> IntSize(270, 480)
            ExportFormat.Wallpaper -> IntSize(270, 580)
        }
    }

    val previewBitmap by produceState<Bitmap?>(null, selectedStyle.id, selectedLight.name, selectedFormat.name, whatToShare, styleOptions) {
        value = withContext(Dispatchers.Default) {
            runCatching {
                selectedStyle.render(resolvedSpec, selectedLight, previewSize)
            }.getOrNull()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = selectedLight.top,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.92f)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Share",
                    style = MakariosTypography.displaySmall,
                    color = selectedLight.text,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close", tint = selectedLight.text)
                }
            }

            Spacer(Modifier.height(12.dp))

            // 1. Live Preview
            Box(
                modifier = Modifier
                    .height(240.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(selectedLight.surfaceTint),
                contentAlignment = Alignment.Center
            ) {
                val bmp = previewBitmap
                if (bmp != null && !bmp.isRecycled) {
                    androidx.compose.foundation.Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Share Preview",
                        modifier = Modifier
                            .fillMaxHeight(0.92f)
                            .aspectRatio(previewSize.width.toFloat() / previewSize.height.toFloat())
                            .clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    CircularProgressIndicator(color = selectedLight.text, modifier = Modifier.size(28.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            // 2. "What to share" chips
            Text(
                "WHAT TO SHARE",
                style = MakariosTypography.labelSmall,
                color = selectedLight.text.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (hasDecl && hasVerse) {
                    MakChip(
                        selected = whatToShare == WhatToShareOption.DECLARATION_AND_VERSE,
                        onClick = { whatToShare = WhatToShareOption.DECLARATION_AND_VERSE },
                        label = { Text("Declaration & verse") }
                    )
                }
                if (hasVerse) {
                    MakChip(
                        selected = whatToShare == WhatToShareOption.VERSE_ONLY,
                        onClick = { whatToShare = WhatToShareOption.VERSE_ONLY },
                        label = { Text("Verse only") }
                    )
                }
                if (hasDecl) {
                    MakChip(
                        selected = whatToShare == WhatToShareOption.DECLARATION_ONLY,
                        onClick = { whatToShare = WhatToShareOption.DECLARATION_ONLY },
                        label = { Text("Declaration only") }
                    )
                }
                MakChip(
                    selected = whatToShare == WhatToShareOption.TEXT_ONLY,
                    onClick = { whatToShare = WhatToShareOption.TEXT_ONLY },
                    label = { Text("Text only") }
                )
            }

            Spacer(Modifier.height(16.dp))

            // 3. Style Picker with Category Chips + Customise button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "STYLE",
                    style = MakariosTypography.labelSmall,
                    color = selectedLight.text.copy(alpha = 0.6f)
                )

                if (selectedStyle.supportedOptions.isNotEmpty()) {
                    TextButton(
                        onClick = { showCustomiseDialog = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Outlined.Tune, contentDescription = null, tint = selectedLight.text, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Customise", style = MakariosTypography.labelMedium, color = selectedLight.text)
                    }
                }
            }
            Spacer(Modifier.height(6.dp))

            // Category Chips: All, Simple, Scene, Frame, Journal, Bold
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StyleRegistry.categories.forEach { category ->
                    val isCatSelected = activeCategory == category
                    MakChip(
                        selected = isCatSelected,
                        onClick = { activeCategory = category },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = selectedLight.anchorRule.copy(alpha = 0.2f),
                            selectedLabelColor = selectedLight.text
                        )
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Style thumbnails row
            val lazyListState = rememberLazyListState()
            LaunchedEffect(selectedStyle.id) {
                val idx = stylesToDisplay.indexOfFirst { it.id == selectedStyle.id }
                if (idx >= 0) {
                    lazyListState.animateScrollToItem(idx)
                }
            }

            LazyRow(
                state = lazyListState,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(stylesToDisplay, key = { "share-style-${it.id}" }) { style ->
                    val isSelected = style.id == selectedStyle.id
                    val thumb by produceState<android.graphics.Bitmap?>(null, style.id, selectedLight.name, whatToShare) {
                        value = withContext(Dispatchers.Default) { runCatching { style.render(resolvedSpec, selectedLight, IntSize(168, 224)) }.getOrNull() }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { selectedStyle = style; persistChoice(style, selectedLight, selectedFormat, whatToShare, styleOptions) }) {
                        Box(Modifier.size(width = 84.dp, height = 112.dp).clip(RoundedCornerShape(12.dp)).background(selectedLight.surfaceTint).border(width = if (isSelected) 3.dp else 1.dp, color = if (isSelected) selectedLight.accent else selectedLight.line, shape = RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                            thumb?.let { androidx.compose.foundation.Image(it.asImageBitmap(), contentDescription = style.displayName, modifier = Modifier.fillMaxSize()) }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(style.displayName, style = MakariosTypography.labelSmall, color = selectedLight.text, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // 4. Light row (8 gradient dots)
            Text(
                "LIGHT",
                style = MakariosTypography.labelSmall,
                color = selectedLight.text.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Light.values().forEach { light ->
                    val isLightSelected = light.name == selectedLight.name
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Brush.verticalGradient(listOf(light.top, light.bottom)))
                            .clickable {
                                selectedLight = light; persistChoice(selectedStyle, light, selectedFormat, whatToShare, styleOptions)
                            }
                            .border(
                                width = if (isLightSelected) 3.dp else 1.dp,
                                color = if (isLightSelected) selectedLight.accent else Color.Transparent,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLightSelected) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(light.text)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // 5. Size chips: Story, Square, Portrait, X post, Wallpaper
            Text(
                "SIZE",
                style = MakariosTypography.labelSmall,
                color = selectedLight.text.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val sizes = listOf(
                    Pair(ExportFormat.Story, "Story"),
                    Pair(ExportFormat.Square, "Square"),
                    Pair(ExportFormat.Portrait, "Portrait"),
                    Pair(ExportFormat.X, "X post"),
                    Pair(ExportFormat.Wallpaper, "Wallpaper")
                )
                sizes.forEach { (fmt, label) ->
                    MakChip(
                        selected = selectedFormat == fmt,
                        onClick = {
                            selectedFormat = fmt
                            persistChoice(selectedStyle, selectedLight, fmt, whatToShare, styleOptions)
                        },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Spacer(Modifier.height(8.dp))

            // 7. Direct Targets Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // WhatsApp
                if (ShareRepository.isWhatsAppInstalled(context)) {
                    DirectTargetIcon(
                        icon = Icons.Outlined.Chat,
                        label = "WhatsApp",
                        light = selectedLight,
                        onClick = {
                            coroutineScope.launch {
                                val req = ShareRequest(resolvedSpec, selectedLight, selectedFormat, selectedStyle, ShareTarget.WhatsAppChat)
                                val res = ShareRepository.share(context, req)
                                if (res is ShareResult.Failed) Toast.makeText(context, "Could not open WhatsApp. Try More.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    DirectTargetIcon(
                        icon = Icons.Outlined.HistoryToggleOff,
                        label = "Status",
                        light = selectedLight,
                        onClick = {
                            coroutineScope.launch {
                                val req = ShareRequest(resolvedSpec, selectedLight, selectedFormat, selectedStyle, ShareTarget.WhatsAppStatus)
                                val res = ShareRepository.share(context, req)
                                if (res is ShareResult.Failed) Toast.makeText(context, "Could not open WhatsApp. Try More.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }

                // Instagram
                if (ShareRepository.isInstagramInstalled(context)) {
                    DirectTargetIcon(
                        icon = Icons.Outlined.CameraAlt,
                        label = "IG Story",
                        light = selectedLight,
                        onClick = {
                            coroutineScope.launch {
                                val req = ShareRequest(resolvedSpec, selectedLight, selectedFormat, selectedStyle, ShareTarget.InstagramStories)
                                val res = ShareRepository.share(context, req)
                                if (res is ShareResult.Failed) Toast.makeText(context, "Could not open Instagram. Try More.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    DirectTargetIcon(
                        icon = Icons.Outlined.Send,
                        label = "IG Chat",
                        light = selectedLight,
                        onClick = {
                            coroutineScope.launch {
                                val req = ShareRequest(resolvedSpec, selectedLight, selectedFormat, selectedStyle, ShareTarget.InstagramChat)
                                val res = ShareRepository.share(context, req)
                                if (res is ShareResult.Failed) Toast.makeText(context, "Could not open Instagram. Try More.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }

                listOf(ShareTarget.X, ShareTarget.Facebook, ShareTarget.Telegram, ShareTarget.Messages, ShareTarget.Email).filter { it.packageName == null || ShareRepository.isAppInstalled(context, it.packageName) }.forEach { target ->
                    DirectTargetIcon(icon = Icons.Outlined.Send, label = target.label, light = selectedLight, onClick = {
                        coroutineScope.launch {
                            val res = ShareRepository.share(context, ShareRequest(resolvedSpec, selectedLight, selectedFormat, selectedStyle, target))
                            if (res is ShareResult.Failed) Toast.makeText(context, "Could not open ${target.label}. Try More.", Toast.LENGTH_SHORT).show()
                        }
                    })
                }

                // Gallery
                DirectTargetIcon(
                    icon = Icons.Outlined.PhotoLibrary,
                    label = "Gallery",
                    light = selectedLight,
                    onClick = {
                        coroutineScope.launch {
                            val req = ShareRequest(resolvedSpec, selectedLight, selectedFormat, selectedStyle, ShareTarget.SaveToGallery)
                            val res = ShareRepository.share(context, req)
                            if (res is ShareResult.Success) Toast.makeText(context, "Saved to gallery", Toast.LENGTH_SHORT).show()
                            else Toast.makeText(context, "Could not save to gallery", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                // Wallpaper
                DirectTargetIcon(
                    icon = Icons.Outlined.Wallpaper,
                    label = "Wallpaper",
                    light = selectedLight,
                    onClick = {
                        showWallpaperDialog = true
                    }
                )

                // Copy Text
                DirectTargetIcon(
                    icon = Icons.Outlined.ContentCopy,
                    label = "Copy text",
                    light = selectedLight,
                    onClick = {
                        coroutineScope.launch {
                            val req = ShareRequest(resolvedSpec, selectedLight, selectedFormat, selectedStyle, ShareTarget.CopyText)
                            ShareRepository.share(context, req)
                            Toast.makeText(context, "Text copied", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                // Copy Image
                DirectTargetIcon(
                    icon = Icons.Outlined.Image,
                    label = "Copy image",
                    light = selectedLight,
                    onClick = {
                        coroutineScope.launch {
                            val req = ShareRequest(resolvedSpec, selectedLight, selectedFormat, selectedStyle, ShareTarget.CopyImage)
                            val res = ShareRepository.share(context, req)
                            if (res is ShareResult.Success) Toast.makeText(context, "Image copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                // More
                DirectTargetIcon(
                    icon = Icons.Outlined.MoreHoriz,
                    label = "More",
                    light = selectedLight,
                    onClick = {
                        coroutineScope.launch {
                            val req = ShareRequest(resolvedSpec, selectedLight, selectedFormat, selectedStyle, ShareTarget.Chooser)
                            ShareRepository.share(context, req)
                        }
                    }
                )
            }

            Spacer(Modifier.height(24.dp))
        }
        // Sticky footer: Share always visible above the gesture area
        Column(Modifier.fillMaxWidth().background(selectedLight.top).padding(horizontal = 20.dp, vertical = 10.dp).navigationBarsPadding()) {
            Button(
                onClick = {
                    isSharing = true
                    coroutineScope.launch {
                        val req = ShareRequest(resolvedSpec, selectedLight, selectedFormat, selectedStyle, ShareTarget.Chooser)
                        val res = ShareRepository.share(context, req)
                        isSharing = false
                        if (res is ShareResult.Failed) Toast.makeText(context, res.reason, Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = selectedLight.buttonFill, contentColor = selectedLight.buttonText),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isSharing) CircularProgressIndicator(color = selectedLight.buttonText, modifier = Modifier.size(20.dp))
                else { Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("Share", style = MakariosTypography.labelLarge, fontWeight = FontWeight.SemiBold) }
            }
            Spacer(Modifier.height(6.dp))
            Text("Or send straight to: use the row above for WhatsApp, Status, Instagram, X, Facebook, Telegram, Messages, Email, Save, Wallpaper, Copy text, Copy image, More (installed apps only)", style = MakariosTypography.labelSmall, color = selectedLight.textMuted)
        }
        }
    }

    // Customise Dialog (Days, Name, Date, Align, Font)
    if (showCustomiseDialog) {
        AlertDialog(
            onDismissRequest = { showCustomiseDialog = false },
            title = { Text("Customise ${selectedStyle.displayName}", style = MakariosTypography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (selectedStyle.supportedOptions.contains(StyleOptionField.DAYS_COUNT)) {
                        var daysStr by remember { mutableStateOf((styleOptions.daysCount ?: 30).toString()) }
                        OutlinedTextField(
                            value = daysStr,
                            onValueChange = {
                                daysStr = it
                                val d = it.toIntOrNull()
                                styleOptions = styleOptions.copy(daysCount = d)
                            },
                            label = { Text("Number of days") },
                            singleLine = true
                        )
                    }

                    if (selectedStyle.supportedOptions.contains(StyleOptionField.RECIPIENT_NAME)) {
                        var name by remember { mutableStateOf(styleOptions.recipientName.orEmpty()) }
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                styleOptions = styleOptions.copy(recipientName = it.ifBlank { null })
                            },
                            label = { Text("Recipient Name") },
                            singleLine = true
                        )
                    }

                    if (selectedStyle.supportedOptions.contains(StyleOptionField.DATE)) {
                        var dateStr by remember { mutableStateOf(styleOptions.date.orEmpty()) }
                        OutlinedTextField(
                            value = dateStr,
                            onValueChange = {
                                dateStr = it
                                styleOptions = styleOptions.copy(date = it.ifBlank { null })
                            },
                            label = { Text("Date (e.g. 26 October 2026)") },
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showCustomiseDialog = false
                    persistChoice(selectedStyle, selectedLight, selectedFormat, whatToShare, styleOptions)
                }) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomiseDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Wallpaper Screen Selection Dialog (Lock, Home, Both)
    if (showWallpaperDialog) {
        AlertDialog(
            onDismissRequest = { showWallpaperDialog = false },
            title = { Text("Set Wallpaper", style = MakariosTypography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            showWallpaperDialog = false
                            coroutineScope.launch {
                                val req = ShareRequest(resolvedSpec, selectedLight, ExportFormat.Wallpaper, selectedStyle, ShareTarget.SetWallpaper, wallpaperScreen = WallpaperScreenTarget.LOCK)
                                val res = ShareRepository.share(context, req)
                                if (res is ShareResult.Success) Toast.makeText(context, "Set on lock screen", Toast.LENGTH_SHORT).show()
                                else Toast.makeText(context, "Could not set wallpaper", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Lock screen")
                    }
                    TextButton(
                        onClick = {
                            showWallpaperDialog = false
                            coroutineScope.launch {
                                val req = ShareRequest(resolvedSpec, selectedLight, ExportFormat.Wallpaper, selectedStyle, ShareTarget.SetWallpaper, wallpaperScreen = WallpaperScreenTarget.HOME)
                                val res = ShareRepository.share(context, req)
                                if (res is ShareResult.Success) Toast.makeText(context, "Set on home screen", Toast.LENGTH_SHORT).show()
                                else Toast.makeText(context, "Could not set wallpaper", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Home screen")
                    }
                    TextButton(
                        onClick = {
                            showWallpaperDialog = false
                            coroutineScope.launch {
                                val req = ShareRequest(resolvedSpec, selectedLight, ExportFormat.Wallpaper, selectedStyle, ShareTarget.SetWallpaper, wallpaperScreen = WallpaperScreenTarget.BOTH)
                                val res = ShareRepository.share(context, req)
                                if (res is ShareResult.Success) Toast.makeText(context, "Set on lock and home screens", Toast.LENGTH_SHORT).show()
                                else Toast.makeText(context, "Could not set wallpaper", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Both screens")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showWallpaperDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DirectTargetIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    light: Light,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(light.text.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = light.text, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            style = MakariosTypography.labelSmall,
            color = light.text.copy(alpha = 0.8f),
            fontSize = 11.sp
        )
    }
}
