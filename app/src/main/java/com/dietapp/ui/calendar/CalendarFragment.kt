package com.dietapp.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dietapp.data.FoodData
import com.dietapp.data.FoodKnowledgeBase
import com.dietapp.data.UserPreferences
import com.dietapp.data.api.DietRecord
import com.dietapp.data.api.FoodItem
import com.dietapp.data.repository.DietRepository
import com.dietapp.ui.theme.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {
    private val repository = DietRepository
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).apply { timeZone = TimeZone.getTimeZone("Asia/Shanghai") }
    private val displayFormat = SimpleDateFormat("MM月dd日 EEEE", Locale.CHINA)
    private var dateList: List<String> = emptyList()
    private var allRecords by mutableStateOf<List<DietRecord>>(emptyList())
    private var selectedDate by mutableStateOf("")
    private lateinit var foodKnowledgeBase: FoodKnowledgeBase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        dateList = (0 until 14).map { daysAgo ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
            dateFormat.format(cal.time)
        }.reversed()
        selectedDate = dateFormat.format(Date())

        val composeView = ComposeView(requireContext()).apply {
            setContent {
                DietAppTheme {
                    CalendarScreen(
                        dateList = dateList,
                        selectedDate = selectedDate,
                        allRecords = allRecords,
                        onDateSelect = { selectedDate = it; loadData() },
                        onDeleteFood = { record, food -> deleteFood(record, food) }
                    )
                }
            }
        }
        return composeView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        foodKnowledgeBase = FoodKnowledgeBase(requireContext())
        loadData()
    }

    /**
     * 从知识库补全食物的蛋白/碳水/脂肪（修复旧数据为0的问题）
     */
    private fun enrichFoods(records: List<DietRecord>): List<DietRecord> {
        return records.map { record ->
            val enriched = record.foods.map { food ->
                if (food.protein == 0.0 && food.carbs == 0.0 && food.fat == 0.0) {
                    val knowledge = foodKnowledgeBase.search(food.name).firstOrNull()
                    if (knowledge != null) {
                        val grams = if (food.weightGrams > 0) food.weightGrams else food.amount * 100.0
                        val ratio = grams / 100.0
                        food.copy(protein = knowledge.protein * ratio, carbs = knowledge.carbs * ratio, fat = knowledge.fat * ratio)
                    } else food
                } else food
            }
            record.copy(foods = enriched)
        }
    }

    private fun loadData() {
        lifecycleScope.launch {
            try {
                val records = withContext(Dispatchers.IO) { repository.getDateRange(dateList.first(), dateList.last()).records }
                allRecords = enrichFoods(records)
            } catch (_: Exception) {}
        }
    }

    private fun deleteFood(record: DietRecord, food: FoodItem) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("确认删除")
            .setMessage("确定要删除「${food.name}」吗？")
            .setPositiveButton("删除") { _, _ ->
                lifecycleScope.launch {
                    try {
                        val foodIndex = record.foods.indexOf(food).takeIf { it >= 0 } ?: return@launch
                        withContext(Dispatchers.IO) { repository.deleteFoodItem(record.id, foodIndex) }
                        Toast.makeText(requireContext(), "已删除：${food.name}", Toast.LENGTH_SHORT).show()
                        loadData()
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "删除失败：${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
}

@Composable
fun CalendarScreen(
    dateList: List<String>,
    selectedDate: String,
    allRecords: List<DietRecord>,
    onDateSelect: (String) -> Unit,
    onDeleteFood: (DietRecord, FoodItem) -> Unit
) {
    val dayRecords = allRecords.filter { it.date == selectedDate }
    val goal = UserPreferences.getDailyCalGoal(LocalContext.current).toFloat()
    val todayCal = dayRecords.sumOf { it.totalCal }
    val dateListState = rememberLazyListState()

    LaunchedEffect(Unit) {
        if (dateList.isNotEmpty()) {
            dateListState.animateScrollToItem(dateList.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // ═══ 日期横条 ═══
        LazyRow(
            state = dateListState,
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBg)
                .padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(dateList) { date ->
                val dayNames = arrayOf("日", "一", "二", "三", "四", "五", "六")
                val cal = Calendar.getInstance().apply { time = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).parse(date) ?: Date() }
                val isToday = date == dateFormat()
                val isSelected = date == selectedDate

                Column(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) Green else CardBg)
                        .clickable { onDateSelect(date) }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isToday) "今天" else "${cal.get(Calendar.DAY_OF_MONTH)}",
                        fontSize = if (isToday) 13.sp else 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) ComposeColor.White else if (isToday) Green else TextPrimary
                    )
                    Text(
                        text = dayNames[cal.get(Calendar.DAY_OF_WEEK) - 1],
                        fontSize = 10.sp,
                        color = if (isSelected) ComposeColor.White.copy(alpha = 0.8f) else TextTertiary
                    )
                }
            }
        }

        HorizontalDivider(color = Divider)

        // ═══ 内容 ═══
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            // 日期标题
            Text(
                text = try { SimpleDateFormat("MM月dd日 EEEE", Locale.CHINA).format(SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).parse(selectedDate) ?: Date()) } catch (_: Exception) { selectedDate },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(Modifier.height(8.dp))

            // 每日营养总结卡片 — 从食物项累加（更可靠）
            val allFoods = dayRecords.flatMap { it.foods }
            val totalProtein = allFoods.sumOf { it.protein }
            val totalCarbs = allFoods.sumOf { it.carbs }
            val totalFat = allFoods.sumOf { it.fat }
            val totalFoodCount = allFoods.size

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("每日总结", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                        Spacer(Modifier.weight(1f))
                        Text("${totalFoodCount} 种食物", fontSize = 12.sp, color = TextSecondary)
                    }
                    Spacer(Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { (todayCal / goal).toFloat().coerceAtMost(1f) },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = if (todayCal <= goal) Green else Orange,
                        trackColor = Divider
                    )
                    Text(
                        text = "热量 ${"%.0f".format(todayCal)} / ${goal.toInt()} kcal · 达标 ${((todayCal / goal * 100).toInt()).coerceAtMost(999)}%",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 6.dp)
                    )

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        NutrientPill(label = "蛋白质", value = "${"%.1f".format(totalProtein)}g", color = Teal)
                        NutrientPill(label = "碳水", value = "${"%.1f".format(totalCarbs)}g", color = Orange)
                        NutrientPill(label = "脂肪", value = "${"%.1f".format(totalFat)}g", color = Purple)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // 热量进度
            LinearProgressIndicator(
                progress = { (todayCal / goal).toFloat().coerceAtMost(1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = if (todayCal <= goal) Green else Orange,
                trackColor = Divider
            )
            Text(
                text = "今日摄入 ${"%.0f".format(todayCal)} / ${goal}kcal",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(16.dp))

            if (dayRecords.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                    Text("📭 该日暂无饮食记录", fontSize = 14.sp, color = TextTertiary)
                }
            } else {
                dayRecords.forEachIndexed { index, record ->
                    MealCard(record = record, onDeleteFood = onDeleteFood, index = index)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun MealCard(record: DietRecord, onDeleteFood: (DietRecord, FoodItem) -> Unit, index: Int = 0) {
    val mealEmoji = when (record.mealType) {
        "breakfast" -> "☀️"; "lunch" -> "🌤"; "dinner" -> "🌙"; else -> "🍪"
    }
    val mealColor = when (record.mealType) {
        "breakfast" -> Orange
        "lunch" -> Green
        "dinner" -> Teal
        else -> Purple
    }
    val mealBg = when (record.mealType) {
        "breakfast" -> ComposeColor(0xFFE8F5E9)
        "lunch" -> ComposeColor(0xFFE0F7FA)
        "dinner" -> ComposeColor(0xFFE8F5E9)
        else -> ComposeColor(0xFFF3E5F5)
    }

    FadeInBox(delayMs = index * 120L) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = mealBg),
            elevation = CardDefaults.cardElevation(5.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = mealColor.copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(mealEmoji, fontSize = 18.sp)
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = when (record.mealType) {
                            "breakfast" -> "早餐"; "lunch" -> "午餐"; "dinner" -> "晚餐"; else -> "加餐"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Spacer(Modifier.weight(1f))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = mealColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${"%.0f".format(record.totalCal)} kcal",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = mealColor
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                record.foods.forEach { food ->
                    val foodEmoji = foodEmojiFor(food.name)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(28.dp),
                            shape = CircleShape,
                            color = GreenLight
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(foodEmoji, fontSize = 14.sp)
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(food.name, modifier = Modifier.weight(1f), fontSize = 14.sp, color = TextPrimary)
                        Text("x${"%.1f".format(food.amount)}${food.unit}", fontSize = 11.sp, color = TextSecondary)
                        Text(
                            "${food.cal.toInt()} kcal",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = OrangeDark,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                        TextButton(
                            onClick = { onDeleteFood(record, food) },
                            contentPadding = PaddingValues(2.dp),
                            modifier = Modifier.size(24.dp)
                        ) { Text("✕", fontSize = 11.sp, color = Red) }
                    }
                }
            }
        }
    }
}

@Composable
private fun NutrientPill(label: String, value: String, color: ComposeColor) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(6.dp))
            Text(label, fontSize = 11.sp, color = TextSecondary)
            Spacer(Modifier.width(4.dp))
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

private fun foodEmojiFor(name: String): String {
    // 优先精确匹配 FoodData
    FoodData.findByName(name)?.let { return it.emoji }
    // 兜底：部分匹配
    return when {
    name.contains("吐司") || name.contains("面包") || name.contains("馒头") || name.contains("饼") -> "🍞"
    name.contains("蛋") && !name.contains("鸡蛋") -> "🍳"
    name.contains("鸡蛋") -> "🥚"
    name.contains("番茄") || name.contains("西红柿") -> "🍅"
    name.contains("黄瓜") -> "🥒"
    name.contains("牛奶") || name.contains("奶") || name.contains("酸奶") -> "🥛"
    name.contains("可乐") || name.contains("饮料") || name.contains("果汁") || name.contains("茶") -> "🥤"
    name.contains("鸡") && !name.contains("鸡蛋") -> "🍗"
    name.contains("鱼") || name.contains("三文鱼") || name.contains("鲈鱼") -> "🐟"
    name.contains("豆腐") || name.contains("豆") -> "🫘"
    name.contains("豆角") || name.contains("蔬菜") || name.contains("青菜") || name.contains("白菜") || name.contains("生菜") || name.contains("菠菜") || name.contains("韭菜") -> "🥬"
    name.contains("苹果") -> "🍎"
    name.contains("香蕉") -> "🍌"
    name.contains("橙") || name.contains("橘") || name.contains("柑") -> "🍊"
    name.contains("葡萄") -> "🍇"
    name.contains("西瓜") -> "🍉"
    name.contains("草莓") -> "🍓"
    name.contains("水果") || name.contains("果") -> "🥝"
    name.contains("沙拉") -> "🥗"
    name.contains("面") || name.contains("粉") -> "🍜"
    name.contains("肉") && !name.contains("鸡") -> "🥩"
    name.contains("虾") -> "🦐"
    name.contains("坚果") || name.contains("核桃") || name.contains("花生") -> "🥜"
    name.contains("巧克力") -> "🍫"
    name.contains("蛋糕") || name.contains("甜") || name.contains("奶茶") -> "🍰"
    name.contains("米饭") || name.contains("饭") -> "🍚"
    name.contains("粥") -> "🥣"
    name.contains("饺子") || name.contains("包子") -> "🥟"
    name.contains("薯") || name.contains("土豆") -> "🥔"
    name.contains("玉米") -> "🌽"
    name.contains("蘑菇") || name.contains("菇") -> "🍄"
    name.contains("辣椒") -> "🌶"
    name.contains("洋葱") -> "🧅"
    name.contains("芝士") || name.contains("奶酪") -> "🧀"
    name.contains("培根") || name.contains("火腿") -> "🥓"
    name.contains("橘") || name.contains("桔") || name.contains("橙") -> "🍊"
    name.contains("辣条") || name.contains("零食") || name.contains("薯片") -> "🍿"
    name.contains("冬瓜") || name.contains("南瓜") || name.contains("丝瓜") -> "🎃"
    else -> "🥗"
    }
}

// 缓存 SimpleDateFormat，避免 Composable 每次重组时创建新实例
private val sharedDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
private fun dateFormat(): String = sharedDateFormat.format(Date())
