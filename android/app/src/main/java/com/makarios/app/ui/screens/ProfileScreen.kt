package com.makarios.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.data.AuthManager
import com.makarios.app.data.CommunityRepository
import com.makarios.app.ui.components.AuthDialog
import com.makarios.app.ui.components.FindFriendsDialog
import com.makarios.app.ui.components.SacredTimePickerDialog
import com.makarios.app.ui.theme.*
import com.makarios.app.util.ReminderManager
import com.makarios.app.util.ReminderManager.ReminderSource
import com.makarios.app.util.UsernameValidator
import com.makarios.app.util.WidgetHelper

@Composable
fun ProfileScreen(
    onRevisitOnboarding: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Auth state
    var showAuthDialog by remember { mutableStateOf(false) }
    var authDialogIsSignUp by remember { mutableStateOf(true) }
    var showSignOutConfirm by remember { mutableStateOf(false) }

    // User name & username
    var userName by remember { mutableStateOf(ReminderManager.getUserName(context)) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showFindFriendsDialog by remember { mutableStateOf(false) }

    val currentProfile = CommunityRepository.currentProfile
    val friends = CommunityRepository.friendsList

    // Daily reminder toggle
    var isReminderEnabled by remember { mutableStateOf(ReminderManager.isDailyReminderEnabled(context)) }

    // Delivery time state
    var showTimePickerDialog by remember { mutableStateOf(false) }
    var reminderTimeText by remember { mutableStateOf(ReminderManager.getFormattedReminderTime(context)) }

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
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .padding(bottom = 56.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Top Status Bar: Small Green Synced Pill ──────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (AuthManager.isLoggedIn && !AuthManager.isAnonymous) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SageLight)
                            .border(0.5.dp, Sage.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Sage)
                            )
                            Text(
                                text = "Synced",
                                fontFamily = BodyFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.5.sp,
                                color = Sage
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(PorcelainWarm)
                            .border(0.5.dp, BorderSubtle, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(StoneMuted)
                            )
                            Text(
                                text = "Local Device",
                                fontFamily = BodyFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.5.sp,
                                color = StoneMuted
                            )
                        }
                    }
                }
            }

            // ── 1. Sacred Identity Card (Calm, Centered Hierarchy) ────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(380)) + slideInVertically(tween(380)) { -14 }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, RoundedCornerShape(22.dp), spotColor = Espresso.copy(alpha = 0.04f))
                        .clip(RoundedCornerShape(22.dp))
                        .background(Surface)
                        .border(0.5.dp, BorderSubtle, RoundedCornerShape(22.dp))
                        .padding(vertical = 24.dp, horizontal = 20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Centered Avatar (Larger, with edit badge)
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clickable { showEditProfileDialog = true }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                PorcelainWarm,
                                                Color(0xFFE8E0D4)
                                            )
                                        )
                                    )
                                    .border(1.dp, Border, CircleShape)
                                    .align(Alignment.Center),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userName.ifBlank { "F" }.take(1).uppercase(),
                                    fontFamily = DisplayFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 32.sp,
                                    color = Espresso
                                )
                            }

                            // Edit badge at bottom end
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Terracotta)
                                    .border(2.dp, Surface, CircleShape)
                                    .align(Alignment.BottomEnd),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit profile",
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Display name in Cormorant serif
                        Text(
                            text = userName.ifBlank { "Friend" },
                            fontFamily = DisplayFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 24.sp,
                            letterSpacing = (-0.3).sp,
                            color = Espresso,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        // @handle below it in terracotta
                        val handle = if (AuthManager.isLoggedIn && !AuthManager.isAnonymous && currentProfile != null && currentProfile.username.isNotBlank()) {
                            "@${currentProfile.username}"
                        } else {
                            "@member"
                        }
                        Text(
                            text = handle,
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.5.sp,
                            color = Terracotta,
                            textAlign = TextAlign.Center
                        )

                        // Soft prompt if name is still default "Friend" or handle is "@member"
                        val isDefaultIdentity = userName.isBlank() || userName.equals("Friend", ignoreCase = true) || handle == "@member"
                        if (isDefaultIdentity) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Add your name to make this yours",
                                fontFamily = BodyFontFamily,
                                fontStyle = FontStyle.Italic,
                                fontSize = 12.sp,
                                color = StoneMuted,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Single "Edit profile" text button (replaces separate edit pencils)
                        TextButton(
                            onClick = { showEditProfileDialog = true },
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text(
                                text = "Edit profile",
                                fontFamily = BodyFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = Espresso
                            )
                        }
                    }
                }
            }

            // ── 2. Stats Row (Clean Card with 3 Evenly Spaced Columns) ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, RoundedCornerShape(20.dp), spotColor = Espresso.copy(alpha = 0.03f))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Surface)
                    .border(0.5.dp, BorderSubtle, RoundedCornerShape(20.dp))
                    .padding(vertical = 18.dp, horizontal = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        StatCadenceItem(count = "$savedCount", label = "Saved")
                    }
                    Box(
                        modifier = Modifier
                            .width(0.5.dp)
                            .height(28.dp)
                            .background(BorderSubtle)
                    )
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        StatCadenceItem(count = "$personalCount", label = "Authored")
                    }
                    Box(
                        modifier = Modifier
                            .width(0.5.dp)
                            .height(28.dp)
                            .background(BorderSubtle)
                    )
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        StatCadenceItem(count = "$totalCount", label = "Total")
                    }
                }
            }

            // ── 3. Account Section Card ─────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, RoundedCornerShape(20.dp), spotColor = Espresso.copy(alpha = 0.03f))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Surface)
                    .border(0.5.dp, BorderSubtle, RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                if (AuthManager.isLoggedIn && !AuthManager.isAnonymous) {
                    // Signed in account view
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(SageLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cloud,
                                    contentDescription = null,
                                    tint = Sage,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Account Synced",
                                    fontFamily = BodyFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.5.sp,
                                    color = Espresso
                                )
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = AuthManager.userEmail?.ifBlank { "Cloud active" } ?: "Cloud active",
                                    fontFamily = BodyFontFamily,
                                    fontSize = 12.sp,
                                    color = Stone
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(SageLight)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Active",
                                fontFamily = BodyFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 10.5.sp,
                                color = Sage
                            )
                        }
                    }
                } else {
                    // Guest mode account view
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(PorcelainWarm),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cloud,
                                    contentDescription = null,
                                    tint = Terracotta,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Back Up Your Declarations",
                                    fontFamily = BodyFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.5.sp,
                                    color = Espresso
                                )
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = "Sign in to keep your declarations safe across all your devices",
                                    fontFamily = BodyFontFamily,
                                    fontSize = 11.5.sp,
                                    color = Stone
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    authDialogIsSignUp = true
                                    showAuthDialog = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Terracotta,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(44.dp)
                            ) {
                                Text(
                                    text = "Create Account",
                                    fontFamily = BodyFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.5.sp
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    authDialogIsSignUp = false
                                    showAuthDialog = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Border),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = Surface),
                                modifier = Modifier
                                    .weight(0.8f)
                                    .height(44.dp)
                            ) {
                                Text(
                                    text = "Sign In",
                                    fontFamily = BodyFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.5.sp,
                                    color = Espresso
                                )
                            }
                        }
                    }
                }
            }

            // ── 4. Invite Code Card ─────────────────────────────────
            val friendCode = currentProfile?.friendCode.orEmpty()
            if (AuthManager.isLoggedIn && !AuthManager.isAnonymous && friendCode.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, RoundedCornerShape(20.dp), spotColor = Espresso.copy(alpha = 0.03f))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Surface)
                        .border(0.5.dp, BorderSubtle, RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Text(
                            text = "Your Invite Code",
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Espresso
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Share with friends to connect and pray together.",
                            fontFamily = BodyFontFamily,
                            fontSize = 12.sp,
                            color = Stone
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Monospace Code Chip
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(PorcelainWarm)
                                .border(0.5.dp, Border, RoundedCornerShape(12.dp))
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = friendCode,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                letterSpacing = 3.sp,
                                color = Espresso
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Proper Buttons for Copy and Share
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Makarios Friend Code", friendCode)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Code copied: $friendCode ✓", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Border),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = Surface)
                            ) {
                                Text(
                                    text = "Copy Code",
                                    fontFamily = BodyFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.5.sp,
                                    color = Espresso
                                )
                            }

                            Button(
                                onClick = {
                                    val inviteLink = "https://makarios.app/friend/$friendCode"
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "Connect with me on Makarios to declare truth together: $inviteLink")
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share Invite Link"))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Espresso,
                                    contentColor = Surface
                                )
                            ) {
                                Text(
                                    text = "Share Link",
                                    fontFamily = BodyFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.5.sp
                                )
                            }
                        }
                    }
                }
            }

            // ── 5. Circle of Friends Card (Safe Wrapping + Avatar Stack) ──
            if (AuthManager.isLoggedIn && !AuthManager.isAnonymous) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, RoundedCornerShape(20.dp), spotColor = Espresso.copy(alpha = 0.03f))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Surface)
                        .border(0.5.dp, BorderSubtle, RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        // Header row with safe text wrapping and avatar stack on right
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = "Circle of Friends",
                                    fontFamily = BodyFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = Espresso
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (friends.isEmpty())
                                        "Connect by @username, email, or code"
                                    else
                                        "${friends.size} connected ${if (friends.size == 1) "friend" else "friends"}",
                                    fontFamily = BodyFontFamily,
                                    fontSize = 12.sp,
                                    color = Stone
                                )
                            }

                            // Avatar stack on right: up to 3 friends plus +N chip
                            Row(
                                horizontalArrangement = Arrangement.spacedBy((-8).dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (friends.isNotEmpty()) {
                                    val displayFriends = friends.take(3)
                                    displayFriends.forEach { friend ->
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(Sage)
                                                .border(1.5.dp, Surface, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = friend.displayName.take(1).uppercase(),
                                                fontFamily = DisplayFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color.White
                                            )
                                        }
                                    }
                                    if (friends.size > 3) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(PorcelainWarm)
                                                .border(1.5.dp, Surface, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "+${friends.size - 3}",
                                                fontFamily = BodyFontFamily,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 10.sp,
                                                color = Espresso
                                            )
                                        }
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(PorcelainWarm)
                                            .border(1.dp, BorderSubtle, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = StoneMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Connected friends list (chips)
                        if (friends.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                friends.forEach { friend ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(PorcelainWarm)
                                            .border(0.5.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .clip(CircleShape)
                                                    .background(Sage),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = friend.displayName.take(1).uppercase(),
                                                    fontFamily = DisplayFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 9.sp,
                                                    color = Color.White
                                                )
                                            }
                                            Text(
                                                text = if (friend.username.isNotBlank()) "@${friend.username}" else friend.displayName,
                                                fontFamily = BodyFontFamily,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 11.5.sp,
                                                color = Espresso
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Full-width "Add friend" button below
                        Button(
                            onClick = { showFindFriendsDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Espresso,
                                contentColor = Surface
                            )
                        ) {
                            Text(
                                text = "Add Friend",
                                fontFamily = BodyFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // ── 6. Daily Scripture Reminders (Grouped in One Card) ───
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(440, delayMillis = 80)) + slideInVertically(tween(440, delayMillis = 80)) { 16 }
            ) {
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
                        // Card Header
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

                        Spacer(modifier = Modifier.height(16.dp))

                        // Master Toggle Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
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

                            // Delivery Time Selector Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(PorcelainWarm.copy(alpha = 0.6f))
                                    .border(0.5.dp, BorderSubtle, RoundedCornerShape(14.dp))
                                    .clickable { showTimePickerDialog = true }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(TerracottaLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccessTime,
                                            contentDescription = null,
                                            tint = Terracotta,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Notification Time",
                                            fontFamily = BodyFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.5.sp,
                                            color = Espresso
                                        )
                                        Text(
                                            text = "Delivered daily at this time",
                                            fontFamily = BodyFontFamily,
                                            fontSize = 11.5.sp,
                                            color = Stone
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Terracotta)
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = reminderTimeText,
                                            fontFamily = BodyFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Column {
                                Text(
                                    text = "WHICH DECLARATIONS TO RECEIVE",
                                    fontFamily = BodyFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.4.sp,
                                    color = StoneMuted
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Select what kind of affirmations appear in your notifications.",
                                    fontFamily = BodyFontFamily,
                                    fontSize = 11.5.sp,
                                    color = Stone
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            val sources = listOf(
                                ReminderSourceOption(
                                    source = ReminderSource.CUSTOM,
                                    title = "My Created Affirmations",
                                    subtitle = "Words you personally wrote",
                                    pill = if (personalCount > 0) "$personalCount authored" else "None yet"
                                ),
                                ReminderSourceOption(
                                    source = ReminderSource.SAVED,
                                    title = "My Saved Favorites",
                                    subtitle = "Your bookmarked collection of biblical declarations",
                                    pill = "$savedCount saved"
                                ),
                                ReminderSourceOption(
                                    source = ReminderSource.ALL,
                                    title = "Daily Scripture Discovery",
                                    subtitle = "A fresh biblical truth rotated from the full library each day",
                                    pill = "$totalCount total"
                                ),
                                ReminderSourceOption(
                                    source = ReminderSource.PINNED,
                                    title = "Specific Pinned Declaration",
                                    subtitle = "Keep meditating continuously on a single chosen verse",
                                    pill = if (ReminderManager.getPinnedAffirmation(context) != null) "Selected" else "Default"
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
                                            text = "TOMORROW'S MESSAGE PREVIEW",
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

                            Spacer(modifier = Modifier.height(14.dp))

                            // Test notification button
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Surface)
                                    .border(0.5.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                    .clickable {
                                        ReminderManager.sendTestNotification(context, isHourly = false)
                                        Toast.makeText(
                                            context,
                                            "Sent a preview notification ✓",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .padding(horizontal = 14.dp, vertical = 11.dp),
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
                                        text = "Send a test notification",
                                        fontFamily = BodyFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.5.sp,
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
                }
            }

            // ── 7. Home Screen Widget Card ──────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(480, delayMillis = 120)) + slideInVertically(tween(480, delayMillis = 120)) { 18 }
            ) {
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
                                            text = "Home Screen Widget",
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
                                        text = "Pin declaration directly to your screen",
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
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("Add Widget", fontFamily = BodyFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                            }
                        }

                        HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)

                        Column {
                            Text(
                                text = "CURRENT DECLARATION",
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

            // ── 8. Welcome Journey ──────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
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
                            text = "Revisit the 3-step intro",
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

            // ── 9. Quiet Sign Out Text Button at Bottom ─────────────
            if (AuthManager.isLoggedIn && !AuthManager.isAnonymous) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(
                        onClick = { showSignOutConfirm = true },
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text(
                            text = "Sign Out",
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = StoneMuted
                        )
                    }
                }
            }

            // ── 10. Free & Sacred Stamp ─────────────────────────────
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
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

    // ── Edit Profile Dialog (Name & Handle Together) ─────────────────
    if (showEditProfileDialog) {
        var tempName by remember { mutableStateOf(userName) }
        var tempUsername by remember { mutableStateOf(currentProfile?.username.orEmpty()) }
        var usernameError by remember { mutableStateOf<String?>(null) }
        var isUpdating by remember { mutableStateOf(false) }

        val isAuthUser = AuthManager.isLoggedIn && !AuthManager.isAnonymous

        AlertDialog(
            onDismissRequest = { if (!isUpdating) showEditProfileDialog = false },
            title = {
                Text(
                    text = "Edit Profile",
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = Espresso
                )
            },
            text = {
                Column {
                    Text(
                        text = "Customize how Makarios greets you and how friends recognize you.",
                        fontFamily = BodyFontFamily,
                        fontSize = 13.sp,
                        color = Stone
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "YOUR NAME",
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        letterSpacing = 1.2.sp,
                        color = StoneMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        singleLine = true,
                        placeholder = { Text("e.g. David", color = StoneMuted) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Terracotta,
                            unfocusedBorderColor = Border
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (isAuthUser) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "COMMUNITY @HANDLE",
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            letterSpacing = 1.2.sp,
                            color = StoneMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = tempUsername,
                            onValueChange = { input ->
                                tempUsername = input
                                val validation = UsernameValidator.validate(input)
                                usernameError = if (validation is UsernameValidator.ValidationResult.Invalid) validation.message else null
                            },
                            prefix = {
                                Text("@", fontFamily = BodyFontFamily, fontWeight = FontWeight.SemiBold, color = Terracotta)
                            },
                            singleLine = true,
                            isError = usernameError != null,
                            supportingText = {
                                if (usernameError != null) {
                                    Text(usernameError!!, color = Terracotta, fontSize = 11.sp)
                                } else {
                                    Text("3-20 letters, numbers, and underscores", fontSize = 11.sp, color = StoneMuted)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Terracotta,
                                unfocusedBorderColor = Border
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !isUpdating && (usernameError == null || !isAuthUser),
                    onClick = {
                        val trimmedName = tempName.trim()
                        if (trimmedName.isNotEmpty()) {
                            userName = trimmedName
                            ReminderManager.setUserName(context, trimmedName)
                        }

                        if (isAuthUser && tempUsername.isNotBlank() && tempUsername != currentProfile?.username) {
                            isUpdating = true
                            CommunityRepository.updateUsername(
                                newUsername = tempUsername,
                                onSuccess = {
                                    isUpdating = false
                                    showEditProfileDialog = false
                                    Toast.makeText(context, "Profile updated ✓", Toast.LENGTH_SHORT).show()
                                },
                                onError = { err ->
                                    isUpdating = false
                                    usernameError = err
                                }
                            )
                        } else {
                            showEditProfileDialog = false
                            Toast.makeText(context, "Profile saved ✓", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text(
                        text = if (isUpdating) "Saving…" else "Save",
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = Terracotta
                    )
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isUpdating,
                    onClick = { showEditProfileDialog = false }
                ) {
                    Text("Cancel", fontFamily = BodyFontFamily, color = Stone)
                }
            },
            containerColor = Surface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // ── Delivery Time Picker Dialog ──────────────────────────────────
    if (showTimePickerDialog) {
        SacredTimePickerDialog(
            initialHour = ReminderManager.getReminderHour(context),
            initialMinute = ReminderManager.getReminderMinute(context),
            title = "Notification Time",
            subtitle = "Choose when you'd like your daily declaration to appear on your screen.",
            onConfirm = { hour, minute ->
                ReminderManager.setReminderTime(context, hour, minute)
                reminderTimeText = ReminderManager.getFormattedReminderTime(context)
                showTimePickerDialog = false
                Toast.makeText(context, "Reminders updated for $reminderTimeText ✓", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showTimePickerDialog = false }
        )
    }

    // ── Auth Dialog (Sign Up / Sign In / Guest) ───────────────────────
    if (showAuthDialog) {
        AuthDialog(
            initialIsSignUp = authDialogIsSignUp,
            onDismiss = { showAuthDialog = false },
            onSuccess = { showAuthDialog = false }
        )
    }

    // ── Sign Out Confirmation Dialog ──────────────────────────────────
    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            title = {
                Text(
                    text = "Sign Out?",
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = Espresso
                )
            },
            text = {
                Text(
                    text = "Your local declarations will remain safe on this device. You can sign back in anytime to sync across devices.",
                    fontFamily = BodyFontFamily,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = Stone
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutConfirm = false
                        AuthManager.signOut()
                        Toast.makeText(context, "Signed out ✓", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(
                        text = "Sign Out",
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = Terracotta
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirm = false }) {
                    Text(
                        text = "Cancel",
                        fontFamily = BodyFontFamily,
                        color = Stone
                    )
                }
            },
            containerColor = Surface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // ── Find Friends Dialog ───────────────────────────────────────────
    if (showFindFriendsDialog) {
        FindFriendsDialog(
            onDismiss = { showFindFriendsDialog = false }
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
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label.uppercase(),
            fontFamily = BodyFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 9.5.sp,
            letterSpacing = 1.sp,
            color = StoneMuted
        )
    }
}
