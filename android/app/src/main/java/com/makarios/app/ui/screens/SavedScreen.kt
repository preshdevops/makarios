package com.makarios.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.makarios.app.data.Affirmation
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.data.CuratedTopicRepository
import com.makarios.app.ui.components.Pairing
import com.makarios.app.ui.theme.*
import com.makarios.app.util.ShareHelper

@Composable
fun SavedScreen(onNavigateToCreate: () -> Unit = {}, modifier: Modifier = Modifier) {
    val context = LocalContext.current; var tab by remember { mutableStateOf("Declarations") }
    val saved = remember(AffirmationRepository.savedAffirmationIds.size) { AffirmationRepository.getSaved() }; val mine = remember(AffirmationRepository.personalAffirmations.size) { AffirmationRepository.personalAffirmations.toList() }; val list = if (tab == "Mine") mine else saved
    SystemBarsController(Light.Dawn)
    Column(modifier.fillMaxSize().background(Color(0xFFFBF9F5)).statusBarsPadding()) {
        Text("Kept", style = MakariosTypography.displayLarge, color = Ink, modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
        Row(Modifier.padding(horizontal = 24.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) { KeptTab("Declarations", saved.size, tab == "Declarations") { tab = "Declarations" }; KeptTab("Mine", mine.size, tab == "Mine") { tab = "Mine" } }
        if (list.isEmpty()) Text(if (tab == "Mine") "Your written declarations will appear here." else "Nothing kept yet.", color = Ink.copy(alpha = .65f), modifier = Modifier.padding(32.dp))
        else LazyColumn(contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { items(list, key = { it.id }) { KeptCard(it, lightFor(it), context) } }
    }
}
private fun lightFor(affirmation: Affirmation): Light = CuratedTopicRepository.TOPIC_METAS.firstOrNull { it.name.equals(affirmation.category, true) }?.light ?: Light.Grove
@Composable private fun KeptCard(affirmation: Affirmation, light: Light, context: android.content.Context) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).drawBehind { drawLight(light, size, horizon = HorizonSpec(.72f, .80f, .88f)) }.padding(20.dp)) {
        Pairing(affirmation.declaration, affirmation.scriptureText, affirmation.reference, light)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { IconButton(onClick = { ShareHelper.shareGeneric(context, affirmation, light) }) { Icon(Icons.Outlined.Share, "Share declaration", tint = light.text) }; IconButton(onClick = { AffirmationRepository.unsave(affirmation.id); Toast.makeText(context, "Removed from Kept", Toast.LENGTH_SHORT).show() }) { Icon(Icons.Outlined.Delete, "Remove from Kept", tint = light.text) } }
    }
}
@Composable private fun KeptTab(label: String, count: Int, selected: Boolean, onClick: () -> Unit) { TextButton(onClick = onClick) { Text("$label  $count", color = if (selected) Ink else Ink.copy(alpha = .55f), style = MakariosTypography.labelLarge) } }


