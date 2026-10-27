package com.makarios.app.ui

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.unit.IntSize
import com.makarios.app.ui.theme.Light
import com.makarios.app.ui.wallpaper.StyleRegistry
import com.makarios.app.ui.wallpaper.StyleSpec
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.abs

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class PreviewQualityTest {

    @Test
    fun previewAndExportGoldenComparisonWithinThreePercentDiff() {
        val testStyles = listOf(
            StyleRegistry.PAIRING,
            StyleRegistry.WINDOWS,
            StyleRegistry.RAYS,
            StyleRegistry.PAIRING,
            StyleRegistry.PAPER
        )

        val spec = StyleSpec(
            declaration = "God is on my inside and his glory surrounds me.",
            verse = "To them God chose to make known how great among the Gentiles are the riches of the glory of this mystery, which is Christ in you, the hope of glory.",
            reference = "Colossians 1:27"
        )

        val exportW = 1080
        val exportH = 1920

        // Real preview resolution on a 2.625x density screen (200dp x 355dp)
        val previewW = 525
        val previewH = 933

        for (style in testStyles) {
            val exportBmp = style.render(spec, Light.Dawn, IntSize(exportW, exportH))
            val previewBmp = style.render(spec, Light.Dawn, IntSize(previewW, previewH))

            // Downscale export to preview size
            val scaledExport = Bitmap.createScaledBitmap(exportBmp, previewW, previewH, true)

            // Calculate mean pixel difference across all pixels
            val totalPixels = previewW * previewH
            var totalDiff = 0.0

            val exportPixels = IntArray(totalPixels)
            val previewPixels = IntArray(totalPixels)
            scaledExport.getPixels(exportPixels, 0, previewW, 0, 0, previewW, previewH)
            previewBmp.getPixels(previewPixels, 0, previewW, 0, 0, previewW, previewH)

            for (i in 0 until totalPixels) {
                val c1 = exportPixels[i]
                val c2 = previewPixels[i]

                val dr = abs(Color.red(c1) - Color.red(c2))
                val dg = abs(Color.green(c1) - Color.green(c2))
                val db = abs(Color.blue(c1) - Color.blue(c2))

                val pixelDiff = (dr + dg + db) / (3.0 * 255.0)
                totalDiff += pixelDiff
            }

            val meanDiffPercent = (totalDiff / totalPixels) * 100.0
            println("Style '${style.displayName}' Golden comparison mean diff: ${String.format("%.2f", meanDiffPercent)}%")

            assertTrue(
                "Style ${style.displayName} mean difference must be under 3.0%, but was ${String.format("%.2f", meanDiffPercent)}%",
                meanDiffPercent < 3.0
            )

            exportBmp.recycle()
            previewBmp.recycle()
            scaledExport.recycle()
        }
    }
}
