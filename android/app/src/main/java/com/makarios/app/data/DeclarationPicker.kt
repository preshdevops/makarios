package com.makarios.app.data

import android.content.Context
import com.makarios.app.data.local.KeptEntity
import com.makarios.app.data.local.KeptRepository
import com.makarios.app.data.local.toAffirmation
import kotlin.random.Random

/**
 * Selects the next declaration to show in a widget or reminder.
 *
 * Rules:
 * 1. Collect flagged items first (inWidget / inReminders == true), then the rest of the chosen sources.
 * 2. Apply SHUFFLE or NEWEST_FIRST ordering within each group.
 * 3. If sources include MINE or KEPT but the resulting pool is empty, fall back to MAKARIOS.
 * 4. Never crash — always return *something*.
 *
 * All database access is a one-shot suspend call (no Flow); call from a
 * background dispatcher (Dispatchers.IO).
 */
object DeclarationPicker {

    /**
     * Pick the next affirmation for the **widget**.
     *
     * @param context          Android Context (application).
     * @param settings         Widget display preferences.
     * @param appWidgetId      Used as a seed offset so different widgets get different picks.
     * @param excludeId        ID of the declaration currently shown — skipped when possible.
     */
    suspend fun pickForWidget(
        context: Context,
        settings: WidgetSettings,
        appWidgetId: Int = 0,
        excludeId: String? = null
    ): Affirmation {
        val pool = buildWidgetPool(context, settings)
        return selectFrom(pool, settings.order, excludeId, seed = appWidgetId.toLong())
            ?: fallbackAffirmation()
    }

    /**
     * Pick the next affirmation for a **reminder notification**.
     *
     * @param context  Android Context (application).
     * @param settings Reminder preferences.
     * @param excludeId  ID of the last-shown reminder declaration — skipped when possible.
     */
    suspend fun pickForReminder(
        context: Context,
        settings: ReminderSettings,
        excludeId: String? = null
    ): Affirmation {
        val pool = buildReminderPool(context, settings)
        return selectFrom(pool, settings.order, excludeId, seed = System.currentTimeMillis())
            ?: fallbackAffirmation()
    }

    // ------------------------------------------------------------------
    // Internal pool builders
    // ------------------------------------------------------------------

    private suspend fun buildWidgetPool(context: Context, s: WidgetSettings): List<Affirmation> {
        val flaggedAffirmations = mutableListOf<Affirmation>()
        val rest = mutableListOf<Affirmation>()

        val allKept: List<KeptEntity> = KeptRepository.getAllList()

        // Build pools for each requested source
        val mineEntities   = allKept.filter { it.sourceType == "authored" }
        val keptEntities   = allKept.filter { it.sourceType != "authored" }
        val makarios       = SeedDeclarations.asAffirmations() + AffirmationRepository.getAll()

        val useMine    = DeclarationSource.MINE    in s.sources
        val useKept    = DeclarationSource.KEPT    in s.sources
        val useMakarios = DeclarationSource.MAKARIOS in s.sources

        var userPoolEmpty = true

        if (useMine) {
            val flagged = mineEntities.filter { it.inWidget }
            val unflagged = mineEntities.filterNot { it.inWidget }
            flaggedAffirmations += flagged.map { it.toAffirmation() }
            rest += unflagged.map { it.toAffirmation() }
            if (mineEntities.isNotEmpty()) userPoolEmpty = false
        }
        if (useKept) {
            val flagged = keptEntities.filter { it.inWidget }
            val unflagged = keptEntities.filterNot { it.inWidget }
            flaggedAffirmations += flagged.map { it.toAffirmation() }
            rest += unflagged.map { it.toAffirmation() }
            if (keptEntities.isNotEmpty()) userPoolEmpty = false
        }

        // Fallback to MAKARIOS when user sources are requested but empty
        if ((useMine || useKept) && userPoolEmpty) {
            return makarios.ifEmpty { listOf(fallbackAffirmation()) }
        }

        if (useMakarios) rest += makarios

        val combined = flaggedAffirmations + rest
        return combined.ifEmpty { makarios.ifEmpty { listOf(fallbackAffirmation()) } }
    }

    private suspend fun buildReminderPool(context: Context, s: ReminderSettings): List<Affirmation> {
        val allKept: List<KeptEntity> = KeptRepository.getAllList()
        val flaggedAffirmations = mutableListOf<Affirmation>()
        val rest = mutableListOf<Affirmation>()

        val mineEntities   = allKept.filter { it.sourceType == "authored" }
        val keptEntities   = allKept.filter { it.sourceType != "authored" }
        val makarios       = SeedDeclarations.asAffirmations() + AffirmationRepository.getAll()

        val useMine    = DeclarationSource.MINE    in s.sources
        val useKept    = DeclarationSource.KEPT    in s.sources
        val useMakarios = DeclarationSource.MAKARIOS in s.sources

        var userPoolEmpty = true

        if (useMine) {
            val flagged = mineEntities.filter { it.inReminders }
            val unflagged = mineEntities.filterNot { it.inReminders }
            flaggedAffirmations += flagged.map { it.toAffirmation() }
            rest += unflagged.map { it.toAffirmation() }
            if (mineEntities.isNotEmpty()) userPoolEmpty = false
        }
        if (useKept) {
            val flagged = keptEntities.filter { it.inReminders }
            val unflagged = keptEntities.filterNot { it.inReminders }
            flaggedAffirmations += flagged.map { it.toAffirmation() }
            rest += unflagged.map { it.toAffirmation() }
            if (keptEntities.isNotEmpty()) userPoolEmpty = false
        }

        if ((useMine || useKept) && userPoolEmpty) {
            return makarios.ifEmpty { listOf(fallbackAffirmation()) }
        }

        if (useMakarios) rest += makarios

        val combined = flaggedAffirmations + rest
        return combined.ifEmpty { makarios.ifEmpty { listOf(fallbackAffirmation()) } }
    }

    // ------------------------------------------------------------------
    // Selection helpers
    // ------------------------------------------------------------------

    private fun selectFrom(
        pool: List<Affirmation>,
        order: DeclarationOrder,
        excludeId: String?,
        seed: Long
    ): Affirmation? {
        if (pool.isEmpty()) return null
        val candidates = if (pool.size > 1 && excludeId != null) {
            pool.filter { it.id != excludeId }.ifEmpty { pool }
        } else pool

        return when (order) {
            DeclarationOrder.NEWEST_FIRST -> candidates.firstOrNull() ?: return null
            DeclarationOrder.SHUFFLE -> candidates[Random(seed).nextInt(candidates.size)]
        }
    }

    private fun fallbackAffirmation(): Affirmation =
        SeedDeclarations.asAffirmations().firstOrNull()
            ?: Affirmation(
                id = "fallback",
                declaration = "I am who God says I am.",
                scriptureText = "Christ in you, the hope of glory.",
                reference = "Colossians 1:27",
                context = "",
                category = "Identity",
                tone = AffirmationTone.RESOLUTE
            )
}
