#!/usr/bin/env python3
import json, re, sys
def tokenize(text, vocab, max_len=128):
    words=re.findall(r"[\w]+|[^\w\s]", text.lower())
    ids=[vocab.get("[CLS]",101)]
    for word in words:
        start=0; pieces=[]
        while start<len(word):
            found=None
            for end in range(len(word),start,-1):
                piece=word[start:end] if start==0 else "##"+word[start:end]
                if piece in vocab: found=vocab[piece],end; break
            if found is None: pieces=[vocab.get("[UNK]",100)]; break
            pieces.append(found[0]); start=found[1]
        if len(ids)+len(pieces)>=max_len: break
        ids.extend(pieces)
    ids.append(vocab.get("[SEP]",102)); return {"input_ids":ids,"attention_mask":[1]*len(ids),"token_type_ids":[0]*len(ids)}
def main():
    vocab={line.rstrip("\n"):i for i,line in enumerate(open(sys.argv[1],encoding="utf-8"))}; print(json.dumps(tokenize(sys.argv[2],vocab)))
if __name__ == "__main__": main()
