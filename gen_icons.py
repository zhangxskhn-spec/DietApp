
from PIL import Image
import os

src = r"D:\xiazai\ChatGPT Image 2026年6月6日 23_57_22.png"
img = Image.open(src)
print(f"原图尺寸: {img.size}, 模式: {img.mode}")

# 转 RGBA（PNG 透明通道）
if img.mode != "RGBA":
    img = img.convert("RGBA")

# Android mipmap 尺寸
sizes = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

# Adaptive icon 前景层尺寸（108dp 对应各密度）
adaptive_sizes = {
    "mipmap-mdpi": 108,
    "mipmap-hdpi": 162,
    "mipmap-xhdpi": 216,
    "mipmap-xxhdpi": 324,
    "mipmap-xxxhdpi": 432,
}

res_dir = r"D:\qimoruanj\DietApp\app\src\main\res"

# 1. 普通图标（ic_launcher.png / ic_launcher_round.png）
for folder, size in sizes.items():
    out_dir = os.path.join(res_dir, folder)
    os.makedirs(out_dir, exist_ok=True)
    
    resized = img.resize((size, size), Image.LANCZOS)
    resized.save(os.path.join(out_dir, "ic_launcher.png"), "PNG")
    resized.save(os.path.join(out_dir, "ic_launcher_round.png"), "PNG")
    print(f"{folder}: {size}x{size} ✓")

# 2. Adaptive icon 前景层（ic_launcher_foreground.png）
fg_dir = os.path.join(res_dir, "drawable")
os.makedirs(fg_dir, exist_ok=True)

# 也生成各密度的前景 PNG（放在 mipmap 下供 adaptive icon 使用）
for folder, size in adaptive_sizes.items():
    out_dir = os.path.join(res_dir, folder)
    os.makedirs(out_dir, exist_ok=True)
    
    # 前景层：108dp 画布，图片居中放在安全区（72dp = 2/3 处）
    canvas = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    # 图片大小 = 72dp 对应的像素
    inner_size = int(size * 72 / 108)
    inner = img.resize((inner_size, inner_size), Image.LANCZOS)
    offset = (size - inner_size) // 2
    canvas.paste(inner, (offset, offset), inner)
    canvas.save(os.path.join(out_dir, "ic_launcher_foreground.png"), "PNG")
    print(f"{folder} foreground: {size}x{size} (inner {inner_size}) ✓")

print("\n✅ 所有图标 PNG 已生成！")
