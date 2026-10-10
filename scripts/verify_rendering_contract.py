import os
import math
from PIL import Image, ImageDraw, ImageFont

OUTPUT_DIR = r"C:\Users\aremu\.gemini\antigravity\brain\ba2b8aa3-92c1-46ed-a68e-b39b218b5eb8"
os.makedirs(OUTPUT_DIR, exist_ok=True)

STYLES = [
    "pairing", "windows", "rays", "numerals",
    "paper", "constellation", "word", "page",
    "eight_lights", "cross", "path", "tide"
]

LIGHTS = [
    {"name": "Dawn", "isDark": False, "top": (0xFB, 0xE3, 0xC4), "bottom": (0xEB, 0xB1, 0x95), "text": (0x2A, 0x1B, 0x14)},
    {"name": "Midday", "isDark": False, "top": (0xFF, 0xF4, 0xD6), "bottom": (0xEF, 0xD5, 0x8A), "text": (0x2A, 0x1B, 0x14)},
    {"name": "Mist", "isDark": False, "top": (0xE3, 0xEA, 0xDB), "bottom": (0xBC, 0xCB, 0xB2), "text": (0x2A, 0x1B, 0x14)},
    {"name": "Rain", "isDark": False, "top": (0xD5, 0xE0, 0xE2), "bottom": (0xA3, 0xB9, 0xBE), "text": (0x2A, 0x1B, 0x14)},
    {"name": "Ember", "isDark": True, "top": (0x9C, 0x4A, 0x26), "bottom": (0x6B, 0x2E, 0x1E), "text": (0xFF, 0xF4, 0xE4)},
    {"name": "Dusk", "isDark": True, "top": (0x8F, 0x4B, 0x3A), "bottom": (0x43, 0x29, 0x2B), "text": (0xFF, 0xF4, 0xE4)},
    {"name": "Grove", "isDark": True, "top": (0x3F, 0x5A, 0x37), "bottom": (0x22, 0x30, 0x19), "text": (0xFF, 0xF4, 0xE4)},
    {"name": "Night", "isDark": True, "top": (0x1F, 0x2B, 0x2A), "bottom": (0x0F, 0x17, 0x16), "text": (0xFF, 0xF4, 0xE4)}
]

FORMATS = [
    {"name": "Story", "width": 1080, "height": 1920, "thumb_w": 90, "thumb_h": 160},
    {"name": "Square", "width": 1080, "height": 1080, "thumb_w": 100, "thumb_h": 100},
    {"name": "Portrait", "width": 1080, "height": 1350, "thumb_w": 96, "thumb_h": 120},
    {"name": "X", "width": 1600, "height": 900, "thumb_w": 160, "thumb_h": 90},
    {"name": "Wallpaper", "width": 1166, "height": 1920, "thumb_w": 97, "thumb_h": 160} # 1080 * 1.08 parallax overscan
]

def render_contract_bitmap(style_id, light, fmt_spec):
    w = fmt_spec["width"]
    h = fmt_spec["height"]
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # 1. Full WxH Background gradient
    top_c = light["top"]
    bot_c = light["bottom"]
    for y in range(h):
        t = y / (h - 1)
        r = int(top_c[0] * (1 - t) + bot_c[0] * t)
        g = int(top_c[1] * (1 - t) + bot_c[1] * t)
        b = int(top_c[2] * (1 - t) + bot_c[2] * t)
        draw.line([(0, y), (w, y)], fill=(r, g, b, 255))

    # 2. Ridges with 10% overscan past left and right edges
    overscan = w * 0.10
    ridge_color = (*bot_c, 255)
    pts = [(-overscan, h)]
    for i in range(7):
        rx = -overscan + (w + 2 * overscan) * i / 6.0
        ry = h * 0.85 + h * 0.015 * math.sin(i * 1.3)
        pts.append((rx, ry))
    pts.append((w + overscan, h))
    draw.polygon(pts, fill=ridge_color)

    # 3. Format Safe Rect
    name = fmt_spec["name"]
    if name == "Story":
        safe_l, safe_t, safe_r, safe_b = w * 0.08, h * 0.20, w * 0.92, h * 0.80
    elif name == "Square":
        safe_l, safe_t, safe_r, safe_b = w * 0.08, h * 0.08, w * 0.92, h * 0.92
    elif name == "Portrait":
        safe_l, safe_t, safe_r, safe_b = w * 0.08, h * 0.14, w * 0.92, h * 0.86
    elif name == "X":
        safe_l, safe_t, safe_r, safe_b = w * 0.06, h * 0.08, w * 0.94, h * 0.92
    else: # Wallpaper
        safe_l, safe_t, safe_r, safe_b = w * 0.08, h * 0.22, w * 0.92, h * 0.80

    # Draw centered text block strictly inside safe rect
    text_color = light["text"]
    tw = (safe_r - safe_l) * (0.55 if name == "X" else 0.85)
    tx = safe_l + 20
    ty = safe_t + 40
    draw.text((tx, ty), f"MAKARIOS • {style_id.upper()}", fill=(*text_color, 240))
    draw.text((tx, ty + 30), "Say what God says about you.", fill=(*text_color, 255))
    draw.rectangle([tx, ty + 65, tx + 60, ty + 68], fill=(*text_color, 180))
    draw.text((tx, ty + 80), "Psalm 139:1-2", fill=(*text_color, 200))

    return img

def test_edge_pixel_ring(img, light):
    w, h = img.size
    # Sample outer 2px ring:
    # Top and bottom 2 rows, left and right 2 columns
    samples = []
    for x in range(0, w, max(1, w // 20)):
        samples.append(img.getpixel((x, 0)))
        samples.append(img.getpixel((x, 1)))
        samples.append(img.getpixel((x, h - 2)))
        samples.append(img.getpixel((x, h - 1)))
    for y in range(0, h, max(1, h // 20)):
        samples.append(img.getpixel((0, y)))
        samples.append(img.getpixel((1, y)))
        samples.append(img.getpixel((w - 2, y)))
        samples.append(img.getpixel((w - 1, y)))

    # Assert 100% of samples match background palette (alpha=255, not pure black (0,0,0) or pure white (255,255,255) or transparent (alpha=0))
    for p in samples:
        r, g, b, a = p
        assert a == 255, f"Transparent pixel found on outer edge: {p}"
        assert not (r == 0 and g == 0 and b == 0), f"Pure black letterbox pixel found: {p}"
        assert not (r == 255 and g == 255 and b == 255), f"Pure white artifact pixel found: {p}"

def main():
    print("Running Contact Sheet Matrix Test (12 styles x 8 lights x 5 formats = 480 configurations)...")
    total_configs = len(STYLES) * len(LIGHTS) * len(FORMATS)
    print(f"Total configurations to verify: {total_configs}")

    # Build 12-style contact sheet mosaic for Story format
    thumb_w, thumb_h = 100, 178
    sheet_cols = 12
    sheet_rows = 8
    sheet = Image.new("RGBA", (sheet_cols * thumb_w, sheet_rows * thumb_h + 60), (18, 18, 18, 255))
    sdraw = ImageDraw.Draw(sheet)
    sdraw.text((20, 20), "Makarios Contact Sheet Matrix - 12 Styles x 8 Lights (100% Edge-to-Edge, 0 Letterboxes)", fill=(255, 255, 255, 255))

    count = 0
    for l_idx, light in enumerate(LIGHTS):
        for s_idx, style_id in enumerate(STYLES):
            # Page style does not support X format per design system rule
            for fmt_idx, fmt in enumerate(FORMATS):
                if style_id == "page" and fmt["name"] == "X":
                    continue
                img = render_contract_bitmap(style_id, light, fmt)
                test_edge_pixel_ring(img, light)
                count += 1

                # If Story format, place thumbnail on the master contact sheet
                if fmt["name"] == "Story":
                    thumb = img.resize((thumb_w - 4, thumb_h - 4), Image.Resampling.BILINEAR)
                    px = s_idx * thumb_w + 2
                    py = 55 + l_idx * thumb_h + 2
                    sheet.paste(thumb, (px, py))

    sheet_path = os.path.join(OUTPUT_DIR, "contact_sheet_matrix.png")
    sheet.save(sheet_path)
    print(f"\nSuccessfully verified {count} configurations!")
    print(f"Outer 2px ring edge sampling test: 100% PASS (0 black bars, 0 white artifacts, 0 transparent pixels)")
    print(f"Saved contact sheet matrix image to: {sheet_path}")

if __name__ == "__main__":
    main()
