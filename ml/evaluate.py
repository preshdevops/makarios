#!/usr/bin/env python3
import argparse, json, math, re, time
from pathlib import Path
def tok(s): return set(re.findall(r"[a-z]{3,}", s.lower()))
def main():
    ap=argparse.ArgumentParser(); ap.add_argument("--eval", required=True); ap.add_argument("--index", required=True); ap.add_argument("--out", required=True); args=ap.parse_args()
    index=json.loads(Path(args.index).read_text(encoding="utf-8")); rows=[json.loads(x) for x in Path(args.eval).read_text().splitlines() if x.strip()]
    metrics={"count":len(rows),"top1":0,"top3":0,"mrr":0.0,"p95_ms":None,"status":"lexical smoke eval; not a production claim"}
    timings=[]
    for row in rows:
        start=time.perf_counter(); q=tok(row["text"]); ranked=[]
        for v in index:
            score=len(q & tok(v["text"]))/max(1,len(q)); ranked.append((score,v["reference"]))
        ranked.sort(reverse=True); refs=row.get("acceptable",[]); rank=next((i+1 for i,x in enumerate(ranked) if x[1] in refs),None)
        metrics["top1"] += int(rank==1); metrics["top3"] += int(rank is not None and rank<=3); metrics["mrr"] += 0 if not rank else 1/rank; timings.append((time.perf_counter()-start)*1000)
    n=max(1,len(rows)); metrics["top1"]/=n; metrics["top3"]/=n; metrics["mrr"]/=n; metrics["p95_ms"]=sorted(timings)[max(0,math.ceil(.95*len(timings))-1)]; Path(args.out).write_text(json.dumps(metrics,indent=2),encoding="utf-8")
if __name__ == "__main__": main()
