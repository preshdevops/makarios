import json
import os

assets_dir = "android/app/src/main/assets"
test_res_dir = "android/app/src/test/resources"
os.makedirs(test_res_dir, exist_ok=True)

# 1. 28 User Seed Declarations
with open(os.path.join(assets_dir, "declarations_seed.json"), "r", encoding="utf-8") as f:
    seeds = json.load(f)

eval_cases = []

for s in seeds:
    eval_cases.append({
        "query": s["declaration"],
        "expected_top1": s["verses"][0]["ref"],
        "expected_top3": [v["ref"] for v in s["verses"]],
        "category": "user_seeds",
        "should_match": True
    })

print(f"Added {len(eval_cases)} user seed cases.")

# 2. 120 Bank entries
with open(os.path.join(assets_dir, "declaration_bank.json"), "r", encoding="utf-8") as f:
    bank = json.load(f)

for b in bank[:150]:
    refs = b["references"]
    if not refs:
        continue
    eval_cases.append({
        "query": b["declaration"],
        "expected_top1": refs[0],
        "expected_top3": refs[:3],
        "category": "bank_primary",
        "should_match": True
    })
    # Also add an alias if present
    for alias in b.get("aliases", [])[:2]:
        eval_cases.append({
            "query": alias,
            "expected_top1": refs[0],
            "expected_top3": refs[:3],
            "category": "bank_alias",
            "should_match": True
        })

print(f"Added bank entries, total now: {len(eval_cases)}")

# 3. 250+ Natural Variations written the way people type
natural_variations = [
    # Inside / Indwelling / Identity
    ("God is on my inside", "Colossians 1:27", ["Colossians 1:27", "1 John 4:4", "Philippians 4:13", "Galatians 2:20"]),
    ("Christ is on my inside", "Colossians 1:27", ["Colossians 1:27", "1 John 4:4", "Galatians 2:20"]),
    ("Greater is he that is in me", "1 John 4:4", ["1 John 4:4", "Colossians 1:27", "Romans 8:37"]),
    ("I have the spirit of God inside me", "1 John 4:4", ["1 John 4:4", "Colossians 1:27", "Romans 8:11"]),
    ("Christ in me the hope of glory", "Colossians 1:27", ["Colossians 1:27", "Galatians 2:20"]),
    ("I am a temple of the Holy Spirit", "1 Corinthians 6:19", ["1 Corinthians 6:19", "2 Corinthians 6:16"]),
    ("God lives inside of me", "Colossians 1:27", ["Colossians 1:27", "1 John 4:4", "Galatians 2:20"]),
    ("I am fearfully and wonderfully made", "Psalm 139:14", ["Psalm 139:14", "Ephesians 2:10"]),
    ("God made me His masterpiece", "Ephesians 2:10", ["Ephesians 2:10", "Psalm 139:14"]),
    ("I am the righteousness of God in Christ", "2 Corinthians 5:21", ["2 Corinthians 5:21", "Romans 8:1"]),
    ("I am a new creation in Christ", "2 Corinthians 5:17", ["2 Corinthians 5:17", "Ephesians 4:24"]),
    ("Old things have passed away", "2 Corinthians 5:17", ["2 Corinthians 5:17", "Isaiah 43:18"]),
    ("I am chosen by God", "1 Peter 2:9", ["1 Peter 2:9", "Ephesians 1:4"]),
    ("I am a child of God", "1 John 3:1", ["1 John 3:1", "John 1:12", "Romans 8:16"]),
    ("God loves me with an everlasting love", "Jeremiah 31:3", ["Jeremiah 31:3", "Romans 8:39"]),
    ("Nothing can separate me from the love of God", "Romans 8:39", ["Romans 8:39", "Romans 8:38"]),
    ("There is no condemnation for me", "Romans 8:1", ["Romans 8:1", "1 John 4:18"]),
    ("I walk in freedom from shame", "Romans 8:1", ["Romans 8:1", "Isaiah 54:4"]),
    ("I am forgiven of all my sins", "1 John 1:9", ["1 John 1:9", "Ephesians 1:7", "Colossians 1:14"]),
    ("He who the Son sets free is free indeed", "John 8:36", ["John 8:36", "Galatians 5:1"]),

    # Ability / Strength / Faith
    ("I can literally do anything", "Philippians 4:13", ["Philippians 4:13", "Mark 9:23", "Romans 8:37"]),
    ("I can do all things through Christ", "Philippians 4:13", ["Philippians 4:13", "2 Corinthians 12:9"]),
    ("Nothing is impossible for me with God", "Luke 1:37", ["Luke 1:37", "Mark 9:23", "Matthew 19:26"]),
    ("All things are possible to him that believes", "Mark 9:23", ["Mark 9:23", "Philippians 4:13"]),
    ("The Lord is my strength and shield", "Psalm 28:7", ["Psalm 28:7", "Psalm 18:2"]),
    ("When I am weak then I am strong", "2 Corinthians 12:9", ["2 Corinthians 12:9", "2 Corinthians 12:10"]),
    ("His grace is sufficient for me", "2 Corinthians 12:9", ["2 Corinthians 12:9", "2 Corinthians 9:8"]),
    ("God renews my strength like eagles", "Isaiah 40:31", ["Isaiah 40:31", "Psalm 103:5"]),
    ("I shall run and not be weary", "Isaiah 40:31", ["Isaiah 40:31", "Galatians 6:9"]),
    ("The joy of the Lord is my strength", "Nehemiah 8:10", ["Nehemiah 8:10", "Psalm 28:7"]),
    ("I am strong and courageous", "Joshua 1:9", ["Joshua 1:9", "Deuteronomy 31:6"]),
    ("I will not fear what man can do to me", "Psalm 118:6", ["Psalm 118:6", "Hebrews 13:6"]),

    # Protection / Refuge / Safety
    ("I am never stranded", "Deuteronomy 31:6", ["Deuteronomy 31:6", "Hebrews 13:5", "Psalm 37:25", "Psalm 23:1"]),
    ("I am not alone in this", "Deuteronomy 31:6", ["Deuteronomy 31:6", "Hebrews 13:5", "Isaiah 41:10"]),
    ("The Lord will never leave me nor forsake me", "Deuteronomy 31:6", ["Deuteronomy 31:6", "Hebrews 13:5"]),
    ("God is with me everywhere I go", "Joshua 1:9", ["Joshua 1:9", "Psalm 139:7"]),
    ("I dwell in the secret place of the Most High", "Psalm 91:1", ["Psalm 91:1", "Psalm 91:2"]),
    ("Under His wings I take refuge", "Psalm 91:4", ["Psalm 91:4", "Psalm 61:4"]),
    ("No weapon formed against me will prosper", "Isaiah 54:17", ["Isaiah 54:17", "Romans 8:31"]),
    ("If God is for me who can be against me", "Romans 8:31", ["Romans 8:31", "Psalm 118:6"]),
    ("A thousand may fall at my side but it won't touch me", "Psalm 91:7", ["Psalm 91:7", "Psalm 91:1"]),
    ("God gives His angels charge over me", "Psalm 91:11", ["Psalm 91:11", "Hebrews 1:14"]),
    ("The Lord preserves my going out and coming in", "Psalm 121:8", ["Psalm 121:8", "Psalm 121:1"]),
    ("My help comes from the Lord maker of heaven and earth", "Psalm 121:2", ["Psalm 121:2", "Psalm 121:1"]),
    ("The Lord is my shepherd I shall not want", "Psalm 23:1", ["Psalm 23:1", "Philippians 4:19"]),
    ("Surely goodness and mercy follow me all my days", "Psalm 23:6", ["Psalm 23:6", "Psalm 23:1"]),
    ("He leads me beside still waters", "Psalm 23:2", ["Psalm 23:2", "Psalm 23:3"]),
    ("Even in the valley of shadow of death I fear no evil", "Psalm 23:4", ["Psalm 23:4", "Psalm 91:1"]),

    # Peace / Anxiety / Rest
    ("Peace of God guards my heart and mind", "Philippians 4:7", ["Philippians 4:7", "Philippians 4:6"]),
    ("I cast all my cares on Him because He cares for me", "1 Peter 5:7", ["1 Peter 5:7", "Psalm 55:22"]),
    ("Do not be anxious about anything", "Philippians 4:6", ["Philippians 4:6", "Philippians 4:7"]),
    ("God keeps me in perfect peace", "Isaiah 26:3", ["Isaiah 26:3", "Philippians 4:7"]),
    ("Come to me all who are weary and I will give you rest", "Matthew 11:28", ["Matthew 11:28", "Hebrews 4:9"]),
    ("My mind is fixed on Jesus", "Isaiah 26:3", ["Isaiah 26:3", "Hebrews 12:2"]),
    ("Peace I leave with you, my peace I give to you", "John 14:27", ["John 14:27", "Philippians 4:7"]),
    ("Let not your heart be troubled", "John 14:1", ["John 14:1", "John 14:27"]),
    ("I sleep peacefully because God watches over me", "Psalm 4:8", ["Psalm 4:8", "Psalm 127:2"]),
    ("God has not given me a spirit of fear", "2 Timothy 1:7", ["2 Timothy 1:7", "Romans 8:15"]),
    ("I have power, love, and a sound mind", "2 Timothy 1:7", ["2 Timothy 1:7", "Romans 8:15"]),
    ("Perfect love casts out fear", "1 John 4:18", ["1 John 4:18", "2 Timothy 1:7"]),

    # Provision / Prosperity / Calling
    ("My God supplies all my needs according to His riches", "Philippians 4:19", ["Philippians 4:19", "Psalm 23:1"]),
    ("God is able to make all grace abound to me", "2 Corinthians 9:8", ["2 Corinthians 9:8", "Philippians 4:19"]),
    ("I have all sufficiency in all things", "2 Corinthians 9:8", ["2 Corinthians 9:8", "Philippians 4:19"]),
    ("The blessing of the Lord makes rich and adds no sorrow", "Proverbs 10:22", ["Proverbs 10:22", "Deuteronomy 28:2"]),
    ("I am blessed in the city and blessed in the country", "Deuteronomy 28:3", ["Deuteronomy 28:3", "Deuteronomy 28:13"]),
    ("I am the head and not the tail", "Deuteronomy 28:13", ["Deuteronomy 28:13", "Deuteronomy 28:1"]),
    ("Above only and never beneath", "Deuteronomy 28:13", ["Deuteronomy 28:13", "Deuteronomy 28:2"]),
    ("All things work together for my good", "Romans 8:28", ["Romans 8:28", "Genesis 50:20"]),
    ("God has plans to prosper me and give me hope", "Jeremiah 29:11", ["Jeremiah 29:11", "Romans 8:28"]),
    ("I know the plans I have for you says the Lord", "Jeremiah 29:11", ["Jeremiah 29:11", "Romans 8:28"]),
    ("He who began a good work in me will complete it", "Philippians 1:6", ["Philippians 1:6", "Psalm 138:8"]),
    ("The Lord will perfect that which concerns me", "Psalm 138:8", ["Psalm 138:8", "Philippians 1:6"]),
    ("Commit your way to the Lord and He will direct your paths", "Proverbs 3:5", ["Proverbs 3:5", "Proverbs 3:6", "Proverbs 16:3"]),
    ("Trust in the Lord with all your heart", "Proverbs 3:5", ["Proverbs 3:5", "Proverbs 3:6"]),

    # Healing / Life
    ("By His stripes I am healed", "Isaiah 53:5", ["Isaiah 53:5", "1 Peter 2:24"]),
    ("He sent His word and healed them", "Psalm 107:20", ["Psalm 107:20", "Matthew 8:17"]),
    ("The Lord forgives all my iniquities and heals all my diseases", "Psalm 103:3", ["Psalm 103:3", "Psalm 103:1"]),
    ("He Himself took our infirmities", "Matthew 8:17", ["Matthew 8:17", "Isaiah 53:4"]),
    ("I shall live and not die to declare the works of the Lord", "Psalm 118:17", ["Psalm 118:17", "Psalm 91:16"]),
    ("With long life will He satisfy me", "Psalm 91:16", ["Psalm 91:16", "Proverbs 3:2"]),
]

# Duplicate variations with subtle phrasing nuances (common user typing patterns)
extended_natural = []
for q, best, alts in natural_variations:
    extended_natural.append((q, best, alts))
    # Variation with lowercase and "Today" or "I declare"
    extended_natural.append((f"Today, {q.lower()}", best, alts))
    extended_natural.append((f"I declare that {q.lower()}", best, alts))

for q, best, alts in extended_natural:
    eval_cases.append({
        "query": q,
        "expected_top1": best,
        "expected_top3": alts,
        "category": "natural_phrasing",
        "should_match": True
    })

# Add Out-of-Distribution / Negative Cases
negative_cases = [
    "qzxv toaster 9911",
    "how to make scrambled eggs with cheese",
    "premier league football table and fixtures",
    "buy cheap airline tickets to London",
    "download latest nvidia graphic card drivers",
    "where is the nearest petrol station open right now",
    "python programming tutorial for beginners",
    "weather forecast for tomorrow afternoon",
    "replace car battery 2018 honda civic",
    "solve quadratic equation x squared minus four",
    "best chocolate chip cookie recipe easy",
    "fix wifi connection on windows 11",
    "stock market prices today apple tesla",
    "install nodejs npm windows powershell",
    "what time is sunset tonight in Lagos"
]

for neg in negative_cases:
    eval_cases.append({
        "query": neg,
        "expected_top1": "",
        "expected_top3": [],
        "category": "negative_ood",
        "should_match": False
    })

print(f"Total evaluation cases: {len(eval_cases)}")

out_path = os.path.join(test_res_dir, "eval_declarations.json")
with open(out_path, "w", encoding="utf-8") as f:
    json.dump(eval_cases, f, indent=2, ensure_ascii=False)

print(f"Saved eval harness to {out_path}")
