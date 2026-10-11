package com.makarios.app.widget

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.makarios.app.data.Affirmation
import com.makarios.app.data.AffirmationTone
import com.makarios.app.ui.theme.Light
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class WidgetBitmapRendererTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    private val testAffirmation = Affirmation(
        id = "test-widget-1",
        declaration = "God is on my inside and His love never fails.",
        scriptureText = "Christ in you, the hope of glory.",
        reference = "Colossians 1:27",
        context = "Testing",
        category = "Identity",
        tone = AffirmationTone.RESOLUTE
    )

    @Test
    fun rendersAllFourWidgetLayouts() {
        val density = context.resources.displayMetrics.density

        // 1. Compact 4x1 (260dp x 60dp)
        val bmp4x1 = WidgetBitmapRenderer.render(
            context = context,
            affirmation = testAffirmation,
            light = Light.Dawn,
            widthPx = (260 * density).toInt(),
            heightPx = (60 * density).toInt(),
            layoutType = WidgetLayoutType.COMPACT_4X1
        )
        assertNotNull(bmp4x1)
        assertTrue(bmp4x1.width > 0 && bmp4x1.height > 0)

        // 2. Small 2x2 (160dp x 160dp)
        val bmp2x2 = WidgetBitmapRenderer.render(
            context = context,
            affirmation = testAffirmation,
            light = Light.Grove,
            widthPx = (160 * density).toInt(),
            heightPx = (160 * density).toInt(),
            layoutType = WidgetLayoutType.SMALL_2X2
        )
        assertNotNull(bmp2x2)
        assertTrue(bmp2x2.width > 0 && bmp2x2.height > 0)

        // 3. Medium Pairing 4x2 (300dp x 150dp)
        val bmp4x2 = WidgetBitmapRenderer.render(
            context = context,
            affirmation = testAffirmation,
            light = Light.Midday,
            widthPx = (300 * density).toInt(),
            heightPx = (150 * density).toInt(),
            layoutType = WidgetLayoutType.PAIRING_4X2
        )
        assertNotNull(bmp4x2)
        assertTrue(bmp4x2.width > 0 && bmp4x2.height > 0)

        // 4. Large Scene 4x4 (300dp x 300dp)
        val bmp4x4 = WidgetBitmapRenderer.render(
            context = context,
            affirmation = testAffirmation,
            light = Light.Night,
            widthPx = (300 * density).toInt(),
            heightPx = (300 * density).toInt(),
            layoutType = WidgetLayoutType.SCENE_4X4
        )
        assertNotNull(bmp4x4)
        assertTrue(bmp4x4.width > 0 && bmp4x4.height > 0)
    }

    @Test
    fun capsOversizedBitmapBytesUnderRemoteViewsLimit() {
        // Enormous dimensions that would exceed RemoteViews 1.5MB limit (e.g. 2000x2000 x 4 = 16MB)
        val bmp = WidgetBitmapRenderer.render(
            context = context,
            affirmation = testAffirmation,
            light = Light.Dawn,
            widthPx = 2000,
            heightPx = 2000,
            layoutType = WidgetLayoutType.SCENE_4X4
        )
        val totalBytes = bmp.width * bmp.height * 4
        assertTrue("Total bitmap bytes must be capped under 1.5MB RemoteViews limit, was $totalBytes", totalBytes <= 1_500_000)
    }
}
