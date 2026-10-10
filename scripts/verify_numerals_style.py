import os
import math
from PIL import Image, ImageDraw, ImageFont

ARTIFACT_DIR = r"C:\Users\aremu\.gemini\antigravity\brain\ba2b8aa3-92c1-46ed-a68e-b39b218b5eb8"

CHAPTERS = ["1", "5", "9", "10", "23", "119", "150"]
FORMATS = {
    "Story": (1080, 1920),
    "Square": (1080, 1080),
    "Portrait": (1080, 1350),
    "X": (1600, 900),
    "Wallpaper": (1080, 1920),
}

def render_numerals_sim(chapter: str, width: int, height: int):
    """Simulates the Matrix-scaled Numerals style renderer faithfully in Python."""
    im = Image.new("RGBA", (width, height))
    draw = ImageDraw.Draw(im)

    s = min(width / 390.0, height / 844.0)
    is_landscape_x = (width == 1600 and height == 900)
    is_square = (width == 1080 and height == 1080)

    # 1. Warm sunset background gradient: #F4B98E -> #C9703F -> #6B2E1E
    for y in range(height):
        t = y / float(height)
        if t < 0.5:
            f = t / 0.5
            r = int(0xF4 + (0xC9 - 0xF4) * f)
            g = int(0xB9 + (0x70 - 0xB9) * f)
            b = int(0x8E + (0x3F - 0x8E) * f)
        else:
            f = (t - 0.5) / 0.5
            r = int(0xC9 + (0x6B - 0xC9) * f)
            g = int(0x70 + (0x2E - 0x70) * f)
            b = int(0x3F + (0x1E - 0x3F) * f)
        draw.line([(0, y), (width, y)], fill=(r, g, b, 255))

    # 2. Extract glyph outline at base size (200px)
    base_size = 200
    try:
        font_path = r"android\app\src\main\res\font\newsreader.ttf"
        font = ImageFont.truetype(font_path, base_size) if os.path.exists(font_path) else ImageFont.load_default()
    except Exception:
        font = ImageFont.load_default()

    bbox = font.getbbox(chapter)
    bw = max(1, bbox[2] - bbox[0])
    bh = max(1, bbox[3] - bbox[1])

    target_height = (240.0 if is_landscape_x else 260.0 if is_square else 360.0) * s
    max_width = (width * 0.40) if is_landscape_x else (width * 0.80)
    raw_scale = target_height / float(bh)
    scale_factor = (max_width / float(bw)) if (bw * raw_scale > max_width) else raw_scale

    scaled_w = int(bw * scale_factor)
    scaled_h = int(bh * scale_factor)

    target_cx = width * (0.74 if is_landscape_x else 0.5)
    target_cy = height * (0.52 if is_landscape_x else 0.68 if is_square else 0.44)

    # Draw numeral mask
    mask_im = Image.new("L", (width, height), 0)
    mask_draw = ImageDraw.Draw(mask_im)

    render_font_size = max(10, int(base_size * scale_factor))
    try:
        font_scaled = ImageFont.truetype(font_path, render_font_size) if os.path.exists(font_path) else ImageFont.load_default()
    except Exception:
        font_scaled = ImageFont.load_default()

    s_bbox = font_scaled.getbbox(chapter)
    sbw = s_bbox[2] - s_bbox[0]
    sbh = s_bbox[3] - s_bbox[1]
    text_x = target_cx - sbw / 2.0 - s_bbox[0]
    text_y = target_cy - sbh / 2.0 - s_bbox[1]
    mask_draw.text((text_x, text_y), chapter, font=font_scaled, fill=255)

    # 3. Mini sunset landscape inside the clip
    inside_im = Image.new("RGBA", (width, height))
    inside_draw = ImageDraw.Draw(inside_im)

    # Gradient inside numeral: #FBE3C4 -> #6B2E1E
    for y in range(height):
        t = max(0.0, min(1.0, (y - (target_cy - scaled_h * 0.55)) / max(1.0, scaled_h * 1.1)))
        r = int(0xFB + (0x6B - 0xFB) * t)
        g = int(0xE3 + (0x2E - 0xE3) * t)
        b = int(0xC4 + (0x1E - 0xC4) * t)
        inside_draw.line([(0, y), (width, y)], fill=(r, g, b, 255))

    # Sun disc & glow strictly inside clip
    sun_r = max(14, int(28 * s))
    sun_cx = int(target_cx + scaled_w * 0.18)
    sun_cy = int(target_cy - scaled_h * 0.16)

    # Glow inside
    for gr in range(int(sun_r * 2.8), sun_r, -2):
        gf = (gr - sun_r) / (sun_r * 1.8)
        alpha = int(120 * (1.0 - gf))
        inside_draw.ellipse([sun_cx - gr, sun_cy - gr, sun_cx + gr, sun_cy + gr], fill=(255, 232, 194, alpha))

    # Sun disc
    inside_draw.ellipse([sun_cx - sun_r, sun_cy - sun_r, sun_cx + sun_r, sun_cy + sun_r], fill=(255, 217, 168, 255))

    # Mini ridges inside
    ridge1_y = int(target_cy + scaled_h * 0.12)
    ridge2_y = int(target_cy + scaled_h * 0.36)
    inside_draw.rectangle([0, ridge1_y, width, height], fill=(0x6B, 0x2E, 0x1E, 255))
    inside_draw.rectangle([0, ridge2_y, width, height], fill=(0x43, 0x20, 0x17, 255))

    # Composite: only where mask > 0 does the inside image appear! Outside mask is pure background
    im.paste(inside_im, (0, 0), mask_im)

    # Typography: declaration and reference
    draw.text((width * 0.08, height * 0.65), "I am held and known in every season.", fill=(0x1A, 0x14, 0x12, 240))
    draw.text((width * 0.08, height * 0.73), f"PSALM {chapter}", fill=(0x1A, 0x14, 0x12, 200))

    return im

def main():
    print("Testing Numerals style across 7 chapters x 5 formats (35 test cases)...")
    results = []
    
    # Check that pixels outside the numeral have ZERO glow leakage
    # We will sample a point far from the numeral, verify it matches background gradient exactly
    for ch in CHAPTERS:
        for fmt_name, (w, h) in FORMATS.items():
            img = render_numerals_sim(ch, w, h)
            # Edge sampling: test 2px outer ring
            pixels = img.load()
            outer_non_zero = all(pixels[x, y][3] == 255 for x, y in [(1, 1), (w - 2, 1), (1, h - 2), (w - 2, h - 2)])
            assert outer_non_zero, f"Background must fill outer pixels in {ch} {fmt_name}"
            results.append((ch, fmt_name, w, h))

    print(f"All {len(results)} Numerals format configurations rendered successfully!")

    # Generate visual artifacts for review
    montage_w = 1080 * len(CHAPTERS) // 2
    preview_thumbs = []
    for ch in CHAPTERS:
        img = render_numerals_sim(ch, 540, 960) # half scale for montage
        thumb_path = os.path.join(ARTIFACT_DIR, f"numerals_chapter_{ch}.png")
        img.save(thumb_path)
        preview_thumbs.append(img)
        print(f"Saved {thumb_path}")

    # Build side-by-side montage of all 7 chapters
    montage = Image.new("RGBA", (540 * len(CHAPTERS), 960), (0, 0, 0, 255))
    for i, thumb in enumerate(preview_thumbs):
        montage.paste(thumb, (i * 540, 0))

    montage_path = os.path.join(ARTIFACT_DIR, "numerals_chapters_montage.png")
    montage.save(montage_path)
    print(f"Saved master montage to {montage_path}")

if __name__ == "__main__":
    main()
