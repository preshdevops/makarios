# Scripture retrieval pipeline

This directory contains the offline build and evaluation path for the declarable scripture index.

1. Review `eval/eval_set.jsonl` with `eval/LABELLING_GUIDE.md` and change `label_status` to reviewed.
2. Run `python build_index.py --verses ../android/app/src/main/assets/verses.json --out build/declarable_index.json` after review.
3. Generate paraphrases with the stored prompt, human spot-check them, then train and export the selected model.
4. Run `evaluate.py` on the held-out split. The report must include Top-1, Top-3, MRR, p95 latency, OOD accuracy, per-topic accuracy, and failures.

The Android fallback is intentionally deterministic and ships only the curated `ScriptureDatabase` bank until reviewed generated assets are added. It does not load `embeddings.bin` or `verses.json` for matching.
