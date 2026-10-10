import os
import math
from PIL import Image, ImageDraw, ImageFont

# Ensure directory exists
os.makedirs("store_assets", exist_ok=True)

# 1. Play Store Icon: 512 x 512
img512 = Image.new("RGBA", (512, 512))
draw512 = ImageDraw.Draw(img512)

# Dawn gradient: #FBE3C4 (251, 227, 196) to #EBB195 (235, 177, 149)
c_top = (251, 227, 196)
c_bot = (235, 177, 149)
for y in range(512):
    t = y / 511.0
    r = int(c_top[0] * (1 - t) + c_bot[0] * t)
    g = int(c_top[1] * (1 - t) + c_bot[1] * t)
    b = int(c_top[2] * (1 - t) + c_bot[2] * t)
    draw512.line([(0, y), (512, y)], fill=(r, g, b, 255))

# Scale mark: viewBox is 100 x 100.
# Center mark inside 512x512 with safe padding: mark size 320x320 centered.
scale = 3.2
ox = (512 - 100 * scale) / 2.0  # 96
oy = (512 - 100 * scale) / 2.0  # 96

# Colors: arch & ground #2A1B14 (42, 27, 20), sun #B5532B (181, 83, 43)
arch_color = (42, 27, 20, 255)
sun_color = (181, 83, 43, 255)
stroke_w = int(4.5 * scale)  # ~14px

# Sun: M32 82 a18 18 0 0 1 36 0 Z
# Bounding box of half sun circle: [32, 64, 68, 100]
sun_bbox = [ox + 32 * scale, oy + 64 * scale, ox + 68 * scale, oy + 100 * scale]
draw512.pieslice(sun_bbox, start=180, end=360, fill=sun_color)

# Arch: M27 82 V46 a23 23 0 0 1 46 0 v36
# Left vertical line: (27, 82) to (27, 46)
draw512.line([(ox + 27 * scale, oy + 82 * scale), (ox + 27 * scale, oy + 46 * scale)], fill=arch_color, width=stroke_w)
# Arc: [27, 23, 73, 69]
arch_bbox = [ox + 27 * scale, oy + 23 * scale, ox + 73 * scale, oy + 69 * scale]
draw512.arc(arch_bbox, start=180, end=360, fill=arch_color, width=stroke_w)
# Right vertical line: (73, 46) to (73, 82)
draw512.line([(ox + 73 * scale, oy + 46 * scale), (ox + 73 * scale, oy + 82 * scale)], fill=arch_color, width=stroke_w)

# Ground line: M18 82 h64
draw512.line([(ox + 18 * scale, oy + 82 * scale), (ox + 82 * scale, oy + 82 * scale)], fill=arch_color, width=stroke_w)

img512.save("store_assets/play_store_512.png")
print("Saved store_assets/play_store_512.png")

# Also generate legacy mipmap PNGs
for density, sz in [("mdpi", 48), ("hdpi", 72), ("xhdpi", 96), ("xxhdpi", 144), ("xxxhdpi", 192)]:
    res = img512.resize((sz, sz), Image.Resampling.LANCZOS)
    dir_path = f"android/app/src/main/res/mipmap-{density}"
    os.makedirs(dir_path, exist_ok=True)
    res.save(os.path.join(dir_path, "ic_launcher.png"))
    res.save(os.path.join(dir_path, "ic_launcher_round.png"))
    print(f"Updated {density} icons ({sz}x{sz})")

# 2. Feature Graphic: 1024 x 500
img1024 = Image.new("RGBA", (1024, 500))
draw1024 = ImageDraw.Draw(img1024)

# Pre-dawn gradient: #0F1716 (0%), #1F2B2A (30%), #43292B (62%), #C9703F (100%)
stops = [
    (0.0, (15, 23, 22)),
    (0.30, (31, 43, 42)),
    (0.62, (67, 41, 43)),
    (1.0, (201, 112, 63))
]
for y in range(500):
    t = y / 499.0
    for i in range(len(stops) - 1):
        if stops[i][0] <= t <= stops[i+1][0]:
            sub_t = (t - stops[i][0]) / (stops[i+1][0] - stops[i][0])
            c1, c2 = stops[i][1], stops[i+1][1]
            r = int(c1[0] * (1 - sub_t) + c2[0] * sub_t)
            g = int(c1[1] * (1 - sub_t) + c2[1] * sub_t)
            b = int(c1[2] * (1 - sub_t) + c2[2] * sub_t)
            draw1024.line([(0, y), (1024, y)], fill=(r, g, b, 255))
            break

# Sun & halo rising behind ridges on right side
sun_cx = 512
sun_cy = 380
# Halo
for radius in range(200, 40, -10):
    alpha = int(45 * (1.0 - radius / 200.0))
    draw1024.ellipse([sun_cx - radius, sun_cy - radius, sun_cx + radius, sun_cy + radius], fill=(255, 217, 168, alpha))
# Sun disc
draw1024.ellipse([sun_cx - 46, sun_cy - 46, sun_cx + 46, sun_cy + 46], fill=(255, 241, 214, 255))

# Dark ridges
ridges = [
    ((42, 22, 24, 240), 400, 25),
    ((26, 14, 16, 255), 440, 20),
    ((15, 23, 22, 255), 475, 15)
]
for color, base_y, amp in ridges:
    pts = [(-20, 520)]
    for x in range(-20, 1050, 20):
        y = base_y + math.sin(x * 0.008 + base_y) * amp
        pts.append((x, y))
    pts.append((1050, 520))
    draw1024.polygon(pts, fill=color)

# Draw mark in center-top
m_scale = 1.4
m_ox = 512 - 50 * m_scale
m_oy = 120
m_arch = (255, 244, 228, 240)
m_sun = (232, 168, 96, 255)
m_sw = int(4.5 * m_scale)

# Mark Sun
draw1024.pieslice([m_ox + 32 * m_scale, m_oy + 64 * m_scale, m_ox + 68 * m_scale, m_oy + 100 * m_scale], start=180, end=360, fill=m_sun)
# Mark Arch
draw1024.line([(m_ox + 27 * m_scale, m_oy + 82 * m_scale), (m_ox + 27 * m_scale, m_oy + 46 * m_scale)], fill=m_arch, width=m_sw)
draw1024.arc([m_ox + 27 * m_scale, m_oy + 23 * m_scale, m_ox + 73 * m_scale, m_oy + 69 * m_scale], start=180, end=360, fill=m_arch, width=m_sw)
draw1024.line([(m_ox + 73 * m_scale, m_oy + 46 * m_scale), (m_ox + 73 * m_scale, m_oy + 82 * m_scale)], fill=m_arch, width=m_sw)
# Mark Ground
draw1024.line([(m_ox + 18 * m_scale, m_oy + 82 * m_scale), (m_ox + 82 * m_scale, m_oy + 82 * m_scale)], fill=m_arch, width=m_sw)

img1024.save("store_assets/feature_graphic_1024x500.png")
print("Saved store_assets/feature_graphic_1024x500.png")

# Also copy to artifacts directory for visual inspection
artifact_dir = r"C:\Users\aremu\.gemini\antigravity\brain\ba2b8aa3-92c1-46ed-a68e-b39b218b5eb8"
img512.save(os.path.join(artifact_dir, "play_store_512.png"))
img1024.save(os.path.join(artifact_dir, "feature_graphic_1024x500.png"))
print("Copied store assets to artifacts directory.")
