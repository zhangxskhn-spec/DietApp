const pptxgen = require("pptxgenjs");
const fs = require("fs");
const path = require("path");

const pres = new pptxgen();
pres.layout = "LAYOUT_16x9";
pres.author = "张豪鑫";
pres.title = "轻食记 DietApp 项目介绍";

// ═══ 调色板 ═══
const C = {
  green: "2E7D32",
  greenLight: "4CAF50",
  greenBg: "E8F5E9",
  greenBg2: "C8E6C9",
  dark: "1B2E1B",
  warm: "F5F3ED",
  cardBg: "FFFDF8",
  orange: "E65100",
  teal: "009688",
  purple: "7E57C2",
  white: "FFFFFF",
  gray: "8A9E8A",
  grayLight: "F0F0F0",
};

const makeShadow = () => ({ type: "outer", color: "000000", blur: 6, offset: 2, angle: 135, opacity: 0.12 });

// 截图路径（本地生成的手机屏幕）
const shots = {
  dashboard: path.join(__dirname, "ppt_assets", "dashboard.png"),
  chat: path.join(__dirname, "ppt_assets", "chat.png"),
  calendar: path.join(__dirname, "ppt_assets", "calendar.png"),
  exercise: path.join(__dirname, "ppt_assets", "exercise.png"),
};

// ═══════════════════════════════════════
// Slide 1: 封面
// ═══════════════════════════════════════
let s1 = pres.addSlide();
s1.background = { color: C.green };

// 装饰圆
s1.addShape(pres.shapes.OVAL, { x: -1, y: -1, w: 4, h: 4, fill: { color: C.greenLight, transparency: 70 } });
s1.addShape(pres.shapes.OVAL, { x: 7, y: 3, w: 5, h: 5, fill: { color: C.greenLight, transparency: 80 } });

s1.addText("🥗", { x: 0, y: 0.8, w: 10, h: 1.2, fontSize: 60, align: "center", color: C.white });
s1.addText("轻食记 DietApp", { x: 0, y: 2.0, w: 10, h: 1, fontSize: 44, fontFace: "Arial Black", color: C.white, align: "center", bold: true });
s1.addText("智能饮食管理 Android 应用", { x: 0, y: 3.0, w: 10, h: 0.6, fontSize: 20, fontFace: "Arial", color: C.greenBg2, align: "center" });

// 分隔线
s1.addShape(pres.shapes.RECTANGLE, { x: 3.5, y: 3.8, w: 3, h: 0.04, fill: { color: C.white, transparency: 50 } });

s1.addText("Kotlin · Jetpack Compose · Material 3", { x: 0, y: 4.1, w: 10, h: 0.5, fontSize: 14, fontFace: "Arial", color: C.greenBg2, align: "center" });
s1.addText("张豪鑫  |  2026", { x: 0, y: 4.8, w: 10, h: 0.4, fontSize: 12, fontFace: "Arial", color: C.greenBg2, align: "center" });

// ═══════════════════════════════════════
// Slide 2: 项目概览
// ═══════════════════════════════════════
let s2 = pres.addSlide();
s2.background = { color: C.warm };

s2.addText("项目概览", { x: 0.8, y: 0.4, w: 8, h: 0.7, fontSize: 36, fontFace: "Arial Black", color: C.dark, bold: true });

// 左侧：功能卡片
const features = [
  { icon: "📊", title: "智能仪表盘", desc: "卡路里追踪 · 营养分析 · 趋势图表", color: C.green },
  { icon: "🤖", title: "AI 饮食助手", desc: "食物识别 · 智能建议 · 对话交互", color: C.teal },
  { icon: "📅", title: "饮食日历", desc: "早中晚餐记录 · 每日汇总 · 历史回顾", color: C.orange },
  { icon: "🏃", title: "运动健康", desc: "心率监测 · 血压血糖 · 运动消耗", color: C.purple },
  { icon: "👤", title: "个人中心", desc: "BMI管理 · 目标设定 · 成就系统", color: C.greenLight },
];

features.forEach((f, i) => {
  const y = 1.4 + i * 0.78;
  s2.addShape(pres.shapes.RECTANGLE, { x: 0.8, y, w: 4.8, h: 0.65, fill: { color: C.white }, shadow: makeShadow() });
  s2.addShape(pres.shapes.RECTANGLE, { x: 0.8, y, w: 0.06, h: 0.65, fill: { color: f.color } });
  s2.addText(f.icon, { x: 1.0, y, w: 0.5, h: 0.65, fontSize: 22, valign: "middle" });
  s2.addText(f.title, { x: 1.6, y, w: 2, h: 0.35, fontSize: 14, fontFace: "Arial", color: C.dark, bold: true, valign: "bottom", margin: 0 });
  s2.addText(f.desc, { x: 1.6, y: y + 0.3, w: 3.8, h: 0.3, fontSize: 10, fontFace: "Arial", color: C.gray, valign: "top", margin: 0 });
});

// 右侧：关键数据
const stats = [
  { num: "5", label: "核心模块" },
  { num: "20+", label: "功能页面" },
  { num: "1M", label: "AI 上下文" },
];

stats.forEach((st, i) => {
  const x = 6.5 + i * 1.2;
  s2.addShape(pres.shapes.RECTANGLE, { x, y: 1.4, w: 1.05, h: 1.2, fill: { color: C.white }, shadow: makeShadow() });
  s2.addText(st.num, { x, y: 1.5, w: 1.05, h: 0.6, fontSize: 28, fontFace: "Arial Black", color: C.green, align: "center", valign: "middle" });
  s2.addText(st.label, { x, y: 2.1, w: 1.05, h: 0.4, fontSize: 10, fontFace: "Arial", color: C.gray, align: "center", valign: "top" });
});

// 技术栈
s2.addShape(pres.shapes.RECTANGLE, { x: 6.0, y: 3.0, w: 3.6, h: 2.2, fill: { color: C.white }, shadow: makeShadow() });
s2.addShape(pres.shapes.RECTANGLE, { x: 6.0, y: 3.0, w: 3.6, h: 0.06, fill: { color: C.green } });
s2.addText("技术栈", { x: 6.2, y: 3.15, w: 3, h: 0.35, fontSize: 14, fontFace: "Arial", color: C.dark, bold: true, margin: 0 });

const techs = ["Kotlin + Jetpack Compose", "Material 3 Design", "Retrofit + OkHttp", "DeepSeek AI 接口", "百度食物识别 API", "MQTT 健康数据", "MPAndroidChart"];
s2.addText(
  techs.map((t, i) => ({ text: "▸ " + t, options: { breakLine: true, fontSize: 10, color: C.dark } })),
  { x: 6.2, y: 3.55, w: 3.2, h: 1.6, fontFace: "Arial", paraSpaceAfter: 2, margin: 0 }
);

// ═══════════════════════════════════════
// Slide 3: 仪表盘
// ═══════════════════════════════════════
let s3 = pres.addSlide();
s3.background = { color: C.warm };

s3.addText("📊 智能仪表盘", { x: 0.8, y: 0.4, w: 5, h: 0.7, fontSize: 32, fontFace: "Arial Black", color: C.dark, bold: true });

// 截图
s3.addImage({ path: shots.dashboard, x: 0.6, y: 1.3, w: 2.4, h: 4.0, sizing: { type: "contain", w: 2.4, h: 4.0 } });

// 右侧：功能说明
const dashFeatures = [
  { icon: "🔥", title: "卡路里追踪", desc: "实时计算每日摄入与消耗，进度条直观展示目标完成度" },
  { icon: "📈", title: "热量趋势", desc: "柱状图展示一周热量变化，超标自动橙色预警" },
  { icon: "🧬", title: "营养结构", desc: "蛋白质/碳水/脂肪三维度分析，药丸标签清晰呈现" },
  { icon: "💡", title: "智能建议", desc: "基于当日数据自动生成个性化饮食建议" },
];

dashFeatures.forEach((f, i) => {
  const y = 1.4 + i * 1.0;
  s3.addShape(pres.shapes.RECTANGLE, { x: 3.6, y, w: 5.8, h: 0.85, fill: { color: C.white }, shadow: makeShadow() });
  s3.addText(f.icon, { x: 3.8, y, w: 0.6, h: 0.85, fontSize: 24, valign: "middle" });
  s3.addText(f.title, { x: 4.5, y, w: 4.5, h: 0.4, fontSize: 14, fontFace: "Arial", color: C.dark, bold: true, valign: "bottom", margin: 0 });
  s3.addText(f.desc, { x: 4.5, y: y + 0.4, w: 4.7, h: 0.4, fontSize: 11, fontFace: "Arial", color: C.gray, valign: "top", margin: 0 });
});

// ═══════════════════════════════════════
// Slide 4: AI 聊天
// ═══════════════════════════════════════
let s4 = pres.addSlide();
s4.background = { color: C.warm };

s4.addText("🤖 AI 饮食助手", { x: 0.8, y: 0.4, w: 5, h: 0.7, fontSize: 32, fontFace: "Arial Black", color: C.dark, bold: true });

// 截图居中
s4.addImage({ path: shots.chat, x: 3.5, y: 1.2, w: 2.8, h: 4.2, sizing: { type: "contain", w: 2.8, h: 4.2 } });

// 左侧功能
const aiFeatures = [
  { icon: "📷", title: "拍照识别", desc: "百度 AI 视觉识别，拍一拍就知道吃了什么" },
  { icon: "💬", title: "智能对话", desc: "DeepSeek 大模型驱动，自然语言交互" },
  { icon: "📊", title: "营养分析", desc: "自动计算卡路里、蛋白质、碳水、脂肪" },
  { icon: "🍽", title: "一键保存", desc: "分析结果直接保存到饮食日记" },
];

aiFeatures.forEach((f, i) => {
  const y = 1.4 + i * 1.0;
  s4.addShape(pres.shapes.RECTANGLE, { x: 0.6, y, w: 2.6, h: 0.85, fill: { color: C.white }, shadow: makeShadow() });
  s4.addShape(pres.shapes.RECTANGLE, { x: 0.6, y, w: 0.06, h: 0.85, fill: { color: C.teal } });
  s4.addText(f.icon, { x: 0.8, y, w: 0.5, h: 0.85, fontSize: 22, valign: "middle" });
  s4.addText(f.title, { x: 1.3, y, w: 1.8, h: 0.4, fontSize: 13, fontFace: "Arial", color: C.dark, bold: true, valign: "bottom", margin: 0 });
  s4.addText(f.desc, { x: 1.3, y: y + 0.4, w: 1.8, h: 0.4, fontSize: 10, fontFace: "Arial", color: C.gray, valign: "top", margin: 0 });
});

// 右侧亮点
s4.addShape(pres.shapes.RECTANGLE, { x: 6.8, y: 1.4, w: 2.8, h: 3.8, fill: { color: C.white }, shadow: makeShadow() });
s4.addShape(pres.shapes.RECTANGLE, { x: 6.8, y: 1.4, w: 2.8, h: 0.06, fill: { color: C.teal } });
s4.addText("技术亮点", { x: 7.0, y: 1.55, w: 2.4, h: 0.35, fontSize: 14, fontFace: "Arial", color: C.dark, bold: true, margin: 0 });

const aiHighlights = [
  "MiMo V2.5 Pro 模型\n1M 上下文长度",
  "多轮对话记忆\n上下文连续追踪",
  "食物知识库\n本地营养数据匹配",
  "实时 API 调用\n毫秒级响应",
];

aiHighlights.forEach((h, i) => {
  const y = 2.1 + i * 0.8;
  s4.addShape(pres.shapes.OVAL, { x: 7.0, y: y + 0.05, w: 0.25, h: 0.25, fill: { color: C.teal } });
  s4.addText(String(i + 1), { x: 7.0, y: y + 0.05, w: 0.25, h: 0.25, fontSize: 10, fontFace: "Arial", color: C.white, align: "center", valign: "middle" });
  s4.addText(h.split("\n")[0], { x: 7.4, y, w: 2, h: 0.25, fontSize: 11, fontFace: "Arial", color: C.dark, bold: true, margin: 0 });
  s4.addText(h.split("\n")[1] || "", { x: 7.4, y: y + 0.25, w: 2, h: 0.25, fontSize: 9, fontFace: "Arial", color: C.gray, margin: 0 });
});

// ═══════════════════════════════════════
// Slide 5: 日历
// ═══════════════════════════════════════
let s5 = pres.addSlide();
s5.background = { color: C.warm };

s5.addText("📅 饮食日历", { x: 0.8, y: 0.4, w: 5, h: 0.7, fontSize: 32, fontFace: "Arial Black", color: C.dark, bold: true });

s5.addImage({ path: shots.calendar, x: 0.6, y: 1.3, w: 2.4, h: 4.0, sizing: { type: "contain", w: 2.4, h: 4.0 } });

// 右侧
const calFeatures = [
  { icon: "📆", title: "14天横向滚动", desc: "LazyRow 日期选择，滑动切换日期" },
  { icon: "☀️🌤🌙", title: "按餐次分类", desc: "早餐/午餐/晚餐/加餐，独立配色标识" },
  { icon: "🍽", title: "食物 emoji 映射", desc: "每种食物自动匹配 emoji 图标" },
  { icon: "📊", title: "每日营养汇总", desc: "进度条 + 药丸标签，一目了然" },
  { icon: "🗑", title: "滑动删除", desc: "左滑即可删除单条食物记录" },
];

calFeatures.forEach((f, i) => {
  const y = 1.3 + i * 0.82;
  s5.addShape(pres.shapes.RECTANGLE, { x: 3.6, y, w: 5.8, h: 0.7, fill: { color: C.white }, shadow: makeShadow() });
  s5.addText(f.icon, { x: 3.8, y, w: 0.6, h: 0.7, fontSize: 20, valign: "middle" });
  s5.addText(f.title, { x: 4.5, y, w: 2, h: 0.35, fontSize: 13, fontFace: "Arial", color: C.dark, bold: true, valign: "bottom", margin: 0 });
  s5.addText(f.desc, { x: 4.5, y: y + 0.33, w: 4.7, h: 0.3, fontSize: 10, fontFace: "Arial", color: C.gray, valign: "top", margin: 0 });
});

// ═══════════════════════════════════════
// Slide 6: 运动健康
// ═══════════════════════════════════════
let s6 = pres.addSlide();
s6.background = { color: C.warm };

s6.addText("🏃 运动健康监测", { x: 0.8, y: 0.4, w: 6, h: 0.7, fontSize: 32, fontFace: "Arial Black", color: C.dark, bold: true });

s6.addImage({ path: shots.exercise, x: 3.5, y: 1.2, w: 2.8, h: 4.2, sizing: { type: "contain", w: 2.8, h: 4.2 } });

// 左侧
const exFeatures = [
  { icon: "💓", title: "实时心率", desc: "Canvas 贝塞尔曲线 + 呼吸脉搏动画" },
  { icon: "🩸", title: "血压血糖", desc: "生命体征网格，2×2 卡片布局" },
  { icon: "👣", title: "步数统计", desc: "每日步数实时追踪" },
  { icon: "🔥", title: "运动消耗", desc: "卡路里消耗精确计算" },
];

exFeatures.forEach((f, i) => {
  const y = 1.4 + i * 1.0;
  s6.addShape(pres.shapes.RECTANGLE, { x: 0.6, y, w: 2.6, h: 0.85, fill: { color: C.white }, shadow: makeShadow() });
  s6.addShape(pres.shapes.RECTANGLE, { x: 0.6, y, w: 0.06, h: 0.85, fill: { color: C.purple } });
  s6.addText(f.icon, { x: 0.8, y, w: 0.5, h: 0.85, fontSize: 22, valign: "middle" });
  s6.addText(f.title, { x: 1.3, y, w: 1.8, h: 0.4, fontSize: 13, fontFace: "Arial", color: C.dark, bold: true, valign: "bottom", margin: 0 });
  s6.addText(f.desc, { x: 1.3, y: y + 0.4, w: 1.8, h: 0.4, fontSize: 10, fontFace: "Arial", color: C.gray, valign: "top", margin: 0 });
});

// 右下：动画亮点
s6.addShape(pres.shapes.RECTANGLE, { x: 6.8, y: 1.4, w: 2.8, h: 2.5, fill: { color: C.white }, shadow: makeShadow() });
s6.addShape(pres.shapes.RECTANGLE, { x: 6.8, y: 1.4, w: 2.8, h: 0.06, fill: { color: C.purple } });
s6.addText("动画效果", { x: 7.0, y: 1.55, w: 2.4, h: 0.35, fontSize: 14, fontFace: "Arial", color: C.dark, bold: true, margin: 0 });

const anims = ["心率数字 animateFloatAsState", "心形图标脉搏呼吸 0.88→1.18x", "Canvas 手绘心率贝塞尔曲线", "心率颜色动态：红/橙/青/绿"];
anims.forEach((a, i) => {
  const y = 2.1 + i * 0.4;
  s6.addShape(pres.shapes.OVAL, { x: 7.0, y: y + 0.05, w: 0.2, h: 0.2, fill: { color: C.purple } });
  s6.addText(a, { x: 7.35, y, w: 2.1, h: 0.35, fontSize: 10, fontFace: "Arial", color: C.dark, valign: "middle", margin: 0 });
});

// ═══════════════════════════════════════
// Slide 7: 设计系统
// ═══════════════════════════════════════
let s7 = pres.addSlide();
s7.background = { color: C.warm };

s7.addText("🎨 设计系统", { x: 0.8, y: 0.4, w: 5, h: 0.7, fontSize: 32, fontFace: "Arial Black", color: C.dark, bold: true });

// 色板
s7.addText("色彩体系", { x: 0.8, y: 1.2, w: 3, h: 0.4, fontSize: 16, fontFace: "Arial", color: C.dark, bold: true, margin: 0 });

const colors = [
  { hex: "4CAF50", name: "主色 Green" },
  { hex: "2E7D32", name: "深绿 Dark" },
  { hex: "C8E6C9", name: "浅绿 Light" },
  { hex: "FF9800", name: "橙色 碳水" },
  { hex: "009688", name: "青色 蛋白" },
  { hex: "7E57C2", name: "紫色 脂肪" },
  { hex: "F5F3ED", name: "暖米背景" },
  { hex: "FFFDF8", name: "卡片白" },
];

colors.forEach((c, i) => {
  const x = 0.8 + (i % 4) * 1.3;
  const y = 1.7 + Math.floor(i / 4) * 0.8;
  s7.addShape(pres.shapes.OVAL, { x, y, w: 0.5, h: 0.5, fill: { color: c.hex } });
  s7.addText(c.name, { x: x - 0.1, y: y + 0.55, w: 0.7, h: 0.25, fontSize: 8, fontFace: "Arial", color: C.dark, align: "center", margin: 0 });
});

// 设计规范
s7.addText("设计规范", { x: 5.5, y: 1.2, w: 4, h: 0.4, fontSize: 16, fontFace: "Arial", color: C.dark, bold: true, margin: 0 });

const specs = [
  { title: "圆角", value: "10/14/16/20/24/28dp 六级体系" },
  { title: "阴影", value: "4-10dp 外阴影，营造层次感" },
  { title: "文字", value: "三级：深绿 #1B2E1B → 灰绿 → 浅灰" },
  { title: "标签", value: "药丸形 999dp + 半透明背景" },
  { title: "Hero卡", value: "绿色渐变 + 28dp 大圆角" },
  { title: "动画", value: "FadeInBox 逐项入场，100ms 递增" },
];

specs.forEach((sp, i) => {
  const y = 1.7 + i * 0.58;
  s7.addShape(pres.shapes.RECTANGLE, { x: 5.5, y, w: 4, h: 0.48, fill: { color: C.white }, shadow: makeShadow() });
  s7.addText(sp.title, { x: 5.7, y, w: 0.9, h: 0.48, fontSize: 11, fontFace: "Arial", color: C.green, bold: true, valign: "middle", margin: 0 });
  s7.addText(sp.value, { x: 6.6, y, w: 2.8, h: 0.48, fontSize: 10, fontFace: "Arial", color: C.dark, valign: "middle", margin: 0 });
});

// ═══════════════════════════════════════
// Slide 8: 总结
// ═══════════════════════════════════════
let s8 = pres.addSlide();
s8.background = { color: C.green };

s8.addShape(pres.shapes.OVAL, { x: -1, y: -1, w: 4, h: 4, fill: { color: C.greenLight, transparency: 70 } });
s8.addShape(pres.shapes.OVAL, { x: 7, y: 3, w: 5, h: 5, fill: { color: C.greenLight, transparency: 80 } });

s8.addText("🥗", { x: 0, y: 1.0, w: 10, h: 1, fontSize: 50, align: "center", color: C.white });
s8.addText("轻食记 · 让健康饮食更简单", { x: 0, y: 2.2, w: 10, h: 0.8, fontSize: 32, fontFace: "Arial Black", color: C.white, align: "center", bold: true });

s8.addShape(pres.shapes.RECTANGLE, { x: 3.5, y: 3.2, w: 3, h: 0.04, fill: { color: C.white, transparency: 50 } });

const summaryItems = ["✅ 智能仪表盘，数据一目了然", "✅ AI 饮食助手，拍一拍就能分析", "✅ 饮食日历，每日营养全记录", "✅ 运动健康，实时监测身体状态", "✅ Material 3 设计，清新自然"];
s8.addText(
  summaryItems.map((t, i) => ({ text: t, options: { breakLine: true, fontSize: 14, color: C.greenBg2 } })),
  { x: 2.5, y: 3.5, w: 5, h: 2.0, fontFace: "Arial", paraSpaceAfter: 4, align: "center" }
);

// ═══ 保存 ═══
const outPath = "D:\\qimoruanj\\DietApp\\轻食记DietApp介绍v2.pptx";
pres.writeFile({ fileName: outPath }).then(() => {
  console.log("✅ PPT 已生成: " + outPath);
}).catch(err => {
  console.error("❌ 错误: " + err.message);
});
