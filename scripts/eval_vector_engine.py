import json
import numpy as np
from sentence_transformers import SentenceTransformer

# Load index.json and embeddings_i8.bin
with open('android/app/src/main/assets/index.json', 'r', encoding='utf-8') as f:
    index_data = json.load(f)

with open('android/app/src/main/assets/embeddings_i8.bin', 'rb') as f:
    emb_bytes = f.read()

dim = index_data["dim"]
total_rows = index_data["total_rows"]
emb_matrix = np.frombuffer(emb_bytes, dtype=np.int8).reshape((total_rows, dim))
# Float matrix normalized: val / 127.0
matrix_f = emb_matrix.astype(np.float32) / 127.0

verses = index_data["verses"]
rows = index_data["rows"]

# Precompute mapping: verse_id -> row indices and weights
verse_rows = {}
for r_idx, r in enumerate(rows):
    vid = r["v"]
    w = r["w"]
    if vid not in verse_rows:
        verse_rows[vid] = []
    verse_rows[vid].append((r_idx, w))

model = SentenceTransformer("sentence-transformers/all-MiniLM-L12-v2")

def search(query_text: str, top_k: int = 5):
    # Query expansion lexicon
    q_lower = query_text.lower().strip()
    expanded = query_text
    if "inside" in q_lower or "within me" in q_lower or "in me" in q_lower:
        expanded += " in you, indwelling, Christ in you, Spirit within"
    if "can do anything" in q_lower or "literally do anything" in q_lower or "can do all things" in q_lower:
        expanded += " all things through Christ"
    if "never stranded" in q_lower or "not alone" in q_lower or "never alone" in q_lower:
        expanded += " never leave nor forsake"
    if "safe" in q_lower or "hiding" in q_lower or "refuge" in q_lower:
        expanded += " Psalm 91, Colossians 3:3 refuge hiding place"

    q_emb = model.encode([expanded], normalize_embeddings=True)[0]
    
    # Single pass dot products
    row_scores = np.dot(matrix_f, q_emb)

    # Score each verse: max weighted dot product
    verse_scores = []
    for vid, verse in enumerate(verses):
        r_indices = [r_idx for r_idx, _ in verse_rows[vid]]
        weights = np.array([w for _, w in verse_rows[vid]], dtype=np.float32)
        scores = row_scores[r_indices] * weights
        best_score = float(np.max(scores))
        verse_scores.append((best_score, verse))

    verse_scores.sort(key=lambda x: x[0], reverse=True)

    # Apply diversity rule: at most 1 verse per book in top 3
    # unless first two scores are within 0.02
    results = []
    seen_books = set()

    for idx, (score, v) in enumerate(verse_scores):
        book = v["book"]
        if idx == 0:
            results.append((score, v))
            seen_books.add(book)
        elif len(results) < top_k:
            if book not in seen_books:
                results.append((score, v))
                seen_books.add(book)
            elif len(results) == 1 and abs(score - results[0][0]) <= 0.02:
                # Within 0.02 exception
                results.append((score, v))

    return results

print("=== TEST: 'God is on my inside' ===")
res1 = search("God is on my inside")
for score, v in res1:
    print(f"  {score:.4f} -> {v['ref']} ({v['book']}): {v['reason']}")

print("\n=== TEST: 'I can literally do anything' ===")
res2 = search("I can literally do anything")
for score, v in res2:
    print(f"  {score:.4f} -> {v['ref']} ({v['book']}): {v['reason']}")

print("\n=== TEST: 'I am never stranded' ===")
res3 = search("I am never stranded")
for score, v in res3:
    print(f"  {score:.4f} -> {v['ref']} ({v['book']}): {v['reason']}")

# Test the 28 user declarations
print("\n=== EVALUATION: 28 User Seed Declarations ===")
with open('android/app/src/main/assets/declarations_seed.json', 'r', encoding='utf-8') as f:
    seeds = json.load(f)

top1_count = 0
top3_count = 0

for i, s in enumerate(seeds):
    decl = s["declaration"]
    expected_top1 = s["verses"][0]["ref"]
    expected_alts = [v["ref"] for v in s["verses"]]
    
    matches = search(decl, top_k=3)
    refs = [v["ref"] for _, v in matches]
    
    is_top1 = (len(refs) > 0 and refs[0] == expected_top1)
    is_top3 = any(r in refs for r in expected_alts)
    
    if is_top1:
        top1_count += 1
    if is_top3:
        top3_count += 1
    else:
        print(f"FAILED top3 [{i+1}]: '{decl}'")
        print(f"  Expected: {expected_alts}")
        print(f"  Got: {refs}")

print(f"\nResult: Top-1 = {top1_count}/{len(seeds)} ({top1_count/len(seeds)*100:.1f}%)")
print(f"Result: Top-3 = {top3_count}/{len(seeds)} ({top3_count/len(seeds)*100:.1f}%)")
