import os
import json
import struct
import urllib.request
import numpy as np

# Standard English names for the 66 biblical books (Genesis to Revelation)
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

# Devotional / Affirmation books where inspirational promises predominate
DEVOTIONAL_BOOKS = {
    "Psalms", "Proverbs",
    "Matthew", "Mark", "Luke", "John",
    "Romans", "1 Corinthians", "2 Corinthians", "Galatians", "Ephesians",
    "Philippians", "Colossians", "1 Thessalonians", "2 Thessalonians",
    "1 Timothy", "2 Timothy", "Titus", "Philemon", "Hebrews", "James",
    "1 Peter", "2 Peter", "1 John", "2 John", "3 John", "Jude"
}

def is_devotional_verse(book_name, chapter_num):
    if book_name in DEVOTIONAL_BOOKS:
        return 1
    # Specific Old Testament comfort & promise chapters
    if book_name == "Isaiah" and chapter_num >= 40:
        return 1
    if book_name == "Jeremiah" and chapter_num in (29, 31, 33):
        return 1
    if book_name == "Lamentations" and chapter_num == 3:
        return 1
    if book_name == "Joshua" and chapter_num == 1:
        return 1
    if book_name == "Deuteronomy" and chapter_num in (28, 30, 31):
        return 1
    if book_name in ("Habakkuk", "Zephaniah") and chapter_num == 3:
        return 1
    return 0

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
                devotional = is_devotional_verse(book_name, chap_num)
                clean_text = verse_text.strip()
                verses.append({
                    "r": ref,
                    "t": clean_text,
                    "d": devotional
                })

    print(f"Total parsed verses: {len(verses)}")
    dev_count = sum(1 for v in verses if v["d"] == 1)
    print(f"Devotional tagged verses: {dev_count} ({dev_count / len(verses) * 100:.1f}%)")
    return verses

def download_file(url, target_path):
    print(f"Downloading {os.path.basename(target_path)}...")
    req = urllib.request.Request(url, headers={"User-Agent": "Makarios-Builder"})
    with urllib.request.urlopen(req) as resp, open(target_path, "wb") as out:
        out.write(resp.read())
    size_mb = os.path.getsize(target_path) / (1024 * 1024)
    print(f"Downloaded {target_path} ({size_mb:.2f} MB)")

def main():
    assets_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "android", "app", "src", "main", "assets"))
    os.makedirs(assets_dir, exist_ok=True)
    print(f"Target assets directory: {assets_dir}")

    # 1. Fetch & parse Bible
    verses = fetch_and_parse_bible()

    # 2. Save verses.json
    verses_json_path = os.path.join(assets_dir, "verses.json")
    with open(verses_json_path, "w", encoding="utf-8") as f:
        json.dump(verses, f, ensure_ascii=False, separators=(',', ':'))
    print(f"Saved {verses_json_path} ({os.path.getsize(verses_json_path) / (1024*1024):.2f} MB)")

    # 3. Compute Embeddings with all-MiniLM-L12-v2
    print("Loading sentence-transformers/all-MiniLM-L12-v2...")
    from sentence_transformers import SentenceTransformer
    model = SentenceTransformer("sentence-transformers/all-MiniLM-L12-v2")

    texts = [v["t"] for v in verses]
    print(f"Encoding {len(texts)} verses...")
    embeddings = model.encode(texts, batch_size=256, show_progress_bar=True, normalize_embeddings=True)
    embeddings = np.ascontiguousarray(embeddings, dtype=np.float32)
    print(f"Embeddings shape: {embeddings.shape}, dtype: {embeddings.dtype}")

    # 4. Write embeddings.bin
    embeddings_bin_path = os.path.join(assets_dir, "embeddings.bin")
    num_verses, dim = embeddings.shape
    with open(embeddings_bin_path, "wb") as f:
        # Header: 4 bytes num_verses (int32), 4 bytes dim (int32) - little endian
        f.write(struct.pack("<ii", num_verses, dim))
        f.write(embeddings.tobytes())
    print(f"Saved {embeddings_bin_path} ({os.path.getsize(embeddings_bin_path) / (1024*1024):.2f} MB)")

    # 5. Download ONNX model & vocab.txt
    onnx_url = "https://huggingface.co/sentence-transformers/all-MiniLM-L12-v2/resolve/main/onnx/model_qint8_arm64.onnx"
    vocab_url = "https://huggingface.co/sentence-transformers/all-MiniLM-L12-v2/resolve/main/vocab.txt"

    onnx_path = os.path.join(assets_dir, "minilm_l12_quantized.onnx")
    vocab_path = os.path.join(assets_dir, "vocab.txt")

    if not os.path.exists(onnx_path):
        download_file(onnx_url, onnx_path)
    else:
        print("ONNX model already exists.")

    if not os.path.exists(vocab_path):
        download_file(vocab_url, vocab_path)
    else:
        print("vocab.txt already exists.")

    print("\n--- All assets generated successfully! ---")

if __name__ == "__main__":
    main()
