package com.makarios.app.data

import android.content.Context

// ---------------------------------------------------------------------------
// Enums shared by WidgetSettings and ReminderSettings
// ---------------------------------------------------------------------------

/** Which pool of declarations to draw from. */
enum class DeclarationSource {
    /** Declarations the user authored (sourceType = "authored"). */
    MINE,
    /** Declarations the user kept from the library (sourceType != "authored"). */
    KEPT,
    /** Seed declarations curated by Makarios. */
    MAKARIOS
}

/** Ordering within a picked pool. */
enum class DeclarationOrder {
    SHUFFLE,
    NEWEST_FIRST
}

// ---------------------------------------------------------------------------
// WidgetSettings
// ---------------------------------------------------------------------------

/** How often the widget rotates its declaration. */
enum class WidgetRotateEvery {
    ON_TAP,    // only rotates when the user taps
    HOURLY,    // ~1 h WorkManager periodic (actual minimum is 15 min)
    THREE_HOUR,
    DAILY
}

/** Whether the widget uses the declaration's original Light or a fixed one. */
enum class WidgetLookMode {
    /** Use the Light the declaration was created with. */
    ORIGINAL,
    /** Always render in the fixed Light chosen at configuration. */
    FIXED_LIGHT
}

data class WidgetSettings(
    val sources: Set<DeclarationSource> = setOf(DeclarationSource.MAKARIOS),
    val order: DeclarationOrder = DeclarationOrder.SHUFFLE,
    val rotateEvery: WidgetRotateEvery = WidgetRotateEvery.ON_TAP,
    val lookMode: WidgetLookMode = WidgetLookMode.ORIGINAL,
    /** Only used when lookMode == FIXED_LIGHT. Empty string means "Auto by time". */
    val fixedLightName: String = ""
)

// ---------------------------------------------------------------------------
// ReminderSettings
// ---------------------------------------------------------------------------

data class ReminderTime(val hour: Int, val minute: Int)

/** Lock-screen visibility choice for reminder notifications. */
enum class LockScreenText {
    SHOW,
    HIDE
}

data class ReminderSettings(
    val sources: Set<DeclarationSource> = setOf(DeclarationSource.MAKARIOS),
    val order: DeclarationOrder = DeclarationOrder.SHUFFLE,
    /**
     * Up to 3 reminder times per day.
     * Default: one at 8:00.
     */
    val times: List<ReminderTime> = listOf(ReminderTime(8, 0)),
    /** Hour (inclusive) when reminders should stop. Null = no quiet hours. */
    val quietHoursStart: Int? = null,
    /** Hour (inclusive) when reminders can resume. Null = no quiet hours. */
    val quietHoursEnd: Int? = null,
    val includeVerse: Boolean = true,
    val lockScreenText: LockScreenText = LockScreenText.SHOW
)

// ---------------------------------------------------------------------------
// Persistence — SharedPreferences-backed (no DataStore dep needed)
// ---------------------------------------------------------------------------

object WidgetSettingsStore {
    private const val PREFS = "makarios_widget_settings"

    fun load(context: Context): WidgetSettings {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val rawSources = p.getStringSet("sources", null)
        val sources: Set<DeclarationSource> = if (rawSources == null) {
            setOf(DeclarationSource.MAKARIOS)
        } else {
            rawSources.mapNotNull { runCatching { DeclarationSource.valueOf(it) }.getOrNull() }.toSet()
                .ifEmpty { setOf(DeclarationSource.MAKARIOS) }
        }
        val order = runCatching {
            p.getString("order", null)?.let { runCatching { DeclarationOrder.valueOf(it) }.getOrNull() } ?: DeclarationOrder.SHUFFLE
        }.getOrDefault(DeclarationOrder.SHUFFLE)
        val rotate = runCatching {
            p.getString("rotateEvery", null)?.let { runCatching { WidgetRotateEvery.valueOf(it) }.getOrNull() } ?: WidgetRotateEvery.ON_TAP
        }.getOrDefault(WidgetRotateEvery.ON_TAP)
        val look = runCatching {
            p.getString("lookMode", null)?.let { runCatching { WidgetLookMode.valueOf(it) }.getOrNull() } ?: WidgetLookMode.ORIGINAL
        }.getOrDefault(WidgetLookMode.ORIGINAL)
        val fixedLight = p.getString("fixedLightName", "") ?: ""
        return WidgetSettings(sources, order, rotate, look, fixedLight)
    }

    fun save(context: Context, s: WidgetSettings) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().apply {
            putStringSet("sources", s.sources.map { it.name }.toSet())
            putString("order", s.order.name)
            putString("rotateEvery", s.rotateEvery.name)
            putString("lookMode", s.lookMode.name)
            putString("fixedLightName", s.fixedLightName)
            apply()
        }
    }

    // Per-widget overrides (keyed by appWidgetId)
    fun loadForWidget(context: Context, appWidgetId: Int): WidgetSettings? {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val rawSources = p.getStringSet("sources_$appWidgetId", null) ?: return null
        val sources = rawSources.mapNotNull { runCatching { DeclarationSource.valueOf(it) }.getOrNull() }.toSet()
            .ifEmpty { return null }
        val order = runCatching {
            p.getString("order_$appWidgetId", null)?.let { runCatching { DeclarationOrder.valueOf(it) }.getOrNull() } ?: DeclarationOrder.SHUFFLE
        }.getOrDefault(DeclarationOrder.SHUFFLE)
        val rotate = runCatching {
            p.getString("rotateEvery_$appWidgetId", null)?.let { runCatching { WidgetRotateEvery.valueOf(it) }.getOrNull() } ?: WidgetRotateEvery.ON_TAP
        }.getOrDefault(WidgetRotateEvery.ON_TAP)
        val look = runCatching {
            p.getString("lookMode_$appWidgetId", null)?.let { runCatching { WidgetLookMode.valueOf(it) }.getOrNull() } ?: WidgetLookMode.ORIGINAL
        }.getOrDefault(WidgetLookMode.ORIGINAL)
        val fixedLight = p.getString("fixedLightName_$appWidgetId", "") ?: ""
        return WidgetSettings(sources, order, rotate, look, fixedLight)
    }

    fun saveForWidget(context: Context, appWidgetId: Int, s: WidgetSettings) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().apply {
            putStringSet("sources_$appWidgetId", s.sources.map { it.name }.toSet())
            putString("order_$appWidgetId", s.order.name)
            putString("rotateEvery_$appWidgetId", s.rotateEvery.name)
            putString("lookMode_$appWidgetId", s.lookMode.name)
            putString("fixedLightName_$appWidgetId", s.fixedLightName)
            apply()
        }
        // Also persist globally so newly placed widgets inherit a sane default
        save(context, s)
    }
}

object ReminderSettingsStore {
    private const val PREFS = "makarios_reminder_settings"

    fun load(context: Context): ReminderSettings {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val rawSources = p.getStringSet("sources", null)
        val sources: Set<DeclarationSource> = if (rawSources == null) {
            setOf(DeclarationSource.MAKARIOS)
        } else {
            rawSources.mapNotNull { runCatching { DeclarationSource.valueOf(it) }.getOrNull() }.toSet()
                .ifEmpty { setOf(DeclarationSource.MAKARIOS) }
        }
        val order = runCatching {
            p.getString("order", null)?.let { runCatching { DeclarationOrder.valueOf(it) }.getOrNull() } ?: DeclarationOrder.SHUFFLE
        }.getOrDefault(DeclarationOrder.SHUFFLE)
        // Reminder times (up to 3): stored as comma-separated "HH:MM" strings
        val rawTimes = p.getString("times", "08:00") ?: "08:00"
        val times = rawTimes.split(",").mapNotNull { token ->
            val parts = token.trim().split(":")
            if (parts.size == 2) {
                val h = parts[0].toIntOrNull() ?: return@mapNotNull null
                val m = parts[1].toIntOrNull() ?: return@mapNotNull null
                ReminderTime(h, m)
            } else null
        }.take(3).ifEmpty { listOf(ReminderTime(8, 0)) }

        val quietStart = p.getInt("quietHoursStart", -1).takeIf { it >= 0 }
        val quietEnd   = p.getInt("quietHoursEnd",   -1).takeIf { it >= 0 }
        val includeVerse = p.getBoolean("includeVerse", true)
        val lockScreen = runCatching {
            p.getString("lockScreenText", null)?.let { runCatching { LockScreenText.valueOf(it) }.getOrNull() } ?: LockScreenText.SHOW
        }.getOrDefault(LockScreenText.SHOW)
        return ReminderSettings(sources, order, times, quietStart, quietEnd, includeVerse, lockScreen)
    }

    fun save(context: Context, s: ReminderSettings) {
        val timesStr = s.times.joinToString(",") { "${it.hour.toString().padStart(2,'0')}:${it.minute.toString().padStart(2,'0')}" }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().apply {
            putStringSet("sources", s.sources.map { it.name }.toSet())
            putString("order", s.order.name)
            putString("times", timesStr)
            if (s.quietHoursStart != null) putInt("quietHoursStart", s.quietHoursStart) else remove("quietHoursStart")
            if (s.quietHoursEnd   != null) putInt("quietHoursEnd",   s.quietHoursEnd)   else remove("quietHoursEnd")
            putBoolean("includeVerse", s.includeVerse)
            putString("lockScreenText", s.lockScreenText.name)
            apply()
        }
    }
}
