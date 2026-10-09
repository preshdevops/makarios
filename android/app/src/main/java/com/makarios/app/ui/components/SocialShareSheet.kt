package com.makarios.app.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.makarios.app.data.Affirmation
import com.makarios.app.ui.theme.*
import com.makarios.app.util.ImageEngine
import com.makarios.app.util.ImageOutputFormat
import com.makarios.app.util.ShareHelper
import com.makarios.app.util.ShareHelper.SocialPlatform
import com.makarios.app.util.WallpaperRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * SocialShareSheet
 *
 * Impeccable, sacred social media sharing sheet.
 * Features platform-specific format previews and direct targeted sharing to:
 * - Instagram Stories (9:16 with safe zones)
 * - Instagram Post (1:1 archival card)
 * - Snapchat (9:16 frosted lens sticker)
 * - X / Twitter (16:9 broadsheet pull-quote)
 * - WhatsApp (4:5 devotional blessing card)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialShareSheet(
    affirmation: Affirmation,
    initialStyleIndex: Int = 0,
    photoUrl: String? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedPlatform by remember { mutableStateOf(SocialPlatform.INSTAGRAM_STORY) }
    var selectedStyleIndex by remember { mutableIntStateOf(initialStyleIndex) }
    var isGenerating by remember { mutableStateOf(false) }
    var hasCopiedText by remember { mutableStateOf(false) }

    val activePhotoUrl = photoUrl ?: affirmation.imageUrl.takeIf { it.isNotBlank() && !it.startsWith("drawable:") }
    val currentStyle = WallpaperRenderer.getStyle(selectedStyleIndex)

    val platforms = listOf(
        SocialPlatform.INSTAGRAM_STORY,
        SocialPlatform.INSTAGRAM_POST,
        SocialPlatform.SNAPCHAT,
        SocialPlatform.X_TWITTER,
        SocialPlatform.WHATSAPP
    )

    val exportGraphicsLayer = rememberGraphicsLayer()
    val engineFormat = remember(selectedPlatform) {
        ImageOutputFormat.fromSocialPlatform(selectedPlatform)
    }
    var decodedPhotoBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(activePhotoUrl, engineFormat) {
        if (activePhotoUrl != null) {
            decodedPhotoBitmap = ImageEngine.decodePhotoAtTargetSize(
                context = context,
                url = activePhotoUrl,
                targetWidth = engineFormat.width,
                targetHeight = engineFormat.height
            )
        } else {
            decodedPhotoBitmap = null
        }
    }

    suspend fun obtainExportBitmap(): Bitmap {
        val layerBitmap: Bitmap? = try {
            val imageBitmap = exportGraphicsLayer.toImageBitmap()
            val bmp = imageBitmap.asAndroidBitmap()
            if (bmp.width >= 1080 && bmp.height >= 1080) {
                Log.d("ImageEngine", "Exported image dimensions: ${bmp.width}x${bmp.height} (GraphicsLayer offscreen)")
                bmp
            } else null
        } catch (e: Exception) {
            Log.w("ImageEngine", "GraphicsLayer offscreen capture fallback: ${e.message}")
            null
        }

        if (layerBitmap != null) {
            return layerBitmap
        }

        val photoBmp: Bitmap? = decodedPhotoBitmap ?: withContext(Dispatchers.IO) {
            activePhotoUrl?.let {
                ImageEngine.decodePhotoAtTargetSize(context, it, engineFormat.width, engineFormat.height)
            }
        }

        return withContext(Dispatchers.IO) {
            ImageEngine.renderCanvasBitmap(
                context = context,
                declaration = affirmation.declaration,
                scripture = affirmation.scriptureText,
                reference = affirmation.reference,
                category = affirmation.category,
                style = currentStyle,
                format = engineFormat,
                photoBitmap = photoBmp
            )
        }
    }

    fun shareCurrentSelection() {
        if (isGenerating) return
        isGenerating = true
        coroutineScope.launch {
            try {
                val bitmap = obtainExportBitmap()
                val caption = "“${affirmation.declaration}”\n\n“${affirmation.scriptureText}”\n— ${affirmation.reference}"
                ShareHelper.shareToSocialPlatform(context, bitmap, selectedPlatform, caption)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Couldn't generate graphic — please retry", Toast.LENGTH_SHORT).show()
            } finally {
                isGenerating = false
            }
        }
    }

    fun saveToGallery() {
        if (isGenerating) return
        isGenerating = true
        coroutineScope.launch {
            try {
                val bitmap = obtainExportBitmap()
                val uri = withContext(Dispatchers.IO) {
                    WallpaperRenderer.saveToGallery(context, bitmap, "Makarios_${selectedPlatform.id}")
                }

                if (uri != null) {
                    Toast.makeText(context, "Saved to your Photos ✓", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Unable to save image", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isGenerating = false
            }
        }
    }

    fun copyTextToClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val text = buildString {
            append("“${affirmation.declaration}”\n\n")
            append("“${affirmation.scriptureText}”\n")
            append("— ${affirmation.reference}\n\n")
            append("Makarios · Sacred Declarations & Scripture")
        }
        val clip = ClipData.newPlainText("Makarios Declaration", text)
        clipboard.setPrimaryClip(clip)
        hasCopiedText = true
        Toast.makeText(context, "Declaration & Scripture copied ✓", Toast.LENGTH_SHORT).show()
        coroutineScope.launch {
            delay(2000)
            hasCopiedText = false
        }
    }

    fun shareViaSystemChooser() {
        if (isGenerating) return
        isGenerating = true
        coroutineScope.launch {
            try {
                val bitmap = obtainExportBitmap()
                val caption = "“${affirmation.declaration}”\n— ${affirmation.reference}"
                ShareHelper.shareAffirmationImage(context, bitmap, "Makarios — ${affirmation.category}", caption)
            } finally {
                isGenerating = false
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Porcelain,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(42.dp)
                    .height(4.5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Border)
            )
        }
    ) {
        // Dedicated offscreen export host: rendered at fixed pixel sizes (density 3.0), never captures visible UI
        OffscreenExportHost(
            affirmation = affirmation,
            format = engineFormat,
            photoBitmap = decodedPhotoBitmap,
            style = currentStyle,
            graphicsLayer = exportGraphicsLayer
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
        ) {
            // ── Header ──────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Share this declaration",
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 21.sp,
                        letterSpacing = (-0.3).sp,
                        color = Espresso
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Choose a platform to share to",
                        fontFamily = BodyFontFamily,
                        fontSize = 12.5.sp,
                        color = Stone
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Surface)
                        .border(1.dp, Border, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Espresso,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ── Platform Format Selector (Horizontal Chips) ─────────
            Text(
                text = "SHARE TO",
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                letterSpacing = 1.4.sp,
                color = StoneMuted
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                platforms.forEach { platform ->
                    val isSelected = selectedPlatform == platform

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Espresso else Surface)
                            .border(1.dp, if (isSelected) Espresso else Border, RoundedCornerShape(20.dp))
                            .clickable { selectedPlatform = platform }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = platform.iconResId),
                                contentDescription = platform.displayName,
                                tint = if (isSelected) Color.White else Color(platform.brandColorHex).copy(alpha = 0.6f),
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = platform.displayName,
                                fontFamily = BodyFontFamily,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 13.sp,
                                color = if (isSelected) Color.White else Espresso
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ── Live Format Preview ─────────────────────────────────
            val previewRatio = when (selectedPlatform) {
                SocialPlatform.INSTAGRAM_STORY, SocialPlatform.SNAPCHAT -> 9f / 16f
                SocialPlatform.INSTAGRAM_POST -> 1f
                SocialPlatform.X_TWITTER -> 16f / 9f
                SocialPlatform.WHATSAPP -> 4f / 5f
            }

            val previewHeight = when (selectedPlatform) {
                SocialPlatform.X_TWITTER -> 190.dp
                SocialPlatform.INSTAGRAM_POST -> 250.dp
                SocialPlatform.WHATSAPP -> 270.dp
                else -> 310.dp
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(previewHeight)
                    .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = Espresso.copy(alpha = 0.16f))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(currentStyle.backgroundColors[0])),
                contentAlignment = Alignment.Center
            ) {
                if (activePhotoUrl != null) {
                    AsyncImage(
                        model = activePhotoUrl,
                        contentDescription = "Background",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Protective scrim
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x44000000),
                                        Color(0x77000000),
                                        Color(0xCC0E0B08)
                                    )
                                )
                            )
                    )
                } else {
                    // Gradient fill
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = currentStyle.backgroundColors.map { Color(it) }
                                )
                            )
                    )
                }

                // Decorative platform overlay
                when (selectedPlatform) {
                    SocialPlatform.X_TWITTER -> {
                        // Landscape Pull-Quote Preview
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Accent Bar
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .fillMaxHeight(0.85f)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(currentStyle.accentColor))
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "“${affirmation.declaration}”",
                                    fontFamily = DisplayFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 17.sp,
                                    lineHeight = 23.sp,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    color = if (activePhotoUrl != null) Color.White else Color(currentStyle.primaryTextColor)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "“${affirmation.scriptureText}”",
                                    fontFamily = DisplayFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = if (activePhotoUrl != null) Color(0xEEFFFFFF) else Color(currentStyle.secondaryTextColor)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "— ${affirmation.reference}",
                                    fontFamily = BodyFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color(currentStyle.accentColor)
                                )
                            }
                        }
                    }

                    SocialPlatform.INSTAGRAM_POST -> {
                        // Archival Museum Post Preview with Double Border
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                                .border(1.dp, Color(currentStyle.accentColor).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(6.dp)
                                .border(0.5.dp, Color(currentStyle.accentColor).copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = affirmation.category.uppercase(),
                                    fontFamily = BodyFontFamily,
                                    fontSize = 9.sp,
                                    letterSpacing = 1.2.sp,
                                    color = Color(currentStyle.accentColor)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "“${affirmation.declaration}”",
                                    fontFamily = DisplayFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp,
                                    lineHeight = 24.sp,
                                    textAlign = TextAlign.Center,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    color = if (activePhotoUrl != null) Color.White else Color(currentStyle.primaryTextColor)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.Black.copy(alpha = 0.14f))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "◆ ${affirmation.reference} ◆",
                                        fontFamily = BodyFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.5.sp,
                                        color = Color(currentStyle.accentColor)
                                    )
                                }
                            }
                        }
                    }

                    SocialPlatform.SNAPCHAT -> {
                        // Frosted Sticker Container Preview
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(18.dp)
                                .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.3f))
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (activePhotoUrl != null) Color(0xCC1A1614) else Color(0xDDFFFFFF))
                                .border(1.5.dp, Color(currentStyle.accentColor).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = affirmation.category.uppercase(),
                                    fontFamily = BodyFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 9.sp,
                                    letterSpacing = 1.2.sp,
                                    color = Color(currentStyle.accentColor)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "“${affirmation.declaration}”",
                                    fontFamily = DisplayFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp,
                                    lineHeight = 24.sp,
                                    textAlign = TextAlign.Center,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    color = if (activePhotoUrl != null) Color.White else Color(0xFF2C2622)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "— ${affirmation.reference} —",
                                    fontFamily = BodyFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = Color(currentStyle.accentColor)
                                )
                            }
                        }
                    }

                    else -> {
                        // Vertical Story / WhatsApp Blessing Preview
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = affirmation.category.uppercase(),
                                fontFamily = BodyFontFamily,
                                fontSize = 9.sp,
                                letterSpacing = 1.2.sp,
                                color = Color(currentStyle.accentColor)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "“${affirmation.declaration}”",
                                fontFamily = DisplayFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 19.sp,
                                lineHeight = 26.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis,
                                color = if (activePhotoUrl != null) Color.White else Color(currentStyle.primaryTextColor)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black.copy(alpha = 0.18f))
                                    .border(0.5.dp, Color(currentStyle.accentColor).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "“${affirmation.scriptureText}”",
                                        fontFamily = DisplayFontFamily,
                                        fontSize = 11.5.sp,
                                        lineHeight = 16.sp,
                                        textAlign = TextAlign.Center,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        color = if (activePhotoUrl != null) Color(0xEEFFFFFF) else Color(currentStyle.secondaryTextColor)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "— ${affirmation.reference} —",
                                        fontFamily = BodyFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = Color(currentStyle.accentColor)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Style Theme Switcher ────────────────────────────────
            if (activePhotoUrl == null) {
                Text(
                    text = "STYLE",
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    letterSpacing = 1.4.sp,
                    color = StoneMuted
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WallpaperRenderer.STYLES.forEachIndexed { index, style ->
                        val isSelected = selectedStyleIndex == index
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(style.backgroundColors[0]))
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) Olive else Border,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedStyleIndex = index },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (style.isDark) Color.White else Espresso,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
            }

            // ── Primary Action: Share to Selected Platform ──────────
            Button(
                onClick = ::shareCurrentSelection,
                enabled = !isGenerating,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Olive,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Preparing…",
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = selectedPlatform.iconResId),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Share · ${selectedPlatform.displayName}",
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Secondary Actions (Save Image, Copy Text, More) ─────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Save to Gallery
                OutlinedButton(
                    onClick = ::saveToGallery,
                    enabled = !isGenerating,
                    border = BorderStroke(1.dp, Border),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Surface),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = Espresso,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Save",
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.5.sp,
                            color = Espresso
                        )
                    }
                }

                // Copy Text
                OutlinedButton(
                    onClick = ::copyTextToClipboard,
                    border = BorderStroke(1.dp, Border),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Surface),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (hasCopiedText) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = if (hasCopiedText) Sage else Espresso,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (hasCopiedText) "Copied" else "Copy",
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.5.sp,
                            color = if (hasCopiedText) Sage else Espresso
                        )
                    }
                }

                // System Chooser (Other Apps)
                OutlinedButton(
                    onClick = ::shareViaSystemChooser,
                    enabled = !isGenerating,
                    border = BorderStroke(1.dp, Border),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Surface),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.IosShare,
                            contentDescription = null,
                            tint = Espresso,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "More…",
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
}
