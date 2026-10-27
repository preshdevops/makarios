package com.makarios.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.makarios.app.data.Affirmation
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.data.local.KeptRepository
import com.makarios.app.data.local.toAffirmation
import com.makarios.app.ui.components.Pairing
import com.makarios.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun DeclarationViewScreen(
    id: String,
    origin: String,
    onClose: () -> Unit,
    onNext: () -> Unit,
    onShare: (Affirmation, Light) -> Unit,
    onKeep: (Affirmation, Light) -> Unit,
    onDeclare: (Affirmation) -> Unit,
    onWallpaper: () -> Unit,
    modifier: Modifier = Modifier
) {
    val light = LocalLight.current
    var affirmation by remember(id) { mutableStateOf<Affirmation?>(null) }
    var missing by remember(id) { mutableStateOf(false) }
    var kept by remember(id) { mutableStateOf(false) }

    LaunchedEffect(id) {
        val local = withContext(Dispatchers.IO) {
            runCatching {
                val entity = KeptRepository.getById(id)
                if (entity?.deletedAt == null) entity else null
            }.getOrNull()
        }
        val item = local?.toAffirmation() ?: AffirmationRepository.getById(id)
        if (item == null) {
            missing = true
            affirmation = AffirmationRepository.affirmationOfTheDay
        } else {
            affirmation = item
            kept = local != null && local.sourceType != "authored"
        }
    }

    val item = affirmation
    SystemBarsController(light)
    Column(
        modifier = modifier.fillMaxSize()
            .drawBehind { drawLight(light, size, horizon = HorizonSpec(.72f, .8f, .88f), discScale = .48f) }
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 22.dp, vertical = 12.dp)
            .semantics {
                contentDescription = item?.let { it.declaration + ". " + it.scriptureText + ". " + it.reference }.orEmpty()
            }
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) {
                Icon(Icons.Outlined.Close, contentDescription = "Close", tint = light.text)
            }
            Text(
                when {
                    missing -> "That one is gone. Here is today's."
                    origin == "widget" -> "From your widget"
                    else -> "From your reminder"
                },
                color = light.text.copy(alpha = light.secondaryAlpha),
                style = MakariosTypography.labelMedium,
                modifier = Modifier.weight(1f)
            )
        }
        if (item == null) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = light.accent)
            }
        } else {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Pairing(
                    declaration = item.declaration,
                    verseText = item.scriptureText,
                    verseReference = item.reference,
                    light = light,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 28.dp)
                )
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { onShare(item, light) }) { Text("Share", color = light.text) }
                TextButton(onClick = { kept = !kept; onKeep(item, light) }) {
                    Icon(if (kept) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder, contentDescription = null, tint = light.text)
                    Spacer(Modifier.width(4.dp))
                    Text(if (kept) "Saved" else "Save", color = light.text)
                }
                TextButton(onClick = onWallpaper) { Text("Wallpaper", color = light.text) }
                TextButton(onClick = onNext) { Text("Next", color = light.text) }
            }
            TextButton(onClick = { onDeclare(item) }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Declare this", color = light.text, style = MakariosTypography.labelLarge)
            }
        }
    }
}

