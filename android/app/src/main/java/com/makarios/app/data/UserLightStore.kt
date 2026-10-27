package com.makarios.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.makarios.app.ui.theme.Light
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userLightDataStore by preferencesDataStore(name = "user_light")

object UserLightStore {
    private val lightKey = stringPreferencesKey("light")
    const val AUTO = "AUTO"

    fun preference(context: Context): Flow<String> =
        context.applicationContext.userLightDataStore.data.map { it[lightKey] ?: AUTO }

    fun light(context: Context): Flow<Light> = preference(context).map(::resolve)

    suspend fun set(context: Context, light: Light?) {
        context.applicationContext.userLightDataStore.edit { values ->
            values[lightKey] = light?.name ?: AUTO
        }
    }

    fun resolve(value: String): Light =
        if (value == AUTO) Light.forNow() else runCatching { Light.valueOf(value) }.getOrDefault(Light.forNow())
}
