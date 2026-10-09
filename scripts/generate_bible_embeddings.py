import os
import json
import struct
import urllib.request
import re
import numpy as np

ENGLISH_BOOK_NAMES = [
    "Genesis", "Exodus", "Leviticus", "Numbers", "Deuteronomy",
    "Joshua", "Judges", "Ruth", "1 Samuel", "2 Samuel",
    "1 Kings", "2 Kings", "1 Chronicles", "2 Chronicles", "Ezra",
    "Nehemiah", "Esther", "Job", "Psalms", "Proverbs",
    "Ecclesiastes", "Song of Solomon", "Isaiah", "Jeremiah", "Lamentations",
    "Ezekiel", "Daniel", "Hosea", "Joel", "Amos",
    "Obadiah", "Jonah", "Micah", "Nahum", "Habakkuk",
    "Zephaniah", "Haggai", "Zechariah", "Malachi",
    "Matthew", "Mark", "Luke", "John", "Acts",
    "Romans", "1 Corinthians", "2 Corinthians", "Galatians", "Ephesians",
    "Philippians", "Colossians", "1 Thessalonians", "2 Thessalonians", "1 Timothy",
    "2 Timothy", "Titus", "Philemon", "Hebrews", "James",
    "1 Peter", "2 Peter", "1 John", "2 John", "3 John",
    "Jude", "Revelation"
]

# A placeholder curated promise list (in a real scenario this would be comprehensive)
CURATED_PROMISES = {
    "Jeremiah 29:11", "Romans 8:28", "Philippians 4:13",
    "Isaiah 41:10", "Proverbs 3:5", "Proverbs 3:6",
    "Matthew 11:28", "Joshua 1:9", "Philippians 4:6",
    "Philippians 4:7", "John 14:27", "2 Timothy 1:7",
    "Zephaniah 3:17", "Isaiah 43:2"
}

def clean_verse_text(text):
    # Remove spaces before punctuation
    text = re.sub(r'\s+([,.:;?!])', r'\1', text)
    # Remove doubled quotes
    text = text.replace('""', '"').replace("''", "'")
    # Remove inline translator notes such as (Elohim), (Yahweh), etc.
    text = re.sub(r'\([^)]*\)', '', text)
    # Clean up extra spaces
    text = re.sub(r'\s+', ' ', text).strip()
    return text

def is_promise_verse(ref):
    return 1 if ref in CURATED_PROMISES else 0

def fetch_and_parse_bible():
    print("Fetching World English Bible (WEB)...")
    url = "https://raw.githubusercontent.com/thiagobodruk/bible/master/json/en_web.json"
    req = urllib.request.Request(url, headers={"User-Agent": "Makarios-Builder"})
    with urllib.request.urlopen(req) as resp:
        raw_data = json.loads(resp.read().decode("utf-8"))

    verses = []
    for book_idx, book in enumerate(raw_data):
        book_name = ENGLISH_BOOK_NAMES[book_idx] if book_idx < len(ENGLISH_BOOK_NAMES) else book.get("name", "Unknown")
        for chapter_idx, chapter in enumerate(book["chapters"]):
            chap_num = chapter_idx + 1
            for verse_idx, verse_text in enumerate(chapter):
                v_num = verse_idx + 1
                ref = f"{book_name} {chap_num}:{v_num}"
                
                clean_text = clean_verse_text(verse_text)
                is_promise = is_promise_verse(ref)
                
                verses.append({
                    "r": ref,
                    "t": clean_text,
                    "d": is_promise
                })

    print(f"Total parsed verses: {len(verses)}")
    dev_count = sum(1 for v in verses if v["d"] == 1)
    print(f"Promise tagged verses: {dev_count} ({dev_count / len(verses) * 100:.3f}%)")
    return verses

def download_file(url, target_path):
    print(f"Downloading {os.path.basename(target_path)}...")
    req = urllib.request.Request(url, headers={"User-Agent": "Makarios-Builder"})
    with urllib.request.urlopen(req) as resp, open(target_path, "wb") as out:
        out.write(resp.read())

def main():
    assets_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "android", "app", "src", "main", "assets"))
    os.makedirs(assets_dir, exist_ok=True)

    verses = fetch_and_parse_bible()

    verses_json_path = os.path.join(assets_dir, "verses.json")
    with open(verses_json_path, "w", encoding="utf-8") as f:
        json.dump(verses, f, ensure_ascii=False, separators=(',', ':'))

    print("Loading sentence-transformers/all-MiniLM-L12-v2...")
    from sentence_transformers import SentenceTransformer
    model = SentenceTransformer("sentence-transformers/all-MiniLM-L12-v2")

    # Embed the CLEANED text
    texts = [v["t"] for v in verses]
    print(f"Encoding {len(texts)} verses...")
    embeddings = model.encode(texts, batch_size=256, show_progress_bar=True, normalize_embeddings=True)
    embeddings = np.ascontiguousarray(embeddings, dtype=np.float32)

    embeddings_bin_path = os.path.join(assets_dir, "embeddings.bin")
    num_verses, dim = embeddings.shape
    with open(embeddings_bin_path, "wb") as f:
        f.write(struct.pack("<ii", num_verses, dim))
        f.write(embeddings.tobytes())

    onnx_url = "https://huggingface.co/sentence-transformers/all-MiniLM-L12-v2/resolve/main/onnx/model_qint8_arm64.onnx"
    vocab_url = "https://huggingface.co/sentence-transformers/all-MiniLM-L12-v2/resolve/main/vocab.txt"

    onnx_path = os.path.join(assets_dir, "minilm_l12_quantized.onnx")
    vocab_path = os.path.join(assets_dir, "vocab.txt")

    if not os.path.exists(onnx_path):
        download_file(onnx_url, onnx_path)
    if not os.path.exists(vocab_path):
        download_file(vocab_url, vocab_path)
    
    print("Done!")

if __name__ == "__main__":
    main()
