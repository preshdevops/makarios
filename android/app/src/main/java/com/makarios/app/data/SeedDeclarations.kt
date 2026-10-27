package com.makarios.app.data

import com.makarios.app.data.bible.ScriptureText

data class SeedVerse(
    val ref: String,
    val text: String
)

data class SeedDeclaration(
    val declaration: String,
    val topic: String,
    val verses: List<SeedVerse>
) {
    val bestVerse: SeedVerse get() = verses.firstOrNull() ?: SeedVerse("Joshua 1:9", "Be strong and courageous.")
}

object SeedDeclarations {
    val entries: List<SeedDeclaration> = listOf(
        SeedDeclaration(
            declaration = "I am experiencing relief from fear and judgement.",
            topic = "Peace",
            verses = listOf(
                SeedVerse("1 John 4:18", "There is no fear in love; but perfect love casts out fear, because fear has punishment. He who fears is not made perfect in love."),
                SeedVerse("Romans 8:1", "There is therefore now no condemnation to those who are in Christ Jesus, who don’ t walk according to the flesh, but according to the Spirit."),
                SeedVerse("Psalm 34:4", "I sought Yahweh, and he answered me, and delivered me from all my fears."),
            )
        ),
        SeedDeclaration(
            declaration = "I am surrounded by greatness, God's love.",
            topic = "Identity",
            verses = listOf(
                SeedVerse("Psalm 32:10", "Many sorrows come to the wicked, but loving kindness shall surround him who trusts in Yahweh."),
                SeedVerse("Romans 8:39", "nor height, nor depth, nor any other created thing will be able to separate us from God ’ s love which is in Christ Jesus our Lord."),
                SeedVerse("Psalm 139:5", "You hem me in behind and before. You laid your hand on me."),
            )
        ),
        SeedDeclaration(
            declaration = "I am perfectly safe in His hands.",
            topic = "Peace",
            verses = listOf(
                SeedVerse("John 10:28", "I give eternal life to them. They will never perish, and no one will snatch them out of my hand."),
                SeedVerse("Isaiah 49:16", "Behold, I have engraved you on the palms of my hands. Your walls are continually before me."),
                SeedVerse("Psalm 91:1", "He who dwells in the secret place of the Most High will rest in the shadow of the Almighty."),
            )
        ),
        SeedDeclaration(
            declaration = "God is giving me the best of care this season.",
            topic = "Rest",
            verses = listOf(
                SeedVerse("Psalm 23:1", "Yahweh is my shepherd; I shall lack nothing."),
                SeedVerse("1 Peter 5:7", "casting all your worries on him, because he cares for you."),
                SeedVerse("Philippians 4:19", "My God will supply every need of yours according to his riches in glory in Christ Jesus."),
            )
        ),
        SeedDeclaration(
            declaration = "I am set for life.",
            topic = "Purpose",
            verses = listOf(
                SeedVerse("2 Peter 1:3", "seeing that his divine power has granted to us all things that pertain to life and godliness, through the knowledge of him who called us by his own glory and virtue,"),
                SeedVerse("Philippians 4:19", "My God will supply every need of yours according to his riches in glory in Christ Jesus."),
                SeedVerse("Psalm 23:1", "Yahweh is my shepherd; I shall lack nothing."),
            )
        ),
        SeedDeclaration(
            declaration = "I am becoming a refuge for people.",
            topic = "Purpose",
            verses = listOf(
                SeedVerse("Isaiah 32:2", "A man shall be as a hiding place from the wind, and a covert from the storm, as streams of water in a dry place, as the shade of a large rock in a weary land."),
                SeedVerse("2 Corinthians 1:4", "who comforts us in all our affliction, that we may be able to comfort those who are in any affliction, through the comfort with which we ourselves are comforted by God."),
                SeedVerse("Psalm 46:1", "God is our refuge and strength, a very present help in trouble."),
            )
        ),
        SeedDeclaration(
            declaration = "Great grace is resting upon me.",
            topic = "Identity",
            verses = listOf(
                SeedVerse("Acts 4:33", "With great power, the apostles gave their testimony of the resurrection of the Lord Jesus. Great grace was on them all."),
                SeedVerse("2 Corinthians 12:9", "He has said to me, \" My grace is sufficient for you, for my power is made perfect in weakness.\" Most gladly therefore I will rather glory in my weaknesses, that the power of Christ may rest on me."),
                SeedVerse("2 Corinthians 9:8", "And God is able to make all grace abound to you, that you, always having all sufficiency in everything, may abound to every good work."),
            )
        ),
        SeedDeclaration(
            declaration = "I have the excellent spirit of God on my inside.",
            topic = "Identity",
            verses = listOf(
                SeedVerse("Daniel 6:3", "Then this Daniel was distinguished above the presidents and the local governors, because an excellent spirit was in him; and the king thought to set him over the whole realm."),
                SeedVerse("Colossians 1:27", "to whom God was pleased to make known what are the riches of the glory of this mystery among the Gentiles, which is Christ in you, the hope of glory."),
                SeedVerse("1 John 4:4", "You are of God, little children, and have overcome them, because greater is he who is in you than he who is in the world."),
            )
        ),
        SeedDeclaration(
            declaration = "God is on my inside, so I can do anything.",
            topic = "Strength",
            verses = listOf(
                SeedVerse("Colossians 1:27", "to whom God was pleased to make known what are the riches of the glory of this mystery among the Gentiles, which is Christ in you, the hope of glory."),
                SeedVerse("1 John 4:4", "You are of God, little children, and have overcome them, because greater is he who is in you than he who is in the world."),
                SeedVerse("Philippians 4:13", "I can do all things through Christ who strengthens me."),
            )
        ),
        SeedDeclaration(
            declaration = "God is just starting with me.",
            topic = "Purpose",
            verses = listOf(
                SeedVerse("Philippians 1:6", "being confident of this very thing, that he who began a good work in you will complete it until the day of Jesus Christ."),
                SeedVerse("Isaiah 43:19", "Behold, I will do a new thing. It springs out now. Don’t you know it? I will even make a way in the wilderness, and rivers in the desert."),
                SeedVerse("Jeremiah 29:11", "For I know the thoughts that I think toward you,\" says Yahweh, \" thoughts of peace, and not of evil, to give you hope and a future."),
            )
        ),
        SeedDeclaration(
            declaration = "I am not permitted to feel like a failure. I have grace.",
            topic = "Strength",
            verses = listOf(
                SeedVerse("Romans 8:37", "No, in all these things we are more than conquerors through him who loved us."),
                SeedVerse("2 Corinthians 12:9", "He has said to me, \" My grace is sufficient for you, for my power is made perfect in weakness.\" Most gladly therefore I will rather glory in my weaknesses, that the power of Christ may rest on me."),
                SeedVerse("Romans 8:1", "There is therefore now no condemnation to those who are in Christ Jesus, who don’ t walk according to the flesh, but according to the Spirit."),
            )
        ),
        SeedDeclaration(
            declaration = "Grace strengthens me.",
            topic = "Strength",
            verses = listOf(
                SeedVerse("2 Timothy 2:1", "You therefore, my child, be strengthened in the grace that is in Christ Jesus."),
                SeedVerse("2 Corinthians 12:9", "He has said to me, \" My grace is sufficient for you, for my power is made perfect in weakness.\" Most gladly therefore I will rather glory in my weaknesses, that the power of Christ may rest on me."),
                SeedVerse("2 Corinthians 9:8", "And God is able to make all grace abound to you, that you, always having all sufficiency in everything, may abound to every good work."),
            )
        ),
        SeedDeclaration(
            declaration = "Every single detail of my life is worked into something good.",
            topic = "Peace",
            verses = listOf(
                SeedVerse("Romans 8:28", "We know that all things work together for good for those who love God, for those who are called according to his purpose."),
                SeedVerse("Genesis 50:20", "As for you, you meant evil against me, but God meant it for good, to save many people alive, as is happening today."),
                SeedVerse("Jeremiah 29:11", "For I know the thoughts that I think toward you,\" says Yahweh, \" thoughts of peace, and not of evil, to give you hope and a future."),
            )
        ),
        SeedDeclaration(
            declaration = "I am never afraid, because I trust God all the time.",
            topic = "Courage",
            verses = listOf(
                SeedVerse("Psalm 56:3", "When I am afraid, I will put my trust in you."),
                SeedVerse("Isaiah 41:10", "Don’t you be afraid, for I am with you. Don’t be dismayed, for I am your God. I will strengthen you. Yes, I will help you. Yes, I will uphold you with the right hand of my righteousness."),
                SeedVerse("Psalm 27:1", "Yahweh is my light and my salvation. Whom shall I fear? Yahweh is the strength of my life. Of whom shall I be afraid?"),
            )
        ),
        SeedDeclaration(
            declaration = "God's strength is my strength.",
            topic = "Strength",
            verses = listOf(
                SeedVerse("Psalm 28:7", "Yahweh is my strength and my shield. My heart has trusted in him, and I am helped. Therefore my heart greatly rejoices. With my song I will thank him."),
                SeedVerse("Nehemiah 8:10", "Then he said to them, \" Go your way. Eat the fat, drink the sweet, and send portions to him for whom nothing is prepared, for today is holy to our Lord. Don’t be grieved, for the joy of Yahweh is your strength.\""),
                SeedVerse("Isaiah 40:31", "but those who wait for Yahweh will renew their strength. They will mount up with wings like eagles. They will run, and not be weary. They will walk, and not faint."),
            )
        ),
        SeedDeclaration(
            declaration = "God is all I need. I depend on Him, I trust Him, I hope in His love.",
            topic = "Rest",
            verses = listOf(
                SeedVerse("Psalm 33:18", "Behold, Yahweh ’s eye is on those who fear him, on those who hope in his loving kindness,"),
                SeedVerse("Psalm 73:26", "My flesh and my heart fails, but God is the strength of my heart and my portion forever."),
                SeedVerse("Psalm 62:5", "My soul, wait in silence for God alone, for my expectation is from him."),
            )
        ),
        SeedDeclaration(
            declaration = "I have grace gifts, and I receive endless energy to use them.",
            topic = "Purpose",
            verses = listOf(
                SeedVerse("1 Peter 4:10", "As each has received a gift, employ it in serving one another, as good managers of the grace of God in its various forms."),
                SeedVerse("Colossians 1:29", "for which I also labor, striving according to his working, which works in me mightily."),
                SeedVerse("Isaiah 40:29", "He gives power to the weak. He increases the strength of him who has no might."),
            )
        ),
        SeedDeclaration(
            declaration = "I can do anything.",
            topic = "Strength",
            verses = listOf(
                SeedVerse("Philippians 4:13", "I can do all things through Christ who strengthens me."),
                SeedVerse("1 Corinthians 15:10", "But by the grace of God I am what I am. His grace which was given to me was not futile, but I worked more than all of them; yet not I, but the grace of God which was with me."),
                SeedVerse("Mark 9:23", "Jesus said to him, \" If you can believe, all things are possible to him who believes.\""),
            )
        ),
        SeedDeclaration(
            declaration = "I live like God, and I watch myself do anything.",
            topic = "Identity",
            verses = listOf(
                SeedVerse("Ephesians 5:1", "Be therefore imitators of God, as beloved children."),
                SeedVerse("1 John 4:17", "In this, love has been made perfect among us, that we may have boldness in the day of judgment, because as he is, even so we are in this world."),
                SeedVerse("Philippians 4:13", "I can do all things through Christ who strengthens me."),
            )
        ),
        SeedDeclaration(
            declaration = "I do His work fearlessly and boldly.",
            topic = "Courage",
            verses = listOf(
                SeedVerse("Acts 4:31", "When they had prayed, the place was shaken where they were gathered together. They were all filled with the Holy Spirit, and they spoke the word of God with boldness."),
                SeedVerse("2 Timothy 1:7", "For God didn’ t give us a spirit of fear, but of power, love, and self-control."),
                SeedVerse("Joshua 1:9", "Haven’t I commanded you? Be strong and courageous. Don’t be afraid. Don’t be dismayed, for Yahweh your God \" (Elohim). is with you wherever you go.\""),
            )
        ),
        SeedDeclaration(
            declaration = "This is my season of uplifting.",
            topic = "Joy",
            verses = listOf(
                SeedVerse("1 Peter 5:6", "Humble yourselves therefore under the mighty hand of God, that he may exalt you in due time,"),
                SeedVerse("Psalm 3:3", "But you, Yahweh, are a shield around me, my glory, and the one who lifts up my head."),
                SeedVerse("Psalm 30:1", "I will extol you, Yahweh, for you have raised me up, and have not made my foes to rejoice over me."),
            )
        ),
        SeedDeclaration(
            declaration = "I am conscious of my blessedness.",
            topic = "Joy",
            verses = listOf(
                SeedVerse("Romans 4:7", "\" Blessed are they whose iniquities are forgiven, whose sins are covered."),
                SeedVerse("Ephesians 1:3", "Blessed be the God and Father of our Lord Jesus Christ, who has blessed us with every spiritual blessing in the heavenly places in Christ,"),
                SeedVerse("Psalm 1:1", "Blessed is the man who doesn’t walk in the counsel of the wicked, nor stand on the path of sinners, nor sit in the seat of scoffers;"),
            )
        ),
        SeedDeclaration(
            declaration = "I am saved by grace through faith.",
            topic = "Identity",
            verses = listOf(
                SeedVerse("Ephesians 2:8", "for by grace you have been saved through faith, and that not of yourselves; it is the gift of God,"),
                SeedVerse("Ephesians 2:9", "not of works, that no one would boast."),
                SeedVerse("Titus 2:11", "For the grace of God has appeared, bringing salvation to all men,"),
            )
        ),
        SeedDeclaration(
            declaration = "I am original, and my journey is my own.",
            topic = "Identity",
            verses = listOf(
                SeedVerse("Psalm 139:14", "I will give thanks to you, for I am fearfully and wonderfully made. Your works are wonderful. My soul knows that very well."),
                SeedVerse("Ephesians 2:10", "For we are his workmanship, created in Christ Jesus for good works, which God prepared before that we would walk in them."),
                SeedVerse("Galatians 6:4", "But let each man examine his own work, and then he will have reason to boast in himself, and not in someone else."),
            )
        ),
        SeedDeclaration(
            declaration = "I trust in God, so I am fruitful.",
            topic = "Purpose",
            verses = listOf(
                SeedVerse("Jeremiah 17:7", "\" Blessed is the man who trusts in Yahweh, and whose confidence is in Yahweh."),
                SeedVerse("Psalm 1:3", "He will be like a tree planted by the streams of water, that produces its fruit in its season, whose leaf also does not wither. Whatever he does shall prosper."),
                SeedVerse("John 15:5", "I am the vine. You are the branches. He who remains in me and I in him bears much fruit, for apart from me you can do nothing."),
            )
        ),
        SeedDeclaration(
            declaration = "I wait on God, and He decides my steps.",
            topic = "Rest",
            verses = listOf(
                SeedVerse("Proverbs 16:9", "A man ’s heart plans his course, but Yahweh directs his steps."),
                SeedVerse("Psalm 37:23", "A man ’s steps are established by Yahweh. He delights in his way."),
                SeedVerse("Proverbs 3:5", "Trust in Yahweh with all your heart, and don’t lean on your own understanding."),
            )
        ),
        SeedDeclaration(
            declaration = "I find real rest in God.",
            topic = "Rest",
            verses = listOf(
                SeedVerse("Hebrews 4:9", "There remains therefore a Sabbath rest for the people of God."),
                SeedVerse("Matthew 11:28", "\" Come to me, all you who labor and are heavily burdened, and I will give you rest."),
                SeedVerse("Psalm 62:1", "My soul rests in God alone. My salvation is from him."),
            )
        ),
        SeedDeclaration(
            declaration = "I am hiding in God, and I am safe.",
            topic = "Peace",
            verses = listOf(
                SeedVerse("Colossians 3:3", "For you died, and your life is hidden with Christ in God."),
                SeedVerse("Psalm 91:1", "He who dwells in the secret place of the Most High will rest in the shadow of the Almighty."),
                SeedVerse("Psalm 32:7", "You are my hiding place. You will preserve me from trouble. You will surround me with songs of deliverance. Selah."),
            )
        ),
    )

    fun asAffirmations(): List<Affirmation> = entries.mapIndexed { idx, item ->
        val best = item.bestVerse
        Affirmation(
            id = "seed-${idx + 1}",
            declaration = item.declaration,
            scriptureText = best.text,
            reference = ScriptureText.normalizeReference(best.ref),
            context = "Reviewed declaration standing on God's word.",
            category = item.topic,
            tone = AffirmationTone.STILL,
            imageUrl = "",
            isFavorite = false
        )
    }

    fun asBankEntries(): List<BankEntry> = entries.map { item ->
        BankEntry(
            declaration = item.declaration,
            aliases = emptyList(),
            references = item.verses.map { it.ref },
            theme = item.topic.lowercase()
        )
    }
}
