package com.makarios.app.util

import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import androidx.test.runner.AndroidJUnit4
import com.makarios.app.ui.theme.Light
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationArtworkGoldenTest {
    @Test fun lightEngineBigPictureGoldenDimensions() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        Light.values().filter { it in listOf(Light.Dawn, Light.Midday, Light.Night) }.forEach { light ->
            val bitmap = NotificationArtwork.bigPicture(context, com.makarios.app.data.AffirmationRepository.affirmationOfTheDay, light)
            assertEquals(1024, bitmap.width)
            assertEquals(512, bitmap.height)
            assertNotEquals(0, bitmap.getPixel(bitmap.width / 2, bitmap.height / 2))
            bitmap.recycle()
        }
    }
}