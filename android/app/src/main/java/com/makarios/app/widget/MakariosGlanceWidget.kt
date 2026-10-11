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
import com.makarios.app.data.SeedDeclarations
import com.makarios.app.data.local.KeptRepository
import com.makarios.app.data.local.toAffirmation
import com.makarios.app.ui.theme.Light
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.LocalTime
import kotlin.random.Random

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
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val key = if (appWidgetId != null) "light_$appWidgetId" else KEY_LIGHT_GLOBAL
        val lightName = prefs.getString(key, null) ?: prefs.getString(KEY_LIGHT_GLOBAL, "AUTO") ?: "AUTO"
        return if (lightName == "AUTO") autoLightByTime() else runCatching { Light.valueOf(lightName) }.getOrDefault(Light.Dawn)
    }

    private fun autoLightByTime(): Light {
        val now = LocalTime.now()
        val minuteOfDay = now.hour * 60 + now.minute
        return when {
            // 05:30 to 09:00 -> Dawn
            minuteOfDay in (5 * 60 + 30) until (9 * 60) -> Light.Dawn
            // 09:00 to 16:30 -> Midday / Grove
            minuteOfDay in (9 * 60) until (16 * 60 + 30) -> Light.Midday
            // 16:30 to 19:30 -> Dusk
            minuteOfDay in (16 * 60 + 30) until (19 * 60 + 30) -> Light.Dusk
            // 19:30 to 05:30 -> Night
            else -> Light.Night
        }
    }

    fun source(context: Context, appWidgetId: Int? = null): WidgetSource {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val key = if (appWidgetId != null) "source_$appWidgetId" else KEY_SOURCE_GLOBAL
        val name = prefs.getString(key, null) ?: prefs.getString(KEY_SOURCE_GLOBAL, WidgetSource.TODAY.name)!!
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

    fun affirmation(context: Context, isSmall: Boolean = false, appWidgetId: Int? = null): Affirmation {
        val src = source(context, appWidgetId)
        val seeds = SeedDeclarations.asAffirmations()

        val pool: List<Affirmation> = when (src) {
            WidgetSource.TODAY -> listOf(seeds.firstOrNull() ?: AffirmationRepository.affirmationOfTheDay)
            WidgetSource.MY_DECLARATIONS -> {
                val kept = runCatching {
                    KeptRepository.init(context)
                    runBlocking { KeptRepository.getAllList().map { it.toAffirmation() } }
                }.getOrDefault(emptyList())
                if (kept.isNotEmpty()) kept else seeds
            }
            WidgetSource.TOPIC -> {
                val top = topic(context, appWidgetId)
                val topicPairs = com.makarios.app.data.CuratedTopicRepository.getPairsForTopic(top)
                if (topicPairs.isNotEmpty()) {
                    topicPairs.map { p ->
                        Affirmation(
                            id = p.id,
                            declaration = p.declaration,
                            scriptureText = "The word of the Lord stands forever.",
                            reference = p.reference,
                            context = top,
                            category = top,
                            tone = com.makarios.app.data.AffirmationTone.RESOLUTE
                        )
                    }
                } else seeds
            }
            WidgetSource.SURPRISE -> seeds + AffirmationRepository.getAll()
        }

        if (isSmall) {
            // For 2x2 small: pick declarations with <= 32 chars, or the shortest available
            val shortMatches = pool.filter { (it.shortText.ifEmpty { it.declaration }).length <= 32 }
            if (shortMatches.isNotEmpty()) {
                val daySeed = LocalDate.now().dayOfYear + (appWidgetId ?: 0)
                return shortMatches[Random(daySeed).nextInt(shortMatches.size)]
            }
            // Fallback: pick shortest declaration in pool
            return pool.minByOrNull { (it.shortText.ifEmpty { it.declaration }).length } ?: seeds.first()
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

            val isSmall = layoutType == WidgetLayoutType.SMALL_2X2
            val affirmation = WidgetStore.affirmation(context, isSmall = isSmall)
            val light = WidgetStore.light(context)

            val widthPx = (size.width.value * density).toInt()
            val heightPx = (size.height.value * density).toInt()

            val bitmap = WidgetBitmapRenderer.render(
                context = context,
                affirmation = affirmation,
                light = light,
                widthPx = widthPx,
                heightPx = heightPx,
                layoutType = layoutType
            )

            // Deep-link intent directly to the declaration in the app
            val deepLinkIntent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse("makarios://declare?id=${affirmation.id}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val desc = "${affirmation.declaration}. ${affirmation.scriptureText} (${affirmation.reference})"

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(24.dp)
                    .clickable(actionStartActivity(android.content.ComponentName(context, MainActivity::class.java)))
            ) {
                Image(
                    provider = ImageProvider(bitmap),
                    contentDescription = desc,
                    modifier = GlanceModifier.fillMaxSize()
                )
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
