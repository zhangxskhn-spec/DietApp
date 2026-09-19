
from PIL import Image
import os

base = r"C:\Users\zhx\AppData\Local\hermes\cache\screenshots"
out = r"D:\qimoruanj\DietApp\ppt_assets"
os.makedirs(out, exist_ok=True)

# 截图文件
screens = {
    "dashboard": "browser_screenshot_7bd6b1a8673a4e9ab81089e757a7a829.png",
    "chat": "browser_screenshot_579e113e69cc41cda8e16ee74cd6ba91.png",
    "calendar": "browser_screenshot_307b4564f2bb4dab9d981b11fa40a092.png",
    "exercise": "browser_screenshot_de9fd4130efb4f17812244a1699aa3c8.png",
}

for name, fname in screens.items():
    img = Image.open(os.path.join(base, fname))
    w, h = img.size
    print(f"{name}: {w}x{h}")
    
    # 手机屏幕在截图中的大致位置（居中，带圆角边框）
    # 需要根据实际截图裁剪
    # 手机宽度约 375px，加上边框约 400px
    # 在 1920 宽的截图中，居中位置约 (760, 0, 1160, 812)
    
    # 自动查找手机区域：找到非灰色背景的区域
    # 简单方案：裁剪截图中心偏上的区域
    phone_w = 420  # 手机+边框宽度
    phone_h = 870  # 手机+边框高度
    
    cx = w // 2
    left = cx - phone_w // 2
    top = 20  # 从顶部开始
    right = left + phone_w
    bottom = top + phone_h
    
    if bottom > h:
        bottom = h
        top = bottom - phone_h
    
    cropped = img.crop((left, top, right, bottom))
    
    # 裁剪掉外边框，只保留手机屏幕
    # 再裁剪掉大约 8px 的边框
    margin = 10
    inner = cropped.crop((margin, margin, phone_w - margin, phone_h - margin))
    
    outpath = os.path.join(out, f"{name}.png")
    inner.save(outpath, "PNG")
    print(f"  -> saved {outpath} ({inner.size[0]}x{inner.size[1]})")

print("\n✅ 裁剪完成")
