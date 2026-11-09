"""Generates the 192x64 scoreboard gifs. Run: python tools/gifs/make_gifs.py [path/to/basics-image]"""
import math
import random
import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

W, H = 192, 64
FRAME_MS = 80
OUT = Path(__file__).resolve().parents[2] / "src/main/resources/META-INF/resources"
BASICS_IMAGE = Path(__file__).resolve().parent / "basics.png"

YELLOW = (255, 214, 0)
ORANGE = (230, 90, 0)
GREEN = (40, 140, 70)
WHITE = (240, 240, 240)
BALL = (150, 85, 35)
FIREWORK_COLORS = [(255, 200, 0), (255, 60, 60), (200, 60, 200), (60, 160, 255), (255, 120, 0)]

FONT_BIG = "C:/Windows/Fonts/ariblk.ttf"
FONT_SMALL = "C:/Windows/Fonts/arialbd.ttf"


def font(path, size):
    return ImageFont.truetype(path, size)


def fit_font(text, max_w, max_h):
    """Largest headline font for which the text fits the box."""
    for size in range(40, 6, -1):
        f = font(FONT_BIG, size)
        l, t, r, b = f.getbbox(text, stroke_width=1)
        if r - l <= max_w and b - t <= max_h:
            return size
    return 7


def headline(draw, text, cx, cy, size, fill=YELLOW):
    f = font(FONT_BIG, max(6, size))
    draw.text((cx, cy), text, font=f, fill=fill, anchor="mm", stroke_width=1, stroke_fill=ORANGE)


def subtitle(draw, cx, y=56):
    draw.text((cx, y), "BRIGANDZE", font=font(FONT_SMALL, 10), fill=GREEN, anchor="mm")


def pop_scale(i, start):
    """0 before start, then overshoot to 1.15 and settle at 1."""
    t = i - start
    if t < 0:
        return 0
    return [0.5, 0.8, 1.1, 1.15, 1.05, 1.0][t] if t < 6 else 1.0


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
    """Rugby H-posts with the crossbar at y=34."""
    c = YELLOW if flash else WHITE
    draw.line((cx - 10, 4, cx - 10, 62), fill=c, width=2)
    draw.line((cx + 10, 4, cx + 10, 62), fill=c, width=2)
    draw.line((cx - 10, 34, cx + 10, 34), fill=c, width=2)


def ball(draw, x, y, size, spin):
    w = max(2, size)
    h = max(2, round(size * (0.55 + 0.25 * abs(math.sin(spin)))))
    draw.ellipse((x - w / 2, y - h / 2, x + w / 2, y + h / 2), fill=BALL, outline=(90, 45, 15))
    if w >= 5:
        draw.line((x - w / 4, y, x + w / 4, y), fill=WHITE)


def kick_gif(text, seed):
    """Ball kicked from the bottom through the posts, then the text pops in with fireworks."""
    frames = []
    posts_x = 26
    text_cx = 26 + (W - 26) // 2 + 6
    size = fit_font(text, W - 58, 34)
    fw = Fireworks(seed, (60, 6, 186, 50), 6, 16, 4)
    rnd = random.Random(seed)
    stars = [(rnd.randint(0, W - 1), rnd.randint(0, 40)) for _ in range(18)]
    flight = 14
    for i in range(46):
        img = Image.new("RGB", (W, H))
        d = ImageDraw.Draw(img)
        for sx, sy in stars:
            if (sx + i) % 7:
                d.point((sx, sy), fill=(60, 60, 60))
        through = i >= flight
        posts(d, posts_x, flash=through and i % 4 < 2 and i < flight + 12)
        if not through:
            t = i / flight
            x = 4 + (posts_x - 4) * t
            y = 60 - 50 * math.sin(t * math.pi * 0.62)
            ball(d, x, y, round(9 - 5 * t), i * 1.3)
        fw.draw(d, i)
        s = pop_scale(i, flight)
        if s:
            headline(d, text, text_cx, 26, round(size * s))
            subtitle(d, text_cx)
        frames.append(img)
    return frames


def basics_gif(image_path):
    """Image on the left, 'DE BASICS!' typed out letter by letter on the right."""
    frames = []
    text = "DE BASICS!"
    box = 58
    text_cx = box + 4 + (W - box - 4) // 2
    size = fit_font(text, W - box - 10, 32)
    logo = load_logo(image_path, box)
    for i in range(46):
        img = Image.new("RGB", (W, H))
        d = ImageDraw.Draw(img)
        # logo slides in from the left
        x = min(3, -box + i * 12)
        bob = round(math.sin(i / 4) * 1.5) if i > 8 else 0
        img.paste(logo, (x, 3 + bob), logo)
        shown = max(0, min(len(text), (i - 6) // 2))
        if shown:
            f = font(FONT_BIG, size)
            full_w = d.textlength(text, font=f)
            left = text_cx - full_w / 2
            fill = YELLOW if i < 30 or i % 6 < 4 else (255, 240, 120)
            d.text((left, 26), text[:shown], font=f, fill=fill, anchor="lm", stroke_width=1, stroke_fill=ORANGE)
        if i >= 6 + 2 * len(text):
            subtitle(d, text_cx)
        frames.append(img)
    return frames


def load_logo(image_path, box):
    if image_path and Path(image_path).exists():
        src = Image.open(image_path).convert("RGBA")
        src.thumbnail((box, box), Image.LANCZOS)
        logo = Image.new("RGBA", (box, box), (0, 0, 0, 0))
        logo.paste(src, ((box - src.width) // 2, (box - src.height) // 2), src)
        return logo
    # placeholder until the real image is provided
    logo = Image.new("RGBA", (box, box), (0, 0, 0, 0))
    d = ImageDraw.Draw(logo)
    for k in range(0, box, 6):
        for a, b in (((k, 0), (k + 3, 0)), ((k, box - 1), (k + 3, box - 1)),
                     ((0, k), (0, k + 3)), ((box - 1, k), (box - 1, k + 3))):
            d.line((a, b), fill=GREEN)
    d.text((box / 2, box / 2), "IMG", font=font(FONT_SMALL, 12), fill=GREEN, anchor="mm")
    return logo


def save(frames, name):
    path = OUT / name
    frames[0].save(path, save_all=True, append_images=frames[1:], duration=FRAME_MS, loop=0, optimize=False)
    print("wrote", path)


if __name__ == "__main__":
    save(kick_gif("CONVERSION", 1), "brigandze_conversion_192x64.gif")
    save(kick_gif("PENALTY", 2), "brigandze_penalty_192x64.gif")
    save(basics_gif(sys.argv[1] if len(sys.argv) > 1 else BASICS_IMAGE), "brigandze_basics_192x64.gif")
