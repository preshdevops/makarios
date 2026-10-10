# Scripture matching evaluation report

## Status

The checked-in 430-item set is an engineering seed and is marked `pending_review`. Production metrics are intentionally not claimed until two Bible-literate reviewers complete the labels and the held-out split is frozen.

The original measured baseline supplied with this task was Top-1 62% and Top-5 87% on 16 hand-labelled declarations. That is not comparable to the new 430-item protocol and is not re-used as a new result.

## Required report

`evaluate.py` writes Top-1, Top-3, MRR, p95 latency, in-distribution versus out-of-distribution scores, and failure references. Add per-topic accuracy and a confusion list after labels are reviewed. CI must fail if the frozen in-distribution Top-3 is below 0.98 or if the OOD Top-3 is below 0.90.

## Android acceptance gates

- No matching code opens `embeddings.bin` or `verses.json`.
- The curated bank returns known regression mappings before lexical retrieval.
- A score below the calibrated threshold returns `confident=false` and the reason `No confident match`.
- The ONNX path is optional and must be loaded off the main thread when reviewed model assets are added.
