package com.carshop.android.ui.checkout.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.carshop.android.data.dto.Order
import com.carshop.android.data.dto.OrderItem
import com.carshop.android.designsystem.components.CarshopButton
import com.carshop.android.designsystem.components.CarshopButtonVariant
import com.carshop.android.designsystem.components.CarshopEmpty
import com.carshop.android.designsystem.components.CarshopLoading
import com.carshop.android.designsystem.components.CarshopPrice
import com.carshop.android.designsystem.components.CarshopTopBar
import com.carshop.android.designsystem.components.ProductPriceType
import com.carshop.android.ui.checkout.confirm.formatYuan

@Composable
fun OrderDetailScreen(navController: NavHostController) {
    val viewModel: OrderDetailViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        CarshopTopBar(
            title = "订单详情",
            leading = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                }
            },
        )
        // 2026-05-27 撤 Q5 加的 TopBar 分隔线(原型没有)
        when (val s = state) {
            is OrderDetailUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CarshopLoading()
            }
            is OrderDetailUiState.NotFound -> CarshopEmpty(
                title = "这单已经找不到了",
                subtitle = "可能已被清理,或者不属于这台车机",
                action = {
                    CarshopButton(
                        text = "返回",
                        variant = CarshopButtonVariant.Secondary,
                        onClick = { navController.popBackStack() },
                    )
                },
            )
            is OrderDetailUiState.Error -> CarshopEmpty(
                title = "哎呀,网络走丢了",
                subtitle = s.message,
                action = {
                    CarshopButton(text = "重试", onClick = { viewModel.retry() })
                },
            )
            is OrderDetailUiState.Success -> DetailContent(
                order = s.order,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DetailContent(order: Order, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 40.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        StatusBanner(status = order.status, paidAt = order.paidAt)
        OrderMetaCard(orderId = order.id, createdAt = order.createdAt, paidAt = order.paidAt)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Column(modifier = Modifier.weight(1.4f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ShippingDisplayCard(order = order)
                ItemsCard(items = order.items)
            }
            Column(modifier = Modifier.weight(1f)) {
                TotalCard(totalAmount = order.totalAmount, status = order.status)
            }
        }
    }
}

@Composable
private fun StatusBanner(status: String, paidAt: String?) {
    val isPaid = status == "paid"
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isPaid) MaterialTheme.colorScheme.tertiaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (isPaid) Icons.Outlined.CheckCircle else Icons.Outlined.Schedule,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = if (isPaid) MaterialTheme.colorScheme.tertiary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = if (isPaid) "已支付" else "待支付",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp),
                    fontWeight = FontWeight.Bold,
                    color = if (isPaid) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (isPaid) "感谢你的支持 · 完成于 ${formatTime(paidAt)}"
                    else "等你支付,2 分钟内有效",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun OrderMetaCard(orderId: String, createdAt: String, paidAt: String?) {
    SectionCard {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "订单号",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    orderId,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                    ),
                    fontWeight = FontWeight.Medium,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "下单时间",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    formatTime(createdAt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (paidAt != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "支付时间",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        formatTime(paidAt),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ShippingDisplayCard(order: Order) {
    SectionCard {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "收货信息",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Row {
                Text(order.shippingInfo.name, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(16.dp))
                Text(
                    order.shippingInfo.phone,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                order.shippingInfo.address,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ItemsCard(items: List<OrderItem>) {
    SectionCard {
        Column(Modifier.padding(20.dp)) {
            Text(
                "商品 · ${items.size} 件",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            items.forEachIndexed { idx, item ->
                Spacer(Modifier.height(if (idx == 0) 12.dp else 0.dp))
                if (idx > 0) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(12.dp))
                }
                ItemRow(item)
            }
        }
    }
}

@Composable
private fun ItemRow(item: OrderItem) {
    // SPEC §5:用 product_snapshot 渲染,不二次查商品
    val snap = item.productSnapshot
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = snap.mainImageUrl,
                contentDescription = snap.title,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                snap.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
            )
            if (!snap.spec.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    snap.spec,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Text(
            "×${item.quantity}",
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(16.dp))
        CarshopPrice(
            priceCents = item.price,
            type = if (snap.productType == "service_voucher")
                ProductPriceType.ServiceVoucher else ProductPriceType.Physical,
        )
    }
}

@Composable
private fun TotalCard(totalAmount: Int, status: String) {
    SectionCard {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("订单金额", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row {
                Text(
                    "应付",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    formatYuan(totalAmount),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    if (status == "paid") "实付" else "待支付",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    formatYuan(totalAmount),
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 32.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        content()
    }
}

// ISO 8601 → 简短可读 "MM-dd HH:mm"。失败原样返回。
internal fun formatTime(iso: String?): String {
    if (iso.isNullOrBlank()) return "—"
    // 形如 "2026-05-25T18:57:14.033006+08:00"
    return runCatching {
        val (date, rest) = iso.split('T', limit = 2)
        val hhmm = rest.take(5)
        val md = date.substring(5)  // MM-dd
        "$md $hhmm"
    }.getOrDefault(iso)
}
