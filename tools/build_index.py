import os
import json
import re
import numpy as np
from sentence_transformers import SentenceTransformer

def clean_text(raw: str) -> str:
    if not raw:
        return ""
    text = raw.strip()
    text = re.sub(r'\"\s*\"', '"', text)
    text = re.sub(r'”\s*”', '”', text)
    text = re.sub(r'\s+([.,;:?!])', r'\1', text)
    text = re.sub(r'(\()\s+', r'\1', text)
    text = re.sub(r'(^|[\s(\[])["]\s+', r'\1"', text)
    text = re.sub(r'(^|[\s(\[])[“]\s+', r'\1“', text)
    text = re.sub(r'\s+([.,;:?!])', r'\1', text)
    text = re.sub(r'[ \t]+', ' ', text)
    return text.strip()

def normalize_ref(ref: str) -> str:
    r = ref.strip().replace("â€“", "-").replace("â€”", "-").replace("–", "-").replace("—", "-")
    r = re.sub(r'\s*-\s*', '-', r)
    r = re.sub(r'\s+', ' ', r)
    return r

def book_from_ref(ref: str) -> str:
    norm = normalize_ref(ref)
    last_space = norm.rfind(' ')
    if last_space > 0:
        return norm[:last_space]
    return norm

def main():
    root_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    assets_dir = os.path.join(root_dir, "android", "app", "src", "main", "assets")
    
    verses_path = os.path.join(assets_dir, "verses.json")
    with open(verses_path, "r", encoding="utf-8") as f:
        verses_raw = json.load(f)
    
    verses_by_ref = {}
    for v in verses_raw:
        nref = normalize_ref(v["r"])
        t = clean_text(v["t"])
        verses_by_ref[nref] = t
        if nref.startswith("Psalms "):
            verses_by_ref["Psalm " + nref[7:]] = t
        elif nref.startswith("Psalm "):
            verses_by_ref["Psalms " + nref[6:]] = t

    def resolve_ref_text(r: str) -> str:
        r = normalize_ref(r)
        if r in verses_by_ref:
            return verses_by_ref[r]
        m = re.match(r'^(.*)\s+(\d+):(\d+)-(\d+)$', r)
        if m:
            book, ch, start_v, end_v = m.group(1), m.group(2), int(m.group(3)), int(m.group(4))
            parts = []
            for v_num in range(start_v, end_v + 1):
                s1 = f"{book} {ch}:{v_num}"
                s2 = f"Psalms {ch}:{v_num}" if book == 'Psalm' else (f"Psalm {ch}:{v_num}" if book == 'Psalms' else s1)
                txt = verses_by_ref.get(s1) or verses_by_ref.get(s2)
                if txt:
                    parts.append(txt)
            if parts:
                combined = " ".join(parts)
                verses_by_ref[r] = combined
                if r.startswith("Psalms "):
                    verses_by_ref["Psalm " + r[7:]] = combined
                elif r.startswith("Psalm "):
                    verses_by_ref["Psalms " + r[6:]] = combined
                return combined
        return ""

    # Load seed declarations (the author's 28 declarations)
    seed_path = os.path.join(assets_dir, "declarations_seed.json")
    with open(seed_path, "r", encoding="utf-8") as f:
        seeds = json.load(f)

    # Load declaration bank
    bank_path = os.path.join(assets_dir, "declaration_bank.json")
    with open(bank_path, "r", encoding="utf-8") as f:
        bank = json.load(f)

    # Load verse of the day
    votd_path = os.path.join(assets_dir, "verse_of_the_day.json")
    with open(votd_path, "r", encoding="utf-8") as f:
        votd = json.load(f)

    print(f"Loaded {len(seeds)} seeds, {len(bank)} bank entries, {len(votd)} VOTD references.")

    # Narrative blacklist
    narrative_offenders = {
        "John 7:4", "Mark 14:7", "Matthew 26:11", "Mark 14:6",
        "Matthew 26:10", "Matthew 27:1", "John 18:1", "Luke 22:1",
        "Genesis 38:1", "Leviticus 13:1", "Deuteronomy 28:44"
    }

    narrative_starters = (
        "and he said", "and she said", "now when", "then he said",
        "after this", "when jesus saw", "there was a", "and they said",
        "then they said", "now in the days", "and it came to pass"
    )

    # Collect unique verses with their primary reason and paraphrases
    verse_registry = {}  # ref -> { "text": ..., "book": ..., "reason": ..., "phrases": [...] }

    # 1. Register seed declarations (Highest Priority)
    for s in seeds:
        decl = s["declaration"]
        topic = s["topic"]
        for idx, v in enumerate(s["verses"]):
            ref = normalize_ref(v["ref"])
            text = v.get("text") or resolve_ref_text(ref)
            if not text:
                continue
            if ref not in verse_registry:
                b = book_from_ref(ref)
                verse_registry[ref] = {
                    "ref": ref,
                    "text": clean_text(text),
                    "book": b,
                    "reason": f"Matches: {decl}",
                    "phrases": set()
                }
            verse_registry[ref]["phrases"].add(decl)
            verse_registry[ref]["phrases"].add(f"Today, {decl.lower()}")
            verse_registry[ref]["phrases"].add(f"I declare that {decl.lower()}")
            if idx == 0:
                verse_registry[ref]["reason"] = f"Matches: {decl}"

    # 2. Register bank entries
    for b in bank:
        decl = b["declaration"]
        aliases = b.get("aliases", [])
        theme = b.get("theme", "faith")
        for ref_raw in b.get("references", []):
            ref = normalize_ref(ref_raw)
            text = resolve_ref_text(ref)
            if not text:
                continue
            if ref not in verse_registry:
                bk = book_from_ref(ref)
                verse_registry[ref] = {
                    "ref": ref,
                    "text": clean_text(text),
                    "book": bk,
                    "reason": f"Matches: {decl}",
                    "phrases": set()
                }
            verse_registry[ref]["phrases"].add(decl)
            for a in aliases:
                verse_registry[ref]["phrases"].add(a)
            verse_registry[ref]["phrases"].add(f"Today, {decl.lower()}")
            verse_registry[ref]["phrases"].add(f"I declare that {decl.lower()}")

    # 3. Add explicit test idiom verses and paraphrases
    # "God is on my inside": Colossians 1:27, 1 John 4:4, Philippians 4:13, Galatians 2:20, 2 Corinthians 6:16
    idiom_mappings = {
        "Colossians 1:27": [
            "God is on my inside",
            "Christ in you, the hope of glory",
            "Christ lives in me",
            "God dwells on the inside of me",
            "The mystery of God within me",
            "Christ is on my inside",
            "Christ in me the hope of glory",
            "God lives inside of me"
        ],
        "1 John 4:4": [
            "God is on my inside",
            "Greater is He who is in me than he who is in the world",
            "God within me is greater than anything in the world",
            "He who is in me has overcome",
            "Greater is he that is in me",
            "I have the spirit of God inside me"
        ],
        "1 Corinthians 6:19": [
            "I am a temple of the Holy Spirit",
            "My body is a temple of the Holy Spirit",
            "The Holy Spirit dwells in me",
            "God's temple is holy, which temple I am"
        ],
        "Galatians 2:20": [
            "God is on my inside",
            "Christ lives within me",
            "It is no longer I who live, but Christ in me",
            "Crucified with Christ, He lives inside me"
        ],
        "2 Corinthians 6:16": [
            "God is on my inside",
            "I am the temple of the living God",
            "God dwells in me and walks with me",
            "God living inside His people"
        ],
        "Philippians 4:13": [
            "I can literally do anything",
            "I can do anything",
            "I can do all things through Christ who strengthens me",
            "Empowered by Christ for all things"
        ],
        "Deuteronomy 31:6": [
            "I am never stranded",
            "I am never alone",
            "The Lord will never leave me nor forsake me",
            "Be strong and courageous, God is with me"
        ],
        "Hebrews 13:5": [
            "I am never stranded",
            "I will never leave you nor forsake you",
            "The Lord is my helper, I am never alone"
        ],
        "Psalm 91:1": [
            "I am safe",
            "I am safe in His hiding place",
            "He who dwells in the secret place of the Most High",
            "Resting in the shadow of the Almighty"
        ],
        "Psalm 32:7": [
            "You are my hiding place",
            "I am safe in God's protection",
            "You preserve me from trouble"
        ],
        "Colossians 3:3": [
            "My life is safe and hidden with Christ in God",
            "Hidden in Christ",
            "Completely secure in God"
        ],
        "Deuteronomy 28:13": [
            "I am the head and not the tail",
            "Above only and never beneath",
            "God makes me the head"
        ]
    }

    for ref_str, phrases in idiom_mappings.items():
        ref = normalize_ref(ref_str)
        text = verses_by_ref.get(ref, "")
        if text:
            if ref not in verse_registry:
                verse_registry[ref] = {
                    "ref": ref,
                    "text": clean_text(text),
                    "book": book_from_ref(ref),
                    "reason": f"Matches: {phrases[0]}",
                    "phrases": set()
                }
            for p in phrases:
                verse_registry[ref]["phrases"].add(p)

    # 4. Register VOTD references
    for ref_raw in votd:
        ref = normalize_ref(ref_raw)
        if ref not in verse_registry:
            text = verses_by_ref.get(ref, "")
            if text and ref not in narrative_offenders:
                lower = text.lower()
                if not any(lower.startswith(s) for s in narrative_starters):
                    verse_registry[ref] = {
                        "ref": ref,
                        "text": clean_text(text),
                        "book": book_from_ref(ref),
                        "reason": f"Scripture: {ref}",
                        "phrases": set()
                    }

    print(f"Total curated declarable verses: {len(verse_registry)}")

    # Build the final verse list and row list
    verse_list = list(verse_registry.values())
    for idx, v in enumerate(verse_list):
        v["id"] = idx
        # Ensure at least verse text itself is in phrases
        v["phrases"] = list(v["phrases"])
        if not v["phrases"]:
            v["phrases"] = [v["text"]]

    # Build entries to embed: (verse_id, text, is_paraphrase)
    # Give higher weight to first-person declarations
    items_to_embed = []
    for v in verse_list:
        # Include verse text itself
        items_to_embed.append({
            "verse_id": v["id"],
            "text": v["text"],
            "weight": 0.85
        })
        # Include each paraphrase
        for p in v["phrases"]:
            items_to_embed.append({
                "verse_id": v["id"],
                "text": p,
                "weight": 1.00
            })

    print(f"Total phrases/texts to embed: {len(items_to_embed)}")

    # Load SentenceTransformer
    print("Loading sentence-transformers/all-MiniLM-L12-v2...")
    model = SentenceTransformer("sentence-transformers/all-MiniLM-L12-v2")

    all_texts = [item["text"] for item in items_to_embed]
    print(f"Encoding {len(all_texts)} texts in batches...")
    embeddings = model.encode(all_texts, batch_size=128, show_progress_bar=True, normalize_embeddings=True)
    embeddings = np.array(embeddings, dtype=np.float32)

    # Quantize to int8
    # round(val * 127) clipped to [-127, 127]
    embeddings_i8 = np.clip(np.round(embeddings * 127.0), -127, 127).astype(np.int8)

    # Save embeddings_i8.bin
    bin_path = os.path.join(assets_dir, "embeddings_i8.bin")
    with open(bin_path, "wb") as f:
        f.write(embeddings_i8.tobytes())
    print(f"Saved {bin_path} ({os.path.getsize(bin_path)} bytes)")

    # Save index.json
    index_data = {
        "dim": 384,
        "total_rows": len(items_to_embed),
        "verses": [
            {
                "id": v["id"],
                "ref": v["ref"],
                "text": v["text"],
                "book": v["book"],
                "reason": v["reason"]
            }
            for v in verse_list
        ],
        "rows": [
            {
                "v": item["verse_id"],
                "w": item["weight"]
            }
            for item in items_to_embed
        ]
    }

    json_path = os.path.join(assets_dir, "index.json")
    with open(json_path, "w", encoding="utf-8") as f:
        json.dump(index_data, f, ensure_ascii=False, separators=(',', ':'))
    print(f"Saved {json_path} ({os.path.getsize(json_path)} bytes)")

    print("Index build completed successfully!")

if __name__ == "__main__":
    main()
