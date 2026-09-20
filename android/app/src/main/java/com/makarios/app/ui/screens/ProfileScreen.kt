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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.ui.theme.*

@Composable
fun ProfileScreen(
    onNavigateToWidgets: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dawnNotification by remember { mutableStateOf(true) }
    var middayNotification by remember { mutableStateOf(false) }
    var eveningNotification by remember { mutableStateOf(true) }
    var selectedSeason by remember { mutableStateOf("Confidence & Calling") }

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
            // Profile Top Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp, bottom = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(PorcelainWarm)
                            .border(1.5.dp, Border, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "P",
                            fontFamily = DisplayFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 24.sp,
                            color = Espresso
                        )
                    }

                    Column {
                        Text(
                            text = "Precious",
                            fontFamily = DisplayFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 22.sp,
                            color = Espresso
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Walking in sovereign peace and stillness",
                            fontFamily = BodyFontFamily,
                            fontSize = 13.sp,
                            color = Stone
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Widget Quick Status Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Surface)
                    .border(1.dp, Border, RoundedCornerShape(16.dp))
                    .clickable(onClick = onNavigateToWidgets)
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PorcelainWarm),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Widgets,
                                contentDescription = null,
                                tint = Espresso,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Android Glance Widget",
                                    fontFamily = BodyFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = Espresso
                                )
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(Sage)
                                )
                            }
                            Text(
                                text = "Mounted on Home Screen · Synchronized",
                                fontFamily = BodyFontFamily,
                                fontSize = 12.sp,
                                color = StoneMuted
                            )
                        }
                    }

                    Text(
                        text = "Manage →",
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Terracotta
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Ambient Cadence & Reflection Times
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "Ambient Cadence",
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Espresso
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Quiet notifications to center your day around biblical declarations.",
                    fontFamily = BodyFontFamily,
                    fontSize = 13.sp,
                    color = StoneMuted
                )

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Surface)
                        .border(1.dp, Border, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    CadenceSwitchRow(
                        title = "Dawn Revelation",
                        subtitle = "06:00 AM · Morning awakening",
                        checked = dawnNotification,
                        onCheckedChange = { dawnNotification = it }
                    )
                    Box(modifier = Modifier.fillMaxWidth().height(0.75.dp).background(Border))
                    CadenceSwitchRow(
                        title = "Midday Stillness",
                        subtitle = "12:30 PM · Peace amidst work",
                        checked = middayNotification,
                        onCheckedChange = { middayNotification = it }
                    )
                    Box(modifier = Modifier.fillMaxWidth().height(0.75.dp).background(Border))
                    CadenceSwitchRow(
                        title = "Evening Examen",
                        subtitle = "08:30 PM · Restful wind-down",
                        checked = eveningNotification,
                        onCheckedChange = { eveningNotification = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Current Spiritual Season Focus
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "Current Spiritual Focus",
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Espresso
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Surface)
                        .border(1.dp, Border, RoundedCornerShape(16.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "Confidence & Calling" to "Exodus 4:12 · Walking boldly in purpose",
                        "Peace over Anxiety" to "John 14:27 · Quiet protection amidst chaos",
                        "Rest & Renewal" to "Matthew 11:28 · Surrendering performance"
                    ).forEach { (season, verse) ->
                        val isSelected = selectedSeason == season
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) PorcelainWarm else Color.Transparent)
                                .clickable { selectedSeason = season }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = season,
                                    fontFamily = BodyFontFamily,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 14.sp,
                                    color = Espresso
                                )
                                Text(
                                    text = verse,
                                    fontFamily = BodyFontFamily,
                                    fontSize = 12.sp,
                                    color = StoneMuted
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Terracotta,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // About Makarios & Brand Stamp
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "MAKARIOS",
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    letterSpacing = 4.sp,
                    color = Espresso
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Gospel. Tech. Precious.",
                    fontFamily = BodyFontFamily,
                    fontSize = 12.sp,
                    color = StoneMuted
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Version 1.0.0 · Offline Ready",
                    fontFamily = BodyFontFamily,
                    fontSize = 11.sp,
                    color = StoneMuted.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun CadenceSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Espresso
            )
            Text(
                text = subtitle,
                fontFamily = BodyFontFamily,
                fontSize = 12.sp,
                color = StoneMuted
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Surface,
                checkedTrackColor = Terracotta
            )
        )
    }
}
