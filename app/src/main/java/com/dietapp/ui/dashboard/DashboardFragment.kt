package com.dietapp.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dietapp.data.FoodKnowledgeBase
import com.dietapp.data.UserPreferences
import com.dietapp.data.api.BodyTrend
import com.dietapp.data.api.DietRecord
import com.dietapp.data.repository.DietRepository
import com.dietapp.ui.theme.Background
import com.dietapp.ui.theme.Border
import com.dietapp.ui.theme.CardBg
import com.dietapp.ui.theme.ChartGrid
import com.dietapp.ui.theme.DietAppTheme
import com.dietapp.ui.theme.Divider
import com.dietapp.ui.theme.Green
import com.dietapp.ui.theme.GreenDark
import com.dietapp.ui.theme.GreenLight
import com.dietapp.ui.theme.HeroEnd
import com.dietapp.ui.theme.HeroStart
import com.dietapp.ui.theme.InsightSurface
import com.dietapp.ui.theme.Orange
import com.dietapp.ui.theme.OrangeDark
import com.dietapp.ui.theme.Purple
import com.dietapp.ui.theme.SuccessSurface
import com.dietapp.ui.theme.Teal
import com.dietapp.ui.theme.TextPrimary
import com.dietapp.ui.theme.TextSecondary
import com.dietapp.ui.theme.TextTertiary
import com.dietapp.ui.theme.FadeInBox
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class DashboardFragment : Fragment() {
    private val repository = DietRepository
    private lateinit var foodKnowledgeBase: FoodKnowledgeBase
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).apply {
        timeZone = TimeZone.getTimeZone("Asia/Shanghai")
    }
    private var selectedRange by mutableStateOf("today")
    private var calRecords by mutableStateOf<List<DietRecord>>(emptyList())
    private var bodyTrend by mutableStateOf<List<BodyTrend>>(emptyList())
    private var todayCal by mutableStateOf(0.0)
    private var todayBurn by mutableStateOf(0)
    private var dailyCalGoal by mutableStateOf(1800)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val composeView = ComposeView(requireContext()).apply {
            setContent {
                DietAppTheme {
                    DashboardScreen(
                        selectedRange = selectedRange,
                        onRangeChange = {
                            selectedRange = it
                            loadData()
                        },
                        todayCal = todayCal,
                        todayBurn = todayBurn,
                        records = calRecords,
                        bodyTrend = bodyTrend,
                        dailyCalGoal = dailyCalGoal
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
            record.copy(
                foods = enriched,
                totalProtein = enriched.sumOf { it.protein },
                totalCarbs = enriched.sumOf { it.carbs },
                totalFat = enriched.sumOf { it.fat }
            )
        }
    }

    private fun loadData() {
        lifecycleScope.launch {
            try {
                dailyCalGoal = UserPreferences.getDailyCalGoal(requireContext())
                val (start, end, today) = getDateRange()
                val dietResponse = try {
                    withContext(Dispatchers.IO) { repository.getDateRange(start, end) }
                } catch (_: Exception) {
                    null
                }
                val trend = try {
                    withContext(Dispatchers.IO) { repository.getBodyTrend() }
                } catch (_: Exception) {
                    emptyList()
                }
                val summary = try {
                    withContext(Dispatchers.IO) { repository.getSummary(today) }
                } catch (_: Exception) {
                    null
                }
                val exercises = try {
                    withContext(Dispatchers.IO) { repository.getExercises(today) }
                } catch (_: Exception) {
                    emptyList()
                }

                todayCal = summary?.intake?.totalCal ?: 0.0
                todayBurn = exercises.sumOf { it.caloriesBurned }
                calRecords = enrichFoods(dietResponse?.records ?: emptyList())
                bodyTrend = trend
            } catch (_: Exception) {
            }
        }
    }

    private fun getDateRange(): Triple<String, String, String> {
        val cal = Calendar.getInstance()
        val today = dateFormat.format(cal.time)
        val start = when (selectedRange) {
            "today" -> today
            "month" -> {
                cal.add(Calendar.MONTH, -1)
                dateFormat.format(cal.time)
            }

            else -> {
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                dateFormat.format(cal.time)
            }
        }
        return Triple(start, today, today)
    }
}

@Composable
fun DashboardScreen(
    selectedRange: String,
    onRangeChange: (String) -> Unit,
    todayCal: Double,
    todayBurn: Int,
    records: List<DietRecord>,
    bodyTrend: List<BodyTrend>,
    dailyCalGoal: Int = 1800
) {
    val goal = dailyCalGoal.toFloat()
    val scrollState = rememberScrollState()
    val netCal = todayCal - todayBurn
    val progress = (todayCal / goal).coerceIn(0.0, 1.0).toFloat()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            listOf("today" to "今日", "week" to "本周", "month" to "本月").forEach { (key, label) ->
                FilterChip(
                    selected = selectedRange == key,
                    onClick = { onRangeChange(key) },
                    label = { Text(text = label, fontSize = 13.sp) },
                    modifier = Modifier.padding(horizontal = 4.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = CardBg,
                        labelColor = TextSecondary,
                        selectedContainerColor = Green,
                        selectedLabelColor = ComposeColor.White
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedRange == key,
                        borderColor = Border,
                        selectedBorderColor = Green
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        FadeInBox(delayMs = 100) {
            CompactHeroCard(
                todayCal = todayCal,
                todayBurn = todayBurn,
                netCal = netCal,
                goal = goal.toDouble(),
                progress = progress
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        FadeInBox(delayMs = 250) {
            CompactMetricsRow(todayCal = todayCal, todayBurn = todayBurn, goal = goal)
        }

        Spacer(modifier = Modifier.height(10.dp))

        FadeInBox(delayMs = 400) {
            ChartSectionCard(title = "热量趋势", subtitle = "") {
                CalorieBarChart(records = records, goal = goal)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        FadeInBox(delayMs = 550) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    ChartSectionCard(title = "营养结构", subtitle = "") {
                        MacroPieChart(records = records)
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    CompactTipCard(netCal = netCal)
                }
            }
        }

        if (bodyTrend.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            FadeInBox(delayMs = 650) {
                ChartSectionCard(title = "体重趋势", subtitle = "") {
                    WeightLineChart(bodyTrend = bodyTrend)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        FadeInBox(delayMs = 750) {
            GoalSettingCard(goal = goal, todayCal = todayCal)
        }

        Spacer(modifier = Modifier.height(10.dp))
        FadeInBox(delayMs = 850) {
            WeeklyAchievementCard(records = records, goal = goal)
        }
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    suffix: String,
    accent: ComposeColor,
    container: ComposeColor
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = container),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = suffix, style = MaterialTheme.typography.labelMedium, color = TextTertiary)
            }
        }
    }
}

@Composable
private fun ChartSectionCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            if (subtitle.isNotEmpty()) {
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(modifier = Modifier.height(10.dp))
            } else {
                Spacer(modifier = Modifier.height(2.dp))
            }
            content()
        }
    }
}

@Composable
private fun CalorieBarChart(records: List<DietRecord>, goal: Float) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        factory = { ctx ->
            BarChart(ctx).apply {
                setNoDataText("还没有足够的饮食记录")
                setNoDataTextColor(TextSecondary.toArgb())
                description.isEnabled = false
                legend.isEnabled = false
                setScaleEnabled(false)
                setPinchZoom(false)
                setDrawGridBackground(false)
                setDrawBarShadow(false)
                setFitBars(true)
                setExtraOffsets(0f, 6f, 0f, 0f)
            }
        },
        update = { chart ->
            val dailyMap = records.groupBy { it.date }
            val sortedDates = dailyMap.keys.sorted()
            if (sortedDates.isEmpty()) {
                chart.clear()
                chart.invalidate()
                return@AndroidView
            }

            val entries = sortedDates.mapIndexed { index, date ->
                BarEntry(index.toFloat(), (dailyMap[date]?.sumOf { it.totalCal } ?: 0.0).toFloat())
            }
            val dataSet = BarDataSet(entries, "热量").apply {
                colors = entries.map { if (it.y > goal) Orange.toArgb() else Green.toArgb() }
                valueTextColor = TextSecondary.toArgb()
                valueTextSize = 10f
            }

            chart.data = BarData(dataSet).apply { barWidth = 0.56f }
            chart.axisLeft.apply {
                removeAllLimitLines()
                axisMinimum = 0f
                axisMaximum = (goal * 1.45f).coerceAtLeast(entries.maxOf { it.y } * 1.15f)
                textColor = TextSecondary.toArgb()
                textSize = 11f
                setDrawAxisLine(false)
                gridColor = ChartGrid.toArgb()
                setDrawGridLines(true)
                addLimitLine(
                    LimitLine(goal, "目标").apply {
                        lineColor = OrangeDark.toArgb()
                        lineWidth = 1.2f
                        enableDashedLine(10f, 8f, 0f)
                        textColor = OrangeDark.toArgb()
                        textSize = 11f
                    }
                )
            }
            chart.axisRight.isEnabled = false
            chart.xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(sortedDates.map { it.drop(5) })
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                textSize = 11f
                textColor = TextSecondary.toArgb()
                setDrawAxisLine(false)
                setDrawGridLines(false)
            }
            chart.animateY(500)
            chart.invalidate()
        }
    )
}

@Composable
private fun MacroPieChart(records: List<DietRecord>) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(232.dp),
        factory = { ctx ->
            PieChart(ctx).apply {
                setNoDataText("暂无营养数据")
                setNoDataTextColor(TextSecondary.toArgb())
                description.isEnabled = false
                setDrawEntryLabels(false)
                legend.apply {
                    isEnabled = true
                    textColor = TextSecondary.toArgb()
                    textSize = 11f
                    verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                    horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                }
                isRotationEnabled = false
                setTransparentCircleAlpha(0)
                holeRadius = 60f
                setHoleColor(CardBg.toArgb())
                centerText = "营养\n结构"
                setCenterTextColor(TextPrimary.toArgb())
                setCenterTextSize(15f)
            }
        },
        update = { chart ->
            val protein = records.sumOf { it.totalProtein }
            val carbs = records.sumOf { it.totalCarbs }
            val fat = records.sumOf { it.totalFat }
            val total = protein + carbs + fat
            if (total <= 0) {
                chart.clear()
                chart.invalidate()
                return@AndroidView
            }

            val entries = listOf(
                PieEntry(protein.toFloat(), "蛋白质"),
                PieEntry(carbs.toFloat(), "碳水"),
                PieEntry(fat.toFloat(), "脂肪")
            )
            chart.data = PieData(
                PieDataSet(entries, "").apply {
                    colors = listOf(Teal.toArgb(), Orange.toArgb(), Purple.toArgb())
                    valueTextColor = Color.WHITE
                    valueTextSize = 12f
                    sliceSpace = 3f
                }
            )
            chart.animateY(500)
            chart.invalidate()
        }
    )
}

@Composable
private fun WeightLineChart(bodyTrend: List<BodyTrend>) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp),
        factory = { ctx ->
            LineChart(ctx).apply {
                setNoDataText("暂无体重趋势")
                setNoDataTextColor(TextSecondary.toArgb())
                description.isEnabled = false
                legend.isEnabled = false
                setScaleEnabled(false)
                setPinchZoom(false)
                setDrawGridBackground(false)
                setExtraOffsets(0f, 6f, 0f, 0f)
            }
        },
        update = { chart ->
            if (bodyTrend.isEmpty()) {
                chart.clear()
                chart.invalidate()
                return@AndroidView
            }

            val entries = bodyTrend.mapIndexed { index, item ->
                Entry(index.toFloat(), item.weight.toFloat())
            }
            chart.data = LineData(
                LineDataSet(entries, "体重").apply {
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    color = Teal.toArgb()
                    setCircleColor(Teal.toArgb())
                    circleRadius = 4f
                    lineWidth = 3f
                    setDrawFilled(true)
                    fillColor = Teal.copy(alpha = 0.18f).toArgb()
                    valueTextSize = 10f
                    valueTextColor = TextSecondary.toArgb()
                }
            )
            chart.xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(bodyTrend.map { it.date.drop(5) })
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                textColor = TextSecondary.toArgb()
                textSize = 11f
                setDrawAxisLine(false)
                setDrawGridLines(false)
            }
            chart.axisLeft.apply {
                val weights = bodyTrend.map { it.weight }
                axisMinimum = ((weights.minOrNull() ?: 0.0) * 0.98).toFloat()
                axisMaximum = ((weights.maxOrNull() ?: 0.0) * 1.02).toFloat()
                textColor = TextSecondary.toArgb()
                textSize = 11f
                setDrawAxisLine(false)
                gridColor = ChartGrid.toArgb()
            }
            chart.axisRight.isEnabled = false
            chart.animateX(500)
            chart.invalidate()
        }
    )
}

@Composable
private fun CompactHeroCard(
    todayCal: Double,
    todayBurn: Int,
    netCal: Double,
    goal: Double,
    progress: Float
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        colors = CardDefaults.cardColors(containerColor = ComposeColor.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(HeroStart, HeroEnd)))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "今日摄入",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "${"%.0f".format(todayCal)} / ${"%.0f".format(goal)} kcal",
                        style = MaterialTheme.typography.titleMedium,
                        color = GreenDark,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = Green,
                    trackColor = ComposeColor.White.copy(alpha = 0.72f)
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CompactPill(label = "运动消耗", value = "$todayBurn kcal", accent = Teal)
                    CompactPill(label = "净摄入", value = "${"%.0f".format(netCal)} kcal", accent = OrangeDark)
                    val pct = ((todayCal / goal) * 100).coerceAtMost(999.0).toInt()
                    CompactPill(label = "完成度", value = "$pct%", accent = GreenDark)
                }
            }
        }
    }
}

@Composable
private fun CompactPill(label: String, value: String, accent: ComposeColor) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = ComposeColor.White.copy(alpha = 0.7f)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Text(text = label, fontSize = 10.sp, color = TextSecondary)
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = accent)
        }
    }
}

@Composable
private fun CompactMetricsRow(todayCal: Double, todayBurn: Int, goal: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricCard(
            modifier = Modifier.weight(1f),
            label = "剩余",
            value = "${"%.0f".format((goal - todayCal).coerceAtLeast(0.0))}",
            suffix = "kcal",
            accent = GreenDark,
            container = SuccessSurface
        )
        MetricCard(
            modifier = Modifier.weight(1f),
            label = "运动",
            value = "$todayBurn",
            suffix = "kcal",
            accent = Teal,
            container = ComposeColor(0xFFEAF7F5)
        )
        MetricCard(
            modifier = Modifier.weight(1f),
            label = "完成",
            value = "${((todayCal / goal) * 100).coerceAtMost(999.0).toInt()}",
            suffix = "%",
            accent = Orange,
            container = InsightSurface
        )
    }
}

@Composable
private fun CompactTipCard(netCal: Double) {
    val tip = when {
        netCal < 1200 -> "晚餐补点蛋白和蔬菜"
        netCal < 1800 -> "节奏不错，继续保持"
        else -> "晚上适合散步放松"
    }
    val dots = listOf("多喝水" to GreenDark, "优先蛋白质" to OrangeDark, "饭后走走" to Teal)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("今日建议", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
            Spacer(Modifier.height(6.dp))
            Text(tip, fontSize = 12.sp, color = TextSecondary)
            Spacer(Modifier.height(8.dp))
            dots.forEach { (label, color) ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
                    Spacer(Modifier.width(6.dp))
                    Text(label, fontSize = 11.sp, color = TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun GoalSettingCard(goal: Float, todayCal: Double) {
    val remaining = (goal - todayCal).coerceAtLeast(0.0)
    val pct = ((todayCal / goal) * 100).toInt().coerceAtMost(999)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("今日目标", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                Spacer(Modifier.weight(1f))
                Surface(shape = RoundedCornerShape(10.dp), color = GreenLight) {
                    Text(
                        text = "${goal.toInt()} kcal",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenDark
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                GoalStat(label = "已摄入", value = "${"%.0f".format(todayCal)}", unit = "kcal", color = Orange)
                GoalStat(label = "剩余", value = "${"%.0f".format(remaining)}", unit = "kcal", color = Green)
                GoalStat(label = "完成", value = "$pct", unit = "%", color = Teal)
            }
        }
    }
}

@Composable
private fun GoalStat(label: String, value: String, unit: String, color: ComposeColor) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        Text(unit, fontSize = 11.sp, color = TextTertiary)
        Spacer(Modifier.height(2.dp))
        Text(label, fontSize = 11.sp, color = TextSecondary)
    }
}

@Composable
private fun WeeklyAchievementCard(records: List<DietRecord>, goal: Float) {
    val dailyMap = records.groupBy { it.date }
    val last7Days = (0 until 7).map { daysAgo ->
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(cal.time)
        val dayCal = dailyMap[date]?.sumOf { it.totalCal } ?: 0.0
        val achieved = dayCal >= goal * 0.8
        Triple(date, dayCal, achieved)
    }.reversed()

    val achievedCount = last7Days.count { it.third }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("本周达标", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                Spacer(Modifier.weight(1f))
                Text(
                    text = "$achievedCount / 7 天",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (achievedCount >= 5) Green else Orange
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                last7Days.forEach { (date, dayCal, achieved) ->
                    val dayLabel = try {
                        val d = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).parse(date)
                        SimpleDateFormat("E", Locale.CHINA).format(d ?: Date())
                    } catch (_: Exception) { "?" }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = if (achieved) Green else if (dayCal > 0) Orange.copy(alpha = 0.2f) else Divider
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (achieved) "✓" else if (dayCal > 0) "○" else "·",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (achieved) ComposeColor.White else if (dayCal > 0) Orange else TextTertiary
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(dayLabel, fontSize = 10.sp, color = TextTertiary)
                    }
                }
            }
        }
    }
}
