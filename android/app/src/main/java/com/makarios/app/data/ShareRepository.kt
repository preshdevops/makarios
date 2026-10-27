package com.makarios.app.data

import android.content.ClipData
import android.content.ClipboardManager
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
import com.makarios.app.util.WallpaperHelper
import com.makarios.app.util.WallpaperScreenTarget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class ShareResult {
    data class Success(val message: String, val uri: Uri? = null) : ShareResult()
    data class FellBackToText(val text: String) : ShareResult()
    data class Failed(val reason: String, val error: Throwable? = null) : ShareResult()
}

enum class ShareTarget(val label: String, val packageName: String?) {
    Chooser("More", null),
    WhatsAppChat("WhatsApp", "com.whatsapp"),
    WhatsAppStatus("WhatsApp Status", "com.whatsapp"),
    InstagramStories("Instagram Story", "com.instagram.android"),
    InstagramChat("Instagram", "com.instagram.android"),
    Facebook("Facebook", "com.facebook.katana"),
    X("X", "com.twitter.android"),
    Telegram("Telegram", "org.telegram.messenger"),
    Messages("Messages", null),
    Email("Email", null),
    SaveToGallery("Save to gallery", null),
    SetWallpaper("Set as wallpaper", null),
    CopyText("Copy text", null),
    CopyImage("Copy image", null);

    // Backward compatibility aliases
    companion object {
        val SaveToPhotos = SaveToGallery
    }
}

data class ShareRequest(
    val spec: StyleSpec,
    val light: Light,
    val format: ExportFormat,
    val style: Style,
    val target: ShareTarget = ShareTarget.Chooser,
    val wallpaperScreen: WallpaperScreenTarget = WallpaperScreenTarget.BOTH
)

object ShareRepository {

    private const val TAG = "ShareRepository"
    private const val FACEBOOK_APP_ID = "makarios_declaration_share"

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

    fun isAppInstalled(context: Context, packageName: String?): Boolean {
        if (packageName.isNullOrBlank()) return true
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(packageName, 0)
            }
            true
        }.getOrDefault(false)
    }

    fun isInstagramInstalled(context: Context): Boolean = isAppInstalled(context, "com.instagram.android")
    fun isWhatsAppInstalled(context: Context): Boolean = isAppInstalled(context, "com.whatsapp")

    suspend fun share(context: Context, request: ShareRequest): ShareResult = withContext(Dispatchers.Default) {
        cleanupOldShareFiles(context)

        // Handle text copy directly
        if (request.target == ShareTarget.CopyText) {
            val res = copyText(context, request)
            if (res is ShareResult.Success) {
                com.makarios.app.ui.screens.ProfileActivityLog.recordShared(context)
            }
            return@withContext res
        }

        // Render at exact format dimensions
        val requestedSize = when (request.format) {
            ExportFormat.Story -> androidx.compose.ui.unit.IntSize(1080, 1920)
            ExportFormat.Square -> androidx.compose.ui.unit.IntSize(1080, 1080)
            ExportFormat.Portrait -> androidx.compose.ui.unit.IntSize(1080, 1350)
            ExportFormat.X -> androidx.compose.ui.unit.IntSize(1600, 900)
            ExportFormat.Wallpaper -> {
                val m = WallpaperHelper.getMetrics(context, request.wallpaperScreen)
                androidx.compose.ui.unit.IntSize(m.renderWidth, m.renderHeight)
            }
        }

        var renderBitmap: Bitmap? = null
        try {
            renderBitmap = request.style.render(
                request.spec,
                request.light,
                requestedSize
            )

            if (renderBitmap == null) {
                Log.e(TAG, "Style render returned null bitmap, falling back to text")
                return@withContext fallbackToText(context, request)
            }

            // Save lossless PNG to cache
            val shareDir = File(context.cacheDir, "share").apply { mkdirs() }
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val file = File(shareDir, "makarios-$dateStr-${System.currentTimeMillis()}.png")

            FileOutputStream(file).use { out ->
                renderBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            val uri = getUriForFile(context, file)
            val caption = buildCaption(request)

            val shareOutcome = when (request.target) {
                ShareTarget.SaveToGallery -> {
                    val saved = saveToGallery(context, file)
                    renderBitmap.recycle()
                    if (saved) ShareResult.Success("Saved to gallery", uri) else ShareResult.Failed("Could not save to gallery")
                }
                ShareTarget.SetWallpaper -> {
                    val res = WallpaperHelper.applyWallpaper(context, renderBitmap, request.wallpaperScreen)
                    renderBitmap.recycle()
                    if (res.isSuccess) {
                        ShareResult.Success(res.getOrDefault("Wallpaper updated"), uri)
                    } else {
                        ShareResult.Failed("Could not set wallpaper")
                    }
                }
                ShareTarget.InstagramStories -> {
                    launchInstagramStories(context, uri, caption)
                    renderBitmap.recycle()
                    ShareResult.Success("Sent to Instagram Story", uri)
                }
                ShareTarget.InstagramChat -> {
                    launchPackageShare(context, uri, caption, "com.instagram.android", "Instagram")
                    renderBitmap.recycle()
                    ShareResult.Success("Sent to Instagram", uri)
                }
                ShareTarget.WhatsAppStatus -> {
                    launchWhatsAppStatus(context, uri, caption)
                    renderBitmap.recycle()
                    ShareResult.Success("Sent to WhatsApp Status", uri)
                }
                ShareTarget.WhatsAppChat -> {
                    launchPackageShare(context, uri, caption, "com.whatsapp", "WhatsApp")
                    renderBitmap.recycle()
                    ShareResult.Success("Sent to WhatsApp", uri)
                }
                ShareTarget.Facebook -> {
                    launchPackageShare(context, uri, caption, "com.facebook.katana", "Facebook")
                    renderBitmap.recycle()
                    ShareResult.Success("Sent to Facebook", uri)
                }
                ShareTarget.X -> {
                    launchPackageShare(context, uri, caption, "com.twitter.android", "X")
                    renderBitmap.recycle()
                    ShareResult.Success("Sent to X", uri)
                }
                ShareTarget.Telegram -> {
                    launchPackageShare(context, uri, caption, "org.telegram.messenger", "Telegram")
                    renderBitmap.recycle()
                    ShareResult.Success("Sent to Telegram", uri)
                }
                ShareTarget.Messages -> {
                    launchMessages(context, uri, caption)
                    renderBitmap.recycle()
                    ShareResult.Success("Sent to Messages", uri)
                }
                ShareTarget.Email -> {
                    launchEmail(context, uri, caption)
                    renderBitmap.recycle()
                    ShareResult.Success("Sent to Email", uri)
                }
                ShareTarget.CopyImage -> {
                    copyImage(context, uri)
                    renderBitmap.recycle()
                    ShareResult.Success("Image copied", uri)
                }
                ShareTarget.CopyText -> {
                    renderBitmap.recycle()
                    copyText(context, request)
                }
                ShareTarget.Chooser -> {
                    launchSystemChooser(context, uri, caption)
                    renderBitmap.recycle()
                    ShareResult.Success("Shared", uri)
                }
            }
            if (shareOutcome is ShareResult.Success) {
                com.makarios.app.ui.screens.ProfileActivityLog.recordShared(context)
            }
            shareOutcome
        } catch (oom: OutOfMemoryError) {
            Log.e(TAG, "OutOfMemoryError in share pipeline, falling back to text: ${oom.message}")
            renderBitmap?.recycle()
            fallbackToText(context, request)
        } catch (e: Throwable) {
            Log.e(TAG, "Exception in share pipeline: ${e.message}", e)
            renderBitmap?.recycle()
            val appName = request.target.label
            ShareResult.Failed("Could not open $appName. Try More.", e)
        }
    }

    private fun getUriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    /**
     * Short caption: "<declaration>" , <reference>
     */
    fun buildCaption(request: ShareRequest): String {
        val decl = request.spec.declaration.trim()
        val ref = request.spec.reference?.trim()
        return if (!ref.isNullOrBlank()) {
            "\"$decl\" , $ref"
        } else {
            decl
        }
    }

    private fun copyText(context: Context, request: ShareRequest): ShareResult {
        return runCatching {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val text = buildCaption(request)
            cm.setPrimaryClip(ClipData.newPlainText("Makarios", text))
            ShareResult.Success("Text copied")
        }.getOrElse {
            ShareResult.Failed("Could not copy text: ${it.message}", it)
        }
    }

    private fun copyImage(context: Context, uri: Uri) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newUri(context.contentResolver, "Makarios Image", uri))
    }

    private fun fallbackToText(context: Context, request: ShareRequest): ShareResult {
        val text = buildCaption(request)
        return runCatching {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "Share").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            ShareResult.FellBackToText(text)
        }.getOrElse {
            ShareResult.Failed("Could not share text: ${it.message}", it)
        }
    }

    private fun launchSystemChooser(context: Context, uri: Uri, caption: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, caption)
            clipData = ClipData.newRawUri("Makarios", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(intent, "Share").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    private fun launchPackageShare(context: Context, uri: Uri, caption: String, packageName: String, appName: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                setPackage(packageName)
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, caption)
                clipData = ClipData.newRawUri(appName, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Throwable) {
            launchSystemChooser(context, uri, caption)
        }
    }

    private fun launchInstagramStories(context: Context, uri: Uri, caption: String) {
        try {
            val storyIntent = Intent("com.instagram.share.ADD_TO_STORY").apply {
                type = "image/png"
                putExtra("background_asset_uri", uri)
                putExtra("source_application", FACEBOOK_APP_ID)
                clipData = ClipData.newRawUri("Instagram Story", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (context.packageManager.queryIntentActivities(storyIntent, PackageManager.MATCH_DEFAULT_ONLY).isNotEmpty()) {
                context.startActivity(storyIntent)
            } else {
                launchPackageShare(context, uri, caption, "com.instagram.android", "Instagram")
            }
        } catch (e: Throwable) {
            launchPackageShare(context, uri, caption, "com.instagram.android", "Instagram")
        }
    }

    private fun launchWhatsAppStatus(context: Context, uri: Uri, caption: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                setPackage("com.whatsapp")
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, caption)
                clipData = ClipData.newRawUri("WhatsApp Status", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Throwable) {
            launchSystemChooser(context, uri, caption)
        }
    }

    private fun launchMessages(context: Context, uri: Uri, caption: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, caption)
            clipData = ClipData.newRawUri("Messages", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(intent, "Send via Messages").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    private fun launchEmail(context: Context, uri: Uri, caption: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Makarios Declaration")
            putExtra(Intent.EXTRA_TEXT, caption)
            clipData = ClipData.newRawUri("Email", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(intent, "Send email").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    private fun saveToGallery(context: Context, file: File): Boolean {
        return runCatching {
            val resolver = context.contentResolver
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val filename = "makarios-$dateStr-${System.currentTimeMillis()}.png"

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
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
}
