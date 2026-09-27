package com.makarios.app.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.ui.theme.*
import com.makarios.app.util.ReminderManager
import com.makarios.app.util.ReminderManager.ReminderSource
import com.makarios.app.util.WidgetHelper

@Composable
fun ProfileScreen(
    onNavigateToWidgets: () -> Unit,
    onRevisitOnboarding: () -> Unit = {},
    onNavigateToSubscription: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // User name state
    var userName by remember { mutableStateOf(ReminderManager.getUserName(context)) }
    var showNameDialog by remember { mutableStateOf(false) }

    // Notification timing states backed by ReminderManager
    var dawnNotification by remember { mutableStateOf(ReminderManager.isDawnEnabled(context)) }
    var middayNotification by remember { mutableStateOf(ReminderManager.isMiddayEnabled(context)) }
    var eveningNotification by remember { mutableStateOf(ReminderManager.isEveningEnabled(context)) }
    var hourlyNotification by remember { mutableStateOf(ReminderManager.isHourlyEnabled(context)) }

    // Reminder declaration pool source (Custom, Saved, Focus, All)
    var selectedReminderSource by remember { mutableStateOf(ReminderManager.getReminderSource(context)) }

    // Active spiritual focus season
    var selectedSeason by remember { mutableStateOf(ReminderManager.getActiveSeason(context)) }

    // Widget states
    var selectedWidgetSource by remember { mutableStateOf("Declaration of the Day") }

    // Entrance animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // Reactive counts
    val savedCount = AffirmationRepository.getSaved().size
    val personalCount = AffirmationRepository.personalAffirmations.size
    val totalCount = AffirmationRepository.getAll().size

    // Preview affirmation for currently selected reminder source
    val currentPreviewAffirmation = remember(selectedReminderSource, selectedSeason, savedCount, personalCount) {
        ReminderManager.resolveAffirmationForReminder(context)
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
                .padding(bottom = 48.dp)
        ) {

            // ── 1. Sacred Identity Card ─────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(380)) + slideInVertically(tween(380)) { -14 }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(24.dp), spotColor = Espresso.copy(alpha = 0.04f))
                            .clip(RoundedCornerShape(24.dp))
                            .background(Surface)
                            .border(0.5.dp, BorderSubtle, RoundedCornerShape(24.dp))
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Monogram Avatar
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.radialGradient(
                                                    colors = listOf(
                                                        PorcelainWarm,
                                                        Color(0xFFE8E0D4)
                                                    )
                                                )
                                            )
                                            .border(1.dp, Border, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = userName.take(1).uppercase(),
                                            fontFamily = DisplayFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 24.sp,
                                            color = Espresso
                                        )
                                    }

                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = userName,
                                                fontFamily = DisplayFontFamily,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 22.sp,
                                                letterSpacing = (-0.3).sp,
                                                color = Espresso
                                            )
                                            IconButton(
                                                onClick = { showNameDialog = true },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Edit name",
                                                    tint = StoneMuted,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "Anchored in sovereign truth & peace",
                                            fontFamily = BodyFontFamily,
                                            fontSize = 12.5.sp,
                                            color = Stone
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Spiritual Cadence Summary Bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(PorcelainWarm.copy(alpha = 0.5f))
                                    .padding(vertical = 10.dp, horizontal = 14.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatCadenceItem(count = "$savedCount", label = "Saved")
                                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Border))
                                StatCadenceItem(count = "$personalCount", label = "Authored")
                                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Border))
                                StatCadenceItem(
                                    count = selectedSeason.substringBefore(" over").substringBefore(" &").substringBefore(" in"),
                                    label = "Season"
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── 2. Makarios+ Membership (Quiet Luxury) ───────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(420, delayMillis = 60)) + slideInVertically(tween(420, delayMillis = 60)) { 14 }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .shadow(3.dp, RoundedCornerShape(22.dp), spotColor = AmberGold.copy(alpha = 0.12f))
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF2C241F),
                                    Color(0xFF1E1714)
                                )
                            )
                        )
                        .border(1.dp, AmberGold.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
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
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    letterSpacing = 2.sp,
                                    color = SunlitGold
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SunlitGold.copy(alpha = 0.18f))
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "SACRED EDITION",
                                        fontFamily = BodyFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 8.5.sp,
                                        letterSpacing = 1.sp,
                                        color = SunlitGold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Unlimited declarations · 4K wallpapers · All glance themes",
                                fontFamily = BodyFontFamily,
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SunlitGold.copy(alpha = 0.20f))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Explore →",
                                fontFamily = BodyFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = SunlitGold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── 3. Sacred Reminders & Custom Affirmation Delivery ────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(460, delayMillis = 100)) + slideInVertically(tween(460, delayMillis = 100)) { 16 }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = "Sacred Reminders",
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        letterSpacing = (-0.2).sp,
                        color = Espresso
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Choose which declarations arrive on your lock screen and notification tray.",
                        fontFamily = BodyFontFamily,
                        fontSize = 12.5.sp,
                        color = Stone
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // ── REMINDER SOURCE PICKER (Custom, Saved, Focus, All) ──
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(1.dp, RoundedCornerShape(22.dp), spotColor = Espresso.copy(alpha = 0.03f))
                            .clip(RoundedCornerShape(22.dp))
                            .background(Surface)
                            .border(0.5.dp, BorderSubtle, RoundedCornerShape(22.dp))
                            .padding(18.dp)
                    ) {
                        Column {
                            Text(
                                text = "DELIVERY SOURCE",
                                fontFamily = BodyFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp,
                                letterSpacing = 1.4.sp,
                                color = StoneMuted
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // 4 Source Options
                            val sources = listOf(
                                ReminderSourceOption(
                                    source = ReminderSource.CUSTOM,
                                    title = "Personal Declarations",
                                    subtitle = "Delivers affirmations you authored",
                                    pill = if (personalCount > 0) "$personalCount authored" else "None yet"
                                ),
                                ReminderSourceOption(
                                    source = ReminderSource.SAVED,
                                    title = "Saved Declarations",
                                    subtitle = "Delivers your bookmarked promises",
                                    pill = "$savedCount saved"
                                ),
                                ReminderSourceOption(
                                    source = ReminderSource.FOCUS,
                                    title = "Current Spiritual Focus",
                                    subtitle = "Anchored to $selectedSeason",
                                    pill = "Focus theme"
                                ),
                                ReminderSourceOption(
                                    source = ReminderSource.ALL,
                                    title = "Any Scripture & Truth",
                                    subtitle = "Rotating across the entire biblical library",
                                    pill = "$totalCount total"
                                )
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                sources.forEach { opt ->
                                    val isSelected = selectedReminderSource == opt.source
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(if (isSelected) PorcelainWarm else Color.Transparent)
                                            .border(
                                                width = if (isSelected) 1.dp else 0.5.dp,
                                                color = if (isSelected) Terracotta.copy(alpha = 0.4f) else BorderSubtle,
                                                shape = RoundedCornerShape(14.dp)
                                            )
                                            .clickable {
                                                selectedReminderSource = opt.source
                                                ReminderManager.setReminderSource(context, opt.source)
                                            }
                                            .padding(horizontal = 14.dp, vertical = 12.dp)
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
                                                        text = opt.title,
                                                        fontFamily = BodyFontFamily,
                                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                                        fontSize = 13.5.sp,
                                                        color = Espresso
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(
                                                                if (isSelected) TerracottaLight else PorcelainWarm
                                                            )
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = opt.pill,
                                                            fontFamily = BodyFontFamily,
                                                            fontWeight = FontWeight.Medium,
                                                            fontSize = 9.sp,
                                                            color = if (isSelected) Terracotta else StoneMuted
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = opt.subtitle,
                                                    fontFamily = BodyFontFamily,
                                                    fontSize = 11.5.sp,
                                                    color = Stone
                                                )
                                            }

                                            if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(22.dp)
                                                        .clip(CircleShape)
                                                        .background(Terracotta),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Active pool preview
                            Spacer(modifier = Modifier.height(14.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PorcelainWarm.copy(alpha = 0.6f))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "NEXT PREVIEW",
                                            fontFamily = BodyFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 9.sp,
                                            letterSpacing = 1.2.sp,
                                            color = Terracotta
                                        )
                                        Text(
                                            text = currentPreviewAffirmation.reference.uppercase(),
                                            fontFamily = BodyFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 9.sp,
                                            color = StoneMuted
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "“${currentPreviewAffirmation.declaration}”",
                                        fontFamily = DisplayFontFamily,
                                        fontStyle = FontStyle.Italic,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp,
                                        color = Espresso,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── SCHEDULE SWITCHES ──
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(1.dp, RoundedCornerShape(22.dp), spotColor = Espresso.copy(alpha = 0.03f))
                            .clip(RoundedCornerShape(22.dp))
                            .background(Surface)
                            .border(0.5.dp, BorderSubtle, RoundedCornerShape(22.dp))
                            .padding(horizontal = 18.dp, vertical = 6.dp)
                    ) {
                        Column {
                            ReminderSwitchRow(
                                title = "Dawn Revelation",
                                subtitle = "06:30 AM · Morning prayer & truth",
                                checked = dawnNotification,
                                onCheckedChange = {
                                    dawnNotification = it
                                    ReminderManager.setDawnEnabled(context, it)
                                    if (it) Toast.makeText(context, "Dawn reminder scheduled for 6:30 AM ✓", Toast.LENGTH_SHORT).show()
                                }
                            )
                            HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
                            ReminderSwitchRow(
                                title = "Midday Stillness",
                                subtitle = "12:30 PM · Rest amid afternoon work",
                                checked = middayNotification,
                                onCheckedChange = {
                                    middayNotification = it
                                    ReminderManager.setMiddayEnabled(context, it)
                                    if (it) Toast.makeText(context, "Midday reminder scheduled for 12:30 PM ✓", Toast.LENGTH_SHORT).show()
                                }
                            )
                            HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
                            ReminderSwitchRow(
                                title = "Evening Examen",
                                subtitle = "08:30 PM · Night contemplation & peace",
                                checked = eveningNotification,
                                onCheckedChange = {
                                    eveningNotification = it
                                    ReminderManager.setEveningEnabled(context, it)
                                    if (it) Toast.makeText(context, "Evening reminder scheduled for 8:30 PM ✓", Toast.LENGTH_SHORT).show()
                                }
                            )
                            HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
                            ReminderSwitchRow(
                                title = "Hourly Stillness",
                                subtitle = "Gentle hourly breath of Scripture",
                                checked = hourlyNotification,
                                onCheckedChange = {
                                    hourlyNotification = it
                                    ReminderManager.setHourlyEnabled(context, it)
                                    if (it) Toast.makeText(context, "Hourly reminders active ✓", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Test Action Pill (Single elegant trigger)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Surface)
                            .border(0.5.dp, BorderSubtle, RoundedCornerShape(14.dp))
                            .clickable {
                                ReminderManager.sendTestNotification(context, isHourly = false)
                                Toast.makeText(
                                    context,
                                    "Delivered ${currentPreviewAffirmation.reference} to your notification tray ✓",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = Terracotta,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Send test notification now",
                                fontFamily = BodyFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = Espresso
                            )
                        }

                        Text(
                            text = "Test →",
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = Terracotta
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── 4. Spiritual Focus Theme ────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(480, delayMillis = 140)) + slideInVertically(tween(480, delayMillis = 140)) { 18 }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = "Current Spiritual Focus",
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        letterSpacing = (-0.2).sp,
                        color = Espresso
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Sets your prayer theme across daily reminders and widgets.",
                        fontFamily = BodyFontFamily,
                        fontSize = 12.5.sp,
                        color = Stone
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(1.dp, RoundedCornerShape(22.dp), spotColor = Espresso.copy(alpha = 0.03f))
                            .clip(RoundedCornerShape(22.dp))
                            .background(Surface)
                            .border(0.5.dp, BorderSubtle, RoundedCornerShape(22.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Peace over Anxiety" to "PHILIPPIANS 4:7 · Guarding hearts and minds in Christ",
                            "Confidence & Calling" to "HEBREWS 13:6 · The Lord is my helper; I will not fear",
                            "Rest & Renewal" to "MATTHEW 11:28 · Come to me, all who are weary",
                            "Divine Provision" to "PHILIPPIANS 4:19 · Meeting every need in glory",
                            "Strength in Weakness" to "2 CORINTHIANS 12:9 · Power made perfect"
                        ).forEach { (season, verse) ->
                            val isSelected = selectedSeason == season
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) PorcelainWarm else Color.Transparent)
                                    .clickable {
                                        selectedSeason = season
                                        ReminderManager.setActiveSeason(context, season)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = season,
                                        fontFamily = BodyFontFamily,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        fontSize = 13.5.sp,
                                        color = Espresso
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = verse,
                                        fontFamily = BodyFontFamily,
                                        fontSize = 11.sp,
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
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── 5. Home Screen & Lock Screen Widgets ─────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500, delayMillis = 180)) + slideInVertically(tween(500, delayMillis = 180)) { 20 }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = "Phone Widgets",
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        letterSpacing = (-0.2).sp,
                        color = Espresso
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Pin biblical declarations to your Android Home Screen glance.",
                        fontFamily = BodyFontFamily,
                        fontSize = 12.5.sp,
                        color = Stone
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(1.dp, RoundedCornerShape(22.dp), spotColor = Espresso.copy(alpha = 0.03f))
                            .clip(RoundedCornerShape(22.dp))
                            .background(Surface)
                            .border(0.5.dp, BorderSubtle, RoundedCornerShape(22.dp))
                            .padding(18.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
                                                text = "Jetpack Glance Widget",
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
                                            text = "Active · Auto-refreshes daily",
                                            fontFamily = BodyFontFamily,
                                            fontSize = 11.5.sp,
                                            color = StoneMuted
                                        )
                                    }
                                }

                                Button(
                                    onClick = { WidgetHelper.pinWidgetToHomeScreen(context) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Espresso,
                                        contentColor = Surface
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("Add Widget", fontFamily = BodyFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                                }
                            }

                            HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)

                            // Widget Source Selection
                            Column {
                                Text(
                                    text = "WIDGET ROTATION",
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
                                    listOf("Truth of Day", "Saved Only", "Personal", "Spiritual Focus").forEach { src ->
                                        val isSelected = selectedWidgetSource == src
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) Espresso else PorcelainWarm.copy(alpha = 0.5f))
                                                .clickable { selectedWidgetSource = src }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = src,
                                                fontFamily = BodyFontFamily,
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                                fontSize = 10.5.sp,
                                                color = if (isSelected) Color.White else Espresso
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── 6. Welcome Journey (Safe Onboarding Entry) ───────────
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
                            text = "Revisit the 3-step sacred onboarding",
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

            // ── 7. Sacred Signature Stamp ────────────────────────────
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
                    fontSize = 13.sp,
                    letterSpacing = 4.sp,
                    color = Espresso
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Biblical declarations for everyday life",
                    fontFamily = BodyFontFamily,
                    fontSize = 12.sp,
                    color = StoneMuted
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Version 1.0.0 · Offline Ready",
                    fontFamily = BodyFontFamily,
                    fontSize = 10.5.sp,
                    color = StoneMuted.copy(alpha = 0.7f)
                )
            }
        }
    }

    // ── Edit Name Dialog ─────────────────────────────────────────────
    if (showNameDialog) {
        var tempName by remember { mutableStateOf(userName) }
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = {
                Text(
                    text = "Your Name",
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = Espresso
                )
            },
            text = {
                Column {
                    Text(
                        text = "How would you like Makarios to greet you each morning?",
                        fontFamily = BodyFontFamily,
                        fontSize = 13.sp,
                        color = Stone
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Terracotta,
                            unfocusedBorderColor = Border
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = tempName.trim()
                        if (trimmed.isNotEmpty()) {
                            userName = trimmed
                            ReminderManager.setUserName(context, trimmed)
                        }
                        showNameDialog = false
                    }
                ) {
                    Text("Save", fontFamily = BodyFontFamily, fontWeight = FontWeight.SemiBold, color = Terracotta)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) {
                    Text("Cancel", fontFamily = BodyFontFamily, color = Stone)
                }
            },
            containerColor = Surface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Components & Helpers
// ─────────────────────────────────────────────────────────────────────────────

private data class ReminderSourceOption(
    val source: ReminderSource,
    val title: String,
    val subtitle: String,
    val pill: String
)

@Composable
private fun StatCadenceItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            fontFamily = DisplayFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            color = Espresso
        )
        Text(
            text = label.uppercase(),
            fontFamily = BodyFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 9.sp,
            letterSpacing = 1.sp,
            color = StoneMuted
        )
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
