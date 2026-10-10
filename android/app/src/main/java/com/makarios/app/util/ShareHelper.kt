package com.makarios.app.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.makarios.app.data.Affirmation
import com.makarios.app.ui.theme.Light
import com.makarios.app.ui.wallpaper.Style
import java.io.File

object ShareHelper {

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

    fun shareToSocialPlatform(
        context: Context,
        affirmation: Affirmation,
        light: Light,
        platform: SocialPlatform,
        style: Style? = null
    ) {
        val cacheDir = File(context.cacheDir, "images").apply { mkdirs() }
        val outputFile = File(cacheDir, "share_${System.currentTimeMillis()}.png")

        val renderer = LightCanvas(context)
        val renderedFile = renderer.render(
            declaration = affirmation.declaration,
            verseText = affirmation.scriptureText,
            verseReference = affirmation.reference,
            light = light,
            format = platform.defaultFormat,
            style = style,
            outputFile = outputFile
        )

        val contentUri = getFileProviderUri(context, renderedFile)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = if (renderedFile.name.endsWith(".jpg") || renderedFile.name.endsWith(".jpeg")) "image/jpeg" else "image/png"
            putExtra(Intent.EXTRA_STREAM, contentUri)
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
    }

    fun shareGeneric(
        context: Context,
        affirmation: Affirmation,
        light: Light,
        format: ExportFormat = ExportFormat.Portrait,
        style: Style? = null
    ) {
        val cacheDir = File(context.cacheDir, "images").apply { mkdirs() }
        val outputFile = File(cacheDir, "share_${System.currentTimeMillis()}.png")

        val renderer = LightCanvas(context)
        val renderedFile = renderer.render(
            declaration = affirmation.declaration,
            verseText = affirmation.scriptureText,
            verseReference = affirmation.reference,
            light = light,
            format = format,
            style = style,
            outputFile = outputFile
        )

        val contentUri = getFileProviderUri(context, renderedFile)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = if (renderedFile.name.endsWith(".jpg") || renderedFile.name.endsWith(".jpeg")) "image/jpeg" else "image/png"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val chooser = Intent.createChooser(shareIntent, "Share via")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun saveToPhotos(
        context: Context,
        affirmation: Affirmation,
        light: Light,
        format: ExportFormat = ExportFormat.Portrait,
        style: Style? = null
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
                    outputFile = file
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
