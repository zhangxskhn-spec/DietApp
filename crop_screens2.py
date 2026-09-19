
from PIL import Image
import os

base = r"C:\Users\zhx\AppData\Local\hermes\cache\screenshots"
out = r"D:\qimoruanj\DietApp\ppt_assets"
os.makedirs(out, exist_ok=True)

screens = {
    "dashboard": "browser_screenshot_7bd6b1a8673a4e9ab81089e757a7a829.png",
    "chat": "browser_screenshot_579e113e69cc41cda8e16ee74cd6ba91.png",
    "calendar": "browser_screenshot_307b4564f2bb4dab9d981b11fa40a092.png",
    "exercise": "browser_screenshot_de9fd4130efb4f17812244a1699aa3c8.png",
}

# 每个截图中，手机的位置取决于滚动位置
# 但更可靠的方法是：找到手机边框（深灰色 #333 圆角矩形）
# 手机尺寸：375x812 + 8px边框 = 391x828

for name, fname in screens.items():
    img = Image.open(os.path.join(base, fname))
    w, h = img.size
    print(f"\n{name}: {w}x{h}")
    
    # 找到深色边框区域（手机外框颜色约 #333333）
    # 扫描中间列的像素，找到从暗到亮的过渡
    mid_x = w // 2
    
    # 找到手机顶部（从上往下，找到第一个深色像素行）
    phone_top = 0
    for y in range(h):
        r, g, b = img.getpixel((mid_x, y))[:3]
        if r < 80 and g < 80 and b < 80:  # 深色边框
            phone_top = y
            break
    
    # 找到手机底部（从 phone_top 往下，找到深色结束的位置）
    phone_bottom = phone_top
    in_dark = True
    for y in range(phone_top + 10, h):
        r, g, b = img.getpixel((mid_x, y))[:3]
        if in_dark:
            if r > 100 or g > 100 or b > 100:  # 离开深色区域
                in_dark = False
                phone_inner_top = y
        else:
            if r < 80 and g < 80 and b < 80:  # 重新进入深色边框
                phone_bottom = y
                break
    
    # 找到手机左侧和右侧
    mid_y = (phone_top + phone_bottom) // 2
    phone_left = 0
    for x in range(w):
        r, g, b = img.getpixel((x, mid_y))[:3]
        if r < 80 and g < 80 and b < 80:
            phone_left = x
            break
    
    phone_right = phone_left
    for x in range(phone_left + 10, w):
        r, g, b = img.getpixel((x, mid_y))[:3]
        if r < 80 and g < 80 and b < 80:
            phone_right = x
            break
    
    print(f"  边框: top={phone_top}, bottom={phone_bottom}, left={phone_left}, right={phone_right}")
    print(f"  尺寸: {phone_right-phone_left}x{phone_bottom-phone_top}")
    
    # 裁剪：包含边框，稍作收缩
    margin = 4
    cropped = img.crop((phone_left + margin, phone_top + margin, phone_right - margin, phone_bottom - margin))
    
    outpath = os.path.join(out, f"{name}.png")
    cropped.save(outpath, "PNG")
    print(f"  -> {outpath} ({cropped.size[0]}x{cropped.size[1]})")

print("\n✅ 裁剪完成")
