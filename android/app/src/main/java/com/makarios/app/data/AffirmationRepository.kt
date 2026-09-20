package com.makarios.app.data

import androidx.compose.runtime.mutableStateListOf

object AffirmationRepository {

    // ── Page 5: Affirmation of the Day ──────────────────────────
    val affirmationOfTheDay = Affirmation(
        id = "aotd-1",
        declaration = "I am fully known, deeply loved, and precisely placed for this moment.",
        scriptureText = "You have searched me, Lord, and you know me. You know when I sit and when I rise; you perceive my thoughts from afar.",
        reference = "Inspired by Psalm 139",
        context = "David meditates on the inescapable love and sovereign knowledge of God. Nothing in your story is an accident.",
        category = "Identity",
        tone = AffirmationTone.RESOLUTE,
        imageUrl = "https://images.unsplash.com/photo-1507652313519-d4e9174996dd?auto=format&fit=crop&w=1000&q=85"
    )

    // ── Page 1: Fullscreen Contemplation Affirmation ────────────
    val fullscreenAffirmation = Affirmation(
        id = "ident-1",
        declaration = "I am the righteousness of God in Christ Jesus. I walk with authority and grace.",
        scriptureText = "God made him who had no sin to be sin for us, so that in him we might become the righteousness of God.",
        reference = "2 CORINTHIANS 5:21",
        personalDeclaration = "Today, I choose to see myself as God sees me. I will not be moved by my feelings or circumstances.",
        context = "Paul anchors the believer's standing not in personal performance, but in Christ's finished reconciliation.",
        category = "Identity",
        tone = AffirmationTone.RESOLUTE,
        imageUrl = "https://images.unsplash.com/photo-1518495973542-4542c06a5843?auto=format&fit=crop&w=1000&q=85"
    )

    // ── Page 5: Discover More Feed Affirmations ─────────────────
    val strengthAffirmation = Affirmation(
        id = "str-1",
        declaration = "My strength is made perfect in weakness, for He is the one who goes before me.",
        scriptureText = "My grace is sufficient for you, for my power is made perfect in weakness.",
        reference = "2 CORINTHIANS 12:9",
        context = "When human stamina reaches its limit, divine power finds its most potent expression.",
        category = "Strength",
        tone = AffirmationTone.RESOLUTE,
        imageUrl = "https://images.unsplash.com/photo-1513519245088-0e12902e5a38?auto=format&fit=crop&w=1000&q=85",
        listenerCount = "+12k"
    )

    val provisionAffirmation = Affirmation(
        id = "prov-1",
        declaration = "I walk in abundance and peace, for my provider is the King of kings.",
        scriptureText = "And my God will meet all your needs according to the riches of his glory in Christ Jesus.",
        reference = "PHILIPPIANS 4:19",
        context = "Paul writes from confinement with unshakable assurance that divine resources are inexhaustible.",
        category = "Provision",
        tone = AffirmationTone.STILL,
        imageUrl = "https://images.unsplash.com/photo-1499209974431-9dddcece7f88?auto=format&fit=crop&w=1000&q=85",
        isPremium = true
    )

    val courageAffirmation = Affirmation(
        id = "cour-1",
        declaration = "Fear has no room where faith resides. I am bold, brave, and courageous.",
        scriptureText = "Have I not commanded you? Be strong and courageous. Do not be afraid; do not be discouraged.",
        reference = "JOSHUA 1:9",
        context = "Joshua stood on the threshold of unknown territory. Courage is obedience in the presence of fear.",
        category = "Courage",
        tone = AffirmationTone.RESOLUTE,
        imageUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=1000&q=85",
        isAddedToDaily = true
    )

    // ── Page 3: Saved Favorites ─────────────────────────────────
    val favorite1 = Affirmation(
        id = "fav-1",
        declaration = "I am more than a conqueror through Him who loved me.",
        scriptureText = "No, in all these things we are more than conquerors through him who loved us.",
        reference = "ROMANS 8:37",
        context = "Victory is not merely surviving difficulty, but emerging deepened in sovereign love.",
        category = "Strength",
        tone = AffirmationTone.RESOLUTE,
        imageUrl = "https://images.unsplash.com/photo-1448375240586-882707db888b?auto=format&fit=crop&w=1000&q=85",
        isFavorite = true
    )

    val favorite2 = Affirmation(
        id = "fav-2",
        declaration = "The joy of the Lord is my strength and my shield.",
        scriptureText = "Do not grieve, for the joy of the Lord is your strength.",
        reference = "NEHEMIAH 8:10",
        context = "Spiritual joy is not superficial optimism; it is the deep, immovable wellspring of God's delight.",
        category = "Joy",
        tone = AffirmationTone.GENTLE,
        imageUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?auto=format&fit=crop&w=1000&q=85",
        isFavorite = true
    )

    val widgetAffirmation = Affirmation(
        id = "widget-1",
        declaration = "Be still. The battle is not yours alone.",
        scriptureText = "You will not have to fight this battle. Take up your positions; stand firm and see the deliverance the Lord will give you.",
        reference = "2 CHRONICLES 20:17",
        context = "Step out of frantic preservation and breathe deeply into sovereign protection. God takes responsibility for the outcome.",
        category = "Peace",
        tone = AffirmationTone.STILL,
        imageUrl = "https://images.unsplash.com/photo-1507652313519-d4e9174996dd?auto=format&fit=crop&w=1000&q=85"
    )

    val starterAffirmation = fullscreenAffirmation

    val focusAreas = listOf("Peace", "Strength", "Identity", "Purpose", "Joy")
    val categories = listOf("Peace", "Strength", "Identity", "Purpose", "Joy", "Provision", "Courage")

    private val allAffirmations = listOf(
        fullscreenAffirmation,
        affirmationOfTheDay,
        strengthAffirmation,
        provisionAffirmation,
        courageAffirmation,
        favorite1,
        favorite2,
        widgetAffirmation
    )

    // Reactive saved IDs (with initial items matching Page 3: 12 ITEMS in favorites)
    val savedAffirmationIds = mutableStateListOf("ident-1", "fav-1", "fav-2")

    fun isSaved(id: String): Boolean = savedAffirmationIds.contains(id)

    fun toggleSave(id: String) {
        if (savedAffirmationIds.contains(id)) {
            savedAffirmationIds.remove(id)
        } else {
            savedAffirmationIds.add(id)
        }
    }

    fun getSaved(): List<Affirmation> = allAffirmations.filter { savedAffirmationIds.contains(it.id) }

    fun getFavorites(): List<Affirmation> = allAffirmations.filter { it.isFavorite || savedAffirmationIds.contains(it.id) }

    fun getById(id: String): Affirmation = allAffirmations.find { it.id == id } ?: fullscreenAffirmation

    fun getAll(): List<Affirmation> = allAffirmations
}
