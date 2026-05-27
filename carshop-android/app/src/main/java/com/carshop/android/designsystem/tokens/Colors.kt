package com.carshop.android.designsystem.tokens

import androidx.compose.ui.graphics.Color

// 从 design/安卓商城/tokens.json v1.0.0 翻译 · 品牌方向 C · 海泡青电车感
// 命名规则:primitive(色阶) + semantic(M3 角色) + Custom(业务专用)

object CarshopColors {
    // ─── primitive · steel(石墨 / 港口灰阶)──────────────────────────────
    val Steel0   = Color(0xFFFFFFFF)
    val Steel50  = Color(0xFFF5F7F9) // 瓷白 · 屏幕底
    val Steel100 = Color(0xFFE8EDF1) // surface-variant
    val Steel200 = Color(0xFFD4DCE4) // outline
    val Steel300 = Color(0xFFB8C5CF) // 划线原价
    val Steel400 = Color(0xFF8FA4B1) // tertiary text
    val Steel500 = Color(0xFF5D7B8A) // secondary
    val Steel600 = Color(0xFF3F5867)
    val Steel700 = Color(0xFF2A3D49) // primary container
    val Steel800 = Color(0xFF1A2027) // primary · 主 CTA
    val Steel900 = Color(0xFF0A0E12)

    // ─── primitive · seafoam(海泡青)─────────────────────────────────────
    val Seafoam50  = Color(0xFFE6FBF6) // 充电标签底
    val Seafoam100 = Color(0xFFC2F5E8)
    val Seafoam500 = Color(0xFF00C2A8) // 品牌锚色
    val Seafoam600 = Color(0xFF00A892) // 服务券价
    val Seafoam700 = Color(0xFF008270)
    val Seafoam900 = Color(0xFF003D33)

    // ─── primitive · signal(信号红)──────────────────────────────────────
    val Signal50  = Color(0xFFFEF2F3)
    val Signal500 = Color(0xFFE63946) // 实物价 / 错误
    val Signal600 = Color(0xFFC9293A)
    val Signal700 = Color(0xFFA91823)
    val Signal900 = Color(0xFF570D12)

    // ─── 业务自定义(SPEC §5 product_type 双价规则)────────────────────
    val TextPrice          = Signal500   // physical 价格
    val TextPriceEnergy    = Seafoam600  // service_voucher 价格
    val TextPriceStrike    = Steel300    // 划线原价
    val BorderAccent       = Seafoam500  // 选中态 3dp 强调条
    val DotOnline          = Seafoam500
    val DotAlert           = Signal500

    val TagOnSaleBg        = Steel100
    val TagOnSaleText      = Steel700
    val TagChargeBg        = Seafoam50
    val TagChargeText      = Seafoam700
    val TagCouponBg        = Signal50
    val TagCouponText      = Signal700
    val TagRecommendBg     = Seafoam500
    val TagRecommendText   = Seafoam900
}
