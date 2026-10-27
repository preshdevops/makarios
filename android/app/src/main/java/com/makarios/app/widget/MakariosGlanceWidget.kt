package com.makarios.app.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.layout.*
import com.makarios.app.MainActivity
import com.makarios.app.data.Affirmation
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.data.DeclarationPicker
import com.makarios.app.data.DeclarationSource
import com.makarios.app.data.SeedDeclarations
import com.makarios.app.data.WidgetLookMode
import com.makarios.app.data.WidgetSettingsStore
import com.makarios.app.data.local.KeptRepository
import com.makarios.app.data.local.toAffirmation
import com.makarios.app.ui.theme.Light
import com.makarios.app.util.DiagnosticStore
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.LocalTime
import kotlin.random.Random

// WidgetSource kept for backward compatibility with the existing WidgetConfigureActivity
enum class WidgetSource {
    TODAY,
    MY_DECLARATIONS,
    TOPIC,
    SURPRISE
}

object WidgetStore {
    private const val PREFS = "makarios_widget_prefs"
    private const val KEY_LIGHT_GLOBAL = "light_global"
    private const val KEY_SOURCE_GLOBAL = "source_global"
    private const val KEY_TOPIC_GLOBAL = "topic_global"
    private const val KEY_LAST_PICKED_PREFIX = "last_picked_"

    fun fixedLight(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LIGHT_GLOBAL, "AUTO") ?: "AUTO"
    }

    fun setFixedLight(context: Context, light: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LIGHT_GLOBAL, light).apply()
        WidgetScheduling.refreshNow(context)
    }

    fun light(context: Context, appWidgetId: Int? = null): Light {
        // Check per-widget settings first
        val widgetSettings = appWidgetId?.let { WidgetSettingsStore.loadForWidget(context, it) }
        if (widgetSettings != null && widgetSettings.lookMode == WidgetLookMode.FIXED_LIGHT) {
            val lightName = widgetSettings.fixedLightName
            if (lightName.isNotEmpty() && lightName != "AUTO") {
                return runCatching { Light.valueOf(lightName) }.getOrDefault(autoLightByTime())
            }
        }

        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val key = if (appWidgetId != null) "light_$appWidgetId" else KEY_LIGHT_GLOBAL
        val lightName = prefs.getString(key, null) ?: prefs.getString(KEY_LIGHT_GLOBAL, "AUTO") ?: "AUTO"
        return if (lightName == "AUTO") autoLightByTime() else runCatching { Light.valueOf(lightName) }.getOrDefault(Light.Dawn)
    }

    private fun autoLightByTime(): Light {
        val now = LocalTime.now()
        val minuteOfDay = now.hour * 60 + now.minute
        return when {
            minuteOfDay in (5 * 60 + 30) until (9 * 60) -> Light.Dawn
            minuteOfDay in (9 * 60) until (16 * 60 + 30) -> Light.Midday
            minuteOfDay in (16 * 60 + 30) until (19 * 60 + 30) -> Light.Dusk
            else -> Light.Night
        }
    }

    fun source(context: Context, appWidgetId: Int? = null): WidgetSource {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val key = if (appWidgetId != null) "source_$appWidgetId" else KEY_SOURCE_GLOBAL
        val name = prefs.getString(key, null) ?: prefs.getString(KEY_SOURCE_GLOBAL, WidgetSource.TODAY.name) ?: WidgetSource.TODAY.name
        return runCatching { WidgetSource.valueOf(name) }.getOrDefault(WidgetSource.TODAY)
    }

    fun topic(context: Context, appWidgetId: Int? = null): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val key = if (appWidgetId != null) "topic_$appWidgetId" else KEY_TOPIC_GLOBAL
        return prefs.getString(key, null) ?: prefs.getString(KEY_TOPIC_GLOBAL, "Identity") ?: "Identity"
    }

    fun setConfig(context: Context, appWidgetId: Int, light: String, source: WidgetSource, topic: String = "Identity") {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString("light_$appWidgetId", light)
            .putString("source_$appWidgetId", source.name)
            .putString("topic_$appWidgetId", topic)
            .putString(KEY_LIGHT_GLOBAL, light)
            .putString(KEY_SOURCE_GLOBAL, source.name)
            .putString(KEY_TOPIC_GLOBAL, topic)
            .apply()
        WidgetScheduling.refreshNow(context)
    }

    fun getLastPickedId(context: Context, appWidgetId: Int): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("${KEY_LAST_PICKED_PREFIX}$appWidgetId", null)

    fun setLastPickedId(context: Context, appWidgetId: Int, id: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString("${KEY_LAST_PICKED_PREFIX}$appWidgetId", id).apply()
        DiagnosticStore.setLastPickedId(context, id)
    }

    /**
     * Main entry point: uses DeclarationPicker with new WidgetSettings if available,
     * otherwise falls back to the old WidgetSource logic for backward compatibility.
     */
    fun affirmation(context: Context, isSmall: Boolean = false, appWidgetId: Int? = null): Affirmation {
        // Try new DeclarationPicker path (WidgetSettings)
        val widgetSettings = appWidgetId?.let { WidgetSettingsStore.loadForWidget(context, it) }
            ?: WidgetSettingsStore.load(context)

        // If default sources (MAKARIOS only) and no user customization — still use picker for consistency
        val excludeId = appWidgetId?.let { getLastPickedId(context, it) }

        val picked = runCatching {
            runBlocking {
                DeclarationPicker.pickForWidget(
                    context = context,
                    settings = widgetSettings,
                    appWidgetId = appWidgetId ?: 0,
                    excludeId = excludeId
                )
            }
        }.getOrNull()

        if (picked != null) {
            appWidgetId?.let { setLastPickedId(context, it, picked.id) }
            return if (isSmall) {
                // For small widgets prefer short declarations
                val shortText = com.makarios.app.data.widgetTextFor(picked.declaration, picked.shortText)
                if (shortText.length <= 45) picked
                else {
                    // Try to find a shorter one from the same pool
                    val seeds = SeedDeclarations.asAffirmations()
                    seeds.filter { com.makarios.app.data.widgetTextFor(it.declaration, it.shortText).length <= 32 }.firstOrNull() ?: picked
                }
            } else {
                picked
            }
        }

        // Legacy fallback path (should never be reached)
        return legacyAffirmation(context, isSmall, appWidgetId)
    }

    /** Kept for backward compatibility only */
    private fun legacyAffirmation(context: Context, isSmall: Boolean, appWidgetId: Int?): Affirmation {
        val src = source(context, appWidgetId)
        val seeds = SeedDeclarations.asAffirmations()
        val pool: List<Affirmation> = when (src) {
            WidgetSource.TODAY -> listOf(seeds.firstOrNull() ?: AffirmationRepository.affirmationOfTheDay)
            WidgetSource.MY_DECLARATIONS -> {
                val kept = runCatching {
                    runBlocking { KeptRepository.getAllList().map { it.toAffirmation() } }
                }.getOrDefault(emptyList())
                if (kept.isNotEmpty()) kept else seeds
            }
            WidgetSource.TOPIC -> {
                val top = topic(context, appWidgetId)
                val topicPairs = com.makarios.app.data.CuratedTopicRepository.getPairsForTopic(top)
                if (topicPairs.isNotEmpty()) topicPairs.map { p ->
                    Affirmation(id = p.id, declaration = p.declaration, scriptureText = "The word of the Lord stands forever.", reference = p.reference, context = top, category = top, tone = com.makarios.app.data.AffirmationTone.RESOLUTE)
                } else seeds
            }
            WidgetSource.SURPRISE -> seeds + AffirmationRepository.getAll()
        }
        val daySeed = LocalDate.now().dayOfYear + (appWidgetId ?: 0)
        return pool[Random(daySeed).nextInt(pool.size)]
    }
}

/**
 * Universal Responsive Makarios Glance Widget supporting 4 layouts:
 * 4x1 (Compact one-line), 2x2 (Small), 4x2 (Pairing), 4x4 (Scene)
 */
class MakariosGlanceWidget : GlanceAppWidget() {

    companion object {
        val SIZE_4X1 = DpSize(240.dp, 50.dp)
        val SIZE_2X2 = DpSize(140.dp, 140.dp)
        val SIZE_4X2 = DpSize(240.dp, 140.dp)
        val SIZE_4X4 = DpSize(240.dp, 260.dp)
    }

    override val sizeMode = SizeMode.Responsive(
        setOf(SIZE_4X1, SIZE_2X2, SIZE_4X2, SIZE_4X4)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val size = LocalSize.current
            val density = context.resources.displayMetrics.density

            val layoutType = when {
                size.height < 90.dp -> WidgetLayoutType.COMPACT_4X1
                size.width < 210.dp && size.height < 210.dp -> WidgetLayoutType.SMALL_2X2
                size.height < 210.dp -> WidgetLayoutType.PAIRING_4X2
                else -> WidgetLayoutType.SCENE_4X4
            }

            val appWidgetId = LocalGlanceId.current.hashCode()
            val isSmall = layoutType == WidgetLayoutType.SMALL_2X2
            val affirmation = WidgetStore.affirmation(context, isSmall = isSmall, appWidgetId = appWidgetId)
            val light = WidgetStore.light(context, appWidgetId)

            val widthPx = (size.width.value * density).toInt()
            val heightPx = (size.height.value * density).toInt()

            val idKey = androidx.glance.action.ActionParameters.Key<String>("id")
            val sourceKey = androidx.glance.action.ActionParameters.Key<String>("source")
            val launchAction = actionStartActivity(
                android.content.ComponentName(context, MainActivity::class.java),
                androidx.glance.action.actionParametersOf(idKey to affirmation.id, sourceKey to "widget")
            )

            val bitmap = runCatching {
                WidgetBitmapRenderer.render(
                    context = context,
                    affirmation = affirmation,
                    light = light,
                    widthPx = widthPx,
                    heightPx = heightPx,
                    layoutType = layoutType
                )
            }.getOrNull()

            val desc = "${affirmation.declaration}. ${affirmation.scriptureText} (${affirmation.reference})"

            if (bitmap != null) {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .cornerRadius(24.dp)
                        .clickable(launchAction)
                ) {
                    Image(
                        provider = ImageProvider(bitmap),
                        contentDescription = desc,
                        modifier = GlanceModifier.fillMaxSize()
                    )
                }
            } else {
                // Plain text fallback if bitmap rendering fails (never a blank widget)
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .cornerRadius(24.dp)
                        .padding(16.dp)
                        .clickable(launchAction),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    androidx.glance.text.Text(
                        text = com.makarios.app.data.widgetTextFor(affirmation.declaration, affirmation.shortText),
                        style = androidx.glance.text.TextStyle(
                            fontWeight = androidx.glance.text.FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    androidx.glance.text.Text(
                        text = affirmation.reference,
                        style = androidx.glance.text.TextStyle()
                    )
                }
            }
        }
    }
}

class MakariosCompactWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) = MakariosGlanceWidget().provideGlance(context, id)
}

class MakariosSmallWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) = MakariosGlanceWidget().provideGlance(context, id)
}

class MakariosMediumWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) = MakariosGlanceWidget().provideGlance(context, id)
}

class MakariosLargeWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) = MakariosGlanceWidget().provideGlance(context, id)
}

class MakariosCompactReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MakariosGlanceWidget()
}

class MakariosSmallReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MakariosGlanceWidget()
}

class MakariosMediumReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MakariosGlanceWidget()
}

class MakariosLargeReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MakariosGlanceWidget()
}
