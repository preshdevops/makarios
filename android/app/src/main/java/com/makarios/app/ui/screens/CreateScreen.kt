package com.makarios.app.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.data.Affirmation
import com.makarios.app.ui.components.Pairing
import com.makarios.app.ui.theme.*

@Composable
fun CreateScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableStateOf(1) }
    var declarationText by remember { mutableStateOf("") }
    var tone by remember { mutableStateOf("Still") }
    var selectedLight by remember { mutableStateOf(Light.Dawn) }
    var format by remember { mutableStateOf("WhatsApp Status") }
    var shareToCommunity by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Porcelain)
            .padding(top = 48.dp) // Status bar padding
    ) {
        // Thin 3-segment progress bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Ink))
            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(if (step >= 2) Ink else Ink.copy(alpha = 0.2f)))
            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(if (step >= 3) Ink else Ink.copy(alpha = 0.2f)))
        }

        Crossfade(targetState = step, label = "CreateSteps") { currentStep ->
            when (currentStep) {
                1 -> WriteStep(
                    text = declarationText,
                    onTextChanged = { declarationText = it },
                    tone = tone,
                    onToneChanged = { tone = it },
                    onNext = { step = 2 }
                )
                2 -> VerseStep(
                    declarationText = declarationText,
                    onNext = { step = 3 },
                    onEdit = { step = 1 }
                )
                3 -> LookStep(
                    declarationText = declarationText,
                    selectedLight = selectedLight,
                    onLightChanged = { selectedLight = it },
                    format = format,
                    onFormatChanged = { format = it },
                    shareToCommunity = shareToCommunity,
                    onShareCommunityChanged = { shareToCommunity = it },
                    onShare = { /* Share Logic */ onNavigateBack() }
                )
            }
        }
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        BasicTextField(
            value = text,
            onValueChange = { if (it.length <= 280) onTextChanged(it) },
            textStyle = MakariosTypography.displaySmall.copy(color = Ink),
            modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp),
            decorationBox = { innerTextField ->
                if (text.isEmpty()) {
                    Text("I am...", style = MakariosTypography.displaySmall.copy(color = Ink.copy(alpha = 0.3f), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic))
                }
                innerTextField()
            }
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Say it in first person.", style = MakariosTypography.labelSmall, color = Ink.copy(alpha = 0.7f))
            Text("${text.length} / 280", style = MakariosTypography.labelSmall, color = Ink.copy(alpha = 0.7f))
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Tone Row
        Row(
            modifier = Modifier.fillMaxWidth().height(40.dp).border(1.dp, Ink.copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("Still", "Bold", "Gentle").forEach { t ->
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (tone == t) Ink else androidx.compose.ui.graphics.Color.Transparent)
                        .clickable { onToneChanged(t) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(t, style = MakariosTypography.labelLarge, color = if (tone == t) Cream else Ink)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text("Or begin with one of these", style = MakariosTypography.labelMedium, color = Ink.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(16.dp))
        
        // Peek Pairings
        // (Horizontally scrolling peek of 3 Pairings) Placeholder
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Cream),
            shape = RoundedCornerShape(28.dp),
            enabled = text.isNotBlank()
        ) {
            Text("Find my verse", style = MakariosTypography.labelLarge)
        }
    }
}

@Composable
fun VerseStep(
    declarationText: String,
    onNext: () -> Unit,
    onEdit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Your words, standing on this verse.", style = MakariosTypography.labelMedium, color = Ink.copy(alpha = 0.7f))
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Box(
            modifier = Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(20.dp)).lightBackground(Light.Midday).padding(24.dp)
        ) {
            Pairing(
                declaration = declarationText,
                verseText = "For we are his workmanship, created in Christ Jesus for good works...",
                verseReference = "Ephesians 2:10",
                light = Light.Midday,
                isCompact = true
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Another verse", style = MakariosTypography.labelLarge, color = Ink, modifier = Modifier.clickable { })
            Text("1 of 20", style = MakariosTypography.labelSmall, color = Ink.copy(alpha = 0.7f))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Search the Bible", style = MakariosTypography.labelLarge, color = Ink, modifier = Modifier.clickable { })
            Text("Edit declaration", style = MakariosTypography.labelLarge, color = Ink, modifier = Modifier.clickable { onEdit() })
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(56.dp),
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
    selectedLight: Light,
    onLightChanged: (Light) -> Unit,
    format: String,
    onFormatChanged: (String) -> Unit,
    shareToCommunity: Boolean,
    onShareCommunityChanged: (Boolean) -> Unit,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp) // Fit to height preview
                .clip(RoundedCornerShape(20.dp))
                .lightBackground(selectedLight)
                .padding(24.dp)
        ) {
            // Simplified preview for aspect ratio
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Formats
        Row(
            modifier = Modifier.fillMaxWidth().height(40.dp).border(1.dp, Ink.copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("Status", "Story", "Square", "X").forEach { f ->
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (format == f) Ink else androidx.compose.ui.graphics.Color.Transparent)
                        .clickable { onFormatChanged(f) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(f, style = MakariosTypography.labelSmall, color = if (format == f) Cream else Ink)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Swatch row (8 lights + auto + photo)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Light.values().take(4).forEach { l ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onLightChanged(l) }) {
                    Box(modifier = Modifier.size(56.dp).clip(CircleShape).lightBackground(l))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(l.name, style = MakariosTypography.labelSmall)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Switch row Share to Community
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Share to Community", style = MakariosTypography.bodyMedium, color = Ink, modifier = Modifier.weight(1f))
            androidx.compose.material3.Switch(
                checked = shareToCommunity,
                onCheckedChange = onShareCommunityChanged,
                colors = androidx.compose.material3.SwitchDefaults.colors(checkedThumbColor = Cream, checkedTrackColor = Ink)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onShare,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Cream),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("Share", style = MakariosTypography.labelLarge)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Text("Save to photos", style = MakariosTypography.labelLarge, color = Ink, modifier = Modifier.clickable { })
            Text("Set as wallpaper", style = MakariosTypography.labelLarge, color = Ink, modifier = Modifier.clickable { })
        }
    }
}
