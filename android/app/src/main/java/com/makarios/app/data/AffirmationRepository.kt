package com.makarios.app.data

import androidx.compose.runtime.mutableStateListOf

object AffirmationRepository {
    val starterAffirmation = Affirmation(
        id = "conf-1",
        declaration = "My worth is not negotiable, and my calling is secured.",
        scriptureText = "Now go; I will help you speak and will teach you what to say.",
        reference = "EXODUS 4:12",
        context = "Moses felt inadequate and slow of speech when God called him to face Pharaoh. God reminded Moses that He is the Creator of mouth and voice, promising His direct presence and wisdom.",
        category = "Confidence",
        tone = AffirmationTone.RESOLUTE,
        imageUrl = "https://images.unsplash.com/photo-1513519245088-0e12902e5a38?auto=format&fit=crop&w=1000&q=85"
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

    val categories = listOf("Confidence", "Peace", "Direction", "Grief", "Rest", "Healing")

    private val curatedAffirmations = listOf(
        starterAffirmation,
        widgetAffirmation,
        Affirmation(
            id = "peace-1",
            declaration = "My heart rests untroubled in His sovereign peace.",
            scriptureText = "Peace I leave with you; my peace I give you. I do not give to you as the world gives.",
            reference = "JOHN 14:27",
            context = "Jesus speaks to disciples troubled by impending departure, assuring them with peace not dependent on external calm.",
            category = "Peace",
            tone = AffirmationTone.STILL,
            imageUrl = "https://images.unsplash.com/photo-1499209974431-9dddcece7f88?auto=format&fit=crop&w=1000&q=85"
        ),
        Affirmation(
            id = "dir-1",
            declaration = "My steps are ordered and guarded by everlasting light.",
            scriptureText = "Your word is a lamp for my feet, a light on my path.",
            reference = "PSALM 119:105",
            context = "Ancient paths required lanterns illuminating one step at a time; divine direction unfolds faithfully step by step.",
            category = "Direction",
            tone = AffirmationTone.GENTLE,
            imageUrl = "https://images.unsplash.com/photo-1518495973542-4542c06a5843?auto=format&fit=crop&w=1000&q=85"
        ),
        Affirmation(
            id = "grief-1",
            declaration = "He is near to the brokenhearted; my sorrow is held in sacred care.",
            scriptureText = "The Lord is close to the brokenhearted and saves those who are crushed in spirit.",
            reference = "PSALM 34:18",
            context = "Grief often feels isolating, but the scriptures promise God's closest proximity during times of deep emotional pain.",
            category = "Grief",
            tone = AffirmationTone.GENTLE,
            imageUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?auto=format&fit=crop&w=1000&q=85"
        ),
        Affirmation(
            id = "rest-1",
            declaration = "I lay down my heavy burdens, for His yoke is easy and gentle.",
            scriptureText = "Come to me, all you who are weary and burdened, and I will give you rest.",
            reference = "MATTHEW 11:28",
            context = "True spiritual rest is not mere physical inactivity; it is surrendering performance to find peace in divine grace.",
            category = "Rest",
            tone = AffirmationTone.STILL,
            imageUrl = "https://images.unsplash.com/photo-1448375240586-882707db888b?auto=format&fit=crop&w=1000&q=85"
        ),
        Affirmation(
            id = "heal-1",
            declaration = "Every wound is seen, and restoration flows like morning light.",
            scriptureText = "He heals the brokenhearted and binds up their wounds.",
            reference = "PSALM 147:3",
            context = "God does not merely cover over human distress; He binds up wounds with tender, restorative craftsmanship.",
            category = "Healing",
            tone = AffirmationTone.RESOLUTE,
            imageUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=1000&q=85"
        )
    )

    // Reactive saved IDs set (initialized with starter for demo discovery)
    val savedAffirmationIds = mutableStateListOf("conf-1")

    fun isSaved(id: String): Boolean = savedAffirmationIds.contains(id)

    fun toggleSave(id: String) {
        if (savedAffirmationIds.contains(id)) {
            savedAffirmationIds.remove(id)
        } else {
            savedAffirmationIds.add(id)
        }
    }

    fun getSaved(): List<Affirmation> = curatedAffirmations.filter { savedAffirmationIds.contains(it.id) }

    fun getById(id: String): Affirmation? = curatedAffirmations.find { it.id == id } ?: curatedAffirmations.firstOrNull()

    fun matchAffirmation(prompt: String, category: String, tone: AffirmationTone): Affirmation {
        val matchingCategory = curatedAffirmations.filter { it.category.equals(category, ignoreCase = true) }
        val matchingTone = matchingCategory.filter { it.tone == tone }
        return matchingTone.firstOrNull() ?: matchingCategory.firstOrNull() ?: curatedAffirmations.random()
    }

    fun getAll(): List<Affirmation> = curatedAffirmations
}
