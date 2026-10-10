#!/usr/bin/env python3
"""Training entry point. Requires sentence-transformers and reviewed pairs in build/pairs.jsonl."""
import argparse
from pathlib import Path
def main():
    ap=argparse.ArgumentParser(); ap.add_argument('--pairs',required=True); ap.add_argument('--model',default='intfloat/e5-small-v2'); ap.add_argument('--out',required=True); args=ap.parse_args()
    try:
        from sentence_transformers import SentenceTransformer, InputExample, losses
    except ImportError as exc: raise SystemExit('Install the pinned ML build environment before training: '+str(exc))
    rows=[line.strip().split('\t') for line in Path(args.pairs).read_text().splitlines() if '\t' in line]
    model=SentenceTransformer(args.model); examples=[InputExample(texts=[q,v]) for q,v in rows]; loader=__import__('torch').utils.data.DataLoader(examples,shuffle=True,batch_size=64); model.fit([(loader,losses.MultipleNegativesRankingLoss(model))],epochs=1,show_progress_bar=True); model.save(args.out)
if __name__ == '__main__': main()
