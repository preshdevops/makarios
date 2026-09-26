package com.makarios.app.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.ui.theme.*
import com.makarios.app.util.ReminderManager
import com.makarios.app.util.WidgetHelper

@Composable
fun ProfileScreen(
    onNavigateToWidgets: () -> Unit,
    onRevisitOnboarding: () -> Unit = {},
    onNavigateToSubscription: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Notification states backed by ReminderManager
    var dawnNotification by remember { mutableStateOf(ReminderManager.isDawnEnabled(context)) }
    var middayNotification by remember { mutableStateOf(ReminderManager.isMiddayEnabled(context)) }
    var eveningNotification by remember { mutableStateOf(ReminderManager.isEveningEnabled(context)) }
    var hourlyNotification by remember { mutableStateOf(ReminderManager.isHourlyEnabled(context)) }

    // Widget states
    var selectedWidgetSource by remember { mutableStateOf("Declaration of the Day") }
    var selectedSchedule by remember { mutableStateOf("Every Dawn") }

    // Spiritual focus state
    var selectedSeason by remember { mutableStateOf("Peace over Anxiety") }

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
            // ── Profile Top Header ──────────────────────────────────
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

            Spacer(modifier = Modifier.height(14.dp))

            // ── Makarios+ Membership Card ───────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = AmberGold.copy(alpha = 0.25f))
                    .clip(RoundedCornerShape(20.dp))
                    .background(SunlitAmberGradient)
                    .clickable(onClick = onNavigateToSubscription)
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "MAKARIOS+",
                                fontFamily = BodyFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                letterSpacing = 2.sp,
                                color = Color.White
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White.copy(alpha = 0.18f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "FREE TRIAL",
                                        fontFamily = BodyFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 9.sp,
                                        letterSpacing = 1.sp,
                                        color = Color.White
                                    )
                                }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Unlock unlimited declarations, 4K wallpapers & all widget themes.",
                            fontFamily = BodyFontFamily,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Upgrade →",
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.5.sp,
                        color = Color.White.copy(alpha = 0.90f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Section 1: Home & Lock Screen Widgets ───────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "Home & Lock Screen Widgets",
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = Espresso
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Keep living declarations on your home screen and lock screen glance.",
                    fontFamily = BodyFontFamily,
                    fontSize = 13.sp,
                    color = StoneMuted
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, RoundedCornerShape(20.dp), spotColor = Espresso.copy(alpha = 0.03f))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Surface)
                        .border(0.5.dp, BorderSubtle, RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Widget Status Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(PorcelainWarm),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Widgets,
                                        contentDescription = null,
                                        tint = Espresso,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Makarios Glance Widget",
                                            fontFamily = BodyFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.5.sp,
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
                                        text = "On · Up to date",
                                        fontFamily = BodyFontFamily,
                                        fontSize = 11.5.sp,
                                        color = StoneMuted
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    WidgetHelper.pinWidgetToHomeScreen(context)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Terracotta,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Add Widget", fontFamily = BodyFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp)
                            }
                        }

                        Divider(color = Border, thickness = 0.75.dp)

                        // Widget Source Selector
                        Column {
                            Text(
                                text = "WIDGET SHOWS",
                                fontFamily = BodyFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp,
                                letterSpacing = 1.4.sp,
                                color = StoneMuted
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Declaration of the Day", "Saved Only", "Spiritual Focus").forEach { source ->
                                    val isSelected = selectedWidgetSource == source
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) Espresso else PorcelainWarm.copy(alpha = 0.5f))
                                            .clickable { selectedWidgetSource = source }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = source,
                                            fontFamily = BodyFontFamily,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            fontSize = 11.sp,
                                            color = if (isSelected) Color.White else Espresso
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Section 2: Gentle Notifications & Reminders ─────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "Gentle Notifications",
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Espresso
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Quiet reminders to anchor your mind in God's promises throughout the day.",
                    fontFamily = BodyFontFamily,
                    fontSize = 13.sp,
                    color = StoneMuted
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, RoundedCornerShape(20.dp), spotColor = Espresso.copy(alpha = 0.03f))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Surface)
                        .border(0.5.dp, BorderSubtle, RoundedCornerShape(20.dp))
                        .padding(horizontal = 18.dp, vertical = 6.dp)
                ) {
                    Column {
                        ReminderSwitchRow(
                            title = "Dawn Revelation",
                            subtitle = "06:30 AM · Morning declaration",
                            checked = dawnNotification,
                            onCheckedChange = {
                                dawnNotification = it
                                ReminderManager.setDawnEnabled(context, it)
                                if (it) Toast.makeText(context, "Dawn reminder set for 6:30 AM", Toast.LENGTH_SHORT).show()
                            }
                        )
                        Divider(color = BorderSubtle, thickness = 0.5.dp)
                        ReminderSwitchRow(
                            title = "Midday Stillness",
                            subtitle = "12:30 PM · Peace in the afternoon",
                            checked = middayNotification,
                            onCheckedChange = {
                                middayNotification = it
                                ReminderManager.setMiddayEnabled(context, it)
                                if (it) Toast.makeText(context, "Midday reminder set for 12:30 PM", Toast.LENGTH_SHORT).show()
                            }
                        )
                        Divider(color = BorderSubtle, thickness = 0.5.dp)
                        ReminderSwitchRow(
                            title = "Evening Examen",
                            subtitle = "08:30 PM · Wind down with scripture",
                            checked = eveningNotification,
                            onCheckedChange = {
                                eveningNotification = it
                                ReminderManager.setEveningEnabled(context, it)
                                if (it) Toast.makeText(context, "Evening reminder set for 8:30 PM", Toast.LENGTH_SHORT).show()
                            }
                        )
                        Divider(color = BorderSubtle, thickness = 0.5.dp)
                        ReminderSwitchRow(
                            title = "Hourly Truth & Peace",
                            subtitle = "Every hour · A gentle scripture reminder",
                            checked = hourlyNotification,
                            onCheckedChange = {
                                hourlyNotification = it
                                ReminderManager.setHourlyEnabled(context, it)
                                if (it) Toast.makeText(context, "Hourly reminders turned on", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Instant Test Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            ReminderManager.sendTestNotification(context, isHourly = false)
                            Toast.makeText(context, "Sent! Check your notifications.", Toast.LENGTH_SHORT).show()
                        },
                        border = BorderStroke(1.dp, Border),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = Terracotta, modifier = Modifier.size(15.dp))
                            Text("Test Daily", fontFamily = BodyFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = Espresso)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            ReminderManager.sendTestNotification(context, isHourly = true)
                            Toast.makeText(context, "Sent! Check your notifications.", Toast.LENGTH_SHORT).show()
                        },
                        border = BorderStroke(1.dp, Border),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = AmberGold, modifier = Modifier.size(15.dp))
                            Text("Test Hourly", fontFamily = BodyFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = Espresso)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Section 3: Spiritual Season Focus ───────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "Current Spiritual Focus",
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = Espresso
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Sets the primary theme for your daily declarations and widgets.",
                    fontFamily = BodyFontFamily,
                    fontSize = 13.sp,
                    color = StoneMuted
                )

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, RoundedCornerShape(20.dp), spotColor = Espresso.copy(alpha = 0.03f))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Surface)
                        .border(0.5.dp, BorderSubtle, RoundedCornerShape(20.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "Peace over Anxiety" to "PHILIPPIANS 4:7 · Guarding hearts and minds in Christ",
                        "Confidence & Calling" to "HEBREWS 13:6 · The Lord is my helper; I will not fear",
                        "Rest & Renewal" to "MATTHEW 11:28 · Come to me, all who are weary",
                        "Divine Provision" to "PHILIPPIANS 4:19 · Meeting every need in glory"
                    ).forEach { (season, verse) ->
                        val isSelected = selectedSeason == season
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) PorcelainWarm else Color.Transparent)
                                .clickable { selectedSeason = season }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = season,
                                    fontFamily = BodyFontFamily,
                                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                    fontSize = 13.5.sp,
                                    color = Espresso
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = verse,
                                    fontFamily = BodyFontFamily,
                                    fontSize = 11.5.sp,
                                    color = if (isSelected) Terracotta else StoneMuted
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

            Spacer(modifier = Modifier.height(20.dp))

            // ── Section 4: Welcome Journey ──────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .shadow(1.dp, RoundedCornerShape(16.dp), spotColor = Espresso.copy(alpha = 0.03f))
                    .clip(RoundedCornerShape(16.dp))
                    .background(Surface)
                    .border(0.5.dp, BorderSubtle, RoundedCornerShape(16.dp))
                    .clickable(onClick = onRevisitOnboarding)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Welcome Journey",
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.5.sp,
                            color = Espresso
                        )
                        Text(
                            text = "Revisit the 4-step onboarding introduction",
                            fontFamily = BodyFontFamily,
                            fontSize = 11.5.sp,
                            color = StoneMuted
                        )
                    }

                    Text(
                        text = "View →",
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Terracotta
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Section 5: Brand Stamp ──────────────────────────────
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
private fun ReminderSwitchRow(
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
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.5.sp,
                color = Espresso
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontFamily = BodyFontFamily,
                fontSize = 11.5.sp,
                color = StoneMuted
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Surface,
                checkedTrackColor = Terracotta,
                uncheckedTrackColor = PorcelainWarm
            )
        )
    }
}
