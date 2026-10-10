package com.makarios.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.ui.theme.*

data class TopicBand(val name: String, val promise: String, val light: Light, val searchCategory: String)
val TopicBands = listOf(
    TopicBand("Identity", "Who you are in Christ", Light.Dawn, "Identity"), TopicBand("Peace", "Rest for a worried mind", Light.Mist, "Peace"),
    TopicBand("Strength", "When you have nothing left", Light.Ember, "Strength"), TopicBand("Purpose", "Why you are here", Light.Rain, "Purpose"),
    TopicBand("Courage", "For the next brave step", Light.Dusk, "Courage"), TopicBand("Joy", "Gladness that lasts", Light.Midday, "Joy"),
    TopicBand("Provision", "Resting in divine abundance", Light.Grove, "Provision"), TopicBand("Discipline", "Clarity, endurance and focus", Light.Night, "Discipline")
)

@Composable
fun ExploreScreen(onNavigateToTopic: (String, Light) -> Unit = { _, _ -> }, onNavigateToCreate: () -> Unit = {}, modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }; val all = remember { AffirmationRepository.getAll() }
    val bands = TopicBands.filter { query.isBlank() || it.name.contains(query, true) || it.promise.contains(query, true) }
    Column(modifier.fillMaxSize().background(Color(0xFFFBF9F5)).padding(top = 48.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("Explore", style = MakariosTypography.displayLarge, color = Ink); IconButton(onClick = onNavigateToCreate) { Icon(Icons.Outlined.MenuBook, "Open Bible reader", tint = Ink) } }
        Row(Modifier.padding(horizontal = 24.dp).fillMaxWidth().height(56.dp).clip(RoundedCornerShape(16.dp)).background(Ink.copy(alpha = .07f)).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Search, "Search library", tint = Ink.copy(alpha = .6f)); Spacer(Modifier.width(12.dp))
            BasicTextField(query, { query = it }, Modifier.weight(1f), singleLine = true, textStyle = MakariosTypography.bodyMedium.copy(color = Ink), decorationBox = { field -> if (query.isEmpty()) Text("Search promises and scriptures", color = Ink.copy(alpha = .5f)); field() })
        }
        Spacer(Modifier.height(20.dp))
        LazyColumn(contentPadding = PaddingValues(horizontal = 24.dp, bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { items(bands, key = { it.name }) { topic ->
            val count = all.count { it.category.equals(topic.searchCategory, true) }
            Box(Modifier.fillMaxWidth().height(112.dp).clip(RoundedCornerShape(20.dp)).drawBehind { drawLight(topic.light, size, horizon = HorizonSpec(.72f, .80f, .88f)) }.clickable { onNavigateToTopic(topic.name, topic.light) }.padding(16.dp)) {
                Text(topic.name, color = topic.light.text, style = MakariosTypography.displaySmall)
                Row(Modifier.align(Alignment.TopEnd), verticalAlignment = Alignment.CenterVertically) { Text(count.toString(), color = topic.light.text.copy(alpha = .8f)); Icon(Icons.Outlined.ChevronRight, "Open ${topic.name}", tint = topic.light.text) }
                Text(topic.promise, color = topic.light.text.copy(alpha = .9f), style = MakariosTypography.bodyLarge.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic), modifier = Modifier.align(Alignment.BottomStart))
            }
        } }
    }
}


