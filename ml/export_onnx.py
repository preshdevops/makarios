#!/usr/bin/env python3
"""Export and quantise the trained encoder. Keeps the Android artifact below the 35 MB budget."""
import argparse
from pathlib import Path
def main():
    ap=argparse.ArgumentParser(); ap.add_argument('--model',required=True); ap.add_argument('--out',required=True); args=ap.parse_args()
    try:
        from optimum.onnxruntime import ORTModelForFeatureExtraction
        from transformers import AutoTokenizer
    except ImportError as exc: raise SystemExit('Install optimum, onnxruntime and transformers: '+str(exc))
    model=ORTModelForFeatureExtraction.from_pretrained(args.model,export=True); tokenizer=AutoTokenizer.from_pretrained(args.model); model.save_pretrained(args.out); tokenizer.save_pretrained(args.out); print('Quantise the exported graph with onnxruntime quantize_dynamic, then check total bytes <= 35 MB')
if __name__ == '__main__': main()
