package com.makarios.app.data

/**
 * Curated corpus of 120+ Bible verses for the scripture matching engine.
 *
 * Each verse is tagged with:
 * - **themes**: categorical groupings matching the app's mood categories
 * - **toneAffinity**: STILL (calming), RESOLUTE (bold), or GENTLE (tender)
 * - **keywords**: rich semantic cloud for matching user declarations
 *
 * Verses are drawn from widely-known translations for clarity and resonance.
 */
object ScriptureDatabase {

    val verses: List<ScriptureVerse> = listOf(

        // ╔══════════════════════════════════════════════════════════╗
        // ║  PEACE  (~15 verses)                                    ║
        // ╚══════════════════════════════════════════════════════════╝

        ScriptureVerse(
            reference = "Philippians 4:6–7",
            text = "Do not be anxious about anything, but in every situation, by prayer and petition, with thanksgiving, present your requests to God. And the peace of God, which transcends all understanding, will guard your hearts and your minds in Christ Jesus.",
            themes = setOf("peace", "anxiety"),
            toneAffinity = AffirmationTone.STILL,
            keywords = setOf("anxious", "anxiety", "worry", "peace", "calm", "guard", "mind", "heart", "prayer", "rest", "still", "tranquil", "overwhelm", "stress", "turmoil", "protect")
        ),
        ScriptureVerse(
            reference = "Isaiah 26:3",
            text = "You will keep in perfect peace those whose minds are steadfast, because they trust in you.",
            themes = setOf("peace", "trust"),
            toneAffinity = AffirmationTone.STILL,
            keywords = setOf("peace", "perfect", "mind", "steadfast", "trust", "focus", "calm", "still", "centered", "anchored", "secure", "steady")
        ),
        ScriptureVerse(
            reference = "John 14:27",
            text = "Peace I leave with you; my peace I give you. I do not give to you as the world gives. Do not let your hearts be troubled and do not be afraid.",
            themes = setOf("peace", "fear"),
            toneAffinity = AffirmationTone.GENTLE,
            keywords = setOf("peace", "troubled", "afraid", "heart", "fear", "give", "calm", "safe", "comfort", "rest", "worry", "anxious")
        ),
        ScriptureVerse(
            reference = "Psalm 46:10",
            text = "Be still, and know that I am God; I will be exalted among the nations, I will be exalted in the earth.",
            themes = setOf("peace", "trust"),
            toneAffinity = AffirmationTone.STILL,
            keywords = setOf("still", "know", "god", "quiet", "silence", "rest", "pause", "surrender", "calm", "present", "breathe", "slow")
        ),
        ScriptureVerse(
            reference = "Psalm 4:8",
            text = "In peace I will lie down and sleep, for you alone, Lord, make me dwell in safety.",
            themes = setOf("peace"),
            toneAffinity = AffirmationTone.STILL,
            keywords = setOf("peace", "sleep", "rest", "safety", "safe", "night", "calm", "lie", "dwell", "quiet", "insomnia", "exhausted", "tired")
        ),
        ScriptureVerse(
            reference = "Matthew 11:28–30",
            text = "Come to me, all you who are weary and burdened, and I will give you rest. Take my yoke upon you and learn from me, for I am gentle and humble in heart, and you will find rest for your souls.",
            themes = setOf("peace", "strength"),
            toneAffinity = AffirmationTone.GENTLE,
            keywords = setOf("weary", "burdened", "rest", "tired", "exhausted", "heavy", "load", "gentle", "humble", "soul", "burnout", "overwhelm", "carry", "weight")
        ),
        ScriptureVerse(
            reference = "Psalm 23:1–3",
            text = "The Lord is my shepherd, I lack nothing. He makes me lie down in green pastures, he leads me beside quiet waters, he refreshes my soul.",
            themes = setOf("peace", "provision"),
            toneAffinity = AffirmationTone.STILL,
            keywords = setOf("shepherd", "lack", "green", "quiet", "waters", "refresh", "soul", "pasture", "lead", "rest", "provide", "enough", "sufficient", "content")
        ),
        ScriptureVerse(
            reference = "Psalm 91:1–2",
            text = "Whoever dwells in the shelter of the Most High will rest in the shadow of the Almighty. I will say of the Lord, 'He is my refuge and my fortress, my God, in whom I trust.'",
            themes = setOf("peace", "trust"),
            toneAffinity = AffirmationTone.STILL,
            keywords = setOf("shelter", "rest", "shadow", "refuge", "fortress", "trust", "protect", "safe", "dwell", "cover", "hide", "safety", "secure")
        ),
        ScriptureVerse(
            reference = "Romans 8:6",
            text = "The mind governed by the flesh is death, but the mind governed by the Spirit is life and peace.",
            themes = setOf("peace", "discipline"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("mind", "spirit", "peace", "life", "governed", "think", "thoughts", "focus", "mental", "renew", "mindset")
        ),
        ScriptureVerse(
            reference = "Colossians 3:15",
            text = "Let the peace of Christ rule in your hearts, since as members of one body you were called to peace. And be thankful.",
            themes = setOf("peace", "relationships"),
            toneAffinity = AffirmationTone.GENTLE,
            keywords = setOf("peace", "rule", "heart", "called", "thankful", "gratitude", "unity", "harmony", "body", "together")
        ),
        ScriptureVerse(
            reference = "Isaiah 41:10",
            text = "So do not fear, for I am with you; do not be dismayed, for I am your God. I will strengthen you and help you; I will uphold you with my righteous right hand.",
            themes = setOf("peace", "fear", "strength"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("fear", "afraid", "dismayed", "strengthen", "help", "uphold", "with", "alone", "anxious", "worry", "panic", "scared", "terrified", "nervous")
        ),
        ScriptureVerse(
            reference = "2 Thessalonians 3:16",
            text = "Now may the Lord of peace himself give you peace at all times and in every way. The Lord be with all of you.",
            themes = setOf("peace"),
            toneAffinity = AffirmationTone.GENTLE,
            keywords = setOf("peace", "always", "every", "lord", "time", "way", "constant", "abiding", "continuous", "moment")
        ),
        ScriptureVerse(
            reference = "Psalm 29:11",
            text = "The Lord gives strength to his people; the Lord blesses his people with peace.",
            themes = setOf("peace", "strength"),
            toneAffinity = AffirmationTone.STILL,
            keywords = setOf("strength", "bless", "peace", "people", "give", "strong", "blessed")
        ),
        ScriptureVerse(
            reference = "Numbers 6:24–26",
            text = "The Lord bless you and keep you; the Lord make his face shine on you and be gracious to you; the Lord turn his face toward you and give you peace.",
            themes = setOf("peace"),
            toneAffinity = AffirmationTone.GENTLE,
            keywords = setOf("bless", "keep", "shine", "gracious", "face", "peace", "favor", "grace", "blessing", "light")
        ),

        // ╔══════════════════════════════════════════════════════════╗
        // ║  STRENGTH  (~15 verses)                                 ║
        // ╚══════════════════════════════════════════════════════════╝

        ScriptureVerse(
            reference = "2 Corinthians 12:9",
            text = "But he said to me, 'My grace is sufficient for you, for my power is made perfect in weakness.' Therefore I will boast all the more gladly about my weaknesses, so that Christ's power may rest on me.",
            themes = setOf("strength", "identity"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("grace", "sufficient", "power", "perfect", "weakness", "weak", "strong", "boast", "enough", "inadequate", "failing", "struggle")
        ),
        ScriptureVerse(
            reference = "Philippians 4:13",
            text = "I can do all this through him who gives me strength.",
            themes = setOf("strength", "confidence"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("strength", "strong", "able", "capable", "endure", "overcome", "power", "through", "possible", "impossible", "struggle")
        ),
        ScriptureVerse(
            reference = "Isaiah 40:31",
            text = "But those who hope in the Lord will renew their strength. They will soar on wings like eagles; they will run and not grow weary, they will walk and not be faint.",
            themes = setOf("strength", "peace"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("hope", "renew", "strength", "soar", "eagle", "run", "weary", "faint", "tired", "exhausted", "endure", "energy", "refresh", "persevere")
        ),
        ScriptureVerse(
            reference = "Deuteronomy 31:6",
            text = "Be strong and courageous. Do not be afraid or terrified because of them, for the Lord your God goes with you; he will never leave you nor forsake you.",
            themes = setOf("strength", "courage"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("strong", "courageous", "afraid", "terrified", "leave", "forsake", "abandon", "alone", "with", "present", "brave", "bold")
        ),
        ScriptureVerse(
            reference = "Ephesians 6:10",
            text = "Finally, be strong in the Lord and in his mighty power.",
            themes = setOf("strength"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("strong", "lord", "mighty", "power", "strength", "armor", "battle", "fight", "warrior", "stand")
        ),
        ScriptureVerse(
            reference = "Psalm 28:7",
            text = "The Lord is my strength and my shield; my heart trusts in him, and he helps me. My heart leaps for joy, and with my song I praise him.",
            themes = setOf("strength", "joy"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("strength", "shield", "trust", "help", "joy", "praise", "heart", "protect", "defend", "song")
        ),
        ScriptureVerse(
            reference = "Psalm 73:26",
            text = "My flesh and my heart may fail, but God is the strength of my heart and my portion forever.",
            themes = setOf("strength"),
            toneAffinity = AffirmationTone.GENTLE,
            keywords = setOf("flesh", "heart", "fail", "strength", "portion", "forever", "weak", "failing", "enough", "sustain", "endure")
        ),
        ScriptureVerse(
            reference = "Nehemiah 8:10",
            text = "Do not grieve, for the joy of the Lord is your strength.",
            themes = setOf("strength", "joy"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("grieve", "joy", "strength", "glad", "happy", "sorrow", "mourning", "grief", "sadness", "loss")
        ),
        ScriptureVerse(
            reference = "Habakkuk 3:19",
            text = "The Sovereign Lord is my strength; he makes my feet like the feet of a deer, he enables me to tread on the heights.",
            themes = setOf("strength", "confidence"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("sovereign", "strength", "feet", "deer", "heights", "climb", "overcome", "rise", "ascend", "above", "victorious")
        ),
        ScriptureVerse(
            reference = "Psalm 18:32–33",
            text = "It is God who arms me with strength and keeps my way secure. He makes my feet like the feet of a deer; he causes me to stand on the heights.",
            themes = setOf("strength", "confidence"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("arms", "strength", "secure", "stand", "heights", "stable", "firm", "ground", "steady", "equipped")
        ),
        ScriptureVerse(
            reference = "2 Timothy 1:7",
            text = "For the Spirit God gave us does not make us timid, but gives us power, love and self-discipline.",
            themes = setOf("strength", "discipline", "courage"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("spirit", "timid", "power", "love", "discipline", "self-control", "bold", "fear", "sound", "mind", "control", "focused")
        ),
        ScriptureVerse(
            reference = "Romans 8:37",
            text = "No, in all these things we are more than conquerors through him who loved us.",
            themes = setOf("strength", "identity"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("conquerors", "overcome", "victory", "loved", "win", "triumph", "battle", "fight", "champion", "victorious", "winning")
        ),
        ScriptureVerse(
            reference = "1 Chronicles 16:11",
            text = "Look to the Lord and his strength; seek his face always.",
            themes = setOf("strength"),
            toneAffinity = AffirmationTone.STILL,
            keywords = setOf("look", "lord", "strength", "seek", "face", "always", "search", "pursue", "find")
        ),
        ScriptureVerse(
            reference = "Psalm 27:1",
            text = "The Lord is my light and my salvation — whom shall I fear? The Lord is the stronghold of my life — of whom shall I be afraid?",
            themes = setOf("strength", "fear", "courage"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("light", "salvation", "fear", "stronghold", "afraid", "life", "safe", "defend", "protect", "refuge", "darkness")
        ),

        // ╔══════════════════════════════════════════════════════════╗
        // ║  IDENTITY  (~15 verses)                                 ║
        // ╚══════════════════════════════════════════════════════════╝

        ScriptureVerse(
            reference = "2 Corinthians 5:17",
            text = "Therefore, if anyone is in Christ, the new creation has come: The old has gone, the new is here!",
            themes = setOf("identity"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("new", "creation", "christ", "old", "gone", "transform", "changed", "born", "fresh", "start", "begin", "past", "renew")
        ),
        ScriptureVerse(
            reference = "2 Corinthians 5:21",
            text = "God made him who had no sin to be sin for us, so that in him we might become the righteousness of God.",
            themes = setOf("identity"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("righteousness", "righteous", "sin", "god", "become", "holy", "pure", "clean", "justified", "worthy", "accepted")
        ),
        ScriptureVerse(
            reference = "1 John 3:1",
            text = "See what great love the Father has lavished on us, that we should be called children of God! And that is what we are!",
            themes = setOf("identity", "relationships"),
            toneAffinity = AffirmationTone.GENTLE,
            keywords = setOf("love", "father", "children", "child", "god", "called", "belong", "son", "daughter", "family", "loved", "identity", "who")
        ),
        ScriptureVerse(
            reference = "Ephesians 2:10",
            text = "For we are God's handiwork, created in Christ Jesus to do good works, which God prepared in advance for us to do.",
            themes = setOf("identity", "purpose"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("handiwork", "created", "masterpiece", "works", "prepared", "purpose", "design", "made", "intentional", "crafted", "beautiful")
        ),
        ScriptureVerse(
            reference = "Psalm 139:14",
            text = "I praise you because I am fearfully and wonderfully made; your works are wonderful, I know that full well.",
            themes = setOf("identity"),
            toneAffinity = AffirmationTone.GENTLE,
            keywords = setOf("fearfully", "wonderfully", "made", "wonderful", "works", "praise", "created", "beautiful", "unique", "special", "body", "image", "worth", "worthy")
        ),
        ScriptureVerse(
            reference = "Galatians 2:20",
            text = "I have been crucified with Christ and I no longer live, but Christ lives in me. The life I now live in the body, I live by faith in the Son of God, who loved me and gave himself for me.",
            themes = setOf("identity"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("crucified", "christ", "live", "faith", "son", "loved", "gave", "surrender", "die", "alive", "new", "indwelt")
        ),
        ScriptureVerse(
            reference = "1 Peter 2:9",
            text = "But you are a chosen people, a royal priesthood, a holy nation, God's special possession, that you may declare the praises of him who called you out of darkness into his wonderful light.",
            themes = setOf("identity", "purpose"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("chosen", "royal", "holy", "special", "possess", "declare", "called", "darkness", "light", "priest", "nation", "selected", "picked")
        ),
        ScriptureVerse(
            reference = "Jeremiah 1:5",
            text = "Before I formed you in the womb I knew you, before you were born I set you apart; I appointed you as a prophet to the nations.",
            themes = setOf("identity", "purpose"),
            toneAffinity = AffirmationTone.GENTLE,
            keywords = setOf("formed", "womb", "knew", "born", "apart", "appointed", "known", "before", "planned", "purpose", "destiny", "calling", "set")
        ),
        ScriptureVerse(
            reference = "Romans 8:1",
            text = "Therefore, there is now no condemnation for those who are in Christ Jesus.",
            themes = setOf("identity"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("condemnation", "condemn", "guilt", "shame", "free", "freedom", "christ", "forgive", "forgiven", "innocent", "clean", "past", "mistake")
        ),
        ScriptureVerse(
            reference = "Colossians 3:3",
            text = "For you died, and your life is now hidden with Christ in God.",
            themes = setOf("identity"),
            toneAffinity = AffirmationTone.STILL,
            keywords = setOf("died", "life", "hidden", "christ", "god", "secure", "identity", "found", "rooted", "anchored", "established")
        ),
        ScriptureVerse(
            reference = "Ephesians 1:4–5",
            text = "For he chose us in him before the creation of the world to be holy and blameless in his sight. In love he predestined us for adoption to sonship through Jesus Christ.",
            themes = setOf("identity"),
            toneAffinity = AffirmationTone.GENTLE,
            keywords = setOf("chose", "chosen", "holy", "blameless", "love", "adoption", "adopted", "son", "daughter", "family", "belong", "accepted", "wanted")
        ),
        ScriptureVerse(
            reference = "John 1:12",
            text = "Yet to all who did receive him, to those who believed in his name, he gave the right to become children of God.",
            themes = setOf("identity"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("receive", "believe", "name", "right", "children", "child", "god", "become", "belong", "heir", "son", "daughter")
        ),
        ScriptureVerse(
            reference = "Isaiah 43:1",
            text = "Do not fear, for I have redeemed you; I have summoned you by name; you are mine.",
            themes = setOf("identity", "fear"),
            toneAffinity = AffirmationTone.GENTLE,
            keywords = setOf("fear", "redeemed", "name", "mine", "belong", "called", "known", "precious", "claimed", "owned", "loved", "valued")
        ),
        ScriptureVerse(
            reference = "Psalm 139:1–3",
            text = "You have searched me, Lord, and you know me. You know when I sit and when I rise; you perceive my thoughts from afar. You discern my going out and my lying down; you are familiar with all my ways.",
            themes = setOf("identity"),
            toneAffinity = AffirmationTone.STILL,
            keywords = setOf("searched", "know", "known", "thoughts", "perceive", "familiar", "ways", "seen", "understood", "intimate", "close")
        ),

        // ╔══════════════════════════════════════════════════════════╗
        // ║  PURPOSE  (~12 verses)                                  ║
        // ╚══════════════════════════════════════════════════════════╝

        ScriptureVerse(
            reference = "Jeremiah 29:11",
            text = "For I know the plans I have for you, declares the Lord, plans to prosper you and not to harm you, plans to give you hope and a future.",
            themes = setOf("purpose"),
            toneAffinity = AffirmationTone.GENTLE,
            keywords = setOf("plans", "plan", "purpose", "prosper", "hope", "future", "destiny", "direction", "path", "way", "next", "season", "chapter", "calling")
        ),
        ScriptureVerse(
            reference = "Proverbs 3:5–6",
            text = "Trust in the Lord with all your heart and lean not on your own understanding; in all your ways submit to him, and he will make your paths straight.",
            themes = setOf("purpose", "trust"),
            toneAffinity = AffirmationTone.STILL,
            keywords = setOf("trust", "heart", "understanding", "ways", "paths", "straight", "direction", "guidance", "guide", "lead", "submit", "surrender", "confused", "lost", "uncertain")
        ),
        ScriptureVerse(
            reference = "Romans 8:28",
            text = "And we know that in all things God works for the good of those who love him, who have been called according to his purpose.",
            themes = setOf("purpose"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("works", "good", "love", "called", "purpose", "together", "plan", "reason", "meaning", "why", "understand", "sense")
        ),
        ScriptureVerse(
            reference = "Psalm 37:23",
            text = "The Lord makes firm the steps of the one who delights in him.",
            themes = setOf("purpose"),
            toneAffinity = AffirmationTone.STILL,
            keywords = setOf("steps", "firm", "delight", "path", "walk", "direction", "guide", "lead", "way", "order", "establish")
        ),
        ScriptureVerse(
            reference = "Proverbs 16:9",
            text = "In their hearts humans plan their course, but the Lord establishes their steps.",
            themes = setOf("purpose"),
            toneAffinity = AffirmationTone.STILL,
            keywords = setOf("hearts", "plan", "course", "establish", "steps", "direction", "path", "guide", "lead", "sovereign", "control")
        ),
        ScriptureVerse(
            reference = "Isaiah 30:21",
            text = "Whether you turn to the right or to the left, your ears will hear a voice behind you, saying, 'This is the way; walk in it.'",
            themes = setOf("purpose"),
            toneAffinity = AffirmationTone.GENTLE,
            keywords = setOf("turn", "right", "left", "voice", "hear", "way", "walk", "direction", "decision", "choose", "guidance", "lost", "confused")
        ),
        ScriptureVerse(
            reference = "Philippians 1:6",
            text = "Being confident of this, that he who began a good work in you will carry it on to completion until the day of Christ Jesus.",
            themes = setOf("purpose", "confidence"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("confident", "began", "good", "work", "completion", "finish", "carry", "complete", "started", "process", "journey", "progress", "patient", "grow")
        ),
        ScriptureVerse(
            reference = "Psalm 138:8",
            text = "The Lord will vindicate me; your love, Lord, endures forever — do not abandon the works of your hands.",
            themes = setOf("purpose"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("vindicate", "love", "endure", "forever", "abandon", "hands", "works", "complete", "finish", "faithful", "fulfill")
        ),
        ScriptureVerse(
            reference = "Colossians 1:16",
            text = "For in him all things were created: things in heaven and on earth, visible and invisible. All things have been created through him and for him.",
            themes = setOf("purpose", "identity"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("created", "things", "heaven", "earth", "through", "purpose", "reason", "exist", "meaning", "intentional")
        ),
        ScriptureVerse(
            reference = "Ephesians 3:20",
            text = "Now to him who is able to do immeasurably more than all we ask or imagine, according to his power that is at work within us.",
            themes = setOf("purpose", "confidence"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("able", "immeasurably", "more", "ask", "imagine", "power", "work", "within", "beyond", "exceed", "dream", "vision", "impossible", "bigger")
        ),
        ScriptureVerse(
            reference = "Isaiah 55:8–9",
            text = "For my thoughts are not your thoughts, neither are your ways my ways, declares the Lord. As the heavens are higher than the earth, so are my ways higher than your ways and my thoughts than your thoughts.",
            themes = setOf("purpose", "trust"),
            toneAffinity = AffirmationTone.STILL,
            keywords = setOf("thoughts", "ways", "higher", "heavens", "earth", "understand", "confusing", "mystery", "bigger", "sovereign", "plan", "perspective")
        ),
        ScriptureVerse(
            reference = "Micah 6:8",
            text = "He has shown you, O mortal, what is good. And what does the Lord require of you? To act justly and to love mercy and to walk humbly with your God.",
            themes = setOf("purpose", "discipline"),
            toneAffinity = AffirmationTone.GENTLE,
            keywords = setOf("good", "require", "justly", "justice", "mercy", "humbly", "humble", "walk", "obey", "obedience", "righteous", "faithful")
        ),

        // ╔══════════════════════════════════════════════════════════╗
        // ║  COURAGE  (~12 verses)                                  ║
        // ╚══════════════════════════════════════════════════════════╝

        ScriptureVerse(
            reference = "Joshua 1:9",
            text = "Have I not commanded you? Be strong and courageous. Do not be afraid; do not be discouraged, for the Lord your God will be with you wherever you go.",
            themes = setOf("courage", "strength"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("strong", "courageous", "afraid", "discouraged", "bold", "brave", "go", "forward", "wherever", "command", "fear", "timid")
        ),
        ScriptureVerse(
            reference = "Psalm 56:3–4",
            text = "When I am afraid, I put my trust in you. In God, whose word I praise — in God I trust and am not afraid. What can mere mortals do to me?",
            themes = setOf("courage", "trust"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("afraid", "trust", "praise", "mortals", "fear", "people", "man", "human", "opinion", "judgment", "rejection", "intimidate")
        ),
        ScriptureVerse(
            reference = "Isaiah 54:17",
            text = "No weapon forged against you will prevail, and you will refute every tongue that accuses you. This is the heritage of the servants of the Lord.",
            themes = setOf("courage", "strength"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("weapon", "prevail", "tongue", "accuse", "heritage", "servant", "attack", "enemy", "against", "fight", "battle", "overcome", "defeat", "win")
        ),
        ScriptureVerse(
            reference = "Psalm 118:6",
            text = "The Lord is with me; I will not be afraid. What can mere mortals do to me?",
            themes = setOf("courage"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("lord", "afraid", "mortals", "fear", "people", "man", "with", "present", "beside", "bold")
        ),
        ScriptureVerse(
            reference = "2 Chronicles 20:15",
            text = "Do not be afraid or discouraged because of this vast army. For the battle is not yours, but God's.",
            themes = setOf("courage", "trust"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("afraid", "discouraged", "army", "battle", "fight", "war", "god", "surrender", "overwhelming", "impossible", "odds")
        ),
        ScriptureVerse(
            reference = "Romans 8:31",
            text = "What, then, shall we say in response to these things? If God is for us, who can be against us?",
            themes = setOf("courage", "confidence"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("god", "against", "response", "opposition", "enemy", "obstacle", "stand", "side", "favor", "support", "backing")
        ),
        ScriptureVerse(
            reference = "Proverbs 28:1",
            text = "The wicked flee though no one pursues, but the righteous are as bold as a lion.",
            themes = setOf("courage"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("bold", "lion", "righteous", "pursue", "brave", "courage", "fearless", "fierce", "confident", "audacious")
        ),
        ScriptureVerse(
            reference = "1 John 4:4",
            text = "You, dear children, are from God and have overcome them, because the one who is in you is greater than the one who is in the world.",
            themes = setOf("courage", "identity"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("overcome", "greater", "world", "god", "children", "inside", "within", "power", "victor", "winning", "conquer")
        ),
        ScriptureVerse(
            reference = "Hebrews 13:6",
            text = "So we say with confidence, 'The Lord is my helper; I will not be afraid. What can mere mortals do to me?'",
            themes = setOf("courage", "confidence"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("confidence", "helper", "afraid", "mortals", "fear", "bold", "declare", "say", "people", "man")
        ),
        ScriptureVerse(
            reference = "Psalm 34:4",
            text = "I sought the Lord, and he answered me; he delivered me from all my fears.",
            themes = setOf("courage", "peace"),
            toneAffinity = AffirmationTone.GENTLE,
            keywords = setOf("sought", "answered", "delivered", "fears", "fear", "free", "freedom", "rescue", "saved", "liberated")
        ),
        ScriptureVerse(
            reference = "2 Chronicles 20:17",
            text = "You will not have to fight this battle. Take up your positions; stand firm and see the deliverance the Lord will give you.",
            themes = setOf("courage", "trust"),
            toneAffinity = AffirmationTone.STILL,
            keywords = setOf("fight", "battle", "stand", "firm", "deliverance", "deliver", "position", "watch", "see", "wait", "trust")
        ),

        // ╔══════════════════════════════════════════════════════════╗
        // ║  JOY  (~10 verses)                                      ║
        // ╚══════════════════════════════════════════════════════════╝

        ScriptureVerse(
            reference = "Psalm 16:11",
            text = "You make known to me the path of life; you will fill me with joy in your presence, with eternal pleasures at your right hand.",
            themes = setOf("joy", "purpose"),
            toneAffinity = AffirmationTone.GENTLE,
            keywords = setOf("path", "life", "joy", "presence", "eternal", "pleasure", "fill", "full", "delight", "happy", "satisfied")
        ),
        ScriptureVerse(
            reference = "James 1:2–3",
            text = "Consider it pure joy, my brothers and sisters, whenever you face trials of many kinds, because you know that the testing of your faith produces perseverance.",
            themes = setOf("joy", "strength"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("joy", "trials", "testing", "faith", "perseverance", "endure", "suffer", "hard", "difficult", "struggle", "challenge", "trial")
        ),
        ScriptureVerse(
            reference = "Psalm 30:5",
            text = "For his anger lasts only a moment, but his favor lasts a lifetime; weeping may stay for the night, but rejoicing comes in the morning.",
            themes = setOf("joy"),
            toneAffinity = AffirmationTone.GENTLE,
            keywords = setOf("anger", "favor", "weeping", "night", "rejoicing", "morning", "cry", "tears", "sadness", "hope", "dawn", "new", "season", "turn")
        ),
        ScriptureVerse(
            reference = "Romans 15:13",
            text = "May the God of hope fill you with all joy and peace as you trust in him, so that you may overflow with hope by the power of the Holy Spirit.",
            themes = setOf("joy", "peace"),
            toneAffinity = AffirmationTone.GENTLE,
            keywords = setOf("hope", "joy", "peace", "trust", "overflow", "spirit", "fill", "full", "abundant", "power", "holy")
        ),
        ScriptureVerse(
            reference = "Psalm 126:5",
            text = "Those who sow with tears will reap with songs of joy.",
            themes = setOf("joy"),
            toneAffinity = AffirmationTone.GENTLE,
            keywords = setOf("sow", "tears", "reap", "songs", "joy", "cry", "harvest", "season", "waiting", "plant", "grow", "fruit", "reward")
        ),
        ScriptureVerse(
            reference = "Zephaniah 3:17",
            text = "The Lord your God is with you, the Mighty Warrior who saves. He will take great delight in you; in his love he will no longer rebuke you, but will rejoice over you with singing.",
            themes = setOf("joy", "identity"),
            toneAffinity = AffirmationTone.GENTLE,
            keywords = setOf("god", "with", "warrior", "saves", "delight", "love", "rejoice", "singing", "sing", "celebrate", "happy", "loved", "treasured")
        ),
        ScriptureVerse(
            reference = "John 15:11",
            text = "I have told you this so that my joy may be in you and that your joy may be complete.",
            themes = setOf("joy"),
            toneAffinity = AffirmationTone.GENTLE,
            keywords = setOf("joy", "complete", "full", "remain", "abide", "inside", "within", "whole", "satisfied", "content")
        ),
        ScriptureVerse(
            reference = "Psalm 100:1–2",
            text = "Shout for joy to the Lord, all the earth. Worship the Lord with gladness; come before him with joyful songs.",
            themes = setOf("joy"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("shout", "joy", "earth", "worship", "gladness", "joyful", "songs", "sing", "praise", "celebrate", "loud", "exuberant")
        ),
        ScriptureVerse(
            reference = "Galatians 5:22",
            text = "But the fruit of the Spirit is love, joy, peace, forbearance, kindness, goodness, faithfulness.",
            themes = setOf("joy", "peace", "relationships"),
            toneAffinity = AffirmationTone.GENTLE,
            keywords = setOf("fruit", "spirit", "love", "joy", "peace", "patience", "kindness", "goodness", "faithfulness", "gentle", "self-control")
        ),
        ScriptureVerse(
            reference = "Isaiah 61:3",
            text = "To bestow on them a crown of beauty instead of ashes, the oil of joy instead of mourning, and a garment of praise instead of a spirit of despair.",
            themes = setOf("joy", "identity"),
            toneAffinity = AffirmationTone.GENTLE,
            keywords = setOf("beauty", "ashes", "joy", "mourning", "praise", "despair", "crown", "exchange", "replace", "instead", "new", "restore", "grief", "loss")
        ),

        // ╔══════════════════════════════════════════════════════════╗
        // ║  PROVISION  (~10 verses)                                ║
        // ╚══════════════════════════════════════════════════════════╝

        ScriptureVerse(
            reference = "Philippians 4:19",
            text = "And my God will meet all your needs according to the riches of his glory in Christ Jesus.",
            themes = setOf("provision"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("god", "meet", "needs", "need", "riches", "glory", "provide", "supply", "enough", "sufficient", "provision", "lack", "want", "money", "financial")
        ),
        ScriptureVerse(
            reference = "Matthew 6:31–33",
            text = "So do not worry, saying, 'What shall we eat?' or 'What shall we drink?' or 'What shall we wear?' But seek first his kingdom and his righteousness, and all these things will be given to you as well.",
            themes = setOf("provision", "trust"),
            toneAffinity = AffirmationTone.STILL,
            keywords = setOf("worry", "eat", "drink", "wear", "seek", "kingdom", "righteousness", "given", "provide", "food", "clothes", "money", "finances", "bills", "rent")
        ),
        ScriptureVerse(
            reference = "Psalm 37:25",
            text = "I was young and now I am old, yet I have never seen the righteous forsaken or their children begging bread.",
            themes = setOf("provision"),
            toneAffinity = AffirmationTone.STILL,
            keywords = setOf("young", "old", "righteous", "forsaken", "children", "bread", "provide", "faithful", "always", "never", "history", "testimony")
        ),
        ScriptureVerse(
            reference = "2 Corinthians 9:8",
            text = "And God is able to bless you abundantly, so that in all things at all times, having all that you need, you will abound in every good work.",
            themes = setOf("provision"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("god", "able", "bless", "abundantly", "things", "times", "need", "abound", "good", "work", "overflow", "plenty", "generous", "abundance")
        ),
        ScriptureVerse(
            reference = "Deuteronomy 8:18",
            text = "But remember the Lord your God, for it is he who gives you the ability to produce wealth, and so confirms his covenant.",
            themes = setOf("provision"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("remember", "god", "gives", "ability", "produce", "wealth", "covenant", "work", "earn", "prosper", "business", "career", "skill", "talent")
        ),
        ScriptureVerse(
            reference = "Malachi 3:10",
            text = "Bring the whole tithe into the storehouse, that there may be food in my house. Test me in this, says the Lord Almighty, and see if I will not throw open the floodgates of heaven and pour out so much blessing that there will not be room enough to store it.",
            themes = setOf("provision"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("tithe", "storehouse", "test", "floodgates", "heaven", "blessing", "pour", "room", "store", "generous", "give", "giving", "overflow", "abundant")
        ),
        ScriptureVerse(
            reference = "Luke 12:24",
            text = "Consider the ravens: They do not sow or reap, they have no storeroom or barn; yet God feeds them. And how much more valuable you are than birds!",
            themes = setOf("provision", "identity"),
            toneAffinity = AffirmationTone.GENTLE,
            keywords = setOf("ravens", "birds", "sow", "reap", "feeds", "valuable", "worth", "care", "provide", "food", "worry", "anxious", "nature")
        ),
        ScriptureVerse(
            reference = "Psalm 34:10",
            text = "The lions may grow weak and hungry, but those who seek the Lord lack no good thing.",
            themes = setOf("provision"),
            toneAffinity = AffirmationTone.STILL,
            keywords = setOf("lions", "weak", "hungry", "seek", "lord", "lack", "good", "thing", "want", "need", "enough", "satisfied")
        ),
        ScriptureVerse(
            reference = "James 1:17",
            text = "Every good and perfect gift is from above, coming down from the Father of the heavenly lights, who does not change like shifting shadows.",
            themes = setOf("provision"),
            toneAffinity = AffirmationTone.GENTLE,
            keywords = setOf("good", "perfect", "gift", "above", "father", "lights", "change", "shifting", "constant", "faithful", "blessing", "giver", "generous")
        ),
        ScriptureVerse(
            reference = "Proverbs 10:22",
            text = "The blessing of the Lord brings wealth, without painful toil for it.",
            themes = setOf("provision"),
            toneAffinity = AffirmationTone.STILL,
            keywords = setOf("blessing", "lord", "wealth", "toil", "painful", "easy", "prosper", "rich", "income", "earn", "labor", "work", "effortless")
        ),

        // ╔══════════════════════════════════════════════════════════╗
        // ║  CONFIDENCE  (~10 verses)                               ║
        // ╚══════════════════════════════════════════════════════════╝

        ScriptureVerse(
            reference = "Hebrews 4:16",
            text = "Let us then approach God's throne of grace with confidence, so that we may receive mercy and find grace to help us in our time of need.",
            themes = setOf("confidence"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("approach", "throne", "grace", "confidence", "mercy", "help", "need", "bold", "access", "prayer", "pray", "ask")
        ),
        ScriptureVerse(
            reference = "Proverbs 3:26",
            text = "For the Lord will be at your side and will keep your foot from being snared.",
            themes = setOf("confidence"),
            toneAffinity = AffirmationTone.STILL,
            keywords = setOf("lord", "side", "foot", "snared", "trap", "fall", "stumble", "catch", "safe", "secure", "protect", "steady")
        ),
        ScriptureVerse(
            reference = "Isaiah 30:15",
            text = "In repentance and rest is your salvation, in quietness and trust is your strength.",
            themes = setOf("confidence", "peace"),
            toneAffinity = AffirmationTone.STILL,
            keywords = setOf("repentance", "rest", "salvation", "quietness", "trust", "strength", "quiet", "calm", "confident", "assurance", "secure")
        ),
        ScriptureVerse(
            reference = "Psalm 62:5–6",
            text = "Yes, my soul, find rest in God; my hope comes from him. Truly he is my rock and my salvation; he is my fortress, I will not be shaken.",
            themes = setOf("confidence", "peace"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("soul", "rest", "god", "hope", "rock", "salvation", "fortress", "shaken", "unmoved", "stable", "firm", "secure", "anchor", "grounded")
        ),
        ScriptureVerse(
            reference = "1 John 5:14",
            text = "This is the confidence we have in approaching God: that if we ask anything according to his will, he hears us.",
            themes = setOf("confidence"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("confidence", "approaching", "god", "ask", "will", "hears", "hear", "listen", "prayer", "pray", "answer", "bold")
        ),
        ScriptureVerse(
            reference = "2 Corinthians 3:12",
            text = "Therefore, since we have such a hope, we are very bold.",
            themes = setOf("confidence", "courage"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("hope", "bold", "very", "confident", "courage", "daring", "fearless", "speak", "declare", "proclaim")
        ),
        ScriptureVerse(
            reference = "Psalm 3:3",
            text = "But you, Lord, are a shield around me, my glory, the One who lifts my head high.",
            themes = setOf("confidence", "identity"),
            toneAffinity = AffirmationTone.GENTLE,
            keywords = setOf("shield", "glory", "lifts", "head", "high", "protect", "honor", "dignity", "proud", "shame", "embarrass", "low")
        ),
        ScriptureVerse(
            reference = "Isaiah 50:7",
            text = "Because the Sovereign Lord helps me, I will not be disgraced. Therefore have I set my face like flint, and I know I will not be put to shame.",
            themes = setOf("confidence", "courage"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("sovereign", "lord", "helps", "disgraced", "face", "flint", "shame", "determined", "resolute", "firm", "unwavering", "embarrass")
        ),
        ScriptureVerse(
            reference = "Proverbs 14:26",
            text = "Whoever fears the Lord has a secure fortress, and for their children it will be a refuge.",
            themes = setOf("confidence"),
            toneAffinity = AffirmationTone.STILL,
            keywords = setOf("fears", "lord", "secure", "fortress", "children", "refuge", "safe", "protect", "family", "legacy", "foundation")
        ),

        // ╔══════════════════════════════════════════════════════════╗
        // ║  RELATIONSHIPS  (~10 verses)                            ║
        // ╚══════════════════════════════════════════════════════════╝

        ScriptureVerse(
            reference = "Ephesians 4:2–3",
            text = "Be completely humble and gentle; be patient, bearing with one another in love. Make every effort to keep the unity of the Spirit through the bond of peace.",
            themes = setOf("relationships"),
            toneAffinity = AffirmationTone.GENTLE,
            keywords = setOf("humble", "gentle", "patient", "patience", "bearing", "love", "unity", "spirit", "bond", "peace", "together", "marriage", "friend", "community")
        ),
        ScriptureVerse(
            reference = "1 Corinthians 13:4–7",
            text = "Love is patient, love is kind. It does not envy, it does not boast, it is not proud. It does not dishonor others, it is not self-seeking, it is not easily angered, it keeps no record of wrongs. Love does not delight in evil but rejoices with the truth. It always protects, always trusts, always hopes, always perseveres.",
            themes = setOf("relationships"),
            toneAffinity = AffirmationTone.GENTLE,
            keywords = setOf("love", "patient", "kind", "envy", "boast", "proud", "dishonor", "angered", "wrongs", "protects", "trusts", "hopes", "perseveres", "forgive", "marriage", "relationship", "spouse", "partner")
        ),
        ScriptureVerse(
            reference = "Colossians 3:13",
            text = "Bear with each other and forgive one another if any of you has a grievance against someone. Forgive as the Lord forgave you.",
            themes = setOf("relationships"),
            toneAffinity = AffirmationTone.GENTLE,
            keywords = setOf("bear", "forgive", "forgiveness", "grievance", "lord", "forgave", "grudge", "offend", "hurt", "let", "release", "bitter", "resentment")
        ),
        ScriptureVerse(
            reference = "Proverbs 17:17",
            text = "A friend loves at all times, and a brother is born for a time of adversity.",
            themes = setOf("relationships"),
            toneAffinity = AffirmationTone.GENTLE,
            keywords = setOf("friend", "loves", "times", "brother", "sister", "adversity", "friendship", "loyal", "faithful", "companionship", "support")
        ),
        ScriptureVerse(
            reference = "Romans 12:10",
            text = "Be devoted to one another in love. Honor one another above yourselves.",
            themes = setOf("relationships"),
            toneAffinity = AffirmationTone.GENTLE,
            keywords = setOf("devoted", "love", "honor", "another", "yourselves", "serve", "selfless", "humble", "esteem", "respect", "community")
        ),
        ScriptureVerse(
            reference = "1 Peter 4:8",
            text = "Above all, love each other deeply, because love covers over a multitude of sins.",
            themes = setOf("relationships"),
            toneAffinity = AffirmationTone.GENTLE,
            keywords = setOf("love", "deeply", "covers", "sins", "deep", "forgive", "grace", "mercy", "compassion", "heart")
        ),
        ScriptureVerse(
            reference = "Ecclesiastes 4:9–10",
            text = "Two are better than one, because they have a good return for their labor: If either of them falls down, one can help the other up.",
            themes = setOf("relationships"),
            toneAffinity = AffirmationTone.GENTLE,
            keywords = setOf("two", "better", "one", "return", "labor", "falls", "help", "together", "partner", "marriage", "team", "community", "support", "lonely", "alone")
        ),
        ScriptureVerse(
            reference = "Matthew 18:20",
            text = "For where two or three gather in my name, there am I with them.",
            themes = setOf("relationships"),
            toneAffinity = AffirmationTone.STILL,
            keywords = setOf("two", "three", "gather", "name", "with", "together", "community", "church", "fellowship", "presence", "meeting")
        ),
        ScriptureVerse(
            reference = "Philippians 2:3–4",
            text = "Do nothing out of selfish ambition or vain conceit. Rather, in humility value others above yourselves, not looking to your own interests, but each of you to the interests of the others.",
            themes = setOf("relationships", "discipline"),
            toneAffinity = AffirmationTone.GENTLE,
            keywords = setOf("selfish", "ambition", "conceit", "humility", "value", "others", "interests", "serve", "selfless", "humble", "consider", "sacrifice")
        ),
        ScriptureVerse(
            reference = "Romans 12:18",
            text = "If it is possible, as far as it depends on you, live at peace with everyone.",
            themes = setOf("relationships", "peace"),
            toneAffinity = AffirmationTone.GENTLE,
            keywords = setOf("possible", "depends", "peace", "everyone", "conflict", "reconcile", "harmony", "neighbor", "enemy", "disagree", "argument", "fight")
        ),

        // ╔══════════════════════════════════════════════════════════╗
        // ║  DISCIPLINE  (~10 verses)                               ║
        // ╚══════════════════════════════════════════════════════════╝

        ScriptureVerse(
            reference = "Romans 12:2",
            text = "Do not conform to the pattern of this world, but be transformed by the renewing of your mind. Then you will be able to test and approve what God's will is — his good, pleasing and perfect will.",
            themes = setOf("discipline", "identity"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("conform", "pattern", "world", "transformed", "renewing", "mind", "test", "approve", "will", "good", "pleasing", "perfect", "think", "thoughts", "mindset", "change")
        ),
        ScriptureVerse(
            reference = "1 Corinthians 9:27",
            text = "No, I strike a blow to my body and make it my slave so that after I have preached to others, I myself will not be disqualified for the prize.",
            themes = setOf("discipline"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("body", "slave", "discipline", "preached", "disqualified", "prize", "train", "training", "control", "master", "flesh", "temptation", "resist")
        ),
        ScriptureVerse(
            reference = "Hebrews 12:11",
            text = "No discipline seems pleasant at the time, but painful. Later on, however, it produces a harvest of righteousness and peace for those who have been trained by it.",
            themes = setOf("discipline"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("discipline", "pleasant", "painful", "harvest", "righteousness", "peace", "trained", "fruit", "later", "reward", "patience", "endure", "process")
        ),
        ScriptureVerse(
            reference = "Proverbs 25:28",
            text = "Like a city whose walls are broken through is a person who lacks self-control.",
            themes = setOf("discipline"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("city", "walls", "broken", "self-control", "control", "discipline", "restrain", "boundaries", "temptation", "impulse", "habit")
        ),
        ScriptureVerse(
            reference = "Galatians 6:9",
            text = "Let us not become weary in doing good, for at the proper time we will reap a harvest if we do not give up.",
            themes = setOf("discipline", "strength"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("weary", "good", "proper", "time", "reap", "harvest", "give", "up", "quit", "persevere", "persist", "continue", "keep", "going", "tired", "burnout")
        ),
        ScriptureVerse(
            reference = "Philippians 3:13–14",
            text = "Brothers and sisters, I do not consider myself yet to have taken hold of it. But one thing I do: Forgetting what is behind and straining toward what is ahead, I press on toward the goal to win the prize.",
            themes = setOf("discipline", "purpose"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("forgetting", "behind", "straining", "ahead", "press", "goal", "prize", "forward", "past", "future", "focus", "race", "run", "determined", "progress")
        ),
        ScriptureVerse(
            reference = "James 1:12",
            text = "Blessed is the one who perseveres under trial because, having stood the test, that person will receive the crown of life that the Lord has promised to those who love him.",
            themes = setOf("discipline", "strength"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("blessed", "perseveres", "trial", "test", "crown", "life", "promised", "endure", "stand", "persist", "reward", "faithful")
        ),
        ScriptureVerse(
            reference = "Colossians 3:2",
            text = "Set your minds on things above, not on earthly things.",
            themes = setOf("discipline"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("minds", "above", "earthly", "things", "focus", "think", "thoughts", "heaven", "eternal", "perspective", "distraction", "priorities")
        ),
        ScriptureVerse(
            reference = "Proverbs 4:23",
            text = "Above all else, guard your heart, for everything you do flows from it.",
            themes = setOf("discipline"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("guard", "heart", "everything", "flows", "protect", "careful", "watch", "soul", "inner", "thoughts", "feelings", "emotions")
        ),
        ScriptureVerse(
            reference = "Joshua 1:8",
            text = "Keep this Book of the Law always on your lips; meditate on it day and night, so that you may be careful to do everything written in it. Then you will be prosperous and successful.",
            themes = setOf("discipline", "purpose"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("book", "law", "lips", "meditate", "day", "night", "careful", "written", "prosperous", "successful", "word", "scripture", "bible", "read", "study", "devotion")
        ),

        // ╔══════════════════════════════════════════════════════════╗
        // ║  HEALING / RESTORATION  (cross-category)                ║
        // ╚══════════════════════════════════════════════════════════╝

        ScriptureVerse(
            reference = "Psalm 147:3",
            text = "He heals the brokenhearted and binds up their wounds.",
            themes = setOf("peace", "identity"),
            toneAffinity = AffirmationTone.GENTLE,
            keywords = setOf("heals", "heal", "healing", "brokenhearted", "broken", "wounds", "wound", "hurt", "pain", "grief", "loss", "restore", "mend", "repair")
        ),
        ScriptureVerse(
            reference = "Jeremiah 30:17",
            text = "But I will restore you to health and heal your wounds, declares the Lord.",
            themes = setOf("strength"),
            toneAffinity = AffirmationTone.GENTLE,
            keywords = setOf("restore", "health", "heal", "wounds", "declares", "lord", "recovery", "sick", "illness", "body", "wholeness", "complete")
        ),
        ScriptureVerse(
            reference = "Joel 2:25",
            text = "I will repay you for the years the locusts have eaten — the great locust and the young locust, the other locusts and the locust swarm.",
            themes = setOf("provision"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("repay", "years", "locusts", "eaten", "restore", "lost", "time", "wasted", "stolen", "back", "redeem", "recover", "make up")
        ),
        ScriptureVerse(
            reference = "Isaiah 61:1–2",
            text = "The Spirit of the Sovereign Lord is on me, because the Lord has anointed me to proclaim good news to the poor. He has sent me to bind up the brokenhearted, to proclaim freedom for the captives and release from darkness for the prisoners.",
            themes = setOf("identity", "courage"),
            toneAffinity = AffirmationTone.RESOLUTE,
            keywords = setOf("spirit", "sovereign", "anointed", "good news", "poor", "brokenhearted", "freedom", "captives", "release", "darkness", "prisoners", "free", "liberate", "chains", "bound")
        ),
        ScriptureVerse(
            reference = "Lamentations 3:22–23",
            text = "Because of the Lord's great love we are not consumed, for his compassions never fail. They are new every morning; great is your faithfulness.",
            themes = setOf("identity", "joy"),
            toneAffinity = AffirmationTone.GENTLE,
            keywords = setOf("love", "consumed", "compassion", "fail", "new", "morning", "faithfulness", "faithful", "mercy", "fresh", "start", "begin", "grace", "daily", "renew")
        ),
        ScriptureVerse(
            reference = "Revelation 21:4",
            text = "He will wipe every tear from their eyes. There will be no more death or mourning or crying or pain, for the old order of things has passed away.",
            themes = setOf("joy", "peace"),
            toneAffinity = AffirmationTone.GENTLE,
            keywords = setOf("wipe", "tear", "tears", "eyes", "death", "mourning", "crying", "pain", "old", "passed", "away", "hope", "heaven", "eternal", "end", "suffering")
        )
    )
}
