package com.makarios.app.data

import android.app.WallpaperManager
import android.content.ClipData
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import com.makarios.app.ui.theme.Light
import com.makarios.app.ui.wallpaper.Style
import com.makarios.app.ui.wallpaper.StyleRegistry
import com.makarios.app.ui.wallpaper.StyleSpec
import com.makarios.app.util.ExportFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

sealed class ShareResult {
    data class Success(val message: String, val uri: Uri? = null) : ShareResult()
    data class FellBackToText(val text: String) : ShareResult()
    data class Failed(val reason: String, val error: Throwable? = null) : ShareResult()
}

enum class ShareTarget {
    Chooser,
    ShareAsFile,
    SaveToPhotos,
    SetWallpaper,
    InstagramStories,
    WhatsAppStatus
}

data class ShareRequest(
    val spec: StyleSpec,
    val light: Light,
    val format: ExportFormat,
    val style: Style,
    val target: ShareTarget = ShareTarget.Chooser
)

object ShareRepository {

    private const val TAG = "ShareRepository"
    private const val FACEBOOK_APP_ID = "makarios_declaration_share"

    /**
     * Deletes temporary share files older than 24 hours.
     */
    fun cleanupOldShareFiles(context: Context) {
        runCatching {
            val shareDir = File(context.cacheDir, "share")
            if (!shareDir.exists()) return
            val cutoff = System.currentTimeMillis() - (24 * 60 * 60 * 1000L)
            shareDir.listFiles()?.forEach { file ->
                if (file.lastModified() < cutoff) {
                    file.delete()
                }
            }
        }.onFailure { Log.w(TAG, "Cleanup failed: ${it.message}") }
    }

    /**
     * Checks if Instagram is installed and supports Stories share intent.
     */
    fun isInstagramInstalled(context: Context): Boolean {
        return runCatching {
            val intent = Intent("com.instagram.share.ADD_TO_STORY").apply {
                type = "image/*"
            }
            context.packageManager.queryIntentActivities(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            ).isNotEmpty()
        }.getOrDefault(false)
    }

    /**
     * Checks if WhatsApp is installed.
     */
    fun isWhatsAppInstalled(context: Context): Boolean {
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo("com.whatsapp", PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo("com.whatsapp", 0)
            }
            true
        }.getOrDefault(false)
    }

    /**
     * Main fail-safe sharing entry point.
     * Never crashes, never exposes Bitmaps or URIs directly to the UI layer.
     */
    suspend fun share(context: Context, request: ShareRequest): ShareResult = withContext(Dispatchers.Default) {
        cleanupOldShareFiles(context)

        // Memory Guard: estimate available heap
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val availableMemory = maxMemory - usedMemory

        val requestedSize = if (request.format.width > 0 && request.format.height > 0) {
            androidx.compose.ui.unit.IntSize(request.format.width, request.format.height)
        } else {
            androidx.compose.ui.unit.IntSize(1080, 1920)
        }
        val isSticker = request.style.id == StyleRegistry.STICKER.id
        val isPngRequired = isSticker || request.target == ShareTarget.ShareAsFile || request.target == ShareTarget.SaveToPhotos

        var scaleMultiplier = 1.0f
        val uncompressedBytes = requestedSize.width * requestedSize.height * 4L
        if (availableMemory < uncompressedBytes * 3L) {
            Log.w(TAG, "Low heap available ($availableMemory bytes), dropping scale to 0.75x")
            scaleMultiplier = 0.75f
        }

        var renderBitmap: Bitmap? = null
        try {
            val renderWidth = (requestedSize.width * scaleMultiplier).roundToInt().coerceAtLeast(200)
            val renderHeight = (requestedSize.height * scaleMultiplier).roundToInt().coerceAtLeast(200)

            renderBitmap = request.style.render(
                request.spec,
                request.light,
                androidx.compose.ui.unit.IntSize(renderWidth, renderHeight)
            )

            if (renderBitmap == null) {
                Log.e(TAG, "Style render returned null bitmap, falling back to text")
                return@withContext fallbackToText(context, request)
            }

            // Save to temporary cache file
            val shareDir = File(context.cacheDir, "share").apply { mkdirs() }
            val ext = if (isPngRequired) "png" else "jpg"
            val file = File(shareDir, "share_${System.currentTimeMillis()}.$ext")

            FileOutputStream(file).use { out ->
                if (isPngRequired) {
                    renderBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                } else {
                    renderBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }
            }

            // Execute target action
            when (request.target) {
                ShareTarget.SaveToPhotos -> {
                    val saved = saveToPhotos(context, file, isPngRequired)
                    renderBitmap.recycle()
                    if (saved) ShareResult.Success("Saved to Photos") else ShareResult.Failed("Could not save to photos")
                }
                ShareTarget.SetWallpaper -> {
                    val applied = setWallpaper(context, file)
                    renderBitmap.recycle()
                    if (applied) ShareResult.Success("Wallpaper updated") else ShareResult.Failed("Could not set wallpaper")
                }
                ShareTarget.InstagramStories -> {
                    val uri = getUriForFile(context, file)
                    launchInstagramStories(context, uri, request, isSticker)
                    renderBitmap.recycle()
                    ShareResult.Success("Sent to Instagram Stories", uri)
                }
                ShareTarget.WhatsAppStatus -> {
                    val uri = getUriForFile(context, file)
                    launchWhatsAppStatus(context, uri, request, isPngRequired)
                    renderBitmap.recycle()
                    ShareResult.Success("Sent to WhatsApp", uri)
                }
                ShareTarget.ShareAsFile -> {
                    val uri = getUriForFile(context, file)
                    launchSystemChooser(context, uri, request, isPng = true, asDocument = true)
                    renderBitmap.recycle()
                    ShareResult.Success("Shared as file", uri)
                }
                ShareTarget.Chooser -> {
                    val uri = getUriForFile(context, file)
                    launchSystemChooser(context, uri, request, isPng = isPngRequired, asDocument = false)
                    renderBitmap.recycle()
                    ShareResult.Success("Shared", uri)
                }
            }
        } catch (oom: OutOfMemoryError) {
            Log.e(TAG, "OutOfMemoryError in share pipeline, falling back to text: ${oom.message}")
            renderBitmap?.recycle()
            fallbackToText(context, request)
        } catch (e: Throwable) {
            Log.e(TAG, "Exception in share pipeline: ${e.message}", e)
            renderBitmap?.recycle()
            fallbackToText(context, request)
        }
    }

    private fun getUriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    private fun buildCaption(request: ShareRequest): String {
        return buildString {
            appendLine(request.spec.declaration)
            request.spec.verse?.let {
                appendLine()
                appendLine("\"$it\"")
            }
            request.spec.reference?.let {
                appendLine(it)
            }
            appendLine()
            append("Shared via Makarios: https://makarios.app")
        }
    }

    private fun fallbackToText(context: Context, request: ShareRequest): ShareResult {
        val text = buildCaption(request)
        return runCatching {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "Share declaration").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            ShareResult.FellBackToText(text)
        }.getOrElse {
            ShareResult.Failed("Could not share text: ${it.message}", it)
        }
    }

    private fun launchSystemChooser(
        context: Context,
        uri: Uri,
        request: ShareRequest,
        isPng: Boolean,
        asDocument: Boolean
    ) {
        val mimeType = if (isPng) "image/png" else "image/jpeg"
        val caption = buildCaption(request)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = if (asDocument) "application/octet-stream" else mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, caption)
            clipData = ClipData.newRawUri("Makarios Declaration", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(intent, "Share declaration").apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    private fun launchInstagramStories(
        context: Context,
        uri: Uri,
        request: ShareRequest,
        isSticker: Boolean
    ) {
        val intent = Intent("com.instagram.share.ADD_TO_STORY").apply {
            setPackage("com.instagram.android")
            val mimeType = if (isSticker) "image/png" else "image/jpeg"
            type = mimeType
            if (isSticker) {
                putExtra("interactive_asset_uri", uri)
                putExtra("top_background_color", "#FFF4E4")
                putExtra("bottom_background_color", "#E8A860")
            } else {
                putExtra("background_asset_uri", uri)
            }
            putExtra("source_application", FACEBOOK_APP_ID)
            clipData = ClipData.newRawUri("Instagram Story", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun launchWhatsAppStatus(
        context: Context,
        uri: Uri,
        request: ShareRequest,
        isPng: Boolean
    ) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            setPackage("com.whatsapp")
            type = if (isPng) "image/png" else "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, buildCaption(request))
            clipData = ClipData.newRawUri("WhatsApp Status", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun saveToPhotos(context: Context, file: File, isPng: Boolean): Boolean {
        return runCatching {
            val resolver = context.contentResolver
            val filename = "Makarios_${System.currentTimeMillis()}.${if (isPng) "png" else "jpg"}"
            val mimeType = if (isPng) "image/png" else "image/jpeg"

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Makarios")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: return false

            resolver.openOutputStream(imageUri)?.use { out ->
                file.inputStream().use { input ->
                    input.copyTo(out)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(imageUri, contentValues, null, null)
            }
            true
        }.getOrDefault(false)
    }

    private fun setWallpaper(context: Context, file: File): Boolean {
        return runCatching {
            val wm = WallpaperManager.getInstance(context)
            val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return false
            wm.setBitmap(bitmap)
            bitmap.recycle()
            true
        }.getOrDefault(false)
    }
}
