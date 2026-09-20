package com.makarios.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.ui.theme.*

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableIntStateOf(0) }
    val selectedAreas = remember { mutableStateListOf("Peace over Anxiety", "Identity in Christ") }
    var selectedStyleIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Porcelain
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ── Top Navigation & Progress Indicator ───────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Brand Wordmark
                Text(
                    text = "MAKARIOS",
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    letterSpacing = 4.sp,
                    color = Espresso
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { index ->
                        val isCurrent = currentStep == index
                        val isDone = currentStep > index
                        Box(
                            modifier = Modifier
                                .height(4.dp)
                                .width(if (isCurrent) 28.dp else 12.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    when {
                                        isCurrent -> Terracotta
                                        isDone -> Espresso
                                        else -> Border
                                    }
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Step Content ──────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "OnboardingStepAnimation"
                ) { step ->
                    when (step) {
                        0 -> OnboardingStepWelcome()
                        1 -> OnboardingStepSeasons(
                            selectedAreas = selectedAreas,
                            onToggleArea = { area ->
                                if (selectedAreas.contains(area)) {
                                    if (selectedAreas.size > 1) selectedAreas.remove(area)
                                } else {
                                    selectedAreas.add(area)
                                }
                            }
                        )
                        2 -> OnboardingStepAesthetic(
                            selectedStyleIndex = selectedStyleIndex,
                            onSelectStyle = { selectedStyleIndex = it }
                        )
                        3 -> OnboardingStepFirstDeclaration(
                            chosenArea = selectedAreas.firstOrNull() ?: "Peace over Anxiety",
                            chosenStyle = designStyles[selectedStyleIndex]
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Bottom Action Button ──────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        if (currentStep < 3) {
                            currentStep++
                        } else {
                            onComplete()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentStep == 3) Terracotta else Espresso,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = when (currentStep) {
                                0 -> "Begin Your Practice"
                                3 -> "Enter Makarios"
                                else -> "Continue"
                            },
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.5.sp
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                if (currentStep > 0 && currentStep < 3) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Skip to Home",
                        fontFamily = BodyFontFamily,
                        fontSize = 13.sp,
                        color = StoneMuted,
                        modifier = Modifier
                            .clickable(onClick = onComplete)
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

// ── STEP 1: WELCOME ───────────────────────────────────────────────
@Composable
private fun OnboardingStepWelcome() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tagline Pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(TerracottaLight)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "CREATE · DECLARE · SHARE",
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                letterSpacing = 1.8.sp,
                color = Terracotta
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Speak truth over your life.",
            fontFamily = DisplayFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = (-0.3).sp,
            textAlign = TextAlign.Center,
            color = Espresso
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Author personal declarations rooted in God's living word. Designed for your phone screen, lock screen, and social media.",
            fontFamily = BodyFontFamily,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
            color = Stone,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Hero Card Mockup
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(22.dp), spotColor = Espresso.copy(alpha = 0.15f))
                .clip(RoundedCornerShape(22.dp))
                .background(AtmosphericGradient)
                .padding(22.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "“I am fearfully and wonderfully made; I walk in purposeful confidence.”",
                    fontFamily = DisplayFontFamily,
                    fontSize = 17.sp,
                    lineHeight = 25.sp,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.2f))
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "PSALM 139:14",
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.5.sp,
                    letterSpacing = 1.5.sp,
                    color = TerracottaLight
                )
            }
        }
    }
}

// ── STEP 2: ENCOURAGEMENT AREAS ──────────────────────────────────
@Composable
private fun OnboardingStepSeasons(
    selectedAreas: List<String>,
    onToggleArea: (String) -> Unit
) {
    val areas = listOf(
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
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "What season are you in?",
            fontFamily = DisplayFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 26.sp,
            letterSpacing = (-0.3).sp,
            color = Espresso
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Select the areas where you need God's promises close.",
            fontFamily = BodyFontFamily,
            fontSize = 13.5.sp,
            textAlign = TextAlign.Center,
            color = Stone
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            areas.chunked(2).forEach { rowAreas ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowAreas.forEach { area ->
                        val isSelected = selectedAreas.contains(area)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(if (isSelected) 2.dp else 0.dp, RoundedCornerShape(14.dp), spotColor = Espresso.copy(0.08f))
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) Espresso else Surface)
                                .border(1.dp, if (isSelected) Espresso else Border, RoundedCornerShape(14.dp))
                                .clickable { onToggleArea(area) }
                                .padding(horizontal = 12.dp, vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = area,
                                fontFamily = BodyFontFamily,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 12.5.sp,
                                textAlign = TextAlign.Center,
                                color = if (isSelected) Color.White else Espresso
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── STEP 3: AESTHETIC STYLE ──────────────────────────────────────
@Composable
private fun OnboardingStepAesthetic(
    selectedStyleIndex: Int,
    onSelectStyle: (Int) -> Unit
) {
    val styles = listOf(
        Triple("Espresso Plum", "Sacred stillness & deep warm earth", Brush.verticalGradient(listOf(Color(0xFF331E2A), Color(0xFF1F1118)))),
        Triple("Warm Porcelain", "Sunlit almond ground, crisp editorial type", Brush.verticalGradient(listOf(Color(0xFFFAF8F5), Color(0xFFEAE3D8)))),
        Triple("Terracotta Sunset", "Radiant earth warmth, bold declaration", Brush.verticalGradient(listOf(Color(0xFF8A4633), Color(0xFF4A1F2C)))),
        Triple("Eucalyptus Sage", "Restful green, peaceful contemplation", Brush.verticalGradient(listOf(Color(0xFF3A5047), Color(0xFF2A3C33))))
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Choose your aesthetic",
            fontFamily = DisplayFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 26.sp,
            letterSpacing = (-0.3).sp,
            color = Espresso
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Typography-led designs crafted for your widgets, wallpapers, and social stories.",
            fontFamily = BodyFontFamily,
            fontSize = 13.5.sp,
            textAlign = TextAlign.Center,
            color = Stone
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            styles.forEachIndexed { index, (name, desc, brush) ->
                val isSelected = selectedStyleIndex == index
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(if (isSelected) 4.dp else 1.dp, RoundedCornerShape(16.dp), spotColor = Espresso.copy(0.12f))
                        .clip(RoundedCornerShape(16.dp))
                        .background(brush)
                        .border(2.dp, if (isSelected) Terracotta else Color.Transparent, RoundedCornerShape(16.dp))
                        .clickable { onSelectStyle(index) }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = name,
                                fontFamily = DisplayFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = if (index == 1) Espresso else Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = desc,
                                fontFamily = BodyFontFamily,
                                fontSize = 11.5.sp,
                                color = if (index == 1) Stone else Color.White.copy(alpha = 0.8f)
                            )
                        }

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Terracotta),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── STEP 4: FIRST DECLARATION ────────────────────────────────────
@Composable
private fun OnboardingStepFirstDeclaration(
    chosenArea: String,
    chosenStyle: DesignStyle
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Your daily foundation",
            fontFamily = DisplayFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 26.sp,
            letterSpacing = (-0.3).sp,
            color = Espresso
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Every declaration in Makarios is rooted in God's living word.",
            fontFamily = BodyFontFamily,
            fontSize = 13.5.sp,
            textAlign = TextAlign.Center,
            color = Stone
        )

        Spacer(modifier = Modifier.height(22.dp))

        // Inaugural Declaration Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(22.dp), spotColor = Espresso.copy(0.2f))
                .clip(RoundedCornerShape(22.dp))
                .background(chosenStyle.background)
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(chosenStyle.accentColor.copy(alpha = 0.18f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = chosenArea.uppercase(),
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 9.sp,
                        letterSpacing = 1.6.sp,
                        color = chosenStyle.accentColor
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "“I do not walk in fear or anxiety. God's peace guards my heart and directs my steps.”",
                    fontFamily = DisplayFontFamily,
                    fontSize = 18.sp,
                    lineHeight = 26.sp,
                    textAlign = TextAlign.Center,
                    color = chosenStyle.textColor
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(1.dp)
                        .background(chosenStyle.textColor.copy(alpha = 0.2f))
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "“Do not be anxious about anything, but in every situation, by prayer and petition, present your requests to God.”",
                    fontFamily = DisplayFontFamily,
                    fontStyle = FontStyle.Italic,
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                    color = chosenStyle.textColor.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "PHILIPPIANS 4:6–7",
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    letterSpacing = 1.4.sp,
                    color = chosenStyle.accentColor
                )
            }
        }
    }
}
