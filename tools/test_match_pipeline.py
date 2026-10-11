import json
import numpy as np
from sentence_transformers import SentenceTransformer

# Load seeds
with open('android/app/src/main/assets/declarations_seed.json', 'r', encoding='utf-8') as f:
    seeds = json.load(f)

print(f"Loaded {len(seeds)} seed declarations.")
for i, s in enumerate(seeds[:5]):
    print(f"{i+1}. '{s['declaration']}' -> Best: {s['verses'][0]['ref']}, Alts: {[v['ref'] for v in s['verses'][1:]]}")
