package com.makarios.app.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.widget.Toast
import androidx.core.content.FileProvider
import com.makarios.app.R
import com.makarios.app.data.Affirmation
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ShareHelper {

    enum class SocialPlatform(
        val id: String,
        val displayName: String,
        val packageName: String,
        val defaultFormat: WallpaperRenderer.OutputFormat,
        val iconResId: Int,
        val brandColorHex: Long
    ) {
        INSTAGRAM_STORY(
            id = "instagram_story",
            displayName = "Instagram Story",
            packageName = "com.instagram.android",
            defaultFormat = WallpaperRenderer.OutputFormat.STORY,
            iconResId = R.drawable.ic_instagram,
            brandColorHex = 0xFFE1306C
        ),
        INSTAGRAM_POST(
            id = "instagram_post",
            displayName = "Instagram Post",
            packageName = "com.instagram.android",
            defaultFormat = WallpaperRenderer.OutputFormat.SQUARE,
            iconResId = R.drawable.ic_instagram,
            brandColorHex = 0xFFC13584
        ),
        SNAPCHAT(
            id = "snapchat",
            displayName = "Snapchat",
            packageName = "com.snapchat.android",
            defaultFormat = WallpaperRenderer.OutputFormat.SNAPCHAT,
            iconResId = R.drawable.ic_snapchat,
            brandColorHex = 0xFFFFFC00
        ),
        X_TWITTER(
            id = "x_twitter",
            displayName = "X (Twitter)",
            packageName = "com.twitter.android",
            defaultFormat = WallpaperRenderer.OutputFormat.X_CARD,
            iconResId = R.drawable.ic_x_twitter,
            brandColorHex = 0xFF14171A
        ),
        WHATSAPP(
            id = "whatsapp",
            displayName = "WhatsApp",
            packageName = "com.whatsapp",
            defaultFormat = WallpaperRenderer.OutputFormat.STATUS,
            iconResId = R.drawable.ic_whatsapp,
            brandColorHex = 0xFF25D366
        )
    }

    /**
     * Checks if a third-party app package is installed on the device.
     */
    fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Share high-resolution graphic directly to a chosen social media platform.
     * If the app is installed, targets it directly; otherwise falls back gracefully
     * to the system share sheet with an informative message.
     */
    fun shareToSocialPlatform(
        context: Context,
        bitmap: Bitmap,
        platform: SocialPlatform,
        captionText: String = ""
    ): Boolean {
        return try {
            val imageFile = ImageEngine.exportToCache(
                context = context,
                bitmap = bitmap,
                format = Bitmap.CompressFormat.PNG,
                prefix = "makarios_${platform.id}"
            )
            val contentUri = ImageEngine.getShareUri(context, imageFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                if (captionText.isNotBlank()) {
                    putExtra(Intent.EXTRA_TEXT, captionText)
                }
                putExtra(Intent.EXTRA_SUBJECT, "Makarios — Sacred Declaration")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (isAppInstalled(context, platform.packageName)) {
                shareIntent.setPackage(platform.packageName)
                context.startActivity(shareIntent)
            } else {
                Toast.makeText(
                    context,
                    "Opening share sheet (${platform.displayName} not installed)",
                    Toast.LENGTH_SHORT
                ).show()
                val chooser = Intent.createChooser(shareIntent, "Share to ${platform.displayName}")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to standard chooser on any exception
            try {
                shareAffirmationImage(context, bitmap, "Makarios Declaration", captionText)
            } catch (ignored: Exception) {
                false
            }
        }
    }

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
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
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
            val imageFile = ImageEngine.exportToCache(
                context = context,
                bitmap = bitmap,
                format = Bitmap.CompressFormat.PNG,
                prefix = "makarios_share"
            )
            val contentUri = ImageEngine.getShareUri(context, imageFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                if (captionText.isNotBlank()) {
                    putExtra(Intent.EXTRA_TEXT, captionText)
                }
                putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
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
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
