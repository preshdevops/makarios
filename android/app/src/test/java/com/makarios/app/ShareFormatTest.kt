package com.makarios.app

import com.makarios.app.util.ExportFormat
import org.junit.Assert.assertEquals
import org.junit.Test

class ShareFormatTest {
 @Test fun exactPixels(){assertEquals(1080,ExportFormat.Story.width);assertEquals(1920,ExportFormat.Story.height);assertEquals(1080,ExportFormat.Square.width);assertEquals(1080,ExportFormat.Square.height);assertEquals(1080,ExportFormat.Portrait.width);assertEquals(1350,ExportFormat.Portrait.height);assertEquals(1600,ExportFormat.X.width);assertEquals(900,ExportFormat.X.height)}
 @Test fun wallpaperRemainsDeviceSized(){assertEquals(-1,ExportFormat.Wallpaper.width);assertEquals(-1,ExportFormat.Wallpaper.height)}
}
