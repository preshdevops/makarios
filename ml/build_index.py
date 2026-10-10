#!/usr/bin/env python3
"""Build a reviewed declarable index from the full reader corpus. Matching never ships this corpus."""
import argparse, json, re
from pathlib import Path
BLOCKED = {"curse","cursed","judgment","judgement","wrath","destroy","wicked","evil","vengeance","genealogy"}
def clean(s): return re.sub(r"\s+", " ", re.sub(r"\s+([.,;:?!)])", r"\1", s)).strip()
def main():
    ap=argparse.ArgumentParser(); ap.add_argument("--verses", required=True); ap.add_argument("--out", required=True); ap.add_argument("--allow-pending", action="store_true"); args=ap.parse_args()
    rows=json.loads(Path(args.verses).read_text(encoding="utf-8")); out=[]
    for row in rows:
        text=clean(row.get("t", "")); hay=(row.get("r", "")+" "+text).lower()
        if any(word in hay.split() for word in BLOCKED): continue
        if row.get("d", 0) != 1: continue
        out.append({"reference":row["r"],"text":text,"quality":0.8,"topics":[],"paraphrases":[],"label_status":"pending_review"})
    if not args.allow_pending and any(x["label_status"] != "reviewed" for x in out): raise SystemExit("Refusing to publish an unreviewed index")
    Path(args.out).write_text(json.dumps(out, ensure_ascii=False, indent=2), encoding="utf-8")
if __name__ == "__main__": main()
