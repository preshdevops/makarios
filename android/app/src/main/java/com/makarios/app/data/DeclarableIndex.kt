package com.makarios.app.data

/** Curated, declarable retrieval index. The build pipeline can regenerate this from the reviewed JSON. */
object DeclarableIndex {
    private val blocked = setOf("curse", "cursed", "judgment", "judgement", "wrath", "destroy", "destruction", "wicked", "evil", "bloodshed", "vengeance")
    val verses: List<ScriptureVerse> by lazy {
        ScriptureDatabase.verses.filter { verse ->
            val text = "${verse.reference} ${verse.text} ${verse.themes.joinToString(" ")} ${verse.keywords.joinToString(" ")}".lowercase()
            blocked.none { it in text } || verse.reference == "Philippians 4:13" || verse.reference == "Deuteronomy 28:13"
        }
    }
}

object DeclarationBank {
    private val aliases = mapOf(
        "i can do anything" to listOf("Philippians 4:13"),
        "i am the head and not the tail" to listOf("Deuteronomy 28:13"),
        "i am fully known deeply loved" to listOf("Psalm 139:1-3"),
        "i forgive myself and walk free from shame" to listOf("Romans 8:1", "1 John 1:9"),
        "i am not condemned" to listOf("Romans 8:1"),
        "i can do all things" to listOf("Philippians 4:13")
    )
    fun referencesFor(text: String): List<String> {
        val normalized = text.lowercase().replace(Regex("[^a-z0-9 ]"), " ").replace(Regex("\\s+"), " ").trim()
        return aliases.entries.firstOrNull { normalized == it.key || normalized.contains(it.key) }?.value.orEmpty()
    }
}



