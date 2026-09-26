package com.makarios.app.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class MoodCategory(
    val name: String,
    val subtitle: String,
    val imageUrl: String
)

object AffirmationRepository {

    // ── Mood categories with atmospheric sacred photography ───────
    val moodCategories = listOf(
        MoodCategory(
            name = "Peace",
            subtitle = "Stillness over anxiety",
            imageUrl = "https://images.unsplash.com/photo-1507652313519-d4e9174996dd?auto=format&fit=crop&w=800&q=85"
        ),
        MoodCategory(
            name = "Strength",
            subtitle = "Endurance in weakness",
            imageUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=800&q=85"
        ),
        MoodCategory(
            name = "Identity",
            subtitle = "Who God says you are",
            imageUrl = "https://images.unsplash.com/photo-1518495973542-4542c06a5843?auto=format&fit=crop&w=800&q=85"
        ),
        MoodCategory(
            name = "Purpose",
            subtitle = "Calling & divine alignment",
            imageUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=800&q=85"
        ),
        MoodCategory(
            name = "Courage",
            subtitle = "Holy boldness over fear",
            imageUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=800&q=85"
        ),
        MoodCategory(
            name = "Joy",
            subtitle = "Unshakeable gladness",
            imageUrl = "https://images.unsplash.com/photo-1500534623283-312aade485b7?auto=format&fit=crop&w=800&q=85"
        ),
        MoodCategory(
            name = "Provision",
            subtitle = "Resting in abundance",
            imageUrl = "https://images.unsplash.com/photo-1499209974431-9dddcece7f88?auto=format&fit=crop&w=800&q=85"
        ),
        MoodCategory(
            name = "Confidence",
            subtitle = "Christ our defense",
            imageUrl = "https://images.unsplash.com/photo-1519681393784-d120267933ba?auto=format&fit=crop&w=800&q=85"
        )
    )

    // ── Curated Affirmations with Evocative Photography ───────────
    val affirmationOfTheDay = Affirmation(
        id = "aotd-1",
        declaration = "I am fully known, deeply loved, and precisely placed for this moment.",
        scriptureText = "You have searched me, Lord, and you know me. You know when I sit and when I rise; you perceive my thoughts from afar.",
        reference = "Inspired by Psalm 139",
        context = "David meditates on the inescapable love and sovereign knowledge of God. Nothing in your story is an accident.",
        category = "Identity",
        tone = AffirmationTone.RESOLUTE,
        imageUrl = "https://images.unsplash.com/photo-1507652313519-d4e9174996dd?auto=format&fit=crop&w=1200&q=85"
    )

    val fullscreenAffirmation = Affirmation(
        id = "ident-1",
        declaration = "I am the righteousness of God in Christ Jesus. I walk with authority and grace.",
        scriptureText = "God made him who had no sin to be sin for us, so that in him we might become the righteousness of God.",
        reference = "2 CORINTHIANS 5:21",
        personalDeclaration = "Today, I choose to see myself as God sees me. I will not be moved by my feelings or circumstances.",
        context = "Paul anchors the believer's standing not in personal performance, but in Christ's finished reconciliation.",
        category = "Identity",
        tone = AffirmationTone.RESOLUTE,
        imageUrl = "https://images.unsplash.com/photo-1518495973542-4542c06a5843?auto=format&fit=crop&w=1200&q=85"
    )

    val strengthAffirmation = Affirmation(
        id = "str-1",
        declaration = "My strength is made perfect in weakness, for He is the one who goes before me.",
        scriptureText = "My grace is sufficient for you, for my power is made perfect in weakness.",
        reference = "2 CORINTHIANS 12:9",
        context = "When human stamina reaches its limit, divine power finds its most potent expression.",
        category = "Strength",
        tone = AffirmationTone.RESOLUTE,
        imageUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=1200&q=85"
    )

    val provisionAffirmation = Affirmation(
        id = "prov-1",
        declaration = "I walk in abundance and peace, for my provider is the King of kings.",
        scriptureText = "And my God will meet all your needs according to the riches of his glory in Christ Jesus.",
        reference = "PHILIPPIANS 4:19",
        context = "Paul writes from confinement with unshakable assurance that divine resources are inexhaustible.",
        category = "Provision",
        tone = AffirmationTone.STILL,
        imageUrl = "https://images.unsplash.com/photo-1499209974431-9dddcece7f88?auto=format&fit=crop&w=1200&q=85"
    )

    val courageAffirmation = Affirmation(
        id = "cour-1",
        declaration = "Fear has no room where faith resides. I am bold, brave, and courageous.",
        scriptureText = "Have I not commanded you? Be strong and courageous. Do not be afraid; do not be discouraged.",
        reference = "JOSHUA 1:9",
        context = "Joshua stood on the threshold of unknown territory. Courage is obedience in the presence of fear.",
        category = "Courage",
        tone = AffirmationTone.RESOLUTE,
        imageUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=1200&q=85"
    )

    val favorite1 = Affirmation(
        id = "fav-1",
        declaration = "I am more than a conqueror through Him who loved me.",
        scriptureText = "No, in all these things we are more than conquerors through him who loved us.",
        reference = "ROMANS 8:37",
        context = "Victory is not merely surviving difficulty, but emerging deepened in sovereign love.",
        category = "Strength",
        tone = AffirmationTone.RESOLUTE,
        imageUrl = "https://images.unsplash.com/photo-1448375240586-882707db888b?auto=format&fit=crop&w=1200&q=85",
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
        imageUrl = "https://images.unsplash.com/photo-1500534623283-312aade485b7?auto=format&fit=crop&w=1200&q=85",
        isFavorite = true
    )

    val defaultWidgetAffirmation = Affirmation(
        id = "widget-1",
        declaration = "Be still. The battle is not yours alone.",
        scriptureText = "You will not have to fight this battle. Take up your positions; stand firm and see the deliverance the Lord will give you.",
        reference = "2 CHRONICLES 20:17",
        context = "Step out of frantic preservation and breathe deeply into sovereign protection. God takes responsibility for the outcome.",
        category = "Peace",
        tone = AffirmationTone.STILL,
        imageUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=1200&q=85"
    )

    var widgetAffirmation by mutableStateOf(defaultWidgetAffirmation)

    fun updateActiveWidgetAffirmation(affirmation: Affirmation) {
        widgetAffirmation = affirmation
    }

    val purposeAffirmation = Affirmation(
        id = "purp-1",
        declaration = "I am created with intention. My steps are ordered and my days are held in His hands.",
        scriptureText = "For we are God's handiwork, created in Christ Jesus to do good works, which God prepared in advance for us to do.",
        reference = "EPHESIANS 2:10",
        context = "You are not a cosmic coincidence. Your life was intentionally authored for sovereign purpose.",
        category = "Purpose",
        tone = AffirmationTone.RESOLUTE,
        imageUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=1200&q=85"
    )

    val confidenceAffirmation = Affirmation(
        id = "conf-1",
        declaration = "I approach every challenge with boldness, knowing the Lord Himself is my defense.",
        scriptureText = "So we say with confidence, 'The Lord is my helper; I will not be afraid. What can mere mortals do to me?'",
        reference = "HEBREWS 13:6",
        context = "True confidence is not reliance on self-sufficiency, but deep reliance on God's unending faithfulness.",
        category = "Confidence",
        tone = AffirmationTone.RESOLUTE,
        imageUrl = "https://images.unsplash.com/photo-1519681393784-d120267933ba?auto=format&fit=crop&w=1200&q=85"
    )

    val relationshipsAffirmation = Affirmation(
        id = "rel-1",
        declaration = "I walk in love, forgiveness, and understanding, bearing with others as Christ bore with me.",
        scriptureText = "Be completely humble and gentle; be patient, bearing with one another in love.",
        reference = "EPHESIANS 4:2",
        context = "Grace in relationships requires laying down self-preservation and offering the same mercy we received.",
        category = "Relationships",
        tone = AffirmationTone.GENTLE,
        imageUrl = "https://images.unsplash.com/photo-1499209974431-9dddcece7f88?auto=format&fit=crop&w=1200&q=85"
    )

    val disciplineAffirmation = Affirmation(
        id = "disc-1",
        declaration = "I have a spirit of self-control and clarity. I run with endurance the race set before me.",
        scriptureText = "For God has not given us a spirit of fear, but of power and of love and of a sound mind.",
        reference = "2 TIMOTHY 1:7",
        context = "Spiritual discipline is not self-punishment; it is training our affections in alignment with God's truth.",
        category = "Discipline",
        tone = AffirmationTone.RESOLUTE,
        imageUrl = "https://images.unsplash.com/photo-1448375240586-882707db888b?auto=format&fit=crop&w=1200&q=85"
    )

    val starterAffirmation = fullscreenAffirmation

    val focusAreas = listOf("Peace", "Strength", "Identity", "Purpose", "Joy", "Confidence", "Relationships", "Discipline")
    val categories = listOf("All", "Identity", "Peace", "Strength", "Purpose", "Courage", "Joy", "Provision", "Confidence", "Relationships", "Discipline")

    private val allAffirmations = listOf(
        fullscreenAffirmation,
        affirmationOfTheDay,
        strengthAffirmation,
        provisionAffirmation,
        courageAffirmation,
        favorite1,
        favorite2,
        defaultWidgetAffirmation,
        purposeAffirmation,
        confidenceAffirmation,
        relationshipsAffirmation,
        disciplineAffirmation
    )

    // Reactive saved IDs
    val savedAffirmationIds = mutableStateListOf("ident-1", "fav-1", "fav-2")

    // Personal user-authored declarations
    val personalAffirmations = mutableStateListOf(
        Affirmation(
            id = "personal-1",
            declaration = "I do not walk in anxiety or overwhelm. God goes before me, and His peace guards my thoughts.",
            scriptureText = "You will keep in perfect peace those whose minds are steadfast, because they trust in you.",
            reference = "ISAIAH 26:3",
            context = "Written for seasons of transition and unexpected decisions.",
            category = "Peace",
            tone = AffirmationTone.STILL,
            imageUrl = "https://images.unsplash.com/photo-1507652313519-d4e9174996dd?auto=format&fit=crop&w=1200&q=85",
            isFavorite = true,
            personalDeclaration = "I release the need to control the outcome. My steps are ordered by the Lord."
        )
    )

    fun addPersonalAffirmation(affirmation: Affirmation) {
        personalAffirmations.add(0, affirmation)
        if (!savedAffirmationIds.contains(affirmation.id)) {
            savedAffirmationIds.add(affirmation.id)
        }
    }

    fun isSaved(id: String): Boolean = savedAffirmationIds.contains(id)

    fun toggleSave(id: String) {
        if (savedAffirmationIds.contains(id)) {
            savedAffirmationIds.remove(id)
        } else {
            savedAffirmationIds.add(id)
        }
    }

    fun getSaved(): List<Affirmation> = (personalAffirmations + allAffirmations).filter { savedAffirmationIds.contains(it.id) }

    fun getFavorites(): List<Affirmation> = (personalAffirmations + allAffirmations).filter { it.isFavorite || savedAffirmationIds.contains(it.id) }

    fun getById(id: String): Affirmation =
        personalAffirmations.find { it.id == id }
            ?: allAffirmations.find { it.id == id }
            ?: fullscreenAffirmation

    fun getAll(): List<Affirmation> = personalAffirmations + allAffirmations
}
