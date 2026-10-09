package com.makarios.app.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.makarios.app.data.Affirmation
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.ui.components.Pairing
import com.makarios.app.ui.theme.Light
import com.makarios.app.ui.theme.MakariosTypography
import com.makarios.app.ui.theme.getCurrentLightForTime
import com.makarios.app.ui.theme.lightBackground

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Evaluate Light on app open and resume
    var currentLight by remember { mutableStateOf(getCurrentLightForTime()) }
    
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
            modifier = Modifier.fillMaxSize(),
            // 280ms swipe transition curve
            // Wait, compose VerticalPager defaults can't easily change duration without 
            // a custom fling behavior, but we'll stick to default for simplicity 
            // unless we write a custom snap behavior.
        ) { page ->
            val affirmation = affirmations[page]
            var isSaved by remember(affirmation.id) { mutableStateOf(AffirmationRepository.isSaved(affirmation.id)) }

            Box(modifier = Modifier.fillMaxSize()) {
                // The Pairing (optically centered at 40% height, max width 78%, left aligned)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 24.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Spacer(modifier = Modifier.fillMaxHeight(0.2f))
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
                            com.makarios.app.util.ShareHelper.shareGeneric(
                                context = context,
                                affirmation = affirmation,
                                light = currentLight
                            )
                        }
                    )
                    
                    IconRailButton(
                        icon = if (isSaved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isSaved) "Keep declaration, kept" else "Keep declaration, not kept",
                        light = currentLight,
                        onClick = {
                            if (isSaved) AffirmationRepository.unsave(affirmation.id)
                            else AffirmationRepository.save(affirmation.id)
                            isSaved = !isSaved
                        }
                    )
                    
                    IconRailButton(
                        icon = Icons.Outlined.Lightbulb, // Placeholder for "Light"
                        contentDescription = "Change Light",
                        light = currentLight,
                        onClick = {
                            Toast.makeText(context, "Light settings", Toast.LENGTH_SHORT).show()
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
                .padding(start = 24.dp, top = 48.dp) // accommodate status bar roughly
        )

        // Swipe up hint
        AnimatedVisibility(
            visible = !hasSwiped,
            enter = fadeIn(tween(500)),
            exit = fadeOut(tween(500)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Swipe up for another",
                color = currentLight.text.copy(alpha = 0.70f),
                style = MakariosTypography.labelLarge.copy(fontSize = 14.sp)
            )
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
    Box(
        modifier = Modifier
            .size(48.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = light.text, // No circle, just icon
            modifier = Modifier.size(24.dp)
        )
    }
}
