package com.makarios.app.data

/**
 * A single Bible verse in the scripture corpus, tagged for matching.
 *
 * @param reference  Biblical citation (e.g. "Isaiah 41:10")
 * @param text       Full verse text
 * @param themes     Thematic categories (e.g. "peace", "fear", "trust")
 * @param toneAffinity  Which tone this verse most naturally expresses
 * @param keywords   Rich semantic keyword cloud for matching user declarations
 */
data class ScriptureVerse(
    val reference: String,
    val text: String,
    val themes: Set<String>,
    val toneAffinity: AffirmationTone,
    val keywords: Set<String>
)
