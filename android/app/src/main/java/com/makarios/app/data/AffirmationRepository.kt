package com.makarios.app.data

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
        context = "Step out of frantic preservation and breathe deeply into sovereign protection....",
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
            context = "Ancient paths required lanterns illuminating one step at a time; trust unfolds step by step.",
            category = "Direction",
            tone = AffirmationTone.GENTLE,
            imageUrl = "https://images.unsplash.com/photo-1518495973542-4542c06a5843?auto=format&fit=crop&w=1000&q=85"
        )
    )

    fun matchAffirmation(prompt: String, category: String, tone: AffirmationTone): Affirmation {
        val matchingCategory = curatedAffirmations.filter { it.category.equals(category, ignoreCase = true) }
        val matchingTone = matchingCategory.filter { it.tone == tone }
        return matchingTone.firstOrNull() ?: matchingCategory.firstOrNull() ?: curatedAffirmations.random()
    }

    fun getAll(): List<Affirmation> = curatedAffirmations
}
