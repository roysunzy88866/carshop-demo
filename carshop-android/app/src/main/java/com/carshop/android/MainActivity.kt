package com.carshop.android

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.EvStation
import androidx.compose.material.icons.outlined.LocalCarWash
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.carshop.android.data.ApiClient
import com.carshop.android.data.DeviceIdProvider
import com.carshop.android.designsystem.CarshopTheme
import com.carshop.android.designsystem.components.*
import com.carshop.android.designsystem.tokens.Sizing
import com.carshop.android.designsystem.tokens.Spacing
import com.carshop.android.ui.shell.AppShell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CarshopTheme {
                val nav = rememberNavController()

                // V4 / V5 验收钩子:启动后调一次 categoryApi.list() 把响应打到 logcat,
                // 09 / 10 会实装 repository / UI,这里只证明链路通。
                LaunchedEffect(Unit) {
                    val deviceId = DeviceIdProvider.get(applicationContext)
                    Log.i("CarshopVerify", "boot · USE_MOCK=${BuildConfig.USE_MOCK} · deviceId=$deviceId")
                    try {
                        val resp = withContext(Dispatchers.IO) {
                            ApiClient.categoryApi.list()
                        }
                        if (resp.isSuccessful) {
                            val body = resp.body()
                            Log.i(
                                "CarshopVerify",
                                "categories.list OK · code=${body?.code} · count=${body?.data?.size} · first=${body?.data?.firstOrNull()?.name}",
                            )
                        } else {
                            Log.w("CarshopVerify", "categories.list HTTP ${resp.code()}")
                        }
                    } catch (t: Throwable) {
                        Log.e("CarshopVerify", "categories.list FAILED", t)
                    }
                }

                AppShell(navController = nav)
            }
        }
    }
}

// ============================================================================
// Session 00 DesignSystemDemo —— 运行时不可达(不被 NavHost 注册),保留是为了
// Studio @Preview 给 09 / 10 session 写新页面时当"活的设计参考"。
// 用户钦定方案 A:MainActivity setContent 替换为 AppShell,demo 留作 Preview。
// ============================================================================

@Composable
fun DesignSystemDemo() {
    val railItems = listOf(
        RailItem("home", "商店", Icons.Outlined.ShoppingBag),
        RailItem("charge", "加油充电", Icons.Outlined.EvStation),
        RailItem("wash", "洗车保养", Icons.Outlined.LocalCarWash),
        RailItem("parts", "汽车用品", Icons.Outlined.DirectionsCar),
    )
    var selectedKey by remember { mutableStateOf("home") }

    Row(Modifier.fillMaxSize()) {
        CarshopRail(items = railItems, selectedKey = selectedKey, onSelect = { selectedKey = it })
        Column(Modifier.weight(1f).fillMaxHeight()) {
            CarshopTopBar(title = "商店 · 设计系统 Demo")
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.padScreenH, vertical = Spacing.s4),
                verticalArrangement = Arrangement.spacedBy(Spacing.s4),
            ) {
                ProductCardsSection()
                ButtonsSection()
                ChipsAndTagsSection()
                PricesSection()
                MiscSection()
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun ProductCardsSection() {
    SectionHeader("商品卡 · 实物 vs 服务券(双价规则)")
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.gapCardGrid)) {
        CarshopCard(modifier = Modifier.weight(1f)) {
            Text("米其林浩悦 4 ST 轮胎", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(Spacing.s2))
            Text("215/55 R17 · 静音舒适", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(Spacing.s3))
            CarshopPrice(
                priceCents = 89900,
                originalCents = 109900,
                type = ProductPriceType.Physical,
            )
        }
        CarshopCard(modifier = Modifier.weight(1f), selected = true) {
            Text("中石化加油卡 100 元", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(Spacing.s2))
            Text("全国通用 · 充值即用", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(Spacing.s3))
            CarshopPrice(priceCents = 9500, type = ProductPriceType.ServiceVoucher)
        }
    }
}

@Composable
private fun ButtonsSection() {
    SectionHeader("按钮")
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s3),
        verticalAlignment = Alignment.CenterVertically) {
        CarshopButton(text = "立即购买", onClick = {})
        CarshopButton(text = "加入收藏", onClick = {}, variant = CarshopButtonVariant.Secondary)
        CarshopButton(text = "查看更多", onClick = {}, variant = CarshopButtonVariant.Text)
        CarshopButton(text = "确认", onClick = {}, compact = true)
    }
}

@Composable
private fun ChipsAndTagsSection() {
    SectionHeader("Chip(可触控) & Tag(静态)")
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s3),
        verticalAlignment = Alignment.CenterVertically) {
        CarshopChip(text = "全部")
        CarshopChip(text = "在售", selected = true)
        CarshopChip(text = "已售罄")
        Spacer(Modifier.width(Spacing.s4))
        CarshopTag(text = "在售", kind = CarshopTagKind.OnSale)
        CarshopTag(text = "充电中", kind = CarshopTagKind.Charge)
        CarshopTag(text = "5 折券", kind = CarshopTagKind.Coupon)
        CarshopTag(text = "推荐", kind = CarshopTagKind.Recommend)
    }
}

@Composable
private fun PricesSection() {
    SectionHeader("价格三种尺寸")
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s5),
        verticalAlignment = Alignment.Bottom) {
        Column { Text("Display 44sp", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
            CarshopPrice(priceCents = 89900, type = ProductPriceType.Physical,
                size = CarshopPriceSize.Display) }
        Column { Text("Card 30sp", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
            CarshopPrice(priceCents = 9500, type = ProductPriceType.ServiceVoucher,
                size = CarshopPriceSize.Card) }
        Column { Text("LineItem 22sp", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
            CarshopPrice(priceCents = 12800, type = ProductPriceType.Physical,
                size = CarshopPriceSize.LineItem) }
    }
}

@Composable
private fun MiscSection() {
    SectionHeader("列表项 / Toast / 二维码框")
    CarshopCard {
        CarshopListItem(
            title = "中石化加油卡 100 元",
            subtitle = "全国通用 · 充值即用",
            onClick = {},
        )
        CarshopListItem(
            title = "壳牌洗车 5 次卡",
            subtitle = "上海可用门店 32 家",
            onClick = {},
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s4),
        verticalAlignment = Alignment.Top) {
        CarshopToast(text = "已加入收藏")
        Spacer(Modifier.width(Spacing.s4))
        CarshopQrCodeBox(remainingSeconds = 165) {
            Box(
                Modifier
                    .size(360.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
        }
    }
}

@Preview(name = "Carshop DS Demo · 1920x1080", widthDp = 1920, heightDp = 1080, showBackground = true)
@Composable
fun DesignSystemDemoPreview() {
    CarshopTheme { Surface(color = MaterialTheme.colorScheme.background) { DesignSystemDemo() } }
}
