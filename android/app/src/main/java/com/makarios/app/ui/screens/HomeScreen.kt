package com.makarios.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.ui.components.Pairing
import com.makarios.app.ui.theme.*
import com.makarios.app.util.ShareHelper
import java.util.Calendar
import kotlin.math.sin

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    initialAffirmationId: String? = null,
    onLightChanged: (Light) -> Unit = {}
) {
    val context = LocalContext.current; val haptic = LocalHapticFeedback.current
    var light by remember { mutableStateOf(Light.forNow()) }
    var picker by remember { mutableStateOf(false) }
    var swiped by remember { mutableStateOf(false) }

    LaunchedEffect(light) {
        onLightChanged(light)
    }
    SystemBarsController(light)

    val affirmations = remember { AffirmationRepository.getAll() }
    val initialPage = affirmations.indexOfFirst { it.id == initialAffirmationId }.coerceAtLeast(0)
    val pager = rememberPagerState(initialPage = initialPage, pageCount = { affirmations.size })
    LaunchedEffect(pager.currentPage) {
        swiped = pager.currentPage > 0
        if (affirmations.isNotEmpty()) ProfileActivityLog.recordShown(context)
    }

    Box(modifier.fillMaxSize()) {
        VerticalPager(state = pager, modifier = Modifier.fillMaxSize()) { page ->
            val affirmation = affirmations[page]
            var kept by remember(affirmation.id) { mutableStateOf(AffirmationRepository.isSaved(affirmation.id)) }
            BoxWithConstraints(
                Modifier
                    .fillMaxSize()
                    .drawBehind { drawLight(light, size, discPos = hourDiscPosition(size)) }
            ) {
                Column(
                    Modifier
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(start = 24.dp, top = maxHeight * .10f)
                        .widthIn(max = 280.dp)
                        .clickable { ProfileActivityLog.recordOpened(context) }
                ) {
                    Pairing(affirmation.declaration, affirmation.scriptureText, affirmation.reference, light)
                }
                Column(
                    Modifier
                        .align(Alignment.CenterEnd)
                        .windowInsetsPadding(WindowInsets.systemBars)
                        .padding(end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconRailButton(Icons.Default.Share, "Share declaration", light) {
                        ProfileActivityLog.recordShared(context)
                        ShareHelper.shareGeneric(context, affirmation, light)
                    }
                    IconRailButton(
                        if (kept) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        if (kept) "Remove from Kept" else "Keep declaration",
                        light
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        AffirmationRepository.toggleSave(affirmation.id)
                        kept = !kept
                        if (kept) ProfileActivityLog.recordKept(context)
                        Toast.makeText(context, if (kept) "Saved to Kept" else "Removed from Kept", Toast.LENGTH_SHORT).show()
                    }
                    IconRailButton(Icons.Outlined.Lightbulb, "Change Light", light) { picker = true }
                }
                if (!swiped) {
                    Text(
                        "Swipe up for another",
                        color = light.text.copy(alpha = .6f),
                        fontSize = 13.sp,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(start = 24.dp, bottom = 76.dp)
                    )
                }
            }
        }
    }
    if (picker) {
        LightPicker(
            current = light,
            choose = {
                light = it
                onLightChanged(it)
                picker = false
            },
            close = { picker = false }
        )
    }
}
private fun hourDiscPosition(size: androidx.compose.ui.geometry.Size): androidx.compose.ui.geometry.Offset { val now = Calendar.getInstance(); val hour = now.get(Calendar.HOUR_OF_DAY) + now.get(Calendar.MINUTE) / 60f; val phase = ((hour - 5.5f) / 14f).coerceIn(0f, 1f); return androidx.compose.ui.geometry.Offset(size.width * (.25f + .5f * phase), size.height * (.65f - .2f * sin(Math.PI * phase).toFloat())) }
@Composable private fun LightPicker(current: Light, choose: (Light) -> Unit, close: () -> Unit) { Dialog(onDismissRequest = close) { Column(Modifier.clip(RoundedCornerShape(24.dp)).background(Color(0xFFFBF9F5)).padding(20.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Choose Light", style = MakariosTypography.displaySmall, color = Ink); TextButton(onClick = close) { Text("Done", color = Ink) } }; listOf(Light.Dawn, Light.Midday, Light.Mist, Light.Rain, Light.Ember, Light.Dusk, Light.Grove, Light.Night).chunked(4).forEach { row -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { row.forEach { item -> Box(Modifier.weight(1f).padding(vertical = 6.dp).size(52.dp).clip(RoundedCornerShape(12.dp)).lightBackground(item).clickable { choose(item) }, contentAlignment = Alignment.Center) { if (item == current) Text("\u2713", color = item.text) } } } } } } }
@Composable fun IconRailButton(icon: androidx.compose.ui.graphics.vector.ImageVector, contentDescription: String, light: Light, onClick: () -> Unit) { IconButton(onClick = onClick, modifier = Modifier.size(48.dp).clip(CircleShape).background(light.text.copy(alpha = .14f))) { Icon(icon, contentDescription, tint = light.text) } }