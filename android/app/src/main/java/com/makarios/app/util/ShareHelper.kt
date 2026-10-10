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

    /**
     * Deletes files in cacheDir/share older than 24 hours.
     */
    fun cleanOldShareFiles(shareDir: File) {
        try {
            val cutoff = System.currentTimeMillis() - (24 * 60 * 60 * 1000L)
            shareDir.listFiles()?.forEach { file ->
                if (file.lastModified() < cutoff) {
                    file.delete()
                }
            }
        } catch (_: Exception) {}
    }

    /**
     * Fallback to sharing plain text when image preparation fails.
     */
    fun sharePlainTextFallback(context: Context, affirmation: Affirmation) {
        val text = buildString {
            appendLine(affirmation.declaration)
            appendLine()
            appendLine("\"${affirmation.scriptureText}\"")
            appendLine(affirmation.reference)
            appendLine()
            append("Shared via Makarios: https://makarios.app")
        }
        val textIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(textIntent, "Share declaration")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun shareToSocialPlatform(
        context: Context,
        affirmation: Affirmation,
        light: Light,
        platform: SocialPlatform,
        style: Style? = null
    ) {
        try {
            val isJpegTarget = platform == SocialPlatform.WHATSAPP_STATUS ||
                    platform == SocialPlatform.INSTAGRAM_STORY ||
                    platform == SocialPlatform.INSTAGRAM_POST

            val renderedFile = synchronized(renderLock) {
                val shareDir = File(context.cacheDir, "share").apply { mkdirs() }
                cleanOldShareFiles(shareDir)
                val ext = if (isJpegTarget) "jpg" else "png"
                val outputFile = File(shareDir, "share_${System.currentTimeMillis()}.$ext")

                val renderer = LightCanvas(context)
                renderer.render(
                    declaration = affirmation.declaration,
                    verseText = affirmation.scriptureText,
                    verseReference = affirmation.reference,
                    light = light,
                    format = platform.defaultFormat,
                    style = style,
                    outputFile = outputFile,
                    asJpeg = isJpegTarget,
                    jpegQuality = 95
                )
            }

            val contentUri = getFileProviderUri(context, renderedFile)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = if (renderedFile.name.endsWith(".jpg") || renderedFile.name.endsWith(".jpeg")) "image/jpeg" else "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                clipData = ClipData.newRawUri("", contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (isAppInstalled(context, platform.packageName)) {
                shareIntent.setPackage(platform.packageName)
                context.startActivity(shareIntent)
            } else {
                Toast.makeText(context, "Opening share sheet (${platform.displayName} not installed)", Toast.LENGTH_SHORT).show()
                val chooser = Intent.createChooser(shareIntent, "Share to ${platform.displayName}")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            }
        } catch (e: Throwable) {
            Toast.makeText(context, "Could not prepare image to share", Toast.LENGTH_SHORT).show()
            sharePlainTextFallback(context, affirmation)
        }
    }

    fun shareGeneric(
        context: Context,
        affirmation: Affirmation,
        light: Light,
        format: ExportFormat = ExportFormat.Portrait,
        style: Style? = null
    ) {
        try {
            val renderedFile = synchronized(renderLock) {
                val shareDir = File(context.cacheDir, "share").apply { mkdirs() }
                cleanOldShareFiles(shareDir)
                val outputFile = File(shareDir, "share_${System.currentTimeMillis()}.png")

                val renderer = LightCanvas(context)
                renderer.render(
                    declaration = affirmation.declaration,
                    verseText = affirmation.scriptureText,
                    verseReference = affirmation.reference,
                    light = light,
                    format = format,
                    style = style,
                    outputFile = outputFile,
                    asJpeg = false
                )
            }

            val contentUri = getFileProviderUri(context, renderedFile)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = if (renderedFile.name.endsWith(".jpg") || renderedFile.name.endsWith(".jpeg")) "image/jpeg" else "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                clipData = ClipData.newRawUri("", contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "Share via")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Throwable) {
            Toast.makeText(context, "Could not prepare image to share", Toast.LENGTH_SHORT).show()
            sharePlainTextFallback(context, affirmation)
        }
    }

    /**
     * Item I: Shares lossless PNG as a document file so WhatsApp and other apps do not recompress.
     */
    fun shareAsFile(
        context: Context,
        affirmation: Affirmation,
        light: Light,
        format: ExportFormat = ExportFormat.Portrait,
        style: Style? = null
    ) {
        try {
            val renderedFile = synchronized(renderLock) {
                val shareDir = File(context.cacheDir, "share").apply { mkdirs() }
                cleanOldShareFiles(shareDir)
                val outputFile = File(shareDir, "document_${System.currentTimeMillis()}.png")

                val renderer = LightCanvas(context)
                renderer.render(
                    declaration = affirmation.declaration,
                    verseText = affirmation.scriptureText,
                    verseReference = affirmation.reference,
                    light = light,
                    format = format,
                    style = style,
                    outputFile = outputFile,
                    asJpeg = false
                )
            }

            val contentUri = getFileProviderUri(context, renderedFile)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                clipData = ClipData.newRawUri("", contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "Share as file")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Throwable) {
            Toast.makeText(context, "Could not prepare file to share", Toast.LENGTH_SHORT).show()
            sharePlainTextFallback(context, affirmation)
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

    private fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: Exception) {
            false
        }
    }
}
