package com.carshop.android.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.em
import com.carshop.android.designsystem.tokens.CarshopColors
import com.carshop.android.designsystem.tokens.CarshopTextStyles
import com.carshop.android.designsystem.tokens.Spacing

enum class ProductPriceType { Physical, ServiceVoucher }
enum class CarshopPriceSize { Display, Card, LineItem }

// 价格组件 · ¥ 缩小 0.5em · 自动按 productType 上色(SPEC §5)
// physical → 信号红 #E63946,service_voucher → 海泡青 #00A892
@Composable
fun CarshopPrice(
    /** 单位:分 */
    priceCents: Int,
    type: ProductPriceType,
    modifier: Modifier = Modifier,
    /** 单位:分,null 不显示划线原价 */
    originalCents: Int? = null,
    size: CarshopPriceSize = CarshopPriceSize.Card,
) {
    val style = when (size) {
        CarshopPriceSize.Display  -> CarshopTextStyles.PriceDisplay
        CarshopPriceSize.Card     -> CarshopTextStyles.PriceCard
        CarshopPriceSize.LineItem -> CarshopTextStyles.PriceLineItem
    }
    val color = when (type) {
        ProductPriceType.Physical       -> CarshopColors.TextPrice
        ProductPriceType.ServiceVoucher -> CarshopColors.TextPriceEnergy
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s2),
    ) {
        Text(text = formatPrice(priceCents), style = style, color = color)
        if (originalCents != null && originalCents > priceCents) {
            Text(
                text = "¥${"%.2f".format(originalCents / 100.0)}",
                style = CarshopTextStyles.PriceStrike.copy(textDecoration = TextDecoration.LineThrough),
                color = CarshopColors.TextPriceStrike,
            )
        }
    }
}

/** ¥ 字符按 0.5em 缩小,数字保持主字号 */
private fun formatPrice(cents: Int): AnnotatedString = buildAnnotatedString {
    withStyle(SpanStyle(fontSize = 0.5.em)) { append("¥") }
    append("%.2f".format(cents / 100.0))
}
