package com.makarios.app.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.ui.components.SacredTimePickerDialog
import com.makarios.app.ui.theme.*
import com.makarios.app.util.NotificationStore
import com.makarios.app.util.ReminderManager
import java.time.DayOfWeek

@Composable
fun NotificationsSettings(context: Context) {
    var morning by remember { mutableStateOf(NotificationStore.morningEnabled(context)) }
    var evening by remember { mutableStateOf(NotificationStore.eveningEnabled(context)) }
    var streak by remember { mutableStateOf(NotificationStore.streakEnabled(context)) }
    var silent by remember { mutableStateOf(NotificationStore.silent(context)) }
    var morningTime by remember { mutableStateOf(NotificationStore.morningTime(context)) }
    var eveningTime by remember { mutableStateOf(NotificationStore.eveningTime(context)) }
    var days by remember { mutableStateOf(NotificationStore.weekdays(context)) }
    var picker by remember { mutableStateOf<String?>(null) }
    val permissionMissing = android.os.Build.VERSION.SDK_INT >= 33 && androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Notifications", style = MakariosTypography.bodyLarge.copy(fontFamily = NewsreaderFontFamily, fontStyle = FontStyle.Italic, fontSize = 20.sp), color = Ink)
        NotificationToggle("Morning declaration", "A short declaration at your chosen hour", morning, { morning = it; NotificationStore.setMorningEnabled(context, it); ReminderManager.rescheduleAll(context) })
        if (morning) NotificationTimeChips(morningTime, { morningTime = it; NotificationStore.setMorningTime(context, it.first, it.second); ReminderManager.rescheduleAll(context) }) { picker = "morning" }
        NotificationToggle("Evening verse", "Before you sleep", evening, { evening = it; NotificationStore.setEveningEnabled(context, it); ReminderManager.rescheduleAll(context) })
        if (evening) NotificationTimeChips(eveningTime, { eveningTime = it; NotificationStore.setEveningTime(context, it.first, it.second); ReminderManager.rescheduleAll(context) }) { picker = "evening" }
        Text("Days", style = MakariosTypography.bodyLarge.copy(fontFamily = NewsreaderFontFamily, fontStyle = FontStyle.Italic, fontSize = 20.sp), color = Ink)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { DayOfWeek.values().forEach { day -> DayCircle(day, day in days) { days = if (day in days) days - day else days + day; NotificationStore.setWeekdays(context, days); ReminderManager.rescheduleAll(context) } } }
        Text("Gentle by default", style = MakariosTypography.bodyLarge.copy(fontFamily = NewsreaderFontFamily, fontStyle = FontStyle.Italic, fontSize = 20.sp), color = Ink)
        NotificationToggle("Silent notifications", "No sound or vibration", silent, { silent = it; NotificationStore.setSilent(context, it) })
        NotificationToggle("Streak reminder", "One quiet nudge at 8 PM", streak, { streak = it; NotificationStore.setStreakEnabled(context, it); ReminderManager.rescheduleAll(context) })
        NotificationPreview()
        TextButton(onClick = { context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)) }) { Text("Manage in system settings", color = Ink) }
        if (permissionMissing && NotificationStore.permissionAsks(context) >= 2) Text("Notifications are off in system settings.", color = Ink.copy(alpha = .72f), style = MakariosTypography.labelSmall)
    }
    picker?.let { kind -> SacredTimePickerDialog(initialHour = if (kind == "morning") morningTime.first else eveningTime.first, initialMinute = if (kind == "morning") morningTime.second else eveningTime.second, title = if (kind == "morning") "Morning declaration" else "Evening verse", onConfirm = { h, m -> if (kind == "morning") { morningTime = h to m; NotificationStore.setMorningTime(context, h, m) } else { eveningTime = h to m; NotificationStore.setEveningTime(context, h, m) }; ReminderManager.rescheduleAll(context); picker = null }, onDismiss = { picker = null }) }
}

@Composable private fun NotificationToggle(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(title, style = MakariosTypography.bodyMedium, color = Ink); Text(subtitle, style = MakariosTypography.labelSmall, color = Ink.copy(alpha = .68f)) }; Switch(checked, onChange, colors = SwitchDefaults.colors(checkedThumbColor = Cream, checkedTrackColor = Ink, uncheckedThumbColor = Ink, uncheckedTrackColor = Ink.copy(alpha = .18f))) } }
@Composable private fun NotificationTimeChips(time: Pair<Int, Int>, onSelect: (Pair<Int, Int>) -> Unit, onCustom: () -> Unit) { Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("6 AM" to (6 to 0), "6:30 AM" to (6 to 30), "7 AM" to (7 to 0)).forEach { (label, value) -> TimeChip(label, value == time) { onSelect(value) } }; TimeChip("Custom", time !in listOf(6 to 0, 6 to 30, 7 to 0), onCustom) } }
@Composable private fun TimeChip(label: String, selected: Boolean, onClick: () -> Unit) { Text(label, color = if (selected) Cream else Ink, style = MakariosTypography.labelSmall, modifier = Modifier.clip(RoundedCornerShape(18.dp)).background(if (selected) Ink else Ink.copy(alpha = .07f)).clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 9.dp), maxLines = 1) }
@Composable private fun DayCircle(day: DayOfWeek, selected: Boolean, onClick: () -> Unit) { Box(Modifier.size(40.dp).clip(CircleShape).background(if (selected) Brush.verticalGradient(listOf(Light.Dawn.top, Light.Dawn.bottom)) else Brush.linearGradient(listOf(Ink.copy(alpha = .06f), Ink.copy(alpha = .06f)))).border(1.dp, if (selected) AnchorLight else Color.Transparent, CircleShape).clickable(onClick = onClick), contentAlignment = Alignment.Center) { Text(day.name.take(1), color = Ink, style = MakariosTypography.labelSmall) } }
@Composable private fun NotificationPreview() { val light = Light.Dawn; val a = AffirmationRepository.affirmationOfTheDay; Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).drawBehind { drawLight(light, size, horizon = HorizonSpec(.78f, .84f, .9f), discScale = .55f) }.padding(16.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.NotificationsNone, "Notification preview", tint = light.text); Spacer(Modifier.width(8.dp)); Text("Good morning", color = light.text, style = MakariosTypography.labelLarge) }; Spacer(Modifier.height(8.dp)); Text(a.declaration, color = light.text, style = MakariosTypography.bodyLarge, maxLines = 2); Text(a.reference, color = light.text.copy(alpha = light.secondaryAlpha), style = MakariosTypography.labelSmall) } }

@Composable
fun NotificationSoftAsk(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { NotificationStore.markAsked(context) }
    val a = remember { AffirmationRepository.affirmationOfTheDay }
    var time by remember { mutableStateOf(NotificationStore.morningTime(context)) }
    AlertDialog(onDismissRequest = { NotificationStore.markAsked(context); onDismiss() }, title = { Text("Let it find you each morning.", style = MakariosTypography.displaySmall, color = Ink) }, text = { Column { Text("One declaration, at the hour you choose. Nothing else. No streak guilt, no marketing.", color = Ink.copy(alpha = .72f)); Spacer(Modifier.height(16.dp)); Text("What time?", style = MakariosTypography.labelLarge, color = Ink); NotificationTimeChips(time, { time = it; NotificationStore.setMorningTime(context, it.first, it.second) }) {}; Spacer(Modifier.height(12.dp)); NotificationPreview() } }, confirmButton = { Button(onClick = { NotificationStore.markAsked(context); if (android.os.Build.VERSION.SDK_INT >= 33) launcher.launch(Manifest.permission.POST_NOTIFICATIONS) else ReminderManager.setDailyReminderEnabled(context, true); onDismiss() }, colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Cream), shape = RoundedCornerShape(22.dp)) { Text("Turn on") } }, dismissButton = { TextButton(onClick = { NotificationStore.markAsked(context); onDismiss() }) { Text("Not now", color = Ink) } }, containerColor = Color(0xFFFFF4D6))
}