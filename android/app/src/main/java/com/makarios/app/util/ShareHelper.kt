package com.makarios.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import com.makarios.app.data.Affirmation
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ShareHelper {

    /**
     * Share affirmation as text.
     */
    fun shareAffirmation(context: Context, affirmation: Affirmation) {
        val shareText = buildString {
            append("“${affirmation.declaration}”\n\n")
            append("“${affirmation.scriptureText}”\n")
            append("— ${affirmation.reference}\n\n")
            append("Shared via Makarios · Sacred Declarations & Scripture")
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "Makarios — Daily Declaration")
            type = "text/plain"
        }

        val chooser = Intent.createChooser(sendIntent, "Share Declaration")
        context.startActivity(chooser)
    }

    /**
     * Share high-resolution rendered graphic with external apps (Stories, Messages, Socials).
     */
    fun shareAffirmationImage(
        context: Context,
        bitmap: Bitmap,
        title: String = "Makarios Declaration",
        captionText: String = ""
    ): Boolean {
        return try {
            val imagesFolder = File(context.cacheDir, "images").apply { mkdirs() }
            val imageFile = File(imagesFolder, "makarios_share_${System.currentTimeMillis()}.png")
            FileOutputStream(imageFile).use { out ->
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                    throw IOException("Failed to write bitmap to cache")
                }
            }

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                if (captionText.isNotBlank()) {
                    putExtra(Intent.EXTRA_TEXT, captionText)
                }
                putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share via")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Renders and shares an affirmation graphic in one shot.
     */
    fun shareAffirmationGraphic(
        context: Context,
        affirmation: Affirmation,
        styleIndex: Int = 0,
        format: WallpaperRenderer.OutputFormat = WallpaperRenderer.OutputFormat.STORY
    ): Boolean {
        val style = WallpaperRenderer.getStyle(styleIndex)
        val bitmap = WallpaperRenderer.renderBitmap(
            context = context,
            declaration = affirmation.declaration,
            scripture = affirmation.scriptureText,
            reference = affirmation.reference,
            category = affirmation.category,
            style = style,
            format = format
        )
        val caption = "“${affirmation.declaration}”\n— ${affirmation.reference}\n\nShared via Makarios"
        return shareAffirmationImage(context, bitmap, "Makarios — ${affirmation.category}", caption)
    }

    fun shareText(context: Context, title: String, text: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, title)
            type = "text/plain"
        }
        val chooser = Intent.createChooser(sendIntent, "Share via")
        context.startActivity(chooser)
    }
}
