package com.carshop.android.designsystem.tokens

import androidx.compose.ui.unit.dp

// 我们极少用阴影,卡片首选 e0(无阴影 + 1dp 描边)
object Elevation {
    val e0 = 0.dp   // 卡片首选 ★
    val e1 = 1.dp
    val e2 = 3.dp   // TopBar 滚动后
    val e3 = 6.dp   // Dialog / BottomSheet
    val e4 = 8.dp   // 菜单 / FAB
    val e5 = 12.dp  // Toast
}
