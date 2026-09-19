
from PIL import Image, ImageDraw, ImageFont
import os
import math
import random

out = r"D:\qimoruanj\DietApp\ppt_assets"
os.makedirs(out, exist_ok=True)

W, H = 375, 812
BG = (245, 243, 237)
GREEN = (76, 175, 80)
GREEN_DARK = (46, 125, 50)
GREEN_LIGHT = (200, 230, 201)
GREEN_BG = (232, 245, 233)
WHITE = (255, 255, 255)
CARD = (255, 253, 248)
TEXT_P = (27, 46, 27)
TEXT_S = (84, 110, 84)
TEXT_T = (138, 158, 138)
ORANGE = (230, 81, 0)
TEAL = (0, 150, 136)
PURPLE = (126, 87, 194)

def rounded_rect(draw, xy, radius, fill):
    x0, y0, x1, y1 = xy
    r = min(radius, (x1-x0)//2, (y1-y0)//2)
    if r < 0: r = 0
    if x1 <= x0 or y1 <= y0: return
    draw.rectangle([x0+r, y0, x1-r, y1], fill=fill)
    draw.rectangle([x0, y0+r, x1, y1-r], fill=fill)
    draw.pieslice([x0, y0, x0+2*r, y0+2*r], 180, 270, fill=fill)
    draw.pieslice([x1-2*r, y0, x1, y0+2*r], 270, 360, fill=fill)
    draw.pieslice([x0, y1-2*r, x0+2*r, y1], 90, 180, fill=fill)
    draw.pieslice([x1-2*r, y1-2*r, x1, y1], 0, 90, fill=fill)

def draw_progress_bar(draw, x, y, w, h, pct, color):
    rounded_rect(draw, (x, y, x+w, y+h), h//2, (232, 245, 233))
    fw = max(h, int(w * pct))
    rounded_rect(draw, (x, y, x+fw, y+h), h//2, color)

def draw_statusbar(draw):
    draw.rectangle([0, 0, W, 44], fill=GREEN_DARK)

def draw_tabbar(draw, active=0):
    tabs = ["📊", "📅", "🤖", "🏃", "👤"]
    labels = ["仪表盘", "日历", "AI", "运动", "我的"]
    draw.rectangle([0, H-56, W, H], fill=WHITE)
    draw.line([(0, H-56), (W, H-56)], fill=(224, 224, 224), width=1)
    for i, (icon, label) in enumerate(zip(tabs, labels)):
        cx = W // 5 * i + W // 10
        color = GREEN if i == active else TEXT_T
        draw.text((cx-8, H-48), icon, fill=color)

# ═══════════════════════════════════════
# Dashboard
# ═══════════════════════════════════════
def draw_dashboard():
    img = Image.new("RGB", (W, H), BG)
    draw = ImageDraw.Draw(img)
    draw_statusbar(draw)
    
    # Hero card
    rounded_rect(draw, (12, 56, W-12, 200), 20, GREEN_BG)
    draw.text((24, 68), "今日摄入", fill=TEXT_S)
    draw.text((24, 90), "1,450", fill=GREEN_DARK)
    draw.text((180, 108), "kcal", fill=TEXT_T)
    draw_progress_bar(draw, 24, 145, W-48, 8, 0.6, GREEN)
    draw.text((24, 160), "已摄入 1,450", fill=TEXT_T)
    draw.text((W-120, 160), "目标 2,400", fill=TEXT_T)
    
    # 3 stat cards
    stats = [("950", "剩余 kcal", GREEN_DARK), ("320", "运动消耗", TEAL), ("78%", "蛋白质", ORANGE)]
    for i, (val, label, color) in enumerate(stats):
        x = 12 + i * (W-24)//3 + i*4
        cw = (W-28)//3
        rounded_rect(draw, (x, 210, x+cw, 290), 16, WHITE)
        draw.text((x+cw//2-20, 225), val, fill=color)
        draw.text((x+cw//2-20, 260), label, fill=TEXT_T)
    
    # Chart card
    rounded_rect(draw, (12, 300, W-12, 470), 16, WHITE)
    draw.text((24, 312), "热量趋势", fill=TEXT_S)
    # Bars
    bar_h = [60, 80, 45, 90, 70, 55, 75]
    bar_colors = [GREEN, GREEN_LIGHT, GREEN, ORANGE, GREEN, GREEN, GREEN]
    for i, (bh, bc) in enumerate(zip(bar_h, bar_colors)):
        bx = 35 + i * 44
        by = 450 - bh
        rounded_rect(draw, (bx, by, bx+24, 450), 4, bc)
    
    # Bottom cards
    rounded_rect(draw, (12, 480, W//2-4, 560), 16, WHITE)
    draw.text((24, 490), "营养结构", fill=TEXT_S)
    # Pills
    pills = [("蛋白68g", TEAL), ("碳水180g", ORANGE), ("脂肪45g", PURPLE)]
    px = 24
    for text, color in pills:
        tw = len(text) * 8 + 16
        rounded_rect(draw, (px, 515, px+tw, 535), 10, (color[0]//4+192, color[1]//4+192, color[2]//4+192))
        draw.text((px+8, 518), text, fill=color)
        px += tw + 4
    
    rounded_rect(draw, (W//2+4, 480, W-12, 560), 16, WHITE)
    draw.text((W//2+16, 490), "今日建议", fill=TEXT_S)
    draw.text((W//2+16, 515), "蛋白质摄入偏低", fill=TEXT_S)
    draw.text((W//2+16, 535), "建议晚餐增加鸡蛋", fill=TEXT_S)
    
    draw_tabbar(draw, 0)
    return img

# ═══════════════════════════════════════
# Chat
# ═══════════════════════════════════════
def draw_chat():
    img = Image.new("RGB", (W, H), BG)
    draw = ImageDraw.Draw(img)
    draw_statusbar(draw)
    
    # AI Header
    rounded_rect(draw, (12, 52, W-12, 110), 16, WHITE)
    # Avatar circle
    draw.ellipse((24, 58, 72, 106), fill=GREEN_BG)
    draw.text((38, 68), "🥗", fill=TEXT_P)
    draw.text((82, 62), "轻食记 AI", fill=TEXT_P)
    draw.text((82, 82), "你的专属饮食分析助手", fill=TEXT_T)
    # Camera button
    rounded_rect(draw, (W-120, 66, W-24, 90), 10, WHITE)
    draw.text((W-112, 70), "📷 拍照识别", fill=GREEN_DARK)
    
    # User message
    rounded_rect(draw, (60, 120, W-12, 160), 20, GREEN_BG)
    draw.text((72, 130), "我刚吃了一碗牛肉面 🍜", fill=TEXT_P)
    
    # AI response card
    rounded_rect(draw, (48, 170, W-60, 380), 20, GREEN_LIGHT)
    draw.text((60, 180), "根据分析，这碗牛肉面包含：", fill=TEXT_P)
    # Food card inside
    rounded_rect(draw, (60, 205, W-72, 365), 14, WHITE)
    draw.text((72, 215), "🍜 牛肉面", fill=TEXT_P)
    draw.text((72, 240), "份量 1.0碗", fill=TEXT_T)
    # Macro pills
    pills = [("碳水65g", ORANGE), ("蛋白28g", TEAL), ("脂肪15g", PURPLE)]
    px = 72
    for text, color in pills:
        tw = len(text) * 8 + 16
        rounded_rect(draw, (px, 265, px+tw, 285), 10, (min(255,color[0]//4+192), min(255,color[1]//4+192), min(255,color[2]//4+192)))
        draw.text((px+8, 268), text, fill=color)
        px += tw + 4
    draw.text((72, 300), "520", fill=ORANGE)
    draw.text((130, 310), "kcal", fill=TEXT_T)
    
    # User second message
    rounded_rect(draw, (80, 390, W-12, 420), 20, GREEN_BG)
    draw.text((92, 396), "那我还能吃什么？", fill=TEXT_P)
    
    # AI second response
    rounded_rect(draw, (48, 430, W-60, 530), 20, GREEN_LIGHT)
    draw.text((60, 440), "今天还剩约 880kcal 余量", fill=TEXT_P)
    draw.text((60, 465), "🍽 晚餐：糙米饭+清蒸鱼", fill=TEXT_P)
    draw.text((60, 485), "   +西兰花（约450kcal）", fill=TEXT_S)
    draw.text((60, 505), "🍪 加餐：苹果+坚果（200kcal）", fill=TEXT_P)
    
    # Action chips
    for i, emoji in enumerate(["🏃", "🍽", "📊"]):
        x = 48 + i * 52
        rounded_rect(draw, (x, 540, x+40, 580), 12, GREEN_BG)
        draw.text((x+10, 548), emoji, fill=TEXT_P)
    
    # Input bar
    rounded_rect(draw, (12, H-110, W-60, H-70), 20, WHITE)
    draw.text((24, H-100), "输入你刚刚吃了什么", fill=TEXT_T)
    draw.ellipse((W-52, H-106, W-20, H-74), fill=GREEN)
    draw.text((W-44, H-98), "➤", fill=WHITE)
    
    draw_tabbar(draw, 2)
    return img

# ═══════════════════════════════════════
# Calendar
# ═══════════════════════════════════════
def draw_calendar():
    img = Image.new("RGB", (W, H), BG)
    draw = ImageDraw.Draw(img)
    draw_statusbar(draw)
    
    draw.text((16, 56), "📅 饮食日历", fill=TEXT_P)
    
    # Date strip
    for i in range(8):
        x = 12 + i * 46
        is_active = (i == 5)
        bg = GREEN if is_active else WHITE
        rounded_rect(draw, (x, 86, x+42, 142), 12, bg)
        tc = WHITE if is_active else TEXT_T
        draw.text((x+14, 92), str(i+1), fill=tc)
        days = ["周一","周二","周三","周四","周五","周六","周日","周一"]
        draw.text((x+4, 115), days[i], fill=tc)
    
    # Date header
    draw.text((16, 152), "6月6日 星期六", fill=TEXT_P)
    
    # Summary pills
    rounded_rect(draw, (16, 180, 140, 204), 12, (255, 224, 178))
    draw.text((24, 184), "🔥 1,850 kcal", fill=ORANGE)
    rounded_rect(draw, (148, 180, 260, 204), 12, (200, 230, 201))
    draw.text((156, 184), "💪 蛋白 72g", fill=TEAL)
    
    # Meal cards
    meals = [
        ("☀️", "早餐", "400", [("🥚","鸡蛋灌饼","280"), ("🥣","黑米粥","120")]),
        ("🌤", "午餐", "790", [("🌶","麻辣香锅","480"), ("🍚","米饭","230"), ("🍵","银耳莲子汤","80")]),
        ("🌙", "晚餐", "660", [("🦀","清蒸大闸蟹","120"), ("🥬","蒜蓉粉丝娃娃菜","90"), ("🥣","小米粥","120")]),
    ]
    
    y = 215
    for emoji, title, total, foods in meals:
        card_h = 45 + len(foods) * 38
        rounded_rect(draw, (12, y, W-12, y+card_h), 16, WHITE)
        draw.text((24, y+10), emoji, fill=TEXT_P)
        draw.text((48, y+10), title, fill=TEXT_P)
        draw.text((W-80, y+10), f"{total}kcal", fill=ORANGE)
        fy = y + 38
        for femoji, fname, fcal in foods:
            draw.text((24, fy), femoji, fill=TEXT_P)
            draw.text((52, fy), fname, fill=TEXT_P)
            draw.text((W-70, fy), f"{fcal}kcal", fill=ORANGE)
            fy += 38
            draw.line([(24, fy-4), (W-24, fy-4)], fill=(240, 240, 240), width=1)
        y += card_h + 8
    
    draw_tabbar(draw, 1)
    return img

# ═══════════════════════════════════════
# Exercise
# ═══════════════════════════════════════
def draw_exercise():
    img = Image.new("RGB", (W, H), BG)
    draw = ImageDraw.Draw(img)
    draw_statusbar(draw)
    
    draw.text((16, 56), "🏃 运动健康", fill=TEXT_P)
    
    # Heart rate hero card
    rounded_rect(draw, (12, 86, W-12, 240), 20, GREEN_DARK)
    # Decorative circle
    draw.ellipse((W-80, 70, W+20, 170), fill=(56, 142, 60))
    draw.text((W//2-40, 100), "实时心率", fill=(255,255,255))
    draw.text((W//2-60, 125), "72", fill=WHITE)
    draw.text((W//2+20, 145), "bpm", fill=(200,230,201))
    draw.text((W//2-8, 175), "💓", fill=WHITE)
    draw.text((W//2-24, 200), "正常范围", fill=(200,230,201))
    
    # Heart rate curve card
    rounded_rect(draw, (12, 250, W-12, 360), 16, WHITE)
    draw.text((24, 260), "心率曲线", fill=TEXT_S)
    # SVG-like curve
    points = []
    for i in range(100):
        x = 24 + int(i * (W-48) / 100)
        y = 340 - int(30 * math.sin(i * 0.15) + 10 * math.sin(i * 0.3))
        points.append((x, y))
    for i in range(len(points)-1):
        draw.line([points[i], points[i+1]], fill=GREEN, width=2)
    
    # 4 metric cards
    metrics = [("血压", "120/80", "mmHg", TEAL), ("血糖", "5.6", "mmol/L", ORANGE),
               ("步数", "8,432", "步", GREEN), ("消耗", "320", "kcal", ORANGE)]
    for i, (label, val, unit, color) in enumerate(metrics):
        col = i % 2
        row = i // 2
        x = 12 + col * (W//2 - 8)
        y = 370 + row * 100
        cw = W//2 - 16
        rounded_rect(draw, (x, y, x+cw, y+90), 16, WHITE)
        draw.text((x+cw//2-16, y+12), label, fill=TEXT_T)
        draw.text((x+cw//2-24, y+35), val, fill=color)
        draw.text((x+cw//2-16, y+65), unit, fill=TEXT_T)
    
    draw_tabbar(draw, 3)
    return img

# Generate all
screens = {
    "dashboard": draw_dashboard,
    "chat": draw_chat,
    "calendar": draw_calendar,
    "exercise": draw_exercise,
}

for name, func in screens.items():
    img = func()
    # Add rounded corners mask
    mask = Image.new("L", (W, H), 0)
    mask_draw = ImageDraw.Draw(mask)
    rounded_rect(mask_draw, (0, 0, W, H), 40, 255)
    
    # Add border
    result = Image.new("RGB", (W+16, H+16), (51, 51, 51))
    result.paste(img, (8, 8))
    
    outpath = os.path.join(out, f"{name}.png")
    result.save(outpath, "PNG")
    print(f"✅ {name}: {outpath}")

print("\n全部完成！")
