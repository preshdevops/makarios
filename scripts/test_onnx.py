import onnxruntime as ort
import numpy as np

model_path = "android/app/src/main/assets/minilm_l12_quantized.onnx"
session = ort.InferenceSession(model_path)

print("Inputs:")
for inp in session.get_inputs():
    print(f"  {inp.name}: {inp.type}, shape={inp.shape}")

print("Outputs:")
for out in session.get_outputs():
    print(f"  {out.name}: {out.type}, shape={out.shape}")
