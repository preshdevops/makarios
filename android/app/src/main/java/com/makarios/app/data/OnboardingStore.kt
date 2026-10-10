package com.makarios.app.data

import android.content.Context
import com.makarios.app.ui.theme.Light
import kotlin.random.Random

data class OnboardingTopicTile(
    val title: String,
    val light: Light,
    val promise: String
)

object OnboardingStore {
    private const val PREFS = "makarios_prefs"
    private const val KEY_DONE = "onboardingDone"
    private const val KEY_LEGACY_DONE = "onboarding_completed"
    private const val KEY_TOPICS = "selectedTopics"

    val TOPIC_TILES = listOf(
        OnboardingTopicTile("Peace", Light.Mist, "Rest for a worried mind"),
        OnboardingTopicTile("Strength", Light.Ember, "When you have nothing left"),
        OnboardingTopicTile("Identity", Light.Dawn, "Who you are in Christ"),
        OnboardingTopicTile("Purpose", Light.Rain, "Why you are here"),
        OnboardingTopicTile("Courage", Light.Dusk, "For the next brave step"),
        OnboardingTopicTile("Joy", Light.Midday, "A heart that sings")
    )

    private val BANK: Map<String, List<Affirmation>> = mapOf(
        "Identity" to listOf(
            Affirmation(
                id = "bank-ident-1",
                declaration = "I am fully known, deeply loved, and precisely placed for this moment.",
                scriptureText = "You have searched me, Lord, and you know me. You know when I sit and when I rise; you perceive my thoughts from afar.",
                reference = "Psalm 139:1-2",
                category = "Identity",
                context = "David meditates on the inescapable love and presence of God. Nothing in your story is an accident.",
                tone = AffirmationTone.RESOLUTE,
                imageUrl = ""
            ),
            Affirmation(
                id = "bank-ident-2",
                declaration = "I am the righteousness of God in Christ Jesus. I walk with authority and grace.",
                scriptureText = "God made him who had no sin to be sin for us, so that in him we might become the righteousness of God.",
                reference = "2 Corinthians 5:21",
                category = "Identity",
                context = "Paul anchors our standing in Christ's finished reconciliation.",
                tone = AffirmationTone.RESOLUTE,
                imageUrl = ""
            ),
            Affirmation(
                id = "bank-ident-3",
                declaration = "I am God's child, chosen and sealed with His holy promise.",
                scriptureText = "In love he predestined us for adoption to sonship through Jesus Christ, in accordance with his pleasure and will.",
                reference = "Ephesians 1:4-5",
                category = "Identity",
                context = "Your belonging was authored before the foundations of the earth.",
                tone = AffirmationTone.RESOLUTE,
                imageUrl = ""
            )
        ),
        "Peace" to listOf(
            Affirmation(
                id = "bank-peace-1",
                declaration = "I do not walk in anxiety or overwhelm. God goes before me, and His peace guards my thoughts.",
                scriptureText = "You will keep in perfect peace those whose minds are steadfast, because they trust in you.",
                reference = "Isaiah 26:3",
                category = "Peace",
                context = "Written for seasons of transition and unexpected decisions.",
                tone = AffirmationTone.STILL,
                imageUrl = ""
            ),
            Affirmation(
                id = "bank-peace-2",
                declaration = "Be still. The battle is not yours alone.",
                scriptureText = "You will not have to fight this battle. Take up your positions; stand firm and see the deliverance the Lord will give you.",
                reference = "2 Chronicles 20:17",
                category = "Peace",
                context = "Resting in sovereign divine deliverance.",
                tone = AffirmationTone.STILL,
                imageUrl = ""
            ),
            Affirmation(
                id = "bank-peace-3",
                declaration = "The peace of God surpasses all understanding and guards my heart.",
                scriptureText = "And the peace of God, which transcends all understanding, will guard your hearts and your minds in Christ Jesus.",
                reference = "Philippians 4:7",
                category = "Peace",
                context = "Surrendering worries into the hands of the Father.",
                tone = AffirmationTone.STILL,
                imageUrl = ""
            )
        ),
        "Strength" to listOf(
            Affirmation(
                id = "bank-strength-1",
                declaration = "My strength is made perfect in weakness, for He is the one who goes before me.",
                scriptureText = "My grace is sufficient for you, for my power is made perfect in weakness.",
                reference = "2 Corinthians 12:9",
                category = "Strength",
                context = "When human stamina reaches its limit, divine power finds expression.",
                tone = AffirmationTone.RESOLUTE,
                imageUrl = ""
            ),
            Affirmation(
                id = "bank-strength-2",
                declaration = "I am more than a conqueror through Him who loved me.",
                scriptureText = "No, in all these things we are more than conquerors through him who loved us.",
                reference = "Romans 8:37",
                category = "Strength",
                context = "Emerging victorious through unbreakable love.",
                tone = AffirmationTone.RESOLUTE,
                imageUrl = ""
            ),
            Affirmation(
                id = "bank-strength-3",
                declaration = "The Lord is my strength and shield; my heart trusts in Him and I am helped.",
                scriptureText = "The Lord is my strength and my shield; my heart trusts in him, and he helps me. My heart leaps for joy.",
                reference = "Psalm 28:7",
                category = "Strength",
                context = "Confidence in the Lord as our everlasting fortress.",
                tone = AffirmationTone.RESOLUTE,
                imageUrl = ""
            )
        ),
        "Purpose" to listOf(
            Affirmation(
                id = "bank-purpose-1",
                declaration = "I am created with intention. My steps are ordered and my days are held in His hands.",
                scriptureText = "For we are God's handiwork, created in Christ Jesus to do good works, which God prepared in advance for us to do.",
                reference = "Ephesians 2:10",
                category = "Purpose",
                context = "You are not a cosmic coincidence. Your life was intentionally authored for purpose.",
                tone = AffirmationTone.RESOLUTE,
                imageUrl = ""
            ),
            Affirmation(
                id = "bank-purpose-2",
                declaration = "God's plans for me are good, giving me a future anchored in unshakeable hope.",
                scriptureText = "For I know the plans I have for you, plans to prosper you and not to harm you, plans to give you hope and a future.",
                reference = "Jeremiah 29:11",
                category = "Purpose",
                context = "Resting in the good design of the Father.",
                tone = AffirmationTone.RESOLUTE,
                imageUrl = ""
            ),
            Affirmation(
                id = "bank-purpose-3",
                declaration = "Every good work begun in me will be brought to completion by His faithful grace.",
                scriptureText = "Being confident of this, that he who began a good work in you will carry it on to completion until the day of Christ Jesus.",
                reference = "Philippians 1:6",
                category = "Purpose",
                context = "Divine patience finishing what grace started.",
                tone = AffirmationTone.RESOLUTE,
                imageUrl = ""
            )
        ),
        "Courage" to listOf(
            Affirmation(
                id = "bank-courage-1",
                declaration = "Fear has no room where faith resides. I am bold, brave, and courageous.",
                scriptureText = "Have I not commanded you? Be strong and courageous. Do not be afraid; do not be discouraged.",
                reference = "Joshua 1:9",
                category = "Courage",
                context = "Courage is obedience in the presence of fear.",
                tone = AffirmationTone.RESOLUTE,
                imageUrl = ""
            ),
            Affirmation(
                id = "bank-courage-2",
                declaration = "I approach every challenge with boldness, knowing the Lord Himself is my defense.",
                scriptureText = "So we say with confidence, 'The Lord is my helper; I will not be afraid. What can mere mortals do to me?'",
                reference = "Hebrews 13:6",
                category = "Courage",
                context = "Holy confidence standing on divine promises.",
                tone = AffirmationTone.RESOLUTE,
                imageUrl = ""
            ),
            Affirmation(
                id = "bank-courage-3",
                declaration = "The Lord is my light and my salvation-whom shall I fear?",
                scriptureText = "The Lord is my light and my salvation-whom shall I fear? The Lord is the stronghold of my life-of whom shall I be afraid?",
                reference = "Psalm 27:1",
                category = "Courage",
                context = "Fear dissolves in the light of His salvation.",
                tone = AffirmationTone.RESOLUTE,
                imageUrl = ""
            )
        ),
        "Joy" to listOf(
            Affirmation(
                id = "bank-joy-1",
                declaration = "The joy of the Lord is my strength and my shield.",
                scriptureText = "Do not grieve, for the joy of the Lord is your strength.",
                reference = "Nehemiah 8:10",
                category = "Joy",
                context = "Spiritual joy is not superficial optimism; it is the deep wellspring of God's delight.",
                tone = AffirmationTone.GENTLE,
                imageUrl = ""
            ),
            Affirmation(
                id = "bank-joy-2",
                declaration = "In His presence there is fullness of joy, and at His right hand are pleasures forevermore.",
                scriptureText = "You make known to me the path of life; you will fill me with joy in your presence.",
                reference = "Psalm 16:11",
                category = "Joy",
                context = "True delight cultivated in communion with God.",
                tone = AffirmationTone.GENTLE,
                imageUrl = ""
            ),
            Affirmation(
                id = "bank-joy-3",
                declaration = "The Lord has done great things for me, and I am filled with joy.",
                scriptureText = "The Lord has done great things for us, and we are filled with joy.",
                reference = "Psalm 126:3",
                category = "Joy",
                context = "Celebrating His faithfulness in all circumstances.",
                tone = AffirmationTone.GENTLE,
                imageUrl = ""
            )
        )
    )

    fun getBankForTopics(topics: Collection<String>): List<Affirmation> {
        val resolved = if (topics.isEmpty()) listOf("Identity") else topics.toList()
        val list = mutableListOf<Affirmation>()
        resolved.forEach { topic ->
            BANK[topic]?.let { list.addAll(it) }
        }
        return if (list.isNotEmpty()) list else BANK["Identity"].orEmpty()
    }

    fun isOnboardingDone(context: Context): Boolean {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return p.getBoolean(KEY_DONE, p.getBoolean(KEY_LEGACY_DONE, false))
    }

    fun setOnboardingDone(context: Context, done: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DONE, done)
            .putBoolean(KEY_LEGACY_DONE, done)
            .apply()
    }

    fun getSelectedTopics(context: Context): Set<String> {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getStringSet(KEY_TOPICS, emptySet()) ?: emptySet()
    }

    fun setSelectedTopics(context: Context, topics: Set<String>) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_TOPICS, topics)
            .apply()
    }

    fun resetOnboarding(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_DONE)
            .remove(KEY_LEGACY_DONE)
            .remove(KEY_TOPICS)
            .apply()
    }

    /**
     * Weights rotation: 60% probability from chosen topics, 40% from others.
     */
    fun getWeightedAffirmation(context: Context, pool: List<Affirmation>, random: Random = Random): Affirmation {
        val selected = getSelectedTopics(context)
        if (selected.isEmpty()) {
            return pool.randomOrNull(random) ?: AffirmationRepository.affirmationOfTheDay
        }

        val matching = pool.filter { a ->
            selected.any { it.equals(a.category, ignoreCase = true) }
        }
        val others = pool.filter { a ->
            selected.none { it.equals(a.category, ignoreCase = true) }
        }

        return if (matching.isNotEmpty() && (others.isEmpty() || random.nextFloat() < 0.60f)) {
            matching.random(random)
        } else if (others.isNotEmpty()) {
            others.random(random)
        } else {
            pool.randomOrNull(random) ?: AffirmationRepository.affirmationOfTheDay
        }
    }
}
