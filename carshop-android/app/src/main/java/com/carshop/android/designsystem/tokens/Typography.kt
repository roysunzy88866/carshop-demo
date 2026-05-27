package com.carshop.android.designsystem.tokens

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// 字体族 · 思源黑体不打包,system fallback。Noto 在国内安卓基本都有。
val CarshopFontSans: FontFamily = FontFamily.SansSerif
val CarshopFontMono: FontFamily = FontFamily.Monospace

// M3 Typography 映射(对应 tokens.json typography 节)
val CarshopTypography = Typography(
    displayLarge = TextStyle(fontFamily = CarshopFontSans, fontWeight = FontWeight.Bold,     fontSize = 64.sp, lineHeight = 72.sp),
    displayMedium = TextStyle(fontFamily = CarshopFontSans, fontWeight = FontWeight.Bold,    fontSize = 56.sp, lineHeight = 64.sp),
    displaySmall = TextStyle(fontFamily = CarshopFontSans, fontWeight = FontWeight.Bold,     fontSize = 48.sp, lineHeight = 56.sp),

    headlineLarge = TextStyle(fontFamily = CarshopFontSans, fontWeight = FontWeight.Bold,    fontSize = 44.sp, lineHeight = 52.sp),
    headlineMedium = TextStyle(fontFamily = CarshopFontSans, fontWeight = FontWeight.Bold,   fontSize = 36.sp, lineHeight = 44.sp),
    headlineSmall = TextStyle(fontFamily = CarshopFontSans, fontWeight = FontWeight.Bold,    fontSize = 32.sp, lineHeight = 40.sp),

    titleLarge = TextStyle(fontFamily = CarshopFontSans, fontWeight = FontWeight.Bold,       fontSize = 28.sp, lineHeight = 36.sp),
    titleMedium = TextStyle(fontFamily = CarshopFontSans, fontWeight = FontWeight.SemiBold,  fontSize = 24.sp, lineHeight = 32.sp),
    titleSmall = TextStyle(fontFamily = CarshopFontSans, fontWeight = FontWeight.SemiBold,   fontSize = 22.sp, lineHeight = 30.sp),

    bodyLarge = TextStyle(fontFamily = CarshopFontSans, fontWeight = FontWeight.Normal,      fontSize = 20.sp, lineHeight = 28.sp),
    bodyMedium = TextStyle(fontFamily = CarshopFontSans, fontWeight = FontWeight.Normal,     fontSize = 18.sp, lineHeight = 26.sp),
    bodySmall = TextStyle(fontFamily = CarshopFontSans, fontWeight = FontWeight.Normal,      fontSize = 16.sp, lineHeight = 22.sp),

    labelLarge = TextStyle(fontFamily = CarshopFontSans, fontWeight = FontWeight.Medium,     fontSize = 22.sp, lineHeight = 28.sp),
    labelMedium = TextStyle(fontFamily = CarshopFontSans, fontWeight = FontWeight.Medium,    fontSize = 20.sp, lineHeight = 26.sp),
    labelSmall = TextStyle(fontFamily = CarshopFontSans, fontWeight = FontWeight.Medium,     fontSize = 16.sp, lineHeight = 22.sp),
)

// 业务专用字号 · 不进 M3 Typography slots
object CarshopTextStyles {
    val PriceDisplay = TextStyle(fontFamily = CarshopFontSans, fontWeight = FontWeight.Bold, fontSize = 44.sp, lineHeight = 48.sp)
    val PriceCard    = TextStyle(fontFamily = CarshopFontSans, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 34.sp)
    val PriceLineItem= TextStyle(fontFamily = CarshopFontSans, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp)
    val PriceStrike  = TextStyle(fontFamily = CarshopFontSans, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 20.sp)

    val ProductTitleCard   = TextStyle(fontFamily = CarshopFontSans, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 30.sp)
    val ProductTitleDetail = TextStyle(fontFamily = CarshopFontSans, fontWeight = FontWeight.Bold,     fontSize = 28.sp, lineHeight = 36.sp)

    val QrCountdown   = TextStyle(fontFamily = CarshopFontMono, fontWeight = FontWeight.Bold,   fontSize = 36.sp, lineHeight = 40.sp)
    val PlateMono     = TextStyle(fontFamily = CarshopFontMono, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 22.sp)
}
