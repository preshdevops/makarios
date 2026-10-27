package com.makarios.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.data.CommunityRepository
import com.makarios.app.data.PublicAffirmation
import com.makarios.app.data.local.KeptEntity
import com.makarios.app.data.local.KeptRepository
import com.makarios.app.data.local.toAffirmation
import com.makarios.app.ui.components.Pairing
import com.makarios.app.ui.theme.*
import com.makarios.app.util.CrashReporter
import com.makarios.app.util.ShareHelper
import com.makarios.app.ui.components.ShareSheet
import com.makarios.app.ui.components.ShareContent
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun SavedScreen(
    onNavigateToCreate: () -> Unit = {},
    onOpenWidgetSettings: () -> Unit = {},
    onOpenReminderSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var tab by rememberSaveable { mutableStateOf("My declarations") }

    val declarations by produceState<List<KeptEntity>>(initialValue = emptyList()) {
        KeptRepository.getDeclarationsFlow()
            .catch { t ->
                CrashReporter.logNonFatal("SavedScreen", t)
                emit(emptyList())
            }
            .collect { value = it }
    }
    val mine by produceState<List<KeptEntity>>(initialValue = emptyList()) {
        KeptRepository.getMineFlow()
            .catch { t ->
                CrashReporter.logNonFatal("SavedScreen", t)
                emit(emptyList())
            }
            .collect { value = it }
    }

    val light = LocalLight.current
    SystemBarsController(light)
    ProvideActiveLight(light, "Saved: $tab") {}

    var shareContentToOpen by remember { mutableStateOf<ShareContent?>(null) }

    // Listen to community stream when on Community tab
    DisposableEffect(tab) {
        if (tab == "Community") {
            runCatching { CommunityRepository.startListeningToPublicDeclarations() }
                .onFailure { CrashReporter.logNonFatal("SavedScreen", it) }
        }
        onDispose {
            if (tab == "Community") {
                runCatching { CommunityRepository.stopListeningToPublicDeclarations() }
            }
        }
    }

    val list = remember(tab, mine, declarations) {
        when (tab) {
            "My declarations" -> mine
            "Verses" -> declarations.filter { it.sourceType != "authored" }
            else -> emptyList()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Ink,
                    contentColor = Cream,
                    actionColor = MakError
                )
            }
        },
        containerColor = light.bottom,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
        ) {
            Text(
                text = "Saved",
                style = MakariosTypography.displayLarge,
                color = Ink,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            // Segmented control: My declarations, Verses, Community
            Row(
                Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SavedTab("My declarations", tab == "My declarations") { tab = "My declarations" }
                SavedTab("Verses", tab == "Verses") { tab = "Verses" }
                SavedTab("Community", tab == "Community") { tab = "Community" }
            }

            if (tab == "Community") {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = "Declarations people chose to share. Yours stay private unless you publish them.",
                        style = MakariosTypography.bodyMedium,
                        color = Ink.copy(alpha = 0.65f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    val communityItems = CommunityRepository.publicDeclarations
                    if (communityItems.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "No public declarations shared yet.",
                                style = MakariosTypography.bodyMedium,
                                color = Ink.copy(alpha = 0.65f)
                            )
                            Button(
                                onClick = onNavigateToCreate,
                                colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Cream)
                            ) {
                                Text("Write a declaration")
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 120.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(communityItems.distinctBy { it.id }, key = { "saved-community-${it.id}" }) { item ->
                                CommunityDeclarationItem(
                                    item = item,
                                    onAmen = {
                                        CommunityRepository.toggleAmen(item.id, item.amenedBy)
                                    },
                                    onSave = {
                                        coroutineScope.launch {
                                            KeptRepository.save(
                                                KeptEntity(
                                                    id = "community-${item.id}",
                                                    declaration = item.declaration,
                                                    verseText = item.scriptureText,
                                                    reference = item.reference,
                                                    lightName = Light.Dawn.name,
                                                    styleId = "scene",
                                                    sourceType = "community"
                                                )
                                            )
                                            snackbarHostState.showSnackbar("Saved to My declarations")
                                        }
                                    },
                                    onShare = {
                                        shareContentToOpen = ShareContent(
                                            declaration = item.declaration,
                                            verse = item.scriptureText,
                                            reference = item.reference,
                                            initialLightName = Light.Dawn.name,
                                            source = "saved", contentType = "community"
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            } else if (list.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (tab == "My declarations") "Your written declarations will appear here." else "Verses you save from the Bible will appear here.",
                        style = MakariosTypography.bodyMedium,
                        color = Ink.copy(alpha = 0.65f)
                    )
                    Button(
                        onClick = onNavigateToCreate,
                        colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Cream)
                    ) {
                        Text(if (tab == "My declarations") "Write a declaration" else "Explore verses")
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(list.distinctBy { it.id }, key = { "saved-item-${it.id}" }) { entity ->
                        val cardLight = runCatching { Light.valueOf(entity.lightName) }.getOrDefault(Light.Grove)
                        SavedItem(
                            entity = entity,
                            light = cardLight,
                            onShare = {
                                shareContentToOpen = ShareContent(
                                    declaration = entity.declaration,
                                    verse = entity.verseText,
                                    reference = entity.reference,
                                    initialStyleId = entity.styleId,
                                    initialLightName = entity.lightName,
                                    initialFormat = entity.format,
                                    initialStyleOptionsJson = entity.styleOptionsJson,
                                    declarationId = entity.id,
                                    source = "saved", contentType = if (entity.sourceType == "verse") "verse" else "declaration"
                                )
                            },
                            onRemove = {
                                coroutineScope.launch {
                                    KeptRepository.softDelete(entity.id)
                                    val undoRequest = coroutineScope.async {
                                        snackbarHostState.showSnackbar(
                                            message = "Removed.",
                                            actionLabel = "Undo",
                                            duration = SnackbarDuration.Indefinite
                                        )
                                    }
                                    val result = withTimeoutOrNull(8_000L) { undoRequest.await() }
                                    if (result == SnackbarResult.ActionPerformed) {
                                        KeptRepository.restore(entity.id)
                                    } else {
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        undoRequest.cancel()
                                    }
                                }
                            },
                            onToggleWidget = {
                                coroutineScope.launch {
                                    val nextVal = !entity.inWidget
                                    KeptRepository.setInWidget(entity.id, nextVal)
                                    snackbarHostState.showSnackbar(
                                        if (nextVal) "Added to widget" else "Removed from widget",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            },
                            onToggleReminders = {
                                coroutineScope.launch {
                                    val nextVal = !entity.inReminders
                                    KeptRepository.setInReminders(entity.id, nextVal)
                                    snackbarHostState.showSnackbar(
                                        if (nextVal) "Added to reminders" else "Removed from reminders",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            },
                            onOpenWidgetSettings = onOpenWidgetSettings,
                            onOpenReminderSettings = onOpenReminderSettings
                        )
                    }
                }
            }
        }
    }

    shareContentToOpen?.let { content ->
        ShareSheet(
            content = content,
            onDismiss = { shareContentToOpen = null }
        )
    }
}

@Composable
private fun SavedItem(
    entity: KeptEntity,
    light: Light,
    onShare: () -> Unit,
    onRemove: () -> Unit,
    onToggleWidget: () -> Unit,
    onToggleReminders: () -> Unit,
    onOpenWidgetSettings: () -> Unit,
    onOpenReminderSettings: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth()) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .drawBehind { drawLight(light, size, horizon = HorizonSpec(.72f, .80f, .88f)) }
                .padding(20.dp)
        ) {
            Pairing(entity.declaration, entity.verseText, entity.reference, light)
        }
        // Action row BELOW the artwork on a plain surface (never on the ridge)
        Row(
            Modifier.fillMaxWidth().background(light.surfaceTint).padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onShare) {
                Icon(Icons.Outlined.Share, "Share declaration", tint = light.text)
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Outlined.Delete, "Remove", tint = light.text)
            }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, "More options", tint = light.text)
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    if (entity.inWidget) "Remove from widget" else "Add to widget",
                                    style = MakariosTypography.bodyMedium
                                )
                                if (entity.inWidget) {
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        "Settings >",
                                        style = MakariosTypography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MakError,
                                        modifier = Modifier.clickable {
                                            menuExpanded = false
                                            onOpenWidgetSettings()
                                        }
                                    )
                                }
                            }
                        },
                        onClick = {
                            menuExpanded = false
                            onToggleWidget()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    if (entity.inReminders) "Remove from reminders" else "Use in reminders",
                                    style = MakariosTypography.bodyMedium
                                )
                                if (entity.inReminders) {
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        "Settings >",
                                        style = MakariosTypography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MakError,
                                        modifier = Modifier.clickable {
                                            menuExpanded = false
                                            onOpenReminderSettings()
                                        }
                                    )
                                }
                            }
                        },
                        onClick = {
                            menuExpanded = false
                            onToggleReminders()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CommunityDeclarationItem(
    item: PublicAffirmation,
    onAmen: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit
) {
    val cardLight = LocalLight.current
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .drawBehind { drawLight(cardLight, size, horizon = HorizonSpec(.72f, .80f, .88f)) }
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (item.authorUsername.isNotBlank()) "@${item.authorUsername}" else item.authorName,
                style = MakariosTypography.labelMedium,
                color = cardLight.text.copy(alpha = 0.70f)
            )
            Text(
                text = item.category,
                style = MakariosTypography.labelSmall,
                color = cardLight.text.copy(alpha = 0.55f)
            )
        }
        Spacer(Modifier.height(8.dp))
        Pairing(item.declaration, item.scriptureText, item.reference, cardLight)
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onAmen) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Amen",
                    tint = cardLight.text,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "${item.amenCount} Amen",
                    color = cardLight.text,
                    style = MakariosTypography.labelMedium
                )
            }
            Row {
                IconButton(onClick = onSave) {
                    Icon(Icons.Outlined.BookmarkBorder, "Save", tint = cardLight.text)
                }
                IconButton(onClick = onShare) {
                    Icon(Icons.Outlined.Share, "Share", tint = cardLight.text)
                }
            }
        }
    }
}

@Composable
private fun SavedTab(label: String, selected: Boolean, onClick: () -> Unit) {
    val light = LocalLight.current
    Column(Modifier.clickable { onClick() }.padding(bottom = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = if (selected) light.text else light.textMuted, style = MakariosTypography.labelLarge.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal))
        Spacer(Modifier.height(6.dp))
        Box(Modifier.height(2.dp).fillMaxWidth().background(if (selected) light.accent else light.line))
    }
}
