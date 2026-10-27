package com.makarios.app.ui.screens
import com.makarios.app.ui.components.MakChip
import com.makarios.app.ui.components.MakSurface

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import com.makarios.app.data.*
import com.makarios.app.ui.components.Pairing
import com.makarios.app.ui.components.SacredTimePickerDialog
import com.makarios.app.ui.theme.*
import com.makarios.app.util.DailyDeclarationWorker
import com.makarios.app.widget.WidgetScheduling
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetRemindersSettingsScreen(
    initialTab: Int = 0,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember(initialTab) { mutableStateOf(initialTab) } // 0 = Widget, 1 = Reminders

    SystemBarsController(LocalLight.current)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Widget & Reminders",
                        style = MakariosTypography.titleLarge,
                        color = Ink
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Ink
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Cream
                )
            )
        },
        containerColor = Cream,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Cream,
                contentColor = Ink,
                divider = {
                    HorizontalDivider(color = Ink.copy(alpha = 0.1f))
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Widget",
                            style = MakariosTypography.labelLarge,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) Ink else Ink.copy(alpha = 0.6f)
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Reminders",
                            style = MakariosTypography.labelLarge,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) Ink else Ink.copy(alpha = 0.6f)
                        )
                    }
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (selectedTab == 0) {
                    WidgetSettingsPage()
                } else {
                    RemindersSettingsPage()
                }
            }
        }
    }
}

@Composable
private fun WidgetSettingsPage() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var widgetSettings by remember { mutableStateOf(WidgetSettingsStore.load(context)) }
    var previewAffirmation by remember {
        mutableStateOf(SeedDeclarations.asAffirmations().firstOrNull() ?: AffirmationRepository.affirmationOfTheDay)
    }

    // Refresh preview declaration when sources change
    LaunchedEffect(widgetSettings.sources) {
        val picked = runCatching {
            DeclarationPicker.pickForWidget(context, widgetSettings)
        }.getOrNull()
        if (picked != null) {
            previewAffirmation = picked
        }
    }

    val previewLight = remember(widgetSettings.lookMode, widgetSettings.fixedLightName) {
        if (widgetSettings.lookMode == WidgetLookMode.FIXED_LIGHT && widgetSettings.fixedLightName.isNotBlank()) {
            runCatching { Light.valueOf(widgetSettings.fixedLightName) }.getOrDefault(Light.forNow())
        } else {
            Light.forNow()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        // Live Preview Section
        Text(
            "Preview",
            style = MakariosTypography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = Ink.copy(alpha = 0.65f),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .drawBehind { drawLight(previewLight, size, horizon = HorizonSpec(.72f, .80f, .88f)) }
                .padding(16.dp)
        ) {
            Pairing(
                declaration = previewAffirmation.declaration,
                verseText = previewAffirmation.scriptureText,
                verseReference = previewAffirmation.reference,
                light = previewLight
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sources section
        Text(
            "Sources",
            style = MakariosTypography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = Ink.copy(alpha = 0.65f),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val sources = widgetSettings.sources
            MakChip(
                selected = sources.contains(DeclarationSource.MINE),
                onClick = {
                    val next = if (sources.contains(DeclarationSource.MINE)) {
                        if (sources.size > 1) sources - DeclarationSource.MINE else sources
                    } else sources + DeclarationSource.MINE
                    widgetSettings = widgetSettings.copy(sources = next)
                },
                label = { Text("My declarations") },
                colors = filterChipColors()
            )
            MakChip(
                selected = sources.contains(DeclarationSource.KEPT),
                onClick = {
                    val next = if (sources.contains(DeclarationSource.KEPT)) {
                        if (sources.size > 1) sources - DeclarationSource.KEPT else sources
                    } else sources + DeclarationSource.KEPT
                    widgetSettings = widgetSettings.copy(sources = next)
                },
                label = { Text("Saved") },
                colors = filterChipColors()
            )
            MakChip(
                selected = sources.contains(DeclarationSource.MAKARIOS),
                onClick = {
                    val next = if (sources.contains(DeclarationSource.MAKARIOS)) {
                        if (sources.size > 1) sources - DeclarationSource.MAKARIOS else sources
                    } else sources + DeclarationSource.MAKARIOS
                    widgetSettings = widgetSettings.copy(sources = next)
                },
                label = { Text("Makarios picks") },
                colors = filterChipColors()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Order section
        Text(
            "Order",
            style = MakariosTypography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = Ink.copy(alpha = 0.65f),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MakChip(
                selected = widgetSettings.order == DeclarationOrder.SHUFFLE,
                onClick = { widgetSettings = widgetSettings.copy(order = DeclarationOrder.SHUFFLE) },
                label = { Text("Shuffle") },
                colors = filterChipColors()
            )
            MakChip(
                selected = widgetSettings.order == DeclarationOrder.NEWEST_FIRST,
                onClick = { widgetSettings = widgetSettings.copy(order = DeclarationOrder.NEWEST_FIRST) },
                label = { Text("Newest first") },
                colors = filterChipColors()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Rotate section
        Text(
            "Rotate",
            style = MakariosTypography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = Ink.copy(alpha = 0.65f),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MakChip(
                selected = widgetSettings.rotateEvery == WidgetRotateEvery.ON_TAP,
                onClick = { widgetSettings = widgetSettings.copy(rotateEvery = WidgetRotateEvery.ON_TAP) },
                label = { Text("On tap") },
                colors = filterChipColors()
            )
            MakChip(
                selected = widgetSettings.rotateEvery == WidgetRotateEvery.HOURLY,
                onClick = { widgetSettings = widgetSettings.copy(rotateEvery = WidgetRotateEvery.HOURLY) },
                label = { Text("Every hour") },
                colors = filterChipColors()
            )
            MakChip(
                selected = widgetSettings.rotateEvery == WidgetRotateEvery.THREE_HOUR,
                onClick = { widgetSettings = widgetSettings.copy(rotateEvery = WidgetRotateEvery.THREE_HOUR) },
                label = { Text("Every 3 hours") },
                colors = filterChipColors()
            )
            MakChip(
                selected = widgetSettings.rotateEvery == WidgetRotateEvery.DAILY,
                onClick = { widgetSettings = widgetSettings.copy(rotateEvery = WidgetRotateEvery.DAILY) },
                label = { Text("Daily") },
                colors = filterChipColors()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Look mode section
        Text(
            "Look",
            style = MakariosTypography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = Ink.copy(alpha = 0.65f),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MakChip(
                selected = widgetSettings.lookMode == WidgetLookMode.ORIGINAL,
                onClick = { widgetSettings = widgetSettings.copy(lookMode = WidgetLookMode.ORIGINAL) },
                label = { Text("Use original look") },
                colors = filterChipColors()
            )
            MakChip(
                selected = widgetSettings.lookMode == WidgetLookMode.FIXED_LIGHT,
                onClick = {
                    val defaultFixed = widgetSettings.fixedLightName.ifBlank { Light.Dawn.name }
                    widgetSettings = widgetSettings.copy(lookMode = WidgetLookMode.FIXED_LIGHT, fixedLightName = defaultFixed)
                },
                label = { Text("Fixed light") },
                colors = filterChipColors()
            )
        }

        if (widgetSettings.lookMode == WidgetLookMode.FIXED_LIGHT) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Light.values().forEach { light ->
                    val isSelected = widgetSettings.fixedLightName == light.name
                    MakChip(
                        selected = isSelected,
                        onClick = { widgetSettings = widgetSettings.copy(fixedLightName = light.name) },
                        label = { Text(light.name) },
                        colors = filterChipColors()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Save Button
        Button(
            onClick = {
                WidgetSettingsStore.save(context, widgetSettings)
                coroutineScope.launch {
                    WidgetScheduling.refreshNow(context)
                }
                Toast.makeText(context, "Widget settings saved", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Ink,
                contentColor = Cream
            ),
            shape = RoundedCornerShape(26.dp)
        ) {
            Text("Save widget settings", style = MakariosTypography.labelLarge)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun RemindersSettingsPage() {
    val context = LocalContext.current
    var reminderSettings by remember { mutableStateOf(ReminderSettingsStore.load(context)) }

    val notificationsEnabled = remember {
        NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    var showTimePickerForIndex by remember { mutableStateOf<Int?>(null) }
    var showQuietStartPicker by remember { mutableStateOf(false) }
    var showQuietEndPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        if (!notificationsEnabled) {
            MakSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Cream
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Outlined.NotificationsOff,
                            contentDescription = null,
                            tint = Ink
                        )
                        Text(
                            "Notifications are off",
                            style = MakariosTypography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )
                    }
                    Text(
                        "Reminders won't fire until you allow notifications in system settings.",
                        style = MakariosTypography.bodyMedium,
                        color = Ink.copy(alpha = 0.75f)
                    )
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink)
                    ) {
                        Text("Open Settings", style = MakariosTypography.labelMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Sources section
        Text(
            "Sources",
            style = MakariosTypography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = Ink.copy(alpha = 0.65f),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val sources = reminderSettings.sources
            MakChip(
                selected = sources.contains(DeclarationSource.MINE),
                onClick = {
                    val next = if (sources.contains(DeclarationSource.MINE)) {
                        if (sources.size > 1) sources - DeclarationSource.MINE else sources
                    } else sources + DeclarationSource.MINE
                    reminderSettings = reminderSettings.copy(sources = next)
                },
                label = { Text("My declarations") },
                colors = filterChipColors()
            )
            MakChip(
                selected = sources.contains(DeclarationSource.KEPT),
                onClick = {
                    val next = if (sources.contains(DeclarationSource.KEPT)) {
                        if (sources.size > 1) sources - DeclarationSource.KEPT else sources
                    } else sources + DeclarationSource.KEPT
                    reminderSettings = reminderSettings.copy(sources = next)
                },
                label = { Text("Saved") },
                colors = filterChipColors()
            )
            MakChip(
                selected = sources.contains(DeclarationSource.MAKARIOS),
                onClick = {
                    val next = if (sources.contains(DeclarationSource.MAKARIOS)) {
                        if (sources.size > 1) sources - DeclarationSource.MAKARIOS else sources
                    } else sources + DeclarationSource.MAKARIOS
                    reminderSettings = reminderSettings.copy(sources = next)
                },
                label = { Text("Makarios picks") },
                colors = filterChipColors()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Order section
        Text(
            "Order",
            style = MakariosTypography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = Ink.copy(alpha = 0.65f),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MakChip(
                selected = reminderSettings.order == DeclarationOrder.SHUFFLE,
                onClick = { reminderSettings = reminderSettings.copy(order = DeclarationOrder.SHUFFLE) },
                label = { Text("Shuffle") },
                colors = filterChipColors()
            )
            MakChip(
                selected = reminderSettings.order == DeclarationOrder.NEWEST_FIRST,
                onClick = { reminderSettings = reminderSettings.copy(order = DeclarationOrder.NEWEST_FIRST) },
                label = { Text("Newest first") },
                colors = filterChipColors()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Reminder Times section (up to 3)
        Text(
            "Reminder times",
            style = MakariosTypography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = Ink.copy(alpha = 0.65f),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            reminderSettings.times.forEachIndexed { index, time ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Cream)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { showTimePickerForIndex = index }) {
                        Text(
                            formatTime(time.hour, time.minute),
                            style = MakariosTypography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Ink
                        )
                    }
                    if (reminderSettings.times.size > 1) {
                        IconButton(
                            onClick = {
                                val nextTimes = reminderSettings.times.toMutableList().also { it.removeAt(index) }
                                reminderSettings = reminderSettings.copy(times = nextTimes)
                            }
                        ) {
                            Icon(Icons.Outlined.Delete, "Remove time", tint = Ink.copy(alpha = 0.6f))
                        }
                    }
                }
            }

            if (reminderSettings.times.size < 3) {
                OutlinedButton(
                    onClick = {
                        val nextTimes = reminderSettings.times + ReminderTime(12, 0)
                        reminderSettings = reminderSettings.copy(times = nextTimes)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink)
                ) {
                    Icon(Icons.Outlined.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Add time (up to 3)", style = MakariosTypography.labelLarge)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quiet Hours section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Quiet hours",
                    style = MakariosTypography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = Ink
                )
                Text(
                    "Mute reminders during these hours",
                    style = MakariosTypography.bodySmall,
                    color = Ink.copy(alpha = 0.65f)
                )
            }
            Switch(
                checked = reminderSettings.quietHoursStart != null && reminderSettings.quietHoursEnd != null,
                onCheckedChange = { checked ->
                    reminderSettings = if (checked) {
                        reminderSettings.copy(quietHoursStart = 22, quietHoursEnd = 7)
                    } else {
                        reminderSettings.copy(quietHoursStart = null, quietHoursEnd = null)
                    }
                },
                colors = switchColors()
            )
        }

        if (reminderSettings.quietHoursStart != null && reminderSettings.quietHoursEnd != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("From", style = MakariosTypography.labelSmall, color = Ink.copy(alpha = 0.65f))
                    OutlinedButton(
                        onClick = { showQuietStartPicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(formatTime((reminderSettings.quietHoursStart ?: 22), 0), color = Ink)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Until", style = MakariosTypography.labelSmall, color = Ink.copy(alpha = 0.65f))
                    OutlinedButton(
                        onClick = { showQuietEndPicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(formatTime((reminderSettings.quietHoursEnd ?: 7), 0), color = Ink)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Include Verse toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Include Scripture verse",
                    style = MakariosTypography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = Ink
                )
                Text(
                    "Shows verse body in the notification",
                    style = MakariosTypography.bodySmall,
                    color = Ink.copy(alpha = 0.65f)
                )
            }
            Switch(
                checked = reminderSettings.includeVerse,
                onCheckedChange = { reminderSettings = reminderSettings.copy(includeVerse = it) },
                colors = switchColors()
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Lock Screen Text toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Show on lock screen",
                    style = MakariosTypography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = Ink
                )
                Text(
                    if (reminderSettings.lockScreenText == LockScreenText.SHOW)
                        "Declaration visible on lock screen"
                    else "Hidden until unlocked",
                    style = MakariosTypography.bodySmall,
                    color = Ink.copy(alpha = 0.65f)
                )
            }
            Switch(
                checked = reminderSettings.lockScreenText == LockScreenText.SHOW,
                onCheckedChange = { checked ->
                    reminderSettings = reminderSettings.copy(
                        lockScreenText = if (checked) LockScreenText.SHOW else LockScreenText.HIDE
                    )
                },
                colors = switchColors()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Save Button
        Button(
            onClick = {
                ReminderSettingsStore.save(context, reminderSettings)
                if (notificationsEnabled) {
                    DailyDeclarationWorker.scheduleAll(context)
                }
                Toast.makeText(context, "Reminder settings saved", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Ink,
                contentColor = Cream
            ),
            shape = RoundedCornerShape(26.dp)
        ) {
            Text("Save reminder settings", style = MakariosTypography.labelLarge)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Time picker dialog for reminder slots
    showTimePickerForIndex?.let { index ->
        val currentSlot = reminderSettings.times.getOrNull(index) ?: ReminderTime(8, 0)
        SacredTimePickerDialog(
            initialHour = currentSlot.hour,
            initialMinute = currentSlot.minute,
            title = "Reminder Time",
            onConfirm = { h, m ->
                val nextTimes = reminderSettings.times.toMutableList().also {
                    if (index < it.size) it[index] = ReminderTime(h, m)
                }
                reminderSettings = reminderSettings.copy(times = nextTimes)
                showTimePickerForIndex = null
            },
            onDismiss = { showTimePickerForIndex = null }
        )
    }

    // Time picker for quiet start
    if (showQuietStartPicker) {
        val currentH = reminderSettings.quietHoursStart ?: 22
        SacredTimePickerDialog(
            initialHour = currentH,
            initialMinute = 0,
            title = "Quiet Hours Start",
            onConfirm = { h, _ ->
                reminderSettings = reminderSettings.copy(quietHoursStart = h)
                showQuietStartPicker = false
            },
            onDismiss = { showQuietStartPicker = false }
        )
    }

    // Time picker for quiet end
    if (showQuietEndPicker) {
        val currentH = reminderSettings.quietHoursEnd ?: 7
        SacredTimePickerDialog(
            initialHour = currentH,
            initialMinute = 0,
            title = "Quiet Hours End",
            onConfirm = { h, _ ->
                reminderSettings = reminderSettings.copy(quietHoursEnd = h)
                showQuietEndPicker = false
            },
            onDismiss = { showQuietEndPicker = false }
        )
    }
}

private fun formatTime(h: Int, m: Int): String {
    val amPm = if (h < 12) "AM" else "PM"
    val h12 = if (h % 12 == 0) 12 else h % 12
    return "%d:%02d %s".format(h12, m, amPm)
}

@Composable
private fun filterChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = Cream,
    labelColor = Ink.copy(alpha = 0.75f),
    selectedContainerColor = Ink,
    selectedLabelColor = Cream
)

@Composable
private fun switchColors() = SwitchDefaults.colors(
    checkedThumbColor = Cream,
    checkedTrackColor = Ink,
    uncheckedThumbColor = Ink.copy(alpha = 0.6f),
    uncheckedTrackColor = Cream
)
