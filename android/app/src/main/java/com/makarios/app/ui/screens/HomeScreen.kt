package com.makarios.app.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.makarios.app.data.Affirmation
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.ui.components.SocialShareSheet
import com.makarios.app.ui.theme.*
import com.makarios.app.util.ReminderManager
import com.makarios.app.util.ShareHelper
import com.makarios.app.util.WidgetHelper

@Composable
fun HomeScreen(
    onNavigateToDetail: (Affirmation) -> Unit,
    onNavigateToCreate: (String) -> Unit,
    onNavigateToLibrary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val aotd = remember { AffirmationRepository.affirmationOfTheDay ?: AffirmationRepository.getAll().first() }
    var isAotdSaved by remember(aotd.id) { mutableStateOf(AffirmationRepository.isSaved(aotd.id)) }
    var shareAffirmationTarget by remember { mutableStateOf<Affirmation?>(null) }

    // Time-aware greeting — only the greeting word, kept brief
    val currentHour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
    val greeting = remember(currentHour) {
        when (currentHour) {
            in 4..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else     -> "Good evening"
        }
    }

    // Entrance animation — content fades + slides in once on first composition
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Porcelain
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {

            // ── 1. Minimal Header ──────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(380)) + slideInVertically(tween(380, easing = { it })) { -12 }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 20.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Makarios",
                            fontFamily = DisplayFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 22.sp,
                            letterSpacing = (-0.3).sp,
                            color = Espresso
                        )
                        Text(
                            text = greeting,
                            fontFamily = DisplayFontFamily,
                            fontStyle = FontStyle.Italic,
                            fontSize = 13.sp,
                            color = Stone
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── 2. Today's Truth — Full Sanctuary Stage ────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500, delayMillis = 80))
                        + slideInVertically(tween(500, delayMillis = 80, easing = { it })) { 24 }
            ) {
                SanctuaryHero(
                    affirmation = aotd,
                    isSaved = isAotdSaved,
                    onToggleSave = {
                        AffirmationRepository.toggleSave(aotd.id)
                        isAotdSaved = !isAotdSaved
                    },
                    onShare = { shareAffirmationTarget = aotd },
                    onTap = { onNavigateToDetail(aotd) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── 3. One Quiet Prompt — the only Create entry point ──
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500, delayMillis = 180))
                        + slideInVertically(tween(500, delayMillis = 180, easing = { it })) { 20 }
            ) {
                AuthorPrompt(onClick = { onNavigateToCreate("") })
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── 4. Quiet Library link — no feed duplication ────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400, delayMillis = 260))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Browse all declarations →",
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = Olive,
                        modifier = Modifier
                            .clickable(onClick = onNavigateToLibrary)
                            .padding(vertical = 8.dp)
                    )
                }
            }
        }
    }

    shareAffirmationTarget?.let { aff ->
        SocialShareSheet(
            affirmation = aff,
            initialStyleIndex = 0,
            photoUrl = aff.imageUrl.takeIf { it.isNotBlank() && !it.startsWith("drawable:") },
            onDismiss = { shareAffirmationTarget = null }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sanctuary Hero — the entire visual stage
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SanctuaryHero(
    affirmation: Affirmation,
    isSaved: Boolean,
    onToggleSave: () -> Unit,
    onShare: () -> Unit,
    onTap: () -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = Espresso.copy(alpha = 0.22f),
                ambientColor = Espresso.copy(alpha = 0.06f)
            )
            .clip(RoundedCornerShape(28.dp))
            .clickable(onClick = onTap)
    ) {
        // Full-bleed photography
        AsyncImage(
            model = affirmation.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            colorFilter = WarmPhotoGrade,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 420.dp)
        )

        // Scrim
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(PhotoTextScrim)
        )

        Column(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = 22.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top row — category chip + high-contrast "Today's declaration" pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ScrimPill {
                    Text(
                        text = affirmation.category.uppercase(),
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 9.sp,
                        letterSpacing = 1.6.sp,
                        color = Color.White
                    )
                }

                ScrimPill {
                    Text(
                        text = "Today's declaration",
                        fontFamily = DisplayFontFamily,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.5.sp,
                        color = Color.White
                    )
                }
            }

            // Declaration + scripture + scripture reference + action buttons
            Column {
                Text(
                    text = "“${affirmation.declaration}”",
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 26.sp,
                    lineHeight = 35.sp,
                    letterSpacing = (-0.3).sp,
                    color = Color.White,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.60f),
                            offset = Offset(0f, 2f),
                            blurRadius = 8f
                        )
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "“${affirmation.scriptureText}”",
                    fontFamily = DisplayFontFamily,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.5.sp,
                    lineHeight = 21.sp,
                    color = Color.White.copy(alpha = 0.90f),
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.45f),
                            offset = Offset(0f, 1f),
                            blurRadius = 6f
                        )
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Scripture reference moved to its own line above the action row (no collision)
                Text(
                    text = affirmation.reference.uppercase(),
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.5.sp,
                    letterSpacing = 1.6.sp,
                    color = Color.White.copy(alpha = 0.75f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action icons in a single evenly spaced row aligned to card's inner padding
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SpringIconButton(
                        onClick = { WidgetHelper.setWidgetAffirmation(context, affirmation) },
                        contentDescription = "Set as Widget"
                    ) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    SpringIconButton(
                        onClick = {
                            ReminderManager.setPinnedAffirmation(context, affirmation)
                            Toast.makeText(context, "Set as daily notification ✓", Toast.LENGTH_SHORT).show()
                        },
                        contentDescription = "Set as Notification"
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    SpringIconButton(
                        onClick = onShare,
                        contentDescription = "Share"
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    SpringIconButton(
                        onClick = onToggleSave,
                        contentDescription = if (isSaved) "Unsave" else "Save",
                        filled = isSaved
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// Pressable icon button with a satisfying spring scale micro-animation (40px circular)
@Composable
private fun SpringIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    filled: Boolean = false,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.82f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "icon-spring"
    )

    Box(
        modifier = Modifier
            .size(40.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                if (filled) Olive
                else ScrimPillColor
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Author Prompt — replaces FAB + CreatorPromptCard, a single quiet invitation
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AuthorPrompt(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.975f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "prompt-spring"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .scale(scale)
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = Espresso.copy(alpha = 0.04f)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(Surface)
            .border(0.5.dp, BorderSubtle, RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "\u201cWhat does your heart need to declare today?\u201d",
                    fontFamily = DisplayFontFamily,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Normal,
                    fontSize = 17.sp,
                    lineHeight = 24.sp,
                    letterSpacing = (-0.2).sp,
                    color = Espresso
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Write a declaration · Grounded in scripture",
                    fontFamily = BodyFontFamily,
                    fontSize = 12.sp,
                    color = Stone
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Espresso),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 20.sp,
                    color = Surface
                )
            }
        }
    }
}
