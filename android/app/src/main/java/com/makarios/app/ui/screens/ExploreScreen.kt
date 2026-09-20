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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.data.AffirmationRepository
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
        containerColor = Porcelain
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp, bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Makarios",
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp,
                        color = Espresso
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(PorcelainWarm),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "M",
                            fontFamily = DisplayFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Espresso
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Everyday Presence",
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp,
                    lineHeight = 32.sp,
                    color = Espresso
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Keep declarations on your home screen, updated quietly throughout the day.",
                    color = Stone,
                    fontFamily = BodyFontFamily,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Size selector tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "small" to "Small 2×2",
                    "medium" to "Medium 4×2",
                    "large" to "Large 4×4"
                ).forEach { (key, label) ->
                    val isSelected = selectedSize == key
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
                            .clickable { selectedSize = key }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Surface else Espresso,
                            fontFamily = BodyFontFamily,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Widget preview card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = Espresso.copy(alpha = 0.06f)
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .background(Surface)
                    .padding(20.dp)
            ) {
                Column {
                    // Reference
                    Text(
                        text = currentWidgetTruth.reference.uppercase(),
                        color = Terracotta,
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Declaration
                    Text(
                        text = "\u201C${currentWidgetTruth.declaration}\u201D",
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp,
                        lineHeight = 26.sp,
                        color = Espresso
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Context
                    Text(
                        text = currentWidgetTruth.context,
                        fontFamily = BodyFontFamily,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Stone
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Divider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Border)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Footer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Evening Examen · 08:30 PM",
                            fontFamily = BodyFontFamily,
                            fontSize = 12.sp,
                            color = StoneMuted
                        )
                        Text(
                            text = "Next truth →",
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = Terracotta
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Update cadence section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Update Cadence",
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Espresso
                    )
                    Text(
                        text = "Every Dawn (6 AM)",
                        fontFamily = BodyFontFamily,
                        fontSize = 13.sp,
                        color = StoneMuted
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Every Dawn", "Every 4h", "On Wake").forEach { cadence ->
                        val isSelected = selectedCadence == cadence
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .then(
                                    if (isSelected) {
                                        Modifier
                                            .background(PorcelainWarm)
                                            .border(1.dp, Border, RoundedCornerShape(12.dp))
                                    } else {
                                        Modifier
                                            .background(Surface)
                                            .border(1.dp, Border, RoundedCornerShape(12.dp))
                                    }
                                )
                                .clickable { selectedCadence = cadence }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cadence,
                                color = if (isSelected) Espresso else Stone,
                                fontFamily = BodyFontFamily,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Add to Home Screen CTA
            Button(
                onClick = { /* Add to Home Screen prompt */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Espresso,
                    contentColor = Surface
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(52.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Surface,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Add to Home Screen",
                        color = Surface,
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
