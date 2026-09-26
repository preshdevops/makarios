package com.makarios.app.data

/**
 * Curated library of 25+ breathtaking, high-resolution photographic backgrounds
 * crafted for YouVersion-style verse and declaration wallpapers.
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

    val PHOTOS: List<SacredPhotoBackground> = listOf(
        // ── Dawn & Light ───────────────────────────────────────────
        SacredPhotoBackground(
            id = "dawn_01",
            title = "Golden Sunrise Mist",
            category = "Dawn & Light",
            photoUrl = "https://images.unsplash.com/photo-1507652313519-d4e9174996dd?auto=format&fit=crop&w=1600&q=85",
            thumbnailUrl = "https://images.unsplash.com/photo-1507652313519-d4e9174996dd?auto=format&fit=crop&w=320&q=80"
        ),
        SacredPhotoBackground(
            id = "dawn_02",
            title = "Radiant Morning Horizon",
            category = "Dawn & Light",
            photoUrl = "https://images.unsplash.com/photo-1495616811223-4d98c6e9c869?auto=format&fit=crop&w=1600&q=85",
            thumbnailUrl = "https://images.unsplash.com/photo-1495616811223-4d98c6e9c869?auto=format&fit=crop&w=320&q=80"
        ),
        SacredPhotoBackground(
            id = "dawn_03",
            title = "Sunbeams Breaking Dawn",
            category = "Dawn & Light",
            photoUrl = "https://images.unsplash.com/photo-1470252649378-9c29740c9fa8?auto=format&fit=crop&w=1600&q=85",
            thumbnailUrl = "https://images.unsplash.com/photo-1470252649378-9c29740c9fa8?auto=format&fit=crop&w=320&q=80"
        ),
        SacredPhotoBackground(
            id = "dawn_04",
            title = "Warm Sunlit Cloudscape",
            category = "Dawn & Light",
            photoUrl = "https://images.unsplash.com/photo-1534088568595-a066f410bcda?auto=format&fit=crop&w=1600&q=85",
            thumbnailUrl = "https://images.unsplash.com/photo-1534088568595-a066f410bcda?auto=format&fit=crop&w=320&q=80"
        ),
        SacredPhotoBackground(
            id = "dawn_05",
            title = "First Light Over Valley",
            category = "Dawn & Light",
            photoUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=1600&q=85",
            thumbnailUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=320&q=80"
        ),
        SacredPhotoBackground(
            id = "dawn_06",
            title = "Amber Horizon Glow",
            category = "Dawn & Light",
            photoUrl = "https://images.unsplash.com/photo-1518495973542-4542c06a5843?auto=format&fit=crop&w=1600&q=85",
            thumbnailUrl = "https://images.unsplash.com/photo-1518495973542-4542c06a5843?auto=format&fit=crop&w=320&q=80"
        ),

        // ── Mountains ──────────────────────────────────────────────
        SacredPhotoBackground(
            id = "mtn_01",
            title = "Alpine Summit Sanctuary",
            category = "Mountains",
            photoUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=1600&q=85",
            thumbnailUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=320&q=80"
        ),
        SacredPhotoBackground(
            id = "mtn_02",
            title = "Majestic Mountain Peaks",
            category = "Mountains",
            photoUrl = "https://images.unsplash.com/photo-1519681393784-d120267933ba?auto=format&fit=crop&w=1600&q=85",
            thumbnailUrl = "https://images.unsplash.com/photo-1519681393784-d120267933ba?auto=format&fit=crop&w=320&q=80"
        ),
        SacredPhotoBackground(
            id = "mtn_03",
            title = "Misty Mountain Range",
            category = "Mountains",
            photoUrl = "https://images.unsplash.com/photo-1486870591958-9b9d0d1dda99?auto=format&fit=crop&w=1600&q=85",
            thumbnailUrl = "https://images.unsplash.com/photo-1486870591958-9b9d0d1dda99?auto=format&fit=crop&w=320&q=80"
        ),
        SacredPhotoBackground(
            id = "mtn_04",
            title = "Sunset Mountain Ridge",
            category = "Mountains",
            photoUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=1600&q=85",
            thumbnailUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=320&q=80"
        ),
        SacredPhotoBackground(
            id = "mtn_05",
            title = "Quiet Mountain Solitude",
            category = "Mountains",
            photoUrl = "https://images.unsplash.com/photo-1499209974431-9dddcece7f88?auto=format&fit=crop&w=1600&q=85",
            thumbnailUrl = "https://images.unsplash.com/photo-1499209974431-9dddcece7f88?auto=format&fit=crop&w=320&q=80"
        ),
        SacredPhotoBackground(
            id = "mtn_06",
            title = "Snowcapped Morning Peaks",
            category = "Mountains",
            photoUrl = "https://images.unsplash.com/photo-1454496522488-7a8e488e8606?auto=format&fit=crop&w=1600&q=85",
            thumbnailUrl = "https://images.unsplash.com/photo-1454496522488-7a8e488e8606?auto=format&fit=crop&w=320&q=80"
        ),

        // ── Living Waters ──────────────────────────────────────────
        SacredPhotoBackground(
            id = "wtr_01",
            title = "Tranquil Emerald Lake",
            category = "Living Waters",
            photoUrl = "https://images.unsplash.com/photo-1439853941329-a9a20243e8e7?auto=format&fit=crop&w=1600&q=85",
            thumbnailUrl = "https://images.unsplash.com/photo-1439853941329-a9a20243e8e7?auto=format&fit=crop&w=320&q=80"
        ),
        SacredPhotoBackground(
            id = "wtr_02",
            title = "Deep Ocean Horizon",
            category = "Living Waters",
            photoUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1600&q=85",
            thumbnailUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=320&q=80"
        ),
        SacredPhotoBackground(
            id = "wtr_03",
            title = "Gentle Morning Shoreline",
            category = "Living Waters",
            photoUrl = "https://images.unsplash.com/photo-1505118380757-91f5f5632de0?auto=format&fit=crop&w=1600&q=85",
            thumbnailUrl = "https://images.unsplash.com/photo-1505118380757-91f5f5632de0?auto=format&fit=crop&w=320&q=80"
        ),
        SacredPhotoBackground(
            id = "wtr_04",
            title = "Living Waterfall Sanctuary",
            category = "Living Waters",
            photoUrl = "https://images.unsplash.com/photo-1432405972618-c60b0225b8f9?auto=format&fit=crop&w=1600&q=85",
            thumbnailUrl = "https://images.unsplash.com/photo-1432405972618-c60b0225b8f9?auto=format&fit=crop&w=320&q=80"
        ),
        SacredPhotoBackground(
            id = "wtr_05",
            title = "Still Lake Reflections",
            category = "Living Waters",
            photoUrl = "https://images.unsplash.com/photo-1501785888041-af3ef285b470?auto=format&fit=crop&w=1600&q=85",
            thumbnailUrl = "https://images.unsplash.com/photo-1501785888041-af3ef285b470?auto=format&fit=crop&w=320&q=80"
        ),

        // ── Forests & Nature ───────────────────────────────────────
        SacredPhotoBackground(
            id = "fst_01",
            title = "Sunbeams Through Tall Pines",
            category = "Forests & Nature",
            photoUrl = "https://images.unsplash.com/photo-1448375240586-882707db888b?auto=format&fit=crop&w=1600&q=85",
            thumbnailUrl = "https://images.unsplash.com/photo-1448375240586-882707db888b?auto=format&fit=crop&w=320&q=80"
        ),
        SacredPhotoBackground(
            id = "fst_02",
            title = "Quiet Forest Pathway",
            category = "Forests & Nature",
            photoUrl = "https://images.unsplash.com/photo-1511497584788-87676104235f?auto=format&fit=crop&w=1600&q=85",
            thumbnailUrl = "https://images.unsplash.com/photo-1511497584788-87676104235f?auto=format&fit=crop&w=320&q=80"
        ),
        SacredPhotoBackground(
            id = "fst_03",
            title = "Lush Eucalyptus Grove",
            category = "Forests & Nature",
            photoUrl = "https://images.unsplash.com/photo-1500534623283-312aade485b7?auto=format&fit=crop&w=1600&q=85",
            thumbnailUrl = "https://images.unsplash.com/photo-1500534623283-312aade485b7?auto=format&fit=crop&w=320&q=80"
        ),
        SacredPhotoBackground(
            id = "fst_04",
            title = "Misty Pine Woodlands",
            category = "Forests & Nature",
            photoUrl = "https://images.unsplash.com/photo-1425913397330-cf8af2ff40a1?auto=format&fit=crop&w=1600&q=85",
            thumbnailUrl = "https://images.unsplash.com/photo-1425913397330-cf8af2ff40a1?auto=format&fit=crop&w=320&q=80"
        ),
        SacredPhotoBackground(
            id = "fst_05",
            title = "Sunlit Wildflower Meadow",
            category = "Forests & Nature",
            photoUrl = "https://images.unsplash.com/photo-1473448912268-2022ce9509d8?auto=format&fit=crop&w=1600&q=85",
            thumbnailUrl = "https://images.unsplash.com/photo-1473448912268-2022ce9509d8?auto=format&fit=crop&w=320&q=80"
        ),

        // ── Starlight ──────────────────────────────────────────────
        SacredPhotoBackground(
            id = "str_01",
            title = "Starry Midnight Heavens",
            category = "Starlight",
            photoUrl = "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?auto=format&fit=crop&w=1600&q=85",
            thumbnailUrl = "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?auto=format&fit=crop&w=320&q=80"
        ),
        SacredPhotoBackground(
            id = "str_02",
            title = "Celestial Milky Way",
            category = "Starlight",
            photoUrl = "https://images.unsplash.com/photo-1538370965046-79c0d6907d47?auto=format&fit=crop&w=1600&q=85",
            thumbnailUrl = "https://images.unsplash.com/photo-1538370965046-79c0d6907d47?auto=format&fit=crop&w=320&q=80"
        ),
        SacredPhotoBackground(
            id = "str_03",
            title = "Twilight Starlit Forest",
            category = "Starlight",
            photoUrl = "https://images.unsplash.com/photo-1419242902214-272b3f66ee7a?auto=format&fit=crop&w=1600&q=85",
            thumbnailUrl = "https://images.unsplash.com/photo-1419242902214-272b3f66ee7a?auto=format&fit=crop&w=320&q=80"
        ),
        SacredPhotoBackground(
            id = "str_04",
            title = "Constellations Over Horizon",
            category = "Starlight",
            photoUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=1600&q=85",
            thumbnailUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=320&q=80"
        ),

        // ── Quiet Solitude ─────────────────────────────────────────
        SacredPhotoBackground(
            id = "sld_01",
            title = "Warm Cathedral Archway",
            category = "Quiet Solitude",
            photoUrl = "https://images.unsplash.com/photo-1513694203232-719a280e022f?auto=format&fit=crop&w=1600&q=85",
            thumbnailUrl = "https://images.unsplash.com/photo-1513694203232-719a280e022f?auto=format&fit=crop&w=320&q=80"
        ),
        SacredPhotoBackground(
            id = "sld_02",
            title = "Desert Sand Dunes Sunrise",
            category = "Quiet Solitude",
            photoUrl = "https://images.unsplash.com/photo-1509316975850-ff9c5deb0cd9?auto=format&fit=crop&w=1600&q=85",
            thumbnailUrl = "https://images.unsplash.com/photo-1509316975850-ff9c5deb0cd9?auto=format&fit=crop&w=320&q=80"
        )
    )

    fun getById(id: String): SacredPhotoBackground? = PHOTOS.find { it.id == id }

    fun getByCategory(category: String): List<SacredPhotoBackground> =
        if (category == "All") PHOTOS else PHOTOS.filter { it.category == category }
}
