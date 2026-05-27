package com.carshop.android.ui.checkout.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.carshop.android.data.dto.Order
import com.carshop.android.designsystem.components.CarshopButton
import com.carshop.android.designsystem.components.CarshopEmpty
import com.carshop.android.designsystem.components.CarshopLoading
import com.carshop.android.nav.Routes
import com.carshop.android.ui.checkout.confirm.formatYuan
import com.carshop.android.ui.checkout.detail.formatTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderListScreen(navController: NavHostController) {
    val viewModel: OrderListViewModel = viewModel()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // 顶部 Tab(Material 3 PrimaryTabRow · contentColor = tertiary 让默认 indicator
        // 自动跟着海泡青走,跟 Q4 服务券视觉一致;不自定义 indicator 以避免 BOM 跨版本
        // tabIndicatorOffset API 漂移)
        PrimaryTabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.tertiary,
        ) {
            OrderListTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { viewModel.selectTab(tab) },
                    modifier = Modifier.heightIn(min = 76.dp),  // Automotive 触控规范
                    selectedContentColor = MaterialTheme.colorScheme.tertiary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ) {
                    Text(
                        tab.label,
                        modifier = Modifier.padding(vertical = 20.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium,
                    )
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        when (val s = state) {
            is OrderListUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CarshopLoading()
            }
            is OrderListUiState.Empty -> CarshopEmpty(
                title = when (selectedTab) {
                    OrderListTab.All -> "还没有订单"
                    OrderListTab.Paid -> "还没有已支付订单"
                    OrderListTab.Pending -> "暂无待支付订单"
                },
                subtitle = "逛逛商品下一单",
                action = {
                    CarshopButton(
                        text = "去首页",
                        onClick = {
                            navController.navigate(Routes.Home.path) {
                                launchSingleTop = true
                            }
                        },
                    )
                },
            )
            is OrderListUiState.Error -> CarshopEmpty(
                title = "哎呀,网络走丢了",
                subtitle = s.message,
                action = {
                    CarshopButton(text = "重试", onClick = { viewModel.refresh() })
                },
            )
            is OrderListUiState.Success -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 40.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(s.orders, key = { it.id }) { order ->
                    OrderListCard(
                        order = order,
                        onClick = {
                            navController.navigate(Routes.OrderDetail.build(order.id))
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderListCard(order: Order, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    order.id,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                StatusTag(status = order.status)
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val firstItem = order.items.firstOrNull()
                if (firstItem != null) {
                    Box(
                        Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        AsyncImage(
                            model = firstItem.productSnapshot.mainImageUrl,
                            contentDescription = firstItem.productSnapshot.title,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            firstItem.productSnapshot.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                        )
                        if (order.items.size > 1) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "等 ${order.items.size} 件",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    Text(
                        "—",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        formatYuan(order.totalAmount),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        formatTime(order.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusTag(status: String) {
    val isPaid = status == "paid"
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isPaid) MaterialTheme.colorScheme.tertiaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            text = if (isPaid) "已支付" else "待支付",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isPaid) MaterialTheme.colorScheme.tertiary
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
