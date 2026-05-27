package com.carshop.android.ui.browse.category

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.carshop.android.designsystem.components.CarshopEmpty
import com.carshop.android.designsystem.components.CarshopLoading
import com.carshop.android.designsystem.components.CarshopTopBar
import com.carshop.android.designsystem.tokens.Sizing
import com.carshop.android.designsystem.tokens.Spacing
import com.carshop.android.nav.Routes
import com.carshop.android.ui.browse.ProductCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

private const val PREFETCH_DISTANCE = 9   // 提前 9 个 item(3 列 × 3 行)预加载下一页

@Composable
fun CategoryProductsScreen(navController: NavController) {
    val vm: CategoryProductsViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // 2026-05-27 用户反馈:撤 Q5 加的筛选按钮(无功能) + 分隔线(原型没有) + 标题字号加大
        CarshopTopBar(
            title = state.categoryName.ifEmpty { "加载中…" },
            titleStyle = MaterialTheme.typography.headlineMedium,
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

        when {
            state.isInitialLoading -> CarshopLoading()
            state.initialError != null -> InitialErrorOrEmpty(
                isNetwork = state.initialError!!.isNetwork,
                onRetry = { vm.loadFirstPage() },
            )
            state.isEmpty -> CarshopEmpty(
                title = "这里还没东西",
                subtitle = "去别的分类看看",
                icon = Icons.Outlined.Inbox,
            )
            else -> ProductsGrid(
                state = state,
                onProductClick = { id -> navController.navigate(Routes.ProductDetail.build(id)) },
                onLoadNext = { vm.loadNextPage() },
                onRetryAppend = { vm.retryAppend() },
            )
        }
    }
}

@Composable
private fun InitialErrorOrEmpty(isNetwork: Boolean, onRetry: () -> Unit) {
    CarshopEmpty(
        title = if (isNetwork) "哎呀,网络走丢了" else "加载失败",
        subtitle = if (isNetwork) "检查一下网络再试试" else "服务器开小差,稍后再试",
        icon = if (isNetwork) Icons.Outlined.WifiOff else null,
        action = {
            OutlinedButton(onClick = onRetry) {
                Text("重试", style = MaterialTheme.typography.labelLarge)
            }
        },
    )
}

@Composable
private fun ProductsGrid(
    state: CategoryProductsState,
    onProductClick: (Int) -> Unit,
    onLoadNext: () -> Unit,
    onRetryAppend: () -> Unit,
) {
    val gridState = rememberLazyGridState()

    // 滚到剩余 3 行触发下一页 · snapshotFlow 真正响应滚动(微交互 #8)
    LaunchedEffect(gridState, state.hasMore, state.isAppending) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1 }
            .distinctUntilChanged()
            .filter { it >= 0 }
            .collectLatest { lastVisible ->
                if (state.hasMore &&
                    !state.isAppending &&
                    state.appendError == null &&
                    lastVisible >= state.items.size - PREFETCH_DISTANCE
                ) {
                    onLoadNext()
                }
            }
    }

    Box(Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.padScreenH),
            contentPadding = PaddingValues(vertical = Spacing.s4),
            verticalArrangement = Arrangement.spacedBy(Spacing.s3),
            horizontalArrangement = Arrangement.spacedBy(Spacing.s3),
        ) {
            itemsIndexed(state.items, key = { _, p -> p.id }) { _, p ->
                ProductCard(
                    product = p,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onProductClick(p.id) },
                )
            }

            item(span = { GridItemSpan(3) }) {
                ListFooter(
                    isAppending = state.isAppending,
                    hasMore = state.hasMore,
                    appendError = state.appendError,
                    onRetry = onRetryAppend,
                )
            }
        }

        AppendToast(error = state.appendError, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun ListFooter(
    isAppending: Boolean,
    hasMore: Boolean,
    appendError: SectionError?,
    onRetry: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(80.dp),
        contentAlignment = Alignment.Center,
    ) {
        when {
            appendError != null -> Text(
                "加载失败,点击重试",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.clickable { onRetry() },
            )
            isAppending -> Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.s2),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.tertiary,
                )
                Text(
                    "加载中…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            !hasMore -> Text(
                "—— 没有更多了 ——",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            else -> Unit
        }
    }
}

/** 局部错误 Toast(下一页失败 2 秒消失) · 微交互 #7 */
@Composable
private fun AppendToast(error: SectionError?, modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(error) {
        if (error != null) {
            visible = true
            delay(2000)
            visible = false
        }
    }
    AnimatedVisibility(
        visible = visible && error != null,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier.padding(bottom = 24.dp),
    ) {
        Box(
            Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(Color(0xCC1A2027))
                .padding(horizontal = 24.dp, vertical = 12.dp),
        ) {
            Text(
                text = "加载失败,稍后再试",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
            )
        }
    }
}
