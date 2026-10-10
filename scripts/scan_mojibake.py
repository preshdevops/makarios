import json

with open('android/app/src/main/assets/verses.json', 'r', encoding='utf-8', errors='replace') as f:
    content = f.read()

bad_tokens = ['\u00e2\u20ac\u201c', '\u00e2\u20ac\u201d', '\u00e2\u20ac\u2122', '\u00e2\u20ac\u0153', '\u00e2\u20ac\u009d', '\u00e2\u20ac', '\u00c3', '\ufffd']
names = ['en-dash mojibake', 'em-dash mojibake', 'right single quote mojibake', 'left double quote mojibake', 'right double quote mojibake', 'mojibake prefix', 'A-tilde mojibake', 'replacement char']

found = {}
for token, name in zip(bad_tokens, names):
    c = content.count(token)
    if c > 0:
        found[name] = c

print("Mojibake scan results:", found)

# Let's inspect verses that have ranges or references
data = json.loads(content)
print(f"Loaded {len(data)} verses.")
sample_refs = [v.get('reference', '') for v in data if any(b in v.get('reference', '') or b in v.get('text', '') for b in bad_tokens)]
print(f"Verses containing mojibake: {len(sample_refs)}")
if sample_refs:
    print("Sample infected references:", sample_refs[:10])
