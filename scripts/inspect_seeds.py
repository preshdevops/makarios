import json

data = json.load(open('android/app/src/main/assets/declarations_seed.json', encoding='utf-8'))
print(f"Total entries: {len(data)}")
for i, d in enumerate(data):
    refs = [v['ref'] for v in d['verses']]
    print(f"{i+1:2d}. [{d['topic']:<8}] {d['declaration']} -> {refs}")
