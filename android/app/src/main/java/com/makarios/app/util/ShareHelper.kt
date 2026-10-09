package com.makarios.app.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.makarios.app.data.Affirmation
import com.makarios.app.ui.theme.Light
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
        userPhotoUri: Uri? = null
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
            userPhotoUri = userPhotoUri,
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
        userPhotoUri: Uri? = null
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
            userPhotoUri = userPhotoUri,
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

    private fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: Exception) {
            false
        }
    }
}
