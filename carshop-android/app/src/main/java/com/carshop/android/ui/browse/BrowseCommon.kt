package com.carshop.android.ui.browse

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.ImageNotSupported
import androidx.compose.material.icons.outlined.LocalCarWash
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.carshop.android.data.dto.Product
import com.carshop.android.designsystem.components.CarshopPrice
import com.carshop.android.designsystem.components.CarshopPriceSize
import com.carshop.android.designsystem.components.ProductPriceType
import com.carshop.android.designsystem.tokens.CarshopTextStyles
import com.carshop.android.designsystem.tokens.Radius
import com.carshop.android.designsystem.tokens.Sizing
import com.carshop.android.designsystem.tokens.Spacing

// 09 共享:分类名 → Material Icon(SVG 全 404 已知,TD-014 持续待还)
fun materialIconForCategory(name: String): ImageVector = when (name) {
    "汽车用品" -> Icons.Outlined.DirectionsCar
    "加油充电" -> Icons.Outlined.LocalGasStation
    "洗车保养" -> Icons.Outlined.LocalCarWash
    "周边餐饮" -> Icons.Outlined.Restaurant
    "旅行服务" -> Icons.Outlined.Map
    else       -> Icons.Outlined.Storefront
}

// SPEC §5:product_type 字符串 → enum(传错时归 physical 兜底)
fun productPriceTypeOf(s: String): ProductPriceType = when (s) {
    "service_voucher" -> ProductPriceType.ServiceVoucher
    else              -> ProductPriceType.Physical
}

/**
 * 商品图占位 · 加载中 surface-variant 灰、失败 ImageNotSupported icon + 小字
 * 09 微交互 #4
 */
@Composable
fun ProductImage(
    url: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    cornerRadius: Dp = 8.dp,
) {
    SubcomposeAsyncImage(
        model = url,
        contentDescription = null,
        contentScale = contentScale,
        modifier = modifier.clip(RoundedCornerShape(cornerRadius)),
        loading = {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
        },
        error = {
            Column(
                Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    Icons.Outlined.ImageNotSupported,
                    contentDescription = null,
                    modifier = Modifier.size(Sizing.iconLg),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(Spacing.s2))
                Text(
                    "图片加载失败",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
    )
}

/**
 * 商品卡(首页推荐横滑 + 分类列表网格)
 * 图片全出血贴卡片边缘 → 分隔线 → body 内边距(原型 .card--product)
 * 不用 CarshopCard:其内置 24dp 全边距会把图片内缩,偏离原型。
 */
@Composable
fun ProductCard(
    product: Product,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = Sizing.minCardProduct),
        shape = Radius.Card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(Sizing.borderDefault, MaterialTheme.colorScheme.outline),
    ) {
        // 全出血图片 4:3 · Box 叠加服务券类型 tag(原型 .tag--green 角标)
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f),
        ) {
            ProductImage(
                url = product.mainImageUrl,
                modifier = Modifier.fillMaxSize(),
                cornerRadius = 0.dp,
            )
            // 服务券类型 tag · 左下角 · seafoam 底色
            if (product.productType == "service_voucher") {
                val tagLabel = when {
                    product.title.contains("加油") -> "加油卡"
                    product.title.contains("充电") -> "充电卡"
                    product.title.contains("洗车") -> "洗车券"
                    product.title.contains("保养") -> "保养券"
                    else -> "服务券"
                }
                Box(
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 12.dp, bottom = 12.dp)
                        .clip(RoundedCornerShape(Radius.xs))
                        .background(MaterialTheme.colorScheme.tertiary)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                ) {
                    Text(
                        tagLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiary,
                    )
                }
            }
        }
        // 图片/内容分隔线(原型 border-bottom on .img)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
        // Body · 原型 padding: 20px 22px 22px
        Column(
            Modifier
                .padding(horizontal = 22.dp)
                .padding(top = 20.dp, bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.s2),
        ) {
            Text(
                product.title,
                style = CarshopTextStyles.ProductTitleCard,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.heightIn(min = 64.dp),
            )
            if (!product.spec.isNullOrBlank()) {
                Text(
                    product.spec,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(Spacing.s2))
            CarshopPrice(
                priceCents = product.price,
                originalCents = product.originalPrice,
                type = productPriceTypeOf(product.productType),
                size = CarshopPriceSize.Card,
            )
        }
    }
}
