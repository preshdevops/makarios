package com.makarios.app.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // User name
    var userName by remember { mutableStateOf(ReminderManager.getUserName(context)) }
    var showNameDialog by remember { mutableStateOf(false) }

    // Clean daily reminder toggle (replaces rigid multi-time switches)
    var isReminderEnabled by remember { mutableStateOf(ReminderManager.isDailyReminderEnabled(context)) }

    // Reminder source (Pinned, Custom, Saved, All)
    var selectedReminderSource by remember { mutableStateOf(ReminderManager.getReminderSource(context)) }

    // Reactive counts
    val savedCount = AffirmationRepository.getSaved().size
    val personalCount = AffirmationRepository.personalAffirmations.size
    val totalCount = AffirmationRepository.getAll().size

    // Preview affirmation for currently selected reminder source
    val currentPreviewAffirmation = remember(selectedReminderSource, savedCount, personalCount) {
        ReminderManager.resolveAffirmationForReminder(context)
    }

    // Active widget affirmation
    val activeWidgetAffirmation = AffirmationRepository.widgetAffirmation

    // Staggered entrance animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

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
                                            text = "Walking in sovereign stillness & grace",
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
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatCadenceItem(count = "$savedCount", label = "Saved Promises")
                                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Border))
                                StatCadenceItem(count = "$personalCount", label = "Authored")
                                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Border))
                                StatCadenceItem(count = "100% Free", label = "No Subscription")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── 2. Daily Reminders (Clean, dignified, zero-bs) ─────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(440, delayMillis = 80)) + slideInVertically(tween(440, delayMillis = 80)) { 16 }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = "Daily Scripture Reminders",
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        letterSpacing = (-0.2).sp,
                        color = Espresso
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Receive a quiet, grounding declaration on your lock screen.",
                        fontFamily = BodyFontFamily,
                        fontSize = 12.5.sp,
                        color = Stone
                    )

                    Spacer(modifier = Modifier.height(12.dp))

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
                            // Master Toggle Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Daily Notification",
                                        fontFamily = BodyFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = Espresso
                                    )
                                    Text(
                                        text = if (isReminderEnabled) "Active · Delivered gently each morning" else "Turned off",
                                        fontFamily = BodyFontFamily,
                                        fontSize = 12.sp,
                                        color = if (isReminderEnabled) Sage else StoneMuted
                                    )
                                }

                                Switch(
                                    checked = isReminderEnabled,
                                    onCheckedChange = {
                                        isReminderEnabled = it
                                        ReminderManager.setDailyReminderEnabled(context, it)
                                        if (it) Toast.makeText(context, "Daily reminders active ✓", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Surface,
                                        checkedTrackColor = Terracotta,
                                        uncheckedTrackColor = PorcelainWarm
                                    )
                                )
                            }

                            if (isReminderEnabled) {
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "DELIVERY SOURCE",
                                    fontFamily = BodyFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.4.sp,
                                    color = StoneMuted
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                val sources = listOf(
                                    ReminderSourceOption(
                                        source = ReminderSource.PINNED,
                                        title = "Pinned Affirmation",
                                        subtitle = "The exact affirmation you selected on any card",
                                        pill = if (ReminderManager.getPinnedAffirmation(context) != null) "Selected" else "Default"
                                    ),
                                    ReminderSourceOption(
                                        source = ReminderSource.CUSTOM,
                                        title = "Personal Declarations",
                                        subtitle = "Affirmations you authored in Create",
                                        pill = if (personalCount > 0) "$personalCount authored" else "None yet"
                                    ),
                                    ReminderSourceOption(
                                        source = ReminderSource.SAVED,
                                        title = "Saved Declarations",
                                        subtitle = "Your bookmarked favorite promises",
                                        pill = "$savedCount saved"
                                    ),
                                    ReminderSourceOption(
                                        source = ReminderSource.ALL,
                                        title = "Any Scripture & Truth",
                                        subtitle = "Rotates across all biblical themes & promises",
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
                                                            .size(20.dp)
                                                            .clip(CircleShape)
                                                            .background(Terracotta),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = null,
                                                            tint = Color.White,
                                                            modifier = Modifier.size(12.dp)
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
                                                text = "NEXT DELIVERY PREVIEW",
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
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Test notification pill
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
                                    "Delivered “${currentPreviewAffirmation.reference}” to notifications ✓",
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

            // ── 3. Home Screen Widget ───────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(480, delayMillis = 120)) + slideInVertically(tween(480, delayMillis = 120)) { 18 }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = "Home Screen Widget",
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        letterSpacing = (-0.2).sp,
                        color = Espresso
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Pin any affirmation directly to your home screen glance.",
                        fontFamily = BodyFontFamily,
                        fontSize = 12.5.sp,
                        color = Stone
                    )

                    Spacer(modifier = Modifier.height(12.dp))

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
                                            text = "Ready · Tap any card in app to pin",
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
                                    Text("Add to Screen", fontFamily = BodyFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                                }
                            }

                            HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)

                            // Active widget declaration preview
                            Column {
                                Text(
                                    text = "CURRENT WIDGET DECLARATION",
                                    fontFamily = BodyFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 9.5.sp,
                                    letterSpacing = 1.3.sp,
                                    color = StoneMuted
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "“${activeWidgetAffirmation.declaration}”",
                                    fontFamily = DisplayFontFamily,
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    color = Espresso
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = activeWidgetAffirmation.reference.uppercase(),
                                    fontFamily = BodyFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.sp,
                                    color = Terracotta
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── 4. Welcome Journey ──────────────────────────────────
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

            // ── 5. Free & Sacred Stamp ──────────────────────────────
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
                    text = "100% Free · No Subscriptions · No Ads",
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Sage
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Biblical declarations for everyday life · v1.0.0",
                    fontFamily = BodyFontFamily,
                    fontSize = 11.sp,
                    color = StoneMuted
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
            fontSize = 16.sp,
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
