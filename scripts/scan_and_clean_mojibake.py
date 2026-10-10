import os

TARGET_DIRS = [
    r'android/app/src/main/java',
    r'android/app/src/main/assets',
    r'android/app/src/main/res'
]

# The corrupted sequences
REPLACEMENTS = [
    ('\u00e2\u20ac\u201c', '-'),  # â€“ -> -
    ('\u00e2\u20ac\u201d', '-'),  # â€” -> -
    ('\u00e2\u20ac\u2122', "'"),  # â€™ -> '
    ('\u00e2\u20ac\u0153', '"'),  # â€œ -> "
    ('\u00e2\u20ac\u009d', '"'),  # â€\x9d -> "
    ('\u2013', '-'),              # en-dash – -> -
    ('\u2014', '-'),              # em-dash — -> -
    ('\ufffd', ''),               # replacement char -> empty
]

total_fixed = 0
fixed_files = []

for base in TARGET_DIRS:
    for root, dirs, files in os.walk(base):
        for file in files:
            if file.endswith(('.kt', '.java', '.json', '.xml', '.txt')):
                path = os.path.join(root, file)
                try:
                    with open(path, 'r', encoding='utf-8') as f:
                        content = f.read()
                except UnicodeDecodeError:
                    with open(path, 'r', encoding='latin1') as f:
                        content = f.read()
                
                original = content
                for bad, good in REPLACEMENTS:
                    content = content.replace(bad, good)
                
                # Also clean up any lingering â€ or Ã
                if '\u00e2\u20ac' in content:
                    content = content.replace('\u00e2\u20ac', '')
                if '\u00c3' in content:
                    content = content.replace('\u00c3', '')
                
                if content != original:
                    with open(path, 'w', encoding='utf-8') as f:
                        f.write(content)
                    print(f"Cleaned mojibake in: {path}")
                    fixed_files.append(path)
                    total_fixed += 1

print(f"Total files sanitized: {total_fixed}")
