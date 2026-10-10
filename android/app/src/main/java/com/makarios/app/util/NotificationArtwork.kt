package com.makarios.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Typeface
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.makarios.app.R
import com.makarios.app.data.Affirmation
import com.makarios.app.ui.theme.Light
import com.makarios.app.ui.theme.renderLightBitmap
import java.io.File

object NotificationArtwork {
    fun bigPicture(context: Context, affirmation: Affirmation, light: Light): Bitmap = renderLightBitmap(light, 1024, 512) {
        drawIntoCanvas { scope ->
            val canvas = scope.nativeCanvas
            val serif = ResourcesCompat.getFont(context, R.font.newsreader) ?: Typeface.SERIF
            val sans = ResourcesCompat.getFont(context, R.font.hanken_grotesk) ?: Typeface.DEFAULT
            val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = light.text.toArgb(); typeface = serif; textSize = 54f; isSubpixelText = true }
            val maxWidth = 500
            var layout = android.text.StaticLayout.Builder.obtain(affirmation.declaration, 0, affirmation.declaration.length, paint, maxWidth).setIncludePad(false).setMaxLines(Int.MAX_VALUE).setEllipsize(null).build()
            while (layout.height > 300 && paint.textSize > 18f) { paint.textSize -= 2f; layout = android.text.StaticLayout.Builder.obtain(affirmation.declaration, 0, affirmation.declaration.length, paint, maxWidth).setIncludePad(false).setMaxLines(Int.MAX_VALUE).setEllipsize(null).build() }
            canvas.save(); canvas.translate(48f, 40f); layout.draw(canvas); canvas.restore()
            val ruleY = (40f + layout.height + 18f).coerceAtMost(360f)
            val rule = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = light.anchorRule.toArgb() }
            canvas.drawRect(48f, ruleY, 68f, ruleY + 2f, rule)
            val ref = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = light.text.copy(alpha = light.secondaryAlpha).toArgb(); typeface = sans; textSize = 22f; letterSpacing = .08f }
            canvas.drawText(affirmation.reference, 48f, ruleY + 40f, ref)
        }
    }
    fun tile(context: Context, light: Light): Bitmap {
        val bitmap = renderLightBitmap(light, 144, 144) { }
        Canvas(bitmap).apply { drawRoundRect(RectF(0f, 0f, 144f, 144f), 36f, 36f, Paint(Paint.ANTI_ALIAS_FLAG).apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN); color = android.graphics.Color.WHITE }) }
        return bitmap
    }
    fun cacheFile(context: Context, affirmation: Affirmation, light: Light): File = File(context.cacheDir, "notification_${affirmation.id}_${light.name}.png")
}