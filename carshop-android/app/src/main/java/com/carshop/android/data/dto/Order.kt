package com.carshop.android.data.dto

import com.squareup.moshi.Json

data class Order(
    // SPEC §13:订单号是 O+18位字符串,不暴露自增 ID
    val id: String,
    @Json(name = "device_id") val deviceId: String,
    // "pending" | "paid"
    val status: String,
    @Json(name = "total_amount") val totalAmount: Int,
    @Json(name = "shipping_info") val shippingInfo: ShippingInfo,
    val items: List<OrderItem>,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "paid_at") val paidAt: String?,
)

data class ShippingInfo(
    val name: String,
    val phone: String,
    val address: String,
)

data class OrderItem(
    val id: Int,
    // SPEC §5:商品可能被删,product_id 允许 null,product_snapshot 保留快照
    @Json(name = "product_id") val productId: Int?,
    @Json(name = "product_snapshot") val productSnapshot: ProductSnapshot,
    val quantity: Int,
    val price: Int,
)

data class ProductSnapshot(
    val id: Int,
    val title: String,
    @Json(name = "product_type") val productType: String,
    val price: Int,
    val spec: String?,
    @Json(name = "main_image_url") val mainImageUrl: String,
)

// POST /api/v1/orders body
data class CreateOrderRequest(
    @Json(name = "product_id") val productId: Int,
    val quantity: Int,
)
