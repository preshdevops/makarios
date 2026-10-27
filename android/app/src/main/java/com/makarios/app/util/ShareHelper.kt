package com.makarios.app.util

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.makarios.app.data.Affirmation
import com.makarios.app.ui.theme.Light
import com.makarios.app.ui.wallpaper.Style
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

object ShareHelper {

    private val renderLock = Any()

    enum class SocialPlatform(
        val displayName: String,
        val packageName: String,
        val defaultFormat: ExportFormat
    ) {
        WHATSAPP_STATUS("WhatsApp", "com.whatsapp", ExportFormat.Story),
        INSTAGRAM_STORY("Instagram Story", "com.instagram.android", ExportFormat.Story),
        INSTAGRAM_POST("Instagram Post", "com.instagram.android", ExportFormat.Square),
        SNAPCHAT("Snapchat", "com.snapchat.android", ExportFormat.Story),
        X_TWITTER("X", "com.twitter.android", ExportFormat.X)
    }

    private fun getFileProviderUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun cleanOldShareFiles(dir: File) {
        if (!dir.exists() || !dir.isDirectory) return
        val cutoff = System.currentTimeMillis() - (24 * 60 * 60 * 1000L)
        dir.listFiles()?.forEach { file ->
            if (file.lastModified() < cutoff) {
                file.delete()
            }
        }
    }

    fun saveToPhotos(
        context: Context,
        affirmation: Affirmation,
        light: Light,
        format: ExportFormat = ExportFormat.Portrait,
        style: Style? = null,
        scale2x: Boolean = true
    ): Uri? {
        val values = android.content.ContentValues().apply {
            put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, "makarios_${System.currentTimeMillis()}.png")
            put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Makarios")
            put(android.provider.MediaStore.Images.Media.IS_PENDING, 1)
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return null
        return runCatching {
            resolver.openOutputStream(uri)?.use { out ->
                val file = File(context.cacheDir, "media_${System.currentTimeMillis()}.png")
                val rendered = LightCanvas(context).render(
                    declaration = affirmation.declaration,
                    verseText = affirmation.scriptureText,
                    verseReference = affirmation.reference,
                    light = light,
                    format = format,
                    style = style,
                    outputFile = file,
                    scaleMultiplier = if (scale2x) 2f else 1f,
                    asJpeg = false
                )
                rendered.inputStream().use { it.copyTo(out) }
                rendered.delete()
            }
            resolver.update(uri, android.content.ContentValues().apply { put(android.provider.MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
            uri
        }.getOrElse { resolver.delete(uri, null, null); null }
    }

    fun shareGeneric(context: Context, affirmation: Affirmation, light: Light = Light.Dawn) {
        val caption = "\"${affirmation.declaration}\"\n\n${affirmation.scriptureText} (${affirmation.reference})"
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, caption)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            val chooser = Intent.createChooser(sendIntent, "Share declaration").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (_: Exception) {
            Toast.makeText(context, "Could not open share sheet", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: Exception) {
            false
        }
    }
}
