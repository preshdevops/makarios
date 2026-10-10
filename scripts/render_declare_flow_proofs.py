import os
import math
from PIL import Image, ImageDraw, ImageFont

def render_screens():
    artifacts_dir = r"C:\Users\aremu\.gemini\antigravity\brain\ba2b8aa3-92c1-46ed-a68e-b39b218b5eb8"
    os.makedirs(artifacts_dir, exist_ok=True)

    font_regular_path = r"c:\Users\aremu\Desktop\projects\makarios\android\app\src\main\res\font\hanken_grotesk.ttf"
    font_serif_path = r"c:\Users\aremu\Desktop\projects\makarios\android\app\src\main\res\font\newsreader.ttf"
    font_italic_path = r"c:\Users\aremu\Desktop\projects\makarios\android\app\src\main\res\font\newsreader_italic.ttf"

    font_title = ImageFont.truetype(font_serif_path, 42)
    font_head = ImageFont.truetype(font_serif_path, 34)
    font_body = ImageFont.truetype(font_regular_path, 20)
    font_body_italic = ImageFont.truetype(font_italic_path, 22)
    font_label = ImageFont.truetype(font_regular_path, 16)
    font_btn = ImageFont.truetype(font_regular_path, 22)
    font_small = ImageFont.truetype(font_regular_path, 14)

    w, h = 540, 1170 # Compact high-res phone screen

    # Colors for Dawn
    top_c = (244, 232, 216)
    bot_c = (215, 194, 168)
    ink = (30, 23, 17)

    def draw_bg(draw):
        # Gradient
        for y in range(h):
            t = y / h
            r = int(top_c[0] * (1 - t) + bot_c[0] * t)
            g = int(top_c[1] * (1 - t) + bot_c[1] * t)
            b = int(top_c[2] * (1 - t) + bot_c[2] * t)
            draw.line([(0, y), (w, y)], fill=(r, g, b))

        # Ridges
        for i, (col, y0, amp) in enumerate([
            ((200, 175, 145), int(h * 0.76), 18),
            ((175, 145, 120), int(h * 0.84), 14),
            (bot_c, int(h * 0.92), 10)
        ]):
            pts = [(0, h)]
            for x in range(0, w + 10, 10):
                ry = y0 + amp * math.sin(x * 0.015 + i * 2.0)
                pts.append((x, ry))
            pts.append((w, h))
            draw.polygon(pts, fill=col)

    # 1. Step 2: Declare Verse
    img2 = Image.new("RGB", (w, h))
    d2 = ImageDraw.Draw(img2)
    draw_bg(d2)

    # System bar / top nav
    d2.text((w//2, 45), "9:41", fill=ink, font=font_small, anchor="mm")
    d2.ellipse([(28, 70), (68, 110)], fill=(30, 23, 17, 20), outline=ink, width=1)
    d2.text((48, 90), "<", fill=ink, font=font_btn, anchor="mm")
    d2.text((w//2, 90), "2. Anchor in Scripture", fill=ink, font=font_label, anchor="mm")

    # Progress bar (step 2 active)
    d2.rectangle([(32, 125), (180, 129)], fill=ink)
    d2.rectangle([(190, 125), (345, 129)], fill=ink)
    d2.rectangle([(355, 125), (508, 129)], fill=(180, 160, 140))

    # Declaration quote card
    d2.rounded_rectangle([(32, 145), (508, 235)], radius=16, fill=(255, 255, 255, 90), outline=(30, 23, 17, 40), width=1)
    d2.text((50, 165), "YOUR DECLARATION", fill=(100, 80, 60), font=font_small)
    d2.text((50, 195), "\"I walk in perfect peace because God guards my heart.\"", fill=ink, font=font_body_italic)

    # Headings
    d2.text((32, 260), "Anchor in Scripture", fill=ink, font=font_title)
    d2.text((32, 310), "Choose the verse that anchors what you are declaring.", fill=(80, 65, 50), font=font_body)

    # Best Match Card
    d2.rounded_rectangle([(32, 355), (508, 595)], radius=20, fill=(255, 255, 255, 220), outline=ink, width=2)
    d2.rounded_rectangle([(52, 375), (160, 402)], radius=8, fill=ink)
    d2.text((106, 388), "BEST MATCH", fill=top_c, font=font_small, anchor="mm")
    d2.ellipse([(468, 376), (492, 400)], fill=ink)
    d2.text((480, 388), "\u2713", fill=top_c, font=font_small, anchor="mm")

    d2.text((52, 420), "Philippians 4:6-7", fill=ink, font=font_body)
    verse_text = "\"Do not be anxious about anything, but in every situation, by prayer and petition, with thanksgiving, present your requests to God. And the peace of God, which transcends all understanding, will guard your hearts...\""
    # Simple wrap
    d2.text((52, 455), verse_text[:95] + "\n" + verse_text[95:185] + "...", fill=(40, 30, 20), font=font_body_italic)

    # Alternatives section
    d2.text((32, 620), "ALTERNATIVES", fill=(100, 80, 60), font=font_small)

    # Alt 1 card
    d2.rounded_rectangle([(32, 645), (508, 775)], radius=16, fill=(255, 255, 255, 170), outline=(30, 23, 17, 30), width=1)
    d2.text((50, 662), "John 14:27", fill=ink, font=font_body)
    d2.text((50, 695), "\"Peace I leave with you; my peace I give you. I do not give\nto you as the world gives. Do not let your hearts be troubled...\"", fill=(60, 50, 40), font=font_small)

    # Alt 2 card
    d2.rounded_rectangle([(32, 795), (508, 925)], radius=16, fill=(255, 255, 255, 170), outline=(30, 23, 17, 30), width=1)
    d2.text((50, 812), "Isaiah 26:3", fill=ink, font=font_body)
    d2.text((50, 845), "\"You will keep in perfect peace those whose minds are\nsteadfast, because they trust in you.\"", fill=(60, 50, 40), font=font_small)

    # Actions: Search the Bible & Edit
    d2.text((50, 955), "\uD83D\uDCD6 Search the Bible", fill=ink, font=font_label)
    d2.text((490, 955), "Edit declaration", fill=(80, 65, 50), font=font_label, anchor="ra")

    # Bottom button "Use this verse"
    d2.rounded_rectangle([(32, 1000), (508, 1070)], radius=35, fill=ink)
    d2.text((w//2, 1035), "Use this verse", fill=top_c, font=font_btn, anchor="mm")

    # 2. Step 3: Declare Look
    img3 = Image.new("RGB", (w, h))
    d3 = ImageDraw.Draw(img3)
    draw_bg(d3)

    d3.text((w//2, 45), "9:41", fill=ink, font=font_small, anchor="mm")
    d3.ellipse([(28, 70), (68, 110)], fill=(30, 23, 17, 20), outline=ink, width=1)
    d3.text((48, 90), "<", fill=ink, font=font_btn, anchor="mm")
    d3.text((w//2, 90), "3. Look (styles)", fill=ink, font=font_label, anchor="mm")

    # Progress bar (all 3 filled)
    d3.rectangle([(32, 125), (180, 129)], fill=ink)
    d3.rectangle([(190, 125), (345, 129)], fill=ink)
    d3.rectangle([(355, 125), (508, 129)], fill=ink)

    # Live preview container
    prev_x0, prev_y0, prev_x1, prev_y1 = w//2 - 110, 150, w//2 + 110, 570
    d3.rounded_rectangle([(prev_x0, prev_y0), (prev_x1, prev_y1)], radius=22, fill=(240, 225, 205), outline=(30, 23, 17, 40), width=1)
    # Sun & rays inside preview
    d3.ellipse([(prev_x0 + 130, prev_y0 + 120), (prev_x0 + 190, prev_y0 + 180)], fill=(255, 240, 210))
    d3.text((prev_x0 + 20, prev_y0 + 70), "I walk in\nperfect peace\nbecause God\nguards my heart.", fill=ink, font=font_body)
    d3.line([(prev_x0 + 20, prev_y0 + 220), (prev_x0 + 55, prev_y0 + 220)], fill=(180, 140, 80), width=2)
    d3.text((prev_x0 + 20, prev_y0 + 235), "\"The peace of God...\"", fill=ink, font=font_small)
    d3.text((prev_x0 + 20, prev_y0 + 265), "PHILIPPIANS 4:7", fill=(100, 80, 60), font=font_small)

    # Style row
    d3.text((32, 600), "Style", fill=ink, font=font_label)
    styles = ["Pairing", "Windows", "Rays", "Numerals", "Paper", "Word"]
    for idx, sname in enumerate(styles[:5]):
        sx = 32 + idx * 96
        is_sel = idx == 0
        d3.rounded_rectangle([(sx, 630), (sx + 84, 760)], radius=12, fill=(230, 215, 195), outline=ink if is_sel else (0,0,0,0), width=2 if is_sel else 0)
        d3.text((sx + 42, 775), sname, fill=ink, font=font_small, anchor="mm")

    # Light row
    d3.text((32, 805), "Light", fill=ink, font=font_label)
    swatches = [(230, 210, 190), (244, 232, 216), (255, 248, 220), (220, 230, 235), (200, 215, 225), (60, 30, 25), (45, 25, 40), (20, 35, 25), (15, 20, 30)]
    for idx, col in enumerate(swatches[:7]):
        lx = 32 + idx * 68
        is_lsel = idx == 1
        d3.rounded_rectangle([(lx, 830), (lx + 54, 874)], radius=10, fill=col, outline=ink if is_lsel else (0,0,0,0), width=2 if is_lsel else 0)

    # Two equal pills side by side: Share (outline) + Done (ink)
    btn_y = 905
    d3.rounded_rectangle([(32, btn_y), (260, btn_y + 64)], radius=32, fill=(0,0,0,0), outline=ink, width=2)
    d3.text((146, btn_y + 32), "\u293F  Share", fill=ink, font=font_btn, anchor="mm")

    d3.rounded_rectangle([(280, btn_y), (508, btn_y + 64)], radius=32, fill=ink)
    d3.text((394, btn_y + 32), "Done", fill=top_c, font=font_btn, anchor="mm")

    # Beneath text actions row
    text_y = 995
    d3.text((50, text_y), "\uD83D\uDDBC Save to photos", fill=ink, font=font_small)
    d3.text((w//2, text_y), "\uD83D\uDCF1 Set as wallpaper", fill=ink, font=font_small, anchor="mm")
    d3.text((490, text_y), "\uD83D\uDCC4 Share as file", fill=ink, font=font_small, anchor="ra")

    # 3. Step 4: Declare Done ("Kept.")
    img4 = Image.new("RGB", (w, h))
    d4 = ImageDraw.Draw(img4)
    draw_bg(d4)

    d4.text((w//2, 45), "9:41", fill=ink, font=font_small, anchor="mm")

    # Checkmark circle icon
    d4.ellipse([(w//2 - 40, 220), (w//2 + 40, 300)], fill=(30, 23, 17, 30))
    d4.text((w//2, 260), "\u2713", fill=ink, font=font_title, anchor="mm")

    # "Kept." headline
    d4.text((w//2, 350), "Kept.", fill=ink, font=ImageFont.truetype(font_serif_path, 52), anchor="mm")
    d4.text((w//2, 400), "Saved to Mine. It will be waiting for you.", fill=(70, 55, 40), font=font_body, anchor="mm")

    # Artwork preview (140x245 equivalent)
    art_w, art_h = 180, 315
    ax0, ay0 = w//2 - art_w//2, 460
    ax1, ay1 = ax0 + art_w, ay0 + art_h
    d4.rounded_rectangle([(ax0, ay0), (ax1, ay1)], radius=18, fill=(240, 225, 205), outline=(30, 23, 17, 30), width=1)
    d4.ellipse([(ax0 + 100, ay0 + 70), (ax0 + 150, ay0 + 120)], fill=(255, 240, 210))
    d4.text((ax0 + 15, ay0 + 40), "I walk in\nperfect peace\nbecause God\nguards my heart.", fill=ink, font=font_small)
    d4.text((ax0 + 15, ay0 + 160), "PHILIPPIANS 4:7", fill=(100, 80, 60), font=ImageFont.truetype(font_regular_path, 11))

    # Primary "Back to Today" button (56dp)
    d4.rounded_rectangle([(32, 850), (508, 920)], radius=35, fill=ink)
    d4.text((w//2, 885), "Back to Today", fill=top_c, font=font_btn, anchor="mm")

    # Text button "Declare another"
    d4.text((w//2, 960), "Declare another", fill=ink, font=font_btn, anchor="mm")

    # Save individuals
    p2 = os.path.join(artifacts_dir, "declare_step2_verse.png")
    p3 = os.path.join(artifacts_dir, "declare_step3_look.png")
    p4 = os.path.join(artifacts_dir, "declare_step4_done.png")
    img2.save(p2)
    img3.save(p3)
    img4.save(p4)

    # Assemble 3-panel montage
    margin = 20
    montage_w = w * 3 + margin * 4
    montage_h = h + margin * 2
    montage = Image.new("RGB", (montage_w, montage_h), (250, 248, 245))

    montage.paste(img2, (margin, margin))
    montage.paste(img3, (margin * 2 + w, margin))
    montage.paste(img4, (margin * 3 + w * 2, margin))

    montage_path = os.path.join(artifacts_dir, "declare_flow_h_montage.png")
    montage.save(montage_path)
    print("Montage saved to:", montage_path)

if __name__ == "__main__":
    render_screens()
