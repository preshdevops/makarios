package com.makarios.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.ui.components.HeaderBar
import com.makarios.app.ui.theme.*

@Composable
fun ExploreScreen(
    modifier: Modifier = Modifier
) {
    var selectedSize by remember { mutableStateOf("large") }
    var selectedCadence by remember { mutableStateOf("Every Dawn") }
    val currentWidgetTruth = AffirmationRepository.widgetAffirmation

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PorcelainBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Header with EXPLORE badge
            HeaderBar(subtitle = "EXPLORE")

            // Section Tag: HOME SCREEN
            Row(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.GridOn,
                    contentDescription = null,
                    tint = SalmonTerracotta,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "HOME SCREEN",
                    color = SalmonTerracotta,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.5.sp,
                    letterSpacing = 1.2.sp
                )
            }

            // Headline
            Text(
                text = "Everyday Presence",
                color = TextPrimary,
                fontFamily = FontFamily.Serif,
                fontSize = 27.sp,
                lineHeight = 34.sp,
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 2.dp)
            )

            // Size Selector Segmented Control
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 10.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(ToneResoluteSand.copy(alpha = 0.5f))
                    .padding(4.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("small" to "Small 2×2", "medium" to "Medium 4×2", "large" to "Large 4×4").forEach { (key, label) ->
                        val isSelected = selectedSize == key
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (isSelected) SurfaceWhite else Color.Transparent)
                                .then(
                                    if (isSelected) Modifier.shadow(2.dp, RoundedCornerShape(999.dp))
                                    else Modifier
                                )
                                .clickable { selectedSize = key }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) TextPrimary else TextMuted,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Phone Simulator Canvas Frame
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFFF4EEE8))
                    .border(1.dp, BorderCard, RoundedCornerShape(28.dp))
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Status Bar Simulation
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp, start = 8.dp, end = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "09:41",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "5G  100%",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 11.sp,
                            color = TextPrimary
                        )
                    }

                    // Live Widget Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(3.dp, RoundedCornerShape(22.dp), spotColor = BrandPlum)
                            .clip(RoundedCornerShape(22.dp))
                            .background(SurfaceWhite)
                            .border(1.dp, BorderCard, RoundedCornerShape(22.dp))
                            .padding(18.dp)
                    ) {
                        Column {
                            // Top Row: Reference & Live Feed Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currentWidgetTruth.reference,
                                    color = SalmonTerracotta,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.5.sp,
                                    letterSpacing = 1.sp
                                )

                                // Live Feed Pill Badge
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(LiveFeedGreenBg)
                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(LiveFeedGreen)
                                    )
                                    Text(
                                        text = "Live Feed",
                                        color = LiveFeedGreen,
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // All-Caps Bold Serif Headline
                            Text(
                                text = currentWidgetTruth.declaration.uppercase(),
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.5.sp,
                                lineHeight = 24.sp,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Reflection Body
                            Text(
                                text = currentWidgetTruth.context,
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 12.5.sp,
                                lineHeight = 19.sp,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Footer Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Evening Examen · 08:30 PM",
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                                Text(
                                    text = "Next truth →",
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = TextPrimary
                                )
                            }
                        }
                    }

                    // 3 Carousel Dots
                    Row(
                        modifier = Modifier.padding(top = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(BorderCard))
                        Box(modifier = Modifier.size(width = 14.dp, height = 5.dp).clip(RoundedCornerShape(999.dp)).background(TextPrimary))
                        Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(BorderCard))
                    }
                }
            }

            // Update Cadence Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "UPDATE CADENCE",
                    color = TextMuted,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.5.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (selectedCadence == "Every Dawn") "Every Dawn (6 AM)" else selectedCadence,
                    color = TextSecondary,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 11.sp
                )
            }

            // Cadence Pill Switcher
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(ToneResoluteSand.copy(alpha = 0.5f))
                    .padding(4.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("Every Dawn", "Every 4h", "On Wake").forEach { c ->
                        val isSelected = selectedCadence == c
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (isSelected) SurfaceWhite else Color.Transparent)
                                .then(
                                    if (isSelected) Modifier.shadow(2.dp, RoundedCornerShape(999.dp))
                                    else Modifier
                                )
                                .clickable { selectedCadence = c }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = c,
                                color = if (isSelected) TextPrimary else TextMuted,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Add to Home Screen Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 14.dp)
            ) {
                Button(
                    onClick = { /* Add to Home Screen prompt */ },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPlum),
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Add to Home Screen",
                            color = Color.White,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.5.sp,
                            letterSpacing = 0.3.sp
                        )
                    }
                }
            }
        }
    }
}
