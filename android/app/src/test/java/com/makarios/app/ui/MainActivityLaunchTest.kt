package com.makarios.app.ui

import androidx.test.core.app.ApplicationProvider
import com.makarios.app.MainActivity
import com.makarios.app.MakariosApp
import com.makarios.app.data.bible.BibleRepository
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = MakariosApp::class)
class MainActivityLaunchTest {

    @Before
    fun setup() {
        val app = ApplicationProvider.getApplicationContext<MakariosApp>()
        kotlinx.coroutines.runBlocking {
            BibleRepository.initialize(app)
        }
    }

    @Test
    fun testActivityLaunchDoesNotCrash() {
        val controller = Robolectric.buildActivity(MainActivity::class.java)
        val activity = controller.create().start().resume().visible().get()
        assertNotNull(activity)
        assertFalse(activity.isFinishing)
        assertFalse(activity.isDestroyed)
    }
}
