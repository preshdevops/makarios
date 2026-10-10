package com.makarios.app.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.makarios.app.data.Affirmation
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.data.OnboardingStore
import com.makarios.app.data.OnboardingTopicTile
import com.makarios.app.ui.components.Pairing
import com.makarios.app.ui.theme.*
import com.makarios.app.util.NotificationStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * Onboarding (first launch only) with a sunrise narrative:
 * Starts in the dark and ends in the light.
 * 3 Screens + Notification Soft Ask.
 */
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(1) }
    var selectedTopics by remember { mutableStateOf<List<String>>(emptyList()) }
    var showNotificationSoftAsk by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInHorizontally { width -> width } + fadeIn(tween(350))) togetherWith
                            (slideOutHorizontally { width -> -width } + fadeOut(tween(350)))
                } else {
                    (slideInHorizontally { width -> -width } + fadeIn(tween(350))) togetherWith
                            (slideOutHorizontally { width -> width } + fadeOut(tween(350)))
                }
            },
            label = "onboarding_step_transition"
        ) { step ->
            when (step) {
                1 -> OnboardingWelcomeScreen(
                    onBegin = { currentStep = 2 }
                )
                2 -> AuthFlowHost(
                    onContinueToApp = { currentStep = 3 },
                    onBack = { currentStep = 1 }
                )
                3 -> OnboardingNeedsScreen(
                    selectedTopics = selectedTopics,
                    onSelectionChanged = { selectedTopics = it },
                    onSkip = {
                        // Skip uses all topics
                        selectedTopics = OnboardingStore.TOPIC_TILES.map { it.title }
                        currentStep = 4
                    },
                    onContinue = { currentStep = 4 }
                )
                4 -> OnboardingDeclarationScreen(
                    selectedTopics = selectedTopics,
                    onFinishOnboarding = {
                        val alreadyDenied = NotificationStore.permissionAsks(context) > 0 &&
                                (Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
                        val alreadyGranted = Build.VERSION.SDK_INT >= 33 &&
                                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

                        if (alreadyDenied || alreadyGranted) {
                            OnboardingStore.setOnboardingDone(context, true)
                            OnboardingStore.setSelectedTopics(context, selectedTopics.toSet())
                            onComplete()
                        } else {
                            showNotificationSoftAsk = true
                        }
                    }
                )
            }
        }

        if (showNotificationSoftAsk) {
            NotificationSoftAsk(
                onDismiss = {
                    OnboardingStore.setOnboardingDone(context, true)
                    OnboardingStore.setSelectedTopics(context, selectedTopics.toSet())
                    showNotificationSoftAsk = false
                    onComplete()
                }
            )
        }
    }
}

// -----------------------------------------------------------------------------
// SCREEN 1: WELCOME ("Begin")
// -----------------------------------------------------------------------------
@Composable
private fun OnboardingWelcomeScreen(
    onBegin: () -> Unit
) {
    val context = LocalContext.current
    val isReducedMotion = remember(context) {
        try {
            val resolver = context.contentResolver
            val durationScale = Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
            val transitionScale = Settings.Global.getFloat(resolver, Settings.Global.TRANSITION_ANIMATION_SCALE, 1f)
            durationScale == 0f || transitionScale == 0f
        } catch (_: Exception) {
            false
        }
    }

    var animationTriggered by remember { mutableStateOf(isReducedMotion) }
    LaunchedEffect(Unit) {
        if (!isReducedMotion) {
            delay(50)
            animationTriggered = true
        }
    }

    val sunRiseOffsetDp by animateFloatAsState(
        targetValue = if (animationTriggered) 0f else 24f,
        animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
        label = "welcome_sun_rise"
    )

    val haloAlpha by animateFloatAsState(
        targetValue = if (animationTriggered) 1f else 0f,
        animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
        label = "welcome_halo_fade"
    )

    val welcomeGradient = Brush.verticalGradient(
        colorStops = arrayOf(
            0.00f to Color(0xFF0F1716),
            0.30f to Color(0xFF1F2B2A),
            0.62f to Color(0xFF43292B),
            1.00f to Color(0xFFC9703F)
        )
    )

    SystemBarsController(Light.Night)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(welcomeGradient)
    ) {
        // Sun rising behind 3 dark ridges
        WelcomeSunriseArt(
            sunRiseOffsetDp = sunRiseOffsetDp,
            haloAlpha = haloAlpha,
            modifier = Modifier.fillMaxSize()
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Wordmark: "makarios" Newsreader italic 22 centred at top (y=80)
            Text(
                text = "makarios",
                style = TextStyle(
                    fontFamily = NewsreaderFontFamily,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Normal,
                    fontSize = 22.sp
                ),
                color = Color(0xFFFFF4E4),
                modifier = Modifier.semantics { heading() }
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Headline: "Say what God says about you." Newsreader 500 44/52, cream #FFF4E4
            Text(
                text = "Say what God says about you.",
                style = TextStyle(
                    fontFamily = NewsreaderFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 44.sp,
                    lineHeight = 52.sp,
                    fontFeatureSettings = "lnum, tnum"
                ),
                color = Color(0xFFFFF4E4),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Subtitle: (17/26, 88% cream)
            Text(
                text = "Declarations matched to scripture, in the light of your day. Beautiful enough to share.",
                style = TextStyle(
                    fontFamily = HankenGroteskFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 17.sp,
                    lineHeight = 26.sp
                ),
                color = Color(0xFFFFF4E4).copy(alpha = 0.88f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.weight(1f).defaultMinSize(minHeight = 80.dp))

            // Primary: cream pill "Begin" (56dp, ink label)
            Button(
                onClick = onBegin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .sizeIn(minHeight = 48.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFF4E4),
                    contentColor = Ink
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = "Begin",
                    style = MakariosTypography.labelLarge.copy(
                        fontFamily = HankenGroteskFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp
                    ),
                    color = Ink
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Caption under it: "Works offline. No account needed."
            Text(
                text = "Works offline. No account needed.",
                style = MakariosTypography.labelSmall.copy(
                    fontFamily = HankenGroteskFontFamily,
                    fontSize = 13.sp
                ),
                color = Color(0xFFFFF4E4).copy(alpha = 0.72f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

/**
 * Draws the rising sun ($r=46$, `#FFF1D6`, centre $x=50\%$, $y=77\%$ of height)
 * rising behind 3 dark ridges (`#2A1618`, `#1A0E10`, `#0F1716`) with warm halo.
 */
@Composable
private fun WelcomeSunriseArt(
    sunRiseOffsetDp: Float,
    haloAlpha: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val density = this

        val sunX = w * 0.50f
        val sunBaseY = h * 0.77f
        val sunY = sunBaseY + sunRiseOffsetDp * density.density

        val sunRadius = 46.dp.toPx()
        val haloRadius = 260.dp.toPx()

        // 1. Warm radial halo (#FFD9A8 85% to 0, r260)
        if (haloAlpha > 0f) {
            val haloBrush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFD9A8).copy(alpha = 0.85f * haloAlpha),
                    Color(0xFFFFD9A8).copy(alpha = 0f)
                ),
                center = Offset(sunX, sunY),
                radius = haloRadius
            )
            drawCircle(brush = haloBrush, radius = haloRadius, center = Offset(sunX, sunY))
        }

        // 2. Sun disc (#FFF1D6, r46)
        drawCircle(
            color = Color(0xFFFFF1D6),
            radius = sunRadius,
            center = Offset(sunX, sunY)
        )

        // 3. Three dark ridges rising in front of the sun
        // Ridge 1 (back): #2A1618 at y=76%
        drawWelcomeRidge(Color(0xFF2A1618), w, h, y0 = h * 0.76f, amp = h * 0.024f, seed = 17)
        // Ridge 2 (mid): #1A0E10 at y=81%
        drawWelcomeRidge(Color(0xFF1A0E10), w, h, y0 = h * 0.81f, amp = h * 0.020f, seed = 31)
        // Ridge 3 (front): #0F1716 at y=87%
        drawWelcomeRidge(Color(0xFF0F1716), w, h, y0 = h * 0.87f, amp = h * 0.016f, seed = 53)
    }
}

private fun DrawScope.drawWelcomeRidge(color: Color, w: Float, h: Float, y0: Float, amp: Float, seed: Int) {
    val path = Path().apply {
        moveTo(0f, h)
        lineTo(0f, y0)
        val steps = 6
        var prevX = 0f
        var prevY = y0
        val r = Random(seed)
        for (i in 1..steps) {
            val nextX = w * (i.toFloat() / steps)
            val nextY = y0 + amp * sin(i * 1.3f + seed) + (r.nextFloat() - 0.5f) * 0.7f * amp
            val midX = (prevX + nextX) / 2f
            val midY = (prevY + nextY) / 2f
            quadraticTo(prevX, prevY, midX, midY)
            prevX = nextX
            prevY = nextY
        }
        lineTo(w, prevY)
        lineTo(w, h)
        close()
    }
    drawPath(path, color)
}

// -----------------------------------------------------------------------------
// SCREEN 2: NEEDS ("What do you need most right now?")
// -----------------------------------------------------------------------------
@Composable
private fun OnboardingNeedsScreen(
    selectedTopics: List<String>,
    onSelectionChanged: (List<String>) -> Unit,
    onSkip: () -> Unit,
    onContinue: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    var shakingTopic by remember { mutableStateOf<String?>(null) }

    val dawnGradient = Brush.verticalGradient(
        listOf(Light.Dawn.top, Light.Dawn.bottom)
    )

    SystemBarsController(Light.Dawn)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dawnGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header Row: Progress Dots and Skip button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OnboardingProgressDots(step = 2)

                TextButton(
                    onClick = onSkip,
                    modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Skip",
                        color = Ink,
                        style = MakariosTypography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Heading Newsreader 34/42 balanced
            Text(
                text = "What do you need most right now?",
                style = TextStyle(
                    fontFamily = NewsreaderFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 34.sp,
                    lineHeight = 42.sp,
                    fontFeatureSettings = "lnum, tnum"
                ),
                color = Ink
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sub: "Pick up to three. We will start there."
            Text(
                text = "Pick up to three. We will start there.",
                style = MakariosTypography.bodyLarge.copy(
                    fontFamily = HankenGroteskFontFamily,
                    fontSize = 16.sp
                ),
                color = Ink.copy(alpha = 0.78f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2x3 Grid of Light Tiles
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OnboardingStore.TOPIC_TILES.chunked(2).forEach { rowTiles ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowTiles.forEach { tile ->
                            val isSelected = tile.title in selectedTopics
                            val isShaking = shakingTopic == tile.title

                            TopicLightTile(
                                tile = tile,
                                isSelected = isSelected,
                                isShaking = isShaking,
                                onToggle = {
                                    if (isSelected) {
                                        onSelectionChanged(selectedTopics - tile.title)
                                    } else {
                                        if (selectedTopics.size < 3) {
                                            onSelectionChanged(selectedTopics + tile.title)
                                        } else {
                                            // 4th tap shakes oldest selection and triggers subtle haptic
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            val oldest = selectedTopics.firstOrNull() ?: tile.title
                                            coroutineScope.launch {
                                                shakingTopic = oldest
                                                delay(400)
                                                shakingTopic = null
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(136.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer count: "2 chosen"
            Text(
                text = "${selectedTopics.size} chosen",
                style = MakariosTypography.labelMedium.copy(
                    fontFamily = HankenGroteskFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                ),
                color = Ink.copy(alpha = 0.72f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Primary ink pill "Continue" (enabled with >= 1)
            Button(
                onClick = onContinue,
                enabled = selectedTopics.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .sizeIn(minHeight = 48.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Ink,
                    contentColor = Cream,
                    disabledContainerColor = Ink.copy(alpha = 0.25f),
                    disabledContentColor = Cream.copy(alpha = 0.5f)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = "Continue",
                    style = MakariosTypography.labelLarge.copy(
                        fontFamily = HankenGroteskFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * 163x136 tile drawing its Light scene with the engine,
 * title Newsreader 22, promise line 12sp, selected = 2dp ink outline with 3dp offset + 26dp check badge.
 */
@Composable
private fun TopicLightTile(
    tile: OnboardingTopicTile,
    isSelected: Boolean,
    isShaking: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shakeOffset by animateFloatAsState(
        targetValue = if (isShaking) 6f else 0f,
        animationSpec = if (isShaking) {
            repeatable(
                iterations = 4,
                animation = tween(durationMillis = 60, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        },
        label = "tile_shake"
    )

    Box(
        modifier = modifier
            .offset(x = shakeOffset.dp)
            .then(
                if (isSelected) {
                    Modifier
                        .border(2.dp, Ink, RoundedCornerShape(23.dp))
                        .padding(3.dp)
                } else {
                    Modifier.padding(3.dp)
                }
            )
            .clip(RoundedCornerShape(20.dp))
            .drawBehind {
                drawLight(
                    light = tile.light,
                    size = size,
                    horizon = HorizonSpec(0.70f, 0.78f, 0.86f),
                    discScale = 0.60f,
                    grain = false
                )
            }
            .clickable(onClick = onToggle)
            .semantics {
                role = Role.Checkbox
                selected = isSelected
                stateDescription = if (isSelected) "${tile.title}, selected" else "${tile.title}, not selected"
            }
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = tile.title,
                    style = TextStyle(
                        fontFamily = NewsreaderFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 22.sp
                    ),
                    color = tile.light.text
                )

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(tile.light.text, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Selected",
                            tint = tile.light.buttonFill,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Text(
                text = tile.promise,
                style = TextStyle(
                    fontFamily = HankenGroteskFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                ),
                color = tile.light.text.copy(alpha = 0.85f),
                maxLines = 2
            )
        }
    }
}

// -----------------------------------------------------------------------------
// SCREEN 3: FIRST DECLARATION
// -----------------------------------------------------------------------------
@Composable
private fun OnboardingDeclarationScreen(
    selectedTopics: List<String>,
    onFinishOnboarding: () -> Unit
) {
    val currentLight = remember { Light.forNow() }
    val bankDeclarations = remember(selectedTopics) {
        OnboardingStore.getBankForTopics(selectedTopics)
    }

    var currentIndex by remember { mutableStateOf(0) }
    val currentAffirmation = remember(currentIndex, bankDeclarations) {
        bankDeclarations.getOrElse(currentIndex % bankDeclarations.size) {
            AffirmationRepository.affirmationOfTheDay
        }
    }

    var isKeptAnimTriggered by remember { mutableStateOf(false) }
    val bookmarkScale by animateFloatAsState(
        targetValue = if (isKeptAnimTriggered) 1.25f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "bookmark_pop"
    )

    SystemBarsController(currentLight)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawLight(currentLight, size = size, grain = true)
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 3 Progress dots (last active)
            OnboardingProgressDots(
                step = 3,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Label "YOUR FIRST DECLARATION" (13sp caps, 10% tracking)
            Text(
                text = "YOUR FIRST DECLARATION",
                style = TextStyle(
                    fontFamily = HankenGroteskFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    letterSpacing = 0.10.em
                ),
                color = currentLight.text.copy(alpha = 0.78f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Pairing with 250ms crossfade on "Show me another"
            AnimatedContent(
                targetState = currentAffirmation,
                transitionSpec = {
                    fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(250))
                },
                label = "pairing_crossfade"
            ) { affirmation ->
                Pairing(
                    declaration = affirmation.declaration,
                    verseText = affirmation.scriptureText,
                    verseReference = affirmation.reference,
                    light = currentLight,
                    isCompact = false
                )
            }

            Spacer(modifier = Modifier.weight(1f).defaultMinSize(minHeight = 40.dp))

            // Primary: ink pill "Keep it" with bookmark icon
            Button(
                onClick = {
                    AffirmationRepository.save(currentAffirmation.id)
                    isKeptAnimTriggered = true
                    onFinishOnboarding()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .sizeIn(minHeight = 48.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = currentLight.buttonFill,
                    contentColor = currentLight.buttonText
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isKeptAnimTriggered) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = currentLight.buttonText,
                        modifier = Modifier
                            .size(20.dp)
                            .scale(bookmarkScale)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Keep it",
                        style = MakariosTypography.labelLarge.copy(
                            fontFamily = HankenGroteskFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp
                        ),
                        color = currentLight.buttonText
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Text button: "Show me another" (cycles bank items with 250ms crossfade)
            TextButton(
                onClick = {
                    if (bankDeclarations.isNotEmpty()) {
                        currentIndex = (currentIndex + 1) % bankDeclarations.size
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .sizeIn(minHeight = 48.dp)
            ) {
                Text(
                    text = "Show me another",
                    style = MakariosTypography.bodyMedium.copy(
                        fontFamily = HankenGroteskFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    ),
                    color = currentLight.text
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

// -----------------------------------------------------------------------------
// PROGRESS DOTS (Step 1, 2, 3)
// -----------------------------------------------------------------------------
@Composable
private fun OnboardingProgressDots(
    step: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.semantics {
            contentDescription = "Step $step of 3"
        },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        (1..3).forEach { i ->
            if (i == step) {
                Box(
                    modifier = Modifier
                        .size(width = 20.dp, height = 8.dp)
                        .background(Ink, RoundedCornerShape(4.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Ink.copy(alpha = 0.25f), CircleShape)
                )
            }
        }
    }
}
