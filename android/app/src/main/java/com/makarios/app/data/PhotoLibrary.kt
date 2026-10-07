package com.makarios.app.data

/**
 * Single source of truth for all photography across Makarios.
 *
 * Rules:
 * 1. Every photo ID is globally unique across all roles.
 * 2. Honest, literal descriptions of what is actually in the image.
 * 3. Consistent warm grade applied via WarmPhotoGrade.
 * 4. Pure Kotlin (JVM-testable, no Android dependencies).
 */
enum class PhotoRole {
    HOME_HERO,
    LIBRARY_FEATURED,
    THEME_IDENTITY,
    THEME_PEACE,
    THEME_STRENGTH,
    THEME_PURPOSE,
    THEME_COURAGE,
    THEME_JOY,
    THEME_PROVISION,
    THEME_CONFIDENCE,
    THEME_RELATIONSHIPS,
    THEME_DISCIPLINE,
    THEME_PERSONAL,
    ONBOARDING_1,
    ONBOARDING_2,
    ONBOARDING_3,
    ONBOARDING_4,
    WALLPAPER_1,
    WALLPAPER_2,
    WALLPAPER_3,
    WALLPAPER_4,
    WALLPAPER_5,
    WALLPAPER_6,
    COMMUNITY_FALLBACK,
    AFFIRMATION_AOTD,
    AFFIRMATION_IDENT_1,
    AFFIRMATION_STR_1,
    AFFIRMATION_PROV_1,
    AFFIRMATION_COUR_1,
    AFFIRMATION_FAV_1,
    AFFIRMATION_FAV_2,
    AFFIRMATION_WIDGET_1,
    AFFIRMATION_PURP_1,
    AFFIRMATION_CONF_1,
    AFFIRMATION_REL_1,
    AFFIRMATION_DISC_1,
    AFFIRMATION_PERSONAL_1,
    CREATE_BACKGROUND
}

data class PhotoEntry(
    val id: String,
    val unsplashId: String,
    val description: String,
    val role: PhotoRole,
    val category: String? = null
) {
    fun url(width: Int = 1200, quality: Int = 85): String =
        "https://images.unsplash.com/photo-$unsplashId?auto=format&fit=crop&w=$width&q=$quality"
}

object PhotoLibrary {

    val entries: List<PhotoEntry> = listOf(
        // ── 1. Home Hero ──────────────────────────────────────────────
        PhotoEntry(
            id = "home_hero",
            unsplashId = "1472214103451-9374bd1c798e",
            description = "Solitary green oak tree standing in a wide sunlit pasture",
            role = PhotoRole.HOME_HERO
        ),

        // ── 2. Library Featured ────────────────────────────────────────
        PhotoEntry(
            id = "library_featured",
            unsplashId = "1426604966848-d7adac402bff",
            description = "Towering granite canyon walls and river valley in afternoon light",
            role = PhotoRole.LIBRARY_FEATURED
        ),

        // ── 3. Themes (One Photo Per Theme) ───────────────────────────
        PhotoEntry(
            id = "theme_identity",
            unsplashId = "1447752875215-b2761acb3c5d",
            description = "Walking path covered in golden autumn leaves through quiet woods",
            role = PhotoRole.THEME_IDENTITY
        ),
        PhotoEntry(
            id = "theme_peace",
            unsplashId = "1470770841072-f978cf4d019e",
            description = "Still wooden dock resting on glass-like lake waters at dawn",
            role = PhotoRole.THEME_PEACE
        ),
        PhotoEntry(
            id = "theme_strength",
            unsplashId = "1483728642387-6c3bdd6c93e5",
            description = "Sharp snow-dusted alpine granite peaks against crisp clear sky",
            role = PhotoRole.THEME_STRENGTH
        ),
        PhotoEntry(
            id = "theme_purpose",
            unsplashId = "1469474968028-56623f02e42e",
            description = "Sunbeams sweeping across open grassy ridges and distant hills",
            role = PhotoRole.THEME_PURPOSE
        ),
        PhotoEntry(
            id = "theme_courage",
            unsplashId = "1542224566-6e85f2e6772f",
            description = "Jagged mountain summits piercing through an unbroken sea of clouds",
            role = PhotoRole.THEME_COURAGE
        ),
        PhotoEntry(
            id = "theme_joy",
            unsplashId = "1465146344425-f00d5f5c8f07",
            description = "Vibrant wildflowers scattered across a sun-drenched meadow",
            role = PhotoRole.THEME_JOY
        ),
        PhotoEntry(
            id = "theme_provision",
            unsplashId = "1470240731273-7821a6eeb6bd",
            description = "Warm golden sunrise breaking over rolling agricultural fields",
            role = PhotoRole.THEME_PROVISION
        ),
        PhotoEntry(
            id = "theme_confidence",
            unsplashId = "1511884642898-4c92249e20b6",
            description = "Turquoise glacial mountain lake mirroring towering pine slopes",
            role = PhotoRole.THEME_CONFIDENCE
        ),
        PhotoEntry(
            id = "theme_relationships",
            unsplashId = "1473773508845-188df298d2d1",
            description = "Wooden boardwalk walkway curving gently over quiet marsh waters",
            role = PhotoRole.THEME_RELATIONSHIPS
        ),
        PhotoEntry(
            id = "theme_discipline",
            unsplashId = "1441974231531-c6227db76b6e",
            description = "Clean morning sunlight warming green forest floor ferns and trunks",
            role = PhotoRole.THEME_DISCIPLINE
        ),
        PhotoEntry(
            id = "theme_personal",
            unsplashId = "1502082553048-f009c37129b9",
            description = "Sunbeams cutting through morning mist in a pine grove",
            role = PhotoRole.THEME_PERSONAL
        ),

        // ── 4. Onboarding Journey ─────────────────────────────────────
        PhotoEntry(
            id = "onboarding_1",
            unsplashId = "1506905925346-21bda4d32df4",
            description = "Sunrise light warming mountain ridges above misty valleys",
            role = PhotoRole.ONBOARDING_1
        ),
        PhotoEntry(
            id = "onboarding_2",
            unsplashId = "1501854140801-50d01698950b",
            description = "Green mountain pass and rolling hills shrouded in soft mist",
            role = PhotoRole.ONBOARDING_2
        ),
        PhotoEntry(
            id = "onboarding_3",
            unsplashId = "1491466424936-e304919aada7",
            description = "Gentle amber sunset glow sweeping over smooth sand dunes",
            role = PhotoRole.ONBOARDING_3
        ),
        PhotoEntry(
            id = "onboarding_4",
            unsplashId = "1518709268805-4e9042af9f23",
            description = "Brilliant night sky of stars arching above snowy mountain ridges",
            role = PhotoRole.ONBOARDING_4
        ),

        // ── 5. Curated Saved Wallpapers ───────────────────────────────
        PhotoEntry(
            id = "wp_1",
            unsplashId = "1505765050516-f72dcac9c60e",
            description = "Soft dawn twilight over layered pine-covered mountains",
            role = PhotoRole.WALLPAPER_1
        ),
        PhotoEntry(
            id = "wp_2",
            unsplashId = "1500382017468-9049fed747ef",
            description = "Solitary oak tree on crest of a golden grassy hill at sunset",
            role = PhotoRole.WALLPAPER_2
        ),
        PhotoEntry(
            id = "wp_3",
            unsplashId = "1497436072909-60f360e1d4b1",
            description = "Glacial river winding through wide green mountain valley",
            role = PhotoRole.WALLPAPER_3
        ),
        PhotoEntry(
            id = "wp_4",
            unsplashId = "1493246507139-91e8fad9978e",
            description = "Misty rolling green hillside terraces in morning stillness",
            role = PhotoRole.WALLPAPER_4
        ),
        PhotoEntry(
            id = "wp_5",
            unsplashId = "1516214104703-d870798883c5",
            description = "Dense layer of low morning fog resting above pine forest basin",
            role = PhotoRole.WALLPAPER_5
        ),
        PhotoEntry(
            id = "wp_6",
            unsplashId = "1542838132-92c53300491e",
            description = "Clear dark night sky with sharp stars above a calm horizon",
            role = PhotoRole.WALLPAPER_6
        ),

        // ── 6. Community Default Fallback ─────────────────────────────
        PhotoEntry(
            id = "community_fallback",
            unsplashId = "1523712999610-f77fbcfc3843",
            description = "Sunbeams piercing through emerald green woodland canopy",
            role = PhotoRole.COMMUNITY_FALLBACK
        ),

        // ── 7. Curated Affirmations ───────────────────────────────────
        PhotoEntry(
            id = "aff_aotd",
            unsplashId = "1532274402911-5a369e4c4bb5",
            description = "Golden autumn trees mirrored in calm river water at dusk",
            role = PhotoRole.AFFIRMATION_AOTD
        ),
        PhotoEntry(
            id = "aff_ident_1",
            unsplashId = "1513836279014-a89f7a76ae86",
            description = "Snow dusting dense evergreen tree canopies from overhead",
            role = PhotoRole.AFFIRMATION_IDENT_1
        ),
        PhotoEntry(
            id = "aff_str_1",
            unsplashId = "1433086966358-54859d0ed716",
            description = "Suspension bridge spanning a deep green mountain canyon",
            role = PhotoRole.AFFIRMATION_STR_1
        ),
        PhotoEntry(
            id = "aff_prov_1",
            unsplashId = "1476673160081-cf065607f449",
            description = "Gentle ocean waves rolling beneath pastel pink and amber twilight",
            role = PhotoRole.AFFIRMATION_PROV_1
        ),
        PhotoEntry(
            id = "aff_cour_1",
            unsplashId = "1472396961693-142e6e269027",
            description = "Sunlit woodland glade with tall trees and quiet grass",
            role = PhotoRole.AFFIRMATION_COUR_1
        ),
        PhotoEntry(
            id = "aff_fav_1",
            unsplashId = "1511447333015-45b65e60f6d5",
            description = "Deep indigo and purple cosmic nebula scattered with stars",
            role = PhotoRole.AFFIRMATION_FAV_1
        ),
        PhotoEntry(
            id = "aff_fav_2",
            unsplashId = "1500534314209-a25ddb2bd429",
            description = "Clear shallow turquoise sea washing over pale clean sand",
            role = PhotoRole.AFFIRMATION_FAV_2
        ),
        PhotoEntry(
            id = "aff_widget_1",
            unsplashId = "1518457607834-6e8d80c183c5",
            description = "Misty calm ocean waters merging into a pale morning horizon",
            role = PhotoRole.AFFIRMATION_WIDGET_1
        ),
        PhotoEntry(
            id = "aff_purp_1",
            unsplashId = "1482938289607-e9573fc25ebb",
            description = "Cold mountain river flowing past tall evergreens and granite banks",
            role = PhotoRole.AFFIRMATION_PURP_1
        ),
        PhotoEntry(
            id = "aff_conf_1",
            unsplashId = "1498429089284-41f8cf3ffd39",
            description = "Dramatic sunbeams bursting through cloud breaks onto rolling hills",
            role = PhotoRole.AFFIRMATION_CONF_1
        ),
        PhotoEntry(
            id = "aff_rel_1",
            unsplashId = "1500530855697-b586d89ba3ee",
            description = "Quiet mountain pass road disappearing into soft morning fog",
            role = PhotoRole.AFFIRMATION_REL_1
        ),
        PhotoEntry(
            id = "aff_disc_1",
            unsplashId = "1504567961542-e24d9439a724",
            description = "Golden sun rays warming dense woodland trees and mossy ground",
            role = PhotoRole.AFFIRMATION_DISC_1
        ),
        PhotoEntry(
            id = "aff_personal_1",
            unsplashId = "1520962880247-cfaf541c8724",
            description = "Soft sunset hues resting gently over distant alpine ridges",
            role = PhotoRole.AFFIRMATION_PERSONAL_1
        ),

        // ── 8. Create Flow Photographic Backgrounds ───────────────────
        // Dawn & Light
        PhotoEntry(
            id = "dawn_01",
            unsplashId = "1507652313519-d4e9174996dd",
            description = "Sun rising through morning mist over an open golden field",
            role = PhotoRole.CREATE_BACKGROUND,
            category = "Dawn & Light"
        ),
        PhotoEntry(
            id = "dawn_02",
            unsplashId = "1495616811223-4d98c6e9c869",
            description = "Golden morning clouds layered across a quiet dawn horizon",
            role = PhotoRole.CREATE_BACKGROUND,
            category = "Dawn & Light"
        ),
        PhotoEntry(
            id = "dawn_03",
            unsplashId = "1470252649378-9c29740c9fa8",
            description = "Sunbeams breaking through tree canopy over a misty meadow",
            role = PhotoRole.CREATE_BACKGROUND,
            category = "Dawn & Light"
        ),
        PhotoEntry(
            id = "dawn_04",
            unsplashId = "1534088568595-a066f410bcda",
            description = "Warm sunlight illuminating thick cumulus cloud formations",
            role = PhotoRole.CREATE_BACKGROUND,
            category = "Dawn & Light"
        ),
        PhotoEntry(
            id = "dawn_05",
            unsplashId = "1470071459604-3b5ec3a7fe05",
            description = "First light touching mountain valley ridges and rising mist",
            role = PhotoRole.CREATE_BACKGROUND,
            category = "Dawn & Light"
        ),
        PhotoEntry(
            id = "dawn_06",
            unsplashId = "1518495973542-4542c06a5843",
            description = "Warm amber glow illuminating tree branches against morning sky",
            role = PhotoRole.CREATE_BACKGROUND,
            category = "Dawn & Light"
        ),

        // Mountains
        PhotoEntry(
            id = "mtn_01",
            unsplashId = "1464822759023-fed622ff2c3b",
            description = "Sharp alpine mountain peaks under crisp clear sky",
            role = PhotoRole.CREATE_BACKGROUND,
            category = "Mountains"
        ),
        PhotoEntry(
            id = "mtn_02",
            unsplashId = "1519681393784-d120267933ba",
            description = "Snowy jagged peak under deep starry twilight",
            role = PhotoRole.CREATE_BACKGROUND,
            category = "Mountains"
        ),
        PhotoEntry(
            id = "mtn_03",
            unsplashId = "1486870591958-9b9d0d1dda99",
            description = "Misty mountain range silhouetted at dusk",
            role = PhotoRole.CREATE_BACKGROUND,
            category = "Mountains"
        ),
        PhotoEntry(
            id = "mtn_04",
            unsplashId = "1506744038136-46273834b3fb",
            description = "Yosemite valley river framed by steep granite cliffs",
            role = PhotoRole.CREATE_BACKGROUND,
            category = "Mountains"
        ),
        PhotoEntry(
            id = "mtn_05",
            unsplashId = "1499209974431-9dddcece7f88",
            description = "Rolling green grassy hills beneath warm morning sunlight",
            role = PhotoRole.CREATE_BACKGROUND,
            category = "Mountains"
        ),
        PhotoEntry(
            id = "mtn_06",
            unsplashId = "1454496522488-7a8e488e8606",
            description = "Snowcapped alpine summits rising into crisp clear air",
            role = PhotoRole.CREATE_BACKGROUND,
            category = "Mountains"
        ),

        // Living Waters
        PhotoEntry(
            id = "wtr_01",
            unsplashId = "1439853941329-a9a20243e8e7",
            description = "Calm turquoise water in a secluded mountain cove",
            role = PhotoRole.CREATE_BACKGROUND,
            category = "Living Waters"
        ),
        PhotoEntry(
            id = "wtr_02",
            unsplashId = "1507525428034-b723cf961d3e",
            description = "Gentle ocean waves washing over a quiet tropical shore",
            role = PhotoRole.CREATE_BACKGROUND,
            category = "Living Waters"
        ),
        PhotoEntry(
            id = "wtr_03",
            unsplashId = "1505118380757-91f5f5632de0",
            description = "Pastel sunset colors reflecting on low tide shoreline",
            role = PhotoRole.CREATE_BACKGROUND,
            category = "Living Waters"
        ),
        PhotoEntry(
            id = "wtr_04",
            unsplashId = "1432405972618-c60b0225b8f9",
            description = "Forest waterfall cascading into a clear rock basin",
            role = PhotoRole.CREATE_BACKGROUND,
            category = "Living Waters"
        ),
        PhotoEntry(
            id = "wtr_05",
            unsplashId = "1501785888041-af3ef285b470",
            description = "Still mountain lake reflecting surrounding evergreen forest",
            role = PhotoRole.CREATE_BACKGROUND,
            category = "Living Waters"
        ),

        // Forests & Nature
        PhotoEntry(
            id = "fst_01",
            unsplashId = "1448375240586-882707db888b",
            description = "Tall pine trees with morning sunlight rays streaming through",
            role = PhotoRole.CREATE_BACKGROUND,
            category = "Forests & Nature"
        ),
        PhotoEntry(
            id = "fst_02",
            unsplashId = "1511497584788-87676104235f",
            description = "Winding forest footpath through tall misty redwoods",
            role = PhotoRole.CREATE_BACKGROUND,
            category = "Forests & Nature"
        ),
        PhotoEntry(
            id = "fst_03",
            unsplashId = "1500534623283-312aade485b7",
            description = "Looking directly upward into a tall eucalyptus canopy",
            role = PhotoRole.CREATE_BACKGROUND,
            category = "Forests & Nature"
        ),
        PhotoEntry(
            id = "fst_04",
            unsplashId = "1425913397330-cf8af2ff40a1",
            description = "Misty evergreen forest in quiet wilderness rain",
            role = PhotoRole.CREATE_BACKGROUND,
            category = "Forests & Nature"
        ),
        PhotoEntry(
            id = "fst_05",
            unsplashId = "1473448912268-2022ce9509d8",
            description = "Sunlight illuminating warm yellow autumn leaves along a wood edge",
            role = PhotoRole.CREATE_BACKGROUND,
            category = "Forests & Nature"
        ),

        // Starlight
        PhotoEntry(
            id = "str_01",
            unsplashId = "1506703719100-a0f3a48c0f86",
            description = "Dark desert sand dunes beneath a vast starry night sky",
            role = PhotoRole.CREATE_BACKGROUND,
            category = "Starlight"
        ),
        PhotoEntry(
            id = "str_02",
            unsplashId = "1538370965046-79c0d6907d47",
            description = "Milky Way galaxy band arching across mountain silhouettes",
            role = PhotoRole.CREATE_BACKGROUND,
            category = "Starlight"
        ),
        PhotoEntry(
            id = "str_03",
            unsplashId = "1419242902214-272b3f66ee7a",
            description = "Starry night heavens framed by dark woodland trees",
            role = PhotoRole.CREATE_BACKGROUND,
            category = "Starlight"
        ),
        PhotoEntry(
            id = "str_04",
            unsplashId = "1451187580459-43490279c0fa",
            description = "Deep space stars and ethereal blue and purple nebula",
            role = PhotoRole.CREATE_BACKGROUND,
            category = "Starlight"
        ),

        // Quiet Solitude
        PhotoEntry(
            id = "sld_01",
            unsplashId = "1513694203232-719a280e022f",
            description = "Soft daylight falling through weathered stone cathedral arches",
            role = PhotoRole.CREATE_BACKGROUND,
            category = "Quiet Solitude"
        ),
        PhotoEntry(
            id = "sld_02",
            unsplashId = "1509316975850-ff9c5deb0cd9",
            description = "Rippled sand dunes stretching into peaceful desert sunrise",
            role = PhotoRole.CREATE_BACKGROUND,
            category = "Quiet Solitude"
        )
    )

    private val byIdMap: Map<String, PhotoEntry> = entries.associateBy { it.id }
    private val byRoleMap: Map<PhotoRole, PhotoEntry> = entries
        .filter { it.role != PhotoRole.CREATE_BACKGROUND }
        .associateBy { it.role }

    fun getById(id: String): PhotoEntry? = byIdMap[id]

    fun getForRole(role: PhotoRole): PhotoEntry =
        byRoleMap[role] ?: entries.first { it.role == role }

    fun getThemePhoto(themeName: String): PhotoEntry = when (themeName.lowercase()) {
        "identity" -> getForRole(PhotoRole.THEME_IDENTITY)
        "peace" -> getForRole(PhotoRole.THEME_PEACE)
        "strength" -> getForRole(PhotoRole.THEME_STRENGTH)
        "purpose" -> getForRole(PhotoRole.THEME_PURPOSE)
        "courage" -> getForRole(PhotoRole.THEME_COURAGE)
        "joy" -> getForRole(PhotoRole.THEME_JOY)
        "provision" -> getForRole(PhotoRole.THEME_PROVISION)
        "confidence" -> getForRole(PhotoRole.THEME_CONFIDENCE)
        "relationships" -> getForRole(PhotoRole.THEME_RELATIONSHIPS)
        "discipline" -> getForRole(PhotoRole.THEME_DISCIPLINE)
        else -> getForRole(PhotoRole.THEME_PERSONAL)
    }

    fun getCreateBackgrounds(): List<PhotoEntry> =
        entries.filter { it.role == PhotoRole.CREATE_BACKGROUND }
}
