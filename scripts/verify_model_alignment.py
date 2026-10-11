import onnxruntime as ort
import numpy as np
from sentence_transformers import SentenceTransformer
from transformers import AutoTokenizer

text = "God is on my inside"

# 1. SentenceTransformer
st_model = SentenceTransformer("sentence-transformers/all-MiniLM-L12-v2")
st_emb = st_model.encode([text], normalize_embeddings=True)[0]

# 2. ONNX model
tokenizer = AutoTokenizer.from_pretrained("sentence-transformers/all-MiniLM-L12-v2")
inputs = tokenizer(text, return_tensors="np", padding=True, truncation=True)
sess = ort.InferenceSession("android/app/src/main/assets/minilm_l12_quantized.onnx")
onnx_out = sess.run(None, {
    "input_ids": inputs["input_ids"].astype(np.int64),
    "attention_mask": inputs["attention_mask"].astype(np.int64),
    "token_type_ids": inputs.get("token_type_ids", np.zeros_like(inputs["input_ids"])).astype(np.int64)
})[0]

# Mean pool
mask = np.expand_dims(inputs["attention_mask"], -1)
sum_emb = np.sum(onnx_out * mask, axis=1)
sum_mask = np.clip(np.sum(mask, axis=1), a_min=1e-9, a_max=None)
mean_pooled = sum_emb / sum_mask
onnx_emb = mean_pooled / np.linalg.norm(mean_pooled, axis=-1, keepdims=True)
onnx_emb = onnx_emb[0]

# Cosine similarity between ST and ONNX
cosine = np.dot(st_emb, onnx_emb)
print(f"Cosine similarity between SentenceTransformer and ONNX: {cosine:.5f}")
