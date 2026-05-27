package com.carshop.android.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.carshop.android.ui.browse.category.CategoryProductsScreen
import com.carshop.android.ui.browse.detail.ProductDetailScreen
import com.carshop.android.ui.browse.home.HomeScreen
import com.carshop.android.ui.checkout.confirm.OrderConfirmScreen
import com.carshop.android.ui.checkout.detail.OrderDetailScreen
import com.carshop.android.ui.checkout.list.OrderListScreen

// 10 把 OrderConfirm / OrderDetail / OrderList 三处占位替换成真 Screen
// About 仍是占位
@Composable
fun CarshopNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Routes.Home.path,
        modifier = modifier,
    ) {
        composable(Routes.Home.path) { HomeScreen(navController) }
        composable(Routes.CategoryProducts.path) { CategoryProductsScreen(navController) }
        composable(Routes.ProductDetail.path) { ProductDetailScreen(navController) }
        composable(Routes.OrderConfirm.path) { OrderConfirmScreen(navController) }
        composable(Routes.OrderDetail.path) { OrderDetailScreen(navController) }
        composable(Routes.OrderList.path) { OrderListScreen(navController) }
        composable(Routes.About.path) {
            ComingSoon("车机商店 Demo · v0.1 · 2026 · powered by carshop.hearagain.space")
        }
    }
}

@Composable
private fun ComingSoon(label: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Coming Soon\n$label",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
