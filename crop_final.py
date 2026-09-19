
from PIL import Image
import os

base = r"C:\Users\zhx\AppData\Local\hermes\cache\screenshots"
out = r"D:\qimoruanj\DietApp\ppt_assets"
os.makedirs(out, exist_ok=True)

screens = {
    "dashboard": "browser_screenshot_052fbbeb8c18436e8f564c4ba5bcff24.png",
    "chat": "browser_screenshot_e74c1c29374d4b1096149aac7d206a41.png",
    "calendar": "browser_screenshot_194478de8c1c49978db0b88e57334de3.png",
    "exercise": "browser_screenshot_67d23151718a411e86810b30c3576c8e.png",
}

for name, fname in screens.items():
    img = Image.open(os.path.join(base, fname))
    w, h = img.size
    print(f"{name}: {w}x{h}")
    
    # 手机在页面中心，扫描找到手机区域
    # 手机背景是 #888888，手机边框是 #333333，手机屏幕是暖白色 #F5F3ED
    # 找到手机边框（深色 #333 区域）
    
    # 方法：找中间列，从上到下找颜色变化
    mid_x = w // 2
    
    # 找边框顶部（#333 区域开始）
    rows = []
    for y in range(h):
        r, g, b = img.getpixel((mid_x, y))[:3]
        avg = (r + g + b) / 3
        rows.append(avg)
    
    # 边框颜色约 51 (#333)，背景约 136 (#888)，屏幕内容 > 200
    # 找到连续深色行（边框）
    border_top = None
    border_bottom = None
    in_border = False
    
    for y in range(len(rows)):
        if rows[y] < 80:  # 深色区域（边框）
            if not in_border:
                border_top = y
                in_border = True
        else:
            if in_border:
                border_bottom = y
                # 继续找下一个深色区域（底部边框）
    
    # 从底部往上找
    for y in range(len(rows) - 1, 0, -1):
        if rows[y] < 80:
            border_bottom = y
            break
    
    if border_top and border_bottom:
        # 找左右边框
        mid_y = (border_top + border_bottom) // 2
        cols = []
        for x in range(w):
            r, g, b = img.getpixel((x, mid_y))[:3]
            avg = (r + g + b) / 3
            cols.append(avg)
        
        border_left = None
        border_right = None
        for x in range(len(cols)):
            if cols[x] < 80:
                border_left = x
                break
        for x in range(len(cols) - 1, 0, -1):
            if cols[x] < 80:
                border_right = x
                break
        
        if border_left and border_right:
            print(f"  手机区域: ({border_left},{border_top}) -> ({border_right},{border_bottom})")
            print(f"  尺寸: {border_right-border_left}x{border_bottom-border_top}")
            
            # 裁剪，留一点边框
            pad = 6
            cropped = img.crop((border_left + pad, border_top + pad, border_right - pad, border_bottom - pad))
            
            outpath = os.path.join(out, f"{name}.png")
            cropped.save(outpath, "PNG")
            print(f"  -> {outpath} ({cropped.size[0]}x{cropped.size[1]})")
        else:
            print("  未找到左右边框")
    else:
        print("  未找到上下边框")

print("\n✅ 完成")
