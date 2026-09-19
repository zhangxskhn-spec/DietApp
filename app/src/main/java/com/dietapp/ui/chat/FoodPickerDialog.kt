package com.dietapp.ui.chat

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dietapp.R
import com.dietapp.data.FoodCategory
import com.dietapp.data.FoodData
import com.dietapp.data.FoodItemDef
import com.dietapp.data.FoodSubcategory
import com.dietapp.data.api.FoodItem
import com.dietapp.databinding.DialogFoodPickerBinding
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * 多级联选食物选择器
 *
 * 导航层级：分类 → 子类 → 食物
 * 底部显示已选列表，确认后直接交给 AI 分析（餐次在保存日记时再选）
 */
class FoodPickerDialog(
    private val onFoodsSelected: (List<FoodItem>) -> Unit
) : DialogFragment() {

    // ── 导航状态 ──
    private sealed class NavLevel {
        data object Categories : NavLevel()
        data class Subcategories(val cat: FoodCategory) : NavLevel()
        data class Items(val cat: FoodCategory, val sub: FoodSubcategory) : NavLevel()
    }

    private var currentLevel: NavLevel = NavLevel.Categories
    private val selectedItems = mutableListOf<SelectedFood>()  // 已选食物
    private val allCategories: List<FoodCategory> = FoodData.categories
        .filter { it.name !in setOf("套餐", "川菜") }

    // ── 已选食物条目 ──
    data class SelectedFood(
        val def: FoodItemDef,
        var amount: Double = 1.0,
        var prep: String = "" // 烹饪方式（蔬菜类适用）
    )

    // ── 绑定 ──
    private var _binding: DialogFoodPickerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): android.app.Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        // 拦截系统返回键：有上级时返回上级，在分类层才关闭
        dialog.setOnKeyListener { _, keyCode, event ->
            if (keyCode == android.view.KeyEvent.KEYCODE_BACK && event.action == android.view.KeyEvent.ACTION_DOWN) {
                if (currentLevel !is NavLevel.Categories) {
                    goBack()
                } else {
                    dismiss()
                }
                true
            } else false
        }
        dialog.window?.apply {
            setLayout(
                (resources.displayMetrics.widthPixels * 0.92).toInt(),
                (resources.displayMetrics.heightPixels * 0.85).toInt()
            )
            setGravity(Gravity.CENTER)
            setBackgroundDrawableResource(android.R.color.transparent)
            decorView.setPadding(0, 0, 0, 0)
        }
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogFoodPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnClose.setOnClickListener { dismiss() }
        binding.btnBack.setOnClickListener { goBack() }

        binding.rvContent.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSelected.layoutManager = LinearLayoutManager(requireContext())

        binding.btnSaveFoods.setOnClickListener { onSaveClicked() }

        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val query = s?.toString()?.trim() ?: ""
                if (query.length >= 2) {
                    showSearchResults(query)
                } else {
                    showCurrentLevel()
                }
            }
        })

        showCategories()
        updateSelectedBar()
    }

    private fun showCurrentLevel() {
        when (val level = currentLevel) {
            is NavLevel.Categories -> showCategories()
            is NavLevel.Subcategories -> showSubcategories(level.cat)
            is NavLevel.Items -> showItems(level.cat, level.sub)
        }
    }

    private fun showSearchResults(query: String) {
        binding.tvTitle.text = "搜索：$query"
        binding.tvBreadcrumb.text = "← 返回分类"
        binding.tvBreadcrumb.visibility = View.VISIBLE
        binding.tvBreadcrumb.setOnClickListener { binding.etSearch.setText(""); showCategories() }
        binding.btnBack.visibility = View.VISIBLE

        val results = mutableListOf<Pair<FoodCategory, FoodItemDef>>()
        for (cat in allCategories) {
            for (sub in cat.subcats) {
                for (item in sub.items) {
                    if (item.name.contains(query, ignoreCase = true)) {
                        results.add(cat to item)
                    }
                }
            }
        }

        binding.rvContent.adapter = object : RecyclerView.Adapter<ItemVH>() {
            override fun onCreateViewHolder(p: ViewGroup, vt: Int): ItemVH {
                val root = layoutInflater.inflate(R.layout.item_food_item, p, false)
                return ItemVH(root)
            }
            override fun onBindViewHolder(h: ItemVH, i: Int) {
                val (cat, item) = results[i]
                h.name.text = "${item.emoji}  ${item.name}"
                h.cal.text = "${item.defaultCal.toInt()} kcal"
                h.unit.text = "/ ${item.defaultUnit}"
                val isSelected = selectedItems.any { it.def.name == item.name }
                h.root.setBackgroundColor(if (isSelected) android.graphics.Color.parseColor("#EEF9F0") else android.graphics.Color.WHITE)
                h.root.setOnClickListener { addFoodItem(item, cat) }
            }
            override fun getItemCount() = results.size
        }
    }

    // ═══════════════════════════════════════════
    //  导航渲染
    // ═══════════════════════════════════════════

    private fun showCategories() {
        currentLevel = NavLevel.Categories
        binding.tvTitle.text = "选择分类"
        binding.tvBreadcrumb.visibility = View.GONE
        binding.btnBack.visibility = View.GONE

        binding.rvContent.adapter = object : RecyclerView.Adapter<CatVH>() {
            override fun onCreateViewHolder(p: ViewGroup, vt: Int): CatVH {
                val root = layoutInflater.inflate(R.layout.item_food_category, p, false)
                return CatVH(root)
            }
            override fun onBindViewHolder(h: CatVH, i: Int) {
                val cat = allCategories[i]
                h.name.text = "${cat.emoji}  ${cat.name}"
                h.count.text = "${cat.subcats.sumOf { it.items.size }}种"
                h.root.setOnClickListener { showSubcategories(cat) }
                h.root.setBackgroundColor(android.graphics.Color.WHITE)
            }
            override fun getItemCount() = allCategories.size
        }
    }

    private fun showSubcategories(cat: FoodCategory) {
        currentLevel = NavLevel.Subcategories(cat)
        binding.tvTitle.text = "${cat.emoji}  ${cat.name}"
        binding.tvBreadcrumb.text = "← 返回分类 · ${cat.name}"
        binding.tvBreadcrumb.visibility = View.VISIBLE
        binding.tvBreadcrumb.setOnClickListener { showCategories() }
        binding.btnBack.visibility = View.VISIBLE

        binding.rvContent.adapter = object : RecyclerView.Adapter<SubVH>() {
            override fun onCreateViewHolder(p: ViewGroup, vt: Int): SubVH {
                val root = layoutInflater.inflate(R.layout.item_food_subcategory, p, false)
                return SubVH(root)
            }
            override fun onBindViewHolder(h: SubVH, i: Int) {
                val sub = cat.subcats[i]
                h.name.text = sub.name
                h.count.text = "${sub.items.size}种"
                h.root.setOnClickListener { showItems(cat, sub) }
            }
            override fun getItemCount() = cat.subcats.size
        }
    }

    private fun showItems(cat: FoodCategory, sub: FoodSubcategory) {
        currentLevel = NavLevel.Items(cat, sub)
        binding.tvTitle.text = sub.name
        binding.tvBreadcrumb.text = "← 返回分类 · ${cat.name} > ${sub.name}"
        binding.tvBreadcrumb.setOnClickListener { showCategories() }
        binding.btnBack.visibility = View.VISIBLE

        binding.rvContent.adapter = object : RecyclerView.Adapter<ItemVH>() {
            override fun onCreateViewHolder(p: ViewGroup, vt: Int): ItemVH {
                val root = layoutInflater.inflate(R.layout.item_food_item, p, false)
                return ItemVH(root)
            }
            override fun onBindViewHolder(h: ItemVH, i: Int) {
                val item = sub.items[i]
                h.name.text = "${item.emoji}  ${item.name}"
                h.cal.text = "${item.defaultCal.toInt()} kcal"
                h.unit.text = "/ ${item.defaultUnit}"

                // 如果已选中，显示高亮
                val isSelected = selectedItems.any { it.def.name == item.name }
                h.root.setBackgroundColor(if (isSelected) Color.parseColor("#EEF9F0") else Color.WHITE)

                h.root.setOnClickListener { addFoodItem(item, cat) }
            }
            override fun getItemCount() = sub.items.size
        }
    }

    // ═══════════════════════════════════════════
    //  返回导航
    // ═══════════════════════════════════════════

    private fun goBack() {
        binding.etSearch.setText("")
        when (val level = currentLevel) {
            is NavLevel.Items -> showCategories()
            is NavLevel.Subcategories -> showCategories()
            else -> dismiss()
        }
    }

    // ═══════════════════════════════════════════
    //  添加食物 + 分量选择
    // ═══════════════════════════════════════════

    private val VEGETABLE_CATEGORIES = setOf("蔬菜")
    private val PREPARATIONS = listOf(
        "清炒" to 1.3,   // 热量倍数
        "白灼" to 1.05,
        "凉拌" to 1.15,
        "蒜蓉" to 1.2,
        "上汤" to 1.1,
        "清蒸" to 1.0
    )

    private fun addFoodItem(item: FoodItemDef, cat: FoodCategory) {
        // 如果已选中，直接移除（toggle）
        val existing = selectedItems.find { it.def.name == item.name }
        if (existing != null) {
            selectedItems.remove(existing)
            refreshCurrentView()
            updateSelectedBar()
            return
        }

        // 蔬菜类：先选烹饪方式
        if (cat.name in VEGETABLE_CATEGORIES) {
            showPrepPicker(item, cat)
        } else {
            showAmountPicker(item, "")
        }
    }

    private fun showPrepPicker(item: FoodItemDef, cat: FoodCategory) {
        val prepNames = PREPARATIONS.map { it.first }.toTypedArray()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("${item.emoji} ${item.name} — 烹饪方式")
            .setItems(prepNames) { _, which ->
                val prep = PREPARATIONS[which]
                showAmountPicker(item, prep.first, prep.second)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showAmountPicker(item: FoodItemDef, prep: String, calMultiplier: Double = 1.0) {
        val defaultAmount = item.defaultAmount
        val inputView = layoutInflater.inflate(R.layout.dialog_amount_picker, null)
        val tvName = inputView.findViewById<TextView>(R.id.tv_amount_name)
        val etAmount = inputView.findViewById<TextView>(R.id.et_amount_value)
        val tvUnit = inputView.findViewById<TextView>(R.id.tv_amount_unit)

        tvName.text = "${item.emoji} ${item.name}"
        if (prep.isNotEmpty()) tvName.text = "${tvName.text}（${prep}）"
        etAmount.text = String.format("%.1f", defaultAmount)
        tvUnit.text = item.defaultUnit

        // + / - 按钮
        inputView.findViewById<View>(R.id.btn_amount_minus).setOnClickListener {
            var v = etAmount.text.toString().toDoubleOrNull() ?: defaultAmount
            v = (v - 0.5).coerceAtLeast(0.5)
            etAmount.text = String.format("%.1f", v)
        }
        inputView.findViewById<View>(R.id.btn_amount_plus).setOnClickListener {
            var v = etAmount.text.toString().toDoubleOrNull() ?: defaultAmount
            v = (v + 0.5).coerceAtMost(10.0)
            etAmount.text = String.format("%.1f", v)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("选择分量")
            .setView(inputView)
            .setPositiveButton("添加") { _, _ ->
                val amount = etAmount.text.toString().toDoubleOrNull() ?: defaultAmount
                val food = SelectedFood(item, amount, prep)
                selectedItems.add(food)
                refreshCurrentView()
                updateSelectedBar()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    // ═══════════════════════════════════════════
    //  已选列表
    // ═══════════════════════════════════════════

    private fun updateSelectedBar() {
        if (selectedItems.isEmpty()) {
            binding.layoutSelected.visibility = View.GONE
            return
        }
        binding.layoutSelected.visibility = View.VISIBLE
        binding.tvSelectedCount.text = "已选 ${selectedItems.size} 项  ·  约 ${selectedItems.sumOf { (it.def.defaultCal * it.amount).toInt() }} kcal"

        binding.rvSelected.adapter = object : RecyclerView.Adapter<SelectedVH>() {
            override fun onCreateViewHolder(p: ViewGroup, vt: Int): SelectedVH {
                val root = layoutInflater.inflate(R.layout.item_selected_food, p, false)
                return SelectedVH(root)
            }
            override fun onBindViewHolder(h: SelectedVH, i: Int) {
                val sf = selectedItems[i]
                val prepStr = if (sf.prep.isNotEmpty()) "（${sf.prep}）" else ""
                h.name.text = "${sf.def.emoji} ${sf.def.name}$prepStr"
                h.amount.text = "x${String.format("%.1f", sf.amount)}"
                h.cal.text = "${(sf.def.defaultCal * sf.amount).toInt()} kcal"

                h.delete.setOnClickListener {
                    selectedItems.removeAt(i)
                    refreshCurrentView()
                    updateSelectedBar()
                }
            }
            override fun getItemCount() = selectedItems.size
        }
    }

    private fun refreshCurrentView() {
        // 通知当前 adapter 刷新（用于更新选中高亮）
        binding.rvContent.adapter?.notifyDataSetChanged()
    }

    private fun onSaveClicked() {
        if (selectedItems.isEmpty()) return

        val items = selectedItems.map { sf ->
            val cal = sf.def.defaultCal * sf.amount
            val name = if (sf.prep.isNotEmpty()) "${sf.prep}${sf.def.name}" else sf.def.name
            FoodItem(
                name = name,
                amount = sf.amount,
                unit = sf.def.defaultUnit,
                cal = cal
            )
        }
        onFoodsSelected(items)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ── ViewHolders ──
    class CatVH(val root: View) : RecyclerView.ViewHolder(root) {
        val name: TextView = root.findViewById(R.id.tv_cat_name)
        val count: TextView = root.findViewById(R.id.tv_cat_count)
    }
    class SubVH(val root: View) : RecyclerView.ViewHolder(root) {
        val name: TextView = root.findViewById(R.id.tv_sub_name)
        val count: TextView = root.findViewById(R.id.tv_sub_count)
    }
    class ItemVH(val root: View) : RecyclerView.ViewHolder(root) {
        val name: TextView = root.findViewById(R.id.tv_item_name)
        val cal: TextView = root.findViewById(R.id.tv_item_cal)
        val unit: TextView = root.findViewById(R.id.tv_item_unit)
    }
    class SelectedVH(val root: View) : RecyclerView.ViewHolder(root) {
        val name: TextView = root.findViewById(R.id.tv_sel_name)
        val amount: TextView = root.findViewById(R.id.tv_sel_amount)
        val cal: TextView = root.findViewById(R.id.tv_sel_cal)
        val delete: TextView = root.findViewById(R.id.tv_sel_delete)
    }
}
