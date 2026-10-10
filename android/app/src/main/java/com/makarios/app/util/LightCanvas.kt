package com.makarios.app.util

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.unit.IntSize
import com.makarios.app.ui.theme.Light
import com.makarios.app.ui.wallpaper.AutoStyleSelector
import com.makarios.app.ui.wallpaper.Style
import com.makarios.app.ui.wallpaper.StyleRegistry
import com.makarios.app.ui.wallpaper.StyleSpec
import com.makarios.app.ui.wallpaper.StyleTypefaces
import java.io.File
import java.io.FileOutputStream

/** Canonical exact-pixel share formats. */
enum class ExportFormat(val width: Int, val height: Int) {
    Square(1080, 1080), Portrait(1080, 1350), Story(1080, 1920), X(1600, 900), Wallpaper(-1, -1);

    companion object {
        fun from(name: String): ExportFormat = when (name.lowercase()) {
            "square" -> Square
            "portrait" -> Portrait
            "story", "whatsapp", "status" -> Story
            "x", "twitter" -> X
            "wallpaper" -> Wallpaper
            else -> Portrait
        }
    }
}

class LightCanvas(private val context: Context) {
    fun render(
        declaration: String,
        verseText: String,
        verseReference: String,
        light: Light,
        format: ExportFormat,
        style: Style? = null,
        deviceWidth: Int = 1080,
        deviceHeight: Int = 1920,
        outputFile: File
    ): File {
        StyleTypefaces.init(context)
        val width = if (format == ExportFormat.Wallpaper) deviceWidth else format.width
        val height = if (format == ExportFormat.Wallpaper) deviceHeight else format.height

        val resolvedStyle = style ?: AutoStyleSelector.pickStyle(
            declaration = declaration,
            verse = verseText,
            reference = verseReference,
            light = light
        )

        val spec = StyleSpec(
            declaration = declaration,
            verse = verseText,
            reference = verseReference
        )

        val bitmap = resolvedStyle.render(spec, light, IntSize(width, height))

        val png = if (outputFile.extension.lowercase() == "png") outputFile else File(outputFile.parentFile, outputFile.nameWithoutExtension + ".png")
        FileOutputStream(png).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        if (png.length() > 8L * 1024L * 1024L) {
            val jpg = File(png.parentFile, png.nameWithoutExtension + ".jpg")
            FileOutputStream(jpg).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 92, it) }
            png.delete()
            bitmap.recycle()
            return jpg
        }
        bitmap.recycle()
        return png
    }
}
