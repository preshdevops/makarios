package com.makarios.app

import com.makarios.app.ui.wallpaper.*
import com.makarios.app.ui.theme.Light
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WallpaperTest {
 @Test fun twelveStylesAreAvailable(){assertEquals(12,WallpaperStyle.values().size)}
 @Test fun curatedVerseSetHasTwelveEntries(){assertEquals(12,CuratedWallpaperVerses.size)}
 @Test fun textStylePreservesUserDeclaration(){val spec=WallpaperSpec(WallpaperStyle.TEXT,Light.Night,declaration="I am held.");assertEquals("I am held.",spec.declaration);assertTrue(spec.light.dark)}
}
