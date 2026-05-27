package com.carshop.android.ui.browse.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.LocalCarWash
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.carshop.android.data.dto.Product
import com.carshop.android.designsystem.components.CarshopButton
import com.carshop.android.designsystem.components.CarshopButtonVariant
import com.carshop.android.designsystem.components.CarshopEmpty
import com.carshop.android.designsystem.components.CarshopLoading
import com.carshop.android.designsystem.components.CarshopPrice
import com.carshop.android.designsystem.components.CarshopPriceSize
import com.carshop.android.designsystem.components.CarshopTopBar
import com.carshop.android.designsystem.tokens.CarshopTextStyles
import com.carshop.android.designsystem.tokens.Radius
import com.carshop.android.designsystem.tokens.Sizing
import com.carshop.android.designsystem.tokens.Spacing
import com.carshop.android.nav.Routes
import com.carshop.android.ui.browse.ProductImage
import com.carshop.android.ui.browse.productPriceTypeOf

@Composable
fun ProductDetailScreen(navController: NavController) {
    val vm: ProductDetailViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()

    // TopBar 标题超长省略(CarshopTopBar 不支持 maxLines,前置截断)
    val titleText = (state as? ProductDetailState.Success)?.product?.title?.let {
        if (it.length > 20) it.take(20) + "…" else it
    } ?: "商品详情"

    Column(Modifier.fillMaxSize()) {
        CarshopTopBar(
            title = titleText,
            leading = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "返回",
                        modifier = Modifier.size(Sizing.iconLg),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            },
        )
        // 2026-05-27 撤 Q5 加的 TopBar 分隔线(原型没有)

        when (val s = state) {
            ProductDetailState.Loading -> CarshopLoading()
            ProductDetailState.NotFound -> CarshopEmpty(
                title = "这个商品已经找不到了",
                icon = Icons.Outlined.SearchOff,
                action = {
                    OutlinedButton(onClick = { navController.popBackStack() }) {
                        Text("返回", style = MaterialTheme.typography.labelLarge)
                    }
                },
            )
            is ProductDetailState.Error -> CarshopEmpty(
                title = if (s.isNetwork) "哎呀,网络走丢了" else "加载失败",
                subtitle = if (s.isNetwork) "检查一下网络再试试" else "服务器开小差,稍后再试",
                icon = if (s.isNetwork) Icons.Outlined.WifiOff else null,
                action = {
                    OutlinedButton(onClick = { vm.refresh() }) {
                        Text("重试", style = MaterialTheme.typography.labelLarge)
                    }
                },
            )
            is ProductDetailState.Success -> DetailContent(
                product = s.product,
                onBuy = { navController.navigate(Routes.OrderConfirm.build(s.product.id)) },
            )
        }
    }
}

@Composable
private fun DetailContent(product: Product, onBuy: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        // 左右分屏内容区 · Row weight(1f) 填满 TopBar 与 CTA 之间的空间
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = Spacing.padScreenH, vertical = Spacing.s4),
            horizontalArrangement = Arrangement.spacedBy(Spacing.s5),
        ) {
            // 左:图片 / 品牌卡 · weight(1f) 与右侧 50/50(原型 grid-template-columns: 800px 1fr)
            Column(Modifier.weight(1f)) {
                val leftMod = Modifier
                    .fillMaxWidth()
                    .aspectRatio(800f / 560f)   // 原型 gallery__main 800×560
                if (product.productType == "service_voucher") {
                    ServiceVoucherCard(product = product, modifier = leftMod)
                } else {
                    ProductImage(
                        url = product.mainImageUrl,
                        modifier = leftMod,
                        cornerRadius = Radius.lg,
                    )
                }
            }

            // 右:标题 / 价格 / 划线原价 / 规格 / 描述
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.s3),
            ) {
                Text(
                    text = product.title,
                    style = CarshopTextStyles.ProductTitleDetail,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                )

                CarshopPrice(
                    priceCents = product.price,
                    originalCents = product.originalPrice,
                    type = productPriceTypeOf(product.productType),
                    size = CarshopPriceSize.Display,
                )

                // 2026-05-27 用户要求:加 hard-code 假数据(已售/7天无理由/24h 极速发货)
                // 已售数用 product.id 派生,保证同一商品稳定不抖
                if (product.onSale) {
                    val soldCount = 100 + (product.id * 73) % 900
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.s2),
                    ) {
                        Text(
                            "已售 $soldCount 件",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "·",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "7天无理由退",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "·",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "24h 极速发货",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                if (!product.onSale) {
                    Surface(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Text(
                            "商品已下架",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(
                                horizontal = Spacing.s3,
                                vertical = Spacing.s2,
                            ),
                        )
                    }
                }

                if (!product.spec.isNullOrBlank()) {
                    Spacer(Modifier.height(Spacing.s2))
                    LabeledRow(label = "规格", value = product.spec)
                }

                Spacer(Modifier.height(Spacing.s3))
                Text(
                    "商品描述",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    product.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // 底部固定 CTA 区(80dp · 对齐 HTML --h-cta-bar: 80px)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
        Surface(
            tonalElevation = 0.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .height(Sizing.touchTarget),  // 80dp 精确
        ) {
            Row(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = Spacing.padScreenH),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CarshopButton(
                    text = if (product.onSale) "立即购买" else "商品已下架",
                    onClick = onBuy,
                    enabled = product.onSale,
                    variant = CarshopButtonVariant.Primary,
                )
            }
        }
    }
}

/**
 * 服务券装饰卡 · 替代图片轮播(原型 屏5 brand-card)
 * 石墨蓝背景 + seafoam 光晕 + 大图标 + 券类型文字
 * 数据来自 product.title 关键词映射,不引入新 API 字段
 */
@Composable
private fun ServiceVoucherCard(product: Product, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(Radius.CardLarge)
            .background(MaterialTheme.colorScheme.primary),
    ) {
        // seafoam 光晕 · 右上角装饰圆(原型 ::before 伪元素)
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .size(280.dp)
                .offset(x = 80.dp, y = (-80).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f),
                            Color.Transparent,
                        ),
                    ),
                    CircleShape,
                ),
        )
        // 内容:overline + 大图标 + 中文类型名
        Column(
            Modifier
                .fillMaxSize()
                .padding(Spacing.padCardLarge),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "SERVICE · VOUCHER",
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 4.sp,
                color = MaterialTheme.colorScheme.tertiary,
            )
            Icon(
                imageVector = serviceVoucherIconFor(product),
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
            )
            Text(
                text = serviceVoucherLabelFor(product),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

private fun serviceVoucherIconFor(product: Product): ImageVector = when {
    product.title.contains("加油") ||
        product.title.contains("充电") ||
        product.title.contains("超充") -> Icons.Outlined.LocalGasStation
    product.title.contains("洗车") -> Icons.Outlined.LocalCarWash
    else -> Icons.Outlined.Storefront
}

private fun serviceVoucherLabelFor(product: Product): String = when {
    product.title.contains("加油") -> "加油卡"
    product.title.contains("超充") || product.title.contains("充电月卡") -> "充电月卡"
    product.title.contains("充电") -> "充电卡"
    product.title.contains("洗车") -> "洗车券"
    product.title.contains("保养") -> "保养券"
    product.title.contains("停车") -> "停车券"
    else -> "服务券"
}

@Composable
private fun LabeledRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            "$label · ",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
