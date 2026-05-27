package com.carshop.android.ui.checkout.confirm

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.carshop.android.data.dto.Product
import com.carshop.android.designsystem.components.CarshopButton
import com.carshop.android.designsystem.components.CarshopButtonVariant
import com.carshop.android.designsystem.components.CarshopEmpty
import com.carshop.android.designsystem.components.CarshopLoading
import com.carshop.android.designsystem.components.CarshopPrice
import com.carshop.android.designsystem.components.CarshopTopBar
import com.carshop.android.designsystem.components.ProductPriceType
import com.carshop.android.nav.Routes
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import androidx.compose.foundation.Image as FoundationImage
import android.widget.Toast

@Composable
fun OrderConfirmScreen(navController: NavHostController) {
    val viewModel: OrderConfirmViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // 支付成功 → 跳订单详情,清掉 confirm 这一层
    LaunchedEffect(Unit) {
        viewModel.paySuccessEvent.collect { orderId ->
            navController.navigate(Routes.OrderDetail.build(orderId)) {
                popUpTo(Routes.Home.path) { inclusive = false }
            }
        }
    }
    // Toast 错误 / 超时
    LaunchedEffect(Unit) {
        viewModel.toastEvent.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        CarshopTopBar(
            title = "确认订单",
            leading = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                }
            },
        )
        // 2026-05-27 撤 Q5 加的 TopBar 分隔线(原型没有)
        when (val s = state) {
            is OrderConfirmUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CarshopLoading()
            }
            is OrderConfirmUiState.Error -> CarshopEmpty(
                title = "哎呀,出问题了",
                subtitle = s.message,
                action = {
                    CarshopButton(
                        text = "返回",
                        variant = CarshopButtonVariant.Secondary,
                        onClick = { navController.popBackStack() },
                    )
                },
            )
            is OrderConfirmUiState.Ready -> {
                ConfirmContent(
                    product = s.product,
                    quantity = 1,
                    isSubmitting = s.isSubmitting,
                    onSubmit = { viewModel.submitOrder() },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }

    // PayDialog overlay(Material3 Dialog,480dp 宽,radius 16dp,点击任意位置触发支付)
    val ready = state as? OrderConfirmUiState.Ready
    val payDialog = ready?.payDialog
    if (payDialog != null) {
        PayDialog(
            state = payDialog,
            onTriggerPay = { viewModel.triggerPay() },
        )
    }
}

@Composable
private fun ConfirmContent(
    product: Product,
    quantity: Int,
    isSubmitting: Boolean,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalAmount = product.price * quantity
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 40.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // 左列 1.4fr:收货地址 + 商品卡
            Column(modifier = Modifier.weight(1.4f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ShippingCard()
                ProductLineCard(product = product, quantity = quantity)
            }
            // 右列 1fr:价格明细
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                PriceDetailCard(price = product.price, totalAmount = totalAmount)
            }
        }
        // 底部 CTA(80dp 高)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        BottomCta(
            totalAmount = totalAmount,
            isSubmitting = isSubmitting,
            onSubmit = onSubmit,
        )
    }
}

@Composable
private fun ShippingCard() {
    // SPEC §5.1 写死默认收货信息(M-Address 模块尚未做,TD-008)
    SectionCard {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("收货信息", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                Text(
                    "Demo · 地址暂不可改",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
            Row {
                Text("车主", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(16.dp))
                Text(
                    "138****0000",
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                "上海市浦东新区世纪大道 100 号",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ProductLineCard(product: Product, quantity: Int) {
    SectionCard {
        Column(Modifier.padding(20.dp)) {
            Text(
                "商品 · ${quantity} 件",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    AsyncImage(
                        model = product.mainImageUrl,
                        contentDescription = product.title,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        product.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                    )
                    if (!product.spec.isNullOrBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            product.spec,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    "×$quantity",
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(16.dp))
                CarshopPrice(
                    priceCents = product.price,
                    type = if (product.productType == "service_voucher")
                        ProductPriceType.ServiceVoucher else ProductPriceType.Physical,
                )
            }
        }
    }
}

@Composable
private fun PriceDetailCard(price: Int, totalAmount: Int) {
    SectionCard {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("订单金额", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row {
                Text("商品小计", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                Text(
                    formatYuan(price),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Row {
                Text("运费", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                Text(
                    "¥0 · 包邮",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(verticalAlignment = Alignment.Bottom) {
                Text("应付", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
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
private fun BottomCta(totalAmount: Int, isSubmitting: Boolean, onSubmit: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 40.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "合计",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                formatYuan(totalAmount),
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        CarshopButton(
            text = if (isSubmitting) "提交中…" else "提交订单",
            enabled = !isSubmitting,
            onClick = onSubmit,
            modifier = Modifier.widthIn(min = 320.dp),
        )
    }
}

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        content()
    }
}

@Composable
private fun PayDialog(state: PayDialogUiState, onTriggerPay: () -> Unit) {
    // spec §1.1.B:Modal,radius 16dp,480dp 宽,点击 Modal 任意位置 → 1 秒后支付
    // 用 Material3 Dialog · 关闭键 / 外部点击都不 dismiss(只能等支付或倒计时)
    Dialog(
        onDismissRequest = { /* 不允许通过返回键 / 外部点击关闭 */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,  // 自己控制宽度
        ),
    ) {
        Surface(
            modifier = Modifier
                .width(480.dp)
                .clickable(enabled = !state.isPaying, onClick = onTriggerPay),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "扫码支付",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Demo · 点击任意位置完成支付",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
                Spacer(Modifier.height(20.dp))
                // QR · 320dp(留出 modal padding 空间)
                val painter = rememberQrCodePainter(data = "carshop://order/${state.order.id}")
                Box(
                    modifier = Modifier
                        .size(320.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    FoundationImage(
                        painter = painter,
                        contentDescription = "支付二维码",
                        modifier = Modifier.size(296.dp),
                    )
                }
                Spacer(Modifier.height(16.dp))
                // 倒计时 mono 大字
                val mm = state.remainingSeconds / 60
                val ss = state.remainingSeconds % 60
                Text(
                    text = if (state.isPaying) "支付中…" else "%02d:%02d".format(mm, ss),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 36.sp,
                    ),
                    fontWeight = FontWeight.Bold,
                    color = if (state.isPaying) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "订单号",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        state.order.id,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "应付金额",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        formatYuan(state.order.totalAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

internal fun formatYuan(cents: Int): String = "¥%.2f".format(cents / 100.0)
