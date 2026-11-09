"""Generates the 192x64 scoreboard gifs. Run: python tools/gifs/make_gifs.py [path/to/basics-image]"""
import math
import random
import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageEnhance, ImageFilter, ImageFont

W, H = 192, 64
FRAME_MS = 80
OUT = Path(__file__).resolve().parents[2] / "src/main/resources/META-INF/resources"
TRY_GIF = OUT / "brigandze_try_fireworks_192x64.gif"
BASICS_IMAGE = Path(__file__).resolve().parent / "basics.png"

YELLOW = (255, 214, 0)
PALE_YELLOW = (255, 240, 120)
ORANGE = (230, 90, 0)
GREEN = (40, 140, 70)
WHITE = (240, 240, 240)
BALL = (150, 85, 35)
FIREWORK_COLORS = [(255, 200, 0), (255, 60, 60), (200, 60, 200), (60, 160, 255), (255, 120, 0)]

# condensed font: long words stay tall on a 192px wide board
FONT_BIG = "C:/Windows/Fonts/impact.ttf"
FONT_SMALL = "C:/Windows/Fonts/arialbd.ttf"

# headline lives above the BRIGANDZE label, which sits at y=51..60 like in the TRY gif
HEADLINE_CY = 25
HEADLINE_MAX_H = 40
LABEL_Y = 51


def font(path, size):
    return ImageFont.truetype(path, size)


def text_sprite(text, size, shown=None, fill=YELLOW):
    """Aliased headline with a 1px orange outline; shown limits it to the first letters, keeping the full layout."""
    f = font(FONT_BIG, size)
    l, t, r, b = f.getbbox(text, stroke_width=1)
    img = Image.new("RGBA", (r - l, b - t), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.fontmode = "1"
    d.text((-l, -t), text if shown is None else text[:shown], font=f, fill=fill, stroke_width=1, stroke_fill=ORANGE)
    # the outline is still smoothed, so snap every pixel to fully on or off
    img.putalpha(img.getchannel("A").point(lambda a: 255 if a >= 128 else 0))
    return img


def fit_size(text, max_w, max_h):
    """Largest headline size for which the text fits the box."""
    for size in range(80, 6, -1):
        l, t, r, b = font(FONT_BIG, size).getbbox(text, stroke_width=1)
        if r - l <= max_w and b - t <= max_h:
            return size
    return 7


def paste_centered(img, sprite, cx, cy):
    img.paste(sprite, (round(cx - sprite.width / 2), round(cy - sprite.height / 2)), sprite)


def brigandze_label():
    """The BRIGANDZE lettering from the TRY gif; the per-pixel median over its frames drops the fireworks."""
    im = Image.open(TRY_GIF)
    frames = []
    for i in range(im.n_frames):
        im.seek(i)
        frames.append(im.convert("RGB"))
    x0, y0, x1, y1 = 74, 47, 148, 64
    label = Image.new("RGB", (x1 - x0, y1 - y0))
    for y in range(y0, y1):
        for x in range(x0, x1):
            pixels = sorted(f.getpixel((x, y)) for f in frames)
            label.putpixel((x - x0, y - y0), pixels[len(pixels) // 2])
    label = label.crop(label.getbbox())
    mask = label.convert("L").point(lambda v: 255 if v > 8 else 0)
    return label, mask


def subtitle(img, label, cx):
    sprite, mask = label
    img.paste(sprite, (round(cx - sprite.width / 2), LABEL_Y), mask)


def pop_scale(i, start):
    """0 before start, then overshoot and settle at 1."""
    t = i - start
    if t < 0:
        return 0
    return [0.5, 0.8, 1.08, 1.04, 1.0][t] if t < 5 else 1.0


class Fireworks:
    def __init__(self, seed, area, count, first, every):
        rnd = random.Random(seed)
        x0, y0, x1, y1 = area
        self.bursts = [(first + k * every + rnd.randint(0, 2), rnd.randint(x0, x1), rnd.randint(y0, y1),
                        rnd.choice(FIREWORK_COLORS), rnd.randint(9, 14)) for k in range(count)]

    def draw(self, draw, i):
        for start, x, y, color, radius in self.bursts:
            age = i - start
            if not 0 <= age < 9:
                continue
            r = radius * math.sin(age / 9 * math.pi / 2)
            fade = 1 - age / 9
            c = tuple(int(v * (0.35 + 0.65 * fade)) for v in color)
            for k in range(14):
                a = k / 14 * 2 * math.pi
                px, py = x + r * math.cos(a), y + r * math.sin(a) + age * 0.4
                draw.point((round(px), round(py)), fill=c)
                if age < 5:
                    draw.point((round(px * 0.85 + x * 0.15), round(py * 0.85 + y * 0.15)), fill=c)


def posts(draw, cx, flash=False):
    """Rugby H-posts with the crossbar at y=38."""
    c = YELLOW if flash else WHITE
    draw.rectangle((cx - 11, 6, cx - 10, 63), fill=c)
    draw.rectangle((cx + 10, 6, cx + 11, 63), fill=c)
    draw.rectangle((cx - 10, 38, cx + 10, 39), fill=c)


def ball(draw, x, y, size, spin):
    w = max(2, size)
    h = max(2, round(size * (0.55 + 0.25 * abs(math.sin(spin)))))
    draw.ellipse((round(x - w / 2), round(y - h / 2), round(x + w / 2), round(y + h / 2)),
                 fill=BALL, outline=(90, 45, 15))
    if w >= 5:
        draw.line((round(x - w / 4), round(y), round(x + w / 4), round(y)), fill=WHITE)


def kick_gif(text, seed, label):
    """Ball kicked over the bar, the posts flash and clear, then the text pops in full width with fireworks."""
    frames = []
    posts_x = W // 2
    flight, cleared = 14, 20
    size = fit_size(text, W - 6, HEADLINE_MAX_H)
    fw = Fireworks(seed, (6, 4, W - 6, 46), 9, cleared + 2, 4)
    rnd = random.Random(seed)
    stars = [(rnd.randint(0, W - 1), rnd.randint(0, 40)) for _ in range(18)]
    for i in range(60):
        img = Image.new("RGB", (W, H))
        d = ImageDraw.Draw(img)
        for sx, sy in stars:
            if (sx + i) % 7:
                d.point((sx, sy), fill=(60, 60, 60))
        if i < cleared:
            posts(d, posts_x, flash=i >= flight and i % 4 < 2)
        if i < flight:
            t = i / flight
            x = 30 + (posts_x - 30) * t
            y = 60 + (22 - 60) * t - 18 * math.sin(math.pi * t)
            ball(d, x, y, round(10 - 7 * t), i * 1.3)
        fw.draw(d, i)
        s = pop_scale(i, cleared)
        if s:
            sprite = text_sprite(text, round(size * s))
            if sprite.width > W:
                sprite = text_sprite(text, size)
            paste_centered(img, sprite, W / 2, HEADLINE_CY)
            subtitle(img, label, W / 2)
        frames.append(img)
    return frames


def basics_gif(image_path, label):
    """Image on the left, 'DE BASICS!' typed out letter by letter on the right."""
    frames = []
    text = "DE BASICS!"
    box = 58
    left = box + 6
    text_cx = left + (W - left) / 2
    size = fit_size(text, W - left - 2, HEADLINE_MAX_H)
    logo = load_logo(image_path, box)
    for i in range(46):
        img = Image.new("RGB", (W, H))
        # logo slides in from the left
        x = min(3, -box + i * 12)
        bob = round(math.sin(i / 4) * 1.5) if i > 8 else 0
        img.paste(logo, (x, 3 + bob), logo)
        shown = max(0, min(len(text), (i - 6) // 2))
        if shown:
            fill = YELLOW if i < 30 or i % 6 < 4 else PALE_YELLOW
            paste_centered(img, text_sprite(text, size, shown, fill), text_cx, HEADLINE_CY)
        if i >= 6 + 2 * len(text):
            subtitle(img, label, text_cx)
        frames.append(img)
    return frames


def load_logo(image_path, box):
    """Photo scaled once to fit inside a 1px green frame, with extra contrast and sharpening for the LED board."""
    if image_path and Path(image_path).exists():
        src = Image.open(image_path).convert("RGB")
        src.thumbnail((box - 2, box - 2), Image.LANCZOS)
        src = ImageEnhance.Contrast(src).enhance(1.35)
        src = ImageEnhance.Color(src).enhance(1.1)
        src = src.filter(ImageFilter.UnsharpMask(radius=1, percent=80, threshold=2))
        logo = Image.new("RGBA", (box, box), GREEN + (255,))
        logo.paste(src, ((box - src.width) // 2, (box - src.height) // 2))
        return logo
    # placeholder until the real image is provided
    logo = Image.new("RGBA", (box, box), (0, 0, 0, 0))
    d = ImageDraw.Draw(logo)
    for k in range(0, box, 6):
        for a, b in (((k, 0), (k + 3, 0)), ((k, box - 1), (k + 3, box - 1)),
                     ((0, k), (0, k + 3)), ((box - 1, k), (box - 1, k + 3))):
            d.line((a, b), fill=GREEN)
    d.fontmode = "1"
    d.text((box / 2, box / 2), "IMG", font=font(FONT_SMALL, 12), fill=GREEN, anchor="mm")
    return logo


def save(frames, name):
    path = OUT / name
    frames[0].save(path, save_all=True, append_images=frames[1:], duration=FRAME_MS, loop=0, optimize=False)
    print("wrote", path)


if __name__ == "__main__":
    label = brigandze_label()
    save(kick_gif("CONVERSION", 1, label), "brigandze_conversion_192x64.gif")
    save(kick_gif("PENALTY", 2, label), "brigandze_penalty_192x64.gif")
    save(basics_gif(sys.argv[1] if len(sys.argv) > 1 else BASICS_IMAGE, label), "brigandze_basics_192x64.gif")
