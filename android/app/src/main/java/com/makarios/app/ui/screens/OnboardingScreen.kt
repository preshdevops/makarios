package com.makarios.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.makarios.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import android.widget.Toast
import com.makarios.app.data.AuthManager
import com.makarios.app.ui.components.AuthDialog

// ─────────────────────────────────────────────────────────────────────────────
// Onboarding — research-backed, 4-step flow
//
// Principles applied:
//   • Value-first: step 0 shows the actual experience (declaration + verse card)
//     before asking anything of the user — "aha moment" within 5 seconds
//   • Progressive discovery: Welcome → Season → Ready Preview → Sacred Account / Guest
//   • 1-tap Guest Mode: Zero friction if user wants to dive in immediately
//   • Progressive disclosure: notification / permissions deferred to app
//   • HorizontalPager with parallax photography for visual richness
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()
    var selectedArea by remember { mutableStateOf("Peace over Anxiety") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Espresso)
    ) {

        // ── Full-bleed background imagery (parallax per page) ──────────────
        OnboardingBackgroundLayer(pagerState)

        // ── Content layer with safe insets ─────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {

            // Top Bar: Brand wordmark & step indicators
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "MAKARIOS",
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.5.sp,
                    letterSpacing = 4.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Page indicators — strictly safe positive weights
                StepIndicators(
                    currentPage = pagerState.currentPage,
                    total = 4
                )
            }

            // ── Pager content ──────────────────────────────────────────────
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = true
            ) { page ->
                val pageOffset = ((pagerState.currentPage - page) +
                        pagerState.currentPageOffsetFraction).coerceIn(-1f, 1f)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            alpha = (1f - pageOffset.absoluteValue * 0.6f).coerceIn(0f, 1f)
                        }
                ) {
                    when (page) {
                        0 -> PageWelcome()
                        1 -> PageSeasonPicker(
                            selected = selectedArea,
                            onSelect = { selectedArea = it }
                        )
                        2 -> PageReady(chosenArea = selectedArea)
                        3 -> PageAuth(onComplete = onComplete)
                    }
                }
            }

            // ── CTA Button + skip (for pages 0-2; page 3 has in-page actions) ──
            if (pagerState.currentPage < 3) {
                OnboardingCTA(
                    page = pagerState.currentPage,
                    onContinue = {
                        scope.launch {
                            pagerState.animateScrollToPage(
                                page = pagerState.currentPage + 1,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                        }
                    },
                    onSkip = {
                        AuthManager.signInAnonymously(
                            onSuccess = onComplete,
                            onError = { onComplete() }
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Background layer: parallax photography + deepening scrim
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun OnboardingBackgroundLayer(pagerState: PagerState) {
    val photos = listOf(
        "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?auto=format&fit=crop&w=900&q=80", // mountain dawn
        "https://images.unsplash.com/photo-1501854140801-50d01698950b?auto=format&fit=crop&w=900&q=80", // valley mist
        "https://images.unsplash.com/photo-1491466424936-e304919aada7?auto=format&fit=crop&w=900&q=80", // golden horizon
        "https://images.unsplash.com/photo-1519681393784-d120267933ba?auto=format&fit=crop&w=900&q=80"  // starry sanctuary
    )

    Box(modifier = Modifier.fillMaxSize()) {
        photos.forEachIndexed { index, url ->
            val pageOffset = (pagerState.currentPage - index) +
                    pagerState.currentPageOffsetFraction
            val alpha = (1f - pageOffset.absoluteValue.coerceIn(0f, 1f))
                .coerceIn(0f, 1f)

            if (alpha > 0.01f) {
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(alpha)
                        .graphicsLayer {
                            // Subtle parallax: image shifts at 35% of scroll speed
                            translationX = -pageOffset.coerceIn(-1.5f, 1.5f) * size.width * 0.35f
                        }
                )
            }
        }

        // Atmospheric scrim — espresso at bottom, breathable at top
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.28f),
                        0.40f to Color.Black.copy(alpha = 0.42f),
                        0.72f to Color(0xCC1B1511),
                        1f to Color(0xF21C1612)
                    )
                )
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Step indicators — guaranteed safe, positive weight scaling
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun StepIndicators(
    currentPage: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { index ->
            val isActive = index == currentPage
            val isDone = index < currentPage

            val weight by animateFloatAsState(
                targetValue = if (isActive) 2.2f else 1.0f,
                animationSpec = spring(
                    stiffness = Spring.StiffnessMedium,
                    dampingRatio = Spring.DampingRatioNoBouncy
                ),
                label = "indicator-weight"
            )
            val alphaVal by animateFloatAsState(
                targetValue = if (isActive) 1f else if (isDone) 0.65f else 0.30f,
                animationSpec = tween(200),
                label = "indicator-alpha"
            )

            Box(
                modifier = Modifier
                    .weight(weight)
                    .height(3.dp)
                    .alpha(alphaVal)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PAGE 0 — Welcome / Value-first
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PageWelcome() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = "Speak truth\nover your life.",
            fontFamily = DisplayFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 36.sp,
            lineHeight = 46.sp,
            letterSpacing = (-0.5).sp,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Write personal declarations rooted in God's living word — then carry them as widgets, wallpapers, and shareable graphics.",
            fontFamily = BodyFontFamily,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            color = Color.White.copy(alpha = 0.80f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Product sample card
        DeclarationPreviewCard()

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun DeclarationPreviewCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float-y"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { translationY = offsetY }
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFAF7F2),
                        Color(0xFFF3EDE4)
                    )
                )
            )
            .padding(22.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(TerracottaLight)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "IDENTITY",
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 9.sp,
                    letterSpacing = 1.6.sp,
                    color = Terracotta
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "“I am fearfully and wonderfully made. I walk in purposeful confidence.”",
                fontFamily = DisplayFontFamily,
                fontSize = 17.5.sp,
                lineHeight = 25.sp,
                textAlign = TextAlign.Center,
                color = Espresso
            )

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.35f)
                    .height(1.dp)
                    .background(Border)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "“I praise you because I am fearfully and wonderfully made; your works are wonderful, I know that full well.”",
                fontFamily = DisplayFontFamily,
                fontStyle = FontStyle.Italic,
                fontSize = 12.5.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center,
                color = EspressoLight.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "PSALM 139:14",
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                letterSpacing = 1.5.sp,
                color = Terracotta
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PAGE 1 — Season picker
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PageSeasonPicker(
    selected: String,
    onSelect: (String) -> Unit
) {
    val seasons = listOf(
        "Peace over Anxiety",
        "Identity in Christ",
        "Confidence & Calling",
        "Strength & Endurance",
        "Divine Provision",
        "Rest & Renewal",
        "Joy & Freedom",
        "Relationships & Grace"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = "What season\nare you in?",
            fontFamily = DisplayFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 36.sp,
            lineHeight = 46.sp,
            letterSpacing = (-0.5).sp,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Makarios surfaces declarations for your specific moment.",
            fontFamily = BodyFontFamily,
            fontSize = 14.sp,
            lineHeight = 21.sp,
            color = Color.White.copy(alpha = 0.75f)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Chip grid
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            seasons.chunked(2).forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    row.forEach { season ->
                        val isSelected = selected == season
                        val interactionSource = remember { MutableInteractionSource() }

                        val bgColor by animateColorAsState(
                            targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.12f),
                            animationSpec = tween(200),
                            label = "chip-bg"
                        )
                        val textColor by animateColorAsState(
                            targetValue = if (isSelected) Espresso else Color.White,
                            animationSpec = tween(200),
                            label = "chip-text"
                        )
                        val chipScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.02f else 1f,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = "chip-scale"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .scale(chipScale)
                                .clip(RoundedCornerShape(14.dp))
                                .background(bgColor)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) { onSelect(season) }
                                .padding(horizontal = 12.dp, vertical = 13.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = season,
                                    fontFamily = BodyFontFamily,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    color = textColor
                                )
                                if (isSelected) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Terracotta,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PAGE 2 — Ready / personalised first declaration
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PageReady(chosenArea: String) {
    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(chosenArea) {
        revealed = false
        delay(150)
        revealed = true
    }

    val textAlpha by animateFloatAsState(
        targetValue = if (revealed) 1f else 0f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "text-reveal"
    )
    val textOffset by animateFloatAsState(
        targetValue = if (revealed) 0f else 14f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "text-offset"
    )

    val (declaration, verse, reference) = remember(chosenArea) {
        when {
            chosenArea.contains("Peace", ignoreCase = true) -> Triple(
                "I do not walk in fear or anxiety. God's peace guards my heart.",
                "Do not be anxious about anything, but in every situation, by prayer and petition, present your requests to God.",
                "PHILIPPIANS 4:6"
            )
            chosenArea.contains("Identity", ignoreCase = true) -> Triple(
                "I am chosen, holy, and dearly loved. My identity is anchored in Christ.",
                "Therefore, if anyone is in Christ, the new creation has come: the old has gone, the new is here.",
                "2 CORINTHIANS 5:17"
            )
            chosenArea.contains("Confidence", ignoreCase = true) || chosenArea.contains("Calling", ignoreCase = true) -> Triple(
                "I am called with a holy purpose. I walk boldly in the path God has set.",
                "For we are God's handiwork, created in Christ Jesus to do good works.",
                "EPHESIANS 2:10"
            )
            chosenArea.contains("Strength", ignoreCase = true) -> Triple(
                "When my strength is spent, His power is made perfect. I will not give up.",
                "I can do all things through Christ who gives me strength.",
                "PHILIPPIANS 4:13"
            )
            chosenArea.contains("Provision", ignoreCase = true) -> Triple(
                "My God supplies every need. I rest in His faithful, generous provision.",
                "And my God will meet all your needs according to the riches of his glory in Christ Jesus.",
                "PHILIPPIANS 4:19"
            )
            chosenArea.contains("Rest", ignoreCase = true) || chosenArea.contains("Renewal", ignoreCase = true) -> Triple(
                "I come to Christ and find true rest. My soul is renewed in His presence.",
                "Come to me, all you who are weary and burdened, and I will give you rest.",
                "MATTHEW 11:28"
            )
            chosenArea.contains("Joy", ignoreCase = true) -> Triple(
                "I am free and full of gladness. His joy is my strength today.",
                "Do not grieve, for the joy of the LORD is your strength.",
                "NEHEMIAH 8:10"
            )
            else -> Triple(
                "I walk in grace and peace. Every relationship I carry is covered by God's love.",
                "Above all, clothe yourselves with love, which binds everything together in perfect harmony.",
                "COLOSSIANS 3:14"
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = "Your first\ndeclaration.",
            fontFamily = DisplayFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 36.sp,
            lineHeight = 46.sp,
            letterSpacing = (-0.5).sp,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Every word in Makarios is grounded in scripture — never a sentiment, always a promise.",
            fontFamily = BodyFontFamily,
            fontSize = 14.sp,
            lineHeight = 21.sp,
            color = Color.White.copy(alpha = 0.75f)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Personalised declaration card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = textAlpha
                    translationY = textOffset
                }
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFFAF7F2), Color(0xFFF3EDE4))
                    )
                )
                .padding(22.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(TerracottaLight)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = chosenArea.uppercase(),
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 9.sp,
                        letterSpacing = 1.6.sp,
                        color = Terracotta
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "“$declaration”",
                    fontFamily = DisplayFontFamily,
                    fontSize = 17.sp,
                    lineHeight = 25.sp,
                    textAlign = TextAlign.Center,
                    color = Espresso
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.35f)
                        .height(1.dp)
                        .background(Border)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "“$verse”",
                    fontFamily = DisplayFontFamily,
                    fontStyle = FontStyle.Italic,
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                    color = EspressoLight.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = reference,
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    letterSpacing = 1.5.sp,
                    color = Terracotta
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PAGE 3 — Sacred Account & Guest Access
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PageAuth(onComplete: () -> Unit) {
    val context = LocalContext.current
    var isSignUp by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun submit() {
        if (isLoading) return
        errorMessage = null

        if (email.isBlank()) {
            errorMessage = "Please enter an email address"
            return
        }
        if (password.length < 6) {
            errorMessage = "Password must be at least 6 characters"
            return
        }

        isLoading = true
        if (isSignUp) {
            AuthManager.signUpWithEmail(
                email = email,
                password = password,
                onSuccess = {
                    isLoading = false
                    Toast.makeText(context, "Welcome to Makarios ✓", Toast.LENGTH_SHORT).show()
                    onComplete()
                },
                onError = { error ->
                    isLoading = false
                    errorMessage = error
                }
            )
        } else {
            AuthManager.signInWithEmail(
                email = email,
                password = password,
                onSuccess = {
                    isLoading = false
                    Toast.makeText(context, "Welcome back ✓", Toast.LENGTH_SHORT).show()
                    onComplete()
                },
                onError = { error ->
                    isLoading = false
                    errorMessage = error
                }
            )
        }
    }

    fun continueAsGuest() {
        if (isLoading) return
        isLoading = true
        AuthManager.signInAnonymously(
            onSuccess = {
                isLoading = false
                onComplete()
            },
            onError = {
                isLoading = false
                onComplete()
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isSignUp) "Preserve Your\nSacred Truths." else "Welcome Back\nto Makarios.",
            fontFamily = DisplayFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = (-0.5).sp,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Back up authored declarations across all your devices, or enter freely as a guest.",
            fontFamily = BodyFontFamily,
            fontSize = 13.5.sp,
            lineHeight = 20.sp,
            color = Color.White.copy(alpha = 0.75f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Sacred card containing the auth inputs
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFFAF7F2))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Tab switch: Create vs Sign In
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(PorcelainWarm)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSignUp) Espresso else Color.Transparent)
                            .clickable {
                                isSignUp = true
                                errorMessage = null
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Create Account",
                            fontFamily = BodyFontFamily,
                            fontWeight = if (isSignUp) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 12.5.sp,
                            color = if (isSignUp) Color.White else Espresso
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (!isSignUp) Espresso else Color.Transparent)
                            .clickable {
                                isSignUp = false
                                errorMessage = null
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sign In",
                            fontFamily = BodyFontFamily,
                            fontWeight = if (!isSignUp) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 12.5.sp,
                            color = if (!isSignUp) Color.White else Espresso
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFDE8E4))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = errorMessage!!,
                            fontFamily = BodyFontFamily,
                            fontSize = 11.5.sp,
                            lineHeight = 15.sp,
                            color = Color(0xFF922B21)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = null
                    },
                    label = { Text("Email", fontFamily = BodyFontFamily, fontSize = 12.5.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Surface,
                        unfocusedContainerColor = Surface,
                        focusedBorderColor = Terracotta,
                        unfocusedBorderColor = Border,
                        focusedLabelColor = Terracotta,
                        cursorColor = Terracotta
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    label = { Text("Password (6+ chars)", fontFamily = BodyFontFamily, fontSize = 12.5.sp) },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = StoneMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Surface,
                        unfocusedContainerColor = Surface,
                        focusedBorderColor = Terracotta,
                        unfocusedBorderColor = Border,
                        focusedLabelColor = Terracotta,
                        cursorColor = Terracotta
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { submit() },
                    enabled = !isLoading,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Terracotta,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (isSignUp) "Create Free Account" else "Sign In to Makarios",
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.5.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dignified Guest Skip Button
        Row(
            modifier = Modifier
                .clickable { continueAsGuest() }
                .padding(vertical = 8.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Skip for now · Continue as Guest",
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.5.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
            Text(
                text = "→",
                fontFamily = BodyFontFamily,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CTA + Skip
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun OnboardingCTA(
    page: Int,
    onContinue: () -> Unit,
    onSkip: () -> Unit
) {
    val isLast = page == 2

    val ctaLabel = when (page) {
        0    -> "Continue"
        1    -> "Continue"
        else -> "Next: Save Your Sanctuary →"
    }

    val ctaBg by animateColorAsState(
        targetValue = if (isLast) Terracotta else Color.White,
        animationSpec = tween(250),
        label = "cta-bg"
    )
    val ctaText by animateColorAsState(
        targetValue = if (isLast) Color.White else Espresso,
        animationSpec = tween(250),
        label = "cta-text"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onContinue,
            colors = ButtonDefaults.buttonColors(
                containerColor = ctaBg,
                contentColor = ctaText
            ),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = ctaLabel,
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.5.sp
            )
        }

        if (page < 2) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Skip for now",
                fontFamily = BodyFontFamily,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.55f),
                modifier = Modifier
                    .clickable(onClick = onSkip)
                    .padding(vertical = 6.dp)
            )
        }
    }
}
