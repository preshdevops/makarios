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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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

enum class CreatorTab(val label: String) {
    TEXT("Text"),
    BG("BG"),
    LAYOUT("Layout"),
    STYLES("Styles")
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
        else AffirmationRepository.affirmationOfTheDay
    }

    var selectedBgIndex by remember { mutableStateOf(0) }
    var selectedTab by remember { mutableStateOf(CreatorTab.TEXT) }

    val backgroundPresets = listOf(
        "Porcelain Solid" to null,
        "Morning Sunrise" to "https://images.unsplash.com/photo-1507652313519-d4e9174996dd?auto=format&fit=crop&w=800&q=80",
        "Golden Marble" to "https://images.unsplash.com/photo-1513519245088-0e12902e5a38?auto=format&fit=crop&w=800&q=80"
    )

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
            // ── Top Header: < Visual Creator with Export pill button (Page 2 in PDF) ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
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

                    Text(
                        text = "Visual Creator",
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp,
                        color = Espresso
                    )
                }

                // Export Button (pill button in Terracotta)
                Button(
                    onClick = {
                        Toast.makeText(context, "Visual exported to gallery", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Terracotta,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = "Export",
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ── BACKGROUNDS Section: Title + "+" button + Horizontal Thumbnails ──
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BACKGROUNDS",
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        letterSpacing = 1.4.sp,
                        color = StoneMuted
                    )

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Surface)
                            .border(1.dp, Border, CircleShape)
                            .clickable {
                                Toast.makeText(context, "Choose photo from gallery", Toast.LENGTH_SHORT).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add background",
                            tint = Terracotta,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Thumbnails
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    backgroundPresets.forEachIndexed { index, (name, url) ->
                        val isSelected = selectedBgIndex == index
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .shadow(2.dp, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .then(
                                    if (url == null) {
                                        Modifier.background(Porcelain)
                                    } else {
                                        Modifier
                                    }
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Terracotta else Border,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedBgIndex = index }
                        ) {
                            if (url != null) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Live Canvas Area ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                val currentBgUrl = backgroundPresets[selectedBgIndex].second

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = Espresso.copy(alpha = 0.08f))
                        .clip(RoundedCornerShape(20.dp))
                        .then(
                            if (currentBgUrl == null) {
                                Modifier.background(Surface)
                            } else {
                                Modifier.background(Espresso)
                            }
                        )
                        .border(1.dp, Border, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (currentBgUrl != null) {
                        AsyncImage(
                            model = currentBgUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.45f))
                        )
                    }

                    val textColor = if (currentBgUrl != null) Color.White else Espresso
                    val subColor = if (currentBgUrl != null) Color.White.copy(alpha = 0.8f) else Terracotta

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = affirmation.category.uppercase(),
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            letterSpacing = 2.sp,
                            color = subColor
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "“${affirmation.declaration}”",
                            fontFamily = DisplayFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 20.sp,
                            lineHeight = 28.sp,
                            textAlign = TextAlign.Center,
                            color = textColor
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = affirmation.reference,
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = if (currentBgUrl != null) Color.White.copy(alpha = 0.7f) else StoneMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Bottom Dock Toolbar: A Text | BG | Layout | Styles (Page 2 in PDF) ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Espresso.copy(alpha = 0.06f))
                    .clip(RoundedCornerShape(16.dp))
                    .background(Surface)
                    .border(1.dp, Border, RoundedCornerShape(16.dp))
                    .padding(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val tabs = listOf(
                        CreatorTab.TEXT to "A",
                        CreatorTab.BG to "🖼",
                        CreatorTab.LAYOUT to "≡",
                        CreatorTab.STYLES to "🎨"
                    )

                    tabs.forEach { (tab, iconText) ->
                        val isSelected = selectedTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .then(
                                    if (isSelected) {
                                        Modifier.background(TerracottaLight)
                                    } else {
                                        Modifier.background(Color.Transparent)
                                    }
                                )
                                .clickable { selectedTab = tab }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = iconText,
                                    fontSize = 15.sp,
                                    color = if (isSelected) Terracotta else StoneMuted
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = tab.label,
                                    fontFamily = BodyFontFamily,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 11.sp,
                                    color = if (isSelected) Terracotta else StoneMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
