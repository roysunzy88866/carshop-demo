package com.carshop.android.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.carshop.android.designsystem.tokens.CarshopColors
import com.carshop.android.designsystem.tokens.Radius
import com.carshop.android.designsystem.tokens.Sizing
import com.carshop.android.designsystem.tokens.Spacing

enum class CarshopTagKind { OnSale, Charge, Coupon, Recommend, PreOrder }

// 32dp 高 · 静态(不可触控)· 多语义预设
@Composable
fun CarshopTag(
    text: String,
    modifier: Modifier = Modifier,
    kind: CarshopTagKind = CarshopTagKind.OnSale,
) {
    val (bg: Color, fg: Color) = when (kind) {
        CarshopTagKind.OnSale    -> CarshopColors.TagOnSaleBg    to CarshopColors.TagOnSaleText
        CarshopTagKind.Charge    -> CarshopColors.TagChargeBg    to CarshopColors.TagChargeText
        CarshopTagKind.Coupon    -> CarshopColors.TagCouponBg    to CarshopColors.TagCouponText
        CarshopTagKind.Recommend -> CarshopColors.TagRecommendBg to CarshopColors.TagRecommendText
        CarshopTagKind.PreOrder  -> CarshopColors.TagOnSaleBg    to CarshopColors.TagOnSaleText
    }
    Box(
        modifier = modifier
            .height(Sizing.heightTag)
            .background(bg, Radius.Tag)
            .padding(horizontal = Spacing.s2),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = fg)
    }
}
