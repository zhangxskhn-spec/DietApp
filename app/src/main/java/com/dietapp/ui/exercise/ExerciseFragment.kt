package com.dietapp.ui.exercise

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MonitorHeart
import androidx.compose.material.icons.rounded.Podcasts
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.dietapp.ui.theme.Background
import com.dietapp.ui.theme.Border
import com.dietapp.ui.theme.CardBg
import com.dietapp.ui.theme.ChartGrid
import com.dietapp.ui.theme.DietAppTheme
import com.dietapp.ui.theme.FadeInBox
import com.dietapp.ui.theme.Green
import com.dietapp.ui.theme.GreenDark
import com.dietapp.ui.theme.GreenLight
import com.dietapp.ui.theme.HeroEnd
import com.dietapp.ui.theme.HeroStart
import com.dietapp.ui.theme.InsightSurface
import com.dietapp.ui.theme.Orange
import com.dietapp.ui.theme.OrangeDark
import com.dietapp.ui.theme.Red
import com.dietapp.ui.theme.Teal
import com.dietapp.ui.theme.TextPrimary
import com.dietapp.ui.theme.TextSecondary
import com.dietapp.ui.theme.TextTertiary
import kotlin.math.max
import kotlin.math.min

class ExerciseFragment : Fragment() {

    private val viewModel: HealthMonitorViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val uiState by viewModel.uiState.collectAsState()
                DietAppTheme {
                    HealthMonitorScreen(uiState = uiState)
                }
            }
        }
    }
}

@Composable
private fun HealthMonitorScreen(uiState: HealthMonitorUiState) {
    val accent = heartAccent(uiState.heartRate)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 14.dp,
            bottom = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            FadeInBox(delayMs = 0) {
                MonitorHeroCard(uiState = uiState, accent = accent)
            }
        }

        item {
            FadeInBox(delayMs = 120) {
                HeartRateChartCard(uiState = uiState, accent = accent)
            }
        }

        item {
            FadeInBox(delayMs = 220) {
                VitalGrid(uiState = uiState)
            }
        }

        item {
            FadeInBox(delayMs = 320) {
                MotionInsightCard(uiState = uiState)
            }
        }

        item {
            FadeInBox(delayMs = 420) {
                RecommendationCard(uiState = uiState)
            }
        }
    }
}

@Composable
private fun MonitorHeroCard(
    uiState: HealthMonitorUiState,
    accent: Color
) {
    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val heartRateAnimated by animateFloatAsState(
        targetValue = uiState.heartRate.toFloat(),
        animationSpec = tween(550),
        label = "heartRateAnimated"
    )

    Card(
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Brush.linearGradient(listOf(HeroStart, HeroEnd)))
                .padding(18.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LiveChip(text = uiState.connectionLabel)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = uiState.streamSource,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "实时生命体征",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = uiState.deviceName,
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = heartRateAnimated.toInt().toString(),
                                fontSize = 54.sp,
                                fontWeight = FontWeight.Bold,
                                color = accent
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "BPM",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        StatusPill(text = uiState.heartStatus, accent = accent)
                    }

                    Box(
                        modifier = Modifier.size(118.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size((86 * pulseScale).dp)
                                .clip(CircleShape)
                                .background(accent.copy(alpha = 0.16f))
                        )
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(CardBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Favorite,
                                contentDescription = "心率",
                                tint = accent,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    InlineStat(label = "压力指数", value = "${uiState.stressScore}")
                    InlineStat(label = "步频", value = "${uiState.cadence} spm")
                    InlineStat(label = "更新时间", value = uiState.lastUpdated)
                }
            }
        }
    }
}

@Composable
private fun HeartRateChartCard(
    uiState: HealthMonitorUiState,
    accent: Color
) {
    val values = uiState.heartRateSamples.map { it.bpm }
    val minValue = values.minOrNull() ?: uiState.heartRate
    val maxValue = values.maxOrNull() ?: uiState.heartRate

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "实时心率曲线",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = uiState.heartTrendLabel,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Min $minValue", color = TextTertiary, fontSize = 11.sp)
                    Text(text = "Max $maxValue", color = TextTertiary, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            HeartRateChart(
                samples = uiState.heartRateSamples,
                accent = accent
            )
        }
    }
}

@Composable
private fun HeartRateChart(
    samples: List<HeartRateSample>,
    accent: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFF8FBF8))
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (samples.isEmpty()) return@Canvas

            val values = samples.map { it.bpm }
            val minValue = max(50, (values.minOrNull() ?: 60) - 8)
            val maxValue = min(150, (values.maxOrNull() ?: 100) + 8)
            val innerWidth = size.width
            val innerHeight = size.height
            val stepX = innerWidth / (samples.size - 1).coerceAtLeast(1)

            repeat(4) { index ->
                val y = innerHeight * index / 3f
                drawLine(
                    color = ChartGrid.copy(alpha = 0.55f),
                    start = Offset(0f, y),
                    end = Offset(innerWidth, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val linePath = Path()
            val fillPath = Path()

            val points = samples.mapIndexed { index, sample ->
                val x = index * stepX
                val fraction = (sample.bpm - minValue).toFloat() / (maxValue - minValue).coerceAtLeast(1)
                val y = innerHeight - (fraction * innerHeight)
                Offset(x, y)
            }

            if (points.isNotEmpty()) {
                linePath.moveTo(points[0].x, points[0].y)
                fillPath.moveTo(points[0].x, innerHeight)
                fillPath.lineTo(points[0].x, points[0].y)

                for (i in 1 until points.size) {
                    val prev = points[i - 1]
                    val curr = points[i]
                    val cx = (prev.x + curr.x) / 2f
                    linePath.cubicTo(cx, prev.y, cx, curr.y, curr.x, curr.y)
                    fillPath.cubicTo(cx, prev.y, cx, curr.y, curr.x, curr.y)
                }

                fillPath.lineTo(points.last().x, innerHeight)
                fillPath.close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(accent.copy(alpha = 0.24f), Color.Transparent)
                ),
                style = Fill
            )
            drawPath(
                path = linePath,
                color = accent,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )

            val last = samples.last()
            val lastFraction = (last.bpm - minValue).toFloat() / (maxValue - minValue).coerceAtLeast(1)
            val lastPoint = Offset(innerWidth, innerHeight - (lastFraction * innerHeight))
            drawCircle(color = accent.copy(alpha = 0.20f), radius = 14.dp.toPx(), center = lastPoint)
            drawCircle(color = accent, radius = 5.dp.toPx(), center = lastPoint)
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "-30s", fontSize = 11.sp, color = TextTertiary)
            Text(text = "现在", fontSize = 11.sp, color = TextTertiary)
        }
    }
}

@Composable
private fun VitalGrid(uiState: HealthMonitorUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            VitalMetricCard(
                modifier = Modifier.weight(1f),
                title = "血压",
                value = "${uiState.bloodPressureSystolic}/${uiState.bloodPressureDiastolic}",
                unit = "mmHg",
                caption = "收缩压 / 舒张压",
                icon = Icons.Rounded.MonitorHeart,
                accent = Color(0xFFE07A5F)
            )
            VitalMetricCard(
                modifier = Modifier.weight(1f),
                title = "血糖",
                value = String.format("%.1f", uiState.bloodGlucose),
                unit = "mmol/L",
                caption = glucoseCaption(uiState.bloodGlucose),
                icon = Icons.Rounded.WaterDrop,
                accent = Teal
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            VitalMetricCard(
                modifier = Modifier.weight(1f),
                title = "步数",
                value = uiState.steps.toString(),
                unit = "steps",
                caption = uiState.activityLabel,
                icon = Icons.AutoMirrored.Rounded.DirectionsWalk,
                accent = GreenDark
            )
            VitalMetricCard(
                modifier = Modifier.weight(1f),
                title = "消耗",
                value = uiState.calories.toString(),
                unit = "kcal",
                caption = "今日累计消耗",
                icon = Icons.Rounded.LocalFireDepartment,
                accent = OrangeDark
            )
        }
    }
}

@Composable
private fun VitalMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    unit: String,
    caption: String,
    icon: ImageVector,
    accent: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = accent.copy(alpha = 0.14f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = title, color = TextSecondary, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = unit, color = TextTertiary, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = caption, color = TextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun MotionInsightCard(uiState: HealthMonitorUiState) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = CircleShape,
                    color = Teal.copy(alpha = 0.14f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Podcasts,
                            contentDescription = "状态",
                            tint = Teal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("运动状态", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimary)
                    Text("动态节奏与恢复建议", fontSize = 12.sp, color = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                InlineStat(label = "活动等级", value = uiState.activityLabel)
                InlineStat(label = "压力指数", value = "${uiState.stressScore}/100")
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = when {
                    uiState.heartRate >= 110 -> "当前心率处于较高区间，适合展示强度训练页面效果，建议保持短时观测并加入异常高亮反馈。"
                    uiState.heartRate >= 90 -> "当前状态适合表现轻运动过程，页面可以强化节奏感和呼吸感，让实时数据变化更有生命力。"
                    else -> "当前处于平稳或恢复状态，适合突出柔和的监测节律和低干扰动画，让页面显得专业而安静。"
                },
                fontSize = 13.sp,
                color = TextSecondary,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun RecommendationCard(uiState: HealthMonitorUiState) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = InsightSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "界面演示建议",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            SuggestionLine("心率卡作为视觉主轴，保证数值、状态和曲线同步联动。")
            SuggestionLine("血压、血糖、步数与消耗做成副卡，突出层级而不是抢主视觉。")
            SuggestionLine("后续接 MQTT 时保留这套 UI，不用重做，只替换数据来源即可。")
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "最后更新时间：${uiState.lastUpdated}",
                fontSize = 12.sp,
                color = TextTertiary
            )
        }
    }
}

@Composable
private fun SuggestionLine(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(Orange)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            color = TextSecondary,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun LiveChip(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Red.copy(alpha = 0.14f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Red)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = Red,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StatusPill(text: String, accent: Color) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = accent.copy(alpha = 0.14f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = accent,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun InlineStat(label: String, value: String) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(text = label, fontSize = 11.sp, color = TextTertiary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
    }
}

private fun heartAccent(heartRate: Int): Color = when {
    heartRate >= 112 -> Red
    heartRate >= 96 -> OrangeDark
    heartRate >= 72 -> Teal
    else -> GreenDark
}

private fun glucoseCaption(glucose: Float): String = when {
    glucose >= 7.8f -> "餐后偏高"
    glucose <= 4.9f -> "偏低注意补给"
    else -> "范围稳定"
}
