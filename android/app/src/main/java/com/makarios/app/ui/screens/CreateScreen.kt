package com.makarios.app.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.ui.theme.*

enum class WallpaperStyle(val label: String) {
    PORCELAIN("Porcelain"),
    SANCTUARY("Sanctuary Dark"),
    TERRACOTTA("Terracotta"),
    PHOTO("Photo Art")
}

enum class WallpaperAspect(val label: String) {
    LOCK_SCREEN("Lock Screen"),
    HOME_SCREEN("Home Screen"),
    SQUARE("Square 1:1")
}

@Composable
fun CreateScreen(
    affirmationId: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val affirmation = remember(affirmationId) {
        if (affirmationId != null) AffirmationRepository.getById(affirmationId)
            ?: AffirmationRepository.starterAffirmation
        else AffirmationRepository.starterAffirmation
    }

    var selectedStyle by remember { mutableStateOf(WallpaperStyle.PORCELAIN) }
    var selectedAspect by remember { mutableStateOf(WallpaperAspect.LOCK_SCREEN) }
    var showScripture by remember { mutableStateOf(true) }
    var showWatermark by remember { mutableStateOf(true) }
    var actionMessage by remember { mutableStateOf<String?>(null) }

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
            // Header bar with back action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
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
                    Column {
                        Text(
                            text = "Creative Studio",
                            fontFamily = DisplayFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 20.sp,
                            color = Espresso
                        )
                        Text(
                            text = "High-resolution wallpaper & lock screen",
                            fontFamily = BodyFontFamily,
                            fontSize = 12.sp,
                            color = StoneMuted
                        )
                    }
                }
            }

            // Wallpaper Live Preview Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                val previewHeight = when (selectedAspect) {
                    WallpaperAspect.LOCK_SCREEN -> 380.dp
                    WallpaperAspect.HOME_SCREEN -> 380.dp
                    WallpaperAspect.SQUARE -> 280.dp
                }
                val previewWidth = when (selectedAspect) {
                    WallpaperAspect.LOCK_SCREEN -> 220.dp
                    WallpaperAspect.HOME_SCREEN -> 220.dp
                    WallpaperAspect.SQUARE -> 280.dp
                }

                Box(
                    modifier = Modifier
                        .size(width = previewWidth, height = previewHeight)
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(20.dp),
                            spotColor = Espresso.copy(alpha = 0.12f)
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .then(
                            when (selectedStyle) {
                                WallpaperStyle.PORCELAIN -> Modifier.background(Porcelain)
                                WallpaperStyle.SANCTUARY -> Modifier.background(Espresso)
                                WallpaperStyle.TERRACOTTA -> Modifier.background(
                                    Brush.verticalGradient(
                                        listOf(Terracotta, Color(0xFFA84E3A))
                                    )
                                )
                                WallpaperStyle.PHOTO -> Modifier.background(Espresso)
                            }
                        )
                        .border(1.5.dp, Border, RoundedCornerShape(20.dp))
                ) {
                    // Photo overlay if style is PHOTO
                    if (selectedStyle == WallpaperStyle.PHOTO) {
                        AsyncImage(
                            model = affirmation.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Dimmer gradient over photo for text contrast
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Black.copy(alpha = 0.35f),
                                            Color.Black.copy(alpha = 0.70f)
                                        )
                                    )
                                )
                        )
                    }

                    // Content inside wallpaper
                    val textColor = when (selectedStyle) {
                        WallpaperStyle.PORCELAIN -> Espresso
                        WallpaperStyle.SANCTUARY, WallpaperStyle.TERRACOTTA, WallpaperStyle.PHOTO -> Surface
                    }
                    val accentColor = when (selectedStyle) {
                        WallpaperStyle.PORCELAIN -> Terracotta
                        WallpaperStyle.SANCTUARY -> Color(0xFFE5B869) // warm amber gold
                        WallpaperStyle.TERRACOTTA -> PorcelainWarm
                        WallpaperStyle.PHOTO -> Porcelain
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Top header: Simulated Lock Screen Time or Category
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (selectedAspect == WallpaperAspect.LOCK_SCREEN) {
                                Text(
                                    text = "09:41",
                                    fontFamily = DisplayFontFamily,
                                    fontWeight = FontWeight.Light,
                                    fontSize = 28.sp,
                                    color = textColor.copy(alpha = 0.85f)
                                )
                                Text(
                                    text = "SUNDAY, SEPTEMBER 20",
                                    fontFamily = BodyFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 8.sp,
                                    letterSpacing = 1.sp,
                                    color = textColor.copy(alpha = 0.65f)
                                )
                            } else {
                                Text(
                                    text = affirmation.category.uppercase(),
                                    fontFamily = BodyFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 9.sp,
                                    letterSpacing = 1.8.sp,
                                    color = accentColor
                                )
                            }
                        }

                        // Center: The Declaration
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        ) {
                            Text(
                                text = "“${affirmation.declaration}”",
                                fontFamily = DisplayFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = if (selectedAspect == WallpaperAspect.SQUARE) 18.sp else 16.sp,
                                lineHeight = if (selectedAspect == WallpaperAspect.SQUARE) 26.sp else 23.sp,
                                textAlign = TextAlign.Center,
                                color = textColor
                            )

                            if (showScripture) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = affirmation.reference.uppercase(),
                                    fontFamily = BodyFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 9.sp,
                                    letterSpacing = 1.4.sp,
                                    textAlign = TextAlign.Center,
                                    color = accentColor
                                )
                            }
                        }

                        // Bottom: Makarios watermark
                        if (showWatermark) {
                            Text(
                                text = "M A K A R I O S",
                                fontFamily = DisplayFontFamily,
                                fontSize = 8.sp,
                                letterSpacing = 3.sp,
                                color = textColor.copy(alpha = 0.45f)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Style Selector Section
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "Atmosphere & Palette",
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Espresso
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WallpaperStyle.values().forEach { style ->
                        val isSelected = selectedStyle == style
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .then(
                                    if (isSelected) {
                                        Modifier.background(Espresso)
                                    } else {
                                        Modifier
                                            .background(Surface)
                                            .border(1.dp, Border, RoundedCornerShape(12.dp))
                                    }
                                )
                                .clickable { selectedStyle = style }
                                .padding(horizontal = 14.dp, vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = style.label,
                                color = if (isSelected) Surface else Espresso,
                                fontFamily = BodyFontFamily,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Aspect Ratio Selector Section
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "Format & Scale",
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Espresso
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WallpaperAspect.values().forEach { aspect ->
                        val isSelected = selectedAspect == aspect
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .then(
                                    if (isSelected) {
                                        Modifier.background(Terracotta)
                                    } else {
                                        Modifier
                                            .background(Surface)
                                            .border(1.dp, Border, RoundedCornerShape(12.dp))
                                    }
                                )
                                .clickable { selectedAspect = aspect }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = aspect.label,
                                color = if (isSelected) Surface else Espresso,
                                fontFamily = BodyFontFamily,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Editorial Toggles (Scripture & Watermark)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Surface)
                    .border(1.dp, Border, RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Include Scripture Reference",
                        fontFamily = BodyFontFamily,
                        fontSize = 14.sp,
                        color = Espresso
                    )
                    Switch(
                        checked = showScripture,
                        onCheckedChange = { showScripture = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Surface,
                            checkedTrackColor = Terracotta
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.75.dp)
                        .background(Border)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Makarios Signature Mark",
                        fontFamily = BodyFontFamily,
                        fontSize = 14.sp,
                        color = Espresso
                    )
                    Switch(
                        checked = showWatermark,
                        onCheckedChange = { showWatermark = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Surface,
                            checkedTrackColor = Terracotta
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bottom Actions: Save & Share
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Share button
                OutlinedButton(
                    onClick = {
                        Toast.makeText(context, "Ready to share with friends and status", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Border)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = Espresso,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Share",
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Espresso
                        )
                    }
                }

                // Download / Set Wallpaper button
                Button(
                    onClick = {
                        actionMessage = "Wallpaper generated & saved to Gallery"
                        Toast.makeText(context, "Wallpaper generated & saved to device gallery", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Espresso,
                        contentColor = Surface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(50.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = Surface,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Save Wallpaper",
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Surface
                        )
                    }
                }
            }
        }
    }
}
