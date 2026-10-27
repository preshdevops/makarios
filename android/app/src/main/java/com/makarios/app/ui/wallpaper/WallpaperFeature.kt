package com.makarios.app.ui.wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

class WallpaperRenderer(private val context: Context) {
    suspend fun preview(spec: WallpaperSpec, width: Int, height: Int): Bitmap = withContext(Dispatchers.Default) {
        val key = "${spec.style.id}:${spec.light.name}:$width:$height:${spec.declaration.hashCode()}:${spec.verse?.text.hashCode()}"
        val cacheKey = "wallpaper:$key"
        com.makarios.app.util.BitmapMemoryCache.getOrPut(context, cacheKey) {
            renderWallpaper(context, spec, width, height)
        }
    }

    suspend fun export(spec: WallpaperSpec): Bitmap = withContext(Dispatchers.Default) {
        val dm = context.resources.displayMetrics
        renderWallpaper(context, spec, dm.widthPixels, dm.heightPixels)
    }

    fun clear() {
        com.makarios.app.util.BitmapMemoryCache.clear()
    }
}

enum class WallpaperTarget { LOCK, HOME, BOTH }

fun applyWallpaper(context: Context, bitmap: Bitmap, target: WallpaperTarget) {
    val manager = WallpaperManager.getInstance(context)
    val flags = when (target) {
        WallpaperTarget.LOCK -> WallpaperManager.FLAG_LOCK
        WallpaperTarget.HOME -> WallpaperManager.FLAG_SYSTEM
        WallpaperTarget.BOTH -> WallpaperManager.FLAG_LOCK or WallpaperManager.FLAG_SYSTEM
    }
    manager.suggestDesiredDimensions(bitmap.width, bitmap.height)
    android.util.Log.d("Wallpaper", "wallpaper render ${bitmap.width}x${bitmap.height} set ${bitmap.width}x${bitmap.height}")
    manager.setBitmap(bitmap, android.graphics.Rect(0, 0, bitmap.width, bitmap.height), true, flags)
}

@Composable
fun WallpaperGallery(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedLight by remember { mutableStateOf(Light.forNow()) }
    val renderer = remember { WallpaperRenderer(context) }
    var selectedStyle by remember { mutableStateOf<Style?>(null) }
    var target by remember { mutableStateOf(WallpaperTarget.LOCK) }
    var showTarget by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(selectedLight.bottom)
            .padding(horizontal = 20.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 28.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Wallpapers",
                style = MakariosTypography.displaySmall,
                color = selectedLight.text,
                modifier = Modifier.weight(1f)
            )
            Text(
                "Close",
                color = selectedLight.text,
                style = MakariosTypography.labelLarge,
                modifier = Modifier.clickable { onBack() }
            )
        }

        // 12 styles in 2-column grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            items(StyleRegistry.all.distinctBy { it.id }, key = { "wallpaper-style-${it.id}" }) { style ->
                WallpaperTile(style, selectedLight, renderer) {
                    selectedStyle = style
                }
            }
        }
    }

    if (selectedStyle != null) {
        WallpaperPreview(
            style = selectedStyle ?: StyleRegistry.PAIRING,
            light = selectedLight,
            onLightChanged = { selectedLight = it },
            renderer = renderer,
            onClose = { selectedStyle = null },
            onSet = { showTarget = true }
        )
    }

    if (showTarget) {
        AlertDialog(
            onDismissRequest = { showTarget = false },
            title = { Text("Set wallpaper", style = MakariosTypography.titleLarge) },
            text = {
                Column {
                    Text("Choose where this artwork belongs.", style = MakariosTypography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    WallpaperTarget.values().forEach { t ->
                        Text(
                            t.name.lowercase().replaceFirstChar { it.uppercase() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    target = t
                                    showTarget = false
                                    val chosen = selectedStyle
                                    selectedStyle = null
                                    if (chosen != null) {
                                        scope.launch {
                                            val bmp = renderer.export(defaultWallpaperSpec(chosen, selectedLight))
                                            try {
                                                applyWallpaper(context, bmp, target)
                                            } finally {
                                                if (!bmp.isRecycled) bmp.recycle()
                                            }
                                        }
                                    }
                                }
                                .padding(vertical = 14.dp),
                            style = MakariosTypography.labelLarge
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTarget = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun WallpaperTile(
    style: Style,
    light: Light,
    renderer: WallpaperRenderer,
    onClick: () -> Unit
) {
    val bitmap by produceState<Bitmap?>(null, style.id, light.name) {
        // 1/4 scale render in coroutine
        value = renderer.preview(defaultWallpaperSpec(style, light), 108, 234)
    }
    DisposableEffect(bitmap) {
        com.makarios.app.util.BitmapMemoryCache.pin(bitmap)
        onDispose { com.makarios.app.util.BitmapMemoryCache.unpin(bitmap) }
    }

    Column(Modifier.clickable { onClick() }) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(9f / 19.5f)
                .clip(RoundedCornerShape(20.dp))
                .background(light.top),
            contentAlignment = Alignment.Center
        ) {
            bitmap?.let {
                androidx.compose.foundation.Image(
                    it.asImageBitmap(),
                    contentDescription = style.displayName,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Text(
            style.displayName,
            style = MakariosTypography.bodyLarge,
            color = light.text,
            modifier = Modifier.padding(top = 7.dp)
        )
    }
}

@Composable
private fun WallpaperPreview(
    style: Style,
    light: Light,
    onLightChanged: (Light) -> Unit,
    renderer: WallpaperRenderer,
    onClose: () -> Unit,
    onSet: () -> Unit
) {
    // Preview at the exact aspect of this phone, rendered by the same function used on apply.
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val deviceAspect = configuration.screenWidthDp.toFloat() / configuration.screenHeightDp.coerceAtLeast(1).toFloat()
    val bitmap by produceState<Bitmap?>(null, style.id, light.name, deviceAspect) {
        value = renderer.preview(defaultWallpaperSpec(style, light), 360, (360 / deviceAspect).toInt().coerceAtLeast(1))
    }
    DisposableEffect(bitmap) {
        com.makarios.app.util.BitmapMemoryCache.pin(bitmap)
        onDispose { com.makarios.app.util.BitmapMemoryCache.unpin(bitmap) }
    }

    Box(Modifier.fillMaxSize().background(light.bottom)) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    style.displayName,
                    style = MakariosTypography.displaySmall,
                    color = light.text,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "Done",
                    color = light.text,
                    style = MakariosTypography.labelLarge,
                    modifier = Modifier.clickable { onClose() }
                )
            }
            Spacer(Modifier.height(16.dp))
            Box(
                Modifier
                    .fillMaxHeight(0.72f)
                    .aspectRatio(deviceAspect)
                    .clip(RoundedCornerShape(24.dp))
                    .background(light.top)
            ) {
                bitmap?.let {
                    androidx.compose.foundation.Image(
                        it.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Text("Light", color = light.text, style = MakariosTypography.labelLarge)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Light.values().forEach { l ->
                    Box(
                        Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(l.top)
                            .border(if (l == light) 2.dp else 0.dp, if (l == light) light.text else androidx.compose.ui.graphics.Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { onLightChanged(l) }
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = onSet,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = light.buttonFill, contentColor = light.buttonText)
            ) {
                Text("Set wallpaper", style = MakariosTypography.labelLarge)
            }
        }
    }
}
