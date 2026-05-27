package com.carshop.android.ui.browse.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.carshop.android.data.dto.Banner
import com.carshop.android.designsystem.components.CarshopEmpty
import com.carshop.android.designsystem.components.CarshopLoading
import com.carshop.android.designsystem.components.CarshopTopBar
import com.carshop.android.designsystem.tokens.Radius
import com.carshop.android.designsystem.tokens.Sizing
import com.carshop.android.designsystem.tokens.Spacing
import com.carshop.android.nav.Routes
import com.carshop.android.ui.browse.ProductCard
import com.carshop.android.ui.browse.ProductImage
import com.carshop.android.ui.browse.materialIconForCategory
import androidx.compose.ui.text.font.FontFamily

private val BANNER_HEIGHT = 240.dp
private val CATEGORY_TILE_HEIGHT = 176.dp
private val RECOMMEND_CARD_WIDTH = 320.dp  // G-10: 280→320dp 大屏更有分量

@Composable
fun HomeScreen(navController: NavController) {
    val vm: HomeViewModel = viewModel()
    val banners by vm.banners.collectAsStateWithLifecycle()
    val categories by vm.categories.collectAsStateWithLifecycle()
    val recommend by vm.recommend.collectAsStateWithLifecycle()

    val allFailed = banners is HomeSectionState.Error &&
        categories is HomeSectionState.Error &&
        recommend is HomeSectionState.Error

    Column(Modifier.fillMaxSize()) {
        CarshopTopBar(
            title = "商店",
            actions = {
                IconButton(onClick = { vm.refreshAll() }) {
                    Icon(
                        Icons.Outlined.Refresh,
                        contentDescription = "刷新",
                        modifier = Modifier.size(Sizing.iconLg),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Spacer(Modifier.width(Spacing.s2))
                IconButton(onClick = { navController.navigate(Routes.OrderList.path) }) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ReceiptLong,
                        contentDescription = "我的订单",
                        modifier = Modifier.size(Sizing.iconLg),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            },
        )
        // 2026-05-27 撤掉 Q5 加的 TopBar 分隔线(用户反馈原型没有)
        if (allFailed) {
            CarshopEmpty(
                title = "哎呀,网络走丢了",
                subtitle = "检查一下网络再试试",
                action = {
                    androidx.compose.material3.OutlinedButton(onClick = { vm.refreshAll() }) {
                        Text("重试", style = MaterialTheme.typography.labelLarge)
                    }
                },
            )
            return@Column
        }

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                // G-04: 显式背景色 steel-50,确保内容区与 TopBar/Surface 一致
                .background(MaterialTheme.colorScheme.background)
                .padding(
                    start = Spacing.padScreenH,
                    end = Spacing.padScreenH,
                    top = Spacing.gapSection,   // G-01: 16dp → 24dp(gap-section)
                    bottom = Spacing.s5,
                ),
            verticalArrangement = Arrangement.spacedBy(Spacing.gapSection),  // G-02: 32dp → 24dp
        ) {
            BannerSection(state = banners, onBannerClick = { b -> handleBannerClick(b, navController) })
            CategorySection(
                state = categories,
                onCategoryClick = { id -> navController.navigate(Routes.CategoryProducts.build(id)) },
            )
            RecommendSection(
                state = recommend,
                onProductClick = { id -> navController.navigate(Routes.ProductDetail.build(id)) },
            )
        }
    }
}

private fun handleBannerClick(banner: Banner, nav: NavController) {
    when (banner.linkType) {
        "product" -> banner.linkTarget?.let { nav.navigate(Routes.ProductDetail.build(it)) }
        "category" -> banner.linkTarget?.let { nav.navigate(Routes.CategoryProducts.build(it)) }
        else -> Unit
    }
}

// ─── Banner 区 ──────────────────────────────────────────────────────────────
@Composable
private fun BannerSection(state: HomeSectionState<List<Banner>>, onBannerClick: (Banner) -> Unit) {
    when (state) {
        HomeSectionState.Loading -> SkeletonBlock(height = BANNER_HEIGHT)
        HomeSectionState.Empty -> Unit
        is HomeSectionState.Error -> SkeletonBlock(
            height = BANNER_HEIGHT,
            label = "Banner 加载失败",
        )
        is HomeSectionState.Success -> BannerCarousel(state.data, onBannerClick)
    }
}

@Composable
private fun BannerCarousel(banners: List<Banner>, onBannerClick: (Banner) -> Unit) {
    var index by remember(banners) { mutableIntStateOf(0) }
    val current = banners[index.coerceIn(0, banners.size - 1)]

    Box(
        Modifier
            .fillMaxWidth()
            .height(BANNER_HEIGHT)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onBannerClick(current) },
    ) {
        ProductImage(
            url = current.imageUrl,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            cornerRadius = 16.dp,
        )

        // 1/N 页码 · 右下角 半透明黑底白字 · mono
        if (banners.size > 1) {
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xCC1A2027))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            ) {
                Text(
                    "${index + 1}/${banners.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }

        // chevron 左右 · 24dp 离边 + 64dp 圆形按钮
        if (banners.size > 1) {
            BannerChevron(
                icon = Icons.Outlined.ChevronLeft,
                contentDescription = "上一张",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 24.dp),
                onClick = {
                    index = if (index == 0) banners.lastIndex else index - 1
                },
            )
            BannerChevron(
                icon = Icons.Outlined.ChevronRight,
                contentDescription = "下一张",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 24.dp),
                onClick = {
                    index = if (index == banners.lastIndex) 0 else index + 1
                },
            )
        }
    }
}

@Composable
private fun BannerChevron(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .size(64.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0x66000000))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(32.dp),
            tint = Color.White,
        )
    }
}

// ─── 分类入口栏 ──────────────────────────────────────────────────────────
@Composable
private fun CategorySection(
    state: HomeSectionState<List<com.carshop.android.data.dto.Category>>,
    onCategoryClick: (Int) -> Unit,
) {
    when (state) {
        HomeSectionState.Loading -> SkeletonRow(itemCount = 5, itemHeight = CATEGORY_TILE_HEIGHT)
        HomeSectionState.Empty -> Unit
        is HomeSectionState.Error -> SkeletonRow(
            itemCount = 5,
            itemHeight = CATEGORY_TILE_HEIGHT,
            label = "分类加载失败",
        )
        is HomeSectionState.Success -> {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(CATEGORY_TILE_HEIGHT),
                horizontalArrangement = Arrangement.spacedBy(Spacing.s3),
            ) {
                state.data.take(5).forEach { c ->
                    val isAccent = c.name == "加油充电"
                    // G-05/G-06/G-07/G-08: 换 M3 Card 直控 padding=28dp + Box居中布局 + badge
                    Card(
                        onClick = { onCategoryClick(c.id) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        shape = Radius.Card,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = BorderStroke(
                            Sizing.borderDefault,
                            MaterialTheme.colorScheme.outline,
                        ),
                    ) {
                        // ColumnScope → Box fills card → BoxScope for badge overlay
                        Box(Modifier.fillMaxSize()) {
                            // 内容区:垂直居中,左对齐
                            Column(
                                Modifier
                                    .fillMaxSize()
                                    .padding(28.dp),
                                verticalArrangement = Arrangement.Center,
                            ) {
                                // 图标容器 56×56(Q4 已对齐,此处保持)
                                Box(
                                    Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(Radius.md))
                                        .background(
                                            if (isAccent) MaterialTheme.colorScheme.tertiaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant,
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        materialIconForCategory(c.name),
                                        contentDescription = null,
                                        modifier = Modifier.size(Sizing.iconLg),
                                        tint = if (isAccent) MaterialTheme.colorScheme.tertiary
                                               else MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                                // G-07: 图标-名称间距 8dp → 14dp(对齐原型 gap:14px)
                                Spacer(Modifier.height(14.dp))
                                Text(
                                    c.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            // G-08: "加油充电" 右上角"推荐" badge(原型 .cat-tile .badge)
                            if (isAccent) {
                                Box(
                                    Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(top = 18.dp, end = 18.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(MaterialTheme.colorScheme.tertiary)
                                        .padding(horizontal = 12.dp, vertical = 5.dp),
                                ) {
                                    Text(
                                        "推荐",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onTertiary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── 推荐商品 LazyRow ────────────────────────────────────────────────────
@Composable
private fun RecommendSection(
    state: HomeSectionState<List<com.carshop.android.data.dto.Product>>,
    onProductClick: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.s3)) {
        // G-09: section head 改为 Row,右侧加"更多→"(tertiary色,装饰性)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                "为你推荐",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "更多 →",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
        when (state) {
            HomeSectionState.Loading -> SkeletonRow(itemCount = 5, itemHeight = 320.dp)
            HomeSectionState.Empty -> {
                CarshopEmpty(
                    title = "推荐准备中",
                    modifier = Modifier.height(240.dp),
                )
            }
            is HomeSectionState.Error -> SkeletonRow(
                itemCount = 5,
                itemHeight = 320.dp,
                label = "推荐加载失败",
            )
            is HomeSectionState.Success -> {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s3),
                ) {
                    items(state.data, key = { it.id }) { p ->
                        ProductCard(
                            product = p,
                            modifier = Modifier.width(RECOMMEND_CARD_WIDTH),
                            onClick = { onProductClick(p.id) },
                        )
                    }
                }
            }
        }
    }
}

// ─── 通用 skeleton(灰块)──────────────────────────────────────────────
@Composable
private fun SkeletonBlock(height: androidx.compose.ui.unit.Dp, label: String? = null) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (label != null) {
            Text(label, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SkeletonRow(itemCount: Int, itemHeight: androidx.compose.ui.unit.Dp, label: String? = null) {
    if (label != null) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(itemHeight),
            contentAlignment = Alignment.Center,
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    Row(
        Modifier.fillMaxWidth().height(itemHeight),
        horizontalArrangement = Arrangement.spacedBy(Spacing.s3),
    ) {
        repeat(itemCount) {
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
        }
    }
}
