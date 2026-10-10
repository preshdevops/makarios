package com.makarios.app.ui.components

import android.app.WallpaperManager
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.makarios.app.data.Affirmation
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.ui.theme.*
import com.makarios.app.util.ExportFormat
import com.makarios.app.util.LightCanvas
import com.makarios.app.util.ShareHelper
import java.io.File

@Composable
fun AffirmationDetailDialog(
    affirmation: Affirmation,
    light: Light,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isSaved by remember { mutableStateOf(AffirmationRepository.isSaved(affirmation.id)) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Ink)
        ) {
            // High-resolution background photo
            if (affirmation.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = affirmation.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    colorFilter = WarmPhotoGrade
                )
            }

            // Dark gradient overlay for contrast and contemplation
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color.Black.copy(alpha = 0.5f),
                            0.4f to Color.Black.copy(alpha = 0.65f),
                            0.8f to Color.Black.copy(alpha = 0.92f),
                            1.0f to Color.Black
                        )
                    )
            )

            // Content Column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(top = 48.dp, bottom = 32.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top close bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = affirmation.category.uppercase(),
                        style = MakariosTypography.labelSmall,
                        color = Cream.copy(alpha = 0.7f),
                        letterSpacing = 2.sp
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Cream
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Declaration & Anchor
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = affirmation.declaration,
                        style = MakariosTypography.displayMedium,
                        color = Cream
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Anchor Rule
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height(2.dp)
                            .background(AnchorDark)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = affirmation.scriptureText,
                        style = MakariosTypography.bodyLarge,
                        color = Cream.copy(alpha = 0.88f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = affirmation.reference,
                        style = MakariosTypography.labelMedium,
                        color = Cream.copy(alpha = 0.70f)
                    )

                    if (affirmation.context.isNotBlank()) {
                        Spacer(modifier = Modifier.height(32.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = affirmation.context,
                                style = MakariosTypography.bodyMedium,
                                color = Cream.copy(alpha = 0.85f),
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Save / Keep Button
                    Button(
                        onClick = {
                            AffirmationRepository.toggleSave(affirmation.id)
                            isSaved = !isSaved
                            Toast.makeText(
                                context,
                                if (isSaved) "Kept in your library" else "Removed from Kept",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSaved) Cream else Color.White.copy(alpha = 0.2f),
                            contentColor = if (isSaved) Ink else Cream
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isSaved) "Kept" else "Keep", style = MakariosTypography.labelLarge)
                    }

                    // Set Wallpaper Button
                    IconButton(
                        onClick = {
                            try {
                                val wm = WallpaperManager.getInstance(context)
                                val renderer = LightCanvas(context)
                                val file = File(context.cacheDir, "wallpaper_${System.currentTimeMillis()}.png")
                                renderer.render(
                                    declaration = affirmation.declaration,
                                    verseText = affirmation.scriptureText,
                                    verseReference = affirmation.reference,
                                    light = light,
                                    format = ExportFormat.Wallpaper,
                                    outputFile = file
                                )
                                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                                wm.setBitmap(bitmap)
                                Toast.makeText(context, "Wallpaper set successfully", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Could not set wallpaper", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Wallpaper,
                            contentDescription = "Set Wallpaper",
                            tint = Cream
                        )
                    }

                    // Share Button
                    IconButton(
                        onClick = {
                            ShareHelper.shareGeneric(
                                context = context,
                                affirmation = affirmation,
                                light = light
                            )
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Cream
                        )
                    }
                }
            }
        }
    }
}
