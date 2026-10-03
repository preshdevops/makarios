package com.makarios.app.ui.screens

import android.widget.Toast
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.makarios.app.R
import com.makarios.app.data.Affirmation
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.ui.theme.*
import com.makarios.app.util.WidgetHelper

enum class WidgetSurface {
    HOME_SCREEN,
    LOCK_SCREEN
}

enum class WidgetSize(val label: String, val gridLabel: String) {
    SIZE_2X2("Compact", "2×2"),
    SIZE_4X2("Banner", "4×2"),
    SIZE_4X3("Feature", "4×3"),
    SIZE_4X4("Full", "4×4")
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
    var selectedSize by remember { mutableStateOf(WidgetSize.SIZE_4X2) }
    var selectedTheme by remember { mutableStateOf("Alabaster Dawn") }
    var selectedLockMode by remember { mutableStateOf("Complication Card") } // "Complication Card" vs "Full Wallpaper"
    var selectedSource by remember { mutableStateOf("Declaration of the Day") }
    var selectedCategory by remember { mutableStateOf("Peace") }
    var selectedSchedule by remember { mutableStateOf("Every Dawn") }

    // Active affirmation powering the widget preview
    var previewAffirmationOverride by remember { mutableStateOf<Affirmation?>(null) }
    val activeAffirmation = remember(selectedSource, selectedCategory, previewAffirmationOverride) {
        previewAffirmationOverride ?: when (selectedSource) {
            "Declaration of the Day" -> AffirmationRepository.affirmationOfTheDay
            "My Saved Declarations" -> AffirmationRepository.getSaved().firstOrNull() ?: AffirmationRepository.affirmationOfTheDay
            else -> AffirmationRepository.getAll().firstOrNull { it.category.equals(selectedCategory, ignoreCase = true) }
                ?: AffirmationRepository.affirmationOfTheDay
        }
    }

    val shufflePreview: () -> Unit = {
        val pool = AffirmationRepository.getAll()
        val next = pool.filter { it.id != activeAffirmation.id }.randomOrNull() ?: activeAffirmation
        previewAffirmationOverride = next
        Toast.makeText(context, "Shuffled declaration ✓", Toast.LENGTH_SHORT).show()
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

                Box(modifier = Modifier.size(38.dp))
            }

            // ── Screen Header ─────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = if (selectedSurface == WidgetSurface.HOME_SCREEN) "Home Screen Widget" else "Lock Screen Widget",
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp,
                    lineHeight = 32.sp,
                    letterSpacing = (-0.3).sp,
                    color = Espresso
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (selectedSurface == WidgetSurface.HOME_SCREEN)
                        "Pin sacred biblical affirmations to your home screen glance."
                    else
                        "Awaken your phone to scripture every time you glance at your lock screen.",
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
                    .background(PorcelainWarm.copy(alpha = 0.6f))
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
                            .padding(vertical = 10.dp),
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
                                modifier = Modifier.size(16.dp)
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

            // ── Surface Sub-Selectors ──────────────────────────────
            if (selectedSurface == WidgetSurface.HOME_SCREEN) {
                // Size Selector
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
                                    fontSize = 12.sp,
                                    color = if (isSelected) Color.White else Espresso
                                )
                                Text(
                                    text = size.gridLabel,
                                    fontFamily = BodyFontFamily,
                                    fontSize = 10.sp,
                                    color = if (isSelected) Color.White.copy(alpha = 0.8f) else StoneMuted
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Aesthetic Theme Selector for Home Screen
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Alabaster Dawn", "Twilight Sanctuary", "Sunlit Gold", "Eucalyptus Sage").forEach { themeName ->
                        val isSelected = selectedTheme == themeName
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Espresso else Surface)
                                .border(1.dp, if (isSelected) Espresso else Border, RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedTheme = themeName
                                    WidgetHelper.setWidgetTheme(
                                        context,
                                        if (themeName.contains("Twilight")) "twilight" else "alabaster"
                                    )
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = themeName,
                                fontFamily = BodyFontFamily,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 11.5.sp,
                                color = if (isSelected) Color.White else Espresso
                            )
                        }
                    }
                }
            } else {
                // Lock Screen Mode Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Complication Card" to "Translucent lock screen pill",
                        "Live Wallpaper" to "Full-bleed typography art"
                    ).forEach { (mode, desc) ->
                        val isSelected = selectedLockMode == mode
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
                                .clickable { selectedLockMode = mode }
                                .padding(vertical = 8.dp, horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = mode,
                                    fontFamily = BodyFontFamily,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    fontSize = 12.5.sp,
                                    color = if (isSelected) Color.White else Espresso
                                )
                                Text(
                                    text = desc,
                                    fontFamily = BodyFontFamily,
                                    fontSize = 9.5.sp,
                                    color = if (isSelected) Color.White.copy(alpha = 0.8f) else StoneMuted,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ── Interactive Simulated Phone Canvas ─────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(26.dp),
                            spotColor = Espresso.copy(alpha = 0.18f)
                        )
                        .clip(RoundedCornerShape(26.dp))
                        .then(
                            if (selectedSurface == WidgetSurface.LOCK_SCREEN) {
                                Modifier.background(Color.Black)
                            } else {
                                Modifier.background(
                                    when (selectedTheme) {
                                        "Twilight Sanctuary" -> Color(0xFF191411)
                                        "Sunlit Gold" -> Color(0xFFFBF4E8)
                                        "Eucalyptus Sage" -> Color(0xFFEDF3EE)
                                        else -> Color(0xFFF7F4EE)
                                    }
                                )
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
                                color = if (selectedSurface == WidgetSurface.LOCK_SCREEN) Color.White.copy(alpha = 0.85f) else Stone
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

                        // ── WIDGET MOCKUP ─────────────────────────
                        when (selectedSurface) {
                            WidgetSurface.HOME_SCREEN -> {
                                when (selectedSize) {
                                    WidgetSize.SIZE_2X2 -> SmallWidgetPreview(
                                        affirmation = activeAffirmation,
                                        theme = selectedTheme,
                                        onShuffle = shufflePreview
                                    )
                                    WidgetSize.SIZE_4X2 -> MediumWidgetPreview(
                                        affirmation = activeAffirmation,
                                        theme = selectedTheme,
                                        onShuffle = shufflePreview
                                    )
                                    WidgetSize.SIZE_4X3 -> FeatureWidgetPreview(
                                        affirmation = activeAffirmation,
                                        theme = selectedTheme,
                                        onShuffle = shufflePreview
                                    )
                                    WidgetSize.SIZE_4X4 -> LargeWidgetPreview(
                                        affirmation = activeAffirmation,
                                        theme = selectedTheme,
                                        onShuffle = shufflePreview
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // App dock icons simulation
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    repeat(4) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (selectedTheme == "Twilight Sanctuary") Color.White.copy(alpha = 0.12f)
                                                    else Surface.copy(alpha = 0.8f)
                                                )
                                                .border(
                                                    0.5.dp,
                                                    if (selectedTheme == "Twilight Sanctuary") Color.White.copy(alpha = 0.18f) else Border,
                                                    RoundedCornerShape(12.dp)
                                                )
                                        )
                                    }
                                }
                            }

                            WidgetSurface.LOCK_SCREEN -> {
                                LockScreenWidgetPreview(
                                    affirmation = activeAffirmation,
                                    isFullWallpaper = selectedLockMode == "Live Wallpaper",
                                    onShuffle = shufflePreview
                                )
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
                    text = "WIDGET CONTENT SOURCE",
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
                    text = "REFRESH CADENCE",
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
                        val isSelected = selectedSchedule == schedule
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

            // ── Action Buttons ───────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (selectedSurface == WidgetSurface.HOME_SCREEN) {
                    Button(
                        onClick = {
                            WidgetHelper.setWidgetAffirmation(context, activeAffirmation)
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
                                text = "Add Widget to Home Screen",
                                fontFamily = BodyFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    // Lock Screen Actions
                    Button(
                        onClick = {
                            WidgetHelper.setLockScreenAffirmation(
                                context = context,
                                affirmation = activeAffirmation,
                                styleIndex = 4,
                                photoUrl = activeAffirmation.imageUrl
                            )
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
                                imageVector = Icons.Default.Wallpaper,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Apply to Lock Screen",
                                fontFamily = BodyFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            WidgetHelper.pinWidgetToLockScreen(context)
                        },
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Border),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Espresso,
                                modifier = Modifier.size(17.dp)
                            )
                            Text(
                                text = "Pin Keyguard Widget",
                                fontFamily = BodyFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.5.sp,
                                color = Espresso
                            )
                        }
                    }

                    // Explanatory guidance note
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(PorcelainWarm.copy(alpha = 0.5f))
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = StoneMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Apply to Lock Screen places the affirmation safely below your clock so no text is obscured. Pin Keyguard Widget places it in your system lock screen widget tray.",
                                fontFamily = BodyFontFamily,
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp,
                                color = Stone
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── WIDGET MOCKUP: Compact (2×2) ──────────────────────────────────
@Composable
private fun SmallWidgetPreview(
    affirmation: Affirmation,
    theme: String = "Alabaster Dawn",
    onShuffle: () -> Unit = {}
) {
    val isDark = theme == "Twilight Sanctuary"
    val bgColor = if (isDark) Color(0xFF161210) else Surface
    val textColor = if (isDark) Color.White else Espresso
    val metadataColor = if (isDark) Color.White.copy(alpha = 0.40f) else Espresso.copy(alpha = 0.40f)
    val refColor = if (isDark) Color.White.copy(alpha = 0.65f) else StoneMuted
    val borderColor = if (isDark) Color(0x33FFFFFF) else Border
    val buttonBg = if (isDark) Color.White.copy(alpha = 0.10f) else Espresso.copy(alpha = 0.06f)
    val buttonTint = if (isDark) Color.White.copy(alpha = 0.75f) else Espresso.copy(alpha = 0.70f)

    Box(
        modifier = Modifier
            .size(152.dp)
            .shadow(2.dp, RoundedCornerShape(22.dp), spotColor = Espresso.copy(alpha = 0.08f))
            .clip(RoundedCornerShape(22.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(22.dp))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            // Top Left Metadata: Left-aligned, tiny 10pt all-caps font, 40% opacity
            Text(
                text = "DAILY ATTUNEMENT",
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 9.sp,
                letterSpacing = 1.1.sp,
                color = metadataColor
            )

            // Center Main Text: Bold font
            Text(
                text = "“${affirmation.declaration}”",
                fontFamily = DisplayFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.5.sp,
                lineHeight = 19.sp,
                color = textColor,
                maxLines = 3
            )

            // Bottom Row: Reference on left, tucked shuffle button on bottom right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = affirmation.reference.uppercase(),
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 8.5.sp,
                    letterSpacing = 0.8.sp,
                    color = refColor,
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(buttonBg)
                        .clickable(onClick = onShuffle),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_widget_loop),
                        contentDescription = "Shuffle declaration",
                        tint = buttonTint,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

// ── WIDGET MOCKUP: Banner (4×2) ────────────────────────────────────
@Composable
private fun MediumWidgetPreview(
    affirmation: Affirmation,
    theme: String = "Alabaster Dawn",
    onShuffle: () -> Unit = {}
) {
    val isDark = theme == "Twilight Sanctuary"
    val bgColor = if (isDark) Color(0xFF161210) else Surface
    val textColor = if (isDark) Color.White else Espresso
    val metadataColor = if (isDark) Color.White.copy(alpha = 0.40f) else Espresso.copy(alpha = 0.40f)
    val refColor = if (isDark) Color.White.copy(alpha = 0.65f) else StoneMuted
    val borderColor = if (isDark) Color(0x33FFFFFF) else Border
    val buttonBg = if (isDark) Color.White.copy(alpha = 0.10f) else Espresso.copy(alpha = 0.06f)
    val buttonTint = if (isDark) Color.White.copy(alpha = 0.75f) else Espresso.copy(alpha = 0.70f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(22.dp), spotColor = Espresso.copy(alpha = 0.08f))
            .clip(RoundedCornerShape(22.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(22.dp))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Left Metadata: Left-aligned, tiny 10pt all-caps font, 40% opacity
            Text(
                text = "DAILY ATTUNEMENT  ·  ${affirmation.category.uppercase()}",
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                letterSpacing = 1.2.sp,
                color = metadataColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Center Main Text: Bold 17.5sp/pt font
            Text(
                text = "“${affirmation.declaration}”",
                fontFamily = DisplayFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 17.5.sp,
                lineHeight = 23.sp,
                color = textColor,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Row: Reference on left, tucked shuffle button on bottom right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "— ${affirmation.reference.uppercase()}",
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    letterSpacing = 0.8.sp,
                    color = refColor
                )

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(buttonBg)
                        .clickable(onClick = onShuffle),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_widget_loop),
                        contentDescription = "Shuffle declaration",
                        tint = buttonTint,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

// ── WIDGET MOCKUP: Feature (4×3) ───────────────────────────────────
@Composable
private fun FeatureWidgetPreview(
    affirmation: Affirmation,
    theme: String = "Alabaster Dawn",
    onShuffle: () -> Unit = {}
) {
    val isDark = theme == "Twilight Sanctuary"
    val bgColor = if (isDark) Color(0xFF161210) else Surface
    val textColor = if (isDark) Color.White else Espresso
    val metadataColor = if (isDark) Color.White.copy(alpha = 0.40f) else Espresso.copy(alpha = 0.40f)
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF6B625B)
    val borderColor = if (isDark) Color(0x33FFFFFF) else Border
    val buttonBg = if (isDark) Color.White.copy(alpha = 0.10f) else Espresso.copy(alpha = 0.06f)
    val buttonTint = if (isDark) Color.White.copy(alpha = 0.75f) else Espresso.copy(alpha = 0.70f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(22.dp), spotColor = Espresso.copy(alpha = 0.08f))
            .clip(RoundedCornerShape(22.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(22.dp))
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Top Left Metadata: Left-aligned, tiny 10pt all-caps font, 40% opacity
            Text(
                text = "DAILY ATTUNEMENT  ·  ${affirmation.category.uppercase()}",
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                letterSpacing = 1.2.sp,
                color = metadataColor
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Center Main Text: Bold 18.5sp/pt font
            Text(
                text = "“${affirmation.declaration}”",
                fontFamily = DisplayFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.5.sp,
                lineHeight = 24.sp,
                color = textColor,
                maxLines = 3
            )

            if (affirmation.scriptureText.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "“${affirmation.scriptureText}”",
                    fontFamily = DisplayFontFamily,
                    fontStyle = FontStyle.Italic,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = secondaryText,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Row: Reference on left, tucked shuffle button on bottom right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "— ${affirmation.reference.uppercase()}",
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    letterSpacing = 0.8.sp,
                    color = secondaryText
                )

                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(buttonBg)
                        .clickable(onClick = onShuffle),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_widget_loop),
                        contentDescription = "Shuffle declaration",
                        tint = buttonTint,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ── WIDGET MOCKUP: Full Hero (4×4) ────────────────────────────────
@Composable
private fun LargeWidgetPreview(
    affirmation: Affirmation,
    theme: String = "Alabaster Dawn",
    onShuffle: () -> Unit = {}
) {
    val isDark = theme == "Twilight Sanctuary"
    val bgColor = if (isDark) Color(0xFF161210) else Surface
    val textColor = if (isDark) Color.White else Espresso
    val metadataColor = if (isDark) Color.White.copy(alpha = 0.40f) else Espresso.copy(alpha = 0.40f)
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF6B625B)
    val borderColor = if (isDark) Color(0x33FFFFFF) else Border
    val buttonBg = if (isDark) Color.White.copy(alpha = 0.10f) else Espresso.copy(alpha = 0.06f)
    val buttonTint = if (isDark) Color.White.copy(alpha = 0.75f) else Espresso.copy(alpha = 0.70f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(22.dp), spotColor = Espresso.copy(alpha = 0.08f))
            .clip(RoundedCornerShape(22.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(22.dp))
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Top Left Metadata: Left-aligned, tiny 10pt all-caps font, 40% opacity
            Text(
                text = "DAILY ATTUNEMENT  ·  ${affirmation.category.uppercase()}",
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 10.5.sp,
                letterSpacing = 1.3.sp,
                color = metadataColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Center Main Text: Bold 21sp font
            Text(
                text = "“${affirmation.declaration}”",
                fontFamily = DisplayFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp,
                lineHeight = 28.sp,
                color = textColor,
                maxLines = 4
            )

            if (affirmation.scriptureText.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "“${affirmation.scriptureText}”",
                    fontFamily = DisplayFontFamily,
                    fontStyle = FontStyle.Italic,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = secondaryText,
                    maxLines = 3
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Row: Reference on left, tucked shuffle button on bottom right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "— ${affirmation.reference.uppercase()}",
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    color = secondaryText
                )

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(buttonBg)
                        .clickable(onClick = onShuffle),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_widget_loop),
                        contentDescription = "Shuffle declaration",
                        tint = buttonTint,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

// ── WIDGET MOCKUP: Lock Screen ────────────────────────────────────
@Composable
private fun LockScreenWidgetPreview(
    affirmation: Affirmation,
    isFullWallpaper: Boolean = false,
    onShuffle: () -> Unit = {}
) {
    val today = remember {
        val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())
        LocalDate.now().format(formatter)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(390.dp)
            .clip(RoundedCornerShape(20.dp))
    ) {
        // Photographic background if available
        if (affirmation.imageUrl.isNotBlank()) {
            AsyncImage(
                model = affirmation.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x66000000),
                                Color(0x99000000),
                                Color(0xE60E0B08)
                            )
                        )
                    )
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AtmosphericGradient)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Lock Screen Clock
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "09:41",
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 48.sp,
                    letterSpacing = (-1).sp,
                    color = Color.White
                )
                Text(
                    text = today,
                    fontFamily = BodyFontFamily,
                    fontSize = 12.5.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            if (isFullWallpaper) {
                // Full wallpaper typography centered in the lower optical zone
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "DAILY ATTUNEMENT  ·  ${affirmation.category.uppercase()}",
                        fontFamily = BodyFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.3.sp,
                        color = Color.White.copy(alpha = 0.40f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "“${affirmation.declaration}”",
                        fontFamily = DisplayFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp,
                        color = Color.White,
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "— ${affirmation.reference.uppercase()}",
                            fontFamily = BodyFontFamily,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp,
                            color = Color.White.copy(alpha = 0.65f)
                        )

                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                                .clickable(onClick = onShuffle),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_widget_loop),
                                contentDescription = "Shuffle declaration",
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            } else {
                // Keyguard Complication Card Widget: Exact prompt specs
                // Top Left: DAILY ATTUNEMENT in 40% opacity white
                // Center Main Text: Bold 18pt font
                // Bottom Right Button: Tucked clean into the lower corner: simple [+] or thin loop icon for shuffling
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.14f))
                        .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(20.dp))
                        .padding(14.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        // Top Left Metadata: Left-aligned, tiny 10pt all-caps font, 40% opacity white
                        Text(
                            text = "DAILY ATTUNEMENT",
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp,
                            letterSpacing = 1.2.sp,
                            color = Color.White.copy(alpha = 0.40f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Center Main Text: Bold 18pt font
                        Text(
                            text = "“${affirmation.declaration}”",
                            fontFamily = DisplayFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            lineHeight = 24.sp,
                            color = Color.White,
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Bottom Row: Reference on left, Shuffle button tucked into bottom right
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "— ${affirmation.reference.uppercase()}",
                                fontFamily = BodyFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 10.sp,
                                letterSpacing = 0.8.sp,
                                color = Color.White.copy(alpha = 0.65f)
                            )

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.16f))
                                    .clickable(onClick = onShuffle),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_widget_loop),
                                    contentDescription = "Shuffle declaration",
                                    tint = Color.White.copy(alpha = 0.85f),
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
