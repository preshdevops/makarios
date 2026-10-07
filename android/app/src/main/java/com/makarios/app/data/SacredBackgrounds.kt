package com.makarios.app.data

/**
 * Curated photographic backgrounds crafted for verse and declaration creation.
 * Backed by PhotoLibrary with literal descriptions and disjoint photo IDs.
 */
data class SacredPhotoBackground(
    val id: String,
    val title: String,
    val category: String,
    val photoUrl: String,
    val thumbnailUrl: String
)

object SacredBackgrounds {

    val CATEGORIES = listOf(
        "All",
        "Dawn & Light",
        "Mountains",
        "Living Waters",
        "Forests & Nature",
        "Starlight",
        "Quiet Solitude"
    )

    val PHOTOS: List<SacredPhotoBackground> = PhotoLibrary.getCreateBackgrounds().map { entry ->
        SacredPhotoBackground(
            id = entry.id,
            title = entry.description,
            category = entry.category ?: "All",
            photoUrl = entry.url(width = 1600, quality = 85),
            thumbnailUrl = entry.url(width = 320, quality = 80)
        )
    }

    fun getById(id: String): SacredPhotoBackground? = PHOTOS.find { it.id == id }

    fun getByCategory(category: String): List<SacredPhotoBackground> =
        if (category == "All") PHOTOS else PHOTOS.filter { it.category == category }
}
