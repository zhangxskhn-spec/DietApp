package com.dietapp.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.dietapp.data.UserPreferences
import com.dietapp.ui.theme.*

class ProfileFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                DietAppTheme {
                    ProfileContainer(
                        initialHeight = UserPreferences.getHeight(requireContext()).toInt(),
                        initialWeight = UserPreferences.getWeightKg(requireContext()).toInt(),
                        initialAge = UserPreferences.getAge(requireContext()),
                        initialGender = UserPreferences.getGender(requireContext()),
                        initialCalGoal = UserPreferences.getDailyCalGoal(requireContext())
                    ) { height, weight, age, gender, calGoal ->
                        UserPreferences.setHeight(requireContext(), height.toFloat())
                        UserPreferences.setWeight(requireContext(), weight.toDouble())
                        UserPreferences.setAge(requireContext(), age)
                        UserPreferences.setGender(requireContext(), gender)
                        UserPreferences.setDailyCalGoal(requireContext(), calGoal)
                        Toast.makeText(requireContext(), "设置已保存", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileContainer(
    initialHeight: Int, initialWeight: Int, initialAge: Int, initialGender: String, initialCalGoal: Int,
    onSave: (Int, Int, Int, String, Int) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var height by remember { mutableIntStateOf(initialHeight.coerceIn(140, 210)) }
    var weight by remember { mutableIntStateOf(initialWeight.coerceIn(35, 150)) }
    var age by remember { mutableIntStateOf(initialAge.coerceIn(10, 80)) }
    var gender by remember { mutableStateOf(initialGender.ifEmpty { "male" }) }
    var calGoal by remember { mutableIntStateOf(initialCalGoal.coerceIn(1000, 4000)) }

    AnimatedContent(
        targetState = isEditing,
        transitionSpec = { fadeIn(tween(300)) + slideInVertically(tween(300)) togetherWith fadeOut(tween(200)) + slideOutVertically(tween(200)) },
        label = "profileMode"
    ) { editing ->
        if (editing) {
            ProfileEditForm(height, weight, age, gender, calGoal,
                { height = it }, { weight = it }, { age = it }, { gender = it }, { calGoal = it },
                { onSave(height, weight, age, gender, calGoal); isEditing = false },
                { isEditing = false }
            )
        } else {
            ProfileDisplay(height, weight, age, gender, calGoal) { isEditing = true }
        }
    }
}

// ═══ 展示模式 ═══
@Composable
private fun ProfileDisplay(height: Int, weight: Int, age: Int, gender: String, calGoal: Int, onEditClick: () -> Unit) {
    val bmi = if (height > 0 && weight > 0) weight.toFloat() / ((height / 100f) * (height / 100f)) else 0f
    val bmiLabel = when { bmi < 18.5f -> "偏瘦"; bmi < 24f -> "正常"; bmi < 28f -> "偏胖"; else -> "肥胖" }
    val bmiColor = when { bmi < 18.5f -> Teal; bmi < 24f -> Green; bmi < 28f -> Orange; else -> Red }

    Column(modifier = Modifier.fillMaxSize().background(Background).verticalScroll(rememberScrollState())) {
        // ═══ Hero 头像区 ═══
        Box(modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(HeroStart, HeroEnd)))) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Icon(Icons.Rounded.Notifications, contentDescription = "通知", tint = TextSecondary, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.height(8.dp))
                // 头像居中
                Surface(modifier = Modifier.size(80.dp), shape = CircleShape, color = Green.copy(alpha = 0.12f), border = BorderStroke(2.5.dp, Green)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(if (gender == "male") "👨" else "👩", fontSize = 40.sp)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text("我的健康档案", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.height(4.dp))
                Text("${height}cm · ${weight}kg · ${age}岁", fontSize = 14.sp, color = TextSecondary)
                Spacer(Modifier.height(6.dp))
                Surface(shape = RoundedCornerShape(10.dp), color = GreenLight) {
                    Text("ID: ${System.currentTimeMillis() % 100000}", modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp), fontSize = 11.sp, color = GreenDark)
                }
            }
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(-16.dp))

            // ═══ 三宫格统计 ═══
            Card(shape = RoundedCornerShape(18.dp), elevation = CardDefaults.cardElevation(6.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatItem(value = "%.1f".format(bmi), label = "BMI", badge = bmiLabel, badgeColor = bmiColor)
                    StatItem(value = "$calGoal", suffix = "kcal", label = "每日目标", badge = "建议摄入", badgeColor = Green)
                    StatItem(value = bmiLabel, label = "BMI 状态", badge = "保持现有", badgeColor = bmiColor)
                }
            }

            Spacer(Modifier.height(14.dp))

            // ═══ 身体信息 ═══
            SectionCard(title = "身体信息", icon = Icons.Rounded.Person, onEdit = onEditClick) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    GridItem(icon = Icons.Rounded.Height, label = "身高", value = "${height} cm", iconColor = Green, bgColor = GreenLight)
                    GridItem(icon = Icons.Rounded.MonitorWeight, label = "体重", value = "${weight} kg", iconColor = Color(0xFF1976D2), bgColor = Color(0xFFE3F2FD))
                    GridItem(icon = Icons.Rounded.Cake, label = "年龄", value = "${age} 岁", iconColor = Orange, bgColor = Color(0xFFFFF3E0))
                    GridItem(icon = Icons.Rounded.PersonOutline, label = "性别", value = if (gender == "male") "男性" else "女性", iconColor = Purple, bgColor = Color(0xFFF3E5F5))
                }
            }

            Spacer(Modifier.height(12.dp))

            // ═══ 健康目标 ═══
            SectionCard(title = "健康目标", icon = Icons.Rounded.Flag, onEdit = onEditClick) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    BmiRing(bmi = bmi, color = bmiColor, label = bmiLabel)
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("每日热量", fontSize = 13.sp, color = TextSecondary)
                            Text("$calGoal kcal", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GreenDark)
                        }
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(progress = { (calGoal / 3000f).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)), color = Green, trackColor = GreenLight)
                        Spacer(Modifier.height(8.dp))
                        InfoLine(label = "BMI 状态", value = bmiLabel, valueColor = bmiColor)
                        InfoLine(label = "建议活动", value = if (bmi > 24) "增加运动" else "保持现有", valueColor = Green)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ═══ 成就徽章 ═══
            SectionCard(title = "成就徽章", icon = Icons.Rounded.EmojiEvents, onEdit = null, trailing = "全部") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    AchievementBadge(emoji = "🌱", label = "初次记录", date = "2024.04.20", color = GreenLight)
                    AchievementBadge(emoji = "🔥", label = "连续3天", date = "2024.04.23", color = Color(0xFFFFF3E0))
                    AchievementBadge(emoji = "💪", label = "运动达人", date = "2024.05.01", color = Color(0xFFE3F2FD))
                    AchievementBadge(emoji = "🏆", label = "七日坚持", date = "2024.05.07", color = Color(0xFFF3E5F5))
                }
            }

            Spacer(Modifier.height(14.dp))

            // ═══ 修改按钮 ═══
            Button(onClick = onEditClick, modifier = Modifier.fillMaxWidth().height(44.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Green)) {
                Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("修改信息", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

// ═══ 编辑模式 ═══
@Composable
private fun ProfileEditForm(
    height: Int, weight: Int, age: Int, gender: String, calGoal: Int,
    onHeightChange: (Int) -> Unit, onWeightChange: (Int) -> Unit, onAgeChange: (Int) -> Unit,
    onGenderChange: (String) -> Unit, onCalGoalChange: (Int) -> Unit,
    onSave: () -> Unit, onBack: () -> Unit
) {
    val bmi = if (height > 0 && weight > 0) weight.toFloat() / ((height / 100f) * (height / 100f)) else 0f
    val bmiProgress = ((bmi - 15f) / 25f).coerceIn(0f, 1f)
    val bmiColor = when { bmi < 18.5f -> Teal; bmi < 24f -> Green; bmi < 28f -> Orange; else -> Red }

    Column(modifier = Modifier.fillMaxSize().background(Background).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Surface(onClick = onBack, modifier = Modifier.size(36.dp), shape = CircleShape, color = CardBg) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.ArrowBack, contentDescription = null, modifier = Modifier.size(20.dp), tint = TextPrimary) }
            }
            Spacer(Modifier.width(12.dp))
            Text("修改信息", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        Spacer(Modifier.height(12.dp))

        Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent), elevation = CardDefaults.cardElevation(6.dp)) {
            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Brush.linearGradient(listOf(HeroStart, HeroEnd))).padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    BmiRing(bmi = bmi, progress = bmiProgress, color = bmiColor, label = when { bmi < 18.5f -> "偏瘦"; bmi < 24f -> "正常"; bmi < 28f -> "偏胖"; else -> "肥胖" })
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${height}cm · ${weight}kg", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("实时预览 BMI 变化", fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GenderCard(modifier = Modifier.weight(1f), emoji = "👨", label = "男性", selected = gender == "male") { onGenderChange("male") }
            GenderCard(modifier = Modifier.weight(1f), emoji = "👩", label = "女性", selected = gender == "female") { onGenderChange("female") }
        }
        Spacer(Modifier.height(12.dp))

        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(3.dp)) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("身体参数", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                StepperRow("身高", height, "cm", 140..210, onHeightChange)
                Spacer(Modifier.height(4.dp))
                StepperRow("体重", weight, "kg", 35..150, onWeightChange)
                Spacer(Modifier.height(4.dp))
                StepperRow("年龄", age, "岁", 10..80, onAgeChange)
            }
        }
        Spacer(Modifier.height(12.dp))

        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(3.dp)) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("每日热量目标", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                val presets = listOf(1200, 1500, 1800, 2000, 2500)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    presets.forEach { preset ->
                        Surface(onClick = { onCalGoalChange(preset) }, modifier = Modifier.weight(1f).height(34.dp), shape = RoundedCornerShape(10.dp), color = if (calGoal == preset) Green else Color.White, border = BorderStroke(1.dp, if (calGoal == preset) Green else Border)) {
                            Box(contentAlignment = Alignment.Center) { Text("$preset", fontSize = 11.sp, color = if (calGoal == preset) Color.White else TextSecondary, fontWeight = if (calGoal == preset) FontWeight.Bold else FontWeight.Normal) }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Surface(onClick = { onCalGoalChange((calGoal - 50).coerceIn(1000, 4000)) }, modifier = Modifier.size(30.dp), shape = CircleShape, color = GreenLight) { Box(contentAlignment = Alignment.Center) { Text("−", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GreenDark) } }
                    Spacer(Modifier.width(12.dp))
                    Surface(shape = RoundedCornerShape(8.dp), color = GreenBg) { Text("$calGoal kcal", modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GreenDark) }
                    Spacer(Modifier.width(12.dp))
                    Surface(onClick = { onCalGoalChange((calGoal + 50).coerceIn(1000, 4000)) }, modifier = Modifier.size(30.dp), shape = CircleShape, color = GreenLight) { Box(contentAlignment = Alignment.Center) { Text("+", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GreenDark) } }
                }
            }
        }
        Spacer(Modifier.height(14.dp))

        Button(onClick = onSave, modifier = Modifier.fillMaxWidth().height(44.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Green)) {
            Text("保存设置", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))
    }
}

// ═══ 通用组件 ═══
@Composable
private fun BmiRing(bmi: Float, color: Color, label: String, progress: Float = ((bmi - 15f) / 25f).coerceIn(0f, 1f)) {
    val anim by animateFloatAsState(targetValue = progress, animationSpec = tween(800))
    Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
            drawArc(color.copy(alpha = 0.15f), 135f, 270f, false, style = Stroke(8.dp.toPx(), cap = StrokeCap.Round))
            drawArc(color, 135f, 270f * anim, false, style = Stroke(8.dp.toPx(), cap = StrokeCap.Round))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("%.1f".format(bmi), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 8.sp, color = color)
        }
    }
}

@Composable
private fun StatItem(value: String, label: String, badge: String, badgeColor: Color, suffix: String = "") {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            if (suffix.isNotEmpty()) Text(suffix, fontSize = 11.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 3.dp))
        }
        Text(label, fontSize = 11.sp, color = TextSecondary)
        Spacer(Modifier.height(6.dp))
        Surface(shape = RoundedCornerShape(8.dp), color = badgeColor.copy(alpha = 0.12f)) {
            Text(badge, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontSize = 10.sp, color = badgeColor)
        }
    }
}

@Composable
private fun SectionCard(title: String, icon: ImageVector, onEdit: (() -> Unit)? = null, trailing: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = Green)
                Spacer(Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                Spacer(Modifier.weight(1f))
                if (trailing != null) {
                    Text(trailing, fontSize = 12.sp, color = TextSecondary)
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextTertiary)
                } else if (onEdit != null) {
                    Text("编辑", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.clickable { onEdit() })
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextTertiary)
                }
            }
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun GridItem(icon: ImageVector, label: String, value: String, iconColor: Color, bgColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = bgColor) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = iconColor)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(label, fontSize = 11.sp, color = TextSecondary)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}

@Composable
private fun InfoLine(label: String, value: String, valueColor: Color = TextPrimary) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 12.sp, color = TextSecondary)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = valueColor)
    }
}

@Composable
private fun AchievementBadge(emoji: String, label: String, date: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(modifier = Modifier.size(52.dp), shape = CircleShape, color = color) {
            Box(contentAlignment = Alignment.Center) { Text(emoji, fontSize = 24.sp) }
        }
        Spacer(Modifier.height(6.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(2.dp))
        Text(date, fontSize = 9.sp, color = TextTertiary)
    }
}

@Composable
private fun GenderCard(modifier: Modifier, emoji: String, label: String, selected: Boolean, onClick: () -> Unit) {
    Card(modifier = modifier.height(76.dp).clickable { onClick() }, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = if (selected) Green else Color.White), elevation = CardDefaults.cardElevation(if (selected) 6.dp else 2.dp), border = if (selected) null else BorderStroke(1.5.dp, Border)) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(emoji, fontSize = 26.sp)
            Spacer(Modifier.height(2.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (selected) Color.White else TextPrimary)
        }
    }
}

@Composable
private fun StepperRow(label: String, value: Int, unit: String, range: IntRange, onValueChange: (Int) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 13.sp, color = TextPrimary, modifier = Modifier.width(55.dp))
        Spacer(Modifier.weight(1f))
        Surface(onClick = { onValueChange((value - 1).coerceAtLeast(range.first)) }, modifier = Modifier.size(28.dp), shape = CircleShape, color = GreenLight) { Box(contentAlignment = Alignment.Center) { Text("−", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GreenDark) } }
        Spacer(Modifier.width(6.dp))
        Surface(shape = RoundedCornerShape(6.dp), color = GreenBg) { Text("$value $unit", modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GreenDark, textAlign = TextAlign.Center) }
        Spacer(Modifier.width(6.dp))
        Surface(onClick = { onValueChange((value + 1).coerceAtMost(range.last)) }, modifier = Modifier.size(28.dp), shape = CircleShape, color = GreenLight) { Box(contentAlignment = Alignment.Center) { Text("+", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GreenDark) } }
    }
}
