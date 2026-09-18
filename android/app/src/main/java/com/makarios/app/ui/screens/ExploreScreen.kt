package com.makarios.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
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
        containerColor = ParchmentBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 36.dp)
        ) {
            // Editorial Masthead
            HeaderBar(subtitle = "THE LIVING ARCHIVE")

            // Section Intro
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "SACRED PRESENCE · GLANCE WIDGET",
                    color = RubricVermilion,
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Everyday Presence",
                    color = InkLampblack,
                    fontFamily = DisplayFontFamily,
                    fontSize = 26.sp,
                    lineHeight = 34.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Keep eternal declarations anchored to your home screen. Updated quietly throughout the day.",
                    color = InkIronGall,
                    fontFamily = DisplayFontFamily,
                    fontStyle = FontStyle.Italic,
                    fontSize = 14.5.sp,
                    lineHeight = 22.sp
                )
            }

            // Size Selector Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "small" to "PETITE (2×2)",
                    "medium" to "CANONICAL (4×2)",
                    "large" to "EXPANSIVE (4×4)"
                ).forEach { (key, label) ->
                    val isSelected = selectedSize == key
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isSelected) InkLampblack else ParchmentWarm)
                            .border(0.75.dp, if (isSelected) InkLampblack else HairlineRule, RoundedCornerShape(3.dp))
                            .clickable { selectedSize = key }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) GoldLeaf else InkIronGall,
                            fontFamily = BodyFontFamily,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            fontSize = 9.5.sp,
                            letterSpacing = 1.2.sp
                        )
                    }
                }
            }

            // Exhibition Gallery Widget Plaque
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(6.dp), spotColor = InkLampblack.copy(alpha = 0.08f))
                    .clip(RoundedCornerShape(6.dp))
                    .background(PaperSurface)
                    .border(1.dp, BorderBroadsheet, RoundedCornerShape(6.dp))
                    .padding(22.dp)
            ) {
                Column {
                    // Top Meta: Reference & Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentWidgetTruth.reference.uppercase(),
                            color = RubricVermilion,
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.5.sp,
                            letterSpacing = 2.sp
                        )

                        Text(
                            text = "✦ LIVE GLANCE",
                            color = GoldLeaf,
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 9.5.sp,
                            letterSpacing = 1.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Declaration in Majestic Serif
                    Text(
                        text = "“${currentWidgetTruth.declaration}”",
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp,
                        lineHeight = 26.sp,
                        color = InkLampblack
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Reflection Commentary
                    Text(
                        text = currentWidgetTruth.context,
                        fontFamily = DisplayFontFamily,
                        fontStyle = FontStyle.Italic,
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp,
                        color = InkIronGall
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Hairline separator
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.75.dp)
                            .background(HairlineRule)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Footer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CADENCE: EVERY DAWN",
                            fontFamily = BodyFontFamily,
                            fontSize = 9.sp,
                            letterSpacing = 1.2.sp,
                            color = InkMuted
                        )
                        Text(
                            text = "MAKARIOS FOLIO № 01",
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 9.sp,
                            letterSpacing = 1.2.sp,
                            color = InkLampblack
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Cadence Selector Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "REVELATION CADENCE",
                    color = RubricVermilion,
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 9.5.sp,
                    letterSpacing = 1.8.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Every Dawn", "Midday", "Eventide").forEach { cadence ->
                        val isSelected = selectedCadence == cadence
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isSelected) ParchmentWarm else Color.Transparent)
                                .border(
                                    0.75.dp,
                                    if (isSelected) BorderBroadsheet else HairlineRule,
                                    RoundedCornerShape(3.dp)
                                )
                                .clickable { selectedCadence = cadence }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cadence.uppercase(),
                                color = if (isSelected) InkLampblack else InkIronGall,
                                fontFamily = BodyFontFamily,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 10.sp,
                                letterSpacing = 1.2.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Mount Widget Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Button(
                    onClick = { /* Add to Home Screen prompt */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = InkLampblack,
                        contentColor = GoldLeaf
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(0.75.dp, BorderBroadsheet, RoundedCornerShape(4.dp))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = GoldLeaf,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "MOUNT HOME SCREEN WIDGET",
                            color = GoldLeaf,
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.5.sp,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }
        }
    }
}
