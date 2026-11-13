"""Generates the 192x64 scoreboard gifs in the style of the TRY gif.

Run: python tools/gifs/make_gifs.py [path/to/basics-image]
The TRY gif itself is hand-made and not generated here; frog.png is the frog taken from it.
"""
import math
import random
import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageEnhance, ImageFilter, ImageFont

W, H = 192, 64
FRAME_MS = 60
FRAMES = 40
HERE = Path(__file__).resolve().parent
OUT = HERE.parents[1] / "src/main/resources/META-INF/resources"
FROG_IMAGE = HERE / "frog.png"
BASICS_IMAGE = HERE / "basics.png"

GREEN = (39, 189, 62)
LIGHT_GREEN = (75, 229, 80)
DARK_GREEN = (21, 109, 44)
WHITE = (255, 255, 255)
BALL = (150, 85, 35)
BALL_EDGE = (90, 45, 15)

# widest first: a long word falls back to a narrower cut so it stays tall on a 192px wide board
FONTS = [HERE / "fonts" / name for name in
         ("Barlow-BlackItalic.ttf", "BarlowSemiCondensed-BlackItalic.ttf", "BarlowCondensed-BlackItalic.ttf")]

# timeline, in frames
SLIDE_START = 3    # stripes start closing in
IMPACT = 12        # stripes are gone, the ring bursts out
TEXT_START = 13    # headline pops out
BAR_START = 16     # underline sweeps in
SPARK_START = 20

STRIPES = 8
STRIPE_PITCH = 7
STRIPE_TOP = 6
STRIPE_LEN = 46
STRIPE_SPEED = 7

TEXT_CY = 32
TEXT_MAX_H = 40
DEPTH = 3          # extrusion of the headline, down and to the right
BAR_TOP = 60
BAR_SPEED = 20
KICK_LEAD = 6      # extra frames up front that give the ball time to fly
GAP = 6            # between the mascot and the headline


def lerp(a, b, t):
    return a + (b - a) * t


def text_sprite(text, font):
    """White headline with a green outline on a dark green extrusion."""
    l, t, r, b = font.getbbox(text, stroke_width=1)
    img = Image.new("RGBA", (r - l + DEPTH, b - t + DEPTH), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    for k in range(DEPTH, 0, -1):
        d.text((k - l, k - t), text, font=font, fill=DARK_GREEN, stroke_width=1, stroke_fill=DARK_GREEN)
    d.text((-l, -t), text, font=font, fill=WHITE, stroke_width=1, stroke_fill=GREEN)
    return img


def fit_font(text, max_w, max_h):
    """The font and size that give the tallest headline inside the box."""
    best = None
    for path in FONTS:
        for size in range(72, 7, -1):
            f = ImageFont.truetype(str(path), size)
            l, t, r, b = f.getbbox(text, stroke_width=1)
            if r - l + DEPTH <= max_w and b - t + DEPTH <= max_h:
                if best is None or size > best[0]:
                    best = (size, f)
                break
    return best[1]


def scaled(sprite, scale):
    w, h = max(1, round(sprite.width * scale)), max(1, round(sprite.height * scale))
    return sprite if (w, h) == sprite.size else sprite.resize((w, h), Image.LANCZOS)


def paste_centered(img, sprite, cx, cy):
    img.paste(sprite, (round(cx - sprite.width / 2), round(cy - sprite.height / 2)), sprite)


def stripes(draw, i, gap=0):
    """Green stripes from the left and white ones from the right, closing in on the middle and skewing as they go.
    gap keeps that many pixels free on both sides of the middle."""
    if i >= IMPACT:
        return
    shift = max(0, (i - SLIDE_START) * STRIPE_SPEED + 1)
    for k in range(STRIPES):
        y = STRIPE_TOP + k * STRIPE_PITCH
        # lower stripes run ahead of the upper ones the further they get
        lead = shift + round(k * 1.4 * shift / 42)
        g0, g1 = lead, min(lead + STRIPE_LEN - 1, W // 2 - 1 - gap)
        w0, w1 = max(W - STRIPE_LEN - lead, W // 2 + gap), W - 1 - lead
        if g1 - g0 >= 8:
            draw.rectangle((g0, y, g1, y + 1), fill=GREEN)
            draw.point((g1 - 7, y - 1), fill=GREEN)
        if w1 - w0 >= 8:
            draw.rectangle((w0, y, w1, y + 1), fill=WHITE)
            draw.point((w0 + 7, y - 1), fill=WHITE)


def posts(draw, flash=False):
    """Rugby H-posts in the middle of the board, crossbar at y=38."""
    cx, c = W // 2, LIGHT_GREEN if flash else WHITE
    draw.rectangle((cx - 11, 6, cx - 10, H - 1), fill=c)
    draw.rectangle((cx + 10, 6, cx + 11, H - 1), fill=c)
    draw.rectangle((cx - 10, 38, cx + 10, 39), fill=c)


def ball(draw, t, spin):
    """Ball kicked from the bottom left over the crossbar, shrinking as it flies away; t runs from 0 to 1."""
    x = lerp(30, W / 2, t)
    y = lerp(60, 22, t) - 18 * math.sin(math.pi * t)
    w = max(2, round(lerp(10, 5, t)))
    h = max(2, round(w * (0.55 + 0.25 * abs(math.sin(spin)))))
    draw.ellipse((round(x - w / 2), round(y - h / 2), round(x + w / 2), round(y + h / 2)), fill=BALL, outline=BALL_EDGE)
    if w >= 5:
        draw.line((round(x - w / 4), round(y), round(x + w / 4), round(y)), fill=WHITE)


def ring(draw, i, mascot_cx, text_x):
    """Shockwave that bursts out of the mascot and travels into the headline."""
    age = i - IMPACT + 1
    if not 0 <= age <= 4:
        return
    rx, ry = [(4, 2), (10, 5), (15, 8), (20, 10), (25, 12)][age]
    cx = lerp(mascot_cx, text_x + 6, max(0, age - 2) / 3)
    draw.ellipse((round(cx - rx), TEXT_CY + 3 - ry, round(cx + rx), TEXT_CY + 3 + ry),
                 outline=LIGHT_GREEN)


def pop_scale(i):
    """0 before the headline starts, then overshoot and settle at 1."""
    t = i - TEXT_START
    if t < 0:
        return 0
    return [0.35, 0.8, 1.07, 1.03][t] if t < 4 else 1.0


class Sparks:
    """A few pixels that burst out of the headline and drift down."""

    def __init__(self, seed, x0, x1):
        rnd = random.Random(seed)
        ox, oy = lerp(x0, x1, 0.55), TEXT_CY - 4
        self.parts = [(ox, oy, rnd.uniform(-2.2, 2.6), rnd.uniform(-1.6, 1.4), rnd.randint(0, 5),
                       rnd.choice((LIGHT_GREEN, LIGHT_GREEN, GREEN, WHITE)), rnd.randint(1, 2)) for _ in range(16)]

    def draw(self, draw, i):
        for ox, oy, vx, vy, delay, color, size in self.parts:
            age = i - SPARK_START - delay
            if age < 0 or (age > 8 and (age + size) % 5 == 0):
                continue
            # fast at first, then slowing down and sinking
            travel = 6 * (1 - math.exp(-age / 5))
            x, y = round(ox + vx * travel), round(oy + vy * travel + age * 0.35)
            draw.rectangle((x, y, x + size - 1, y + size - 1), fill=color)


def celebration(text, mascot, seed, breathe=True, kick=False):
    """The TRY choreography: stripes close in while the mascot flies to the middle, then it jumps aside and the
    headline pops out of it, an underline sweeps in and sparks fly.
    With kick the ball is kicked over the posts instead of the mascot flying in, and the mascot pops up afterwards."""
    lead = KICK_LEAD if kick else 0
    rest_w = mascot.width
    rest_cx, rest_cy = 1 + rest_w / 2, 31
    text_x = round(1 + rest_w + GAP)
    sprite = text_sprite(text, fit_font(text, W - text_x - 2, TEXT_MAX_H))
    text_cx = text_x + sprite.width / 2
    sparks = Sparks(seed, text_x, text_x + sprite.width)
    frames = []
    for n in range(FRAMES + lead):
        i = n - lead
        img = Image.new("RGB", (W, H))
        d = ImageDraw.Draw(img)
        stripes(d, i, 15 if kick else 0)  # the posts stay clear

        if i >= BAR_START:
            d.rectangle((max(text_x - 2, W - (i - BAR_START + 1) * BAR_SPEED), BAR_TOP, W - 1, H - 1), fill=GREEN)

        # mascot: flies in from the left growing, hits the middle, then jumps to its resting place
        if i < TEXT_START and kick:
            scale = 0
            # the posts flash once the ball is over
            posts(d, flash=i >= IMPACT - 1 and i % 2 == 0)
            if i < IMPACT:
                ball(d, n / (lead + IMPACT - 1), n * 1.3)
        elif 1 <= i <= IMPACT:
            t = (i - 1) / (IMPACT - 1)
            scale, cx, cy = lerp(0.25, 0.87, t ** 0.8), lerp(6, W / 2, t), lerp(25, 31, t)
        elif i == TEXT_START:
            scale, cx, cy = (0.5, rest_cx, rest_cy) if kick else (0.85, W / 2 - 12, 31)
        elif i > TEXT_START:
            settle = min(1, (i - TEXT_START) / 2)
            cycle = math.sin((i - TEXT_START - 3) / 11 * 2 * math.pi)
            if breathe:
                scale, cx, cy = lerp(0.97, 0.94 + 0.06 * cycle, settle), rest_cx, rest_cy
            else:
                scale, cx, cy = 1, rest_cx, rest_cy + round(1.5 * cycle) * settle
        else:
            scale = 0
        if scale:
            paste_centered(img, scaled(mascot, scale), cx, cy)

        ring(d, i, W / 2 if i <= TEXT_START else rest_cx + rest_w / 2, text_x)

        s = pop_scale(i)
        if s:
            head = scaled(sprite, s)
            # grows out of the mascot: starts right of the middle, lands on its final spot
            land = min(1, (i - TEXT_START) / 2)
            cx = lerp(W / 2 + 14 + head.width / 2, text_cx, land)
            cx = min(cx, W - 1 - head.width / 2)
            paste_centered(img, head, cx, TEXT_CY)
        sparks.draw(d, i)
        frames.append(img)
    return frames


def load_frog():
    return Image.open(FROG_IMAGE).convert("RGBA")


def load_photo(image_path, box):
    """Photo scaled once to fit inside a 1px green frame, with extra contrast and sharpening for the LED board."""
    photo = Image.new("RGBA", (box, box), GREEN + (255,))
    if image_path and Path(image_path).exists():
        src = Image.open(image_path).convert("RGB")
        src.thumbnail((box - 2, box - 2), Image.LANCZOS)
        src = ImageEnhance.Contrast(src).enhance(1.35)
        src = ImageEnhance.Color(src).enhance(1.1)
        src = src.filter(ImageFilter.UnsharpMask(radius=1, percent=80, threshold=2))
        photo.paste(src, ((box - src.width) // 2, (box - src.height) // 2))
    else:
        # placeholder until the real image is provided
        ImageDraw.Draw(photo).rectangle((1, 1, box - 2, box - 2), fill=(0, 0, 0))
    return photo


def save(frames, name):
    path = OUT / name
    frames[0].save(path, save_all=True, append_images=frames[1:], duration=FRAME_MS, loop=0, optimize=False)
    print("wrote", path)


if __name__ == "__main__":
    frog = load_frog()
    # the longer the word, the smaller the frog, so the headline keeps its height
    save(celebration("CONVERSION!", scaled(frog, 0.7), 1, kick=True), "brigandze_conversion_192x64.gif")
    save(celebration("PENALTY!", scaled(frog, 0.85), 2, kick=True), "brigandze_penalty_192x64.gif")
    photo = load_photo(sys.argv[1] if len(sys.argv) > 1 else BASICS_IMAGE, 54)
    save(celebration("DE BASICS!", photo, 3, breathe=False), "brigandze_basics_192x64.gif")
