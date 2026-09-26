package com.makarios.app.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.makarios.app.ui.theme.*
import com.makarios.app.util.WallpaperRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * WallpaperActionDialog
 *
 * Exquisite dialog allowing users to set a declaration as their system wallpaper
 * (Lock Screen, Home Screen, Both) or download high-resolution graphics to their gallery.
 */
@Composable
fun WallpaperActionDialog(
    declaration: String,
    scripture: String,
    reference: String,
    category: String = "DECLARATION",
    styleIndex: Int = 0,
    photoUrl: String? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = { if (!isProcessing) onDismiss() }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Surface)
                .border(1.dp, Border, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sacred Wallpaper",
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        color = Espresso
                    )
                    IconButton(
                        onClick = onDismiss,
                        enabled = !isProcessing,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Stone,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Let God’s truth meet your eyes each time you unlock your device.",
                    fontFamily = BodyFontFamily,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = Stone,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (isProcessing) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = Terracotta,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = statusMessage ?: "Preparing sacred wallpaper…",
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = Espresso
                        )
                    }
                } else {
                    // Option 1: Lock Screen
                    WallpaperOptionItem(
                        icon = Icons.Default.Lock,
                        title = "Set on Lock Screen",
                        subtitle = "Optimal positioning below the clock",
                        onClick = {
                            isProcessing = true
                            statusMessage = "Setting Lock Screen wallpaper…"
                            coroutineScope.launch {
                                val success = withContext(Dispatchers.IO) {
                                    val photoBmp = if (!photoUrl.isNullOrBlank()) {
                                        WallpaperRenderer.fetchBitmapFromUrl(context, photoUrl)
                                    } else null
                                    val bitmap = WallpaperRenderer.renderBitmap(
                                        context = context,
                                        declaration = declaration,
                                        scripture = scripture,
                                        reference = reference,
                                        category = category,
                                        style = WallpaperRenderer.getStyle(styleIndex),
                                        format = WallpaperRenderer.OutputFormat.WALLPAPER,
                                        photoBitmap = photoBmp
                                    )
                                    WallpaperRenderer.setAsSystemWallpaper(
                                        context = context,
                                        bitmap = bitmap,
                                        target = WallpaperRenderer.WallpaperTarget.LOCK_SCREEN
                                    )
                                }
                                isProcessing = false
                                if (success) {
                                    Toast.makeText(context, "Lock screen wallpaper updated", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                } else {
                                    Toast.makeText(context, "Could not set wallpaper on this device", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Option 2: Home Screen
                    WallpaperOptionItem(
                        icon = Icons.Default.Home,
                        title = "Set on Home Screen",
                        subtitle = "Gentle backdrop behind your apps",
                        onClick = {
                            isProcessing = true
                            statusMessage = "Setting Home Screen wallpaper…"
                            coroutineScope.launch {
                                val success = withContext(Dispatchers.IO) {
                                    val photoBmp = if (!photoUrl.isNullOrBlank()) {
                                        WallpaperRenderer.fetchBitmapFromUrl(context, photoUrl)
                                    } else null
                                    val bitmap = WallpaperRenderer.renderBitmap(
                                        context = context,
                                        declaration = declaration,
                                        scripture = scripture,
                                        reference = reference,
                                        category = category,
                                        style = WallpaperRenderer.getStyle(styleIndex),
                                        format = WallpaperRenderer.OutputFormat.WALLPAPER,
                                        photoBitmap = photoBmp
                                    )
                                    WallpaperRenderer.setAsSystemWallpaper(
                                        context = context,
                                        bitmap = bitmap,
                                        target = WallpaperRenderer.WallpaperTarget.HOME_SCREEN
                                    )
                                }
                                isProcessing = false
                                if (success) {
                                    Toast.makeText(context, "Home screen wallpaper updated", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                } else {
                                    Toast.makeText(context, "Could not set wallpaper on this device", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Option 3: Both Screens
                    WallpaperOptionItem(
                        icon = Icons.Default.Wallpaper,
                        title = "Set on Both Screens",
                        subtitle = "Unified sacred sanctuary display",
                        onClick = {
                            isProcessing = true
                            statusMessage = "Applying to Lock & Home screens…"
                            coroutineScope.launch {
                                val success = withContext(Dispatchers.IO) {
                                    val photoBmp = if (!photoUrl.isNullOrBlank()) {
                                        WallpaperRenderer.fetchBitmapFromUrl(context, photoUrl)
                                    } else null
                                    val bitmap = WallpaperRenderer.renderBitmap(
                                        context = context,
                                        declaration = declaration,
                                        scripture = scripture,
                                        reference = reference,
                                        category = category,
                                        style = WallpaperRenderer.getStyle(styleIndex),
                                        format = WallpaperRenderer.OutputFormat.WALLPAPER,
                                        photoBitmap = photoBmp
                                    )
                                    WallpaperRenderer.setAsSystemWallpaper(
                                        context = context,
                                        bitmap = bitmap,
                                        target = WallpaperRenderer.WallpaperTarget.BOTH
                                    )
                                }
                                isProcessing = false
                                if (success) {
                                    Toast.makeText(context, "Wallpaper set on both screens", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                } else {
                                    Toast.makeText(context, "Could not set wallpaper on this device", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Option 4: Save to Photos (Gallery download)
                    WallpaperOptionItem(
                        icon = Icons.Default.Download,
                        title = "Save to Photos (Download)",
                        subtitle = "High-resolution PNG saved to Pictures/Makarios",
                        accentColor = Terracotta,
                        onClick = {
                            isProcessing = true
                            statusMessage = "Saving high-res wallpaper…"
                            coroutineScope.launch {
                                val uri = withContext(Dispatchers.IO) {
                                    val photoBmp = if (!photoUrl.isNullOrBlank()) {
                                        WallpaperRenderer.fetchBitmapFromUrl(context, photoUrl)
                                    } else null
                                    val bitmap = WallpaperRenderer.renderBitmap(
                                        context = context,
                                        declaration = declaration,
                                        scripture = scripture,
                                        reference = reference,
                                        category = category,
                                        style = WallpaperRenderer.getStyle(styleIndex),
                                        format = WallpaperRenderer.OutputFormat.WALLPAPER,
                                        photoBitmap = photoBmp
                                    )
                                    WallpaperRenderer.saveToGallery(
                                        context = context,
                                        bitmap = bitmap,
                                        title = "Makarios Wallpaper"
                                    )
                                }
                                isProcessing = false
                                if (uri != null) {
                                    Toast.makeText(context, "Saved to Photos in Makarios album", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                } else {
                                    Toast.makeText(context, "Failed to save image to gallery", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun WallpaperOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color = Espresso,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PorcelainWarm.copy(alpha = 0.5f))
            .border(0.5.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Surface)
                    .border(1.dp, Border, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(19.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.5.sp,
                    color = Espresso
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontFamily = BodyFontFamily,
                    fontSize = 11.5.sp,
                    color = Stone
                )
            }
        }
    }
}
