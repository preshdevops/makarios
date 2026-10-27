package com.makarios.app.ui.screens
import com.makarios.app.ui.components.MakChip
import com.makarios.app.ui.components.MakSurface
import com.makarios.app.ui.components.MakSwitch

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.makarios.app.BuildConfig
import com.makarios.app.data.AuthManager
import com.makarios.app.data.DeclarationSource
import com.makarios.app.data.UserLightStore
import com.makarios.app.data.WidgetSettingsStore
import com.makarios.app.data.local.KeptRepository
import com.makarios.app.ui.components.SacredTimePickerDialog
import com.makarios.app.ui.theme.*
import com.makarios.app.util.CrashReporter
import com.makarios.app.util.NotificationStore
import com.makarios.app.util.ReminderManager
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

object ProfileActivityLog {
    private const val PREFS = "makarios_activity_log"
    private const val SHOWN = "shown"
    private const val ACTION = "action"
    private const val SHARES = "shares_total"
    private const val BEST_STREAK = "best_streak"
    private const val JOINED_DATE = "joined_date"

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private fun today() = LocalDate.now().toString()

    fun recordShown(context: Context) {
        val p = prefs(context)
        val current = p.getStringSet(SHOWN, emptySet()) ?: emptySet()
        p.edit().putStringSet(SHOWN, current + today()).apply()
        ensureJoined(context)
    }

    fun recordQualified(context: Context) {
        val p = prefs(context)
        val current = p.getStringSet(ACTION, emptySet()) ?: emptySet()
        p.edit().putStringSet(ACTION, current + today()).apply()
        ensureJoined(context)
        updateBestStreak(context)
    }

    fun recordOpened(context: Context) = recordQualified(context)
    fun recordKept(context: Context) = recordQualified(context)

    fun recordShared(context: Context) {
        recordQualified(context)
        val p = prefs(context)
        val current = p.getInt(SHARES, 0)
        p.edit().putInt(SHARES, current + 1).apply()
    }

    fun totalShares(context: Context): Int = prefs(context).getInt(SHARES, 0)

    fun qualifiedDates(context: Context): Set<LocalDate> {
        val raw = prefs(context).getStringSet(ACTION, emptySet()) ?: emptySet()
        return raw.mapNotNull { str ->
            runCatching { LocalDate.parse(str) }.getOrNull()
        }.toSet()
    }

    fun streak(context: Context): Int {
        val dates = qualifiedDates(context)
        var date = LocalDate.now()
        var count = 0
        while (date in dates) {
            count++
            date = date.minusDays(1)
        }
        return count
    }

    fun bestStreak(context: Context): Int {
        val current = streak(context)
        val saved = prefs(context).getInt(BEST_STREAK, 0)
        val best = maxOf(current, saved)
        if (best > saved) {
            prefs(context).edit().putInt(BEST_STREAK, best).apply()
        }
        return best
    }

    private fun updateBestStreak(context: Context) {
        val current = streak(context)
        val saved = prefs(context).getInt(BEST_STREAK, 0)
        if (current > saved) {
            prefs(context).edit().putInt(BEST_STREAK, current).apply()
        }
    }

    private fun ensureJoined(context: Context) {
        val p = prefs(context)
        if (!p.contains(JOINED_DATE)) {
            val now = LocalDate.now()
            p.edit().putString(JOINED_DATE, "${now.year}-${now.monthValue}").apply()
        }
    }

    fun joinedMonthYear(context: Context): String {
        ensureJoined(context)
        val raw = prefs(context).getString(JOINED_DATE, null)
        return runCatching {
            val parts = raw?.split("-")
            val year = parts?.get(0)?.toInt() ?: LocalDate.now().year
            val month = parts?.get(1)?.toInt() ?: LocalDate.now().monthValue
            val d = LocalDate.of(year, month, 1)
            val monthName = d.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
            "$monthName $year"
        }.getOrDefault("Recently")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onSignOut: () -> Unit,
    onOpenWallpapers: () -> Unit = {},
    onResetOnboarding: () -> Unit = {},
    onOpenWidgetSettings: () -> Unit = {},
    onOpenReminderSettings: () -> Unit = {},
    onOpenMakeItYours: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val user = AuthManager.currentUser
    val guest = AuthManager.isAnonymous || user == null
    val currentLight = LocalLight.current
    val setUserLight = LocalSetLight.current
    val lightPreference by UserLightStore.preference(context).collectAsState(initial = UserLightStore.AUTO)

    var showAuth by remember { mutableStateOf(false) }
    var authInitialStep by remember { mutableStateOf(AuthFlowStep.CHOOSE) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showStreakSheet by remember { mutableStateOf(false) }
    var showPrivacySheet by remember { mutableStateOf(false) }
    var showSoundDialog by remember { mutableStateOf(false) }
    var showLightPickerSheet by remember { mutableStateOf(false) }

    var reminderEnabled by remember { mutableStateOf(ReminderManager.isDailyReminderEnabled(context)) }
    var hour by remember { mutableStateOf(ReminderManager.getReminderHour(context)) }
    var minute by remember { mutableStateOf(ReminderManager.getReminderMinute(context)) }
    var soundChoice by remember { mutableStateOf(NotificationStore.soundChoice(context)) }
    var wordmarkEnabled by remember { mutableStateOf(com.makarios.app.ui.wallpaper.ShareSettings.isWordmarkEnabled(context)) }

    val keptRows by KeptRepository.getAllFlow().collectAsState(initial = emptyList())
    val keptCount = keptRows.count { it.sourceType != "authored" }
    val writtenCount = keptRows.count { it.sourceType == "authored" }
    val sharedCount = ProfileActivityLog.totalShares(context)
    val streak = ProfileActivityLog.streak(context)
    val bestStreak = ProfileActivityLog.bestStreak(context)
    val joinedMonthYear = ProfileActivityLog.joinedMonthYear(context)
    val dates = ProfileActivityLog.qualifiedDates(context)

    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val isEmailVerified by AuthManager.isEmailVerified.collectAsState()

    val widgetSettings = remember { WidgetSettingsStore.load(context) }
    val widgetChoiceLabel = if (widgetSettings.sources.contains(DeclarationSource.MINE)) {
        "My declarations"
    } else {
        "Daily verses"
    }

    val formattedReminderTime = remember(hour, minute) {
        val isAm = hour < 12
        val h = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        val m = minute.toString().padStart(2, '0')
        "$h:$m ${if (isAm) "AM" else "PM"}"
    }

    val soundDisplayLabel = when (soundChoice) {
        NotificationStore.SoundChoice.CHIME -> "Quiet chime"
        NotificationStore.SoundChoice.SOFT -> "System default"
        NotificationStore.SoundChoice.SILENT -> "Silent"
    }

    val currentThemeName = when (lightPreference) {
        UserLightStore.AUTO -> "Auto (${currentLight.name})"
        else -> currentLight.name
    }

    val cardBg = if (currentLight.isDark) Ink.copy(alpha = 0.92f) else Cream
    val cardBorder = currentLight.text.copy(alpha = 0.12f)
    val accentColor = if (currentLight.isDark) AnchorDark else AnchorLight

    SystemBarsController(currentLight)
    ProvideActiveLight(currentLight, "You") {}

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(currentLight.top, currentLight.bottom))),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = statusBarTop + 16.dp, bottom = navBottom + 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Header
        item(key = "profile-header") {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "You",
                    style = MakariosTypography.displayLarge.copy(
                        fontFamily = NewsreaderFontFamily,
                                                fontSize = 38.sp
                    ),
                    color = currentLight.text
                )
                Spacer(Modifier.height(6.dp))
                if (!guest && user != null) {
                    Text(
                        text = "Signed in as ${user.email ?: "Signed in"}",
                        style = MakariosTypography.bodyMedium.copy(fontSize = 15.sp),
                        color = currentLight.text.copy(alpha = 0.75f)
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Guest. Sign in to back up your declarations.",
                            style = MakariosTypography.bodyMedium.copy(fontSize = 15.sp),
                            color = currentLight.text.copy(alpha = 0.75f),
                            modifier = Modifier.weight(1f).padding(end = 12.dp)
                        )
                        Button(
                            onClick = {
                                authInitialStep = AuthFlowStep.CHOOSE
                                showAuth = true
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentColor,
                                contentColor = Cream
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "Sign in",
                                style = MakariosTypography.labelMedium.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }
        }

        // Email verification banner if needed
        if (user != null && !guest && !isEmailVerified) {
            item(key = "profile-email-banner") {
                EmailVerificationBanner(
                    onResend = {
                        AuthManager.sendEmailVerification(
                            onSuccess = { Toast.makeText(context, "Verification email sent.", Toast.LENGTH_SHORT).show() },
                            onError = { err -> Toast.makeText(context, err, Toast.LENGTH_SHORT).show() }
                        )
                    },
                    onAccountDeleted = {
                        authInitialStep = AuthFlowStep.CHOOSE
                        showAuth = true
                    }
                )
            }
        }

        // 2. Card: "Your streak"
        item(key = "profile-streak-card") {
            val today = LocalDate.now()
            val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val weekDays = (0..6).map { monday.plusDays(it.toLong()) }
            val weekCount = weekDays.count { it in dates }

            MakSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showStreakSheet = true },
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Your streak",
                        style = MakariosTypography.titleMedium.copy(
                            fontFamily = NewsreaderFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = currentLight.text
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$streak",
                            style = MakariosTypography.displayLarge.copy(
                                fontSize = 44.sp,
                                fontFeatureSettings = "lnum, tnum",
                                fontWeight = FontWeight.Bold
                            ),
                            color = currentLight.text
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "days in a row",
                            style = MakariosTypography.bodyLarge.copy(
                                fontSize = 18.sp,
                                fontFamily = NewsreaderFontFamily,
                                                            ),
                            color = currentLight.text.copy(alpha = 0.85f),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "A day counts when you read, write, save or share a declaration.",
                        style = MakariosTypography.bodySmall.copy(fontSize = 14.sp),
                        color = currentLight.text.copy(alpha = 0.72f)
                    )
                    Spacer(Modifier.height(18.dp))

                    // 7 labeled circles: Mon Tue Wed Thu Fri Sat Sun
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        weekDays.forEach { day ->
                            val isDone = day in dates
                            val isToday = day == today
                            val isPast = day.isBefore(today)
                            val dayLabel = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isDone) accentColor else Color.Transparent
                                        )
                                        .border(
                                            width = if (isToday) 2.dp else 1.dp,
                                            color = when {
                                                isDone -> accentColor
                                                isToday -> accentColor
                                                isPast -> currentLight.text.copy(alpha = 0.22f)
                                                else -> currentLight.text.copy(alpha = 0.10f)
                                            },
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isDone) {
                                        Icon(
                                            imageVector = Icons.Outlined.Check,
                                            contentDescription = null,
                                            tint = Cream,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    } else if (isToday) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(accentColor)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = dayLabel,
                                    style = MakariosTypography.labelSmall.copy(
                                        fontSize = 12.sp,
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isToday) currentLight.text else currentLight.text.copy(alpha = 0.70f)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "This week: $weekCount of 7 days",
                        style = MakariosTypography.bodyMedium.copy(fontSize = 15.sp),
                        color = currentLight.text.copy(alpha = 0.80f)
                    )
                }
            }
        }

        // 3. Card: "Your activity"
        item(key = "profile-activity-card") {
            MakSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Your activity",
                        style = MakariosTypography.titleMedium.copy(
                            fontFamily = NewsreaderFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = currentLight.text
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ActivityTile(
                            modifier = Modifier.weight(1f),
                            count = keptCount,
                            label = "Saved",
                            textColor = currentLight.text
                        )
                        ActivityTile(
                            modifier = Modifier.weight(1f),
                            count = writtenCount,
                            label = "Written by you",
                            textColor = currentLight.text
                        )
                        ActivityTile(
                            modifier = Modifier.weight(1f),
                            count = sharedCount,
                            label = "Shared",
                            textColor = currentLight.text
                        )
                    }
                }
            }
        }

        // 4. Section: Reminders
        item(key = "profile-section-reminders") {
            SectionContainer(
                title = "Reminders",
                titleColor = currentLight.text,
                cardBg = cardBg,
                cardBorder = cardBorder
            ) {
                SettingActionRow(
                    title = "Daily reminder",
                    subtitle = if (reminderEnabled) formattedReminderTime else "Off",
                    textColor = currentLight.text,
                    onClick = { showTimePicker = true },
                    trailing = {
                        MakSwitch(
                            checked = reminderEnabled,
                            onCheckedChange = { checked ->
                                reminderEnabled = checked
                                ReminderManager.setDailyReminderEnabled(context, checked)
                            }
                        )
                    }
                )
                SettingDivider(currentLight.text)
                SettingActionRow(
                    title = "Reminder sound",
                    subtitle = soundDisplayLabel,
                    textColor = currentLight.text,
                    onClick = { showSoundDialog = true },
                    trailing = {
                        Icon(
                            imageVector = Icons.Outlined.ChevronRight,
                            contentDescription = null,
                            tint = currentLight.text.copy(alpha = 0.50f)
                        )
                    }
                )
            }
        }

        // 5. Section: Widget
        item(key = "profile-section-widget") {
            SectionContainer(
                title = "Widget",
                titleColor = currentLight.text,
                cardBg = cardBg,
                cardBorder = cardBorder
            ) {
                SettingActionRow(
                    title = "Widget content",
                    subtitle = widgetChoiceLabel,
                    textColor = currentLight.text,
                    onClick = onOpenWidgetSettings,
                    trailing = {
                        Icon(
                            imageVector = Icons.Outlined.ChevronRight,
                            contentDescription = null,
                            tint = currentLight.text.copy(alpha = 0.50f)
                        )
                    }
                )
            }
        }

        // 6. Section: Look
        item(key = "profile-section-look") {
            SectionContainer(
                title = "Look",
                titleColor = currentLight.text,
                cardBg = cardBg,
                cardBorder = cardBorder
            ) {
                SettingActionRow(
                    title = "Theme",
                    subtitle = currentThemeName,
                    textColor = currentLight.text,
                    onClick = { showLightPickerSheet = true },
                    trailing = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.verticalGradient(listOf(currentLight.top, currentLight.bottom))
                                    )
                                    .border(1.dp, currentLight.text.copy(alpha = 0.20f), CircleShape)
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Outlined.ChevronRight,
                                contentDescription = null,
                                tint = currentLight.text.copy(alpha = 0.50f)
                            )
                        }
                    }
                )
                SettingDivider(currentLight.text)
                SettingActionRow(
                    title = "Show Makarios name on images",
                    subtitle = "Include 'makarios' wordmark on shared artwork",
                    textColor = currentLight.text,
                    onClick = {
                        val next = !wordmarkEnabled
                        wordmarkEnabled = next
                        com.makarios.app.ui.wallpaper.ShareSettings.setWordmarkEnabled(context, next)
                    },
                    trailing = {
                        MakSwitch(
                            checked = wordmarkEnabled,
                            onCheckedChange = { checked ->
                                wordmarkEnabled = checked
                                com.makarios.app.ui.wallpaper.ShareSettings.setWordmarkEnabled(context, checked)
                            }
                        )
                    }
                )
            }
        }

        // 7. Section: Account
        item(key = "profile-section-account") {
            SectionContainer(
                title = "Account",
                titleColor = currentLight.text,
                cardBg = cardBg,
                cardBorder = cardBorder
            ) {
                if (guest) {
                    SettingActionRow(
                        title = "Sign in",
                        subtitle = "Sign in to back up your declarations",
                        textColor = currentLight.text,
                        onClick = {
                            authInitialStep = AuthFlowStep.CHOOSE
                            showAuth = true
                        },
                        trailing = {
                            Icon(
                                imageVector = Icons.Outlined.ChevronRight,
                                contentDescription = null,
                                tint = currentLight.text.copy(alpha = 0.50f)
                            )
                        }
                    )
                } else {
                    SettingActionRow(
                        title = "Sign out",
                        subtitle = "Switch account",
                        textColor = currentLight.text,
                        onClick = { showSignOutDialog = true },
                        trailing = {
                            Icon(
                                imageVector = Icons.Outlined.ChevronRight,
                                contentDescription = null,
                                tint = currentLight.text.copy(alpha = 0.50f)
                            )
                        }
                    )
                }
                SettingDivider(currentLight.text)
                SettingActionRow(
                    title = "Sync status",
                    subtitle = if (!guest) "Backed up" else "Not backed up",
                    textColor = currentLight.text,
                    onClick = {
                        if (guest) {
                            authInitialStep = AuthFlowStep.CHOOSE
                            showAuth = true
                        }
                    },
                    trailing = {
                        if (!guest) {
                            Icon(
                                imageVector = Icons.Outlined.CloudDone,
                                contentDescription = null,
                                tint = accentColor
                            )
                        }
                    }
                )
                if (!guest) {
                    SettingDivider(currentLight.text)
                    SettingActionRow(
                        title = "Delete account",
                        subtitle = "Permanently delete your account and backups",
                        textColor = MakError,
                        onClick = { showDeleteAccountDialog = true },
                        trailing = {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = null,
                                tint = MakError
                            )
                        }
                    )
                }
            }
        }

        // 8. Section: About
        item(key = "profile-section-about") {
            SectionContainer(
                title = "About",
                titleColor = currentLight.text,
                cardBg = cardBg,
                cardBorder = cardBorder
            ) {
                SettingActionRow(
                    title = "Version",
                    subtitle = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    textColor = currentLight.text,
                    onClick = {}
                )
                SettingDivider(currentLight.text)
                SettingActionRow(
                    title = "Send a crash report",
                    subtitle = "Email diagnostics and recent logs to developer",
                    textColor = currentLight.text,
                    onClick = { CrashReporter.sendCrashReportEmail(context) },
                    trailing = {
                        Icon(
                            imageVector = Icons.Outlined.BugReport,
                            contentDescription = null,
                            tint = currentLight.text.copy(alpha = 0.50f)
                        )
                    }
                )
                SettingDivider(currentLight.text)
                SettingActionRow(
                    title = "Privacy",
                    subtitle = "How Makarios protects your data",
                    textColor = currentLight.text,
                    onClick = { showPrivacySheet = true },
                    trailing = {
                        Icon(
                            imageVector = Icons.Outlined.ChevronRight,
                            contentDescription = null,
                            tint = currentLight.text.copy(alpha = 0.50f)
                        )
                    }
                )
            }
        }
    }

    // Modal Sheet: How streaks work
    if (showStreakSheet) {
        ModalBottomSheet(
            onDismissRequest = { showStreakSheet = false },
            containerColor = cardBg,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "How streaks work",
                    style = MakariosTypography.titleLarge.copy(
                        fontFamily = NewsreaderFontFamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = currentLight.text
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "A day counts toward your streak when you:",
                    style = MakariosTypography.bodyMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.Medium),
                    color = currentLight.text
                )
                Spacer(Modifier.height(10.dp))
                StreakRuleRow("1", "Read today's declaration or Bible passage", currentLight.text, accentColor)
                StreakRuleRow("2", "Write your own declaration", currentLight.text, accentColor)
                StreakRuleRow("3", "Save a declaration or verse to your collection", currentLight.text, accentColor)
                StreakRuleRow("4", "Share a declaration with others", currentLight.text, accentColor)

                Spacer(Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(currentLight.text.copy(alpha = 0.12f))
                )
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Your best",
                            style = MakariosTypography.labelSmall.copy(fontSize = 13.sp),
                            color = currentLight.text.copy(alpha = 0.70f)
                        )
                        Text(
                            text = "$bestStreak days",
                            style = MakariosTypography.titleMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                            color = currentLight.text
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Joined",
                            style = MakariosTypography.labelSmall.copy(fontSize = 13.sp),
                            color = currentLight.text.copy(alpha = 0.70f)
                        )
                        Text(
                            text = joinedMonthYear,
                            style = MakariosTypography.titleMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                            color = currentLight.text
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { showStreakSheet = false },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Cream)
                ) {
                    Text("Got it", style = MakariosTypography.labelLarge.copy(fontSize = 15.sp))
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // Modal Sheet: Privacy
    if (showPrivacySheet) {
        ModalBottomSheet(
            onDismissRequest = { showPrivacySheet = false },
            containerColor = cardBg,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Privacy at Makarios",
                    style = MakariosTypography.titleLarge.copy(
                        fontFamily = NewsreaderFontFamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = currentLight.text
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Makarios never sells your data. Declarations are yours alone.",
                    style = MakariosTypography.bodyLarge.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 24.sp
                    ),
                    color = currentLight.text
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Your declarations and activity stay completely private on this device. When you choose to sign in, your data is securely backed up so you never lose it. Declarations are only visible to others if you explicitly publish them to Community.",
                    style = MakariosTypography.bodyMedium.copy(
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    ),
                    color = currentLight.text.copy(alpha = 0.78f)
                )
                Spacer(Modifier.height(28.dp))
                Button(
                    onClick = { showPrivacySheet = false },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Cream)
                ) {
                    Text("Done", style = MakariosTypography.labelLarge.copy(fontSize = 15.sp))
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // Sheet: Light Theme Picker (8 gradient dots + Auto)
    if (showLightPickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLightPickerSheet = false },
            containerColor = cardBg,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Theme & Light",
                    style = MakariosTypography.titleLarge.copy(
                        fontFamily = NewsreaderFontFamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = currentLight.text
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Choose the atmosphere you see throughout Makarios.",
                    style = MakariosTypography.bodyMedium.copy(fontSize = 14.sp),
                    color = currentLight.text.copy(alpha = 0.70f)
                )
                Spacer(Modifier.height(16.dp))

                // Auto option
                val isAuto = lightPreference == UserLightStore.AUTO
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            setUserLight(null)
                            showLightPickerSheet = false
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isAuto,
                        onClick = {
                            setUserLight(null)
                            showLightPickerSheet = false
                        },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = accentColor,
                            unselectedColor = currentLight.text.copy(alpha = 0.40f)
                        )
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Auto",
                            style = MakariosTypography.bodyLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                            color = currentLight.text
                        )
                        Text(
                            text = "Changes naturally with the time of day",
                            style = MakariosTypography.bodySmall.copy(fontSize = 13.sp),
                            color = currentLight.text.copy(alpha = 0.65f)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().height(1.dp).background(currentLight.text.copy(alpha = 0.10f))
                )
                Spacer(Modifier.height(12.dp))

                // 8 Lights
                Light.values().forEach { lightOption ->
                    val isSelected = !isAuto && currentLight == lightOption
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                setUserLight(lightOption)
                                showLightPickerSheet = false
                            }
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                setUserLight(lightOption)
                                showLightPickerSheet = false
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = accentColor,
                                unselectedColor = currentLight.text.copy(alpha = 0.40f)
                            )
                        )
                        Spacer(Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Brush.verticalGradient(listOf(lightOption.top, lightOption.bottom)))
                                .border(1.dp, currentLight.text.copy(alpha = 0.20f), CircleShape)
                        )
                        Spacer(Modifier.width(14.dp))
                        Text(
                            text = lightOption.name,
                            style = MakariosTypography.bodyLarge.copy(
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = currentLight.text
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // Dialog: Reminder Sound Picker
    if (showSoundDialog) {
        AlertDialog(
            onDismissRequest = { showSoundDialog = false },
            title = {
                Text(
                    text = "Reminder sound",
                    style = MakariosTypography.titleMedium.copy(
                        fontFamily = NewsreaderFontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = currentLight.text
                )
            },
            text = {
                Column {
                    listOf(
                        NotificationStore.SoundChoice.SOFT to "System default",
                        NotificationStore.SoundChoice.CHIME to "Quiet chime",
                        NotificationStore.SoundChoice.SILENT to "Silent"
                    ).forEach { (choice, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    soundChoice = choice
                                    NotificationStore.setSoundChoice(context, choice)
                                    showSoundDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = soundChoice == choice,
                                onClick = {
                                    soundChoice = choice
                                    NotificationStore.setSoundChoice(context, choice)
                                    showSoundDialog = false
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = accentColor,
                                    unselectedColor = currentLight.text.copy(alpha = 0.40f)
                                )
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = label,
                                style = MakariosTypography.bodyLarge.copy(fontSize = 16.sp),
                                color = currentLight.text
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSoundDialog = false }) {
                    Text("Close", color = accentColor, fontWeight = FontWeight.SemiBold)
                }
            },
            containerColor = cardBg,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Dialog: Time Picker
    if (showTimePicker) {
        SacredTimePickerDialog(
            initialHour = hour,
            initialMinute = minute,
            onConfirm = { h, m ->
                hour = h
                minute = m
                ReminderManager.setReminderTime(context, h, m)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }

    // Dialog: Sign Out
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = {
                Text(
                    "Sign out",
                    style = MakariosTypography.displaySmall.copy(
                        fontSize = 20.sp,
                        fontFamily = NewsreaderFontFamily
                    ),
                    color = currentLight.text
                )
            },
            text = {
                Text(
                    "Do you want to save your declarations on this phone, or clear them upon signing out?",
                    style = MakariosTypography.bodyMedium.copy(fontSize = 15.sp),
                    color = currentLight.text.copy(alpha = 0.85f)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutDialog = false
                        AuthManager.signOut()
                        onSignOut()
                    }
                ) {
                    Text("Save on device", color = accentColor, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            showSignOutDialog = false
                            AuthManager.signOut()
                            onSignOut()
                        }
                    ) {
                        Text("Clear data", color = MakError)
                    }
                    TextButton(onClick = { showSignOutDialog = false }) {
                        Text("Cancel", color = currentLight.text.copy(alpha = 0.65f))
                    }
                }
            },
            containerColor = cardBg,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Dialog: Delete Account
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = {
                Text(
                    "Delete Account",
                    style = MakariosTypography.displaySmall.copy(
                        fontSize = 20.sp,
                        fontFamily = NewsreaderFontFamily
                    ),
                    color = MakError
                )
            },
            text = {
                Text(
                    "This permanently deletes your Makarios account and all cloud backups. This action cannot be undone.",
                    style = MakariosTypography.bodyMedium.copy(fontSize = 15.sp),
                    color = currentLight.text.copy(alpha = 0.85f)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAccountDialog = false
                        AuthManager.deleteAccount(
                            onSuccess = {
                                Toast.makeText(context, "Account deleted.", Toast.LENGTH_SHORT).show()
                                onSignOut()
                            },
                            onError = { err ->
                                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                ) {
                    Text("Delete account", color = MakError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel", color = currentLight.text)
                }
            },
            containerColor = cardBg,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Dialog: Auth Flow
    if (showAuth) {
        Dialog(
            onDismissRequest = { showAuth = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            AuthFlowHost(
                onContinueToApp = {
                    showAuth = false
                    Toast.makeText(context, "Welcome to Makarios!", Toast.LENGTH_SHORT).show()
                },
                initialStep = authInitialStep,
                onBack = { showAuth = false }
            )
        }
    }
}

@Composable
private fun ActivityTile(
    modifier: Modifier = Modifier,
    count: Int,
    label: String,
    textColor: Color
) {
    Column(
        modifier = modifier.padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            style = MakariosTypography.displaySmall.copy(
                fontSize = 32.sp,
                fontFeatureSettings = "lnum, tnum",
                fontWeight = FontWeight.Bold
            ),
            color = textColor
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MakariosTypography.bodyMedium.copy(fontSize = 14.sp),
            color = textColor.copy(alpha = 0.72f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SectionContainer(
    title: String,
    titleColor: Color,
    cardBg: Color,
    cardBorder: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MakariosTypography.titleLarge.copy(
                fontFamily = NewsreaderFontFamily,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = titleColor,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        MakSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.dp, cardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                content = content
            )
        }
    }
}

@Composable
private fun SettingActionRow(
    title: String,
    subtitle: String,
    textColor: Color,
    onClick: () -> Unit,
    trailing: @Composable () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f).padding(end = 12.dp)
        ) {
            Text(
                text = title,
                style = MakariosTypography.bodyLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Medium),
                color = textColor
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MakariosTypography.bodySmall.copy(fontSize = 14.sp),
                color = textColor.copy(alpha = 0.70f)
            )
        }
        trailing()
    }
}

@Composable
private fun SettingDivider(textColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(textColor.copy(alpha = 0.08f))
    )
}

@Composable
private fun StreakRuleRow(
    number: String,
    text: String,
    textColor: Color,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MakariosTypography.labelSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = accentColor
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            style = MakariosTypography.bodyMedium.copy(fontSize = 15.sp),
            color = textColor
        )
    }
}

@Composable
private fun EmailVerificationBanner(
    onResend: () -> Unit,
    onAccountDeleted: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val isVerified by AuthManager.isEmailVerified.collectAsState()

    var isChecking by remember { mutableStateOf(false) }
    var inlineError by remember { mutableStateOf<String?>(null) }
    var resendCooldown by remember { mutableStateOf(0) }

    LaunchedEffect(isVerified, lifecycleOwner) {
        while (!isVerified) {
            kotlinx.coroutines.delay(3000)
            if (lifecycleOwner.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)) {
                try {
                    val verified = AuthManager.reloadUser()
                    if (verified) break
                } catch (e: com.google.firebase.auth.FirebaseAuthInvalidUserException) {
                    onAccountDeleted()
                    break
                } catch (_: Exception) {}
            }
        }
    }

    LaunchedEffect(resendCooldown) {
        if (resendCooldown > 0) {
            kotlinx.coroutines.delay(1000)
            resendCooldown -= 1
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Cream.copy(alpha = 0.95f))
            .border(1.dp, Ink.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.MarkEmailUnread,
                contentDescription = null,
                tint = Ink,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Please verify your email address.",
                    style = MakariosTypography.labelMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
                    color = Ink
                )
                Text(
                    text = "Check your inbox for a verification link.",
                    style = MakariosTypography.labelSmall.copy(fontSize = 13.sp),
                    color = Ink.copy(alpha = 0.70f)
                )
            }
        }

        if (inlineError != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = inlineError ?: "",
                style = MakariosTypography.labelSmall.copy(fontSize = 13.sp, color = MakError),
                modifier = Modifier.padding(start = 34.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = {
                    if (resendCooldown == 0) {
                        resendCooldown = 60
                        inlineError = null
                        onResend()
                    }
                },
                enabled = resendCooldown == 0,
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Text(
                    text = if (resendCooldown > 0) "Resend in ${resendCooldown}s" else "Resend email",
                    style = MakariosTypography.labelSmall.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (resendCooldown == 0) TextDecoration.Underline else TextDecoration.None
                    ),
                    color = if (resendCooldown == 0) Ink else Ink.copy(alpha = 0.45f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (!isChecking) {
                        isChecking = true
                        inlineError = null
                        coroutineScope.launch {
                            try {
                                val verified = AuthManager.reloadUser()
                                if (!verified) {
                                    inlineError = "Not verified yet. Check your spam folder or tap Resend below."
                                }
                            } catch (e: com.google.firebase.auth.FirebaseAuthInvalidUserException) {
                                onAccountDeleted()
                            } catch (e: Exception) {
                                inlineError = "Not verified yet. Check your spam folder or tap Resend below."
                            } finally {
                                isChecking = false
                            }
                        }
                    }
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Cream),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                if (isChecking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Cream,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = "I've verified",
                    style = MakariosTypography.labelSmall.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}
