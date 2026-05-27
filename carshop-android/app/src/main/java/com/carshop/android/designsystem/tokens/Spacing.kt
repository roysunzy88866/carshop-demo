package com.carshop.android.designsystem.tokens

import androidx.compose.ui.unit.dp

// 8dp 网格 · 全部用 token,不许在业务代码里写 magic number dp
object Spacing {
    val s0  = 0.dp
    val s1  = 4.dp     // 行内 icon-text
    val s2  = 8.dp     // 网格单元
    val s3  = 16.dp    // 卡片间 gap ★
    val s4  = 24.dp    // 卡片内 padding ★
    val s5  = 32.dp    // 大卡内 padding
    val s6  = 40.dp    // 页面左右边距 padScreenH ★
    val s7  = 48.dp
    val s8  = 64.dp    // 紧凑触控
    val s9  = 80.dp    // 主触控 ★
    val s10 = 96.dp
    val s11 = 128.dp

    val padScreenH  = s6   // 40
    val padScreenV  = s5   // 32
    val padCard     = s4   // 24
    val padCardLarge= s5   // 32
    val gapCardGrid = s3   // 16
    val gapSection  = s4   // 24
    val padButtonH  = s4   // 24
}
