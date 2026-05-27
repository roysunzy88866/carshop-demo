package com.carshop.android.designsystem.tokens

import androidx.compose.ui.unit.dp

object Sizing {
    // 触控
    val touchTarget        = 80.dp   // 主触控 ★ 按钮 / TopBar / Rail / List
    val touchTargetCompact = 64.dp
    val heightChip         = 48.dp
    val heightTag          = 32.dp
    val heightTagLarge     = 40.dp
    val heightHero         = 240.dp

    // 导航与内容
    val widthRail    = 240.dp   // 左侧 Rail ★
    val widthContent = 1680.dp  // 1920 - 240

    // 商品 / icon
    val minCatTile        = 176.dp
    val minCardProduct    = 320.dp
    val minCardService    = 176.dp

    val iconSm = 20.dp
    val iconMd = 28.dp
    val iconLg = 32.dp
    val iconXl = 48.dp

    val avatarSm = 40.dp
    val avatarMd = 56.dp
    val avatarLg = 80.dp

    val thumbSm = 80.dp
    val thumbMd = 112.dp
    val thumbLg = 160.dp

    val qrCode = 480.dp // ★ 扫码弹窗

    // 描边宽度
    val borderDefault = 1.dp
    val borderThick   = 2.dp
    val borderAccent  = 3.dp     // 选中态海泡青条
}
