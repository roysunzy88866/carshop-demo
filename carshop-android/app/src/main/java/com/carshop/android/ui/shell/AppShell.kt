package com.carshop.android.ui.shell

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.carshop.android.designsystem.components.CarshopRail
import com.carshop.android.designsystem.components.CarshopTopBar
import com.carshop.android.designsystem.components.RailItem
import com.carshop.android.nav.CarshopNavHost
import com.carshop.android.nav.Routes

// 整体壳:左 Rail(240dp,2 item)+ 主区(1680dp 占余宽)
// 2026-05-27 用户反馈调整:Rail 简化为【首页 / 订单】两项(去掉 08 占位的"关于";
// 设计稿原型 5 项【首页/分类/购物车/收藏/订单】里购物车/收藏 MVP 砍掉,"分类"无独立总览页用户决定不加)。
// Rail 选中态联动 NavController 当前 destination(spec §1.4)
//
// 09 偏离:Home / CategoryProducts / ProductDetail 三个路由的 TopBar 由各自 Screen
// 自己画(因为它们有定制的 leading / actions:返回箭头、刷新、我的订单按钮)。
// 10 增加 OrderConfirm / OrderDetail / OrderList 全自画 TopBar。
private val ROUTES_WITH_OWN_TOPBAR = setOf(
    Routes.Home.path,
    Routes.CategoryProducts.path,
    Routes.ProductDetail.path,
    Routes.OrderConfirm.path,
    Routes.OrderDetail.path,
    Routes.OrderList.path,
)

@Composable
fun AppShell(navController: NavHostController) {
    val railItems = listOf(
        RailItem(Routes.Home.path, "首页", Icons.Outlined.Home),
        RailItem(Routes.OrderList.path, "订单", Icons.Outlined.ShoppingBag),
    )

    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    // 子页面(category/product/order detail/confirm)落回最近的 Rail 入口
    val selectedKey = when {
        currentRoute == null -> Routes.Home.path
        currentRoute == Routes.OrderList.path ||
            currentRoute == Routes.OrderDetail.path -> Routes.OrderList.path
        else -> Routes.Home.path
    }

    val topBarTitle = when (selectedKey) {
        Routes.OrderList.path -> "订单"
        else -> "车机商店"
    }

    val screenOwnsTopBar = currentRoute in ROUTES_WITH_OWN_TOPBAR

    Surface(color = MaterialTheme.colorScheme.background) {
        Row(Modifier.fillMaxSize()) {
            CarshopRail(
                items = railItems,
                selectedKey = selectedKey,
                onSelect = { key ->
                    if (key != currentRoute) {
                        navController.navigate(key) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
            )
            Column(Modifier.weight(1f).fillMaxHeight()) {
                if (!screenOwnsTopBar) {
                    CarshopTopBar(title = topBarTitle)
                }
                CarshopNavHost(navController = navController, modifier = Modifier.weight(1f))
            }
        }
    }
}
