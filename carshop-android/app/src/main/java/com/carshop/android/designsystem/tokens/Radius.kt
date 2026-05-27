package com.carshop.android.designsystem.tokens

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

object Radius {
    val none = 0.dp
    val xs   = 4.dp    // tag
    val sm   = 8.dp    // 商品图 / icon tile
    val md   = 12.dp   // 主卡片 / 按钮 / 输入框 ★
    val lg   = 16.dp   // Hero / Dialog
    val xl   = 20.dp   // BottomSheet 顶部
    val xl2  = 24.dp
    val full = 9999.dp // chip / pill

    val Card        = RoundedCornerShape(md)
    val CardLarge   = RoundedCornerShape(lg)
    val Button      = RoundedCornerShape(md)
    val Image       = RoundedCornerShape(sm)
    val Tag         = RoundedCornerShape(xs)
    val Chip        = RoundedCornerShape(full)
    val Dialog      = RoundedCornerShape(lg)
    val BottomSheet = RoundedCornerShape(topStart = xl, topEnd = xl, bottomStart = none, bottomEnd = none)
}
