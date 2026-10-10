# Scripture matching labelling guide

Each item must be reviewed by two Bible-literate reviewers independently.

- Accept one to five verses that directly ground the declaration, not merely share a word.
- Reject curses, judgments, narrative-only verses, genealogies, and verses that reverse the declaration.
- For blessings, Deuteronomy 28:13 is acceptable for “head and not the tail”; Deuteronomy 28:44 is not.
- “I can do anything” is a shorthand regression and must map to Philippians 4:13.
- Mark gibberish, off-topic, or irreconcilably vague text as `negative=true` with an empty acceptable set.
- Resolve disagreements with a third reviewer and record the reason in `notes`.

The checked-in seed set is deliberately marked for review. The pipeline refuses to publish a production index while `label_status` is pending.
