package com.makarios.app.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.data.Affirmation
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.ui.theme.*
import com.makarios.app.util.WidgetHelper

enum class WidgetSurface {
    HOME_SCREEN,
    LOCK_SCREEN
}

enum class WidgetSize(val label: String, val dimensions: String) {
    SMALL("Small", "2×2"),
    MEDIUM("Medium", "4×2"),
    LARGE("Large", "4×4")
}

@Composable
fun ExploreScreen(
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    WidgetStudioScreen(onBack = onBack, modifier = modifier)
}

@Composable
fun WidgetStudioScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedSurface by remember { mutableStateOf(WidgetSurface.HOME_SCREEN) }
    var selectedSize by remember { mutableStateOf(WidgetSize.MEDIUM) }
    var selectedSource by remember { mutableStateOf("Declaration of the Day") }
    var selectedCategory by remember { mutableStateOf("Peace") }
    var selectedSchedule by remember { mutableStateOf("Every Dawn (06:30 AM)") }

    // Active affirmation powering the widget preview
    val activeAffirmation = remember(selectedSource, selectedCategory) {
        when (selectedSource) {
            "Declaration of the Day" -> AffirmationRepository.affirmationOfTheDay
            "My Saved Declarations" -> AffirmationRepository.getSaved().firstOrNull() ?: AffirmationRepository.affirmationOfTheDay
            else -> AffirmationRepository.getAll().firstOrNull { it.category.equals(selectedCategory, ignoreCase = true) }
                ?: AffirmationRepository.affirmationOfTheDay
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Porcelain
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 36.dp)
        ) {
            // ── Top Bar ───────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Surface)
                        .border(1.dp, Border, CircleShape)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Espresso,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = "Widget Studio",
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = Espresso
                )

                Box(modifier = Modifier.size(38.dp)) // Spacer to balance
            }

            // ── Screen Header ─────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "Everyday Presence",
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp,
                    lineHeight = 32.sp,
                    letterSpacing = (-0.3).sp,
                    color = Espresso
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Keep biblical declarations and grounding scripture woven into your daily phone glance.",
                    fontFamily = BodyFontFamily,
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp,
                    color = Stone
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ── Surface Toggle: Home Screen vs Lock Screen ────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(PorcelainWarm.copy(alpha = 0.5f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                WidgetSurface.values().forEach { surface ->
                    val isSelected = selectedSurface == surface
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .background(Espresso)
                                        .shadow(2.dp, RoundedCornerShape(16.dp))
                                } else {
                                    Modifier.background(Color.Transparent)
                                }
                            )
                            .clickable { selectedSurface = surface }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (surface == WidgetSurface.HOME_SCREEN) Icons.Default.Smartphone else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else Espresso,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = if (surface == WidgetSurface.HOME_SCREEN) "Home Screen" else "Lock Screen",
                                fontFamily = BodyFontFamily,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 13.sp,
                                color = if (isSelected) Color.White else Espresso
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Size Selector (Only on Home Screen) ───────────────
            if (selectedSurface == WidgetSurface.HOME_SCREEN) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WidgetSize.values().forEach { size ->
                        val isSelected = selectedSize == size
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .then(
                                    if (isSelected) {
                                        Modifier
                                            .background(Terracotta)
                                            .shadow(2.dp, RoundedCornerShape(14.dp))
                                    } else {
                                        Modifier
                                            .background(Surface)
                                            .border(1.dp, Border, RoundedCornerShape(14.dp))
                                    }
                                )
                                .clickable { selectedSize = size }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = size.label,
                                    fontFamily = BodyFontFamily,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    fontSize = 12.5.sp,
                                    color = if (isSelected) Color.White else Espresso
                                )
                                Text(
                                    text = size.dimensions,
                                    fontFamily = BodyFontFamily,
                                    fontSize = 10.sp,
                                    color = if (isSelected) Color.White.copy(alpha = 0.8f) else StoneMuted
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
            }

            // ── Interactive Simulated Phone Preview Canvas ────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                // Simulated phone device frame
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(26.dp),
                            spotColor = Espresso.copy(alpha = 0.15f)
                        )
                        .clip(RoundedCornerShape(26.dp))
                        .then(
                            if (selectedSurface == WidgetSurface.LOCK_SCREEN) {
                                Modifier.background(AtmosphericGradient)
                            } else {
                                Modifier.background(Color(0xFFEFEBE4))
                            }
                        )
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Phone top status bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "09:41",
                                fontFamily = BodyFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.5.sp,
                                color = if (selectedSurface == WidgetSurface.LOCK_SCREEN) Color.White.copy(alpha = 0.8f) else Stone
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (selectedSurface == WidgetSurface.LOCK_SCREEN) Color.White.copy(alpha = 0.7f) else StoneMuted)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (selectedSurface == WidgetSurface.LOCK_SCREEN) Color.White.copy(alpha = 0.7f) else StoneMuted)
                                )
                            }
                        }

                        // ── WIDGET MOCKUP BASED ON SELECTION ───────
                        when (selectedSurface) {
                            WidgetSurface.HOME_SCREEN -> {
                                when (selectedSize) {
                                    WidgetSize.SMALL -> SmallWidgetPreview(affirmation = activeAffirmation)
                                    WidgetSize.MEDIUM -> MediumWidgetPreview(affirmation = activeAffirmation)
                                    WidgetSize.LARGE -> LargeWidgetPreview(affirmation = activeAffirmation)
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                // Simulated app dock icons at bottom of home screen
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    repeat(4) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Surface.copy(alpha = 0.75f))
                                                .border(0.5.dp, Border, RoundedCornerShape(12.dp))
                                        )
                                    }
                                }
                            }

                            WidgetSurface.LOCK_SCREEN -> {
                                LockScreenWidgetPreview(affirmation = activeAffirmation)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Widget Content Source Selector ────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "FEED CONTENT SOURCE",
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    letterSpacing = 1.4.sp,
                    color = StoneMuted
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Surface)
                        .border(1.dp, Border, RoundedCornerShape(16.dp))
                        .padding(6.dp)
                ) {
                    listOf(
                        "Declaration of the Day" to "Refreshes automatically every dawn",
                        "My Saved Declarations" to "Rotates through your personal bookmarks",
                        "Specific Category" to "Locks widget to $selectedCategory declarations"
                    ).forEach { (source, desc) ->
                        val isSelected = selectedSource == source
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) PorcelainWarm else Color.Transparent)
                                .clickable { selectedSource = source }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = source,
                                    fontFamily = BodyFontFamily,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    fontSize = 13.5.sp,
                                    color = Espresso
                                )
                                Text(
                                    text = desc,
                                    fontFamily = BodyFontFamily,
                                    fontSize = 11.5.sp,
                                    color = StoneMuted
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Terracotta,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }

                // If specific category is selected, show category pills
                if (selectedSource == "Specific Category") {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AffirmationRepository.categories.filter { it != "All" }.forEach { cat ->
                            val isCatSelected = selectedCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isCatSelected) Espresso else Surface)
                                    .border(1.dp, if (isCatSelected) Espresso else Border, RoundedCornerShape(16.dp))
                                    .clickable { selectedCategory = cat }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = cat,
                                    fontFamily = BodyFontFamily,
                                    fontWeight = if (isCatSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 12.sp,
                                    color = if (isCatSelected) Color.White else Espresso
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Refresh Frequency ────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "HOW OFTEN IT REFRESHES",
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    letterSpacing = 1.4.sp,
                    color = StoneMuted
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Every Dawn", "Twice Daily", "Every 4 Hours").forEach { schedule ->
                        val isSelected = selectedSchedule.startsWith(schedule)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Espresso else Surface)
                                .border(1.dp, if (isSelected) Espresso else Border, RoundedCornerShape(12.dp))
                                .clickable { selectedSchedule = schedule }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = schedule,
                                fontFamily = BodyFontFamily,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (isSelected) Color.White else Espresso
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // ── Main Action: Add to Home Screen ───────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Button(
                    onClick = {
                        WidgetHelper.pinWidgetToHomeScreen(context)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Terracotta,
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
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (selectedSurface == WidgetSurface.HOME_SCREEN) "Add Widget to Home Screen" else "Set Lock Screen Widget",
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

// ── WIDGET MOCKUP: Small (2×2) ────────────────────────────────────
@Composable
private fun SmallWidgetPreview(affirmation: Affirmation) {
    Box(
        modifier = Modifier
            .size(150.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = Espresso.copy(alpha = 0.12f))
            .clip(RoundedCornerShape(20.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = affirmation.category.uppercase(),
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 9.sp,
                letterSpacing = 1.4.sp,
                color = Terracotta
            )

            Text(
                text = "“${affirmation.declaration}”",
                fontFamily = DisplayFontFamily,
                fontSize = 12.5.sp,
                lineHeight = 17.sp,
                color = Espresso,
                maxLines = 4
            )

            Text(
                text = affirmation.reference.uppercase(),
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 8.5.sp,
                letterSpacing = 1.sp,
                color = StoneMuted
            )
        }
    }
}

// ── WIDGET MOCKUP: Medium (4×2) ───────────────────────────────────
@Composable
private fun MediumWidgetPreview(affirmation: Affirmation) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = Espresso.copy(alpha = 0.12f))
            .clip(RoundedCornerShape(20.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = affirmation.reference.uppercase(),
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.5.sp,
                    letterSpacing = 1.4.sp,
                    color = Terracotta
                )

                Text(
                    text = "06:30 AM",
                    fontFamily = BodyFontFamily,
                    fontSize = 10.sp,
                    color = StoneMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "“${affirmation.declaration}”",
                fontFamily = DisplayFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 15.5.sp,
                lineHeight = 22.sp,
                color = Espresso,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Porcelain)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "“${affirmation.scriptureText}”",
                    fontFamily = DisplayFontFamily,
                    fontStyle = FontStyle.Italic,
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp,
                    color = Stone,
                    maxLines = 2
                )
            }
        }
    }
}

// ── WIDGET MOCKUP: Large (4×4) ────────────────────────────────────
@Composable
private fun LargeWidgetPreview(affirmation: Affirmation) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .shadow(6.dp, RoundedCornerShape(22.dp), spotColor = Espresso.copy(alpha = 0.18f))
            .clip(RoundedCornerShape(22.dp))
            .background(AtmosphericGradient)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.18f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = affirmation.category.uppercase(),
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 9.sp,
                        letterSpacing = 1.4.sp,
                        color = Color.White
                    )
                }

                Text(
                    text = "DAILY DECLARATION",
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 9.sp,
                    letterSpacing = 1.4.sp,
                    color = TerracottaLight
                )
            }

            Text(
                text = "“${affirmation.declaration}”",
                fontFamily = DisplayFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                lineHeight = 25.sp,
                color = Color.White,
                maxLines = 4
            )

            Column {
                Text(
                    text = "“${affirmation.scriptureText}”",
                    fontFamily = DisplayFontFamily,
                    fontStyle = FontStyle.Italic,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = affirmation.reference.uppercase(),
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    letterSpacing = 1.2.sp,
                    color = TerracottaLight
                )
            }
        }
    }
}

// ── WIDGET MOCKUP: Lock Screen ────────────────────────────────────
@Composable
private fun LockScreenWidgetPreview(affirmation: Affirmation) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Large lock screen clock
        Text(
            text = "09:41",
            fontFamily = DisplayFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 52.sp,
            letterSpacing = (-1).sp,
            color = Color.White
        )
        Text(
            text = "Sunday, September 20",
            fontFamily = BodyFontFamily,
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Lock screen compact widget
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.14f))
                .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "“${affirmation.declaration}”",
                    fontFamily = DisplayFontFamily,
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = affirmation.reference.uppercase(),
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 9.5.sp,
                    letterSpacing = 1.4.sp,
                    color = TerracottaLight
                )
            }
        }
    }
}
