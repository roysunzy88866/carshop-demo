package com.carshop.android.nav

// SPEC §6.1 公开接口 + 设计稿对应的 6 个页面 route。
// Rail 上只露 Home / OrderList / About 3 个根入口(spec §1.4 钦定),
// 其余 4 个是 NavHost 内部跳转目标(09/10 填内容时直接 nav.navigate)。
sealed class Routes(val path: String) {
    object Home : Routes("home")
    object CategoryProducts : Routes("category/{categoryId}") {
        fun build(categoryId: Int) = "category/$categoryId"
    }
    object ProductDetail : Routes("product/{productId}") {
        fun build(productId: Int) = "product/$productId"
    }
    object OrderConfirm : Routes("order/confirm/{productId}") {
        fun build(productId: Int) = "order/confirm/$productId"
    }
    object OrderDetail : Routes("order/detail/{orderId}") {
        fun build(orderId: String) = "order/detail/$orderId"
    }
    object OrderList : Routes("orders")
    object About : Routes("about")
}
