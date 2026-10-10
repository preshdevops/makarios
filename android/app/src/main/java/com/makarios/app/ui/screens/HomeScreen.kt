package com.makarios.app.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.makarios.app.data.Affirmation
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.ui.components.AffirmationDetailDialog
import com.makarios.app.ui.components.Pairing
import com.makarios.app.ui.theme.*
import com.makarios.app.util.ShareHelper

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var currentLight by remember { mutableStateOf(getCurrentLightForTime()) }
    var selectedDetailAffirmation by remember { mutableStateOf<Affirmation?>(null) }
    var showLightPicker by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                currentLight = getCurrentLightForTime()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val affirmations = remember { AffirmationRepository.getAll() }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { affirmations.size }
    )

    var hasSwiped by remember { mutableStateOf(false) }
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage > 0) hasSwiped = true
    }

    val currentHour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
    val greeting = remember(currentHour) {
        when (currentHour) {
            in 4..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else     -> "Good evening"
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .lightBackground(currentLight)
    ) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val affirmation = affirmations[page]
            var isSaved by remember(affirmation.id, AffirmationRepository.savedAffirmationIds.size) {
                mutableStateOf(AffirmationRepository.isSaved(affirmation.id))
            }

            Box(modifier = Modifier.fillMaxSize()) {
                // High-resolution photography layer with subtle grade & scrim
                if (affirmation.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = affirmation.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        colorFilter = WarmPhotoGrade
                    )
                    // Gradient scrim to ensure 100% text legibility over photo
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    0.0f to currentLight.top.copy(alpha = 0.85f),
                                    0.45f to currentLight.top.copy(alpha = 0.70f),
                                    1.0f to currentLight.bottom.copy(alpha = 0.95f)
                                )
                            )
                    )
                }

                // The Pairing (optically centered at 40% height, max width 78%, left aligned, clickable)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 24.dp)
                        .clickable { selectedDetailAffirmation = affirmation },
                    horizontalAlignment = Alignment.Start
                ) {
                    Spacer(modifier = Modifier.fillMaxHeight(0.25f))
                    Pairing(
                        declaration = affirmation.declaration,
                        verseText = affirmation.scriptureText,
                        verseReference = affirmation.reference,
                        light = currentLight,
                        modifier = Modifier.fillMaxWidth(0.78f)
                    )
                }

                // Right edge rail (Share, Keep, Light)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 24.dp, bottom = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconRailButton(
                        icon = Icons.Default.Share,
                        contentDescription = "Share",
                        light = currentLight,
                        onClick = {
                            ShareHelper.shareGeneric(
                                context = context,
                                affirmation = affirmation,
                                light = currentLight
                            )
                        }
                    )

                    IconRailButton(
                        icon = if (isSaved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isSaved) "Kept" else "Keep",
                        light = currentLight,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            AffirmationRepository.toggleSave(affirmation.id)
                            isSaved = !isSaved
                            Toast.makeText(
                                context,
                                if (isSaved) "Saved to Kept" else "Removed from Kept",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )

                    IconRailButton(
                        icon = Icons.Outlined.Lightbulb,
                        contentDescription = "Change Light",
                        light = currentLight,
                        onClick = {
                            showLightPicker = true
                        }
                    )
                }
            }
        }

        // Top left Greeting
        Text(
            text = greeting,
            color = currentLight.text.copy(alpha = 0.70f),
            style = MakariosTypography.labelLarge.copy(fontSize = 14.sp),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 24.dp, top = 48.dp)
        )

        // One-time hint: "Swipe up for another"
        AnimatedVisibility(
            visible = !hasSwiped,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 24.dp)
        ) {
            Text(
                text = "Swipe up for another",
                color = currentLight.text.copy(alpha = 0.50f),
                style = MakariosTypography.labelSmall
            )
        }
    }

    // Detail / Contemplation Dialog
    if (selectedDetailAffirmation != null) {
        AffirmationDetailDialog(
            affirmation = selectedDetailAffirmation!!,
            light = currentLight,
            onDismiss = { selectedDetailAffirmation = null }
        )
    }

    // Light Picker Dialog
    if (showLightPicker) {
        Dialog(onDismissRequest = { showLightPicker = false }) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFFBF9F5))
                    .padding(24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Choose Light", style = MakariosTypography.displaySmall, color = Ink)
                        IconButton(onClick = { showLightPicker = false }) {
                            Icon(Icons.Outlined.Close, contentDescription = "Close", tint = Ink)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val allLights = listOf(
                        "Dawn" to Light.Dawn,
                        "Midday" to Light.Midday,
                        "Mist" to Light.Mist,
                        "Rain" to Light.Rain,
                        "Ember" to Light.Ember,
                        "Dusk" to Light.Dusk,
                        "Grove" to Light.Grove,
                        "Night" to Light.Night
                    )

                    allLights.chunked(4).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { (name, l) ->
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            currentLight = l
                                            showLightPicker = false
                                        },
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .lightBackground(l)
                                            .border(
                                                width = if (currentLight == l) 2.dp else 0.dp,
                                                color = if (currentLight == l) Ink else Color.Transparent,
                                                shape = RoundedCornerShape(12.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (currentLight == l) {
                                            Icon(Icons.Outlined.Check, contentDescription = null, tint = l.text, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(name, style = MakariosTypography.labelSmall, color = Ink)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IconRailButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    light: Light,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.15f))
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = light.text,
            modifier = Modifier.size(24.dp)
        )
    }
}
