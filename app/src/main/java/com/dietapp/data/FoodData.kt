package com.dietapp.data

data class FoodCategory(val name: String, val emoji: String, val subcats: List<FoodSubcategory>)
data class FoodSubcategory(val name: String, val items: List<FoodItemDef>)
data class FoodItemDef(val name: String, val emoji: String, val defaultCal: Double, val defaultAmount: Double = 1.0, val defaultUnit: String = "份")

object FoodData {
    val categories = listOf(
        FoodCategory("主食", "🍚", listOf(
            FoodSubcategory("🍚 米饭类", listOf(
                FoodItemDef("白米饭","🍚",230.0,1.0,"碗(150g)"),
                FoodItemDef("糙米饭","🍚",220.0,1.0,"碗(150g)"),
                FoodItemDef("蛋炒饭","🍳",320.0,1.0,"份(300g)"),
                FoodItemDef("杂粮饭","🌾",210.0,1.0,"碗(150g)")
            )),
            FoodSubcategory("🍜 面食类", listOf(
                FoodItemDef("面条","🍜",250.0,1.0,"碗"),
                FoodItemDef("牛肉面","🍜",400.0,1.0,"碗"),
                FoodItemDef("炸酱面","🍝",380.0,1.0,"碗"),
                FoodItemDef("凉面","🍝",300.0,1.0,"碗"),
                FoodItemDef("米线","🍜",280.0,1.0,"碗"),
                FoodItemDef("过桥米线","🍲",350.0,1.0,"份(500g)"),
                FoodItemDef("螺蛳粉","🍜",450.0,1.0,"份(400g)"),
                FoodItemDef("热干面","🍝",400.0,1.0,"碗"),
                FoodItemDef("拉面","🍜",380.0,1.0,"碗")
            )),
            FoodSubcategory("🍜 面点类", listOf(
                FoodItemDef("馒头","🍜",220.0,1.0,"个"),
                FoodItemDef("包子","🍜",210.0,1.0,"个"),
                FoodItemDef("饺子","🍜",240.0,10.0,"个"),
                FoodItemDef("馄饨","🍜",200.0,1.0,"碗"),
                FoodItemDef("烧卖","🍜",210.0,1.0,"份(100g)"),
                FoodItemDef("蒸饺","🍜",240.0,10.0,"个"),
                FoodItemDef("生煎","🍜",280.0,1.0,"份(120g)"),
                FoodItemDef("锅贴","🍜",260.0,1.0,"份(120g)"),
                FoodItemDef("汤圆","🍡",220.0,6.0,"个"),
                FoodItemDef("粽子","🍙",300.0,1.0,"个")
            )),
            FoodSubcategory("🌽 杂粮类", listOf(
                FoodItemDef("玉米","🌽",112.0,1.0,"根"),
                FoodItemDef("红薯","🍠",86.0,1.0,"个"),
                FoodItemDef("紫薯","🍠",82.0,1.0,"个"),
                FoodItemDef("土豆","🥔",81.0,1.0,"个"),
                FoodItemDef("山药","🥔",57.0,1.0,"根"),
                FoodItemDef("芋头","🥔",58.0,1.0,"个"),
                FoodItemDef("燕麦","🍜",150.0,1.0,"份(40g)"),
                FoodItemDef("全麦面包","🍞",127.0,2.0,"片"),
                FoodItemDef("小米粥","🍜",90.0,1.0,"碗"),
                FoodItemDef("油条","🍞",386.0,1.0,"根"),
                FoodItemDef("煎饼","🍞",350.0,1.0,"份(150g)")
            ))
        )),

        FoodCategory("肉类", "🍖", listOf(
            FoodSubcategory("🍖 猪肉", listOf(
                FoodItemDef("红烧肉","🍖",280.0,1.0,"份(200g)"),
                FoodItemDef("排骨","🍖",264.0,1.0,"份(200g)"),
                FoodItemDef("猪蹄","🐷",260.0,1.0,"个"),
                FoodItemDef("回锅肉","🍖",320.0,1.0,"份(200g)"),
                FoodItemDef("糖醋里脊","🍖",240.0,1.0,"份(200g)"),
                FoodItemDef("猪肝","🐷",130.0,1.0,"份(100g)")
            )),
            FoodSubcategory("🍗 鸡鸭", listOf(
                FoodItemDef("鸡胸肉","🍗",133.0,1.0,"块(150g)"),
                FoodItemDef("鸡腿","🍗",181.0,1.0,"个(120g)"),
                FoodItemDef("鸡翅","🍗",194.0,1.0,"个(50g)"),
                FoodItemDef("烤鸭","🍗",280.0,1.0,"份(150g)"),
                FoodItemDef("白切鸡","🍗",170.0,1.0,"份(150g)"),
                FoodItemDef("宫保鸡丁","🍗",180.0,1.0,"份(200g)")
            )),
            FoodSubcategory("🍖 牛羊肉", listOf(
                FoodItemDef("牛肉","🍖",125.0,1.0,"份(150g)"),
                FoodItemDef("牛腩","🍖",200.0,1.0,"份(200g)"),
                FoodItemDef("肥牛","🍖",250.0,1.0,"份(150g)"),
                FoodItemDef("牛排","🍖",280.0,1.0,"份(200g)"),
                FoodItemDef("羊肉","🐑",203.0,1.0,"份(150g)"),
                FoodItemDef("羊肉串","🍢",250.0,5.0,"串")
            )),
            FoodSubcategory("🐟 鱼虾海鲜", listOf(
                FoodItemDef("清蒸鱼","🐟",120.0,1.0,"份(200g)"),
                FoodItemDef("酸菜鱼","🐟",180.0,1.0,"份(500g)"),
                FoodItemDef("水煮鱼","🐟",250.0,1.0,"份(500g)"),
                FoodItemDef("烤鱼","🐟",300.0,1.0,"份(600g)"),
                FoodItemDef("虾仁","🦐",99.0,1.0,"份(100g)"),
                FoodItemDef("小龙虾","🦐",180.0,1.0,"份(500g)"),
                FoodItemDef("螃蟹","🦀",95.0,1.0,"只(200g)"),
                FoodItemDef("三文鱼","🍣",208.0,1.0,"份(150g)")
            )),
            FoodSubcategory("🍳 蛋类", listOf(
                FoodItemDef("鸡蛋","🍳",72.0,1.0,"个"),
                FoodItemDef("煎蛋","🍳",196.0,1.0,"个"),
                FoodItemDef("水煮蛋","🍳",72.0,1.0,"个"),
                FoodItemDef("茶叶蛋","🍳",80.0,1.0,"个"),
                FoodItemDef("皮蛋","🍳",70.0,1.0,"个"),
                FoodItemDef("卤蛋","🍳",75.0,1.0,"个")
            ))
        )),

        FoodCategory("蔬菜", "🌿", listOf(
            FoodSubcategory("🌿 叶菜类", listOf(
                FoodItemDef("白菜","🌿",17.0,1.0,"份(200g)"),
                FoodItemDef("菠菜","🌿",24.0,1.0,"份(200g)"),
                FoodItemDef("油菜","🌿",15.0,1.0,"份(200g)"),
                FoodItemDef("生菜","🌿",13.0,1.0,"份(200g)"),
                FoodItemDef("空心菜","🌿",22.0,1.0,"份(200g)"),
                FoodItemDef("韭菜","🌿",26.0,1.0,"份(200g)")
            )),
            FoodSubcategory("🌽 花瓜类", listOf(
                FoodItemDef("西兰花","🌽",34.0,1.0,"份(200g)"),
                FoodItemDef("菜花","🌽",25.0,1.0,"份(200g)"),
                FoodItemDef("黄瓜","🌶",15.0,1.0,"根(150g)"),
                FoodItemDef("冬瓜","🌶",12.0,1.0,"份(300g)"),
                FoodItemDef("南瓜","🎃",22.0,1.0,"份(200g)"),
                FoodItemDef("苦瓜","🌶",22.0,1.0,"根"),
                FoodItemDef("丝瓜","🌶",20.0,1.0,"根"),
                FoodItemDef("茄子","🍆",21.0,1.0,"根")
            )),
            FoodSubcategory("🍅 茄果类", listOf(
                FoodItemDef("番茄","🍅",18.0,1.0,"个"),
                FoodItemDef("胡萝卜","🍅",41.0,1.0,"根"),
                FoodItemDef("青椒","🌶",22.0,1.0,"个"),
                FoodItemDef("玉米笋","🌽",26.0,1.0,"份(100g)"),
                FoodItemDef("莲藕","🌿",74.0,1.0,"节(200g)"),
                FoodItemDef("白萝卜","🌿",16.0,1.0,"份(200g)"),
                FoodItemDef("莴笋","🌿",15.0,1.0,"份(200g)")
            )),
            FoodSubcategory("🍄 菌菇豆类", listOf(
                FoodItemDef("香菇","🍄",26.0,1.0,"份(100g)"),
                FoodItemDef("金针菇","🍄",32.0,1.0,"份(100g)"),
                FoodItemDef("蘑菇","🍄",20.0,1.0,"份(100g)"),
                FoodItemDef("木耳","🍄",21.0,1.0,"份(100g)"),
                FoodItemDef("豆腐","🌰",81.0,1.0,"块(200g)"),
                FoodItemDef("毛豆","🌰",131.0,1.0,"份(150g)"),
                FoodItemDef("豌豆","🌰",105.0,1.0,"份(150g)")
            ))
        )),

        FoodCategory("水果", "🍎", listOf(
            FoodSubcategory("🍎 常见水果", listOf(
                FoodItemDef("苹果","🍎",53.0,1.0,"个"),
                FoodItemDef("香蕉","🍌",93.0,1.0,"根"),
                FoodItemDef("橙子","🍊",47.0,1.0,"个"),
                FoodItemDef("梨","🍐",51.0,1.0,"个"),
                FoodItemDef("桃子","🍑",42.0,1.0,"个"),
                FoodItemDef("西瓜","🍉",31.0,1.0,"块(300g)"),
                FoodItemDef("哈密瓜","🍈",34.0,1.0,"块(200g)"),
                FoodItemDef("葡萄","🍇",45.0,1.0,"串(200g)")
            )),
            FoodSubcategory("🍇 浆果柑橘", listOf(
                FoodItemDef("草莓","🍓",32.0,10.0,"颗"),
                FoodItemDef("蓝莓","🍇",57.0,1.0,"盒"),
                FoodItemDef("柚子","🍊",42.0,2.0,"瓣"),
                FoodItemDef("橘子","🍊",45.0,1.0,"个"),
                FoodItemDef("柠檬","🍋",22.0,2.0,"片"),
                FoodItemDef("猕猴桃","🍍",61.0,1.0,"个"),
                FoodItemDef("芒果","🍍",35.0,1.0,"个"),
                FoodItemDef("菠萝","🍍",41.0,1.0,"份(200g)")
            )),
            FoodSubcategory("🍒 其他", listOf(
                FoodItemDef("樱桃","🍒",46.0,10.0,"颗"),
                FoodItemDef("红枣","🌰",264.0,3.0,"颗"),
                FoodItemDef("荔枝","🍇",66.0,5.0,"颗"),
                FoodItemDef("龙眼","🍇",60.0,5.0,"颗"),
                FoodItemDef("山竹","🍇",72.0,1.0,"个"),
                FoodItemDef("火龙果","🍍",55.0,1.0,"个"),
                FoodItemDef("牛油果","🍍",160.0,0.5,"个")
            ))
        )),

        FoodCategory("川菜", "🌶", listOf(
            FoodSubcategory("🌶 经典川菜", listOf(
                FoodItemDef("麻婆豆腐","🌶",120.0,1.0,"份(200g)"),
                FoodItemDef("宫保鸡丁","🌶",180.0,1.0,"份(200g)"),
                FoodItemDef("鱼香肉丝","🌶",165.0,1.0,"份(200g)"),
                FoodItemDef("回锅肉","🌶",320.0,1.0,"份(200g)"),
                FoodItemDef("水煮肉片","🌶",280.0,1.0,"份(200g)"),
                FoodItemDef("辣子鸡","🌶",250.0,1.0,"份(200g)")
            )),
            FoodSubcategory("🌶 麻辣烫/火锅", listOf(
                FoodItemDef("麻辣烫","🌶",350.0,1.0,"份(200g)"),
                FoodItemDef("冒菜","🌶",380.0,1.0,"份(200g)"),
                FoodItemDef("火锅","🍲",800.0,1.0,"次"),
                FoodItemDef("串串香","🍢",500.0,1.0,"份(200g)"),
                FoodItemDef("酸辣粉","🍜",350.0,1.0,"份(200g)")
            ))
        )),

        FoodCategory("零食", "🍪", listOf(
            FoodSubcategory("🍪 膨化零食", listOf(
                FoodItemDef("薯片","🍪",536.0,1.0,"包(75g)"),
                FoodItemDef("辣条","🌶",420.0,1.0,"袋(80g)"),
                FoodItemDef("饼干","🍪",450.0,1.0,"包(100g)"),
                FoodItemDef("虾条","🍪",500.0,1.0,"包"),
                FoodItemDef("爆米花","🍿",420.0,1.0,"份(100g)")
            )),
            FoodSubcategory("🍫 糖果巧克力", listOf(
                FoodItemDef("巧克力","🍫",546.0,1.0,"条(50g)"),
                FoodItemDef("糖果","🍬",400.0,5.0,"颗"),
                FoodItemDef("冰淇淋","🍦",207.0,1.0,"个(100g)"),
                FoodItemDef("蛋糕","🍰",350.0,1.0,"块(100g)"),
                FoodItemDef("面包","🍞",250.0,1.0,"个(80g)"),
                FoodItemDef("月饼","🍪",400.0,1.0,"个(100g)")
            )),
            FoodSubcategory("🌰 坚果", listOf(
                FoodItemDef("花生","🌰",563.0,1.0,"把(30g)"),
                FoodItemDef("核桃","🌰",654.0,2.0,"个(30g)"),
                FoodItemDef("腰果","🌰",553.0,1.0,"把(30g)"),
                FoodItemDef("杏仁","🌰",578.0,1.0,"把(30g)"),
                FoodItemDef("瓜子","🌻",580.0,1.0,"把(30g)")
            ))
        )),

        FoodCategory("饮品", "🍹", listOf(
            FoodSubcategory("🍹 碳酸饮料", listOf(
                FoodItemDef("可口可乐","🍹",142.0,1.0,"罐(330ml)"),
                FoodItemDef("百事可乐","🍹",142.0,1.0,"罐(330ml)"),
                FoodItemDef("雪碧","🍹",140.0,1.0,"罐(330ml)"),
                FoodItemDef("芬达","🍹",150.0,1.0,"罐(330ml)")
            )),
            FoodSubcategory("🍵 奶制品", listOf(
                FoodItemDef("纯牛奶","☕",135.0,1.0,"杯(250ml)"),
                FoodItemDef("酸奶","☕",144.0,1.0,"杯(200g)"),
                FoodItemDef("拿铁","☕",110.0,1.0,"杯(300ml)"),
                FoodItemDef("奶茶","☕",300.0,1.0,"杯(500ml)")
            )),
            FoodSubcategory("☕ 咖啡茶饮", listOf(
                FoodItemDef("美式咖啡","☕",15.0,1.0,"杯(300ml)"),
                FoodItemDef("拿铁咖啡","☕",110.0,1.0,"杯(300ml)"),
                FoodItemDef("绿茶","🍵",1.0,1.0,"杯"),
                FoodItemDef("红茶","🍵",1.0,1.0,"杯"),
                FoodItemDef("果汁","🍹",115.0,1.0,"杯(250ml)"),
                FoodItemDef("椰汁","🍹",80.0,1.0,"罐")
            )),
            FoodSubcategory("🍺 酒类", listOf(
                FoodItemDef("啤酒","🍺",150.0,1.0,"瓶(500ml)"),
                FoodItemDef("白酒","🍶",350.0,1.0,"两(50ml)"),
                FoodItemDef("红酒","🍷",125.0,1.0,"杯(150ml)")
            ))
        )),

        FoodCategory("套餐", "🍱", listOf(
            FoodSubcategory("🍱 套餐", listOf(
                FoodItemDef("一荤一素+米饭","🍱",650.0,1.0,"份(200g)"),
                FoodItemDef("两荤一素+米饭","🍱",850.0,1.0,"份(200g)"),
                FoodItemDef("一荤一素一面","🍜",700.0,1.0,"份(200g)"),
                FoodItemDef("麻辣烫+饭","🍲",750.0,1.0,"份(200g)"),
                FoodItemDef("火锅（自助）","🍲",1000.0,1.0,"次"),
                FoodItemDef("汉堡+薯条+可乐","🍔",700.0,1.0,"套餐"),
                FoodItemDef("KFC/麦当劳","🍔",650.0,1.0,"份(200g)"),
                FoodItemDef("学校食堂一餐","🍱",600.0,1.0,"份(200g)")
            ))
        )),

        FoodCategory("家常菜", "🍳", listOf(
            FoodSubcategory("🍳 热菜", listOf(
                FoodItemDef("西红柿炒蛋","🍳",120.0,1.0,"份(250g)"),
                FoodItemDef("酸辣土豆丝","🥔",110.0,1.0,"份(250g)"),
                FoodItemDef("手撕包菜","🌿",80.0,1.0,"份(250g)"),
                FoodItemDef("青椒肉丝","🍖",160.0,1.0,"份(250g)"),
                FoodItemDef("红烧茄子","🍆",90.0,1.0,"份(250g)"),
                FoodItemDef("鱼香茄子","🍆",120.0,1.0,"份(250g)")
            )),
            FoodSubcategory("🍲 汤羹", listOf(
                FoodItemDef("紫菜蛋花汤","🍲",30.0,1.0,"碗"),
                FoodItemDef("番茄蛋汤","🍲",40.0,1.0,"碗"),
                FoodItemDef("排骨汤","🍲",200.0,1.0,"碗"),
                FoodItemDef("鸡汤","🍲",150.0,1.0,"碗"),
                FoodItemDef("银耳汤","🍲",60.0,1.0,"碗")
            ))
        ))
    )

    // 用 HashMap 缓存，O(1) 查找替代嵌套循环
    private val foodMap: Map<String, FoodItemDef> by lazy {
        buildMap {
            for (cat in categories) {
                for (sub in cat.subcats) {
                    for (item in sub.items) {
                        put(item.name, item)
                    }
                }
            }
        }
    }

    fun findByName(name: String): FoodItemDef? = foodMap[name]
}
