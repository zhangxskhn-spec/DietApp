package com.dietapp.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 全局统一设计规范
 * 所有页面的圆角、间距、阴影都从这里取值
 */
object Ds {
    // ═══ 圆角 ═══
    val radiusSm = RoundedCornerShape(12.dp)     // 按钮、小卡片
    val radiusMd = RoundedCornerShape(16.dp)     // 普通卡片
    val radiusLg = RoundedCornerShape(20.dp)     // 大卡片、Hero
    val radiusXl = RoundedCornerShape(24.dp)     // 顶部标题栏
    val radiusPill = RoundedCornerShape(999.dp)  // 药丸形标签

    // ═══ 间距 ═══
    val spaceXs = 4.dp
    val spaceSm = 8.dp
    val spaceMd = 12.dp
    val spaceLg = 16.dp
    val spaceXl = 20.dp

    // ═══ 阴影 ═══
    val shadowNone = 0.dp
    val shadowSm = 3.dp     // 小元素
    val shadowMd = 6.dp     // 普通卡片
    val shadowLg = 10.dp    // Hero 卡片

    // ═══ 卡片内边距 ═══
    val cardPadding = 16.dp
    val cardPaddingSm = 12.dp

    // ═══ 字号 ═══
    val textXs = 11.sp
    val textSm = 13.sp
    val textMd = 15.sp
    val textLg = 18.sp
    val textXl = 24.sp
    val textHero = 48.sp
}
