package com.makarios.app.data

import androidx.test.core.app.ApplicationProvider
import com.makarios.app.ui.theme.Light
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UserLightStoreTest {
    @Before
    fun clearStore() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        context.filesDir.resolve("datastore/user_light.preferences_pb").delete()
    }

    @Test
    fun defaultsToAutoAndResolvesNamedLights() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        assertEquals(UserLightStore.AUTO, UserLightStore.preference(context).first())
        Light.values().forEach { light ->
            UserLightStore.set(context, light)
            assertEquals(light, UserLightStore.light(context).first())
        }
        UserLightStore.set(context, null)
        assertEquals(UserLightStore.AUTO, UserLightStore.preference(context).first())
    }

    @Test
    fun invalidStoredNameFallsBackToCurrentAutoLight() {
        assertEquals(Light.forNow(), UserLightStore.resolve("unknown-light"))
        assertEquals(Light.forNow(), UserLightStore.resolve(UserLightStore.AUTO))
    }
}


