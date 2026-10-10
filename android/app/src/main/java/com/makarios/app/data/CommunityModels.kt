package com.makarios.app.data

/**
 * CommunityModels
 *
 * Domain models for Makarios Community Declarations and Friend Connections.
 */

data class UserProfile(
    val userId: String = "",
    val displayName: String = "Friend",
    val username: String = "",
    val email: String = "",
    val friendCode: String = "",
    val photoUrl: String? = null,
    val bio: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class PublicAffirmation(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "Friend",
    val authorUsername: String = "",
    val declaration: String = "",
    val scriptureText: String = "",
    val reference: String = "",
    val category: String = "Peace",
    val imageUrl: String? = null,
    val amenCount: Long = 0,
    val amenedBy: List<String> = emptyList(),
    val saveCount: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Converts a PublicAffirmation to the standard local Affirmation model
     * so it can be previewed, saved, or shared with full design studio capabilities.
     */
    fun toAffirmation(): Affirmation {
        return Affirmation(
            id = id,
            declaration = declaration,
            scriptureText = scriptureText,
            reference = reference,
            context = "Authored by $authorName${if (authorUsername.isNotBlank()) " (@$authorUsername)" else ""}",
            category = category,
            tone = AffirmationTone.RESOLUTE,
            imageUrl = imageUrl ?: "",
            isFavorite = false,
            personalDeclaration = declaration
        )
    }
}

