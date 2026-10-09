package com.makarios.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentLight = remember { getCurrentLightForTime() } // or pinned
    var name by remember { mutableStateOf("Friend") }
    var handle by remember { mutableStateOf("@friend") }
    var email by remember { mutableStateOf("user@makarios.app") }
    
    var dailyDeclEnabled by remember { mutableStateOf(true) }
    var selectedTime by remember { mutableStateOf("8:30 AM") }

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
        
        // Monogram
        Box(
            modifier = Modifier.size(72.dp).clip(CircleShape).lightBackground(currentLight),
            contentAlignment = Alignment.Center
        ) {
            val initial = if (name == "Friend") "m" else name.take(1)
            Text(initial, style = MakariosTypography.displayMedium.copy(fontSize = 32.sp), color = currentLight.text)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (name == "Friend") {
            Text("Add your name", style = MakariosTypography.displaySmall, color = Ink, modifier = Modifier.clickable { })
        } else {
            Text(name, style = MakariosTypography.displaySmall, color = Ink)
            Text(handle, style = MakariosTypography.bodyMedium, color = Ink.copy(alpha = 0.7f))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = { /* Edit profile */ },
            modifier = Modifier.height(40.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink),
            border = androidx.compose.foundation.BorderStroke(1.dp, Ink.copy(alpha = 0.2f))
        ) {
            Text("Edit profile", style = MakariosTypography.labelLarge)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("3 kept · 1 written · 13 in library", style = MakariosTypography.bodyMedium, color = Ink) // lining figures in font
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Invite a friend
        Text("Invite a friend", style = MakariosTypography.bodyLarge, color = Ink)
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Ink.copy(alpha = 0.05f)).padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text("XR79K", style = MakariosTypography.bodyMedium.copy(fontSize = 20.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp), color = Ink)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = { },
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink),
                border = androidx.compose.foundation.BorderStroke(1.dp, Ink.copy(alpha = 0.2f))
            ) {
                Text("Copy code", style = MakariosTypography.labelLarge)
            }
            Button(
                onClick = { },
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Cream)
            ) {
                Text("Share link", style = MakariosTypography.labelLarge)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Your circle
        Text("Your circle", style = MakariosTypography.bodyLarge, color = Ink)
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Avatar stack placeholder
            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Light.Dusk.top))
            Box(modifier = Modifier.offset(x = (-8).dp).size(32.dp).clip(CircleShape).background(Light.Mist.top))
            Box(modifier = Modifier.offset(x = (-16).dp).size(32.dp).clip(CircleShape).background(Light.Dawn.top), contentAlignment = Alignment.Center) {
                Text("+2", style = MakariosTypography.labelSmall, color = Ink)
            }
            Spacer(modifier = Modifier.width(16.dp))
            OutlinedButton(onClick = { }, modifier = Modifier.height(40.dp)) {
                Text("Add a friend", style = MakariosTypography.labelLarge)
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Daily declaration
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Daily declaration", style = MakariosTypography.bodyLarge, color = Ink, modifier = Modifier.weight(1f))
            Switch(
                checked = dailyDeclEnabled,
                onCheckedChange = { dailyDeclEnabled = it },
                colors = SwitchDefaults.colors(checkedThumbColor = Cream, checkedTrackColor = Ink)
            )
        }
        if (dailyDeclEnabled) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(selectedTime, style = MakariosTypography.displaySmall, color = Ink, modifier = Modifier.clickable { /* open TimePicker */ })
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Dawn 6:30", "Morning 8:30", "Midday 12:30", "Evening 18:00").forEach { preset ->
                    Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Ink.copy(alpha = 0.05f)).clickable { selectedTime = preset.split(" ")[1] + if (preset.startsWith("E")) " PM" else " AM" }.padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Text(preset, style = MakariosTypography.labelSmall, color = Ink)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Light", style = MakariosTypography.bodyLarge, color = Ink, modifier = Modifier.weight(1f))
            Text("Auto", style = MakariosTypography.bodyMedium, color = Ink.copy(alpha = 0.7f)) // Or pinned
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Account
        Text("Account", style = MakariosTypography.bodyLarge, color = Ink)
        Spacer(modifier = Modifier.height(8.dp))
        Text(email, style = MakariosTypography.bodyMedium, color = Ink.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(androidx.compose.ui.graphics.Color(0xFF4A5A3C))) // green dot
            Spacer(modifier = Modifier.width(8.dp))
            Text("Backed up", style = MakariosTypography.labelSmall, color = Ink.copy(alpha = 0.7f))
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        TextButton(
            onClick = onSignOut,
            modifier = Modifier.height(48.dp).fillMaxWidth()
        ) {
            Text("Sign out", style = MakariosTypography.labelLarge, color = Ink.copy(alpha = 0.5f))
        }
    }
}
