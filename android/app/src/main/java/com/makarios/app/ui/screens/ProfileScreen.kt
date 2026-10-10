package com.makarios.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.data.AuthManager
import com.makarios.app.ui.components.AuthDialog
import com.makarios.app.ui.components.FindFriendsDialog
import com.makarios.app.ui.components.SacredTimePickerDialog
import com.makarios.app.ui.theme.*
import com.makarios.app.widget.WidgetStore
import com.makarios.app.util.ReminderManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onSignOut: () -> Unit,
    onOpenWallpapers: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLight = remember { getCurrentLightForTime() }
    var widgetLight by remember { mutableStateOf(WidgetStore.fixedLight(context)) }

    // Dialog visibility states
    var showAuthDialog by remember { mutableStateOf(false) }
    var isAuthSignUpMode by remember { mutableStateOf(false) }
    var showFindFriendsDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }

    // Auth state from AuthManager
    val user = AuthManager.currentUser
    val isGuest = AuthManager.isAnonymous || user == null
    val displayName = AuthManager.displayName
    val emailText = user?.email ?: if (isGuest) "Guest Account (Local)" else "No email connected"

    // Reminder state
    var dailyDeclEnabled by remember {
        mutableStateOf(ReminderManager.isDailyReminderEnabled(context))
    }
    var reminderHour by remember {
        mutableStateOf(ReminderManager.getReminderHour(context))
    }
    var reminderMinute by remember {
        mutableStateOf(ReminderManager.getReminderMinute(context))
    }
    var formattedReminderTime by remember(reminderHour, reminderMinute) {
        mutableStateOf(ReminderManager.formatTime(reminderHour, reminderMinute))
    }

    val inviteCode = remember(user) {
        val uid = user?.uid ?: "XR79K"
        if (uid.length >= 5) uid.take(5).uppercase() else "XR79K"
    }

    val keptCount = AffirmationRepository.getSaved().size
    val writtenCount = AffirmationRepository.personalAffirmations.size
    val totalCount = AffirmationRepository.getAll().size

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFBF9F5))
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp, bottom = 100.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start
    ) {
        Text("You", style = MakariosTypography.displayLarge, color = Ink)

        Spacer(modifier = Modifier.height(32.dp))

        // Monogram & Profile Information
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .lightBackground(currentLight),
                contentAlignment = Alignment.Center
            ) {
                val initial = if (isGuest) "M" else displayName.firstOrNull()?.uppercase() ?: "M"
                Text(
                    text = initial,
                    style = MakariosTypography.displayMedium.copy(fontSize = 32.sp),
                    color = currentLight.text
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    style = MakariosTypography.displaySmall,
                    color = Ink
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = emailText,
                    style = MakariosTypography.bodyMedium,
                    color = Ink.copy(alpha = 0.65f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Sign In / Edit Profile Action
        if (isGuest) {
            Button(
                onClick = {
                    isAuthSignUpMode = true
                    showAuthDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Cream)
            ) {
                Text("Sign In / Join to Sync Across Devices", style = MakariosTypography.labelLarge)
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        isAuthSignUpMode = false
                        showAuthDialog = true
                    },
                    modifier = Modifier.height(40.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink),
                    border = BorderStroke(1.dp, Ink.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Account Settings", style = MakariosTypography.labelLarge)
                }

                OutlinedButton(
                    onClick = {
                        AuthManager.signOut()
                        onSignOut()
                        Toast.makeText(context, "Signed out successfully", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.height(40.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink.copy(alpha = 0.7f)),
                    border = BorderStroke(1.dp, Ink.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Sign out", style = MakariosTypography.labelLarge)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Lining figures count badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Ink.copy(alpha = 0.05f))
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = "$keptCount kept  Â·  $writtenCount written  Â·  $totalCount in library",
                style = MakariosTypography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = Ink
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Invite a friend section
        Text("Invite a friend", style = MakariosTypography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = Ink)
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Ink.copy(alpha = 0.06f))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = inviteCode,
                    style = MakariosTypography.bodyMedium.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = Ink
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Makarios Invite Code", inviteCode)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Invite code $inviteCode copied!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.height(44.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink),
                    border = BorderStroke(1.dp, Ink.copy(alpha = 0.2f))
                ) {
                    Text("Copy", style = MakariosTypography.labelLarge)
                }

                Button(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Join me in speaking God's truth daily on Makarios. Use my invite code: $inviteCode https://makarios.app/join/$inviteCode"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Invite a friend"))
                    },
                    modifier = Modifier.height(44.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Cream)
                ) {
                    Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share link", style = MakariosTypography.labelLarge)
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Your circle section
        Text("Your circle", style = MakariosTypography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = Ink)
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Light.Dusk.top))
                Box(modifier = Modifier.offset(x = (-8).dp).size(36.dp).clip(CircleShape).background(Light.Mist.top))
                Box(
                    modifier = Modifier
                        .offset(x = (-16).dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Light.Dawn.top),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+2", style = MakariosTypography.labelSmall, color = Ink)
                }
            }

            OutlinedButton(
                onClick = { showFindFriendsDialog = true },
                modifier = Modifier.height(40.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink),
                border = BorderStroke(1.dp, Ink.copy(alpha = 0.2f))
            ) {
                Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add a friend", style = MakariosTypography.labelLarge)
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Daily declaration reminder settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Daily declaration",
                    style = MakariosTypography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = Ink
                )
                Text(
                    "Receive daily biblical truth at your chosen hour",
                    style = MakariosTypography.labelSmall,
                    color = Ink.copy(alpha = 0.6f)
                )
            }

            Switch(
                checked = dailyDeclEnabled,
                onCheckedChange = { isEnabled ->
                    dailyDeclEnabled = isEnabled
                    ReminderManager.setDailyReminderEnabled(context, isEnabled)
                    Toast.makeText(
                        context,
                        if (isEnabled) "Daily reminders enabled" else "Daily reminders paused",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                colors = SwitchDefaults.colors(checkedThumbColor = Cream, checkedTrackColor = Ink)
            )
        }

        if (dailyDeclEnabled) {
            Spacer(modifier = Modifier.height(16.dp))

            // Clickable selected time display
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Ink.copy(alpha = 0.05f))
                    .clickable { showTimePickerDialog = true }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Notifications, contentDescription = null, tint = Ink, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = formattedReminderTime,
                    style = MakariosTypography.displaySmall,
                    color = Ink
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "(tap to change)",
                    style = MakariosTypography.labelSmall,
                    color = Ink.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Presets row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val presets = listOf(
                    Triple("Dawn", 6, 30),
                    Triple("Morning", 8, 30),
                    Triple("Midday", 12, 30),
                    Triple("Evening", 18, 0)
                )
                presets.forEach { (label, h, m) ->
                    val isCurrent = reminderHour == h && reminderMinute == m
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isCurrent) Ink else Ink.copy(alpha = 0.05f))
                            .clickable {
                                reminderHour = h
                                reminderMinute = m
                                formattedReminderTime = ReminderManager.formatTime(h, m)
                                ReminderManager.setReminderTime(context, h, m)
                                Toast.makeText(context, "Reminder set for $label (${ReminderManager.formatTime(h, m)})", Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$label\n${String.format("%02d:%02d", h, m)}",
                            style = MakariosTypography.labelSmall.copy(fontSize = 11.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center),
                            color = if (isCurrent) Cream else Ink
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Light system row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Atmospheric Light", style = MakariosTypography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = Ink)
                Text("Matches sunrise, midday, and twilight naturally", style = MakariosTypography.labelSmall, color = Ink.copy(alpha = 0.6f))
            }
            Text(
                text = currentLight.name,
                style = MakariosTypography.labelLarge,
                color = Ink
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        Text("Widget Light", style = MakariosTypography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = Ink)
        Text("Auto follows time of day, or fix a Light for your widgets", style = MakariosTypography.labelSmall, color = Ink.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(28.dp).clip(RoundedCornerShape(7.dp)).background(Ink).clickable { widgetLight = "AUTO"; WidgetStore.setLight(context, "AUTO"); com.makarios.app.widget.WidgetScheduling.refreshNow(context) })
            Light.values().forEach { candidate -> Box(Modifier.size(28.dp).clip(RoundedCornerShape(7.dp)).background(candidate.top).clickable { widgetLight = candidate.name; WidgetStore.setLight(context, candidate.name); com.makarios.app.widget.WidgetScheduling.refreshNow(context) }) }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Account status
        Text("Account & Storage", style = MakariosTypography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = Ink)
        Spacer(modifier = Modifier.height(8.dp))
        Text(emailText, style = MakariosTypography.bodyMedium, color = Ink.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4A5A3C)) // Green status dot
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isGuest) "Local storage active (sign in to sync)" else "Cloud sync active",
                style = MakariosTypography.labelSmall,
                color = Ink.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))
    }

    // Auth Dialog
    if (showAuthDialog) {
        AuthDialog(
            initialIsSignUp = isAuthSignUpMode,
            onDismiss = { showAuthDialog = false },
            onSuccess = {
                showAuthDialog = false
                Toast.makeText(context, "Welcome to Makarios!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Find Friends Dialog
    if (showFindFriendsDialog) {
        FindFriendsDialog(
            onDismiss = { showFindFriendsDialog = false }
        )
    }

    // Sacred Time Picker Dialog
    if (showTimePickerDialog) {
        SacredTimePickerDialog(
            initialHour = reminderHour,
            initialMinute = reminderMinute,
            onConfirm = { h, m ->
                reminderHour = h
                reminderMinute = m
                formattedReminderTime = ReminderManager.formatTime(h, m)
                ReminderManager.setReminderTime(context, h, m)
                showTimePickerDialog = false
                Toast.makeText(context, "Reminder time set to $formattedReminderTime", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showTimePickerDialog = false }
        )
    }
}





