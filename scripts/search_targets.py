import os

targets = ["Romans 8:28", "Psalm 28:7", "promises", "Still", "Bold", "Gentle"]
results = {t: [] for t in targets}

search_dirs = [
    os.path.join("android", "app", "src", "main"),
    os.path.join("android", "app", "src", "test"),
]

for sdir in search_dirs:
    for root, dirs, files in os.walk(sdir):
        for f in files:
            if not f.endswith((".kt", ".java", ".xml", ".json")):
                continue
            if f in ("verses.json", "verse_of_the_day.json", "declarations_seed.json"):
                continue
            path = os.path.join(root, f)
            try:
                with open(path, "r", encoding="utf-8", errors="ignore") as fp:
                    for idx, line in enumerate(fp, 1):
                        for t in targets:
                            if t.lower() in line.lower():
                                results[t].append((path, idx, line.strip()))
            except Exception as e:
                pass

for t in targets:
    print(f"=== SEARCH: {t} ({len(results[t])} occurrences) ===")
    for path, idx, line in results[t]:
        print(f"  {path}:{idx} -> {line[:120]}")
    print()
