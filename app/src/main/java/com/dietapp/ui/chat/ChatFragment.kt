package com.dietapp.ui.chat

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.dietapp.data.FoodData
import com.dietapp.data.FoodKnowledgeBase
import com.dietapp.data.MealTypeResolver
import com.dietapp.data.TextCleaner
import com.dietapp.data.UserPreferences
import com.dietapp.data.api.BaiduResult
import com.dietapp.data.api.DietRecordRequest
import com.dietapp.data.api.FoodItem
import com.dietapp.data.repository.DietRepository
import com.dietapp.ui.theme.DietAppTheme
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ChatFragment : Fragment() {

    private val chatViewModel: ChatViewModel by activityViewModels()
    private val repository = DietRepository
    private lateinit var foodKnowledgeBase: FoodKnowledgeBase
    private val messageHistory get() = chatViewModel.messageHistory
    private var pendingFoods: List<FoodItem>
        get() = chatViewModel.pendingFoods
        set(value) { chatViewModel.pendingFoods = value }
    private var lastFoodText = ""
    private var selectedImagePath by mutableStateOf<String?>(null)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).apply { timeZone = TimeZone.getTimeZone("Asia/Shanghai") }
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.CHINA)
    // Gson 复用实例，避免 parseAiResponse 每次创建新对象
    private val gson = com.google.gson.Gson()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                DietAppTheme {
                    ChatScreen(
                        messages = messageHistory,
                        onSendText = { text -> sendChatText(text) },
                        onCameraClick = { showCameraPicker() },
                        onFoodPickerClick = { showFoodPicker() },
                        onSaveClick = { msg, pos, mealType -> saveToDiary(msg, pos, mealType) },
                        onEditClick = { msg ->
                            val idx = messageHistory.indexOf(msg)
                            if (idx >= 0) showEditFoodDialog(msg, idx)
                        },
                        onActionClick = { action, msg -> handleAction(action, msg) },
                        onEditFoodItem = { msg, msgPos, foodIndex ->
                            showEditSingleFoodDialog(msg, msgPos, foodIndex)
                        },
                        imagePath = selectedImagePath,
                        onRemoveImage = { selectedImagePath = null }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        foodKnowledgeBase = FoodKnowledgeBase(requireContext())

        if (messageHistory.isEmpty() && !chatViewModel.hasShownGreeting) {
            addMessage("assistant", "你好！我是轻食记 AI 营养助手。\n\n在下方输入食物名称，或使用拍照、食物选择器，我来帮你分析营养。保存日记时再选择餐次。")
            chatViewModel.hasShownGreeting = true
        }
    }

    private fun sendChatText(text: String) {
        val imagePath = selectedImagePath
        if (text.isEmpty() && imagePath == null) return

        if (imagePath != null) {
            selectedImagePath = null
            sendWithImage(text, imagePath)
        } else {
            addMessage("user", text)
            analyzeWithAi(text)
        }
    }

    private fun sendWithImage(text: String, photoPath: String) {
        val userContent = text.ifEmpty { "📷 拍照识别" }
        val userMsgIndex = messageHistory.size
        addMessage("user", userContent, imagePath = photoPath)
        lifecycleScope.launch {
            try {
                val bitmap = android.graphics.BitmapFactory.decodeFile(photoPath)
                val stream = java.io.ByteArrayOutputStream()
                bitmap?.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, stream)
                val base64Image = android.util.Base64.encodeToString(stream.toByteArray(), android.util.Base64.NO_WRAP)
                val baiduResult = withContext(Dispatchers.IO) { repository.recognizeFood(base64Image) }
                val sortedResults = baiduResult.result
                    .filter { it.name.isNotBlank() && it.probability > 0.3 }
                    .sortedByDescending { it.probability }
                val deduplicated = mutableListOf<BaiduResult>()
                for (r in sortedResults) {
                    val isSimilar = deduplicated.any { existing ->
                        r.name.any { c -> c in existing.name } &&
                        (r.name.length <= 3 || existing.name.length <= 3)
                    }
                    if (!isSimilar) deduplicated.add(r)
                }
                val topResults = deduplicated.take(4)
                val inputText = if (topResults.isNotEmpty()) {
                    val mainFood = topResults.first().name
                    val extras = topResults.drop(1).map { it.name }
                    val recognized = if (extras.isNotEmpty()) "拍照识别：$mainFood、${extras.joinToString("、")}" else "拍照识别：$mainFood"
                    if (text.isNotEmpty()) "$text\n📷 $recognized" else "📷 $recognized"
                } else {
                    text.ifEmpty { "📷 拍照识别：未能识别出食物" }
                }
                if (userMsgIndex in messageHistory.indices) {
                    messageHistory[userMsgIndex] = messageHistory[userMsgIndex].copy(content = inputText)
                }
                analyzeWithAi(inputText)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "图片识别失败，已用文字继续分析", Toast.LENGTH_SHORT).show()
                analyzeWithAi(text.ifEmpty { "拍照识别" })
            }
        }
    }

    private fun showCameraPicker() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("添加食物图片")
            .setItems(arrayOf("拍照", "从相册选择")) { _, which ->
                if (which == 0) openCamera() else openGallery()
            }
            .show()
    }

    // ═══ 相机 ═══
    private var currentPhotoUri: android.net.Uri? = null
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            currentPhotoUri?.let { uri ->
                try {
                    val bitmap = android.graphics.BitmapFactory.decodeStream(requireContext().contentResolver.openInputStream(uri))
                    if (bitmap != null) {
                        val file = java.io.File(requireContext().cacheDir, "food_${System.currentTimeMillis()}.jpg")
                        file.outputStream().use { out -> bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, out) }
                        selectedImagePath = file.absolutePath
                        Toast.makeText(requireContext(), "已选择图片，输入文字后发送", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) { Toast.makeText(requireContext(), "拍照处理失败", Toast.LENGTH_SHORT).show() }
            }
        }
    }
    private val requestCameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) launchCameraCapture() else Toast.makeText(requireContext(), "需要相机权限才能拍照", Toast.LENGTH_SHORT).show()
    }
    private val requestGalleryPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) galleryLauncher.launch("image/*") else Toast.makeText(requireContext(), "需要相册权限才能选择图片", Toast.LENGTH_SHORT).show()
    }
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: android.net.Uri? ->
        uri?.let {
            try {
                val bitmap = android.graphics.BitmapFactory.decodeStream(requireContext().contentResolver.openInputStream(it))
                if (bitmap != null) {
                    val file = java.io.File(requireContext().cacheDir, "food_gallery_${System.currentTimeMillis()}.jpg")
                    file.outputStream().use { out -> bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, out) }
                    selectedImagePath = file.absolutePath
                    Toast.makeText(requireContext(), "已选择图片，输入文字后发送", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) { Toast.makeText(requireContext(), "图片加载失败", Toast.LENGTH_SHORT).show() }
        }
    }
    private fun openGallery() {
        if (!isAdded) return
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED)
                    galleryLauncher.launch("image/*")
                else requestGalleryPermission.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
            else -> galleryLauncher.launch("image/*")
        }
    }
    private fun openCamera() {
        if (!isAdded) return
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> launchCameraCapture()
            else -> requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }
    private fun launchCameraCapture() {
        try {
            val photoFile = java.io.File(requireContext().cacheDir, "food_${System.currentTimeMillis()}.jpg")
            photoFile.parentFile?.mkdirs()
            currentPhotoUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", photoFile)
            val intent = android.content.Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(android.provider.MediaStore.EXTRA_OUTPUT, currentPhotoUri)
                addFlags(android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
            cameraLauncher.launch(intent)
        } catch (e: Exception) { Toast.makeText(requireContext(), "无法打开相机：${e.message}", Toast.LENGTH_LONG).show() }
    }

    private fun showFoodPicker() {
        FoodPickerDialog { foods ->
            if (foods.isNotEmpty()) {
                pendingFoods = foods
                lastFoodText = foods.joinToString(" + ") { formatFoodSummary(it) }
                addMessage("user", "我吃了：$lastFoodText")
                analyzeWithAi("我吃了：$lastFoodText")
            }
        }.show(parentFragmentManager, "food_picker")
    }

    private fun addMessage(role: String, content: String, foods: List<FoodItem> = emptyList(), report: String = "", imagePath: String? = null) {
        val time = timeFormat.format(Date())
        messageHistory.add(ChatMessage(role, content, pendingSaveFoods = foods, pendingSaveReport = report, imagePath = imagePath, timestamp = time))
    }

    private fun analyzeWithAi(userInput: String, retryCount: Int = 1) {
        val mealLabel = MealTypeResolver.label(MealTypeResolver.parseFromText(userInput) ?: MealTypeResolver.inferByHour())
        val loadingIndex = messageHistory.size
        messageHistory.add(ChatMessage("assistant", "分析中..."))

        lifecycleScope.launch {
            try {
                val ragPrompt = foodKnowledgeBase.buildRagPrompt(userInput)
                val todayCal = try { withContext(Dispatchers.IO) { repository.getSummary(dateFormat.format(Date())) } } catch (_: Exception) { null }
                val alreadyAte = todayCal?.intake?.totalCal ?: 0.0
                val goal = UserPreferences.getDailyCalGoal(requireContext())
                val systemPrompt = buildString {
                    appendLine("你是专业的注册营养师级 AI 助手。用户尚未指定餐次（仅供参考当前可能是$mealLabel），分析时不要写死餐次名称。")
                    appendLine("## 重要规则")
                    appendLine("- 当输入包含「拍照识别」时，你必须分析图片中【所有可见食物】，不能只分析识别结果列出的那几个")
                    appendLine("- 用户额外输入的文字（如「还有可乐」）视为补充食物，也要一起分析")
                    appendLine("- 一张照片中通常有多道菜/多种食物，请根据常识判断并逐一列出（如：吐司、煎蛋、番茄、黄瓜、牛奶等）")
                    appendLine("- 不要重复分析相似的食物（如「鸡腿」和「炸鸡」只保留一个）")
                    appendLine("- 图片识别可能不准确，如果识别结果与图片明显不符，请根据你的专业知识补充修正")
                    appendLine("## 输出格式")
                    appendLine("1. 每种食物单独一行：名称 + 份量 + 克数 + 热量与三大营养素")
                    appendLine("2. 空一行后写本餐合计")
                    appendLine("3. 空一行后写详细建议（80-150字）")
                    appendLine("4. 最后输出【JSON】...【/JSON】")
                    appendLine("JSON格式：{\"foods\":[{\"name\":\"米饭\",\"amount\":1,\"unit\":\"碗\",\"weight_g\":250,\"cal\":116,\"protein\":2.6,\"carbs\":25.9,\"fat\":0.3}],\"total_cal\":116,\"total_protein\":2.6,\"total_carbs\":25.9,\"total_fat\":0.3,\"ai_report\":\"详细建议\"}")
                    appendLine("今日已摄入${"%.0f".format(alreadyAte)}kcal / 目标${goal}kcal")
                }
                val rawResult = withContext(Dispatchers.IO) { repository.aiChat(systemPrompt, ragPrompt) }
                val (foods, report, displayText) = parseAiResponse(rawResult)
                val enrichedFoods = enrichFromKnowledgeBase(foods)
                messageHistory[loadingIndex] = ChatMessage("assistant", displayText, isFoodAnalysis = enrichedFoods.isNotEmpty(), pendingSaveFoods = enrichedFoods, pendingSaveReport = report, showActions = true)
            } catch (e: Exception) {
                if (retryCount > 0) {
                    messageHistory.removeAt(loadingIndex)
                    analyzeWithAi(userInput, retryCount - 1)
                } else {
                    messageHistory[loadingIndex] = ChatMessage("assistant", "❌ 分析失败：${e.message}", showActions = true)
                }
            }
        }
    }

    private fun parseAiResponse(raw: String): Triple<List<FoodItem>, String, String> {
        val result = TextCleaner.clean(raw)
        // 1. JSON 标记
        val jStart = result.indexOf("【JSON】")
        val jEnd = result.indexOf("【/JSON】")
        if (jStart >= 0 && jEnd > jStart) {
            try {
                val jsonStr = TextCleaner.cleanJson(result.substring(jStart + 6, jEnd))
                val parsed = gson.fromJson(jsonStr, AiParseResult::class.java)
                if (parsed.foods.isNotEmpty()) {
                    val foods = parsed.foods.map { toFoodItem(it) }
                    val displayText = TextCleaner.clean(result.replace(Regex("【JSON】.*?【/JSON】"), ""))
                    return Triple(foods, parsed.ai_report ?: "", displayText.ifEmpty { "🍽️ 识别到 ${foods.size} 种食物" })
                }
            } catch (_: Exception) {}
        }
        // 2. 裸 JSON
        try {
            val bs = result.indexOf('{')
            val be = result.lastIndexOf('}')
            if (bs >= 0 && be > bs) {
                val parsed = gson.fromJson(result.substring(bs, be + 1), AiParseResult::class.java)
                if (parsed.foods.isNotEmpty()) {
                    val displayText = TextCleaner.clean(result.replace(Regex("\\{.*\\}"), ""))
                    return Triple(parsed.foods.map { toFoodItem(it) }, parsed.ai_report ?: "", displayText.ifEmpty { "🍽️ 识别到 ${parsed.foods.size} 种食物" })
                }
            }
        } catch (_: Exception) {}
        // 3. 兜底
        val names = Regex("[\\u4e00-\\u9fff]{2,6}").findAll(result).map { it.value }.distinct().take(10).toList()
        if (names.isNotEmpty()) return Triple(names.map { FoodItem(name = it, amount = 1.0, unit = "份", cal = 100.0) }, "", TextCleaner.removeJsonPart(result))
        return Triple(emptyList(), "", result.take(200))
    }

    private fun handleAction(action: String, msg: ChatMessage) {
        when (action) {
            "save" -> { val idx = messageHistory.indexOf(msg); if (idx >= 0) saveToDiary(msg, idx, MealTypeResolver.inferByHour()) }
            "edit" -> { val idx = messageHistory.indexOf(msg); if (idx >= 0) showEditFoodDialog(msg, idx) }
            "exercise" -> { addMessage("user", "针对我吃的这些，需要运动多久才能消耗？分别说跑步、跳绳、快走各需多久"); analyzeWithAi("运动消耗分析") }
            "remaining" -> {
                lifecycleScope.launch {
                    val ate = try { withContext(Dispatchers.IO) { repository.getSummary(dateFormat.format(Date())) } } catch (_: Exception) { null }?.intake?.totalCal ?: 0.0
                    val goal = UserPreferences.getDailyCalGoal(requireContext())
                    addMessage("user", "今日已摄入${"%.0f".format(ate)}kcal，目标${goal}kcal。我今天还能吃什么？推荐一些低热量食物")
                    analyzeWithAi("还能吃什么")
                }
            }
            "deficit" -> { addMessage("user", "分析我的热量缺口，告诉我今天这样吃是增重还是减脂"); analyzeWithAi("热量缺口分析") }
        }
    }

    private fun showEditSingleFoodDialog(msg: ChatMessage, msgIndex: Int, foodIndex: Int) {
        val foods = msg.pendingSaveFoods.toMutableList()
        if (foodIndex >= foods.size) return
        val food = foods[foodIndex]
        val inputView = layoutInflater.inflate(com.dietapp.R.layout.dialog_text_input, null)
        val editText = inputView.findViewById<com.google.android.material.textfield.TextInputEditText>(com.dietapp.R.id.et_food_input)
        editText.setText("${food.name} ${food.cal.toInt()}kcal x${food.amount}${food.unit}")
        editText.setHint("格式：食物名 热量kcal x分量单位")
        MaterialAlertDialogBuilder(requireContext()).setTitle("✏️ 修改：${food.name}").setView(inputView)
            .setPositiveButton("确定") { _, _ ->
                val text = editText.text.toString().trim()
                if (text.isNotEmpty()) {
                    val parts = text.split(" ")
                    if (parts.size >= 2) {
                        foods[foodIndex] = food.copy(name = parts[0], cal = parts.getOrNull(1)?.replace("kcal", "")?.toDoubleOrNull() ?: food.cal, amount = parts.getOrNull(2)?.replace("x", "")?.toDoubleOrNull() ?: food.amount)
                        messageHistory[msgIndex] = msg.copy(pendingSaveFoods = foods.toList())
                        Toast.makeText(requireContext(), "✅ 已更新", Toast.LENGTH_SHORT).show()
                    }
                }
            }.setNegativeButton("取消", null).show()
    }

    private fun showEditFoodDialog(msg: ChatMessage, index: Int) {
        val foods = msg.pendingSaveFoods.toMutableList()
        if (foods.isEmpty()) return
        val names = foods.mapIndexed { i, f -> "${i + 1}. ${f.name}  ${f.cal.toInt()}kcal x${f.amount}" }.toTypedArray()
        val checked = BooleanArray(foods.size) { true }
        MaterialAlertDialogBuilder(requireContext()).setTitle("✏️ 编辑食物").setMultiChoiceItems(names, checked) { _, which, isChecked -> checked[which] = isChecked }
            .setPositiveButton("修改分量") { _, _ -> showEditAmountSequential(foods, checked, 0, msg, index) }
            .setNeutralButton("删除选中") { _, _ ->
                for (i in foods.indices.reversed()) { if (checked[i]) foods.removeAt(i) }
                if (foods.isEmpty()) return@setNeutralButton
                messageHistory[index] = msg.copy(pendingSaveFoods = foods.toList())
                Toast.makeText(requireContext(), "已更新", Toast.LENGTH_SHORT).show()
            }.setNegativeButton("取消", null).show()
    }

    private fun showEditAmountSequential(foods: MutableList<FoodItem>, checked: BooleanArray, pos: Int, msg: ChatMessage, msgIndex: Int) {
        if (pos >= foods.size) { messageHistory[msgIndex] = msg.copy(pendingSaveFoods = foods.toList()); Toast.makeText(requireContext(), "✅ 已更新", Toast.LENGTH_SHORT).show(); return }
        if (!checked[pos]) { showEditAmountSequential(foods, checked, pos + 1, msg, msgIndex); return }
        val food = foods[pos]
        val inputView = layoutInflater.inflate(com.dietapp.R.layout.dialog_text_input, null)
        val editText = inputView.findViewById<com.google.android.material.textfield.TextInputEditText>(com.dietapp.R.id.et_food_input)
        editText.setText("${food.name} ${food.cal.toInt()}kcal x${food.amount}${food.unit}")
        MaterialAlertDialogBuilder(requireContext()).setTitle("编辑 #${pos + 1}").setView(inputView)
            .setPositiveButton("下一个") { _, _ ->
                val text = editText.text.toString().trim()
                if (text.isNotEmpty()) {
                    val p = text.split(" ")
                    if (p.size >= 2) foods[pos] = food.copy(name = p[0], cal = p.getOrNull(1)?.replace("kcal", "")?.toDoubleOrNull() ?: food.cal, amount = p.getOrNull(2)?.replace("x", "")?.toDoubleOrNull() ?: food.amount)
                }
                showEditAmountSequential(foods, checked, pos + 1, msg, msgIndex)
            }.setNegativeButton("跳过", null).show()
    }

    private fun saveToDiary(msg: ChatMessage, index: Int, mealType: String) {
        if (!isAdded) return
        val ri = messageHistory.indexOf(msg).takeIf { it >= 0 } ?: index
        if (ri !in messageHistory.indices) return
        val lm = messageHistory[ri]
        if (lm.pendingSaveFoods.isEmpty() && pendingFoods.isEmpty()) { Toast.makeText(requireContext(), "没有可保存的食物数据", Toast.LENGTH_SHORT).show(); return }
        performSaveToDiary(lm, ri, mealType)
    }

    private fun performSaveToDiary(msg: ChatMessage, index: Int, mealType: String) {
        lifecycleScope.launch {
            try {
                val foodsToSave = if (pendingFoods.isNotEmpty()) { val f = pendingFoods; pendingFoods = emptyList(); f } else msg.pendingSaveFoods
                withContext(Dispatchers.IO) { repository.addRecord(DietRecordRequest(date = dateFormat.format(Date()), mealType = mealType, foods = foodsToSave, totalCal = foodsToSave.sumOf { it.cal }, totalProtein = foodsToSave.sumOf { it.protein }, totalCarbs = foodsToSave.sumOf { it.carbs }, totalFat = foodsToSave.sumOf { it.fat }, aiReport = msg.pendingSaveReport)) }
                Toast.makeText(requireContext(), "已保存到${MealTypeResolver.label(mealType)}", Toast.LENGTH_SHORT).show()
                messageHistory[index] = msg.copy(content = "已保存到${MealTypeResolver.label(mealType)}", pendingSaveFoods = emptyList(), isFoodAnalysis = false, showActions = false)
            } catch (e: Exception) { Toast.makeText(requireContext(), "保存失败：${e.message}", Toast.LENGTH_SHORT).show() }
        }
    }

    private fun formatFoodSummary(food: FoodItem): String = "${food.name} ${"%.1f".format(food.amount)}${food.unit}${if (food.weightGrams > 0) "≈${food.weightGrams.toInt()}g" else ""} · ${food.cal.toInt()}kcal"
    private fun toFoodItem(it: AiFoodItem): FoodItem = FoodItem(it.name, it.amount ?: 1.0, it.unit ?: "份", it.weight_g ?: 0.0, it.cal ?: 0.0, it.protein ?: 0.0, it.carbs ?: 0.0, it.fat ?: 0.0)

    /**
     * 从食材知识库回填 AI 未返回的宏量营养数据
     * 避免保存后蛋白/碳水/脂肪显示为 0
     */
    private fun enrichFromKnowledgeBase(foods: List<FoodItem>): List<FoodItem> {
        return foods.map { food ->
            if (food.protein == 0.0 && food.carbs == 0.0 && food.fat == 0.0) {
                val knowledge = foodKnowledgeBase.search(food.name).firstOrNull()
                if (knowledge != null) {
                    // 优先用 weightGrams 算比例，否则用 amount * 默认份量估算
                    val grams = if (food.weightGrams > 0) {
                        food.weightGrams
                    } else {
                        val def = FoodData.findByName(food.name)
                        if (def != null) {
                            // 从单位里提取克数，如"碗(150g)" → 150
                            val unitGrams = Regex("\\((\\d+)g\\)").find(def.defaultUnit)
                                ?.groupValues?.get(1)?.toDoubleOrNull() ?: 100.0
                            food.amount * unitGrams
                        } else {
                            food.amount * 100.0 // 兜底：按每份100g估算
                        }
                    }
                    val ratio = grams / 100.0
                    food.copy(
                        weightGrams = grams,
                        protein = knowledge.protein * ratio,
                        carbs = knowledge.carbs * ratio,
                        fat = knowledge.fat * ratio
                    )
                } else {
                    // 知识库无匹配，用通用估算（一般混合食物约：蛋白10%，碳水15%，脂肪8%按热量）
                    val estimatedWeight = if (food.weightGrams > 0) food.weightGrams else food.amount * 100.0
                    food.copy(
                        weightGrams = estimatedWeight,
                        protein = estimatedWeight * 0.10,
                        carbs = estimatedWeight * 0.15,
                        fat = estimatedWeight * 0.08
                    )
                }
            } else food
        }
    }
}

// ═══ 数据模型 ═══
data class ChatMessage(
    val role: String,
    val content: String,
    val isFoodAnalysis: Boolean = false,
    val pendingSaveFoods: List<FoodItem> = emptyList(),
    val pendingSaveReport: String = "",
    val showActions: Boolean = false,
    val imagePath: String? = null,
    val timestamp: String = ""
)

data class AiParseResult(val foods: List<AiFoodItem> = emptyList(), val total_cal: Double? = null, val total_protein: Double? = null, val total_carbs: Double? = null, val total_fat: Double? = null, val ai_report: String? = "")
data class AiFoodItem(val name: String = "", val amount: Double? = null, val unit: String? = null, val weight_g: Double? = null, val cal: Double? = null, val protein: Double? = null, val carbs: Double? = null, val fat: Double? = null)
