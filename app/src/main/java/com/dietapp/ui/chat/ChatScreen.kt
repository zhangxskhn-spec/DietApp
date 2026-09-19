package com.dietapp.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dietapp.R
import com.dietapp.data.api.FoodItem
import kotlin.math.*
import com.dietapp.ui.theme.Background
import com.dietapp.ui.theme.Border
import com.dietapp.ui.theme.CardBg
import com.dietapp.ui.theme.Divider
import com.dietapp.ui.theme.GlassGreen
import com.dietapp.ui.theme.Green
import com.dietapp.ui.theme.GreenBg
import com.dietapp.ui.theme.GreenDark
import com.dietapp.ui.theme.GreenLight
import com.dietapp.ui.theme.InsightSurface
import com.dietapp.ui.theme.OrangeDark
import com.dietapp.ui.theme.Purple
import com.dietapp.ui.theme.Red
import com.dietapp.ui.theme.SpinachGreen
import com.dietapp.ui.theme.Teal
import com.dietapp.ui.theme.TealDark
import com.dietapp.ui.theme.TextPrimary
import com.dietapp.ui.theme.TextSecondary
import com.dietapp.ui.theme.TextTertiary
import com.dietapp.ui.theme.pressScale

@Composable
fun ChatScreen(
    messages: List<ChatMessage>,
    onSendText: (String) -> Unit,
    onCameraClick: () -> Unit,
    onFoodPickerClick: () -> Unit,
    onSaveClick: (ChatMessage, Int, String) -> Unit,
    onEditClick: (ChatMessage) -> Unit,
    onActionClick: (String, ChatMessage) -> Unit,
    onEditFoodItem: (ChatMessage, Int, Int) -> Unit,
    imagePath: String? = null,
    onRemoveImage: () -> Unit = {},
) {
    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        CoachHeader(onCameraClick = onCameraClick)

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(messages, key = { _, msg -> msg.hashCode() }) { index, msg ->
                Column {
                    when {
                        msg.role == "user" -> UserMessageBubble(msg.content, msg.imagePath, msg.timestamp)
                        msg.isFoodAnalysis && msg.pendingSaveFoods.isNotEmpty() -> {
                            AiFoodResultCard(
                                msg = msg,
                                position = index,
                                onSaveClick = onSaveClick,
                                onEditClick = onEditClick,
                                onActionClick = onActionClick,
                                onEditFoodItem = onEditFoodItem
                            )
                        }
                        else -> AiTextBubble(msg.content)
                    }
                    // Action chips — 动画滑入，气泡下方
                    if (msg.isFoodAnalysis && msg.pendingSaveFoods.isNotEmpty() && msg.showActions) {
                        ActionChipsRow(onActionClick = { action -> onActionClick(action, msg) })
                    }
                }
            }
        }

        if (imagePath != null) {
            ImagePreviewBar(imagePath = imagePath, onRemove = onRemoveImage)
        }

        ChatInputBar(
            inputText = inputText,
            onInputChange = { inputText = it },
            onSend = {
                val value = inputText.trim()
                if (value.isNotEmpty() || imagePath != null) {
                    onSendText(value)
                    inputText = ""
                }
            },
            onCameraClick = onCameraClick,
            onFoodPickerClick = onFoodPickerClick
        )
    }
}

@Composable
private fun CoachHeader(onCameraClick: () -> Unit = {}) {
    val transition = rememberInfiniteTransition(label = "headerPulse")

    // ── 呼吸动画参数 ──
    val ringOuterAlpha by transition.animateFloat(
        initialValue = 0.0f, targetValue = 0.55f,
        animationSpec = infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ringOuterAlpha"
    )
    val ringMidAlpha by transition.animateFloat(
        initialValue = 0.05f, targetValue = 0.75f,
        animationSpec = infiniteRepeatable(tween(1800, delayMillis = 300, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ringMidAlpha"
    )
    val coreScale by transition.animateFloat(
        initialValue = 0.96f, targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "coreScale"
    )
    val coreGlowAlpha by transition.animateFloat(
        initialValue = 0.15f, targetValue = 0.50f,
        animationSpec = infiniteRepeatable(tween(2000, delayMillis = 200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "coreGlowAlpha"
    )
    // 波浪呼吸 — 与光晕错开
    val waveBreathAlpha by transition.animateFloat(
        initialValue = 0.2f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(3000, delayMillis = 100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "waveBreathAlpha"
    )

    // ── 波浪相位：持续前进 ──
    val wavePhase = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            wavePhase.animateTo(
                targetValue = wavePhase.value + 6.2832f,
                animationSpec = tween(6000, easing = LinearEasing)
            )
        }
    }

    // ── 浪花粒子状态 ──
    val foamCount = 16
    val foamStates = remember { List(foamCount) { FoamState.random() } }

    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ── 头像区域：光晕 + 波浪 + 浪花 ──
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    // Canvas 绘制：光晕 + 波浪 + 浪花
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = 34.dp.toPx()
                        val avatarR = 18.dp.toPx()

                        // ── 外圈光晕 ──
                        drawCircle(
                            color = Color(0xFF4CAF50).copy(alpha = ringOuterAlpha * 0.35f),
                            radius = avatarR * 1.6f,
                            center = Offset(cx, cy)
                        )
                        // ── 中间描边环 ──
                        drawCircle(
                            color = Color(0xFF4CAF50).copy(alpha = ringMidAlpha),
                            radius = avatarR * 1.3f,
                            center = Offset(cx, cy),
                            style = Stroke(width = 2.5.dp.toPx())
                        )
                        // ── 核心光晕底色 ──
                        drawCircle(
                            color = Color(0xFF2E7D32).copy(alpha = coreGlowAlpha),
                            radius = avatarR * 1.08f,
                            center = Offset(cx, cy)
                        )

                        // ── 波浪层 ──
                        val waveBaseY = cy + avatarR * 0.6f
                        val waveBaseR = avatarR * 0.9f
                        val phase = wavePhase.value

                        for (layer in 0..2) {
                            val layerOffset = layer * 6.dp.toPx()
                            val layerAlpha = (0.45f - layer * 0.14f) * waveBreathAlpha
                            val phaseOff = layer * 0.8f

                            val path = Path()
                            var started = false
                            var prevX = 0f
                            var prevY = 0f

                            // 从左侧到右侧画波浪弧线
                            val steps = 60
                            for (i in 0..steps) {
                                val t = i.toFloat() / steps
                                val angle = -PI.toFloat() * 0.15f + t * PI.toFloat() * 1.3f

                                val r = waveBaseR + layerOffset
                                val wave = sin(angle * 3f + phase * 2f + phaseOff) * 4.dp.toPx() +
                                        sin(angle * 5f - phase * 3f + phaseOff * 2f) * 2.dp.toPx()

                                val x = cx + cos(angle) * (r + wave)
                                val y = waveBaseY + sin(angle) * (r * 0.45f + wave * 0.5f)

                                // 只画下半部分
                                if (y >= waveBaseY - 3.dp.toPx()) {
                                    if (!started) {
                                        path.moveTo(x, y)
                                        started = true
                                    } else {
                                        path.lineTo(x, y)
                                    }
                                    prevX = x
                                    prevY = y
                                }
                            }

                            if (started) {
                                drawPath(
                                    path = path,
                                    color = Color(0xFF4CAF50).copy(alpha = layerAlpha),
                                    style = Stroke(
                                        width = (2f - layer * 0.3f).dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                )
                            }

                            // 泡沫线 — 更亮
                            val foamPath = Path()
                            var foamStarted = false
                            for (i in 0..steps) {
                                val t = i.toFloat() / steps
                                val angle = -PI.toFloat() * 0.1f + t * PI.toFloat() * 1.2f

                                val r = waveBaseR + layerOffset + 3.dp.toPx()
                                val wave = sin(angle * 4f + phase * 2.5f + phaseOff + 1f) * 3.dp.toPx() +
                                        cos(angle * 6f - phase * 2f + phaseOff) * 1.5.dp.toPx()

                                val x = cx + cos(angle) * (r + wave)
                                val y = waveBaseY + sin(angle) * (r * 0.45f + wave * 0.5f)

                                if (y >= waveBaseY - 3.dp.toPx()) {
                                    if (!foamStarted) {
                                        foamPath.moveTo(x, y)
                                        foamStarted = true
                                    } else {
                                        foamPath.lineTo(x, y)
                                    }
                                }
                            }

                            if (foamStarted) {
                                drawPath(
                                    path = foamPath,
                                    color = Color(0xFFC8E6C9).copy(alpha = layerAlpha * 0.6f),
                                    style = Stroke(
                                        width = 1.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                )
                            }
                        }

                        // ── 浪花粒子 ──
                        for (foam in foamStates) {
                            val fx = cx + cos(foam.angle) * (waveBaseR + foam.radiusOffset)
                            val fy = waveBaseY + sin(foam.angle) * (waveBaseR * 0.45f + foam.radiusOffset * 0.4f) +
                                    sin(phase * foam.bobSpeed + foam.phase) * foam.bobAmp.dp.toPx()

                            if (fy >= waveBaseY - 5.dp.toPx()) {
                                val a = foam.alpha * foam.life * waveBreathAlpha
                                // 主粒子
                                drawCircle(
                                    color = Color(0xFF4CAF50).copy(alpha = a),
                                    radius = foam.size.dp.toPx() / 2f,
                                    center = Offset(fx, fy)
                                )
                                // 高光
                                drawCircle(
                                    color = Color.White.copy(alpha = a * 0.5f),
                                    radius = foam.size.dp.toPx() * 0.18f,
                                    center = Offset(fx - foam.size.dp.toPx() * 0.12f, fy - foam.size.dp.toPx() * 0.12f)
                                )
                            }
                        }
                    }

                    // 头像本体 — 在 Canvas 之上
                    Image(
                        painter = painterResource(R.drawable.ic_ai_avatar),
                        contentDescription = "AI头像",
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .size(36.dp)
                            .graphicsLayer {
                                scaleX = coreScale
                                scaleY = coreScale
                            }
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("轻食记 AI", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(shape = RoundedCornerShape(6.dp), color = Green.copy(alpha = 0.20f)) {
                            Text("Beta", modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GreenDark)
                        }
                    }
                    Text("你的专属饮食分析助手", fontSize = 11.sp, color = TextSecondary)
                }
                Surface(onClick = onCameraClick, shape = RoundedCornerShape(12.dp), color = Color.White, border = BorderStroke(1.dp, Border)) {
                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp), tint = GreenDark)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("拍照识别食物", fontSize = 11.sp, color = GreenDark)
                    }
                }
            }
        }
    }

    // 更新浪花粒子生命周期
    val phaseVal = wavePhase.value
    LaunchedEffect(phaseVal) {
        for (foam in foamStates) {
            foam.life -= foam.decay
            if (foam.life <= 0f) {
                foam.respawn()
            }
        }
    }
}

// ── 浪花粒子数据 ──
private class FoamState(
    var angle: Float,
    var radiusOffset: Float,
    var size: Float,
    var alpha: Float,
    var phase: Float,
    var bobAmp: Float,
    var bobSpeed: Float,
    var life: Float,
    var decay: Float,
) {
    companion object {
        fun random() = FoamState(
            angle = (Math.random() * Math.PI * 2).toFloat(),
            radiusOffset = (Math.random() * 16 + 4).toFloat(),
            size = (Math.random() * 4 + 2).toFloat(),
            alpha = (Math.random() * 0.5f + 0.3f).toFloat(),
            phase = (Math.random() * Math.PI * 2).toFloat(),
            bobAmp = (Math.random() * 3 + 1.5f).toFloat(),
            bobSpeed = (Math.random() * 2 + 1).toFloat(),
            life = (Math.random() * 0.7f + 0.3f).toFloat(),
            decay = (Math.random() * 0.004f + 0.001f).toFloat(),
        )
    }

    fun respawn() {
        angle = (Math.random() * Math.PI * 2).toFloat()
        radiusOffset = (Math.random() * 16 + 4).toFloat()
        size = (Math.random() * 4 + 2).toFloat()
        alpha = (Math.random() * 0.5f + 0.3f).toFloat()
        phase = (Math.random() * Math.PI * 2).toFloat()
        bobAmp = (Math.random() * 3 + 1.5f).toFloat()
        bobSpeed = (Math.random() * 2 + 1).toFloat()
        life = 1f
        decay = (Math.random() * 0.004f + 0.001f).toFloat()
    }
}

@Composable
fun UserMessageBubble(content: String, imagePath: String? = null, timestamp: String = "") {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = 20.dp,
                bottomEnd = 6.dp
            ),
            color = SpinachGreen,
            shadowElevation = 5.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column {
                if (imagePath != null) {
                    val bitmap = remember(imagePath) {
                        try {
                            val options = android.graphics.BitmapFactory.Options().apply {
                                inSampleSize = 2
                            }
                            android.graphics.BitmapFactory.decodeFile(imagePath, options)
                        } catch (_: Exception) { null }
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "发送的图片",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 180.dp)
                                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Text(
                    text = content,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            }
        }
        // 时间戳 + 对勾
        if (timestamp.isNotEmpty()) {
            Row(
                modifier = Modifier.padding(top = 2.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(timestamp, fontSize = 10.sp, color = TextTertiary)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(12.dp), tint = Green)
            }
        }
    }
}

@Composable
fun AiTextBubble(content: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        CoachAvatar()
        Spacer(modifier = Modifier.width(10.dp))
        Surface(
            shape = RoundedCornerShape(
                topStart = 6.dp,
                topEnd = 20.dp,
                bottomStart = 20.dp,
                bottomEnd = 20.dp
            ),
            color = GlassGreen,
            shadowElevation = 4.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = content,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun CoachAvatar() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(R.drawable.ic_ai_avatar),
            contentDescription = "AI头像",
            modifier = Modifier.size(38.dp).clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "轻食记", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
    }
}

@Composable
fun AiFoodResultCard(
    msg: ChatMessage,
    position: Int,
    onSaveClick: (ChatMessage, Int, String) -> Unit,
    onEditClick: (ChatMessage) -> Unit,
    onActionClick: (String, ChatMessage) -> Unit,
    onEditFoodItem: (ChatMessage, Int, Int) -> Unit,
) {
    val mealOptions = listOf("☀️ 早餐" to "breakfast", "🌤 午餐" to "lunch", "🌙 晚餐" to "dinner", "🍪 加餐" to "snack")
    val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val defaultMeal = when (currentHour) {
        in 5..10 -> "breakfast"
        in 11..16 -> "lunch"
        in 17..21 -> "dinner"
        else -> "snack"
    }
    var selectedMeal by remember { mutableStateOf(defaultMeal) }
    var mealMenuExpanded by remember { mutableStateOf(false) }
    val selectedMealLabel = mealOptions.firstOrNull { it.second == selectedMeal }?.first ?: "🌙 晚餐"
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        CoachAvatar()
        Spacer(modifier = Modifier.width(10.dp))
        Box {
            Card(
                modifier = Modifier.widthIn(max = 300.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = GreenLight
                    ) {
                        Text(
                            text = "本次分析",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = GreenDark
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    msg.pendingSaveFoods.forEachIndexed { index, food ->
                        FoodResultRow(food = food, onEdit = {
                            onEditFoodItem(msg, position, index)
                        })
                        if (index < msg.pendingSaveFoods.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = Divider,
                                thickness = 1.dp
                            )
                        }
                    }

                    SummaryRow(foods = msg.pendingSaveFoods)

                    if (msg.pendingSaveReport.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            color = GreenBg
                        ) {
                            Text(
                                text = "建议：${msg.pendingSaveReport}",
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f).height(40.dp)) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                Box {
                                    Surface(
                                        onClick = { mealMenuExpanded = true },
                                        modifier = Modifier.height(40.dp).width(56.dp),
                                        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
                                        color = Color(0xFFD4E8D0)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text(text = selectedMealLabel.split(" ").last(), fontSize = 11.sp, color = Color(0xFF1A1A1A), fontWeight = FontWeight.Bold)
                                            Text(text = " ▾", fontSize = 9.sp, color = Color(0xFF1A1A1A))
                                        }
                                    }
                                    DropdownMenu(
                                        expanded = mealMenuExpanded,
                                        onDismissRequest = { mealMenuExpanded = false }
                                    ) {
                                        mealOptions.forEach { (label, key) ->
                                            DropdownMenuItem(
                                                text = { Text(label, fontSize = 14.sp) },
                                                onClick = {
                                                    selectedMeal = key
                                                    mealMenuExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                                Button(
                                    onClick = { onSaveClick(msg, position, selectedMeal) },
                                    modifier = Modifier.height(40.dp).weight(1f).pressScale(),
                                    shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp, topStart = 0.dp, bottomStart = 0.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Green),
                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                ) {
                                    Text(text = "保存到日记", fontSize = 13.sp, color = Color.White)
                                }
                            }
                        }
                        Button(
                            onClick = { onEditClick(msg) },
                            modifier = Modifier.weight(1f).height(40.dp).pressScale(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Teal),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text(text = "修改", fontSize = 13.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        if (mealMenuExpanded) {
            AlertDialog(
                onDismissRequest = { mealMenuExpanded = false },
                title = { Text("选择餐次") },
                text = {
                    Column {
                        mealOptions.forEach { (label, key) ->
                            val isSelected = key == selectedMeal
                            Surface(
                                onClick = {
                                    selectedMeal = key
                                    mealMenuExpanded = false
                                },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) GreenLight else Color.Transparent
                            ) {
                                Text(
                                    text = label,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                    fontSize = 16.sp,
                                    color = if (isSelected) GreenDark else TextPrimary
                                )
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { mealMenuExpanded = false }) {
                        Text("取消")
                    }
                }
            )
        }
    }
}

@Composable
private fun ActionChipsRow(onActionClick: (String) -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInVertically(tween(400), initialOffsetY = { it / 4 })
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 48.dp, top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconChip(icon = "🏃", label = "运动", onClick = { onActionClick("exercise") })
            IconChip(icon = "🍽", label = "还能吃", onClick = { onActionClick("remaining") })
            IconChip(icon = "📊", label = "缺口", onClick = { onActionClick("deficit") })
        }
    }
}

@Composable
fun FoodResultRow(food: FoodItem, onEdit: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val foodEmoji = com.dietapp.data.FoodData.findByName(food.name)?.emoji
                    ?: com.dietapp.data.FoodData.categories.firstOrNull { food.name.contains(it.name) }?.emoji
                    ?: "🍽️"
                Text(
                    text = "$foodEmoji ${food.name}",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "份量 ${"%.1f".format(food.amount)}${food.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${food.cal.toInt()} kcal",
                style = MaterialTheme.typography.titleMedium,
                color = OrangeDark
            )
            TextButton(onClick = onEdit) {
                Text(text = "编辑", color = GreenDark)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MacroPill(label = "蛋白", value = "${"%.1f".format(food.protein)}g", color = TealDark)
            MacroPill(label = "碳水", value = "${"%.1f".format(food.carbs)}g", color = OrangeDark)
            MacroPill(label = "脂肪", value = "${"%.1f".format(food.fat)}g", color = Purple)
        }
    }
}

@Composable
private fun MacroPill(label: String, value: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = "$label $value",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
fun SummaryRow(foods: List<FoodItem>) {
    val totalCal = foods.sumOf { it.cal }
    val totalProtein = foods.sumOf { it.protein }
    val totalCarbs = foods.sumOf { it.carbs }
    val totalFat = foods.sumOf { it.fat }

    Spacer(modifier = Modifier.height(6.dp))
    HorizontalDivider(color = Divider, thickness = 1.dp)
    Spacer(modifier = Modifier.height(6.dp))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = InsightSurface
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = "总览",
                style = MaterialTheme.typography.labelSmall,
                color = OrangeDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryMetric(value = "%.0f".format(totalCal), label = "kcal")
                SummaryMetric(value = "%.1f".format(totalProtein), label = "蛋白")
                SummaryMetric(value = "%.1f".format(totalCarbs), label = "碳水")
                SummaryMetric(value = "%.1f".format(totalFat), label = "脂肪")
            }
        }
    }
}

@Composable
private fun SummaryMetric(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

@Composable
fun ChatInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onCameraClick: () -> Unit,
    onFoodPickerClick: () -> Unit,
) {
    Surface(color = Color.Transparent) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChange,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 38.dp),
                placeholder = { Text("输入你刚刚吃了什么", fontSize = 13.sp) },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Green,
                    unfocusedBorderColor = Border,
                    focusedContainerColor = CardBg,
                    unfocusedContainerColor = CardBg
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
            IconButton(onClick = onCameraClick, modifier = Modifier.size(36.dp)) {
                Icon(painter = painterResource(R.drawable.ic_camera), contentDescription = "拍照", tint = GreenDark, modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onFoodPickerClick, modifier = Modifier.size(36.dp)) {
                Icon(painter = painterResource(R.drawable.ic_food), contentDescription = "食物", tint = GreenDark, modifier = Modifier.size(20.dp))
            }
            Surface(onClick = onSend, modifier = Modifier.size(38.dp), shape = CircleShape, color = Green) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "发送", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
        }



@Composable
fun ImagePreviewBar(imagePath: String, onRemove: () -> Unit) {
    val bitmap = remember(imagePath) {
        try {
            val options = android.graphics.BitmapFactory.Options().apply {
                inSampleSize = 2
            }
            android.graphics.BitmapFactory.decodeFile(imagePath, options)
        } catch (_: Exception) { null }
    }

    Surface(
        color = CardBg,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "已选图片",
                    modifier = Modifier
                        .height(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .aspectRatio(bitmap.width.toFloat() / bitmap.height.toFloat()),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onRemove) {
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = CircleShape,
                    color = Red.copy(alpha = 0.12f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("✕", fontSize = 12.sp, color = Red)
                    }
                }
            }
        }
    }
}

@Composable
private fun IconChip(icon: String, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(14.dp),
            color = GreenBg,
            modifier = Modifier.size(44.dp).pressScale()
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(icon, fontSize = 20.sp)
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, fontSize = 10.sp, color = TextSecondary)
    }
}
