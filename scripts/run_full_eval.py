import json
import time
import re
import numpy as np
from sentence_transformers import SentenceTransformer

with open('android/app/src/main/assets/index.json', 'r', encoding='utf-8') as f:
    index_data = json.load(f)

with open('android/app/src/main/assets/embeddings_i8.bin', 'rb') as f:
    emb_bytes = f.read()

dim = index_data["dim"]
total_rows = index_data["total_rows"]
emb_matrix = np.frombuffer(emb_bytes, dtype=np.int8).reshape((total_rows, dim))
matrix_f = emb_matrix.astype(np.float32) / 127.0

verses = index_data["verses"]
verse_by_ref = {v["ref"]: v for v in verses}
rows = index_data["rows"]

verse_rows = {}
for r_idx, r in enumerate(rows):
    vid = r["v"]
    w = r["w"]
    if vid not in verse_rows:
        verse_rows[vid] = []
    verse_rows[vid].append((r_idx, w))

# Load seeds & bank for Rule 1 (Reviewed Bank Exact/Near Match)
with open('android/app/src/main/assets/declarations_seed.json', 'r', encoding='utf-8') as f:
    seeds = json.load(f)

with open('android/app/src/main/assets/declaration_bank.json', 'r', encoding='utf-8') as f:
    bank = json.load(f)

model = SentenceTransformer("sentence-transformers/all-MiniLM-L12-v2")

# Embed reviewed bank phrases
reviewed_bank_entries = []
for s in seeds:
    decl = s["declaration"]
    refs = [v["ref"] for v in s["verses"]]
    reviewed_bank_entries.append({
        "phrase": decl,
        "refs": refs,
        "reason": f"Matches: {decl}"
    })

for b in bank:
    decl = b["declaration"]
    refs = b.get("references", [])
    if refs:
        reviewed_bank_entries.append({
            "phrase": decl,
            "refs": refs,
            "reason": f"Matches: {decl}"
        })
        for a in b.get("aliases", [])[:2]:
            reviewed_bank_entries.append({
                "phrase": a,
                "refs": refs,
                "reason": f"Matches: {decl}"
            })

bank_phrases = [e["phrase"] for e in reviewed_bank_entries]
bank_embeddings = model.encode(bank_phrases, batch_size=128, normalize_embeddings=True)

def expand_query(query_text: str) -> str:
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
    return expanded

# Load evaluation cases
with open('android/app/src/test/resources/eval_declarations.json', 'r', encoding='utf-8') as f:
    eval_cases = json.load(f)

print(f"Loaded {len(eval_cases)} evaluation cases.")

raw_queries = [c["query"] for c in eval_cases]
expanded_queries = [expand_query(q) for q in raw_queries]

print("Batch encoding raw queries...")
raw_embs = model.encode(raw_queries, batch_size=128, normalize_embeddings=True)

print("Batch encoding expanded queries...")
exp_embs = model.encode(expanded_queries, batch_size=128, normalize_embeddings=True)

print("Running fast vector evaluation...")
t0 = time.time()

user_28_top1 = 0
user_28_top3 = 0
user_28_total = 0

in_dist_top1 = 0
in_dist_top3 = 0
in_dist_total = 0

ood_correct = 0
ood_total = 0

threshold = 0.42
top_k = 3

for i, c in enumerate(eval_cases):
    q = c["query"]
    cat = c["category"]
    should_match = c["should_match"]
    exp_top1 = c["expected_top1"]
    exp_top3 = c["expected_top3"]

    q_emb = raw_embs[i]
    exp_emb = exp_embs[i]

    # Rule 1: Reviewed bank match
    bank_dots = np.dot(bank_embeddings, q_emb)
    best_bank_idx = int(np.argmax(bank_dots))
    best_bank_score = float(bank_dots[best_bank_idx])

    results = []
    confident = False
    reason = ""

    if best_bank_score >= 0.80:
        entry = reviewed_bank_entries[best_bank_idx]
        curated_refs = entry["refs"]
        curated_matches = []
        seen_books = set()
        for r in curated_refs:
            v = verse_by_ref.get(r)
            if v and v["book"] not in seen_books:
                curated_matches.append((best_bank_score, v, entry["reason"]))
                seen_books.add(v["book"])
                if len(curated_matches) >= top_k:
                    break
        if curated_matches:
            results = curated_matches
            confident = True
            reason = entry["reason"]

    if not results:
        # Rule 2: Vector embedding search
        row_scores = np.dot(matrix_f, exp_emb)
        verse_scores = []
        for vid, verse in enumerate(verses):
            r_indices = [r_idx for r_idx, _ in verse_rows[vid]]
            weights = np.array([w for _, w in verse_rows[vid]], dtype=np.float32)
            scores = row_scores[r_indices] * weights
            best_score = float(np.max(scores))
            verse_scores.append((best_score, verse))

        verse_scores.sort(key=lambda x: x[0], reverse=True)
        top_score = verse_scores[0][0] if verse_scores else 0.0
        confident = top_score >= threshold

        if confident:
            seen_books = set()
            reason = verse_scores[0][1]["reason"]
            for idx, (score, v) in enumerate(verse_scores):
                book = v["book"]
                if idx == 0:
                    results.append((score, v, reason))
                    seen_books.add(book)
                elif len(results) < top_k:
                    if book not in seen_books:
                        results.append((score, v, v["reason"]))
                        seen_books.add(book)
                    elif len(results) == 1 and abs(score - results[0][0]) <= 0.02:
                        results.append((score, v, v["reason"]))

    matched_refs = [v["ref"] for _, v, _ in results]

    if not should_match:
        ood_total += 1
        if not confident or len(results) == 0:
            ood_correct += 1
        continue

    in_dist_total += 1

    def ref_matches_single(got: str, exp: str) -> bool:
        g = got.strip()
        e = exp.strip()
        if g.startswith("Psalms "): g = "Psalm " + g[7:]
        if e.startswith("Psalms "): e = "Psalm " + e[7:]
        if g == e: return True
        # Check range containment: got is range e is single, or vice versa
        m_g = re.match(r'^(.*)\s+(\d+):(\d+)-(\d+)$', g)
        m_e = re.match(r'^(.*)\s+(\d+):(\d+)$', e)
        if m_g and m_e and m_g.group(1) == m_e.group(1) and m_g.group(2) == m_e.group(2):
            if int(m_g.group(3)) <= int(m_e.group(3)) <= int(m_g.group(4)):
                return True
        m_e2 = re.match(r'^(.*)\s+(\d+):(\d+)-(\d+)$', e)
        m_g2 = re.match(r'^(.*)\s+(\d+):(\d+)$', g)
        if m_e2 and m_g2 and m_e2.group(1) == m_g2.group(1) and m_e2.group(2) == m_g2.group(2):
            if int(m_e2.group(3)) <= int(m_g2.group(3)) <= int(m_e2.group(4)):
                return True
        return False

    has_top1 = (len(matched_refs) > 0 and ref_matches_single(matched_refs[0], exp_top1))
    has_top3 = any(any(ref_matches_single(got_r, exp_r) for exp_r in exp_top3) for got_r in matched_refs)

    if has_top1:
        in_dist_top1 += 1
    if has_top3:
        in_dist_top3 += 1

    if cat == "user_seeds":
        user_28_total += 1
        if has_top1:
            user_28_top1 += 1
        if has_top3:
            user_28_top3 += 1

elapsed = time.time() - t0
avg_time_ms = (elapsed / len(eval_cases)) * 1000

print("\n================ EVALUATION RESULTS ================")
print(f"Total Evaluated: {len(eval_cases)} queries in {elapsed:.2f}s ({avg_time_ms:.2f} ms/query)")
print(f"1. User 28 Seeds:")
print(f"   Top-1: {user_28_top1}/{user_28_total} ({user_28_top1/user_28_total*100:.1f}%) [Requirement: >= 24/28 (85.7%)]")
print(f"   Top-3: {user_28_top3}/{user_28_total} ({user_28_top3/user_28_total*100:.1f}%) [Requirement: 28/28 (100%)]")

print(f"\n2. In-Distribution Set ({in_dist_total} queries):")
print(f"   Top-1: {in_dist_top1}/{in_dist_total} ({in_dist_top1/in_dist_total*100:.1f}%)")
print(f"   Top-3: {in_dist_top3}/{in_dist_total} ({in_dist_top3/in_dist_total*100:.1f}%) [Requirement: >= 95.0%]")

print(f"\n3. Out-of-Distribution / No-Match Precision ({ood_total} queries):")
print(f"   No-Match Precision: {ood_correct}/{ood_total} ({ood_correct/ood_total*100:.1f}%)")
print("====================================================\n")
